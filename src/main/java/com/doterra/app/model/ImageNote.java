package com.doterra.app.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

public class ImageNote implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private final String id;
    private byte[] imageData;
    private String note;
    private LocalDateTime timestamp;
    private String fileName;
    private String mimeType;
    
    public ImageNote(byte[] imageData, String fileName, String mimeType) {
        this.id = UUID.randomUUID().toString();
        this.imageData = imageData;
        this.fileName = fileName;
        this.mimeType = mimeType;
        this.note = "";
        this.timestamp = LocalDateTime.now();
    }
    
    public String getId() {
        return id;
    }
    
    public byte[] getImageData() {
        return imageData;
    }
    
    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }
    
    public String getNote() {
        return note;
    }
    
    public void setNote(String note) {
        this.note = note;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getFileName() {
        return fileName;
    }
    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getMimeType() {
        return mimeType;
    }
    
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}