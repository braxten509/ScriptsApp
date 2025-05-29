package com.doterra.app.view;

import com.doterra.app.controller.ButtonController;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import com.doterra.app.util.ColorUtil;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.StageStyle;
import javafx.scene.Node;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;

import java.util.Optional;

public class ChatScriptsPanel {
    
    private final BorderPane root;
    private final TabPane tabPane;
    private final TextArea textArea;
    private ButtonController buttonController;
    private ScriptButton selectedButton;
    
    public ChatScriptsPanel() {
        root = new BorderPane();
        root.setPadding(new Insets(10));
        
        buttonController = new ButtonController();
        
        // Create tab pane for button categories
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        // Create default tab if none exist
        if (buttonController.getAllTabs().isEmpty()) {
            ButtonTab defaultTab = new ButtonTab("Quick Responses");
            buttonController.addTab(defaultTab);
        }
        
        // Create text area for script editing
        textArea = new TextArea();
        textArea.setPromptText("Enter your script content here...");
        textArea.setPrefHeight(150);
        
        // Create controls
        Button addButton = new Button("Add Script");
        addButton.setOnAction(e -> showCreateButtonDialog());
        
        HBox controls = new HBox(10, addButton);
        controls.setPadding(new Insets(5, 0, 5, 0));
        controls.setAlignment(Pos.CENTER_LEFT);
        
        // Build UI
        setupTabsFromController();
        
        VBox centerSection = new VBox(5, controls, tabPane);
        root.setCenter(centerSection);
        root.setBottom(textArea);
        
        // Keyboard shortcuts
        setupKeyboardShortcuts();
    }
    
    private void setupTabsFromController() {
        tabPane.getTabs().clear();
        
        for (ButtonTab buttonTab : buttonController.getAllTabs()) {
            addTabToUI(buttonTab);
        }
        
        // Add the "+" tab for creating new tabs
        Tab addTab = new Tab("+");
        addTab.setId("addTab");
        addTab.setClosable(false);
        
        // Add both selection handler and mouse click handler
        addTab.setOnSelectionChanged(e -> {
            if (addTab.isSelected()) {
                showAddTabDialog();
                // Deselect this tab after showing dialog
                if (tabPane.getTabs().size() > 1) {
                    tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
                }
            }
        });
        
        // Add direct click handler for testing
        Button addTabButton = new Button("+");
        addTabButton.setOnAction(e -> showAddTabDialog());
        addTabButton.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
        addTab.setGraphic(addTabButton);
        
        tabPane.getTabs().add(addTab);
        
        // Select first regular tab if available
        if (tabPane.getTabs().size() > 1) {
            tabPane.getSelectionModel().select(0);
        }
    }
    
