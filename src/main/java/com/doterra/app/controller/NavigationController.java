package com.doterra.app.controller;

import com.doterra.app.view.ChatScriptsPanel;
import com.doterra.app.view.EmailScriptsPanel;
import javafx.scene.layout.BorderPane;

public class NavigationController {

    private final BorderPane mainContainer;
    private final ChatScriptsPanel chatScriptsPanel;
    private final EmailScriptsPanel emailScriptsPanel;
    
    public NavigationController(BorderPane mainContainer, 
                               ChatScriptsPanel chatScriptsPanel,
                               EmailScriptsPanel emailScriptsPanel) {
        this.mainContainer = mainContainer;
        this.chatScriptsPanel = chatScriptsPanel;
        this.emailScriptsPanel = emailScriptsPanel;
    }
    
    public void showPanel(String panelId) {
        switch (panelId.toLowerCase()) {
            case "chat":
                mainContainer.setCenter(chatScriptsPanel.getRoot());
                break;
            case "email":
                mainContainer.setCenter(emailScriptsPanel.getRoot());
                break;
            default:
                // Default to chat scripts panel
                mainContainer.setCenter(chatScriptsPanel.getRoot());
                break;
        }
    }
}