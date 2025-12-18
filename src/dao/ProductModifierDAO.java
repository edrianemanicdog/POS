package dao;

import app.ProductModifier;
import java.util.List;

public interface ProductModifierDAO {
    List<ProductModifier> findAll();
    List<ProductModifier> findByProductId(int productId);
    ProductModifier findById(int id);
    int insert(ProductModifier modifier);
    boolean update(ProductModifier modifier);
    boolean delete(int id);
}

