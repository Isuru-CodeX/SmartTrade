package lk.jiat.smarttrade.controller.api;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lk.jiat.smarttrade.dto.UserDTO;
import lk.jiat.smarttrade.entity.Order;
import lk.jiat.smarttrade.entity.OrderItem;
import lk.jiat.smarttrade.entity.Product;
import lk.jiat.smarttrade.entity.Stock;
import lk.jiat.smarttrade.entity.User;
import lk.jiat.smarttrade.service.UserService;
import lk.jiat.smarttrade.util.AppUtil;
import lk.jiat.smarttrade.util.HibernateUtil;
import org.hibernate.Session;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Path("/users")
public class UserController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMM dd, yyyy · hh:mm a");

    @Path("/logout")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@Context HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession != null) {
            httpSession.removeAttribute("user");
            httpSession.invalidate();
        }
        JsonObject res = new JsonObject();
        res.addProperty("status", "success");
        res.addProperty("message", "Logged out successfully");
        return Response.ok(AppUtil.GSON.toJson(res)).build();
    }

    /** Lightweight "who am I" call used by header and app store to switch user contexts. */
    @Path("/session")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response sessionInfo(@Context HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        HttpSession httpSession = request.getSession(false);
        boolean loggedIn = httpSession != null && httpSession.getAttribute("user") != null;
        responseObject.addProperty("loggedIn", loggedIn);
        if (loggedIn) {
            User user = (User) httpSession.getAttribute("user");
            responseObject.addProperty("userId", user.getId());
            responseObject.addProperty("id", user.getId());
            responseObject.addProperty("name", user.getFirstName() + " " + user.getLastName());
            responseObject.addProperty("email", user.getEmail());
            responseObject.addProperty("profileImage", user.getProfileImage());
            responseObject.addProperty("isAdmin", user.isAdmin());
        }
        return Response.ok().entity(AppUtil.GSON.toJson(responseObject)).build();
    }

    @Path("/orders")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMyOrders(@Context HttpServletRequest request) {
        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"status\":\"unauthorized\",\"message\":\"Please sign in to view order history.\"}")
                    .build();
        }
        User sessionUser = (User) httpSession.getAttribute("user");

        try (Session hibernateSession = HibernateUtil.getSessionFactory().openSession()) {
            List<Order> orders = hibernateSession.createQuery(
                    "FROM Order o WHERE o.user.id = :userId ORDER BY o.id DESC", Order.class)
                    .setParameter("userId", sessionUser.getId())
                    .getResultList();

            JsonArray arr = new JsonArray();
            for (Order o : orders) {
                JsonObject obj = new JsonObject();
                obj.addProperty("id", o.getId());
                obj.addProperty("orderNumber", "#ST-ORD-" + String.format("%05d", o.getId()));
                obj.addProperty("status", o.getStatus() != null ? o.getStatus().getValue() : "PAID");
                obj.addProperty("createdAt", o.getCreatedAt() != null ? o.getCreatedAt().format(DATE_FMT) : "—");
                obj.addProperty("deliveryType", o.getDeliveryType() != null ? o.getDeliveryType().getName() : "Standard Delivery");
                obj.addProperty("shippingAddress", o.getShippingAddress() != null ? o.getShippingAddress() : "—");
                obj.addProperty("stripePaymentId", o.getStripePaymentId() != null ? o.getStripePaymentId() : "—");

                double subtotal = 0.0;
                int itemsCount = 0;
                JsonArray itemsArr = new JsonArray();
                if (o.getOrderItems() != null) {
                    for (OrderItem oi : o.getOrderItems()) {
                        itemsCount += oi.getQty();
                        JsonObject itemObj = new JsonObject();
                        itemObj.addProperty("qty", oi.getQty());
                        Stock st = oi.getStock();
                        if (st != null) {
                            double price = st.getPrice();
                            double total = price * oi.getQty();
                            subtotal += total;
                            itemObj.addProperty("unitPrice", price);
                            itemObj.addProperty("total", total);
                            Product p = st.getProduct();
                            if (p != null) {
                                itemObj.addProperty("productId", p.getId());
                                itemObj.addProperty("title", p.getTitle());
                                String img = (p.getImages() != null && !p.getImages().isEmpty())
                                        ? p.getImages().get(0) : "assets/images/placeholder.png";
                                itemObj.addProperty("image", img);
                            }
                        }
                        itemsArr.add(itemObj);
                    }
                }
                double deliveryFee = (o.getDeliveryType() != null && o.getDeliveryType().getPrice() != null)
                        ? o.getDeliveryType().getPrice() : 1000.0;
                double grandTotal = (o.getTotalAmount() != null && o.getTotalAmount() > 0)
                        ? o.getTotalAmount() : (subtotal + deliveryFee);

                obj.addProperty("subtotal", subtotal);
                obj.addProperty("deliveryFee", deliveryFee);
                obj.addProperty("total", grandTotal);
                obj.addProperty("itemsCount", itemsCount);
                obj.add("items", itemsArr);
                arr.add(obj);
            }

            JsonObject result = new JsonObject();
            result.addProperty("status", "success");
            result.add("orders", arr);
            return Response.ok(AppUtil.GSON.toJson(result)).build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"status\":\"error\",\"message\":\"Failed to load orders: " + e.getMessage() + "\"}")
                    .build();
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createNewAccount(String jsonData) {
        UserDTO userDTO = AppUtil.GSON.fromJson(jsonData, UserDTO.class);
        String responseJson = new UserService().addNewUser(userDTO);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/login")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response userLogin(String jsonData, @Context HttpServletRequest request) {
        UserDTO userDTO = AppUtil.GSON.fromJson(jsonData, UserDTO.class);
        String responseJson = new UserService().userLogin(userDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/forgot-password")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response forgotPassword(String jsonData) {
        UserDTO userDTO = AppUtil.GSON.fromJson(jsonData, UserDTO.class);
        String responseJson = new UserService().sendPasswordResetCode(userDTO);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/reset-password")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(String jsonData) {
        UserDTO userDTO = AppUtil.GSON.fromJson(jsonData, UserDTO.class);
        String responseJson = new UserService().resetPassword(userDTO);
        return Response.ok().entity(responseJson).build();
    }
}
