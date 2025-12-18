package dao.impl;

import dao.SupplierDAO;
import app.Supplier;
import database.Database;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SupplierDAOImpl implements SupplierDAO {

    @Override
    public List<Supplier> findAll() {
        List<Supplier> suppliers = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return suppliers;
            ResultSet rs = con.createStatement().executeQuery("SELECT id, name, email, phone, address FROM Supplier");
            while (rs.next()) {
                suppliers.add(new Supplier(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return suppliers;
    }

    @Override
    public Supplier findById(int id) {
        try (Connection con = Database.connect()) {
            if (con == null) return null;
            PreparedStatement ps = con.prepareStatement("SELECT id, name, email, phone, address FROM Supplier WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Supplier(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Supplier findByName(String name) {
        try (Connection con = Database.connect()) {
            if (con == null) return null;
            PreparedStatement ps = con.prepareStatement("SELECT id, name, email, phone, address FROM Supplier WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Supplier(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("address")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public int insert(Supplier supplier) {
        try (Connection con = Database.connect()) {
            if (con == null) return -1;
            PreparedStatement ps = con.prepareStatement("INSERT INTO Supplier(name, email, phone, address) VALUES (?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getEmail());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getAddress());
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
    public boolean update(Supplier supplier) {
        try (Connection con = Database.connect()) {
            if (con == null) return false;
            PreparedStatement ps = con.prepareStatement("UPDATE Supplier SET name=?, email=?, phone=?, address=? WHERE id=?");
            ps.setString(1, supplier.getName());
            ps.setString(2, supplier.getEmail());
            ps.setString(3, supplier.getPhone());
            ps.setString(4, supplier.getAddress());
            ps.setInt(5, supplier.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        try (Connection con = Database.connect()) {
            if (con == null) return false;
            PreparedStatement ps = con.prepareStatement("DELETE FROM Supplier WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public List<String> getAllSupplierNames() {
        List<String> names = new ArrayList<>();
        try (Connection con = Database.connect()) {
            if (con == null) return names;
            ResultSet rs = con.createStatement().executeQuery("SELECT name FROM Supplier");
            while (rs.next()) {
                names.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return names;
    }
}

