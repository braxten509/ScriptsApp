package com.doterra.app.controller;

import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ButtonDeletionTest {
    
    private ButtonController controller;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        String testSaveFile = tempDir.resolve("test_buttons.dat").toString();
        controller = new ButtonController(false, testSaveFile);
    }
    
    @Test
    @DisplayName("Button should be deleted and persist after save/load cycle")
    void testButtonDeletionPersistence() {
        // Given - Create a tab with two buttons
        ButtonTab tab = new ButtonTab("Test Tab");
        controller.addTab(tab);
        
        ScriptButton button1 = new ScriptButton("Button 1", "Content 1", Color.RED);
        ScriptButton button2 = new ScriptButton("Button 2", "Content 2", Color.BLUE);
        
        controller.addButtonToTab(tab.getId(), button1);
        controller.addButtonToTab(tab.getId(), button2);
        
        // Verify initial state
        assertEquals(2, controller.getTab(tab.getId()).getButtons().size());
        
        // Save state
        controller.saveState();
        
        // When - Delete one button
        boolean removed = controller.removeButtonFromTab(tab.getId(), button1.getId());
        assertTrue(removed);
        
        // Verify button is removed from controller
        assertEquals(1, controller.getTab(tab.getId()).getButtons().size());
        assertNull(controller.getButton(tab.getId(), button1.getId()));
        assertNotNull(controller.getButton(tab.getId(), button2.getId()));
        
        // Save state after deletion
        controller.saveState();
        
        // Then - Create new controller and load state
        String saveFile = tempDir.resolve("test_buttons.dat").toString();
        ButtonController newController = new ButtonController(true, saveFile);
        
        // Verify the deletion persisted
        ButtonTab loadedTab = newController.getAllTabs().get(0); // Should be our test tab
        assertEquals(1, loadedTab.getButtons().size());
        assertEquals("Button 2", loadedTab.getButtons().get(0).getName());
        
        // Verify button1 is not found
        assertNull(newController.getButton(loadedTab.getId(), button1.getId()));
        // Verify button2 is still there
        assertNotNull(newController.getButton(loadedTab.getId(), button2.getId()));
    }
}