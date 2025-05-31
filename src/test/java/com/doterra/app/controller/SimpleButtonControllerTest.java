package com.doterra.app.controller;

import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Test for ButtonController without JavaFX dependencies.
 */
class SimpleButtonControllerTest {
    
    private ButtonController controller;
    
    @BeforeEach
    void setUp() {
        controller = new ButtonController(false);
    }
    
    @Test
    @DisplayName("Constructor should create controller with empty tabs")
    void testConstructor() {
        // Given/When
        ButtonController newController = new ButtonController(false);
        
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
    }
    
    @Test
    @DisplayName("addButtonToTab should add button to existing tab")
    void testAddButtonToExistingTab() {
        // Given
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        ScriptButton button = new ScriptButton("Button", "Content", null);
        
        // When
        controller.addButtonToTab(tab.getId(), button);
        
        // Then
        ButtonTab retrievedTab = controller.getTab(tab.getId());
        assertEquals(1, retrievedTab.getButtons().size());
        assertTrue(retrievedTab.getButtons().contains(button));
    }
    
    @Test
    @DisplayName("removeButtonFromTab should remove button and return true")
    void testRemoveButtonFromTab() {
        // Given
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        ScriptButton button = new ScriptButton("Button", "Content", null);
        controller.addButtonToTab(tab.getId(), button);
        
        // When
        boolean removed = controller.removeButtonFromTab(tab.getId(), button.getId());
        
        // Then
        assertTrue(removed);
        ButtonTab retrievedTab = controller.getTab(tab.getId());
        assertEquals(0, retrievedTab.getButtons().size());
    }
    
    @Test
    @DisplayName("getButton should return button from existing tab")
    void testGetButtonFromTab() {
        // Given
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        ScriptButton button = new ScriptButton("Button", "Content", null);
        controller.addButtonToTab(tab.getId(), button);
        
        // When
        ScriptButton retrieved = controller.getButton(tab.getId(), button.getId());
        
        // Then
        assertNotNull(retrieved);
        assertEquals(button, retrieved);
    }
    
    @Test
    @DisplayName("Should handle multiple tabs with multiple buttons")
    void testMultipleTabsWithButtons() {
        // Given
        ButtonTab tab1 = new ButtonTab("Chat Scripts");
        ButtonTab tab2 = new ButtonTab("Email Scripts");
        
        ScriptButton chatButton1 = new ScriptButton("Greeting", "Hello!", null);
        ScriptButton chatButton2 = new ScriptButton("Goodbye", "Bye!", null);
        ScriptButton emailButton1 = new ScriptButton("Welcome", "Welcome email", null);
        
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
    }
}