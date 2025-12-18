package dao;

import app.ProductVariant;
import java.util.List;

public interface ProductVariantDAO {
    List<ProductVariant> findAll();
    List<ProductVariant> findByProductId(int productId);
    ProductVariant findById(int id);
    int insert(ProductVariant variant);
    boolean update(ProductVariant variant);
    boolean delete(int id);
}

