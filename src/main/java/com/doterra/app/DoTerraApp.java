package com.doterra.app;

import com.doterra.app.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.awt.*;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.WeakChangeListener;

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
        
        // Set up cleanup when application closes
        primaryStage.setOnCloseRequest(e -> {
            mainView.cleanup();
        });
        
        // Add shutdown hook for abnormal termination
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            // This runs on shutdown, but not on JavaFX thread
            try {
                mainView.cleanup();
            } catch (Exception ex) {
                // Ignore exceptions during shutdown
            }
        }));
        
        // Set up taskbar badge based on ready todo count
        setupTaskbarBadge(primaryStage, mainView);
        
        primaryStage.show();
    }
    
    /**
     * Set up taskbar badge to show ready todo count in window title and icon
     */
    private void setupTaskbarBadge(Stage primaryStage, MainView mainView) {
        // Create the listener for todo count changes
        ChangeListener<Number> todoCountListener = (obs, oldCount, newCount) -> {
            int count = newCount.intValue();
            if (count > 0) {
                primaryStage.setTitle(APP_TITLE + " (" + count + ")");
                primaryStage.getIcons().clear();
                primaryStage.getIcons().add(createBadgedIcon(count));
            } else {
                primaryStage.setTitle(APP_TITLE);
                primaryStage.getIcons().clear();
                primaryStage.getIcons().add(createDefaultIcon());
            }
        };
        
        // Use weak listener to avoid strong reference from TodoPanel to Stage
        mainView.getTodoPanel().readyTodoCountProperty().addListener(
            new WeakChangeListener<>(todoCountListener)
        );
        
        // Initial setup
        primaryStage.getIcons().add(createDefaultIcon());
        int initialCount = mainView.getTodoPanel().getReadyTodoCount();
        if (initialCount > 0) {
            primaryStage.setTitle(APP_TITLE + " (" + initialCount + ")");
            primaryStage.getIcons().clear();
            primaryStage.getIcons().add(createBadgedIcon(initialCount));
        }
    }
    
    /**
     * Create the default application icon (green circle with '0')
     */
    private Image createDefaultIcon() {
        BufferedImage bufferedImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw green circle
        g2d.setColor(new Color(76, 175, 80)); // Material Green
        g2d.fillOval(4, 4, 56, 56);
        
        // Draw white '0' in center
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "0";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2d.drawString(text, (64 - textWidth) / 2, (64 + textHeight) / 2 - 4);
        
        g2d.dispose();
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
    
    /**
     * Create an application icon with red circle showing the count
     */
    private Image createBadgedIcon(int count) {
        BufferedImage bufferedImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw red circle
        g2d.setColor(new Color(244, 67, 54)); // Material Red
        g2d.fillOval(4, 4, 56, 56);
        
        // Draw white count number in center
        g2d.setColor(Color.WHITE);
        String countText = String.valueOf(Math.min(count, 99)); // Limit to 99
        
        // Choose font size based on number of digits
        int fontSize = countText.length() == 1 ? 36 : (countText.length() == 2 ? 28 : 24);
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(countText);
        int textHeight = fm.getAscent();
        g2d.drawString(countText, (64 - textWidth) / 2, (64 + textHeight) / 2 - 4);
        
        g2d.dispose();
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public static void main(String[] args) {
        launch(args);
    }
}