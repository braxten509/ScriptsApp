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

public class SaveStateDebugTest {
    
    private ButtonController controller;
    
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        String testSaveFile = tempDir.resolve("debug_test_buttons.dat").toString();
        controller = new ButtonController(false, testSaveFile);
    }
    
    @Test
    @DisplayName("Debug save/load cycle with button deletion")
    void testSaveLoadCycleDebug() {
        System.out.println("=== DEBUG: Starting test ===");
        
        // Given - Create a tab with buttons
        ButtonTab tab = new ButtonTab("Debug Tab");
        controller.addTab(tab);
        
        ScriptButton button1 = new ScriptButton("Debug Button 1", "Debug Content 1", Color.RED);
        ScriptButton button2 = new ScriptButton("Debug Button 2", "Debug Content 2", Color.BLUE);
        
        controller.addButtonToTab(tab.getId(), button1);
        controller.addButtonToTab(tab.getId(), button2);
        
        System.out.println("DEBUG: Added 2 buttons, current count: " + controller.getTab(tab.getId()).getButtons().size());
        
        // Save initial state
        System.out.println("DEBUG: Calling saveState() for initial state");
        controller.saveState();
        
        // Verify save file exists
        String saveFile = tempDir.resolve("debug_test_buttons.dat").toString();
        File file = new File(saveFile);
        assertTrue(file.exists(), "Save file should exist after saveState()");
        System.out.println("DEBUG: Save file exists: " + file.getAbsolutePath() + ", size: " + file.length());
        
        // Delete button1
        System.out.println("DEBUG: Removing button1 with ID: " + button1.getId());
        boolean removed = controller.removeButtonFromTab(tab.getId(), button1.getId());
        assertTrue(removed, "Button should be removed successfully");
        System.out.println("DEBUG: Button removed, current count: " + controller.getTab(tab.getId()).getButtons().size());
        
        // Save state after deletion
        System.out.println("DEBUG: Calling saveState() after deletion");
        controller.saveState();
        
        // Check file size changed
        long fileSizeAfterDeletion = file.length();
        System.out.println("DEBUG: Save file size after deletion: " + fileSizeAfterDeletion);
        
        // Load state with new controller
        System.out.println("DEBUG: Creating new controller and loading state");
        ButtonController newController = new ButtonController(true, saveFile);
        
        // Verify the deletion persisted
        assertEquals(1, newController.getAllTabs().size(), "Should have 1 tab");
        ButtonTab loadedTab = newController.getAllTabs().get(0);
        assertEquals(1, loadedTab.getButtons().size(), "Should have 1 button after deletion");
        assertEquals("Debug Button 2", loadedTab.getButtons().get(0).getName(), "Remaining button should be Button 2");
        
        System.out.println("DEBUG: Loaded tab has " + loadedTab.getButtons().size() + " buttons");
        System.out.println("DEBUG: Remaining button name: " + loadedTab.getButtons().get(0).getName());
        
        // Verify button1 is not found
        assertNull(newController.getButton(loadedTab.getId(), button1.getId()), "Button1 should not be found");
        // Verify button2 is still there
        assertNotNull(newController.getButton(loadedTab.getId(), button2.getId()), "Button2 should still be found");
        
        System.out.println("=== DEBUG: Test completed successfully ===");
    }
    
    @Test
    @DisplayName("Test with read-only directory to simulate save failure")
    void testSaveFailureScenario() {
        // Create a directory and make it read-only
        File readOnlyDir = tempDir.resolve("readonly").toFile();
        readOnlyDir.mkdirs();
        readOnlyDir.setReadOnly();
        
        String failSaveFile = readOnlyDir.getAbsolutePath() + "/should_fail.dat";
        System.out.println("DEBUG: Testing save failure with file: " + failSaveFile);
        
        ButtonController failController = new ButtonController(false, failSaveFile);
        
        // Add a tab and button
        ButtonTab tab = new ButtonTab("Fail Tab");
        failController.addTab(tab);
        ScriptButton button = new ScriptButton("Fail Button", "Fail Content", Color.GREEN);
        failController.addButtonToTab(tab.getId(), button);
        
        // Try to save - this should fail but not throw an exception
        System.out.println("DEBUG: Attempting to save to read-only directory (should fail silently)");
        
        // Capture stderr to see if error is logged
        java.io.ByteArrayOutputStream stderr = new java.io.ByteArrayOutputStream();
        java.io.PrintStream originalErr = System.err;
        System.setErr(new java.io.PrintStream(stderr));
        
        try {
            failController.saveState();
            
            // Check if error was logged
            String errorOutput = stderr.toString();
            System.setErr(originalErr);
            System.out.println("DEBUG: Error output from saveState(): " + errorOutput);
            
            // The save should have failed
            File saveFile = new File(failSaveFile);
            assertFalse(saveFile.exists(), "Save file should not exist due to permission error");
            
        } finally {
            System.setErr(originalErr);
            // Clean up - remove read-only attribute
            readOnlyDir.setWritable(true);
        }
    }
}