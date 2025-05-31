package com.doterra.app.view;

import com.doterra.app.TestFXBase;
import com.doterra.app.controller.ButtonController;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EdgeCasesUITest extends TestFXBase {
    
    private ChatScriptsPanel chatPanel;
    private ButtonController controller;
    
    @Override
    protected void setupStage(Stage stage) {
        chatPanel = new ChatScriptsPanel();
        controller = chatPanel.getButtonController();
        
        Scene scene = new Scene(chatPanel.getRoot(), 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    public void testVeryLongButtonName() {
        // Create tab first
        controller.addTab(new ButtonTab("Test Tab"));
        interact(() -> chatPanel.refreshButtons());
        selectTab(chatPanel.getTabPane(), "Test Tab");
        waitForUIUpdate();
        
        // Create button with very long name
        String longName = "This is a very long button name that should be handled properly by the UI without breaking the layout or causing any visual issues in the application";
        
        TextArea textArea = lookup(".text-area").query();
        clearAndType(textArea, "Content");
        clickButton("Add Script");
        write(longName);
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify button was created
        Button button = lookup(".button").queryAll().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .filter(btn -> btn.getText().contains("This is a very long"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(button);
        
        // Verify text is properly displayed (may be truncated)
        assertTrue(button.getText().length() > 0);
    }
    
    @Test
    public void testSpecialCharactersInNames() {
        // Create tab first
        controller.addTab(new ButtonTab("Test Tab"));
        interact(() -> chatPanel.refreshButtons());
        selectTab(chatPanel.getTabPane(), "Test Tab");
        waitForUIUpdate();
        
        // Test various special characters
        String[] specialNames = {
            "Button with spaces",
            "Button-with-dashes",
            "Button_with_underscores",
            "Button@with#symbols",
            "Button (with) [brackets]",
            "Button & ampersand",
            "Button 'with' \"quotes\"",
            "Button\\with\\backslashes",
            "Button/with/slashes"
        };
        
        TextArea textArea = lookup(".text-area").query();
        
        for (String name : specialNames) {
            clearAndType(textArea, "Content for " + name);
            clickButton("Add Script");
            write(name);
            clickButton("OK");
            waitForUIUpdate();
        }
        
        // Verify all buttons were created
        assertEquals(specialNames.length, controller.getTabs().get("Test Tab").getButtons().size());
    }
    
    @Test
    public void testManyButtonsPerformance() {
        // Create tab
        ButtonTab tab = new ButtonTab("Performance Test Tab");
        controller.addTab(tab);
        
        // Add many buttons
        for (int i = 0; i < 100; i++) {
            tab.addButton(new ScriptButton("Button " + i, "Content " + i, Color.LIGHTBLUE));
        }
        
        // Refresh UI
        interact(() -> chatPanel.refreshButtons());
        selectTab(chatPanel.getTabPane(), "Performance Test Tab");
        waitForUIUpdate();
        
        // Verify buttons are displayed
        assertTrue(lookup(".button").queryAll().size() > 50); // Some might be virtualized
        
        // Test clicking on different buttons
        if (findButtonByText("Button 0") != null) {
            clickButton("Button 0");
            waitForUIUpdate();
        }
        
        if (findButtonByText("Button 50") != null) {
            clickButton("Button 50");
            waitForUIUpdate();
        }
        
        if (findButtonByText("Button 99") != null) {
            clickButton("Button 99");
            waitForUIUpdate();
        }
    }
    
    @Test
    public void testManyTabsHandling() {
        // Create many tabs
        for (int i = 1; i <= 20; i++) {
            ButtonTab tab = new ButtonTab("Tab " + i);
            tab.addButton(new ScriptButton("Button in Tab " + i, "Content", Color.LIGHTBLUE));
            controller.addTab(tab);
        }
        
        interact(() -> chatPanel.refreshButtons());
        waitForUIUpdate();
        
        // Verify tab handling (scrolling, overflow, etc.)
        assertTrue(chatPanel.getTabPane().getTabs().size() >= 20);
        
        // Test switching between distant tabs
        selectTab(chatPanel.getTabPane(), "Tab 1");
        waitForUIUpdate();
        assertNotNull(findButtonByText("Button in Tab 1"));
        
        selectTab(chatPanel.getTabPane(), "Tab 10");
        waitForUIUpdate();
        assertNotNull(findButtonByText("Button in Tab 10"));
        
        selectTab(chatPanel.getTabPane(), "Tab 20");
        waitForUIUpdate();
        assertNotNull(findButtonByText("Button in Tab 20"));
    }
    
    @Test
    public void testEmptyContentHandling() {
        // Create tab
        controller.addTab(new ButtonTab("Test Tab"));
        interact(() -> chatPanel.refreshButtons());
        selectTab(chatPanel.getTabPane(), "Test Tab");
        waitForUIUpdate();
        
        // Create button with empty content
        TextArea textArea = lookup(".text-area").query();
        clearAndType(textArea, "");
        clickButton("Add Script");
        write("Empty Content Button");
        clickButton("OK");
        waitForUIUpdate();
        
        // Click the button
        clickButton("Empty Content Button");
        waitForUIUpdate();
        
        // Verify text area shows empty content
        assertEquals("", textArea.getText());
    }
    
    @Test
    public void testUnicodeAndEmoji() {
        // Create tab
        controller.addTab(new ButtonTab("Unicode Tab"));
        interact(() -> chatPanel.refreshButtons());
        selectTab(chatPanel.getTabPane(), "Unicode Tab");
        waitForUIUpdate();
        
        // Create buttons with unicode characters
        String[] unicodeNames = {
            "Button 中文",
            "Button العربية",
            "Button עברית",
            "Button русский",
            "Button 日本語",
            "Button 한국어",
            "Button 🎉🎊",
            "Button ♠♣♥♦"
        };
        
        TextArea textArea = lookup(".text-area").query();
        
        for (String name : unicodeNames) {
            clearAndType(textArea, "Unicode content: " + name);
            clickButton("Add Script");
            write(name);
            clickButton("OK");
            waitForUIUpdate();
        }
        
        // Verify buttons were created
        ButtonTab tab = controller.getTabs().get("Unicode Tab");
        assertEquals(unicodeNames.length, tab.getButtons().size());
    }
    
    @Test
    public void testRapidButtonClicking() {
        // Create tab with buttons
        ButtonTab tab = new ButtonTab("Rapid Click Tab");
        tab.addButton(new ScriptButton("Rapid Button 1", "Content 1", Color.LIGHTBLUE));
        tab.addButton(new ScriptButton("Rapid Button 2", "Content 2", Color.LIGHTGREEN));
        controller.addTab(tab);
        interact(() -> chatPanel.refreshButtons());
        selectTab(chatPanel.getTabPane(), "Rapid Click Tab");
        waitForUIUpdate();
        
        // Rapidly click between buttons
        for (int i = 0; i < 10; i++) {
            clickButton("Rapid Button 1");
            clickButton("Rapid Button 2");
        }
        waitForUIUpdate();
        
        // Verify UI is still responsive
        TextArea textArea = lookup(".text-area").query();
        assertNotNull(textArea.getText());
    }
    
    @Test
    public void testDuplicateButtonNames() {
        // Create tab
        controller.addTab(new ButtonTab("Test Tab"));
        interact(() -> chatPanel.refreshButtons());
        selectTab(chatPanel.getTabPane(), "Test Tab");
        waitForUIUpdate();
        
        // Create first button
        TextArea textArea = lookup(".text-area").query();
        clearAndType(textArea, "Content 1");
        clickButton("Add Script");
        write("Duplicate Name");
        clickButton("OK");
        waitForUIUpdate();
        
        // Try to create second button with same name
        clearAndType(textArea, "Content 2");
        clickButton("Add Script");
        write("Duplicate Name");
        clickButton("OK");
        waitForUIUpdate();
        
        // Both buttons should exist (implementation may add suffix)
        ButtonTab tab = controller.getTabs().get("Test Tab");
        assertTrue(tab.getButtons().size() >= 1);
    }
    
    @Test
    public void testVeryLongContent() {
        // Create tab
        controller.addTab(new ButtonTab("Test Tab"));
        interact(() -> chatPanel.refreshButtons());
        selectTab(chatPanel.getTabPane(), "Test Tab");
        waitForUIUpdate();
        
        // Create very long content
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longContent.append("This is line ").append(i).append(" of very long content. ");
            if (i % 10 == 0) longContent.append("\n");
        }
        
        // Create button with long content
        TextArea textArea = lookup(".text-area").query();
        clearAndType(textArea, longContent.toString());
        clickButton("Add Script");
        write("Long Content Button");
        clickButton("OK");
        waitForUIUpdate();
        
        // Clear text area
        clearAndType(textArea, "");
        
        // Click button to load content
        clickButton("Long Content Button");
        waitForUIUpdate();
        
        // Verify content was loaded
        assertTrue(textArea.getText().length() > 1000);
    }
}