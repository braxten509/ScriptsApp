package com.doterra.app;

import com.doterra.app.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.awt.*;
import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;

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
        
        // Set up taskbar badge based on ready todo count
        setupTaskbarBadge(primaryStage, mainView);
        
        primaryStage.show();
    }
    
    /**
     * Set up taskbar badge to show ready todo count in window title and icon
     */
    private void setupTaskbarBadge(Stage primaryStage, MainView mainView) {
        mainView.getTodoPanel().readyTodoCountProperty().addListener((obs, oldCount, newCount) -> {
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
        });
        
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
     * Create the default application icon
     */
    private Image createDefaultIcon() {
        BufferedImage bufferedImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a simple icon (green circle with "dT" text)
        g2d.setColor(new Color(76, 175, 80)); // Material Green
        g2d.fillOval(4, 4, 56, 56);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "dT";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2d.drawString(text, (64 - textWidth) / 2, (64 + textHeight) / 2 - 2);
        
        g2d.dispose();
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }
    
    /**
     * Create an application icon with a badge showing the count
     */
    private Image createBadgedIcon(int count) {
        BufferedImage bufferedImage = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        // Draw the base icon (green circle with "dT" text) - KEEP FULL SIZE like default icon
        g2d.setColor(new Color(76, 175, 80)); // Material Green
        g2d.fillOval(4, 4, 56, 56);
        
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 22));
        FontMetrics fm = g2d.getFontMetrics();
        String text = "dT";
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getAscent();
        g2d.drawString(text, (64 - textWidth) / 2, (64 + textHeight) / 2 - 2);
        
        // Draw an ENORMOUS badge overlapping the icon - positioned to not obscure the dT text
        String badgeText = String.valueOf(Math.min(count, 99)); // Limit to 99
        g2d.setColor(new Color(244, 67, 54)); // Material Red
        
        // Make badge ENORMOUS but ensure it doesn't get cut off - 28px for single digits, 36px for double digits
        int badgeSize = badgeText.length() == 1 ? 28 : 36;
        int badgeX = 64 - badgeSize - 1; // Keep within bounds but still prominent
        int badgeY = 1; // Keep within bounds
        
        // Draw badge background
        g2d.fillOval(badgeX, badgeY, badgeSize, badgeSize);
        
        // Add white border around badge for contrast
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(3));
        g2d.drawOval(badgeX, badgeY, badgeSize, badgeSize);
        
        // Draw badge text - MASSIVE font
        g2d.setColor(Color.WHITE);
        int fontSize = badgeText.length() == 1 ? 20 : 16;
        g2d.setFont(new Font("Arial", Font.BOLD, fontSize));
        FontMetrics badgeFm = g2d.getFontMetrics();
        int badgeTextWidth = badgeFm.stringWidth(badgeText);
        int badgeTextHeight = badgeFm.getAscent();
        int textX = badgeX + (badgeSize - badgeTextWidth) / 2;
        int textY = badgeY + (badgeSize + badgeTextHeight) / 2 - 2;
        g2d.drawString(badgeText, textX, textY);
        
        g2d.dispose();
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    public static void main(String[] args) {
        launch(args);
    }
}