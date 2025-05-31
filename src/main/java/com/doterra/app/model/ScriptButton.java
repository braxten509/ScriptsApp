package com.doterra.app.model;

import javafx.scene.paint.Color;
import java.io.Serializable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;

public class ScriptButton implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String id;
    private String name;
    private String content;
    private transient Color color;
    
    // Serializable color components for persistence
    private double red;
    private double green;
    private double blue;
    private double opacity;
    
    public ScriptButton(String name, String content, Color color) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.content = content;
        setColor(color);
    }
    
    public ScriptButton(ScriptButton other) {
        this.id = UUID.randomUUID().toString();
        this.name = other.name + " (Copy)";
        this.content = other.content;
        setColor(other.getColor());
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
        if (color == null && red >= 0 && green >= 0 && blue >= 0 && opacity >= 0) {
            color = new Color(red, green, blue, opacity);
        }
        return color;
    }
    
    public void setColor(Color color) {
        this.color = color;
        if (color != null) {
            this.red = color.getRed();
            this.green = color.getGreen();
            this.blue = color.getBlue();
            this.opacity = color.getOpacity();
        } else {
            this.red = -1;
            this.green = -1;
            this.blue = -1;
            this.opacity = -1;
        }
    }
    
    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }
    
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Reconstruct color from components
        if (red >= 0 && green >= 0 && blue >= 0 && opacity >= 0) {
            this.color = new Color(red, green, blue, opacity);
        }
    }
}