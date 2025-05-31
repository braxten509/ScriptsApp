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
    private static final String SAVE_FILE = "doterra_buttons.dat";
    
    public ButtonController() {
        this(true);
    }
    
    /**
     * Constructor for testing purposes
     * @param loadState whether to load existing state from file
     */
    public ButtonController(boolean loadState) {
        tabs = new LinkedHashMap<>(); // Use LinkedHashMap to preserve order
        if (loadState) {
            loadState();
        }
    }
    
    public void addTab(ButtonTab tab) {
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
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            // Convert to list for serialization
            List<ButtonTab> tabList = new ArrayList<>(tabs.values());
            oos.writeObject(tabList);
        } catch (IOException e) {
            System.err.println("Error saving button state: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    public void loadState() {
        File saveFile = new File(SAVE_FILE);
        if (!saveFile.exists()) {
            return; // No saved state to load
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
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
}