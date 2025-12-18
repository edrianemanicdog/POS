package app;

public class ProductBundle {
    private int id;
    private int bundleProductId;
    private int itemProductId;
    private int quantity;
    private String bundleProductName; // optional, for display
    private String itemProductName;   // optional, for display

    public ProductBundle() {}

    // Constructor for ID + IDs + quantity
    public ProductBundle(int id, int bundleProductId, int itemProductId, int quantity) {
        this.id = id;
        this.bundleProductId = bundleProductId;
        this.itemProductId = itemProductId;
        this.quantity = quantity;
    }

    // Constructor with product names for display
    public ProductBundle(int id, int bundleProductId, int itemProductId, int quantity, String bundleProductName, String itemProductName) {
        this.id = id;
        this.bundleProductId = bundleProductId;
        this.itemProductId = itemProductId;
        this.quantity = quantity;
        this.bundleProductName = bundleProductName;
        this.itemProductName = itemProductName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBundleProductId() { return bundleProductId; }
    public void setBundleProductId(int bundleProductId) { this.bundleProductId = bundleProductId; }

    public int getItemProductId() { return itemProductId; }
    public void setItemProductId(int itemProductId) { this.itemProductId = itemProductId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getBundleProductName() { return bundleProductName; }
    public void setBundleProductName(String bundleProductName) { this.bundleProductName = bundleProductName; }

    public String getItemProductName() { return itemProductName; }
    public void setItemProductName(String itemProductName) { this.itemProductName = itemProductName; }
}
