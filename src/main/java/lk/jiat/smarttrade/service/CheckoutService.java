package lk.jiat.smarttrade.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lk.jiat.smarttrade.entity.*;
import lk.jiat.smarttrade.util.AppUtil;
import lk.jiat.smarttrade.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.time.format.DateTimeFormatter;
import java.util.*;

public class CheckoutService {

    private static final DateTimeFormatter RECEIPT_DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy · hh:mm a");
    private final StripeService stripeService = new StripeService();

    /**
     * Calculates order subtotal, delivery fee, and creates a Stripe PaymentIntent.
     */
    public String createPaymentIntent(String requestJson, User sessionUser) {
        JsonObject result = new JsonObject();
        try {
            JsonObject body = AppUtil.GSON.fromJson(requestJson, JsonObject.class);
            if (body == null || !body.has("items") || body.getAsJsonArray("items").isEmpty()) {
                result.addProperty("status", "error");
                result.addProperty("message", "No items to checkout.");
                return AppUtil.GSON.toJson(result);
            }

            JsonArray itemsArr = body.getAsJsonArray("items");
            int deliveryTypeId = body.has("deliveryTypeId") ? body.get("deliveryTypeId").getAsInt() : 1;
            JsonObject shipping = body.has("shipping") ? body.getAsJsonObject("shipping") : new JsonObject();

            double subtotal = 0.0;
            double deliveryFee = 1000.0;
            int totalUnits = 0;

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                // Verify delivery type
                DeliveryType dt = session.get(DeliveryType.class, deliveryTypeId);
                if (dt != null && dt.getPrice() != null) {
                    deliveryFee = dt.getPrice();
                }

                // Verify each item's stock and price
                for (JsonElement el : itemsArr) {
                    JsonObject it = el.getAsJsonObject();
                    int stockId = it.get("stockId").getAsInt();
                    int qty = it.has("qty") ? it.get("qty").getAsInt() : 1;
                    if (qty <= 0) qty = 1;

                    Stock stock = session.get(Stock.class, stockId);
                    if (stock == null) {
                        result.addProperty("status", "error");
                        result.addProperty("message", "Stock item #" + stockId + " not found.");
                        return AppUtil.GSON.toJson(result);
                    }
                    if (stock.getQty() < qty) {
                        result.addProperty("status", "error");
                        String pTitle = (stock.getProduct() != null) ? stock.getProduct().getTitle() : "Product";
                        result.addProperty("message", "Only " + stock.getQty() + " unit(s) available for " + pTitle);
                        return AppUtil.GSON.toJson(result);
                    }

                    subtotal += stock.getPrice() * qty;
                    totalUnits += qty;
                }
            }

            double grandTotal = subtotal + deliveryFee;
            long amountInCents = Math.round(grandTotal * 100.0);

            String custEmail = shipping.has("email") ? shipping.get("email").getAsString() : (sessionUser != null ? sessionUser.getEmail() : "");
            String custName = (shipping.has("firstName") ? shipping.get("firstName").getAsString() : "") + " " +
                    (shipping.has("lastName") ? shipping.get("lastName").getAsString() : "");
            custName = custName.trim();
            if (custName.isEmpty() && sessionUser != null) {
                custName = sessionUser.getFirstName() + " " + sessionUser.getLastName();
            }

            Map<String, String> meta = new HashMap<>();
            meta.put("customer_name", custName);
            meta.put("customer_email", custEmail);
            meta.put("total_units", String.valueOf(totalUnits));

            JsonObject intentRes = stripeService.createPaymentIntent(amountInCents, StripeService.getCurrency(), custEmail, custName, meta);

            if (!"success".equalsIgnoreCase(intentRes.get("status").getAsString())) {
                return AppUtil.GSON.toJson(intentRes);
            }

            result.addProperty("status", "success");
            result.addProperty("clientSecret", intentRes.get("clientSecret").getAsString());
            result.addProperty("paymentIntentId", intentRes.get("id").getAsString());
            result.addProperty("amount", amountInCents);
            result.addProperty("subtotal", subtotal);
            result.addProperty("deliveryFee", deliveryFee);
            result.addProperty("total", grandTotal);
            result.addProperty("currency", StripeService.getCurrency());
            result.addProperty("isMock", intentRes.has("isMock") && intentRes.get("isMock").getAsBoolean());
            result.addProperty("publishableKey", StripeService.getPublishableKey());

            return AppUtil.GSON.toJson(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("status", "error");
            result.addProperty("message", "Failed to initiate payment: " + e.getMessage());
            return AppUtil.GSON.toJson(result);
        }
    }

