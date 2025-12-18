package controller;

import app.Product;
import app.ProductVariant;
import app.ProductModifier;
import app.ProductModifierOption;
import app.ProductBundle;
import dao.ProductVariantDAO;
import dao.ProductModifierDAO;
import dao.ProductBundleDAO;
import dao.ProductModifierOptionDAO;
import dao.impl.ProductVariantDAOImpl;
import dao.impl.ProductModifierDAOImpl;
import dao.impl.ProductBundleDAOImpl;
import dao.impl.ProductModifierOptionDAOImpl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;

import java.util.ArrayList;
import java.util.List;

public class ProductSelectionDialog extends Dialog<ProductSelectionDialog.ProductSelectionResult> {
    
    private Product product;
    private ProductVariantDAO variantDAO;
    private ProductModifierDAO modifierDAO;
    private ProductBundleDAO bundleDAO;
    private ProductModifierOptionDAO modifierOptionDAO;
    
    private ComboBox<ProductVariant> variantComboBox;
    private ListView<ProductModifierSelection> modifierListView;
    private ComboBox<ProductBundle> bundleComboBox;
    private TextField quantityField;
    
    private List<ProductVariant> variants;
    private List<ProductModifier> modifiers;
    private List<ProductBundle> bundles;
    
    public ProductSelectionDialog(Product product) {
        this.product = product;
        this.variantDAO = new ProductVariantDAOImpl();
        this.modifierDAO = new ProductModifierDAOImpl();
        this.bundleDAO = new ProductBundleDAOImpl();
        this.modifierOptionDAO = new ProductModifierOptionDAOImpl();
        
        setTitle("Select Product Options");
        setHeaderText("Configure: " + product.getName());
        
        // Set dialog modality
        initModality(Modality.APPLICATION_MODAL);
        
        // Create dialog pane
        DialogPane dialogPane = getDialogPane();
        dialogPane.setContent(createContent());
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        // Load data
        loadVariants();
        loadModifiers();
        loadBundles();
        
        // Set default quantity
        quantityField.setText("1");
        
        // Validate before closing
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        okButton.setOnAction(e -> {
            if (validateInput()) {
                setResult(createResult());
            } else {
                e.consume();
            }
        });
    }
    
    private VBox createContent() {
        VBox content = new VBox(12);
        content.setPadding(new Insets(20));
        content.setPrefWidth(520);
        
        // Header: product name only (simple)
        Label titleLabel = new Label(product.getName());
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Main form grid (quantity, variant, type, bundle)
        GridPane formGrid = new GridPane();
        formGrid.setHgap(8);
        formGrid.setVgap(8);

        // Quantity
        Label qtyLabel = new Label("Quantity:");
        quantityField = new TextField("1");
        quantityField.setPrefWidth(70);
        quantityField.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        formGrid.add(qtyLabel, 0, 0);
        formGrid.add(quantityField, 1, 0);

        // Variant (if available)
        Label variantLabel = new Label("Variant:");
        variantComboBox = new ComboBox<>();
        variantComboBox.setPrefWidth(220);
        formGrid.add(variantLabel, 0, 1);
        formGrid.add(variantComboBox, 1, 1, 2, 1);

        // Bundle (optional)
        Label bundleLabel = new Label("Bundle:");
        bundleComboBox = new ComboBox<>();
        bundleComboBox.setPrefWidth(220);
        formGrid.add(bundleLabel, 0, 2);
        formGrid.add(bundleComboBox, 1, 2, 2, 1);
        
        // Modifiers section
        VBox modifierBox = new VBox(6);
        Label modifiersLabel = new Label("Modifiers");
        modifiersLabel.setStyle("-fx-font-weight: bold;");
        modifierListView = new ListView<>();
        modifierListView.setPrefHeight(180);
        modifierBox.getChildren().addAll(modifiersLabel, modifierListView);
        
        content.getChildren().addAll(
            titleLabel,
            new Separator(),
            formGrid,
            new Separator(),
            modifierBox
        );
        
        return content;
    }
    
