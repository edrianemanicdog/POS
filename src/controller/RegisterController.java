package controller;

import app.Main;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import javafx.application.Platform;
import javafx.scene.layout.VBox;

public class RegisterController {

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
    private PasswordField confirmPasswordField; // <-- new field

    // Go back to Login Page
    @FXML
    private void goToLogin() {
        try {
            Main.setRoot("Login"); // Loads /fxml/Login.fxml
            System.out.println("Switched to Login scene");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to load Login.fxml");
        }
    }

    // Register user to database
@FXML
public void registerUser() {
    String email = emailField.getText();
    String password = passwordField.getText();
    String confirmPassword = confirmPasswordField.getText(); // get confirm password

    if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
        showAlert(Alert.AlertType.ERROR, "Error", "Please fill all fields!");
        return;
    }

    // Email format validation
    if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
        showAlert(Alert.AlertType.ERROR, "Error", "Please enter a valid email address!");
        return;
    }

    if (!password.equals(confirmPassword)) {
        showAlert(Alert.AlertType.ERROR, "Error", "Passwords do not match!");
        return;
    }

    try (Connection conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/posdb", "root", "")) {

        String query = "INSERT INTO user (email, password, role) VALUES (?, ?, 'cashier')";
        PreparedStatement stmt = conn.prepareStatement(query);
        stmt.setString(1, email);
        stmt.setString(2, password);
        stmt.executeUpdate();

        showAlert(Alert.AlertType.INFORMATION, "Success", "User Registered Successfully!");

        // After registration, go to login screen
        Main.setRoot("Login");

    } catch (Exception e) {
        e.printStackTrace();
        showAlert(Alert.AlertType.ERROR, "Database Error", "Registration failed!");
    }
}


    // Alert helper method
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
