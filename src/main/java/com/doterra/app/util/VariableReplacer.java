package com.doterra.app.util;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.StageStyle;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Font;
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
     * Variables with empty or no values are left as literal text (variable name).
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
        Map<String, String> variableValues = promptForAllVariables(variables, scriptName, content);
        if (variableValues == null) {
            // User cancelled, return null to indicate cancellation
            return null;
        }
        
        String result = tempContent;
        
        // Replace all variables with their values
        for (String variable : variables) {
            String replacement = variableValues.get(variable);
            if (replacement != null && !replacement.trim().isEmpty()) {
                // Replace the first occurrence of this variable with the provided value
                result = result.replaceFirst("(?<!\\\\)\\(" + Pattern.quote(variable) + "\\)", 
                                          Matcher.quoteReplacement(replacement));
            }
            // If replacement is null or empty, keep the literal variable text (variable)
            // This allows users to see and edit templates by leaving variables unfilled
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
     * @param content The original content to extract context from
     * @return Map of variable names to their values, or null if cancelled
     */
    private static Map<String, String> promptForAllVariables(List<String> variables, String scriptName, String content) {
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
        grid.setVgap(15);
        grid.setPadding(new Insets(20, 20, 10, 10));
        
        List<TextField> textFields = new ArrayList<>();
        
        for (int i = 0; i < variables.size(); i++) {
            String variable = variables.get(i);
            
            // Create context label with "Replace <variable> in:" format
            Label contextLabel = new Label("Replace " + variable + " in:");
            contextLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
            
            // Create context display with rich text formatting
            TextFlow contextDisplay = createFormattedContext(content, variable);
            contextDisplay.setMaxWidth(450);
            
            TextField textField = new TextField();
            textField.setPromptText("Enter " + variable);
            textField.setPrefColumnCount(25);
            
            // Add components to grid with better spacing
            grid.add(contextLabel, 0, i * 3, 2, 1);
            grid.add(contextDisplay, 0, i * 3 + 1, 2, 1);
            grid.add(new Label("Value:"), 0, i * 3 + 2);
            grid.add(textField, 1, i * 3 + 2);
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
     * Creates a formatted TextFlow showing context around a variable with proper styling.
     * Shows a few words before and after the variable, with the variable name bolded.
     * 
     * @param content The full content containing the variable
     * @param variableName The name of the variable to find context for
     * @return TextFlow with formatted context
     */
    private static TextFlow createFormattedContext(String content, String variableName) {
        TextFlow textFlow = new TextFlow();
        textFlow.setStyle("-fx-padding: 5; -fx-background-color: #f9f9f9; -fx-border-color: #e0e0e0; -fx-border-radius: 3;");
        
        if (content == null || variableName == null) {
            Text fallback = new Text("(" + variableName + ")");
            fallback.setStyle("-fx-font-weight: bold;");
            textFlow.getChildren().add(fallback);
            return textFlow;
        }
        
        // Handle escaped parentheses temporarily
        String tempContent = content.replace("\\(", "ESCAPED_OPEN_PAREN").replace("\\)", "ESCAPED_CLOSE_PAREN");
        
        // Find the variable pattern in the content
        String variablePattern = "\\(" + Pattern.quote(variableName) + "\\)";
        Matcher matcher = Pattern.compile(variablePattern).matcher(tempContent);
        
        if (!matcher.find()) {
            Text fallback = new Text("(" + variableName + ")");
            fallback.setStyle("-fx-font-weight: bold;");
            textFlow.getChildren().add(fallback);
            return textFlow;
        }
        
        int variableStart = matcher.start();
        int variableEnd = matcher.end();
        
        // Extract context before and after the variable
        String beforeContext = extractContextBefore(tempContent, variableStart);
        String afterContext = extractContextAfter(tempContent, variableEnd);
        
        // Restore escaped parentheses in context
        beforeContext = beforeContext.replace("ESCAPED_OPEN_PAREN", "(").replace("ESCAPED_CLOSE_PAREN", ")");
        afterContext = afterContext.replace("ESCAPED_OPEN_PAREN", "(").replace("ESCAPED_CLOSE_PAREN", ")");
        
        // Create text elements
        if (!beforeContext.isEmpty()) {
            Text beforeText = new Text(beforeContext + " ");
            beforeText.setStyle("-fx-font-size: 11px; -fx-fill: #666666;");
            textFlow.getChildren().add(beforeText);
        }
        
        // Bold variable name
        Text variableText = new Text("(" + variableName + ")");
        variableText.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-fill: #333333;");
        textFlow.getChildren().add(variableText);
        
        if (!afterContext.isEmpty()) {
            Text afterText = new Text(" " + afterContext);
            afterText.setStyle("-fx-font-size: 11px; -fx-fill: #666666;");
            textFlow.getChildren().add(afterText);
        }
        
        return textFlow;
    }
    
    /**
     * Extracts context before the variable position.
     */
    private static String extractContextBefore(String content, int position) {
        if (position <= 0) return "";
        
        String before = content.substring(0, position);
        
        // Split into words and take the last few words
        String[] words = before.trim().split("\\s+");
        StringBuilder context = new StringBuilder();
        
        // Take up to 5 words before, or start from beginning if fewer words
        int startIndex = Math.max(0, words.length - 5);
        for (int i = startIndex; i < words.length; i++) {
            if (context.length() > 0) context.append(" ");
            context.append(words[i]);
        }
        
        return context.toString();
    }
    
    /**
     * Extracts context after the variable position.
     */
    private static String extractContextAfter(String content, int position) {
        if (position >= content.length()) return "";
        
        String after = content.substring(position);
        
        // Split into words and take the first few words
        String[] words = after.trim().split("\\s+");
        StringBuilder context = new StringBuilder();
        
        // Take up to 5 words after, but if we hit a sentence ending, include it and stop
        for (int i = 0; i < Math.min(5, words.length); i++) {
            if (context.length() > 0) context.append(" ");
            context.append(words[i]);
            
            // If this word ends with sentence punctuation, stop here
            if (words[i].matches(".*[.!?]$")) {
                break;
            }
        }
        
        return context.toString();
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