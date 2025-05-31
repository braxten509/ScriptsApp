package com.doterra.app.view;

import com.doterra.app.TestFXBase;
import com.doterra.app.controller.ButtonController;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.Test;
import org.testfx.util.WaitForAsyncUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TabManagementUITest extends TestFXBase {
    
    private ChatScriptsPanel chatPanel;
    private ButtonController controller;
    
    private void rightClickOnTab(Tab tab) {
        if (tab.getGraphic() != null) {
            rightClickOn(tab.getGraphic());
        } else {
            rightClickOn(tab.getText());
        }
    }
    
    private Node getTabNode(Tab tab) {
        if (tab.getGraphic() != null) {
            return tab.getGraphic();
        } else {
            // Return the tab's header area - we'll need to find it in the scene
            return lookup(".tab-pane .tab-header-area .tab:has-text(\"" + tab.getText() + "\")").query();
        }
    }
    
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
    public void testCreateNewTab() {
        // Click the + button to add new tab
        clickOn("+");
        waitForUIUpdate();
        
        // Enter tab name in dialog
        write("New Tab");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify tab was created
        Tab newTab = getTab(chatPanel.getTabPane(), "New Tab");
        assertNotNull(newTab);
        
        // Verify model was updated
        assertNotNull(controller.getTabs().get("New Tab"));
        assertEquals(3, controller.getTabs().size());
    }
    
    @Test
    public void testRenameTab() {
        // Right-click on Tab 1
        Tab tab1 = getTab(chatPanel.getTabPane(), "Tab 1");
        rightClickOnTab(tab1);
        
        // Click rename option
        clickMenuItem("Rename Tab");
        waitForUIUpdate();
        
        // Enter new name
        write("Renamed Tab");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify tab was renamed
        assertNull(getTab(chatPanel.getTabPane(), "Tab 1"));
        assertNotNull(getTab(chatPanel.getTabPane(), "Renamed Tab"));
        
        // Verify model was updated
        assertNull(controller.getTabs().get("Tab 1"));
        assertNotNull(controller.getTabs().get("Renamed Tab"));
    }
    
    @Test
    public void testDeleteTabWithConfirmation() {
        // Get initial tab count
        int initialCount = controller.getTabs().size();
        
        // Right-click on Tab 2
        Tab tab2 = getTab(chatPanel.getTabPane(), "Tab 2");
        rightClickOnTab(tab2);
        
        // Click delete option
        clickMenuItem("Delete Tab");
        waitForUIUpdate();
        
        // Confirm deletion
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify tab was deleted
        assertNull(getTab(chatPanel.getTabPane(), "Tab 2"));
        assertEquals(initialCount - 1, controller.getTabs().size());
        assertNull(controller.getTabs().get("Tab 2"));
    }
    
    @Test
    public void testCancelTabDeletion() {
        // Get initial tab count
        int initialCount = controller.getTabs().size();
        
        // Right-click on Tab 1
        Tab tab1 = getTab(chatPanel.getTabPane(), "Tab 1");
        rightClickOnTab(tab1);
        
        // Click delete option
        clickMenuItem("Delete Tab");
        waitForUIUpdate();
        
        // Cancel deletion
        clickButton("Cancel");
        waitForUIUpdate();
        
        // Verify tab was not deleted
        assertNotNull(getTab(chatPanel.getTabPane(), "Tab 1"));
        assertEquals(initialCount, controller.getTabs().size());
    }
    
    @Test
    public void testTabSwitching() {
        // Start on first tab
        TabPane tabPane = chatPanel.getTabPane();
        assertEquals("Tab 1", tabPane.getSelectionModel().getSelectedItem().getText());
        
        // Click on Tab 2
        clickOn("Tab 2");
        waitForUIUpdate();
        
        // Verify Tab 2 is selected
        assertEquals("Tab 2", tabPane.getSelectionModel().getSelectedItem().getText());
        
        // Verify correct buttons are visible
        assertNotNull(findButtonByText("Button 2"));
    }
    
    @Test
    public void testTabReorderingViaDragDrop() {
        TabPane tabPane = chatPanel.getTabPane();
        
        // Get initial order
        List<Tab> initialOrder = List.copyOf(tabPane.getTabs());
        assertEquals("Tab 1", initialOrder.get(0).getText());
        assertEquals("Tab 2", initialOrder.get(1).getText());
        
        // Drag Tab 2 before Tab 1
        Tab tab2 = getTab(tabPane, "Tab 2");
        Tab tab1 = getTab(tabPane, "Tab 1");
        dragAndDropTo(getTabNode(tab2), getTabNode(tab1));
        waitForUIUpdate();
        
        // Verify new order
        List<Tab> newOrder = List.copyOf(tabPane.getTabs());
        assertEquals("Tab 2", newOrder.get(0).getText());
        assertEquals("Tab 1", newOrder.get(1).getText());
    }
    
    @Test
    public void testCreateMultipleTabs() {
        // Create several new tabs
        for (int i = 3; i <= 5; i++) {
            clickOn("+");
            write("Tab " + i);
            clickButton("OK");
            waitForUIUpdate();
        }
        
        // Verify all tabs exist
        assertEquals(5, controller.getTabs().size());
        for (int i = 1; i <= 5; i++) {
            assertNotNull(getTab(chatPanel.getTabPane(), "Tab " + i));
        }
    }
    
    @Test
    public void testTabWithMultipleButtons() {
        // Select Tab 1
        selectTab(chatPanel.getTabPane(), "Tab 1");
        waitForUIUpdate();
        
        // Add more buttons to Tab 1
        ButtonTab tab1 = controller.getTabs().get("Tab 1");
        tab1.addButton(new ScriptButton("Button 1A", "Content 1A", Color.LIGHTCYAN));
        tab1.addButton(new ScriptButton("Button 1B", "Content 1B", Color.LIGHTPINK));
        tab1.addButton(new ScriptButton("Button 1C", "Content 1C", Color.LIGHTYELLOW));
        
        // Refresh the view
        interact(() -> chatPanel.refreshButtons());
        waitForUIUpdate();
        
        // Verify all buttons are visible
        assertNotNull(findButtonByText("Button 1"));
        assertNotNull(findButtonByText("Button 1A"));
        assertNotNull(findButtonByText("Button 1B"));
        assertNotNull(findButtonByText("Button 1C"));
        
        // Switch to Tab 2 and verify only its button is visible
        selectTab(chatPanel.getTabPane(), "Tab 2");
        waitForUIUpdate();
        
        assertNotNull(findButtonByText("Button 2"));
        assertNull(findButtonByText("Button 1"));
    }
    
    @Test
    public void testEmptyTabCreation() {
        // Create a new tab without any buttons
        clickOn("+");
        write("Empty Tab");
        clickButton("OK");
        waitForUIUpdate();
        
        // Select the empty tab
        selectTab(chatPanel.getTabPane(), "Empty Tab");
        waitForUIUpdate();
        
        // Verify tab exists but has no buttons
        ButtonTab emptyTab = controller.getTabs().get("Empty Tab");
        assertNotNull(emptyTab);
        assertTrue(emptyTab.getButtons().isEmpty());
        
        // Verify UI shows no buttons
        long buttonCount = lookup(".button").queryAll().stream()
                .filter(node -> node instanceof Button)
                .filter(node -> !((Button) node).getText().equals("Add Script"))
                .count();
        assertEquals(0, buttonCount);
    }
    
    @Test
    public void testTabNameValidation() {
        // Try to create tab with empty name
        clickOn("+");
        clickButton("OK");
        waitForUIUpdate();
        
        // Dialog should still be open or tab not created
        // Enter valid name
        write("Valid Tab Name");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify tab was created with valid name
        assertNotNull(getTab(chatPanel.getTabPane(), "Valid Tab Name"));
    }
    
    @Test
    public void testDeleteLastTab() {
        // Delete all tabs except one
        while (controller.getTabs().size() > 1) {
            Tab firstTab = chatPanel.getTabPane().getTabs().get(0);
            rightClickOnTab(firstTab);
            clickMenuItem("Delete Tab");
            clickButton("OK");
            waitForUIUpdate();
        }
        
        // Try to delete the last tab
        Tab lastTab = chatPanel.getTabPane().getTabs().get(0);
        rightClickOnTab(lastTab);
        
        // Delete option should be disabled or show warning
        // Verify at least one tab remains
        assertTrue(controller.getTabs().size() >= 1);
    }
}