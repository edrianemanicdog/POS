package dao.impl;

import dao.SalesDAO;
import app.Sales;
import database.Database;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SalesDAOImpl implements SalesDAO {

    @Override
    public List<Sales> findAll() {
        List<Sales> sales = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return sales;
            ResultSet rs = con.createStatement().executeQuery(
                "SELECT id, saleDate, totalAmount, totalItems, cashierEmail FROM Sales ORDER BY saleDate DESC"
            );
            while (rs.next()) {
                sales.add(mapResultSetToSales(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    @Override
    public Sales findById(int id) {
        try (Connection con = Database.connect()) {
            if (con == null) return null;
            PreparedStatement ps = con.prepareStatement(
                "SELECT id, saleDate, totalAmount, totalItems, cashierEmail FROM Sales WHERE id = ?"
            );
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapResultSetToSales(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<Sales> findByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sales> sales = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return sales;
            PreparedStatement ps = con.prepareStatement(
                "SELECT id, saleDate, totalAmount, totalItems, cashierEmail FROM Sales " +
                "WHERE saleDate >= ? AND saleDate <= ? ORDER BY saleDate DESC"
            );
            ps.setTimestamp(1, Timestamp.valueOf(startDate));
            ps.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sales.add(mapResultSetToSales(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    @Override
    public List<Sales> findByCashier(String cashierEmail) {
        List<Sales> sales = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return sales;
            PreparedStatement ps = con.prepareStatement(
                "SELECT id, saleDate, totalAmount, totalItems, cashierEmail FROM Sales " +
                "WHERE cashierEmail = ? ORDER BY saleDate DESC"
            );
            ps.setString(1, cashierEmail);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                sales.add(mapResultSetToSales(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return sales;
    }

    @Override
    public int insert(Sales sale) {
        try (Connection con = Database.connect()) {
            if (con == null) return -1;
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO Sales(saleDate, totalAmount, totalItems, cashierEmail) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setTimestamp(1, Timestamp.valueOf(sale.getSaleDate()));
            ps.setDouble(2, sale.getTotalAmount());
            ps.setInt(3, sale.getTotalItems());
            ps.setString(4, sale.getCashierEmail());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public double getTotalSalesByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (Connection con = Database.connect()) {
            if (con == null) return 0.0;
            PreparedStatement ps = con.prepareStatement(
                "SELECT SUM(totalAmount) AS total FROM Sales WHERE saleDate >= ? AND saleDate <= ?"
            );
            ps.setTimestamp(1, Timestamp.valueOf(startDate));
            ps.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public int getTotalItemsSoldByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        try (Connection con = Database.connect()) {
            if (con == null) return 0;
            PreparedStatement ps = con.prepareStatement(
                "SELECT SUM(totalItems) AS total FROM Sales WHERE saleDate >= ? AND saleDate <= ?"
            );
            ps.setTimestamp(1, Timestamp.valueOf(startDate));
            ps.setTimestamp(2, Timestamp.valueOf(endDate));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Sales mapResultSetToSales(ResultSet rs) throws SQLException {
        Timestamp timestamp = rs.getTimestamp("saleDate");
        LocalDateTime saleDate = timestamp != null ? timestamp.toLocalDateTime() : LocalDateTime.now();
        return new Sales(
            rs.getInt("id"),
            saleDate,
            rs.getDouble("totalAmount"),
            rs.getInt("totalItems"),
            rs.getString("cashierEmail")
        );
    }
}

