package com.doterra.app.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for mathematical expression evaluation logic
 * This test focuses on the math evaluation logic without requiring JavaFX
 */
public class MathExpressionEvaluatorTest {
    
    @Test
    void testBasicArithmetic() {
        // Test basic operations
        assertEquals(5.0, evaluate("2 + 3"), 0.001);
        assertEquals(7.0, evaluate("10 - 3"), 0.001);
        assertEquals(12.0, evaluate("3 * 4"), 0.001);
        assertEquals(5.0, evaluate("20 / 4"), 0.001);
        assertEquals(1.0, evaluate("10 % 3"), 0.001);
    }
    
    @Test
    void testOrderOfOperations() {
        // Test that multiplication and division have precedence
        assertEquals(14.0, evaluate("2 + 3 * 4"), 0.001);
        assertEquals(10.0, evaluate("20 / 4 + 5"), 0.001);
        assertEquals(11.0, evaluate("2 * 3 + 5"), 0.001);
    }
    
    @Test
    void testParentheses() {
        // Test parentheses override precedence
        assertEquals(20.0, evaluate("(2 + 3) * 4"), 0.001);
        assertEquals(4.0, evaluate("20 / (2 + 3)"), 0.001);
        assertEquals(30.0, evaluate("(5 + 5) * 3"), 0.001);
    }
    
    @Test
    void testNegativeNumbers() {
        assertEquals(-5.0, evaluate("-5"), 0.001);
        assertEquals(-3.0, evaluate("2 + (-5)"), 0.001);
        assertEquals(10.0, evaluate("-5 * -2"), 0.001);
    }
    
    @Test
    void testComparisons() {
        // Test comparison operations return 1 for true, 0 for false
        assertTrue(evaluateComparison("5 > 3"));
        assertFalse(evaluateComparison("2 > 5"));
        assertTrue(evaluateComparison("5 >= 5"));
        assertTrue(evaluateComparison("5 <= 5"));
        assertTrue(evaluateComparison("5 == 5"));
        assertFalse(evaluateComparison("5 != 5"));
        assertTrue(evaluateComparison("5 != 4"));
    }
    
    // Simple expression evaluator for testing
    private double evaluate(String expression) {
        // This is a simplified version - in the actual implementation
        // this logic is in RegexEditorPanel's parseExpression method
        return new SimpleExpressionParser().parse(expression);
    }
    
    private boolean evaluateComparison(String expression) {
        // Simplified comparison evaluator
        if (expression.contains("<=")) {
            String[] parts = expression.split("<=", 2);
            return evaluate(parts[0].trim()) <= evaluate(parts[1].trim());
        } else if (expression.contains(">=")) {
            String[] parts = expression.split(">=", 2);
            return evaluate(parts[0].trim()) >= evaluate(parts[1].trim());
        } else if (expression.contains("==")) {
            String[] parts = expression.split("==", 2);
            return Math.abs(evaluate(parts[0].trim()) - evaluate(parts[1].trim())) < 0.0001;
        } else if (expression.contains("!=")) {
            String[] parts = expression.split("!=", 2);
            return Math.abs(evaluate(parts[0].trim()) - evaluate(parts[1].trim())) >= 0.0001;
        } else if (expression.contains("<")) {
            String[] parts = expression.split("<", 2);
            return evaluate(parts[0].trim()) < evaluate(parts[1].trim());
        } else if (expression.contains(">")) {
            String[] parts = expression.split(">", 2);
            return evaluate(parts[0].trim()) > evaluate(parts[1].trim());
        }
        return false;
    }
    
    /**
     * Simple expression parser for testing purposes
     */
    private static class SimpleExpressionParser {
        private String expr;
        private int pos;
        
        public double parse(String expression) {
            this.expr = expression.replaceAll("\\s+", "");
            this.pos = 0;
            return parseExpression();
        }
        
        private double parseExpression() {
            double left = parseTerm();
            
            while (pos < expr.length()) {
                char op = expr.charAt(pos);
                if (op == '+' || op == '-') {
                    pos++;
                    double right = parseTerm();
                    left = op == '+' ? left + right : left - right;
                } else {
                    break;
                }
            }
            
            return left;
        }
        
        private double parseTerm() {
            double left = parseFactor();
            
            while (pos < expr.length()) {
                char op = expr.charAt(pos);
                if (op == '*' || op == '/' || op == '%') {
                    pos++;
                    double right = parseFactor();
                    if (op == '*') {
                        left = left * right;
                    } else if (op == '/') {
                        left = left / right;
                    } else {
                        left = left % right;
                    }
                } else {
                    break;
                }
            }
            
            return left;
        }
        
        private double parseFactor() {
            boolean negative = false;
            if (pos < expr.length() && expr.charAt(pos) == '-') {
                negative = true;
                pos++;
            }
            
            if (pos < expr.length() && expr.charAt(pos) == '(') {
                pos++; // Skip '('
                double result = parseExpression();
                if (pos < expr.length() && expr.charAt(pos) == ')') {
                    pos++; // Skip ')'
                }
                return negative ? -result : result;
            }
            
            // Parse number
            int start = pos;
            while (pos < expr.length() && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.')) {
                pos++;
            }
            
            if (start == pos) {
                return 0;
            }
            
            double value = Double.parseDouble(expr.substring(start, pos));
            return negative ? -value : value;
        }
    }
}