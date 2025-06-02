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
        titleColumn.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<StickyNote, String> modifiedColumn = new TableColumn<>("Last Modified");
        modifiedColumn.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getLastModifiedDate().format(DATE_FORMAT)
            );
        });
        modifiedColumn.setPrefWidth(150);
        modifiedColumn.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<StickyNote, Void> actionsColumn = new TableColumn<>("Actions");
        actionsColumn.setPrefWidth(280);
        actionsColumn.setStyle("-fx-alignment: CENTER;");
        
        Callback<TableColumn<StickyNote, Void>, TableCell<StickyNote, Void>> cellFactory = 
            new Callback<TableColumn<StickyNote, Void>, TableCell<StickyNote, Void>>() {
            @Override
            public TableCell<StickyNote, Void> call(final TableColumn<StickyNote, Void> param) {
                final TableCell<StickyNote, Void> cell = new TableCell<StickyNote, Void>() {
                    private final Button openBtn = new Button("Open");
                    private final Button renameBtn = new Button("Rename");
                    private final Button deleteBtn = new Button("Delete");
                    private final Button colorBtn = new Button("Color");
                    
                    {
                        openBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-padding: 5 10;");
                        renameBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 5 10;");
                        deleteBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 5 10;");
                        colorBtn.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-padding: 5 10;");
                        
                        openBtn.setOnAction(e -> {
                            StickyNote note = getTableView().getItems().get(getIndex());
                            openStickyNote(note);
                        });
                        
                        renameBtn.setOnAction(e -> {
                            StickyNote note = getTableView().getItems().get(getIndex());
                            renameNote(note);
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
                            buttons.setAlignment(Pos.CENTER);
                            buttons.getChildren().addAll(openBtn, renameBtn, colorBtn, deleteBtn);
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
        
        // Add double-click handler to open notes
        notesTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                StickyNote selectedNote = notesTable.getSelectionModel().getSelectedItem();
                if (selectedNote != null) {
                    openStickyNote(selectedNote);
                }
            }
        });
        
        getChildren().addAll(titleLabel, createNoteBox, notesTable);
    }
    
    private void createNewNote(String title) {
        StickyNote note = new StickyNote(title);
        stickyNotes.add(note);
        saveStickyNotes();
        openStickyNote(note);
    }
    
    private void openStickyNote(StickyNote note) {
        if (openWindows.containsKey(note.getId()) && openWindows.get(note.getId()).isShowing()) {
            Stage window = openWindows.get(note.getId());
            // Restore window if it's minimized
            if (window.isIconified()) {
                window.setIconified(false);
            }
            window.toFront();
            window.requestFocus();
            return;
        }
        
        Stage stickyWindow = new Stage();
        stickyWindow.initStyle(StageStyle.DECORATED);
        stickyWindow.setTitle(note.getTitle());
        stickyWindow.setAlwaysOnTop(true);
        
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
        
        VBox noteLayout = new VBox();
        noteLayout.setPadding(new Insets(5));
        noteLayout.getChildren().add(stickyTextArea);
        VBox.setVgrow(stickyTextArea, Priority.ALWAYS);
        
        // Set position and size from note with validation
        double width = note.getWidth() > 0 ? note.getWidth() : 300;
        double height = note.getHeight() > 0 ? note.getHeight() : 400;
        
        // Ensure window is on screen
        double x = note.getX();
        double y = note.getY();
        if (x < 0) x = 100;
        if (y < 0) y = 100;
        
        Scene scene = new Scene(noteLayout, width - 10, height - 35); // Account for window decorations
        stickyWindow.setScene(scene);
        
        // Set minimum size to ensure window is visible
        stickyWindow.setMinWidth(250);
        stickyWindow.setMinHeight(200);
        
        // Set window position
        stickyWindow.setX(x);
        stickyWindow.setY(y);
        
        // Save position when window moves (after initial setup)
        Platform.runLater(() -> {
            stickyWindow.xProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.doubleValue() >= 0) {
                    note.setX(newVal.doubleValue());
                    saveStickyNotes();
                }
            });
            
            stickyWindow.yProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.doubleValue() >= 0) {
                    note.setY(newVal.doubleValue());
                    saveStickyNotes();
                }
            });
            
            stickyWindow.widthProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.doubleValue() > 0) {
                    note.setWidth(newVal.doubleValue());
                    saveStickyNotes();
                }
            });
            
            stickyWindow.heightProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal != null && newVal.doubleValue() > 0) {
                    note.setHeight(newVal.doubleValue());
                    saveStickyNotes();
                }
            });
        });
        
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
        
        // Show window and ensure it's visible
        stickyWindow.show();
        Platform.runLater(() -> {
            stickyWindow.toFront();
            stickyWindow.setAlwaysOnTop(true);
            stickyWindow.requestFocus();
        });
        
        // Refresh table to show updated last modified time
        notesTable.refresh();
    }
    
    private void renameNote(StickyNote note) {
        TextInputDialog dialog = new TextInputDialog(note.getTitle());
        dialog.setTitle("Rename Note");
        dialog.setHeaderText("Rename '" + note.getTitle() + "'");
        dialog.setContentText("New title:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newTitle -> {
            if (!newTitle.trim().isEmpty()) {
                note.setTitle(newTitle.trim());
                saveStickyNotes();
                
                // Update window title if open
                if (openWindows.containsKey(note.getId())) {
                    openWindows.get(note.getId()).setTitle(newTitle.trim());
                }
                
                // Refresh table to show updated title
                notesTable.refresh();
            }
        });
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