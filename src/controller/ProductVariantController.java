package controller;

import app.ProductVariant;
import app.Product;
import database.Database;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class ProductVariantController {

    @FXML private TableView<ProductVariant> tableVariants;
    @FXML private TableColumn<ProductVariant, Integer> colId;
    @FXML private TableColumn<ProductVariant, String> colProduct;
    @FXML private TableColumn<ProductVariant, String> colName;
    @FXML private TableColumn<ProductVariant, String> colSKU;
    @FXML private TableColumn<ProductVariant, String> colBarcode;
    @FXML private TableColumn<ProductVariant, Double> colPrice;
    @FXML private TableColumn<ProductVariant, Double> colCost;
    @FXML private TableColumn<ProductVariant, Integer> colStock;
    @FXML private TableColumn<ProductVariant, String> colAttributes;
    @FXML private TableColumn<ProductVariant, Boolean> colIsActive;

    @FXML private ComboBox<String> cbProduct;
    @FXML private TextField txtName, txtSKU, txtBarcode, txtPrice, txtCost, txtStock, txtAttributes;
    @FXML private CheckBox chkIsActive;

    private ObservableList<ProductVariant> variants = FXCollections.observableArrayList();
    private ObservableList<String> products = FXCollections.observableArrayList();
    private Integer filteredProductId = null;
    private String filteredProductName = null;

    @FXML
    public void initialize() {
        // Table columns
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colProduct.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductName()));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colSKU.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSku()));
        colBarcode.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBarcode()));
        colPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        colCost.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getCost()).asObject());
        colStock.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getStock()).asObject());
        colAttributes.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAttributes()));
        if (colIsActive != null) {
            colIsActive.setCellValueFactory(data -> new javafx.beans.property.SimpleBooleanProperty(data.getValue().isIsActive()));
            colIsActive.setCellFactory(column -> new TableCell<ProductVariant, Boolean>() {
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

        tableVariants.setItems(variants);

        loadProducts();
        loadVariants();

        tableVariants.setOnMouseClicked(e -> {
            ProductVariant v = tableVariants.getSelectionModel().getSelectedItem();
            if (v != null) {
                cbProduct.setValue(v.getProductName());
                txtName.setText(v.getName());
                txtSKU.setText(v.getSku());
                txtBarcode.setText(v.getBarcode());
                txtPrice.setText(String.valueOf(v.getPrice()));
                txtCost.setText(String.valueOf(v.getCost()));
                txtStock.setText(String.valueOf(v.getStock()));
                txtAttributes.setText(v.getAttributes());
                if (chkIsActive != null) chkIsActive.setSelected(v.isIsActive());
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
            ResultSet rs = con.createStatement().executeQuery(query);
            while (rs.next()) products.add(rs.getString("name"));
            cbProduct.setItems(products);
            // Auto-select filtered product if set
            if (filteredProductName != null && products.contains(filteredProductName)) {
                cbProduct.setValue(filteredProductName);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadVariants() {
        variants.clear();
        try (Connection con = Database.connect()) {
            String query = "SELECT pv.id, pv.name, pv.sku, pv.barcode, pv.price, pv.cost, pv.stock, pv.attributes, pv.isActive, p.name AS productName " +
                "FROM ProductVariant pv " +
                "LEFT JOIN Product p ON pv.productId = p.id";
            if (filteredProductId != null) {
                query += " WHERE pv.productId = " + filteredProductId;
            }
            ResultSet rs = con.createStatement().executeQuery(query);
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
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    /** Set product filter to show only variants for a specific product */
    public void setProductFilter(int productId, String productName) {
        this.filteredProductId = productId;
        this.filteredProductName = productName;
        // Reload data with filter
        loadProducts();
        loadVariants();
    }

    @FXML
    public void addVariant() {
        String name = txtName.getText().trim();
        String sku = txtSKU.getText().trim();
        String barcode = txtBarcode.getText().trim();
        String productName = cbProduct.getValue();
        double price = txtPrice.getText().isEmpty() ? 0 : Double.parseDouble(txtPrice.getText());
        Double cost = txtCost.getText().isEmpty() ? null : Double.parseDouble(txtCost.getText());
        int stock = txtStock.getText().isEmpty() ? 0 : Integer.parseInt(txtStock.getText());
        String attributes = txtAttributes.getText().trim();
        boolean isActive = chkIsActive != null ? chkIsActive.isSelected() : true;

        if (name.isEmpty() || productName == null) return;

        try (Connection con = Database.connect()) {
            int productId = 0;
            ResultSet rs = con.createStatement().executeQuery("SELECT id FROM Product WHERE name='" + productName + "'");
            if (rs.next()) productId = rs.getInt("id");

            String sql = "INSERT INTO ProductVariant(productId, name, sku, barcode, price, cost, stock, attributes, isActive) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, productId);
            ps.setString(2, name);
            ps.setString(3, sku.isEmpty() ? null : sku);
            ps.setString(4, barcode.isEmpty() ? null : barcode);
            ps.setDouble(5, price);
            if (cost != null) ps.setDouble(6, cost); else ps.setNull(6, Types.DECIMAL);
            ps.setInt(7, stock);
            ps.setString(8, attributes.isEmpty() ? null : attributes);
            ps.setBoolean(9, isActive);
            ps.executeUpdate();

            ResultSet rsKeys = ps.getGeneratedKeys();
            if (rsKeys.next()) {
                variants.add(new ProductVariant(rsKeys.getInt(1), name, sku, barcode, price, cost, stock, attributes, isActive, productName));
            }

            loadVariants(); // Reload table to ensure consistency
            clearForm();
            showAlert("Success", "Variant added successfully!");
        } catch (SQLException | NumberFormatException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to add variant: " + e.getMessage());
        }
    }

    @FXML
    public void updateVariant() {
        ProductVariant v = tableVariants.getSelectionModel().getSelectedItem();
        if (v == null) return;

        String name = txtName.getText().trim();
        String sku = txtSKU.getText().trim();
        String barcode = txtBarcode.getText().trim();
        String productName = cbProduct.getValue();
        double price = txtPrice.getText().isEmpty() ? 0 : Double.parseDouble(txtPrice.getText());
        Double cost = txtCost.getText().isEmpty() ? null : Double.parseDouble(txtCost.getText());
        int stock = txtStock.getText().isEmpty() ? 0 : Integer.parseInt(txtStock.getText());
        String attributes = txtAttributes.getText().trim();
        boolean isActive = chkIsActive != null ? chkIsActive.isSelected() : true;

        if (name.isEmpty() || productName == null) return;

        try (Connection con = Database.connect()) {
            int productId = 0;
            ResultSet rs = con.createStatement().executeQuery("SELECT id FROM Product WHERE name='" + productName + "'");
            if (rs.next()) productId = rs.getInt("id");

            String sql = "UPDATE ProductVariant SET productId=?, name=?, sku=?, barcode=?, price=?, cost=?, stock=?, attributes=?, isActive=? WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, productId);
            ps.setString(2, name);
            ps.setString(3, sku.isEmpty() ? null : sku);
            ps.setString(4, barcode.isEmpty() ? null : barcode);
            ps.setDouble(5, price);
            if (cost != null) ps.setDouble(6, cost); else ps.setNull(6, Types.DECIMAL);
            ps.setInt(7, stock);
            ps.setString(8, attributes.isEmpty() ? null : attributes);
            ps.setBoolean(9, isActive);
            ps.setInt(10, v.getId());
            ps.executeUpdate();

            v.setName(name);
            v.setSku(sku);
            v.setBarcode(barcode);
            v.setPrice(price);
            v.setCost(cost);
            v.setStock(stock);
            v.setAttributes(attributes);
            v.setIsActive(isActive);
            v.setProductName(productName);
            
            loadVariants(); // Reload table to ensure consistency
            showAlert("Updated", "Variant updated successfully!");
        } catch (SQLException | NumberFormatException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to update variant: " + e.getMessage());
        }
    }

    @FXML
    public void deleteVariant() {
        ProductVariant v = tableVariants.getSelectionModel().getSelectedItem();
        if (v == null) return;

        try (Connection con = Database.connect()) {
            String sql = "DELETE FROM ProductVariant WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, v.getId());
            ps.executeUpdate();

            loadVariants(); // Reload table to ensure consistency
            clearForm();
            showAlert("Deleted", "Variant deleted successfully!");
        } catch (SQLException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to delete variant: " + e.getMessage());
        }
    }

    @FXML
    public void clearForm() {
        cbProduct.getSelectionModel().clearSelection();
        txtName.clear();
        txtSKU.clear();
        txtBarcode.clear();
        txtPrice.clear();
        txtCost.clear();
        txtStock.clear();
        txtAttributes.clear();
        if (chkIsActive != null) chkIsActive.setSelected(true);
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
