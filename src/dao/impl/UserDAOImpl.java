package dao.impl;

import dao.UserDAO;
import database.Database;
import java.security.MessageDigest;
import java.sql.*;
import java.util.Optional;

public class UserDAOImpl implements UserDAO {

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean createUser(String email, String password) {
        try (Connection con = Database.connect()) {
            if (con == null) return false;
            String hashedPassword = hashPassword(password);
            PreparedStatement ps = con.prepareStatement("INSERT INTO User(email, password) VALUES (?, ?)");
            ps.setString(1, email);
            ps.setString(2, hashedPassword);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean authenticate(String email, String password) {
        try (Connection con = Database.connect()) {
            if (con == null) return false;
            String hashedPassword = hashPassword(password);
            PreparedStatement ps = con.prepareStatement("SELECT * FROM User WHERE email=? AND password=?");
            ps.setString(1, email);
            ps.setString(2, hashedPassword);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean userExists(String email) {
        try (Connection con = Database.connect()) {
            if (con == null) return false;
            PreparedStatement ps = con.prepareStatement("SELECT * FROM User WHERE email=?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Optional<String> getUserEmail(String email) {
        try (Connection con = Database.connect()) {
            if (con == null) return Optional.empty();
            PreparedStatement ps = con.prepareStatement("SELECT email FROM User WHERE email=?");
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(rs.getString("email"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }
}

