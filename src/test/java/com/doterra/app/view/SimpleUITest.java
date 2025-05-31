package com.doterra.app.view;

import com.doterra.app.TestFXBase;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SimpleUITest extends TestFXBase {
    
    private ChatScriptsPanel chatPanel;
    
    @Override
    protected void setupStage(Stage stage) {
        chatPanel = new ChatScriptsPanel();
        Scene scene = new Scene(chatPanel.getRoot(), 400, 300);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    public void testChatPanelLoads() {
        // Simple test to verify the panel loads without crashing
        assertNotNull(chatPanel);
        assertNotNull(chatPanel.getRoot());
        
        // Verify the Add Script button exists
        Button addButton = lookup("Add Script").query();
        assertNotNull(addButton);
    }
}