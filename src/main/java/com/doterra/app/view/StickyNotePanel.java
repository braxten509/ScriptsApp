package com.doterra.app.view;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.Scene;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import com.doterra.app.model.StickyNote;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.format.DateTimeFormatter;

/**
 * Sticky note panel that manages multiple sticky notes.
 * Each note can be opened in its own floating window.
 */
public class StickyNotePanel extends VBox {
    
    private static final String STICKY_NOTES_FILE = "data/sticky_notes.dat";
    private static final String OLD_STICKY_NOTE_FILE = "data/sticky_note.txt";
    private ObservableList<StickyNote> stickyNotes;
    private Map<String, Stage> openWindows;
    private Map<String, TextArea> noteTextAreas;
    private TableView<StickyNote> notesTable;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    
    public StickyNotePanel() {
        stickyNotes = FXCollections.observableArrayList();
        openWindows = new HashMap<>();
        noteTextAreas = new HashMap<>();
        initializePanel();
        loadStickyNotes();
    }
    
    private void initializePanel() {
        setSpacing(10);
        setPadding(new Insets(20));
        
        Label titleLabel = new Label("Sticky Notes");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Create new note section
        HBox createNoteBox = new HBox(10);
        createNoteBox.setAlignment(Pos.CENTER_LEFT);
        
        TextField newNoteTitle = new TextField();
        newNoteTitle.setPromptText("Enter note title...");
        newNoteTitle.setPrefWidth(200);
        
        Button createButton = new Button("Create New Note");
        createButton.setStyle(
            "-fx-background-color: #4CAF50; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 8 16; " +
            "-fx-background-radius: 5;"
        );
        createButton.setOnAction(e -> {
            String title = newNoteTitle.getText().trim();
            if (title.isEmpty()) {
                title = "Note " + (stickyNotes.size() + 1);
            }
            createNewNote(title);
            newNoteTitle.clear();
        });
        
        createNoteBox.getChildren().addAll(new Label("Title:"), newNoteTitle, createButton);
        
        // Notes table
        notesTable = new TableView<>();
        notesTable.setPrefHeight(400);
        
        TableColumn<StickyNote, String> titleColumn = new TableColumn<>("Title");
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleColumn.setPrefWidth(200);
        
        TableColumn<StickyNote, String> modifiedColumn = new TableColumn<>("Last Modified");
        modifiedColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getLastModifiedDate().format(DATE_FORMAT)
            );
        });
        modifiedColumn.setPrefWidth(150);
        
        TableColumn<StickyNote, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(200);
        
        Callback<TableColumn<StickyNote, Void>, TableCell<StickyNote, Void>> cellFactory = 
            new Callback<TableColumn<StickyNote, Void>, TableCell<StickyNote, Void>>() {
            @Override
            public TableCell<StickyNote, Void> call(final TableColumn<StickyNote, Void> param) {
                final TableCell<StickyNote, Void> cell = new TableCell<StickyNote, Void>() {
                    private final Button openBtn = new Button("Open");
                    private final Button deleteBtn = new Button("Delete");
                    private final Button colorBtn = new Button("Color");
                    
                    {
                        openBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 5 10;");
                        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 10;");
                        colorBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 5 10;");
                        
                        openBtn.setOnAction(e -> {
                            StickyNote note = getTableView().getItems().get(getIndex());
                            openStickyNote(note);
                        });
                        
                        deleteBtn.setOnAction(e -> {
                            StickyNote note = getTableView().getItems().get(getIndex());
                            deleteNote(note);
                        });
                        
                        colorBtn.setOnAction(e -> {
                            StickyNote note = getTableView().getItems().get(getIndex());
                            changeNoteColor(note);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5);
                            buttons.getChildren().addAll(openBtn, colorBtn, deleteBtn);
                            setGraphic(buttons);
                        }
                    }
                };
                return cell;
            }
        };
        
        actionsColumn.setCellFactory(cellFactory);
        
        notesTable.getColumns().addAll(titleColumn, modifiedColumn, actionsColumn);
        notesTable.setItems(stickyNotes);
        
        // Import old note button (if old file exists)
        Button importOldButton = new Button("Import Old Sticky Note");
        importOldButton.setStyle(
            "-fx-background-color: #9C27B0; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 12px; " +
            "-fx-padding: 6 12; " +
            "-fx-background-radius: 5;"
        );
        importOldButton.setOnAction(e -> importOldStickyNote());
        
        getChildren().addAll(titleLabel, createNoteBox, importOldButton, notesTable);
    }
    
    private void createNewNote(String title) {
        StickyNote note = new StickyNote(title);
        stickyNotes.add(note);
        saveStickyNotes();
        openStickyNote(note);
    }
    
    private void openStickyNote(StickyNote note) {
        if (openWindows.containsKey(note.getId()) && openWindows.get(note.getId()).isShowing()) {
            openWindows.get(note.getId()).toFront();
            return;
        }
        
        Stage stickyWindow = new Stage();
        stickyWindow.initStyle(StageStyle.DECORATED);
        stickyWindow.setTitle(note.getTitle());
        stickyWindow.setAlwaysOnTop(true);
        
        // Set position and size from note
        stickyWindow.setX(note.getX());
        stickyWindow.setY(note.getY());
        stickyWindow.setWidth(note.getWidth());
        stickyWindow.setHeight(note.getHeight());
        
        // Create the text area for the note
        TextArea stickyTextArea = new TextArea(note.getContent());
        stickyTextArea.setWrapText(true);
        stickyTextArea.setStyle(
            "-fx-font-family: 'Segoe UI', Arial, sans-serif; " +
            "-fx-font-size: 12px; " +
            "-fx-background-color: " + note.getColor() + "; " +
            "-fx-control-inner-background: " + note.getColor() + ";"
        );
        
        // Auto-save on every text change
        stickyTextArea.textProperty().addListener((obs, oldText, newText) -> {
            note.setContent(newText);
            Platform.runLater(() -> saveStickyNotes());
        });
        
        // Save position when window moves
        stickyWindow.xProperty().addListener((obs, oldVal, newVal) -> {
            note.setX(newVal.doubleValue());
            saveStickyNotes();
        });
        
        stickyWindow.yProperty().addListener((obs, oldVal, newVal) -> {
            note.setY(newVal.doubleValue());
            saveStickyNotes();
        });
        
        stickyWindow.widthProperty().addListener((obs, oldVal, newVal) -> {
            note.setWidth(newVal.doubleValue());
            saveStickyNotes();
        });
        
        stickyWindow.heightProperty().addListener((obs, oldVal, newVal) -> {
            note.setHeight(newVal.doubleValue());
            saveStickyNotes();
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
        
        stickyWindow.setOnCloseRequest(e -> {
            openWindows.remove(note.getId());
            noteTextAreas.remove(note.getId());
        });
        
        openWindows.put(note.getId(), stickyWindow);
        noteTextAreas.put(note.getId(), stickyTextArea);
        
        stickyWindow.show();
        stickyWindow.toFront();
        stickyWindow.setAlwaysOnTop(true);
        
        // Refresh table to show updated last modified time
        notesTable.refresh();
    }
    
    private void deleteNote(StickyNote note) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Note");
        confirm.setHeaderText("Delete '" + note.getTitle() + "'?");
        confirm.setContentText("This action cannot be undone.");
        
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Close window if open
            if (openWindows.containsKey(note.getId())) {
                openWindows.get(note.getId()).close();
                openWindows.remove(note.getId());
                noteTextAreas.remove(note.getId());
            }
            
            stickyNotes.remove(note);
            saveStickyNotes();
        }
    }
    
    private void changeNoteColor(StickyNote note) {
        List<String> colors = Arrays.asList(
            "#FFEB3B", // Yellow
            "#FF9800", // Orange
            "#4CAF50", // Green
            "#2196F3", // Blue
            "#E91E63", // Pink
            "#9C27B0", // Purple
            "#00BCD4", // Cyan
            "#CDDC39"  // Lime
        );
        
        ChoiceDialog<String> dialog = new ChoiceDialog<>(note.getColor(), colors);
        dialog.setTitle("Choose Color");
        dialog.setHeaderText("Select a color for the sticky note");
        dialog.setContentText("Color:");
        
        // Style the dialog to show colors
        dialog.setGraphic(null);
        ComboBox<String> comboBox = (ComboBox<String>) dialog.getDialogPane().lookup(".combo-box");
        if (comboBox != null) {
            comboBox.setCellFactory(lv -> new ListCell<String>() {
                @Override
                protected void updateItem(String color, boolean empty) {
                    super.updateItem(color, empty);
                    if (empty || color == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(color);
                        setStyle("-fx-background-color: " + color + ";");
                    }
                }
            });
        }
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(color -> {
            note.setColor(color);
            saveStickyNotes();
            
            // Update open window if exists
            if (noteTextAreas.containsKey(note.getId())) {
                TextArea textArea = noteTextAreas.get(note.getId());
                textArea.setStyle(
                    "-fx-font-family: 'Segoe UI', Arial, sans-serif; " +
                    "-fx-font-size: 12px; " +
                    "-fx-background-color: " + color + "; " +
                    "-fx-control-inner-background: " + color + ";"
                );
            }
        });
    }
    
    private void importOldStickyNote() {
        try {
            Path oldFilePath = Paths.get(OLD_STICKY_NOTE_FILE);
            if (Files.exists(oldFilePath)) {
                String content = Files.readString(oldFilePath);
                if (!content.trim().isEmpty()) {
                    StickyNote importedNote = new StickyNote("Imported Note");
                    importedNote.setContent(content);
                    stickyNotes.add(importedNote);
                    saveStickyNotes();
                    
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Import Successful");
                    alert.setHeaderText(null);
                    alert.setContentText("Old sticky note has been imported successfully!");
                    alert.showAndWait();
                    
                    // Delete old file after successful import
                    Files.delete(oldFilePath);
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Old Note Found");
                alert.setHeaderText(null);
                alert.setContentText("No old sticky note file found to import.");
                alert.showAndWait();
            }
        } catch (IOException e) {
            System.err.println("Error importing old sticky note: " + e.getMessage());
        }
    }
    
    private void saveStickyNotes() {
        try {
            // Ensure parent directory exists
            File file = new File(STICKY_NOTES_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(STICKY_NOTES_FILE))) {
                oos.writeObject(new ArrayList<>(stickyNotes));
            }
        } catch (IOException e) {
            System.err.println("Error saving sticky notes: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadStickyNotes() {
        try {
            File file = new File(STICKY_NOTES_FILE);
            if (file.exists()) {
                try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                    List<StickyNote> loadedNotes = (List<StickyNote>) ois.readObject();
                    stickyNotes.clear();
                    stickyNotes.addAll(loadedNotes);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading sticky notes: " + e.getMessage());
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
     * Cleanup method to ensure sticky notes are saved when application closes.
     */
    public void cleanup() {
        saveStickyNotes();
        
        // Close all open windows
        for (Stage window : openWindows.values()) {
            if (window.isShowing()) {
                window.close();
            }
        }
        openWindows.clear();
        noteTextAreas.clear();
    }
}