package com.doterra.app.view;

import com.doterra.app.TestFXBase;
import com.doterra.app.controller.ButtonController;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DebugTabTest extends TestFXBase {
    
    private ChatScriptsPanel chatPanel;
    private ButtonController controller;
    
    @Override
    protected void setupStage(Stage stage) {
        chatPanel = new ChatScriptsPanel();
        controller = chatPanel.getButtonController();
        
        // Create initial test data
        ButtonTab tab1 = new ButtonTab("Tab 1");
        tab1.addButton(new ScriptButton("Button 1", "Content 1", Color.LIGHTBLUE));
        controller.addTab(tab1);
        
        ButtonTab tab2 = new ButtonTab("Tab 2");
        tab2.addButton(new ScriptButton("Button 2", "Content 2", Color.LIGHTGREEN));
        controller.addTab(tab2);
        
        // Refresh UI to show the tabs and buttons
        interact(() -> chatPanel.refreshButtons());
        
        Scene scene = new Scene(chatPanel.getRoot(), 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    public void testInitialSetup() {
        // Check initial state
        TabPane tabPane = chatPanel.getTabPane();
        System.out.println("Number of tabs in UI: " + tabPane.getTabs().size());
        for (int i = 0; i < tabPane.getTabs().size(); i++) {
            Tab tab = tabPane.getTabs().get(i);
            System.out.println("Tab " + i + ": " + tab.getText() + " (ID: " + tab.getId() + ")");
        }
        
        System.out.println("Number of tabs in controller: " + controller.getTabs().size());
        for (String name : controller.getTabs().keySet()) {
            System.out.println("Controller tab: " + name);
        }
        
        // Basic assertions
        assertTrue(tabPane.getTabs().size() >= 2); // At least Tab 1, Tab 2
        assertNotNull(getTab(tabPane, "Tab 1"));
        assertNotNull(getTab(tabPane, "Tab 2"));
    }
    
    @Test
    public void testAddTabInteraction() {
        TabPane tabPane = chatPanel.getTabPane();
        int initialTabCount = tabPane.getTabs().size();
        
        System.out.println("Initial tab count: " + initialTabCount);
        
        // Try to click the + tab
        try {
            Tab plusTab = null;
            for (Tab tab : tabPane.getTabs()) {
                if ("+".equals(tab.getText())) {
                    plusTab = tab;
                    break;
                }
            }
            
            if (plusTab != null) {
                System.out.println("Found + tab");
                clickOn("+");
                waitForUIUpdate();
                
                // Check if dialog appeared and try to interact
                try {
                    System.out.println("Attempting to write in dialog");
                    write("Debug Tab");
                    waitForUIUpdate();
                    
                    System.out.println("Attempting to click OK");
                    clickButton("OK");
                    waitForUIUpdate();
                    
                    System.out.println("After interaction - tab count: " + tabPane.getTabs().size());
                    System.out.println("Controller tabs: " + controller.getTabs().size());
                    
                } catch (Exception e) {
                    System.out.println("Dialog interaction failed: " + e.getMessage());
                }
            } else {
                System.out.println("+ tab not found");
                fail("+ tab not found in UI");
            }
        } catch (Exception e) {
            System.out.println("Test failed with exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}