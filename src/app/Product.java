package app;

import java.util.List;

public class Product {
    private int id;
    private String name;
    private String description;
    private String categoryName;
    private String supplierName;
    private String sku;
    private String barcode;
    private String inventoryTracking;
    private String baseUnit;
    private double price;
    private Double cost;
    // Discount percentage (0–100) configured by admin
    private double discount;
    private int initialStock;
    private int currentStock;
    private int reorderLevel;
    private String productType;
    private boolean isActive;

    private List<ProductVariant> variants;
    private List<ProductModifier> modifiers;
    private List<ProductBundle> bundleItems;

    // Full constructor
    public Product(int id, String name, String description, String categoryName, String supplierName,
                   String sku, String barcode, String inventoryTracking, String baseUnit, double price,
                   Double cost, int initialStock, int currentStock, int reorderLevel,
                   String productType, boolean isActive) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryName = categoryName;
        this.supplierName = supplierName;
        this.sku = sku;
        this.barcode = barcode;
        this.inventoryTracking = inventoryTracking;
        this.baseUnit = baseUnit;
        this.price = price;
        this.cost = cost;
        this.initialStock = initialStock;
        this.currentStock = currentStock;
        this.reorderLevel = reorderLevel;
        this.productType = productType;
        this.isActive = isActive;
        this.discount = 0.0;
    }

    // Lightweight constructor for TableView including description
    public Product(int id, String name, String description, String categoryName, String supplierName, double price, int stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.categoryName = categoryName;
        this.supplierName = supplierName;
        this.price = price;
        this.currentStock = stock;
        this.discount = 0.0;
    }

    // Default constructor
    public Product() {
        this.discount = 0.0;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getSku() { return sku; }
    public void setSku(String sku) { this.sku = sku; }

    public String getBarcode() { return barcode; }
    public void setBarcode(String barcode) { this.barcode = barcode; }

    public String getInventoryTracking() { return inventoryTracking; }
    public void setInventoryTracking(String inventoryTracking) { this.inventoryTracking = inventoryTracking; }

    public String getBaseUnit() { return baseUnit; }
    public void setBaseUnit(String baseUnit) { this.baseUnit = baseUnit; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }

    public double getDiscount() { return discount; }
    public void setDiscount(double discount) {
        if (discount < 0) {
            discount = 0;
        } else if (discount > 100) {
            discount = 100;
        }
        this.discount = discount;
    }

    public int getInitialStock() { return initialStock; }
    public void setInitialStock(int initialStock) { this.initialStock = initialStock; }

    public int getCurrentStock() { return currentStock; }
    public void setCurrentStock(int currentStock) { this.currentStock = currentStock; }

    public int getReorderLevel() { return reorderLevel; }
    public void setReorderLevel(int reorderLevel) { this.reorderLevel = reorderLevel; }

    public String getProductType() { return productType; }
    public void setProductType(String productType) { this.productType = productType; }

    public boolean isIsActive() { return isActive; }
    public void setIsActive(boolean isActive) { this.isActive = isActive; }

    public List<ProductVariant> getVariants() { return variants; }
    public void setVariants(List<ProductVariant> variants) { this.variants = variants; }

    public List<ProductModifier> getModifiers() { return modifiers; }
    public void setModifiers(List<ProductModifier> modifiers) { this.modifiers = modifiers; }

    public List<ProductBundle> getBundleItems() { return bundleItems; }
    public void setBundleItems(List<ProductBundle> bundleItems) { this.bundleItems = bundleItems; }

    // TableView-friendly methods
    public String getCategory() { return categoryName; }
    public String getSupplier() { return supplierName; }
    public int getStock() { return currentStock; }

    // Optional: placeholder for Category/Supplier IDs if needed later
    private int categoryId;
    private int supplierId;

    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public void setSupplierId(int supplierId) { this.supplierId = supplierId; }
    public int getCategoryId() { return categoryId; }
    public int getSupplierId() { return supplierId; }
}
