package com.doterra.app.view;

import com.doterra.app.controller.NavigationController;
import com.doterra.app.model.NavigationSection;
import com.doterra.app.util.CssInspector;
import com.doterra.app.util.SimpleStyler;
import com.doterra.app.util.ComplexStyler;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.beans.property.IntegerProperty;
import javafx.stage.Stage;
import javafx.scene.layout.Region;
import javafx.animation.RotateTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import javafx.scene.control.ListView;
import javafx.scene.control.ListCell;
import javafx.scene.control.MultipleSelectionModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class MainView {
    
    private final BorderPane root;
    private final VBox sidebar;
    private final NavigationController navigationController;
    private final CssInspector cssInspector;
    
    // Pre-initialized lightweight panels
    private final ChatScriptsPanel chatScriptsPanel;
    private final EmailScriptsPanel emailScriptsPanel;
    private final CalculatorPanel calculatorPanel;
    
    // Track active navigation item
    private String activeNavItem;
    
    public MainView() {
        root = new BorderPane();
        
        // Initialize CSS inspector
        cssInspector = new CssInspector();
        
        // Initialize only lightweight panels immediately (no file I/O)
        chatScriptsPanel = new ChatScriptsPanel();
        emailScriptsPanel = new EmailScriptsPanel();
        calculatorPanel = new CalculatorPanel();
        
        // Set up navigation controller with lazy loading for heavy panels
        navigationController = new NavigationController(root, chatScriptsPanel, emailScriptsPanel, calculatorPanel);
        
        // Create sidebar
        sidebar = createSidebar();
        root.setLeft(sidebar);
        
        // Set the initial panel (Chat Scripts)
        navigationController.showPanel("chat");
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPadding(new Insets(15));
        SimpleStyler.styleSidebar(sidebar);
        sidebar.setPrefWidth(200);
        sidebar.setAlignment(Pos.TOP_CENTER);
        
        // App title/logo
        Label titleLabel = new Label("doTERRA App");
        SimpleStyler.styleTitleLabel(titleLabel);
        titleLabel.setPadding(new Insets(0, 0, 20, 0));
        
        // Create navigation sections
        VBox navigationContainer = new VBox(5);
        for (NavigationSection section : navigationController.getNavigationSections()) {
            VBox sectionContainer = createNavigationSection(section);
            navigationContainer.getChildren().add(sectionContainer);
        }
        
        // Add spacer to push content to top
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        // Create feature panel at bottom
        HBox featurePanel = createFeaturePanel();
        
        sidebar.getChildren().addAll(titleLabel, navigationContainer, spacer, featurePanel);
        return sidebar;
    }
    
    private VBox createNavigationSection(NavigationSection section) {
        VBox sectionContainer = new VBox(3);
        
        // Section header button
        Button sectionHeader = new Button(section.getTitle() + (section.isExpanded() ? " ▼" : " ▶"));
        sectionHeader.setMaxWidth(Double.MAX_VALUE);
        sectionHeader.setPrefHeight(35);
        sectionHeader.setStyle(
            "-fx-background-color: #37474F; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 13px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 12; " +
            "-fx-background-radius: 3; " +
            "-fx-cursor: hand; " +
            "-fx-alignment: center-left;"
        );
        
        // Section header hover effect
        sectionHeader.setOnMouseEntered(e -> 
            sectionHeader.setStyle(sectionHeader.getStyle().replace("#37474F", "#455A64"))
        );
        sectionHeader.setOnMouseExited(e -> 
            sectionHeader.setStyle(sectionHeader.getStyle().replace("#455A64", "#37474F"))
        );
        
        // Create ListView for navigation items
        ListView<NavigationSection.NavigationItem> itemsList = new ListView<>();
        ObservableList<NavigationSection.NavigationItem> items = FXCollections.observableArrayList(section.getItems());
        itemsList.setItems(items);
        
        // Calculate height based on number of items
        int itemHeight = 32; // Height per item
        int listHeight = section.getItems().size() * itemHeight;
        itemsList.setPrefHeight(listHeight);
        itemsList.setMaxHeight(listHeight);
        itemsList.setMinHeight(listHeight);
        
        // Style the ListView to completely hide selection
        itemsList.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-background-insets: 0; " +
            "-fx-padding: 0; " +
            "-fx-border-width: 0; " +
            "-fx-focus-color: transparent; " +
            "-fx-faint-focus-color: transparent; " +
            "-fx-selection-bar: transparent; " +
            "-fx-selection-bar-non-focused: transparent; " +
            "-fx-cell-focus-inner-border: transparent; " +
            "-fx-cell-border: transparent; " +
            "-fx-cell-selection-border: transparent;"
        );
        
        // Disable focus traversal to prevent keyboard selection
        itemsList.setFocusTraversable(false);
        
        // Create a custom selection model that doesn't visually select items
        itemsList.setSelectionModel(new NoSelectionModel<>());
        
        // Force clear any initial selection
        itemsList.getSelectionModel().clearSelection();
        
        // Ensure ListView doesn't create extra cells
        itemsList.setFixedCellSize(itemHeight);
        
        // Custom cell factory for navigation items
        itemsList.setCellFactory(listView -> new ListCell<NavigationSection.NavigationItem>() {
            private HBox badgeContainer;
            private Label badge;
            
            @Override
            protected void updateItem(NavigationSection.NavigationItem item, boolean empty) {
                super.updateItem(item, empty);
                
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle(getEmptyCellStyle());
                    setOnMouseClicked(null);
                    setOnMouseEntered(null);
                    setOnMouseExited(null);
                } else {
                    setText(item.getLabel());
                    
                    // Handle badge for todo item
                    if (item.hasBadge() && item.getPanelId().equals("todo")) {
                        if (badgeContainer == null) {
                            // Create HBox to position text on left and badge on right
                            HBox container = new HBox();
                            container.setAlignment(Pos.CENTER_LEFT);
                            container.setSpacing(5);
                            
                            // Create label for the text
                            Label textLabel = new Label();
                            textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                            
                            // Create spacer to push badge to the right
                            Region spacer = new Region();
                            HBox.setHgrow(spacer, Priority.ALWAYS);
                            
                            // Create badge
                            badge = new Label();
                            badge.setStyle(
                                "-fx-background-color: #f44336; " +
                                "-fx-text-fill: white; " +
                                "-fx-font-size: 9px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 1 4 1 4; " +
                                "-fx-background-radius: 8; " +
                                "-fx-min-width: 16; " +
                                "-fx-min-height: 16; " +
                                "-fx-alignment: center;"
                            );
                            
                            container.getChildren().addAll(textLabel, spacer, badge);
                            badgeContainer = container;
                        }
                        
                        // Update text label
                        HBox container = (HBox) badgeContainer;
                        Label textLabel = (Label) container.getChildren().get(0);
                        textLabel.setText(item.getLabel());
                        
                        // Update text color based on current cell state
                        if (item.getPanelId().equals(activeNavItem)) {
                            textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                        } else {
                            textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                        }
                        
                        // Clear the cell text since we're using the graphic
                        setText(null);
                        
                        // Bind badge to todo count
                        IntegerProperty todoCount = navigationController.getTodoPanel().readyTodoCountProperty();
                        todoCount.addListener((obs, oldCount, newCount) -> {
                            if (newCount.intValue() > 0) {
                                badge.setText(String.valueOf(newCount.intValue()));
                                badge.setVisible(true);
                            } else {
                                badge.setVisible(false);
                            }
                        });
                        
                        // Initial setup
                        if (todoCount.get() > 0) {
                            badge.setText(String.valueOf(todoCount.get()));
                            badge.setVisible(true);
                        } else {
                            badge.setVisible(false);
                        }
                        
                        setGraphic(badgeContainer);
                    } else {
                        setGraphic(null);
                    }
                    
                    // Apply active style if this is the active item
                    if (item.getPanelId().equals(activeNavItem)) {
                        setStyle(getActiveCellStyle());
                    } else {
                        setStyle(getDefaultCellStyle());
                    }
                    
                    // Set up hover effects
                    setOnMouseEntered(e -> {
                        if (!item.getPanelId().equals(activeNavItem)) {
                            setStyle(getHoverCellStyle());
                            updateTextLabelColor(item, false);
                        }
                    });
                    
                    setOnMouseExited(e -> {
                        if (item.getPanelId().equals(activeNavItem)) {
                            setStyle(getActiveCellStyle());
                            updateTextLabelColor(item, true);
                        } else {
                            setStyle(getDefaultCellStyle());
                            updateTextLabelColor(item, false);
                        }
                    });
                    
                    // Handle selection
                    setOnMouseClicked(e -> {
                        setActiveNavItem(item.getPanelId());
                        navigationController.showPanel(item.getPanelId());
                        // Update all cells in all sections
                        updateAllNavigationCells();
                    });
                }
            }
            
            private String getDefaultCellStyle() {
                return "-fx-background-color: transparent; " +
                       "-fx-text-fill: white; " +
                       "-fx-font-size: 12px; " +
                       "-fx-padding: 6 12 6 20; " +
                       "-fx-border-width: 0; " +
                       "-fx-background-radius: 0; " +
                       "-fx-cursor: hand; " +
                       "-fx-cell-selection-border: transparent; " +
                       "-fx-focus-color: transparent; " +
                       "-fx-selection-bar: transparent;";
            }
            
            private String getHoverCellStyle() {
                return "-fx-background-color: #455A64; " +
                       "-fx-text-fill: white; " +
                       "-fx-font-size: 12px; " +
                       "-fx-padding: 6 12 6 20; " +
                       "-fx-border-width: 0; " +
                       "-fx-background-radius: 0; " +
                       "-fx-cursor: hand; " +
                       "-fx-cell-selection-border: transparent; " +
                       "-fx-focus-color: transparent; " +
                       "-fx-selection-bar: transparent;";
            }
            
            private String getActiveCellStyle() {
                return "-fx-background-color: #2196F3; " +
                       "-fx-text-fill: white; " +
                       "-fx-font-size: 12px; " +
                       "-fx-font-weight: bold; " +
                       "-fx-padding: 6 12 6 20; " +
                       "-fx-border-width: 0; " +
                       "-fx-background-radius: 0; " +
                       "-fx-cursor: hand; " +
                       "-fx-cell-selection-border: transparent; " +
                       "-fx-focus-color: transparent; " +
                       "-fx-selection-bar: transparent;";
            }
            
            private String getEmptyCellStyle() {
                return "-fx-background-color: transparent !important; " +
                       "-fx-text-fill: transparent; " +
                       "-fx-font-size: 0px; " +
                       "-fx-padding: 0; " +
                       "-fx-border-width: 0; " +
                       "-fx-background-radius: 0; " +
                       "-fx-cursor: default; " +
                       "-fx-cell-selection-border: transparent !important; " +
                       "-fx-focus-color: transparent !important; " +
                       "-fx-selection-bar: transparent !important; " +
                       "-fx-selection-bar-non-focused: transparent !important;";
            }
            
            private void updateTextLabelColor(NavigationSection.NavigationItem item, boolean isActive) {
                if (item.hasBadge() && item.getPanelId().equals("todo") && badgeContainer != null) {
                    if (!badgeContainer.getChildren().isEmpty() && badgeContainer.getChildren().get(0) instanceof Label) {
                        Label textLabel = (Label) badgeContainer.getChildren().get(0);
                        if (isActive) {
                            textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold;");
                        } else {
                            textLabel.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                        }
                    }
                }
            }
        });
        
        // Set initial active item (Chat Scripts)
        if (section.getTitle().equals("Basic") && !section.getItems().isEmpty()) {
            setActiveNavItem(section.getItems().get(0).getPanelId());
        }
        
        // Set initial visibility based on expanded state
        itemsList.setVisible(section.isExpanded());
        itemsList.setManaged(section.isExpanded());
        
        // Section header click handler
        sectionHeader.setOnAction(e -> {
            navigationController.toggleSection(section.getTitle());
            boolean newExpanded = section.isExpanded();
            itemsList.setVisible(newExpanded);
            itemsList.setManaged(newExpanded);
            sectionHeader.setText(section.getTitle() + (newExpanded ? " ▼" : " ▶"));
        });
        
        sectionContainer.getChildren().addAll(sectionHeader, itemsList);
        return sectionContainer;
    }
    
    private void setActiveNavItem(String panelId) {
        activeNavItem = panelId;
    }
    
    private void updateAllNavigationCells() {
        // Force refresh of all ListView cells to update active states
        VBox navigationContainer = (VBox) ((VBox) root.getLeft()).getChildren().get(1);
        for (javafx.scene.Node sectionNode : navigationContainer.getChildren()) {
            if (sectionNode instanceof VBox) {
                VBox sectionContainer = (VBox) sectionNode;
                for (javafx.scene.Node child : sectionContainer.getChildren()) {
                    if (child instanceof ListView) {
                        @SuppressWarnings("unchecked")
                        ListView<NavigationSection.NavigationItem> listView = (ListView<NavigationSection.NavigationItem>) child;
                        listView.refresh();
                    }
                }
            }
        }
    }
    
    
    private HBox createFeaturePanel() {
        HBox panel = new HBox(5);
        panel.setAlignment(Pos.CENTER_LEFT);
        panel.setPadding(new Insets(10, 0, 10, 0));
        
        // CSS Inspector toggle button
        ToggleButton cssInspectorBtn = new ToggleButton("CSS");
        cssInspectorBtn.setPrefSize(35, 35);
        SimpleStyler.styleCssInspectorButton(cssInspectorBtn);
        cssInspectorBtn.setTooltip(new javafx.scene.control.Tooltip("Toggle CSS Inspector - Shows CSS classes on hover"));
        
        // Style for selected state
        ComplexStyler.applyCssInspectorToggleEffect(cssInspectorBtn);
        
        // Handle CSS inspector enable/disable logic
        cssInspectorBtn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                // Enable CSS inspector when scene is available
                if (root.getScene() != null) {
                    cssInspector.enable(root.getScene());
                } else {
                    // Wait for scene to be available
                    root.sceneProperty().addListener((sceneObs, oldScene, newScene) -> {
                        if (newScene != null && cssInspectorBtn.isSelected()) {
                            cssInspector.enable(newScene);
                        }
                    });
                }
            } else {
                cssInspector.disable();
            }
        });
        
        // Pin to top toggle button
        ToggleButton pinBtn = new ToggleButton("📌");
        pinBtn.setPrefSize(35, 35);
        SimpleStyler.styleCssInspectorButton(pinBtn);
        pinBtn.setTooltip(new javafx.scene.control.Tooltip("Pin app to stay on top of other windows"));
        
        // Style for selected state
        ComplexStyler.applyCssInspectorToggleEffect(pinBtn);
        
        // Handle pin to top logic
        pinBtn.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            Stage stage = (Stage) root.getScene().getWindow();
            if (stage != null) {
                stage.setAlwaysOnTop(isSelected);
            }
        });
        
        panel.getChildren().addAll(cssInspectorBtn, pinBtn);
        return panel;
    }
    
    public BorderPane getRoot() {
        return root;
    }
    
    public NavigationController getNavigationController() {
        return navigationController;
    }
    
    /**
     * Get the TodoPanel (will be created if not already loaded)
     */
    public TodoPanel getTodoPanel() {
        return navigationController.getTodoPanel();
    }
    
    /**
     * Cleanup method to be called when the application is closing.
     */
    public void cleanup() {
        // Shutdown async file operations
        com.doterra.app.util.AsyncFileOperations.shutdown();
        
        // Cleanup panels if they exist
        NavigationController navController = getNavigationController();
        if (navController != null) {
            navController.cleanup();
        }
    }
    
    /**
     * Custom selection model that prevents visual selection but doesn't cause NPE
     */
    private static class NoSelectionModel<T> extends MultipleSelectionModel<T> {
        
        @Override
        public ObservableList<Integer> getSelectedIndices() {
            return FXCollections.emptyObservableList();
        }
        
        @Override
        public ObservableList<T> getSelectedItems() {
            return FXCollections.emptyObservableList();
        }
        
        @Override
        public void selectIndices(int index, int... indices) {
            // Do nothing - prevent selection
        }
        
        @Override
        public void selectAll() {
            // Do nothing - prevent selection
        }
        
        @Override
        public void selectFirst() {
            // Do nothing - prevent selection
        }
        
        @Override
        public void selectLast() {
            // Do nothing - prevent selection
        }
        
        @Override
        public void clearAndSelect(int index) {
            // Do nothing - prevent selection
        }
        
        @Override
        public void select(int index) {
            // Do nothing - prevent selection
        }
        
        @Override
        public void select(T obj) {
            // Do nothing - prevent selection
        }
        
        @Override
        public void clearSelection(int index) {
            // Do nothing - already no selection
        }
        
        @Override
        public void clearSelection() {
            // Do nothing - already no selection
        }
        
        @Override
        public boolean isSelected(int index) {
            return false; // Never selected
        }
        
        @Override
        public boolean isEmpty() {
            return true; // Always empty selection
        }
        
        @Override
        public void selectPrevious() {
            // Do nothing - prevent selection
        }
        
        @Override
        public void selectNext() {
            // Do nothing - prevent selection
        }
    }
}