package lk.jiat.smarttrade.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lk.jiat.smarttrade.entity.Cart;
import lk.jiat.smarttrade.entity.Product;
import lk.jiat.smarttrade.entity.Stock;
import lk.jiat.smarttrade.entity.User;
import lk.jiat.smarttrade.util.AppUtil;
import lk.jiat.smarttrade.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public class CartService {

    public String getCartJson(User user) {
        JsonObject result = new JsonObject();
        if (user == null) {
            result.addProperty("status", "guest");
            result.addProperty("authenticated", false);
            result.add("items", new JsonArray());
            result.addProperty("count", 0);
            result.addProperty("total", 0.0);
            return AppUtil.GSON.toJson(result);
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Cart> cartList = session.createQuery(
                    "FROM Cart c WHERE c.user.id = :userId ORDER BY c.id ASC", Cart.class)
                    .setParameter("userId", user.getId())
                    .getResultList();

            JsonArray itemsArray = new JsonArray();
            int totalCount = 0;
            double totalPrice = 0.0;

            for (Cart c : cartList) {
                Stock stock = c.getStock();
                if (stock == null) continue;
                Product product = stock.getProduct();
                if (product == null) continue;

                int qty = c.getQty();
                if (qty <= 0) qty = 1;
                // Cap to current available stock
                if (stock.getQty() > 0 && qty > stock.getQty()) {
                    qty = stock.getQty();
                }

                double unitPrice = stock.getPrice();
                double lineTotal = unitPrice * qty;
                totalCount += qty;
                totalPrice += lineTotal;

                JsonObject itemObj = new JsonObject();
                itemObj.addProperty("cartId", c.getId());
                itemObj.addProperty("stockId", stock.getId());
                itemObj.addProperty("productId", product.getId());
                itemObj.addProperty("title", product.getTitle());

                String brand = (product.getModel() != null && product.getModel().getBrand() != null)
                        ? product.getModel().getBrand().getName() : "";
                itemObj.addProperty("brand", brand);

                String spec = "";
                if (product.getStorage() != null && product.getQuality() != null) {
                    spec = product.getStorage().getValue() + " · " + product.getQuality().getValue();
                } else if (product.getStorage() != null) {
                    spec = product.getStorage().getValue();
                } else if (product.getQuality() != null) {
                    spec = product.getQuality().getValue();
                }
                itemObj.addProperty("spec", spec);

                itemObj.addProperty("price", unitPrice);
                itemObj.addProperty("qty", qty);
                itemObj.addProperty("max", stock.getQty());

                String img = (product.getImages() != null && !product.getImages().isEmpty())
                        ? product.getImages().get(0) : "assets/images/placeholder.png";
                itemObj.addProperty("image", img);

                itemsArray.add(itemObj);
            }

            result.addProperty("status", "success");
            result.addProperty("authenticated", true);
            result.addProperty("userId", user.getId());
            result.add("items", itemsArray);
            result.addProperty("count", totalCount);
            result.addProperty("total", totalPrice);
            return AppUtil.GSON.toJson(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("status", "error");
            result.addProperty("message", "Failed to retrieve cart: " + e.getMessage());
            return AppUtil.GSON.toJson(result);
        }
    }

    public String addToCart(User user, int stockId, int addQty) {
        JsonObject result = new JsonObject();
        if (user == null) {
            result.addProperty("status", "unauthorized");
            result.addProperty("message", "User must be authenticated.");
            return AppUtil.GSON.toJson(result);
        }
        if (addQty <= 0) addQty = 1;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Stock stock = session.get(Stock.class, stockId);
            if (stock == null) {
                result.addProperty("status", "error");
                result.addProperty("message", "Stock item not found.");
                return AppUtil.GSON.toJson(result);
            }
            if (stock.getQty() <= 0) {
                result.addProperty("status", "error");
                result.addProperty("message", "This product is currently out of stock.");
                return AppUtil.GSON.toJson(result);
            }

            Transaction tx = session.beginTransaction();
            Cart existing = session.createQuery(
                    "FROM Cart c WHERE c.user.id = :userId AND c.stock.id = :stockId", Cart.class)
                    .setParameter("userId", user.getId())
                    .setParameter("stockId", stockId)
                    .uniqueResult();

            if (existing != null) {
                int newQty = existing.getQty() + addQty;
                if (newQty > stock.getQty()) {
                    tx.rollback();
                    result.addProperty("status", "error");
                    result.addProperty("message", "Only " + stock.getQty() + " unit(s) available.");
                    return AppUtil.GSON.toJson(result);
                }
                existing.setQty(newQty);
                session.update(existing);
            } else {
                if (addQty > stock.getQty()) {
                    tx.rollback();
                    result.addProperty("status", "error");
                    result.addProperty("message", "Only " + stock.getQty() + " unit(s) available.");
                    return AppUtil.GSON.toJson(result);
                }
                Cart newCart = new Cart();
                newCart.setUser(user);
                newCart.setStock(stock);
                newCart.setQty(addQty);
                session.save(newCart);
            }
            tx.commit();
            return getCartJson(user);

        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("status", "error");
            result.addProperty("message", "Failed to add to cart: " + e.getMessage());
            return AppUtil.GSON.toJson(result);
        }
    }

    public String updateQty(User user, int stockId, int newQty) {
        JsonObject result = new JsonObject();
        if (user == null) {
            result.addProperty("status", "unauthorized");
            result.addProperty("message", "User must be authenticated.");
            return AppUtil.GSON.toJson(result);
        }
        if (newQty <= 0) newQty = 1;

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            Cart existing = session.createQuery(
                    "FROM Cart c WHERE c.user.id = :userId AND c.stock.id = :stockId", Cart.class)
                    .setParameter("userId", user.getId())
                    .setParameter("stockId", stockId)
                    .uniqueResult();

            if (existing == null) {
                tx.rollback();
                result.addProperty("status", "error");
                result.addProperty("message", "Cart item not found.");
                return AppUtil.GSON.toJson(result);
            }

            Stock stock = existing.getStock();
            int max = (stock != null && stock.getQty() > 0) ? stock.getQty() : 99;
            existing.setQty(Math.min(newQty, max));
            session.update(existing);
            tx.commit();
            return getCartJson(user);

        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("status", "error");
            result.addProperty("message", "Failed to update quantity: " + e.getMessage());
            return AppUtil.GSON.toJson(result);
        }
    }

    public String removeItem(User user, int stockId) {
        JsonObject result = new JsonObject();
        if (user == null) {
            result.addProperty("status", "unauthorized");
            result.addProperty("message", "User must be authenticated.");
            return AppUtil.GSON.toJson(result);
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("DELETE FROM Cart c WHERE c.user.id = :userId AND c.stock.id = :stockId")
                    .setParameter("userId", user.getId())
                    .setParameter("stockId", stockId)
                    .executeUpdate();
            tx.commit();
            return getCartJson(user);

        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("status", "error");
            result.addProperty("message", "Failed to remove item: " + e.getMessage());
            return AppUtil.GSON.toJson(result);
        }
    }

    public String clearCart(User user) {
        JsonObject result = new JsonObject();
        if (user == null) {
            result.addProperty("status", "unauthorized");
            result.addProperty("message", "User must be authenticated.");
            return AppUtil.GSON.toJson(result);
        }

        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.createQuery("DELETE FROM Cart c WHERE c.user.id = :userId")
                    .setParameter("userId", user.getId())
                    .executeUpdate();
            tx.commit();
            result.addProperty("status", "success");
            result.addProperty("message", "Cart cleared successfully.");
            result.add("items", new JsonArray());
            result.addProperty("count", 0);
            result.addProperty("total", 0.0);
            return AppUtil.GSON.toJson(result);

        } catch (Exception e) {
            e.printStackTrace();
            result.addProperty("status", "error");
            result.addProperty("message", "Failed to clear cart: " + e.getMessage());
            return AppUtil.GSON.toJson(result);
        }
    }
}
