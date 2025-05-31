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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;

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
    private ComboBox<String> savedRegexCombo;
    private ComboBox<String> operationCombo;
    private ObservableList<String> savedRegexPatterns;
    private static final String REGEX_FILE = "calculator_regex_patterns.dat";
    
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
    }
    
    private VBox createTopSection() {
        VBox topSection = new VBox(10);
        topSection.setPadding(new Insets(10));
        topSection.setStyle("-fx-background-color: white; -fx-border-color: #9C27B0; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label inputLabel = new Label("Paste your text here:");
        inputLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        inputArea = new TextArea();
        inputArea.setPrefRowCount(3);
        inputArea.setPromptText("Paste text containing numbers...");
        inputArea.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1; -fx-border-radius: 3;");
        inputArea.textProperty().addListener((obs, old, text) -> processText(text));
        
        topSection.getChildren().addAll(inputLabel, inputArea);
        return topSection;
    }
    
    private VBox createMiddleSection() {
        VBox middleSection = new VBox(10);
        middleSection.setPadding(new Insets(10));
        middleSection.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-radius: 5;");
        
        Label displayLabel = new Label("Click on numbers to build your equation:");
        displayLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        ScrollPane scrollPane = new ScrollPane();
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
        
        savedRegexCombo = new ComboBox<>(savedRegexPatterns);
        savedRegexCombo.setPromptText("Saved patterns");
        savedRegexCombo.setPrefWidth(150);
        savedRegexCombo.setOnAction(e -> {
            String selected = savedRegexCombo.getValue();
            if (selected != null) {
                regexField.setText(selected);
            }
        });
        
        Button saveRegexBtn = new Button("Save");
        saveRegexBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        saveRegexBtn.setOnAction(e -> saveRegexPattern());
        
        Button deleteRegexBtn = new Button("Delete");
        deleteRegexBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        deleteRegexBtn.setOnAction(e -> deleteRegexPattern());
        
        regexControls.getChildren().addAll(regexField, savedRegexCombo, saveRegexBtn, deleteRegexBtn);
        
        HBox operationControls = new HBox(10);
        operationControls.setAlignment(Pos.CENTER_LEFT);
        
        Label operationLabel = new Label("Bulk Operation:");
        
        operationCombo = new ComboBox<>();
        operationCombo.getItems().addAll("Add All (+)", "Multiply All (×)", "Add Sequentially", "Custom Expression");
        operationCombo.setValue("Add All (+)");
        operationCombo.setPrefWidth(150);
        
        Button applyRegexBtn = new Button("Find & Apply");
        applyRegexBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        applyRegexBtn.setOnAction(e -> applyRegexOperation());
        
        Button highlightOnlyBtn = new Button("Highlight Only");
        highlightOnlyBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        highlightOnlyBtn.setOnAction(e -> highlightRegexMatches());
        
        operationControls.getChildren().addAll(operationLabel, operationCombo, applyRegexBtn, highlightOnlyBtn);
        
        regexSection.getChildren().addAll(regexLabel, regexControls, operationControls);
        return regexSection;
    }
    
    private VBox createEquationSection() {
        VBox equationSection = new VBox(10);
        equationSection.setPadding(new Insets(10));
        equationSection.setStyle("-fx-background-color: white; -fx-border-color: #2196F3; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        
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
        
        operatorBox.getChildren().addAll(plusBtn, minusBtn, multiplyBtn, divideBtn, leftParenBtn, rightParenBtn);
        
        // Control buttons
        HBox controlBox = new HBox(10);
        controlBox.setAlignment(Pos.CENTER);
        
        Button eraseBtn = new Button("Erase Last");
        eraseBtn.setStyle("-fx-background-color: #ff6b6b; -fx-text-fill: white; -fx-font-weight: bold;");
        eraseBtn.setOnAction(e -> eraseLastSelection());
        
        Button clearBtn = new Button("Clear All");
        clearBtn.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white; -fx-font-weight: bold;");
        clearBtn.setOnAction(e -> clearAll());
        
        Button calculateBtn = new Button("Calculate");
        calculateBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px;");
        calculateBtn.setPrefWidth(150);
        calculateBtn.setOnAction(e -> calculate());
        
        controlBox.getChildren().addAll(eraseBtn, clearBtn, calculateBtn);
        
        equationSection.getChildren().addAll(equationLabel, equationField, operatorBox, controlBox);
        return equationSection;
    }
    
    private HBox createResultSection() {
        HBox resultSection = new HBox(10);
        resultSection.setPadding(new Insets(15));
        resultSection.setAlignment(Pos.CENTER_LEFT);
        resultSection.setStyle("-fx-background-color: white; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        
        Label resultLabel = new Label("Result:");
        resultLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        resultField = new TextField();
        resultField.setEditable(false);
        resultField.setPrefWidth(200);
        resultField.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-background-color: #e8f5e9; -fx-border-color: #4CAF50; -fx-border-width: 1; -fx-border-radius: 3;");
        
        Button copyBtn = new Button("Copy");
        copyBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white;");
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
        
        Pattern pattern = Pattern.compile("-?\\d+\\.?\\d*");
        Matcher matcher = pattern.matcher(text);
        
        int lastEnd = 0;
        while (matcher.find()) {
            // Add non-number text as gray
            if (matcher.start() > lastEnd) {
                Text grayText = new Text(text.substring(lastEnd, matcher.start()));
                grayText.setFill(Color.GRAY);
                displayFlow.getChildren().add(grayText);
            }
            
            // Create number node
            String number = matcher.group();
            NumberNode node = new NumberNode(number, matcher.start(), matcher.end());
            
            Text numberText = new Text(number);
            numberText.setFill(Color.BLACK);
            numberText.setStyle("-fx-font-weight: bold; -fx-cursor: hand;");
            node.textNode = numberText;
            
            numberText.setOnMouseClicked(e -> selectNumber(node));
            
            // Add tooltip
            Tooltip tooltip = new Tooltip("Click to add to equation");
            Tooltip.install(numberText, tooltip);
            
            allNumbers.add(node);
            displayFlow.getChildren().add(numberText);
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
        
        // Extract all numbers from both texts
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
            return; // Already selected
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
    
    private void highlightRegexMatches() {
        String pattern = regexField.getText().trim();
        if (pattern.isEmpty()) {
            showAlert("No Pattern", "Please enter a regex pattern.");
            return;
        }
        
        clearAll();
        
        try {
            Pattern regex = Pattern.compile(pattern);
            for (NumberNode node : allNumbers) {
                if (regex.matcher(node.value).matches()) {
                    selectNumber(node);
                }
            }
        } catch (Exception e) {
            showAlert("Invalid Regex", "Invalid regex pattern: " + e.getMessage());
        }
    }
    
    private void applyRegexOperation() {
        String pattern = regexField.getText().trim();
        if (pattern.isEmpty()) {
            showAlert("No Pattern", "Please enter a regex pattern.");
            return;
        }
        
        clearAll();
        
        try {
            Pattern regex = Pattern.compile(pattern);
            List<NumberNode> matches = new ArrayList<>();
            
            for (NumberNode node : allNumbers) {
                if (regex.matcher(node.value).matches()) {
                    matches.add(node);
                }
            }
            
            if (matches.isEmpty()) {
                showAlert("No Matches", "No numbers match the pattern.");
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
            
        } catch (Exception e) {
            showAlert("Invalid Regex", "Invalid regex pattern: " + e.getMessage());
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
        
        if (!savedRegexPatterns.contains(pattern)) {
            savedRegexPatterns.add(pattern);
            saveRegexPatternsToDisk();
            showAlert("Saved", "Pattern saved successfully!");
        } else {
            showAlert("Exists", "This pattern already exists.");
        }
    }
    
    private void deleteRegexPattern() {
        String selected = savedRegexCombo.getValue();
        if (selected != null) {
            savedRegexPatterns.remove(selected);
            savedRegexCombo.setValue(null);
            saveRegexPatternsToDisk();
            showAlert("Deleted", "Pattern deleted successfully!");
        }
    }
    
    private void saveRegexPatternsToDisk() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(REGEX_FILE))) {
            oos.writeObject(new ArrayList<>(savedRegexPatterns));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadRegexPatterns() {
        File file = new File(REGEX_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                List<String> patterns = (List<String>) ois.readObject();
                savedRegexPatterns.addAll(patterns);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
    
    private Button createOperatorButton(String display, String actual, boolean isDefault) {
        Button btn = new Button(display);
        btn.setPrefWidth(50);
        btn.setPrefHeight(35);
        operatorButtons.put(actual, btn);
        
        if (isDefault) {
            btn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-border-color: #FF9800; -fx-border-width: 3; -fx-border-radius: 5;");
        } else {
            btn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        }
        
        btn.setOnAction(e -> {
            // Update current operator
            currentOperator = actual;
            
            // Update button styles
            operatorButtons.forEach((op, button) -> {
                if (op.equals(actual)) {
                    button.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-border-color: #FF9800; -fx-border-width: 3; -fx-border-radius: 5;");
                } else if (!op.equals("(") && !op.equals(")")) {
                    button.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
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
        Button btn = new Button(operator);
        btn.setPrefWidth(50);
        btn.setPrefHeight(35);
        btn.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        btn.setOnAction(e -> {
            equationField.setText(equationField.getText() + operator);
        });
        return btn;
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
}