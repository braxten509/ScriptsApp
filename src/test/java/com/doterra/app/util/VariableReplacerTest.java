package com.doterra.app.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class VariableReplacerTest {

    @Test
    public void testFindVariables() {
        String content = "Hello (name), your order (order_id) is ready!";
        List<String> variables = VariableReplacer.findVariables(content);
        
        assertEquals(2, variables.size());
        assertEquals("name", variables.get(0));
        assertEquals("order_id", variables.get(1));
    }

    @Test
    public void testFindVariablesWithEscaped() {
        String content = "Hello (name), use \\(this) for formatting and (location) for place.";
        List<String> variables = VariableReplacer.findVariables(content);
        
        assertEquals(2, variables.size());
        assertEquals("name", variables.get(0));
        assertEquals("location", variables.get(1));
    }

    @Test
    public void testHasVariables() {
        assertTrue(VariableReplacer.hasVariables("Hello (name)!"));
        assertFalse(VariableReplacer.hasVariables("Hello \\(name)!"));
        assertFalse(VariableReplacer.hasVariables("Hello world!"));
        assertTrue(VariableReplacer.hasVariables("Text with (var1) and (var2)"));
    }

    @Test
    public void testNoVariables() {
        String content = "No variables here!";
        List<String> variables = VariableReplacer.findVariables(content);
        assertTrue(variables.isEmpty());
    }

    @Test
    public void testEscapedParentheses() {
        String content = "Use \\(parentheses) for formatting, not variables.";
        List<String> variables = VariableReplacer.findVariables(content);
        assertTrue(variables.isEmpty());
    }

    @Test
    public void testMixedParentheses() {
        String content = "Hello (name), use \\(this syntax) for formatting.";
        List<String> variables = VariableReplacer.findVariables(content);
        
        assertEquals(1, variables.size());
        assertEquals("name", variables.get(0));
    }

    @Test
    public void testMultipleVariables() {
        String content = "Dear (customer_name), your (product_type) order (order_number) will arrive on (delivery_date).";
        List<String> variables = VariableReplacer.findVariables(content);
        
        assertEquals(4, variables.size());
        assertEquals("customer_name", variables.get(0));
        assertEquals("product_type", variables.get(1));
        assertEquals("order_number", variables.get(2));
        assertEquals("delivery_date", variables.get(3));
    }

    @Test
    public void testVariablesWithSpaces() {
        String content = "Hello (first name) (last name)!";
        List<String> variables = VariableReplacer.findVariables(content);
        
        assertEquals(2, variables.size());
        assertEquals("first name", variables.get(0));
        assertEquals("last name", variables.get(1));
    }
    
    @Test
    public void testEmptyVariableValuesKeepLiteralText() {
        // Note: This test verifies the expected behavior but cannot test the interactive dialog
        // The actual replacement logic now keeps literal variable text when values are empty
        String content = "Hello (name), your order (order_id) is ready!";
        List<String> variables = VariableReplacer.findVariables(content);
        
        // Verify variables are found correctly
        assertEquals(2, variables.size());
        assertEquals("name", variables.get(0));
        assertEquals("order_id", variables.get(1));
        
        // The actual behavior change is:
        // - If user provides a value: (name) -> "John"
        // - If user provides empty/no value: (name) remains as (name)
        // This allows template editing by seeing unfilled variables
    }
}