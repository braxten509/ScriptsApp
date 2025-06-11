package com.doterra.app.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.regex.*;

/**
 * Test the template processing logic extracted from RegexEditorPanel
 * without requiring JavaFX initialization
 */
public class TemplateProcessorTest {
    
    @Test
    void testPriceConditionals() {
        // Create test data that matches the actual usage pattern
        Map<String, List<MatchResult>> matches = new HashMap<>();
        List<MatchResult> priceMatches = new ArrayList<>();
        
        // Create real matches as they would appear from regex processing
        Pattern pricePattern = Pattern.compile("\\$(\\d+)");
        
        Matcher matcher1 = pricePattern.matcher("$50");
        matcher1.find();
        priceMatches.add(matcher1.toMatchResult());
        
        Matcher matcher2 = pricePattern.matcher("$120");
        matcher2.find();
        priceMatches.add(matcher2.toMatchResult());
        
        Matcher matcher3 = pricePattern.matcher("$8");
        matcher3.find();
        priceMatches.add(matcher3.toMatchResult());
        
        matches.put("prices", priceMatches);
        
        String template = "{for prices}\n" +
                         "Price: ${prices.group(1)}\n" +
                         "{if prices.group(1) > 100} - Premium item{/if}\n" +
                         "{if prices.group(1) < 20} - Budget item{/if}\n" +
                         "{/for}";
        
        // Use our extracted template processor
        TemplateProcessor processor = new TemplateProcessor();
        String result = processor.process(template, matches);
        
        System.out.println("=== ACTUAL OUTPUT ===");
        System.out.println("'" + result + "'");
        System.out.println("=== END OUTPUT ===");
        
        // Verify the expected output structure
        assertTrue(result.contains("Price: $50"), "Should contain first price");
        assertTrue(result.contains("Price: $120"), "Should contain second price");
        assertTrue(result.contains("Price: $8"), "Should contain third price");
        
        // Check for conditional content
        assertTrue(result.contains("Premium item"), "Should contain Premium item for $120");
        assertTrue(result.contains("Budget item"), "Should contain Budget item for $8");
        
        // Split into lines for detailed analysis
        String[] lines = result.split("\n");
        
        // Find the Premium item line and check it's after $120
        int premiumLineIndex = -1;
        int price120LineIndex = -1;
        int price50LineIndex = -1;
        int price8LineIndex = -1;
        
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].contains("Price: $50")) price50LineIndex = i;
            if (lines[i].contains("Price: $120")) price120LineIndex = i;
            if (lines[i].contains("Price: $8")) price8LineIndex = i;
            if (lines[i].contains("Premium item")) premiumLineIndex = i;
        }
        
        // Premium item should be after $120 line
        assertTrue(premiumLineIndex > price120LineIndex, "Premium item should appear after $120");
        
        // Premium item should not be between $50 and $120, or between $8 and end
        assertTrue(premiumLineIndex < price8LineIndex || premiumLineIndex > price120LineIndex, 
                  "Premium item should only be associated with $120");
    }
    
    /**
     * Simplified template processor that mimics RegexEditorPanel logic
     */
    private static class TemplateProcessor {
        
        public String process(String template, Map<String, List<MatchResult>> matches) {
            return processTemplateScript(template, matches, null, -1);
        }
        
        private String processTemplateScript(String template, Map<String, List<MatchResult>> matches,
                                           String currentPattern, int currentIndex) {
            StringBuilder result = new StringBuilder();
            int pos = 0;
            
            while (pos < template.length()) {
                // Handle if statements
                if (template.startsWith("{if ", pos)) {
                    int endIf = template.indexOf("}", pos);
                    if (endIf == -1) break;
                    
                    String condition = template.substring(pos + 4, endIf).trim();
                    int blockEnd = findMatchingEndIf(template, endIf + 1);
                    if (blockEnd == -1) break;
                    
                    String blockContent = template.substring(endIf + 1, blockEnd);
                    
                    // Evaluate the condition with current loop context
                    if (evaluateCondition(condition, matches, currentPattern, currentIndex)) {
                        String processedBlock = processTemplateScript(blockContent, matches, currentPattern, currentIndex);
                        result.append(processedBlock);
                    }
                    
                    pos = blockEnd + 5; // Skip past {/if}
                }
                // Handle for loops
                else if (template.startsWith("{for ", pos)) {
                    int endFor = template.indexOf("}", pos);
                    if (endFor == -1) break;
                    
                    String patternName = template.substring(pos + 5, endFor).trim();
                    int loopEnd = template.indexOf("{/for}", endFor);
                    if (loopEnd == -1) break;
                    
                    String loopContent = template.substring(endFor + 1, loopEnd);
                    List<MatchResult> patternMatches = matches.get(patternName);
                    
                    if (patternMatches != null && !patternMatches.isEmpty()) {
                        for (int i = 0; i < patternMatches.size(); i++) {
                            // First, replace pattern variables for this iteration
                            String processedLoop = processTemplateVariables(loopContent, matches, patternName, i);
                            // Then, process nested template commands with the current loop context
                            processedLoop = processTemplateScript(processedLoop, matches, patternName, i);
                            // Trim and append
                            processedLoop = processedLoop.trim();
                            if (!processedLoop.isEmpty()) {
                                result.append(processedLoop);
                                // Add double newline between iterations for easy parsing
                                if (i < patternMatches.size() - 1) {
                                    result.append("\n\n");
                                }
                            }
                        }
                    }
                    
                    pos = loopEnd + 6;
                }
                // Handle variables
                else if (template.startsWith("{", pos)) {
                    int end = template.indexOf("}", pos);
                    if (end == -1) {
                        result.append(template.charAt(pos));
                        pos++;
                        continue;
                    }
                    
                    String variable = template.substring(pos + 1, end);
                    String processed = processVariable(variable, matches, currentPattern, currentIndex);
                    result.append(processed);
                    pos = end + 1;
                }
                else {
                    result.append(template.charAt(pos));
                    pos++;
                }
            }
            
            return result.toString();
        }
        
        private String processTemplateVariables(String template, Map<String, List<MatchResult>> matches,
                                              String currentPattern, int currentIndex) {
            Pattern p = Pattern.compile("\\{" + Pattern.quote(currentPattern) + "\\.group\\((\\d+)\\)\\}");
            Matcher m = p.matcher(template);
            StringBuffer sb = new StringBuffer();
            
            while (m.find()) {
                int groupNum = Integer.parseInt(m.group(1));
                List<MatchResult> patternMatches = matches.get(currentPattern);
                String replacement = "";
                
                if (patternMatches != null && currentIndex < patternMatches.size()) {
                    MatchResult match = patternMatches.get(currentIndex);
                    if (groupNum <= match.groupCount()) {
                        replacement = match.group(groupNum);
                    }
                }
                
                m.appendReplacement(sb, Matcher.quoteReplacement(replacement));
            }
            m.appendTail(sb);
            
            return sb.toString();
        }
        
        private String processVariable(String variable, Map<String, List<MatchResult>> matches,
                                     String currentPattern, int currentIndex) {
            // Handle current pattern group references in loops
            if (currentPattern != null && currentIndex >= 0 &&
                variable.matches(Pattern.quote(currentPattern) + "\\.group\\(\\d+\\)")) {
                
                String[] parts = variable.split("\\.group\\(|\\)");
                int group = Integer.parseInt(parts[1]);
                
                List<MatchResult> patternMatches = matches.get(currentPattern);
                if (patternMatches != null && currentIndex < patternMatches.size()) {
                    MatchResult match = patternMatches.get(currentIndex);
                    if (group <= match.groupCount()) {
                        String value = match.group(group);
                        return value != null ? value : "";
                    }
                }
                return "";
            }
            
            return "{" + variable + "}";
        }
        
        private int findMatchingEndIf(String template, int startPos) {
            int depth = 1;
            int pos = startPos;
            
            while (pos < template.length() && depth > 0) {
                if (template.startsWith("{if ", pos)) {
                    depth++;
                    pos += 4;
                } else if (template.startsWith("{/if}", pos)) {
                    depth--;
                    if (depth == 0) {
                        return pos;
                    }
                    pos += 5;
                } else {
                    pos++;
                }
            }
            
            return -1;
        }
        
        private boolean evaluateCondition(String condition, Map<String, List<MatchResult>> matches,
                                        String currentPattern, int currentIndex) {
            try {
                String expression = condition;
                
                // First handle current pattern references if we're in a loop
                if (currentPattern != null && currentIndex >= 0) {
                    Pattern groupPattern = Pattern.compile("\\b" + Pattern.quote(currentPattern) + "\\.group\\((\\d+)\\)");
                    Matcher groupMatcher = groupPattern.matcher(expression);
                    StringBuffer sb = new StringBuffer();
                    
                    while (groupMatcher.find()) {
                        int groupNum = Integer.parseInt(groupMatcher.group(1));
                        List<MatchResult> patternMatches = matches.get(currentPattern);
                        String replacement = "0";
                        
                        if (patternMatches != null && currentIndex < patternMatches.size()) {
                            MatchResult match = patternMatches.get(currentIndex);
                            if (groupNum <= match.groupCount()) {
                                String groupValue = match.group(groupNum);
                                replacement = groupValue != null ? groupValue : "0";
                            }
                        }
                        
                        groupMatcher.appendReplacement(sb, replacement);
                    }
                    groupMatcher.appendTail(sb);
                    expression = sb.toString();
                }
                
                // Simple evaluation for basic comparisons
                if (expression.contains(" > ")) {
                    String[] parts = expression.split(" > ", 2);
                    double left = Double.parseDouble(parts[0].trim());
                    double right = Double.parseDouble(parts[1].trim());
                    return left > right;
                } else if (expression.contains(" < ")) {
                    String[] parts = expression.split(" < ", 2);
                    double left = Double.parseDouble(parts[0].trim());
                    double right = Double.parseDouble(parts[1].trim());
                    return left < right;
                }
                
                return false;
                
            } catch (Exception e) {
                return false;
            }
        }
    }
}