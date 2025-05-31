package com.doterra.app.util;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;
import javafx.geometry.Insets;
import javafx.scene.Node;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling variable replacement in script content.
 * Variables are defined using parentheses: (variable name)
 * Escaped parentheses using backslash are ignored: \(not a variable)
 */
public class VariableReplacer {
    
    // Pattern to match (variable) but not \(escaped)
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("(?<!\\\\)\\(([^)]+)\\)");
    
    /**
     * Finds all variables in the given content.
     * 
     * @param content The content to search for variables
     * @return List of variable names (without parentheses)
     */
    public static List<String> findVariables(String content) {
        List<String> variables = new ArrayList<>();
        Matcher matcher = VARIABLE_PATTERN.matcher(content);
        
        while (matcher.find()) {
            variables.add(matcher.group(1));
        }
        
        return variables;
    }
    
    /**
     * Replaces all variables in the content with user-provided values.
     * Shows input dialogs for each variable found.
     * 
     * @param content The original content with variables
     * @param scriptName The name of the script (for dialog titles)
     * @return The content with variables replaced, or null if user cancelled
     */
    public static String replaceVariables(String content, String scriptName) {
        if (content == null || content.isEmpty()) {
            return content;
        }
        
        // First, handle escaped parentheses by temporarily replacing them
        String tempContent = content.replace("\\(", "ESCAPED_OPEN_PAREN").replace("\\)", "ESCAPED_CLOSE_PAREN");
        
        List<String> variables = findVariables(tempContent);
        
        if (variables.isEmpty()) {
            // No variables found, just restore escaped parentheses and return
            return tempContent.replace("ESCAPED_OPEN_PAREN", "(").replace("ESCAPED_CLOSE_PAREN", ")");
        }
        
        // Prompt user for all variables in a single dialog
        Map<String, String> variableValues = promptForAllVariables(variables, scriptName);
        if (variableValues == null) {
            // User cancelled, return null to indicate cancellation
            return null;
        }
        
        String result = tempContent;
        
        // Replace all variables with their values
        for (String variable : variables) {
            String replacement = variableValues.get(variable);
            if (replacement != null) {
                // Replace the first occurrence of this variable
                result = result.replaceFirst("(?<!\\\\)\\(" + Pattern.quote(variable) + "\\)", 
                                          Matcher.quoteReplacement(replacement));
            }
        }
        
        // Restore escaped parentheses
        result = result.replace("ESCAPED_OPEN_PAREN", "(").replace("ESCAPED_CLOSE_PAREN", ")");
        
        return result;
    }
    
    /**
     * Prompts the user to enter values for all variables in a single dialog.
     * 
     * @param variables List of variable names
     * @param scriptName The name of the script
     * @return Map of variable names to their values, or null if cancelled
     */
    private static Map<String, String> promptForAllVariables(List<String> variables, String scriptName) {
        Dialog<Map<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Script Variables - " + scriptName);
        dialog.setHeaderText("Enter values for variables:");
        dialog.initStyle(StageStyle.UTILITY);
        
        // Set the button types
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
        
        // Create the content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        List<TextField> textFields = new ArrayList<>();
        
        for (int i = 0; i < variables.size(); i++) {
            String variable = variables.get(i);
            Label label = new Label(variable + ":");
            TextField textField = new TextField();
            textField.setPromptText("Enter " + variable);
            textField.setPrefColumnCount(20);
            
            grid.add(label, 0, i);
            grid.add(textField, 1, i);
            textFields.add(textField);
        }
        
        // Enable/Disable OK button depending on whether fields are filled
        Node okButton = dialog.getDialogPane().lookupButton(okButtonType);
        okButton.setDisable(false); // Allow empty values
        
        dialog.getDialogPane().setContent(grid);
        
        // Focus on first text field
        if (!textFields.isEmpty()) {
            textFields.get(0).requestFocus();
        }
        
        // Convert the result when OK button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                Map<String, String> result = new HashMap<>();
                for (int i = 0; i < variables.size(); i++) {
                    String variable = variables.get(i);
                    String value = textFields.get(i).getText();
                    result.put(variable, value != null ? value : "");
                }
                return result;
            }
            return null;
        });
        
        Optional<Map<String, String>> result = dialog.showAndWait();
        return result.orElse(null);
    }
    
    /**
     * Checks if the content contains any variables.
     * 
     * @param content The content to check
     * @return true if variables are found, false otherwise
     */
    public static boolean hasVariables(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        
        // Temporarily handle escaped parentheses
        String tempContent = content.replace("\\(", "ESCAPED_OPEN_PAREN").replace("\\)", "ESCAPED_CLOSE_PAREN");
        return VARIABLE_PATTERN.matcher(tempContent).find();
    }
}