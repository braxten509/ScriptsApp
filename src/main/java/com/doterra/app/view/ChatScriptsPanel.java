package com.doterra.app.view;

import com.doterra.app.controller.ButtonController;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import com.doterra.app.util.ColorUtil;
import com.doterra.app.util.SimpleStyler;
import com.doterra.app.util.ComplexStyler;
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
        SimpleStyler.applyDefaultLayout(root);
        
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
        SimpleStyler.setTextAreaHeight(textArea);
        
        // Create controls
        Button addButton = new Button("Add Script");
        addButton.setOnAction(e -> showCreateButtonDialog());
        
        HBox controls = new HBox(10, addButton);
        controls.setPadding(new Insets(5, 0, 5, 0));
        controls.setAlignment(Pos.CENTER_LEFT);
        
        // Build UI
        setupTabsFromController();
        
        VBox centerSection = new VBox(5, controls, tabPane);
        VBox.setVgrow(tabPane, Priority.ALWAYS); // Make tabPane expand to fill available space
        root.setCenter(centerSection);
        root.setBottom(textArea);
        
        // Keyboard shortcuts
        setupKeyboardShortcuts();
        
        // Click handler to deselect buttons when clicking outside
        root.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            // Check if the target or any of its parents is a button
            Node target = e.getPickResult().getIntersectedNode();
            boolean isButton = false;
            
            while (target != null) {
                if (target instanceof Button && target.getStyleClass().contains("script-button")) {
                    isButton = true;
                    break;
                }
                target = target.getParent();
            }
            
            // Only clear selection if we didn't click on a script button
            if (!isButton) {
                clearButtonSelection();
                selectedButton = null;
                textArea.clear();
            }
        });
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
        SimpleStyler.styleAddTabButton(addTabButton);
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
            ComplexStyler.toggleSelectedClass(button, true);
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
                ComplexStyler.applyDragStartVisuals(button);
                e.consume();
            }
        });
        
        button.setOnDragDone(e -> {
            ComplexStyler.applyDragEndVisuals(button);
            
            // Force JavaFX to re-evaluate hover states by simulating mouse movement
            javafx.application.Platform.runLater(() -> {
                // Get the scene and current mouse position
                if (button.getScene() != null) {
                    double mouseX = e.getScreenX();
                    double mouseY = e.getScreenY();
                    
                    // Convert screen coordinates to scene coordinates
                    javafx.geometry.Point2D sceneCoords = button.getScene().getRoot().screenToLocal(mouseX, mouseY);
                    
                    // Fire a mouse moved event on the scene to trigger hover detection
                    MouseEvent moveEvent = new MouseEvent(MouseEvent.MOUSE_MOVED,
                        sceneCoords.getX(), sceneCoords.getY(), mouseX, mouseY,
                        MouseButton.NONE, 0, false, false, false, false,
                        false, false, false, false, false, false, null);
                    
                    button.getScene().getRoot().fireEvent(moveEvent);
                }
            });
            
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
            
            ComplexStyler.removeDragTargetVisual(button);
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
                String draggedButtonId = db.getString();
                Button draggedButton = findButtonById(grid, draggedButtonId);
                
                if (draggedButton != null) {
                    // Calculate target cell
                    double cellWidth = (grid.getWidth() - grid.getPadding().getLeft() - grid.getPadding().getRight() - (5 * grid.getHgap())) / 6;
                    double cellHeight = (grid.getHeight() - grid.getPadding().getTop() - grid.getPadding().getBottom() - (5 * grid.getVgap())) / 6;
                    
                    double adjustedX = e.getX() - grid.getPadding().getLeft();
                    double adjustedY = e.getY() - grid.getPadding().getTop();
                    
                    int targetCol = Math.max(0, Math.min(5, (int) (adjustedX / (cellWidth + grid.getHgap()))));
                    int targetRow = Math.max(0, Math.min(5, (int) (adjustedY / (cellHeight + grid.getVgap()))));
                    
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
            String trimmedName = name.trim();
            if (!trimmedName.isEmpty()) {
                if (buttonController.isTabNameDuplicate(trimmedName)) {
                    // Show error dialog for duplicate name
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Duplicate Tab Name");
                    alert.setHeaderText("Tab name already exists");
                    alert.setContentText("A tab with the name '" + trimmedName + "' already exists. Please choose a different name.");
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
            String trimmedName = name.trim();
            if (!trimmedName.isEmpty()) {
                ButtonTab buttonTab = buttonController.getTab(tab.getId());
                if (buttonTab != null) {
                    // Check for duplicate name, excluding the current tab
                    if (buttonController.isTabNameDuplicate(trimmedName, buttonTab.getId())) {
                        // Show error dialog for duplicate name
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Duplicate Tab Name");
                        alert.setHeaderText("Tab name already exists");
                        alert.setContentText("A tab with the name '" + trimmedName + "' already exists. Please choose a different name.");
                        alert.showAndWait();
                        
                        // Recursively show the dialog again
                        showRenameTabDialog(tab);
                        return;
                    }
                    
                    buttonTab.setName(trimmedName);
                    tab.setText(trimmedName);
                    ((Label) tab.getGraphic()).setText(trimmedName);
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