    private void addTabToUI(ButtonTab buttonTab) {
        Tab tab = new Tab(buttonTab.getName());
        tab.setId(buttonTab.getId());
        
        // Create 6x6 button grid
        GridPane buttonGrid = new GridPane();
        buttonGrid.setPadding(new Insets(10));
        buttonGrid.setHgap(5);
        buttonGrid.setVgap(5);
        
        // Configure grid columns and rows for 6x6
        for (int i = 0; i < 6; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setMinWidth(120);
            col.setPrefWidth(120);
            col.setMaxWidth(120);
            buttonGrid.getColumnConstraints().add(col);
            
            RowConstraints row = new RowConstraints();
            row.setMinHeight(50);
            row.setPrefHeight(50);
            row.setMaxHeight(50);
            buttonGrid.getRowConstraints().add(row);
        }
        
        ScrollPane scrollPane = new ScrollPane(buttonGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        tab.setContent(scrollPane);
        
        // Add existing buttons to grid
        addButtonsToGrid(buttonGrid, buttonTab.getButtons(), tab);
        
        // Setup drag and drop for the grid
        setupGridDragAndDrop(buttonGrid);
        
        // Add context menu to tab
        setupTabContextMenu(tab);
        
        tabPane.getTabs().add(tab);
    }
    
    private void addButtonsToGrid(GridPane buttonGrid, java.util.List<ScriptButton> buttons, Tab tab) {
        buttonGrid.getChildren().clear();
        
        int row = 0;
        int col = 0;
        
        for (ScriptButton scriptButton : buttons) {
            if (row >= 6) break; // Don't exceed 6x6 grid
            
            Button button = createButtonUI(scriptButton);
            setupButtonDragAndDrop(button, scriptButton);
            setupButtonContextMenu(button, scriptButton, tab);
            
            buttonGrid.add(button, col, row);
            
            col++;
            if (col >= 6) {
                col = 0;
                row++;
            }
        }
    }
    
    private void addButtonToTab(Tab tab, ScriptButton scriptButton) {
        ScrollPane scrollPane = (ScrollPane) tab.getContent();
        GridPane buttonGrid = (GridPane) scrollPane.getContent();
        
        // Find next available position
        int nextPosition = buttonGrid.getChildren().size();
        if (nextPosition >= 36) return; // Grid is full
        
        int row = nextPosition / 6;
        int col = nextPosition % 6;
        
        Button button = createButtonUI(scriptButton);
        setupButtonDragAndDrop(button, scriptButton);
        setupButtonContextMenu(button, scriptButton, tab);
        
        buttonGrid.add(button, col, row);
    }
    
    private Button createButtonUI(ScriptButton scriptButton) {
        Button button = new Button(scriptButton.getName());
        button.getStyleClass().add("script-button");
        button.setPrefSize(120, 50);
        button.setWrapText(true);
        button.setUserData(scriptButton); // Store script button reference for drag-and-drop
        
        // Set button color
        if (scriptButton.getColor() != null) {
            String colorStyle = ColorUtil.colorToHex(scriptButton.getColor());
            button.setStyle("-fx-base: " + colorStyle + ";");
        }
        
        // Button click action
        button.setOnAction(e -> {
            selectedButton = scriptButton;
            textArea.setText(scriptButton.getContent());
            
            // Copy to clipboard
            ClipboardContent content = new ClipboardContent();
            content.putString(scriptButton.getContent());
            Clipboard.getSystemClipboard().setContent(content);
            
            // Visual feedback for selection
            clearButtonSelection();
            button.getStyleClass().add("selected");
        });
        
        return button;
    }
    
    private void setupButtonDragAndDrop(Button button, ScriptButton scriptButton) {
        button.setOnDragDetected(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                Dragboard dragboard = button.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(scriptButton.getId());
                dragboard.setContent(content);
                
                // Create drag view
                WritableImage snapshot = button.snapshot(new SnapshotParameters(), null);
                dragboard.setDragView(snapshot, e.getX(), e.getY());
                
                // Add visual feedback
                button.setOpacity(0.5);
                e.consume();
            }
        });
        
        button.setOnDragDone(e -> {
            button.setOpacity(1.0);
            e.consume();
        });
        
        button.setOnDragOver(e -> {
            if (e.getGestureSource() != button && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                button.getStyleClass().add("drag-target");
            }
            e.consume();
        });
        
        // Also add drag-over support to the grid itself for empty cells
        GridPane parentGrid = null;
        button.parentProperty().addListener((obs, oldParent, newParent) -> {
            if (newParent instanceof GridPane) {
                GridPane grid = (GridPane) newParent;
                setupGridDragAndDrop(grid);
            }
        });
        
        button.setOnDragExited(e -> {
            button.getStyleClass().remove("drag-target");
            e.consume();
        });
        
        button.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String draggedButtonId = db.getString();
                GridPane grid = (GridPane) button.getParent();
                
                // Find the dragged button
                Button draggedButton = findButtonById(grid, draggedButtonId);
                
