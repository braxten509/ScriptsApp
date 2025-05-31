package com.doterra.app.controller;

import com.doterra.app.view.ChatScriptsPanel;
import com.doterra.app.view.EmailScriptsPanel;
import com.doterra.app.view.RegexEditorPanel;
import com.doterra.app.view.CalculatorPanel;
import com.doterra.app.view.TodoPanel;
import com.doterra.app.view.StickyNotePanel;
import javafx.scene.layout.BorderPane;

public class NavigationController {

    private final BorderPane mainContainer;
    private final ChatScriptsPanel chatScriptsPanel;
    private final EmailScriptsPanel emailScriptsPanel;
    private final RegexEditorPanel regexEditorPanel;
    private final CalculatorPanel calculatorPanel;
    private final TodoPanel todoPanel;
    private final StickyNotePanel stickyNotePanel;
    
    public NavigationController(BorderPane mainContainer, 
                               ChatScriptsPanel chatScriptsPanel,
                               EmailScriptsPanel emailScriptsPanel,
                               RegexEditorPanel regexEditorPanel,
                               CalculatorPanel calculatorPanel,
                               TodoPanel todoPanel,
                               StickyNotePanel stickyNotePanel) {
        this.mainContainer = mainContainer;
        this.chatScriptsPanel = chatScriptsPanel;
        this.emailScriptsPanel = emailScriptsPanel;
        this.regexEditorPanel = regexEditorPanel;
        this.calculatorPanel = calculatorPanel;
        this.todoPanel = todoPanel;
        this.stickyNotePanel = stickyNotePanel;
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
                mainContainer.setCenter(regexEditorPanel);
                break;
            case "calculator":
                mainContainer.setCenter(calculatorPanel);
                break;
            case "todo":
                mainContainer.setCenter(todoPanel);
                break;
            case "stickynote":
                mainContainer.setCenter(stickyNotePanel.getRoot());
                break;
            default:
                // Default to chat scripts panel
                mainContainer.setCenter(chatScriptsPanel.getRoot());
                break;
        }
    }
}