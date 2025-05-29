package com.doterra.app.model;

import javafx.scene.paint.Color;
import java.io.Serializable;
import java.util.UUID;

public class ScriptButton implements Serializable {
    
    private String id;
    private String name;
    private String content;
    private Color color;
    
    public ScriptButton(String name, String content, Color color) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.content = content;
        this.color = color;
    }
    
    public ScriptButton(ScriptButton other) {
        this.id = UUID.randomUUID().toString();
        this.name = other.name + " (Copy)";
        this.content = other.content;
        this.color = other.color;
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
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Color getColor() {
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
    }
}