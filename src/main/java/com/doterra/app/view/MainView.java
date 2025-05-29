package com.doterra.app.view;

import com.doterra.app.controller.NavigationController;
import com.doterra.app.util.CssInspector;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Priority;
import javafx.scene.Scene;

public class MainView {
    
    private final BorderPane root;
    private final VBox sidebar;
    private final NavigationController navigationController;
    private final CssInspector cssInspector;
    
    // Panels
    private final ChatScriptsPanel chatScriptsPanel;
    private final EmailScriptsPanel emailScriptsPanel;
    
    public MainView() {
        root = new BorderPane();
        sidebar = createSidebar();
        root.setLeft(sidebar);
        
        // Initialize CSS inspector
        cssInspector = new CssInspector();
        
        // Initialize panels
        chatScriptsPanel = new ChatScriptsPanel();
        emailScriptsPanel = new EmailScriptsPanel();
        
        // Set up navigation controller
        navigationController = new NavigationController(root, chatScriptsPanel, emailScriptsPanel);
        
        // Set the initial panel (Chat Scripts)
        navigationController.showPanel("chat");
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        sidebar.setStyle("-fx-background-color: #3a3a3a;");
        sidebar.setPrefWidth(200);
        sidebar.setAlignment(Pos.TOP_CENTER);
        
        // App title/logo
        Label titleLabel = new Label("doTERRA App");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
        titleLabel.setPadding(new Insets(0, 0, 20, 0));
        
        // Navigation buttons
        Button chatScriptsBtn = createNavButton("Chat Scripts");
        chatScriptsBtn.setOnAction(e -> navigationController.showPanel("chat"));
        
        Button emailScriptsBtn = createNavButton("Email Scripts");
        emailScriptsBtn.setOnAction(e -> navigationController.showPanel("email"));
        
        // Add spacer to push content to top
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Create feature panel at bottom
        HBox featurePanel = createFeaturePanel();
        
        sidebar.getChildren().addAll(titleLabel, chatScriptsBtn, emailScriptsBtn, spacer, featurePanel);
        return sidebar;
    }
    
    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(40);
        button.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-size: 14px;");
        
        // Add hover effect
        button.setOnMouseEntered(e -> 
            button.setStyle("-fx-background-color: #777777; -fx-text-fill: white; -fx-font-size: 14px;"));
        button.setOnMouseExited(e -> 
            button.setStyle("-fx-background-color: #555555; -fx-text-fill: white; -fx-font-size: 14px;"));
            
        return button;
    }
    
    private HBox createFeaturePanel() {
        HBox panel = new HBox(5);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(10, 0, 10, 0));
        
        // CSS Inspector toggle button
        ToggleButton cssInspectorBtn = new ToggleButton("CSS");
        cssInspectorBtn.setPrefSize(35, 35);
        cssInspectorBtn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; -fx-font-size: 10px;");
        cssInspectorBtn.setTooltip(new javafx.scene.control.Tooltip("Toggle CSS Inspector - Shows CSS classes on hover"));
        
        // Style for selected state
        cssInspectorBtn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                cssInspectorBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px;");
                // Enable CSS inspector when scene is available
                if (root.getScene() != null) {
                    cssInspector.enable(root.getScene());
                } else {
                    // Wait for scene to be available
                    root.sceneProperty().addListener((sceneObs, oldScene, newScene) -> {
                        if (newScene != null && cssInspectorBtn.isSelected()) {
                            cssInspector.enable(newScene);
                        }
                    });
                }
            } else {
                cssInspectorBtn.setStyle("-fx-background-color: #444444; -fx-text-fill: white; -fx-font-size: 10px;");
                cssInspector.disable();
            }
        });
        
        panel.getChildren().add(cssInspectorBtn);
        return panel;
    }
    
    public BorderPane getRoot() {
        return root;
    }
}