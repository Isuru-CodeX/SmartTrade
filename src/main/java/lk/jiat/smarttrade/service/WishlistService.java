package lk.jiat.smarttrade.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lk.jiat.smarttrade.entity.Product;
import lk.jiat.smarttrade.entity.User;
import lk.jiat.smarttrade.entity.Wishlist;
import lk.jiat.smarttrade.util.AppUtil;
import lk.jiat.smarttrade.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class WishlistService {

    public String getWishlistIdsJson(User user) {
        JsonObject result = new JsonObject();
        if (user == null) {
            result.addProperty("status", "guest");
            result.addProperty("authenticated", false);
            result.add("ids", new JsonArray());
            return AppUtil.GSON.toJson(result);
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Integer> ids = session.createQuery(
                    "SELECT w.product.id FROM Wishlist w WHERE w.user.id = :userId ORDER BY w.id DESC", Integer.class)
                    .setParameter("userId", user.getId())
                    .getResultList();

            JsonArray arr = new JsonArray();
            for (Integer id : ids) {
                arr.add(id);
            }

            result.addProperty("status", "success");
            result.addProperty("authenticated", true);
            result.addProperty("userId", user.getId());
            result.add("ids", arr);
            return AppUtil.GSON.toJson(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("status", "error");
            result.addProperty("message", "Failed to retrieve wishlist: " + e.getMessage());
            return AppUtil.GSON.toJson(result);
        }
    }

    public String toggleWishlist(User user, int productId) {
        JsonObject result = new JsonObject();
        if (user == null) {
            result.addProperty("status", "unauthorized");
            result.addProperty("message", "User must be authenticated.");
            return AppUtil.GSON.toJson(result);
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Product product = session.get(Product.class, productId);
            if (product == null) {
                result.addProperty("status", "error");
                result.addProperty("message", "Product not found.");
                return AppUtil.GSON.toJson(result);
            }

            Transaction tx = session.beginTransaction();
            Wishlist existing = session.createQuery(
                    "FROM Wishlist w WHERE w.user.id = :userId AND w.product.id = :productId", Wishlist.class)
                    .setParameter("userId", user.getId())
                    .setParameter("productId", productId)
                    .uniqueResult();

            boolean wished;
            if (existing != null) {
                session.delete(existing);
                wished = false;
                result.addProperty("action", "removed");
            } else {
                Wishlist w = new Wishlist();
                w.setUser(user);
                w.setProduct(product);
                session.save(w);
                wished = true;
                result.addProperty("action", "added");
            }
            tx.commit();

            result.addProperty("status", "success");
            result.addProperty("productId", productId);
            result.addProperty("wished", wished);

            // Also attach latest complete list of ids
            List<Integer> ids = session.createQuery(
                    "SELECT w.product.id FROM Wishlist w WHERE w.user.id = :userId ORDER BY w.id DESC", Integer.class)
                    .setParameter("userId", user.getId())
                    .getResultList();
            JsonArray arr = new JsonArray();
            for (Integer id : ids) arr.add(id);
            result.add("ids", arr);

            return AppUtil.GSON.toJson(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("status", "error");
            result.addProperty("message", "Failed to toggle wishlist: " + e.getMessage());
            return AppUtil.GSON.toJson(result);
        }
    }
}
