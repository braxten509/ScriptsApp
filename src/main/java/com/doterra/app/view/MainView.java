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
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.beans.property.IntegerProperty;
import javafx.stage.Stage;

public class MainView {
    
    private final BorderPane root;
    private final VBox sidebar;
    private final NavigationController navigationController;
    private final CssInspector cssInspector;
    
    // Pre-initialized lightweight panels
    private final ChatScriptsPanel chatScriptsPanel;
    private final EmailScriptsPanel emailScriptsPanel;
    private final CalculatorPanel calculatorPanel;
    
    // Track active button
    private Button activeNavButton;
    
    public MainView() {
        root = new BorderPane();
        
        // Initialize CSS inspector
        cssInspector = new CssInspector();
        
        // Initialize only lightweight panels immediately (no file I/O)
        chatScriptsPanel = new ChatScriptsPanel();
        emailScriptsPanel = new EmailScriptsPanel();
        calculatorPanel = new CalculatorPanel();
        
        // Set up navigation controller with lazy loading for heavy panels
        navigationController = new NavigationController(root, chatScriptsPanel, emailScriptsPanel, calculatorPanel);
        
        // Create sidebar
        sidebar = createSidebar();
        root.setLeft(sidebar);
        
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
        chatScriptsBtn.setOnAction(e -> {
            setActiveButton(chatScriptsBtn);
            navigationController.showPanel("chat");
        });
        
        Button emailScriptsBtn = createNavButton("Email Scripts");
        emailScriptsBtn.setOnAction(e -> {
            setActiveButton(emailScriptsBtn);
            navigationController.showPanel("email");
        });
        
        Button regexEditorBtn = createNavButton("Regex Editor");
        regexEditorBtn.setOnAction(e -> {
            setActiveButton(regexEditorBtn);
            navigationController.showPanel("regex");
        });
        
        Button calculatorBtn = createNavButton("Calculator");
        calculatorBtn.setOnAction(e -> {
            setActiveButton(calculatorBtn);
            navigationController.showPanel("calculator");
        });
        
        // Create todo button with lazy badge initialization
        Button todoBtn = createNavButton("Todo");
        todoBtn.setOnAction(e -> {
            setActiveButton(todoBtn);
            navigationController.showPanel("todo");
        });
        
        Button stickyNoteBtn = createNavButton("Sticky Notes");
        stickyNoteBtn.setOnAction(e -> {
            setActiveButton(stickyNoteBtn);
            navigationController.showPanel("stickynote");
        });
        
        Button calendarBtn = createNavButton("Calendar");
        calendarBtn.setOnAction(e -> {
            setActiveButton(calendarBtn);
            navigationController.showPanel("calendar");
        });
        
        Button imageNotesBtn = createNavButton("Image Notes");
        imageNotesBtn.setOnAction(e -> {
            setActiveButton(imageNotesBtn);
            navigationController.showPanel("imagenotes");
        });
        
        // Set initial active button
        setActiveButton(chatScriptsBtn);
        
        // Add spacer to push content to top
        VBox spacer = new VBox();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Create feature panel at bottom
        HBox featurePanel = createFeaturePanel();
        
        sidebar.getChildren().addAll(titleLabel, chatScriptsBtn, emailScriptsBtn, regexEditorBtn, calculatorBtn, todoBtn, stickyNoteBtn, calendarBtn, imageNotesBtn, spacer, featurePanel);
        return sidebar;
    }
    
    private Button createNavButton(String text) {
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(40);
        SimpleStyler.styleNavigationButton(button);
        
        // Add hover effect that respects active state
        button.setOnMouseEntered(e -> {
            if (button != activeNavButton) {
                button.setStyle(SimpleStyler.NAV_BUTTON_HOVER_STYLE);
            }
        });
        button.setOnMouseExited(e -> {
            if (button != activeNavButton) {
                button.setStyle(SimpleStyler.NAV_BUTTON_STYLE);
            }
        });
            
        return button;
    }
    
