package com.doterra.app;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.testfx.api.FxRobot;
import org.testfx.api.FxRobotInterface;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.util.concurrent.TimeoutException;

/**
 * Base class for TestFX UI tests providing common functionality
 */
public abstract class TestFXBase extends ApplicationTest {
    
    protected FxRobot robot;
    
    @BeforeAll
    public static void setupHeadlessMode() {
        if (Boolean.getBoolean("headless")) {
            System.setProperty("testfx.robot", "glass");
            System.setProperty("testfx.headless", "true");
            System.setProperty("prism.order", "sw");
            System.setProperty("prism.text", "t2k");
            System.setProperty("java.awt.headless", "true");
        }
    }
    
    @Override
    public void start(Stage stage) throws Exception {
        robot = new FxRobot();
        setupStage(stage);
    }
    
    /**
     * Override this method to setup your stage for testing
     */
    protected abstract void setupStage(Stage stage) throws Exception;
    
    @AfterEach
    public void tearDown() throws TimeoutException {
        FxToolkit.hideStage();
        FxToolkit.cleanupStages();
    }
    
    // Helper methods for common UI interactions
    
    protected void clickButton(String buttonText) {
        clickOn(buttonText);
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    public FxRobotInterface rightClickOn(Node node) {
        moveTo(node);
        clickOn(MouseButton.SECONDARY);
        WaitForAsyncUtils.waitForFxEvents();
        return this;
    }
    
    public FxRobotInterface rightClickOn(String query) {
        clickOn(query, MouseButton.SECONDARY);
        WaitForAsyncUtils.waitForFxEvents();
        return this;
    }
    
    protected void clickMenuItem(String menuItemText) {
        // Try to find and click on MenuItem in context menu
        Node node = lookup(menuItemText).query();
        if (node != null) {
            clickOn(node);
        } else {
            // Try to find MenuItem by traversing context menus
            robot.listWindows().stream()
                .filter(window -> window instanceof ContextMenu)
                .map(window -> (ContextMenu) window)
                .flatMap(menu -> menu.getItems().stream())
                .filter(item -> item.getText().equals(menuItemText))
                .findFirst()
                .ifPresent(MenuItem::fire);
        }
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    protected void typeText(String text) {
        write(text);
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    protected void clearAndType(TextArea textArea, String text) {
        clickOn(textArea);
        push(KeyCode.CONTROL, KeyCode.A);
        write(text);
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    protected void dragAndDropTo(Node source, Node target) {
        drag(source);
        dropTo(target);
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    protected void dragAndDropBy(Node node, double deltaX, double deltaY) {
        drag(node);
        moveBy(deltaX, deltaY);
        drop();
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    protected Tab getTab(TabPane tabPane, String tabText) {
        return tabPane.getTabs().stream()
                .filter(tab -> tab.getText().equals(tabText))
                .findFirst()
                .orElse(null);
    }
    
    protected Button findButtonByText(String buttonText) {
        return lookup(buttonText).queryAs(Button.class);
    }
    
    protected boolean isContextMenuVisible() {
        return !robot.listWindows().stream()
                .filter(window -> window instanceof ContextMenu)
                .findAny()
                .isEmpty();
    }
    
    protected void waitForUIUpdate() {
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    protected void selectTab(TabPane tabPane, String tabText) {
        Tab tab = getTab(tabPane, tabText);
        if (tab != null) {
            interact(() -> tabPane.getSelectionModel().select(tab));
            WaitForAsyncUtils.waitForFxEvents();
        }
    }
    
    protected void pressKey(KeyCode... keys) {
        press(keys);
        release(keys);
        WaitForAsyncUtils.waitForFxEvents();
    }
    
    protected void pressCtrlKey(KeyCode key) {
        pressKey(KeyCode.CONTROL, key);
    }
    
    protected void pressShiftKey(KeyCode key) {
        pressKey(KeyCode.SHIFT, key);
    }
    
    protected void pressAltKey(KeyCode key) {
        pressKey(KeyCode.ALT, key);
    }
}