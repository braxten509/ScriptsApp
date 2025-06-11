package com.doterra.app.view;

import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.regex.*;
import java.lang.reflect.Method;

/**
 * Test the actual RegexEditorPanel methods to see what's happening
 */
public class ActualRegexTest {
    
    @Test
    void testActualRegexProcessing() throws Exception {
        // Create real matches as they would appear from regex processing
        Map<String, List<MatchResult>> matches = new HashMap<>();
        List<MatchResult> priceMatches = new ArrayList<>();
        
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
        
        // Use reflection to call the actual processTemplateScript method
        // We can't instantiate RegexEditorPanel due to JavaFX, but we can create a mock subclass
        TestableRegexEditorPanel panel = new TestableRegexEditorPanel();
        
        String result = panel.testProcessTemplateScript(template, matches);
        
        System.out.println("=== ACTUAL REGEXEDITORPANEL OUTPUT ===");
        System.out.println("'" + result + "'");
        System.out.println("=== END OUTPUT ===");
    }
    
    /**
     * Subclass that doesn't initialize UI but allows us to test template processing
     */
    private static class TestableRegexEditorPanel extends RegexEditorPanel {
        
        public TestableRegexEditorPanel() {
            // Override constructor to not call setupUI
        }
        
        @Override
        protected void setupUI() {
            // Don't setup UI for testing
        }
        
        // Make the private method accessible for testing
        public String testProcessTemplateScript(String template, Map<String, List<MatchResult>> matches) {
            try {
                Method method = RegexEditorPanel.class.getDeclaredMethod(
                    "processTemplateScript", String.class, Map.class);
                method.setAccessible(true);
                return (String) method.invoke(this, template, matches);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}