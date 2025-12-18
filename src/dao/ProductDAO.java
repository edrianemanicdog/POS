package dao;

import app.Product;
import java.util.List;

public interface ProductDAO {
    List<Product> findAll();
    Product findById(int id);
    Product findByName(String name);
    List<Product> findByCategory(String categoryName);
    List<Product> findBySupplier(String supplierName);
    int insert(Product product);
    boolean update(Product product);
    boolean delete(int id);
    boolean updateStock(int productId, int newStock);
}

