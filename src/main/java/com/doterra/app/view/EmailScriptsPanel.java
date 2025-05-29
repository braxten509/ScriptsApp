package com.doterra.app.view;

import com.doterra.app.controller.ButtonController;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import com.doterra.app.util.ColorUtil;
import java.util.List;
import com.doterra.app.util.HtmlEditor;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.StageStyle;
import javafx.scene.input.MouseButton;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.Node;

import java.util.Optional;
import java.util.ArrayList;

public class EmailScriptsPanel {
    
    private final BorderPane root;
    private final TabPane tabPane;
    private final HtmlEditor htmlEditor;
    private final ButtonController buttonController;
    
    public EmailScriptsPanel() {
        root = new BorderPane();
        root.setPadding(new Insets(10));
        
        // Create controller
        buttonController = new ButtonController();
        
        // Create top section with tabs for button categories
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Create default "General" tab
        // Load existing tabs or create default
        List<ButtonTab> existingTabs = buttonController.getAllTabs();
        if (existingTabs.isEmpty()) {
            // Create default tab only if no saved tabs exist
            ButtonTab generalTab = new ButtonTab("General");
            buttonController.addTab(generalTab);
            buttonController.saveState();
            addTabToUI(generalTab);
        } else {
            // Load all existing tabs
            for (ButtonTab tab : existingTabs) {
                addTabToUI(tab);
            }
        }
        
        // Create HTML editor for rich text
        htmlEditor = new HtmlEditor();
        htmlEditor.setPrefHeight(300);
        htmlEditor.setMinHeight(100);
        
        // Create controls for bottom section
        HBox controls = createControlsBar();
        
        // Add new tab button to tab pane
        Button addTabButton = new Button("+");
        addTabButton.setOnAction(e -> showAddTabDialog());
        
        HBox tabControls = new HBox(5, addTabButton);
        tabControls.setAlignment(Pos.CENTER_RIGHT);
        tabControls.setPadding(new Insets(5));
        
        // Layout for bottom section
        VBox bottomSection = new VBox(5, controls, htmlEditor);
        VBox.setVgrow(htmlEditor, Priority.ALWAYS);
        
        // Create top section with tabs
        VBox topSection = new VBox(5, tabControls, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        // Create resizable split pane
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.getItems().addAll(topSection, bottomSection);
        splitPane.setDividerPositions(0.6);
        
        // Add split pane to root
        root.setCenter(splitPane);
    }
    
    private HBox createControlsBar() {
        Button createButton = new Button("Create Button");
        createButton.setOnAction(e -> showCreateButtonDialog());
        
        Button clearButton = new Button("Clear");
        clearButton.setOnAction(e -> htmlEditor.setHtmlText(""));
        
        HBox controls = new HBox(10, createButton, clearButton);
        controls.setPadding(new Insets(5, 0, 5, 0));
        return controls;
    }
    
    private void showCreateButtonDialog() {
        Dialog<ScriptButton> dialog = new Dialog<>();
        dialog.setTitle("Create New Button");
        dialog.setHeaderText("Enter button details");
        
        // Set the button types
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
        
        // Create fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        TextField nameField = new TextField();
        nameField.setPromptText("Button Name");
        
        ColorPicker colorPicker = new ColorPicker(Color.LIGHTBLUE);
        
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Color:"), 0, 1);
        grid.add(colorPicker, 1, 1);
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the name field by default
        nameField.requestFocus();
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String buttonText = nameField.getText();
                if (buttonText.isEmpty()) {
                    showAlert("Name Required", "Please enter a name for the button.");
                    return null;
                }
                return new ScriptButton(buttonText, htmlEditor.getHtmlText(), colorPicker.getValue());
            }
            return null;
        });
        
        Optional<ScriptButton> result = dialog.showAndWait();
        
        result.ifPresent(button -> {
            // Get the currently selected tab
            Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
            if (selectedTab != null) {
                String tabId = selectedTab.getId();
                ButtonTab buttonTab = buttonController.getTab(tabId);
                if (buttonTab != null) {
                    // Add button to the data model
                    buttonController.addButtonToTab(tabId, button);
                    
                    // Add button to the UI
                    addButtonToTab(selectedTab, button);
                    buttonController.saveState();
                }
            }
        });
    }
    
    private void showAddTabDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Tab");
        dialog.setHeaderText("Create a new tab for organizing buttons");
        dialog.setContentText("Tab name:");
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                ButtonTab newTab = new ButtonTab(name);
                buttonController.addTab(newTab);
                addTabToUI(newTab);
                buttonController.saveState();
            } else {
                showAlert("Invalid Name", "Tab name cannot be empty.");
            }
        });
    }
    
    private void addTabToUI(ButtonTab buttonTab) {
        // Create a new tab
        Tab tab = new Tab(buttonTab.getName());
        tab.setId(buttonTab.getId());
        
        // Create flow pane for buttons
        FlowPane buttonPane = new FlowPane();
        buttonPane.setPadding(new Insets(10));
        buttonPane.setHgap(10);
        buttonPane.setVgap(10);
        
        // Enable drag-and-drop for buttons within the pane
        setupButtonPaneDragAndDrop(buttonPane);
        
        // Wrap in scroll pane
        ScrollPane scrollPane = new ScrollPane(buttonPane);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        tab.setContent(scrollPane);
        
        // Add existing buttons to the tab
        for (ScriptButton button : buttonTab.getButtons()) {
            addButtonToTab(tab, button);
        }
        
        // Add tab to tab pane
        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
        
        // Enable drag-and-drop for the tab
        setupTabDragAndDrop(tab);
    }
    
    private void addButtonToTab(Tab tab, ScriptButton scriptButton) {
        ScrollPane scrollPane = (ScrollPane) tab.getContent();
        FlowPane buttonPane = (FlowPane) scrollPane.getContent();
        
        // Create button UI
        Button button = new Button(scriptButton.getName());
        button.getStyleClass().add("script-button");
        button.setPrefSize(120, 50);
        button.setWrapText(true);
        
        // Set button color using -fx-base to preserve hover effects
        String colorStyle = ColorUtil.colorToHex(scriptButton.getColor());
        button.setStyle("-fx-base: " + colorStyle + ";");
        
        // Set button action
        button.setOnAction(e -> {
            String content = scriptButton.getContent();
            htmlEditor.setHtmlText(content);
            
            // Copy to clipboard with HTML format
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putHtml(content);
            clipboardContent.putString(content); // Also put as plain text
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        });
        
        // Add context menu for right-click
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> showRenameButtonDialog(scriptButton, button));
        
        MenuItem changeColorItem = new MenuItem("Change Color");
        changeColorItem.setOnAction(e -> showChangeColorDialog(scriptButton, button));
        
        MenuItem duplicateItem = new MenuItem("Duplicate");
        duplicateItem.setOnAction(e -> duplicateButton(tab, scriptButton));
        
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> {
            buttonController.removeButtonFromTab(tab.getId(), scriptButton.getId());
            buttonPane.getChildren().remove(button);
            buttonController.saveState();
        });
        
        contextMenu.getItems().addAll(renameItem, changeColorItem, duplicateItem, deleteItem);
        
        // Set context menu to show on right-click
        button.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.SECONDARY) {
                contextMenu.show(button, event.getScreenX(), event.getScreenY());
            }
        });
        
        // Enable drag-and-drop for the button
        setupButtonDragAndDrop(button, scriptButton);
        
        // Add button to the flow pane
        buttonPane.getChildren().add(button);
    }
    
    private void showRenameButtonDialog(ScriptButton scriptButton, Button button) {
        TextInputDialog dialog = new TextInputDialog(scriptButton.getName());
        dialog.setTitle("Rename Button");
        dialog.setHeaderText("Enter a new name for the button");
        dialog.setContentText("Name:");
        
        Optional<String> result = dialog.showAndWait();
        
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                scriptButton.setName(name);
                button.setText(name);
                buttonController.saveState();
            } else {
                showAlert("Invalid Name", "Button name cannot be empty.");
            }
        });
    }
    
    private void showChangeColorDialog(ScriptButton scriptButton, Button button) {
        Dialog<Color> dialog = new Dialog<>();
        dialog.setTitle("Change Button Color");
        dialog.setHeaderText("Select a new color for the button");
        
        // Set the button types
        ButtonType applyButtonType = new ButtonType("Apply", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);
        
        // Create color picker
        ColorPicker colorPicker = new ColorPicker(scriptButton.getColor());
        
        dialog.getDialogPane().setContent(colorPicker);
        
        // Convert the result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == applyButtonType) {
                return colorPicker.getValue();
            }
            return null;
        });
        
        Optional<Color> result = dialog.showAndWait();
        
        result.ifPresent(color -> {
            scriptButton.setColor(color);
            String colorStyle = ColorUtil.colorToHex(color);
            button.setStyle("-fx-base: " + colorStyle + ";");
            buttonController.saveState();
        });
    }
    
    private void duplicateButton(Tab tab, ScriptButton scriptButton) {
        ScriptButton duplicatedButton = new ScriptButton(scriptButton);
        String tabId = tab.getId();
        
        buttonController.addButtonToTab(tabId, duplicatedButton);
        addButtonToTab(tab, duplicatedButton);
        buttonController.saveState();
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY);
        alert.showAndWait();
    }
    
    public BorderPane getRoot() {
        return root;
    }
    
    public String getHtmlContent() {
        return htmlEditor.getHtmlText();
    }
    
    public void setHtmlContent(String content) {
        htmlEditor.setHtmlText(content);
    }
    
    private void setupTabDragAndDrop(Tab tab) {
        // Make tab draggable
        Label tabLabel = (Label) tab.getGraphic();
        if (tabLabel == null) {
            tabLabel = new Label(tab.getText());
            tab.setGraphic(tabLabel);
            tab.setText("");
        }
        
        final Label finalTabLabel = tabLabel;
        
        finalTabLabel.setOnDragDetected(event -> {
            Dragboard dragboard = finalTabLabel.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(tab.getId());
            dragboard.setContent(content);
            event.consume();
        });
        
        finalTabLabel.setOnDragOver(event -> {
            if (event.getGestureSource() != finalTabLabel && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        
        finalTabLabel.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasString()) {
                String draggedTabId = dragboard.getString();
                Tab draggedTab = findTabById(draggedTabId);
                Tab targetTab = tab;
                
                if (draggedTab != null && draggedTab != targetTab) {
                    int draggedIndex = tabPane.getTabs().indexOf(draggedTab);
                    int targetIndex = tabPane.getTabs().indexOf(targetTab);
                    
                    // Reorder tabs
                    tabPane.getTabs().remove(draggedTab);
                    tabPane.getTabs().add(targetIndex, draggedTab);
                    
                    // Update controller order
                    updateTabOrder();
                    success = true;
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
    private void setupButtonDragAndDrop(Button button, ScriptButton scriptButton) {
        button.setOnDragDetected(event -> {
            Dragboard dragboard = button.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(scriptButton.getId());
            dragboard.setContent(content);
            
            // Store the source tab ID
            Tab sourceTab = findTabContainingButton(button);
            if (sourceTab != null) {
                button.getProperties().put("sourceTabId", sourceTab.getId());
            }
            event.consume();
        });
    }
    
    private void setupButtonPaneDragAndDrop(FlowPane buttonPane) {
        buttonPane.setOnDragOver(event -> {
            if (event.getGestureSource() != buttonPane && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        
        buttonPane.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasString()) {
                String draggedButtonId = dragboard.getString();
                Node source = (Node) event.getGestureSource();
                
                if (source instanceof Button) {
                    Button draggedButton = (Button) source;
                    String sourceTabId = (String) draggedButton.getProperties().get("sourceTabId");
                    
                    // Get target tab
                    Tab targetTab = findTabContainingPane(buttonPane);
                    if (targetTab == null) {
                        event.setDropCompleted(false);
                        event.consume();
                        return;
                    }
                    String targetTabId = targetTab.getId();
                    
                    // Find the script button
                    ScriptButton scriptButton = findScriptButton(sourceTabId, draggedButtonId);
                    
                    if (scriptButton != null) {
                        // Calculate drop position
                        double dropX = event.getX();
                        double dropY = event.getY();
                        int targetIndex = calculateDropIndex(buttonPane, dropX, dropY);
                        
                        // Move button to new position
                        if (!sourceTabId.equals(targetTabId)) {
                            // Moving between tabs
                            buttonController.removeButtonFromTab(sourceTabId, draggedButtonId);
                            buttonController.addButtonToTab(targetTabId, scriptButton);
                            buttonPane.getChildren().remove(draggedButton);
                        }
                        
                        // Reorder within the pane
                        buttonPane.getChildren().remove(draggedButton);
                        buttonPane.getChildren().add(targetIndex, draggedButton);
                        
                        // Update the button's properties for future drags
                        draggedButton.getProperties().put("sourceTabId", targetTabId);
                        
                        // Update button order in controller
                        updateButtonOrder(targetTab);
                        success = true;
                    }
                }
            }
            
            event.setDropCompleted(success);
            event.consume();
        });
    }
    
    private Tab findTabById(String tabId) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getId().equals(tabId)) {
                return tab;
            }
        }
        return null;
    }
    
    private ScriptButton findScriptButton(String tabId, String buttonId) {
        List<ButtonTab> tabs = buttonController.getAllTabs();
        for (ButtonTab tab : tabs) {
            if (tab.getId().equals(tabId)) {
                for (ScriptButton button : tab.getButtons()) {
                    if (button.getId().equals(buttonId)) {
                        return button;
                    }
                }
            }
        }
        return null;
    }
    
    private int calculateDropIndex(FlowPane pane, double x, double y) {
        int index = 0;
        for (Node node : pane.getChildren()) {
            if (node instanceof Button) {
                double nodeX = node.getLayoutX() + node.getBoundsInLocal().getWidth() / 2;
                double nodeY = node.getLayoutY() + node.getBoundsInLocal().getHeight() / 2;
                
                if (y < nodeY || (y <= nodeY + node.getBoundsInLocal().getHeight() && x < nodeX)) {
                    break;
                }
                index++;
            }
        }
        return index;
    }
    
    private void updateTabOrder() {
        List<ButtonTab> newOrder = new ArrayList<>();
        for (Tab tab : tabPane.getTabs()) {
            String tabId = tab.getId();
            ButtonTab buttonTab = findButtonTab(tabId);
            if (buttonTab != null) {
                newOrder.add(buttonTab);
            }
        }
        buttonController.reorderTabs(newOrder);
        buttonController.saveState();
    }
    
    private void updateButtonOrder(Tab tab) {
        String tabId = tab.getId();
        ButtonTab buttonTab = findButtonTab(tabId);
        
        if (buttonTab != null) {
            List<ScriptButton> newOrder = new ArrayList<>();
            ScrollPane scrollPane = (ScrollPane) tab.getContent();
            FlowPane buttonPane = (FlowPane) scrollPane.getContent();
            
            for (Node node : buttonPane.getChildren()) {
                if (node instanceof Button) {
                    Button btn = (Button) node;
                    // Find the script button by name
                    for (ScriptButton scriptButton : buttonTab.getButtons()) {
                        if (scriptButton.getName().equals(btn.getText())) {
                            newOrder.add(scriptButton);
                            break;
                        }
                    }
                }
            }
            
            buttonTab.setButtons(newOrder);
            buttonController.saveState();
        }
    }
    
    private ButtonTab findButtonTab(String tabId) {
        List<ButtonTab> tabs = buttonController.getAllTabs();
        for (ButtonTab tab : tabs) {
            if (tab.getId().equals(tabId)) {
                return tab;
            }
        }
        return null;
    }
    
    private Tab findTabContainingButton(Button button) {
        Node parent = button.getParent();
        while (parent != null) {
            for (Tab tab : tabPane.getTabs()) {
                if (tab.getContent() != null && isNodeInTab(parent, tab)) {
                    return tab;
                }
            }
            parent = parent.getParent();
        }
        return null;
    }
    
    private Tab findTabContainingPane(FlowPane pane) {
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getContent() instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) tab.getContent();
                if (scrollPane.getContent() == pane) {
                    return tab;
                }
            }
        }
        return null;
    }
    
    private boolean isNodeInTab(Node node, Tab tab) {
        Node tabContent = tab.getContent();
        Node current = node;
        while (current != null) {
            if (current == tabContent) {
                return true;
            }
            current = current.getParent();
        }
        return false;
    }
}