package app;

import java.time.LocalDateTime;

public class Sales {
    private int id;
    private LocalDateTime saleDate;
    private double totalAmount;
    private int totalItems;
    private String cashierEmail;
    
    // Constructor
    public Sales(int id, LocalDateTime saleDate, double totalAmount, int totalItems, String cashierEmail) {
        this.id = id;
        this.saleDate = saleDate;
        this.totalAmount = totalAmount;
        this.totalItems = totalItems;
        this.cashierEmail = cashierEmail;
    }
    
    // Default constructor
    public Sales() {}
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public LocalDateTime getSaleDate() {
        return saleDate;
    }
    
    public void setSaleDate(LocalDateTime saleDate) {
        this.saleDate = saleDate;
    }
    
    public double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public int getTotalItems() {
        return totalItems;
    }
    
    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }
    
    public String getCashierEmail() {
        return cashierEmail;
    }
    
    public void setCashierEmail(String cashierEmail) {
        this.cashierEmail = cashierEmail;
    }
}

