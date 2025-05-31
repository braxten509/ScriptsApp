package com.doterra.app.view;

import com.doterra.app.controller.NavigationController;
import com.doterra.app.util.CssInspector;
import com.doterra.app.util.SimpleStyler;
import com.doterra.app.util.ComplexStyler;
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
    private final RegexEditorPanel regexEditorPanel;
    private final CalculatorPanel calculatorPanel;
    
    public MainView() {
        root = new BorderPane();
        sidebar = createSidebar();
        root.setLeft(sidebar);
        
        // Initialize CSS inspector
        cssInspector = new CssInspector();
        
        // Initialize panels
        chatScriptsPanel = new ChatScriptsPanel();
        emailScriptsPanel = new EmailScriptsPanel();
        regexEditorPanel = new RegexEditorPanel();
        calculatorPanel = new CalculatorPanel();
        
        // Set up navigation controller
        navigationController = new NavigationController(root, chatScriptsPanel, emailScriptsPanel, regexEditorPanel, calculatorPanel);
        
        // Set the initial panel (Chat Scripts)
        navigationController.showPanel("chat");
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(10);
        sidebar.setPadding(new Insets(15));
        SimpleStyler.styleSidebar(sidebar);
        sidebar.setPrefWidth(200);
        sidebar.setAlignment(Pos.TOP_CENTER);
        
        // App title/logo
        Label titleLabel = new Label("doTERRA App");
        SimpleStyler.styleTitleLabel(titleLabel);
        titleLabel.setPadding(new Insets(0, 0, 20, 0));
        
        // Navigation buttons
        Button chatScriptsBtn = createNavButton("Chat Scripts");
        chatScriptsBtn.setOnAction(e -> navigationController.showPanel("chat"));
        
        Button emailScriptsBtn = createNavButton("Email Scripts");
        emailScriptsBtn.setOnAction(e -> navigationController.showPanel("email"));
        
        Button regexEditorBtn = createNavButton("Regex Editor");
        regexEditorBtn.setOnAction(e -> navigationController.showPanel("regex"));
        
        Button calculatorBtn = createNavButton("Calculator");
        calculatorBtn.setOnAction(e -> navigationController.showPanel("calculator"));
        
        // Add spacer to push content to top
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Create feature panel at bottom
        HBox featurePanel = createFeaturePanel();
        
        sidebar.getChildren().addAll(titleLabel, chatScriptsBtn, emailScriptsBtn, regexEditorBtn, calculatorBtn, spacer, featurePanel);
        return sidebar;
    }
    
    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(40);
        SimpleStyler.styleNavigationButton(button);
        
        // Add hover effect
        ComplexStyler.applyNavigationButtonHoverEffect(button);
            
        return button;
    }
    
    private HBox createFeaturePanel() {
        HBox panel = new HBox(5);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(10, 0, 10, 0));
        
        // CSS Inspector toggle button
        ToggleButton cssInspectorBtn = new ToggleButton("CSS");
        cssInspectorBtn.setPrefSize(35, 35);
        SimpleStyler.styleCssInspectorButton(cssInspectorBtn);
        cssInspectorBtn.setTooltip(new javafx.scene.control.Tooltip("Toggle CSS Inspector - Shows CSS classes on hover"));
        
        // Style for selected state
        ComplexStyler.applyCssInspectorToggleEffect(cssInspectorBtn);
        
        // Handle CSS inspector enable/disable logic
        cssInspectorBtn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
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