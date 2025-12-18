package dao.impl;

import dao.ProductModifierDAO;
import app.ProductModifier;
import database.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductModifierDAOImpl implements ProductModifierDAO {

    @Override
    public List<ProductModifier> findAll() {
        List<ProductModifier> modifiers = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return modifiers;
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT pm.id, pm.productId, pm.name, pm.type, pm.required, p.name AS productName " +
                "FROM ProductModifier pm " +
                "LEFT JOIN Product p ON pm.productId = p.id"
            );
            while (rs.next()) {
                modifiers.add(mapResultSetToModifier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modifiers;
    }

    @Override
    public List<ProductModifier> findByProductId(int productId) {
        List<ProductModifier> modifiers = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return modifiers;
            PreparedStatement ps = con.prepareStatement(
                "SELECT pm.id, pm.productId, pm.name, pm.type, pm.required, p.name AS productName " +
                "FROM ProductModifier pm " +
                "LEFT JOIN Product p ON pm.productId = p.id " +
                "WHERE pm.productId = ?"
            );
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                modifiers.add(mapResultSetToModifier(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return modifiers;
    }

    @Override
    public ProductModifier findById(int id) {
        try (Connection con = Database.connect()) {
            if (con == null) return null;
            PreparedStatement ps = con.prepareStatement(
                "SELECT pm.id, pm.productId, pm.name, pm.type, pm.required, p.name AS productName " +
                "FROM ProductModifier pm " +
                "LEFT JOIN Product p ON pm.productId = p.id " +
                "WHERE pm.id = ?"
            );
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToModifier(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insert(ProductModifier modifier) {
        try (Connection con = Database.connect()) {
            if (con == null) return -1;
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO ProductModifier(productId, name, type, required) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, modifier.getProductId());
            ps.setString(2, modifier.getName());
            ps.setString(3, modifier.getType());
            ps.setBoolean(4, modifier.isRequired());
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
    public boolean update(ProductModifier modifier) {
        try (Connection con = Database.connect()) {
            if (con == null) return false;
            PreparedStatement ps = con.prepareStatement(
                "UPDATE ProductModifier SET productId=?, name=?, type=?, required=? WHERE id=?"
            );
            ps.setInt(1, modifier.getProductId());
            ps.setString(2, modifier.getName());
            ps.setString(3, modifier.getType());
            ps.setBoolean(4, modifier.isRequired());
            ps.setInt(5, modifier.getId());
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
            PreparedStatement ps = con.prepareStatement("DELETE FROM ProductModifier WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ProductModifier mapResultSetToModifier(ResultSet rs) throws SQLException {
        ProductModifier modifier = new ProductModifier();
        modifier.setId(rs.getInt("id"));
        modifier.setProductId(rs.getInt("productId"));
        modifier.setName(rs.getString("name"));
        modifier.setType(rs.getString("type"));
        modifier.setRequired(rs.getBoolean("required"));
        modifier.setProductName(rs.getString("productName"));
        return modifier;
    }
}

