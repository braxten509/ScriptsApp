package com.doterra.app.controller;

import com.doterra.app.view.ChatScriptsPanel;
import com.doterra.app.view.EmailScriptsPanel;
import com.doterra.app.view.RegexEditorPanel;
import com.doterra.app.view.CalculatorPanel;
import com.doterra.app.view.TodoPanel;
import com.doterra.app.view.StickyNotePanel;
import com.doterra.app.view.CalendarPanel;
import com.doterra.app.view.ImageNotesPanel;
import com.doterra.app.model.NavigationSection;
import com.doterra.app.util.NavigationPreferences;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.Label;
import javafx.geometry.Pos;
import java.util.ArrayList;
import java.util.List;

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
    
    // Navigation structure
    private NavigationPreferences navigationPreferences;
    private List<NavigationSection> navigationSections;
    
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
        
        // Initialize navigation structure
        this.navigationPreferences = NavigationPreferences.load();
        this.navigationSections = createNavigationSections();
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
    
    /**
     * Create the navigation sections structure
     */
    private List<NavigationSection> createNavigationSections() {
        List<NavigationSection> sections = new ArrayList<>();
        
        // Basic section (expanded by default)
        NavigationSection basicSection = new NavigationSection("Basic", navigationPreferences.isSectionExpanded("Basic"));
        basicSection.addItem("Chat Scripts", "chat");
        basicSection.addItem("Email Scripts", "email");
        basicSection.addItem("Todo", "todo", true); // Has badge
        basicSection.addItem("Sticky Notes", "stickynote");
        basicSection.addItem("Calendar", "calendar");
        sections.add(basicSection);
        
        // Advanced section (collapsed by default)
        NavigationSection advancedSection = new NavigationSection("Advanced", navigationPreferences.isSectionExpanded("Advanced"));
        advancedSection.addItem("Regex Editor", "regex");
        advancedSection.addItem("Calculator", "calculator");
        advancedSection.addItem("Image Notes", "imagenotes");
        sections.add(advancedSection);
        
        return sections;
    }
    
    /**
     * Get the navigation sections
     */
    public List<NavigationSection> getNavigationSections() {
        return navigationSections;
    }
    
    /**
     * Toggle a section's expanded/collapsed state
     */
    public void toggleSection(String sectionTitle) {
        NavigationSection section = navigationSections.stream()
            .filter(s -> s.getTitle().equals(sectionTitle))
            .findFirst()
            .orElse(null);
        
        if (section != null) {
            boolean newState = !section.isExpanded();
            section.setExpanded(newState);
            navigationPreferences.setSectionExpanded(sectionTitle, newState);
            navigationPreferences.save();
        }
    }
    
    /**
     * Get navigation preferences
     */
    public NavigationPreferences getNavigationPreferences() {
        return navigationPreferences;
    }
    
    /**
     * Cleanup all panels that have been created
     */
    public void cleanup() {
        if (todoPanel != null) {
            todoPanel.cleanup();
        }
        if (calendarPanel != null) {
            calendarPanel.cleanup();
        }
        if (regexEditorPanel != null) {
            regexEditorPanel.cleanup();
        }
        
        // Save navigation preferences on cleanup
        if (navigationPreferences != null) {
            navigationPreferences.save();
        }
    }
}