    private StackPane createNavButtonWithBadge(String text, IntegerProperty badgeCountProperty) {
        // Create a StackPane to hold both the button and badge
        StackPane buttonContainer = new StackPane();
        
        Button button = new Button(text);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(40);
        SimpleStyler.styleNavigationButton(button);
        
        // Add hover effect that respects active state
        button.setOnMouseEntered(e -> {
            if (button != activeNavButton) {
                button.setStyle(SimpleStyler.NAV_BUTTON_HOVER_STYLE);
            }
        });
        button.setOnMouseExited(e -> {
            if (button != activeNavButton) {
                button.setStyle(SimpleStyler.NAV_BUTTON_STYLE);
            }
        });
        
        // Create badge label
        Label badge = new Label();
        badge.setStyle(
            "-fx-background-color: #f44336; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 10px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 2 6 2 6; " +
            "-fx-background-radius: 10; " +
            "-fx-min-width: 18; " +
            "-fx-min-height: 18; " +
            "-fx-alignment: center;"
        );
        badge.setVisible(false);
        
        // Position badge in top-right corner
        StackPane.setAlignment(badge, Pos.TOP_RIGHT);
        badge.setTranslateX(-5);
        badge.setTranslateY(4);
        
        // Bind badge text and visibility to the count property
        badgeCountProperty.addListener((obs, oldCount, newCount) -> {
            if (newCount.intValue() > 0) {
                badge.setText(String.valueOf(newCount.intValue()));
                badge.setVisible(true);
            } else {
                badge.setVisible(false);
            }
        });
        
        // Initial setup
        if (badgeCountProperty.get() > 0) {
            badge.setText(String.valueOf(badgeCountProperty.get()));
            badge.setVisible(true);
        }
        
        buttonContainer.getChildren().addAll(button, badge);
        buttonContainer.setMaxWidth(Double.MAX_VALUE);
        
        return buttonContainer;
    }
    
    private void setActiveButton(Button button) {
        // Remove active style from previous button
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().remove("nav-button-active");
            // Reapply normal style
            SimpleStyler.styleNavigationButton(activeNavButton);
        }
        
        // Set new active button
        activeNavButton = button;
        
        // Apply active style
        if (activeNavButton != null) {
            activeNavButton.getStyleClass().add("nav-button-active");
            activeNavButton.setStyle(
                "-fx-background-color: #2196F3; " +  // Bright blue for active state
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +  // Bold text for active
                "-fx-padding: 10 15; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);"  // Subtle shadow
            );
        }
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
        
        // Pin to top toggle button
        ToggleButton pinBtn = new ToggleButton("📌");
        pinBtn.setPrefSize(35, 35);
        SimpleStyler.styleCssInspectorButton(pinBtn);
        pinBtn.setTooltip(new javafx.scene.control.Tooltip("Pin app to stay on top of other windows"));
        
        // Style for selected state
        ComplexStyler.applyCssInspectorToggleEffect(pinBtn);
        
        // Handle pin to top logic
        pinBtn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            Stage stage = (Stage) root.getScene().getWindow();
            if (stage != null) {
                stage.setAlwaysOnTop(isSelected);
            }
        });
        
        panel.getChildren().addAll(cssInspectorBtn, pinBtn);
        return panel;
    }
    
    public BorderPane getRoot() {
        return root;
    }
    
    public NavigationController getNavigationController() {
        return navigationController;
    }
    
    /**
     * Get the TodoPanel (will be created if not already loaded)
     */
    public TodoPanel getTodoPanel() {
        return navigationController.getTodoPanel();
    }
    
    /**
     * Cleanup method to be called when the application is closing.
     */
    public void cleanup() {
        // Shutdown async file operations
        com.doterra.app.util.AsyncFileOperations.shutdown();
        
        // Cleanup panels if they exist
        NavigationController navController = getNavigationController();
        if (navController != null) {
            // These will be null if not loaded yet, which is fine
            // No need to explicitly cleanup lazy-loaded panels
        }
    }
}