    private void loadVariants() {
        variants = variantDAO.findByProductId(product.getId());
        ObservableList<ProductVariant> variantList = FXCollections.observableArrayList();

        // If there are no variants, clearly indicate that in the UI
        if (variants == null || variants.isEmpty()) {
            variantComboBox.getItems().clear();
            variantComboBox.setDisable(true);
            variantComboBox.setPromptText("No variants available");
        } else {
            // When variants exist, populate list (no null option so selection is required)
        variantList.addAll(variants);
        variantComboBox.setItems(variantList);
            variantComboBox.setDisable(false);
            variantComboBox.setPromptText("Select variant");
        }

        // Nice display text for variants: name + attributes + price
        variantComboBox.setCellFactory(cb -> new ListCell<ProductVariant>() {
            @Override
            protected void updateItem(ProductVariant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    StringBuilder text = new StringBuilder(item.getName() != null ? item.getName() : "");
                    if (item.getAttributes() != null && !item.getAttributes().isEmpty()) {
                        text.append("  (").append(item.getAttributes()).append(")");
                    }
                    text.append("  -  ₱").append(String.format("%.2f", item.getPrice()));
                    setText(text.toString());
                }
            }
        });
        variantComboBox.setButtonCell(new ListCell<ProductVariant>() {
            @Override
            protected void updateItem(ProductVariant item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    StringBuilder text = new StringBuilder(item.getName() != null ? item.getName() : "");
                    if (item.getAttributes() != null && !item.getAttributes().isEmpty()) {
                        text.append("  (").append(item.getAttributes()).append(")");
                    }
                    text.append("  -  ₱").append(String.format("%.2f", item.getPrice()));
                    setText(text.toString());
                }
            }
        });
    }
    
    private void loadModifiers() {
        modifiers = modifierDAO.findByProductId(product.getId());
        ObservableList<ProductModifierSelection> modifierSelections = FXCollections.observableArrayList();
        
        if (modifiers == null || modifiers.isEmpty()) {
            // No modifiers available - disable list and show friendly message
            modifierListView.setItems(FXCollections.observableArrayList());
            modifierListView.setDisable(true);
            modifierListView.setPlaceholder(new Label("No modifiers available for this product."));
        } else {
        for (ProductModifier modifier : modifiers) {
            List<ProductModifierOption> options = modifierOptionDAO.findByModifierId(modifier.getId());
            modifier.setOptions(options);
            modifierSelections.add(new ProductModifierSelection(modifier));
        }
        
            modifierListView.setDisable(false);
        modifierListView.setItems(modifierSelections);
        modifierListView.setCellFactory(list -> new ModifierListCell());
        }
    }
    
    private void loadBundles() {
        bundles = bundleDAO.findByBundleProductId(product.getId());
        ObservableList<ProductBundle> bundleList = FXCollections.observableArrayList();

        if (bundles == null || bundles.isEmpty()) {
            // No bundles available - keep it simple
            bundleComboBox.getItems().clear();
            bundleComboBox.setDisable(true);
            bundleComboBox.setPromptText("No bundles available");
        } else {
            bundleList.add(null); // "no bundle" option
        bundleList.addAll(bundles);
        bundleComboBox.setItems(bundleList);
            bundleComboBox.setDisable(false);
            bundleComboBox.setPromptText("Select bundle (optional)");

            // Show a readable name instead of app.ProductBundle@...
            bundleComboBox.setCellFactory(cb -> new ListCell<ProductBundle>() {
                @Override
                protected void updateItem(ProductBundle item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("No bundle");
                    } else {
                        String name = item.getItemProductName() != null
                                ? item.getItemProductName()
                                : "Item #" + item.getItemProductId();
                        setText(item.getQuantity() + " × " + name);
                    }
                }
            });
            bundleComboBox.setButtonCell(new ListCell<ProductBundle>() {
                @Override
                protected void updateItem(ProductBundle item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select bundle (optional)");
                    } else {
                        String name = item.getItemProductName() != null
                                ? item.getItemProductName()
                                : "Item #" + item.getItemProductId();
                        setText(item.getQuantity() + " × " + name);
                    }
                }
            });
        }
    }
    
    private boolean validateInput() {
        // Validate quantity
        try {
            int qty = Integer.parseInt(quantityField.getText().trim());
            if (qty <= 0) {
                showAlert("Quantity must be greater than 0!");
                return false;
            }
            
            // Check stock
            ProductVariant selectedVariant = variantComboBox.getValue();
            int availableStock = selectedVariant != null ? selectedVariant.getStock() : product.getCurrentStock();
            if (qty > availableStock) {
                showAlert("Insufficient stock! Available: " + availableStock);
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Please enter a valid quantity!");
            return false;
        }
        
        // Validate required modifiers
        for (ProductModifierSelection modSel : modifierListView.getItems()) {
            if (modSel.getModifier().isRequired() && modSel.getSelectedOptions().isEmpty()) {
                showAlert("Please select options for required modifier: " + modSel.getModifier().getName());
                return false;
            }
        }

        // If product has variants, a variant must be selected
        if (variants != null && !variants.isEmpty()) {
            ProductVariant selectedVariant = variantComboBox.getValue();
            if (selectedVariant == null) {
                showAlert("Please select a variant for this product.");
                return false;
            }
        }
        
        return true;
    }
    
    private ProductSelectionResult createResult() {
        int quantity = Integer.parseInt(quantityField.getText().trim());
        ProductVariant selectedVariant = variantComboBox.getValue();
        
        List<ProductModifierOption> selectedModifiers = new ArrayList<>();
        for (ProductModifierSelection modSel : modifierListView.getItems()) {
            selectedModifiers.addAll(modSel.getSelectedOptions());
        }
        
        ProductBundle selectedBundle = bundleComboBox.getValue();
        
        return new ProductSelectionResult(selectedVariant, selectedModifiers, selectedBundle, quantity);
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Inner class for modifier selection
    private static class ProductModifierSelection {
        private ProductModifier modifier;
        private List<ProductModifierOption> selectedOptions;
        
        public ProductModifierSelection(ProductModifier modifier) {
            this.modifier = modifier;
            this.selectedOptions = new ArrayList<>();
        }
        
        public ProductModifier getModifier() {
            return modifier;
        }
        
        public List<ProductModifierOption> getSelectedOptions() {
            return selectedOptions;
        }
        
        public void setSelectedOptions(List<ProductModifierOption> options) {
            this.selectedOptions = options;
        }
    }
    
    // Custom list cell for modifiers
    private class ModifierListCell extends ListCell<ProductModifierSelection> {
        @Override
        protected void updateItem(ProductModifierSelection item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setGraphic(null);
            } else {
                VBox box = new VBox(5);
                Label nameLabel = new Label(item.getModifier().getName() + 
                    (item.getModifier().isRequired() ? " *" : ""));
                nameLabel.setStyle("-fx-font-weight: bold;");
                
                // Create checkboxes/radio buttons for options
                VBox optionsBox = new VBox(3);
                ToggleGroup group = null;
                
                if (item.getModifier().getType().equals("single")) {
                    group = new ToggleGroup();
                }
                
                for (ProductModifierOption option : item.getModifier().getOptions()) {
                    if (item.getModifier().getType().equals("single")) {
                        // Radio button for single selection
                        RadioButton radio = new RadioButton(option.getName() + " (+₱" + 
                            String.format("%.2f", option.getPrice()) + ")");
                        radio.setToggleGroup(group);
                        radio.setUserData(option);
                        
                        radio.setOnAction(e -> {
                            item.getSelectedOptions().clear();
                            if (radio.isSelected()) {
                                item.getSelectedOptions().add(option);
                            }
                        });
                        
                        optionsBox.getChildren().add(radio);
                    } else {
                        // Checkbox for multiple selection
                        CheckBox checkBox = new CheckBox(option.getName() + " (+₱" + 
                            String.format("%.2f", option.getPrice()) + ")");
                        checkBox.setUserData(option);
                        
                        checkBox.setOnAction(e -> {
                            if (checkBox.isSelected()) {
                                item.getSelectedOptions().add(option);
                            } else {
                                item.getSelectedOptions().remove(option);
                            }
                        });
                        
                        optionsBox.getChildren().add(checkBox);
                    }
                }
                
                box.getChildren().addAll(nameLabel, optionsBox);
                box.setPadding(new Insets(5));
                setGraphic(box);
            }
        }
    }
    
    // Result class - static inner class
    public static class ProductSelectionResult {
        private ProductVariant variant;
        private List<ProductModifierOption> modifiers;
        private ProductBundle bundle;
        private int quantity;
        
        public ProductSelectionResult(ProductVariant variant, List<ProductModifierOption> modifiers, 
                                     ProductBundle bundle, int quantity) {
            this.variant = variant;
            this.modifiers = modifiers;
            this.bundle = bundle;
            this.quantity = quantity;
        }
        
        public ProductVariant getVariant() { return variant; }
        public List<ProductModifierOption> getModifiers() { return modifiers; }
        public ProductBundle getBundle() { return bundle; }
        public int getQuantity() { return quantity; }
    }
}

