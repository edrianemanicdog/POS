package controller;

import app.CartItem;
import app.Main;
import app.Product;
import app.UserSession;
import controller.ProductSelectionDialog;
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
import javafx.scene.layout.HBox;

public class POSController implements Initializable {

    @FXML private TableView<Product> productTable;
    @FXML private TableColumn<Product, Integer> colProductId;
    @FXML private TableColumn<Product, String> colProductName;
    @FXML private TableColumn<Product, Double> colProductPrice;
    @FXML private TableColumn<Product, Integer> colProductStock;
    @FXML private TableColumn<Product, String> colProductCategory;
    
    @FXML private TableView<CartItem> cartTable;
    @FXML private TableColumn<CartItem, String> colCartProduct;
    @FXML private TableColumn<CartItem, Double> colCartPrice;
    @FXML private TableColumn<CartItem, Integer> colCartQuantity;
    @FXML private TableColumn<CartItem, Double> colCartSubtotal;
    
    @FXML private TextField searchField;
    @FXML private Label totalItemsLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label cartItemCountLabel;
    @FXML private Label headerTotalLabel;
    @FXML private Label productCountLabel;
    @FXML private TextField discountField;
    @FXML private TextField cashField;
    @FXML private Label changeLabel;
    @FXML private HBox categoryBar; // Reference to the HBox in the ScrollPane

    private ObservableList<Product> products = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartItems = FXCollections.observableArrayList();
    private double currentTotalAmount = 0.0; // total after cart-level discount
    private double currentDiscountPercent = 0.0;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupProductTable();
        setupCartTable();
        loadProducts();
        createSalesTableIfNotExists();
        loadCategories(); // Added this
        
        // Initialize cash/change display
        if (changeLabel != null) {
            changeLabel.setText("₱0.00");
        }
        if (cashField != null) {
            cashField.textProperty().addListener((obs, oldVal, newVal) -> updateChangeDisplay());
        }
        if (discountField != null) {
            discountField.setText("0");
            discountField.textProperty().addListener((obs, oldVal, newVal) -> updateTotals());
        }
        
