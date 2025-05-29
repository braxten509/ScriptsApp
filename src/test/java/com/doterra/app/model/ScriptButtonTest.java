package com.doterra.app.model;

import com.doterra.app.TestConfiguration;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class ScriptButtonTest extends TestConfiguration {

    @Test
    @DisplayName("Constructor should create button with provided values and unique ID")
    void testConstructor() {
        // Given
        String expectedName = "Test Button";
        String expectedContent = "Test content";
        Color expectedColor = Color.RED;
        
        // When
        ScriptButton button = new ScriptButton(expectedName, expectedContent, expectedColor);
        
        // Then
        assertNotNull(button.getId());
        assertEquals(expectedName, button.getName());
        assertEquals(expectedContent, button.getContent());
        assertEquals(expectedColor, button.getColor());
    }
    
    @Test
    @DisplayName("Each button should have a unique ID")
    void testUniqueIds() {
        // Given/When
        ScriptButton button1 = new ScriptButton("Button 1", "Content 1", Color.RED);
        ScriptButton button2 = new ScriptButton("Button 2", "Content 2", Color.GREEN);
        
        // Then
        assertNotEquals(button1.getId(), button2.getId());
    }
    
    @Test
    @DisplayName("Copy constructor should create new button with new ID and ' (Copy)' suffix")
    void testCopyConstructor() {
        // Given
        ScriptButton original = new ScriptButton("Original Button", "Original content", Color.BLUE);
        
        // When
        ScriptButton copy = new ScriptButton(original);
        
        // Then
        assertNotEquals(original.getId(), copy.getId());
        assertEquals("Original Button (Copy)", copy.getName());
        assertEquals(original.getContent(), copy.getContent());
        assertEquals(original.getColor(), copy.getColor());
    }
    
    @Test
    @DisplayName("Name getter and setter should work correctly")
    void testNameGetterSetter() {
        // Given
        ScriptButton button = new ScriptButton("Initial", "Content", Color.BLACK);
        String newName = "Updated Name";
        
        // When
        button.setName(newName);
        
        // Then
        assertEquals(newName, button.getName());
    }
    
    @Test
    @DisplayName("Content getter and setter should work correctly")
    void testContentGetterSetter() {
        // Given
        ScriptButton button = new ScriptButton("Button", "Initial content", Color.BLACK);
        String newContent = "Updated content with <html>tags</html>";
        
        // When
        button.setContent(newContent);
        
        // Then
        assertEquals(newContent, button.getContent());
    }
    
    @Test
    @DisplayName("Color getter and setter should work correctly")
    void testColorGetterSetter() {
        // Given
        ScriptButton button = new ScriptButton("Button", "Content", Color.RED);
        Color newColor = Color.GREEN;
        
        // When
        button.setColor(newColor);
        
        // Then
        assertEquals(newColor, button.getColor());
    }
    
    @Test
    @DisplayName("Should handle null values in setters")
    void testNullHandling() {
        // Given
        ScriptButton button = new ScriptButton("Button", "Content", Color.BLACK);
        
        // When/Then - setters should accept null
        assertDoesNotThrow(() -> {
            button.setName(null);
            button.setContent(null);
            button.setColor(null);
        });
        
        assertNull(button.getName());
        assertNull(button.getContent());
        assertNull(button.getColor());
    }
    
    @Test
    @DisplayName("Should handle empty strings in constructor and setters")
    void testEmptyStrings() {
        // Given/When
        ScriptButton button = new ScriptButton("", "", Color.BLACK);
        
        // Then
        assertEquals("", button.getName());
        assertEquals("", button.getContent());
        assertEquals("", button.getColor());
        
        // When
        button.setName("");
        button.setContent("");
        button.setColor(Color.WHITE);
        
        // Then
        assertEquals("", button.getName());
        assertEquals("", button.getContent());
        assertEquals(Color.WHITE, button.getColor());
    }
}