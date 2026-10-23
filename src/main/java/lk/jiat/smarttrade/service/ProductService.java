package lk.jiat.smarttrade.service;

import com.google.gson.JsonObject;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.Context;
import lk.jiat.smarttrade.dto.ProductDTO;
import lk.jiat.smarttrade.dto.StockDTO;
import lk.jiat.smarttrade.entity.*;
import lk.jiat.smarttrade.util.AppUtil;
import lk.jiat.smarttrade.util.HibernateUtil;
import org.glassfish.jersey.media.multipart.ContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductService {
    public String getAllUserProducts(@Context HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            message = "Session Expired. Please logged in!";
        } else {
            User sessionUser = (User) httpSession.getAttribute("user");
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            try {
                Seller seller = getOrCreateSeller(hibernateSession, sessionUser);
                if (seller == null) {
                    message = "Could not initialize seller account. Please contact support.";
                } else {
                    Set<Product> productSet = seller.getProducts();
                    if (productSet == null || productSet.isEmpty()) {
                        message = "You have not listed any phones yet.";
                        status = true;
                        responseObject.add("products", new com.google.gson.JsonArray());
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy MMMM dd");

                        // Batch fetch stocks for all products to avoid N+1 query problem
                        List<Stock> allStocks = hibernateSession.createQuery("FROM Stock s WHERE s.product IN (:products) ORDER BY s.id DESC", Stock.class)
                                .setParameterList("products", productSet)
                                .getResultList();

                        Map<Integer, List<Stock>> stockMap = allStocks.stream()
                                .collect(Collectors.groupingBy(s -> s.getProduct().getId()));

                        List<ProductDTO> productDTOList = new ArrayList<>();
                        for (Product p : productSet) {
                            ProductDTO dto = new ProductDTO();
                            dto.setProductId(p.getId());
                            dto.setTitle(p.getTitle());
                            dto.setDescription(p.getDescription());
                            if (p.getModel() != null) {
                                dto.setModelId(p.getModel().getId());
                                dto.setModelName(p.getModel().getName());
                                if (p.getModel().getBrand() != null) {
                                    dto.setBrandId(p.getModel().getBrand().getId());
                                    dto.setBrandName(p.getModel().getBrand().getName());
                                }
                            }
                            if (p.getStorage() != null) {
                                dto.setStorageId(p.getStorage().getId());
                                dto.setStorageValue(p.getStorage().getValue());
                            }
                            if (p.getColor() != null) {
                                dto.setColorId(p.getColor().getId());
                                dto.setColorValue(p.getColor().getValue());
                            }
                            if (p.getQuality() != null) {
                                dto.setQualityId(p.getQuality().getId());
                                dto.setQualityValue(p.getQuality().getValue());
                            }
                            dto.setImages(p.getImages() != null ? new ArrayList<>(p.getImages()) : new ArrayList<>());

                            List<Stock> stocks = stockMap.getOrDefault(p.getId(), new ArrayList<>());

                            List<StockDTO> stockDTOList = new ArrayList<>();
                            String mainStatus = "APPROVED";
                            for (Stock s : stocks) {
                                StockDTO stockDTO = new StockDTO();
                                stockDTO.setStockId(s.getId());
                                stockDTO.setProductId(s.getProduct().getId());
                                stockDTO.setQty(s.getQty());
                                stockDTO.setPrice(s.getPrice());
                                stockDTO.setCreatedAt(formatter.format(s.getCreatedAt()));
                                String sStatus = s.getStatus() != null ? s.getStatus().getValue() : "APPROVED";
                                stockDTO.setStatus(sStatus);
                                mainStatus = sStatus;
                                stockDTOList.add(stockDTO);
                            }
                            if (!stocks.isEmpty()) {
                                dto.setPrice(stocks.get(0).getPrice());
                                dto.setQty(stocks.get(0).getQty());
                            }
                            dto.setStatus(mainStatus);
                            dto.setStockDTOList(stockDTOList);
                            productDTOList.add(dto);
                        }
                        productDTOList.sort(Comparator.comparing(ProductDTO::getProductId).reversed());
                        responseObject.add("products", AppUtil.GSON.toJsonTree(productDTOList));
                        status = true;
                        message = "Product loading successful";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                message = "Failed to load listings: " + e.getMessage();
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);

        return AppUtil.GSON.toJson(responseObject);
    }

    private Seller getOrCreateSeller(Session hibernateSession, User sessionUser) {
        if (sessionUser == null) return null;
        Seller seller = hibernateSession.createQuery("FROM Seller s WHERE s.user.id=:userId", Seller.class)
                .setParameter("userId", sessionUser.getId())
                .uniqueResult();
        if (seller == null) {
            Status approvedStatus = null;
            try {
                approvedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                        .setParameter("value", String.valueOf(Status.Type.APPROVED))
                        .uniqueResult();
            } catch (Exception ignored) {}
            if (approvedStatus == null) {
                try {
                    approvedStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                            .setParameter("value", String.valueOf(Status.Type.ACTIVE))
                            .uniqueResult();
                } catch (Exception ignored) {}
            }
            User dbUser = hibernateSession.find(User.class, sessionUser.getId());
            if (dbUser == null) return null;

            seller = new Seller();
            String name = ((dbUser.getFirstName() != null ? dbUser.getFirstName() : "") + " " +
                    (dbUser.getLastName() != null ? dbUser.getLastName() : "")).trim();
            seller.setCompanyName(!name.isEmpty() ? name : "Seller " + dbUser.getId());
            seller.setCompanyEmail(dbUser.getEmail() != null ? dbUser.getEmail() : "seller" + dbUser.getId() + "@smarttrade.lk");
            String mobile = "";
            if (dbUser.getAddresses() != null && !dbUser.getAddresses().isEmpty()) {
                for (Address addr : dbUser.getAddresses()) {
                    if (addr.getMobile() != null && !addr.getMobile().trim().isEmpty()) {
                        mobile = addr.getMobile().trim();
                        break;
                    }
                }
            }
            if (mobile.length() > 10) mobile = mobile.substring(0, 10);
            if (mobile.isEmpty()) mobile = "0770000000";
            seller.setCompanyMobile(mobile);
            seller.setStatus(approvedStatus);
            seller.setUser(dbUser);

            Transaction tx = hibernateSession.beginTransaction();
            try {
                hibernateSession.persist(seller);
                tx.commit();
            } catch (Exception e) {
                if (tx != null && tx.isActive()) tx.rollback();
                e.printStackTrace();
            }
        }
        return seller;
    }

    public String uploadProductImages(int productId, List<FormDataBodyPart> bodyParts, ServletContext context) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (productId <= 0) {
            responseObject.addProperty("status", false);
            responseObject.addProperty("message", "Invalid product ID!");
            return AppUtil.GSON.toJson(responseObject);
        }

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = hibernateSession.beginTransaction();
        try {
            Product product = hibernateSession.find(Product.class, productId);
            if (product == null) {
                message = "Product not found!";
            } else {
                List<String> images = product.getImages();
                if (images == null) {
                    images = new ArrayList<>();
                    product.setImages(images);
                } else {
                    images.clear();
                }

                if (bodyParts != null && !bodyParts.isEmpty()) {
                    FileUploadService fileUploadService = new FileUploadService(context);
                    for (FormDataBodyPart bodyPart : bodyParts) {
                        if (images.size() >= 8) {
                            break; // Cap at 8 images maximum
                        }
                        ContentDisposition contentDisposition = bodyPart.getContentDisposition();
                        if (contentDisposition != null && contentDisposition.getFileName() != null && !contentDisposition.getFileName().trim().isEmpty()) {
                            InputStream inputStream = bodyPart.getEntityAs(InputStream.class);
                            FileUploadService.FileItem fileItem = fileUploadService.uploadFile("product/" + productId, inputStream, contentDisposition);
                            if (fileItem != null && fileItem.getFullUrl() != null) {
                                images.add(fileItem.getFullUrl());
                            }
                        }
                    }
                }

                hibernateSession.update(product);
                transaction.commit();
                status = true;
                message = "Product images uploaded successfully (" + images.size() + " photo(s) saved, 1st is Cover)";
                responseObject.add("images", AppUtil.GSON.toJsonTree(images));
            }
        } catch (Exception e) {
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            e.printStackTrace();
            message = "Product image uploading failed: " + e.getMessage();
        } finally {
            hibernateSession.close();
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String updateProduct(Product product) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = hibernateSession.beginTransaction();
        try {
            hibernateSession.merge(product);
            transaction.commit();
            status = true;
            message = "Product images uploading successful";
        } catch (HibernateException e) {
            transaction.rollback();
            message = "Product image uploading failed!";
        } finally {
            hibernateSession.close();
        }
        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public Product getProductById(int id) {
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        Product product = hibernateSession.find(Product.class, id);
        hibernateSession.close();
        return product;
    }

    public String addNewProduct(ProductDTO productDTO, @Context HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        /// product-inserting-part-start
        if (productDTO.getBrandId() <= 0) {
            message = "Invalid brand type. Please select a correct brand!";
        } else if (productDTO.getModelId() <= 0) {
            message = "Invalid model type. Please select a correct model!";
        } else if (productDTO.getTitle() == null) {
            message = "Product title is required!";
        } else if (productDTO.getTitle().isBlank()) {
            message = "Product title can not be empty!";
        } else if (productDTO.getDescription() == null) {
            message = "Product description is required!";
        } else if (productDTO.getDescription().isBlank()) {
            message = "Product description can not be empty!";
        } else if (productDTO.getStorageId() <= 0) {
            message = "Invalid storage type. Please select a correct storage!";
        } else if (productDTO.getColorId() <= 0) {
            message = "Invalid color type. Please select a correct color!";
        } else if (productDTO.getQualityId() <= 0) {
            message = "Invalid condition type. Please select a correct condition!";
        } else if (productDTO.getPrice() <= 0) {
            message = "Product price can not be less than or equal to 0";
        } else if (productDTO.getQty() <= 0) {
            message = "Product quantity can not be less than or equal to 0";
        } else {
            HttpSession httpSession = request.getSession(false);
            if (httpSession == null) {
                message = "Session expired! Please logged in";
            } else if (httpSession.getAttribute("user") == null) {
                message = "Please logged in";
            } else {
                User sessionUser = (User) httpSession.getAttribute("user");
                Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
                try {
                    Seller seller = getOrCreateSeller(hibernateSession, sessionUser);
                    if (seller == null) {
                        message = "Could not initialize seller account. Please try again.";
                    } else {
                        Model model = hibernateSession.find(Model.class, productDTO.getModelId());
                        if (model == null) {
                            message = "Model not found. Please contact administration";
                        } else if (model.getBrand() == null || model.getBrand().getId() != productDTO.getBrandId()) {
                            message = "The selected model does not belong to the selected brand!";
                        } else {
                            Storage storage = hibernateSession.find(Storage.class, productDTO.getStorageId());
                            if (storage == null) {
                                message = "Storage not found. Please contact administration";
                            } else {
                                Color color = hibernateSession.find(Color.class, productDTO.getColorId());
                                if (color == null) {
                                    message = "Color not found. Please contact administration";
                                } else {
                                    Quality quality = hibernateSession.find(Quality.class, productDTO.getQualityId());
                                    if (quality == null) {
                                        message = "Quality not found. Please contact administration";
                                    } else {
                                        Product product = new Product();
                                        product.setTitle(productDTO.getTitle());
                                        product.setDescription(productDTO.getDescription());
                                        product.setModel(model);
                                        product.setStorage(storage);
                                        product.setQuality(quality);
                                        product.setColor(color);
                                        product.setSeller(seller);

                                        Stock stock = new Stock();
                                        stock.setProduct(product);
                                        stock.setPrice(productDTO.getPrice());
                                        stock.setQty(productDTO.getQty());

                                        Discount defaultDiscount = null;
                                        try {
                                            defaultDiscount = hibernateSession.createNamedQuery("Discount.findDefault", Discount.class)
                                                    .getSingleResult();
                                        } catch (Exception ignored) {}
                                        if (defaultDiscount == null) {
                                            defaultDiscount = hibernateSession.createQuery("FROM Discount d WHERE d.couponCode='DEFAULT'", Discount.class)
                                                    .setMaxResults(1)
                                                    .uniqueResult();
                                        }

                                        Status stockStatus = null;
                                        try {
                                            stockStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                                                    .setParameter("value", String.valueOf(Status.Type.APPROVED))
                                                    .getSingleResult();
                                        } catch (Exception ignored) {}
                                        if (stockStatus == null) {
                                            try {
                                                stockStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                                                        .setParameter("value", String.valueOf(Status.Type.ACTIVE))
                                                        .getSingleResult();
                                            } catch (Exception ignored) {}
                                        }
                                        if (stockStatus == null) {
                                            stockStatus = hibernateSession.createNamedQuery("Status.findByValue", Status.class)
                                                    .setParameter("value", String.valueOf(Status.Type.PENDING))
                                                    .getSingleResult();
                                        }

                                        stock.setDiscount(defaultDiscount);
                                        stock.setStatus(stockStatus);

                                        Transaction transaction = hibernateSession.beginTransaction();
                                        try {
                                            hibernateSession.persist(product);
                                            hibernateSession.persist(stock);
                                            transaction.commit();
                                            status = true;
                                            message = "Product saved successfully!";
                                            responseObject.addProperty("productId", product.getId());
                                        } catch (Exception e) {
                                            if (transaction != null && transaction.isActive()) {
                                                transaction.rollback();
                                            }
                                            e.printStackTrace();
                                            message = "Failed to save product: " + e.getMessage();
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    message = "Failed to save product: " + e.getMessage();
                } finally {
                    hibernateSession.close();
                }
            }
        }
        /// product-inserting-part-end

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String getProductForEdit(int productId, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            message = "Session expired. Please log in!";
        } else {
            User sessionUser = (User) httpSession.getAttribute("user");
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            try {
                Product product = hibernateSession.find(Product.class, productId);
                if (product == null) {
                    message = "Product not found!";
                } else {
                    Seller seller = getOrCreateSeller(hibernateSession, sessionUser);
                    if (!sessionUser.isAdmin() && (seller == null || product.getSeller() == null || product.getSeller().getId() != seller.getId())) {
                        message = "You do not have permission to view or edit this product.";
                    } else {
                        ProductDTO dto = new ProductDTO();
                        dto.setProductId(product.getId());
                        dto.setTitle(product.getTitle());
                        dto.setDescription(product.getDescription());
                        if (product.getModel() != null) {
                            dto.setModelId(product.getModel().getId());
                            dto.setModelName(product.getModel().getName());
                            if (product.getModel().getBrand() != null) {
                                dto.setBrandId(product.getModel().getBrand().getId());
                                dto.setBrandName(product.getModel().getBrand().getName());
                            }
                        }
                        if (product.getStorage() != null) {
                            dto.setStorageId(product.getStorage().getId());
                            dto.setStorageValue(product.getStorage().getValue());
                        }
                        if (product.getColor() != null) {
                            dto.setColorId(product.getColor().getId());
                            dto.setColorValue(product.getColor().getValue());
                        }
                        if (product.getQuality() != null) {
                            dto.setQualityId(product.getQuality().getId());
                            dto.setQualityValue(product.getQuality().getValue());
                        }
                        dto.setImages(product.getImages() != null ? new ArrayList<>(product.getImages()) : new ArrayList<>());

                        // Get latest stock
                        List<Stock> stocks = hibernateSession.createQuery(
                                "FROM Stock s WHERE s.product=:product ORDER BY s.id DESC", Stock.class)
                                .setParameter("product", product)
                                .setMaxResults(1)
                                .getResultList();
                        if (!stocks.isEmpty()) {
                            Stock latestStock = stocks.get(0);
                            dto.setPrice(latestStock.getPrice());
                            dto.setQty(latestStock.getQty());
                            dto.setStatus(latestStock.getStatus() != null ? latestStock.getStatus().getValue() : "APPROVED");
                        }

                        responseObject.add("product", AppUtil.GSON.toJsonTree(dto));
                        status = true;
                        message = "Product loaded successfully";
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                message = "Failed to load product: " + e.getMessage();
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String updateUserProduct(ProductDTO productDTO, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            message = "Session expired. Please log in!";
        } else if (productDTO.getProductId() <= 0) {
            message = "Invalid product ID!";
        } else if (productDTO.getTitle() == null || productDTO.getTitle().isBlank()) {
            message = "Product title is required!";
        } else if (productDTO.getPrice() <= 0) {
            message = "Price must be greater than 0!";
        } else if (productDTO.getQty() <= 0) {
            message = "Quantity must be greater than 0!";
        } else {
            User sessionUser = (User) httpSession.getAttribute("user");
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;
            try {
                Seller seller = getOrCreateSeller(hibernateSession, sessionUser);
                Product product = hibernateSession.find(Product.class, productDTO.getProductId());
                if (product == null) {
                    message = "Product not found!";
                } else if (seller == null || product.getSeller() == null || product.getSeller().getId() != seller.getId()) {
                    message = "You do not have permission to edit this product.";
                } else {
                    product.setTitle(productDTO.getTitle());
                    if (productDTO.getDescription() != null && !productDTO.getDescription().isBlank()) {
                        product.setDescription(productDTO.getDescription());
                    }

                    // Update the latest stock's price and qty
                    List<Stock> stocks = hibernateSession.createQuery(
                            "FROM Stock s WHERE s.product=:product ORDER BY s.id DESC", Stock.class)
                            .setParameter("product", product)
                            .setMaxResults(1)
                            .getResultList();

                    transaction = hibernateSession.beginTransaction();
                    hibernateSession.update(product);

                    if (!stocks.isEmpty()) {
                        Stock latestStock = stocks.get(0);
                        latestStock.setPrice(productDTO.getPrice());
                        latestStock.setQty(productDTO.getQty());
                        hibernateSession.update(latestStock);
                    }

                    transaction.commit();
                    status = true;
                    message = "Product updated successfully!";
                }
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                e.printStackTrace();
                message = "Failed to update product: " + e.getMessage();
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String deleteUserProduct(int productId, HttpServletRequest request) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null || httpSession.getAttribute("user") == null) {
            message = "Session expired. Please log in!";
        } else if (productId <= 0) {
            message = "Invalid product ID!";
        } else {
            User sessionUser = (User) httpSession.getAttribute("user");
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            Transaction transaction = null;
            try {
                Seller seller = getOrCreateSeller(hibernateSession, sessionUser);
                Product product = hibernateSession.find(Product.class, productId);
                if (product == null) {
                    message = "Product not found!";
                } else if (!sessionUser.isAdmin() && (seller == null || product.getSeller() == null || product.getSeller().getId() != seller.getId())) {
                    message = "You do not have permission to delete this product.";
                } else {
                    // Check if any placed customer orders reference this product's stock
                    Long orderItemCount = hibernateSession.createQuery(
                            "SELECT COUNT(oi) FROM OrderItem oi WHERE oi.stock.product.id = :productId", Long.class)
                            .setParameter("productId", productId)
                            .getSingleResult();

                    if (orderItemCount > 0) {
                        message = "Cannot delete product: It is associated with placed customer orders.";
                    } else {
                        transaction = hibernateSession.beginTransaction();

                        // 1. Delete cart items referencing stocks of this product
                        hibernateSession.createQuery(
                                "DELETE FROM Cart c WHERE c.stock.id IN (SELECT s.id FROM Stock s WHERE s.product.id = :productId)")
                                .setParameter("productId", productId)
                                .executeUpdate();

                        // 1.1 Delete wishlist items referencing this product
                        hibernateSession.createQuery(
                                "DELETE FROM Wishlist w WHERE w.product.id = :productId")
                                .setParameter("productId", productId)
                                .executeUpdate();

                        // 2. Delete stocks for this product
                        hibernateSession.createQuery(
                                "DELETE FROM Stock s WHERE s.product.id = :productId")
                                .setParameter("productId", productId)
                                .executeUpdate();

                        // 3. Delete product images collection
                        hibernateSession.createNativeQuery(
                                "DELETE FROM product_images WHERE pr_id = :productId")
                                .setParameter("productId", productId)
                                .executeUpdate();

                        // 4. Delete the product itself
                        hibernateSession.createQuery(
                                "DELETE FROM Product p WHERE p.id = :productId")
                                .setParameter("productId", productId)
                                .executeUpdate();

                        transaction.commit();
                        status = true;
                        message = "Product deleted successfully!";
                    }
                }
            } catch (Exception e) {
                if (transaction != null && transaction.isActive()) {
                    transaction.rollback();
                }
                e.printStackTrace();
                message = "Failed to delete product: " + (e.getMessage() != null ? e.getMessage() : e.toString());
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }
}
