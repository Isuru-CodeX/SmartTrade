package lk.jiat.smarttrade.controller.api;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lk.jiat.smarttrade.annotation.IsUser;
import lk.jiat.smarttrade.dto.UserDTO;
import lk.jiat.smarttrade.service.ProfileService;
import lk.jiat.smarttrade.util.AppUtil;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

@Path("/profiles")
public class ProfileController {
    @IsUser
    @Path("/addresses")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadAddresses(@Context HttpServletRequest request) {
        String responseJson = new ProfileService().loadUserAddresses(request);
        return Response.ok().entity(responseJson).build();
    }

    @IsUser
    @Path("/update-profile")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUserProfile(String jsonData, @Context HttpServletRequest request) {
        UserDTO userDTO = AppUtil.GSON.fromJson(jsonData, UserDTO.class);
        String responseJson = new ProfileService().updateProfile(userDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @IsUser
    @Path("/user-profile")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadUserProfile(@Context HttpServletRequest request) {
        String responseJson = new ProfileService().userProfile(request);
        return Response.ok().entity(responseJson).build();
    }

    @IsUser
    @Path("/upload-avatar")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadAvatar(
            @FormDataParam("avatar") FormDataBodyPart formDataBodyPart,
            @Context HttpServletRequest request,
            @Context ServletContext context) {
        String responseJson = new ProfileService().uploadAvatar(formDataBodyPart, request, context);
        return Response.ok().entity(responseJson).build();
    }

    @IsUser
    @Path("/remove-avatar")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeAvatar(@Context HttpServletRequest request) {
        String responseJson = new ProfileService().removeAvatar(request);
        return Response.ok().entity(responseJson).build();
    }
}
