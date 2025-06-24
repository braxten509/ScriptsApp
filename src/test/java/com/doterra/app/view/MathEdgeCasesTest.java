package com.doterra.app.view;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test edge cases and potential issues with MATH syntax
 */
public class MathEdgeCasesTest {
    
    @Test
    void testValidMathExpressions() {
        // Test various valid cases that should work
        assertEquals("13", processTemplate("{MATH 5 + 8}"));
        assertEquals("40", processTemplate("{MATH 5 * 8}"));
        assertEquals("2", processTemplate("{MATH 10 / 5}"));
        assertEquals("3", processTemplate("{MATH 10 - 7}"));
        assertEquals("1", processTemplate("{MATH 10 % 3}"));
        assertEquals("25", processTemplate("{MATH (2 + 3) * 5}"));
        assertEquals("14", processTemplate("{MATH 2 + 3 * 4}"));
    }
    
    @Test
    void testInvalidMathExpressions() {
        // Test cases that might cause issues
        assertEquals("0", processTemplate("{MATH abc}"));         // Invalid variable
        assertEquals("0", processTemplate("{MATH 5 + }"));       // Incomplete expression
        assertEquals("0", processTemplate("{MATH + 5}"));        // Invalid start
        assertEquals("0", processTemplate("{MATH }"));           // Empty expression
        assertEquals("0", processTemplate("{MATH 5 / 0}"));      // Division by zero
    }
    
    @Test
    void testMathWithWhitespace() {
        // Test various whitespace scenarios
        assertEquals("13", processTemplate("{MATH 5+8}"));          // No spaces
        assertEquals("13", processTemplate("{MATH 5 + 8}"));        // Normal spaces
        assertEquals("13", processTemplate("{MATH  5  +  8  }"));   // Extra spaces
        assertEquals("13", processTemplate("{MATH\t5\t+\t8\t}"));   // Tabs
        assertEquals("13", processTemplate("{MATH\n5\n+\n8\n}"));   // Newlines (though unlikely)
    }
    
    @Test
    void testUnmatchedBraces() {
        // Test what happens with unmatched braces
        assertEquals("{MATH 5 + 8", processTemplate("{MATH 5 + 8"));   // Missing closing brace
        assertEquals("", processTemplate("MATH 5 + 8}"));               // Missing opening brace
    }
    
    @Test
    void testNestedBraces() {
        // Test expressions with braces inside
        assertEquals("15", processTemplate("{MATH (5 + 8) + 2}"));
        assertEquals("26", processTemplate("{MATH ((5 + 8) * 2)}"));
    }
    
    // Simplified template processor for testing
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
    
    private double evaluateArithmetic(String expression) {
        try {
            // Remove whitespace and newlines
            expression = expression.replaceAll("\\s+", "");
            
            if (expression.isEmpty()) {
                return 0;
            }
            
            // Simple recursive descent parser for arithmetic
            return parseExpression(expression, 0).value;
        } catch (Exception e) {
            // If evaluation fails, return 0 (matching the actual implementation)
            return 0;
        }
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
                    if (right.value == 0) {
                        throw new ArithmeticException("Division by zero");
                    }
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
        if (pos < expr.length() && expr.charAt(pos) == '(') {
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
            // No number found - this is an error case
            throw new NumberFormatException("Expected number at position " + pos);
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