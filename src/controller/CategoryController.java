package controller;

import app.Category;
import database.Database;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.*;

public class CategoryController {

    @FXML private TableView<Category> tableCategories;
    @FXML private TableColumn<Category, Integer> colId;
    @FXML private TableColumn<Category, String> colName;
    @FXML private TextField txtName;

    private ObservableList<Category> categories = FXCollections.observableArrayList();
    private ProductController productController; // Reference to ProductController

    @FXML
    public void initialize() {
        colId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        tableCategories.setItems(categories);

        loadCategories();

        tableCategories.setOnMouseClicked(e -> {
            Category c = tableCategories.getSelectionModel().getSelectedItem();
            if (c != null) txtName.setText(c.getName());
        });
    }

    /** Set reference to ProductController */
    public void setProductController(ProductController pc) {
        this.productController = pc;
    }

    /** Load categories */
    private void loadCategories() {
        categories.clear();
        try (Connection con = Database.connect()) {
            ResultSet rs = con.createStatement().executeQuery("SELECT id, name FROM Category");
            while (rs.next()) categories.add(new Category(rs.getInt("id"), rs.getString("name")));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    /** Add category and refresh ProductController */
    @FXML
    public void addCategory() {
        String name = txtName.getText().trim();
        if (name.isEmpty()) return;

        try (Connection con = Database.connect()) {
            PreparedStatement ps = con.prepareStatement("INSERT INTO Category(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, name);
            ps.executeUpdate();

            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) categories.add(new Category(rs.getInt(1), name));

            // Refresh ProductController ComboBox
            if (productController != null) productController.loadCategories();

            clearForm();
            showAlert("Success", "Category added successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void updateCategory() {
        Category c = tableCategories.getSelectionModel().getSelectedItem();
        if (c == null) return;

        String name = txtName.getText().trim();
        if (name.isEmpty()) return;

        try (Connection con = Database.connect()) {
            PreparedStatement ps = con.prepareStatement("UPDATE Category SET name=? WHERE id=?");
            ps.setString(1, name);
            ps.setInt(2, c.getId());
            ps.executeUpdate();

            c.setName(name);
            tableCategories.refresh();

            if (productController != null) productController.loadCategories();

            showAlert("Updated", "Category updated successfully!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void deleteCategory() {
        Category c = tableCategories.getSelectionModel().getSelectedItem();
        if (c == null) return;

        try (Connection con = Database.connect()) {
            PreparedStatement ps = con.prepareStatement("DELETE FROM Category WHERE id=?");
            ps.setInt(1, c.getId());
            ps.executeUpdate();

            categories.remove(c);
            if (productController != null) productController.loadCategories();

            clearForm();
            showAlert("Deleted", "Category deleted successfully!");
        } catch (SQLException e) { e.printStackTrace(); }
    }

    @FXML
    public void clearForm() { txtName.clear(); }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setContentText(msg);
        a.show();
    }
}
