package lk.jiat.smarttrade.controller.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lk.jiat.smarttrade.dto.UserDTO;
import lk.jiat.smarttrade.entity.*;
import lk.jiat.smarttrade.service.CheckoutService;
import lk.jiat.smarttrade.service.ContentService;
import lk.jiat.smarttrade.util.AppUtil;
import lk.jiat.smarttrade.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Path("/admin")
public class AdminController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private boolean isAuthenticated(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) return false;
        Object adminObj = session.getAttribute("adminUser");
        return adminObj != null;
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(String jsonData, @Context HttpServletRequest request) {
        JsonObject response = new JsonObject();
        try {
            UserDTO userDTO = AppUtil.GSON.fromJson(jsonData, UserDTO.class);
            if (userDTO == null || userDTO.getEmail() == null || userDTO.getPassword() == null) {
                response.addProperty("status", "error");
                response.addProperty("message", "Email and password are required.");
                return Response.status(Response.Status.BAD_REQUEST).entity(AppUtil.GSON.toJson(response)).build();
            }

            try (Session session = HibernateUtil.getSessionFactory().openSession()) {
                User admin = session.createQuery("FROM User WHERE LOWER(email) = LOWER(:email)", User.class)
                        .setParameter("email", userDTO.getEmail().trim())
                        .uniqueResult();

                // Self-healing: if canonical admin@gmail.com is present, ensure isAdmin is true
                if (admin != null && "admin@gmail.com".equalsIgnoreCase(admin.getEmail()) && !admin.isAdmin()) {
                    Transaction tx = session.beginTransaction();
                    admin.setAdmin(true);
                    session.update(admin);
                    tx.commit();
                }

                if (admin != null && admin.getPassword().equals(userDTO.getPassword().trim()) && admin.isAdmin()) {
                    HttpSession oldSession = request.getSession(false);
                    if (oldSession != null) {
                        oldSession.invalidate();
                    }
                    HttpSession httpSession = request.getSession(true);
                    httpSession.setAttribute("adminUser", admin);

                    JsonObject adminJson = new JsonObject();
                    adminJson.addProperty("id", admin.getId());
                    adminJson.addProperty("name", admin.getFirstName() + " " + admin.getLastName());
                    adminJson.addProperty("email", admin.getEmail());

                    response.addProperty("status", "success");
                    response.addProperty("message", "Login successful");
                    response.add("admin", adminJson);
                    return Response.ok(AppUtil.GSON.toJson(response)).build();
                } else {
                    response.addProperty("status", "error");
                    response.addProperty("message", "Invalid admin email or password.");
                    return Response.status(Response.Status.UNAUTHORIZED).entity(AppUtil.GSON.toJson(response)).build();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            response.addProperty("status", "error");
            response.addProperty("message", "Internal server error: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(AppUtil.GSON.toJson(response)).build();
        }
    }

    @GET
    @Path("/me")
    @Produces(MediaType.APPLICATION_JSON)
    public Response me(@Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        User admin = (User) request.getSession(false).getAttribute("adminUser");
        JsonObject obj = new JsonObject();
        obj.addProperty("id", admin.getId());
        obj.addProperty("name", admin.getFirstName() + " " + admin.getLastName());
        obj.addProperty("email", admin.getEmail());
        return Response.ok(AppUtil.GSON.toJson(obj)).build();
    }

    @GET
    @Path("/stats")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStats(@Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Long usersCount = (Long) session.createQuery("SELECT count(u) FROM User u WHERE u.isAdmin = false").uniqueResult();
            Long sellersCount = (Long) session.createQuery("SELECT count(s) FROM Seller s").uniqueResult();
            Long pendingSellersCount = (Long) session.createQuery("SELECT count(s) FROM Seller s WHERE s.status.value = 'PENDING'").uniqueResult();
            Long productsCount = (Long) session.createQuery("SELECT count(p) FROM Product p").uniqueResult();
            Long ordersCount = (Long) session.createQuery("SELECT count(o) FROM Order o").uniqueResult();

            JsonObject stats = new JsonObject();
            stats.addProperty("usersCount", usersCount != null ? usersCount : 0L);
            stats.addProperty("sellersCount", sellersCount != null ? sellersCount : 0L);
            stats.addProperty("pendingSellersCount", pendingSellersCount != null ? pendingSellersCount : 0L);
            stats.addProperty("productsCount", productsCount != null ? productsCount : 0L);
            stats.addProperty("ordersCount", ordersCount != null ? ordersCount : 0L);

            // Calculate real orders, revenue and month-by-month breakdown
            List<Order> allOrders = session.createQuery("FROM Order o ORDER BY o.id DESC", Order.class).getResultList();
            double totalRev = 0.0;
            Map<YearMonth, Double> monthlyMap = new TreeMap<>();
            DateTimeFormatter mFmt = DateTimeFormatter.ofPattern("MMM yyyy");

            for (Order o : allOrders) {
                double ordTotal = 0.0;
                if (o.getOrderItems() != null) {
                    for (OrderItem oi : o.getOrderItems()) {
                        if (oi.getStock() != null) {
                            ordTotal += oi.getQty() * oi.getStock().getPrice();
                        }
                    }
                }
                totalRev += ordTotal;
                if (ordTotal > 0) {
                    YearMonth ym = o.getCreatedAt() != null ? YearMonth.from(o.getCreatedAt()) : YearMonth.now();
                    monthlyMap.put(ym, monthlyMap.getOrDefault(ym, 0.0) + ordTotal);
                }
            }

            stats.addProperty("totalRevenue", totalRev);
            stats.addProperty("totalExpenses", totalRev * 0.35);
            stats.addProperty("totalProfit", totalRev * 0.65);

            // Monthly revenue array: ONLY includes months that have actual revenue > 0
            JsonArray monthlyArr = new JsonArray();
            for (Map.Entry<YearMonth, Double> entry : monthlyMap.entrySet()) {
                if (entry.getValue() > 0) {
                    JsonObject mObj = new JsonObject();
                    mObj.addProperty("month", entry.getKey().format(mFmt));
                    mObj.addProperty("revenue", entry.getValue());
                    monthlyArr.add(mObj);
                }
            }
            stats.add("monthlyRevenue", monthlyArr);

            // Recent customers (latest 5)
            List<User> recentUsers = session.createQuery("FROM User u WHERE u.isAdmin = false ORDER BY u.id DESC", User.class)
                    .setMaxResults(5)
                    .getResultList();
            JsonArray recentCustArr = new JsonArray();
            for (User u : recentUsers) {
                JsonObject r = new JsonObject();
                r.addProperty("id", u.getId());
                r.addProperty("name", u.getFirstName() + " " + u.getLastName());
                r.addProperty("email", u.getEmail());
                r.addProperty("status", u.getStatus() != null ? u.getStatus().getValue() : "UNKNOWN");
                r.addProperty("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().format(DATE_FMT) : "—");
                recentCustArr.add(r);
            }
            stats.add("recentCustomers", recentCustArr);

            // Recent orders (latest 5)
            List<Order> recentOrders = session.createQuery("FROM Order o ORDER BY o.id DESC", Order.class)
                    .setMaxResults(5)
                    .getResultList();
            JsonArray recentOrdArr = new JsonArray();
            for (Order o : recentOrders) {
                JsonObject r = new JsonObject();
                r.addProperty("id", o.getId());
                r.addProperty("customer", o.getUser() != null ? o.getUser().getFirstName() + " " + o.getUser().getLastName() : "Guest");
                r.addProperty("status", o.getStatus() != null ? o.getStatus().getValue() : "PENDING");
                r.addProperty("createdAt", o.getCreatedAt() != null ? o.getCreatedAt().format(DATE_FMT) : "—");
                double ordTotal = 0;
                if (o.getOrderItems() != null) {
                    for (OrderItem oi : o.getOrderItems()) {
                        if (oi.getStock() != null) ordTotal += oi.getQty() * oi.getStock().getPrice();
                    }
                }
                r.addProperty("total", ordTotal);
                recentOrdArr.add(r);
            }
            stats.add("recentOrders", recentOrdArr);

            return Response.ok(AppUtil.GSON.toJson(stats)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/revenue")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getRevenueReport(@Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Order> orders = session.createQuery("FROM Order o ORDER BY o.id DESC", Order.class).getResultList();

            double totalRev = 0.0;
            int totalOrders = orders.size();

            Map<Integer, JsonObject> productRevMap = new LinkedHashMap<>();
            Map<Integer, JsonObject> sellerRevMap = new LinkedHashMap<>();
            Map<Integer, JsonObject> customerRevMap = new LinkedHashMap<>();
            Map<YearMonth, double[]> monthRevMap = new TreeMap<>();

            DateTimeFormatter monthFmt = DateTimeFormatter.ofPattern("MMM yyyy");

            for (Order o : orders) {
                double orderTotal = 0.0;
                YearMonth ym = o.getCreatedAt() != null ? YearMonth.from(o.getCreatedAt()) : YearMonth.now();

                User u = o.getUser();
                int custId = u != null ? u.getId() : 0;
                String custName = u != null ? u.getFirstName() + " " + u.getLastName() : "Guest";
                String custEmail = u != null ? u.getEmail() : "—";

                Set<Integer> sellersInThisOrder = new HashSet<>();

                if (o.getOrderItems() != null) {
                    for (OrderItem oi : o.getOrderItems()) {
                        int qty = oi.getQty();
                        double price = oi.getStock() != null ? oi.getStock().getPrice() : 0.0;
                        double itemTotal = qty * price;
                        orderTotal += itemTotal;

                        if (oi.getStock() != null && oi.getStock().getProduct() != null) {
                            Product p = oi.getStock().getProduct();
                            int pId = p.getId();
                            JsonObject pObj = productRevMap.computeIfAbsent(pId, k -> {
                                JsonObject obj = new JsonObject();
                                obj.addProperty("id", p.getId());
                                obj.addProperty("title", p.getTitle());
                                obj.addProperty("seller", p.getSeller() != null ? p.getSeller().getCompanyName() : "SmartTrade");
                                obj.addProperty("unitsSold", 0);
                                obj.addProperty("revenue", 0.0);
                                return obj;
                            });
                            pObj.addProperty("unitsSold", pObj.get("unitsSold").getAsInt() + qty);
                            pObj.addProperty("revenue", pObj.get("revenue").getAsDouble() + itemTotal);

                            if (p.getSeller() != null) {
                                Seller sl = p.getSeller();
                                int slId = sl.getId();
                                sellersInThisOrder.add(slId);
                                JsonObject sObj = sellerRevMap.computeIfAbsent(slId, k -> {
                                    JsonObject obj = new JsonObject();
                                    obj.addProperty("id", sl.getId());
                                    obj.addProperty("companyName", sl.getCompanyName());
                                    obj.addProperty("unitsSold", 0);
                                    obj.addProperty("revenue", 0.0);
                                    obj.addProperty("ordersCount", 0);
                                    return obj;
                                });
                                sObj.addProperty("unitsSold", sObj.get("unitsSold").getAsInt() + qty);
                                sObj.addProperty("revenue", sObj.get("revenue").getAsDouble() + itemTotal);
                            }
                        }
                    }
                }

                totalRev += orderTotal;

                for (int sId : sellersInThisOrder) {
                    JsonObject sObj = sellerRevMap.get(sId);
                    if (sObj != null) {
                        sObj.addProperty("ordersCount", sObj.get("ordersCount").getAsInt() + 1);
                    }
                }

                if (custId > 0) {
                    JsonObject cObj = customerRevMap.computeIfAbsent(custId, k -> {
                        JsonObject obj = new JsonObject();
                        obj.addProperty("id", custId);
                        obj.addProperty("name", custName);
                        obj.addProperty("email", custEmail);
                        obj.addProperty("ordersCount", 0);
                        obj.addProperty("revenue", 0.0);
                        return obj;
                    });
                    cObj.addProperty("ordersCount", cObj.get("ordersCount").getAsInt() + 1);
                    cObj.addProperty("revenue", cObj.get("revenue").getAsDouble() + orderTotal);
                }

                if (orderTotal > 0) {
                    monthRevMap.computeIfAbsent(ym, k -> new double[]{0.0, 0.0});
                    monthRevMap.get(ym)[0] += orderTotal;
                    monthRevMap.get(ym)[1] += 1.0;
                }
            }

            JsonObject response = new JsonObject();
            response.addProperty("totalRevenue", totalRev);
            response.addProperty("totalOrders", totalOrders);
            double avg = totalOrders > 0 ? (totalRev / totalOrders) : 0.0;
            response.addProperty("avgOrderValue", avg);

            List<JsonObject> prodList = new ArrayList<>(productRevMap.values());
            prodList.sort((a, b) -> Double.compare(b.get("revenue").getAsDouble(), a.get("revenue").getAsDouble()));
            JsonArray prodArr = new JsonArray();
            prodList.forEach(prodArr::add);
            response.add("byProduct", prodArr);

            List<JsonObject> sellerList = new ArrayList<>(sellerRevMap.values());
            sellerList.sort((a, b) -> Double.compare(b.get("revenue").getAsDouble(), a.get("revenue").getAsDouble()));
            JsonArray sellerArr = new JsonArray();
            sellerList.forEach(sellerArr::add);
            response.add("bySeller", sellerArr);

            List<JsonObject> custList = new ArrayList<>(customerRevMap.values());
            custList.sort((a, b) -> Double.compare(b.get("revenue").getAsDouble(), a.get("revenue").getAsDouble()));
            JsonArray custArr = new JsonArray();
            custList.forEach(custArr::add);
            response.add("byCustomer", custArr);

            JsonArray monthArr = new JsonArray();
            for (Map.Entry<YearMonth, double[]> entry : monthRevMap.entrySet()) {
                if (entry.getValue()[0] > 0) {
                    JsonObject mObj = new JsonObject();
                    mObj.addProperty("month", entry.getKey().format(monthFmt));
                    mObj.addProperty("revenue", entry.getValue()[0]);
                    mObj.addProperty("ordersCount", (int) entry.getValue()[1]);
                    monthArr.add(mObj);
                }
            }
            response.add("monthlyRevenue", monthArr);

            return Response.ok(AppUtil.GSON.toJson(response)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/customers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCustomers(@Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<User> users = session.createQuery("FROM User WHERE isAdmin = false ORDER BY id DESC", User.class).getResultList();
            JsonArray jsonArray = new JsonArray();
            for (User u : users) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", u.getId());
                obj.addProperty("firstName", u.getFirstName());
                obj.addProperty("lastName", u.getLastName());
                obj.addProperty("email", u.getEmail());
                obj.addProperty("status", u.getStatus() != null ? u.getStatus().getValue() : "ACTIVE");
                obj.addProperty("createdAt", u.getCreatedAt() != null ? u.getCreatedAt().format(DATE_FMT) : "—");
                obj.addProperty("profileImage", u.getProfileImage() != null ? u.getProfileImage() : "");
                jsonArray.add(obj);
            }
            return Response.ok(AppUtil.GSON.toJson(jsonArray)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/customers/{id}/toggle-status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response toggleCustomerStatus(@PathParam("id") int id, @Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user == null || user.isAdmin()) {
                tx.rollback();
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            String currentStatus = user.getStatus() != null ? user.getStatus().getValue() : "ACTIVE";
            String newStatusValue = "BLOCKED".equalsIgnoreCase(currentStatus) ? "VERIFIED" : "BLOCKED";

            Status newStatus = session.createQuery("FROM Status WHERE value = :val", Status.class)
                    .setParameter("val", newStatusValue)
                    .uniqueResult();
            if (newStatus == null) {
                newStatus = new Status();
                newStatus.setValue(newStatusValue);
                session.save(newStatus);
            }

            user.setStatus(newStatus);
            session.update(user);
            tx.commit();

            JsonObject res = new JsonObject();
            res.addProperty("status", "success");
            res.addProperty("newStatus", newStatusValue);
            return Response.ok(AppUtil.GSON.toJson(res)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/sellers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSellers(@Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Seller> sellers = session.createQuery("FROM Seller ORDER BY id DESC", Seller.class).getResultList();
            JsonArray jsonArray = new JsonArray();
            for (Seller s : sellers) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", s.getId());
                obj.addProperty("companyName", s.getCompanyName());
                obj.addProperty("contactNumber", s.getCompanyMobile());
                if (s.getUser() != null) {
                    obj.addProperty("email", s.getUser().getEmail());
                    obj.addProperty("ownerName", s.getUser().getFirstName() + " " + s.getUser().getLastName());
                } else {
                    obj.addProperty("email", s.getCompanyEmail());
                    obj.addProperty("ownerName", "—");
                }
                obj.addProperty("status", s.getStatus() != null ? s.getStatus().getValue() : "PENDING");
                obj.addProperty("createdAt", s.getCreatedAt() != null ? s.getCreatedAt().format(DATE_FMT) : "—");
                obj.addProperty("productsCount", s.getProducts() != null ? s.getProducts().size() : 0);
                jsonArray.add(obj);
            }
            return Response.ok(AppUtil.GSON.toJson(jsonArray)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/sellers/{id}/approve")
    @Produces(MediaType.APPLICATION_JSON)
    public Response approveSeller(@PathParam("id") int id, @Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Seller seller = session.get(Seller.class, id);
            if (seller != null) {
                Status approved = session.createQuery("FROM Status WHERE value = 'APPROVED'", Status.class).uniqueResult();
                if (approved == null) {
                    approved = session.createQuery("FROM Status WHERE value = 'VERIFIED'", Status.class).uniqueResult();
                }
                seller.setStatus(approved);
                session.update(seller);
                tx.commit();
                return Response.ok("{\"status\":\"success\",\"message\":\"Seller approved successfully\"}").build();
            } else {
                tx.rollback();
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/sellers/{id}/reject")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rejectSeller(@PathParam("id") int id, @Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Seller seller = session.get(Seller.class, id);
            if (seller != null) {
                Status rejected = session.createQuery("FROM Status WHERE value = 'REJECTED'", Status.class).uniqueResult();
                seller.setStatus(rejected);
                session.update(seller);
                tx.commit();
                return Response.ok("{\"status\":\"success\",\"message\":\"Seller rejected\"}").build();
            } else {
                tx.rollback();
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/products")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProducts(@Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Product> products = session.createQuery("FROM Product ORDER BY id DESC", Product.class).getResultList();
            JsonArray jsonArray = new JsonArray();
            for (Product p : products) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", p.getId());
                obj.addProperty("title", p.getTitle());

                String brand = p.getModel() != null && p.getModel().getBrand() != null ? p.getModel().getBrand().getName() : "General";
                String model = p.getModel() != null ? p.getModel().getName() : "—";
                obj.addProperty("brand", brand);
                obj.addProperty("model", model);

                String sellerName = p.getSeller() != null ? p.getSeller().getCompanyName() : "SmartTrade";
                obj.addProperty("seller", sellerName);

                double price = 0.0;
                int qty = 0;
                String status = "ACTIVE";
                if (p.getStocks() != null && !p.getStocks().isEmpty()) {
                    Stock firstStock = p.getStocks().iterator().next();
                    price = firstStock.getPrice();
                    qty = firstStock.getQty();
                    if (firstStock.getStatus() != null) {
                        status = firstStock.getStatus().getValue();
                    }
                }
                obj.addProperty("price", price);
                obj.addProperty("qty", qty);
                obj.addProperty("status", status);

                String img = "";
                if (p.getImages() != null && !p.getImages().isEmpty()) {
                    img = p.getImages().get(0);
                }
                obj.addProperty("image", img);
                obj.addProperty("createdAt", p.getCreatedAt() != null ? p.getCreatedAt().format(DATE_FMT) : "—");

                jsonArray.add(obj);
            }
            return Response.ok(AppUtil.GSON.toJson(jsonArray)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/products/{id}/approve")
    @Produces(MediaType.APPLICATION_JSON)
    public Response approveProduct(@PathParam("id") int id, @Context HttpServletRequest request) {
        return updateProductStatusInternal(id, "APPROVED", request);
    }

    @POST
    @Path("/products/{id}/reject")
    @Produces(MediaType.APPLICATION_JSON)
    public Response rejectProduct(@PathParam("id") int id, @Context HttpServletRequest request) {
        return updateProductStatusInternal(id, "REJECTED", request);
    }

    @POST
    @Path("/products/{id}/toggle-status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response toggleProductStatus(@PathParam("id") int id, @Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Product product = session.get(Product.class, id);
            if (product == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"status\":\"error\",\"message\":\"Product not found\"}").build();
            }

            String currentStatus = "APPROVED";
            if (product.getStocks() != null && !product.getStocks().isEmpty()) {
                Stock firstStock = product.getStocks().iterator().next();
                if (firstStock.getStatus() != null) {
                    currentStatus = firstStock.getStatus().getValue();
                }
            }

            String newStatusStr = "REJECTED".equalsIgnoreCase(currentStatus) ? "APPROVED" : "REJECTED";
            return updateProductStatusInternal(id, newStatusStr, request);
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @POST
    @Path("/products/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProductStatus(@PathParam("id") int id, String body, @Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String targetStatus = "APPROVED";
        try {
            JsonObject json = AppUtil.GSON.fromJson(body, JsonObject.class);
            if (json != null && json.has("status")) {
                targetStatus = json.get("status").getAsString().toUpperCase();
            }
        } catch (Exception ignored) {}
        return updateProductStatusInternal(id, targetStatus, request);
    }

    private Response updateProductStatusInternal(int productId, String newStatusStr, HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Product product = session.get(Product.class, productId);
            if (product == null) {
                tx.rollback();
                return Response.status(Response.Status.NOT_FOUND)
                        .entity("{\"status\":\"error\",\"message\":\"Product not found\"}").build();
            }

            Status status = session.createQuery("FROM Status WHERE value = :val", Status.class)
                    .setParameter("val", newStatusStr)
                    .uniqueResult();
            if (status == null) {
                status = new Status();
                status.setValue(newStatusStr);
                session.persist(status);
                session.flush();
            }

            if (product.getStocks() != null) {
                for (Stock s : product.getStocks()) {
                    s.setStatus(status);
                    session.update(s);
                }
            }
            tx.commit();

            JsonObject res = new JsonObject();
            res.addProperty("status", "success");
            res.addProperty("message", "Product status updated to " + newStatusStr);
            res.addProperty("newStatus", newStatusStr);
            return Response.ok(AppUtil.GSON.toJson(res)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/orders")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrders(@Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Order> orders = session.createQuery("FROM Order ORDER BY id DESC", Order.class).getResultList();
            JsonArray jsonArray = new JsonArray();
            for (Order o : orders) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", o.getId());

                String custName = o.getCustomerName();
                if (custName == null || custName.isBlank()) {
                    custName = o.getUser() != null ? o.getUser().getFirstName() + " " + o.getUser().getLastName() : "Guest Customer";
                }
                String custEmail = o.getCustomerEmail();
                if (custEmail == null || custEmail.isBlank()) {
                    custEmail = o.getUser() != null ? o.getUser().getEmail() : "—";
                }

                obj.addProperty("customerName", custName);
                obj.addProperty("customerEmail", custEmail);
                obj.addProperty("customerMobile", o.getCustomerMobile() != null ? o.getCustomerMobile() : "—");
                obj.addProperty("shippingAddress", o.getShippingAddress() != null ? o.getShippingAddress() : "—");
                obj.addProperty("status", o.getStatus() != null ? o.getStatus().getValue() : "PENDING");
                obj.addProperty("deliveryType", o.getDeliveryType() != null ? o.getDeliveryType().getName() : "Standard Delivery");
                double deliveryFee = (o.getDeliveryType() != null && o.getDeliveryType().getPrice() != null) ? o.getDeliveryType().getPrice() : 1000.0;
                obj.addProperty("deliveryFee", deliveryFee);
                obj.addProperty("createdAt", o.getCreatedAt() != null ? o.getCreatedAt().format(DATE_FMT) : "—");
                obj.addProperty("stripePaymentId", o.getStripePaymentId() != null ? o.getStripePaymentId() : "—");

                double subtotal = 0.0;
                int itemsCount = 0;
                JsonArray itemsArr = new JsonArray();
                if (o.getOrderItems() != null) {
                    for (OrderItem oi : o.getOrderItems()) {
                        itemsCount += oi.getQty();
                        JsonObject it = new JsonObject();
                        it.addProperty("id", oi.getId());
                        it.addProperty("qty", oi.getQty());
                        if (oi.getStock() != null) {
                            Stock st = oi.getStock();
                            double price = st.getPrice();
                            subtotal += price * oi.getQty();
                            it.addProperty("price", price);
                            it.addProperty("total", price * oi.getQty());
                            if (st.getProduct() != null) {
                                Product pr = st.getProduct();
                                it.addProperty("title", pr.getTitle());
                                String img = (pr.getImages() != null && !pr.getImages().isEmpty()) ? pr.getImages().get(0) : "assets/images/placeholder.png";
                                it.addProperty("image", img);
                                it.addProperty("brand", (pr.getModel() != null && pr.getModel().getBrand() != null) ? pr.getModel().getBrand().getName() : "");
                                it.addProperty("model", pr.getModel() != null ? pr.getModel().getName() : "");
                                it.addProperty("storage", pr.getStorage() != null ? pr.getStorage().getValue() : "");
                                it.addProperty("color", pr.getColor() != null ? pr.getColor().getValue() : "");
                            }
                        }
                        itemsArr.add(it);
                    }
                }
                double grandTotal = (o.getTotalAmount() != null && o.getTotalAmount() > 0) ? o.getTotalAmount() : (subtotal + deliveryFee);
                obj.addProperty("subtotal", subtotal);
                obj.addProperty("total", grandTotal);
                obj.addProperty("itemsCount", itemsCount);
                obj.add("items", itemsArr);
                jsonArray.add(obj);
            }
            return Response.ok(AppUtil.GSON.toJson(jsonArray)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/orders/{id}/receipt")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getOrderReceipt(@PathParam("id") int id, @Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String receiptJson = new CheckoutService().getReceipt(id);
        return Response.ok(receiptJson).build();
    }

    @POST
    @Path("/orders/{id}/status")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateOrderStatus(@PathParam("id") int id, String jsonBody, @Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Order order = session.get(Order.class, id);
            if (order == null) {
                tx.rollback();
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            JsonObject body = AppUtil.GSON.fromJson(jsonBody, JsonObject.class);
            String statusValue = body.has("status") ? body.get("status").getAsString() : "COMPLETED";

            Status status = session.createQuery("FROM Status WHERE value = :val", Status.class)
                    .setParameter("val", statusValue)
                    .uniqueResult();
            if (status != null) {
                order.setStatus(status);
                session.update(order);
                tx.commit();
                return Response.ok("{\"status\":\"success\",\"message\":\"Order updated\"}").build();
            } else {
                tx.rollback();
                return Response.status(Response.Status.BAD_REQUEST).entity("{\"status\":\"error\",\"message\":\"Invalid status\"}").build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @GET
    @Path("/brands-overview")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getBrandsOverview(@Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"status\":\"error\",\"message\":\"Unauthorized\"}").build();
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Brand> brands = session.createQuery("FROM Brand b ORDER BY b.name ASC", Brand.class).getResultList();
            com.google.gson.JsonArray arr = new com.google.gson.JsonArray();

            List<Object[]> productCounts = session.createQuery(
                    "SELECT p.model.brand.id, COUNT(p) FROM Product p GROUP BY p.model.brand.id",
                    Object[].class
            ).getResultList();

            java.util.Map<Integer, Long> brandProductCounts = productCounts.stream()
                    .collect(java.util.stream.Collectors.toMap(
                            row -> (Integer) row[0],
                            row -> (Long) row[1]
                    ));

            for (Brand b : brands) {
                JsonObject bObj = new JsonObject();
                bObj.addProperty("id", b.getId());
                bObj.addProperty("name", b.getName());

                List<Model> models = session.createQuery("FROM Model m WHERE m.brand.id = :bid ORDER BY m.name ASC", Model.class)
                        .setParameter("bid", b.getId())
                        .getResultList();
                bObj.addProperty("modelsCount", models.size());

                com.google.gson.JsonArray mArr = new com.google.gson.JsonArray();
                for (Model m : models) {
                    JsonObject mObj = new JsonObject();
                    mObj.addProperty("id", m.getId());
                    mObj.addProperty("name", m.getName());
                    mArr.add(mObj);
                }
                bObj.add("models", mArr);

                long productCount = brandProductCounts.getOrDefault(b.getId(), 0L);
                bObj.addProperty("productsCount", productCount);

                arr.add(bObj);
            }
            JsonObject res = new JsonObject();
            res.addProperty("status", "success");
            res.add("brands", arr);
            return Response.ok(AppUtil.GSON.toJson(res)).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
        }
    }

    @DELETE
    @Path("/brands/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteBrand(@PathParam("id") int id, @Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"status\":\"error\",\"message\":\"Unauthorized\"}").build();
        }
        String res = new ContentService().deleteBrand(id);
        return Response.ok(res).build();
    }

    @DELETE
    @Path("/models/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteModel(@PathParam("id") int id, @Context HttpServletRequest request) {
        if (!isAuthenticated(request)) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("{\"status\":\"error\",\"message\":\"Unauthorized\"}").build();
        }
        String res = new ContentService().deleteModel(id);
        return Response.ok(res).build();
    }

    @POST
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@Context HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return Response.ok("{\"status\":\"success\",\"message\":\"Logged out\"}").build();
    }
}
