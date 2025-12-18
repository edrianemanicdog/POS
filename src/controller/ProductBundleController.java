package controller;

import app.ProductBundle;
import database.Database;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class ProductBundleController {

    @FXML private TableView<ProductBundle> tableBundles;
    @FXML private TableColumn<ProductBundle, Integer> colId;
    @FXML private TableColumn<ProductBundle, String> colBundleProduct;
    @FXML private TableColumn<ProductBundle, String> colItemProduct;
    @FXML private TableColumn<ProductBundle, Integer> colQuantity;

    @FXML private ComboBox<String> cbBundleProduct, cbItemProduct;
    @FXML private TextField txtQuantity;

    private ObservableList<ProductBundle> bundles = FXCollections.observableArrayList();
    private ObservableList<String> products = FXCollections.observableArrayList();
    private Integer filteredProductId = null;
    private String filteredProductName = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colBundleProduct.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBundleProductName()));
        colItemProduct.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getItemProductName()));
        colQuantity.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());

        tableBundles.setItems(bundles);
        loadProducts();
        loadBundles();

        tableBundles.setOnMouseClicked(e -> {
            ProductBundle b = tableBundles.getSelectionModel().getSelectedItem();
            if (b != null) {
                cbBundleProduct.setValue(b.getBundleProductName());
                cbItemProduct.setValue(b.getItemProductName());
                txtQuantity.setText(String.valueOf(b.getQuantity()));
            }
        });
    }

    private void loadProducts() {
        products.clear();
        try (Connection con = Database.connect()) {
            String query = "SELECT name FROM Product";
            if (filteredProductId != null) {
                query += " WHERE id = " + filteredProductId;
            }
            query += " ORDER BY name";
            ResultSet rs = con.createStatement().executeQuery(query);
            while (rs.next()) products.add(rs.getString("name"));
            if (cbBundleProduct != null) {
                cbBundleProduct.setItems(products);
                // Auto-select filtered product if set
                if (filteredProductName != null && products.contains(filteredProductName)) {
                    cbBundleProduct.setValue(filteredProductName);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        
        // Item product can be any product - load separately
        try (Connection con = Database.connect()) {
            ObservableList<String> allProducts = FXCollections.observableArrayList();
            ResultSet rsAll = con.createStatement().executeQuery("SELECT name FROM Product ORDER BY name");
            while (rsAll.next()) allProducts.add(rsAll.getString("name"));
            if (cbItemProduct != null) cbItemProduct.setItems(allProducts);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadBundles() {
        bundles.clear();
        try (Connection con = Database.connect()) {
            String query = "SELECT pb.id, pb.bundleProductId, pb.itemProductId, pb.quantity, " +
                "bp.name AS bundleProductName, ip.name AS itemProductName " +
                "FROM ProductBundle pb " +
                "LEFT JOIN Product bp ON pb.bundleProductId = bp.id " +
                "LEFT JOIN Product ip ON pb.itemProductId = ip.id";
            if (filteredProductId != null) {
                query += " WHERE pb.bundleProductId = " + filteredProductId;
            }
            ResultSet rs = con.createStatement().executeQuery(query);
            while (rs.next()) {
                ProductBundle bundle = new ProductBundle(
                        rs.getInt("id"),
                        rs.getInt("bundleProductId"),
                        rs.getInt("itemProductId"),
                        rs.getInt("quantity")
                );
                bundle.setBundleProductName(rs.getString("bundleProductName"));
                bundle.setItemProductName(rs.getString("itemProductName"));
                bundles.add(bundle);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    /** Set product filter to show only bundles for a specific product */
    public void setProductFilter(int productId, String productName) {
        this.filteredProductId = productId;
        this.filteredProductName = productName;
        // Reload data with filter
        loadProducts();
        loadBundles();
    }

    @FXML
    public void addBundle() {
        String bundleProductName = cbBundleProduct.getValue();
        String itemProductName = cbItemProduct.getValue();
        String quantityText = txtQuantity.getText().trim();
        
        if (bundleProductName == null || itemProductName == null || quantityText.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }
        
        if (bundleProductName.equals(itemProductName)) {
            showAlert("Error", "Bundle product and item product cannot be the same");
            return;
        }

        try (Connection con = Database.connect()) {
            // Get bundle product ID
            int bundleProductId = 0;
            ResultSet rs = con.createStatement().executeQuery("SELECT id FROM Product WHERE name='" + bundleProductName + "'");
            if (rs.next()) bundleProductId = rs.getInt("id");
            
            // Get item product ID
            int itemProductId = 0;
            rs = con.createStatement().executeQuery("SELECT id FROM Product WHERE name='" + itemProductName + "'");
            if (rs.next()) itemProductId = rs.getInt("id");
            
            if (bundleProductId == 0 || itemProductId == 0) {
                showAlert("Error", "Could not find one or both products");
                return;
            }
            
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showAlert("Error", "Quantity must be greater than 0");
                return;
            }
            
            String sql = "INSERT INTO ProductBundle(bundleProductId, itemProductId, quantity) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, bundleProductId);
            ps.setInt(2, itemProductId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
            
            loadBundles(); // Reload table to ensure consistency
            clearForm();
            showAlert("Success", "Bundle added successfully!");
        } catch (SQLException | NumberFormatException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to add bundle: " + e.getMessage());
        }
    }

    @FXML
    public void updateBundle() {
        ProductBundle b = tableBundles.getSelectionModel().getSelectedItem();
        if (b == null) {
            showAlert("Error", "Please select a bundle to update");
            return;
        }

        String bundleProductName = cbBundleProduct.getValue();
        String itemProductName = cbItemProduct.getValue();
        String quantityText = txtQuantity.getText().trim();
        
        if (bundleProductName == null || itemProductName == null || quantityText.isEmpty()) {
            showAlert("Error", "Please fill in all fields");
            return;
        }
        
        if (bundleProductName.equals(itemProductName)) {
            showAlert("Error", "Bundle product and item product cannot be the same");
            return;
        }

        try (Connection con = Database.connect()) {
            // Get bundle product ID
            int bundleProductId = 0;
            ResultSet rs = con.createStatement().executeQuery("SELECT id FROM Product WHERE name='" + bundleProductName + "'");
            if (rs.next()) bundleProductId = rs.getInt("id");
            
            // Get item product ID
            int itemProductId = 0;
            rs = con.createStatement().executeQuery("SELECT id FROM Product WHERE name='" + itemProductName + "'");
            if (rs.next()) itemProductId = rs.getInt("id");
            
            if (bundleProductId == 0 || itemProductId == 0) {
                showAlert("Error", "Could not find one or both products");
                return;
            }
            
            int quantity = Integer.parseInt(quantityText);
            if (quantity <= 0) {
                showAlert("Error", "Quantity must be greater than 0");
                return;
            }
            
            String sql = "UPDATE ProductBundle SET bundleProductId=?, itemProductId=?, quantity=? WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, bundleProductId);
            ps.setInt(2, itemProductId);
            ps.setInt(3, quantity);
            ps.setInt(4, b.getId());
            ps.executeUpdate();

            loadBundles(); // Reload table to ensure consistency
            showAlert("Updated", "Bundle updated successfully!");
        } catch (SQLException | NumberFormatException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to update bundle: " + e.getMessage());
        }
    }

    @FXML
    public void deleteBundle() {
        ProductBundle b = tableBundles.getSelectionModel().getSelectedItem();
        if (b == null) return;

        try (Connection con = Database.connect()) {
            String sql = "DELETE FROM ProductBundle WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, b.getId());
            ps.executeUpdate();

            loadBundles(); // Reload table to ensure consistency
            clearForm();
            showAlert("Deleted", "Bundle deleted successfully!");
        } catch (SQLException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to delete bundle: " + e.getMessage());
        }
    }

    @FXML
    public void clearForm() {
        if (cbBundleProduct != null) cbBundleProduct.getSelectionModel().clearSelection();
        if (cbItemProduct != null) cbItemProduct.getSelectionModel().clearSelection();
        txtQuantity.clear();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }
    
    private void showAlert(String title, String msg, Alert.AlertType type) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }
}
