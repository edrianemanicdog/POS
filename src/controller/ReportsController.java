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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ReportsController implements Initializable {

    @FXML private TableView<Sales> reportsTable;
    @FXML private TableColumn<Sales, Integer> colReportId;
    @FXML private TableColumn<Sales, String> colReportDate;
    @FXML private TableColumn<Sales, Double> colReportAmount;
    @FXML private TableColumn<Sales, Integer> colReportItems;
    @FXML private TableColumn<Sales, String> colReportCashier;
    
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    
    @FXML private Label transactionsCountLabel;
    @FXML private Label totalSalesLabel;
    @FXML private Label totalItemsLabel;
    @FXML private Label averageSaleLabel;

    private ObservableList<Sales> sales = FXCollections.observableArrayList();
    private LocalDate reportStartDate;
    private LocalDate reportEndDate;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        createSalesTableIfNotExists();
        setupTable();
        
        // Set default dates (today)
        LocalDate today = LocalDate.now();
        startDatePicker.setValue(today);
        endDatePicker.setValue(today);
        
        // Load today's sales by default
        generateDailyReport();
    }

    private void setupTable() {
        colReportId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        
        // Format date column
        colReportDate.setCellValueFactory(data -> {
            LocalDateTime date = data.getValue().getSaleDate();
            if (date != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                return new SimpleStringProperty(date.format(formatter));
            }
            return new SimpleStringProperty("");
        });
        
        colReportAmount.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getTotalAmount()).asObject());
        colReportItems.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getTotalItems()).asObject());
        colReportCashier.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getCashierEmail() != null ? data.getValue().getCashierEmail() : "N/A"));
        
        // Format amount column
        colReportAmount.setCellFactory(column -> new TableCell<Sales, Double>() {
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
        
        reportsTable.setItems(sales);
    }

    @FXML
    private void generateDailyReport() {
        LocalDate selectedDate = startDatePicker.getValue();
        if (selectedDate == null) {
            showAlert("Please select a date!");
            return;
        }
        
        reportStartDate = selectedDate;
        reportEndDate = selectedDate;
        
        loadSalesByDateRange(selectedDate.atStartOfDay(), selectedDate.plusDays(1).atStartOfDay());
    }

    @FXML
    private void generateMonthlyReport() {
        LocalDate selectedDate = startDatePicker.getValue();
        if (selectedDate == null) {
            showAlert("Please select a date!");
            return;
        }
        
        // Get first and last day of the month
        LocalDate firstDay = selectedDate.withDayOfMonth(1);
        LocalDate lastDay = selectedDate.withDayOfMonth(selectedDate.lengthOfMonth());
        
        reportStartDate = firstDay;
        reportEndDate = lastDay;
        
        loadSalesByDateRange(firstDay.atStartOfDay(), lastDay.plusDays(1).atStartOfDay());
    }

    @FXML
    private void generateCustomReport() {
        LocalDate startDate = startDatePicker.getValue();
        LocalDate endDate = endDatePicker.getValue();
        
        if (startDate == null || endDate == null) {
            showAlert("Please select both start and end dates!");
            return;
        }
        
        if (startDate.isAfter(endDate)) {
            showAlert("Start date must be before or equal to end date!");
            return;
        }
        
        reportStartDate = startDate;
        reportEndDate = endDate;
        
        loadSalesByDateRange(startDate.atStartOfDay(), endDate.plusDays(1).atStartOfDay());
    }

    @FXML
    private void loadAllSales() {
        reportStartDate = null;
        reportEndDate = null;
        
        sales.clear();
        try (Connection con = Database.connect()) {
            if (con == null) {
                showAlert("Database connection failed!");
                return;
            }
            
            String query = "SELECT * FROM sales ORDER BY saleDate DESC";
            ResultSet rs = con.createStatement().executeQuery(query);
            
            loadSalesFromResultSet(rs);
            updateSummary();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading sales: " + e.getMessage());
        }
    }

    private void loadSalesByDateRange(LocalDateTime start, LocalDateTime end) {
        sales.clear();
        try (Connection con = Database.connect()) {
            if (con == null) {
                showAlert("Database connection failed!");
                return;
            }
            
            String query = "SELECT * FROM sales WHERE saleDate >= ? AND saleDate < ? ORDER BY saleDate DESC";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setTimestamp(1, Timestamp.valueOf(start));
            ps.setTimestamp(2, Timestamp.valueOf(end));
            
            ResultSet rs = ps.executeQuery();
            loadSalesFromResultSet(rs);
            updateSummary();
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading sales: " + e.getMessage());
        }
    }

    private void loadSalesFromResultSet(ResultSet rs) throws SQLException {
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
    }

    private void updateSummary() {
        int transactionsCount = sales.size();
        double totalSales = 0.0;
        int totalItems = 0;
        
        for (Sales sale : sales) {
            totalSales += sale.getTotalAmount();
            totalItems += sale.getTotalItems();
        }
        
        double averageSale = transactionsCount > 0 ? totalSales / transactionsCount : 0.0;
        
        transactionsCountLabel.setText(String.valueOf(transactionsCount));
        totalSalesLabel.setText(String.format("₱%.2f", totalSales));
        totalItemsLabel.setText(String.valueOf(totalItems));
        averageSaleLabel.setText(String.format("₱%.2f", averageSale));
    }

    @FXML
    private void exportToCSV() {
        if (sales.isEmpty()) {
            showAlert("No data to export!");
            return;
        }
        
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save CSV Report");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        
        // Set default filename
        String defaultFileName = "SalesReport_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".csv";
        fileChooser.setInitialFileName(defaultFileName);
        
        Stage stage = (Stage) reportsTable.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write header
                writer.append("Sale ID,Date & Time,Total Amount,Items,Cashier\n");
                
                // Write data
                for (Sales sale : sales) {
                    writer.append(String.valueOf(sale.getId())).append(",");
                    writer.append(sale.getSaleDate() != null ? 
                        sale.getSaleDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "").append(",");
                    writer.append(String.format("%.2f", sale.getTotalAmount())).append(",");
                    writer.append(String.valueOf(sale.getTotalItems())).append(",");
                    writer.append(sale.getCashierEmail() != null ? sale.getCashierEmail() : "N/A").append("\n");
                }
                
                // Write summary
                writer.append("\n");
                writer.append("Summary\n");
                writer.append("Total Transactions,").append(String.valueOf(sales.size())).append("\n");
                
                double totalSales = sales.stream().mapToDouble(Sales::getTotalAmount).sum();
                int totalItems = sales.stream().mapToInt(Sales::getTotalItems).sum();
                double averageSale = sales.size() > 0 ? totalSales / sales.size() : 0.0;
                
                writer.append("Total Sales,").append(String.format("%.2f", totalSales)).append("\n");
                writer.append("Total Items Sold,").append(String.valueOf(totalItems)).append("\n");
                writer.append("Average Sale,").append(String.format("%.2f", averageSale)).append("\n");
                
                if (reportStartDate != null && reportEndDate != null) {
                    writer.append("Report Period,").append(reportStartDate.toString())
                          .append(" to ").append(reportEndDate.toString()).append("\n");
                }
                
                showAlert(Alert.AlertType.INFORMATION, "Success", "Report exported to CSV successfully!");
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error exporting to CSV: " + e.getMessage());
            }
        }
    }

    private void createSalesTableIfNotExists() {
        try (Connection con = Database.connect()) {
            if (con == null) return;
            
            String createSalesTable = "CREATE TABLE IF NOT EXISTS sales (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "saleDate DATETIME NOT NULL, " +
                    "totalAmount DECIMAL(10,2) NOT NULL, " +
                    "totalItems INT NOT NULL, " +
                    "cashierEmail VARCHAR(255)" +
                    ")";
            con.createStatement().executeUpdate(createSalesTable);
            
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

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}

