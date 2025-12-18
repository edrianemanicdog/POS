package dao;

import app.ProductBundle;
import java.util.List;

public interface ProductBundleDAO {
    List<ProductBundle> findAll();
    List<ProductBundle> findByBundleProductId(int bundleProductId);
    ProductBundle findById(int id);
    int insert(ProductBundle bundle);
    boolean update(ProductBundle bundle);
    boolean delete(int id);
}

