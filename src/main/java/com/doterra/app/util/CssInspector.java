package com.doterra.app.util;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.geometry.Insets;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Popup;
import javafx.scene.Scene;
import java.util.ArrayList;
import java.util.List;

public class CssInspector {
    
    private boolean enabled = false;
    private Popup currentPopup;
    private Scene scene;
    private Node lastTarget;
    
    public CssInspector() {
        currentPopup = new Popup();
    }
    
    public void enable(Scene scene) {
        this.scene = scene;
        this.enabled = true;
        addEventHandlers();
    }
    
    public void disable() {
        this.enabled = false;
        removeEventHandlers();
        hidePopup();
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    private void addEventHandlers() {
        if (scene != null) {
            scene.addEventFilter(MouseEvent.MOUSE_MOVED, this::handleMouseMove);
        }
    }
    
    private void removeEventHandlers() {
        if (scene != null) {
            scene.removeEventFilter(MouseEvent.MOUSE_MOVED, this::handleMouseMove);
        }
    }
    
    private void handleMouseMove(MouseEvent event) {
        if (!enabled) return;
        
        Node target = event.getPickResult().getIntersectedNode();
        
        // Only update if target changed to prevent flashing
        if (target != lastTarget) {
            lastTarget = target;
            if (target != null) {
                showInspectorInfo(target, event.getScreenX(), event.getScreenY());
            }
        } else if (currentPopup.isShowing()) {
            // Update popup position to follow cursor
            currentPopup.setX(event.getScreenX() + 15);
            currentPopup.setY(event.getScreenY() + 15);
        }
    }
    
    
    private void showInspectorInfo(Node node, double x, double y) {
        String info = getNodeInfo(node);
        
        if (!info.isEmpty()) {
            hidePopup();
            
            Label label = new Label(info);
            label.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
            label.setTextFill(Color.BLACK);
            label.setPadding(new Insets(4, 8, 4, 8));
            
            // Add a semi-transparent white background for readability
            label.setBackground(new Background(new BackgroundFill(
                Color.rgb(255, 255, 255, 0.85), 
                new CornerRadii(4), 
                Insets.EMPTY
            )));
            
            // Add a subtle border
            label.setStyle("-fx-border-color: rgba(0,0,0,0.2); -fx-border-width: 1px; -fx-border-radius: 4px;");
            label.setMouseTransparent(true);
            
            currentPopup = new Popup();
            currentPopup.getContent().add(label);
            currentPopup.setAutoHide(false);
            
            // Position slightly offset from cursor to avoid interference
            if (scene != null && scene.getWindow() != null) {
                currentPopup.show(scene.getWindow(), x + 15, y + 15);
            }
        }
    }
    
    private void hidePopup() {
        if (currentPopup != null) {
            currentPopup.hide();
        }
    }
    
    private String getNodeInfo(Node node) {
        StringBuilder info = new StringBuilder();
        
        // Get node type
        String nodeType = node.getClass().getSimpleName();
        
        // Skip certain common wrapper nodes
        if (nodeType.equals("Path") || nodeType.equals("Text") || nodeType.equals("Group")) {
            return "";
        }
        
        info.append(nodeType);
        
        // Get ID if present
        if (node.getId() != null && !node.getId().isEmpty()) {
            info.append("#").append(node.getId());
        }
        
        // Get style classes
        List<String> meaningfulClasses = new ArrayList<>();
        for (String styleClass : node.getStyleClass()) {
            // Filter out default JavaFX classes
            if (!styleClass.equals("root") && !styleClass.equals("text-input") && 
                !styleClass.equals("text-field") && !styleClass.equals("labeled")) {
                meaningfulClasses.add(styleClass);
            }
        }
        
        if (!meaningfulClasses.isEmpty()) {
            info.append(" .");
            info.append(String.join(" .", meaningfulClasses));
        }
        
        // Get inline styles if present (truncate if too long)
        if (node.getStyle() != null && !node.getStyle().isEmpty()) {
            String style = node.getStyle().trim();
            if (style.length() > 50) {
                style = style.substring(0, 47) + "...";
            }
            info.append("\n").append(style);
        }
        
        // Add layout styling unavailable notice for pure layout containers
        if (isPureLayoutContainer(nodeType)) {
            info.append("\n[Layout properties unavailable - use Java code]");
        }
        
        return info.toString();
    }
    
    private boolean isPureLayoutContainer(String nodeType) {
        // These containers have no meaningful CSS styling - only layout behavior
        return nodeType.equals("BorderPane") || 
               nodeType.equals("VBox") || 
               nodeType.equals("HBox") || 
               nodeType.equals("FlowPane") || 
               nodeType.equals("GridPane") || 
               nodeType.equals("StackPane") || 
               nodeType.equals("AnchorPane") || 
               nodeType.equals("TilePane");
        // Note: SplitPane removed as it has CSS-styleable dividers
    }
}