package lk.jiat.smarttrade.config;

import org.glassfish.jersey.server.ResourceConfig;

public class AppConfig extends ResourceConfig {
    public AppConfig(){
        packages("lk.jiat.smarttrade.controller.api", "lk.jiat.smarttrade.controller", "lk.jiat.smarttrade.middleware");
        register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
        register(lk.jiat.smarttrade.controller.api.AdminController.class);
        register(lk.jiat.smarttrade.controller.api.UserController.class);
        register(lk.jiat.smarttrade.controller.api.ProductController.class);
        register(lk.jiat.smarttrade.controller.api.ProfileController.class);
        register(lk.jiat.smarttrade.controller.api.ContentController.class);
        register(lk.jiat.smarttrade.controller.api.VerificationController.class);
        register(lk.jiat.smarttrade.controller.api.CheckoutController.class);
        register(lk.jiat.smarttrade.controller.api.CartController.class);
        register(lk.jiat.smarttrade.controller.api.WishlistController.class);
    }
}
