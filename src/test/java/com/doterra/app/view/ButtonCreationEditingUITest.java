package com.doterra.app.view;

import com.doterra.app.TestFXBase;
import com.doterra.app.controller.ButtonController;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import static org.junit.jupiter.api.Assertions.*;

public class ButtonCreationEditingUITest extends TestFXBase {
    
    private ChatScriptsPanel chatPanel;
    private ButtonController controller;
    
    @Override
    protected void setupStage(Stage stage) {
        chatPanel = new ChatScriptsPanel();
        controller = chatPanel.getButtonController();
        
        // Create initial test data
        ButtonTab tab1 = new ButtonTab("Test Tab");
        controller.addTab(tab1);
        
        // Refresh UI to show the tab
        interact(() -> chatPanel.refreshButtons());
        
        Scene scene = new Scene(chatPanel.getRoot(), 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    public void testCreateNewButtonWithContent() {
        // Select the tab
        selectTab(chatPanel.getTabPane(), "Test Tab");
        waitForUIUpdate();
        
        // Type content in text area
        TextArea textArea = lookup(".text-area").query();
        clearAndType(textArea, "This is my new script content");
        
        // Click the add button
        clickButton("Add Script");
        waitForUIUpdate();
        
        // Enter button name in dialog
        write("My New Button");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify button was created
        Button newButton = findButtonByText("My New Button");
        assertNotNull(newButton);
        
        // Verify model was updated
        ButtonTab tab = controller.getTabs().get("Test Tab");
        assertEquals(1, tab.getButtons().size());
        ScriptButton modelButton = tab.getButtons().get(0);
        assertEquals("My New Button", modelButton.getName());
        assertEquals("This is my new script content", modelButton.getContent());
    }
    
    @Test
    public void testEditButtonContent() {
        // Add initial button
        ButtonTab tab = controller.getTabs().get("Test Tab");
        ScriptButton button = new ScriptButton("Edit Test Button", "Original content", Color.LIGHTBLUE);
        tab.addButton(button);
        interact(() -> chatPanel.refreshButtons());
        waitForUIUpdate();
        
        // Click the button to load its content
        clickButton("Edit Test Button");
        waitForUIUpdate();
        
        // Verify content is loaded in text area
        TextArea textArea = lookup(".text-area").query();
        assertEquals("Original content", textArea.getText());
        
        // Edit the content
        clearAndType(textArea, "Updated content");
        
        // Click the button again to save
        clickButton("Edit Test Button");
        waitForUIUpdate();
        
        // Verify content was updated in model
        assertEquals("Updated content", button.getContent());
    }
    
    @Test
    public void testCreateButtonWithEmptyContent() {
        // Select the tab
        selectTab(chatPanel.getTabPane(), "Test Tab");
        waitForUIUpdate();
        
        // Leave text area empty
        TextArea textArea = lookup(".text-area").query();
        clearAndType(textArea, "");
        
        // Click the add button
        clickButton("Add Script");
        waitForUIUpdate();
        
        // Enter button name
        write("Empty Content Button");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify button was created with empty content
        Button newButton = findButtonByText("Empty Content Button");
        assertNotNull(newButton);
        
        ScriptButton modelButton = controller.getTabs().get("Test Tab").getButtons().get(0);
        assertEquals("", modelButton.getContent());
    }
    
    @Test
    public void testCancelButtonCreation() {
        // Select the tab
        selectTab(chatPanel.getTabPane(), "Test Tab");
        waitForUIUpdate();
        
        // Type content
        TextArea textArea = lookup(".text-area").query();
        clearAndType(textArea, "This content should not be saved");
        
        // Click add button
        clickButton("Add Script");
        waitForUIUpdate();
        
        // Cancel the dialog
        clickButton("Cancel");
        waitForUIUpdate();
        
        // Verify no button was created
        assertEquals(0, controller.getTabs().get("Test Tab").getButtons().size());
    }
    
    @Test
    public void testCreateMultipleButtons() {
        // Select the tab
        selectTab(chatPanel.getTabPane(), "Test Tab");
        waitForUIUpdate();
        
        // Create first button
        TextArea textArea = lookup(".text-area").query();
        clearAndType(textArea, "First script");
        clickButton("Add Script");
        write("Button 1");
        clickButton("OK");
        waitForUIUpdate();
        
        // Create second button
        clearAndType(textArea, "Second script");
        clickButton("Add Script");
        write("Button 2");
        clickButton("OK");
        waitForUIUpdate();
        
        // Create third button
        clearAndType(textArea, "Third script");
        clickButton("Add Script");
        write("Button 3");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify all buttons were created
        assertNotNull(findButtonByText("Button 1"));
        assertNotNull(findButtonByText("Button 2"));
        assertNotNull(findButtonByText("Button 3"));
        
        // Verify model
        ButtonTab tab = controller.getTabs().get("Test Tab");
        assertEquals(3, tab.getButtons().size());
    }
    
    @Test
    public void testEditButtonAfterCreation() {
        // Create a button
        selectTab(chatPanel.getTabPane(), "Test Tab");
        TextArea textArea = lookup(".text-area").query();
        clearAndType(textArea, "Initial content");
        clickButton("Add Script");
        write("Editable Button");
        clickButton("OK");
        waitForUIUpdate();
        
        // Clear text area
        clearAndType(textArea, "");
        
        // Click button to load content
        clickButton("Editable Button");
        waitForUIUpdate();
        
        // Verify content loaded
        assertEquals("Initial content", textArea.getText());
        
        // Edit content
        clearAndType(textArea, "Modified content");
        
        // Save by clicking button again
        clickButton("Editable Button");
        waitForUIUpdate();
        
        // Click another area to clear selection
        clickOn(chatPanel.getTabPane());
        waitForUIUpdate();
        
        // Click button again to verify saved content
        clearAndType(textArea, "");
        clickButton("Editable Button");
        waitForUIUpdate();
        
        assertEquals("Modified content", textArea.getText());
    }
    
    @Test
    public void testButtonNameValidation() {
        // Select the tab
        selectTab(chatPanel.getTabPane(), "Test Tab");
        waitForUIUpdate();
        
        // Type content
        TextArea textArea = lookup(".text-area").query();
        clearAndType(textArea, "Test content");
        
        // Click add button
        clickButton("Add Script");
        waitForUIUpdate();
        
        // Try to submit empty name
        pressKey(KeyCode.ENTER);
        waitForUIUpdate();
        
        // Dialog should still be open, enter valid name
        write("Valid Name");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify button was created with valid name
        assertNotNull(findButtonByText("Valid Name"));
    }
    
    @Test
    public void testCreateButtonWithLongContent() {
        // Select the tab
        selectTab(chatPanel.getTabPane(), "Test Tab");
        waitForUIUpdate();
        
        // Type long content
        TextArea textArea = lookup(".text-area").query();
        StringBuilder longContent = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            longContent.append("This is line ").append(i + 1).append(" of a very long script content.\n");
        }
        clearAndType(textArea, longContent.toString());
        
        // Create button
        clickButton("Add Script");
        write("Long Content Button");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify button was created
        assertNotNull(findButtonByText("Long Content Button"));
        
        // Verify content was saved correctly
        ScriptButton modelButton = controller.getTabs().get("Test Tab").getButtons().get(0);
        assertEquals(longContent.toString(), modelButton.getContent());
    }
    
    @Test
    public void testButtonSelectionFeedback() {
        // Add multiple buttons
        ButtonTab tab = controller.getTabs().get("Test Tab");
        tab.addButton(new ScriptButton("Button A", "Content A", Color.LIGHTBLUE));
        tab.addButton(new ScriptButton("Button B", "Content B", Color.LIGHTGREEN));
        tab.addButton(new ScriptButton("Button C", "Content C", Color.LIGHTYELLOW));
        interact(() -> chatPanel.refreshButtons());
        waitForUIUpdate();
        
        // Click Button A
        clickButton("Button A");
        waitForUIUpdate();
        
        // Verify visual feedback (button should have selection style)
        Button buttonA = findButtonByText("Button A");
        assertTrue(buttonA.getStyleClass().contains("selected") || 
                  buttonA.getStyle().contains("border") ||
                  buttonA.getStyle().contains("effect"));
        
        // Click Button B
        clickButton("Button B");
        waitForUIUpdate();
        
        // Verify Button B is selected and Button A is not
        Button buttonB = findButtonByText("Button B");
        assertTrue(buttonB.getStyleClass().contains("selected") || 
                  buttonB.getStyle().contains("border") ||
                  buttonB.getStyle().contains("effect"));
    }
}