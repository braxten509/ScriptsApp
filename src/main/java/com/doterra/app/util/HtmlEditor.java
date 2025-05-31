package com.doterra.app.util;

import javafx.scene.web.HTMLEditor;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.Node;
import javafx.scene.layout.Priority;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.application.Platform;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import java.util.Optional;

public class HtmlEditor extends HTMLEditor {
    
    private WebView webView;
    private WebEngine webEngine;
    private Button linkButton;
    
    public HtmlEditor() {
        super();
        
        // Set the default size
        setPrefHeight(300);
        
        // Add custom buttons and setup copy handler after the editor is initialized
        Platform.runLater(() -> {
            addCustomButtons();
            setupCopyHandler();
        });
    }
    
    private void addCustomButtons() {
        // Find the toolbar in the HTMLEditor
        Node toolNode = this.lookup(".tool-bar");
        if (toolNode instanceof ToolBar) {
            ToolBar toolBar = (ToolBar) toolNode;
            
            // Create a link button
            linkButton = new Button("Link");
            linkButton.getStyleClass().add("html-editor-button");
            linkButton.setStyle("-fx-font-family: Arial; -fx-font-size: 11px; -fx-font-weight: bold;");
            linkButton.setOnAction(e -> insertLink());
            
            // Add separator and button to toolbar
            toolBar.getItems().addAll(new Separator(), linkButton);
        }
        
        // Get reference to the WebView for executing JavaScript
        webView = (WebView) this.lookup("WebView");
        if (webView != null) {
            webEngine = webView.getEngine();
        }
    }
    
    private void setupCopyHandler() {
        // Override the copy behavior to ensure HTML content is copied
        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.isControlDown() && event.getCode() == KeyCode.C) {
                copyHtmlToClipboard();
                event.consume();
            }
        });
    }
    
    private void copyHtmlToClipboard() {
        if (webEngine != null) {
            // Check if there's selected text
            String selectedHtml = (String) webEngine.executeScript(
                "window.getSelection().rangeCount > 0 ? " +
                "window.getSelection().getRangeAt(0).cloneContents().children.length > 0 || window.getSelection().toString().length > 0 ? " +
                "(function() {" +
                "  var container = document.createElement('div');" +
                "  container.appendChild(window.getSelection().getRangeAt(0).cloneContents());" +
                "  return container.innerHTML;" +
                "})() : null : null"
            );
            
            String htmlContent;
            String plainText;
            
            if (selectedHtml != null && !selectedHtml.isEmpty()) {
                // Copy only selected content
                htmlContent = selectedHtml;
                plainText = (String) webEngine.executeScript("window.getSelection().toString()");
            } else {
                // Copy entire content if no selection
                htmlContent = getHtmlText();
                plainText = htmlContent.replaceAll("<[^>]*>", "");
            }
            
            // Copy to clipboard with both HTML and plain text formats
            ClipboardContent content = new ClipboardContent();
            content.putHtml(htmlContent);
            content.putString(plainText);
            Clipboard.getSystemClipboard().setContent(content);
        }
    }
    
    private void insertLink() {
        // Get the currently selected text
        String selectedText = (String) webView.getEngine().executeScript(
            "window.getSelection().toString();"
        );
        
        // Show dialog to get URL
        TextInputDialog urlDialog = new TextInputDialog("https://");
        urlDialog.setTitle("Insert Link");
        urlDialog.setHeaderText("Enter the URL for the link");
        urlDialog.setContentText("URL:");
        
        // Configure dialog to be independent and always on top
        DialogUtil.configureDialog(urlDialog);
        
        Optional<String> result = urlDialog.showAndWait();
        result.ifPresent(url -> {
            if (!url.trim().isEmpty()) {
                // If no text is selected, ask for link text
                if (selectedText == null || selectedText.trim().isEmpty()) {
                    TextInputDialog textDialog = new TextInputDialog();
                    textDialog.setTitle("Insert Link");
                    textDialog.setHeaderText("Enter the text for the link");
                    textDialog.setContentText("Link text:");
                    
                    // Configure dialog to be independent and always on top
                    DialogUtil.configureDialog(textDialog);
                    
                    Optional<String> textResult = textDialog.showAndWait();
                    textResult.ifPresent(linkText -> {
                        if (!linkText.trim().isEmpty()) {
                            // Insert link with provided text
                            webView.getEngine().executeScript(
                                String.format(
                                    "document.execCommand('insertHTML', false, '<a href=\"%s\">%s</a>');",
                                    escapeJavaScript(url),
                                    escapeJavaScript(linkText)
                                )
                            );
                        }
                    });
                } else {
                    // Create link from selected text
                    webView.getEngine().executeScript(
                        String.format(
                            "document.execCommand('createLink', false, '%s');",
                            escapeJavaScript(url)
                        )
                    );
                }
            }
        });
    }
    
    private String escapeJavaScript(String str) {
        return str.replace("\\", "\\\\")
                  .replace("'", "\\'")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
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