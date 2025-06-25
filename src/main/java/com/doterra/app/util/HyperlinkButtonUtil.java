package com.doterra.app.util;

import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.Node;
import javafx.animation.ScaleTransition;
import javafx.animation.Interpolator;
import javafx.util.Duration;

/**
 * Utility class for creating hyperlink-style buttons with smooth hover animations.
 * Provides a consistent styling approach across the application.
 */
public class HyperlinkButtonUtil {
    
    /**
     * CSS class name for hyperlink-style buttons
     */
    public static final String HYPERLINK_BUTTON_CLASS = "regex-hyperlink-button";
    
    /**
     * CSS class name for button groups
     */
    public static final String BUTTON_GROUP_CLASS = "regex-button-group";
    
    /**
     * CSS class name for vertical button groups
     */
    public static final String BUTTON_GROUP_VERTICAL_CLASS = "regex-button-group-vertical";
    
    /**
     * CSS class name for selected buttons (bold styling)
     */
    public static final String SELECTED_CLASS = "selected";
    
    /**
     * CSS class name for active buttons (non-bold styling)
     */
    public static final String ACTIVE_CLASS = "active";
    
    /**
     * CSS class name for hyperlink-style ComboBoxes
     */
    public static final String HYPERLINK_COMBO_CLASS = "hyperlink-combo-box";
    
    /**
     * CSS class name for hyperlink-style CheckBoxes
     */
    public static final String HYPERLINK_CHECKBOX_CLASS = "hyperlink-checkbox";
    
    /**
     * Creates a hyperlink-style button with the given text and applies smooth hover animations.
     * 
     * @param text The button text
     * @return A styled Button with hyperlink appearance and animations
     */
    public static Button createHyperlinkButton(String text) {
        Button button = new Button(text);
        styleAsHyperlinkButton(button);
        return button;
    }
    
    /**
     * Applies hyperlink styling and smooth hover animations to an existing button.
     * 
     * @param button The button to style
     */
    public static void styleAsHyperlinkButton(Button button) {
        button.getStyleClass().add(HYPERLINK_BUTTON_CLASS);
        addSmoothHoverAnimation(button);
    }
    
    /**
     * Creates a horizontal button group container with visual grouping.
     * 
     * @param spacing The spacing between buttons
     * @param buttons The buttons to include in the group
     * @return An HBox container with button group styling
     */
    public static HBox createButtonGroup(double spacing, Button... buttons) {
        HBox group = new HBox(spacing);
        group.getStyleClass().add(BUTTON_GROUP_CLASS);
        group.getChildren().addAll(buttons);
        return group;
    }
    
    /**
     * Creates a vertical button group container with visual grouping.
     * 
     * @param spacing The spacing between buttons
     * @param buttons The buttons to include in the group
     * @return An HBox container with vertical button group styling
     */
    public static HBox createVerticalButtonGroup(double spacing, Button... buttons) {
        HBox group = new HBox(spacing);
        group.getStyleClass().add(BUTTON_GROUP_VERTICAL_CLASS);
        group.getChildren().addAll(buttons);
        return group;
    }
    
    /**
     * Creates a horizontal button group container with visual grouping that can handle mixed node types.
     * 
     * @param spacing The spacing between nodes
     * @param nodes The nodes to include in the group
     * @return An HBox container with button group styling
     */
    public static HBox createButtonGroup(double spacing, Node... nodes) {
        HBox group = new HBox(spacing);
        group.getStyleClass().add(BUTTON_GROUP_CLASS);
        group.getChildren().addAll(nodes);
        return group;
    }
    
    /**
     * Creates a spacer region for separating button groups.
     * 
     * @param width The width of the spacer
     * @return A Region configured as a spacer
     */
    public static Region createGroupSpacer(double width) {
        Region spacer = new Region();
        spacer.setPrefWidth(width);
        return spacer;
    }
    
    /**
     * Applies hyperlink styling to a ToggleButton.
     * 
     * @param toggleButton The ToggleButton to style
     */
    public static void styleAsHyperlinkToggleButton(ToggleButton toggleButton) {
        toggleButton.getStyleClass().add(HYPERLINK_BUTTON_CLASS);
        addSmoothHoverAnimation(toggleButton);
    }
    
