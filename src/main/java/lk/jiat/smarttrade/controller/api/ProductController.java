package lk.jiat.smarttrade.controller.api;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import lk.jiat.smarttrade.dto.ProductDTO;
import lk.jiat.smarttrade.service.ProductService;
import lk.jiat.smarttrade.util.AppUtil;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import java.util.ArrayList;
import java.util.List;

@Path("/products")
public class ProductController {

    @Path("/all")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response loadAllUserProducts(@Context HttpServletRequest request) {
        String responseJson = new ProductService().getAllUserProducts(request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/{productId}/upload-images")
    @PUT
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadProductImagesPut(
            @PathParam("productId") int productId,
            FormDataMultiPart multiPart,
            @Context ServletContext context) {
        return handleImageUpload(productId, multiPart, context);
    }

    @Path("/{productId}/upload-images")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadProductImagesPost(
            @PathParam("productId") int productId,
            FormDataMultiPart multiPart,
            @Context ServletContext context) {
        return handleImageUpload(productId, multiPart, context);
    }

    private Response handleImageUpload(int productId, FormDataMultiPart multiPart, ServletContext context) {
        List<FormDataBodyPart> bodyParts = null;
        if (multiPart != null) {
            bodyParts = multiPart.getFields("images[]");
            if (bodyParts == null || bodyParts.isEmpty()) {
                bodyParts = multiPart.getFields("images");
            }
            if (bodyParts == null || bodyParts.isEmpty()) {
                bodyParts = new ArrayList<>();
                for (org.glassfish.jersey.media.multipart.BodyPart bp : multiPart.getBodyParts()) {
                    if (bp instanceof FormDataBodyPart) {
                        FormDataBodyPart fdbp = (FormDataBodyPart) bp;
                        if (fdbp.getContentDisposition() != null && fdbp.getContentDisposition().getFileName() != null) {
                            bodyParts.add(fdbp);
                        }
                    }
                }
            }
        }
        String responseJson = new ProductService().uploadProductImages(productId, bodyParts, context);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/save-product")
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response saveProduct(@FormDataParam("product") String productJson, @Context HttpServletRequest request) {
        ProductDTO productDTO = AppUtil.GSON.fromJson(productJson, ProductDTO.class);
        String responseJson = new ProductService().addNewProduct(productDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/{productId}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getProductForEdit(
            @PathParam("productId") int productId,
            @Context HttpServletRequest request) {
        String responseJson = new ProductService().getProductForEdit(productId, request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/update-product")
    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateProduct(String productJson, @Context HttpServletRequest request) {
        ProductDTO productDTO = AppUtil.GSON.fromJson(productJson, ProductDTO.class);
        String responseJson = new ProductService().updateUserProduct(productDTO, request);
        return Response.ok().entity(responseJson).build();
    }

    @Path("/{productId}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteProduct(
            @PathParam("productId") int productId,
            @Context HttpServletRequest request) {
        String responseJson = new ProductService().deleteUserProduct(productId, request);
        return Response.ok().entity(responseJson).build();
    }

}
