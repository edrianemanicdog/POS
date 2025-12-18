package controller;

import app.Main;
import app.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.sql.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class LoginController {
    @FXML
private VBox mainVBox;

@FXML
public void initialize() {
    // Delay focus until after scene is ready
    Platform.runLater(() -> mainVBox.requestFocus());
}


    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

@FXML
public void loginUser() {
    String email = emailField.getText();
    String password = passwordField.getText();

    if (email.isEmpty() || password.isEmpty()) {
        showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields!");
        return;
    }

    // Hardcoded super admin credentials
    if ("admin@admin.com".equals(email) && "admin123".equals(password)) {
        UserSession.setUser(email, "superadmin");
        try {
            Main.setRoot("Dashboard");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load Dashboard!");
        }
        return;
    }

    try (Connection conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/posdb", "root", "")) {

        String query = "SELECT * FROM user WHERE email = ? AND password = ?";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, email);
        stmt.setString(2, password);

        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            String username = rs.getString("email"); // or "name" column
            String role = rs.getString("role");
            
            // Set user session
            UserSession.setUser(username, role);

            // Route based on role
            if ("cashier".equals(role)) {
                Main.setRoot("CashierDashboard");
            } else if ("admin".equals(role) || "superadmin".equals(role)) {
                Main.setRoot("Dashboard");
            } else {
                // Default fallback
                Main.setRoot("CashierDashboard");
            }
        } else {
            // Wrong credentials
            showAlert(Alert.AlertType.ERROR, "Login Failed", "Incorrect email or password!");
        }

    } catch (SQLException e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Database Error", "Could not connect to database!");
    } catch (Exception e) {
        e.printStackTrace();
    }
}


    @FXML
    private void goToRegister() {
        try {
            Main.setRoot("Register");
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load Register page!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
