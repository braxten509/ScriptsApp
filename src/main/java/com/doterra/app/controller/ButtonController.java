package com.doterra.app.controller;

import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ButtonController {
    
    private final Map<String, ButtonTab> tabs;
    
    public ButtonController() {
        tabs = new HashMap<>();
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
        // To be implemented: Save the state to a file
    }
    
    public void loadState() {
        // To be implemented: Load the state from a file
    }
}