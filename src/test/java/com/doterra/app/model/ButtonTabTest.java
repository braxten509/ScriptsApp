package com.doterra.app.model;

import com.doterra.app.TestConfiguration;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class ButtonTabTest extends TestConfiguration {
    
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
    @DisplayName("Name getter and setter should work correctly")
    void testNameGetterSetter() {
        // Given
        String newName = "Updated Tab Name";
        
        // When
        tab.setName(newName);
        
        // Then
        assertEquals(newName, tab.getName());
    }
    
    @Test
    @DisplayName("addButton should add button to the list")
    void testAddButton() {
        // Given
        ScriptButton button1 = new ScriptButton("Button 1", "Content 1", Color.RED);
        ScriptButton button2 = new ScriptButton("Button 2", "Content 2", Color.GREEN);
        
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
        ScriptButton button1 = new ScriptButton("Button 1", "Content 1", Color.RED);
        ScriptButton button2 = new ScriptButton("Button 2", "Content 2", Color.GREEN);
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
        ScriptButton button = new ScriptButton("Button", "Content", Color.RED);
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
        ScriptButton button1 = new ScriptButton("Button 1", "Content 1", Color.RED);
        ScriptButton button2 = new ScriptButton("Button 2", "Content 2", Color.GREEN);
        tab.addButton(button1);
        tab.addButton(button2);
        
        // When
        ScriptButton found = tab.getButton(button2.getId());
        
        // Then
        assertNotNull(found);
        assertEquals(button2, found);
        assertEquals(button2.getId(), found.getId());
    }
    
    @Test
    @DisplayName("getButton should return null for non-existing button")
    void testGetNonExistingButton() {
        // Given
        ScriptButton button = new ScriptButton("Button", "Content", Color.RED);
        tab.addButton(button);
        
        // When
        ScriptButton found = tab.getButton("non-existing-id");
        
        // Then
        assertNull(found);
    }
    
    @Test
    @DisplayName("getButtons should return modifiable list")
    void testGetButtonsModifiable() {
        // Given
        ScriptButton button = new ScriptButton("Button", "Content", Color.RED);
        
        // When
        List<ScriptButton> buttons = tab.getButtons();
        buttons.add(button);
        
        // Then
        assertEquals(1, tab.getButtons().size());
        assertTrue(tab.getButtons().contains(button));
    }
    
    @Test
    @DisplayName("Should handle null values appropriately")
    void testNullHandling() {
        // Test null name
        assertDoesNotThrow(() -> tab.setName(null));
        assertNull(tab.getName());
        
        // Test null button ID for removeButton
        assertFalse(tab.removeButton(null));
        
        // Test null button ID for getButton
        assertNull(tab.getButton(null));
    }
    
    @Test
    @DisplayName("Should maintain button order when adding")
    void testButtonOrder() {
        // Given
        ScriptButton button1 = new ScriptButton("First", "Content 1", Color.RED);
        ScriptButton button2 = new ScriptButton("Second", "Content 2", Color.GREEN);
        ScriptButton button3 = new ScriptButton("Third", "Content 3", Color.BLUE);
        
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