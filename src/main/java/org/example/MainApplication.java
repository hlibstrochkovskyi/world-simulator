package org.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Main application class for the WorldSimulator.
 * Initializes the JavaFX application and displays the main window.
 */
public class MainApplication extends Application {

    /**
     * The main entry point for all JavaFX applications.
     *
     * @param primaryStage The primary stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {
        // BorderPane is a simple layout container
        BorderPane root = new BorderPane();

        // Create the main scene (the content of the window)
        Scene scene = new Scene(root, 1024, 768); // Window size 1024x768

        // Configure the Stage (the window)
        primaryStage.setTitle("WorldSimulator"); // Window title
        primaryStage.setScene(scene); // Set the scene to the stage
        primaryStage.show(); // Display the window
    }

    /**
     * Main method to launch the application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        // This method launches the JavaFX application
        launch(args);
    }
}

