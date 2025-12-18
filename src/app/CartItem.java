package app;

import java.util.ArrayList;
import java.util.List;

public class CartItem {
    private Product product;
    private ProductVariant selectedVariant;
    private List<ProductModifierOption> selectedModifiers;
    private ProductBundle selectedBundle;
    private int quantity;
    private double subtotal;
    
    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.selectedModifiers = new ArrayList<>();
        calculateSubtotal();
    }
    
    public CartItem(Product product, ProductVariant variant, List<ProductModifierOption> modifiers, ProductBundle bundle, int quantity) {
        this.product = product;
        this.selectedVariant = variant;
        this.selectedModifiers = modifiers != null ? modifiers : new ArrayList<>();
        this.selectedBundle = bundle;
        this.quantity = quantity;
        calculateSubtotal();
    }
    
    private void calculateSubtotal() {
        double basePrice = selectedVariant != null ? selectedVariant.getPrice() : product.getPrice();
        double modifierPrice = 0.0;
        for (ProductModifierOption option : selectedModifiers) {
            modifierPrice += option.getPrice();
        }
        this.subtotal = (basePrice + modifierPrice) * quantity;
    }
    
    // Getters and Setters
    public Product getProduct() {
        return product;
    }
    
    public void setProduct(Product product) {
        this.product = product;
        calculateSubtotal();
    }
    
    public ProductVariant getSelectedVariant() {
        return selectedVariant;
    }
    
    public void setSelectedVariant(ProductVariant selectedVariant) {
        this.selectedVariant = selectedVariant;
        calculateSubtotal();
    }
    
    public List<ProductModifierOption> getSelectedModifiers() {
        return selectedModifiers;
    }
    
    public void setSelectedModifiers(List<ProductModifierOption> selectedModifiers) {
        this.selectedModifiers = selectedModifiers != null ? selectedModifiers : new ArrayList<>();
        calculateSubtotal();
    }
    
    public ProductBundle getSelectedBundle() {
        return selectedBundle;
    }
    
    public void setSelectedBundle(ProductBundle selectedBundle) {
        this.selectedBundle = selectedBundle;
    }
    
    public int getQuantity() {
        return quantity;
    }
    
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        calculateSubtotal();
    }
    
    public double getSubtotal() {
        return subtotal;
    }
    
    public void updateSubtotal() {
        calculateSubtotal();
    }
    
    // Helper methods for TableView
    public String getProductName() {
        StringBuilder name = new StringBuilder(product.getName());
        if (selectedVariant != null) {
            name.append(" - ").append(selectedVariant.getName());
        }
        if (selectedBundle != null) {
            name.append(" (Bundle)");
        }
        if (!selectedModifiers.isEmpty()) {
            name.append(" [");
            for (int i = 0; i < selectedModifiers.size(); i++) {
                if (i > 0) name.append(", ");
                name.append(selectedModifiers.get(i).getName());
            }
            name.append("]");
        }
        return name.toString();
    }
    
    public double getPrice() {
        double basePrice = selectedVariant != null ? selectedVariant.getPrice() : product.getPrice();
        double modifierPrice = 0.0;
        for (ProductModifierOption option : selectedModifiers) {
            modifierPrice += option.getPrice();
        }
        return basePrice + modifierPrice;
    }
    
    public int getProductId() {
        return product.getId();
    }
    
    public int getVariantId() {
        return selectedVariant != null ? selectedVariant.getId() : -1;
    }
    
    public int getStock() {
        return selectedVariant != null ? selectedVariant.getStock() : product.getCurrentStock();
    }
}

