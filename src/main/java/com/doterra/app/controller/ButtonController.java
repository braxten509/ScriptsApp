package com.doterra.app.controller;

import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ButtonController {
    
    private final Map<String, ButtonTab> tabs;
    private final String saveFile;
    
    public ButtonController() {
        this(true, "data/doterra_buttons.dat");
    }
    
    /**
     * Constructor with custom save file
     * @param saveFileName the name of the save file to use
     */
    public ButtonController(String saveFileName) {
        this(true, saveFileName);
    }
    
    /**
     * Constructor for testing purposes
     * @param loadState whether to load existing state from file
     */
    public ButtonController(boolean loadState) {
        this(loadState, "data/doterra_buttons.dat");
    }
    
    /**
     * Full constructor
     * @param loadState whether to load existing state from file
     * @param saveFileName the name of the save file to use
     */
    public ButtonController(boolean loadState, String saveFileName) {
        tabs = new LinkedHashMap<>(); // Use LinkedHashMap to preserve order
        this.saveFile = saveFileName;
        if (loadState) {
            loadState();
        }
    }
    
    public void addTab(ButtonTab tab) {
        // Check if a tab with the same name already exists
        for (ButtonTab existingTab : tabs.values()) {
            if (existingTab.getName().equals(tab.getName())) {
                // Don't add duplicate tabs with the same name
                return;
            }
        }
        tabs.put(tab.getId(), tab);
    }
    
    public boolean removeTab(String tabId) {
        return tabs.remove(tabId) != null;
    }
    
    public ButtonTab getTab(String tabId) {
        return tabs.get(tabId);
    }
    
    public List<ButtonTab> getAllTabs() {
        return new ArrayList<>(tabs.values());
    }
    
    public void addButtonToTab(String tabId, ScriptButton button) {
        ButtonTab tab = tabs.get(tabId);
        if (tab != null) {
            tab.addButton(button);
        }
    }
    
    public boolean removeButtonFromTab(String tabId, String buttonId) {
        ButtonTab tab = tabs.get(tabId);
        if (tab != null) {
            return tab.removeButton(buttonId);
        }
        return false;
    }
    
    public ScriptButton getButton(String tabId, String buttonId) {
        ButtonTab tab = tabs.get(tabId);
        if (tab != null) {
            return tab.getButton(buttonId);
        }
        return null;
    }
    
    public void saveState() {
        try {
            // Ensure parent directory exists
            File file = new File(saveFile);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(saveFile))) {
                // Convert to list for serialization
                List<ButtonTab> tabList = new ArrayList<>(tabs.values());
                oos.writeObject(tabList);
            }
        } catch (IOException e) {
            System.err.println("Error saving button state: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    public void loadState() {
        File file = new File(saveFile);
        if (!file.exists()) {
            return; // No saved state to load
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(saveFile))) {
            List<ButtonTab> tabList = (List<ButtonTab>) ois.readObject();
            tabs.clear();
            for (ButtonTab tab : tabList) {
                tabs.put(tab.getId(), tab);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading button state: " + e.getMessage());
        }
    }
    
    public void reorderTabs(List<ButtonTab> newOrder) {
        // Clear existing tabs and rebuild in new order
        tabs.clear();
        for (ButtonTab tab : newOrder) {
            tabs.put(tab.getId(), tab);
        }
    }
    
    /**
     * Reorders tabs by moving the dragged tab to the position of the target tab.
     * 
     * @param draggedTabId ID of the tab being moved
     * @param targetTabId ID of the tab to move next to
     * @return true if reordering was successful
     */
    public boolean reorderTabs(String draggedTabId, String targetTabId) {
        ButtonTab draggedTab = tabs.get(draggedTabId);
        ButtonTab targetTab = tabs.get(targetTabId);
        
        if (draggedTab == null || targetTab == null) {
            return false;
        }
        
        // Create new ordered list
        List<ButtonTab> tabList = new ArrayList<>(tabs.values());
        
        // Find current positions
        int draggedIndex = -1;
        int targetIndex = -1;
        
        for (int i = 0; i < tabList.size(); i++) {
            if (tabList.get(i).getId().equals(draggedTabId)) {
                draggedIndex = i;
            }
            if (tabList.get(i).getId().equals(targetTabId)) {
                targetIndex = i;
            }
        }
        
        if (draggedIndex != -1 && targetIndex != -1 && draggedIndex != targetIndex) {
            // Remove and reinsert at new position
            ButtonTab removedTab = tabList.remove(draggedIndex);
            
            // Adjust target index if we removed a tab before it
            if (draggedIndex < targetIndex) {
                targetIndex--;
            }
            
            tabList.add(targetIndex, removedTab);
            
            // Update the tabs map with new order
            reorderTabs(tabList);
            return true;
        }
        
        return false;
    }
    
    public Map<String, ButtonTab> getTabs() {
        // Return tabs indexed by name for test compatibility
        Map<String, ButtonTab> tabsByName = new LinkedHashMap<>();
        for (ButtonTab tab : tabs.values()) {
            tabsByName.put(tab.getName(), tab);
        }
        return tabsByName;
    }
    
    public Map<String, ButtonTab> getTabsById() {
        return new LinkedHashMap<>(tabs);
    }
    
    /**
     * Checks if a tab name already exists.
     * 
     * @param name the tab name to check
     * @return true if the name is already used by another tab
     */
    public boolean isTabNameDuplicate(String name) {
        return tabs.values().stream()
                .anyMatch(tab -> tab.getName().equals(name));
    }
    
    /**
     * Checks if a tab name already exists, excluding a specific tab.
     * This is useful for renaming operations where the current tab should be ignored.
     * 
     * @param name the tab name to check
     * @param excludeTabId the tab ID to exclude from the check
     * @return true if the name is already used by another tab (excluding the specified tab)
     */
    public boolean isTabNameDuplicate(String name, String excludeTabId) {
        return tabs.values().stream()
                .filter(tab -> !tab.getId().equals(excludeTabId))
                .anyMatch(tab -> tab.getName().equals(name));
    }
    
    /**
     * Moves a button from one tab to another.
     * 
     * @param sourceTabId the ID of the source tab
     * @param targetTabId the ID of the target tab
     * @param buttonId the ID of the button to move
     * @return true if the move was successful, false otherwise
     */
    public boolean moveButtonBetweenTabs(String sourceTabId, String targetTabId, String buttonId) {
        if (sourceTabId.equals(targetTabId)) {
            return false; // Same tab, no move needed
        }
        
        ButtonTab sourceTab = tabs.get(sourceTabId);
        ButtonTab targetTab = tabs.get(targetTabId);
        
        if (sourceTab == null || targetTab == null) {
            return false;
        }
        
        ScriptButton button = sourceTab.getButton(buttonId);
        if (button == null) {
            return false;
        }
        
        // Remove from source tab
        if (sourceTab.removeButton(buttonId)) {
            // Add to target tab
            targetTab.addButton(button);
            return true;
        }
        
        return false;
    }
}