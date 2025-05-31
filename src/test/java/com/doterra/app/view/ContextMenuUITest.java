package com.doterra.app.view;

import com.doterra.app.TestFXBase;
import com.doterra.app.controller.ButtonController;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

public class ContextMenuUITest extends TestFXBase {
    
    private ChatScriptsPanel chatPanel;
    private ButtonController controller;
    
    @Override
    protected void setupStage(Stage stage) {
        chatPanel = new ChatScriptsPanel();
        controller = chatPanel.getButtonController();
        
        // Create initial test data
        ButtonTab tab1 = new ButtonTab("Test Tab 1");
        ScriptButton button1 = new ScriptButton("Button 1", "Content 1", Color.LIGHTBLUE);
        ScriptButton button2 = new ScriptButton("Button 2", "Content 2", Color.LIGHTGREEN);
        tab1.addButton(button1);
        tab1.addButton(button2);
        controller.addTab(tab1);
        
        ButtonTab tab2 = new ButtonTab("Test Tab 2");
        ScriptButton button3 = new ScriptButton("Button 3", "Content 3", Color.LIGHTYELLOW);
        tab2.addButton(button3);
        controller.addTab(tab2);
        
        // Refresh UI to show the buttons
        interact(() -> chatPanel.refreshButtons());
        
        Scene scene = new Scene(chatPanel.getRoot(), 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    public void testRenameButtonViaContextMenu() {
        // Select first tab
        selectTab(chatPanel.getTabPane(), "Test Tab 1");
        waitForUIUpdate();
        
        // Right-click on Button 1
        rightClickOn("Button 1");
        
        // Verify context menu is visible
        assertTrue(isContextMenuVisible());
        
        // Click rename option
        clickMenuItem("Rename");
        waitForUIUpdate();
        
        // Type new name in dialog
        write("Renamed Button");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify button was renamed
        Button renamedButton = findButtonByText("Renamed Button");
        assertNotNull(renamedButton);
        assertNull(findButtonByText("Button 1"));
        
        // Verify model was updated
        ScriptButton modelButton = controller.getTabs().get("Test Tab 1").getButtons().get(0);
        assertEquals("Renamed Button", modelButton.getName());
    }
    
    @Test
    public void testChangeButtonColorViaContextMenu() {
        // Select first tab
        selectTab(chatPanel.getTabPane(), "Test Tab 1");
        waitForUIUpdate();
        
        // Right-click on Button 2
        rightClickOn("Button 2");
        
        // Click change color option
        clickMenuItem("Change Color");
        waitForUIUpdate();
        
        // Find color picker dialog
        ColorPicker colorPicker = lookup(".color-picker").query();
        assertNotNull(colorPicker);
        
        // Select a new color
        interact(() -> colorPicker.setValue(Color.RED));
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify button color was changed
        Button button = findButtonByText("Button 2");
        String style = button.getStyle();
        assertTrue(style.contains("background-color"));
        
        // Verify model was updated
        ScriptButton modelButton = controller.getTabs().get("Test Tab 1").getButtons().get(1);
        assertEquals(Color.RED, modelButton.getColor());
    }
    
    @Test
    public void testDuplicateButtonViaContextMenu() {
        // Select first tab
        selectTab(chatPanel.getTabPane(), "Test Tab 1");
        waitForUIUpdate();
        
        // Get initial button count
        int initialCount = controller.getTabs().get("Test Tab 1").getButtons().size();
        
        // Right-click on Button 1
        rightClickOn("Button 1");
        
        // Click duplicate option
        clickMenuItem("Duplicate");
        waitForUIUpdate();
        
        // Verify button was duplicated
        assertEquals(initialCount + 1, controller.getTabs().get("Test Tab 1").getButtons().size());
        
        // Find the duplicate button (should have "Copy" suffix)
        Button copyButton = lookup(".button").queryAll().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .filter(btn -> btn.getText().contains("Button 1") && btn.getText().contains("Copy"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(copyButton);
        
        // Verify duplicate has same content
        ScriptButton original = controller.getTabs().get("Test Tab 1").getButtons().get(0);
        ScriptButton duplicate = controller.getTabs().get("Test Tab 1").getButtons().stream()
                .filter(btn -> btn.getName().contains("Button 1") && btn.getName().contains("Copy"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(duplicate);
        assertEquals(original.getContent(), duplicate.getContent());
        assertEquals(original.getColor(), duplicate.getColor());
    }
    
    @Test
    public void testDeleteButtonViaContextMenu() {
        // Select second tab
        selectTab(chatPanel.getTabPane(), "Test Tab 2");
        waitForUIUpdate();
        
        // Get initial button count
        int initialCount = controller.getTabs().get("Test Tab 2").getButtons().size();
        
        // Right-click on Button 3
        rightClickOn("Button 3");
        
        // Click delete option
        clickMenuItem("Delete");
        waitForUIUpdate();
        
        // Click confirmation in alert dialog
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify button was deleted
        assertEquals(initialCount - 1, controller.getTabs().get("Test Tab 2").getButtons().size());
        assertNull(findButtonByText("Button 3"));
    }
    
    @Test
    public void testContextMenuCancellation() {
        // Select first tab
        selectTab(chatPanel.getTabPane(), "Test Tab 1");
        waitForUIUpdate();
        
        // Right-click on Button 1
        rightClickOn("Button 1");
        
        // Click rename option
        clickMenuItem("Rename");
        waitForUIUpdate();
        
        // Cancel the dialog
        clickButton("Cancel");
        waitForUIUpdate();
        
        // Verify button name unchanged
        assertNotNull(findButtonByText("Button 1"));
        assertEquals("Button 1", controller.getTabs().get("Test Tab 1").getButtons().get(0).getName());
    }
    
    @Test
    public void testContextMenuOnEmptySpace() {
        // Select first tab
        selectTab(chatPanel.getTabPane(), "Test Tab 1");
        waitForUIUpdate();
        
        // Right-click on empty space in button panel
        rightClickOn(chatPanel.getTabPane());
        
        // Verify no context menu appears (or different menu)
        // Context menu should only appear on buttons
        assertFalse(isContextMenuVisible());
    }
    
    @Test
    public void testMultipleContextMenuOperations() {
        // Select first tab
        selectTab(chatPanel.getTabPane(), "Test Tab 1");
        waitForUIUpdate();
        
        // Rename Button 1
        rightClickOn("Button 1");
        clickMenuItem("Rename");
        write("First Renamed");
        clickButton("OK");
        waitForUIUpdate();
        
        // Change color of renamed button
        rightClickOn("First Renamed");
        clickMenuItem("Change Color");
        ColorPicker colorPicker = lookup(".color-picker").query();
        interact(() -> colorPicker.setValue(Color.PURPLE));
        clickButton("OK");
        waitForUIUpdate();
        
        // Duplicate the modified button
        rightClickOn("First Renamed");
        clickMenuItem("Duplicate");
        waitForUIUpdate();
        
        // Verify all operations succeeded
        assertNotNull(findButtonByText("First Renamed"));
        Button duplicate = lookup(".button").queryAll().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .filter(btn -> btn.getText().contains("First Renamed") && btn.getText().contains("Copy"))
                .findFirst()
                .orElse(null);
        assertNotNull(duplicate);
        
        // Verify model state
        ButtonTab tab = controller.getTabs().get("Test Tab 1");
        assertEquals(3, tab.getButtons().size()); // Original 2 + 1 duplicate
        
        ScriptButton renamedButton = tab.getButtons().stream()
                .filter(btn -> btn.getName().equals("First Renamed"))
                .findFirst()
                .orElse(null);
        assertNotNull(renamedButton);
        assertEquals(Color.PURPLE, renamedButton.getColor());
    }
    
    @Test
    public void testContextMenuWithEmptyButtonName() {
        // Select first tab
        selectTab(chatPanel.getTabPane(), "Test Tab 1");
        waitForUIUpdate();
        
        // Right-click on Button 1
        rightClickOn("Button 1");
        clickMenuItem("Rename");
        
        // Clear the field and try to submit empty name
        pressCtrlKey(javafx.scene.input.KeyCode.A);
        pressKey(javafx.scene.input.KeyCode.DELETE);
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify button name is unchanged (empty names should be rejected)
        assertNotNull(findButtonByText("Button 1"));
        assertEquals("Button 1", controller.getTabs().get("Test Tab 1").getButtons().get(0).getName());
    }
}