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
import java.util.Arrays;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.TableCell;
import javafx.util.Callback;
import javafx.geometry.Pos;
import com.doterra.app.model.RegexTemplate;
import com.doterra.app.model.RegexTest;
import com.doterra.app.model.RegexTestManager;
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
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.StageStyle;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableRow;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Separator;
import javafx.geometry.Orientation;
import javafx.concurrent.Worker;
import netscape.javascript.JSObject;
import javafx.scene.input.KeyCode;

public class RegexEditorPanel extends BorderPane {
    private static final String TEMPLATES_FILE = "data/regex_templates.dat";
    private static final String PREFERENCES_FILE = "data/regex_preferences.dat";
    
    private TextArea inputTextArea;
    private CodeArea templateArea;
    private ScrollPane outputScrollPane;
    private WebView outputWebView;
    private WebEngine webEngine;
    private TableView<PatternEntry> patternsTable;
    private ObservableList<PatternEntry> patterns;
    private ComboBox<RegexTemplate> templateComboBox;
    private List<RegexTemplate> templates;
    private RegexTemplate currentTemplate;
    private Map<String, Double> columnWidths = new HashMap<>();
    private Stage popOutWindow;
    private CheckBox showNoMatchesCheckBox;
    private CheckBox debugOutputCheckBox;
    private RegexTestManager testManager;
    private TableView<RegexTest> testsTable;
    private ObservableList<RegexTest> testsList;
    
    public RegexEditorPanel() {
        patterns = FXCollections.observableArrayList();
        templates = new ArrayList<>();
        testManager = new RegexTestManager();
        testsList = FXCollections.observableArrayList();
        testsList.addAll(testManager.getTests());
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
    
    protected void setupUI() {
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
        
        // Input section with label and clear button
        HBox inputHeader = new HBox(10);
        inputHeader.setAlignment(Pos.CENTER_LEFT);
        Label inputLabel = new Label("Input Text:");
        
        // Spacer to push clear button to the right
        Region inputSpacer = new Region();
        HBox.setHgrow(inputSpacer, Priority.ALWAYS);
        
        Button clearInputBtn = new Button("Clear Input");
        clearInputBtn.setOnAction(e -> inputTextArea.clear());
        
        inputHeader.getChildren().addAll(inputLabel, inputSpacer, clearInputBtn);
        
        inputTextArea = new TextArea();
        inputTextArea.setPrefRowCount(8);
        inputTextArea.setPromptText("Paste or type your raw text here...");
        
        topSection.getChildren().addAll(templateBar, inputHeader, inputTextArea);
        
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
        
        // Add context menu to patterns table
        setupPatternsContextMenu();
        
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
        
        Button manageTestsBtn = new Button("Tests");
        manageTestsBtn.setStyle("-fx-font-size: 12px; -fx-padding: 2 6 2 6;");
        manageTestsBtn.setOnAction(e -> showTestsDialog());
        manageTestsBtn.setTooltip(new Tooltip("Manage and run tests"));
        
        templateHeader.getChildren().addAll(templateTextLabel, helpBtn, manageTestsBtn);
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
        
        // Checkbox for showing no-match patterns
        showNoMatchesCheckBox = new CheckBox("Show no matches");
        showNoMatchesCheckBox.setSelected(false);
        showNoMatchesCheckBox.setOnAction(e -> {
            // Re-process template if there's content
            if (webEngine.getDocument() != null) {
                processTemplate();
            }
        });
        
        // Checkbox for debug output
        debugOutputCheckBox = new CheckBox("Debug output");
        debugOutputCheckBox.setSelected(false);
        debugOutputCheckBox.setTooltip(new Tooltip("Enable debug output to console for regex processing"));
        
        // Spacer to push buttons to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Button processBtn = new Button("Process");
        Button clearBtn = new Button("Clear Output");
        Button popOutBtn = new Button("Pop Out");
        processBtn.setOnAction(e -> processTemplate());
        clearBtn.setOnAction(e -> webEngine.loadContent("<html><body style='font-family: Segoe UI, Arial, sans-serif; font-size: 14px; margin: 10px; background: white;'></body></html>"));
        popOutBtn.setOnAction(e -> {
            // Check if template is empty
            String template = templateArea.getText();
            if (template.isEmpty()) {
                showAlert("Please provide a template before popping out.");
                return;
            }
            
            // Process template first
            processTemplate();
            
            // Wait for the content to load, then create pop-out window
            webEngine.getLoadWorker().stateProperty().addListener(new javafx.beans.value.ChangeListener<Worker.State>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends Worker.State> obs, Worker.State oldState, Worker.State newState) {
                    if (newState == Worker.State.SUCCEEDED) {
                        // Remove this listener to avoid multiple calls
                        webEngine.getLoadWorker().stateProperty().removeListener(this);
                        // Create pop-out window after content is loaded
                        Platform.runLater(() -> createPopOutWindow());
                    }
                }
            });
        });
        
        outputHeader.getChildren().addAll(outputLabel, showNoMatchesCheckBox, debugOutputCheckBox, spacer, processBtn, clearBtn, popOutBtn);
        
        // Create WebView for selectable output with clickable links
        outputWebView = new WebView();
        webEngine = outputWebView.getEngine();
        outputWebView.setPrefHeight(200);
        outputWebView.setMinHeight(200);
        
        // Initialize with empty content
        webEngine.loadContent("<html><body style='font-family: Segoe UI, Arial, sans-serif; font-size: 14px; margin: 10px; background: white;'></body></html>");
        
