package com.doterra.app.view;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.beans.property.SimpleStringProperty;
import java.util.regex.*;
import java.util.*;
import java.io.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.util.Callback;
import javafx.geometry.Pos;
import com.doterra.app.model.RegexTemplate;
import com.doterra.app.util.DialogUtil;
import java.io.*;
import javafx.util.StringConverter;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

public class RegexEditorPanel extends BorderPane {
    private static final String TEMPLATES_FILE = "data/regex_templates.dat";
    
    private TextArea inputTextArea;
    private TextArea templateArea;
    private TextArea outputArea;
    private TableView<PatternEntry> patternsTable;
    private ObservableList<PatternEntry> patterns;
    private ComboBox<RegexTemplate> templateComboBox;
    private List<RegexTemplate> templates;
    private RegexTemplate currentTemplate;
    
    public RegexEditorPanel() {
        patterns = FXCollections.observableArrayList();
        templates = new ArrayList<>();
        loadTemplates();
        setupUI();
        
        // Load default template if exists
        RegexTemplate defaultTemplate = templates.stream()
            .filter(RegexTemplate::isDefault)
            .findFirst()
            .orElse(null);
        
        if (defaultTemplate != null) {
            loadTemplate(defaultTemplate);
        }
    }
    
    private void setupUI() {
        setPadding(new Insets(10));
        
        // Top: Template management and input text area
        VBox topSection = new VBox(5);
        topSection.setPadding(new Insets(0, 0, 10, 0));
        
        // Template management bar
        HBox templateBar = new HBox(10);
        templateBar.setAlignment(Pos.CENTER_LEFT);
        
        Label templateLabel = new Label("Template:");
        templateComboBox = new ComboBox<>();
        templateComboBox.setConverter(new StringConverter<RegexTemplate>() {
            @Override
            public String toString(RegexTemplate template) {
                if (template == null) return "";
                return (template.isDefault() ? "★ " : "") + template.getName();
            }
            
            @Override
            public RegexTemplate fromString(String string) {
                return null;
            }
        });
        templateComboBox.setPrefWidth(200);
        templateComboBox.setItems(FXCollections.observableArrayList(templates));
        templateComboBox.setOnAction(e -> {
            RegexTemplate selected = templateComboBox.getValue();
            if (selected != null) {
                loadTemplate(selected);
            }
        });
        
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> saveCurrentTemplate());
        
        Button saveAsBtn = new Button("Save As...");
        saveAsBtn.setOnAction(e -> saveAsNewTemplate());
        
