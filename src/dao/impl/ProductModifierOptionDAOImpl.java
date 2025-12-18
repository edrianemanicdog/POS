package dao.impl;

import dao.ProductModifierOptionDAO;
import app.ProductModifierOption;
import database.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductModifierOptionDAOImpl implements ProductModifierOptionDAO {

    @Override
    public List<ProductModifierOption> findAll() {
        List<ProductModifierOption> options = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return options;
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT pmo.id, pmo.modifierId, pmo.name, pmo.price, " +
                "CONCAT(p.name, ' - ', pm.name) AS modifierName " +
                "FROM ProductModifierOption pmo " +
                "LEFT JOIN ProductModifier pm ON pmo.modifierId = pm.id " +
                "LEFT JOIN Product p ON pm.productId = p.id"
            );
            while (rs.next()) {
                options.add(mapResultSetToOption(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return options;
    }

    @Override
    public List<ProductModifierOption> findByModifierId(int modifierId) {
        List<ProductModifierOption> options = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return options;
            PreparedStatement ps = con.prepareStatement(
                "SELECT pmo.id, pmo.modifierId, pmo.name, pmo.price, " +
                "CONCAT(p.name, ' - ', pm.name) AS modifierName " +
                "FROM ProductModifierOption pmo " +
                "LEFT JOIN ProductModifier pm ON pmo.modifierId = pm.id " +
                "LEFT JOIN Product p ON pm.productId = p.id " +
                "WHERE pmo.modifierId = ?"
            );
            ps.setInt(1, modifierId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                options.add(mapResultSetToOption(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return options;
    }

    @Override
    public List<ProductModifierOption> findByProductId(int productId) {
        List<ProductModifierOption> options = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return options;
            PreparedStatement ps = con.prepareStatement(
                "SELECT pmo.id, pmo.modifierId, pmo.name, pmo.price, " +
                "CONCAT(p.name, ' - ', pm.name) AS modifierName " +
                "FROM ProductModifierOption pmo " +
                "LEFT JOIN ProductModifier pm ON pmo.modifierId = pm.id " +
                "LEFT JOIN Product p ON pm.productId = p.id " +
                "WHERE pm.productId = ?"
            );
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                options.add(mapResultSetToOption(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return options;
    }

    @Override
    public ProductModifierOption findById(int id) {
        try (Connection con = Database.connect()) {
            if (con == null) return null;
            PreparedStatement ps = con.prepareStatement(
                "SELECT pmo.id, pmo.modifierId, pmo.name, pmo.price, " +
                "CONCAT(p.name, ' - ', pm.name) AS modifierName " +
                "FROM ProductModifierOption pmo " +
                "LEFT JOIN ProductModifier pm ON pmo.modifierId = pm.id " +
                "LEFT JOIN Product p ON pm.productId = p.id " +
                "WHERE pmo.id = ?"
            );
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToOption(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insert(ProductModifierOption option) {
        try (Connection con = Database.connect()) {
            if (con == null) return -1;
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO ProductModifierOption(name, price, modifierId) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, option.getName());
            ps.setDouble(2, option.getPrice());
            ps.setInt(3, option.getModifierId());
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
    public boolean update(ProductModifierOption option) {
        try (Connection con = Database.connect()) {
            if (con == null) return false;
            PreparedStatement ps = con.prepareStatement(
                "UPDATE ProductModifierOption SET name=?, price=?, modifierId=? WHERE id=?"
            );
            ps.setString(1, option.getName());
            ps.setDouble(2, option.getPrice());
            ps.setInt(3, option.getModifierId());
            ps.setInt(4, option.getId());
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
            PreparedStatement ps = con.prepareStatement("DELETE FROM ProductModifierOption WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ProductModifierOption mapResultSetToOption(ResultSet rs) throws SQLException {
        ProductModifierOption option = new ProductModifierOption(
            rs.getInt("id"),
            rs.getInt("modifierId"),
            rs.getString("name"),
            rs.getDouble("price")
        );
        option.setModifierName(rs.getString("modifierName"));
        return option;
    }
}

