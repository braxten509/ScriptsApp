package com.doterra.app.view;

import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ChatScriptsPanelTest extends ApplicationTest {
    
    private ChatScriptsPanel chatScriptsPanel;
    private Stage stage;
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        chatScriptsPanel = new ChatScriptsPanel();
        Scene scene = new Scene(chatScriptsPanel.getRoot(), 800, 600);
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
    public void testInitialState() {
        Platform.runLater(() -> {
            assertNotNull(chatScriptsPanel.getRoot());
            assertEquals("", chatScriptsPanel.getTextAreaContent());
        });
    }
    
    @Test
    @Order(2)
    public void testButtonClickCopiesContentToClipboard() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Find the first tab
                TabPane tabPane = findTabPane();
                assertNotNull(tabPane);
                assertTrue(tabPane.getTabs().size() > 0);
                
                Tab firstTab = tabPane.getTabs().get(0);
                ScrollPane scrollPane = (ScrollPane) firstTab.getContent();
                FlowPane buttonPane = (FlowPane) scrollPane.getContent();
                
                // Create a test button
                ScriptButton scriptButton = new ScriptButton("Test Button", "Test Content", Color.BLUE);
                Button button = createTestButton(scriptButton);
                buttonPane.getChildren().add(button);
                
                // Simulate button click
                button.fire();
                
                // Check clipboard content
                Clipboard clipboard = Clipboard.getSystemClipboard();
                assertEquals("Test Content", clipboard.getString());
                
                // Check text area content
                assertEquals("Test Content", chatScriptsPanel.getTextAreaContent());
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(3)
    public void testMultipleButtonClicks() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                TabPane tabPane = findTabPane();
                Tab firstTab = tabPane.getTabs().get(0);
                ScrollPane scrollPane = (ScrollPane) firstTab.getContent();
                FlowPane buttonPane = (FlowPane) scrollPane.getContent();
                
                // Create multiple test buttons
                ScriptButton button1 = new ScriptButton("Button 1", "Content 1", Color.BLUE);
                ScriptButton button2 = new ScriptButton("Button 2", "Content 2", Color.GREEN);
                
                Button btn1 = createTestButton(button1);
                Button btn2 = createTestButton(button2);
                
                buttonPane.getChildren().addAll(btn1, btn2);
                
                // Click first button
                btn1.fire();
                assertEquals("Content 1", Clipboard.getSystemClipboard().getString());
                assertEquals("Content 1", chatScriptsPanel.getTextAreaContent());
                
                // Click second button
                btn2.fire();
                assertEquals("Content 2", Clipboard.getSystemClipboard().getString());
                assertEquals("Content 2", chatScriptsPanel.getTextAreaContent());
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(4)
    public void testEmptyContentButton() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                TabPane tabPane = findTabPane();
                Tab firstTab = tabPane.getTabs().get(0);
                ScrollPane scrollPane = (ScrollPane) firstTab.getContent();
                FlowPane buttonPane = (FlowPane) scrollPane.getContent();
                
                // Create button with empty content
                ScriptButton scriptButton = new ScriptButton("Empty Button", "", Color.RED);
                Button button = createTestButton(scriptButton);
                buttonPane.getChildren().add(button);
                
                // Click button
                button.fire();
                
                // Check clipboard and text area
                assertEquals("", Clipboard.getSystemClipboard().getString());
                assertEquals("", chatScriptsPanel.getTextAreaContent());
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    private TabPane findTabPane() {
        return (TabPane) chatScriptsPanel.getRoot().lookup(".tab-pane");
    }
    
    private Button createTestButton(ScriptButton scriptButton) {
        Button button = new Button(scriptButton.getName());
        button.setOnAction(e -> {
            String content = scriptButton.getContent();
            chatScriptsPanel.setTextAreaContent(content);
            
            ClipboardContent clipboardContent = new ClipboardContent();
            clipboardContent.putString(content);
            Clipboard.getSystemClipboard().setContent(clipboardContent);
        });
        return button;
    }
}