package com.doterra.app.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ButtonTab implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private List<ScriptButton> buttons;
    
    public ButtonTab(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.buttons = new ArrayList<>();
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<ScriptButton> getButtons() {
        return buttons;
    }
    
    public void setButtons(List<ScriptButton> buttons) {
        this.buttons = buttons;
    }
    
    public void addButton(ScriptButton button) {
        buttons.add(button);
    }
    
    public boolean removeButton(String buttonId) {
        if (buttonId == null) {
            return false;
        }
        return buttons.removeIf(button -> button != null && button.getId().equals(buttonId));
    }
    
    public ScriptButton getButton(String buttonId) {
        if (buttonId == null) {
            return null;
        }
        for (ScriptButton button : buttons) {
            if (button != null && button.getId().equals(buttonId)) {
                return button;
            }
        }
        return null;
    }
}