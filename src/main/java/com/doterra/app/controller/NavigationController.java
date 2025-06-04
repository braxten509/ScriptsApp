package com.doterra.app.controller;

import com.doterra.app.view.ChatScriptsPanel;
import com.doterra.app.view.EmailScriptsPanel;
import com.doterra.app.view.RegexEditorPanel;
import com.doterra.app.view.CalculatorPanel;
import com.doterra.app.view.TodoPanel;
import com.doterra.app.view.StickyNotePanel;
import com.doterra.app.view.CalendarPanel;
import com.doterra.app.view.ImageNotesPanel;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.geometry.Pos;

public class NavigationController {

    private final BorderPane mainContainer;
    
    // Pre-initialized panels (lightweight)
    private final ChatScriptsPanel chatScriptsPanel;
    private final EmailScriptsPanel emailScriptsPanel;
    private final CalculatorPanel calculatorPanel;
    
    // Lazy-loaded panels (heavyweight due to file I/O)
    private RegexEditorPanel regexEditorPanel;
    private TodoPanel todoPanel;
    private StickyNotePanel stickyNotePanel;
    private CalendarPanel calendarPanel;
    private ImageNotesPanel imageNotesPanel;
    
    public NavigationController(BorderPane mainContainer, 
                               ChatScriptsPanel chatScriptsPanel,
                               EmailScriptsPanel emailScriptsPanel,
                               CalculatorPanel calculatorPanel) {
        this.mainContainer = mainContainer;
        this.chatScriptsPanel = chatScriptsPanel;
        this.emailScriptsPanel = emailScriptsPanel;
        this.calculatorPanel = calculatorPanel;
        
        // Heavyweight panels will be lazily loaded
        this.regexEditorPanel = null;
        this.todoPanel = null;
        this.stickyNotePanel = null;
        this.calendarPanel = null;
        this.imageNotesPanel = null;
    }
    
    public void showPanel(String panelId) {
        switch (panelId.toLowerCase()) {
            case "chat":
                mainContainer.setCenter(chatScriptsPanel.getRoot());
                break;
            case "email":
                mainContainer.setCenter(emailScriptsPanel.getRoot());
                break;
            case "regex":
                loadAndShowPanel(() -> {
                    if (regexEditorPanel == null) {
                        regexEditorPanel = new RegexEditorPanel();
                    }
                    return regexEditorPanel;
                });
                break;
            case "calculator":
                mainContainer.setCenter(calculatorPanel);
                break;
            case "todo":
                loadAndShowPanel(() -> {
                    if (todoPanel == null) {
                        todoPanel = new TodoPanel();
                    }
                    return todoPanel;
                });
                break;
            case "stickynote":
                loadAndShowPanel(() -> {
                    if (stickyNotePanel == null) {
                        stickyNotePanel = new StickyNotePanel();
                    }
                    return stickyNotePanel.getRoot();
                });
                break;
            case "calendar":
                loadAndShowPanel(() -> {
                    if (calendarPanel == null) {
                        // Get TodoPanel first if needed
                        if (todoPanel == null) {
                            todoPanel = new TodoPanel();
                        }
                        calendarPanel = new CalendarPanel(todoPanel);
                    }
                    return calendarPanel;
                });
                break;
            case "imagenotes":
                loadAndShowPanel(() -> {
                    if (imageNotesPanel == null) {
                        imageNotesPanel = new ImageNotesPanel();
                    }
                    return imageNotesPanel;
                });
                break;
            default:
                // Default to chat scripts panel
                mainContainer.setCenter(chatScriptsPanel.getRoot());
                break;
        }
    }
    
    /**
     * Load a panel asynchronously and show a loading indicator
     */
    private void loadAndShowPanel(java.util.function.Supplier<javafx.scene.Node> panelLoader) {
        // Show loading indicator
        Label loadingLabel = new Label("Loading...");
        loadingLabel.setAlignment(Pos.CENTER);
        loadingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
        mainContainer.setCenter(loadingLabel);
        
        // Load panel asynchronously
        com.doterra.app.util.AsyncFileOperations.executeAsync(
            () -> {
                // This will run on background thread, but panel creation must be on JavaFX thread
                javafx.application.Platform.runLater(() -> {
                    try {
                        javafx.scene.Node panel = panelLoader.get();
                        mainContainer.setCenter(panel);
                    } catch (Exception e) {
                        Label errorLabel = new Label("Error loading panel: " + e.getMessage());
                        errorLabel.setAlignment(Pos.CENTER);
                        errorLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: red;");
                        mainContainer.setCenter(errorLabel);
                        e.printStackTrace();
                    }
                });
            }
        );
    }
    
    /**
     * Get the TodoPanel for external access (for calendar integration)
     */
    public TodoPanel getTodoPanel() {
        if (todoPanel == null) {
            todoPanel = new TodoPanel();
        }
        return todoPanel;
    }
}