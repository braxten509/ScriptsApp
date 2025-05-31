package com.doterra.app.view;

import com.doterra.app.TestFXBase;
import com.doterra.app.controller.ButtonController;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

public class KeyboardShortcutsUITest extends TestFXBase {
    
    private ChatScriptsPanel chatPanel;
    private ButtonController controller;
    
    @Override
    protected void setupStage(Stage stage) {
        chatPanel = new ChatScriptsPanel();
        controller = chatPanel.getButtonController();
        
        // Create initial test data
        ButtonTab tab1 = new ButtonTab("Test Tab");
        ScriptButton button1 = new ScriptButton("Button 1", "Content for button 1", Color.LIGHTBLUE);
        ScriptButton button2 = new ScriptButton("Button 2", "Content for button 2", Color.LIGHTGREEN);
        tab1.addButton(button1);
        tab1.addButton(button2);
        controller.addTab(tab1);
        
        // Refresh UI to show the tab and buttons
        interact(() -> chatPanel.refreshButtons());
        
        Scene scene = new Scene(chatPanel.getRoot(), 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    public void testCopyPasteShortcuts() {
        // Select the tab
        selectTab(chatPanel.getTabPane(), "Test Tab");
        waitForUIUpdate();
        
        // Click on text area and type some text
        TextArea textArea = lookup(".text-area").query();
        clickOn(textArea);
        write("Test content to copy");
        
        // Select all with Ctrl+A
        pressCtrlKey(KeyCode.A);
        
        // Copy with Ctrl+C
        pressCtrlKey(KeyCode.C);
        
        // Clear the text area
        pressKey(KeyCode.DELETE);
        
        // Paste with Ctrl+V
        pressCtrlKey(KeyCode.V);
        
        // Verify content was pasted
        assertEquals("Test content to copy", textArea.getText());
    }
    
    @Test
    public void testCutPasteShortcuts() {
        // Click on text area and type some text
        TextArea textArea = lookup(".text-area").query();
        clickOn(textArea);
        write("Text to cut");
        
        // Select all with Ctrl+A
        pressCtrlKey(KeyCode.A);
        
        // Cut with Ctrl+X
        pressCtrlKey(KeyCode.X);
        
        // Verify text was cut
        assertEquals("", textArea.getText());
        
        // Paste with Ctrl+V
        pressCtrlKey(KeyCode.V);
        
        // Verify content was pasted
        assertEquals("Text to cut", textArea.getText());
    }
    
    @Test
    public void testUndoRedoShortcuts() {
        // Click on text area
        TextArea textArea = lookup(".text-area").query();
        clickOn(textArea);
        
        // Type some text
        write("First text");
        waitForUIUpdate();
        
        // Undo with Ctrl+Z
        pressCtrlKey(KeyCode.Z);
        waitForUIUpdate();
        
        // Verify undo worked
        assertEquals("", textArea.getText());
        
        // Redo with Ctrl+Y (or Ctrl+Shift+Z depending on platform)
        pressCtrlKey(KeyCode.Y);
        waitForUIUpdate();
        
        // Verify redo worked
        assertEquals("First text", textArea.getText());
    }
    
    @Test
    public void testDeleteKeyOnButton() {
        // Select the tab
        selectTab(chatPanel.getTabPane(), "Test Tab");
        waitForUIUpdate();
        
        // Get initial button count
        int initialCount = controller.getTabs().get("Test Tab").getButtons().size();
        
        // Click on Button 1 to select it
        Button button1 = findButtonByText("Button 1");
        clickOn(button1);
        waitForUIUpdate();
        
        // Press Delete key
        pressKey(KeyCode.DELETE);
        waitForUIUpdate();
        
        // Confirm deletion if dialog appears
        if (lookup("OK").tryQuery().isPresent()) {
            clickButton("OK");
            waitForUIUpdate();
        }
        
        // Verify button was deleted
        assertEquals(initialCount - 1, controller.getTabs().get("Test Tab").getButtons().size());
        assertNull(findButtonByText("Button 1"));
    }
    
    @Test
    public void testTabNavigationWithKeyboard() {
        // Add another tab
        ButtonTab tab2 = new ButtonTab("Test Tab 2");
        tab2.addButton(new ScriptButton("Button 3", "Content 3", Color.LIGHTYELLOW));
        controller.addTab(tab2);
        interact(() -> chatPanel.refreshButtons());
        waitForUIUpdate();
        
        // Focus on tab pane
        clickOn(chatPanel.getTabPane());
        
        // Navigate to next tab with Ctrl+Tab
        pressCtrlKey(KeyCode.TAB);
        waitForUIUpdate();
        
        // Verify tab changed
        assertEquals("Test Tab 2", chatPanel.getTabPane().getSelectionModel().getSelectedItem().getText());
        
        // Navigate back with Ctrl+Shift+Tab
        pressKey(KeyCode.CONTROL, KeyCode.SHIFT, KeyCode.TAB);
        waitForUIUpdate();
        
        // Verify tab changed back
        assertEquals("Test Tab", chatPanel.getTabPane().getSelectionModel().getSelectedItem().getText());
    }
    
    @Test
    public void testSelectAllInTextArea() {
        // Click on text area and type some text
        TextArea textArea = lookup(".text-area").query();
        clickOn(textArea);
        write("This is a test of select all functionality");
        
        // Move cursor to beginning
        pressCtrlKey(KeyCode.HOME);
        
        // Select all with Ctrl+A
        pressCtrlKey(KeyCode.A);
        
        // Type new text (should replace all)
        write("Replaced text");
        
        // Verify all text was replaced
        assertEquals("Replaced text", textArea.getText());
    }
    
    @Test
    public void testEscapeKeyClosesDialogs() {
        // Click add button to open dialog
        clickButton("Add Script");
        waitForUIUpdate();
        
        // Press Escape key
        pressKey(KeyCode.ESCAPE);
        waitForUIUpdate();
        
        // Verify dialog was closed (no new button created)
        assertEquals(2, controller.getTabs().get("Test Tab").getButtons().size());
    }
    
    @Test
    public void testArrowKeyNavigation() {
        // Add more buttons for navigation
        ButtonTab tab = controller.getTabs().get("Test Tab");
        tab.addButton(new ScriptButton("Button 3", "Content 3", Color.LIGHTCYAN));
        tab.addButton(new ScriptButton("Button 4", "Content 4", Color.LIGHTPINK));
        interact(() -> chatPanel.refreshButtons());
        waitForUIUpdate();
        
        // Click on first button
        clickButton("Button 1");
        waitForUIUpdate();
        
        // Try arrow key navigation (implementation dependent)
        pressKey(KeyCode.RIGHT);
        waitForUIUpdate();
        
        pressKey(KeyCode.DOWN);
        waitForUIUpdate();
        
        // Verify focus or selection changed (implementation dependent)
        // This test may need adjustment based on actual implementation
    }
    
    @Test
    public void testF2KeyForRename() {
        // Click on Button 1 to select it
        clickButton("Button 1");
        waitForUIUpdate();
        
        // Press F2 to rename
        pressKey(KeyCode.F2);
        waitForUIUpdate();
        
        // If rename dialog opens, type new name
        if (lookup(".text-input-dialog").tryQuery().isPresent()) {
            write("Renamed via F2");
            clickButton("OK");
            waitForUIUpdate();
            
            // Verify button was renamed
            assertNotNull(findButtonByText("Renamed via F2"));
            assertNull(findButtonByText("Button 1"));
        }
    }
    
    @Test
    public void testCtrlNForNewButton() {
        // Get initial button count
        int initialCount = controller.getTabs().get("Test Tab").getButtons().size();
        
        // Type content in text area
        TextArea textArea = lookup(".text-area").query();
        clearAndType(textArea, "New button content");
        
        // Press Ctrl+N for new button
        pressCtrlKey(KeyCode.N);
        waitForUIUpdate();
        
        // If dialog opens, enter name
        if (lookup(".text-input-dialog").tryQuery().isPresent()) {
            write("New Button via Ctrl+N");
            clickButton("OK");
            waitForUIUpdate();
            
            // Verify button was created
            assertEquals(initialCount + 1, controller.getTabs().get("Test Tab").getButtons().size());
            assertNotNull(findButtonByText("New Button via Ctrl+N"));
        }
    }
    
    @Test
    public void testCtrlSForSave() {
        // Make some changes
        TextArea textArea = lookup(".text-area").query();
        clearAndType(textArea, "Content to save");
        
        // Click a button to associate content
        clickButton("Button 1");
        waitForUIUpdate();
        
        // Press Ctrl+S to save
        pressCtrlKey(KeyCode.S);
        waitForUIUpdate();
        
        // Verify content was saved to button
        ScriptButton button = controller.getTabs().get("Test Tab").getButtons().get(0);
        assertEquals("Content to save", button.getContent());
    }
}