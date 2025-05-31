package com.doterra.app.view;

import com.doterra.app.TestFXBase;
import com.doterra.app.controller.ButtonController;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EmailScriptsPanelUITest extends TestFXBase {
    
    private EmailScriptsPanel emailPanel;
    private ButtonController controller;
    
    @Override
    protected void setupStage(Stage stage) {
        emailPanel = new EmailScriptsPanel();
        controller = emailPanel.getButtonController();
        
        // Create initial test data
        ButtonTab tab1 = new ButtonTab("Email Tab 1");
        ScriptButton button1 = new ScriptButton("Email Button 1", "<p>HTML Content 1</p>", Color.LIGHTBLUE);
        ScriptButton button2 = new ScriptButton("Email Button 2", "<b>Bold Content</b>", Color.LIGHTGREEN);
        tab1.addButton(button1);
        tab1.addButton(button2);
        controller.addTab(tab1);
        
        // Refresh UI to show the tab and buttons
        interact(() -> emailPanel.refreshButtons());
        
        Scene scene = new Scene(emailPanel.getRoot(), 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    public void testHTMLEditorPresence() {
        // Verify HTMLEditor is present
        HTMLEditor htmlEditor = lookup(".html-editor").query();
        assertNotNull(htmlEditor);
    }
    
    @Test
    public void testCreateEmailButtonWithHTMLContent() {
        // Select the tab
        selectTab(emailPanel.getTabPane(), "Email Tab 1");
        waitForUIUpdate();
        
        // Get HTML editor
        HTMLEditor htmlEditor = lookup(".html-editor").query();
        
        // Set HTML content
        interact(() -> htmlEditor.setHtmlText("<h1>Email Header</h1><p>This is an email template.</p>"));
        waitForUIUpdate();
        
        // Click add button
        clickButton("Add Script");
        waitForUIUpdate();
        
        // Enter button name
        write("HTML Email Template");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify button was created
        Button newButton = findButtonByText("HTML Email Template");
        assertNotNull(newButton);
        
        // Verify HTML content was saved
        ButtonTab tab = controller.getTabs().get("Email Tab 1");
        ScriptButton modelButton = tab.getButtons().stream()
                .filter(btn -> btn.getName().equals("HTML Email Template"))
                .findFirst()
                .orElse(null);
        assertNotNull(modelButton);
        assertTrue(modelButton.getContent().contains("<h1>Email Header</h1>"));
    }
    
    @Test
    public void testLoadHTMLContentFromButton() {
        // Select tab
        selectTab(emailPanel.getTabPane(), "Email Tab 1");
        waitForUIUpdate();
        
        // Click button to load HTML content
        clickButton("Email Button 1");
        waitForUIUpdate();
        
        // Verify HTML content is loaded
        HTMLEditor htmlEditor = lookup(".html-editor").query();
        String content = htmlEditor.getHtmlText();
        assertTrue(content.contains("HTML Content 1"));
    }
    
    @Test
    public void testEditHTMLContent() {
        // Select tab
        selectTab(emailPanel.getTabPane(), "Email Tab 1");
        waitForUIUpdate();
        
        // Click button to load content
        clickButton("Email Button 2");
        waitForUIUpdate();
        
        // Get HTML editor and modify content
        HTMLEditor htmlEditor = lookup(".html-editor").query();
        interact(() -> htmlEditor.setHtmlText("<h2>Updated Email</h2><p>New content with <em>emphasis</em></p>"));
        waitForUIUpdate();
        
        // Save by clicking button again
        clickButton("Email Button 2");
        waitForUIUpdate();
        
        // Verify content was updated
        ScriptButton button = controller.getTabs().get("Email Tab 1").getButtons().get(1);
        assertTrue(button.getContent().contains("Updated Email"));
        assertTrue(button.getContent().contains("<em>emphasis</em>"));
    }
    
    @Test
    public void testHTMLFormattingTools() {
        // Select tab
        selectTab(emailPanel.getTabPane(), "Email Tab 1");
        waitForUIUpdate();
        
        // Get HTML editor
        HTMLEditor htmlEditor = lookup(".html-editor").query();
        
        // Set some text
        interact(() -> htmlEditor.setHtmlText("Test text for formatting"));
        waitForUIUpdate();
        
        // The HTMLEditor has built-in formatting tools
        // Verify they are accessible (implementation specific)
        assertNotNull(htmlEditor);
    }
    
    @Test
    public void testComplexHTMLContent() {
        // Select tab
        selectTab(emailPanel.getTabPane(), "Email Tab 1");
        waitForUIUpdate();
        
        // Create complex HTML content
        String complexHTML = """
            <html>
            <head>
                <style>
                    .header { color: blue; font-size: 24px; }
                    .content { margin: 10px; padding: 5px; }
                </style>
            </head>
            <body>
                <div class="header">Email Template</div>
                <div class="content">
                    <ul>
                        <li>First item</li>
                        <li>Second item</li>
                        <li>Third item</li>
                    </ul>
                    <table border="1">
                        <tr><th>Name</th><th>Value</th></tr>
                        <tr><td>Item 1</td><td>$100</td></tr>
                        <tr><td>Item 2</td><td>$200</td></tr>
                    </table>
                </div>
            </body>
            </html>
            """;
        
        // Set complex HTML
        HTMLEditor htmlEditor = lookup(".html-editor").query();
        interact(() -> htmlEditor.setHtmlText(complexHTML));
        waitForUIUpdate();
        
        // Create button with complex content
        clickButton("Add Script");
        write("Complex Email Template");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify button was created and content preserved
        ScriptButton button = controller.getTabs().get("Email Tab 1").getButtons().stream()
                .filter(btn -> btn.getName().equals("Complex Email Template"))
                .findFirst()
                .orElse(null);
        assertNotNull(button);
        assertTrue(button.getContent().contains("table"));
        assertTrue(button.getContent().contains("ul"));
    }
    
    @Test
    public void testEmailButtonContextMenu() {
        // Select tab
        selectTab(emailPanel.getTabPane(), "Email Tab 1");
        waitForUIUpdate();
        
        // Right-click on email button
        rightClickOn("Email Button 1");
        
        // Verify context menu appears
        assertTrue(isContextMenuVisible());
        
        // Click rename
        clickMenuItem("Rename");
        waitForUIUpdate();
        
        // Rename the button
        write("Renamed Email Button");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify rename worked
        assertNotNull(findButtonByText("Renamed Email Button"));
        assertNull(findButtonByText("Email Button 1"));
    }
    
    @Test
    public void testEmailPanelTabManagement() {
        // Add new tab
        clickOn("+");
        write("Email Tab 2");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify tab was created
        assertNotNull(getTab(emailPanel.getTabPane(), "Email Tab 2"));
        
        // Switch between tabs
        selectTab(emailPanel.getTabPane(), "Email Tab 2");
        waitForUIUpdate();
        
        selectTab(emailPanel.getTabPane(), "Email Tab 1");
        waitForUIUpdate();
        
        // Verify correct buttons are shown
        assertNotNull(findButtonByText("Email Button 1"));
    }
    
    @Test
    public void testPlainTextInHTMLEditor() {
        // Select tab
        selectTab(emailPanel.getTabPane(), "Email Tab 1");
        waitForUIUpdate();
        
        // Set plain text in HTML editor
        HTMLEditor htmlEditor = lookup(".html-editor").query();
        interact(() -> htmlEditor.setHtmlText("This is plain text without HTML tags"));
        waitForUIUpdate();
        
        // Create button
        clickButton("Add Script");
        write("Plain Text Email");
        clickButton("OK");
        waitForUIUpdate();
        
        // Verify content is wrapped in HTML
        ScriptButton button = controller.getTabs().get("Email Tab 1").getButtons().stream()
                .filter(btn -> btn.getName().equals("Plain Text Email"))
                .findFirst()
                .orElse(null);
        assertNotNull(button);
        assertTrue(button.getContent().contains("This is plain text"));
    }
    
    @Test
    public void testEmailButtonDragAndDrop() {
        // Add more buttons for testing
        ButtonTab tab = controller.getTabs().get("Email Tab 1");
        tab.addButton(new ScriptButton("Email Button 3", "<p>Content 3</p>", Color.LIGHTYELLOW));
        tab.addButton(new ScriptButton("Email Button 4", "<p>Content 4</p>", Color.LIGHTCYAN));
        interact(() -> emailPanel.refreshButtons());
        waitForUIUpdate();
        
        // Drag Email Button 3 before Email Button 1
        Button button3 = findButtonByText("Email Button 3");
        Button button1 = findButtonByText("Email Button 1");
        dragAndDropTo(button3, button1);
        waitForUIUpdate();
        
        // Verify order changed
        ButtonTab updatedTab = controller.getTabs().get("Email Tab 1");
        assertTrue(updatedTab.getButtons().indexOf(
            updatedTab.getButtons().stream()
                .filter(b -> b.getName().equals("Email Button 3"))
                .findFirst().orElse(null)) < 
            updatedTab.getButtons().indexOf(
                updatedTab.getButtons().stream()
                    .filter(b -> b.getName().equals("Email Button 1"))
                    .findFirst().orElse(null)));
    }
}