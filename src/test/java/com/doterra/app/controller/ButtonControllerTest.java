package com.doterra.app.controller;

import com.doterra.app.TestConfiguration;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class ButtonControllerTest extends TestConfiguration {
    
    private ButtonController controller;
    
    @BeforeEach
    void setUp() {
        controller = new ButtonController();
    }
    
    @Test
    @DisplayName("Constructor should create controller with empty tabs")
    void testConstructor() {
        // Given/When
        ButtonController newController = new ButtonController();
        
        // Then
        assertTrue(newController.getAllTabs().isEmpty());
    }
    
    @Test
    @DisplayName("addTab should add tab to controller")
    void testAddTab() {
        // Given
        ButtonTab tab1 = new ButtonTab("Tab 1");
        ButtonTab tab2 = new ButtonTab("Tab 2");
        
        // When
        controller.addTab(tab1);
        controller.addTab(tab2);
        
        // Then
        assertEquals(2, controller.getAllTabs().size());
        assertTrue(controller.getAllTabs().contains(tab1));
        assertTrue(controller.getAllTabs().contains(tab2));
    }
    
    @Test
    @DisplayName("removeTab should remove existing tab and return true")
    void testRemoveExistingTab() {
        // Given
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        
        // When
        boolean removed = controller.removeTab(tab.getId());
        
        // Then
        assertTrue(removed);
        assertFalse(controller.getAllTabs().contains(tab));
        assertEquals(0, controller.getAllTabs().size());
    }
    
    @Test
    @DisplayName("removeTab should return false for non-existing tab")
    void testRemoveNonExistingTab() {
        // Given
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        
        // When
        boolean removed = controller.removeTab("non-existing-id");
        
        // Then
        assertFalse(removed);
        assertEquals(1, controller.getAllTabs().size());
    }
    
    @Test
    @DisplayName("getTab should return existing tab")
    void testGetExistingTab() {
        // Given
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        
        // When
        ButtonTab retrieved = controller.getTab(tab.getId());
        
        // Then
        assertNotNull(retrieved);
        assertEquals(tab, retrieved);
        assertEquals(tab.getId(), retrieved.getId());
    }
    
    @Test
    @DisplayName("getTab should return null for non-existing tab")
    void testGetNonExistingTab() {
        // Given
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        
        // When
        ButtonTab retrieved = controller.getTab("non-existing-id");
        
        // Then
        assertNull(retrieved);
    }
    
    @Test
    @DisplayName("getAllTabs should return copy of tabs list")
    void testGetAllTabsReturnsCopy() {
        // Given
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        
        // When
        List<ButtonTab> tabs = controller.getAllTabs();
        tabs.clear(); // Modify the returned list
        
        // Then
        assertEquals(1, controller.getAllTabs().size()); // Original should be unchanged
    }
    
    @Test
    @DisplayName("addButtonToTab should add button to existing tab")
    void testAddButtonToExistingTab() {
        // Given
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        ScriptButton button = new ScriptButton("Button", "Content", Color.RED);
        
        // When
        controller.addButtonToTab(tab.getId(), button);
        
        // Then
        ButtonTab retrievedTab = controller.getTab(tab.getId());
        assertEquals(1, retrievedTab.getButtons().size());
        assertTrue(retrievedTab.getButtons().contains(button));
    }
    
    @Test
    @DisplayName("addButtonToTab should do nothing for non-existing tab")
    void testAddButtonToNonExistingTab() {
        // Given
        ScriptButton button = new ScriptButton("Button", "Content", Color.RED);
        
        // When/Then - Should not throw exception
        assertDoesNotThrow(() -> controller.addButtonToTab("non-existing-id", button));
    }
    
    @Test
    @DisplayName("removeButtonFromTab should remove button and return true")
    void testRemoveButtonFromTab() {
        // Given
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        ScriptButton button = new ScriptButton("Button", "Content", Color.RED);
        controller.addButtonToTab(tab.getId(), button);
        
        // When
        boolean removed = controller.removeButtonFromTab(tab.getId(), button.getId());
        
        // Then
        assertTrue(removed);
        ButtonTab retrievedTab = controller.getTab(tab.getId());
        assertEquals(0, retrievedTab.getButtons().size());
    }
    
    @Test
    @DisplayName("removeButtonFromTab should return false for non-existing tab")
    void testRemoveButtonFromNonExistingTab() {
        // When
        boolean removed = controller.removeButtonFromTab("non-existing-tab", "button-id");
        
        // Then
        assertFalse(removed);
    }
    
    @Test
    @DisplayName("removeButtonFromTab should return false for non-existing button")
    void testRemoveNonExistingButtonFromTab() {
        // Given
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        
        // When
        boolean removed = controller.removeButtonFromTab(tab.getId(), "non-existing-button");
        
        // Then
        assertFalse(removed);
    }
    
    @Test
    @DisplayName("getButton should return button from existing tab")
    void testGetButtonFromTab() {
        // Given
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        ScriptButton button = new ScriptButton("Button", "Content", Color.RED);
        controller.addButtonToTab(tab.getId(), button);
        
        // When
        ScriptButton retrieved = controller.getButton(tab.getId(), button.getId());
        
        // Then
        assertNotNull(retrieved);
        assertEquals(button, retrieved);
    }
    
    @Test
    @DisplayName("getButton should return null for non-existing tab")
    void testGetButtonFromNonExistingTab() {
        // When
        ScriptButton retrieved = controller.getButton("non-existing-tab", "button-id");
        
        // Then
        assertNull(retrieved);
    }
    
    @Test
    @DisplayName("getButton should return null for non-existing button")
    void testGetNonExistingButtonFromTab() {
        // Given
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        
        // When
        ScriptButton retrieved = controller.getButton(tab.getId(), "non-existing-button");
        
        // Then
        assertNull(retrieved);
    }
    
    @Test
    @DisplayName("Should handle multiple tabs with multiple buttons")
    void testMultipleTabsWithButtons() {
        // Given
        ButtonTab tab1 = new ButtonTab("Chat Scripts");
        ButtonTab tab2 = new ButtonTab("Email Scripts");
        
        ScriptButton chatButton1 = new ScriptButton("Greeting", "Hello!", Color.GREEN);
        ScriptButton chatButton2 = new ScriptButton("Goodbye", "Bye!", Color.RED);
        ScriptButton emailButton1 = new ScriptButton("Welcome", "Welcome email", Color.BLUE);
        
        // When
        controller.addTab(tab1);
        controller.addTab(tab2);
        controller.addButtonToTab(tab1.getId(), chatButton1);
        controller.addButtonToTab(tab1.getId(), chatButton2);
        controller.addButtonToTab(tab2.getId(), emailButton1);
        
        // Then
        assertEquals(2, controller.getAllTabs().size());
        assertEquals(2, controller.getTab(tab1.getId()).getButtons().size());
        assertEquals(1, controller.getTab(tab2.getId()).getButtons().size());
        
        // Verify we can retrieve specific buttons
        assertEquals(chatButton1, controller.getButton(tab1.getId(), chatButton1.getId()));
        assertEquals(emailButton1, controller.getButton(tab2.getId(), emailButton1.getId()));
    }
}