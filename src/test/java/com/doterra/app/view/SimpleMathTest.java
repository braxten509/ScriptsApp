package com.doterra.app.view;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import java.util.regex.*;

/**
 * Test the math evaluation logic in isolation
 * by replicating the key methods from RegexEditorPanel
 */
public class SimpleMathTest {
    
    @Test
    void testMathProcessing() {
        String template = "{MATH 5 + 8}";
        
        // Process using our extracted logic
        String result = processTemplate(template);
        
        System.out.println("Template: " + template);
        System.out.println("Result: '" + result + "'");
        
        assertEquals("13", result);
    }
    
    @Test
    void testMultipleMath() {
        String template = "Result: {MATH 5 + 8}, Another: {MATH 10 - 3}";
        
        String result = processTemplate(template);
        
        System.out.println("Template: " + template);
        System.out.println("Result: '" + result + "'");
        
        assertEquals("Result: 13, Another: 7", result);
    }
    
    @Test
    void testMathWithSpaces() {
        String[] templates = {
            "{MATH 5+8}",
            "{MATH 5 + 8}",
            "{MATH  5  +  8  }",
            "{MATH 5+ 8}",
            "{MATH 5 +8}"
        };
        
        for (String template : templates) {
            String result = processTemplate(template);
            System.out.println("Template: '" + template + "' -> Result: '" + result + "'");
            assertEquals("13", result, "Failed for template: " + template);
        }
    }
    
    @Test
    void testArithmeticDirectly() {
        // Test the arithmetic parser directly
        assertEquals(13.0, evaluateArithmetic("5 + 8"), 0.001);
        assertEquals(13.0, evaluateArithmetic("5+8"), 0.001);
        assertEquals(13.0, evaluateArithmetic("  5  +  8  "), 0.001);
        assertEquals(40.0, evaluateArithmetic("5 * 8"), 0.001);
        assertEquals(2.5, evaluateArithmetic("10 / 4"), 0.001);
        assertEquals(3.0, evaluateArithmetic("10 - 7"), 0.001);
    }
    
    // Simplified version of the template processing logic
    private String processTemplate(String template) {
        StringBuilder result = new StringBuilder();
        int pos = 0;
        
        while (pos < template.length()) {
            // Handle math expressions
            if (template.startsWith("{MATH ", pos)) {
                int end = template.indexOf("}", pos);
                if (end == -1) {
                    result.append(template.charAt(pos));
                    pos++;
                    continue;
                }
                
                String mathExpression = template.substring(pos + 6, end).trim();
                double value = evaluateArithmetic(mathExpression);
                
                // Format the result nicely (remove .0 for whole numbers)
                if (value == (long) value) {
                    result.append(String.valueOf((long) value));
                } else {
                    result.append(String.valueOf(value));
                }
                
                pos = end + 1;
            }
            else {
                result.append(template.charAt(pos));
                pos++;
            }
        }
        
        return result.toString();
    }
    
    // Simplified arithmetic evaluator based on the RegexEditorPanel logic
    private double evaluateArithmetic(String expression) {
        // Remove whitespace
        expression = expression.replaceAll("\\s+", "");
        
        // Simple recursive descent parser for arithmetic
        return parseExpression(expression, 0).value;
    }
    
    private ParseResult parseExpression(String expr, int pos) {
        ParseResult left = parseTerm(expr, pos);
        pos = left.position;
        
        while (pos < expr.length()) {
            char op = expr.charAt(pos);
            if (op == '+' || op == '-') {
                pos++;
                ParseResult right = parseTerm(expr, pos);
                left.value = op == '+' ? left.value + right.value : left.value - right.value;
                pos = right.position;
            } else {
                break;
            }
        }
        
        return new ParseResult(left.value, pos);
    }
    
    private ParseResult parseTerm(String expr, int pos) {
        ParseResult left = parseFactor(expr, pos);
        pos = left.position;
        
        while (pos < expr.length()) {
            char op = expr.charAt(pos);
            if (op == '*' || op == '/' || op == '%') {
                pos++;
                ParseResult right = parseFactor(expr, pos);
                if (op == '*') {
                    left.value = left.value * right.value;
                } else if (op == '/') {
                    left.value = left.value / right.value;
                } else {
                    left.value = left.value % right.value;
                }
                pos = right.position;
            } else {
                break;
            }
        }
        
        return new ParseResult(left.value, pos);
    }
    
    private ParseResult parseFactor(String expr, int pos) {
        // Skip whitespace
        while (pos < expr.length() && Character.isWhitespace(expr.charAt(pos))) {
            pos++;
        }
        
        if (pos >= expr.length()) {
            return new ParseResult(0, pos);
        }
        
        // Handle negative numbers
        boolean negative = false;
        if (expr.charAt(pos) == '-') {
            negative = true;
            pos++;
        }
        
        // Handle parentheses
        if (expr.charAt(pos) == '(') {
            pos++; // Skip '('
            ParseResult result = parseExpression(expr, pos);
            pos = result.position;
            if (pos < expr.length() && expr.charAt(pos) == ')') {
                pos++; // Skip ')'
            }
            return new ParseResult(negative ? -result.value : result.value, pos);
        }
        
        // Parse number
        int start = pos;
        while (pos < expr.length() && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.')) {
            pos++;
        }
        
        if (start == pos) {
            return new ParseResult(0, pos);
        }
        
        double value = Double.parseDouble(expr.substring(start, pos));
        return new ParseResult(negative ? -value : value, pos);
    }
    
    private static class ParseResult {
        double value;
        int position;
        
        ParseResult(double value, int position) {
            this.value = value;
            this.position = position;
        }
    }
}