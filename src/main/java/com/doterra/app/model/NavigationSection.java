package com.doterra.app.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a collapsible section in the navigation sidebar
 */
public class NavigationSection {
    private String title;
    private boolean expanded;
    private List<NavigationItem> items;
    
    public NavigationSection(String title, boolean expanded) {
        this.title = title;
        this.expanded = expanded;
        this.items = new ArrayList<>();
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public boolean isExpanded() {
        return expanded;
    }
    
    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
    
    public List<NavigationItem> getItems() {
        return items;
    }
    
    public void addItem(NavigationItem item) {
        this.items.add(item);
    }
    
    public void addItem(String label, String panelId) {
        this.items.add(new NavigationItem(label, panelId));
    }
    
    public void addItem(String label, String panelId, boolean hasBadge) {
        this.items.add(new NavigationItem(label, panelId, hasBadge));
    }
    
    /**
     * Represents an individual navigation item within a section
     */
    public static class NavigationItem {
        private String label;
        private String panelId;
        private boolean hasBadge;
        
        public NavigationItem(String label, String panelId) {
            this(label, panelId, false);
        }
        
        public NavigationItem(String label, String panelId, boolean hasBadge) {
            this.label = label;
            this.panelId = panelId;
            this.hasBadge = hasBadge;
        }
        
        public String getLabel() {
            return label;
        }
        
        public void setLabel(String label) {
            this.label = label;
        }
        
        public String getPanelId() {
            return panelId;
        }
        
        public void setPanelId(String panelId) {
            this.panelId = panelId;
        }
        
        public boolean hasBadge() {
            return hasBadge;
        }
        
        public void setHasBadge(boolean hasBadge) {
            this.hasBadge = hasBadge;
        }
    }
}