                if (draggedButton != null && draggedButton != button) {
                    // Get target position (where we want to drop)
                    int targetRow = GridPane.getRowIndex(button) != null ? GridPane.getRowIndex(button) : 0;
                    int targetCol = GridPane.getColumnIndex(button) != null ? GridPane.getColumnIndex(button) : 0;
                    
                    // Get source position (where the dragged button came from)
                    int sourceRow = GridPane.getRowIndex(draggedButton) != null ? GridPane.getRowIndex(draggedButton) : 0;
                    int sourceCol = GridPane.getColumnIndex(draggedButton) != null ? GridPane.getColumnIndex(draggedButton) : 0;
                    
                    // Swap the positions
                    GridPane.setRowIndex(draggedButton, targetRow);
                    GridPane.setColumnIndex(draggedButton, targetCol);
                    GridPane.setRowIndex(button, sourceRow);
                    GridPane.setColumnIndex(button, sourceCol);
                    
                    success = true;
                    updateButtonOrder(grid);
                }
            }
            
            button.getStyleClass().remove("drag-target");
            e.setDropCompleted(success);
            e.consume();
        });
    }
    
    private Button findButtonById(GridPane grid, String buttonId) {
        for (Node child : grid.getChildren()) {
            if (child instanceof Button) {
                Button btn = (Button) child;
                if (btn.getUserData() instanceof ScriptButton) {
                    ScriptButton script = (ScriptButton) btn.getUserData();
                    if (script.getId().equals(buttonId)) {
                        return btn;
                    }
                }
            }
        }
        return null;
    }
    
    private void setupGridDragAndDrop(GridPane grid) {
        // Add drag-over and drop support to the grid itself for empty cells
        grid.setOnDragOver(e -> {
            if (e.getDragboard().hasString()) {
                // Calculate which grid cell the mouse is over
                double cellWidth = grid.getWidth() / 6;
                double cellHeight = grid.getHeight() / 6;
                
                int col = (int) (e.getX() / cellWidth);
                int row = (int) (e.getY() / cellHeight);
                
                // Check if this position is empty
                if (col >= 0 && col < 6 && row >= 0 && row < 6 && isGridCellEmpty(grid, col, row)) {
                    e.acceptTransferModes(TransferMode.MOVE);
                }
            }
            e.consume();
        });
        
        grid.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String draggedButtonId = db.getString();
                Button draggedButton = findButtonById(grid, draggedButtonId);
                
                if (draggedButton != null) {
                    // Calculate target cell
                    double cellWidth = grid.getWidth() / 6;
                    double cellHeight = grid.getHeight() / 6;
                    
                    int targetCol = Math.max(0, Math.min(5, (int) (e.getX() / cellWidth)));
                    int targetRow = Math.max(0, Math.min(5, (int) (e.getY() / cellHeight)));
                    
                    // Only move if target cell is empty
                    if (isGridCellEmpty(grid, targetCol, targetRow)) {
                        GridPane.setRowIndex(draggedButton, targetRow);
                        GridPane.setColumnIndex(draggedButton, targetCol);
                        success = true;
                        updateButtonOrder(grid);
                    }
                }
            }
            e.setDropCompleted(success);
            e.consume();
        });
    }
    
    private boolean isGridCellEmpty(GridPane grid, int col, int row) {
        for (Node child : grid.getChildren()) {
            if (child instanceof Button) {
                Integer childCol = GridPane.getColumnIndex(child);
                Integer childRow = GridPane.getRowIndex(child);
                if ((childCol == null ? 0 : childCol) == col && (childRow == null ? 0 : childRow) == row) {
                    return false;
                }
            }
        }
        return true;
    }
    
    private void updateButtonOrder(GridPane grid) {
        // Get current tab
        Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
        if (currentTab == null || "addTab".equals(currentTab.getId())) return;
        
        ButtonTab buttonTab = buttonController.getTab(currentTab.getId());
        if (buttonTab == null) return;
        
        // Create new ordered list based on grid positions
        java.util.List<ScriptButton> newOrder = new java.util.ArrayList<>();
        
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 6; col++) {
                for (Node child : grid.getChildren()) {
                    if (child instanceof Button) {
                        Integer childRow = GridPane.getRowIndex(child);
                        Integer childCol = GridPane.getColumnIndex(child);
                        if ((childRow == null ? 0 : childRow) == row && 
                            (childCol == null ? 0 : childCol) == col) {
                            Button btn = (Button) child;
                            if (btn.getUserData() instanceof ScriptButton) {
                                newOrder.add((ScriptButton) btn.getUserData());
                            }
                        }
                    }
                }
            }
        }
        
        // Update the button tab with new order
        buttonTab.getButtons().clear();
        buttonTab.getButtons().addAll(newOrder);
        buttonController.saveState();
    }
    
    private void setupButtonContextMenu(Button button, ScriptButton scriptButton, Tab tab) {
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> showRenameButtonDialog(scriptButton, button));
        
        MenuItem changeColorItem = new MenuItem("Change Color");
        changeColorItem.setOnAction(e -> showChangeColorDialog(scriptButton, button));
        
        MenuItem duplicateItem = new MenuItem("Duplicate");
        duplicateItem.setOnAction(e -> duplicateButton(tab, scriptButton));
        
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> deleteButton(tab, scriptButton, button));
        
        contextMenu.getItems().addAll(renameItem, changeColorItem, duplicateItem, deleteItem);
        button.setContextMenu(contextMenu);
    }
    
    private void setupTabContextMenu(Tab tab) {
        if ("addTab".equals(tab.getId())) return;
        
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem renameItem = new MenuItem("Rename Tab");
        renameItem.setOnAction(e -> showRenameTabDialog(tab));
        
        MenuItem deleteItem = new MenuItem("Delete Tab");
        deleteItem.setOnAction(e -> deleteTab(tab));
        
        contextMenu.getItems().addAll(renameItem, deleteItem);
        
        Label tabLabel = new Label(tab.getText());
        tabLabel.setContextMenu(contextMenu);
        tab.setGraphic(tabLabel);
    }
    
    private void showCreateButtonDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Script Button");
        dialog.setHeaderText("Create a new script button");
        dialog.setContentText("Button name:");
        dialog.initStyle(StageStyle.UTILITY);
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                if (selectedTab != null && !"addTab".equals(selectedTab.getId())) {
                    ScriptButton newButton = new ScriptButton(name, textArea.getText(), Color.LIGHTBLUE);
                    buttonController.addButtonToTab(selectedTab.getId(), newButton);
                    addButtonToTab(selectedTab, newButton);
                    buttonController.saveState();
                }
            }
        });
    }
    
    private void showAddTabDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Tab");
        dialog.setHeaderText("Create a new tab");
        dialog.setContentText("Tab name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                ButtonTab newTab = new ButtonTab(name);
                buttonController.addTab(newTab);
                
                // Remove the "+" tab temporarily
                Tab addTab = tabPane.getTabs().get(tabPane.getTabs().size() - 1);
                tabPane.getTabs().remove(addTab);
                
                // Add new tab
                addTabToUI(newTab);
                
                // Re-add the "+" tab
                tabPane.getTabs().add(addTab);
                
                // Select the new tab
                tabPane.getSelectionModel().select(tabPane.getTabs().size() - 2);
                
                buttonController.saveState();
            }
        });
    }
    
    private void showRenameButtonDialog(ScriptButton scriptButton, Button button) {
        TextInputDialog dialog = new TextInputDialog(scriptButton.getName());
        dialog.setTitle("Rename Button");
        dialog.setHeaderText("Rename script button");
        dialog.setContentText("Button name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                scriptButton.setName(name);
                button.setText(name);
                buttonController.saveState();
            }
        });
    }
    
    private void showChangeColorDialog(ScriptButton scriptButton, Button button) {
        ColorPicker colorPicker = new ColorPicker(scriptButton.getColor());
        Dialog<Color> dialog = new Dialog<>();
        dialog.setTitle("Change Button Color");
        dialog.setHeaderText("Choose a color for the button");
        
        ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);
        
        dialog.getDialogPane().setContent(colorPicker);
        
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
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
        ScriptButton duplicate = new ScriptButton(scriptButton);
        buttonController.addButtonToTab(tab.getId(), duplicate);
        addButtonToTab(tab, duplicate);
        buttonController.saveState();
    }
    
    private void deleteButton(Tab tab, ScriptButton scriptButton, Button button) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Button");
        alert.setHeaderText("Delete script button");
        alert.setContentText("Are you sure you want to delete '" + scriptButton.getName() + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            buttonController.removeButtonFromTab(tab.getId(), scriptButton.getId());
            
            ScrollPane scrollPane = (ScrollPane) tab.getContent();
            GridPane buttonGrid = (GridPane) scrollPane.getContent();
            buttonGrid.getChildren().remove(button);
            
            if (selectedButton == scriptButton) {
                selectedButton = null;
                textArea.clear();
            }
            
            buttonController.saveState();
        }
    }
    
    private void showRenameTabDialog(Tab tab) {
        TextInputDialog dialog = new TextInputDialog(tab.getText());
        dialog.setTitle("Rename Tab");
        dialog.setHeaderText("Rename tab");
        dialog.setContentText("Tab name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                ButtonTab buttonTab = buttonController.getTab(tab.getId());
                if (buttonTab != null) {
                    buttonTab.setName(name);
                    tab.setText(name);
                    ((Label) tab.getGraphic()).setText(name);
                    buttonController.saveState();
                }
            }
        });
    }
    
    private void deleteTab(Tab tab) {
        if (tabPane.getTabs().size() <= 2) { // Account for the "+" tab
            showAlert("Cannot Delete", "Cannot delete the last tab.");
            return;
        }
        
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Tab");
        alert.setHeaderText("Delete tab");
        alert.setContentText("Are you sure you want to delete '" + tab.getText() + "'?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            buttonController.removeTab(tab.getId());
            tabPane.getTabs().remove(tab);
            buttonController.saveState();
        }
    }
    
    private void setupKeyboardShortcuts() {
        root.setOnKeyPressed(e -> {
            if (e.isControlDown()) {
                switch (e.getCode()) {
                    case N:
                        showCreateButtonDialog();
                        e.consume();
                        break;
                    case S:
                        if (selectedButton != null) {
                            selectedButton.setContent(textArea.getText());
                            buttonController.saveState();
                        }
                        e.consume();
                        break;
                }
            } else if (e.getCode() == KeyCode.DELETE && selectedButton != null) {
                // Find and delete selected button
                Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                if (currentTab != null && !"addTab".equals(currentTab.getId())) {
                    // Find the button in the UI and delete it
                    ScrollPane scrollPane = (ScrollPane) currentTab.getContent();
                    GridPane buttonGrid = (GridPane) scrollPane.getContent();
                    
                    buttonGrid.getChildren().removeIf(node -> {
                        if (node instanceof Button) {
                            Button btn = (Button) node;
                            return btn.getText().equals(selectedButton.getName());
                        }
                        return false;
                    });
                    
                    buttonController.removeButtonFromTab(currentTab.getId(), selectedButton.getId());
                    selectedButton = null;
                    textArea.clear();
                    buttonController.saveState();
                }
                e.consume();
            }
        });
    }
    
    private void clearButtonSelection() {
        // Remove selection class from all buttons
        for (Tab tab : tabPane.getTabs()) {
            if (tab.getContent() instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) tab.getContent();
                if (scrollPane.getContent() instanceof GridPane) {
                    GridPane buttonGrid = (GridPane) scrollPane.getContent();
                    buttonGrid.getChildren().forEach(node -> {
                        if (node instanceof Button) {
                            node.getStyleClass().remove("selected");
                        }
                    });
                }
            }
        }
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    // Public methods for testing and external access
    public BorderPane getRoot() {
        return root;
    }
    
    public String getTextAreaContent() {
        return textArea.getText();
    }
    
    public TabPane getTabPane() {
        return tabPane;
    }
    
    public ButtonController getButtonController() {
        return buttonController;
    }
    
    public void refreshButtons() {
        setupTabsFromController();
    }
    
    public void setTextAreaContent(String content) {
        textArea.setText(content);
    }
}