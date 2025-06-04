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
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Tooltip;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.Node;
import java.util.Set;
import java.util.HashSet;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.control.IndexRange;
import javafx.application.Platform;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import javafx.geometry.Point2D;
import javafx.util.Duration;

public class RegexEditorPanel extends BorderPane {
    private static final String TEMPLATES_FILE = "data/regex_templates.dat";
    private static final String PREFERENCES_FILE = "data/regex_preferences.dat";
    
    private TextArea inputTextArea;
    private CodeArea templateArea;
    private ScrollPane outputScrollPane;
    private TextFlow outputFlow;
    private TableView<PatternEntry> patternsTable;
    private ObservableList<PatternEntry> patterns;
    private ComboBox<RegexTemplate> templateComboBox;
    private List<RegexTemplate> templates;
    private RegexTemplate currentTemplate;
    private Map<String, Double> columnWidths = new HashMap<>();
    
    public RegexEditorPanel() {
        patterns = FXCollections.observableArrayList();
        templates = new ArrayList<>();
        loadTemplates();
        loadPreferences();
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
        
        Button newBtn = new Button("New");
        newBtn.setOnAction(e -> createNewTemplate());
        
        Button duplicateBtn = new Button("Duplicate");
        duplicateBtn.setOnAction(e -> duplicateTemplate());
        
        Button renameBtn = new Button("Rename");
        renameBtn.setOnAction(e -> renameTemplate());
        
        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> deleteTemplate());
        
        Button setDefaultBtn = new Button("Set Default");
        setDefaultBtn.setOnAction(e -> setDefaultTemplate());
        
        templateBar.getChildren().addAll(templateLabel, templateComboBox, saveBtn, newBtn, duplicateBtn, renameBtn, deleteBtn, setDefaultBtn);
        
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
        nameCol.setMinWidth(50);
        nameCol.setResizable(true);
        
        TableColumn<PatternEntry, String> patternCol = new TableColumn<>("Pattern");
        patternCol.setCellValueFactory(new PropertyValueFactory<>("pattern"));
        patternCol.setCellFactory(createPatternCellFactory());
        patternCol.setPrefWidth(200);
        patternCol.setMinWidth(100);
        patternCol.setResizable(true);
        
        patternsTable.getColumns().addAll(nameCol, patternCol);
        
        // Apply saved column widths
        applyColumnWidths();
        
        // Add listeners to save column widths when they change
        nameCol.widthProperty().addListener((obs, oldWidth, newWidth) -> savePreferences());
        patternCol.widthProperty().addListener((obs, oldWidth, newWidth) -> savePreferences());
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
        
        templateArea = new CodeArea();
        // CodeArea doesn't have setPromptText, we'll add a placeholder text manually
        templateArea.setParagraphGraphicFactory(null); // Remove line numbers
        // CodeArea will show existing content properly
        VBox.setVgrow(templateArea, Priority.ALWAYS);
        
        // Add real-time validation for template syntax
        templateArea.textProperty().addListener((obs, oldText, newText) -> {
            validateTemplateSyntax(newText);
        });
        
        // Style the template area to match other text areas
        templateArea.setStyle("-fx-border-color: #cccccc; -fx-border-width: 1px; -fx-font-family: 'Segoe UI', Arial, sans-serif; -fx-font-size: 14px;");
        
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
        clearBtn.setOnAction(e -> outputFlow.getChildren().clear());
        
        outputHeader.getChildren().addAll(outputLabel, spacer, processBtn, clearBtn);
        
        // Create TextFlow for clickable output
        outputFlow = new TextFlow();
        outputFlow.setPadding(new Insets(5));
        outputFlow.setMinHeight(190); // Make it fill the scroll pane minus padding
        
        outputScrollPane = new ScrollPane(outputFlow);
        outputScrollPane.setFitToWidth(true);
        outputScrollPane.setFitToHeight(true);
        outputScrollPane.setPrefViewportHeight(200);
        outputScrollPane.setMinHeight(200);
        outputScrollPane.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        
        bottomSection.getChildren().addAll(outputHeader, outputScrollPane);
        
