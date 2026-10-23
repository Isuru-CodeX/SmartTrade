package lk.jiat.smarttrade.controller.api;

import com.google.gson.JsonObject;
import lk.jiat.smarttrade.entity.User;
import lk.jiat.smarttrade.service.CheckoutService;
import lk.jiat.smarttrade.service.StripeService;
import lk.jiat.smarttrade.util.AppUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService = new CheckoutService();

    @GET
    @Path("/stripe-config")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStripeConfig() {
        JsonObject res = new JsonObject();
        res.addProperty("status", "success");
        res.addProperty("publishableKey", StripeService.getPublishableKey());
        res.addProperty("isConfigured", StripeService.isConfigured());
        res.addProperty("currency", StripeService.getCurrency());
        return Response.ok(AppUtil.GSON.toJson(res)).build();
    }

    @POST
    @Path("/create-intent")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createPaymentIntent(String jsonData, @Context HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute("user") : null;
        String responseJson = checkoutService.createPaymentIntent(jsonData, sessionUser);
        return Response.ok(responseJson).build();
    }

    @POST
    @Path("/confirm")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response confirmOrder(String jsonData, @Context HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        User sessionUser = (session != null) ? (User) session.getAttribute("user") : null;
        String responseJson = checkoutService.confirmOrder(jsonData, sessionUser);
        try {
            JsonObject res = AppUtil.GSON.fromJson(responseJson, JsonObject.class);
            if (res != null && res.has("orderId")) {
                request.getSession(true).setAttribute("lastOrderId", res.get("orderId").getAsInt());
            }
        } catch (Exception ignored) {}
        return Response.ok(responseJson).build();
    }

    @GET
    @Path("/receipt/{orderId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getReceipt(@PathParam("orderId") int orderId, @Context HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        boolean isAdmin = session != null && session.getAttribute("adminUser") != null;
        User sessionUser = session != null ? (User) session.getAttribute("user") : null;

        // Verify ownership: Admin can inspect any receipt; customer can only view own order
        if (!isAdmin) {
            try (org.hibernate.Session dbSession = lk.jiat.smarttrade.util.HibernateUtil.getSessionFactory().openSession()) {
                lk.jiat.smarttrade.entity.Order order = dbSession.get(lk.jiat.smarttrade.entity.Order.class, orderId);
                if (order == null) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity("{\"status\":\"error\",\"message\":\"Order not found\"}").build();
                }
                boolean isOwner = sessionUser != null && order.getUser() != null && order.getUser().getId() == sessionUser.getId();
                boolean isRecentGuest = session != null && session.getAttribute("lastOrderId") != null && session.getAttribute("lastOrderId").equals(orderId);
                if (!isOwner && !isRecentGuest) {
                    return Response.status(Response.Status.FORBIDDEN)
                            .entity("{\"status\":\"error\",\"message\":\"You are not authorized to view this receipt.\"}").build();
                }
            } catch (Exception e) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}").build();
            }
        }

        String responseJson = checkoutService.getReceipt(orderId);
        return Response.ok(responseJson).build();
    }
}
