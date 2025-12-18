package dao.impl;

import dao.CategoryDAO;
import app.Category;
import database.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAOImpl implements CategoryDAO {

    @Override
    public List<Category> findAll() {
        List<Category> categories = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return categories;
            ResultSet rs = con.createStatement().executeQuery("SELECT id, name FROM Category");
            while (rs.next()) {
                categories.add(new Category(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @Override
    public Category findById(int id) {
        try (Connection con = Database.connect()) {
            if (con == null) return null;
            PreparedStatement ps = con.prepareStatement("SELECT id, name FROM Category WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Category(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Category findByName(String name) {
        try (Connection con = Database.connect()) {
            if (con == null) return null;
            PreparedStatement ps = con.prepareStatement("SELECT id, name FROM Category WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Category(rs.getInt("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insert(Category category) {
        try (Connection con = Database.connect()) {
            if (con == null) return -1;
            PreparedStatement ps = con.prepareStatement("INSERT INTO Category(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, category.getName());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean update(Category category) {
        try (Connection con = Database.connect()) {
            if (con == null) return false;
            PreparedStatement ps = con.prepareStatement("UPDATE Category SET name=? WHERE id=?");
            ps.setString(1, category.getName());
            ps.setInt(2, category.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        try (Connection con = Database.connect()) {
            if (con == null) return false;
            PreparedStatement ps = con.prepareStatement("DELETE FROM Category WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getAllCategoryNames() {
        List<String> names = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return names;
            ResultSet rs = con.createStatement().executeQuery("SELECT name FROM Category");
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }
}

