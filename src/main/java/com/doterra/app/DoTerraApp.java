package com.doterra.app;

import com.doterra.app.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DoTerraApp extends Application {

    private static final String APP_TITLE = "doTERRA App 2.0";
    private static final double WINDOW_WIDTH = 1200;
    private static final double WINDOW_HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {
        // Create main view
        MainView mainView = new MainView();
        
        // Set up the scene
        Scene scene = new Scene(mainView.getRoot(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        
        // Configure stage
        primaryStage.setTitle(APP_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}