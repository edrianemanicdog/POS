package dao;

import app.Sales;
import java.time.LocalDateTime;
import java.util.List;

public interface SalesDAO {
    List<Sales> findAll();
    Sales findById(int id);
    List<Sales> findByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    List<Sales> findByCashier(String cashierEmail);
    int insert(Sales sale);
    double getTotalSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    int getTotalItemsSoldByDateRange(LocalDateTime startDate, LocalDateTime endDate);
}