    /**
     * Adds smooth scaling animation to a button on hover.
     * The button will smoothly scale to 105% on hover and back to 100% when not hovered.
     * 
     * @param button The button to animate
     */
    public static void addSmoothHoverAnimation(Button button) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), button);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);
        scaleUp.setInterpolator(Interpolator.EASE_OUT);
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), button);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.setInterpolator(Interpolator.EASE_OUT);
        
        button.setOnMouseEntered(e -> {
            scaleDown.stop();
            scaleUp.play();
        });
        
        button.setOnMouseExited(e -> {
            scaleUp.stop();
            scaleDown.play();
        });
    }
    
    /**
     * Adds smooth scaling animation to a ToggleButton on hover.
     * The button will smoothly scale to 105% on hover and back to 100% when not hovered.
     * 
     * @param toggleButton The ToggleButton to animate
     */
    public static void addSmoothHoverAnimation(ToggleButton toggleButton) {
        ScaleTransition scaleUp = new ScaleTransition(Duration.millis(200), toggleButton);
        scaleUp.setToX(1.05);
        scaleUp.setToY(1.05);
        scaleUp.setInterpolator(Interpolator.EASE_OUT);
        
        ScaleTransition scaleDown = new ScaleTransition(Duration.millis(200), toggleButton);
        scaleDown.setToX(1.0);
        scaleDown.setToY(1.0);
        scaleDown.setInterpolator(Interpolator.EASE_OUT);
        
        toggleButton.setOnMouseEntered(e -> {
            scaleDown.stop();
            scaleUp.play();
        });
        
        toggleButton.setOnMouseExited(e -> {
            scaleUp.stop();
            scaleDown.play();
        });
    }
    
    /**
     * Removes the smooth hover animation from a button.
     * Useful when you need to change the button's behavior or styling.
     * 
     * @param button The button to remove animations from
     */
    public static void removeSmoothHoverAnimation(Button button) {
        button.setOnMouseEntered(null);
        button.setOnMouseExited(null);
    }
    
    /**
     * Convenience method to create multiple hyperlink-style buttons at once.
     * 
     * @param texts The button texts
     * @return An array of styled buttons
     */
    public static Button[] createHyperlinkButtons(String... texts) {
        Button[] buttons = new Button[texts.length];
        for (int i = 0; i < texts.length; i++) {
            buttons[i] = createHyperlinkButton(texts[i]);
        }
        return buttons;
    }
    
    /**
     * Applies hyperlink styling to multiple existing buttons.
     * 
     * @param buttons The buttons to style
     */
    public static void styleAsHyperlinkButtons(Button... buttons) {
        for (Button button : buttons) {
            styleAsHyperlinkButton(button);
        }
    }
    
    /**
     * Sets a button as selected (adds selected styling).
     * 
     * @param button The button to mark as selected
     */
    public static void setButtonSelected(Button button) {
        button.getStyleClass().add(SELECTED_CLASS);
    }
    
    /**
     * Sets a button as unselected (removes selected styling).
     * 
     * @param button The button to mark as unselected
     */
    public static void setButtonUnselected(Button button) {
        button.getStyleClass().remove(SELECTED_CLASS);
    }
    
    /**
     * Sets a ToggleButton as selected (adds selected styling).
     * 
     * @param toggleButton The ToggleButton to mark as selected
     */
    public static void setButtonSelected(ToggleButton toggleButton) {
        toggleButton.getStyleClass().add(SELECTED_CLASS);
    }
    
    /**
     * Sets a ToggleButton as unselected (removes selected styling).
     * 
     * @param toggleButton The ToggleButton to mark as unselected
     */
    public static void setButtonUnselected(ToggleButton toggleButton) {
        toggleButton.getStyleClass().remove(SELECTED_CLASS);
    }
    
    /**
     * Sets a button as active (adds active styling - no bold).
     * 
     * @param button The button to mark as active
     */
    public static void setButtonActive(Button button) {
        button.getStyleClass().add(ACTIVE_CLASS);
    }
    
    /**
     * Sets a button as inactive (removes active styling).
     * 
     * @param button The button to mark as inactive
     */
    public static void setButtonInactive(Button button) {
        button.getStyleClass().remove(ACTIVE_CLASS);
    }
    
    /**
     * Sets a ToggleButton as active (adds active styling - no bold).
     * 
     * @param toggleButton The ToggleButton to mark as active
     */
    public static void setButtonActive(ToggleButton toggleButton) {
        toggleButton.getStyleClass().add(ACTIVE_CLASS);
    }
    
    /**
     * Sets a ToggleButton as inactive (removes active styling).
     * 
     * @param toggleButton The ToggleButton to mark as inactive
     */
    public static void setButtonInactive(ToggleButton toggleButton) {
        toggleButton.getStyleClass().remove(ACTIVE_CLASS);
    }
    
    /**
     * Toggles the selected state of a button.
     * 
     * @param button The button to toggle
     * @return true if the button is now selected, false otherwise
     */
    public static boolean toggleButtonSelection(Button button) {
        if (button.getStyleClass().contains(SELECTED_CLASS)) {
            setButtonUnselected(button);
            return false;
        } else {
            setButtonSelected(button);
            return true;
        }
    }
    
    /**
     * Toggles the active state of a button.
     * 
     * @param button The button to toggle
     * @return true if the button is now active, false otherwise
     */
    public static boolean toggleButtonActive(Button button) {
        if (button.getStyleClass().contains(ACTIVE_CLASS)) {
            setButtonInactive(button);
            return false;
        } else {
            setButtonActive(button);
            return true;
        }
    }
    
    /**
     * Toggles the active state of a ToggleButton.
     * 
     * @param toggleButton The ToggleButton to toggle
     * @return true if the button is now active, false otherwise
     */
    public static boolean toggleButtonActive(ToggleButton toggleButton) {
        if (toggleButton.getStyleClass().contains(ACTIVE_CLASS)) {
            setButtonInactive(toggleButton);
            return false;
        } else {
            setButtonActive(toggleButton);
            return true;
        }
    }
    
    /**
     * Sets one button as selected and all others as unselected in a group.
     * 
     * @param selectedButton The button to select
     * @param allButtons All buttons in the group
     */
    public static void setExclusiveSelection(Button selectedButton, Button... allButtons) {
        for (Button button : allButtons) {
            setButtonUnselected(button);
        }
        setButtonSelected(selectedButton);
    }
    
    /**
     * Sets one button as active and all others as inactive in a group.
     * 
     * @param activeButton The button to set as active
     * @param allButtons All buttons in the group
     */
    public static void setExclusiveActive(Button activeButton, Button... allButtons) {
        for (Button button : allButtons) {
            setButtonInactive(button);
        }
        setButtonActive(activeButton);
    }
    
    /**
     * Applies hyperlink styling to a ComboBox.
     * 
     * @param comboBox The ComboBox to style
     */
    public static void styleAsHyperlinkComboBox(ComboBox<?> comboBox) {
        comboBox.getStyleClass().add(HYPERLINK_COMBO_CLASS);
    }
    
    /**
     * Creates and styles multiple ComboBoxes.
     * 
     * @param comboBoxes The ComboBoxes to style
     */
    public static void styleAsHyperlinkComboBoxes(ComboBox<?>... comboBoxes) {
        for (ComboBox<?> comboBox : comboBoxes) {
            styleAsHyperlinkComboBox(comboBox);
        }
    }
    
    /**
     * Applies hyperlink styling to a CheckBox.
     * 
     * @param checkBox The CheckBox to style
     */
    public static void styleAsHyperlinkCheckBox(CheckBox checkBox) {
        checkBox.getStyleClass().add(HYPERLINK_CHECKBOX_CLASS);
    }
    
    /**
     * Creates and styles multiple CheckBoxes.
     * 
     * @param checkBoxes The CheckBoxes to style
     */
    public static void styleAsHyperlinkCheckBoxes(CheckBox... checkBoxes) {
        for (CheckBox checkBox : checkBoxes) {
            styleAsHyperlinkCheckBox(checkBox);
        }
    }
}