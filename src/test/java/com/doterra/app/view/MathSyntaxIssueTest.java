package com.doterra.app.view;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to identify specific issues with MATH syntax processing
 */
public class MathSyntaxIssueTest {
    
    @Test
    void testMathDetection() {
        // Test if the template.startsWith("{MATH ", pos) logic works correctly
        String template = "{MATH 5 + 8}";
        
        assertTrue(template.startsWith("{MATH ", 0));
        
        // Test substring extraction
        int pos = 0;
        int end = template.indexOf("}", pos);
        assertEquals(12, end);
        
        String mathExpression = template.substring(pos + 6, end).trim();
        assertEquals("5 + 8", mathExpression);
    }
    
    @Test
    void testTabHandling() {
        // Test if tabs cause issues
        String template = "{MATH\t5\t+\t8\t}";
        System.out.println("Template with tabs: '" + template + "'");
        System.out.println("Starts with '{MATH ': " + template.startsWith("{MATH ", 0));
        System.out.println("Starts with '{MATH': " + template.startsWith("{MATH", 0));
        
        // The issue is here! The pattern looks for "{MATH " (with space) but tabs don't match that
        assertFalse(template.startsWith("{MATH ", 0));
        assertTrue(template.startsWith("{MATH", 0));
    }
    
    @Test
    void testVariousWhitespace() {
        String[] templates = {
            "{MATH 5 + 8}",        // Space after MATH
            "{MATH\t5 + 8}",       // Tab after MATH
            "{MATH\n5 + 8}",       // Newline after MATH
            "{MATH  5 + 8}",       // Multiple spaces after MATH
        };
        
        for (String template : templates) {
            System.out.println("Template: '" + template.replace("\t", "\\t").replace("\n", "\\n") + "'");
            System.out.println("  Starts with '{MATH ': " + template.startsWith("{MATH ", 0));
            System.out.println("  Starts with '{MATH': " + template.startsWith("{MATH", 0));
            
            if (template.startsWith("{MATH", 0)) {
                // Find the first whitespace or closing brace
                int mathStart = 5; // After "{MATH"
                while (mathStart < template.length() && Character.isWhitespace(template.charAt(mathStart))) {
                    mathStart++;
                }
                int end = template.indexOf("}", mathStart);
                if (end != -1) {
                    String expression = template.substring(mathStart, end).trim();
                    System.out.println("  Expression: '" + expression + "'");
                }
            }
            System.out.println();
        }
    }
    
    @Test
    void testPartialExpression() {
        // Test what happens with "5 + " - incomplete expression
        try {
            String expr = "5+";
            double result = parseSimpleExpression(expr);
            System.out.println("Expression '" + expr + "' -> " + result);
        } catch (Exception e) {
            System.out.println("Expression '5+' threw exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
    
    // Simple parser to test partial expressions
    private double parseSimpleExpression(String expr) {
        expr = expr.trim();
        if (expr.isEmpty()) return 0;
        
        // Very simple: just try to parse as number first
        try {
            return Double.parseDouble(expr);
        } catch (NumberFormatException e) {
            // If not a simple number, try basic arithmetic
            if (expr.contains("+")) {
                String[] parts = expr.split("\\+", 2);
                if (parts.length == 2 && !parts[1].trim().isEmpty()) {
                    return Double.parseDouble(parts[0].trim()) + Double.parseDouble(parts[1].trim());
                } else {
                    // Incomplete expression - just return the first part
                    return Double.parseDouble(parts[0].trim());
                }
            }
            throw new NumberFormatException("Cannot parse: " + expr);
        }
    }
}