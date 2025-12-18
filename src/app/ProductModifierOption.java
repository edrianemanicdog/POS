package app;

public class ProductModifierOption {
    private int id;
    private int modifierId;
    private String name;
    private double price;
    private String modifierName; // Helper field for display

    public ProductModifierOption() {}

    public ProductModifierOption(int id, int modifierId, String name, double price) {
        this.id = id;
        this.modifierId = modifierId;
        this.name = name;
        this.price = price;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getModifierId() { return modifierId; }
    public void setModifierId(int modifierId) { this.modifierId = modifierId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getModifierName() { return modifierName; }
    public void setModifierName(String modifierName) { this.modifierName = modifierName; }
}
