package com.doterra.app.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for ScriptButton without JavaFX dependencies.
 * This tests the business logic without requiring JavaFX runtime.
 */
class SimpleScriptButtonTest {

    @Test
    @DisplayName("Constructor should create button with unique ID")
    void testConstructorGeneratesUniqueId() {
        // Given/When - Create two buttons with null color (avoiding JavaFX)
        ScriptButton button1 = new ScriptButton("Button 1", "Content 1", null);
        ScriptButton button2 = new ScriptButton("Button 2", "Content 2", null);
        
        // Then
        assertNotNull(button1.getId());
        assertNotNull(button2.getId());
        assertNotEquals(button1.getId(), button2.getId());
    }
    
    @Test
    @DisplayName("Constructor should set name and content correctly")
    void testConstructorSetsNameAndContent() {
        // Given
        String expectedName = "Test Button";
        String expectedContent = "Test content with <html>";
        
        // When
        ScriptButton button = new ScriptButton(expectedName, expectedContent, null);
        
        // Then
        assertEquals(expectedName, button.getName());
        assertEquals(expectedContent, button.getContent());
    }
    
    @Test
    @DisplayName("Copy constructor should create new button with new ID and copy suffix")
    void testCopyConstructor() {
        // Given
        ScriptButton original = new ScriptButton("Original", "Content", null);
        
        // When
        ScriptButton copy = new ScriptButton(original);
        
        // Then
        assertNotEquals(original.getId(), copy.getId());
        assertEquals("Original (Copy)", copy.getName());
        assertEquals(original.getContent(), copy.getContent());
    }
    
    @Test
    @DisplayName("Setters should update values correctly")
    void testSetters() {
        // Given
        ScriptButton button = new ScriptButton("Initial", "Initial content", null);
        
        // When
        button.setName("Updated Name");
        button.setContent("Updated content");
        
        // Then
        assertEquals("Updated Name", button.getName());
        assertEquals("Updated content", button.getContent());
    }
    
    @Test
    @DisplayName("Should handle null values in setters")
    void testNullHandling() {
        // Given
        ScriptButton button = new ScriptButton("Button", "Content", null);
        
        // When
        button.setName(null);
        button.setContent(null);
        button.setColor(null);
        
        // Then
        assertNull(button.getName());
        assertNull(button.getContent());
        assertNull(button.getColor());
    }
    
    @Test
    @DisplayName("Should handle empty strings")
    void testEmptyStrings() {
        // Given/When
        ScriptButton button = new ScriptButton("", "", null);
        
        // Then
        assertEquals("", button.getName());
        assertEquals("", button.getContent());
    }
}