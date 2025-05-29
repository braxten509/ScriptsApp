package com.doterra.app.util;

import javafx.scene.web.HTMLEditor;

public class HtmlEditor extends HTMLEditor {
    
    public HtmlEditor() {
        super();
        
        // Set the default size
        setPrefHeight(300);
        
        // Remove JavaFX HTMLEditor default stylesheets to customize it
        // Note: This is optional and would allow for full customization
        // setStyleClass();
    }
    
    /**
     * Gets the HTML content of the editor.
     * 
     * @return The HTML content as a string
     */
    @Override
    public String getHtmlText() {
        return super.getHtmlText();
    }
    
    /**
     * Sets the HTML content of the editor.
     * 
     * @param html The HTML content as a string
     */
    @Override
    public void setHtmlText(String html) {
        super.setHtmlText(html);
    }
    
    /**
     * Gets plain text content (without HTML tags).
     * 
     * @return The plain text content
     */
    public String getPlainText() {
        String html = getHtmlText();
        // A simple way to strip HTML tags
        return html.replaceAll("<[^>]*>", "");
    }
}