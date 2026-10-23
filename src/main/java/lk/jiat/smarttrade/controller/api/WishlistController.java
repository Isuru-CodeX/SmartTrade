package lk.jiat.smarttrade.controller.api;

import lk.jiat.smarttrade.entity.User;
import lk.jiat.smarttrade.service.WishlistService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/wishlist")
public class WishlistController {

    private final WishlistService wishlistService = new WishlistService();

    private User getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("user") != null) {
            return (User) session.getAttribute("user");
        }
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getWishlist(@Context HttpServletRequest request) {
        User user = getSessionUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"status\":\"unauthorized\",\"message\":\"Authentication required.\"}")
                    .build();
        }
        String res = wishlistService.getWishlistIdsJson(user);
        return Response.ok(res).build();
    }

    @POST
    @Path("/toggle/{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response toggleWishlist(@PathParam("productId") int productId, @Context HttpServletRequest request) {
        User user = getSessionUser(request);
        if (user == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("{\"status\":\"unauthorized\",\"message\":\"Please sign in to save items to your wishlist.\"}")
                    .build();
        }
        String res = wishlistService.toggleWishlist(user, productId);
        return Response.ok(res).build();
    }
}
