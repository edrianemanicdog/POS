package controller;

import app.ProductModifierOption;
import database.Database;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class ProductModifierOptionController {

    @FXML private TableView<ProductModifierOption> tableOptions;
    @FXML private TableColumn<ProductModifierOption, Integer> colId;
    @FXML private TableColumn<ProductModifierOption, String> colModifier;
    @FXML private TableColumn<ProductModifierOption, String> colName;
    @FXML private TableColumn<ProductModifierOption, Double> colPrice;

    @FXML private ComboBox<String> cbModifier;
    @FXML private TextField txtName, txtPrice;

    private ObservableList<ProductModifierOption> options = FXCollections.observableArrayList();
    private ObservableList<String> modifiers = FXCollections.observableArrayList();
    private Integer filteredProductId = null;
    private String filteredProductName = null;

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        if (colModifier != null) {
            colModifier.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getModifierName()));
        }
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());

        tableOptions.setItems(options);
        loadModifiers();
        loadOptions();

        tableOptions.setOnMouseClicked(e -> {
            ProductModifierOption o = tableOptions.getSelectionModel().getSelectedItem();
            if (o != null) {
                cbModifier.setValue(o.getModifierName());
                txtName.setText(o.getName());
                txtPrice.setText(String.valueOf(o.getPrice()));
            }
        });
    }

    private void loadModifiers() {
        modifiers.clear();
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
                modifiers.add(displayName);
            }
            if (cbModifier != null) cbModifier.setItems(modifiers);
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadOptions() {
        options.clear();
        try (Connection con = Database.connect()) {
            String query = "SELECT pmo.id, pmo.modifierId, pmo.name, pmo.price, " +
                "CONCAT(p.name, ' - ', pm.name) AS modifierName " +
                "FROM ProductModifierOption pmo " +
                "LEFT JOIN ProductModifier pm ON pmo.modifierId = pm.id " +
                "LEFT JOIN Product p ON pm.productId = p.id";
            if (filteredProductId != null) {
                query += " WHERE pm.productId = " + filteredProductId;
            }
            ResultSet rs = con.createStatement().executeQuery(query);
            while (rs.next()) {
                ProductModifierOption option = new ProductModifierOption(
                        rs.getInt("id"),
                        rs.getInt("modifierId"),
                        rs.getString("name"),
                        rs.getDouble("price")
                );
                option.setModifierName(rs.getString("modifierName"));
                options.add(option);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
    
    /** Set product filter to show only modifier options for modifiers of a specific product */
    public void setProductFilter(int productId, String productName) {
        this.filteredProductId = productId;
        this.filteredProductName = productName;
        // Reload data with filter
        loadModifiers();
        loadOptions();
    }

    @FXML
    public void addOption() {
        String name = txtName.getText().trim();
        String modifierDisplay = cbModifier.getValue();
        
        if (name.isEmpty() || modifierDisplay == null) {
            showAlert("Error", "Please fill in all required fields (Name and Modifier)");
            return;
        }

        try (Connection con = Database.connect()) {
            // Extract modifierId from the display string (format: "ProductName - ModifierName")
            int modifierId = 0;
            String[] parts = modifierDisplay.split(" - ", 2);
            if (parts.length == 2) {
                String productName = parts[0];
                String modifierName = parts[1];
                ResultSet rs = con.createStatement().executeQuery(
                    "SELECT pm.id FROM ProductModifier pm " +
                    "LEFT JOIN Product p ON pm.productId = p.id " +
                    "WHERE p.name='" + productName + "' AND pm.name='" + modifierName + "'"
                );
                if (rs.next()) modifierId = rs.getInt("id");
            }
            
            if (modifierId == 0) {
                showAlert("Error", "Could not find the selected modifier");
                return;
            }

            double price = txtPrice.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtPrice.getText().trim());
            
            String sql = "INSERT INTO ProductModifierOption(name, price, modifierId) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, modifierId);
            ps.executeUpdate();

            loadOptions(); // Reload table to ensure consistency
            clearForm();
            showAlert("Success", "Option added successfully!");
        } catch (SQLException | NumberFormatException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to add option: " + e.getMessage());
        }
    }

    @FXML
    public void updateOption() {
        ProductModifierOption o = tableOptions.getSelectionModel().getSelectedItem();
        if (o == null) {
            showAlert("Error", "Please select an option to update");
            return;
        }

        String name = txtName.getText().trim();
        String modifierDisplay = cbModifier.getValue();
        
        if (name.isEmpty() || modifierDisplay == null) {
            showAlert("Error", "Please fill in all required fields (Name and Modifier)");
            return;
        }

        try (Connection con = Database.connect()) {
            // Extract modifierId from the display string
            int modifierId = 0;
            String[] parts = modifierDisplay.split(" - ", 2);
            if (parts.length == 2) {
                String productName = parts[0];
                String modifierName = parts[1];
                ResultSet rs = con.createStatement().executeQuery(
                    "SELECT pm.id FROM ProductModifier pm " +
                    "LEFT JOIN Product p ON pm.productId = p.id " +
                    "WHERE p.name='" + productName + "' AND pm.name='" + modifierName + "'"
                );
                if (rs.next()) modifierId = rs.getInt("id");
            }
            
            if (modifierId == 0) {
                showAlert("Error", "Could not find the selected modifier");
                return;
            }

            double price = txtPrice.getText().trim().isEmpty() ? 0.0 : Double.parseDouble(txtPrice.getText().trim());
            
            String sql = "UPDATE ProductModifierOption SET name=?, price=?, modifierId=? WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name);
            ps.setDouble(2, price);
            ps.setInt(3, modifierId);
            ps.setInt(4, o.getId());
            ps.executeUpdate();

            loadOptions(); // Reload table to ensure consistency
            showAlert("Updated", "Option updated successfully!");
        } catch (SQLException | NumberFormatException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to update option: " + e.getMessage());
        }
    }

    @FXML
    public void deleteOption() {
        ProductModifierOption o = tableOptions.getSelectionModel().getSelectedItem();
        if (o == null) return;

        try (Connection con = Database.connect()) {
            String sql = "DELETE FROM ProductModifierOption WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, o.getId());
            ps.executeUpdate();

            loadOptions(); // Reload table to ensure consistency
            clearForm();
            showAlert("Deleted", "Option deleted successfully!");
        } catch (SQLException e) { 
            e.printStackTrace();
            showAlert("Error", "Failed to delete option: " + e.getMessage());
        }
    }

    @FXML
    public void clearForm() {
        if (cbModifier != null) cbModifier.getSelectionModel().clearSelection();
        txtName.clear();
        txtPrice.clear();
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
