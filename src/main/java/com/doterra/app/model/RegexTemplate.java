package com.doterra.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RegexTemplate implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String templateText;
    private List<PatternData> patterns;
    private boolean isDefault;
    
    public RegexTemplate(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.templateText = "";
        this.patterns = new ArrayList<>();
        this.isDefault = false;
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getTemplateText() { return templateText; }
    public void setTemplateText(String templateText) { this.templateText = templateText; }
    
    public List<PatternData> getPatterns() { return patterns; }
    public void setPatterns(List<PatternData> patterns) { this.patterns = patterns; }
    
    public boolean isDefault() { return isDefault; }
    public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
    
    public static class PatternData implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String name;
        private String pattern;
        
        public PatternData(String name, String pattern) {
            this.name = name;
            this.pattern = pattern;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }
    }
}