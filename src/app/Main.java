package app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.layout.BorderPane;

public class Main extends Application {

    private static Stage stage; 
    private static BorderPane dashboardRoot; // store dashboard root for center swapping

    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        // Start with Login
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());

        stage.setTitle("POS System");
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }

    /** Replace entire scene root (Login -> Dashboard) */
    public static void setRoot(String fxmlFile) throws Exception {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/" + fxmlFile + ".fxml"));
        Parent root = loader.load();
        stage.getScene().setRoot(root);

        // Store dashboard root if Dashboard loaded
        if (fxmlFile.equals("Dashboard") && root instanceof BorderPane) {
            dashboardRoot = (BorderPane) root;
        }
    }

    /** Replace only center of dashboard */
    public static void setDashboardCenter(String fxmlFile) throws Exception {
    if (dashboardRoot == null) return;

    FXMLLoader loader = new FXMLLoader(Main.class.getResource("/fxml/" + fxmlFile + ".fxml"));
    Parent newContent = loader.load();

    BorderPane bp = (BorderPane) dashboardRoot; // cast to BorderPane
    bp.setCenter(newContent);
}


    public static Stage getStage() {
        return stage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