        // Add double-click to add product to cart
        productTable.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    addToCart(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupProductTable() {
        colProductId.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getId()).asObject());
        colProductName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        colProductPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        colProductStock.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getCurrentStock()).asObject());
        colProductCategory.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().getCategoryName() != null ? data.getValue().getCategoryName() : "N/A"));
        
        // Add action column with "Add" button
        TableColumn<Product, Void> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(100);
        colAction.setCellFactory(param -> new TableCell<Product, Void>() {
            private final Button btn = new Button("Add");
            
            {
                btn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    addToCart(product);
                });
                btn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-background-radius: 3;");
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
        productTable.getColumns().add(colAction);
        
        productTable.setItems(products);
    }

    private void setupCartTable() {
        colCartProduct.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProductName()));
        colCartPrice.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getPrice()).asObject());
        colCartQuantity.setCellValueFactory(data -> new SimpleIntegerProperty(data.getValue().getQuantity()).asObject());
        colCartSubtotal.setCellValueFactory(data -> new SimpleDoubleProperty(data.getValue().getSubtotal()).asObject());
        
        // Format price column
        colCartPrice.setCellFactory(column -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText("");
                } else {
                    setText(String.format("₱%.2f", price));
                }
            }
        });
        
        // Format subtotal column
        colCartSubtotal.setCellFactory(column -> new TableCell<CartItem, Double>() {
            @Override
            protected void updateItem(Double subtotal, boolean empty) {
                super.updateItem(subtotal, empty);
                if (empty || subtotal == null) {
                    setText("");
                } else {
                    setText(String.format("₱%.2f", subtotal));
                }
            }
        });
        // Make quantity editable
        colCartQuantity.setCellFactory(column -> new TableCell<CartItem, Integer>() {
            private final TextField textField = new TextField();
            private final HBox controls = new HBox(0);
            private final Button btnMinus = new Button("−");
            private final Button btnPlus = new Button("+");
            
            {
                controls.setAlignment(javafx.geometry.Pos.CENTER);
                controls.setSpacing(0);
                
                // Minus button - rounded, subtle
                btnMinus.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #e74c3c; " +
                                 "-fx-background-radius: 15 0 0 15; -fx-border-radius: 15 0 0 15; " +
                                 "-fx-border-color: #dee2e6; -fx-border-width: 1; " +
                                 "-fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32; " +
                                 "-fx-font-size: 18px; -fx-font-weight: bold; " +
                                 "-fx-cursor: hand; -fx-padding: 0;");
                btnMinus.setOnMouseEntered(e -> btnMinus.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #e74c3c; " +
                                 "-fx-background-radius: 15 0 0 15; -fx-border-radius: 15 0 0 15; " +
                                 "-fx-border-color: #dee2e6; -fx-border-width: 1; " +
                                 "-fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32; " +
                                 "-fx-font-size: 18px; -fx-font-weight: bold; " +
                                 "-fx-cursor: hand; -fx-padding: 0;"));
                btnMinus.setOnMouseExited(e -> btnMinus.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #e74c3c; " +
                                 "-fx-background-radius: 15 0 0 15; -fx-border-radius: 15 0 0 15; " +
                                 "-fx-border-color: #dee2e6; -fx-border-width: 1; " +
                                 "-fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32; " +
                                 "-fx-font-size: 18px; -fx-font-weight: bold; " +
                                 "-fx-cursor: hand; -fx-padding: 0;"));
                
                // Plus button - rounded, subtle
                btnPlus.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #27ae60; " +
                                "-fx-background-radius: 0 15 15 0; -fx-border-radius: 0 15 15 0; " +
                                "-fx-border-color: #dee2e6; -fx-border-width: 1; " +
                                "-fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32; " +
                                "-fx-font-size: 16px; -fx-font-weight: bold; " +
                                "-fx-cursor: hand; -fx-padding: 0;");
                btnPlus.setOnMouseEntered(e -> btnPlus.setStyle("-fx-background-color: #e9ecef; -fx-text-fill: #27ae60; " +
                                "-fx-background-radius: 0 15 15 0; -fx-border-radius: 0 15 15 0; " +
                                "-fx-border-color: #dee2e6; -fx-border-width: 1; " +
                                "-fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32; " +
                                "-fx-font-size: 16px; -fx-font-weight: bold; " +
                                "-fx-cursor: hand; -fx-padding: 0;"));
                btnPlus.setOnMouseExited(e -> btnPlus.setStyle("-fx-background-color: #f8f9fa; -fx-text-fill: #27ae60; " +
                                "-fx-background-radius: 0 15 15 0; -fx-border-radius: 0 15 15 0; " +
                                "-fx-border-color: #dee2e6; -fx-border-width: 1; " +
                                "-fx-min-width: 32; -fx-min-height: 32; -fx-max-width: 32; -fx-max-height: 32; " +
                                "-fx-font-size: 16px; -fx-font-weight: bold; " +
                                "-fx-cursor: hand; -fx-padding: 0;"));
                
                // Text field - integrated look
                textField.setStyle("-fx-alignment: center; -fx-pref-width: 50; -fx-pref-height: 32; " +
                                  "-fx-background-color: white; -fx-border-color: #dee2e6; " +
                                  "-fx-border-width: 1 0 1 0; -fx-border-radius: 0; " +
                                  "-fx-font-size: 14px; -fx-font-weight: bold; " +
                                  "-fx-padding: 0;");
                
                btnMinus.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    if (item.getQuantity() > 1) {
                        item.setQuantity(item.getQuantity() - 1);
                        updateTotals();
                        getTableView().refresh();
                    }
                });
                
                btnPlus.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    int maxStock = item.getStock();
                    if (item.getQuantity() < maxStock) {
                        item.setQuantity(item.getQuantity() + 1);
                        updateTotals();
                        getTableView().refresh();
                    } else {
                        showAlert("Cannot add more! Available stock: " + maxStock);
                    }
                });
                
                textField.setOnAction(e -> {
                    try {
                        int newQty = Integer.parseInt(textField.getText());
                        CartItem item = getTableView().getItems().get(getIndex());
                        int maxStock = item.getStock();
                        
                        if (newQty <= 0) {
                            showAlert("Quantity must be greater than 0!");
                            textField.setText(String.valueOf(item.getQuantity()));
                            return;
                        }
                        
                        if (newQty > maxStock) {
                            showAlert("Insufficient stock! Available: " + maxStock);
                            textField.setText(String.valueOf(item.getQuantity()));
                            return;
                        }
                        
                        item.setQuantity(newQty);
                        updateTotals();
                        getTableView().refresh();
                    } catch (NumberFormatException ex) {
                        CartItem item = getTableView().getItems().get(getIndex());
                        textField.setText(String.valueOf(item.getQuantity()));
                    }
                });
                
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        try {
                            int newQty = Integer.parseInt(textField.getText());
                            CartItem item = getTableView().getItems().get(getIndex());
                            int maxStock = item.getStock();
                            
                            if (newQty <= 0) {
                                textField.setText(String.valueOf(item.getQuantity()));
                                return;
                            }
                            
                            if (newQty > maxStock) {
                                showAlert("Insufficient stock! Available: " + maxStock);
                                textField.setText(String.valueOf(item.getQuantity()));
                                return;
                            }
                            
                            item.setQuantity(newQty);
                            updateTotals();
                            getTableView().refresh();
                        } catch (NumberFormatException ex) {
                            CartItem item = getTableView().getItems().get(getIndex());
                            textField.setText(String.valueOf(item.getQuantity()));
                        }
                    }
                });
            }
            
            @Override
            protected void updateItem(Integer quantity, boolean empty) {
                super.updateItem(quantity, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    CartItem item = getTableView().getItems().get(getIndex());
                    textField.setText(String.valueOf(item.getQuantity()));
                    controls.getChildren().setAll(btnMinus, textField, btnPlus);
                    setGraphic(controls);
                }
            }
        });
        
        // Add action column with "Remove" button
        TableColumn<CartItem, Void> colAction = new TableColumn<>("Action");
        colAction.setPrefWidth(60);
        colAction.setCellFactory(param -> new TableCell<CartItem, Void>() {
            private final Button btn = new Button("X");
            
            {
                btn.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    removeFromCart(item);
                });
                btn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-background-radius: 3; -fx-min-width: 30;");
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
        cartTable.getColumns().add(colAction);
        
        cartTable.setItems(cartItems);
    }

    private void loadProducts() {
        products.clear();
        try (Connection con = Database.connect()) {
            if (con == null) {
                showAlert("Database connection failed!");
                return;
            }
            
            String query = "SELECT p.id, p.name, p.description, p.price, p.currentStock, " +
                    "c.name AS category, s.name AS supplier " +
                    "FROM Product p " +
                    "LEFT JOIN Category c ON p.categoryId = c.id " +
                    "LEFT JOIN Supplier s ON p.supplierId = s.id " +
                    "WHERE p.isActive = true AND p.currentStock > 0 " +
                    "ORDER BY p.name";
            
            ResultSet rs = con.createStatement().executeQuery(query);
            
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setCurrentStock(rs.getInt("currentStock"));
                product.setCategoryName(rs.getString("category"));
                product.setSupplierName(rs.getString("supplier"));
                products.add(product);
            }
            
            // Update product count
            if (productCountLabel != null) {
                productCountLabel.setText("Products: " + products.size());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error loading products: " + e.getMessage());
        }
    }

    @FXML
    private void searchProducts() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        
        if (searchTerm.isEmpty()) {
            loadProducts();
            return;
        }
        
        products.clear();
        try (Connection con = Database.connect()) {
            if (con == null) {
                showAlert("Database connection failed!");
                return;
            }
            
            String query = "SELECT p.id, p.name, p.description, p.price, p.currentStock, " +
                    "c.name AS category, s.name AS supplier " +
                    "FROM Product p " +
                    "LEFT JOIN Category c ON p.categoryId = c.id " +
                    "LEFT JOIN Supplier s ON p.supplierId = s.id " +
                    "WHERE p.isActive = true AND p.currentStock > 0 " +
                    "AND (LOWER(p.name) LIKE ? OR LOWER(p.description) LIKE ? OR LOWER(p.sku) LIKE ?) " +
                    "ORDER BY p.name";
            
            PreparedStatement ps = con.prepareStatement(query);
            String searchPattern = "%" + searchTerm + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ps.setString(3, searchPattern);
            
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setName(rs.getString("name"));
                product.setDescription(rs.getString("description"));
                product.setPrice(rs.getDouble("price"));
                product.setCurrentStock(rs.getInt("currentStock"));
                product.setCategoryName(rs.getString("category"));
                product.setSupplierName(rs.getString("supplier"));
                products.add(product);
            }
            
            // Update product count
            if (productCountLabel != null) {
                productCountLabel.setText("Products: " + products.size());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error searching products: " + e.getMessage());
        }
    }

    @FXML
    private void clearSearch() {
        searchField.clear();
        loadProducts();
    }

    @FXML
    private void addToCart(Product product) {
        if (product == null) return;
        
        // Check stock availability
        if (product.getCurrentStock() <= 0) {
            showAlert("Product is out of stock!");
            return;
        }
        
        // Show product selection dialog
        ProductSelectionDialog dialog = new ProductSelectionDialog(product);
        ProductSelectionDialog.ProductSelectionResult result = dialog.showAndWait().orElse(null);
        
        if (result == null) {
            return; // User cancelled
        }
        
        int quantity = result.getQuantity();
        
        // Check if product with same configuration already in cart
        for (CartItem item : cartItems) {
            boolean sameVariant = (item.getSelectedVariant() == null && result.getVariant() == null) ||
                                  (item.getSelectedVariant() != null && result.getVariant() != null &&
                                   item.getSelectedVariant().getId() == result.getVariant().getId());
            
            boolean sameModifiers = item.getSelectedModifiers().size() == result.getModifiers().size() &&
                                  item.getSelectedModifiers().containsAll(result.getModifiers());
            
            boolean sameBundle = (item.getSelectedBundle() == null && result.getBundle() == null) ||
                                (item.getSelectedBundle() != null && result.getBundle() != null &&
                                 item.getSelectedBundle().getId() == result.getBundle().getId());
            
            if (item.getProduct().getId() == product.getId() && sameVariant && sameModifiers && sameBundle) {
                // Update quantity if adding same product with same configuration
                int newQuantity = item.getQuantity() + quantity;
                int availableStock = result.getVariant() != null ? result.getVariant().getStock() : product.getCurrentStock();
                if (newQuantity > availableStock) {
                    showAlert("Cannot add more! Available stock: " + availableStock);
                    return;
                }
                item.setQuantity(newQuantity);
                updateTotals();
                return;
            }
        }
        
        // Add new item to cart with selections
        CartItem cartItem = new CartItem(
            product,
            result.getVariant(),
            result.getModifiers(),
            result.getBundle(),
            quantity
        );
        cartItems.add(cartItem);
        updateTotals();
    }

    @FXML
    private void removeFromCart(CartItem item) {
        cartItems.remove(item);
        updateTotals();
    }

    @FXML
    private void clearCart() {
        cartItems.clear();
        updateTotals();
        if (cashField != null) {
            cashField.clear();
        }
        if (changeLabel != null) {
            changeLabel.setText("₱0.00");
        }
    }

    private void updateTotals() {
        int totalItems = 0;
        double grossTotal = 0.0;
        
        for (CartItem item : cartItems) {
            totalItems += item.getQuantity();
            grossTotal += item.getSubtotal();
        }
        
        // Parse cart-level discount percentage
        double discountPercent = 0.0;
        if (discountField != null) {
            String text = discountField.getText() != null ? discountField.getText().trim() : "";
            if (!text.isEmpty()) {
                try {
                    discountPercent = Double.parseDouble(text);
                } catch (NumberFormatException e) {
                    discountPercent = 0.0;
                }
            }
        }
        if (discountPercent < 0) discountPercent = 0;
        if (discountPercent > 100) discountPercent = 100;
        currentDiscountPercent = discountPercent;

        double discountAmount = grossTotal * (discountPercent / 100.0);
        double netTotal = grossTotal - discountAmount;
        currentTotalAmount = netTotal;
        
        totalItemsLabel.setText(String.valueOf(totalItems));
        String formattedAmount = String.format("₱%.2f", netTotal);
        totalAmountLabel.setText(formattedAmount);
        
        // Update header total label if it exists
        if (headerTotalLabel != null) {
            headerTotalLabel.setText(formattedAmount);
        }
        
        // Update cart item count label
        if (cartItemCountLabel != null) {
            int itemCount = cartItems.size();
            if (itemCount == 0) {
                cartItemCountLabel.setText("Cart: 0 items");
            } else if (itemCount == 1) {
                cartItemCountLabel.setText("Cart: 1 item");
            } else {
                cartItemCountLabel.setText("Cart: " + itemCount + " items");
            }
        }
        
        // Update product count label
        if (productCountLabel != null) {
            productCountLabel.setText("Products: " + products.size());
        }

        // Update change display based on current cash input
        updateChangeDisplay();
    }

    private void updateChangeDisplay() {
        if (cashField == null || changeLabel == null) {
            return;
        }
        String cashText = cashField.getText() != null ? cashField.getText().trim() : "";
        double cash = 0.0;
        if (!cashText.isEmpty()) {
            try {
                cash = Double.parseDouble(cashText);
            } catch (NumberFormatException e) {
                changeLabel.setText("₱0.00");
                return;
            }
        }
        double change = cash - currentTotalAmount;
        if (change < 0) {
            changeLabel.setText("₱0.00");
        } else {
            changeLabel.setText(String.format("₱%.2f", change));
        }
    }

    @FXML
    private void completeSale() {
        if (cartItems.isEmpty()) {
            showAlert("Cart is empty! Please add products to cart.");
            return;
        }

        // Validate cash received and compute change
        double cashReceived;
        double totalAmountForPayment = currentTotalAmount;

        if (cashField == null) {
            showAlert("Cash input field is not available.");
            return;
        }

        String cashText = cashField.getText() != null ? cashField.getText().trim() : "";
        if (cashText.isEmpty()) {
            showAlert("Please enter the cash received from the customer.");
            return;
        }
        try {
            cashReceived = Double.parseDouble(cashText);
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid cash amount.");
            return;
        }
        if (cashReceived < totalAmountForPayment) {
            showAlert(String.format("Insufficient cash. Total is ₱%.2f, received ₱%.2f.", totalAmountForPayment, cashReceived));
            return;
        }
        double change = cashReceived - totalAmountForPayment;
        
        String cashierEmail = UserSession.getCurrentUserEmail();
        if (cashierEmail == null || cashierEmail.isEmpty()) {
            cashierEmail = "guest"; // Fallback if no user session
        }
        
        try (Connection con = Database.connect()) {
            if (con == null) {
                showAlert("Database connection failed!");
                return;
            }
            
            con.setAutoCommit(false); // Start transaction
            
            try {
                // Calculate totals
                int totalItems = 0;
                for (CartItem item : cartItems) {
                    totalItems += item.getQuantity();
                }
                double totalAmount = currentTotalAmount;
                
                // Insert sale record
                String saleQuery = "INSERT INTO sales (saleDate, totalAmount, totalItems, cashierEmail) VALUES (?, ?, ?, ?)";
                PreparedStatement saleStmt = con.prepareStatement(saleQuery, Statement.RETURN_GENERATED_KEYS);
                saleStmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                saleStmt.setDouble(2, totalAmount);
                saleStmt.setInt(3, totalItems);
                saleStmt.setString(4, cashierEmail);
                saleStmt.executeUpdate();
                
                // Get generated sale ID
                ResultSet rs = saleStmt.getGeneratedKeys();
                int saleId = 0;
                if (rs.next()) {
                    saleId = rs.getInt(1);
                }
                
                // Insert sale items
                String itemQuery = "INSERT INTO sale_items (saleId, productId, productName, quantity, price, subtotal) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement itemStmt = con.prepareStatement(itemQuery);
                
                for (CartItem item : cartItems) {
                    itemStmt.setInt(1, saleId);
                    itemStmt.setInt(2, item.getProduct().getId());
                    itemStmt.setString(3, item.getProduct().getName());
                    itemStmt.setInt(4, item.getQuantity());
                    itemStmt.setDouble(5, item.getPrice());
                    itemStmt.setDouble(6, item.getSubtotal());
                    itemStmt.addBatch();
                    
                    // Update product stock
                    String updateStockQuery = "UPDATE Product SET currentStock = currentStock - ? WHERE id = ?";
                    PreparedStatement stockStmt = con.prepareStatement(updateStockQuery);
                    stockStmt.setInt(1, item.getQuantity());
                    stockStmt.setInt(2, item.getProduct().getId());
                    stockStmt.executeUpdate();
                }
                
                itemStmt.executeBatch();
                
                con.commit(); // Commit transaction
                
                // Show success message
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Sale Completed");
                alert.setHeaderText(null);
                alert.setContentText(String.format(
                    "Sale completed successfully!\nTotal: ₱%.2f\nDiscount: %.0f%%\nCash: ₱%.2f\nChange: ₱%.2f\nItems: %d",
                    totalAmount, currentDiscountPercent, cashReceived, change, totalItems));
                alert.showAndWait();
                
                // Clear cart and reload products
                clearCart();
                loadProducts();
                
            } catch (SQLException e) {
                con.rollback(); // Rollback on error
                throw e;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Error completing sale: " + e.getMessage());
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
    
    private void loadCategories() {
    categoryBar.getChildren().clear();
    
    // 1. Add "All" Button
    Button allBtn = createCategoryButton("All", -1);
    allBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 15; -fx-padding: 5 15; -fx-font-weight: bold;");
    categoryBar.getChildren().add(allBtn);

    try (Connection con = Database.connect()) {
        if (con == null) return;
        String query = "SELECT id, name FROM Category ORDER BY name";
        ResultSet rs = con.createStatement().executeQuery(query);
        
        while (rs.next()) {
            Button catBtn = createCategoryButton(rs.getString("name"), rs.getInt("id"));
            categoryBar.getChildren().add(catBtn);
        }
    } catch (SQLException e) {
        e.printStackTrace();
    }
}

private Button createCategoryButton(String name, int categoryId) {
    Button btn = new Button(name);
    
    // Increased horizontal padding (20) and added a standard font size
    String base = "-fx-background-color: #f8f9fa; " +
                  "-fx-border-color: #dee2e6; " +
                  "-fx-border-radius: 20; " +
                  "-fx-background-radius: 20; " +
                  "-fx-cursor: hand; " +
                  "-fx-padding: 8 20; " + // More breathing room
                  "-fx-text-fill: #2c3e50; " +
                  "-fx-font-size: 13px;";

    String active = "-fx-background-color: #3498db; " +
                    "-fx-text-fill: white; " +
                    "-fx-border-color: #2980b9; " +
                    "-fx-background-radius: 20; " +
                    "-fx-border-radius: 20; " +
                    "-fx-font-weight: bold;";
    
    btn.setStyle(base);
    
    btn.setOnAction(e -> {
        categoryBar.getChildren().forEach(n -> n.setStyle(base));
        btn.setStyle(base + active); // Combine base and active
        filterByCategory(categoryId);
    });
    
    return btn;
}

private void filterByCategory(int categoryId) {
    if (categoryId == -1) {
        loadProducts(); // "All" selected
        return;
    }

    products.clear();
    try (Connection con = Database.connect()) {
        String query = "SELECT p.id, p.name, p.description, p.price, p.currentStock, " +
                       "c.name AS category, s.name AS supplier " +
                       "FROM Product p " +
                       "LEFT JOIN Category c ON p.categoryId = c.id " +
                       "LEFT JOIN Supplier s ON p.supplierId = s.id " +
                       "WHERE p.isActive = true AND p.currentStock > 0 AND p.categoryId = ? " +
                       "ORDER BY p.name";
        
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, categoryId);
        ResultSet rs = ps.executeQuery();
        
        while (rs.next()) {
            Product product = new Product();
            product.setId(rs.getInt("id"));
            product.setName(rs.getString("name"));
            product.setDescription(rs.getString("description"));
            product.setPrice(rs.getDouble("price"));
            product.setCurrentStock(rs.getInt("currentStock"));
            product.setCategoryName(rs.getString("category"));
            product.setSupplierName(rs.getString("supplier"));
            products.add(product);
        }
        
        if (productCountLabel != null) {
            productCountLabel.setText("Products: " + products.size());
        }
    } catch (SQLException e) {
        e.printStackTrace();
        showAlert("Error filtering by category: " + e.getMessage());
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
