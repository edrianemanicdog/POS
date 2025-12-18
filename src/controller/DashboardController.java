package controller;

import app.Main;
import app.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.net.URL;
import java.util.ResourceBundle;

public class DashboardController implements Initializable {

    @FXML
    private Label welcomeLabel;

    @FXML
    private AnchorPane centerPane; // <<< Add this

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Check if user is authorized (admin or superadmin only)
        String role = UserSession.getCurrentUserRole();
        if (role == null || (!role.equals("admin") && !role.equals("superadmin"))) {
            // Unauthorized access - redirect to login
            try {
                UserSession.clear();
                Main.setRoot("Login");
                showAlert(Alert.AlertType.ERROR, "Access Denied", "You do not have permission to access this page!");
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        // Set user info from session
        String username = UserSession.getCurrentUserEmail();
        if (username != null && role != null) {
            welcomeLabel.setText("Welcome, " + username + " | Role: " + role);
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** Set name and role after login */
    public void setUser(String username, String role) {
        welcomeLabel.setText("Welcome, " + username + " | Role: " + role);
    }

    @FXML
    private void openPOS() {
        try {
            Parent posPane = FXMLLoader.load(getClass().getResource("/fxml/POS.fxml"));

            centerPane.getChildren().clear();
            centerPane.getChildren().add(posPane);

            // Anchor all sides so it fills the centerPane
            AnchorPane.setTopAnchor(posPane, 0.0);
            AnchorPane.setBottomAnchor(posPane, 0.0);
            AnchorPane.setLeftAnchor(posPane, 0.0);
            AnchorPane.setRightAnchor(posPane, 0.0);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load POS");
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = e.getClass().getSimpleName() + ": " + e.toString();
            }
            alert.setContentText("Error: " + errorMsg);
            alert.showAndWait();
        }
    }

    @FXML
    private void openProducts() {
        try {
            Parent productPane = FXMLLoader.load(getClass().getResource("/fxml/Product.fxml"));

            centerPane.getChildren().clear();
            centerPane.getChildren().add(productPane);

            // Anchor all sides so it fills the centerPane
            AnchorPane.setTopAnchor(productPane, 0.0);
            AnchorPane.setBottomAnchor(productPane, 0.0);
            AnchorPane.setLeftAnchor(productPane, 0.0);
            AnchorPane.setRightAnchor(productPane, 0.0);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load Products");
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = e.getClass().getSimpleName() + ": " + e.toString();
            }
            // Show full stack trace in content
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            e.printStackTrace(pw);
            alert.setContentText("Error: " + errorMsg + "\n\nDetails:\n" + sw.toString());
            alert.setResizable(true);
            alert.getDialogPane().setPrefSize(600, 400);
            alert.showAndWait();
        }
    }

    @FXML
    private void openSales() {
        try {
            Parent salesPane = FXMLLoader.load(getClass().getResource("/fxml/Sales.fxml"));

            centerPane.getChildren().clear();
            centerPane.getChildren().add(salesPane);

            // Anchor all sides so it fills the centerPane
            AnchorPane.setTopAnchor(salesPane, 0.0);
            AnchorPane.setBottomAnchor(salesPane, 0.0);
            AnchorPane.setLeftAnchor(salesPane, 0.0);
            AnchorPane.setRightAnchor(salesPane, 0.0);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load Sales");
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = e.getClass().getSimpleName() + ": " + e.toString();
            }
            alert.setContentText("Error: " + errorMsg);
            alert.showAndWait();
        }
    }

    @FXML
    private void openReports() {
        try {
            Parent reportsPane = FXMLLoader.load(getClass().getResource("/fxml/Reports.fxml"));

            centerPane.getChildren().clear();
            centerPane.getChildren().add(reportsPane);

            // Anchor all sides so it fills the centerPane
            AnchorPane.setTopAnchor(reportsPane, 0.0);
            AnchorPane.setBottomAnchor(reportsPane, 0.0);
            AnchorPane.setLeftAnchor(reportsPane, 0.0);
            AnchorPane.setRightAnchor(reportsPane, 0.0);

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load Reports");
            String errorMsg = e.getMessage();
            if (errorMsg == null || errorMsg.isEmpty()) {
                errorMsg = e.getClass().getSimpleName() + ": " + e.toString();
            }
            alert.setContentText("Error: " + errorMsg);
            alert.showAndWait();
        }
    }

    @FXML
    private void logout() {
        UserSession.clear();
        try {
            Main.setRoot("Login");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
