package com.doterra.app.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.animation.ScaleTransition;
import javafx.animation.Interpolator;
import javafx.util.Duration;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import com.doterra.app.util.HyperlinkButtonUtil;

public class CalculatorPanel extends BorderPane {
    private TextArea inputArea;
    private TextFlow displayFlow;
    private TextField equationField;
    private TextField resultField;
    private List<NumberSelection> selections;
    private List<NumberNode> allNumbers;
    private Map<String, NumberSelection> selectionMap;
    private int currentIndex;
    private static final Color[] HIGHLIGHT_COLORS = {
        Color.GOLD, Color.LIMEGREEN, Color.DODGERBLUE, 
        Color.HOTPINK, Color.DARKORANGE, Color.TURQUOISE,
        Color.MEDIUMPURPLE, Color.TOMATO, Color.DARKKHAKI
    };
    private int colorIndex;
    private String lastProcessedText = "";
    
    // Operator controls
    private String currentOperator = "+";
    private Map<String, Button> operatorButtons;
    
    // Regex pattern controls
    private TextField regexField;
    private ComboBox<NamedPattern> savedRegexCombo;
    private ComboBox<String> operationCombo;
    private ObservableList<NamedPattern> savedRegexPatterns;
    private static final String REGEX_FILE = "data/calculator_regex_patterns.dat";
    private boolean useCustomRegex = false;
    private Pattern customPattern = null;
    
    // Pop-out window reference
    private Stage popOutStage = null;
    
    private static class NumberNode {
        String value;
        int startPos;
        int endPos;
        Text textNode;
        boolean isHighlighted;
        
        NumberNode(String value, int startPos, int endPos) {
            this.value = value;
            this.startPos = startPos;
            this.endPos = endPos;
            this.isHighlighted = false;
        }
    }
    
    private static class NamedPattern implements Serializable {
        private static final long serialVersionUID = 1L;
        private String name;
        private String pattern;
        private boolean isDefault;
        
        public NamedPattern(String name, String pattern) {
            this.name = name;
            this.pattern = pattern;
            this.isDefault = false;
        }
        
        public NamedPattern(String name, String pattern, boolean isDefault) {
            this.name = name;
            this.pattern = pattern;
            this.isDefault = isDefault;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getPattern() {
            return pattern;
        }
        
        public boolean isDefault() {
            return isDefault;
        }
        
        public void setDefault(boolean isDefault) {
            this.isDefault = isDefault;
        }
        
        @Override
        public String toString() {
            return isDefault ? name + " (Default)" : name;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            NamedPattern that = (NamedPattern) obj;
            return pattern.equals(that.pattern);
        }
        
        @Override
        public int hashCode() {
            return pattern.hashCode();
        }
    }
    
    private static class NumberSelection {
        String value;
        int index;
        NumberNode node;
        Color color;
        Text indexLabel;
        
        NumberSelection(String value, int index, NumberNode node, Color color) {
            this.value = value;
            this.index = index;
            this.node = node;
            this.color = color;
        }
    }
    
    public CalculatorPanel() {
        selections = new ArrayList<>();
        allNumbers = new ArrayList<>();
        selectionMap = new HashMap<>();
        operatorButtons = new HashMap<>();
        currentIndex = 1;
        colorIndex = 0;
        savedRegexPatterns = FXCollections.observableArrayList();
        
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f5f5f5;");
        
        // Create main content
        VBox mainContent = new VBox(15);
        
        // Top section - Input area
        VBox topSection = createTopSection();
        
        // Middle section - Display area
        VBox middleSection = createMiddleSection();
        
        // Regex section
        VBox regexSection = createRegexSection();
        
        // Equation building section
        VBox equationSection = createEquationSection();
        
        // Result section
        HBox resultSection = createResultSection();
        
        mainContent.getChildren().addAll(topSection, middleSection, regexSection, equationSection, resultSection);
        
        setCenter(mainContent);
        
        // Load saved regex patterns
        loadRegexPatterns();
        
        // Load default pattern if set
        loadDefaultPattern();
    }
    
    private VBox createTopSection() {
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(10));
        topSection.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        HBox labelWithButtons = new HBox(10);
        labelWithButtons.setAlignment(Pos.CENTER_LEFT);
        
        Label inputLabel = new Label("Paste your text here:");
        inputLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button clearBtn = HyperlinkButtonUtil.createHyperlinkButton("Clear");
        clearBtn.setOnAction(e -> inputArea.clear());
        
        Button popOutBtn = HyperlinkButtonUtil.createHyperlinkButton("Pop Out");
        popOutBtn.setOnAction(e -> showInputPopOut());
        
        // Input actions group
        HBox inputActionsGroup = HyperlinkButtonUtil.createButtonGroup(5, clearBtn, popOutBtn);
        
        labelWithButtons.getChildren().addAll(inputLabel, spacer, inputActionsGroup);
        
        inputArea = new TextArea();
        inputArea.setPrefRowCount(3);
        inputArea.setPromptText("Paste text containing numbers...");
        inputArea.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 3;");
        inputArea.textProperty().addListener((obs, old, text) -> processText(text));
        
        topSection.getChildren().addAll(labelWithButtons, inputArea);
        return topSection;
    }
    