        // Handle link clicks for copying to clipboard
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) webEngine.executeScript("window");
                window.setMember("javaApp", this);
                webEngine.executeScript("""
                    document.addEventListener('click', function(e) {
                        if (e.target.classList.contains('regex-match')) {
                            e.preventDefault();
                            window.javaApp.copyToClipboard(e.target.textContent);
                        }
                    });
                """);
            }
        });
        
        outputScrollPane = new ScrollPane(outputWebView);
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
        webEngine.loadContent("<html><body style='font-family: Segoe UI, Arial, sans-serif; font-size: 14px; margin: 10px; background: white;'></body></html>");
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
            new HelpEntry("{pattern_name.group(n)}", "Inside loop: shows group n of current match"),
            new HelpEntry("{if condition}...{/if}", "Conditional block (condition evaluated)"),
            new HelpEntry("{if pattern > 5}...{/if}", "Mathematical comparison"),
            new HelpEntry("{if pattern1 + pattern2 > 10}...{/if}", "Mathematical operations"),
            new HelpEntry("Math operations", "+ - * / % supported in conditions"),
            new HelpEntry("Comparisons", "< > <= >= == != supported"),
            new HelpEntry("Math functions", "abs(), sqrt(), pow(x,y), min(x,y), max(x,y)")
        );
        
        helpTable.setItems(helpEntries);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(10));
        
        Label exampleLabel = new Label("Example:");
        TextArea exampleArea = new TextArea();
        exampleArea.setPrefRowCount(10);
        exampleArea.setEditable(false);
        exampleArea.setText("Input: 'Price $50, Price $120, Price $8'\n" +
                          "Pattern 'prices': \\$(\\d+)\n\n" +
                          "Template:\n" +
                          "{for prices}\n" +
                          "Price: ${prices.group(1)}\n" +
                          "{if prices.group(1) > 100} - Premium item{/if}\n" +
                          "{if prices.group(1) < 20} - Budget item{/if}\n" +
                          "{/for}\n\n" +
                          "Math example: {if abs(prices[0].group(1) - 100) < 50}Close to $100{/if}");
        
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
        // First, get all the matches that will be displayed
        Map<String, Set<String>> displayedMatches = new HashMap<>();
        for (Map.Entry<String, List<MatchResult>> entry : matches.entrySet()) {
            Set<String> matchTexts = new HashSet<>();
            for (MatchResult match : entry.getValue()) {
                String matchText = match.group();
                if (matchText != null && !matchText.isEmpty()) {
                    matchTexts.add(matchText);
                }
            }
            displayedMatches.put(entry.getKey(), matchTexts);
        }
        
        // Process the template to get the output text
        String output = processTemplateScript(template, matches);
        
        // Process the entire output as one string to handle multiline matches
        StringBuilder htmlContent = new StringBuilder();
        int lastEnd = 0;
        
        // Create a list of all matches in the entire output with their positions
        List<MatchInfo> matchInfos = new ArrayList<>();
        
        // Create a list of all match texts sorted by length (longest first)
        List<String> allMatchTexts = new ArrayList<>();
        Map<String, String> matchToPattern = new HashMap<>();
        
        for (Map.Entry<String, Set<String>> entry : displayedMatches.entrySet()) {
            for (String matchText : entry.getValue()) {
                if (!matchText.isEmpty()) {
                    allMatchTexts.add(matchText);
                    matchToPattern.put(matchText, entry.getKey());
                }
            }
        }
        
        // Sort by length descending to find longer matches first
        allMatchTexts.sort((a, b) -> Integer.compare(b.length(), a.length()));
        
        for (String matchText : allMatchTexts) {
            int index = 0;
            while ((index = output.indexOf(matchText, index)) != -1) {
                matchInfos.add(new MatchInfo(index, index + matchText.length(), matchText, matchToPattern.get(matchText)));
                index += matchText.length();
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
        
        // Generate HTML with clickable links
        for (MatchInfo match : nonOverlapping) {
            // Add text before the match
            if (match.start > lastEnd) {
                String beforeText = output.substring(lastEnd, match.start);
                // Check if text contains HTML tags (like <i> for no matches)
                if (beforeText.contains("<i>") || beforeText.contains("</i>")) {
                    htmlContent.append(beforeText); // Don't escape HTML
                } else {
                    htmlContent.append(escapeHtml(beforeText));
                }
            }
            
            // Create clickable span for the match
            htmlContent.append("<span class='regex-match' style='color: #0066cc; text-decoration: none; cursor: pointer;' title='Pattern: ")
                      .append(escapeHtml(match.patternName))
                      .append(" - Click to copy'>")
                      .append(escapeHtml(match.text))
                      .append("</span>");
            
            lastEnd = match.end;
        }
        
        // Add remaining text
        if (lastEnd < output.length()) {
            String remainingText = output.substring(lastEnd);
            // Check if text contains HTML tags (like <i> for no matches)
            if (remainingText.contains("<i>") || remainingText.contains("</i>")) {
                htmlContent.append(remainingText); // Don't escape HTML
            } else {
                htmlContent.append(escapeHtml(remainingText));
            }
        }
        
        // Create complete HTML document
        String fullHtml = "<html><body style='font-family: Segoe UI, Arial, sans-serif; font-size: 14px; margin: 10px; background: white; white-space: pre-wrap;'>" 
                         + htmlContent.toString() 
                         + "</body></html>";
        
        // Load the HTML content into the WebView
        webEngine.loadContent(fullHtml);
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
    
    /**
     * Called from JavaScript to copy text to clipboard
     */
    public void copyToClipboard(String text) {
        ClipboardContent content = new ClipboardContent();
        content.putString(text);
        Clipboard.getSystemClipboard().setContent(content);
    }
    
    /**
     * Helper method to check if debug output is enabled
     */
    private boolean isDebugEnabled() {
        return debugOutputCheckBox != null && debugOutputCheckBox.isSelected();
    }
    
    /**
     * Helper method to print debug output only if enabled
     */
    private void debugPrint(String message) {
        if (isDebugEnabled()) {
            System.out.println(message);
        }
    }
    
    /**
     * Escapes HTML special characters
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;")
                  .replace("\n", "<br>");
    }
    
    private String processTemplateScript(String template, Map<String, List<MatchResult>> matches) {
        return processTemplateScript(template, matches, null, -1);
    }
    
    private String processTemplateScript(String template, Map<String, List<MatchResult>> matches,
                                       String currentPattern, int currentIndex) {
        StringBuilder result = new StringBuilder();
        int pos = 0;
        
        while (pos < template.length()) {
            // Handle if statements
            if (template.startsWith("{if ", pos)) {
                int endIf = template.indexOf("}", pos);
                if (endIf == -1) break;
                
                String condition = template.substring(pos + 4, endIf).trim();
                int blockEnd = findMatchingEndIf(template, endIf + 1);
                if (blockEnd == -1) break;
                
                String blockContent = template.substring(endIf + 1, blockEnd);
                
                // Evaluate the condition with current loop context
                debugPrint("DEBUG CONDITION: Evaluating '" + condition + "' with pattern=" + currentPattern + ", index=" + currentIndex);
                boolean conditionResult = evaluateCondition(condition, matches, currentPattern, currentIndex);
                debugPrint("DEBUG CONDITION: Result = " + conditionResult);
                
                if (conditionResult) {
                    // Process the block content recursively with same context
                    String processedBlock = processTemplateScript(blockContent, matches, currentPattern, currentIndex);
                    result.append(processedBlock);
                }
                
                pos = blockEnd + 5; // Skip past {/if}
            }
            // Handle for loops
            else if (template.startsWith("{for ", pos)) {
                int endFor = template.indexOf("}", pos);
                if (endFor == -1) break;
                
                String patternName = template.substring(pos + 5, endFor).trim();
                int loopEnd = template.indexOf("{/for}", endFor);
                if (loopEnd == -1) break;
                
                String loopContent = template.substring(endFor + 1, loopEnd);
                List<MatchResult> patternMatches = matches.get(patternName);
                
                if (patternMatches != null && !patternMatches.isEmpty()) {
                    for (int i = 0; i < patternMatches.size(); i++) {
                        // First, replace pattern variables for this iteration
                        String processedLoop = processTemplateVariables(loopContent, matches, patternName, i);
                        // Then, process nested template commands with the current loop context
                        processedLoop = processTemplateScript(processedLoop, matches, patternName, i);
                        // Remove command-only lines from loop content
                        processedLoop = removeCommandOnlyLines(processedLoop);
                        // Clean up excessive newlines
                        processedLoop = processedLoop.replaceAll("\n\n+", "\n");
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
                } else if (showNoMatchesCheckBox != null && showNoMatchesCheckBox.isSelected()) {
                    // Show no matches found message in italics
                    result.append("<i>No matches found for \"").append(patternName).append("\"</i>");
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
                String processed = processVariable(variable, matches, currentPattern, currentIndex);
                // Mark empty results with a special marker
                if (processed.isEmpty() || processed.equals("{" + variable + "}")) {
                    result.append("__EMPTY_PATTERN__");
                } else {
                    result.append(processed);
                }
                pos = end + 1;
            }
            else {
                result.append(template.charAt(pos));
                pos++;
            }
        }
        
        // Remove command-only lines and handle empty patterns
        return processOutputLines(result.toString());
    }
    
    /**
     * Finds the matching {/if} for a given {if} block
     */
    private int findMatchingEndIf(String template, int startPos) {
        int depth = 1;
        int pos = startPos;
        
        while (pos < template.length() && depth > 0) {
            if (template.startsWith("{if ", pos)) {
                depth++;
                pos += 4;
            } else if (template.startsWith("{/if}", pos)) {
                depth--;
                if (depth == 0) {
                    return pos;
                }
                pos += 5;
            } else {
                pos++;
            }
        }
        
        return -1; // No matching {/if} found
    }
    
    /**
     * Evaluates a condition expression with mathematical operations and comparisons
     */
    private boolean evaluateCondition(String condition, Map<String, List<MatchResult>> matches) {
        return evaluateCondition(condition, matches, null, -1);
    }
    
    /**
     * Evaluates a condition expression with mathematical operations and comparisons
     */
    private boolean evaluateCondition(String condition, Map<String, List<MatchResult>> matches,
                                    String currentPattern, int currentIndex) {
        try {
            // Replace pattern references with their numeric values
            String expression = condition;
            // First handle current pattern references if we're in a loop
            if (currentPattern != null && currentIndex >= 0) {
                // Handle pattern.group(n) syntax for current loop pattern
                Pattern groupPattern = Pattern.compile("\\b" + Pattern.quote(currentPattern) + "\\.group\\((\\d+)\\)");
                Matcher groupMatcher = groupPattern.matcher(expression);
                StringBuffer sb = new StringBuffer();
                
                while (groupMatcher.find()) {
                    int groupNum = Integer.parseInt(groupMatcher.group(1));
                    List<MatchResult> patternMatches = matches.get(currentPattern);
                    String replacement = "0";
                    
                    if (patternMatches != null && currentIndex < patternMatches.size()) {
                        MatchResult match = patternMatches.get(currentIndex);
                        if (groupNum <= match.groupCount()) {
                            String groupValue = match.group(groupNum);
                            replacement = groupValue != null ? groupValue : "0";
                        }
                    }
                    
                    groupMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                }
                groupMatcher.appendTail(sb);
                expression = sb.toString();
            }
            
            // Find all pattern references in the condition (but not pure numbers)
            Pattern patternRef = Pattern.compile("\\b([a-zA-Z]\\w*)(?:\\[(\\d+)\\])?(?:\\.group\\((\\d+)\\))?");
            Matcher matcher = patternRef.matcher(expression);
            
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String fullMatch = matcher.group();
                String patternName = matcher.group(1);
                String indexStr = matcher.group(2);
                String groupStr = matcher.group(3);
                
                debugPrint("DEBUG PATTERN: Found match '" + fullMatch + "' -> name='" + patternName + "', index='" + indexStr + "', group='" + groupStr + "'");
                
                // Skip if it's a keyword or function
                if (isKeywordOrFunction(patternName)) {
                    debugPrint("DEBUG PATTERN: Skipping keyword/function: " + patternName);
                    matcher.appendReplacement(sb, matcher.group());
                    continue;
                }
                
                // Only process if this is actually a pattern name that exists
                if (matches.containsKey(patternName)) {
                    // Get the value for this pattern reference
                    String value = getPatternValue(patternName, indexStr, groupStr, matches);
                    debugPrint("DEBUG PATTERN: Pattern '" + patternName + "' exists, value='" + value + "'");
                    
                    // Try to parse as number, otherwise use 0
                    try {
                        Double.parseDouble(value);
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
                    } catch (NumberFormatException e) {
                        // If it's not a number, use the length of the string
                        matcher.appendReplacement(sb, String.valueOf(value.length()));
                    }
                } else {
                    // Not a pattern name, keep as is
                    debugPrint("DEBUG PATTERN: Pattern '" + patternName + "' not found, keeping as-is");
                    matcher.appendReplacement(sb, matcher.group());
                }
            }
            matcher.appendTail(sb);
            expression = sb.toString();
            
            // Evaluate the mathematical expression
            debugPrint("DEBUG MATH: Final expression to evaluate: '" + expression + "'");
            boolean result = evaluateMathExpression(expression);
            debugPrint("DEBUG MATH: Result: " + result);
            return result;
            
        } catch (Exception e) {
            // If evaluation fails, return false
            return false;
        }
    }
    
    /**
     * Checks if a word is a keyword or function name
     */
    private boolean isKeywordOrFunction(String word) {
        return word.matches("abs|sqrt|pow|min|max|if|for");
    }
    
    /**
     * Gets the value of a pattern reference
     */
    private String getPatternValue(String patternName, String indexStr, String groupStr, 
                                  Map<String, List<MatchResult>> matches) {
        List<MatchResult> patternMatches = matches.get(patternName);
        if (patternMatches == null || patternMatches.isEmpty()) {
            return "0";
        }
        
        int index = 0;
        if (indexStr != null) {
            index = Integer.parseInt(indexStr);
            if (index >= patternMatches.size()) {
                return "0";
            }
        }
        
        MatchResult match = patternMatches.get(index);
        
        if (groupStr != null) {
            int group = Integer.parseInt(groupStr);
            if (group <= match.groupCount()) {
                String groupValue = match.group(group);
                return groupValue != null ? groupValue : "0";
            }
        }
        
        return match.group();
    }
    
    /**
     * Evaluates a mathematical expression with comparisons
     */
    private boolean evaluateMathExpression(String expression) {
        // Handle logical operators first (they have lower precedence)
        if (expression.contains("&&")) {
            String[] parts = expression.split("&&", 2);
            boolean left = evaluateMathExpression(parts[0].trim());
            if (!left) {
                // Short-circuit evaluation for AND
                return false;
            }
            boolean right = evaluateMathExpression(parts[1].trim());
            return left && right;
        } else if (expression.contains("||")) {
            String[] parts = expression.split("\\|\\|", 2);
            boolean left = evaluateMathExpression(parts[0].trim());
            if (left) {
                // Short-circuit evaluation for OR
                return true;
            }
            boolean right = evaluateMathExpression(parts[1].trim());
            return left || right;
        }
        
        // Handle comparison operators
        if (expression.contains("<=")) {
            String[] parts = expression.split("<=", 2);
            return evaluateArithmetic(parts[0].trim()) <= evaluateArithmetic(parts[1].trim());
        } else if (expression.contains(">=")) {
            String[] parts = expression.split(">=", 2);
            return evaluateArithmetic(parts[0].trim()) >= evaluateArithmetic(parts[1].trim());
        } else if (expression.contains("==")) {
            String[] parts = expression.split("==", 2);
            return Math.abs(evaluateArithmetic(parts[0].trim()) - evaluateArithmetic(parts[1].trim())) < 0.0001;
        } else if (expression.contains("!=")) {
            String[] parts = expression.split("!=", 2);
            return Math.abs(evaluateArithmetic(parts[0].trim()) - evaluateArithmetic(parts[1].trim())) >= 0.0001;
        } else if (expression.contains("<")) {
            String[] parts = expression.split("<", 2);
            return evaluateArithmetic(parts[0].trim()) < evaluateArithmetic(parts[1].trim());
        } else if (expression.contains(">")) {
            String[] parts = expression.split(">", 2);
            return evaluateArithmetic(parts[0].trim()) > evaluateArithmetic(parts[1].trim());
        } else {
            // No comparison operator, evaluate as boolean (non-zero is true)
            return evaluateArithmetic(expression) != 0;
        }
    }
    
    /**
     * Evaluates an arithmetic expression
     */
    private double evaluateArithmetic(String expression) {
        // Remove whitespace
        expression = expression.replaceAll("\\s+", "");
        
        // Handle functions
        expression = evaluateFunctions(expression);
        
        // Simple recursive descent parser for arithmetic
        return parseExpression(expression, 0).value;
    }
    
    /**
     * Evaluates mathematical functions in the expression
     */
    private String evaluateFunctions(String expression) {
        debugPrint("DEBUG FUNCTIONS: Input expression: '" + expression + "'");
        // Handle nested functions by processing innermost first
        boolean changed = true;
        while (changed) {
            changed = false;
            String oldExpression = expression;
            
            // Handle abs(x)
            Pattern absPattern = Pattern.compile("abs\\(([^()]+)\\)");
            Matcher absMatcher = absPattern.matcher(expression);
            StringBuffer sb = new StringBuffer();
            while (absMatcher.find()) {
                String innerExpr = absMatcher.group(1);
                debugPrint("DEBUG ABS: Found abs(" + innerExpr + ")");
                double value = parseSimpleExpression(innerExpr);
                debugPrint("DEBUG ABS: Parsed value: " + value);
                double result = Math.abs(value);
                debugPrint("DEBUG ABS: abs(" + value + ") = " + result);
                absMatcher.appendReplacement(sb, String.valueOf(result));
                changed = true;
            }
            absMatcher.appendTail(sb);
            expression = sb.toString();
            if (changed) {
                debugPrint("DEBUG ABS: Expression after abs processing: '" + expression + "'");
            }
            
            // Handle sqrt(x)
            Pattern sqrtPattern = Pattern.compile("sqrt\\(([^()]+)\\)");
            Matcher sqrtMatcher = sqrtPattern.matcher(expression);
            sb = new StringBuffer();
            while (sqrtMatcher.find()) {
                double value = parseSimpleExpression(sqrtMatcher.group(1));
                sqrtMatcher.appendReplacement(sb, String.valueOf(Math.sqrt(value)));
                changed = true;
            }
            sqrtMatcher.appendTail(sb);
            expression = sb.toString();
            
            // Handle pow(x,y)
            Pattern powPattern = Pattern.compile("pow\\(([^(),]+),([^()]+)\\)");
            Matcher powMatcher = powPattern.matcher(expression);
            sb = new StringBuffer();
            while (powMatcher.find()) {
                double x = parseSimpleExpression(powMatcher.group(1));
                double y = parseSimpleExpression(powMatcher.group(2));
                powMatcher.appendReplacement(sb, String.valueOf(Math.pow(x, y)));
                changed = true;
            }
            powMatcher.appendTail(sb);
            expression = sb.toString();
            
            // Handle min(x,y) and max(x,y)
            Pattern minMaxPattern = Pattern.compile("(min|max)\\(([^(),]+),([^()]+)\\)");
            Matcher minMaxMatcher = minMaxPattern.matcher(expression);
            sb = new StringBuffer();
            while (minMaxMatcher.find()) {
                String func = minMaxMatcher.group(1);
                double x = parseSimpleExpression(minMaxMatcher.group(2));
                double y = parseSimpleExpression(minMaxMatcher.group(3));
                double result = func.equals("min") ? Math.min(x, y) : Math.max(x, y);
                minMaxMatcher.appendReplacement(sb, String.valueOf(result));
                changed = true;
            }
            minMaxMatcher.appendTail(sb);
            expression = sb.toString();
        }
        
        return expression;
    }
    
    /**
     * Parses a simple numeric expression (just a number or negative number)
     */
    private double parseSimpleExpression(String expr) {
        expr = expr.trim();
        try {
            return Double.parseDouble(expr);
        } catch (NumberFormatException e) {
            // If it's not a simple number, try to evaluate it
            return parseExpression(expr, 0).value;
        }
    }
    
    /**
     * Parse result holder
     */
    private static class ParseResult {
        double value;
        int position;
        
        ParseResult(double value, int position) {
            this.value = value;
            this.position = position;
        }
    }
    
    /**
     * Parses an expression (handles + and -)
     */
    private ParseResult parseExpression(String expr, int pos) {
        ParseResult left = parseTerm(expr, pos);
        pos = left.position;
        
        while (pos < expr.length()) {
            char op = expr.charAt(pos);
            if (op == '+' || op == '-') {
                pos++;
                ParseResult right = parseTerm(expr, pos);
                left.value = op == '+' ? left.value + right.value : left.value - right.value;
                pos = right.position;
            } else {
                break;
            }
        }
        
        return new ParseResult(left.value, pos);
    }
    
    /**
     * Parses a term (handles *, / and %)
     */
    private ParseResult parseTerm(String expr, int pos) {
        ParseResult left = parseFactor(expr, pos);
        pos = left.position;
        
        while (pos < expr.length()) {
            char op = expr.charAt(pos);
            if (op == '*' || op == '/' || op == '%') {
                pos++;
                ParseResult right = parseFactor(expr, pos);
                if (op == '*') {
                    left.value = left.value * right.value;
                } else if (op == '/') {
                    left.value = left.value / right.value;
                } else {
                    left.value = left.value % right.value;
                }
                pos = right.position;
            } else {
                break;
            }
        }
        
        return new ParseResult(left.value, pos);
    }
    
    /**
     * Parses a factor (number or parenthesized expression)
     */
    private ParseResult parseFactor(String expr, int pos) {
        // Skip whitespace
        while (pos < expr.length() && Character.isWhitespace(expr.charAt(pos))) {
            pos++;
        }
        
        if (pos >= expr.length()) {
            return new ParseResult(0, pos);
        }
        
        // Handle negative numbers
        boolean negative = false;
        if (expr.charAt(pos) == '-') {
            negative = true;
            pos++;
        }
        
        // Handle parentheses
        if (expr.charAt(pos) == '(') {
            pos++; // Skip '('
            ParseResult result = parseExpression(expr, pos);
            pos = result.position;
            if (pos < expr.length() && expr.charAt(pos) == ')') {
                pos++; // Skip ')'
            }
            return new ParseResult(negative ? -result.value : result.value, pos);
        }
        
        // Parse number
        int start = pos;
        while (pos < expr.length() && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.')) {
            pos++;
        }
        
        if (start == pos) {
            return new ParseResult(0, pos);
        }
        
        double value = Double.parseDouble(expr.substring(start, pos));
        return new ParseResult(negative ? -value : value, pos);
    }

    private String removeCommandOnlyLines(String text) {
        String[] lines = text.split("\n", -1);
        List<String> resultLines = new ArrayList<>();
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // Check if line contains only commands
            boolean isCommandOnlyLine = trimmedLine.matches("\\{for\\s+\\w+\\}") || 
                                       trimmedLine.equals("{/for}") ||
                                       trimmedLine.matches("\\{if\\s+.+\\}") ||
                                       trimmedLine.equals("{/if}");
            
            // If it's not a command-only line, add it to result
            if (!isCommandOnlyLine) {
                // Preserve the line structure
                resultLines.add(line);
            }
        }
        
        return String.join("\n", resultLines);
    }
    
    private String processOutputLines(String text) {
        String[] lines = text.split("\n", -1);
        List<String> resultLines = new ArrayList<>();
        
        for (String line : lines) {
            // Check if this line contains only an empty pattern marker
            if (line.trim().equals("__EMPTY_PATTERN__")) {
                // Skip this line entirely
                continue;
            }
            
            // Replace empty pattern markers with empty string
            String processedLine = line.replace("__EMPTY_PATTERN__", "");
            
            // Always add the line to preserve user's formatting
            resultLines.add(processedLine);
        }
        
        return String.join("\n", resultLines);
    }
    
    private String processTemplateVariables(String template, Map<String, List<MatchResult>> matches, 
                                          String currentPattern, int currentIndex) {
        // First handle variables in braces like {pattern.group(n)}
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
        
        // Also handle pattern references in conditions like {if pattern.group(n) > 100}
        String result = sb.toString();
        Pattern conditionPattern = Pattern.compile("(\\{if\\s+[^}]*)" + Pattern.quote(currentPattern) + "\\.group\\((\\d+)\\)([^}]*\\})");
        Matcher conditionMatcher = conditionPattern.matcher(result);
        StringBuffer conditionSb = new StringBuffer();
        
        while (conditionMatcher.find()) {
            int groupNum = Integer.parseInt(conditionMatcher.group(2));
            List<MatchResult> patternMatches = matches.get(currentPattern);
            String replacement = "0";
            
            if (patternMatches != null && currentIndex < patternMatches.size()) {
                MatchResult match = patternMatches.get(currentIndex);
                if (groupNum <= match.groupCount()) {
                    String groupValue = match.group(groupNum);
                    replacement = groupValue != null ? groupValue : "0";
                }
            }
            
            String fullReplacement = conditionMatcher.group(1) + replacement + conditionMatcher.group(3);
            conditionMatcher.appendReplacement(conditionSb, Matcher.quoteReplacement(fullReplacement));
        }
        conditionMatcher.appendTail(conditionSb);
        
        return conditionSb.toString();
    }
    
    private String processVariable(String variable, Map<String, List<MatchResult>> matches) {
        return processVariable(variable, matches, null, -1);
    }
    
    private String processVariable(String variable, Map<String, List<MatchResult>> matches,
                                 String currentPattern, int currentIndex) {
        // Handle current pattern group references in loops
        if (currentPattern != null && currentIndex >= 0 &&
            variable.matches(Pattern.quote(currentPattern) + "\\.group\\(\\d+\\)")) {
            
            String[] parts = variable.split("\\.group\\(|\\)");
            int group = Integer.parseInt(parts[1]);
            
            List<MatchResult> patternMatches = matches.get(currentPattern);
            if (patternMatches != null && currentIndex < patternMatches.size()) {
                MatchResult match = patternMatches.get(currentIndex);
                if (group <= match.groupCount()) {
                    String value = match.group(group);
                    return value != null ? value : "";
                }
            }
            return "";
        }
        
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
        // Handle pattern_name.group(n) - for all matches concatenated
        else if (variable.matches("\\w+\\.group\\(\\d+\\)")) {
            String[] parts = variable.split("\\.group\\(|\\)");
            String patternName = parts[0];
            int group = Integer.parseInt(parts[1]);
            
            List<MatchResult> patternMatches = matches.get(patternName);
            if (patternMatches != null) {
                if (patternMatches.isEmpty()) {
                    if (showNoMatchesCheckBox != null && showNoMatchesCheckBox.isSelected()) {
                        return "<i>No matches found for \"" + patternName + "\"</i>";
                    } else {
                        return ""; // Return empty string for no matches when checkbox is unchecked
                    }
                }
                StringBuilder sb = new StringBuilder();
                for (MatchResult match : patternMatches) {
                    if (sb.length() > 0) sb.append(", ");
                    if (group <= match.groupCount()) {
                        sb.append(match.group(group));
                    }
                }
                return sb.toString();
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
            if (patternMatches.isEmpty()) {
                if (showNoMatchesCheckBox != null && showNoMatchesCheckBox.isSelected()) {
                    return "<i>No matches found for \"" + variable + "\"</i>";
                } else {
                    return ""; // Return empty string for no matches when checkbox is unchecked
                }
            }
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
     * Creates a detached pop-out window with the current output content
     */
    private void createPopOutWindow() {
        // Check if there's content to pop out
        if (webEngine.getDocument() == null) {
            showAlert("No output to pop out.");
            return;
        }
        
        // Check if the output has actual content (not just the default empty page)
        try {
            String bodyContent = (String) webEngine.executeScript("document.body.textContent.trim()");
            if (bodyContent == null || bodyContent.isEmpty()) {
                showAlert("No output to pop out. Make sure you have input text and valid patterns.");
                return;
            }
        } catch (Exception ex) {
            showAlert("Error creating pop-out window.");
            return;
        }
        
        // Close existing pop-out window if it exists
        if (popOutWindow != null) {
            popOutWindow.close();
        }
        
        // Create new stage for pop-out window
        popOutWindow = new Stage();
        popOutWindow.setTitle("Regex Output");
        popOutWindow.setAlwaysOnTop(true);
        
        // Create a new WebView with the same content
        WebView popOutWebView = new WebView();
        WebEngine popOutEngine = popOutWebView.getEngine();
        
        // Copy the current HTML content to the new WebView
        String currentContent = (String) webEngine.executeScript("document.documentElement.outerHTML");
        popOutEngine.loadContent(currentContent);
        
        // Set up clipboard functionality for the pop-out window
        popOutEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                JSObject window = (JSObject) popOutEngine.executeScript("window");
                window.setMember("javaApp", this);
                popOutEngine.executeScript("""
                    document.addEventListener('click', function(e) {
                        if (e.target.classList.contains('regex-match')) {
                            e.preventDefault();
                            window.javaApp.copyToClipboard(e.target.textContent);
                        }
                    });
                """);
            }
        });
        
        // Create scroll pane for the content
        ScrollPane scrollPane = new ScrollPane(popOutWebView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setStyle("-fx-background-color: white; -fx-border-color: #cccccc; -fx-border-width: 1px;");
        
        // Set reasonable window size
        double windowHeight = 400;
        
        // Create scene
        Scene scene = new Scene(scrollPane, 800, windowHeight);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        popOutWindow.setScene(scene);
        
        // Show the window
        popOutWindow.show();
        
        // Position window at top center of screen after showing
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double centerX = (screenBounds.getWidth() - popOutWindow.getWidth()) / 2;
        popOutWindow.setX(centerX);
        popOutWindow.setY(screenBounds.getMinY() + 30); // 30px from top
        
        // Handle window closing
        popOutWindow.setOnCloseRequest(e -> popOutWindow = null);
    }
    
    
    /**
     * Sets up context menu for the patterns table
     */
    private void setupPatternsContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem openEditorItem = new MenuItem("Open Editor");
        openEditorItem.setOnAction(e -> {
            PatternEntry selected = patternsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                openRegexEditor(selected);
            }
        });
        
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            PatternEntry selected = patternsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                patterns.remove(selected);
                // Re-validate template syntax when patterns change
                validateTemplateSyntax(templateArea.getText());
            }
        });
        
        contextMenu.getItems().addAll(openEditorItem, deleteItem);
        
        // Set context menu to show only when right-clicking on a row
        patternsTable.setRowFactory(tv -> {
            TableRow<PatternEntry> row = new TableRow<>();
            row.setOnContextMenuRequested(event -> {
                if (!row.isEmpty()) {
                    patternsTable.getSelectionModel().select(row.getItem());
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });
    }
    
    /**
     * Opens a dedicated regex editor window for better pattern editing
     */
    private void openRegexEditor(PatternEntry pattern) {
        Stage editorStage = new Stage();
        editorStage.setTitle("Regex Editor - " + pattern.getName());
        editorStage.setAlwaysOnTop(true);
        
        VBox root = new VBox(10);
        root.setPadding(new Insets(15));
        
        // Pattern name field
        Label nameLabel = new Label("Pattern Name:");
        TextField nameField = new TextField(pattern.getName());
        nameField.setPrefWidth(400);
        
        // Pattern regex field with larger area
        Label regexLabel = new Label("Regular Expression:");
        TextArea regexArea = new TextArea(pattern.getPattern());
        regexArea.setPrefRowCount(8);
        regexArea.setPrefWidth(400);
        regexArea.setWrapText(true);
        regexArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 14px;");
        
        // Validation feedback
        Label validationLabel = new Label();
        validationLabel.setStyle("-fx-text-fill: red;");
        
        // Real-time validation
        regexArea.textProperty().addListener((obs, oldText, newText) -> {
            try {
                Pattern.compile(newText);
                validationLabel.setText("✓ Valid regex pattern");
                validationLabel.setStyle("-fx-text-fill: green;");
                regexArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 14px; -fx-border-color: #4CAF50; -fx-border-width: 1px;");
            } catch (PatternSyntaxException e) {
                validationLabel.setText("✗ Invalid regex: " + e.getDescription());
                validationLabel.setStyle("-fx-text-fill: red;");
                regexArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 14px; -fx-border-color: #F44336; -fx-border-width: 1px;");
            }
        });
        
        // Trigger initial validation
        regexArea.textProperty().setValue(regexArea.getText());
        
        // Test area
        Label testLabel = new Label("Test Text (optional):");
        TextArea testArea = new TextArea();
        testArea.setPrefRowCount(3);
        testArea.setPromptText("Enter text to test the regex pattern against...");
        
        // Test results
        Label resultsLabel = new Label("Test Results:");
        TextArea resultsArea = new TextArea();
        resultsArea.setPrefRowCount(4);
        resultsArea.setEditable(false);
        resultsArea.setStyle("-fx-background-color: #f8f8f8;");
        
        // Test button
        Button testButton = new Button("Test Pattern");
        testButton.setOnAction(e -> {
            String regexText = regexArea.getText();
            String testText = testArea.getText();
            
            if (regexText.isEmpty() || testText.isEmpty()) {
                resultsArea.setText("Please enter both a regex pattern and test text.");
                return;
            }
            
            try {
                Pattern testPattern = Pattern.compile(regexText);
                Matcher matcher = testPattern.matcher(testText);
                StringBuilder results = new StringBuilder();
                
                int matchCount = 0;
                while (matcher.find()) {
                    matchCount++;
                    results.append("Match ").append(matchCount).append(": \"").append(matcher.group()).append("\"\n");
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        results.append("  Group ").append(i).append(": \"").append(matcher.group(i)).append("\"\n");
                    }
                    results.append("\n");
                }
                
                if (matchCount == 0) {
                    results.append("No matches found.");
                }
                
                resultsArea.setText(results.toString());
            } catch (PatternSyntaxException ex) {
                resultsArea.setText("Error: " + ex.getDescription());
            }
        });
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        
        Button saveButton = new Button("Save");
        saveButton.setOnAction(e -> {
            try {
                Pattern.compile(regexArea.getText()); // Validate before saving
                pattern.setName(nameField.getText());
                pattern.setPattern(regexArea.getText());
                patternsTable.refresh();
                // Re-validate template syntax when patterns change
                validateTemplateSyntax(templateArea.getText());
                editorStage.close();
            } catch (PatternSyntaxException ex) {
                showAlert("Cannot save invalid regex pattern: " + ex.getDescription());
            }
        });
        
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> editorStage.close());
        
        buttonBox.getChildren().addAll(cancelButton, saveButton);
        
        root.getChildren().addAll(
            nameLabel, nameField,
            regexLabel, regexArea, validationLabel,
            testLabel, testArea, testButton,
            resultsLabel, resultsArea,
            buttonBox
        );
        
        Scene scene = new Scene(root, 450, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        editorStage.setScene(scene);
        
        editorStage.show();
        
        // Position window at top center of screen after showing
        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
        double centerX = (screenBounds.getWidth() - editorStage.getWidth()) / 2;
        editorStage.setX(centerX);
        editorStage.setY(screenBounds.getMinY() + 30); // 30px from top
    }
    
    /**
     * Validates template syntax and highlights invalid commands with red underlines.
     * @param templateText the template text to validate
     */
    private void validateTemplateSyntax(String templateText) {
        Platform.runLater(() -> {
            // Check if templateArea is still available and content hasn't changed
            if (templateArea == null || templateText == null || templateText.isEmpty()) {
                if (templateArea != null && templateArea.getLength() > 0) {
                    templateArea.clearStyle(0, templateArea.getLength());
                }
                return;
            }
            
            // Get current content to ensure we're applying styles to the right text
            String currentContent = templateArea.getText();
            if (!templateText.equals(currentContent)) {
                // Content has changed since validation was scheduled, skip this update
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
            
            // Apply the styles with additional safety check
            try {
                StyleSpans<Collection<String>> styles = spansBuilder.create();
                // Double-check that content length matches expected length
                if (templateArea.getLength() == templateText.length()) {
                    templateArea.setStyleSpans(0, styles);
                }
            } catch (IndexOutOfBoundsException e) {
                // Ignore styling errors during content changes
                System.out.println("DEBUG: Skipping template validation due to content change");
            }
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
        // 3. if condition
        // 4. /if
        // 5. pattern_name
        // 6. pattern_name[index]
        // 7. pattern_name.group(n)
        // 8. pattern_name[index].group(n)
        
        // Check for loop commands
        if (command.equals("/for")) {
            return true;
        }
        
        if (command.startsWith("for ")) {
            String patternName = command.substring(4).trim();
            return isValidPatternReference(patternName);
        }
        
        // Check for if commands
        if (command.equals("/if")) {
            return true;
        }
        
        if (command.startsWith("if ")) {
            // For now, we'll consider any if condition as valid
            // More sophisticated validation could be added here
            return true;
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
        
        // Check for .group(n) syntax without index (e.g., pattern_name.group(0))
        if (remainder.isEmpty() && reference.matches("\\w+\\.group\\(\\d+\\)")) {
            return isValidPatternReference(reference);
        }
        
        return remainder.isEmpty();
    }
    
    // Test case data structure (kept for compatibility with existing test result display)
    private static class TestCase {
        final String name;
        final String input;
        final String patternName;
        final String patternRegex;
        final String template;
        final String expectedOutput;
        
        TestCase(String name, String input, String patternName, String patternRegex, String template, String expectedOutput) {
            this.name = name;
            this.input = input;
            this.patternName = patternName;
            this.patternRegex = patternRegex;
            this.template = template;
            this.expectedOutput = expectedOutput.trim();
        }
    }
    
    // Test result data structure
    private static class TestResult {
        final TestCase testCase;
        final boolean passed;
        final String actualOutput;
        final String debugOutput;
        final String errorMessage;
        
        TestResult(TestCase testCase, boolean passed, String actualOutput, String debugOutput, String errorMessage) {
            this.testCase = testCase;
            this.passed = passed;
            this.actualOutput = actualOutput;
            this.debugOutput = debugOutput;
            this.errorMessage = errorMessage;
        }
    }
    
    /**
     * Shows the test management dialog
     */
    private void showTestsDialog() {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Regex Tests Manager");
        dialog.setHeaderText("Manage and run regex tests");
        
        DialogUtil.configureDialog(dialog);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setPrefWidth(900);
        content.setPrefHeight(600);
        
        // Test management buttons
        HBox buttonBar = new HBox(10);
        Button addBtn = new Button("Add Test");
        Button editBtn = new Button("Edit");
        Button deleteBtn = new Button("Delete");
        Button duplicateBtn = new Button("Duplicate");
        Button runSelectedBtn = new Button("Run Selected");
        Button runAllBtn = new Button("Run All");
        
        editBtn.setDisable(true);
        deleteBtn.setDisable(true);
        duplicateBtn.setDisable(true);
        runSelectedBtn.setDisable(true);
        
        buttonBar.getChildren().addAll(addBtn, editBtn, deleteBtn, duplicateBtn, 
                                       new Separator(Orientation.VERTICAL),
                                       runSelectedBtn, runAllBtn);
        
        // Tests table
        testsTable = new TableView<>();
        testsTable.setPrefHeight(400);
        
        TableColumn<RegexTest, String> nameCol = new TableColumn<>("Test Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        nameCol.setPrefWidth(200);
        
        TableColumn<RegexTest, String> inputCol = new TableColumn<>("Input");
        inputCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getInput()));
        inputCol.setPrefWidth(200);
        
        TableColumn<RegexTest, String> patternCol = new TableColumn<>("Patterns");
        patternCol.setCellValueFactory(data -> {
            StringBuilder patternsStr = new StringBuilder();
            for (RegexTest.PatternEntry pattern : data.getValue().getPatterns()) {
                if (patternsStr.length() > 0) patternsStr.append("; ");
                patternsStr.append(pattern.getName()).append(": ").append(pattern.getRegex());
            }
            return new SimpleStringProperty(patternsStr.toString());
        });
        patternCol.setCellFactory(column -> {
            return new TableCell<RegexTest, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        
                        // Check if all patterns in this test are valid
                        RegexTest test = getTableView().getItems().get(getIndex());
                        boolean allValid = test.getPatterns().stream()
                            .allMatch(p -> isValidRegex(p.getRegex()));
                        
                        if (allValid) {
                            setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;");
                        } else {
                            setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828;");
                        }
                    }
                }
            };
        });
        patternCol.setPrefWidth(250);
        
        TableColumn<RegexTest, String> expectedCol = new TableColumn<>("Expected Output");
        expectedCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getExpectedOutput()));
        expectedCol.setPrefWidth(200);
        
        testsTable.getColumns().addAll(nameCol, inputCol, patternCol, expectedCol);
        
        // Load tests
        testsList.clear();
        testsList.addAll(testManager.getTests());
        testsTable.setItems(testsList);
        
        // Enable/disable buttons based on selection
        testsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            boolean hasSelection = newSel != null;
            editBtn.setDisable(!hasSelection);
            deleteBtn.setDisable(!hasSelection);
            duplicateBtn.setDisable(!hasSelection);
            runSelectedBtn.setDisable(!hasSelection);
        });
        
        // Button actions
        addBtn.setOnAction(e -> {
            RegexTest newTest = showTestEditDialog(null);
            if (newTest != null) {
                testManager.addTest(newTest);
                testsList.add(newTest);
            }
        });
        
        editBtn.setOnAction(e -> {
            RegexTest selected = testsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                RegexTest edited = showTestEditDialog(selected);
                if (edited != null) {
                    testManager.updateTest(selected.getId(), edited);
                    int index = testsList.indexOf(selected);
                    testsList.set(index, edited);
                }
            }
        });
        
        deleteBtn.setOnAction(e -> {
            RegexTest selected = testsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Delete Test");
                confirm.setHeaderText("Delete test: " + selected.getName() + "?");
                confirm.setContentText("This action cannot be undone.");
                DialogUtil.configureDialog(confirm);
                
                if (confirm.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
                    testManager.deleteTest(selected.getId());
                    testsList.remove(selected);
                }
            }
        });
        
        duplicateBtn.setOnAction(e -> {
            RegexTest selected = testsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                RegexTest copy = selected.copy();
                testManager.addTest(copy);
                testsList.add(copy);
            }
        });
        
        runSelectedBtn.setOnAction(e -> {
            RegexTest selected = testsTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                runTests(Arrays.asList(selected));
            }
        });
        
        runAllBtn.setOnAction(e -> {
            if (!testsList.isEmpty()) {
                runTests(new ArrayList<>(testsList));
            }
        });
        
        // Add double-click to edit
        testsTable.setRowFactory(tv -> {
            TableRow<RegexTest> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    RegexTest test = row.getItem();
                    RegexTest edited = showTestEditDialog(test);
                    if (edited != null) {
                        testManager.updateTest(test.getId(), edited);
                        int index = testsList.indexOf(test);
                        testsList.set(index, edited);
                    }
                }
            });
            return row;
        });
        
        content.getChildren().addAll(buttonBar, testsTable);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
    /**
     * Shows dialog to add/edit a test
     */
    private RegexTest showTestEditDialog(RegexTest existingTest) {
        Dialog<RegexTest> dialog = new Dialog<>();
        dialog.setTitle(existingTest == null ? "Add New Test" : "Edit Test");
        dialog.setHeaderText(null);
        
        DialogUtil.configureDialog(dialog);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(600);
        
        // Test name
        TextField nameField = new TextField(existingTest != null ? existingTest.getName() : "");
        nameField.setPrefWidth(400);
        
        // Input text
        TextArea inputArea = new TextArea(existingTest != null ? existingTest.getInput() : "");
        inputArea.setPrefRowCount(3);
        inputArea.setWrapText(true);
        
        // Patterns section
        VBox patternsSection = new VBox(5);
        Label patternsLabel = new Label("Patterns:");
        
        // Pattern list
        ObservableList<RegexTest.PatternEntry> patternsList = FXCollections.observableArrayList();
        if (existingTest != null && !existingTest.getPatterns().isEmpty()) {
            for (RegexTest.PatternEntry pattern : existingTest.getPatterns()) {
                patternsList.add(new RegexTest.PatternEntry(pattern.getName(), pattern.getRegex()));
            }
        } else {
            patternsList.add(new RegexTest.PatternEntry("pattern1", ""));
        }
        
        // Create a holder for the validation function that will be initialized later
        final Runnable[] validateFieldsHolder = new Runnable[1];
        
        TableView<RegexTest.PatternEntry> patternsTable = new TableView<>(patternsList);
        patternsTable.setPrefHeight(150);
        patternsTable.setEditable(true);
        
        TableColumn<RegexTest.PatternEntry, String> patternNameCol = new TableColumn<>("Name");
        patternNameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        patternNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        patternNameCol.setOnEditCommit(event -> {
            event.getRowValue().setName(event.getNewValue());
            if (validateFieldsHolder[0] != null) {
                validateFieldsHolder[0].run();
            }
        });
        patternNameCol.setPrefWidth(150);
        
        TableColumn<RegexTest.PatternEntry, String> patternRegexCol = new TableColumn<>("Regex");
        patternRegexCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRegex()));
        patternRegexCol.setCellFactory(column -> {
            return new TableCell<RegexTest.PatternEntry, String>() {
                private TextField textField;
                
                @Override
                public void startEdit() {
                    super.startEdit();
                    if (textField == null) {
                        createTextField();
                    }
                    setText(null);
                    setGraphic(textField);
                    textField.selectAll();
                    textField.requestFocus();
                }
                
                @Override
                public void cancelEdit() {
                    super.cancelEdit();
                    setText(getItem());
                    setGraphic(null);
                    updateStyle(getItem());
                }
                
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                        setStyle("");
                    } else {
                        if (isEditing()) {
                            if (textField != null) {
                                textField.setText(item);
                            }
                            setText(null);
                            setGraphic(textField);
                        } else {
                            setText(item);
                            setGraphic(null);
                            updateStyle(item);
                        }
                    }
                }
                
                private void createTextField() {
                    textField = new TextField(getItem());
                    textField.setOnKeyPressed(event -> {
                        if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                            commitEdit(textField.getText());
                            event.consume();
                        } else if (event.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                            cancelEdit();
                            event.consume();
                        }
                    });
                    
                    // Real-time validation while typing
                    textField.textProperty().addListener((obs, oldText, newText) -> {
                        if (isValidRegex(newText)) {
                            textField.setStyle("-fx-control-inner-background: #e8f5e9; -fx-border-color: #4caf50; -fx-border-width: 1px;");
                        } else {
                            textField.setStyle("-fx-control-inner-background: #ffebee; -fx-border-color: #f44336; -fx-border-width: 1px;");
                        }
                    });
                    
                    textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                        if (!isNowFocused) {
                            commitEdit(textField.getText());
                        }
                    });
                }
                
                private void updateStyle(String regex) {
                    if (regex == null || regex.trim().isEmpty()) {
                        setStyle("");
                    } else if (isValidRegex(regex)) {
                        setStyle("-fx-background-color: #e8f5e9; -fx-text-fill: #2e7d32;");
                    } else {
                        setStyle("-fx-background-color: #ffebee; -fx-text-fill: #c62828;");
                    }
                }
                
                private boolean isValidRegex(String regex) {
                    if (regex == null || regex.trim().isEmpty()) {
                        return true; // Empty is considered valid (not yet entered)
                    }
                    try {
                        Pattern.compile(regex);
                        return true;
                    } catch (PatternSyntaxException e) {
                        return false;
                    }
                }
            };
        });
        patternRegexCol.setOnEditCommit(event -> {
            event.getRowValue().setRegex(event.getNewValue());
            if (validateFieldsHolder[0] != null) {
                validateFieldsHolder[0].run();
            }
        });
        patternRegexCol.setPrefWidth(400);
        
        patternsTable.getColumns().addAll(patternNameCol, patternRegexCol);
        
        // Pattern buttons
        HBox patternButtons = new HBox(5);
        Button addPatternBtn = new Button("Add Pattern");
        Button removePatternBtn = new Button("Remove Pattern");
        removePatternBtn.setDisable(true);
        
        addPatternBtn.setOnAction(e -> {
            patternsList.add(new RegexTest.PatternEntry("pattern" + (patternsList.size() + 1), ""));
        });
        
        removePatternBtn.setOnAction(e -> {
            RegexTest.PatternEntry selected = patternsTable.getSelectionModel().getSelectedItem();
            if (selected != null && patternsList.size() > 1) {
                patternsList.remove(selected);
            }
        });
        
        patternsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            removePatternBtn.setDisable(newSel == null || patternsList.size() <= 1);
        });
        
        patternButtons.getChildren().addAll(addPatternBtn, removePatternBtn);
        patternsSection.getChildren().addAll(patternsLabel, patternsTable, patternButtons);
        
        // Template
        TextArea templateArea = new TextArea(existingTest != null ? existingTest.getTemplate() : "");
        templateArea.setPrefRowCount(5);
        templateArea.setWrapText(true);
        
        // Expected output
        TextArea expectedArea = new TextArea(existingTest != null ? existingTest.getExpectedOutput() : "");
        expectedArea.setPrefRowCount(5);
        expectedArea.setWrapText(true);
        
        // Add all components
        content.getChildren().addAll(
            new Label("Test Name:"), nameField,
            new Label("Input Text:"), inputArea,
            patternsSection,
            new Label("Template:"), templateArea,
            new Label("Expected Output:"), expectedArea
        );
        
        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(600);
        
        dialog.getDialogPane().setContent(scrollPane);
        
        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Enable/disable save button based on required fields
        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        validateFieldsHolder[0] = () -> {
            boolean hasValidPattern = patternsList.stream()
                .anyMatch(p -> !p.getName().trim().isEmpty() && !p.getRegex().trim().isEmpty() && isValidRegex(p.getRegex()));
            
            saveButton.setDisable(
                nameField.getText().trim().isEmpty() ||
                inputArea.getText().trim().isEmpty() ||
                !hasValidPattern ||
                templateArea.getText().trim().isEmpty() ||
                expectedArea.getText().trim().isEmpty()
            );
        };
        
        Runnable validateFields = validateFieldsHolder[0];
        
        nameField.textProperty().addListener((obs, old, text) -> validateFields.run());
        inputArea.textProperty().addListener((obs, old, text) -> validateFields.run());
        templateArea.textProperty().addListener((obs, old, text) -> validateFields.run());
        expectedArea.textProperty().addListener((obs, old, text) -> validateFields.run());
        patternsList.addListener((javafx.collections.ListChangeListener<RegexTest.PatternEntry>) change -> {
            validateFields.run();
            patternsTable.refresh(); // Force refresh to update styling
        });
        
        validateFields.run();
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // Filter out empty patterns
                List<RegexTest.PatternEntry> validPatterns = new ArrayList<>();
                for (RegexTest.PatternEntry pattern : patternsList) {
                    if (!pattern.getName().trim().isEmpty() && !pattern.getRegex().trim().isEmpty()) {
                        validPatterns.add(new RegexTest.PatternEntry(pattern.getName().trim(), pattern.getRegex().trim()));
                    }
                }
                
                return new RegexTest(
                    nameField.getText().trim(),
                    inputArea.getText().trim(),
                    validPatterns,
                    templateArea.getText().trim(),
                    expectedArea.getText().trim()
                );
            }
            return null;
        });
        
        return dialog.showAndWait().orElse(null);
    }
    
    private boolean isValidRegex(String regex) {
        if (regex == null || regex.trim().isEmpty()) {
            return false;
        }
        try {
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }
    
    /**
     * Runs the selected tests
     */
    private void runTests(List<RegexTest> testsToRun) {
        List<TestResult> results = new ArrayList<>();
        
        // Save current state
        String originalInput = inputTextArea.getText();
        String originalTemplate = templateArea.getText();
        ObservableList<PatternEntry> originalPatterns = FXCollections.observableArrayList(patterns);
        boolean originalDebugState = debugOutputCheckBox.isSelected();
        boolean originalShowNoMatchesState = showNoMatchesCheckBox.isSelected();
        
        try {
            // Enable debug output for detailed logging and ensure consistent "show no matches" state
            debugOutputCheckBox.setSelected(true);
            // For tests, we want consistent behavior - don't show "no matches" indicators
            showNoMatchesCheckBox.setSelected(false);
            
            for (RegexTest test : testsToRun) {
                results.add(runSingleTest(test));
            }
            
            // Show results dialog
            showTestResults(results);
            
        } finally {
            // Restore original state
            inputTextArea.setText(originalInput);
            templateArea.replaceText(originalTemplate);
            patterns.clear();
            patterns.addAll(originalPatterns);
            debugOutputCheckBox.setSelected(originalDebugState);
            showNoMatchesCheckBox.setSelected(originalShowNoMatchesState);
        }
    }
    
    
    /**
     * Runs a single test case
     */
    private TestResult runSingleTest(RegexTest test) {
        // Create a TestCase wrapper for compatibility with existing code
        // For now, use the first pattern for the TestCase (for display purposes)
        String firstPatternName = test.getPatterns().isEmpty() ? "" : test.getPatterns().get(0).getName();
        String firstPatternRegex = test.getPatterns().isEmpty() ? "" : test.getPatterns().get(0).getRegex();
        
        TestCase testCase = new TestCase(
            test.getName(),
            test.getInput(),
            firstPatternName,
            firstPatternRegex,
            test.getTemplate(),
            test.getExpectedOutput()
        );
        StringBuilder debugLog = new StringBuilder();
        String actualOutput = "";
        String errorMessage = "";
        boolean passed = false;
        
        try {
            // Capture debug output
            debugLog.append("=== TEST: ").append(testCase.name).append(" ===\n");
            debugLog.append("Input: ").append(testCase.input).append("\n");
            debugLog.append("Pattern: ").append(testCase.patternRegex).append("\n");
            debugLog.append("Template: ").append(testCase.template.replace("\n", "\\n")).append("\n\n");
            
            // Set up test
            inputTextArea.setText(test.getInput());
            templateArea.replaceText(test.getTemplate());
            
            // Clear and add all patterns
            patterns.clear();
            for (RegexTest.PatternEntry patternEntry : test.getPatterns()) {
                patterns.add(new PatternEntry(patternEntry.getName(), patternEntry.getRegex()));
            }
            
            // Process all patterns and capture detailed debug info
            Map<String, List<MatchResult>> matches = new HashMap<>();
            debugLog.append("=== PATTERN MATCHING ===\n");
            
            for (RegexTest.PatternEntry patternEntry : test.getPatterns()) {
                debugLog.append("\nPattern '").append(patternEntry.getName()).append("': ").append(patternEntry.getRegex()).append("\n");
                Pattern pattern = Pattern.compile(patternEntry.getRegex());
                Matcher matcher = pattern.matcher(test.getInput());
                List<MatchResult> matchResults = new ArrayList<>();
                
                int matchCount = 0;
                while (matcher.find()) {
                    MatchResult matchResult = matcher.toMatchResult();
                    matchResults.add(matchResult);
                    debugLog.append("  Match ").append(matchCount++).append(": \"").append(matchResult.group()).append("\"\n");
                    for (int i = 1; i <= matchResult.groupCount(); i++) {
                        debugLog.append("    Group ").append(i).append(": \"").append(matchResult.group(i)).append("\"\n");
                    }
                }
                matches.put(patternEntry.getName(), matchResults);
                debugLog.append("  Total matches for '").append(patternEntry.getName()).append("': ").append(matchResults.size()).append("\n");
            }
            debugLog.append("\n");
            
            // Get output with detailed debug tracing
            debugLog.append("=== TEMPLATE PROCESSING ===\n");
            actualOutput = processTemplateScriptWithDebug(testCase.template, matches, debugLog).trim();
            
            // Compare with expected
            String normalizedActual = normalizeOutput(actualOutput);
            String normalizedExpected = normalizeOutput(testCase.expectedOutput);
            
            passed = normalizedActual.equals(normalizedExpected);
            
            if (!passed && errorMessage.isEmpty()) {
                // Create a concise error message for the table
                String expectedShort = testCase.expectedOutput.replace("\n", "\\n");
                String actualShort = actualOutput.replace("\n", "\\n");
                
                // Truncate if too long for table display
                if (expectedShort.length() > 50) {
                    expectedShort = expectedShort.substring(0, 47) + "...";
                }
                if (actualShort.length() > 50) {
                    actualShort = actualShort.substring(0, 47) + "...";
                }
                
                errorMessage = "Expected: " + expectedShort + " | Got: " + actualShort;
            }
            
            debugLog.append("\n=== COMPARISON ===\n");
            debugLog.append("Expected: ").append(testCase.expectedOutput.replace("\n", "\\n")).append("\n");
            debugLog.append("Actual: ").append(actualOutput.replace("\n", "\\n")).append("\n");
            debugLog.append("Normalized Expected: ").append(normalizedExpected.replace("\n", "\\n")).append("\n");
            debugLog.append("Normalized Actual: ").append(normalizedActual.replace("\n", "\\n")).append("\n");
            debugLog.append("RESULT: ").append(passed ? "PASS" : "FAIL").append("\n");
            
            if (!passed) {
                debugLog.append("FAILURE REASON: ").append(errorMessage).append("\n");
            }
            
        } catch (Exception e) {
            passed = false;
            errorMessage = e.getMessage();
            debugLog.append("ERROR: ").append(errorMessage).append("\n");
            debugLog.append("Stack trace: ").append(getStackTrace(e)).append("\n");
        }
        
        return new TestResult(testCase, passed, actualOutput, debugLog.toString(), errorMessage);
    }
    
    /**
     * Helper method to get stack trace as string
     */
    private String getStackTrace(Exception e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    
    /**
     * Process template script with detailed debug logging
     */
    private String processTemplateScriptWithDebug(String template, Map<String, List<MatchResult>> matches, StringBuilder debugLog) {
        debugLog.append("Processing template: ").append(template.replace("\n", "\\n")).append("\n");
        return processTemplateScriptWithDebug(template, matches, null, -1, debugLog);
    }
    
    /**
     * Process template script with detailed debug logging and loop context
     */
    private String processTemplateScriptWithDebug(String template, Map<String, List<MatchResult>> matches,
                                                 String currentPattern, int currentIndex, StringBuilder debugLog) {
        StringBuilder result = new StringBuilder();
        int pos = 0;
        
        while (pos < template.length()) {
            // Handle if statements
            if (template.startsWith("{if ", pos)) {
                int endIf = template.indexOf("}", pos);
                if (endIf == -1) break;
                
                String condition = template.substring(pos + 4, endIf).trim();
                int blockEnd = findMatchingEndIf(template, endIf + 1);
                if (blockEnd == -1) break;
                
                String blockContent = template.substring(endIf + 1, blockEnd);
                
                debugLog.append("Found IF condition: '").append(condition).append("' with pattern=").append(currentPattern).append(", index=").append(currentIndex).append("\n");
                
                // Evaluate the condition with current loop context
                boolean conditionResult = evaluateConditionWithDebug(condition, matches, currentPattern, currentIndex, debugLog);
                
                debugLog.append("Condition result: ").append(conditionResult).append("\n");
                
                if (conditionResult) {
                    debugLog.append("Processing IF block content: '").append(blockContent.replace("\n", "\\n")).append("'\n");
                    String processedBlock = processTemplateScriptWithDebug(blockContent, matches, currentPattern, currentIndex, debugLog);
                    result.append(processedBlock);
                    debugLog.append("IF block produced: '").append(processedBlock.replace("\n", "\\n")).append("'\n");
                } else {
                    debugLog.append("Skipping IF block (condition false)\n");
                }
                
                pos = blockEnd + 5; // Skip past {/if}
            }
            // Handle for loops
            else if (template.startsWith("{for ", pos)) {
                int endFor = template.indexOf("}", pos);
                if (endFor == -1) break;
                
                String patternName = template.substring(pos + 5, endFor).trim();
                int loopEnd = template.indexOf("{/for}", endFor);
                if (loopEnd == -1) break;
                
                String loopContent = template.substring(endFor + 1, loopEnd);
                List<MatchResult> patternMatches = matches.get(patternName);
                
                debugLog.append("Found FOR loop: pattern='").append(patternName).append("', content='").append(loopContent.replace("\n", "\\n")).append("'\n");
                debugLog.append("Pattern matches: ").append(patternMatches != null ? patternMatches.size() : 0).append("\n");
                
                if (patternMatches != null && !patternMatches.isEmpty()) {
                    for (int i = 0; i < patternMatches.size(); i++) {
                        debugLog.append("Processing FOR iteration ").append(i).append(" for pattern ").append(patternName).append("\n");
                        
                        // First, replace pattern variables for this iteration
                        String processedLoop = processTemplateVariablesWithDebug(loopContent, matches, patternName, i, debugLog);
                        
                        // Then, process nested template commands with the current loop context
                        processedLoop = processTemplateScriptWithDebug(processedLoop, matches, patternName, i, debugLog);
                        
                        // Remove command-only lines from loop content
                        processedLoop = removeCommandOnlyLines(processedLoop);
                        // Clean up excessive newlines
                        processedLoop = processedLoop.replaceAll("\n\n+", "\n");
                        // Trim leading and trailing whitespace from each iteration
                        processedLoop = processedLoop.trim();
                        
                        debugLog.append("FOR iteration ").append(i).append(" final result: '").append(processedLoop.replace("\n", "\\n")).append("'\n");
                        
                        if (!processedLoop.isEmpty()) {
                            result.append(processedLoop);
                            // Add newline only if not the last iteration and content exists
                            if (i < patternMatches.size() - 1) {
                                result.append("\n");
                            }
                        }
                    }
                } else if (showNoMatchesCheckBox != null && showNoMatchesCheckBox.isSelected()) {
                    // Show no matches found message in italics
                    result.append("<i>No matches found for \"").append(patternName).append("\"</i>");
                    debugLog.append("No matches found for pattern '").append(patternName).append("'\n");
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
                debugLog.append("Processing variable: '").append(variable).append("'\n");
                String processed = processVariable(variable, matches, currentPattern, currentIndex);
                debugLog.append("Variable '").append(variable).append("' resolved to: '").append(processed).append("'\n");
                
                // Mark empty results with a special marker
                if (processed.isEmpty() || processed.equals("{" + variable + "}")) {
                    result.append("__EMPTY_PATTERN__");
                } else {
                    result.append(processed);
                }
                pos = end + 1;
            }
            else {
                result.append(template.charAt(pos));
                pos++;
            }
        }
        
        // Remove command-only lines and handle empty patterns
        String finalResult = processOutputLines(result.toString());
        debugLog.append("Final processed result: '").append(finalResult.replace("\n", "\\n")).append("'\n");
        return finalResult;
    }
    
    /**
     * Process template variables with debug logging
     */
    private String processTemplateVariablesWithDebug(String template, Map<String, List<MatchResult>> matches, 
                                                    String currentPattern, int currentIndex, StringBuilder debugLog) {
        debugLog.append("Processing template variables for pattern '").append(currentPattern).append("' index ").append(currentIndex).append("\n");
        debugLog.append("Template before variable processing: '").append(template.replace("\n", "\\n")).append("'\n");
        
        // Process the template variables using the existing method
        String result = processTemplateVariables(template, matches, currentPattern, currentIndex);
        
        debugLog.append("Template after variable processing: '").append(result.replace("\n", "\\n")).append("'\n");
        return result;
    }
    
    /**
     * Evaluate condition with debug logging
     */
    private boolean evaluateConditionWithDebug(String condition, Map<String, List<MatchResult>> matches,
                                             String currentPattern, int currentIndex, StringBuilder debugLog) {
        debugLog.append("Evaluating condition: '").append(condition).append("'\n");
        
        try {
            // Replace pattern references with their numeric values
            String expression = condition;
            
            // First handle current pattern references if we're in a loop
            if (currentPattern != null && currentIndex >= 0) {
                debugLog.append("Processing current pattern references (").append(currentPattern).append("[").append(currentIndex).append("])\n");
                // Handle pattern.group(n) syntax for current loop pattern
                Pattern groupPattern = Pattern.compile("\\b" + Pattern.quote(currentPattern) + "\\.group\\((\\d+)\\)");
                Matcher groupMatcher = groupPattern.matcher(expression);
                StringBuffer sb = new StringBuffer();
                
                while (groupMatcher.find()) {
                    int groupNum = Integer.parseInt(groupMatcher.group(1));
                    List<MatchResult> patternMatches = matches.get(currentPattern);
                    String replacement = "0";
                    
                    if (patternMatches != null && currentIndex < patternMatches.size()) {
                        MatchResult match = patternMatches.get(currentIndex);
                        if (groupNum <= match.groupCount()) {
                            String groupValue = match.group(groupNum);
                            replacement = groupValue != null ? groupValue : "0";
                        }
                    }
                    
                    debugLog.append("Replacing '").append(groupMatcher.group()).append("' with '").append(replacement).append("'\n");
                    groupMatcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                }
                groupMatcher.appendTail(sb);
                expression = sb.toString();
                debugLog.append("After current pattern processing: '").append(expression).append("'\n");
            }
            
            // Find all pattern references in the condition (but not pure numbers)
            Pattern patternRef = Pattern.compile("\\b([a-zA-Z]\\w*)(?:\\[(\\d+)\\])?(?:\\.group\\((\\d+)\\))?");
            Matcher matcher = patternRef.matcher(expression);
            
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String fullMatch = matcher.group();
                String patternName = matcher.group(1);
                String indexStr = matcher.group(2);
                String groupStr = matcher.group(3);
                
                debugLog.append("Found pattern reference: '").append(fullMatch).append("' -> name='").append(patternName).append("', index='").append(indexStr).append("', group='").append(groupStr).append("'\n");
                
                // Skip if it's a keyword or function
                if (isKeywordOrFunction(patternName)) {
                    debugLog.append("Skipping keyword/function: ").append(patternName).append("\n");
                    matcher.appendReplacement(sb, matcher.group());
                    continue;
                }
                
                // Only process if this is actually a pattern name that exists
                if (matches.containsKey(patternName)) {
                    // Get the value for this pattern reference
                    String value = getPatternValue(patternName, indexStr, groupStr, matches);
                    debugLog.append("Pattern '").append(patternName).append("' exists, value='").append(value).append("'\n");
                    
                    // Try to parse as number, otherwise use 0
                    try {
                        Double.parseDouble(value);
                        matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
                    } catch (NumberFormatException e) {
                        // If it's not a number, use the length of the string
                        matcher.appendReplacement(sb, String.valueOf(value.length()));
                    }
                } else {
                    // Not a pattern name, keep as is
                    debugLog.append("Pattern '").append(patternName).append("' not found, keeping as-is\n");
                    matcher.appendReplacement(sb, matcher.group());
                }
            }
            matcher.appendTail(sb);
            expression = sb.toString();
            
            debugLog.append("Final math expression: '").append(expression).append("'\n");
            
            // Evaluate the mathematical expression
            boolean result = evaluateMathExpressionWithDebug(expression, debugLog);
            debugLog.append("Math evaluation result: ").append(result).append("\n");
            return result;
            
        } catch (Exception e) {
            debugLog.append("ERROR in condition evaluation: ").append(e.getMessage()).append("\n");
            return false;
        }
    }
    
    /**
     * Evaluate math expression with debug logging
     */
    private boolean evaluateMathExpressionWithDebug(String expression, StringBuilder debugLog) {
        debugLog.append("Evaluating math expression: '").append(expression).append("'\n");
        
        // Handle logical operators first (they have lower precedence)
        if (expression.contains("&&")) {
            String[] parts = expression.split("&&", 2);
            boolean left = evaluateMathExpressionWithDebug(parts[0].trim(), debugLog);
            if (!left) {
                // Short-circuit evaluation for AND
                debugLog.append("Short-circuit AND: left side is false, result = false\n");
                return false;
            }
            boolean right = evaluateMathExpressionWithDebug(parts[1].trim(), debugLog);
            boolean result = left && right;
            debugLog.append("AND operation: ").append(left).append(" && ").append(right).append(" = ").append(result).append("\n");
            return result;
        } else if (expression.contains("||")) {
            String[] parts = expression.split("\\|\\|", 2);
            boolean left = evaluateMathExpressionWithDebug(parts[0].trim(), debugLog);
            if (left) {
                // Short-circuit evaluation for OR
                debugLog.append("Short-circuit OR: left side is true, result = true\n");
                return true;
            }
            boolean right = evaluateMathExpressionWithDebug(parts[1].trim(), debugLog);
            boolean result = left || right;
            debugLog.append("OR operation: ").append(left).append(" || ").append(right).append(" = ").append(result).append("\n");
            return result;
        }
        
        // Handle comparison operators
        if (expression.contains("<=")) {
            String[] parts = expression.split("<=", 2);
            double left = evaluateArithmetic(parts[0].trim());
            double right = evaluateArithmetic(parts[1].trim());
            boolean result = left <= right;
            debugLog.append("Comparison: ").append(left).append(" <= ").append(right).append(" = ").append(result).append("\n");
            return result;
        } else if (expression.contains(">=")) {
            String[] parts = expression.split(">=", 2);
            double left = evaluateArithmetic(parts[0].trim());
            double right = evaluateArithmetic(parts[1].trim());
            boolean result = left >= right;
            debugLog.append("Comparison: ").append(left).append(" >= ").append(right).append(" = ").append(result).append("\n");
            return result;
        } else if (expression.contains("==")) {
            String[] parts = expression.split("==", 2);
            double left = evaluateArithmetic(parts[0].trim());
            double right = evaluateArithmetic(parts[1].trim());
            boolean result = Math.abs(left - right) < 0.0001;
            debugLog.append("Comparison: ").append(left).append(" == ").append(right).append(" = ").append(result).append("\n");
            return result;
        } else if (expression.contains("!=")) {
            String[] parts = expression.split("!=", 2);
            double left = evaluateArithmetic(parts[0].trim());
            double right = evaluateArithmetic(parts[1].trim());
            boolean result = Math.abs(left - right) >= 0.0001;
            debugLog.append("Comparison: ").append(left).append(" != ").append(right).append(" = ").append(result).append("\n");
            return result;
        } else if (expression.contains("<")) {
            String[] parts = expression.split("<", 2);
            double left = evaluateArithmetic(parts[0].trim());
            double right = evaluateArithmetic(parts[1].trim());
            boolean result = left < right;
            debugLog.append("Comparison: ").append(left).append(" < ").append(right).append(" = ").append(result).append("\n");
            return result;
        } else if (expression.contains(">")) {
            String[] parts = expression.split(">", 2);
            double left = evaluateArithmetic(parts[0].trim());
            double right = evaluateArithmetic(parts[1].trim());
            boolean result = left > right;
            debugLog.append("Comparison: ").append(left).append(" > ").append(right).append(" = ").append(result).append("\n");
            return result;
        } else {
            // No comparison operator, evaluate as boolean (non-zero is true)
            double value = evaluateArithmetic(expression);
            boolean result = value != 0;
            debugLog.append("Boolean evaluation: ").append(value).append(" != 0 = ").append(result).append("\n");
            return result;
        }
    }
    
    /**
     * Normalizes output for comparison (removes extra whitespace, etc.)
     */
    private String normalizeOutput(String output) {
        return output.replaceAll("\\s+", " ").trim();
    }
    
    /**
     * Shows test results in a popup dialog
     */
    private void showTestResults(List<TestResult> results) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Regex Test Results");
        dialog.setHeaderText(null);
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(dialog);
        
        VBox content = new VBox(10);
        content.setPadding(new Insets(15));
        content.setPrefWidth(800);
        content.setPrefHeight(600);
        
        // Summary
        long passedCount = results.stream().mapToLong(r -> r.passed ? 1 : 0).sum();
        long failedCount = results.size() - passedCount;
        
        Label summaryLabel = new Label(String.format("Test Results: %d passed, %d failed out of %d total", 
                                                     passedCount, failedCount, results.size()));
        summaryLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        // Results table
        TableView<TestResult> resultsTable = new TableView<>();
        resultsTable.setPrefHeight(300);
        
        TableColumn<TestResult, String> nameCol = new TableColumn<>("Test Name");
        nameCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().testCase.name));
        nameCol.setPrefWidth(200);
        
        TableColumn<TestResult, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().passed ? "PASS" : "FAIL"));
        statusCol.setPrefWidth(80);
        statusCol.setCellFactory(column -> new TableCell<TestResult, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("PASS".equals(item)) {
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
        
        TableColumn<TestResult, String> errorCol = new TableColumn<>("Error/Failure Reason");
        errorCol.setCellValueFactory(data -> new SimpleStringProperty(
            data.getValue().errorMessage != null && !data.getValue().errorMessage.isEmpty() 
                ? data.getValue().errorMessage : ""));
        errorCol.setPrefWidth(300);
        errorCol.setCellFactory(column -> new TableCell<TestResult, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    if (!item.isEmpty()) {
                        // Set tooltip for long error messages
                        setTooltip(new Tooltip(item));
                        setStyle("-fx-text-fill: #cc0000;");
                    } else {
                        setTooltip(null);
                        setStyle("");
                    }
                }
            }
        });
        
        resultsTable.getColumns().addAll(nameCol, statusCol, errorCol);
        resultsTable.setItems(FXCollections.observableArrayList(results));
        
        // Debug output area
        Label debugLabel = new Label("Debug Output for Selected Test:");
        debugLabel.setStyle("-fx-font-weight: bold;");
        
        TextArea debugArea = new TextArea();
        debugArea.setEditable(false);
        debugArea.setPrefRowCount(10);
        debugArea.setStyle("-fx-font-family: 'Courier New', monospace; -fx-font-size: 12px;");
        
        // Update debug area when selection changes
        resultsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                debugArea.setText(newSelection.debugOutput);
            }
        });
        
        // Select first item by default
        if (!results.isEmpty()) {
            resultsTable.getSelectionModel().selectFirst();
        }
        
        content.getChildren().addAll(summaryLabel, resultsTable, debugLabel, debugArea);
        
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        
        dialog.showAndWait();
    }
    
}
