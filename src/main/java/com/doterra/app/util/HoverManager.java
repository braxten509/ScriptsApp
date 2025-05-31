package com.doterra.app.util;

import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.BlurType;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseEvent;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.util.WeakHashMap;

public class HoverManager {
    
    private static final WeakHashMap<Button, HoverState> buttonStates = new WeakHashMap<>();
    
    private static class HoverState {
        boolean isHovered = false;
        boolean isDragging = false;
        ScaleTransition scaleIn;
        ScaleTransition scaleOut;
        DropShadow defaultEffect;
        DropShadow hoverEffect;
        
        HoverState() {
            defaultEffect = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.15), 3, 0, 0, 1);
            hoverEffect = new DropShadow(BlurType.GAUSSIAN, Color.rgb(0, 0, 0, 0.3), 8, 0, 0, 3);
        }
    }
    
    public static void applyHoverEffects(Button button) {
        HoverState state = new HoverState();
        buttonStates.put(button, state);
        
        // Initialize with default effect
        button.setEffect(state.defaultEffect);
        
        // Create smooth scale transitions
        state.scaleIn = new ScaleTransition(Duration.millis(150), button);
        state.scaleIn.setToX(1.05);
        state.scaleIn.setToY(1.05);
        
        state.scaleOut = new ScaleTransition(Duration.millis(150), button);
        state.scaleOut.setToX(1.0);
        state.scaleOut.setToY(1.0);
        
        // Simple hover tracking
        button.setOnMouseEntered(e -> {
            if (!state.isDragging) {
                enterHover(button, state);
            }
        });
        
        button.setOnMouseExited(e -> {
            if (!state.isDragging) {
                exitHover(button, state);
            }
        });
        
        // Handle drag states
        button.addEventFilter(MouseEvent.DRAG_DETECTED, e -> {
            state.isDragging = true;
            exitHover(button, state);
        });
        
        // Force mouse position check after drag ends
        button.addEventFilter(MouseEvent.ANY, e -> {
            if (e.getEventType() == MouseEvent.MOUSE_MOVED && !state.isDragging) {
                // Check if we're over the button
                if (!state.isHovered && button.isHover()) {
                    enterHover(button, state);
                } else if (state.isHovered && !button.isHover()) {
                    exitHover(button, state);
                }
            }
        });
    }
    
    private static void enterHover(Button button, HoverState state) {
        state.isHovered = true;
        state.scaleOut.stop();
        state.scaleIn.play();
        button.setEffect(state.hoverEffect);
    }
    
    private static void exitHover(Button button, HoverState state) {
        state.isHovered = false;
        state.scaleIn.stop();
        state.scaleOut.play();
        button.setEffect(state.defaultEffect);
    }
    
    public static void endDrag(Button button) {
        HoverState state = buttonStates.get(button);
        if (state != null) {
            state.isDragging = false;
        }
    }
}