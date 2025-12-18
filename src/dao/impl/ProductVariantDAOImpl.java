package dao.impl;

import dao.ProductVariantDAO;
import app.ProductVariant;
import database.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductVariantDAOImpl implements ProductVariantDAO {

    @Override
    public List<ProductVariant> findAll() {
        List<ProductVariant> variants = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return variants;
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT pv.id, pv.name, pv.sku, pv.barcode, pv.price, pv.cost, pv.stock, pv.attributes, pv.isActive, p.name AS productName " +
                "FROM ProductVariant pv " +
                "LEFT JOIN Product p ON pv.productId = p.id"
            );
            while (rs.next()) {
                variants.add(new ProductVariant(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("sku"),
                    rs.getString("barcode"),
                    rs.getDouble("price"),
                    rs.getDouble("cost"),
                    rs.getInt("stock"),
                    rs.getString("attributes"),
                    rs.getBoolean("isActive"),
                    rs.getString("productName")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return variants;
    }

    @Override
    public List<ProductVariant> findByProductId(int productId) {
        List<ProductVariant> variants = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return variants;
            PreparedStatement ps = con.prepareStatement(
                "SELECT pv.id, pv.name, pv.sku, pv.barcode, pv.price, pv.cost, pv.stock, pv.attributes, pv.isActive, p.name AS productName " +
                "FROM ProductVariant pv " +
                "LEFT JOIN Product p ON pv.productId = p.id " +
                "WHERE pv.productId = ?"
            );
            ps.setInt(1, productId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                variants.add(new ProductVariant(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("sku"),
                    rs.getString("barcode"),
                    rs.getDouble("price"),
                    rs.getDouble("cost"),
                    rs.getInt("stock"),
                    rs.getString("attributes"),
                    rs.getBoolean("isActive"),
                    rs.getString("productName")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return variants;
    }

    @Override
    public ProductVariant findById(int id) {
        try (Connection con = Database.connect()) {
            if (con == null) return null;
            PreparedStatement ps = con.prepareStatement(
                "SELECT pv.id, pv.name, pv.sku, pv.barcode, pv.price, pv.cost, pv.stock, pv.attributes, pv.isActive, p.name AS productName " +
                "FROM ProductVariant pv " +
                "LEFT JOIN Product p ON pv.productId = p.id " +
                "WHERE pv.id = ?"
            );
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new ProductVariant(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("sku"),
                    rs.getString("barcode"),
                    rs.getDouble("price"),
                    rs.getDouble("cost"),
                    rs.getInt("stock"),
                    rs.getString("attributes"),
                    rs.getBoolean("isActive"),
                    rs.getString("productName")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insert(ProductVariant variant) {
        try (Connection con = Database.connect()) {
            if (con == null) return -1;
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO ProductVariant(productId, name, sku, barcode, price, cost, stock, attributes, isActive) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, variant.getProductId());
            ps.setString(2, variant.getName());
            ps.setString(3, variant.getSku());
            ps.setString(4, variant.getBarcode());
            ps.setDouble(5, variant.getPrice());
            if (variant.getCost() != null) {
                ps.setDouble(6, variant.getCost());
            } else {
                ps.setNull(6, Types.DECIMAL);
            }
            ps.setInt(7, variant.getStock());
            ps.setString(8, variant.getAttributes());
            ps.setBoolean(9, variant.isIsActive());
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
    public boolean update(ProductVariant variant) {
        try (Connection con = Database.connect()) {
            if (con == null) return false;
            PreparedStatement ps = con.prepareStatement(
                "UPDATE ProductVariant SET productId=?, name=?, sku=?, barcode=?, price=?, cost=?, stock=?, attributes=?, isActive=? WHERE id=?"
            );
            ps.setInt(1, variant.getProductId());
            ps.setString(2, variant.getName());
            ps.setString(3, variant.getSku());
            ps.setString(4, variant.getBarcode());
            ps.setDouble(5, variant.getPrice());
            if (variant.getCost() != null) {
                ps.setDouble(6, variant.getCost());
            } else {
                ps.setNull(6, Types.DECIMAL);
            }
            ps.setInt(7, variant.getStock());
            ps.setString(8, variant.getAttributes());
            ps.setBoolean(9, variant.isIsActive());
            ps.setInt(10, variant.getId());
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
            PreparedStatement ps = con.prepareStatement("DELETE FROM ProductVariant WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}

