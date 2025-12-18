package dao;

import app.Supplier;
import java.util.List;

public interface SupplierDAO {
    List<Supplier> findAll();
    Supplier findById(int id);
    Supplier findByName(String name);
    int insert(Supplier supplier);
    boolean update(Supplier supplier);
    boolean delete(int id);
    List<String> getAllSupplierNames();
}

