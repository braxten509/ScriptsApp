package com.doterra.app.util;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages navigation section expanded/collapsed state persistence
 */
public class NavigationPreferences implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final String PREFERENCES_FILE = "data/navigation_preferences.dat";
    
    private Map<String, Boolean> sectionStates;
    
    public NavigationPreferences() {
        this.sectionStates = new HashMap<>();
        // Set default states
        this.sectionStates.put("Basic", true);  // Basic expanded by default
        this.sectionStates.put("Advanced", false);  // Advanced collapsed by default
    }
    
    public boolean isSectionExpanded(String sectionTitle) {
        return sectionStates.getOrDefault(sectionTitle, false);
    }
    
    public void setSectionExpanded(String sectionTitle, boolean expanded) {
        sectionStates.put(sectionTitle, expanded);
    }
    
    /**
     * Save navigation preferences to file
     */
    public void save() {
        try {
            Path dataDir = Paths.get("data");
            if (!Files.exists(dataDir)) {
                Files.createDirectories(dataDir);
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(PREFERENCES_FILE)))) {
                oos.writeObject(this);
            }
        } catch (IOException e) {
            System.err.println("Error saving navigation preferences: " + e.getMessage());
        }
    }
    
    /**
     * Load navigation preferences from file
     */
    public static NavigationPreferences load() {
        Path preferencesPath = Paths.get(PREFERENCES_FILE);
        if (!Files.exists(preferencesPath)) {
            return new NavigationPreferences(); // Return default preferences
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(preferencesPath))) {
            return (NavigationPreferences) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading navigation preferences: " + e.getMessage());
            return new NavigationPreferences(); // Return default preferences on error
        }
    }
}