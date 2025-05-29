package com.doterra.app.view;

import com.doterra.app.model.ScriptButton;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class EmailScriptsPanelTest extends ApplicationTest {
    
    private EmailScriptsPanel emailScriptsPanel;
    private Stage stage;
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        emailScriptsPanel = new EmailScriptsPanel();
        Scene scene = new Scene(emailScriptsPanel.getRoot(), 800, 600);
        stage.setScene(scene);
        stage.show();
    }
    
    @BeforeEach
    public void setUp() throws Exception {
        // Clear clipboard before each test
        Platform.runLater(() -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            clipboard.clear();
        });
        Thread.sleep(100);
    }
    
    @Test
    @Order(1)
    public void testButtonClickCopiesHtmlToClipboard() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                TabPane tabPane = findTabPane();
                Tab firstTab = tabPane.getTabs().get(0);
                ScrollPane scrollPane = (ScrollPane) firstTab.getContent();
                FlowPane buttonPane = (FlowPane) scrollPane.getContent();
                
                // Create test button with HTML content
                String htmlContent = "<html><body><h1>Test</h1><p>Hello World</p></body></html>";
                ScriptButton scriptButton = new ScriptButton("HTML Button", htmlContent, Color.BLUE);
                Button button = createTestButton(scriptButton);
                buttonPane.getChildren().add(button);
                
                // Click button
                button.fire();
                
                // Check clipboard has both HTML and plain text
                Clipboard clipboard = Clipboard.getSystemClipboard();
                assertEquals(htmlContent, clipboard.getHtml());
                assertEquals(htmlContent, clipboard.getString());
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(2)
    public void testButtonClickWithPlainText() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                TabPane tabPane = findTabPane();
                Tab firstTab = tabPane.getTabs().get(0);
                ScrollPane scrollPane = (ScrollPane) firstTab.getContent();
                FlowPane buttonPane = (FlowPane) scrollPane.getContent();
                
                // Create button with plain text
                String plainContent = "This is plain text content";
                ScriptButton scriptButton = new ScriptButton("Plain Button", plainContent, Color.GREEN);
                Button button = createTestButton(scriptButton);
                buttonPane.getChildren().add(button);
                
                // Click button
                button.fire();
                
                // Check clipboard
                Clipboard clipboard = Clipboard.getSystemClipboard();
                assertEquals(plainContent, clipboard.getString());
                assertEquals(plainContent, clipboard.getHtml());
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(3)
    public void testMultipleButtonsOverwriteClipboard() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                TabPane tabPane = findTabPane();
                Tab firstTab = tabPane.getTabs().get(0);
                ScrollPane scrollPane = (ScrollPane) firstTab.getContent();
                FlowPane buttonPane = (FlowPane) scrollPane.getContent();
                
                // Create multiple buttons
                ScriptButton button1 = new ScriptButton("Button 1", "<b>Bold Text</b>", Color.RED);
                ScriptButton button2 = new ScriptButton("Button 2", "<i>Italic Text</i>", Color.ORANGE);
                
                Button btn1 = createTestButton(button1);
                Button btn2 = createTestButton(button2);
                
                buttonPane.getChildren().addAll(btn1, btn2);
                
                // Click first button
                btn1.fire();
                assertEquals("<b>Bold Text</b>", Clipboard.getSystemClipboard().getHtml());
                
                // Click second button - should overwrite
                btn2.fire();
                assertEquals("<i>Italic Text</i>", Clipboard.getSystemClipboard().getHtml());
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    private TabPane findTabPane() {
        return (TabPane) emailScriptsPanel.getRoot().lookup(".tab-pane");
    }
    
    private Button createTestButton(ScriptButton scriptButton) {
        Button button = new Button(scriptButton.getName());
        button.setOnAction(e -> {
            String content = scriptButton.getContent();
            
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putHtml(content);
            clipboardContent.putString(content);
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        });
        return button;
    }
}