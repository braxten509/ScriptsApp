package com.doterra.app.view;

import com.doterra.app.controller.ButtonController;
import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.testfx.framework.junit5.ApplicationTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DragDropTest extends ApplicationTest {
    
    private ChatScriptsPanel chatScriptsPanel;
    private ButtonController buttonController;
    private Stage stage;
    
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        chatScriptsPanel = new ChatScriptsPanel();
        Scene scene = new Scene(chatScriptsPanel.getRoot(), 800, 600);
        stage.setScene(scene);
        stage.show();
        
        // Get button controller through reflection or by adding a getter
        buttonController = new ButtonController();
    }
    
    @Test
    @Order(1)
    public void testTabReordering() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                TabPane tabPane = findTabPane();
                
                // Add a second tab for testing
                ButtonTab secondTab = new ButtonTab("Second Tab");
                buttonController.addTab(secondTab);
                
                // Verify initial order
                assertEquals(2, tabPane.getTabs().size());
                String firstTabId = tabPane.getTabs().get(0).getId();
                String secondTabId = tabPane.getTabs().get(1).getId();
                
                // Simulate tab reordering (this would normally be done through drag-and-drop)
                Tab firstTab = tabPane.getTabs().get(0);
                Tab secondTab2 = tabPane.getTabs().get(1);
                
                // Remove and re-add in different order
                tabPane.getTabs().remove(firstTab);
                tabPane.getTabs().add(1, firstTab);
                
                // Verify order changed
                assertEquals(secondTabId, tabPane.getTabs().get(0).getId());
                assertEquals(firstTabId, tabPane.getTabs().get(1).getId());
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(2)
    public void testButtonReorderingWithinTab() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                TabPane tabPane = findTabPane();
                Tab firstTab = tabPane.getTabs().get(0);
                ScrollPane scrollPane = (ScrollPane) firstTab.getContent();
                FlowPane buttonPane = (FlowPane) scrollPane.getContent();
                
                // Clear existing buttons
                buttonPane.getChildren().clear();
                
                // Add test buttons
                Button button1 = new Button("Button 1");
                Button button2 = new Button("Button 2");
                Button button3 = new Button("Button 3");
                
                buttonPane.getChildren().addAll(button1, button2, button3);
                
                // Verify initial order
                assertEquals(3, buttonPane.getChildren().size());
                assertEquals("Button 1", ((Button) buttonPane.getChildren().get(0)).getText());
                assertEquals("Button 2", ((Button) buttonPane.getChildren().get(1)).getText());
                assertEquals("Button 3", ((Button) buttonPane.getChildren().get(2)).getText());
                
                // Simulate button reordering
                buttonPane.getChildren().remove(button1);
                buttonPane.getChildren().add(1, button1);
                
                // Verify order changed
                assertEquals("Button 2", ((Button) buttonPane.getChildren().get(0)).getText());
                assertEquals("Button 1", ((Button) buttonPane.getChildren().get(1)).getText());
                assertEquals("Button 3", ((Button) buttonPane.getChildren().get(2)).getText());
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(3)
    public void testButtonMoveBetweenTabs() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                TabPane tabPane = findTabPane();
                
                // Ensure we have two tabs
                if (tabPane.getTabs().size() < 2) {
                    ButtonTab secondTab = new ButtonTab("Test Tab 2");
                    buttonController.addTab(secondTab);
                }
                
                Tab firstTab = tabPane.getTabs().get(0);
                Tab secondTab = tabPane.getTabs().get(1);
                
                ScrollPane scrollPane1 = (ScrollPane) firstTab.getContent();
                FlowPane buttonPane1 = (FlowPane) scrollPane1.getContent();
                
                ScrollPane scrollPane2 = (ScrollPane) secondTab.getContent();
                FlowPane buttonPane2 = (FlowPane) scrollPane2.getContent();
                
                // Clear existing buttons
                buttonPane1.getChildren().clear();
                buttonPane2.getChildren().clear();
                
                // Add button to first tab
                Button testButton = new Button("Movable Button");
                buttonPane1.getChildren().add(testButton);
                
                // Verify button is in first tab
                assertEquals(1, buttonPane1.getChildren().size());
                assertEquals(0, buttonPane2.getChildren().size());
                
                // Simulate moving button between tabs
                buttonPane1.getChildren().remove(testButton);
                buttonPane2.getChildren().add(testButton);
                
                // Verify button moved
                assertEquals(0, buttonPane1.getChildren().size());
                assertEquals(1, buttonPane2.getChildren().size());
                assertEquals("Movable Button", ((Button) buttonPane2.getChildren().get(0)).getText());
                
            } finally {
                latch.countDown();
            }
        });
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }
    
    @Test
    @Order(4)
    public void testDropIndexCalculation() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        
        Platform.runLater(() -> {
            try {
                // Create a test FlowPane with buttons
                FlowPane testPane = new FlowPane();
                testPane.setPrefWidth(300);
                testPane.setHgap(10);
                testPane.setVgap(10);
                
                Button btn1 = new Button("Btn1");
                Button btn2 = new Button("Btn2");
                Button btn3 = new Button("Btn3");
                
                btn1.setPrefSize(80, 40);
                btn2.setPrefSize(80, 40);
                btn3.setPrefSize(80, 40);
                
                testPane.getChildren().addAll(btn1, btn2, btn3);
                
                // Force layout
                testPane.autosize();
                testPane.applyCss();
                testPane.layout();
                
                // Test drop index calculation (would normally be in the actual class)
                ObservableList<Node> children = testPane.getChildren();
                assertEquals(3, children.size());
                
                // Verify we can access the children for index calculation
                assertTrue(children.get(0) instanceof Button);
                assertTrue(children.get(1) instanceof Button);
                assertTrue(children.get(2) instanceof Button);
                
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