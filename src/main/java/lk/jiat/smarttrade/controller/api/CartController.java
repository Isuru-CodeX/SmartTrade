package lk.jiat.smarttrade.controller.api;

import com.google.gson.JsonObject;
import lk.jiat.smarttrade.entity.User;
import lk.jiat.smarttrade.service.CartService;
import lk.jiat.smarttrade.util.AppUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/cart")
public class CartController {

    private final CartService cartService = new CartService();

    private User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            return (User) session.getAttribute("user");
        }
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCart(@Context HttpServletRequest request) {
        User user = getSessionUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"status\":\"unauthorized\",\"message\":\"Authentication required.\"}")
                    .build();
        }
        String res = cartService.getCartJson(user);
        return Response.ok(res).build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addToCart(String body, @Context HttpServletRequest request) {
        User user = getSessionUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"status\":\"unauthorized\",\"message\":\"Please sign in to save items to your account cart.\"}")
                    .build();
        }
        try {
            JsonObject json = AppUtil.GSON.fromJson(body, JsonObject.class);
            int stockId = json.has("stockId") ? json.get("stockId").getAsInt() : 0;
            int qty = json.has("qty") ? json.get("qty").getAsInt() : 1;
            String res = cartService.addToCart(user, stockId, qty);
            return Response.ok(res).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"error\",\"message\":\"Invalid request format.\"}")
                    .build();
        }
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateQty(String body, @Context HttpServletRequest request) {
        User user = getSessionUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"status\":\"unauthorized\",\"message\":\"Please sign in to update your cart.\"}")
                    .build();
        }
        try {
            JsonObject json = AppUtil.GSON.fromJson(body, JsonObject.class);
            int stockId = json.has("stockId") ? json.get("stockId").getAsInt() : 0;
            int qty = json.has("qty") ? json.get("qty").getAsInt() : 1;
            String res = cartService.updateQty(user, stockId, qty);
            return Response.ok(res).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"status\":\"error\",\"message\":\"Invalid request format.\"}")
                    .build();
        }
    }

    @DELETE
    @Path("/{stockId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeItem(@PathParam("stockId") int stockId, @Context HttpServletRequest request) {
        User user = getSessionUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"status\":\"unauthorized\",\"message\":\"Please sign in.\"}")
                    .build();
        }
        String res = cartService.removeItem(user, stockId);
        return Response.ok(res).build();
    }

    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response clearCart(@Context HttpServletRequest request) {
        User user = getSessionUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"status\":\"unauthorized\",\"message\":\"Please sign in.\"}")
                    .build();
        }
        String res = cartService.clearCart(user);
        return Response.ok(res).build();
    }
}
