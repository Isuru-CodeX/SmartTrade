package lk.jiat.smarttrade.service;

import com.google.gson.JsonObject;
import lk.jiat.smarttrade.dto.ProductDTO;
import lk.jiat.smarttrade.dto.StockDTO;
import lk.jiat.smarttrade.entity.*;
import lk.jiat.smarttrade.util.AppUtil;
import lk.jiat.smarttrade.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.ArrayList;
import java.util.List;

public class ContentService {
    public String loadNewArrivalProducts() {
        JsonObject responseObject = new JsonObject();
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        List<Product> productList = hibernateSession.createQuery(
                "FROM Product p WHERE NOT EXISTS (" +
                "   SELECT s FROM Stock s WHERE s.product = p AND s.status.value = 'REJECTED'" +
                ") ORDER BY p.createdAt DESC", Product.class)
                .setMaxResults(10)
                .getResultList();
        List<ProductDTO> productDTOList = new ArrayList<>();
        for (Product product : productList) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setProductId(product.getId());
            productDTO.setTitle(product.getTitle());
            productDTO.setColorId(product.getColor().getId());
            productDTO.setColorValue(product.getColor().getValue());
            productDTO.setImages(product.getImages() != null ? new ArrayList<>(product.getImages()) : new ArrayList<>());
            List<StockDTO> stockDTOList = new ArrayList<>();
            String pStatus = "APPROVED";
            for (Stock stock : product.getStocks()) {
                StockDTO stockDTO = new StockDTO();
                stockDTO.setProductId(product.getId());
                stockDTO.setStockId(stock.getId());
                stockDTO.setQty(stock.getQty());
                stockDTO.setPrice(stock.getPrice());
                if (stock.getStatus() != null) {
                    pStatus = stock.getStatus().getValue();
                    stockDTO.setStatus(pStatus);
                }
                stockDTOList.add(stockDTO);
            }
            productDTO.setStatus(pStatus);
            productDTO.setStockDTOList(stockDTOList);
            productDTOList.add(productDTO);
        }
        hibernateSession.close();
        responseObject.add("newArrivals", AppUtil.GSON.toJsonTree(productDTOList));
        return AppUtil.GSON.toJson(responseObject);
    }

    /**
     * Full catalogue (newest first) with brand / model / storage / condition details.
     * Used by the search page (client side filters + pagination) and the single product page.
     * Rejected products are excluded.
     */
    public String loadAllProducts() {
        JsonObject responseObject = new JsonObject();
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        try {
            List<Product> productList = hibernateSession.createQuery(
                    "FROM Product p WHERE NOT EXISTS (" +
                    "   SELECT s FROM Stock s WHERE s.product = p AND s.status.value = 'REJECTED'" +
                    ") ORDER BY p.createdAt DESC", Product.class)
                    .getResultList();
            List<ProductDTO> productDTOList = new ArrayList<>();
            for (Product product : productList) {
                ProductDTO productDTO = new ProductDTO();
                productDTO.setProductId(product.getId());
                productDTO.setTitle(product.getTitle());
                productDTO.setDescription(product.getDescription());
                productDTO.setColorId(product.getColor().getId());
                productDTO.setColorValue(product.getColor().getValue());
                productDTO.setStorageId(product.getStorage().getId());
                productDTO.setStorageValue(product.getStorage().getValue());
                productDTO.setQualityId(product.getQuality().getId());
                productDTO.setQualityValue(product.getQuality().getValue());
                productDTO.setModelId(product.getModel().getId());
                productDTO.setModelName(product.getModel().getName());
                productDTO.setBrandId(product.getModel().getBrand().getId());
                productDTO.setBrandName(product.getModel().getBrand().getName());
                productDTO.setImages(product.getImages() != null ? new ArrayList<>(product.getImages()) : new ArrayList<>());
                List<StockDTO> stockDTOList = new ArrayList<>();
                String pStatus = "APPROVED";
                for (Stock stock : product.getStocks()) {
                    StockDTO stockDTO = new StockDTO();
                    stockDTO.setProductId(product.getId());
                    stockDTO.setStockId(stock.getId());
                    stockDTO.setQty(stock.getQty());
                    stockDTO.setPrice(stock.getPrice());
                    stockDTO.setCreatedAt(String.valueOf(stock.getCreatedAt()));
                    if (stock.getStatus() != null) {
                        pStatus = stock.getStatus().getValue();
                        stockDTO.setStatus(pStatus);
                    }
                    stockDTOList.add(stockDTO);
                }
                productDTO.setStatus(pStatus);
                productDTO.setStockDTOList(stockDTOList);
                productDTOList.add(productDTO);
            }
            responseObject.add("products", AppUtil.GSON.toJsonTree(productDTOList));
        } finally {
            hibernateSession.close();
        }
        return AppUtil.GSON.toJson(responseObject);
    }

    public String loadProductSpecifications() {
        JsonObject responseObject = new JsonObject();
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();

        List<Storage> storageList = hibernateSession.createQuery("FROM Storage s", Storage.class)
                .getResultList();
        responseObject.add("storages", AppUtil.GSON.toJsonTree(storageList));

        List<Color> colorList = hibernateSession.createQuery("FROM Color c", Color.class)
                .getResultList();
        responseObject.add("colors", AppUtil.GSON.toJsonTree(colorList));

        List<Quality> qualityList = hibernateSession.createQuery("FROM Quality q", Quality.class)
                .getResultList();
        responseObject.add("qualities", AppUtil.GSON.toJsonTree(qualityList));

        hibernateSession.close();
        return AppUtil.GSON.toJson(responseObject);
    }

    public String loadBrandDetails() {
        JsonObject responseObject = new JsonObject();
        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        List<Brand> brandList = hibernateSession.createQuery("FROM Brand b ORDER BY b.name ASC", Brand.class).getResultList();
        responseObject.add("brands", AppUtil.GSON.toJsonTree(ContentService.brands(brandList)));
        hibernateSession.close();
        return AppUtil.GSON.toJson(responseObject);
    }

    public String loadModelDetails(int id) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (id <= 0) {
            message = "Please select a brand!";
        } else {
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            Brand brand = hibernateSession.find(Brand.class, id);
            if (brand == null) {
                message = "Please provide correct brand!";
            } else {
                List<Model> modelList = hibernateSession.createQuery("FROM Model m WHERE m.brand=:brand ORDER BY m.name ASC", Model.class)
                        .setParameter("brand", brand)
                        .getResultList();
                responseObject.add("models", AppUtil.GSON.toJsonTree(ContentService.models(modelList)));
                status = true;
                message = modelList.isEmpty() ? "No models found for this brand yet." : "Models data loading successful";
            }
            hibernateSession.close();
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String addBrand(String name) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (name == null || name.trim().isEmpty()) {
            message = "Brand name cannot be empty!";
        } else {
            String brandName = name.trim();
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            try {
                Brand existing = hibernateSession.createQuery("FROM Brand b WHERE LOWER(b.name) = LOWER(:name)", Brand.class)
                        .setParameter("name", brandName)
                        .uniqueResult();
                if (existing != null) {
                    JsonObject brandObj = new JsonObject();
                    brandObj.addProperty("id", existing.getId());
                    brandObj.addProperty("name", existing.getName());
                    responseObject.add("brand", brandObj);
                    status = true;
                    message = "Brand already exists";
                } else {
                    Brand brand = new Brand();
                    brand.setName(brandName);
                    Transaction tx = hibernateSession.beginTransaction();
                    hibernateSession.persist(brand);
                    tx.commit();

                    JsonObject brandObj = new JsonObject();
                    brandObj.addProperty("id", brand.getId());
                    brandObj.addProperty("name", brand.getName());
                    responseObject.add("brand", brandObj);
                    status = true;
                    message = "Brand added successfully!";
                }
            } catch (Exception e) {
                message = "Failed to save brand: " + e.getMessage();
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String addModel(int brandId, String name) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (brandId <= 0) {
            message = "Please select a valid brand first!";
        } else if (name == null || name.trim().isEmpty()) {
            message = "Model name cannot be empty!";
        } else {
            String modelName = name.trim();
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            try {
                Brand brand = hibernateSession.find(Brand.class, brandId);
                if (brand == null) {
                    message = "Selected brand does not exist!";
                } else {
                    Model existing = hibernateSession.createQuery("FROM Model m WHERE LOWER(m.name) = LOWER(:name) AND m.brand.id = :brandId", Model.class)
                            .setParameter("name", modelName)
                            .setParameter("brandId", brandId)
                            .uniqueResult();
                    if (existing != null) {
                        JsonObject modelObj = new JsonObject();
                        modelObj.addProperty("id", existing.getId());
                        modelObj.addProperty("name", existing.getName());
                        modelObj.addProperty("brandId", brandId);
                        responseObject.add("model", modelObj);
                        status = true;
                        message = "Model already exists";
                    } else {
                        Model model = new Model();
                        model.setName(modelName);
                        model.setBrand(brand);
                        Transaction tx = hibernateSession.beginTransaction();
                        hibernateSession.persist(model);
                        tx.commit();

                        JsonObject modelObj = new JsonObject();
                        modelObj.addProperty("id", model.getId());
                        modelObj.addProperty("name", model.getName());
                        modelObj.addProperty("brandId", brandId);
                        responseObject.add("model", modelObj);
                        status = true;
                        message = "Model added successfully!";
                    }
                }
            } catch (Exception e) {
                message = "Failed to save model: " + e.getMessage();
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String addStorage(String value) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (value == null || value.trim().isEmpty()) {
            message = "Storage value cannot be empty!";
        } else {
            String storageVal = value.trim();
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            try {
                Storage existing = hibernateSession.createQuery("FROM Storage s WHERE LOWER(s.value) = LOWER(:val)", Storage.class)
                        .setParameter("val", storageVal)
                        .uniqueResult();
                if (existing != null) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", existing.getId());
                    obj.addProperty("value", existing.getValue());
                    responseObject.add("storage", obj);
                    status = true;
                    message = "Storage option already exists";
                } else {
                    Storage storage = new Storage();
                    storage.setValue(storageVal);
                    Transaction tx = hibernateSession.beginTransaction();
                    hibernateSession.persist(storage);
                    tx.commit();

                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", storage.getId());
                    obj.addProperty("value", storage.getValue());
                    responseObject.add("storage", obj);
                    status = true;
                    message = "Storage option added successfully!";
                }
            } catch (Exception e) {
                message = "Failed to save storage: " + e.getMessage();
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String addColor(String value) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        if (value == null || value.trim().isEmpty()) {
            message = "Colour name cannot be empty!";
        } else {
            String colorVal = value.trim();
            Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
            try {
                Color existing = hibernateSession.createQuery("FROM Color c WHERE LOWER(c.value) = LOWER(:val)", Color.class)
                        .setParameter("val", colorVal)
                        .uniqueResult();
                if (existing != null) {
                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", existing.getId());
                    obj.addProperty("value", existing.getValue());
                    responseObject.add("color", obj);
                    status = true;
                    message = "Colour option already exists";
                } else {
                    Color color = new Color();
                    color.setValue(colorVal);
                    Transaction tx = hibernateSession.beginTransaction();
                    hibernateSession.persist(color);
                    tx.commit();

                    JsonObject obj = new JsonObject();
                    obj.addProperty("id", color.getId());
                    obj.addProperty("value", color.getValue());
                    responseObject.add("color", obj);
                    status = true;
                    message = "Colour option added successfully!";
                }
            } catch (Exception e) {
                message = "Failed to save colour: " + e.getMessage();
            } finally {
                hibernateSession.close();
            }
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String deleteBrand(int id) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        try {
            Brand brand = hibernateSession.find(Brand.class, id);
            if (brand == null) {
                message = "Brand not found!";
            } else {
                long productCount = hibernateSession.createQuery("SELECT COUNT(p) FROM Product p WHERE p.model.brand.id = :id", Long.class)
                        .setParameter("id", id)
                        .uniqueResult();
                if (productCount > 0) {
                    message = "Cannot delete brand: it is associated with " + productCount + " active product(s).";
                } else {
                    Transaction tx = hibernateSession.beginTransaction();
                    // Delete any models under this brand if none have products
                    hibernateSession.createQuery("DELETE FROM Model m WHERE m.brand.id = :id")
                            .setParameter("id", id)
                            .executeUpdate();
                    hibernateSession.delete(brand);
                    tx.commit();
                    status = true;
                    message = "Brand deleted successfully!";
                }
            }
        } catch (Exception e) {
            message = "Failed to delete brand: " + e.getMessage();
        } finally {
            hibernateSession.close();
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    public String deleteModel(int id) {
        JsonObject responseObject = new JsonObject();
        boolean status = false;
        String message = "";

        Session hibernateSession = HibernateUtil.getSessionFactory().openSession();
        try {
            Model model = hibernateSession.find(Model.class, id);
            if (model == null) {
                message = "Model not found!";
            } else {
                long productCount = hibernateSession.createQuery("SELECT COUNT(p) FROM Product p WHERE p.model.id = :id", Long.class)
                        .setParameter("id", id)
                        .uniqueResult();
                if (productCount > 0) {
                    message = "Cannot delete model: it is associated with " + productCount + " active product(s).";
                } else {
                    Transaction tx = hibernateSession.beginTransaction();
                    hibernateSession.delete(model);
                    tx.commit();
                    status = true;
                    message = "Model deleted successfully!";
                }
            }
        } catch (Exception e) {
            message = "Failed to delete model: " + e.getMessage();
        } finally {
            hibernateSession.close();
        }

        responseObject.addProperty("status", status);
        responseObject.addProperty("message", message);
        return AppUtil.GSON.toJson(responseObject);
    }

    private static List<JsonObject> models(List<Model> modelList) {
        List<JsonObject> brandJson = new ArrayList<>();
        for (Model b : modelList) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", b.getId());
            obj.addProperty("name", b.getName());
            brandJson.add(obj);
        }
        return brandJson;
    }

    private static List<JsonObject> brands(List<Brand> brandList) {
        List<JsonObject> brandJson = new ArrayList<>();
        for (Brand b : brandList) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", b.getId());
            obj.addProperty("name", b.getName());
            brandJson.add(obj);
        }
        return brandJson;
    }

}
