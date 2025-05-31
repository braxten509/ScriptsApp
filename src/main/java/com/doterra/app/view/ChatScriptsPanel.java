package com.doterra.app.view;

import com.doterra.app.controller.ButtonController;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import com.doterra.app.util.ColorUtil;
import com.doterra.app.util.SimpleStyler;
import com.doterra.app.util.ComplexStyler;
import com.doterra.app.util.HoverManager;
import com.doterra.app.util.VariableReplacer;
import com.doterra.app.util.DialogUtil;
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
    private String originalContent; // Track original content for change detection
    private boolean contentChanged; // Flag to track if content has been modified
    private boolean isVariableReplacement; // Flag to track if current text change is from variable replacement
    
    public ChatScriptsPanel() {
        root = new BorderPane();
        SimpleStyler.applyDefaultLayout(root);
        
        buttonController = new ButtonController("doterra_chat_buttons.dat");
        
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
        SimpleStyler.setTextAreaHeight(textArea);
        
        // Add text change listener to track modifications
        textArea.textProperty().addListener((observable, oldValue, newValue) -> {
            if (selectedButton != null && originalContent != null) {
                // Don't mark as changed if this is from variable replacement
                if (!isVariableReplacement) {
                    contentChanged = !originalContent.equals(newValue);
                }
            }
        });
        
        // Create controls
        Button addButton = new Button("Add Script");
        addButton.setOnAction(e -> showCreateButtonDialog());
        
        HBox controls = new HBox(10, addButton);
        controls.setPadding(new Insets(5, 0, 5, 0));
        controls.setAlignment(Pos.CENTER_LEFT);
        
        // Build UI
        setupTabsFromController();
        
        VBox centerSection = new VBox(5, controls, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS);
        
        // Create a SplitPane for resizable text area
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(javafx.geometry.Orientation.VERTICAL);
        splitPane.getItems().addAll(centerSection, textArea);
        
        // Set initial divider position (70% for buttons, 30% for text area)
        splitPane.setDividerPositions(0.7);
        
        // Set minimum sizes to keep buttons visible
        centerSection.setMinHeight(300);
        textArea.setMinHeight(100);
        
        root.setCenter(splitPane);
        
        // Keyboard shortcuts
        setupKeyboardShortcuts();
        
        // Click handler to deselect buttons when clicking outside
        root.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            // Check if the target or any of its parents is a button or text area
            Node target = e.getPickResult().getIntersectedNode();
            boolean isButton = false;
            boolean isTextArea = false;
            
            while (target != null) {
                if (target instanceof Button && target.getStyleClass().contains("script-button")) {
                    isButton = true;
                    break;
                }
                if (target == textArea || target.getParent() == textArea) {
                    isTextArea = true;
                    break;
                }
                // Check for text area's internal components
                if (target.getClass().getSimpleName().contains("TextArea")) {
                    isTextArea = true;
                    break;
                }
                target = target.getParent();
            }
            
            // Only clear selection if we didn't click on a script button or text area
            if (!isButton && !isTextArea) {
                // Check if we need to save changes before clearing
                if (checkAndPromptSaveChanges()) {
                    clearButtonSelection();
                    selectedButton = null;
                    textArea.clear();
                    originalContent = null;
                    contentChanged = false;
                    isVariableReplacement = false;
                }
            }
        });
    }
    
    private void setupTabsFromController() {
        // Clear ALL tabs (including the + tab)
        tabPane.getTabs().clear();
        
        // Get unique tabs from controller
        java.util.Map<String, ButtonTab> uniqueTabs = new java.util.LinkedHashMap<>();
        
        for (ButtonTab buttonTab : buttonController.getAllTabs()) {
            // Use tab name as key to ensure uniqueness
            if (!uniqueTabs.containsKey(buttonTab.getName())) {
                uniqueTabs.put(buttonTab.getName(), buttonTab);
            }
        }
        
        // Add unique tabs to UI
        for (ButtonTab buttonTab : uniqueTabs.values()) {
            addTabToUI(buttonTab);
        }
        
        // Add the "+" tab for creating new tabs
        Tab addTab = new Tab(); // Don't set text to avoid duplicate "+"
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
        SimpleStyler.styleAddTabButton(addTabButton);
        addTab.setGraphic(addTabButton);
        
        tabPane.getTabs().add(addTab);
        
        // Select first regular tab if available
        if (tabPane.getTabs().size() > 1) {
            tabPane.getSelectionModel().select(0);
        }
    }
    
    private void addTabToUI(ButtonTab buttonTab) {
        // Check if a tab with this ID already exists in the TabPane
        for (Tab existingTab : tabPane.getTabs()) {
            if (buttonTab.getId().equals(existingTab.getId())) {
                return; // Tab already exists, don't add duplicate
            }
        }
        
        Tab tab = new Tab(); // Don't set text here since we'll use a graphic label
        tab.setId(buttonTab.getId());
        
        // Create 6x6 button grid
        GridPane buttonGrid = new GridPane();
        SimpleStyler.applyDefaultLayout(buttonGrid);
        
        // Configure grid columns and rows for 6x6 with responsive sizing
        ComplexStyler.applyResponsiveGridLayout(buttonGrid, 6, 6);
        
        ScrollPane scrollPane = new ScrollPane(buttonGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        
        // Make grid pane fill the entire scroll pane
        buttonGrid.prefWidthProperty().bind(scrollPane.widthProperty());
        buttonGrid.prefHeightProperty().bind(scrollPane.heightProperty());
        
        tab.setContent(scrollPane);
        
        // Add existing buttons to grid
        addButtonsToGrid(buttonGrid, buttonTab.getButtons(), tab);
        
        // Setup drag and drop for the grid
        setupGridDragAndDrop(buttonGrid);
        
        // Add context menu to tab
        setupTabContextMenu(tab, buttonTab);
        
        tabPane.getTabs().add(tab);
    }
    
    private void addButtonsToGrid(GridPane buttonGrid, java.util.List<ScriptButton> buttons, Tab tab) {
        buttonGrid.getChildren().clear();
        
        int row = 0;
        int col = 0;
        
        for (ScriptButton scriptButton : buttons) {
            if (row >= 6) break; // Don't exceed 6x6 grid
            
            Button button = createButtonUI(scriptButton);
            
            // No need to bind height here, let grid constraints handle it
            
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
        
        // No need to bind height here, let grid constraints handle it
        
        setupButtonDragAndDrop(button, scriptButton);
        setupButtonContextMenu(button, scriptButton, tab);
        
        buttonGrid.add(button, col, row);
    }
    
    private Button createButtonUI(ScriptButton scriptButton) {
        Button button = new Button(scriptButton.getName());
        ComplexStyler.applyScriptButtonClass(button);
        SimpleStyler.makeButtonFillSpace(button);
        button.setWrapText(true);
        button.setUserData(scriptButton); // Store script button reference for drag-and-drop
        
        // Set button color
        ComplexStyler.applyButtonColor(button, scriptButton);
        
        // Apply custom hover effects that work with drag-and-drop
        HoverManager.applyHoverEffects(button);
        
        // Button click action
        button.setOnAction(e -> {
            // Check if we need to save changes before switching
            if (!checkAndPromptSaveChanges()) {
                return; // User cancelled or there was an error
            }
            
            selectedButton = scriptButton;
            
            // Process variables in the script content
            String originalContent = scriptButton.getContent();
            String processedContent = originalContent;
            
            if (VariableReplacer.hasVariables(originalContent)) {
                processedContent = VariableReplacer.replaceVariables(originalContent, scriptButton.getName());
                if (processedContent == null) {
                    // User cancelled variable input, don't proceed
                    return;
                }
            }
            
            // Set flag to indicate this text change is from variable replacement
            isVariableReplacement = true;
            textArea.setText(processedContent);
            // Store original content for change detection
            this.originalContent = scriptButton.getContent();
            this.contentChanged = false;
            // Reset the flag after text is set
            isVariableReplacement = false;
            
            // Copy to clipboard
            ClipboardContent content = new ClipboardContent();
            content.putString(processedContent);
            Clipboard.getSystemClipboard().setContent(content);
            
            // Visual feedback for selection
            clearButtonSelection();
            ComplexStyler.toggleSelectedClass(button, true);
        });
        
        
        return button;
    }
    
    private void setupButtonDragAndDrop(Button button, ScriptButton scriptButton) {
        button.setOnDragDetected(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                Dragboard dragboard = button.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                
                // Store both button ID and source tab ID for cross-tab moves
                Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                String dragData = scriptButton.getId() + ":" + currentTab.getId();
                content.putString(dragData);
                dragboard.setContent(content);
                
                // Create drag view
                WritableImage snapshot = button.snapshot(new SnapshotParameters(), null);
                dragboard.setDragView(snapshot, e.getX(), e.getY());
                
                // Mark as dragging
                button.getProperties().put("isDragging", true);
                
                // Add visual feedback
                ComplexStyler.applyDragStartVisuals(button);
                e.consume();
            }
        });
        
        button.setOnDragDone(e -> {
            ComplexStyler.applyDragEndVisuals(button);
            HoverManager.endDrag(button);
            e.consume();
        });
        
        button.setOnDragOver(e -> {
            if (e.getGestureSource() != button && e.getDragboard().hasString()) {
                e.acceptTransferModes(TransferMode.MOVE);
                ComplexStyler.addDragTargetVisual(button);
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
            ComplexStyler.removeDragTargetVisual(button);
            e.consume();
        });
        
        button.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String dragData = db.getString();
                String[] parts = dragData.split(":");
                String draggedButtonId = parts[0];
                String sourceTabId = parts.length > 1 ? parts[1] : null;
                
                GridPane grid = (GridPane) button.getParent();
                Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                
                // Check if this is a cross-tab move
                if (sourceTabId != null && !sourceTabId.equals(currentTab.getId()) && !"addTab".equals(currentTab.getId())) {
                    // Cross-tab move
                    ScriptButton draggedScriptButton = buttonController.getButton(sourceTabId, draggedButtonId);
                    if (draggedScriptButton != null) {
                        // Move button to new tab
                        if (buttonController.moveButtonBetweenTabs(sourceTabId, currentTab.getId(), draggedButtonId)) {
                            // Remove button from source tab UI
                            removeButtonFromTabUI(sourceTabId, draggedButtonId);
                            
                            // Add button to current tab
                            addButtonToTab(currentTab, draggedScriptButton);
                            
                            success = true;
                            buttonController.saveState();
                        }
                    }
                } else {
                    // Same tab move - find the dragged button
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
            }
            
            ComplexStyler.removeDragTargetVisual(button);
            e.setDropCompleted(success);
            e.consume();
        });
    }
    
    private Button findButtonById(GridPane grid, String buttonId) {
        // Handle both formats: "buttonId" and "buttonId:tabId"
        String actualButtonId = buttonId.contains(":") ? buttonId.split(":")[0] : buttonId;
        
        for (Node child : grid.getChildren()) {
            if (child instanceof Button) {
                Button btn = (Button) child;
                if (btn.getUserData() instanceof ScriptButton) {
                    ScriptButton script = (ScriptButton) btn.getUserData();
                    if (script.getId().equals(actualButtonId)) {
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
                double cellWidth = (grid.getWidth() - grid.getPadding().getLeft() - grid.getPadding().getRight() - (5 * grid.getHgap())) / 6;
                double cellHeight = (grid.getHeight() - grid.getPadding().getTop() - grid.getPadding().getBottom() - (5 * grid.getVgap())) / 6;
                
                double adjustedX = e.getX() - grid.getPadding().getLeft();
                double adjustedY = e.getY() - grid.getPadding().getTop();
                
                int col = (int) (adjustedX / (cellWidth + grid.getHgap()));
                int row = (int) (adjustedY / (cellHeight + grid.getVgap()));
                
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
                String dragData = db.getString();
                String[] parts = dragData.split(":");
                String draggedButtonId = parts[0];
                String sourceTabId = parts.length > 1 ? parts[1] : null;
                
                Tab currentTab = tabPane.getSelectionModel().getSelectedItem();
                
                // Calculate target cell
                double cellWidth = (grid.getWidth() - grid.getPadding().getLeft() - grid.getPadding().getRight() - (5 * grid.getHgap())) / 6;
                double cellHeight = (grid.getHeight() - grid.getPadding().getTop() - grid.getPadding().getBottom() - (5 * grid.getVgap())) / 6;
                
                double adjustedX = e.getX() - grid.getPadding().getLeft();
                double adjustedY = e.getY() - grid.getPadding().getTop();
                
                int targetCol = Math.max(0, Math.min(5, (int) (adjustedX / (cellWidth + grid.getHgap()))));
                int targetRow = Math.max(0, Math.min(5, (int) (adjustedY / (cellHeight + grid.getVgap()))));
                
                // Check if target cell is empty
                if (isGridCellEmpty(grid, targetCol, targetRow)) {
                    // Check if this is a cross-tab move
                    if (sourceTabId != null && !sourceTabId.equals(currentTab.getId()) && !"addTab".equals(currentTab.getId())) {
                        // Cross-tab move
                        ScriptButton draggedScriptButton = buttonController.getButton(sourceTabId, draggedButtonId);
                        if (draggedScriptButton != null) {
                            // Move button to new tab
                            if (buttonController.moveButtonBetweenTabs(sourceTabId, currentTab.getId(), draggedButtonId)) {
                                // Remove button from source tab UI
                                removeButtonFromTabUI(sourceTabId, draggedButtonId);
                                
                                // Create new button and add at specific position
                                Button newButton = createButtonUI(draggedScriptButton);
                                setupButtonDragAndDrop(newButton, draggedScriptButton);
                                setupButtonContextMenu(newButton, draggedScriptButton, currentTab);
                                
                                GridPane.setRowIndex(newButton, targetRow);
                                GridPane.setColumnIndex(newButton, targetCol);
                                grid.getChildren().add(newButton);
                                
                                success = true;
                                updateButtonOrder(grid);
                                buttonController.saveState();
                            }
                        }
                    } else {
                        // Same tab move
                        Button draggedButton = findButtonById(grid, draggedButtonId);
                        if (draggedButton != null) {
                            GridPane.setRowIndex(draggedButton, targetRow);
                            GridPane.setColumnIndex(draggedButton, targetCol);
                            success = true;
                            updateButtonOrder(grid);
                        }
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
        
        MenuItem updateItem = new MenuItem("Update");
        updateItem.setOnAction(e -> updateButtonContent(scriptButton));
        
        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(e -> showRenameButtonDialog(scriptButton, button));
        
        MenuItem changeColorItem = new MenuItem("Change Color");
        changeColorItem.setOnAction(e -> showChangeColorDialog(scriptButton, button));
        
        MenuItem duplicateItem = new MenuItem("Duplicate");
        duplicateItem.setOnAction(e -> duplicateButton(tab, scriptButton));
        
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(e -> deleteButton(tab, scriptButton, button));
        
        contextMenu.getItems().addAll(updateItem, new SeparatorMenuItem(), renameItem, changeColorItem, duplicateItem, deleteItem);
        button.setContextMenu(contextMenu);
    }
    
    private void setupTabContextMenu(Tab tab, ButtonTab buttonTab) {
        if ("addTab".equals(tab.getId())) return;
        
        ContextMenu contextMenu = new ContextMenu();
        
        MenuItem renameItem = new MenuItem("Rename Tab");
        renameItem.setOnAction(e -> showRenameTabDialog(tab));
        
        MenuItem deleteItem = new MenuItem("Delete Tab");
        deleteItem.setOnAction(e -> deleteTab(tab));
        
        contextMenu.getItems().addAll(renameItem, deleteItem);
        
        // Create a label that sizes to its content
        Label tabLabel = new Label(buttonTab.getName()); // Use ButtonTab name, not tab text
        tabLabel.setContextMenu(contextMenu);
        // Remove fixed widths to allow proportional sizing based on text content
        tabLabel.setMinWidth(Region.USE_COMPUTED_SIZE);
        tabLabel.setPrefWidth(Region.USE_COMPUTED_SIZE);
        tabLabel.setMaxWidth(Region.USE_COMPUTED_SIZE);
        
        // Add drag-and-drop support to the label (which will now be larger)
        setupTabHeaderDragAndDrop(tabLabel, tab);
        
        tab.setGraphic(tabLabel);
    }
    
    private void showCreateButtonDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Script Button");
        dialog.setHeaderText("Create a new script button");
        dialog.setContentText("Button name:");
        dialog.initStyle(StageStyle.UTILITY);
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(dialog);
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                if (selectedTab != null && !"addTab".equals(selectedTab.getId())) {
                    ScriptButton newButton = new ScriptButton(name, textArea.getText(), Color.GRAY);
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
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(dialog);
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmedName = name.trim();
            if (!trimmedName.isEmpty()) {
                if (buttonController.isTabNameDuplicate(trimmedName)) {
                    // Show error dialog for duplicate name
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Duplicate Tab Name");
                    alert.setHeaderText("Tab name already exists");
                    alert.setContentText("A tab with the name '" + trimmedName + "' already exists. Please choose a different name.");
                    
                    // Configure dialog to be independent and always on top
                    DialogUtil.configureDialog(alert);
                    alert.showAndWait();
                    
                    // Recursively show the dialog again
                    showAddTabDialog();
                    return;
                }
                
                ButtonTab newTab = new ButtonTab(trimmedName);
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
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(dialog);
        
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
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(dialog);
        
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
            scriptButton.setColor(color);
            ComplexStyler.applyButtonColor(button, scriptButton);
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
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(alert);
        
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
        ButtonTab buttonTab = buttonController.getTab(tab.getId());
        if (buttonTab == null) return;
        
        TextInputDialog dialog = new TextInputDialog(buttonTab.getName());
        dialog.setTitle("Rename Tab");
        dialog.setHeaderText("Rename tab");
        dialog.setContentText("Tab name:");
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(dialog);
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String trimmedName = name.trim();
            if (!trimmedName.isEmpty()) {
                // Check for duplicate name, excluding the current tab
                if (buttonController.isTabNameDuplicate(trimmedName, buttonTab.getId())) {
                    // Show error dialog for duplicate name
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Duplicate Tab Name");
                    alert.setHeaderText("Tab name already exists");
                    alert.setContentText("A tab with the name '" + trimmedName + "' already exists. Please choose a different name.");
                    
                    // Configure dialog to be independent and always on top
                    DialogUtil.configureDialog(alert);
                    alert.showAndWait();
                    
                    // Recursively show the dialog again
                    showRenameTabDialog(tab);
                    return;
                }
                
                buttonTab.setName(trimmedName);
                ((Label) tab.getGraphic()).setText(trimmedName);
                buttonController.saveState();
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
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(alert);
        
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
                        handleSaveShortcut();
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
                            ComplexStyler.toggleSelectedClass((Button) node, false);
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
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(alert);
        alert.showAndWait();
    }
    
    /**
     * Handles Ctrl+S shortcut - saves current button or prompts to create new one.
     */
    private void handleSaveShortcut() {
        if (selectedButton != null) {
            // Save current button content
            selectedButton.setContent(textArea.getText());
            buttonController.saveState();
            
            // Update tracking variables 
            originalContent = textArea.getText();
            contentChanged = false;
            
            showAlert("Saved", "Button '" + selectedButton.getName() + "' has been saved.");
        } else {
            // No button selected, prompt to create new one
            String currentText = textArea.getText().trim();
            if (!currentText.isEmpty()) {
                // Ask user for button name
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Save as New Button");
                dialog.setHeaderText("Create a new button with the current text");
                dialog.setContentText("Button name:");
                
                // Configure dialog to be independent and always on top
                DialogUtil.configureDialog(dialog);
                
                Optional<String> result = dialog.showAndWait();
                result.ifPresent(name -> {
                    if (!name.trim().isEmpty()) {
                        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
                        if (selectedTab != null && !"addTab".equals(selectedTab.getId())) {
                            ScriptButton newButton = new ScriptButton(name.trim(), currentText, Color.GRAY);
                            buttonController.addButtonToTab(selectedTab.getId(), newButton);
                            addButtonToTab(selectedTab, newButton);
                            buttonController.saveState();
                            
                            // Select the new button
                            selectedButton = newButton;
                            originalContent = currentText;
                            contentChanged = false;
                            
                            showAlert("Created", "New button '" + name.trim() + "' has been created and saved.");
                        }
                    }
                });
            } else {
                showAlert("No Content", "Enter some text before using Ctrl+S to save.");
            }
        }
    }
    
    private void removeButtonFromTabUI(String tabId, String buttonId) {
        // Find the tab by ID
        for (Tab tab : tabPane.getTabs()) {
            if (tabId.equals(tab.getId()) && tab.getContent() instanceof ScrollPane) {
                ScrollPane scrollPane = (ScrollPane) tab.getContent();
                if (scrollPane.getContent() instanceof GridPane) {
                    GridPane grid = (GridPane) scrollPane.getContent();
                    // Remove the button with matching ID
                    grid.getChildren().removeIf(node -> {
                        if (node instanceof Button) {
                            Button btn = (Button) node;
                            if (btn.getUserData() instanceof ScriptButton) {
                                ScriptButton scriptBtn = (ScriptButton) btn.getUserData();
                                return scriptBtn.getId().equals(buttonId);
                            }
                        }
                        return false;
                    });
                }
            }
        }
    }
    
    private void setupTabHeaderDragAndDrop(Label tabLabel, Tab tab) {
        // Add padding to ensure good click area for drag-and-drop
        tabLabel.setPadding(new Insets(8, 16, 8, 16));
        
        // Enable tab dragging for reordering
        tabLabel.setOnDragDetected(e -> {
            if (e.getButton() == MouseButton.PRIMARY && !"addTab".equals(tab.getId())) {
                Dragboard dragboard = tabLabel.startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString("TAB:" + tab.getId()); // Prefix with TAB: to distinguish from button drags
                dragboard.setContent(content);
                
                // Add visual feedback
                tabLabel.setStyle("-fx-background-color: #b0b0b0; -fx-background-radius: 3px;");
                e.consume();
            }
        });
        
        tabLabel.setOnDragDone(e -> {
            tabLabel.setStyle("");
            e.consume();
        });
        
        tabLabel.setOnDragOver(e -> {
            if (e.getDragboard().hasString() && !"addTab".equals(tab.getId())) {
                String dragData = e.getDragboard().getString();
                
                if (dragData.startsWith("TAB:")) {
                    // Tab reordering - show different visual feedback
                    e.acceptTransferModes(TransferMode.MOVE);
                    tabLabel.setStyle("-fx-background-color: #90CAF9; -fx-background-radius: 3px; -fx-border-color: #2196F3; -fx-border-width: 0 0 3 0;");
                } else {
                    // Button dropping - existing behavior
                    e.acceptTransferModes(TransferMode.MOVE);
                    tabLabel.setStyle("-fx-background-color: #e0e0e0; -fx-background-radius: 3px;");
                    
                    // Automatically switch to this tab when dragging over it
                    if (!tabPane.getSelectionModel().getSelectedItem().equals(tab)) {
                        tabPane.getSelectionModel().select(tab);
                    }
                }
            }
            e.consume();
        });
        
        tabLabel.setOnDragExited(e -> {
            tabLabel.setStyle("");
            e.consume();
        });
        
        tabLabel.setOnDragDropped(e -> {
            Dragboard db = e.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String dragData = db.getString();
                
                if (dragData.startsWith("TAB:")) {
                    // Tab reordering
                    String draggedTabId = dragData.substring(4); // Remove "TAB:" prefix
                    if (!draggedTabId.equals(tab.getId())) {
                        success = reorderTabs(draggedTabId, tab.getId());
                    }
                } else {
                    // Button dropping - existing behavior
                    String[] parts = dragData.split(":");
                    String draggedButtonId = parts[0];
                    String sourceTabId = parts.length > 1 ? parts[1] : null;
                    
                    // Only process if this is a different tab
                    if (sourceTabId != null && !sourceTabId.equals(tab.getId())) {
                        ScriptButton draggedScriptButton = buttonController.getButton(sourceTabId, draggedButtonId);
                        if (draggedScriptButton != null) {
                            // Move button to new tab
                            if (buttonController.moveButtonBetweenTabs(sourceTabId, tab.getId(), draggedButtonId)) {
                                // Remove button from source tab UI
                                removeButtonFromTabUI(sourceTabId, draggedButtonId);
                                
                                // Add button to target tab
                                addButtonToTab(tab, draggedScriptButton);
                                
                                // Switch to the target tab
                                tabPane.getSelectionModel().select(tab);
                                
                                success = true;
                                buttonController.saveState();
                            }
                        }
                    }
                }
            }
            tabLabel.setStyle("");
            e.setDropCompleted(success);
            e.consume();
        });
    }
    
    private Tab getCurrentTab() {
        return tabPane.getSelectionModel().getSelectedItem();
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
    
    /**
     * Reorders tabs by moving the dragged tab to the position of the target tab.
     * 
     * @param draggedTabId ID of the tab being dragged
     * @param targetTabId ID of the tab being dropped on
     * @return true if reordering was successful
     */
    private boolean reorderTabs(String draggedTabId, String targetTabId) {
        try {
            // Find current positions
            int draggedIndex = -1;
            int targetIndex = -1;
            
            for (int i = 0; i < tabPane.getTabs().size(); i++) {
                Tab tab = tabPane.getTabs().get(i);
                if (draggedTabId.equals(tab.getId())) {
                    draggedIndex = i;
                }
                if (targetTabId.equals(tab.getId())) {
                    targetIndex = i;
                }
            }
            
            if (draggedIndex != -1 && targetIndex != -1 && draggedIndex != targetIndex) {
                // Remove the dragged tab and insert it at the target position
                Tab draggedTab = tabPane.getTabs().remove(draggedIndex);
                
                // Adjust target index if we removed a tab before it
                if (draggedIndex < targetIndex) {
                    targetIndex--;
                }
                
                tabPane.getTabs().add(targetIndex, draggedTab);
                
                // Update the button controller tab order
                buttonController.reorderTabs(draggedTabId, targetTabId);
                buttonController.saveState();
                
                // Keep the dragged tab selected
                tabPane.getSelectionModel().select(draggedTab);
                
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    
    /**
     * Updates the button content with current text area content after confirmation.
     * @param scriptButton the button to update
     */
    private void updateButtonContent(ScriptButton scriptButton) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Update Button Content");
        alert.setHeaderText("Update \"" + scriptButton.getName() + "\" with current text?");
        alert.setContentText("This will replace the button's content with the text currently in the text area.");
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(alert);
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            scriptButton.setContent(textArea.getText());
            buttonController.saveState();
            
            // Update tracking variables if this is the currently selected button
            if (selectedButton == scriptButton) {
                originalContent = textArea.getText();
                contentChanged = false;
            }
            
            showAlert("Success", "Button content updated successfully.");
        }
    }
    
    /**
     * Checks if content has been changed and prompts user to save if needed.
     * @return true if it's safe to proceed (no changes or user saved/discarded), false if user cancelled
     */
    private boolean checkAndPromptSaveChanges() {
        if (selectedButton != null && contentChanged && originalContent != null) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save Changes");
            alert.setHeaderText("Save changes to \"" + selectedButton.getName() + "\"?");
            alert.setContentText("You have unsaved changes. Do you want to save them?");
            
            // Configure dialog to be independent and always on top
            DialogUtil.configureDialog(alert);
            
            ButtonType saveButton = new ButtonType("Save", ButtonBar.ButtonData.YES);
            ButtonType discardButton = new ButtonType("Discard", ButtonBar.ButtonData.NO);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(saveButton, discardButton, cancelButton);
            
            Optional<ButtonType> result = alert.showAndWait();
            
            if (result.isPresent()) {
                if (result.get() == saveButton) {
                    // Save the changes
                    selectedButton.setContent(textArea.getText());
                    buttonController.saveState();
                    originalContent = textArea.getText();
                    contentChanged = false;
                    return true;
                } else if (result.get() == discardButton) {
                    // Discard changes
                    contentChanged = false;
                    return true;
                } else {
                    // Cancel - don't proceed
                    return false;
                }
            } else {
                // Dialog was closed without selection - treat as cancel
                return false;
            }
        }
        return true; // No changes to save
    }
}