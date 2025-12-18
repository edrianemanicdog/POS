package dao;

import app.ProductModifierOption;
import java.util.List;

public interface ProductModifierOptionDAO {
    List<ProductModifierOption> findAll();
    List<ProductModifierOption> findByModifierId(int modifierId);
    List<ProductModifierOption> findByProductId(int productId);
    ProductModifierOption findById(int id);
    int insert(ProductModifierOption option);
    boolean update(ProductModifierOption option);
    boolean delete(int id);
}

