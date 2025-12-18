package controller;

import app.ProductModifier;
import app.ProductModifierOption;
import app.Product;
import database.Database;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class ProductModifierController {

    @FXML private TableView<ProductModifier> tableModifiers;
    @FXML private TableColumn<ProductModifier, Integer> colId;
    @FXML private TableColumn<ProductModifier, String> colProduct;
    @FXML private TableColumn<ProductModifier, String> colName;
    @FXML private TableColumn<ProductModifier, String> colType;
    @FXML private TableColumn<ProductModifier, String> colRequired;

    @FXML private ComboBox<String> cbProduct;
    @FXML private TextField txtName;
    @FXML private ComboBox<String> cbType;
    @FXML private CheckBox chkRequired;

    // Modifier Options UI
    @FXML private ComboBox<String> cbModifierForOption;
    @FXML private TextField txtOptionName;
    @FXML private TextField txtOptionPrice;
    @FXML private TableView<ProductModifierOption> tableModifierOptions;
    @FXML private TableColumn<ProductModifierOption, Integer> colOptionId;
    @FXML private TableColumn<ProductModifierOption, String> colOptionModifier;
    @FXML private TableColumn<ProductModifierOption, String> colOptionName;
    @FXML private TableColumn<ProductModifierOption, Double> colOptionPrice;

    private ObservableList<ProductModifier> modifiers = FXCollections.observableArrayList();
    private ObservableList<String> products = FXCollections.observableArrayList();
    private ObservableList<String> types = FXCollections.observableArrayList("single", "multiple");
    private ObservableList<ProductModifierOption> modifierOptions = FXCollections.observableArrayList();
    private ObservableList<String> modifierNames = FXCollections.observableArrayList();
    private Integer filteredProductId = null;
    private String filteredProductName = null;
    private Integer selectedModifierId = null;

    @FXML
    public void initialize() {
        // Table columns
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colProduct.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductName()));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colType.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getType()));
        colRequired.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().isRequired() ? "Yes" : "No"));

        tableModifiers.setItems(modifiers);

        // Populate combo boxes
        cbType.setItems(types);
        loadProducts();

        loadModifiers();

        // Setup modifier options table columns
        if (colOptionId != null) colOptionId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        if (colOptionModifier != null) colOptionModifier.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getModifierName()));
        if (colOptionName != null) colOptionName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        if (colOptionPrice != null) colOptionPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        if (tableModifierOptions != null) tableModifierOptions.setItems(modifierOptions);

        // Load modifier names for options combo box
        loadModifierNames();

        // Fill form on row click
        tableModifiers.setOnMouseClicked(e -> {
            ProductModifier m = tableModifiers.getSelectionModel().getSelectedItem();
            if (m != null) {
                cbProduct.setValue(m.getProductName());
                txtName.setText(m.getName());
                cbType.setValue(m.getType());
                chkRequired.setSelected(m.isRequired());
                
                // Update selected modifier and load its options
                selectedModifierId = m.getId();
                String modifierDisplay = m.getProductName() + " - " + m.getName();
                if (cbModifierForOption != null) {
                    cbModifierForOption.setValue(modifierDisplay);
                }
                loadModifierOptions();
            }
        });

        // Fill option form on row click
        if (tableModifierOptions != null) {
            tableModifierOptions.setOnMouseClicked(e -> {
                ProductModifierOption o = tableModifierOptions.getSelectionModel().getSelectedItem();
                if (o != null) {
                    if (cbModifierForOption != null) cbModifierForOption.setValue(o.getModifierName());
                    if (txtOptionName != null) txtOptionName.setText(o.getName());
                    if (txtOptionPrice != null) txtOptionPrice.setText(String.valueOf(o.getPrice()));
                }
            });
        }

        // Load modifier options when modifier selection changes
        if (cbModifierForOption != null) {
            cbModifierForOption.setOnAction(e -> {
                String selected = cbModifierForOption.getValue();
                if (selected != null) {
                    // Extract modifier ID from display string
                    String[] parts = selected.split(" - ", 2);
                    if (parts.length == 2) {
                        try (Connection con = Database.connect()) {
                            ResultSet rs = con.createStatement().executeQuery(
                                "SELECT pm.id FROM ProductModifier pm " +
                                "LEFT JOIN Product p ON pm.productId = p.id " +
                                "WHERE p.name='" + parts[0] + "' AND pm.name='" + parts[1] + "'"
                            );
                            if (rs.next()) {
                                selectedModifierId = rs.getInt("id");
                                loadModifierOptions();
                            }
                        } catch (SQLException ex) { ex.printStackTrace(); }
                    }
                }
            });
        }
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

    private void loadModifiers() {
        modifiers.clear();
        try (Connection con = Database.connect()) {
            String query = "SELECT pm.id, pm.name, pm.type, pm.required, p.name AS productName " +
                "FROM ProductModifier pm " +
                "LEFT JOIN Product p ON pm.productId = p.id";
            if (filteredProductId != null) {
                query += " WHERE pm.productId = " + filteredProductId;
            }
            ResultSet rs = con.createStatement().executeQuery(query);
            while (rs.next()) {
                modifiers.add(new ProductModifier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("type"),
                        rs.getBoolean("required"),
                        rs.getString("productName")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        // Reload modifier names for options combo box
        loadModifierNames();
    }

    private void loadModifierNames() {
        modifierNames.clear();
        try (Connection con = Database.connect()) {
            String query = "SELECT pm.id, pm.name, p.name AS productName " +
                "FROM ProductModifier pm " +
                "LEFT JOIN Product p ON pm.productId = p.id";
            if (filteredProductId != null) {
                query += " WHERE pm.productId = " + filteredProductId;
            }
            query += " ORDER BY p.name, pm.name";
            ResultSet rs = con.createStatement().executeQuery(query);
            while (rs.next()) {
                String displayName = rs.getString("productName") + " - " + rs.getString("name");
                modifierNames.add(displayName);
            }
            if (cbModifierForOption != null) cbModifierForOption.setItems(modifierNames);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadModifierOptions() {
        modifierOptions.clear();
        if (selectedModifierId == null) return;
        
        try (Connection con = Database.connect()) {
            String query = "SELECT pmo.id, pmo.modifierId, pmo.name, pmo.price, " +
                "CONCAT(p.name, ' - ', pm.name) AS modifierName " +
                "FROM ProductModifierOption pmo " +
                "LEFT JOIN ProductModifier pm ON pmo.modifierId = pm.id " +
                "LEFT JOIN Product p ON pm.productId = p.id " +
                "WHERE pmo.modifierId = " + selectedModifierId;
            ResultSet rs = con.createStatement().executeQuery(query);
            while (rs.next()) {
                ProductModifierOption option = new ProductModifierOption(
                        rs.getInt("id"),
                        rs.getInt("modifierId"),
                        rs.getString("name"),
                        rs.getDouble("price")
                );
                option.setModifierName(rs.getString("modifierName"));
                modifierOptions.add(option);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    /** Set product filter to show only modifiers for a specific product */
    public void setProductFilter(int productId, String productName) {
        this.filteredProductId = productId;
        this.filteredProductName = productName;
        // Reload data with filter
        loadProducts();
        loadModifiers();
        loadModifierNames();
    }

    @FXML
    public void addModifier() {
        String name = txtName.getText().trim();
        String type = cbType.getValue();
        boolean required = chkRequired.isSelected();
        String productName = cbProduct.getValue();

        if (name.isEmpty() || type == null || productName == null) return;

        try (Connection con = Database.connect()) {
            // Get productId
            int productId = 0;
            ResultSet rs = con.createStatement().executeQuery("SELECT id FROM Product WHERE name='" + productName + "'");
            if (rs.next()) productId = rs.getInt("id");

            String sql = "INSERT INTO ProductModifier(productId, name, type, required) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, productId);
            ps.setString(2, name);
            ps.setString(3, type);
            ps.setBoolean(4, required);
            ps.executeUpdate();

            ResultSet rsKeys = ps.getGeneratedKeys();
            if (rsKeys.next()) {
                modifiers.add(new ProductModifier(rsKeys.getInt(1), name, type, required, productName));
            }

            loadModifiers(); // Reload table to ensure consistency
            clearForm();
            showAlert("Success", "Modifier added successfully!");
            loadModifierNames(); // Reload modifier names for options
        } catch (SQLException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to add modifier: " + e.getMessage());
        }
    }

    @FXML
    public void updateModifier() {
        ProductModifier m = tableModifiers.getSelectionModel().getSelectedItem();
        if (m == null) return;

        String name = txtName.getText().trim();
        String type = cbType.getValue();
        boolean required = chkRequired.isSelected();
        String productName = cbProduct.getValue();

        if (name.isEmpty() || type == null || productName == null) return;

        try (Connection con = Database.connect()) {
            int productId = 0;
            ResultSet rs = con.createStatement().executeQuery("SELECT id FROM Product WHERE name='" + productName + "'");
            if (rs.next()) productId = rs.getInt("id");

            String sql = "UPDATE ProductModifier SET productId=?, name=?, type=?, required=? WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, productId);
            ps.setString(2, name);
            ps.setString(3, type);
            ps.setBoolean(4, required);
            ps.setInt(5, m.getId());
            ps.executeUpdate();

            m.setName(name);
            m.setType(type);
            m.setRequired(required);
            m.setProductName(productName);
            
            loadModifiers(); // Reload table to ensure consistency
            showAlert("Updated", "Modifier updated successfully!");
            loadModifierNames(); // Reload modifier names for options
        } catch (SQLException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to update modifier: " + e.getMessage());
        }
    }

    @FXML
    public void deleteModifier() {
        ProductModifier m = tableModifiers.getSelectionModel().getSelectedItem();
        if (m == null) return;

        try (Connection con = Database.connect()) {
            String sql = "DELETE FROM ProductModifier WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, m.getId());
            ps.executeUpdate();

            loadModifiers(); // Reload table to ensure consistency
            clearForm();
            showAlert("Deleted", "Modifier deleted successfully!");
            loadModifierNames(); // Reload modifier names for options
            selectedModifierId = null;
            loadModifierOptions(); // Clear options
        } catch (SQLException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to delete modifier: " + e.getMessage());
        }
    }

    @FXML
    public void clearForm() {
        cbProduct.getSelectionModel().clearSelection();
        txtName.clear();
        cbType.getSelectionModel().clearSelection();
        chkRequired.setSelected(false);
    }

    // Modifier Options Methods
    @FXML
    public void addModifierOption() {
        String name = txtOptionName.getText().trim();
        String modifierDisplay = cbModifierForOption.getValue();
        
        if (name.isEmpty() || modifierDisplay == null) {
            showAlert("Error", "Please fill in all required fields (Name and Modifier)");
            return;
        }

        try (Connection con = Database.connect()) {
            // Extract modifierId from the display string
            int modifierId = 0;
            String[] parts = modifierDisplay.split(" - ", 2);
            if (parts.length == 2) {
                ResultSet rs = con.createStatement().executeQuery(
                    "SELECT pm.id FROM ProductModifier pm " +
                    "LEFT JOIN Product p ON pm.productId = p.id " +
                    "WHERE p.name='" + parts[0] + "' AND pm.name='" + parts[1] + "'"
                );
                if (rs.next()) modifierId = rs.getInt("id");
            }
            
            if (modifierId == 0) {
                showAlert("Error", "Could not find the selected modifier");
                return;
            }

            double price = txtOptionPrice.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtOptionPrice.getText().trim());
            
            String sql = "INSERT INTO ProductModifierOption(name, price, modifierId) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, modifierId);
            ps.executeUpdate();

            loadModifierOptions();
            clearOptionForm();
            showAlert("Success", "Option added successfully!");
        } catch (SQLException | NumberFormatException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to add option: " + e.getMessage());
        }
    }

    @FXML
    public void updateModifierOption() {
        ProductModifierOption o = tableModifierOptions.getSelectionModel().getSelectedItem();
        if (o == null) {
            showAlert("Error", "Please select an option to update");
            return;
        }

        String name = txtOptionName.getText().trim();
        String modifierDisplay = cbModifierForOption.getValue();
        
        if (name.isEmpty() || modifierDisplay == null) {
            showAlert("Error", "Please fill in all required fields (Name and Modifier)");
            return;
        }

        try (Connection con = Database.connect()) {
            int modifierId = 0;
            String[] parts = modifierDisplay.split(" - ", 2);
            if (parts.length == 2) {
                ResultSet rs = con.createStatement().executeQuery(
                    "SELECT pm.id FROM ProductModifier pm " +
                    "LEFT JOIN Product p ON pm.productId = p.id " +
                    "WHERE p.name='" + parts[0] + "' AND pm.name='" + parts[1] + "'"
                );
                if (rs.next()) modifierId = rs.getInt("id");
            }
            
            if (modifierId == 0) {
                showAlert("Error", "Could not find the selected modifier");
                return;
            }

            double price = txtOptionPrice.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtOptionPrice.getText().trim());
            
            String sql = "UPDATE ProductModifierOption SET name=?, price=?, modifierId=? WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, modifierId);
            ps.setInt(4, o.getId());
            ps.executeUpdate();

            loadModifierOptions();
            showAlert("Updated", "Option updated successfully!");
        } catch (SQLException | NumberFormatException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to update option: " + e.getMessage());
        }
    }

    @FXML
    public void deleteModifierOption() {
        ProductModifierOption o = tableModifierOptions.getSelectionModel().getSelectedItem();
        if (o == null) return;

        try (Connection con = Database.connect()) {
            String sql = "DELETE FROM ProductModifierOption WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, o.getId());
            ps.executeUpdate();

            loadModifierOptions();
            clearOptionForm();
            showAlert("Deleted", "Option deleted successfully!");
        } catch (SQLException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to delete option: " + e.getMessage());
        }
    }

    @FXML
    public void clearOptionForm() {
        if (txtOptionName != null) txtOptionName.clear();
        if (txtOptionPrice != null) txtOptionPrice.clear();
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
