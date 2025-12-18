package app;

public class ProductVariant {

    private int id;
    private int productId;
    private String name;
    private String sku;
    private String barcode;
    private double price;
    private Double cost;
    private int stock;
    private String attributes;
    private boolean isActive;

    // Helper field for display in TableView
    private String productName;

    public ProductVariant() {}

    // Constructor including productName for TableView
    public ProductVariant(int id, String name, String sku, String barcode, double price, Double cost, int stock, String attributes, String productName) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.barcode = barcode;
        this.price = price;
        this.cost = cost;
        this.stock = stock;
        this.attributes = attributes;
        this.productName = productName;
        this.isActive = true; // Default value
    }
    
    // Constructor with isActive
    public ProductVariant(int id, String name, String sku, String barcode, double price, Double cost, int stock, String attributes, boolean isActive, String productName) {
        this.id = id;
        this.name = name;
        this.sku = sku;
        this.barcode = barcode;
        this.price = price;
        this.cost = cost;
        this.stock = stock;
        this.attributes = attributes;
        this.isActive = isActive;
        this.productName = productName;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public String getAttributes() { return attributes; }
    public void setAttributes(String attributes) { this.attributes = attributes; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public boolean isIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }
}
