package dao;

import app.Category;
import java.util.List;

public interface CategoryDAO {
    List<Category> findAll();
    Category findById(int id);
    Category findByName(String name);
    int insert(Category category);
    boolean update(Category category);
    boolean delete(int id);
    List<String> getAllCategoryNames();
}

