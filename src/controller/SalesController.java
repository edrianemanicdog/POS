package controller;

import app.Sales;
import database.Database;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class SalesController implements Initializable {

    @FXML private TableView<Sales> salesTable;
    @FXML private TableColumn<Sales, Integer> colSaleId;
    @FXML private TableColumn<Sales, String> colSaleDate;
    @FXML private TableColumn<Sales, Double> colTotalAmount;
    @FXML private TableColumn<Sales, Integer> colTotalItems;
    @FXML private TableColumn<Sales, String> colCashier;
    
    @FXML private TextField searchField;
    @FXML private Label totalSalesLabel;
    @FXML private Label totalRevenueLabel;
    @FXML private Label totalItemsSoldLabel;

    private ObservableList<Sales> sales = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createSalesTableIfNotExists();
        setupTable();
        loadSales();
    }

    private void setupTable() {
        colSaleId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        
        // Format date column
        colSaleDate.setCellValueFactory(data -> {
            LocalDateTime date = data.getValue().getSaleDate();
            String formattedDate = date != null ? 
                date.toString().replace("T", " ") : "";
            return new SimpleStringProperty(formattedDate);
        });
        
        colTotalAmount.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotalAmount()).asObject());
        colTotalItems.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTotalItems()).asObject());
        colCashier.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getCashierEmail() != null ? data.getValue().getCashierEmail() : "N/A"));
        
        // Format total amount column
        colTotalAmount.setCellFactory(column -> new TableCell<Sales, Double>() {
            @Override
            protected void updateItem(Double amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText("");
                } else {
                    setText(String.format("₱%.2f", amount));
                }
            }
        });
        
        // Add action column with "View Details" button
        TableColumn<Sales, Void> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(100);
        colAction.setCellFactory(param -> new TableCell<Sales, Void>() {
            private final Button btn = new Button("Details");
            
            {
                btn.setOnAction(event -> {
                    Sales sale = getTableView().getItems().get(getIndex());
                    viewSaleDetails(sale);
                });
                btn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 3;");
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
        salesTable.getColumns().add(colAction);
        
        salesTable.setItems(sales);
    }

    @FXML
    private void loadSales() {
        sales.clear();
        try (Connection con = Database.connect()) {
            if (con == null) {
                showAlert("Database connection failed!");
                return;
            }
            
            String query = "SELECT * FROM sales ORDER BY saleDate DESC";
            ResultSet rs = con.createStatement().executeQuery(query);
            
            while (rs.next()) {
                Sales sale = new Sales();
                sale.setId(rs.getInt("id"));
                
                Timestamp timestamp = rs.getTimestamp("saleDate");
                if (timestamp != null) {
                    sale.setSaleDate(timestamp.toLocalDateTime());
                }
                
                sale.setTotalAmount(rs.getDouble("totalAmount"));
                sale.setTotalItems(rs.getInt("totalItems"));
                sale.setCashierEmail(rs.getString("cashierEmail"));
                sales.add(sale);
            }
            
            updateSummary();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading sales: " + e.getMessage());
        }
    }

    @FXML
    private void searchSales() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        
        if (searchTerm.isEmpty()) {
            loadSales();
            return;
        }
        
        sales.clear();
        try (Connection con = Database.connect()) {
            if (con == null) {
                showAlert("Database connection failed!");
                return;
            }
            
            String query = "SELECT * FROM sales WHERE LOWER(cashierEmail) LIKE ? ORDER BY saleDate DESC";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, "%" + searchTerm + "%");
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Sales sale = new Sales();
                sale.setId(rs.getInt("id"));
                
                Timestamp timestamp = rs.getTimestamp("saleDate");
                if (timestamp != null) {
                    sale.setSaleDate(timestamp.toLocalDateTime());
                }
                
                sale.setTotalAmount(rs.getDouble("totalAmount"));
                sale.setTotalItems(rs.getInt("totalItems"));
                sale.setCashierEmail(rs.getString("cashierEmail"));
                sales.add(sale);
            }
            
            updateSummary();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error searching sales: " + e.getMessage());
        }
    }

    @FXML
    private void clearSearch() {
        searchField.clear();
        loadSales();
    }

    private void updateSummary() {
        int totalSales = sales.size();
        double totalRevenue = 0.0;
        int totalItems = 0;
        
        for (Sales sale : sales) {
            totalRevenue += sale.getTotalAmount();
            totalItems += sale.getTotalItems();
        }
        
        totalSalesLabel.setText(String.valueOf(totalSales));
        totalRevenueLabel.setText(String.format("₱%.2f", totalRevenue));
        totalItemsSoldLabel.setText(String.valueOf(totalItems));
    }

    private void viewSaleDetails(Sales sale) {
        try (Connection con = Database.connect()) {
            if (con == null) {
                showAlert("Database connection failed!");
                return;
            }
            
            String query = "SELECT * FROM sale_items WHERE saleId = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, sale.getId());
            
            ResultSet rs = ps.executeQuery();
            
            StringBuilder details = new StringBuilder();
            details.append("Sale ID: ").append(sale.getId()).append("\n");
            details.append("Date: ").append(sale.getSaleDate()).append("\n");
            details.append("Cashier: ").append(sale.getCashierEmail()).append("\n");
            details.append("Total: ₱").append(String.format("%.2f", sale.getTotalAmount())).append("\n\n");
            details.append("Items:\n");
            details.append("----------------------------------------\n");
            
            int itemNum = 1;
            while (rs.next()) {
                details.append(itemNum++).append(". ")
                       .append(rs.getString("productName"))
                       .append(" - Qty: ").append(rs.getInt("quantity"))
                       .append(" - Price: ₱").append(String.format("%.2f", rs.getDouble("price")))
                       .append(" - Subtotal: ₱").append(String.format("%.2f", rs.getDouble("subtotal")))
                       .append("\n");
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sale Details");
            alert.setHeaderText("Sale #" + sale.getId());
            alert.setContentText(details.toString());
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(500, 400);
            alert.showAndWait();
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading sale details: " + e.getMessage());
        }
    }

    private void createSalesTableIfNotExists() {
        try (Connection con = Database.connect()) {
            if (con == null) return;
            
            // Create sales table
            String createSalesTable = "CREATE TABLE IF NOT EXISTS sales (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "saleDate DATETIME NOT NULL, " +
                    "totalAmount DECIMAL(10,2) NOT NULL, " +
                    "totalItems INT NOT NULL, " +
                    "cashierEmail VARCHAR(255)" +
                    ")";
            con.createStatement().executeUpdate(createSalesTable);
            
            // Create sale_items table
            String createSaleItemsTable = "CREATE TABLE IF NOT EXISTS sale_items (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "saleId INT NOT NULL, " +
                    "productId INT NOT NULL, " +
                    "productName VARCHAR(255) NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "price DECIMAL(10,2) NOT NULL, " +
                    "subtotal DECIMAL(10,2) NOT NULL, " +
                    "FOREIGN KEY (saleId) REFERENCES sales(id) ON DELETE CASCADE" +
                    ")";
            con.createStatement().executeUpdate(createSaleItemsTable);
            
        } catch (SQLException e) {
            System.err.println("Error creating sales tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

