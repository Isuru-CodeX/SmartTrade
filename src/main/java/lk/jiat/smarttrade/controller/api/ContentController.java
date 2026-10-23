package lk.jiat.smarttrade.controller.api;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import com.google.gson.JsonObject;
import lk.jiat.smarttrade.service.CityService;
import lk.jiat.smarttrade.service.ContentService;
import lk.jiat.smarttrade.util.AppUtil;

@Path("/data")
public class ContentController {

    @Path("/new-arrivals")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadNewArrivals() {
        String responseJson = new ContentService().loadNewArrivalProducts();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/products")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadAllProducts() {
        String responseJson = new ContentService().loadAllProducts();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/cities")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadCities() {
        String loadAllCities = new CityService().loadAllCities();
        return Response.ok().entity(loadAllCities).build();
    }

    @Path("/brands")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadBrands() {
        String responseJson = new ContentService().loadBrandDetails();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/{brandId}/models")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadModels(@PathParam("brandId") int id) {
        String responseJson = new ContentService().loadModelDetails(id);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/specifications")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadSpecifications() {
        String responseJson = new ContentService().loadProductSpecifications();
        return Response.ok().entity(responseJson).build();
    }

    @Path("/brand")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addBrand(String jsonBody, @Context HttpServletRequest request) {
        if (!isAuthorized(request)) {
            return unauthorizedResponse();
        }
        JsonObject body = AppUtil.GSON.fromJson(jsonBody, JsonObject.class);
        String name = body != null && body.has("name") ? body.get("name").getAsString() : "";
        String responseJson = new ContentService().addBrand(name);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/model")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addModel(String jsonBody, @Context HttpServletRequest request) {
        if (!isAuthorized(request)) {
            return unauthorizedResponse();
        }
        JsonObject body = AppUtil.GSON.fromJson(jsonBody, JsonObject.class);
        int brandId = body != null && body.has("brandId") ? body.get("brandId").getAsInt() : 0;
        String name = body != null && body.has("name") ? body.get("name").getAsString() : "";
        String responseJson = new ContentService().addModel(brandId, name);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/storage")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addStorage(String jsonBody, @Context HttpServletRequest request) {
        if (!isAuthorized(request)) {
            return unauthorizedResponse();
        }
        JsonObject body = AppUtil.GSON.fromJson(jsonBody, JsonObject.class);
        String value = body != null && body.has("value") ? body.get("value").getAsString() : "";
        String responseJson = new ContentService().addStorage(value);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/color")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addColor(String jsonBody, @Context HttpServletRequest request) {
        if (!isAuthorized(request)) {
            return unauthorizedResponse();
        }
        JsonObject body = AppUtil.GSON.fromJson(jsonBody, JsonObject.class);
        String value = body != null && body.has("value") ? body.get("value").getAsString() : "";
        String responseJson = new ContentService().addColor(value);
        return Response.ok().entity(responseJson).build();
    }

    private boolean isAuthorized(HttpServletRequest request) {
        if (request == null) return false;
        HttpSession session = request.getSession(false);
        return session != null && (session.getAttribute("user") != null || session.getAttribute("adminUser") != null);
    }

    private Response unauthorizedResponse() {
        JsonObject res = new JsonObject();
        res.addProperty("status", false);
        res.addProperty("message", "Please log in to add options.");
        return Response.status(Response.Status.UNAUTHORIZED).entity(AppUtil.GSON.toJson(res)).build();
    }
}
