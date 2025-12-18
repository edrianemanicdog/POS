package dao.impl;

import dao.ProductDAO;
import app.Product;
import database.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return products;
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT p.id, p.name, p.description, p.sku, p.barcode, p.inventoryTracking, p.baseUnit, " +
                "p.price, p.cost, p.initialStock, p.currentStock, p.reorderLevel, p.productType, p.isActive, " +
                "c.name AS category, s.name AS supplier " +
                "FROM Product p " +
                "LEFT JOIN Category c ON p.categoryId = c.id " +
                "LEFT JOIN Supplier s ON p.supplierId = s.id"
            );
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public Product findById(int id) {
        try (Connection con = Database.connect()) {
            if (con == null) return null;
            PreparedStatement ps = con.prepareStatement(
                "SELECT p.id, p.name, p.description, p.sku, p.barcode, p.inventoryTracking, p.baseUnit, " +
                "p.price, p.cost, p.initialStock, p.currentStock, p.reorderLevel, p.productType, p.isActive, " +
                "c.name AS category, s.name AS supplier " +
                "FROM Product p " +
                "LEFT JOIN Category c ON p.categoryId = c.id " +
                "LEFT JOIN Supplier s ON p.supplierId = s.id " +
                "WHERE p.id = ?"
            );
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Product findByName(String name) {
        try (Connection con = Database.connect()) {
            if (con == null) return null;
            PreparedStatement ps = con.prepareStatement(
                "SELECT p.id, p.name, p.description, p.sku, p.barcode, p.inventoryTracking, p.baseUnit, " +
                "p.price, p.cost, p.initialStock, p.currentStock, p.reorderLevel, p.productType, p.isActive, " +
                "c.name AS category, s.name AS supplier " +
                "FROM Product p " +
                "LEFT JOIN Category c ON p.categoryId = c.id " +
                "LEFT JOIN Supplier s ON p.supplierId = s.id " +
                "WHERE p.name = ?"
            );
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToProduct(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Product> findByCategory(String categoryName) {
        List<Product> products = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return products;
            PreparedStatement ps = con.prepareStatement(
                "SELECT p.id, p.name, p.description, p.sku, p.barcode, p.inventoryTracking, p.baseUnit, " +
                "p.price, p.cost, p.initialStock, p.currentStock, p.reorderLevel, p.productType, p.isActive, " +
                "c.name AS category, s.name AS supplier " +
                "FROM Product p " +
                "LEFT JOIN Category c ON p.categoryId = c.id " +
                "LEFT JOIN Supplier s ON p.supplierId = s.id " +
                "WHERE c.name = ?"
            );
            ps.setString(1, categoryName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findBySupplier(String supplierName) {
        List<Product> products = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return products;
            PreparedStatement ps = con.prepareStatement(
                "SELECT p.id, p.name, p.description, p.sku, p.barcode, p.inventoryTracking, p.baseUnit, " +
                "p.price, p.cost, p.initialStock, p.currentStock, p.reorderLevel, p.productType, p.isActive, " +
                "c.name AS category, s.name AS supplier " +
                "FROM Product p " +
                "LEFT JOIN Category c ON p.categoryId = c.id " +
                "LEFT JOIN Supplier s ON p.supplierId = s.id " +
                "WHERE s.name = ?"
            );
            ps.setString(1, supplierName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public int insert(Product product) {
        try (Connection con = Database.connect()) {
            if (con == null) return -1;
            
            // Get categoryId and supplierId
            Integer categoryId = null;
            if (product.getCategoryName() != null) {
                PreparedStatement psCat = con.prepareStatement("SELECT id FROM Category WHERE name = ?");
                psCat.setString(1, product.getCategoryName());
                ResultSet rsCat = psCat.executeQuery();
                if (rsCat.next()) {
                    categoryId = rsCat.getInt("id");
                }
            }
            
            Integer supplierId = null;
            if (product.getSupplierName() != null) {
                PreparedStatement psSup = con.prepareStatement("SELECT id FROM Supplier WHERE name = ?");
                psSup.setString(1, product.getSupplierName());
                ResultSet rsSup = psSup.executeQuery();
                if (rsSup.next()) {
                    supplierId = rsSup.getInt("id");
                }
            }
            
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO Product(name, description, categoryId, supplierId, sku, barcode, " +
                "inventoryTracking, baseUnit, price, cost, initialStock, currentStock, reorderLevel, " +
                "productType, isActive, image) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            if (categoryId != null) {
                ps.setInt(3, categoryId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            if (supplierId != null) {
                ps.setInt(4, supplierId);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, product.getSku());
            ps.setString(6, product.getBarcode());
            ps.setString(7, product.getInventoryTracking() != null ? product.getInventoryTracking() : "track_stock");
            ps.setString(8, product.getBaseUnit() != null ? product.getBaseUnit() : "piece");
            ps.setDouble(9, product.getPrice());
            if (product.getCost() != null) {
                ps.setDouble(10, product.getCost());
            } else {
                ps.setNull(10, Types.DECIMAL);
            }
            ps.setInt(11, product.getInitialStock());
            ps.setInt(12, product.getCurrentStock());
            ps.setInt(13, product.getReorderLevel());
            ps.setString(14, product.getProductType() != null ? product.getProductType() : "simple");
            ps.setBoolean(15, product.isIsActive());
            ps.setString(16, null); // image field - can be extended later
            
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
    public boolean update(Product product) {
        try (Connection con = Database.connect()) {
            if (con == null) return false;
            
            // Get categoryId and supplierId
            Integer categoryId = null;
            if (product.getCategoryName() != null) {
                PreparedStatement psCat = con.prepareStatement("SELECT id FROM Category WHERE name = ?");
                psCat.setString(1, product.getCategoryName());
                ResultSet rsCat = psCat.executeQuery();
                if (rsCat.next()) {
                    categoryId = rsCat.getInt("id");
                }
            }
            
            Integer supplierId = null;
            if (product.getSupplierName() != null) {
                PreparedStatement psSup = con.prepareStatement("SELECT id FROM Supplier WHERE name = ?");
                psSup.setString(1, product.getSupplierName());
                ResultSet rsSup = psSup.executeQuery();
                if (rsSup.next()) {
                    supplierId = rsSup.getInt("id");
                }
            }
            
            PreparedStatement ps = con.prepareStatement(
                "UPDATE Product SET name=?, description=?, categoryId=?, supplierId=?, sku=?, barcode=?, " +
                "inventoryTracking=?, baseUnit=?, price=?, cost=?, initialStock=?, currentStock=?, reorderLevel=?, " +
                "productType=?, isActive=? WHERE id=?"
            );
            ps.setString(1, product.getName());
            ps.setString(2, product.getDescription());
            if (categoryId != null) {
                ps.setInt(3, categoryId);
            } else {
                ps.setNull(3, Types.INTEGER);
            }
            if (supplierId != null) {
                ps.setInt(4, supplierId);
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setString(5, product.getSku());
            ps.setString(6, product.getBarcode());
            ps.setString(7, product.getInventoryTracking() != null ? product.getInventoryTracking() : "track_stock");
            ps.setString(8, product.getBaseUnit() != null ? product.getBaseUnit() : "piece");
            ps.setDouble(9, product.getPrice());
            if (product.getCost() != null) {
                ps.setDouble(10, product.getCost());
            } else {
                ps.setNull(10, Types.DECIMAL);
            }
            ps.setInt(11, product.getInitialStock());
            ps.setInt(12, product.getCurrentStock());
            ps.setInt(13, product.getReorderLevel());
            ps.setString(14, product.getProductType() != null ? product.getProductType() : "simple");
            ps.setBoolean(15, product.isIsActive());
            ps.setInt(16, product.getId());
            
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
            PreparedStatement ps = con.prepareStatement("DELETE FROM Product WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateStock(int productId, int newStock) {
        try (Connection con = Database.connect()) {
            if (con == null) return false;
            PreparedStatement ps = con.prepareStatement("UPDATE Product SET currentStock = ? WHERE id = ?");
            ps.setInt(1, newStock);
            ps.setInt(2, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getInt("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setSku(rs.getString("sku"));
        product.setBarcode(rs.getString("barcode"));
        product.setInventoryTracking(rs.getString("inventoryTracking"));
        product.setBaseUnit(rs.getString("baseUnit"));
        product.setPrice(rs.getDouble("price"));
        Object costObj = rs.getObject("cost");
        product.setCost(costObj != null ? rs.getDouble("cost") : null);
        product.setInitialStock(rs.getInt("initialStock"));
        product.setCurrentStock(rs.getInt("currentStock"));
        product.setReorderLevel(rs.getInt("reorderLevel"));
        product.setProductType(rs.getString("productType"));
        product.setIsActive(rs.getBoolean("isActive"));
        product.setCategoryName(rs.getString("category"));
        product.setSupplierName(rs.getString("supplier"));
        return product;
    }
}

