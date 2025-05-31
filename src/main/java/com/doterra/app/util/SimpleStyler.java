package com.doterra.app.util;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;

public class SimpleStyler {
    
    // Navigation and Sidebar Styles
    public static final String SIDEBAR_STYLE = "-fx-background-color: #3a3a3a;";
    public static final String TITLE_LABEL_STYLE = "-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;";
    public static final String NAV_BUTTON_STYLE = "-fx-background-color: #555555; -fx-text-fill: white; -fx-font-size: 14px;";
    public static final String NAV_BUTTON_HOVER_STYLE = "-fx-background-color: #777777; -fx-text-fill: white; -fx-font-size: 14px;";
    
    // CSS Inspector Button Styles
    public static final String CSS_INSPECTOR_NORMAL_STYLE = "-fx-background-color: #444444; -fx-text-fill: white; -fx-font-size: 10px;";
    public static final String CSS_INSPECTOR_ACTIVE_STYLE = "-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 10px;";
    
    // Tab and Button Styles
    public static final String ADD_TAB_BUTTON_STYLE = "-fx-background-color: transparent; -fx-border-color: transparent;";
    
    // Inspector Label Styles
    public static final String INSPECTOR_LABEL_STYLE = "-fx-border-color: rgba(0,0,0,0.2); -fx-border-width: 1px; -fx-border-radius: 4px;";
    
    // Layout Constants
    public static final Insets DEFAULT_PADDING = new Insets(10);
    public static final Insets INSPECTOR_LABEL_PADDING = new Insets(4, 8, 4, 8);
    public static final double BUTTON_GRID_GAP = 5.0;
    public static final double TEXT_AREA_HEIGHT = 150.0;
    public static final double HTML_EDITOR_HEIGHT = 200.0;
    
    // Background Definitions
    public static final Background INSPECTOR_LABEL_BACKGROUND = new Background(
        new BackgroundFill(
            Color.rgb(255, 255, 255, 0.85), 
            new CornerRadii(4), 
            Insets.EMPTY
        )
    );
    
    // Style Application Methods
    public static void styleSidebar(VBox sidebar) {
        sidebar.setStyle(SIDEBAR_STYLE);
    }
    
    public static void styleTitleLabel(Label label) {
        label.setStyle(TITLE_LABEL_STYLE);
    }
    
    public static void styleNavigationButton(Button button) {
        button.setStyle(NAV_BUTTON_STYLE);
    }
    
    public static void styleCssInspectorButton(ToggleButton button) {
        button.setStyle(CSS_INSPECTOR_NORMAL_STYLE);
    }
    
    public static void styleAddTabButton(Button button) {
        button.setStyle(ADD_TAB_BUTTON_STYLE);
    }
    
    public static void styleInspectorLabel(Label label) {
        label.setStyle(INSPECTOR_LABEL_STYLE);
        label.setPadding(INSPECTOR_LABEL_PADDING);
        label.setBackground(INSPECTOR_LABEL_BACKGROUND);
    }
    
    public static void applyDefaultLayout(VBox container) {
        container.setPadding(DEFAULT_PADDING);
    }
    
    public static void applyDefaultLayout(GridPane grid) {
        grid.setPadding(DEFAULT_PADDING);
        grid.setHgap(BUTTON_GRID_GAP);
        grid.setVgap(BUTTON_GRID_GAP);
    }
    
    public static void applyDefaultLayout(BorderPane container) {
        container.setPadding(DEFAULT_PADDING);
    }
    
    public static void setTextAreaHeight(TextArea textArea) {
        textArea.setPrefHeight(TEXT_AREA_HEIGHT);
    }
    
    public static void setHtmlEditorHeight(HTMLEditor htmlEditor) {
        htmlEditor.setPrefHeight(HTML_EDITOR_HEIGHT);
    }
    
    public static void makeButtonFillSpace(Button button) {
        button.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
    }
}