    private VBox createMiddleSection() {
        VBox middleSection = new VBox(10);
        middleSection.setPadding(new Insets(10));
        middleSection.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        Label displayLabel = new Label("Click on numbers to build your equation:");
        displayLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 3; -fx-background-radius: 3;");
        displayFlow = new TextFlow();
        displayFlow.setPadding(new Insets(10));
        displayFlow.setLineSpacing(5);
        scrollPane.setContent(displayFlow);
        scrollPane.setPrefHeight(200);
        scrollPane.setFitToWidth(true);
        
        middleSection.getChildren().addAll(displayLabel, scrollPane);
        return middleSection;
    }
    
    private VBox createRegexSection() {
        VBox regexSection = new VBox(10);
        regexSection.setPadding(new Insets(10));
        regexSection.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        Label regexLabel = new Label("Regex Pattern Matching:");
        regexLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        HBox regexControls = new HBox(10);
        regexControls.setAlignment(Pos.CENTER_LEFT);
        
        regexField = new TextField();
        regexField.setPromptText("Enter regex pattern (e.g., \\d{3}\\.\\d{2})");
        regexField.setPrefWidth(250);
        
        // Add real-time regex validation
        regexField.textProperty().addListener((obs, old, text) -> validateRegexPattern(text));
        
        savedRegexCombo = new ComboBox<>(savedRegexPatterns);
        savedRegexCombo.setPromptText("Saved patterns");
        savedRegexCombo.setPrefWidth(200);
        HyperlinkButtonUtil.styleAsHyperlinkComboBox(savedRegexCombo);
        savedRegexCombo.setOnAction(e -> {
            NamedPattern selected = savedRegexCombo.getValue();
            if (selected != null) {
                regexField.setText(selected.getPattern());
            }
        });
        
        regexControls.getChildren().addAll(regexField, savedRegexCombo);
        
        // Pattern management buttons
        HBox patternButtons = new HBox(5);
        patternButtons.setAlignment(Pos.CENTER_LEFT);
        
        Button[] patternManagementButtons = HyperlinkButtonUtil.createHyperlinkButtons(
            "New", "Clear", "Save", "Duplicate", "Rename", "Set Default", "Delete");
        
        Button newBtn = patternManagementButtons[0];
        newBtn.setOnAction(e -> newRegexPattern());
        
        Button clearBtn = patternManagementButtons[1];
        clearBtn.setOnAction(e -> clearRegexPattern());
        
        Button saveBtn = patternManagementButtons[2];
        saveBtn.setOnAction(e -> saveRegexPattern());
        
        Button duplicateBtn = patternManagementButtons[3];
        duplicateBtn.setOnAction(e -> duplicateRegexPattern());
        
        Button renameBtn = patternManagementButtons[4];
        renameBtn.setOnAction(e -> renameRegexPattern());
        
        Button setDefaultBtn = patternManagementButtons[5];
        setDefaultBtn.setOnAction(e -> setDefaultRegexPattern());
        
        Button deleteBtn = patternManagementButtons[6];
        deleteBtn.setOnAction(e -> deleteRegexPattern());
        
        // Pattern management group
        HBox patternManagementGroup = HyperlinkButtonUtil.createButtonGroup(5, 
            newBtn, clearBtn, saveBtn, duplicateBtn, renameBtn, setDefaultBtn, deleteBtn);
        
        patternButtons.getChildren().addAll(patternManagementGroup);
        
        HBox operationControls = new HBox(10);
        operationControls.setAlignment(Pos.CENTER_LEFT);
        
        Label operationLabel = new Label("Bulk Operation:");
        
        operationCombo = new ComboBox<>();
        operationCombo.getItems().addAll("Add All (+)", "Multiply All (×)", "Add Sequentially", "Custom Expression");
        operationCombo.setValue("Add All (+)");
        operationCombo.setPrefWidth(150);
        HyperlinkButtonUtil.styleAsHyperlinkComboBox(operationCombo);
        
        Button applyRegexBtn = HyperlinkButtonUtil.createHyperlinkButton("Find & Apply");
        applyRegexBtn.setOnAction(e -> applyRegexOperation());
        
        Button highlightOnlyBtn = HyperlinkButtonUtil.createHyperlinkButton("Highlight Only");
        highlightOnlyBtn.setOnAction(e -> highlightRegexMatches());
        
        // Operation actions group
        HBox operationActionsGroup = HyperlinkButtonUtil.createButtonGroup(5, applyRegexBtn, highlightOnlyBtn);
        
        operationControls.getChildren().addAll(operationLabel, operationCombo, operationActionsGroup);
        
        regexSection.getChildren().addAll(regexLabel, regexControls, patternButtons, operationControls);
        return regexSection;
    }
    
    private VBox createEquationSection() {
        VBox equationSection = new VBox(10);
        equationSection.setPadding(new Insets(10));
        equationSection.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        Label equationLabel = new Label("Mathematical Equation:");
        equationLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        equationField = new TextField();
        equationField.setEditable(false);
        equationField.setStyle("-fx-font-size: 16px; -fx-background-color: #f9f9f9; -fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 3;");
        
        // Operator buttons
        HBox operatorBox = new HBox(10);
        operatorBox.setAlignment(Pos.CENTER);
        
        Button plusBtn = createOperatorButton("+", "+", true);
        Button minusBtn = createOperatorButton("-", "-", false);
        Button multiplyBtn = createOperatorButton("×", "*", false);
        Button divideBtn = createOperatorButton("÷", "/", false);
        Button leftParenBtn = createSpecialOperatorButton("(");
        Button rightParenBtn = createSpecialOperatorButton(")");
        
        // Math operators group
        HBox mathOperatorsGroup = HyperlinkButtonUtil.createButtonGroup(5, plusBtn, minusBtn, multiplyBtn, divideBtn);
        
        // Parentheses group  
        HBox parenthesesGroup = HyperlinkButtonUtil.createButtonGroup(5, leftParenBtn, rightParenBtn);
        
        Region operatorSpacer = HyperlinkButtonUtil.createGroupSpacer(10);
        
        operatorBox.getChildren().addAll(mathOperatorsGroup, operatorSpacer, parenthesesGroup);
        
        // Control buttons
        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.CENTER);
        
