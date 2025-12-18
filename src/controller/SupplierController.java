package controller;

import app.Supplier;
import database.Database;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class SupplierController {

    @FXML private TableView<Supplier> tableSuppliers;
    @FXML private TableColumn<Supplier, Integer> colId;
    @FXML private TableColumn<Supplier, String> colName;
    @FXML private TableColumn<Supplier, String> colEmail;
    @FXML private TableColumn<Supplier, String> colPhone;
    @FXML private TableColumn<Supplier, String> colAddress;

    @FXML private TextField txtName, txtEmail, txtPhone, txtAddress;

    private ObservableList<Supplier> suppliers = FXCollections.observableArrayList();
    private ProductController productController; // Reference to ProductController

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail() != null ? data.getValue().getEmail() : ""));
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone() != null ? data.getValue().getPhone() : ""));
        colAddress.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAddress() != null ? data.getValue().getAddress() : ""));

        tableSuppliers.setItems(suppliers);
        loadSuppliers();

        tableSuppliers.setOnMouseClicked(e -> {
            Supplier s = tableSuppliers.getSelectionModel().getSelectedItem();
            if (s != null) {
                txtName.setText(s.getName());
                txtEmail.setText(s.getEmail());
                txtPhone.setText(s.getPhone());
                txtAddress.setText(s.getAddress());
            }
        });
    }

    private void loadSuppliers() {
        suppliers.clear();
        try (Connection con = Database.connect()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT * FROM Supplier");
            while (rs.next()) {
                suppliers.add(new Supplier(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("address")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Set reference to ProductController */
    public void setProductController(ProductController pc) {
        this.productController = pc;
    }

    @FXML
    public void addSupplier() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) return;

        try (Connection con = Database.connect()) {
            String sql = "INSERT INTO Supplier(name, email, phone, address) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.setString(2, txtEmail.getText().trim());
            ps.setString(3, txtPhone.getText().trim());
            ps.setString(4, txtAddress.getText().trim());
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) suppliers.add(new Supplier(rs.getInt(1), name, txtEmail.getText().trim(), txtPhone.getText().trim(), txtAddress.getText().trim()));
            
            // Refresh ProductController ComboBox
            if (productController != null) productController.loadSuppliers();
            
            clearForm();
            showAlert("Success", "Supplier added successfully!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void updateSupplier() {
        Supplier s = tableSuppliers.getSelectionModel().getSelectedItem();
        if (s == null) return;

        try (Connection con = Database.connect()) {
            String sql = "UPDATE Supplier SET name=?, email=?, phone=?, address=? WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, txtName.getText().trim());
            ps.setString(2, txtEmail.getText().trim());
            ps.setString(3, txtPhone.getText().trim());
            ps.setString(4, txtAddress.getText().trim());
            ps.setInt(5, s.getId());
            ps.executeUpdate();

            s.setName(txtName.getText().trim());
            s.setEmail(txtEmail.getText().trim());
            s.setPhone(txtPhone.getText().trim());
            s.setAddress(txtAddress.getText().trim());

            tableSuppliers.refresh();
            
            // Refresh ProductController ComboBox
            if (productController != null) productController.loadSuppliers();
            
            showAlert("Updated", "Supplier updated successfully!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void deleteSupplier() {
        Supplier s = tableSuppliers.getSelectionModel().getSelectedItem();
        if (s == null) return;

        try (Connection con = Database.connect()) {
            String sql = "DELETE FROM Supplier WHERE id=?";
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, s.getId());
            ps.executeUpdate();

            suppliers.remove(s);
            
            // Refresh ProductController ComboBox
            if (productController != null) productController.loadSuppliers();
            
            clearForm();
            showAlert("Deleted", "Supplier deleted successfully!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void clearForm() {
        txtName.clear();
        txtEmail.clear();
        txtPhone.clear();
        txtAddress.clear();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }
}
