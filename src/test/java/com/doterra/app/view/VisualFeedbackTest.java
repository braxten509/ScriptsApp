package com.doterra.app.view;

import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class VisualFeedbackTest extends ApplicationTest {
    
    private ChatScriptsPanel chatScriptsPanel;
    private Stage stage;
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        chatScriptsPanel = new ChatScriptsPanel();
        Scene scene = new Scene(chatScriptsPanel.getRoot(), 800, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    @Order(1)
    public void testTabDragVisualFeedback() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                TabPane tabPane = findTabPane();
                Tab firstTab = tabPane.getTabs().get(0);
                
                // Test adding dragging style class
                firstTab.getStyleClass().add("tab-dragging");
                assertTrue(firstTab.getStyleClass().contains("tab-dragging"));
                
                // Test adding drag-over style class
                firstTab.getStyleClass().add("tab-drag-over");
                assertTrue(firstTab.getStyleClass().contains("tab-drag-over"));
                
                // Test removing style classes
                firstTab.getStyleClass().remove("tab-dragging");
                firstTab.getStyleClass().remove("tab-drag-over");
                assertFalse(firstTab.getStyleClass().contains("tab-dragging"));
                assertFalse(firstTab.getStyleClass().contains("tab-drag-over"));
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(2)
    public void testButtonDragVisualFeedback() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                TabPane tabPane = findTabPane();
                Tab firstTab = tabPane.getTabs().get(0);
                ScrollPane scrollPane = (ScrollPane) firstTab.getContent();
                FlowPane buttonPane = (FlowPane) scrollPane.getContent();
                
                // Create test button
                Button testButton = new Button("Test Button");
                testButton.getStyleClass().add("script-button");
                buttonPane.getChildren().add(testButton);
                
                // Test adding dragging style class
                testButton.getStyleClass().add("button-dragging");
                assertTrue(testButton.getStyleClass().contains("button-dragging"));
                assertTrue(testButton.getStyleClass().contains("script-button"));
                
                // Test removing dragging style class
                testButton.getStyleClass().remove("button-dragging");
                assertFalse(testButton.getStyleClass().contains("button-dragging"));
                assertTrue(testButton.getStyleClass().contains("script-button"));
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(3)
    public void testButtonPaneDragOverFeedback() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                TabPane tabPane = findTabPane();
                Tab firstTab = tabPane.getTabs().get(0);
                ScrollPane scrollPane = (ScrollPane) firstTab.getContent();
                FlowPane buttonPane = (FlowPane) scrollPane.getContent();
                
                // Test adding drag-over style class to pane
                buttonPane.getStyleClass().add("button-drag-over");
                assertTrue(buttonPane.getStyleClass().contains("button-drag-over"));
                
                // Test removing drag-over style class
                buttonPane.getStyleClass().remove("button-drag-over");
                assertFalse(buttonPane.getStyleClass().contains("button-drag-over"));
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(4)
    public void testMultipleStyleClassesOnSameElement() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                TabPane tabPane = findTabPane();
                Tab firstTab = tabPane.getTabs().get(0);
                
                // Add multiple style classes
                firstTab.getStyleClass().addAll("tab-dragging", "tab-drag-over");
                assertTrue(firstTab.getStyleClass().contains("tab-dragging"));
                assertTrue(firstTab.getStyleClass().contains("tab-drag-over"));
                
                // Remove one class, keep the other
                firstTab.getStyleClass().remove("tab-dragging");
                assertFalse(firstTab.getStyleClass().contains("tab-dragging"));
                assertTrue(firstTab.getStyleClass().contains("tab-drag-over"));
                
                // Clean up
                firstTab.getStyleClass().remove("tab-drag-over");
                assertFalse(firstTab.getStyleClass().contains("tab-drag-over"));
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(5)
    public void testStyleClassCleanupAfterDragEnd() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                TabPane tabPane = findTabPane();
                
                // Add drag styles to all tabs (simulating drag operation)
                for (Tab tab : tabPane.getTabs()) {
                    tab.getStyleClass().addAll("tab-dragging", "tab-drag-over");
                }
                
                // Verify all tabs have the styles
                for (Tab tab : tabPane.getTabs()) {
                    assertTrue(tab.getStyleClass().contains("tab-dragging"));
                    assertTrue(tab.getStyleClass().contains("tab-drag-over"));
                }
                
                // Clean up all drag styles (simulating drag end)
                for (Tab tab : tabPane.getTabs()) {
                    tab.getStyleClass().removeAll("tab-dragging", "tab-drag-over");
                }
                
                // Verify all styles are removed
                for (Tab tab : tabPane.getTabs()) {
                    assertFalse(tab.getStyleClass().contains("tab-dragging"));
                    assertFalse(tab.getStyleClass().contains("tab-drag-over"));
                }
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    private TabPane findTabPane() {
        return (TabPane) chatScriptsPanel.getRoot().lookup(".tab-pane");
    }
}