        Button eraseBtn = HyperlinkButtonUtil.createHyperlinkButton("Erase Last");
        eraseBtn.setOnAction(e -> eraseLastSelection());
        
        Button clearBtn = HyperlinkButtonUtil.createHyperlinkButton("Clear All");
        clearBtn.setOnAction(e -> clearAll());
        
        Button calculateBtn = HyperlinkButtonUtil.createHyperlinkButton("Calculate");
        calculateBtn.setOnAction(e -> calculate());
        calculateBtn.setPrefWidth(150);
        
        // Calculator control group
        HBox calculatorControlGroup = HyperlinkButtonUtil.createButtonGroup(5, eraseBtn, clearBtn);
        
        // Main action group (separate for emphasis)
        HBox calculateGroup = HyperlinkButtonUtil.createButtonGroup(5, calculateBtn);
        
        Region controlSpacer = HyperlinkButtonUtil.createGroupSpacer(10);
        
        controlBox.getChildren().addAll(calculatorControlGroup, controlSpacer, calculateGroup);
        
        equationSection.getChildren().addAll(equationLabel, equationField, operatorBox, controlBox);
        return equationSection;
    }
    
    private HBox createResultSection() {
        HBox resultSection = new HBox(10);
        resultSection.setPadding(new Insets(15));
        resultSection.setAlignment(Pos.CENTER_LEFT);
        resultSection.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        Label resultLabel = new Label("Result:");
        resultLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        resultField = new TextField();
        resultField.setEditable(false);
        resultField.setPrefWidth(200);
        resultField.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #e8f5e9; -fx-border-color: #4CAF50; -fx-border-width: 1; -fx-border-radius: 3;");
        
        Button copyBtn = HyperlinkButtonUtil.createHyperlinkButton("Copy");
        copyBtn.setOnAction(e -> copyResult());
        
        resultSection.getChildren().addAll(resultLabel, resultField, copyBtn);
        return resultSection;
    }
    
    private void processText(String text) {
        if (text == null || text.isEmpty()) {
            displayFlow.getChildren().clear();
            allNumbers.clear();
            clearAll();
            lastProcessedText = "";
            return;
        }
        
        // Check if only non-number text changed
        if (shouldPreserveSelections(text)) {
            updateDisplayWithSelections();
            return;
        }
        
        // Full reprocess if numbers changed
        displayFlow.getChildren().clear();
        allNumbers.clear();
        clearAll();
        lastProcessedText = text;
        
        // Use custom regex if available and valid, otherwise use default number pattern
        Pattern pattern;
        boolean usingGroups = false;
        
        if (useCustomRegex && customPattern != null) {
            pattern = customPattern;
            // Check if pattern has capturing groups
            usingGroups = pattern.pattern().contains("(") && !pattern.pattern().contains("\\(");
        } else {
            pattern = Pattern.compile("-?\\d+\\.?\\d*");
        }
        
        Matcher matcher = pattern.matcher(text);
        
        int lastEnd = 0;
        while (matcher.find()) {
            // Add non-matching text as gray
            if (matcher.start() > lastEnd) {
                Text grayText = new Text(text.substring(lastEnd, matcher.start()));
                grayText.setFill(Color.GRAY);
                displayFlow.getChildren().add(grayText);
            }
            
            // Get the matched value
            String matchedValue;
            int matchStart, matchEnd;
            
            if (usingGroups && matcher.groupCount() > 0) {
                try {
                    // Use the first capturing group if available
                    matchedValue = matcher.group(1);
                    matchStart = matcher.start(1);
                    matchEnd = matcher.end(1);
                } catch (Exception e) {
                    // If capture group is null or invalid, skip this match
                    lastEnd = matcher.end();
                    continue;
                }
                
                // Add text before the captured group as gray
                if (matcher.start() < matchStart) {
                    Text preGroupText = new Text(text.substring(matcher.start(), matchStart));
                    preGroupText.setFill(Color.GRAY);
                    displayFlow.getChildren().add(preGroupText);
                }
            } else {
                // Use the entire match
                matchedValue = matcher.group();
                matchStart = matcher.start();
                matchEnd = matcher.end();
            }
            
            // Validate that the matched value is numeric
            if (isNumeric(matchedValue)) {
                // Create number node
                NumberNode node = new NumberNode(matchedValue, matchStart, matchEnd);
                
                Text numberText = new Text(matchedValue);
                numberText.setFill(Color.BLACK);
                numberText.setStyle("-fx-font-weight: bold; -fx-cursor: hand;");
                node.textNode = numberText;
                
                numberText.setOnMouseClicked(e -> selectNumber(node));
                
                // Add tooltip
                Tooltip tooltip = new Tooltip("Click to add to equation");
                Tooltip.install(numberText, tooltip);
                
                allNumbers.add(node);
                displayFlow.getChildren().add(numberText);
            } else {
                // Show non-numeric captures in red
                Text errorText = new Text(matchedValue);
                errorText.setFill(Color.RED);
                errorText.setStyle("-fx-font-weight: bold;");
                Tooltip errorTooltip = new Tooltip("Not a valid number: " + matchedValue);
                Tooltip.install(errorText, errorTooltip);
                displayFlow.getChildren().add(errorText);
            }
            
            // Add text after the captured group but before match end
            if (usingGroups && matcher.groupCount() > 0 && matchEnd < matcher.end()) {
                Text postGroupText = new Text(text.substring(matchEnd, matcher.end()));
                postGroupText.setFill(Color.GRAY);
                displayFlow.getChildren().add(postGroupText);
            }
            
            lastEnd = matcher.end();
        }
        
        // Add remaining text as gray
        if (lastEnd < text.length()) {
            Text grayText = new Text(text.substring(lastEnd));
            grayText.setFill(Color.GRAY);
            displayFlow.getChildren().add(grayText);
        }
    }
    
    private boolean shouldPreserveSelections(String newText) {
        if (lastProcessedText.isEmpty()) return false;
        
        // For custom regex, we can't easily determine if selections should be preserved
        // so we'll always reprocess when using custom regex
        if (useCustomRegex && customPattern != null) {
            return false;
        }
        
        // Extract all numbers from both texts using default pattern
        Pattern pattern = Pattern.compile("-?\\d+\\.?\\d*");
        List<String> oldNumbers = new ArrayList<>();
        List<String> newNumbers = new ArrayList<>();
        
        Matcher oldMatcher = pattern.matcher(lastProcessedText);
        while (oldMatcher.find()) {
            oldNumbers.add(oldMatcher.group());
        }
        
        Matcher newMatcher = pattern.matcher(newText);
        while (newMatcher.find()) {
            newNumbers.add(newMatcher.group());
        }
        
        // Check if numbers are the same
        return oldNumbers.equals(newNumbers);
    }
    
    private void updateDisplayWithSelections() {
        // Update display preserving current selections
        String text = inputArea.getText();
        displayFlow.getChildren().clear();
        
        Pattern pattern = Pattern.compile("-?\\d+\\.?\\d*");
        Matcher matcher = pattern.matcher(text);
        
        int lastEnd = 0;
        int numberIndex = 0;
        
        while (matcher.find()) {
            // Add non-number text as gray
            if (matcher.start() > lastEnd) {
                Text grayText = new Text(text.substring(lastEnd, matcher.start()));
                grayText.setFill(Color.GRAY);
                displayFlow.getChildren().add(grayText);
            }
            
            // Update number node position
            if (numberIndex < allNumbers.size()) {
                NumberNode node = allNumbers.get(numberIndex);
                node.startPos = matcher.start();
                node.endPos = matcher.end();
                
                // Check if this node has a selection
                NumberSelection selection = selectionMap.get(node.value + "_" + numberIndex);
                if (selection != null) {
                    node.textNode.setFill(selection.color);
                    displayFlow.getChildren().add(node.textNode);
                    
                    // Re-add index label
                    if (selection.indexLabel != null) {
                        displayFlow.getChildren().add(selection.indexLabel);
                    }
                } else {
                    displayFlow.getChildren().add(node.textNode);
                }
            }
            
            numberIndex++;
            lastEnd = matcher.end();
        }
        
        // Add remaining text as gray
        if (lastEnd < text.length()) {
            Text grayText = new Text(text.substring(lastEnd));
            grayText.setFill(Color.GRAY);
            displayFlow.getChildren().add(grayText);
        }
        
        lastProcessedText = text;
    }
    
    private void selectNumber(NumberNode node) {
        if (node.isHighlighted) {
            // Deselect the number
            deselectNumber(node);
            return;
        }
        
        Color color = HIGHLIGHT_COLORS[colorIndex % HIGHLIGHT_COLORS.length];
        
        // Highlight the number
        node.textNode.setFill(color);
        node.textNode.setStyle("-fx-font-weight: bold; -fx-cursor: hand; -fx-background-color: " + toHexString(color) + ";");
        node.isHighlighted = true;
        
        // Add index label
        Text indexText = new Text(" [" + currentIndex + "] ");
        indexText.setFill(Color.DARKGRAY);
        indexText.setStyle("-fx-font-size: 10px;");
        
        int position = displayFlow.getChildren().indexOf(node.textNode);
        displayFlow.getChildren().add(position + 1, indexText);
        
        // Add to selections
        NumberSelection selection = new NumberSelection(node.value, currentIndex, node, color);
        selection.indexLabel = indexText;
        selections.add(selection);
        
        // Store in map for preservation
        int nodeIndex = allNumbers.indexOf(node);
        selectionMap.put(node.value + "_" + nodeIndex, selection);
        
        // Update equation with automatic operator insertion
        if (!equationField.getText().isEmpty()) {
            equationField.setText(equationField.getText() + " " + currentOperator + " " + node.value);
        } else {
            equationField.setText(node.value);
        }
        
        currentIndex++;
        colorIndex++;
    }
    
    private void deselectNumber(NumberNode node) {
        // Find the selection for this node
        int nodeIndex = allNumbers.indexOf(node);
        String key = node.value + "_" + nodeIndex;
        NumberSelection selection = selectionMap.get(key);
        
        if (selection != null) {
            // Reset node appearance
            node.textNode.setFill(Color.BLACK);
            node.textNode.setStyle("-fx-font-weight: bold; -fx-cursor: hand;");
            node.isHighlighted = false;
            
            // Remove index label
            if (selection.indexLabel != null) {
                displayFlow.getChildren().remove(selection.indexLabel);
            }
            
            // Remove from selections
            selections.remove(selection);
            selectionMap.remove(key);
            
            // Remove from equation
            removeFromEquation(selection);
            
            // Reindex remaining selections
            reindexSelections();
        }
    }
    
    private void removeFromEquation(NumberSelection selection) {
        String equation = equationField.getText();
        String value = selection.value;
        
        // Try to find and remove the value with its operator
        String[] patterns = {
            " \\+ " + Pattern.quote(value) + " \\+ ",  // middle of equation
            " \\* " + Pattern.quote(value) + " \\* ",  // middle of equation
            " - " + Pattern.quote(value) + " - ",      // middle of equation
            " / " + Pattern.quote(value) + " / ",      // middle of equation
            " \\+ " + Pattern.quote(value) + "$",      // end with +
            " \\* " + Pattern.quote(value) + "$",      // end with *
            " - " + Pattern.quote(value) + "$",        // end with -
            " / " + Pattern.quote(value) + "$",        // end with /
            "^" + Pattern.quote(value) + " \\+ ",      // beginning with +
            "^" + Pattern.quote(value) + " \\* ",      // beginning with *
            "^" + Pattern.quote(value) + " - ",        // beginning with -
            "^" + Pattern.quote(value) + " / ",        // beginning with /
            "^" + Pattern.quote(value) + "$"           // only value
        };
        
        String[] replacements = {
            " + ", " * ", " - ", " / ",  // for middle patterns
            "", "", "", "",              // for end patterns
            "", "", "", "",              // for beginning patterns
            ""                           // for only value
        };
        
        for (int i = 0; i < patterns.length; i++) {
            String newEquation = equation.replaceFirst(patterns[i], replacements[i]);
            if (!newEquation.equals(equation)) {
                equationField.setText(newEquation.trim());
                return;
            }
        }
    }
    
    private void reindexSelections() {
        // Sort selections by their current position in the display
        List<NumberSelection> sortedSelections = new ArrayList<>(selections);
        sortedSelections.sort((a, b) -> {
            int posA = displayFlow.getChildren().indexOf(a.node.textNode);
            int posB = displayFlow.getChildren().indexOf(b.node.textNode);
            return Integer.compare(posA, posB);
        });
        
        // Update indices
        for (int i = 0; i < sortedSelections.size(); i++) {
            NumberSelection sel = sortedSelections.get(i);
            sel.index = i + 1;
            if (sel.indexLabel != null) {
                sel.indexLabel.setText(" [" + sel.index + "] ");
            }
        }
        
        // Reset currentIndex for next selection
        currentIndex = sortedSelections.size() + 1;
    }
    
    private void highlightRegexMatches() {
        // First ensure we have a valid custom pattern
        String pattern = regexField.getText().trim();
        if (pattern.isEmpty()) {
            showAlert("No Pattern", "Please enter a regex pattern.");
            return;
        }
        
        if (!useCustomRegex || customPattern == null) {
            showAlert("Invalid Pattern", "The regex pattern is invalid. Please fix it first.");
            return;
        }
        
        // Clear current selections and trigger re-processing
        clearAll();
        
        // The numbers are already filtered by the custom regex in processText
        // Just select all numeric values found
        for (NumberNode node : allNumbers) {
            selectNumber(node);
        }
        
        if (allNumbers.isEmpty()) {
            showAlert("No Matches", "No numeric values match the pattern.");
        }
    }
    
    private void applyRegexOperation() {
        // First ensure we have a valid custom pattern
        String pattern = regexField.getText().trim();
        if (pattern.isEmpty()) {
            showAlert("No Pattern", "Please enter a regex pattern.");
            return;
        }
        
        if (!useCustomRegex || customPattern == null) {
            showAlert("Invalid Pattern", "The regex pattern is invalid. Please fix it first.");
            return;
        }
        
        clearAll();
        
        // The numbers are already filtered by the custom regex in processText
        List<NumberNode> matches = new ArrayList<>(allNumbers);
        
        if (matches.isEmpty()) {
            showAlert("No Matches", "No numeric values match the pattern.");
            return;
        }
        
        String operation = operationCombo.getValue();
        if (operation == null) operation = "Add All (+)";
        
        switch (operation) {
            case "Add All (+)":
                for (int i = 0; i < matches.size(); i++) {
                    selectNumber(matches.get(i));
                    if (i < matches.size() - 1) {
                        equationField.setText(equationField.getText() + " + ");
                    }
                }
                break;
                
            case "Multiply All (×)":
                for (int i = 0; i < matches.size(); i++) {
                    selectNumber(matches.get(i));
                    if (i < matches.size() - 1) {
                        equationField.setText(equationField.getText() + " * ");
                    }
                }
                break;
                
            case "Add Sequentially":
                equationField.setText("(");
                for (int i = 0; i < matches.size(); i++) {
                    selectNumber(matches.get(i));
                    if (i < matches.size() - 1) {
                        equationField.setText(equationField.getText() + " + ");
                    }
                }
                equationField.setText(equationField.getText() + ")");
                break;
                
            case "Custom Expression":
                showCustomExpressionDialog(matches);
                break;
        }
    }
    
    private void showCustomExpressionDialog(List<NumberNode> matches) {
        TextInputDialog dialog = new TextInputDialog("(n1 + n2) * n3");
        dialog.setTitle("Custom Expression");
        dialog.setHeaderText("Enter expression using n1, n2, n3, etc.");
        dialog.setContentText("Expression:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String expression = result.get();
            
            // Replace n1, n2, etc. with actual numbers
            for (int i = 0; i < matches.size(); i++) {
                selectNumber(matches.get(i));
                expression = expression.replace("n" + (i + 1), matches.get(i).value);
            }
            
            equationField.setText(expression);
        }
    }
    
    private void saveRegexPattern() {
        String pattern = regexField.getText().trim();
        if (pattern.isEmpty()) {
            showAlert("No Pattern", "Please enter a regex pattern to save.");
            return;
        }
        
        // Check if pattern already exists
        for (NamedPattern np : savedRegexPatterns) {
            if (np.getPattern().equals(pattern)) {
                showAlert("Exists", "This pattern already exists as '" + np.getName() + "'.");
                return;
            }
        }
        
        // Ask for a name for the pattern
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Save Regex Pattern");
        dialog.setHeaderText("Name your regex pattern");
        dialog.setContentText("Pattern name:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String name = result.get().trim();
            if (name.isEmpty()) {
                showAlert("Invalid Name", "Please enter a valid name for the pattern.");
                return;
            }
            
            // Check if name already exists
            for (NamedPattern np : savedRegexPatterns) {
                if (np.getName().equals(name)) {
                    showAlert("Name Exists", "A pattern with this name already exists.");
                    return;
                }
            }
            
            savedRegexPatterns.add(new NamedPattern(name, pattern));
            saveRegexPatternsToDisk();
            showAlert("Saved", "Pattern '" + name + "' saved successfully!");
        }
    }
    
    private void deleteRegexPattern() {
        NamedPattern selected = savedRegexCombo.getValue();
        if (selected != null) {
            savedRegexPatterns.remove(selected);
            savedRegexCombo.setValue(null);
            saveRegexPatternsToDisk();
            showAlert("Deleted", "Pattern '" + selected.getName() + "' deleted successfully!");
        }
    }
    
    private void saveRegexPatternsToDisk() {
        try {
            // Ensure parent directory exists
            File file = new File(REGEX_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(REGEX_FILE))) {
                oos.writeObject(new ArrayList<NamedPattern>(savedRegexPatterns));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadRegexPatterns() {
        File file = new File(REGEX_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object obj = ois.readObject();
                if (obj instanceof List) {
                    List<?> list = (List<?>) obj;
                    if (!list.isEmpty()) {
                        Object first = list.get(0);
                        if (first instanceof String) {
                            // Old format - convert to new format
                            List<String> oldPatterns = (List<String>) obj;
                            for (int i = 0; i < oldPatterns.size(); i++) {
                                boolean isDefault = i == 0; // Make first pattern default
                                savedRegexPatterns.add(new NamedPattern("Pattern " + (i + 1), oldPatterns.get(i), isDefault));
                            }
                        } else if (first instanceof NamedPattern) {
                            // New format
                            List<NamedPattern> patterns = (List<NamedPattern>) obj;
                            savedRegexPatterns.addAll(patterns);
                        }
                    }
                }
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    
    private Button createOperatorButton(String display, String actual, boolean isDefault) {
        Button btn = HyperlinkButtonUtil.createHyperlinkButton(display);
        btn.setPrefWidth(50);
        btn.setPrefHeight(35);
        operatorButtons.put(actual, btn);
        
        if (isDefault) {
            HyperlinkButtonUtil.setButtonSelected(btn);
        }
        
        btn.setOnAction(e -> {
            // Update current operator
            currentOperator = actual;
            
            // Update button selection states
            operatorButtons.forEach((op, button) -> {
                if (!op.equals("(") && !op.equals(")")) { // Skip parentheses buttons
                    if (op.equals(actual)) {
                        HyperlinkButtonUtil.setButtonSelected(button);
                    } else {
                        HyperlinkButtonUtil.setButtonUnselected(button);
                    }
                }
            });
            
            // Also add operator to equation if there's content
            if (!equationField.getText().isEmpty() && !equationField.getText().endsWith(" ")) {
                equationField.setText(equationField.getText() + " " + actual + " ");
            }
        });
        return btn;
    }
    
    private Button createSpecialOperatorButton(String operator) {
        Button btn = HyperlinkButtonUtil.createHyperlinkButton(operator);
        btn.setPrefWidth(50);
        btn.setPrefHeight(35);
        
        btn.setOnAction(e -> {
            equationField.setText(equationField.getText() + operator);
        });
        return btn;
    }
    
    private Button createStyledButton(String text, String baseColor, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button(text);
        String baseStyle = "-fx-background-color: " + baseColor + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-border-radius: 3; -fx-background-radius: 3;";
        btn.setStyle(baseStyle);
        
        // Create hover color (lighter version of base color)
        String hoverColor = lightenColor(baseColor);
        String hoverStyle = "-fx-background-color: " + hoverColor + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-padding: 5 10; -fx-border-radius: 3; -fx-background-radius: 3;";
        
        // Add hover effects
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
        
        btn.setOnAction(action);
        return btn;
    }
    
    private String lightenColor(String hexColor) {
        // Simple color lightening - increase each RGB component
        if (!hexColor.startsWith("#")) return hexColor;
        
        try {
            int r = Integer.valueOf(hexColor.substring(1, 3), 16);
            int g = Integer.valueOf(hexColor.substring(3, 5), 16);
            int b = Integer.valueOf(hexColor.substring(5, 7), 16);
            
            // Lighten by adding 30 to each component (max 255)
            r = Math.min(255, r + 30);
            g = Math.min(255, g + 30);
            b = Math.min(255, b + 30);
            
            return String.format("#%02X%02X%02X", r, g, b);
        } catch (Exception e) {
            return hexColor; // Return original if parsing fails
        }
    }
    
    private void eraseLastSelection() {
        if (selections.isEmpty()) {
            return;
        }
        
        // Remove last selection
        NumberSelection lastSelection = selections.remove(selections.size() - 1);
        
        // Reset the number's appearance
        lastSelection.node.textNode.setFill(Color.BLACK);
        lastSelection.node.textNode.setStyle("-fx-font-weight: bold; -fx-cursor: hand;");
        lastSelection.node.isHighlighted = false;
        
        // Remove the index label
        displayFlow.getChildren().remove(lastSelection.indexLabel);
        
        // Remove from selection map
        int nodeIndex = allNumbers.indexOf(lastSelection.node);
        selectionMap.remove(lastSelection.node.value + "_" + nodeIndex);
        
        // Update indices for remaining selections
        currentIndex = 1;
        for (NumberSelection selection : selections) {
            selection.index = currentIndex++;
            selection.indexLabel.setText(" [" + selection.index + "] ");
        }
        
        // Rebuild equation
        rebuildEquation();
        
        // Update color index
        if (colorIndex > 0) {
            colorIndex--;
        }
    }
    
    private void rebuildEquation() {
        equationField.clear();
        for (int i = 0; i < selections.size(); i++) {
            NumberSelection selection = selections.get(i);
            if (i > 0) {
                equationField.setText(equationField.getText() + " " + currentOperator + " ");
            }
            equationField.setText(equationField.getText() + selection.value);
        }
    }
    
    private void clearAll() {
        selections.clear();
        selectionMap.clear();
        equationField.clear();
        resultField.clear();
        currentIndex = 1;
        colorIndex = 0;
        
        // Reset all number appearances
        for (NumberNode node : allNumbers) {
            node.textNode.setFill(Color.BLACK);
            node.textNode.setStyle("-fx-font-weight: bold; -fx-cursor: hand;");
            node.isHighlighted = false;
        }
        
        // Remove all index labels
        displayFlow.getChildren().removeIf(node -> 
            node instanceof Text && ((Text)node).getText().matches(" \\[\\d+\\] "));
    }
    
    private void calculate() {
        String equation = equationField.getText().trim();
        if (equation.isEmpty()) {
            showAlert("No Equation", "Please build an equation first.");
            return;
        }
        
        try {
            // Use JavaScript engine for evaluation
            javax.script.ScriptEngineManager manager = new javax.script.ScriptEngineManager();
            javax.script.ScriptEngine engine = manager.getEngineByName("JavaScript");
            
            if (engine == null) {
                // Fallback to Nashorn if JavaScript engine is not available
                engine = manager.getEngineByName("nashorn");
            }
            
            if (engine == null) {
                // If still null, try a simple math expression evaluator
                double result = evaluateExpression(equation);
                resultField.setText(String.format("%.2f", result));
            } else {
                Object result = engine.eval(equation);
                resultField.setText(result.toString());
            }
        } catch (Exception e) {
            showAlert("Calculation Error", "Invalid equation: " + e.getMessage());
        }
    }
    
    // Simple expression evaluator as fallback
    private double evaluateExpression(String expression) {
        return new Object() {
            int pos = -1, ch;
            
            void nextChar() {
                ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
            }
            
            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }
            
            double parse() {
                nextChar();
                double x = parseExpression();
                if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char)ch);
                return x;
            }
            
            double parseExpression() {
                double x = parseTerm();
                for (;;) {
                    if      (eat('+')) x += parseTerm();
                    else if (eat('-')) x -= parseTerm();
                    else return x;
                }
            }
            
            double parseTerm() {
                double x = parseFactor();
                for (;;) {
                    if      (eat('*')) x *= parseFactor();
                    else if (eat('/')) x /= parseFactor();
                    else return x;
                }
            }
            
            double parseFactor() {
                if (eat('+')) return +parseFactor();
                if (eat('-')) return -parseFactor();
                
                double x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    if (!eat(')')) throw new RuntimeException("Missing ')'");
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expression.substring(startPos, this.pos));
                } else {
                    throw new RuntimeException("Unexpected: " + (char)ch);
                }
                
                return x;
            }
        }.parse();
    }
    
    private void copyResult() {
        String result = resultField.getText();
        if (result.isEmpty()) {
            showAlert("No Result", "Please calculate a result first.");
            return;
        }
        
        ClipboardContent content = new ClipboardContent();
        content.putString(result);
        Clipboard.getSystemClipboard().setContent(content);
        
        showAlert("Copied", "Result copied to clipboard!");
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
            (int)(color.getRed() * 255),
            (int)(color.getGreen() * 255),
            (int)(color.getBlue() * 255));
    }
    
    private boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    private void validateRegexPattern(String pattern) {
        if (pattern == null || pattern.isEmpty()) {
            regexField.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 3;");
            useCustomRegex = false;
            customPattern = null;
            // Reprocess text with default pattern
            processText(inputArea.getText());
            return;
        }
        
        try {
            Pattern testPattern = Pattern.compile(pattern);
            // Valid regex - set green border
            regexField.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 3; -fx-background-color: #E8F5E9;");
            customPattern = testPattern;
            useCustomRegex = true;
            // Reprocess text with new pattern
            processText(inputArea.getText());
        } catch (Exception e) {
            // Invalid regex - set red border
            regexField.setStyle("-fx-border-color: #f44336; -fx-border-width: 2; -fx-border-radius: 3; -fx-background-color: #FFEBEE;");
            useCustomRegex = false;
            customPattern = null;
            // Keep using default pattern
            processText(inputArea.getText());
        }
    }
    
    private void showInputPopOut() {
        // If window already exists and is showing, bring it to front
        if (popOutStage != null && popOutStage.isShowing()) {
            popOutStage.toFront();
            popOutStage.requestFocus();
            return;
        }
        
        popOutStage = new Stage();
        popOutStage.setTitle("Calculator Input - Pop Out");
        popOutStage.setAlwaysOnTop(true); // Keep window on top
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(10));
        
        Label label = new Label("Enter or paste your text:");
        label.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        TextArea popOutTextArea = new TextArea();
        popOutTextArea.setText(inputArea.getText());
        popOutTextArea.setPrefRowCount(20);
        popOutTextArea.setPrefColumnCount(50);
        popOutTextArea.setWrapText(true);
        popOutTextArea.setStyle("-fx-font-family: monospace;");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button applyBtn = HyperlinkButtonUtil.createHyperlinkButton("Apply");
        applyBtn.setOnAction(e -> {
            inputArea.setText(popOutTextArea.getText());
            popOutStage.close();
            popOutStage = null;
        });
        
        Button cancelBtn = HyperlinkButtonUtil.createHyperlinkButton("Cancel");
        cancelBtn.setOnAction(e -> {
            popOutStage.close();
            popOutStage = null;
        });
        
        // Dialog actions group
        HBox dialogActionsGroup = HyperlinkButtonUtil.createButtonGroup(5, cancelBtn, applyBtn);
        
        buttonBox.getChildren().addAll(dialogActionsGroup);
        
        root.getChildren().addAll(label, popOutTextArea, buttonBox);
        
        Scene scene = new Scene(root, 600, 400);
        popOutStage.setScene(scene);
        
        // Handle window close button
        popOutStage.setOnCloseRequest(e -> popOutStage = null);
        
        popOutStage.show();
    }
    
    private void newRegexPattern() {
        regexField.clear();
        savedRegexCombo.setValue(null);
    }
    
    private void clearRegexPattern() {
        regexField.clear();
    }
    
    private void duplicateRegexPattern() {
        NamedPattern selected = savedRegexCombo.getValue();
        if (selected == null) {
            showAlert("No Selection", "Please select a pattern to duplicate.");
            return;
        }
        
        // Ask for a name for the duplicated pattern
        TextInputDialog dialog = new TextInputDialog(selected.getName() + " Copy");
        dialog.setTitle("Duplicate Regex Pattern");
        dialog.setHeaderText("Name for the duplicated pattern");
        dialog.setContentText("Pattern name:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String name = result.get().trim();
            if (name.isEmpty()) {
                showAlert("Invalid Name", "Please enter a valid name for the pattern.");
                return;
            }
            
            // Check if name already exists
            for (NamedPattern np : savedRegexPatterns) {
                if (np.getName().equals(name)) {
                    showAlert("Name Exists", "A pattern with this name already exists.");
                    return;
                }
            }
            
            savedRegexPatterns.add(new NamedPattern(name, selected.getPattern()));
            saveRegexPatternsToDisk();
            showAlert("Duplicated", "Pattern '" + name + "' created successfully!");
        }
    }
    
    private void renameRegexPattern() {
        NamedPattern selected = savedRegexCombo.getValue();
        if (selected == null) {
            showAlert("No Selection", "Please select a pattern to rename.");
            return;
        }
        
        // Ask for new name
        TextInputDialog dialog = new TextInputDialog(selected.getName());
        dialog.setTitle("Rename Regex Pattern");
        dialog.setHeaderText("Enter new name for the pattern");
        dialog.setContentText("Pattern name:");
        
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newName = result.get().trim();
            if (newName.isEmpty()) {
                showAlert("Invalid Name", "Please enter a valid name for the pattern.");
                return;
            }
            
            // Check if name already exists (excluding current pattern)
            for (NamedPattern np : savedRegexPatterns) {
                if (np != selected && np.getName().equals(newName)) {
                    showAlert("Name Exists", "A pattern with this name already exists.");
                    return;
                }
            }
            
            selected.setName(newName);
            saveRegexPatternsToDisk();
            savedRegexCombo.getSelectionModel().clearSelection();
            savedRegexCombo.setValue(selected);
            showAlert("Renamed", "Pattern renamed to '" + newName + "' successfully!");
        }
    }
    
    private void setDefaultRegexPattern() {
        NamedPattern selected = savedRegexCombo.getValue();
        if (selected == null) {
            showAlert("No Selection", "Please select a pattern to set as default.");
            return;
        }
        
        // Clear existing default
        for (NamedPattern np : savedRegexPatterns) {
            np.setDefault(false);
        }
        
        // Set new default
        selected.setDefault(true);
        saveRegexPatternsToDisk();
        
        // Refresh the combo box to show the updated display
        savedRegexCombo.getSelectionModel().clearSelection();
        savedRegexCombo.setValue(selected);
        
        showAlert("Default Set", "'" + selected.getName() + "' is now the default pattern.");
    }
    
    private void loadDefaultPattern() {
        // Load default pattern on startup if available
        for (NamedPattern pattern : savedRegexPatterns) {
            if (pattern.isDefault()) {
                regexField.setText(pattern.getPattern());
                savedRegexCombo.setValue(pattern);
                break;
            }
        }
    }
    
    /**
     * Adds smooth scaling animation to a button on hover
     */
    private void addSmoothHoverAnimation(Button button) {
        HyperlinkButtonUtil.addSmoothHoverAnimation(button);
    }
}