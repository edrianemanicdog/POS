package app;

import java.util.List;

public class ProductModifier {

    private int id;
    private int productId;
    private String productName; // for table display
    private String name;
    private String type; // single or multiple
    private boolean required;
    private List<ProductModifierOption> options;

    public ProductModifier() {}

    // Constructor without productName
    public ProductModifier(int id, int productId, String name, String type, boolean required) {
        this.id = id;
        this.productId = productId;
        this.name = name;
        this.type = type;
        this.required = required;
    }

    // Constructor with productName (for table display)
    public ProductModifier(int id, String name, String type, boolean required, String productName) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.required = required;
        this.productName = productName;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isRequired() { return required; }
    public void setRequired(boolean required) { this.required = required; }

    public List<ProductModifierOption> getOptions() { return options; }
    public void setOptions(List<ProductModifierOption> options) { this.options = options; }
}
