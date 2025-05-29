package com.doterra.app.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

/**
 * Test for ButtonTab without JavaFX dependencies.
 */
class SimpleButtonTabTest {
    
    private ButtonTab tab;
    
    @BeforeEach
    void setUp() {
        tab = new ButtonTab("Test Tab");
    }
    
    @Test
    @DisplayName("Constructor should create tab with name and empty button list")
    void testConstructor() {
        // Given/When
        ButtonTab newTab = new ButtonTab("My Tab");
        
        // Then
        assertNotNull(newTab.getId());
        assertEquals("My Tab", newTab.getName());
        assertNotNull(newTab.getButtons());
        assertTrue(newTab.getButtons().isEmpty());
    }
    
    @Test
    @DisplayName("Each tab should have a unique ID")
    void testUniqueIds() {
        // Given/When
        ButtonTab tab1 = new ButtonTab("Tab 1");
        ButtonTab tab2 = new ButtonTab("Tab 2");
        
        // Then
        assertNotEquals(tab1.getId(), tab2.getId());
    }
    
    @Test
    @DisplayName("addButton should add button to the list")
    void testAddButton() {
        // Given
        ScriptButton button1 = new ScriptButton("Button 1", "Content 1", null);
        ScriptButton button2 = new ScriptButton("Button 2", "Content 2", null);
        
        // When
        tab.addButton(button1);
        tab.addButton(button2);
        
        // Then
        assertEquals(2, tab.getButtons().size());
        assertTrue(tab.getButtons().contains(button1));
        assertTrue(tab.getButtons().contains(button2));
    }
    
    @Test
    @DisplayName("removeButton should remove existing button and return true")
    void testRemoveExistingButton() {
        // Given
        ScriptButton button1 = new ScriptButton("Button 1", "Content 1", null);
        ScriptButton button2 = new ScriptButton("Button 2", "Content 2", null);
        tab.addButton(button1);
        tab.addButton(button2);
        
        // When
        boolean removed = tab.removeButton(button1.getId());
        
        // Then
        assertTrue(removed);
        assertEquals(1, tab.getButtons().size());
        assertFalse(tab.getButtons().contains(button1));
        assertTrue(tab.getButtons().contains(button2));
    }
    
    @Test
    @DisplayName("removeButton should return false for non-existing button")
    void testRemoveNonExistingButton() {
        // Given
        ScriptButton button = new ScriptButton("Button", "Content", null);
        tab.addButton(button);
        
        // When
        boolean removed = tab.removeButton("non-existing-id");
        
        // Then
        assertFalse(removed);
        assertEquals(1, tab.getButtons().size());
    }
    
    @Test
    @DisplayName("getButton should return button when it exists")
    void testGetExistingButton() {
        // Given
        ScriptButton button1 = new ScriptButton("Button 1", "Content 1", null);
        ScriptButton button2 = new ScriptButton("Button 2", "Content 2", null);
        tab.addButton(button1);
        tab.addButton(button2);
        
        // When
        ScriptButton found = tab.getButton(button2.getId());
        
        // Then
        assertNotNull(found);
        assertEquals(button2, found);
    }
    
    @Test
    @DisplayName("getButton should return null for non-existing button")
    void testGetNonExistingButton() {
        // When
        ScriptButton found = tab.getButton("non-existing-id");
        
        // Then
        assertNull(found);
    }
    
    @Test
    @DisplayName("Should maintain button order when adding")
    void testButtonOrder() {
        // Given
        ScriptButton button1 = new ScriptButton("First", "Content 1", null);
        ScriptButton button2 = new ScriptButton("Second", "Content 2", null);
        ScriptButton button3 = new ScriptButton("Third", "Content 3", null);
        
        // When
        tab.addButton(button1);
        tab.addButton(button2);
        tab.addButton(button3);
        
        // Then
        List<ScriptButton> buttons = tab.getButtons();
        assertEquals(button1, buttons.get(0));
        assertEquals(button2, buttons.get(1));
        assertEquals(button3, buttons.get(2));
    }
}