package dao.impl;

import dao.ProductBundleDAO;
import app.ProductBundle;
import database.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductBundleDAOImpl implements ProductBundleDAO {

    @Override
    public List<ProductBundle> findAll() {
        List<ProductBundle> bundles = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return bundles;
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT pb.id, pb.bundleProductId, pb.itemProductId, pb.quantity, " +
                "bp.name AS bundleProductName, ip.name AS itemProductName " +
                "FROM ProductBundle pb " +
                "LEFT JOIN Product bp ON pb.bundleProductId = bp.id " +
                "LEFT JOIN Product ip ON pb.itemProductId = ip.id"
            );
            while (rs.next()) {
                bundles.add(mapResultSetToBundle(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bundles;
    }

    @Override
    public List<ProductBundle> findByBundleProductId(int bundleProductId) {
        List<ProductBundle> bundles = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return bundles;
            PreparedStatement ps = con.prepareStatement(
                "SELECT pb.id, pb.bundleProductId, pb.itemProductId, pb.quantity, " +
                "bp.name AS bundleProductName, ip.name AS itemProductName " +
                "FROM ProductBundle pb " +
                "LEFT JOIN Product bp ON pb.bundleProductId = bp.id " +
                "LEFT JOIN Product ip ON pb.itemProductId = ip.id " +
                "WHERE pb.bundleProductId = ?"
            );
            ps.setInt(1, bundleProductId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                bundles.add(mapResultSetToBundle(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bundles;
    }

    @Override
    public ProductBundle findById(int id) {
        try (Connection con = Database.connect()) {
            if (con == null) return null;
            PreparedStatement ps = con.prepareStatement(
                "SELECT pb.id, pb.bundleProductId, pb.itemProductId, pb.quantity, " +
                "bp.name AS bundleProductName, ip.name AS itemProductName " +
                "FROM ProductBundle pb " +
                "LEFT JOIN Product bp ON pb.bundleProductId = bp.id " +
                "LEFT JOIN Product ip ON pb.itemProductId = ip.id " +
                "WHERE pb.id = ?"
            );
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToBundle(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insert(ProductBundle bundle) {
        try (Connection con = Database.connect()) {
            if (con == null) return -1;
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO ProductBundle(bundleProductId, itemProductId, quantity) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setInt(1, bundle.getBundleProductId());
            ps.setInt(2, bundle.getItemProductId());
            ps.setInt(3, bundle.getQuantity());
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
    public boolean update(ProductBundle bundle) {
        try (Connection con = Database.connect()) {
            if (con == null) return false;
            PreparedStatement ps = con.prepareStatement(
                "UPDATE ProductBundle SET bundleProductId=?, itemProductId=?, quantity=? WHERE id=?"
            );
            ps.setInt(1, bundle.getBundleProductId());
            ps.setInt(2, bundle.getItemProductId());
            ps.setInt(3, bundle.getQuantity());
            ps.setInt(4, bundle.getId());
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
            PreparedStatement ps = con.prepareStatement("DELETE FROM ProductBundle WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private ProductBundle mapResultSetToBundle(ResultSet rs) throws SQLException {
        ProductBundle bundle = new ProductBundle(
            rs.getInt("id"),
            rs.getInt("bundleProductId"),
            rs.getInt("itemProductId"),
            rs.getInt("quantity")
        );
        bundle.setBundleProductName(rs.getString("bundleProductName"));
        bundle.setItemProductName(rs.getString("itemProductName"));
        return bundle;
    }
}

