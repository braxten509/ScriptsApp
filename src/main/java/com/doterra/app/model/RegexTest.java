package com.doterra.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a regex test case with multiple patterns
 */
public class RegexTest implements Serializable {
    private static final long serialVersionUID = 2L;
    
    private String id;
    private String name;
    private String input;
    private List<PatternEntry> patterns;
    private String template;
    private String expectedOutput;
    
    public RegexTest() {
        this.id = UUID.randomUUID().toString();
        this.name = "New Test";
        this.input = "";
        this.patterns = new ArrayList<>();
        this.patterns.add(new PatternEntry("pattern1", ""));
        this.template = "";
        this.expectedOutput = "";
    }
    
    public RegexTest(String name, String input, List<PatternEntry> patterns, 
                     String template, String expectedOutput) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.input = input;
        this.patterns = new ArrayList<>(patterns);
        this.template = template;
        this.expectedOutput = expectedOutput;
    }
    
    // Legacy constructor for backward compatibility
    public RegexTest(String name, String input, String patternName, String patternRegex, 
                     String template, String expectedOutput) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.input = input;
        this.patterns = new ArrayList<>();
        this.patterns.add(new PatternEntry(patternName, patternRegex));
        this.template = template;
        this.expectedOutput = expectedOutput;
    }
    
    /**
     * Represents a single pattern within a test
     */
    public static class PatternEntry implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private String name;
        private String regex;
        
        public PatternEntry() {
            this.name = "";
            this.regex = "";
        }
        
        public PatternEntry(String name, String regex) {
            this.name = name;
            this.regex = regex;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getRegex() { return regex; }
        public void setRegex(String regex) { this.regex = regex; }
        
        @Override
        public String toString() {
            return name + ": " + regex;
        }
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getInput() { return input; }
    public void setInput(String input) { this.input = input; }
    
    public List<PatternEntry> getPatterns() { return patterns; }
    public void setPatterns(List<PatternEntry> patterns) { this.patterns = new ArrayList<>(patterns); }
    
    // Legacy getters for backward compatibility
    public String getPatternName() { 
        return patterns.isEmpty() ? "" : patterns.get(0).getName(); 
    }
    public void setPatternName(String patternName) { 
        if (patterns.isEmpty()) {
            patterns.add(new PatternEntry(patternName, ""));
        } else {
            patterns.get(0).setName(patternName);
        }
    }
    
    public String getPatternRegex() { 
        return patterns.isEmpty() ? "" : patterns.get(0).getRegex(); 
    }
    public void setPatternRegex(String patternRegex) { 
        if (patterns.isEmpty()) {
            patterns.add(new PatternEntry("pattern1", patternRegex));
        } else {
            patterns.get(0).setRegex(patternRegex);
        }
    }
    
    public String getTemplate() { return template; }
    public void setTemplate(String template) { this.template = template; }
    
    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
    
    // Copy constructor
    public RegexTest copy() {
        RegexTest copy = new RegexTest();
        copy.id = UUID.randomUUID().toString(); // New ID for the copy
        copy.name = this.name + " (Copy)";
        copy.input = this.input;
        copy.patterns = new ArrayList<>();
        for (PatternEntry pattern : this.patterns) {
            copy.patterns.add(new PatternEntry(pattern.getName(), pattern.getRegex()));
        }
        copy.template = this.template;
        copy.expectedOutput = this.expectedOutput;
        return copy;
    }
}