package com.doterra.app.util;

import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import com.doterra.app.model.ScriptButton;

public class ComplexStyler {
    
    // Opacity values for drag operations
    private static final double DRAG_OPACITY = 0.5;
    private static final double NORMAL_OPACITY = 1.0;
    
    // Style classes
    private static final String DRAG_TARGET_CLASS = "drag-target";
    private static final String SCRIPT_BUTTON_CLASS = "script-button";
    private static final String SELECTED_CLASS = "selected";
    
    // Hover Effects
    public static void applyNavigationButtonHoverEffect(Button button) {
        button.setOnMouseEntered(e -> 
            button.setStyle(SimpleStyler.NAV_BUTTON_HOVER_STYLE));
        button.setOnMouseExited(e -> 
            button.setStyle(SimpleStyler.NAV_BUTTON_STYLE));
    }
    
    // Toggle State Styling
    public static void applyCssInspectorToggleEffect(ToggleButton button) {
        button.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                button.setStyle(SimpleStyler.CSS_INSPECTOR_ACTIVE_STYLE);
            } else {
                button.setStyle(SimpleStyler.CSS_INSPECTOR_NORMAL_STYLE);
            }
        });
    }
    
    // Button Color Styling
    public static void applyButtonColor(Button button, ScriptButton scriptButton) {
        if (scriptButton.getColor() != null) {
            String colorStyle = ColorUtil.colorToHex(scriptButton.getColor());
            button.setStyle("-fx-background-color: " + colorStyle + "; -fx-text-fill: white;");
        }
    }
    
    // Drag and Drop Visual Feedback
    public static void applyDragStartVisuals(Button button) {
        button.setOpacity(DRAG_OPACITY);
    }
    
    public static void applyDragEndVisuals(Button button) {
        button.setOpacity(NORMAL_OPACITY);
    }
    
    public static void addDragTargetVisual(Button button) {
        if (!button.getStyleClass().contains(DRAG_TARGET_CLASS)) {
            button.getStyleClass().add(DRAG_TARGET_CLASS);
        }
    }
    
    public static void removeDragTargetVisual(Button button) {
        button.getStyleClass().remove(DRAG_TARGET_CLASS);
    }
    
    // Script Button Styling
    public static void applyScriptButtonClass(Button button) {
        if (!button.getStyleClass().contains(SCRIPT_BUTTON_CLASS)) {
            button.getStyleClass().add(SCRIPT_BUTTON_CLASS);
        }
    }
    
    // Programmatic hover effects for script buttons
    public static void applyScriptButtonHoverEffects(Button button) {
        // Store default effect
        javafx.scene.effect.Effect defaultEffect = new javafx.scene.effect.DropShadow(
            javafx.scene.effect.BlurType.GAUSSIAN,
            javafx.scene.paint.Color.rgb(0, 0, 0, 0.15),
            3, 0, 0, 1
        );
        button.setEffect(defaultEffect);
        
        // Create hover handler that tracks state properly
        button.addEventHandler(javafx.scene.input.MouseEvent.ANY, e -> {
            if (button.isDisabled()) return;
            
            boolean isDragging = button.getProperties().containsKey("isDragging") && 
                                (Boolean) button.getProperties().get("isDragging");
            
            if (e.getEventType() == javafx.scene.input.MouseEvent.MOUSE_ENTERED && !isDragging) {
                button.setScaleX(1.05);
                button.setScaleY(1.05);
                button.setEffect(new javafx.scene.effect.DropShadow(
                    javafx.scene.effect.BlurType.GAUSSIAN,
                    javafx.scene.paint.Color.rgb(0, 0, 0, 0.3),
                    8, 0, 0, 3
                ));
            } else if (e.getEventType() == javafx.scene.input.MouseEvent.MOUSE_EXITED) {
                button.setScaleX(1.0);
                button.setScaleY(1.0);
                button.setEffect(defaultEffect);
            } else if (e.getEventType() == javafx.scene.input.MouseEvent.MOUSE_PRESSED && e.isPrimaryButtonDown()) {
                button.setScaleX(0.98);
                button.setScaleY(0.98);
                button.setEffect(new javafx.scene.effect.DropShadow(
                    javafx.scene.effect.BlurType.GAUSSIAN,
                    javafx.scene.paint.Color.rgb(0, 0, 0, 0.1),
                    2, 0, 0, 1
                ));
            } else if (e.getEventType() == javafx.scene.input.MouseEvent.MOUSE_RELEASED) {
                // Check if mouse is still over button
                if (button.contains(button.screenToLocal(e.getScreenX(), e.getScreenY()))) {
                    button.setScaleX(1.05);
                    button.setScaleY(1.05);
                    button.setEffect(new javafx.scene.effect.DropShadow(
                        javafx.scene.effect.BlurType.GAUSSIAN,
                        javafx.scene.paint.Color.rgb(0, 0, 0, 0.3),
                        8, 0, 0, 3
                    ));
                } else {
                    button.setScaleX(1.0);
                    button.setScaleY(1.0);
                    button.setEffect(defaultEffect);
                }
            }
        });
    }
    
    public static void toggleSelectedClass(Button button, boolean selected) {
        if (selected) {
            if (!button.getStyleClass().contains(SELECTED_CLASS)) {
                button.getStyleClass().add(SELECTED_CLASS);
            }
        } else {
            button.getStyleClass().remove(SELECTED_CLASS);
        }
    }
    
    // Responsive Grid Layout
    public static void applyResponsiveGridLayout(GridPane grid, int columns, int rows) {
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        
        // Create column constraints
        for (int i = 0; i < columns; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / columns);
            col.setHgrow(Priority.ALWAYS);
            col.setFillWidth(true);
            grid.getColumnConstraints().add(col);
        }
        
        // Create row constraints
        for (int i = 0; i < rows; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(100.0 / rows);
            row.setVgrow(Priority.ALWAYS);
            row.setFillHeight(true);
            grid.getRowConstraints().add(row);
        }
    }
    
    // Drag Detection and Restoration
    public static class DragVisualState {
        private final Button button;
        private boolean hadHoverEffect = false;
        
        public DragVisualState(Button button) {
            this.button = button;
        }
        
        public void saveHoverState() {
            // Check if button has hover handlers
            hadHoverEffect = button.getOnMouseEntered() != null;
        }
        
        public void restoreHoverState() {
            if (hadHoverEffect) {
                // For navigation buttons, restore the hover effect
                if (button.getStyle() != null && button.getStyle().contains("#555555")) {
                    applyNavigationButtonHoverEffect(button);
                }
            }
        }
    }
    
    // Utility method to handle drag visual state
    public static DragVisualState createDragVisualState(Button button) {
        return new DragVisualState(button);
    }
}