package com.doterra.app.view;

import com.doterra.app.model.ImageNote;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ImageNotesPanel extends BorderPane {
    private static final int THUMBNAIL_SIZE = 150;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private final List<ImageNote> imageNotes = new ArrayList<>();
    private final Set<String> processedFiles = new HashSet<>();
    private final TilePane thumbnailGrid;
    private final ScrollPane scrollPane;
    private WatchService watchService;
    private Thread watchThread;
    private boolean isMonitoring = true;
    
    // Common screenshot directories
    private final List<Path> screenshotDirs = new ArrayList<>();
    
    public ImageNotesPanel() {
        getStyleClass().add("image-notes-panel");
        
        // Initialize screenshot directories
        initializeScreenshotDirectories();
        
        // Create toolbar
        ToolBar toolbar = createToolbar();
        setTop(toolbar);
        
        // Create thumbnail grid
        thumbnailGrid = new TilePane();
        thumbnailGrid.setPadding(new Insets(10));
        thumbnailGrid.setHgap(10);
        thumbnailGrid.setVgap(10);
        thumbnailGrid.setPrefColumns(5);
        
        scrollPane = new ScrollPane(thumbnailGrid);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("image-notes-scroll");
        setCenter(scrollPane);
        
        // Load saved images
        loadState();
        
        // Start monitoring for screenshots
        startScreenshotMonitoring();
    }
    
    private void initializeScreenshotDirectories() {
        String userHome = System.getProperty("user.home");
        
        // Windows screenshot directories
        screenshotDirs.add(Paths.get(userHome, "Pictures", "Screenshots"));
        screenshotDirs.add(Paths.get(userHome, "Desktop"));
        screenshotDirs.add(Paths.get(userHome, "Documents"));
        
        // Also check OneDrive if it exists
        Path oneDrivePictures = Paths.get(userHome, "OneDrive", "Pictures", "Screenshots");
        if (Files.exists(oneDrivePictures)) {
            screenshotDirs.add(oneDrivePictures);
        }
        
        // Remove directories that don't exist
        screenshotDirs.removeIf(dir -> !Files.exists(dir));
    }
    
    private ToolBar createToolbar() {
        Button uploadButton = new Button("Upload Image");
        uploadButton.setOnAction(e -> uploadImage());
        
        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> checkForNewScreenshots());
        
        ToggleButton monitorToggle = new ToggleButton("Monitor Screenshots");
        monitorToggle.setSelected(isMonitoring);
        monitorToggle.setOnAction(e -> {
            isMonitoring = monitorToggle.isSelected();
            if (isMonitoring) {
                startScreenshotMonitoring();
            } else {
                stopScreenshotMonitoring();
            }
        });
        
        Label infoLabel = new Label("Images: " + imageNotes.size());
        
        Label monitoringLabel = new Label("Monitoring: " + screenshotDirs.size() + " directories");
        
        ToolBar toolbar = new ToolBar(uploadButton, refreshButton, new Separator(), monitorToggle, 
                                     new Separator(), infoLabel, new Separator(), monitoringLabel);
        toolbar.getStyleClass().add("image-notes-toolbar");
        return toolbar;
    }
    
    private void startScreenshotMonitoring() {
        if (!isMonitoring || screenshotDirs.isEmpty()) return;
        
        try {
            watchService = FileSystems.getDefault().newWatchService();
            
            // Register all screenshot directories
            for (Path dir : screenshotDirs) {
                try {
                    dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE);
                } catch (IOException e) {
                    System.err.println("Failed to register directory: " + dir + " - " + e.getMessage());
                }
            }
            
            // Initial scan for existing screenshots
            checkForNewScreenshots();
            
            // Start watch thread
            watchThread = new Thread(() -> {
                while (isMonitoring) {
                    try {
                        WatchKey key = watchService.poll(1, TimeUnit.SECONDS);
                        if (key != null) {
                            for (WatchEvent<?> event : key.pollEvents()) {
                                if (event.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                                    Path dir = (Path) key.watchable();
                                    Path fullPath = dir.resolve((Path) event.context());
                                    
                                    // Check if it's an image file
                                    String fileName = fullPath.getFileName().toString().toLowerCase();
                                    if (isImageFile(fileName)) {
                                        // Wait a bit for file to be fully written
                                        Thread.sleep(500);
                                        Platform.runLater(() -> processScreenshot(fullPath));
                                    }
                                }
                            }
                            key.reset();
                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        System.err.println("Error in watch service: " + e.getMessage());
                    }
                }
            });
            watchThread.setDaemon(true);
            watchThread.start();
            
        } catch (IOException e) {
            showError("Failed to start screenshot monitoring: " + e.getMessage());
        }
    }
    
    private void stopScreenshotMonitoring() {
        isMonitoring = false;
        if (watchThread != null) {
            watchThread.interrupt();
        }
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                System.err.println("Error closing watch service: " + e.getMessage());
            }
        }
    }
    
    private void checkForNewScreenshots() {
        for (Path dir : screenshotDirs) {
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
                for (Path file : stream) {
                    if (Files.isRegularFile(file) && isImageFile(file.getFileName().toString())) {
                        processScreenshot(file);
                    }
                }
            } catch (IOException e) {
                System.err.println("Error scanning directory " + dir + ": " + e.getMessage());
            }
        }
    }
    
    private boolean isImageFile(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || 
               lower.endsWith(".jpeg") || lower.endsWith(".gif") || 
               lower.endsWith(".bmp");
    }
    
    private void processScreenshot(Path file) {
        String absolutePath = file.toAbsolutePath().toString();
        
        // Skip if already processed
        if (processedFiles.contains(absolutePath)) {
            return;
        }
        
        try {
            // Check if file was created recently (within last hour)
            BasicFileAttributes attrs = Files.readAttributes(file, BasicFileAttributes.class);
            long ageInMinutes = TimeUnit.MILLISECONDS.toMinutes(
                System.currentTimeMillis() - attrs.creationTime().toMillis()
            );
            
            // Only auto-import recent screenshots (created in last hour)
            if (ageInMinutes <= 60) {
                byte[] imageData = Files.readAllBytes(file);
                String mimeType = Files.probeContentType(file);
                ImageNote imageNote = new ImageNote(imageData, file.getFileName().toString(), mimeType);
                
                processedFiles.add(absolutePath);
                addImageNote(imageNote);
            }
        } catch (IOException e) {
            System.err.println("Failed to process screenshot " + file + ": " + e.getMessage());
        }
    }
    
    private void uploadImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.bmp"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File file = fileChooser.showOpenDialog(getScene().getWindow());
        if (file != null) {
            try {
                byte[] imageData = Files.readAllBytes(file.toPath());
                String mimeType = Files.probeContentType(file.toPath());
                ImageNote imageNote = new ImageNote(imageData, file.getName(), mimeType);
                addImageNote(imageNote);
            } catch (IOException e) {
                showError("Failed to load image: " + e.getMessage());
            }
        }
    }
    
    private void addImageNote(ImageNote imageNote) {
        imageNotes.add(imageNote);
        addThumbnail(imageNote);
        saveState();
        updateToolbar();
    }
    
    private void addThumbnail(ImageNote imageNote) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageNote.getImageData());
            Image image = new Image(bais);
            
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(THUMBNAIL_SIZE);
            imageView.setFitHeight(THUMBNAIL_SIZE);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            
            // Create a StackPane to layer the delete button over the image
            StackPane imageContainer = new StackPane();
            
            // Create delete button
            Button deleteBtn = new Button("✕");
            deleteBtn.getStyleClass().add("thumbnail-delete-btn");
            deleteBtn.setVisible(false);
            deleteBtn.setOnAction(e -> {
                e.consume(); // Prevent event from propagating to image click
                deleteImage(imageNote, imageContainer.getParent());
            });
            
            // Position delete button in top-right
            StackPane.setAlignment(deleteBtn, Pos.TOP_RIGHT);
            StackPane.setMargin(deleteBtn, new Insets(5, 5, 0, 0));
            
            imageContainer.getChildren().addAll(imageView, deleteBtn);
            
            VBox thumbnailBox = new VBox(5);
            thumbnailBox.setAlignment(Pos.CENTER);
            thumbnailBox.getStyleClass().add("image-thumbnail");
            
            Label timeLabel = new Label(imageNote.getTimestamp().format(DATE_FORMAT));
            timeLabel.getStyleClass().add("thumbnail-time");
            
            thumbnailBox.getChildren().addAll(imageContainer, timeLabel);
            
            // Show/hide delete button on hover
            thumbnailBox.setOnMouseEntered(e -> deleteBtn.setVisible(true));
            thumbnailBox.setOnMouseExited(e -> deleteBtn.setVisible(false));
            
            // Context menu
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem viewItem = new MenuItem("View Full Size");
            viewItem.setOnAction(e -> showFullImage(imageNote));
            
            MenuItem noteItem = new MenuItem("Edit Note");
            noteItem.setOnAction(e -> editNote(imageNote));
            
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(e -> deleteImage(imageNote, thumbnailBox));
            
            contextMenu.getItems().addAll(viewItem, noteItem, new SeparatorMenuItem(), deleteItem);
            
            imageContainer.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                    showFullImage(imageNote);
                } else if (e.getButton() == MouseButton.SECONDARY) {
                    contextMenu.show(thumbnailBox, e.getScreenX(), e.getScreenY());
                }
            });
            
            thumbnailGrid.getChildren().add(thumbnailBox);
        } catch (Exception e) {
            showError("Failed to create thumbnail: " + e.getMessage());
        }
    }
    
    private void showFullImage(ImageNote imageNote) {
        Stage imageStage = new Stage();
        imageStage.initModality(Modality.APPLICATION_MODAL);
        imageStage.setTitle(imageNote.getFileName());
        
        ByteArrayInputStream bais = new ByteArrayInputStream(imageNote.getImageData());
        Image image = new Image(bais);
        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);
        
        ScrollPane imageScroll = new ScrollPane(imageView);
        imageScroll.setPannable(true);
        imageScroll.setFitToWidth(true);
        imageScroll.setFitToHeight(true);
        
        // Create zoom controls
        HBox zoomControls = new HBox(10);
        zoomControls.setAlignment(Pos.CENTER);
        zoomControls.setPadding(new Insets(5));
        
        Button fitToWindowBtn = new Button("Fit to Window");
        Button actualSizeBtn = new Button("Actual Size");
        Button zoomInBtn = new Button("Zoom In");
        Button zoomOutBtn = new Button("Zoom Out");
        Label zoomLabel = new Label("100%");
        
        fitToWindowBtn.setOnAction(e -> {
            imageView.setFitWidth(imageScroll.getViewportBounds().getWidth());
            imageView.setFitHeight(imageScroll.getViewportBounds().getHeight());
            updateZoomLabel(zoomLabel, imageView, image);
        });
        
        actualSizeBtn.setOnAction(e -> {
            imageView.setFitWidth(image.getWidth());
            imageView.setFitHeight(image.getHeight());
            updateZoomLabel(zoomLabel, imageView, image);
        });
        
        zoomInBtn.setOnAction(e -> {
            imageView.setFitWidth(imageView.getFitWidth() * 1.2);
            imageView.setFitHeight(imageView.getFitHeight() * 1.2);
            updateZoomLabel(zoomLabel, imageView, image);
        });
        
        zoomOutBtn.setOnAction(e -> {
            imageView.setFitWidth(imageView.getFitWidth() / 1.2);
            imageView.setFitHeight(imageView.getFitHeight() / 1.2);
            updateZoomLabel(zoomLabel, imageView, image);
        });
        
        zoomControls.getChildren().addAll(fitToWindowBtn, actualSizeBtn, new Separator(Orientation.VERTICAL),
                                         zoomOutBtn, zoomInBtn, zoomLabel);
        
        // Note display/edit area
        TextArea noteArea = new TextArea(imageNote.getNote());
        noteArea.setWrapText(true);
        noteArea.setPrefRowCount(3);
        noteArea.setPromptText("Add notes about this image...");
        
        // Auto-save notes as user types
        noteArea.textProperty().addListener((observable, oldValue, newValue) -> {
            imageNote.setNote(newValue);
            saveState();
        });
        
        Label noteLabel = new Label("Notes (auto-saved):");
        noteLabel.setStyle("-fx-font-weight: bold;");
        
        VBox bottomControls = new VBox(5);
        bottomControls.getChildren().addAll(noteLabel, noteArea);
        
        BorderPane mainContent = new BorderPane();
        mainContent.setTop(zoomControls);
        mainContent.setCenter(imageScroll);
        mainContent.setBottom(bottomControls);
        BorderPane.setMargin(bottomControls, new Insets(10));
        
        Scene scene = new Scene(mainContent, 1000, 700);
        imageStage.setScene(scene);
        
        // Initially fit to window
        imageStage.setOnShown(e -> {
            imageView.setFitWidth(imageScroll.getViewportBounds().getWidth());
            imageView.setFitHeight(imageScroll.getViewportBounds().getHeight());
            updateZoomLabel(zoomLabel, imageView, image);
        });
        
        imageStage.show();
    }
    
    private void updateZoomLabel(Label zoomLabel, ImageView imageView, Image image) {
        double zoomPercent = (imageView.getFitWidth() / image.getWidth()) * 100;
        zoomLabel.setText(String.format("%.0f%%", zoomPercent));
    }
    
    private void editNote(ImageNote imageNote) {
        TextInputDialog dialog = new TextInputDialog(imageNote.getNote());
        dialog.setTitle("Edit Note");
        dialog.setHeaderText("Edit note for: " + imageNote.getFileName());
        dialog.setContentText("Note:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(note -> {
            imageNote.setNote(note);
            saveState();
        });
    }
    
    private void deleteImage(ImageNote imageNote, javafx.scene.Parent thumbnailParent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Delete Image");
        alert.setHeaderText("Delete this image?");
        alert.setContentText("This action cannot be undone.");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            imageNotes.remove(imageNote);
            thumbnailGrid.getChildren().remove(thumbnailParent);
            saveState();
            updateToolbar();
        }
    }
    
    private void updateToolbar() {
        ToolBar toolbar = (ToolBar) getTop();
        for (int i = 0; i < toolbar.getItems().size(); i++) {
            if (toolbar.getItems().get(i) instanceof Label) {
                Label label = (Label) toolbar.getItems().get(i);
                if (label.getText().startsWith("Images:")) {
                    label.setText("Images: " + imageNotes.size());
                    break;
                }
            }
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void saveState() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
        
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream("data/image_notes.dat"))) {
            oos.writeObject(imageNotes);
            oos.writeObject(processedFiles);
        } catch (IOException e) {
            System.err.println("Failed to save image notes: " + e.getMessage());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void loadState() {
        File dataFile = new File("data/image_notes.dat");
        if (dataFile.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(dataFile))) {
                List<ImageNote> loaded = (List<ImageNote>) ois.readObject();
                imageNotes.addAll(loaded);
                
                // Try to load processed files set
                try {
                    Set<String> loadedFiles = (Set<String>) ois.readObject();
                    processedFiles.addAll(loadedFiles);
                } catch (Exception e) {
                    // Old data file without processed files set
                }
                
                for (ImageNote imageNote : imageNotes) {
                    addThumbnail(imageNote);
                }
                updateToolbar();
            } catch (Exception e) {
                System.err.println("Failed to load image notes: " + e.getMessage());
            }
        }
    }
    
    public void cleanup() {
        stopScreenshotMonitoring();
    }
}