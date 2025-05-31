package com.doterra.app.view;

import com.doterra.app.controller.ButtonController;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ButtonDragDropTest extends ApplicationTest {
    
    private ChatScriptsPanel chatScriptsPanel;
    private ButtonController buttonController;
    private Stage stage;
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        chatScriptsPanel = new ChatScriptsPanel();
        Scene scene = new Scene(chatScriptsPanel.getRoot(), 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
        
        // Get button controller through reflection
        try {
            Field controllerField = ChatScriptsPanel.class.getDeclaredField("buttonController");
            controllerField.setAccessible(true);
            buttonController = (ButtonController) controllerField.get(chatScriptsPanel);
        } catch (Exception e) {
            fail("Could not access buttonController: " + e.getMessage());
        }
    }
    
    @BeforeEach
    public void setUp() {
        Platform.runLater(() -> {
            // Clear all tabs and buttons before each test
            buttonController.getAllTabs().clear();
            buttonController.saveState();
        });
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    @Test
    @Order(1)
    public void testButtonDragDetection() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Create a tab with a button
                ButtonTab tab = new ButtonTab("Test Tab");
                ScriptButton scriptButton = new ScriptButton("Test Button", "Test Content", Color.BLUE);
                tab.addButton(scriptButton);
                buttonController.addTab(tab);
                
                // Add tab to UI
                chatScriptsPanel = new ChatScriptsPanel();
                Scene scene = new Scene(chatScriptsPanel.getRoot(), 800, 600);
                stage.setScene(scene);
                
                TabPane tabPane = findTabPane();
                Tab uiTab = tabPane.getTabs().get(0);
                ScrollPane scrollPane = (ScrollPane) uiTab.getContent();
                FlowPane buttonPane = (FlowPane) scrollPane.getContent();
                
                // Find the button
                Button button = (Button) buttonPane.getChildren().get(0);
                assertNotNull(button);
                
                // Simulate drag detection
                MouseEvent dragEvent = new MouseEvent(
                    MouseEvent.DRAG_DETECTED,
                    0, 0, 0, 0,
                    MouseButton.PRIMARY, 1,
                    false, false, false, false,
                    true, false, false,
                    false, false, false,
                    null
                );
                
                button.fireEvent(dragEvent);
                
                // Verify drag was initiated
                assertTrue(button.getStyleClass().contains("button-dragging"));
                
            } catch (Exception e) {
                fail("Test failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(2)
    public void testButtonReorderingWithSameNames() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Create a tab with buttons having the same name
                ButtonTab tab = new ButtonTab("Test Tab");
                ScriptButton button1 = new ScriptButton("Button", "Content 1", Color.RED);
                ScriptButton button2 = new ScriptButton("Button", "Content 2", Color.GREEN);
                ScriptButton button3 = new ScriptButton("Button", "Content 3", Color.BLUE);
                
                tab.addButton(button1);
                tab.addButton(button2);
                tab.addButton(button3);
                buttonController.addTab(tab);
                
                // Refresh UI
                chatScriptsPanel = new ChatScriptsPanel();
                Scene scene = new Scene(chatScriptsPanel.getRoot(), 800, 600);
                stage.setScene(scene);
                
                TabPane tabPane = findTabPane();
                Tab uiTab = tabPane.getTabs().get(0);
                ScrollPane scrollPane = (ScrollPane) uiTab.getContent();
                FlowPane buttonPane = (FlowPane) scrollPane.getContent();
                
                // Verify initial order by checking button IDs
                assertEquals(3, buttonPane.getChildren().size());
                
                // Simulate reordering - move first button to last position
                Button firstButton = (Button) buttonPane.getChildren().get(0);
                buttonPane.getChildren().remove(firstButton);
                buttonPane.getChildren().add(firstButton);
                
                // Trigger update
                updateButtonOrder(uiTab);
                
                // Verify the order in the controller matches UI order
                List<ScriptButton> buttons = buttonController.getTab(tab.getId()).getButtons();
                assertEquals(button2.getId(), buttons.get(0).getId());
                assertEquals(button3.getId(), buttons.get(1).getId());
                assertEquals(button1.getId(), buttons.get(2).getId());
                
            } catch (Exception e) {
                fail("Test failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(3)
    public void testButtonDragBetweenTabs() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Create two tabs with buttons
                ButtonTab tab1 = new ButtonTab("Tab 1");
                ButtonTab tab2 = new ButtonTab("Tab 2");
                
                ScriptButton button1 = new ScriptButton("Button 1", "Content 1", Color.RED);
                ScriptButton button2 = new ScriptButton("Button 2", "Content 2", Color.GREEN);
                
                tab1.addButton(button1);
                tab1.addButton(button2);
                buttonController.addTab(tab1);
                buttonController.addTab(tab2);
                
                // Refresh UI
                chatScriptsPanel = new ChatScriptsPanel();
                Scene scene = new Scene(chatScriptsPanel.getRoot(), 800, 600);
                stage.setScene(scene);
                
                TabPane tabPane = findTabPane();
                Tab uiTab1 = tabPane.getTabs().get(0);
                Tab uiTab2 = tabPane.getTabs().get(1);
                
                ScrollPane scrollPane1 = (ScrollPane) uiTab1.getContent();
                FlowPane buttonPane1 = (FlowPane) scrollPane1.getContent();
                
                ScrollPane scrollPane2 = (ScrollPane) uiTab2.getContent();
                FlowPane buttonPane2 = (FlowPane) scrollPane2.getContent();
                
                // Verify initial state
                assertEquals(2, buttonPane1.getChildren().size());
                assertEquals(0, buttonPane2.getChildren().size());
                
                // Simulate drag from tab1 to tab2
                Button dragButton = (Button) buttonPane1.getChildren().get(0);
                
                // Start drag
                MouseEvent dragDetected = new MouseEvent(
                    MouseEvent.DRAG_DETECTED,
                    0, 0, 0, 0,
                    MouseButton.PRIMARY, 1,
                    false, false, false, false,
                    true, false, false,
                    false, false, false,
                    null
                );
                dragButton.fireEvent(dragDetected);
                
                // Simulate drop on tab2
                // Note: Creating a proper DragEvent is complex in JavaFX tests
                // For now, we'll test the logic directly by calling the handler
                
                // This test should expose that drag-drop between tabs doesn't work properly
                
                // Verify button moved
                assertEquals(1, buttonController.getTab(tab1.getId()).getButtons().size());
                assertEquals(1, buttonController.getTab(tab2.getId()).getButtons().size());
                
            } catch (Exception e) {
                fail("Test failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(4)
    public void testDragDropWithPropertyStorage() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Create a tab with buttons
                ButtonTab tab = new ButtonTab("Test Tab");
                ScriptButton scriptButton = new ScriptButton("Test Button", "Content", Color.BLUE);
                tab.addButton(scriptButton);
                buttonController.addTab(tab);
                
                // Refresh UI
                chatScriptsPanel = new ChatScriptsPanel();
                Scene scene = new Scene(chatScriptsPanel.getRoot(), 800, 600);
                stage.setScene(scene);
                
                TabPane tabPane = findTabPane();
                Tab uiTab = tabPane.getTabs().get(0);
                ScrollPane scrollPane = (ScrollPane) uiTab.getContent();
                FlowPane buttonPane = (FlowPane) scrollPane.getContent();
                
                Button button = (Button) buttonPane.getChildren().get(0);
                
                // Verify button properties are set during drag
                MouseEvent dragEvent = new MouseEvent(
                    MouseEvent.DRAG_DETECTED,
                    0, 0, 0, 0,
                    MouseButton.PRIMARY, 1,
                    false, false, false, false,
                    true, false, false,
                    false, false, false,
                    null
                );
                
                button.fireEvent(dragEvent);
                
                // Check if sourceTabId is stored
                assertNotNull(button.getProperties().get("sourceTabId"));
                assertEquals(uiTab.getId(), button.getProperties().get("sourceTabId"));
                
            } catch (Exception e) {
                fail("Test failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(5)
    public void testButtonIdentificationByIdNotName() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Create buttons with duplicate names but different IDs
                ButtonTab tab = new ButtonTab("Test Tab");
                ScriptButton button1 = new ScriptButton("Duplicate", "Content 1", Color.RED);
                ScriptButton button2 = new ScriptButton("Duplicate", "Content 2", Color.BLUE);
                
                tab.addButton(button1);
                tab.addButton(button2);
                buttonController.addTab(tab);
                
                // Store original IDs
                String id1 = button1.getId();
                String id2 = button2.getId();
                
                // Refresh UI
                chatScriptsPanel = new ChatScriptsPanel();
                Scene scene = new Scene(chatScriptsPanel.getRoot(), 800, 600);
                stage.setScene(scene);
                
                TabPane tabPane = findTabPane();
                Tab uiTab = tabPane.getTabs().get(0);
                ScrollPane scrollPane = (ScrollPane) uiTab.getContent();
                FlowPane buttonPane = (FlowPane) scrollPane.getContent();
                
                // Both buttons should be present
                assertEquals(2, buttonPane.getChildren().size());
                
                // Swap button positions
                Button uiButton1 = (Button) buttonPane.getChildren().get(0);
                Button uiButton2 = (Button) buttonPane.getChildren().get(1);
                
                buttonPane.getChildren().clear();
                buttonPane.getChildren().add(uiButton2);
                buttonPane.getChildren().add(uiButton1);
                
                // Update order
                updateButtonOrder(uiTab);
                
                // Verify buttons are correctly identified by ID, not name
                List<ScriptButton> reorderedButtons = buttonController.getTab(tab.getId()).getButtons();
                assertEquals(id2, reorderedButtons.get(0).getId());
                assertEquals(id1, reorderedButtons.get(1).getId());
                
            } catch (Exception e) {
                fail("Test failed: " + e.getMessage());
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    // Helper method to simulate updateButtonOrder from ChatScriptsPanel
    private void updateButtonOrder(Tab tab) {
        try {
            // Use reflection to call private method
            java.lang.reflect.Method method = ChatScriptsPanel.class.getDeclaredMethod("updateButtonOrder", Tab.class);
            method.setAccessible(true);
            method.invoke(chatScriptsPanel, tab);
        } catch (Exception e) {
            fail("Could not call updateButtonOrder: " + e.getMessage());
        }
    }
    
    private TabPane findTabPane() {
        return (TabPane) chatScriptsPanel.getRoot().lookup(".tab-pane");
    }
}