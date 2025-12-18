package controller;

import app.Product;
import database.Database;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;

public class ProductController {

    @FXML private TableView<Product> tableProducts;
    @FXML private TableColumn<Product, Integer> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colDescription;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, Double> colPrice;
    @FXML private TableColumn<Product, Integer> colCurrentStock;
    @FXML private TableColumn<Product, Boolean> colIsActive;

    @FXML private TextField txtName, txtDescription, txtSku, txtBarcode, txtBaseUnit;
    @FXML private TextField txtPrice, txtCost, txtInitialStock, txtCurrentStock, txtReorderLevel, txtImage;
    @FXML private ComboBox<String> cbCategory, cbSupplier, cbInventoryTracking, cbProductType;
    @FXML private CheckBox chkIsActive;

    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ObservableList<String> categories = FXCollections.observableArrayList();
    private ObservableList<String> suppliers = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        try {
            // Setup table columns
            if (colId != null) colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
            if (colName != null) colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
            if (colDescription != null) colDescription.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription() != null ? data.getValue().getDescription() : ""));
            if (colCategory != null) colCategory.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getCategory() != null ? data.getValue().getCategory() : "N/A"));
            if (colPrice != null) colPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
            if (colCurrentStock != null) colCurrentStock.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCurrentStock()).asObject());
            if (colIsActive != null) {
                colIsActive.setCellValueFactory(data -> new javafx.beans.property.SimpleBooleanProperty(data.getValue().isIsActive()));
                colIsActive.setCellFactory(column -> new TableCell<Product, Boolean>() {
                    @Override
                    protected void updateItem(Boolean item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText("");
                        } else {
                            setText(item ? "Yes" : "No");
                            setStyle(item ? "-fx-text-fill: #27ae60;" : "-fx-text-fill: #e74c3c;");
                        }
                    }
                });
            }

            if (tableProducts != null) {
                tableProducts.setItems(products);
                tableProducts.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            }

            // Setup combo boxes
            if (cbInventoryTracking != null) {
                cbInventoryTracking.setItems(FXCollections.observableArrayList("track_stock", "bundle_recipe", "no_tracking"));
                cbInventoryTracking.setValue("track_stock");
            }
            if (cbProductType != null) {
                cbProductType.setItems(FXCollections.observableArrayList("simple", "variable"));
                cbProductType.setValue("simple");
            }
            if (txtBaseUnit != null) txtBaseUnit.setText("piece");

            // Load data with error handling
            try {
                loadCategories();
                loadSuppliers();
                loadProducts();
            } catch (Exception e) {
                System.err.println("Error loading data in ProductController: " + e.getMessage());
                e.printStackTrace();
                // Continue anyway - form will still work, just no data loaded
            }
        } catch (Exception e) {
            System.err.println("Error initializing ProductController: " + e.getMessage());
            e.printStackTrace();
            // Don't throw - let the form load even if initialization has issues
        }

        tableProducts.setOnMouseClicked(e -> {
            Product p = tableProducts.getSelectionModel().getSelectedItem();
            if (p != null) {
                txtName.setText(p.getName() != null ? p.getName() : "");
                txtDescription.setText(p.getDescription() != null ? p.getDescription() : "");
                txtSku.setText(p.getSku() != null ? p.getSku() : "");
                txtBarcode.setText(p.getBarcode() != null ? p.getBarcode() : "");
                txtBaseUnit.setText(p.getBaseUnit() != null ? p.getBaseUnit() : "piece");
                cbCategory.setValue(p.getCategory());
                cbSupplier.setValue(p.getSupplier());
                cbInventoryTracking.setValue(p.getInventoryTracking() != null ? p.getInventoryTracking() : "track_stock");
                cbProductType.setValue(p.getProductType() != null ? p.getProductType() : "simple");
                txtPrice.setText(String.valueOf(p.getPrice()));
                txtCost.setText(p.getCost() != null ? String.valueOf(p.getCost()) : "");
                txtInitialStock.setText(String.valueOf(p.getInitialStock()));
                txtCurrentStock.setText(String.valueOf(p.getCurrentStock()));
                txtReorderLevel.setText(String.valueOf(p.getReorderLevel()));
                chkIsActive.setSelected(p.isIsActive());
            }
        });
    }

    /** Load products from DB */
    public void loadProducts() {
        products.clear();
        try (Connection con = Database.connect()) {
            if (con == null) {
                System.err.println("Database connection is null");
                return;
            }
            
            // Start with basic query that should work with existing schema
            String query = "SELECT p.id, p.name, p.description, p.price, p.currentStock, " +
                    "c.name AS category, s.name AS supplier " +
                    "FROM Product p " +
                    "LEFT JOIN Category c ON p.categoryId = c.id " +
                    "LEFT JOIN Supplier s ON p.supplierId = s.id";

            ResultSet rs = con.createStatement().executeQuery(query);
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            // Build list of available columns
            Set<String> availableColumns = new HashSet<>();
            for (int i = 1; i <= columnCount; i++) {
                availableColumns.add(metaData.getColumnName(i).toLowerCase());
            }

            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                
                // Get new columns if they exist, otherwise use defaults
                if (availableColumns.contains("sku")) {
                    product.setSku(rs.getString("sku"));
                } else {
                    product.setSku(null);
                }
                
                if (availableColumns.contains("barcode")) {
                    product.setBarcode(rs.getString("barcode"));
                } else {
                    product.setBarcode(null);
                }
                
                if (availableColumns.contains("inventorytracking")) {
                    product.setInventoryTracking(rs.getString("inventoryTracking"));
                } else {
                    product.setInventoryTracking("track_stock");
                }
                
                if (availableColumns.contains("baseunit")) {
                    product.setBaseUnit(rs.getString("baseUnit"));
                } else {
                    product.setBaseUnit("piece");
                }
                
                product.setPrice(rs.getDouble("price"));
                
                if (availableColumns.contains("cost")) {
                    Double cost = rs.getObject("cost") != null ? rs.getDouble("cost") : null;
                    product.setCost(cost);
                } else {
                    product.setCost(null);
                }
                
                if (availableColumns.contains("initialstock")) {
                    product.setInitialStock(rs.getInt("initialStock"));
                } else {
                    product.setInitialStock(0);
                }
                
                product.setCurrentStock(rs.getInt("currentStock"));
                
                if (availableColumns.contains("reorderlevel")) {
                    product.setReorderLevel(rs.getInt("reorderLevel"));
                } else {
                    product.setReorderLevel(0);
                }
                
                if (availableColumns.contains("producttype")) {
                    product.setProductType(rs.getString("productType"));
                } else {
                    product.setProductType("simple");
                }
                
                if (availableColumns.contains("isactive")) {
                    product.setIsActive(rs.getBoolean("isActive"));
                } else {
                    product.setIsActive(true);
                }
                
                product.setCategoryName(rs.getString("category"));
                product.setSupplierName(rs.getString("supplier"));
                products.add(product);
            }
        } catch (SQLException e) { 
            System.err.println("Error loading products: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /** Load categories */
    public void loadCategories() {
        categories.clear();
        try (Connection con = Database.connect()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT name FROM Category");
            while (rs.next()) categories.add(rs.getString("name"));
            cbCategory.setItems(categories);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Load suppliers */
    public void loadSuppliers() {
        suppliers.clear();
        try (Connection con = Database.connect()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT name FROM Supplier");
            while (rs.next()) suppliers.add(rs.getString("name"));
            cbSupplier.setItems(suppliers);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Add Product */
    @FXML
    public void addProduct() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            showAlert("Error", "Product name is required!");
            return;
        }

        String description = txtDescription.getText().trim();
        String sku = txtSku.getText().trim();
        String barcode = txtBarcode.getText().trim();
        String baseUnit = txtBaseUnit.getText().trim();
        if (baseUnit.isEmpty()) baseUnit = "piece";
        
        String inventoryTracking = cbInventoryTracking.getValue() != null ? cbInventoryTracking.getValue() : "track_stock";
        String productType = cbProductType.getValue() != null ? cbProductType.getValue() : "simple";
        boolean isActive = chkIsActive.isSelected();
        String image = txtImage.getText().trim();

        double price;
        Double cost = null;
        int initialStock = 0;
        int currentStock = 0;
        int reorderLevel = 0;

        try {
            price = Double.parseDouble(txtPrice.getText().trim());
            if (!txtCost.getText().trim().isEmpty()) {
                cost = Double.parseDouble(txtCost.getText().trim());
            }
            if (!txtInitialStock.getText().trim().isEmpty()) {
                initialStock = Integer.parseInt(txtInitialStock.getText().trim());
            }
            if (!txtCurrentStock.getText().trim().isEmpty()) {
                currentStock = Integer.parseInt(txtCurrentStock.getText().trim());
            }
            if (!txtReorderLevel.getText().trim().isEmpty()) {
                reorderLevel = Integer.parseInt(txtReorderLevel.getText().trim());
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for price, cost, stock, and discount fields");
            return;
        }

        try (Connection con = Database.connect()) {
            Integer categoryId = null;
            if (cbCategory.getValue() != null) {
                ResultSet rs = con.createStatement().executeQuery("SELECT id FROM Category WHERE name='" + cbCategory.getValue() + "'");
                if (rs.next()) categoryId = rs.getInt("id");
            }

            Integer supplierId = null;
            if (cbSupplier.getValue() != null) {
                ResultSet rs = con.createStatement().executeQuery("SELECT id FROM Supplier WHERE name='" + cbSupplier.getValue() + "'");
                if (rs.next()) supplierId = rs.getInt("id");
            }

            String sql = "INSERT INTO Product(name, description, categoryId, supplierId, sku, barcode, " +
                    "inventoryTracking, baseUnit, price, cost, initialStock, currentStock, reorderLevel, " +
                    "productType, isActive, image, discount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, description.isEmpty() ? null : description);
            if (categoryId != null) ps.setInt(3, categoryId); else ps.setNull(3, Types.INTEGER);
            if (supplierId != null) ps.setInt(4, supplierId); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, sku.isEmpty() ? null : sku);
            ps.setString(6, barcode.isEmpty() ? null : barcode);
            ps.setString(7, inventoryTracking);
            ps.setString(8, baseUnit);
            ps.setDouble(9, price);
            if (cost != null) ps.setDouble(10, cost); else ps.setNull(10, Types.DECIMAL);
            ps.setInt(11, initialStock);
            ps.setInt(12, currentStock);
            ps.setInt(13, reorderLevel);
            ps.setString(14, productType);
            ps.setBoolean(15, isActive);
            ps.setString(16, image.isEmpty() ? null : image);
            ps.setDouble(17, 0.0); // product-level discount no longer used; keep column at 0
            ps.executeUpdate();

            loadProducts();
            clearForm();
            showAlert("Success", "Product added successfully!");
        } catch (SQLException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to add product: " + e.getMessage());
        }
    }

    /** Update Product */
    @FXML
    public void updateProduct() {
        Product p = tableProducts.getSelectionModel().getSelectedItem();
        if (p == null) {
            showAlert("Error", "Please select a product to update");
            return;
        }

        String name = txtName.getText().trim();
        if (name.isEmpty()) {
            showAlert("Error", "Product name is required!");
            return;
        }

        String description = txtDescription.getText().trim();
        String sku = txtSku.getText().trim();
        String barcode = txtBarcode.getText().trim();
        String baseUnit = txtBaseUnit.getText().trim();
        if (baseUnit.isEmpty()) baseUnit = "piece";
        
        String inventoryTracking = cbInventoryTracking.getValue() != null ? cbInventoryTracking.getValue() : "track_stock";
        String productType = cbProductType.getValue() != null ? cbProductType.getValue() : "simple";
        boolean isActive = chkIsActive.isSelected();
        String image = txtImage.getText().trim();

        double price;
        Double cost = null;
        int initialStock = 0;
        int currentStock = 0;
        int reorderLevel = 0;

        try {
            price = Double.parseDouble(txtPrice.getText().trim());
            if (!txtCost.getText().trim().isEmpty()) {
                cost = Double.parseDouble(txtCost.getText().trim());
            }
            if (!txtInitialStock.getText().trim().isEmpty()) {
                initialStock = Integer.parseInt(txtInitialStock.getText().trim());
            }
            if (!txtCurrentStock.getText().trim().isEmpty()) {
                currentStock = Integer.parseInt(txtCurrentStock.getText().trim());
            }
            if (!txtReorderLevel.getText().trim().isEmpty()) {
                reorderLevel = Integer.parseInt(txtReorderLevel.getText().trim());
            }
        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter valid numbers for price, cost, stock, and discount fields");
            return;
        }

        try (Connection con = Database.connect()) {
            Integer categoryId = null;
            if (cbCategory.getValue() != null) {
                ResultSet rs = con.createStatement().executeQuery("SELECT id FROM Category WHERE name='" + cbCategory.getValue() + "'");
                if (rs.next()) categoryId = rs.getInt("id");
            }

            Integer supplierId = null;
            if (cbSupplier.getValue() != null) {
                ResultSet rs = con.createStatement().executeQuery("SELECT id FROM Supplier WHERE name='" + cbSupplier.getValue() + "'");
                if (rs.next()) supplierId = rs.getInt("id");
            }

            String sql = "UPDATE Product SET name=?, description=?, categoryId=?, supplierId=?, sku=?, barcode=?, " +
                    "inventoryTracking=?, baseUnit=?, price=?, cost=?, initialStock=?, currentStock=?, reorderLevel=?, " +
                    "productType=?, isActive=?, image=?, discount=? WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, description.isEmpty() ? null : description);
            if (categoryId != null) ps.setInt(3, categoryId); else ps.setNull(3, Types.INTEGER);
            if (supplierId != null) ps.setInt(4, supplierId); else ps.setNull(4, Types.INTEGER);
            ps.setString(5, sku.isEmpty() ? null : sku);
            ps.setString(6, barcode.isEmpty() ? null : barcode);
            ps.setString(7, inventoryTracking);
            ps.setString(8, baseUnit);
            ps.setDouble(9, price);
            if (cost != null) ps.setDouble(10, cost); else ps.setNull(10, Types.DECIMAL);
            ps.setInt(11, initialStock);
            ps.setInt(12, currentStock);
            ps.setInt(13, reorderLevel);
            ps.setString(14, productType);
            ps.setBoolean(15, isActive);
            ps.setString(16, image.isEmpty() ? null : image);
            ps.setDouble(17, 0.0); // keep DB column but ignore product-level discount
            ps.setInt(18, p.getId());
            ps.executeUpdate();

            loadProducts();
            showAlert("Updated", "Product updated successfully!");
        } catch (SQLException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to update product: " + e.getMessage());
        }
    }

    /** Delete Product */
    @FXML
    public void deleteProduct() {
        Product p = tableProducts.getSelectionModel().getSelectedItem();
        if (p == null) return;

        try (Connection con = Database.connect()) {
            String sql = "DELETE FROM Product WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, p.getId());
            ps.executeUpdate();

            loadProducts();
            clearForm();
            showAlert("Deleted", "Product deleted successfully!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Clear form */
    @FXML
    public void clearForm() {
        txtName.clear();
        txtDescription.clear();
        txtSku.clear();
        txtBarcode.clear();
        txtBaseUnit.setText("piece");
        cbCategory.getSelectionModel().clearSelection();
        cbSupplier.getSelectionModel().clearSelection();
        cbInventoryTracking.setValue("track_stock");
        cbProductType.setValue("simple");
        txtPrice.clear();
        txtCost.clear();
        txtInitialStock.clear();
        txtCurrentStock.clear();
        txtReorderLevel.clear();
        txtImage.clear();
        chkIsActive.setSelected(true);
        tableProducts.getSelectionModel().clearSelection();
    }

    /** Show alert */
    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }

    /** Generic method to open any management window */
    private void openWindow(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            
            // Set ProductController reference for Category and Supplier controllers
            if (controller instanceof CategoryController) {
                ((CategoryController) controller).setProductController(this);
            } else if (controller instanceof SupplierController) {
                ((SupplierController) controller).setProductController(this);
            }
            
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- Menu Actions ---
    @FXML private void openCategoryWindow() { openWindow("/fxml/category.fxml", "Category Management"); }
    @FXML private void openSupplierWindow() { openWindow("/fxml/supplier.fxml", "Supplier Management"); }
    
    // --- Product-specific Actions ---
    @FXML private void openVariantWindow() {
        Product p = tableProducts.getSelectionModel().getSelectedItem();
        if (p == null) {
            showAlert("Error", "Please select a product first");
            return;
        }
        openFilteredWindow("/fxml/productvariant.fxml", "Product Variant Management - " + p.getName(), p.getId(), p.getName());
    }
    
    @FXML private void openModifierWindow() {
        Product p = tableProducts.getSelectionModel().getSelectedItem();
        if (p == null) {
            showAlert("Error", "Please select a product first");
            return;
        }
        openFilteredWindow("/fxml/productmodifier.fxml", "Product Modifier Management - " + p.getName(), p.getId(), p.getName());
    }
    
    @FXML private void openBundleWindow() {
        Product p = tableProducts.getSelectionModel().getSelectedItem();
        if (p == null) {
            showAlert("Error", "Please select a product first");
            return;
        }
        openFilteredWindow("/fxml/productbundle.fxml", "Product Bundle Management - " + p.getName(), p.getId(), p.getName());
    }
    
    /** Open window with product filter */
    private void openFilteredWindow(String fxmlPath, String title, int productId, String productName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            
            // Set product filter for each controller type
            if (controller instanceof ProductVariantController) {
                ((ProductVariantController) controller).setProductFilter(productId, productName);
            } else if (controller instanceof ProductModifierController) {
                ((ProductModifierController) controller).setProductFilter(productId, productName);
            } else if (controller instanceof ProductBundleController) {
                ((ProductBundleController) controller).setProductFilter(productId, productName);
            }
            
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to open window: " + e.getMessage());
        }
    }

}
