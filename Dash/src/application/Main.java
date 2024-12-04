package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;

public class Main extends Application {
    private static final String LOCK_FILE = System.getProperty("user.home") + "/.dashboard.lock";

    @Override
    public void start(Stage primaryStage) {
        if (!acquireLock()) {
            System.out.println("Another instance is already running. Exiting...");
            System.exit(0); // Prevent further execution
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("MainScene.fxml"));
            Parent root = loader.load();
            
            primaryStage.setTitle("Farm Management Dashboard");

           
            Scene scene = new Scene(root, 1200, 800); 
            scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
            primaryStage.setScene(scene);

            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);
            primaryStage.setMaxWidth(1600);
            primaryStage.setMaxHeight(1000);

            primaryStage.setOnCloseRequest(event -> releaseLock()); // Clean up on close

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            releaseLock(); // Clean up in case of errors
        }
    }

    private boolean acquireLock() {
        File lockFile = new File(LOCK_FILE);
        if (lockFile.exists()) {
            return false; // Lock file exists, so another instance is running
        }

        try {
            // Create the lock file
            lockFile.createNewFile();
            lockFile.deleteOnExit(); // Ensure it's deleted on app exit
            return true;
        } catch (IOException e) {
            System.err.println("Failed to create lock file: " + e.getMessage());
            return false;
        }
    }

    private void releaseLock() {
        File lockFile = new File(LOCK_FILE);
        if (lockFile.exists()) {
            lockFile.delete(); // Delete the lock file
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}