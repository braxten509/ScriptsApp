package com.doterra.app.controller;

import com.doterra.app.view.ChatScriptsPanel;
import com.doterra.app.view.EmailScriptsPanel;
import com.doterra.app.view.RegexEditorPanel;
import javafx.scene.layout.BorderPane;

public class NavigationController {

    private final BorderPane mainContainer;
    private final ChatScriptsPanel chatScriptsPanel;
    private final EmailScriptsPanel emailScriptsPanel;
    private final RegexEditorPanel regexEditorPanel;
    
    public NavigationController(BorderPane mainContainer, 
                               ChatScriptsPanel chatScriptsPanel,
                               EmailScriptsPanel emailScriptsPanel,
                               RegexEditorPanel regexEditorPanel) {
        this.mainContainer = mainContainer;
        this.chatScriptsPanel = chatScriptsPanel;
        this.emailScriptsPanel = emailScriptsPanel;
        this.regexEditorPanel = regexEditorPanel;
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
            default:
                // Default to chat scripts panel
                mainContainer.setCenter(chatScriptsPanel.getRoot());
                break;
        }
    }
}