        // Layout
        setTop(topSection);
        setCenter(centerSplit);
        setBottom(bottomSection);
    }
    
    private void saveCurrentTemplate() {
        if (currentTemplate == null) {
            createNewTemplate();
            return;
        }
        
        updateCurrentTemplate();
        saveTemplates();
        showInfo("Template saved successfully");
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
    
    private void createNewTemplate() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Template");
        dialog.setHeaderText("Enter template name:");
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(dialog);
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                // Check if name already exists
                boolean nameExists = templates.stream()
                    .anyMatch(t -> t.getName().equals(name));
                
                if (nameExists) {
                    showAlert("A template with that name already exists");
                    return;
                }
                
                RegexTemplate newTemplate = new RegexTemplate(name);
                templates.add(newTemplate);
                currentTemplate = newTemplate;
                
                // Clear the UI for the new template
                clearAll();
                
                saveTemplates();
                refreshTemplateComboBox();
                templateComboBox.setValue(currentTemplate);
                
                showInfo("New template '" + name + "' created");
            }
        });
    }
    
    private void duplicateTemplate() {
        if (currentTemplate == null) {
            showAlert("No template selected to duplicate");
            return;
        }
        
        TextInputDialog dialog = new TextInputDialog(currentTemplate.getName() + " (Copy)");
        dialog.setTitle("Duplicate Template");
        dialog.setHeaderText("Enter name for duplicate template:");
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(dialog);
        dialog.showAndWait().ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                // Check if name already exists
                boolean nameExists = templates.stream()
                    .anyMatch(t -> t.getName().equals(name));
                
                if (nameExists) {
                    showAlert("A template with that name already exists");
                    return;
                }
                
                // Create a copy of the current template
                RegexTemplate duplicateTemplate = new RegexTemplate(name);
                
                // Copy patterns from current template
                List<RegexTemplate.PatternData> patternsCopy = new ArrayList<>();
                for (RegexTemplate.PatternData pd : currentTemplate.getPatterns()) {
                    patternsCopy.add(new RegexTemplate.PatternData(pd.getName(), pd.getPattern()));
                }
                duplicateTemplate.setPatterns(patternsCopy);
                
                // Copy template text
                duplicateTemplate.setTemplateText(currentTemplate.getTemplateText());
                
                templates.add(duplicateTemplate);
                currentTemplate = duplicateTemplate;
                
                saveTemplates();
                refreshTemplateComboBox();
                templateComboBox.setValue(currentTemplate);
                
                // Load the duplicated template
                loadTemplate(currentTemplate);
                
                showInfo("Template duplicated as '" + name + "'");
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
        templateArea.replaceText(template.getTemplateText());
        
        // Validate template syntax after loading
        validateTemplateSyntax(template.getTemplateText());
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
        outputFlow.getChildren().clear();
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
                // Re-validate template syntax when patterns change
                validateTemplateSyntax(templateArea.getText());
            }
        });
    }
    
    private void removeSelectedPattern() {
        PatternEntry selected = patternsTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            patterns.remove(selected);
            // Re-validate template syntax when patterns change
            validateTemplateSyntax(templateArea.getText());
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
            
            // Process template and display with clickable links
            processTemplateWithLinks(template, patternMatches);
            
        } catch (Exception e) {
            showAlert("Error processing template: " + e.getMessage());
        }
    }
    
    private void processTemplateWithLinks(String template, Map<String, List<MatchResult>> matches) {
        outputFlow.getChildren().clear();
        
        // First, get all the matches that will be displayed
        Map<String, Set<String>> displayedMatches = new HashMap<>();
        for (Map.Entry<String, List<MatchResult>> entry : matches.entrySet()) {
            Set<String> matchTexts = new HashSet<>();
            for (MatchResult match : entry.getValue()) {
                matchTexts.add(match.group());
            }
            displayedMatches.put(entry.getKey(), matchTexts);
        }
        
        // Process the template to get the output text
        String output = processTemplateScript(template, matches);
        
        // Split the output into parts and create hyperlinks for matches
        String[] lines = output.split("\n");
        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            String line = lines[lineIndex];
            
            // Check each pattern's matches in this line
            List<Node> lineNodes = new ArrayList<>();
            int lastEnd = 0;
            
            // Create a list of all matches in this line with their positions
            List<MatchInfo> matchInfos = new ArrayList<>();
            
            for (Map.Entry<String, Set<String>> entry : displayedMatches.entrySet()) {
                for (String matchText : entry.getValue()) {
                    if (matchText.isEmpty()) continue;
                    
                    int index = 0;
                    while ((index = line.indexOf(matchText, index)) != -1) {
                        matchInfos.add(new MatchInfo(index, index + matchText.length(), matchText, entry.getKey()));
                        index += matchText.length();
                    }
                }
            }
            
            // Sort matches by start position
            matchInfos.sort((a, b) -> Integer.compare(a.start, b.start));
            
            // Remove overlapping matches (keep the first one)
            List<MatchInfo> nonOverlapping = new ArrayList<>();
            for (MatchInfo match : matchInfos) {
                if (nonOverlapping.isEmpty() || match.start >= nonOverlapping.get(nonOverlapping.size() - 1).end) {
                    nonOverlapping.add(match);
                }
            }
            
            // Create nodes for this line
            for (MatchInfo match : nonOverlapping) {
                // Add text before the match
                if (match.start > lastEnd) {
                    lineNodes.add(new Text(line.substring(lastEnd, match.start)));
                }
                
                // Create hyperlink for the match
                Hyperlink link = new Hyperlink(match.text);
                link.setStyle("-fx-text-fill: #0066cc; -fx-underline: true;");
                link.setOnAction(e -> {
                    // Copy to clipboard
                    ClipboardContent content = new ClipboardContent();
                    content.putString(match.text);
                    Clipboard.getSystemClipboard().setContent(content);
                });
                
                // Add tooltip to show which pattern matched
                Tooltip tooltip = new Tooltip("Pattern: " + match.patternName + "\nClick to copy");
                Tooltip.install(link, tooltip);
                
                lineNodes.add(link);
                lastEnd = match.end;
            }
            
            // Add remaining text
            if (lastEnd < line.length()) {
                lineNodes.add(new Text(line.substring(lastEnd)));
            }
            
            // Add line nodes to output
            outputFlow.getChildren().addAll(lineNodes);
            
            // Add newline if not the last line
            if (lineIndex < lines.length - 1) {
                outputFlow.getChildren().add(new Text("\n"));
            }
        }
    }
    
    // Helper class to track match information
    private static class MatchInfo {
        final int start;
        final int end;
        final String text;
        final String patternName;
        
        MatchInfo(int start, int end, String text, String patternName) {
            this.start = start;
            this.end = end;
            this.text = text;
            this.patternName = patternName;
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
        List<String> resultLines = new ArrayList<>();
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // Check if line contains only commands
            boolean isCommandOnlyLine = trimmedLine.matches("\\{for\\s+\\w+\\}") || 
                                       trimmedLine.equals("{/for}");
            
            // If it's not a command-only line, add it to result
            if (!isCommandOnlyLine) {
                // For empty lines, preserve them as empty lines
                if (trimmedLine.isEmpty()) {
                    resultLines.add("");
                } else {
                    // Trim leading whitespace from content lines to remove indentation
                    resultLines.add(trimmedLine);
                }
            }
        }
        
        return String.join("\n", resultLines);
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
    
    /**
     * Load UI preferences including column widths
     */
    @SuppressWarnings("unchecked")
    private void loadPreferences() {
        File file = new File(PREFERENCES_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                columnWidths = (Map<String, Double>) ois.readObject();
            } catch (Exception e) {
                columnWidths = new HashMap<>();
            }
        }
    }
    
    /**
     * Save UI preferences including column widths
     */
    private void savePreferences() {
        try {
            // Save current column widths
            for (TableColumn<PatternEntry, ?> column : patternsTable.getColumns()) {
                if (column.getText() != null && !column.getText().isEmpty()) {
                    columnWidths.put(column.getText(), column.getWidth());
                }
            }
            
            // Ensure parent directory exists
            File file = new File(PREFERENCES_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(PREFERENCES_FILE))) {
                oos.writeObject(columnWidths);
            }
        } catch (Exception e) {
            // Silently ignore preference save errors
        }
    }
    
    /**
     * Apply saved column widths to the table
     */
    private void applyColumnWidths() {
        if (columnWidths == null || columnWidths.isEmpty()) {
            return;
        }
        
        for (TableColumn<PatternEntry, ?> column : patternsTable.getColumns()) {
            String columnName = column.getText();
            if (columnName != null && columnWidths.containsKey(columnName)) {
                Double width = columnWidths.get(columnName);
                if (width != null && width > 0) {
                    column.setPrefWidth(width);
                }
            }
        }
    }
    
    /**
     * Validates template syntax and highlights invalid commands with red underlines.
     * @param templateText the template text to validate
     */
    private void validateTemplateSyntax(String templateText) {
        Platform.runLater(() -> {
            if (templateText == null || templateText.isEmpty()) {
                templateArea.clearStyle(0, templateArea.getLength());
                return;
            }
            
            // Create style spans for highlighting invalid commands
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            
            // Find all {command} patterns
            Pattern commandPattern = Pattern.compile("\\{([^}]+)\\}");
            Matcher matcher = commandPattern.matcher(templateText);
            
            int lastEnd = 0;
            
            // Clear any existing tooltips
            templateArea.setOnMouseMoved(null);
            
            while (matcher.find()) {
                String command = matcher.group(1).trim();
                int start = matcher.start();
                int end = matcher.end();
                
                // Add normal styling for text before this command
                if (start > lastEnd) {
                    spansBuilder.add(Collections.emptyList(), start - lastEnd);
                }
                
                // Add styling for the command itself
                if (!isValidCommand(command)) {
                    spansBuilder.add(Collections.singletonList("invalid-command"), end - start);
                } else {
                    spansBuilder.add(Collections.emptyList(), end - start);
                }
                
                lastEnd = end;
            }
            
            // Add normal styling for any remaining text
            if (lastEnd < templateText.length()) {
                spansBuilder.add(Collections.emptyList(), templateText.length() - lastEnd);
            }
            
            // Apply the styles
            StyleSpans<Collection<String>> styles = spansBuilder.create();
            templateArea.setStyleSpans(0, styles);
        });
    }
    
    /**
     * Checks if a command is valid according to the template syntax.
     * @param command the command text (without braces)
     * @return true if the command is valid
     */
    private boolean isValidCommand(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        
        command = command.trim();
        
        // Check for valid command patterns:
        // 1. for pattern_name
        // 2. /for
        // 3. pattern_name
        // 4. pattern_name[index]
        // 5. pattern_name.group(n)
        // 6. pattern_name[index].group(n)
        
        // Check for loop commands
        if (command.equals("/for")) {
            return true;
        }
        
        if (command.startsWith("for ")) {
            String patternName = command.substring(4).trim();
            return isValidPatternReference(patternName);
        }
        
        // Check for pattern references
        return isValidPatternReference(command);
    }
    
    /**
     * Checks if a pattern reference is valid.
     * @param reference the pattern reference
     * @return true if the reference is valid
     */
    private boolean isValidPatternReference(String reference) {
        if (reference == null || reference.trim().isEmpty()) {
            return false;
        }
        
        reference = reference.trim();
        
        // Check for valid pattern name (alphanumeric + underscore)
        String patternName;
        String remainder = "";
        
        // Extract pattern name (before [ or .)
        int bracketIndex = reference.indexOf('[');
        int dotIndex = reference.indexOf('.');
        
        if (bracketIndex == -1 && dotIndex == -1) {
            // Simple pattern name
            patternName = reference;
        } else {
            int splitIndex = -1;
            if (bracketIndex != -1 && dotIndex != -1) {
                splitIndex = Math.min(bracketIndex, dotIndex);
            } else if (bracketIndex != -1) {
                splitIndex = bracketIndex;
            } else {
                splitIndex = dotIndex;
            }
            
            patternName = reference.substring(0, splitIndex);
            remainder = reference.substring(splitIndex);
        }
        
        // Validate pattern name format
        if (!patternName.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
            return false;
        }
        
        // Check if pattern name exists in the current patterns
        boolean patternExists = patterns.stream()
            .anyMatch(pattern -> pattern.getName().equals(patternName));
        
        if (!patternExists) {
            return false;
        }
        
        // Validate remainder syntax
        if (remainder.isEmpty()) {
            return true;
        }
        
        // Check for [index] syntax
        if (remainder.startsWith("[")) {
            int closeBracket = remainder.indexOf(']');
            if (closeBracket == -1) {
                return false;
            }
            
            String indexStr = remainder.substring(1, closeBracket);
            try {
                Integer.parseInt(indexStr);
            } catch (NumberFormatException e) {
                return false;
            }
            
            remainder = remainder.substring(closeBracket + 1);
        }
        
        // Check for .group(n) syntax
        if (remainder.startsWith(".group(")) {
            int closeParen = remainder.indexOf(')');
            if (closeParen == -1 || closeParen != remainder.length() - 1) {
                return false;
            }
            
            String groupStr = remainder.substring(7, closeParen);
            try {
                Integer.parseInt(groupStr);
            } catch (NumberFormatException e) {
                return false;
            }
            
            return true;
        }
        
        return remainder.isEmpty();
    }
    
    
}