    /**
     * Confirms the payment, creates Order and OrderItems, decrements stock, and returns the full receipt.
     */
    public String confirmOrder(String requestJson, User sessionUser) {
        JsonObject result = new JsonObject();
        try {
            JsonObject body = AppUtil.GSON.fromJson(requestJson, JsonObject.class);
            if (body == null || !body.has("items") || body.getAsJsonArray("items").isEmpty()) {
                result.addProperty("status", "error");
                result.addProperty("message", "No items in order.");
                return AppUtil.GSON.toJson(result);
            }

            String paymentIntentId = body.has("paymentIntentId") ? body.get("paymentIntentId").getAsString() : "";
            if (paymentIntentId.isBlank()) {
                result.addProperty("status", "error");
                result.addProperty("message", "Payment verification token missing.");
                return AppUtil.GSON.toJson(result);
            }

            // Verify with Stripe or sandbox simulator
            if (!stripeService.verifyPayment(paymentIntentId)) {
                result.addProperty("status", "error");
                result.addProperty("message", "Payment could not be confirmed with Stripe.");
                return AppUtil.GSON.toJson(result);
            }

            JsonArray itemsArr = body.getAsJsonArray("items");
            int deliveryTypeId = body.has("deliveryTypeId") ? body.get("deliveryTypeId").getAsInt() : 1;
            JsonObject shipping = body.has("shipping") ? body.getAsJsonObject("shipping") : new JsonObject();

            int createdOrderId = 0;

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                Transaction tx = session.beginTransaction();

                try {
                    // Resolve user
                    User orderUser = null;
                    if (sessionUser != null) {
                        orderUser = session.get(User.class, sessionUser.getId());
                    }

                    String firstName = shipping.has("firstName") ? shipping.get("firstName").getAsString().trim() : "";
                    String lastName = shipping.has("lastName") ? shipping.get("lastName").getAsString().trim() : "";
                    String email = shipping.has("email") ? shipping.get("email").getAsString().trim() : "";
                    String mobile = shipping.has("mobile") ? shipping.get("mobile").getAsString().trim() : "";
                    String lineOne = shipping.has("lineOne") ? shipping.get("lineOne").getAsString().trim() : "";
                    String lineTwo = shipping.has("lineTwo") ? shipping.get("lineTwo").getAsString().trim() : "";
                    String postalCode = shipping.has("postalCode") ? shipping.get("postalCode").getAsString().trim() : "";
                    int cityId = shipping.has("cityId") ? shipping.get("cityId").getAsInt() : 0;

                    String cityName = "";
                    City city = null;
                    if (cityId > 0) {
                        city = session.get(City.class, cityId);
                        if (city != null) cityName = city.getName();
                    }

                    if (orderUser == null && !email.isBlank()) {
                        // Check if user exists by email
                        List<User> existing = session.createNamedQuery("User.getByEmail", User.class)
                                .setParameter("email", email)
                                .getResultList();
                        if (!existing.isEmpty()) {
                            orderUser = existing.get(0);
                        } else {
                            // Create customer user record
                            Status activeStatus = session.createQuery("FROM Status WHERE value = :val", Status.class)
                                    .setParameter("val", "ACTIVE")
                                    .uniqueResult();
                            if (activeStatus == null) {
                                activeStatus = session.get(Status.class, 1);
                            }

                            User newUser = new User();
                            newUser.setFirstName(!firstName.isEmpty() ? firstName : "Customer");
                            newUser.setLastName(!lastName.isEmpty() ? lastName : "");
                            newUser.setEmail(email);
                            newUser.setPassword(UUID.randomUUID().toString().substring(0, 10));
                            newUser.setVerificationCode("VERIFIED");
                            newUser.setStatus(activeStatus);
                            session.save(newUser);
                            orderUser = newUser;
                        }
                    }

                    // Resolve Delivery Type
                    DeliveryType deliveryType = session.get(DeliveryType.class, deliveryTypeId);
                    if (deliveryType == null) {
                        deliveryType = session.createQuery("FROM DeliveryType", DeliveryType.class).setMaxResults(1).uniqueResult();
                    }
                    double deliveryFee = (deliveryType != null && deliveryType.getPrice() != null) ? deliveryType.getPrice() : 1000.0;

                    // Resolve Status
                    Status orderStatus = session.createQuery("FROM Status WHERE value = :val", Status.class)
                            .setParameter("val", "COMPLETED")
                            .uniqueResult();
                    if (orderStatus == null) {
                        orderStatus = session.createQuery("FROM Status WHERE value = :val", Status.class)
                                .setParameter("val", "APPROVED")
                                .uniqueResult();
                    }
                    if (orderStatus == null) {
                        orderStatus = session.get(Status.class, 1);
                    }

                    // Format full shipping address string
                    StringBuilder addrBuilder = new StringBuilder();
                    if (!lineOne.isEmpty()) addrBuilder.append(lineOne);
                    if (!lineTwo.isEmpty()) addrBuilder.append(", ").append(lineTwo);
                    if (!cityName.isEmpty()) addrBuilder.append(", ").append(cityName);
                    if (!postalCode.isEmpty()) addrBuilder.append(" ").append(postalCode);
                    String fullAddress = addrBuilder.toString();

                    String custFullName = (firstName + " " + lastName).trim();
                    if (custFullName.isEmpty() && orderUser != null) {
                        custFullName = orderUser.getFirstName() + " " + orderUser.getLastName();
                    }

                    // Create Order
                    Order order = new Order();
                    order.setUser(orderUser);
                    order.setDeliveryType(deliveryType);
                    order.setStatus(orderStatus);
                    order.setCustomerName(custFullName);
                    order.setCustomerEmail(!email.isEmpty() ? email : (orderUser != null ? orderUser.getEmail() : ""));
                    order.setCustomerMobile(mobile);
                    order.setShippingAddress(fullAddress);
                    order.setStripePaymentId(paymentIntentId);

                    double subtotal = 0.0;

                    // Validate stocks and prepare items
                    List<OrderItem> savedItems = new ArrayList<>();
                    for (JsonElement el : itemsArr) {
                        JsonObject it = el.getAsJsonObject();
                        int stockId = it.get("stockId").getAsInt();
                        int qty = it.has("qty") ? it.get("qty").getAsInt() : 1;
                        if (qty <= 0) qty = 1;

                        Stock stock = session.get(Stock.class, stockId);
                        if (stock == null) {
                            tx.rollback();
                            result.addProperty("status", "error");
                            result.addProperty("message", "Stock item #" + stockId + " not found.");
                            return AppUtil.GSON.toJson(result);
                        }

                        // Decrement stock qty
                        stock.setQty(Math.max(0, stock.getQty() - qty));
                        session.update(stock);

                        OrderItem orderItem = new OrderItem();
                        orderItem.setOrder(order);
                        orderItem.setStock(stock);
                        orderItem.setQty(qty);
                        orderItem.setRating(0);
                        savedItems.add(orderItem);

                        subtotal += stock.getPrice() * qty;
                    }

                    order.setTotalAmount(subtotal + deliveryFee);
                    session.save(order);

                    for (OrderItem oi : savedItems) {
                        oi.setOrder(order);
                        session.save(oi);
                    }

                    // Clear user's saved cart in database upon successful order
                    if (orderUser != null) {
                        session.createQuery("DELETE FROM Cart c WHERE c.user.id = :uid")
                                .setParameter("uid", orderUser.getId())
                                .executeUpdate();
                    }

                    tx.commit();
                    createdOrderId = order.getId();

                } catch (Exception e) {
                    tx.rollback();
                    throw e;
                }
            }

            // Generate full receipt
            return getReceipt(createdOrderId);

        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("status", "error");
            result.addProperty("message", "Failed to place order: " + e.getMessage());
            return AppUtil.GSON.toJson(result);
        }
    }

    /**
     * Builds a comprehensive receipt JSON for any given Order ID.
     */
    public String getReceipt(int orderId) {
        JsonObject result = new JsonObject();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Order order = session.get(Order.class, orderId);
            if (order == null) {
                result.addProperty("status", "error");
                result.addProperty("message", "Order not found.");
                return AppUtil.GSON.toJson(result);
            }

            result.addProperty("status", "success");
            result.addProperty("orderId", order.getId());
            result.addProperty("orderNumber", String.format("ST-ORD-%05d", order.getId()));
            result.addProperty("createdAt", order.getCreatedAt() != null ? order.getCreatedAt().format(RECEIPT_DATE_FMT) : "—");
            result.addProperty("orderStatus", order.getStatus() != null ? order.getStatus().getValue() : "PAID");
            result.addProperty("stripePaymentId", order.getStripePaymentId() != null ? order.getStripePaymentId() : "—");
            result.addProperty("paymentMethod", "Stripe (Card Payment)");

            // Customer details
            result.addProperty("customerName", order.getCustomerName() != null ? order.getCustomerName()
                    : (order.getUser() != null ? order.getUser().getFirstName() + " " + order.getUser().getLastName() : "Valued Customer"));
            result.addProperty("customerEmail", order.getCustomerEmail() != null ? order.getCustomerEmail()
                    : (order.getUser() != null ? order.getUser().getEmail() : "—"));
            result.addProperty("customerMobile", order.getCustomerMobile() != null ? order.getCustomerMobile() : "—");
            result.addProperty("shippingAddress", order.getShippingAddress() != null ? order.getShippingAddress() : "—");

            // Delivery info
            String delName = (order.getDeliveryType() != null) ? order.getDeliveryType().getName() : "Standard Delivery";
            double delFee = (order.getDeliveryType() != null && order.getDeliveryType().getPrice() != null) ? order.getDeliveryType().getPrice() : 1000.0;
            result.addProperty("deliveryType", delName);
            result.addProperty("deliveryFee", delFee);

            // Itemized breakdown
            JsonArray itemsArray = new JsonArray();
            double subtotal = 0.0;
            int totalUnits = 0;

            if (order.getOrderItems() != null) {
                for (OrderItem oi : order.getOrderItems()) {
                    JsonObject itemObj = new JsonObject();
                    itemObj.addProperty("orderItemId", oi.getId());
                    itemObj.addProperty("qty", oi.getQty());
                    totalUnits += oi.getQty();

                    if (oi.getStock() != null) {
                        Stock st = oi.getStock();
                        double unitPrice = st.getPrice();
                        double lineTotal = unitPrice * oi.getQty();
                        subtotal += lineTotal;

                        itemObj.addProperty("stockId", st.getId());
                        itemObj.addProperty("unitPrice", unitPrice);
                        itemObj.addProperty("lineTotal", lineTotal);

                        if (st.getProduct() != null) {
                            Product p = st.getProduct();
                            itemObj.addProperty("productId", p.getId());
                            itemObj.addProperty("title", p.getTitle());
                            itemObj.addProperty("brand", (p.getModel() != null && p.getModel().getBrand() != null) ? p.getModel().getBrand().getName() : "");
                            itemObj.addProperty("model", p.getModel() != null ? p.getModel().getName() : "");
                            itemObj.addProperty("color", p.getColor() != null ? p.getColor().getValue() : "");
                            itemObj.addProperty("storage", p.getStorage() != null ? p.getStorage().getValue() : "");
                            itemObj.addProperty("quality", p.getQuality() != null ? p.getQuality().getValue() : "");

                            String img = (p.getImages() != null && !p.getImages().isEmpty()) ? p.getImages().get(0) : "assets/images/placeholder.png";
                            itemObj.addProperty("image", img);
                        } else {
                            itemObj.addProperty("title", "SmartTrade Item");
                            itemObj.addProperty("image", "assets/images/placeholder.png");
                        }
                    } else {
                        itemObj.addProperty("title", "Item");
                        itemObj.addProperty("unitPrice", 0.0);
                        itemObj.addProperty("lineTotal", 0.0);
                        itemObj.addProperty("image", "assets/images/placeholder.png");
                    }
                    itemsArray.add(itemObj);
                }
            }

            result.add("items", itemsArray);
            result.addProperty("itemsCount", totalUnits);
            result.addProperty("subtotal", subtotal);
            double grandTotal = order.getTotalAmount() != null ? order.getTotalAmount() : (subtotal + delFee);
            result.addProperty("total", grandTotal);

            return AppUtil.GSON.toJson(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("status", "error");
            result.addProperty("message", "Error fetching receipt: " + e.getMessage());
            return AppUtil.GSON.toJson(result);
        }
    }
}