        Button renameBtn = new Button("Rename");
        renameBtn.setOnAction(e -> renameTemplate());
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> deleteTemplate());
        
        Button setDefaultBtn = new Button("Set Default");
        setDefaultBtn.setOnAction(e -> setDefaultTemplate());
        
        templateBar.getChildren().addAll(templateLabel, templateComboBox, saveBtn, saveAsBtn, renameBtn, deleteBtn, setDefaultBtn);
        
        Label inputLabel = new Label("Input Text:");
        inputTextArea = new TextArea();
        inputTextArea.setPrefRowCount(8);
        inputTextArea.setPromptText("Paste or type your raw text here...");
        
        topSection.getChildren().addAll(templateBar, inputLabel, inputTextArea);
        
        // Center: Split pane with patterns table and template editor
        SplitPane centerSplit = new SplitPane();
        centerSplit.setOrientation(javafx.geometry.Orientation.HORIZONTAL);
        
        // Left side: Patterns table
        VBox patternsSection = new VBox(5);
        patternsSection.setPadding(new Insets(5));
        
        // Pattern buttons header (replacing "Regex Patterns:" label)
        HBox patternButtons = new HBox(5);
        patternButtons.setAlignment(Pos.CENTER_LEFT);
        Button addPatternBtn = new Button("Add Pattern");
        Button removePatternBtn = new Button("Remove");
        // Make buttons same size as help button
        addPatternBtn.setStyle("-fx-font-size: 12px; -fx-padding: 2 6 2 6;");
        removePatternBtn.setStyle("-fx-font-size: 12px; -fx-padding: 2 6 2 6;");
        addPatternBtn.setOnAction(e -> addPattern());
        removePatternBtn.setOnAction(e -> removeSelectedPattern());
        patternButtons.getChildren().addAll(addPatternBtn, removePatternBtn);
        
        patternsTable = new TableView<>(patterns);
        patternsTable.setEditable(true);
        patternsTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        TableColumn<PatternEntry, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setCellFactory(createEditableTextFieldCellFactory());
        nameCol.setPrefWidth(100);
        
        TableColumn<PatternEntry, String> patternCol = new TableColumn<>("Pattern");
        patternCol.setCellValueFactory(new PropertyValueFactory<>("pattern"));
        patternCol.setCellFactory(createPatternCellFactory());
        patternCol.setPrefWidth(200);
        
        patternsTable.getColumns().addAll(nameCol, patternCol);
        VBox.setVgrow(patternsTable, Priority.ALWAYS);
        
        patternsSection.getChildren().addAll(patternButtons, patternsTable);
        
        // Right side: Template editor
        VBox templateSection = new VBox(5);
        templateSection.setPadding(new Insets(5));
        
        HBox templateHeader = new HBox(10);
        Label templateTextLabel = new Label("Output Template:");
        Button helpBtn = new Button("?");
        helpBtn.setStyle("-fx-font-size: 12px; -fx-padding: 2 6 2 6;");
        helpBtn.setOnAction(e -> showHelpDialog());
        templateHeader.getChildren().addAll(templateTextLabel, helpBtn);
        templateHeader.setAlignment(Pos.CENTER_LEFT);
        
        templateArea = new TextArea();
        templateArea.setPromptText("Click '?' button for syntax help");
        VBox.setVgrow(templateArea, Priority.ALWAYS);
        
        templateSection.getChildren().addAll(templateHeader, templateArea);
        
        centerSplit.getItems().addAll(patternsSection, templateSection);
        centerSplit.setDividerPositions(0.4);
        
        // Bottom: Output area and process button
        VBox bottomSection = new VBox(5);
        bottomSection.setPadding(new Insets(10, 0, 0, 0));
        
        // Output header with buttons on the right
        HBox outputHeader = new HBox(10);
        outputHeader.setAlignment(Pos.CENTER_LEFT);
        Label outputLabel = new Label("Output:");
        
        // Spacer to push buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button processBtn = new Button("Process");
        Button clearBtn = new Button("Clear Output");
        processBtn.setOnAction(e -> processTemplate());
        clearBtn.setOnAction(e -> outputArea.clear());
        
        outputHeader.getChildren().addAll(outputLabel, spacer, processBtn, clearBtn);
        
        outputArea = new TextArea();
        outputArea.setPrefRowCount(8);
        outputArea.setEditable(false);
        
        bottomSection.getChildren().addAll(outputHeader, outputArea);
        
        // Layout
        setTop(topSection);
        setCenter(centerSplit);
        setBottom(bottomSection);
    }
    
    private void saveCurrentTemplate() {
        if (currentTemplate == null) {
            saveAsNewTemplate();
            return;
        }
        
        updateCurrentTemplate();
        saveTemplates();
        showInfo("Template saved successfully");
    }
    
    private void saveAsNewTemplate() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Save Template");
        dialog.setHeaderText("Enter template name:");
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(dialog);
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                RegexTemplate newTemplate = new RegexTemplate(name);
                updateTemplateData(newTemplate);
                templates.add(newTemplate);
                currentTemplate = newTemplate;
                
                saveTemplates();
                refreshTemplateComboBox();
                templateComboBox.setValue(currentTemplate);
                
                showInfo("Template saved as '" + name + "'");
            }
        });
    }
    
    private void deleteTemplate() {
        if (currentTemplate == null) {
            showAlert("No template selected");
            return;
        }
        
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Template");
        confirm.setHeaderText("Delete template '" + currentTemplate.getName() + "'?");
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(confirm);
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                templates.remove(currentTemplate);
                currentTemplate = null;
                saveTemplates();
                refreshTemplateComboBox();
                clearAll();
                showInfo("Template deleted");
            }
        });
    }
    
    private void setDefaultTemplate() {
        if (currentTemplate == null) {
            showAlert("No template selected");
            return;
        }
        
        // Clear all default flags
        templates.forEach(t -> t.setDefault(false));
        
        // Set current as default
        currentTemplate.setDefault(true);
        saveTemplates();
        refreshTemplateComboBox();
        
        showInfo("'" + currentTemplate.getName() + "' set as default template");
    }
    
    private void renameTemplate() {
        if (currentTemplate == null) {
            showAlert("No template selected");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(currentTemplate.getName());
        dialog.setTitle("Rename Template");
        dialog.setHeaderText("Enter new template name:");
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(dialog);
        dialog.showAndWait().ifPresent(newName -> {
            if (!newName.trim().isEmpty() && !newName.equals(currentTemplate.getName())) {
                // Check if name already exists
                boolean nameExists = templates.stream()
                    .anyMatch(t -> t != currentTemplate && t.getName().equals(newName));
                
                if (nameExists) {
                    showAlert("A template with that name already exists");
                    return;
                }
                
                String oldName = currentTemplate.getName();
                currentTemplate.setName(newName);
                saveTemplates();
                refreshTemplateComboBox();
                templateComboBox.setValue(currentTemplate);
                
                showInfo("Template renamed from '" + oldName + "' to '" + newName + "'");
            }
        });
    }
    
    private void loadTemplate(RegexTemplate template) {
        currentTemplate = template;
        templateComboBox.setValue(template);
        
        // Clear and load patterns
        patterns.clear();
        for (RegexTemplate.PatternData pd : template.getPatterns()) {
            patterns.add(new PatternEntry(pd.getName(), pd.getPattern()));
        }
        
        // Load template text
        templateArea.setText(template.getTemplateText());
    }
    
    private void updateCurrentTemplate() {
        if (currentTemplate != null) {
            updateTemplateData(currentTemplate);
        }
    }
    
    private void updateTemplateData(RegexTemplate template) {
        // Save patterns
        List<RegexTemplate.PatternData> patternData = new ArrayList<>();
        for (PatternEntry entry : patterns) {
            patternData.add(new RegexTemplate.PatternData(entry.getName(), entry.getPattern()));
        }
        template.setPatterns(patternData);
        
        // Save template text
        template.setTemplateText(templateArea.getText());
    }
    
    private void clearAll() {
        patterns.clear();
        templateArea.clear();
        outputArea.clear();
    }
    
    private void refreshTemplateComboBox() {
        RegexTemplate selected = templateComboBox.getValue();
        templateComboBox.setItems(FXCollections.observableArrayList(templates));
        if (selected != null && templates.contains(selected)) {
            templateComboBox.setValue(selected);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadTemplates() {
        File file = new File(TEMPLATES_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                templates = (List<RegexTemplate>) ois.readObject();
            } catch (Exception e) {
                e.printStackTrace();
                templates = new ArrayList<>();
            }
        }
    }
    
    private void saveTemplates() {
        try {
            // Ensure parent directory exists
            File file = new File(TEMPLATES_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TEMPLATES_FILE))) {
                oos.writeObject(templates);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Failed to save templates: " + e.getMessage());
        }
    }
    
    private Callback<TableColumn<PatternEntry, String>, TableCell<PatternEntry, String>> createEditableTextFieldCellFactory() {
        return column -> new TableCell<PatternEntry, String>() {
            private TextField textField;
            
            @Override
            public void startEdit() {
                super.startEdit();
                if (textField == null) {
                    createTextField();
                }
                textField.setText(getString());
                setText(null);
                setGraphic(textField);
                textField.selectAll();
                textField.requestFocus();
            }
            
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getString());
                setGraphic(null);
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    if (isEditing()) {
                        if (textField != null) {
                            textField.setText(getString());
                        }
                        setText(null);
                        setGraphic(textField);
                    } else {
                        setText(getString());
                        setGraphic(null);
                    }
                }
            }
            
            private void createTextField() {
                textField = new TextField(getString());
                textField.setOnAction(e -> commitEdit(textField.getText()));
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        commitEdit(textField.getText());
                    }
                });
            }
            
            private String getString() {
                return getItem() == null ? "" : getItem();
            }
        };
    }
    
    private Callback<TableColumn<PatternEntry, String>, TableCell<PatternEntry, String>> createPatternCellFactory() {
        return column -> new TableCell<PatternEntry, String>() {
            private TextField textField;
            
            @Override
            public void startEdit() {
                super.startEdit();
                if (textField == null) {
                    createTextField();
                }
                textField.setText(getString());
                setText(null);
                setGraphic(textField);
                textField.selectAll();
                textField.requestFocus();
                validatePattern(textField.getText());
            }
            
            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getString());
                setGraphic(null);
                setStyle("");
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                } else {
                    if (isEditing()) {
                        if (textField != null) {
                            textField.setText(getString());
                            validatePattern(getString());
                        }
                        setText(null);
                        setGraphic(textField);
                    } else {
                        setText(getString());
                        setGraphic(null);
                        setStyle("");
                    }
                }
            }
            
            private void createTextField() {
                textField = new TextField(getString());
                textField.textProperty().addListener((obs, oldText, newText) -> {
                    validatePattern(newText);
                });
                textField.setOnAction(e -> {
                    if (isValidPattern(textField.getText())) {
                        commitEdit(textField.getText());
                    }
                });
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused && isValidPattern(textField.getText())) {
                        commitEdit(textField.getText());
                    }
                });
            }
            
            private void validatePattern(String pattern) {
                if (textField != null) {
                    if (isValidPattern(pattern)) {
                        textField.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 2;");
                    } else {
                        textField.setStyle("-fx-border-color: #F44336; -fx-border-width: 2;");
                    }
                }
            }
            
            private boolean isValidPattern(String pattern) {
                try {
                    Pattern.compile(pattern);
                    return true;
                } catch (PatternSyntaxException e) {
                    return false;
                }
            }
            
            private String getString() {
                return getItem() == null ? "" : getItem();
            }
        };
    }
    
    private void showHelpDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Template Syntax Help");
        dialog.setHeaderText("Regex Editor Template Commands");
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(dialog);
        
        TableView<HelpEntry> helpTable = new TableView<>();
        helpTable.setPrefSize(600, 400);
        
        TableColumn<HelpEntry, String> syntaxCol = new TableColumn<>("Syntax");
        syntaxCol.setCellValueFactory(new PropertyValueFactory<>("syntax"));
        syntaxCol.setPrefWidth(200);
        
        TableColumn<HelpEntry, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(400);
        
        helpTable.getColumns().addAll(syntaxCol, descCol);
        
        ObservableList<HelpEntry> helpEntries = FXCollections.observableArrayList(
            new HelpEntry("{pattern_name}", "Shows all matches found, comma-separated"),
            new HelpEntry("{pattern_name[0]}", "Shows specific match by index (0-based)"),
            new HelpEntry("{pattern_name[2]}", "Shows the third match (index 2)"),
            new HelpEntry("{pattern_name.group(0)}", "Shows the entire match (group 0)"),
            new HelpEntry("{pattern_name.group(1)}", "Shows capture group 1"),
            new HelpEntry("{pattern_name[0].group(1)}", "Shows capture group 1 of the first match"),
            new HelpEntry("{for pattern_name}...{/for}", "Loops through all matches"),
            new HelpEntry("{pattern_name.group(n)}", "Inside loop: shows group n of current match")
        );
        
        helpTable.setItems(helpEntries);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label exampleLabel = new Label("Example:");
        TextArea exampleArea = new TextArea();
        exampleArea.setPrefRowCount(6);
        exampleArea.setEditable(false);
        exampleArea.setText("Input: 'Email john@test.com or jane@example.org'\n" +
                          "Pattern 'emails': (\\w+)@(\\w+\\.\\w+)\n\n" +
                          "Template:\n" +
                          "{for emails}\n" +
                          "User: {emails.group(1)}, Domain: {emails.group(2)}\n" +
                          "{/for}");
        
        content.getChildren().addAll(helpTable, exampleLabel, exampleArea);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }
    
    private void addPattern() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Pattern");
        dialog.setHeaderText("Enter pattern name:");
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(dialog);
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                patterns.add(new PatternEntry(name, ".*"));
            }
        });
    }
    
    private void removeSelectedPattern() {
        PatternEntry selected = patternsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            patterns.remove(selected);
        }
    }
    
    private void processTemplate() {
        String inputText = inputTextArea.getText();
        String template = templateArea.getText();
        
        if (inputText.isEmpty() || template.isEmpty()) {
            showAlert("Please provide both input text and template");
            return;
        }
        
        try {
            Map<String, List<MatchResult>> patternMatches = new HashMap<>();
            
            // Find all matches for each pattern
            for (PatternEntry entry : patterns) {
                Pattern pattern = Pattern.compile(entry.getPattern());
                Matcher matcher = pattern.matcher(inputText);
                List<MatchResult> matches = new ArrayList<>();
                
                while (matcher.find()) {
                    matches.add(matcher.toMatchResult());
                }
                
                patternMatches.put(entry.getName(), matches);
            }
            
            // Process template
            String output = processTemplateScript(template, patternMatches);
            outputArea.setText(output);
            
        } catch (Exception e) {
            showAlert("Error processing template: " + e.getMessage());
        }
    }
    
    private String processTemplateScript(String template, Map<String, List<MatchResult>> matches) {
        StringBuilder result = new StringBuilder();
        int pos = 0;
        
        while (pos < template.length()) {
            // Handle for loops
            if (template.startsWith("{for ", pos)) {
                int endFor = template.indexOf("}", pos);
                if (endFor == -1) break;
                
                String patternName = template.substring(pos + 5, endFor).trim();
                int loopEnd = template.indexOf("{/for}", endFor);
                if (loopEnd == -1) break;
                
                String loopContent = template.substring(endFor + 1, loopEnd);
                List<MatchResult> patternMatches = matches.get(patternName);
                
                if (patternMatches != null) {
                    for (int i = 0; i < patternMatches.size(); i++) {
                        String processedLoop = processTemplateVariables(loopContent, matches, patternName, i);
                        // Remove command-only lines from loop content
                        processedLoop = removeCommandOnlyLines(processedLoop);
                        // Trim leading and trailing whitespace from each iteration
                        processedLoop = processedLoop.trim();
                        if (!processedLoop.isEmpty()) {
                            result.append(processedLoop);
                            // Add newline only if not the last iteration and content exists
                            if (i < patternMatches.size() - 1) {
                                result.append("\n");
                            }
                        }
                    }
                }
                
                pos = loopEnd + 6;
            }
            // Handle variables
            else if (template.startsWith("{", pos)) {
                int end = template.indexOf("}", pos);
                if (end == -1) {
                    result.append(template.charAt(pos));
                    pos++;
                    continue;
                }
                
                String variable = template.substring(pos + 1, end);
                result.append(processVariable(variable, matches));
                pos = end + 1;
            }
            else {
                result.append(template.charAt(pos));
                pos++;
            }
        }
        
        // Remove command-only lines from the final result
        return removeCommandOnlyLines(result.toString());
    }
    
    private String removeCommandOnlyLines(String text) {
        String[] lines = text.split("\n", -1);
        StringBuilder result = new StringBuilder();
        boolean hasContent = false;
        
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            String trimmedLine = line.trim();
            
            // Check if line contains only commands or is empty/whitespace
            boolean isCommandOnlyLine = trimmedLine.matches("\\{for\\s+\\w+\\}") || 
                                       trimmedLine.equals("{/for}") ||
                                       trimmedLine.isEmpty();
            
            // If it's not a command-only line, add it to result
            if (!isCommandOnlyLine) {
                if (hasContent) {
                    result.append("\n");
                }
                // Trim leading whitespace from content lines to remove indentation
                result.append(trimmedLine);
                hasContent = true;
            }
        }
        
        return result.toString();
    }
    
    private String processTemplateVariables(String template, Map<String, List<MatchResult>> matches, 
                                          String currentPattern, int currentIndex) {
        Pattern p = Pattern.compile("\\{" + Pattern.quote(currentPattern) + "\\.group\\((\\d+)\\)\\}");
        Matcher m = p.matcher(template);
        StringBuffer sb = new StringBuffer();
        
        while (m.find()) {
            int groupNum = Integer.parseInt(m.group(1));
            List<MatchResult> patternMatches = matches.get(currentPattern);
            String replacement = "";
            
            if (patternMatches != null && currentIndex < patternMatches.size()) {
                MatchResult match = patternMatches.get(currentIndex);
                if (groupNum <= match.groupCount()) {
                    replacement = match.group(groupNum);
                }
            }
            
            m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        m.appendTail(sb);
        
        return sb.toString();
    }
    
    private String processVariable(String variable, Map<String, List<MatchResult>> matches) {
        // Handle pattern_name[index].group(n)
        if (variable.matches("\\w+\\[\\d+\\]\\.group\\(\\d+\\)")) {
            String[] parts = variable.split("\\[|\\]\\.group\\(|\\)");
            String patternName = parts[0];
            int index = Integer.parseInt(parts[1]);
            int group = Integer.parseInt(parts[2]);
            
            List<MatchResult> patternMatches = matches.get(patternName);
            if (patternMatches != null && index < patternMatches.size()) {
                MatchResult match = patternMatches.get(index);
                if (group <= match.groupCount()) {
                    return match.group(group);
                }
            }
        }
        // Handle pattern_name[index]
        else if (variable.matches("\\w+\\[\\d+\\]")) {
            String[] parts = variable.split("\\[|\\]");
            String patternName = parts[0];
            int index = Integer.parseInt(parts[1]);
            
            List<MatchResult> patternMatches = matches.get(patternName);
            if (patternMatches != null && index < patternMatches.size()) {
                return patternMatches.get(index).group();
            }
        }
        // Handle pattern_name (all matches)
        else if (matches.containsKey(variable)) {
            List<MatchResult> patternMatches = matches.get(variable);
            StringBuilder sb = new StringBuilder();
            for (MatchResult match : patternMatches) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(match.group());
            }
            return sb.toString();
        }
        
        return "{" + variable + "}";
    }
    
    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setContentText(message);
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(alert);
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(alert);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    public static class PatternEntry {
        private final SimpleStringProperty name;
        private final SimpleStringProperty pattern;
        
        public PatternEntry(String name, String pattern) {
            this.name = new SimpleStringProperty(name);
            this.pattern = new SimpleStringProperty(pattern);
        }
        
        public String getName() { return name.get(); }
        public void setName(String value) { name.set(value); }
        public SimpleStringProperty nameProperty() { return name; }
        
        public String getPattern() { return pattern.get(); }
        public void setPattern(String value) { pattern.set(value); }
        public SimpleStringProperty patternProperty() { return pattern; }
    }
    
    public static class HelpEntry {
        private final SimpleStringProperty syntax;
        private final SimpleStringProperty description;
        
        public HelpEntry(String syntax, String description) {
            this.syntax = new SimpleStringProperty(syntax);
            this.description = new SimpleStringProperty(description);
        }
        
        public String getSyntax() { return syntax.get(); }
        public SimpleStringProperty syntaxProperty() { return syntax; }
        
        public String getDescription() { return description.get(); }
        public SimpleStringProperty descriptionProperty() { return description; }
    }
}