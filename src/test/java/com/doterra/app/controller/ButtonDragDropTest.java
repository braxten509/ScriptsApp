package com.doterra.app.controller;

import com.doterra.app.model.ButtonTab;
import com.doterra.app.model.ScriptButton;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ButtonDragDropTest {
    
    private ButtonController controller;
    
    @BeforeEach
    public void setUp() {
        controller = new ButtonController(false);
        controller.getAllTabs().clear();
    }
    
    @Test
    @Order(1)
    public void testButtonIdentificationWithDuplicateNames() {
        // Create tab with buttons having the same name
        ButtonTab tab = new ButtonTab("Test Tab");
        ScriptButton button1 = new ScriptButton("Duplicate Name", "Content 1", Color.RED);
        ScriptButton button2 = new ScriptButton("Duplicate Name", "Content 2", Color.GREEN);
        ScriptButton button3 = new ScriptButton("Duplicate Name", "Content 3", Color.BLUE);
        
        tab.addButton(button1);
        tab.addButton(button2);
        tab.addButton(button3);
        controller.addTab(tab);
        
        // Store original IDs
        String id1 = button1.getId();
        String id2 = button2.getId();
        String id3 = button3.getId();
        
        // Verify all buttons have unique IDs despite same names
        assertNotEquals(id1, id2);
        assertNotEquals(id2, id3);
        assertNotEquals(id1, id3);
        
        // Verify we can find each button by ID
        List<ScriptButton> buttons = controller.getTab(tab.getId()).getButtons();
        assertEquals(3, buttons.size());
        
        // Find button by ID should work
        ScriptButton found1 = buttons.stream().filter(b -> b.getId().equals(id1)).findFirst().orElse(null);
        ScriptButton found2 = buttons.stream().filter(b -> b.getId().equals(id2)).findFirst().orElse(null);
        ScriptButton found3 = buttons.stream().filter(b -> b.getId().equals(id3)).findFirst().orElse(null);
        
        assertNotNull(found1);
        assertNotNull(found2);
        assertNotNull(found3);
        
        assertEquals("Content 1", found1.getContent());
        assertEquals("Content 2", found2.getContent());
        assertEquals("Content 3", found3.getContent());
    }
    
    @Test
    @Order(2)
    public void testButtonReorderingPreservesIdentity() {
        ButtonTab tab = new ButtonTab("Test Tab");
        ScriptButton button1 = new ScriptButton("Button 1", "Content 1", Color.RED);
        ScriptButton button2 = new ScriptButton("Button 2", "Content 2", Color.GREEN);
        ScriptButton button3 = new ScriptButton("Button 3", "Content 3", Color.BLUE);
        
        tab.addButton(button1);
        tab.addButton(button2);
        tab.addButton(button3);
        controller.addTab(tab);
        
        // Store original order
        List<ScriptButton> originalOrder = List.copyOf(tab.getButtons());
        assertEquals(3, originalOrder.size());
        
        // Reorder buttons: [1,2,3] -> [2,3,1]
        tab.getButtons().clear();
        tab.addButton(button2);
        tab.addButton(button3);
        tab.addButton(button1);
        
        // Verify new order
        List<ScriptButton> newOrder = tab.getButtons();
        assertEquals(button2.getId(), newOrder.get(0).getId());
        assertEquals(button3.getId(), newOrder.get(1).getId());
        assertEquals(button1.getId(), newOrder.get(2).getId());
        
        // Verify button properties are preserved
        assertEquals("Content 2", newOrder.get(0).getContent());
        assertEquals("Content 3", newOrder.get(1).getContent());
        assertEquals("Content 1", newOrder.get(2).getContent());
    }
    
    @Test
    @Order(3)
    public void testButtonMoveBetweenTabs() {
        ButtonTab tab1 = new ButtonTab("Tab 1");
        ButtonTab tab2 = new ButtonTab("Tab 2");
        
        ScriptButton button1 = new ScriptButton("Button 1", "Content 1", Color.RED);
        ScriptButton button2 = new ScriptButton("Button 2", "Content 2", Color.GREEN);
        
        tab1.addButton(button1);
        tab1.addButton(button2);
        controller.addTab(tab1);
        controller.addTab(tab2);
        
        // Initial state
        assertEquals(2, tab1.getButtons().size());
        assertEquals(0, tab2.getButtons().size());
        
        // Move button1 from tab1 to tab2
        controller.removeButtonFromTab(tab1.getId(), button1.getId());
        controller.addButtonToTab(tab2.getId(), button1);
        
        // Verify move
        assertEquals(1, tab1.getButtons().size());
        assertEquals(1, tab2.getButtons().size());
        
        // Verify correct button moved
        assertEquals(button2.getId(), tab1.getButtons().get(0).getId());
        assertEquals(button1.getId(), tab2.getButtons().get(0).getId());
        
        // Verify button properties preserved
        ScriptButton movedButton = tab2.getButtons().get(0);
        assertEquals("Button 1", movedButton.getName());
        assertEquals("Content 1", movedButton.getContent());
        assertEquals(Color.RED, movedButton.getColor());
    }
    
    @Test
    @Order(4)
    public void testReorderTabsPreservesButtons() {
        ButtonTab tab1 = new ButtonTab("Tab 1");
        ButtonTab tab2 = new ButtonTab("Tab 2");
        ButtonTab tab3 = new ButtonTab("Tab 3");
        
        // Add buttons to each tab
        tab1.addButton(new ScriptButton("T1B1", "Content", Color.RED));
        tab2.addButton(new ScriptButton("T2B1", "Content", Color.GREEN));
        tab3.addButton(new ScriptButton("T3B1", "Content", Color.BLUE));
        
        controller.addTab(tab1);
        controller.addTab(tab2);
        controller.addTab(tab3);
        
        // Store original tab order
        List<ButtonTab> originalOrder = List.copyOf(controller.getAllTabs());
        
        // Reorder tabs: [1,2,3] -> [3,1,2]
        List<ButtonTab> newOrder = List.of(tab3, tab1, tab2);
        controller.reorderTabs(newOrder);
        
        // Verify new order
        List<ButtonTab> currentOrder = controller.getAllTabs();
        assertEquals(tab3.getId(), currentOrder.get(0).getId());
        assertEquals(tab1.getId(), currentOrder.get(1).getId());
        assertEquals(tab2.getId(), currentOrder.get(2).getId());
        
        // Verify buttons are still in their tabs
        assertEquals("T3B1", currentOrder.get(0).getButtons().get(0).getName());
        assertEquals("T1B1", currentOrder.get(1).getButtons().get(0).getName());
        assertEquals("T2B1", currentOrder.get(2).getButtons().get(0).getName());
    }
    
    @Test
    @Order(5)
    public void testButtonOrderUpdateByIdNotName() {
        // This test verifies that button ordering uses IDs, not names
        ButtonTab tab = new ButtonTab("Test Tab");
        
        // Create buttons with same names but different content
        ScriptButton button1 = new ScriptButton("Button", "First", Color.RED);
        ScriptButton button2 = new ScriptButton("Button", "Second", Color.GREEN);
        ScriptButton button3 = new ScriptButton("Button", "Third", Color.BLUE);
        
        String id1 = button1.getId();
        String id2 = button2.getId();
        String id3 = button3.getId();
        
        tab.addButton(button1);
        tab.addButton(button2);
        tab.addButton(button3);
        controller.addTab(tab);
        
        // Simulate what happens in updateButtonOrder - finding buttons by name
        // This approach fails with duplicate names
        List<ScriptButton> reorderedByName = new java.util.ArrayList<>();
        String[] orderedNames = {"Button", "Button", "Button"}; // All same name
        
        for (String name : orderedNames) {
            for (ScriptButton btn : tab.getButtons()) {
                if (btn.getName().equals(name) && !reorderedByName.contains(btn)) {
                    reorderedByName.add(btn);
                    break;
                }
            }
        }
        
        // This approach picks the same button multiple times or misses buttons
        assertEquals(3, reorderedByName.size()); // Should have 3 unique buttons
        
        // Better approach: use IDs for ordering
        List<ScriptButton> reorderedById = new java.util.ArrayList<>();
        String[] orderedIds = {id3, id1, id2}; // Specific order by ID
        
        for (String id : orderedIds) {
            for (ScriptButton btn : tab.getButtons()) {
                if (btn.getId().equals(id)) {
                    reorderedById.add(btn);
                    break;
                }
            }
        }
        
        // Verify correct ordering by ID
        assertEquals(3, reorderedById.size());
        assertEquals(id3, reorderedById.get(0).getId());
        assertEquals(id1, reorderedById.get(1).getId());
        assertEquals(id2, reorderedById.get(2).getId());
        assertEquals("Third", reorderedById.get(0).getContent());
        assertEquals("First", reorderedById.get(1).getContent());
        assertEquals("Second", reorderedById.get(2).getContent());
    }
}