package com.doterra.app.view;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.application.Platform;
import java.io.*;
import java.nio.file.*;

/**
 * Sticky note panel that provides a sidebar tab and floating note window.
 * Stores plain text only and saves automatically on every keystroke.
 */
public class StickyNotePanel extends VBox {
    
    private static final String STICKY_NOTE_FILE = "sticky_note.txt";
    private Stage stickyWindow;
    private TextArea stickyTextArea;
    private String lastSavedContent = "";
    
    public StickyNotePanel() {
        initializePanel();
    }
    
    private void initializePanel() {
        setSpacing(10);
        setPadding(new Insets(20));
        
        Label titleLabel = new Label("Sticky Notes");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        Label descriptionLabel = new Label("Click the button below to open a floating sticky note that stays on top of all windows.");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setStyle("-fx-text-fill: #666666;");
        
        Button openNoteButton = new Button("Open Sticky Note");
        openNoteButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;"
        );
        openNoteButton.setOnAction(e -> openStickyNote());
        
        Button closeNoteButton = new Button("Close Sticky Note");
        closeNoteButton.setStyle(
            "-fx-background-color: #f44336; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 5;"
        );
        closeNoteButton.setOnAction(e -> closeStickyNote());
        
        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(openNoteButton, closeNoteButton);
        
        getChildren().addAll(titleLabel, descriptionLabel, buttonBox);
    }
    
    private void openStickyNote() {
        if (stickyWindow != null && stickyWindow.isShowing()) {
            stickyWindow.toFront();
            return;
        }
        
        createStickyWindow();
        loadStickyNoteContent();
        stickyWindow.show();
        stickyWindow.toFront();
        stickyWindow.setAlwaysOnTop(true);
    }
    
    private void closeStickyNote() {
        if (stickyWindow != null && stickyWindow.isShowing()) {
            stickyWindow.close();
        }
    }
    
    private void createStickyWindow() {
        stickyWindow = new Stage();
        stickyWindow.initStyle(StageStyle.DECORATED);
        stickyWindow.setTitle("Sticky Note");
        stickyWindow.setAlwaysOnTop(true);
        
        // Position at top-right of screen
        stickyWindow.setX(javafx.stage.Screen.getPrimary().getVisualBounds().getMaxX() - 320);
        stickyWindow.setY(50);
        stickyWindow.setWidth(300);
        stickyWindow.setHeight(400);
        
        // Create the text area for the note
        stickyTextArea = new TextArea();
        stickyTextArea.setWrapText(true);
        stickyTextArea.setStyle(
            "-fx-font-family: 'Segoe UI', Arial, sans-serif; " +
            "-fx-font-size: 12px; " +
            "-fx-background-color: #fffbf0; " +
            "-fx-control-inner-background: #fffbf0;"
        );
        
        // Auto-save on every text change
        stickyTextArea.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.equals(lastSavedContent)) {
                Platform.runLater(() -> saveStickyNoteContent(newText));
            }
        });
        
        VBox noteLayout = new VBox();
        noteLayout.setPadding(new Insets(5));
        noteLayout.getChildren().add(stickyTextArea);
        VBox.setVgrow(stickyTextArea, Priority.ALWAYS);
        
        Scene scene = new Scene(noteLayout);
        stickyWindow.setScene(scene);
        
        // Keep window on top when it loses focus
        stickyWindow.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                Platform.runLater(() -> stickyWindow.setAlwaysOnTop(true));
            }
        });
    }
    
    private void loadStickyNoteContent() {
        try {
            Path filePath = Paths.get(STICKY_NOTE_FILE);
            if (Files.exists(filePath)) {
                String content = Files.readString(filePath);
                stickyTextArea.setText(content);
                lastSavedContent = content;
            }
        } catch (IOException e) {
            System.err.println("Error loading sticky note: " + e.getMessage());
        }
    }
    
    private void saveStickyNoteContent(String content) {
        try {
            Files.writeString(Paths.get(STICKY_NOTE_FILE), content);
            lastSavedContent = content;
        } catch (IOException e) {
            System.err.println("Error saving sticky note: " + e.getMessage());
        }
    }
    
    /**
     * Gets the root VBox for this panel.
     * 
     * @return The root VBox
     */
    public VBox getRoot() {
        return this;
    }
    
    /**
     * Cleanup method to ensure sticky note is saved when application closes.
     */
    public void cleanup() {
        if (stickyTextArea != null) {
            saveStickyNoteContent(stickyTextArea.getText());
        }
        if (stickyWindow != null && stickyWindow.isShowing()) {
            stickyWindow.close();
        }
    }
}