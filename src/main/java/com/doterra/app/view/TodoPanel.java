package com.doterra.app.view;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Callback;
import javafx.util.StringConverter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Arrays;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TodoPanel extends BorderPane {
    private TableView<TodoTask> activeTasksTable;
    private TableView<CompletedTask> completedTasksTable;
    private ObservableList<TodoTask> activeTasks;
    private ObservableList<CompletedTask> completedTasks;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm a");
    
    // Property to track ready todo count for badge notifications
    private final IntegerProperty readyTodoCount = new SimpleIntegerProperty(0);
    
    // Timer to check for wait until tasks that should become ready
    private Timeline waitUntilChecker;
    
    // File path for saving todo data
    private static final String TODO_DATA_FILE = "data/doterra_todos.dat";
    
    // Store loaded column widths to apply after table creation
    private Map<String, Double> loadedActiveColumnWidths = new HashMap<>();
    private Map<String, Double> loadedCompletedColumnWidths = new HashMap<>();
    
    // Serializable data classes for persistence
    private static class TodoData implements Serializable {
        private static final long serialVersionUID = 3L; // Increment for backward compatibility
        public List<SerializableTodoTask> activeTasks;
        public List<SerializableCompletedTask> completedTasks;
        public Map<String, Double> activeTableColumnWidths;
        public Map<String, Double> completedTableColumnWidths;
        
        public TodoData() {
            activeTasks = new ArrayList<>();
            completedTasks = new ArrayList<>();
            activeTableColumnWidths = new HashMap<>();
            completedTableColumnWidths = new HashMap<>();
        }
    }
    
    private static class SerializableTodoTask implements Serializable {
        private static final long serialVersionUID = 2L;
        public String name;
        public String id;
        public String description;
        public TaskStatus status;
        public LocalDateTime waitUntil;
        public LocalDateTime lastContacted;
        public LocalDateTime dateCreated;
        
        public SerializableTodoTask() {}
        
        public SerializableTodoTask(TodoTask task) {
            this.name = task.getName();
            this.id = task.getId();
            this.description = task.getDescription();
            this.status = task.getStatus();
            this.waitUntil = task.getWaitUntil();
            this.lastContacted = task.getLastContacted();
            this.dateCreated = task.getDateCreated();
        }
    }
    
    private static class SerializableCompletedTask implements Serializable {
        private static final long serialVersionUID = 1L;
        public String name;
        public String id;
        public String description;
        public LocalDateTime completedDate;
        
        public SerializableCompletedTask() {}
        
        public SerializableCompletedTask(CompletedTask task) {
            this.name = task.getName();
            this.id = task.getId();
            this.description = task.getDescription();
            this.completedDate = task.getCompletedDate();
        }
    }
    
    public static class TodoTask {
        private final BooleanProperty completed = new SimpleBooleanProperty(false);
        private final ObjectProperty<TaskStatus> status = new SimpleObjectProperty<>(TaskStatus.NONE);
        private final StringProperty name = new SimpleStringProperty("");
        private final StringProperty id = new SimpleStringProperty("");
        private final StringProperty description = new SimpleStringProperty("");
        private final ObjectProperty<LocalDateTime> waitUntil = new SimpleObjectProperty<>();
        private final ObjectProperty<LocalDateTime> lastContacted = new SimpleObjectProperty<>();
        private final ObjectProperty<LocalDateTime> dateCreated = new SimpleObjectProperty<>(LocalDateTime.now());
        private final BooleanProperty detailsExpanded = new SimpleBooleanProperty(false);
        
        public TodoTask() {}
        
        public TodoTask(String name, String id, String description) {
            this.name.set(name);
            this.id.set(id);
            this.description.set(description);
        }
        
        // Property getters
        public BooleanProperty completedProperty() { return completed; }
        public ObjectProperty<TaskStatus> statusProperty() { return status; }
        public StringProperty nameProperty() { return name; }
        public StringProperty idProperty() { return id; }
        public StringProperty descriptionProperty() { return description; }
        public ObjectProperty<LocalDateTime> waitUntilProperty() { return waitUntil; }
        public ObjectProperty<LocalDateTime> lastContactedProperty() { return lastContacted; }
        public ObjectProperty<LocalDateTime> dateCreatedProperty() { return dateCreated; }
        public BooleanProperty detailsExpandedProperty() { return detailsExpanded; }
        
        // Getters and setters
        public boolean isCompleted() { return completed.get(); }
        public void setCompleted(boolean completed) { this.completed.set(completed); }
        
        public TaskStatus getStatus() { return status.get(); }
        public void setStatus(TaskStatus status) { this.status.set(status); }
        
        public String getName() { return name.get(); }
        public void setName(String name) { this.name.set(name); }
        
        public String getId() { return id.get(); }
        public void setId(String id) { this.id.set(id); }
        
        public String getDescription() { return description.get(); }
        public void setDescription(String description) { this.description.set(description); }
        
        public LocalDateTime getWaitUntil() { return waitUntil.get(); }
        public void setWaitUntil(LocalDateTime waitUntil) { this.waitUntil.set(waitUntil); }
        
        public LocalDateTime getLastContacted() { return lastContacted.get(); }
        public void setLastContacted(LocalDateTime lastContacted) { this.lastContacted.set(lastContacted); }
        
        public LocalDateTime getDateCreated() { return dateCreated.get(); }
        public void setDateCreated(LocalDateTime dateCreated) { this.dateCreated.set(dateCreated); }
        
        public boolean isDetailsExpanded() { return detailsExpanded.get(); }
        public void setDetailsExpanded(boolean expanded) { this.detailsExpanded.set(expanded); }
    }
    
    public static class CompletedTask {
        private final StringProperty name;
        private final StringProperty id;
        private final StringProperty description;
        private final ObjectProperty<LocalDateTime> completedDate;
        
        public CompletedTask(TodoTask task) {
            this.name = new SimpleStringProperty(task.getName());
            this.id = new SimpleStringProperty(task.getId());
            this.description = new SimpleStringProperty(task.getDescription());
            this.completedDate = new SimpleObjectProperty<>(LocalDateTime.now());
        }
        
        public CompletedTask(String name, String id, String description, LocalDateTime completedDate) {
            this.name = new SimpleStringProperty(name);
            this.id = new SimpleStringProperty(id);
            this.description = new SimpleStringProperty(description);
            this.completedDate = new SimpleObjectProperty<>(completedDate);
        }
        
        public StringProperty nameProperty() { return name; }
        public StringProperty idProperty() { return id; }
        public StringProperty descriptionProperty() { return description; }
        public ObjectProperty<LocalDateTime> completedDateProperty() { return completedDate; }
        
        public String getName() { return name.get(); }
        public String getId() { return id.get(); }
        public String getDescription() { return description.get(); }
        public LocalDateTime getCompletedDate() { return completedDate.get(); }
    }
    
    public enum TaskStatus {
        NONE("None", Color.GRAY),
        READY("Ready", Color.LIGHTGREEN),
        ON_HOLD("On Hold", Color.web("#f44336")), // Same red as remove button
        WAITING_RESPONSE("Waiting for response...", Color.YELLOW),
        WAIT_UNTIL("Wait until", Color.YELLOW);
        
        private final String display;
        private final Color color;
        
        TaskStatus(String display, Color color) {
            this.display = display;
            this.color = color;
        }
        
        public String getDisplay() { return display; }
        public Color getColor() { return color; }
        
        @Override
        public String toString() { return display; }
    }
    
    public TodoPanel() {
        activeTasks = FXCollections.observableArrayList();
        completedTasks = FXCollections.observableArrayList();
        
        // Set up listeners to track ready todo count
        setupReadyTodoTracking();
        
        // Set up timer to check wait until tasks
        setupWaitUntilChecker();
        
        // Load saved todo data
        loadTodoData();
        
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f5f5f5;");
        
        VBox mainContent = new VBox(20);
        
        // Active tasks section
        VBox activeSection = createActiveTasksSection();
        VBox.setVgrow(activeSection, Priority.ALWAYS);
        
        // Completed tasks section - smaller and responsive
        VBox completedSection = createCompletedTasksSection();
        
        mainContent.getChildren().addAll(activeSection, completedSection);
        VBox.setVgrow(mainContent, Priority.ALWAYS);
        
        setCenter(mainContent);
    }
    
    /**
     * Get the property that tracks the number of ready todos for badge notifications
     */
    public IntegerProperty readyTodoCountProperty() {
        return readyTodoCount;
    }
    
    /**
     * Get the current count of ready todos
     */
    public int getReadyTodoCount() {
        return readyTodoCount.get();
    }
    
    /**
     * Set up listeners to automatically track ready todo count changes
     */
    private void setupReadyTodoTracking() {
        // Listen for changes to the active tasks list
        activeTasks.addListener((ListChangeListener<TodoTask>) change -> {
            updateReadyTodoCount();
            
            // Also listen to status changes on each task
            while (change.next()) {
                if (change.wasAdded()) {
                    for (TodoTask task : change.getAddedSubList()) {
                        task.statusProperty().addListener((obs, oldStatus, newStatus) -> updateReadyTodoCount());
                    }
                }
            }
        });
        
        // Initial count
        updateReadyTodoCount();
    }
    
    /**
     * Update the ready todo count by counting tasks with READY status
     */
    private void updateReadyTodoCount() {
        long count = activeTasks.stream()
            .filter(task -> task.getStatus() == TaskStatus.READY)
            .count();
        readyTodoCount.set((int) count);
    }
    
    /**
     * Save todo data to file
     */
    private void saveTodoData() {
        try {
            TodoData data = new TodoData();
            
            // Convert active tasks
            for (TodoTask task : activeTasks) {
                data.activeTasks.add(new SerializableTodoTask(task));
            }
            
            // Convert completed tasks
            for (CompletedTask task : completedTasks) {
                data.completedTasks.add(new SerializableCompletedTask(task));
            }
            
            // Save column widths from active tasks table
            if (activeTasksTable != null) {
                for (TableColumn<TodoTask, ?> column : activeTasksTable.getColumns()) {
                    if (column.getText() != null && !column.getText().isEmpty()) {
                        data.activeTableColumnWidths.put(column.getText(), column.getWidth());
                    }
                }
            }
            
            // Save column widths from completed tasks table
            if (completedTasksTable != null) {
                for (TableColumn<CompletedTask, ?> column : completedTasksTable.getColumns()) {
                    if (column.getText() != null && !column.getText().isEmpty()) {
                        data.completedTableColumnWidths.put(column.getText(), column.getWidth());
                    }
                }
            }
            
            // Save to file
            // Ensure parent directory exists
            File file = new File(TODO_DATA_FILE);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TODO_DATA_FILE))) {
                oos.writeObject(data);
            }
        } catch (Exception e) {
            System.err.println("Error saving todo data: " + e.getMessage());
        }
    }
    
    /**
     * Load todo data from file
     */
    private void loadTodoData() {
        try {
            if (!Files.exists(Paths.get(TODO_DATA_FILE))) {
                return; // No saved data yet
            }
            
            Object loadedData;
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(TODO_DATA_FILE))) {
                loadedData = ois.readObject();
            }
            
            // Clear existing data
            activeTasks.clear();
            completedTasks.clear();
            
            // Handle different data format versions
            if (loadedData instanceof TodoData) {
                // Current format (version 3+)
                loadCurrentFormat((TodoData) loadedData);
            } else if (loadedData instanceof List) {
                // Legacy format - likely a simple list of tasks
                loadLegacyListFormat((List<?>) loadedData);
            } else if (loadedData instanceof Map) {
                // Legacy format - might be a map-based structure
                loadLegacyMapFormat((Map<?, ?>) loadedData);
            } else {
                // Try to handle as older TodoData format with different serialVersionUID
                loadOlderTodoDataFormat(loadedData);
            }
            
        } catch (Exception e) {
            System.err.println("Error loading todo data: " + e.getMessage());
            e.printStackTrace(); // More detailed error information for debugging
        }
    }
    
    /**
     * Load data using the current TodoData format (version 3+)
     */
    private void loadCurrentFormat(TodoData data) {
        // Load active tasks
        if (data.activeTasks != null) {
            for (SerializableTodoTask serializable : data.activeTasks) {
                TodoTask task = new TodoTask(
                    serializable.name != null ? serializable.name : "Untitled", 
                    serializable.id != null ? serializable.id : UUID.randomUUID().toString(), 
                    serializable.description != null ? serializable.description : ""
                );
                
                if (serializable.status != null) {
                    task.setStatus(serializable.status);
                }
                if (serializable.waitUntil != null) {
                    task.setWaitUntil(serializable.waitUntil);
                }
                // Handle new fields for backward compatibility
                if (serializable.lastContacted != null) {
                    task.setLastContacted(serializable.lastContacted);
                }
                if (serializable.dateCreated != null) {
                    task.setDateCreated(serializable.dateCreated);
                }
                activeTasks.add(task);
                addTaskListeners(task);
            }
        }
        
        // Load completed tasks
        if (data.completedTasks != null) {
            for (SerializableCompletedTask serializable : data.completedTasks) {
                CompletedTask task = new CompletedTask(
                    serializable.name != null ? serializable.name : "Untitled",
                    serializable.id != null ? serializable.id : UUID.randomUUID().toString(),
                    serializable.description != null ? serializable.description : "",
                    serializable.completedDate != null ? serializable.completedDate : LocalDateTime.now()
                );
                completedTasks.add(task);
            }
        }
        
        // Load column widths (handle backward compatibility)
        if (data.activeTableColumnWidths != null) {
            loadedActiveColumnWidths.putAll(data.activeTableColumnWidths);
        }
        if (data.completedTableColumnWidths != null) {
            loadedCompletedColumnWidths.putAll(data.completedTableColumnWidths);
        }
    }
    
    /**
     * Load legacy format that was a simple list of tasks
     */
    @SuppressWarnings("unchecked")
    private void loadLegacyListFormat(List<?> taskList) {
        System.out.println("Loading legacy list format TODO data...");
        
        for (Object taskObj : taskList) {
            try {
                // Try to extract task information using reflection or known patterns
                if (taskObj instanceof Map) {
                    Map<String, Object> taskMap = (Map<String, Object>) taskObj;
                    String name = (String) taskMap.getOrDefault("name", "Imported Task");
                    String description = (String) taskMap.getOrDefault("description", "");
                    String id = (String) taskMap.getOrDefault("id", UUID.randomUUID().toString());
                    
                    TodoTask task = new TodoTask(name, id, description);
                    
                    // Try to get status if available
                    Object statusObj = taskMap.get("status");
                    if (statusObj instanceof String) {
                        try {
                            TaskStatus status = TaskStatus.valueOf((String) statusObj);
                            task.setStatus(status);
                        } catch (IllegalArgumentException e) {
                            // Use default status
                        }
                    }
                    
                    activeTasks.add(task);
                    addTaskListeners(task);
                } else {
                    // Handle other task object formats
                    String taskName = taskObj.toString();
                    if (taskName != null && !taskName.trim().isEmpty()) {
                        TodoTask task = new TodoTask(taskName, UUID.randomUUID().toString(), "");
                        activeTasks.add(task);
                        addTaskListeners(task);
                    }
                }
            } catch (Exception e) {
                System.err.println("Error loading legacy task: " + e.getMessage());
            }
        }
    }
    
    /**
     * Load legacy format that was a map-based structure
     */
    @SuppressWarnings("unchecked")
    private void loadLegacyMapFormat(Map<?, ?> dataMap) {
        System.out.println("Loading legacy map format TODO data...");
        
        try {
            // Try to find tasks in the map
            Object tasksObj = dataMap.get("tasks");
            if (tasksObj == null) {
                tasksObj = dataMap.get("activeTasks");
            }
            if (tasksObj == null) {
                tasksObj = dataMap.get("todoTasks");
            }
            
            if (tasksObj instanceof List) {
                loadLegacyListFormat((List<?>) tasksObj);
            } else {
                // Try to convert map entries to tasks
                for (Map.Entry<?, ?> entry : dataMap.entrySet()) {
                    String key = entry.getKey().toString();
                    String value = entry.getValue().toString();
                    
                    if (!key.isEmpty() && !value.isEmpty()) {
                        TodoTask task = new TodoTask(key, UUID.randomUUID().toString(), value);
                        activeTasks.add(task);
                        addTaskListeners(task);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading legacy map format: " + e.getMessage());
        }
    }
    
    /**
     * Try to load older TodoData formats with different serialVersionUID
     */
    private void loadOlderTodoDataFormat(Object data) {
        System.out.println("Attempting to load older TodoData format...");
        
        try {
            // Use reflection to access fields that might exist in older versions
            java.lang.reflect.Field[] fields = data.getClass().getDeclaredFields();
            
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(data);
                
                if (field.getName().contains("task") && value instanceof List) {
                    loadLegacyListFormat((List<?>) value);
                    break;
                }
            }
        } catch (Exception e) {
            System.err.println("Error loading older TodoData format: " + e.getMessage());
            // As a last resort, create a single task with the object's string representation
            String taskName = data.toString();
            if (taskName != null && !taskName.trim().isEmpty() && !taskName.contains("@")) {
                TodoTask task = new TodoTask("Imported: " + taskName, UUID.randomUUID().toString(), "Automatically imported from old format");
                activeTasks.add(task);
                addTaskListeners(task);
            }
        }
    }
    
    /**
     * Add all necessary listeners to a task
     */
    private void addTaskListeners(TodoTask task) {
        task.statusProperty().addListener((obs, oldStatus, newStatus) -> saveTodoData());
        task.nameProperty().addListener((obs, oldValue, newValue) -> saveTodoData());
        task.idProperty().addListener((obs, oldValue, newValue) -> saveTodoData());
        task.descriptionProperty().addListener((obs, oldValue, newValue) -> saveTodoData());
        task.waitUntilProperty().addListener((obs, oldValue, newValue) -> saveTodoData());
        task.lastContactedProperty().addListener((obs, oldValue, newValue) -> saveTodoData());
        task.dateCreatedProperty().addListener((obs, oldValue, newValue) -> saveTodoData());
    }
    
    /**
     * Set up a timer to periodically check if any "Wait until" tasks should become "Ready"
     */
    private void setupWaitUntilChecker() {
        waitUntilChecker = new Timeline(new KeyFrame(Duration.seconds(30), e -> checkWaitUntilTasks()));
        waitUntilChecker.setCycleCount(Timeline.INDEFINITE);
        waitUntilChecker.play();
    }
    
    /**
     * Check all tasks with WAIT_UNTIL status and change to READY if time has passed
     */
    private void checkWaitUntilTasks() {
        LocalDateTime now = LocalDateTime.now();
        boolean anyChanged = false;
        
        for (TodoTask task : activeTasks) {
            if (task.getStatus() == TaskStatus.WAIT_UNTIL && task.getWaitUntil() != null) {
                if (now.isAfter(task.getWaitUntil()) || now.equals(task.getWaitUntil())) {
                    task.setStatus(TaskStatus.READY);
                    task.setWaitUntil(null); // Clear the wait until time
                    anyChanged = true;
                }
            }
        }
        
        // Refresh the table and save data if any tasks changed
        if (anyChanged) {
            activeTasksTable.refresh();
            saveTodoData();
        }
    }
    
    private VBox createActiveTasksSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: white; -fx-border-color: #2196F3; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        section.setPadding(new Insets(15));
        
        Label titleLabel = new Label("Active Tasks");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Control buttons
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        controls.setPadding(new Insets(10, 0, 10, 0));
        
        Button addRowBtn = new Button("Add Task");
        addRowBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        addRowBtn.setOnAction(e -> addNewTask());
        
        Button removeRowBtn = new Button("Remove Selected");
        removeRowBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
        removeRowBtn.setOnAction(e -> removeSelectedTask());
        
        controls.getChildren().addAll(addRowBtn, removeRowBtn);
        
        // Create table with scroll
        activeTasksTable = new TableView<>(activeTasks);
        activeTasksTable.setEditable(true);
        
        setupActiveTasksTable();
        setupRowDragAndDrop();
        
        // Add table directly without ScrollPane wrapper
        VBox.setVgrow(activeTasksTable, Priority.ALWAYS);
        
        section.getChildren().addAll(titleLabel, controls, activeTasksTable);
        VBox.setVgrow(section, Priority.ALWAYS);
        return section;
    }
    
    private void setupActiveTasksTable() {
        // Checkbox column
        TableColumn<TodoTask, Boolean> checkCol = new TableColumn<>("");
        checkCol.setPrefWidth(25);
        checkCol.setMinWidth(25);
        checkCol.setMaxWidth(25);
        checkCol.setResizable(false);
        checkCol.setCellValueFactory(cellData -> cellData.getValue().completedProperty());
        checkCol.setCellFactory(column -> {
            CheckBoxTableCell<TodoTask, Boolean> cell = new CheckBoxTableCell<TodoTask, Boolean>() {
                @Override
                public void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (!empty) {
                        // Add listener to the checkbox directly
                        CheckBox checkBox = (CheckBox) getGraphic();
                        if (checkBox != null) {
                            checkBox.setOnAction(e -> {
                                if (checkBox.isSelected()) {
                                    TodoTask task = getTableView().getItems().get(getIndex());
                                    // Use Platform.runLater to avoid concurrent modification
                                    javafx.application.Platform.runLater(() -> moveToCompleted(task));
                                }
                            });
                        }
                    }
                }
            };
            return cell;
        });
        
        // Status column with custom cell factory
        TableColumn<TodoTask, TaskStatus> statusCol = new TableColumn<>("Status");
        statusCol.setPrefWidth(150); // Reduced to fit screen
        statusCol.setMinWidth(100);
        statusCol.setResizable(true);
        statusCol.setStyle("-fx-border-color: transparent #cccccc transparent transparent; -fx-border-width: 0 1 0 0;");
        statusCol.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
        statusCol.setCellFactory(column -> new StatusCell());
        
        // Name column
        TableColumn<TodoTask, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(140);
        nameCol.setMinWidth(80);
        nameCol.setResizable(true);
        nameCol.setStyle("-fx-border-color: transparent #cccccc transparent transparent; -fx-border-width: 0 1 0 0;");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setCellFactory(column -> new EditableTextCell());
        nameCol.setOnEditCommit(event -> {
            event.getRowValue().setName(event.getNewValue());
        });
        
        // ID column
        TableColumn<TodoTask, String> idCol = new TableColumn<>("ID");
        idCol.setPrefWidth(60);
        idCol.setMinWidth(40);
        idCol.setResizable(true);
        idCol.setStyle("-fx-border-color: transparent #cccccc transparent transparent; -fx-border-width: 0 1 0 0;");
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        idCol.setCellFactory(column -> new EditableTextCell());
        idCol.setOnEditCommit(event -> {
            event.getRowValue().setId(event.getNewValue());
        });
        
        // Last Contacted column
        TableColumn<TodoTask, LocalDateTime> lastContactedCol = new TableColumn<>("Last Contacted");
        lastContactedCol.setPrefWidth(180);
        lastContactedCol.setMinWidth(120);
        lastContactedCol.setResizable(true);
        lastContactedCol.setStyle("-fx-border-color: transparent #cccccc transparent transparent; -fx-border-width: 0 1 0 0;");
        lastContactedCol.setCellValueFactory(cellData -> cellData.getValue().lastContactedProperty());
        lastContactedCol.setCellFactory(column -> new TableCell<TodoTask, LocalDateTime>() {
            private final Button button = new Button("Update");
            private final Label label = new Label();
            private final HBox hbox = new HBox(5);
            private boolean initialized = false;
            
            {
                // Initialize layout once
                hbox.setAlignment(Pos.CENTER);
                hbox.getChildren().addAll(label, button);
                
                button.setOnAction(e -> {
                    if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                        TodoTask task = getTableView().getItems().get(getIndex());
                        task.setLastContacted(LocalDateTime.now());
                        saveTodoData();
                    }
                });
            }
            
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    initialized = false; // Reset when cell becomes empty
                } else {
                    // Only update the label text, don't recreate the layout
                    if (item == null) {
                        label.setText("Never");
                    } else {
                        label.setText(item.format(DATE_FORMAT));
                    }
                    
                    // Always ensure the graphic is set (handles restore case)
                    if (getGraphic() != hbox) {
                        setGraphic(hbox);
                        setAlignment(Pos.CENTER);
                    }
                }
            }
        });
        
        // Date Created column
        TableColumn<TodoTask, LocalDateTime> dateCreatedCol = new TableColumn<>("Date Created");
        dateCreatedCol.setPrefWidth(130);
        dateCreatedCol.setMinWidth(100);
        dateCreatedCol.setResizable(true);
        dateCreatedCol.setStyle("-fx-border-color: transparent #cccccc transparent transparent; -fx-border-width: 0 1 0 0;");
        dateCreatedCol.setCellValueFactory(cellData -> cellData.getValue().dateCreatedProperty());
        dateCreatedCol.setCellFactory(column -> new TableCell<TodoTask, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DATE_FORMAT));
                }
                setAlignment(Pos.CENTER);
            }
        });
        
        // Details column (renamed from Description)
        TableColumn<TodoTask, String> detailsCol = new TableColumn<>("Details");
        detailsCol.setPrefWidth(220);
        detailsCol.setMinWidth(150);
        detailsCol.setResizable(true);
        detailsCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        detailsCol.setCellFactory(column -> new ExpandableDetailsCell());
        detailsCol.setOnEditCommit(event -> {
            event.getRowValue().setDescription(event.getNewValue());
        });
        
        
        activeTasksTable.getColumns().addAll(checkCol, statusCol, nameCol, idCol, lastContactedCol, dateCreatedCol, detailsCol);
        
        // Use CONSTRAINED_RESIZE_POLICY to make the table stretch to fill available width
        activeTasksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Enable row selection
        activeTasksTable.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        
        // Apply saved column widths
        applyColumnWidths(activeTasksTable, loadedActiveColumnWidths);
        
        // Add listeners to save column widths when they change
        for (TableColumn<TodoTask, ?> column : activeTasksTable.getColumns()) {
            column.widthProperty().addListener((obs, oldWidth, newWidth) -> saveTodoData());
        }
        
        // Add global click listener to collapse expanded details when clicking outside
        activeTasksTable.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
            // Get the source of the click
            Object source = event.getTarget();
            
            // Check if any task has expanded details
            for (TodoTask task : activeTasks) {
                if (task.isDetailsExpanded()) {
                    // Check if the click was on a TextArea (expanded details) - if so, don't collapse
                    if (source instanceof TextArea) {
                        System.out.println("DEBUG: Click on TextArea - not collapsing");
                        return;
                    }
                    
                    // Use Platform.runLater to allow focus listener to trigger first
                    javafx.application.Platform.runLater(() -> {
                        // Collapse the expanded details after a brief delay
                        task.setDetailsExpanded(false);
                        System.out.println("DEBUG: Collapsed expanded details due to outside click");
                        activeTasksTable.refresh();
                    });
                    break; // Only one can be expanded at a time
                }
            }
        });
    }
    
    private void setupRowDragAndDrop() {
        activeTasksTable.setRowFactory(tv -> {
            TableRow<TodoTask> row = new TableRow<>();
            
            row.setOnDragDetected(event -> {
                if (!row.isEmpty()) {
                    Integer index = row.getIndex();
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    db.setDragView(row.snapshot(null, null));
                    ClipboardContent cc = new ClipboardContent();
                    cc.putString(index.toString());
                    db.setContent(cc);
                    event.consume();
                }
            });
            
            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    if (row.getIndex() != Integer.parseInt(db.getString())) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                        event.consume();
                    }
                }
            });
            
            row.setOnDragEntered(event -> {
                if (event.getDragboard().hasString()) {
                    row.setStyle("-fx-background-color: #E3F2FD;");
                }
            });
            
            row.setOnDragExited(event -> {
                row.setStyle("");
            });
            
            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    TodoTask draggedItem = activeTasksTable.getItems().remove(draggedIndex);
                    
                    int dropIndex;
                    if (row.isEmpty()) {
                        dropIndex = activeTasksTable.getItems().size();
                    } else {
                        dropIndex = row.getIndex();
                    }
                    
                    activeTasksTable.getItems().add(dropIndex, draggedItem);
                    event.setDropCompleted(true);
                    activeTasksTable.getSelectionModel().select(dropIndex);
                    event.consume();
                }
            });
            
            return row;
        });
    }
    
    private class StatusCell extends TableCell<TodoTask, TaskStatus> {
        private final ComboBox<TaskStatus> comboBox;
        private final DatePicker datePicker;
        private final TextField timeField;
        private final HBox container;
        
        public StatusCell() {
            comboBox = new ComboBox<>(FXCollections.observableArrayList(TaskStatus.values()));
            comboBox.setConverter(new StringConverter<TaskStatus>() {
                @Override
                public String toString(TaskStatus status) {
                    if (status == TaskStatus.WAIT_UNTIL) {
                        // Check if we have a task and it has a wait until time
                        if (getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                            TodoTask task = getTableView().getItems().get(getIndex());
                            LocalDateTime waitUntil = task.getWaitUntil();
                            if (waitUntil != null) {
                                return "Wait until " + waitUntil.format(DATE_FORMAT);
                            }
                        }
                        return "Wait until...";
                    }
                    return status.getDisplay();
                }
                
                @Override
                public TaskStatus fromString(String string) {
                    return null;
                }
            });
            
            datePicker = new DatePicker();
            datePicker.setPrefWidth(100);
            
            timeField = new TextField();
            timeField.setPromptText("HH:mm");
            timeField.setPrefWidth(60);
            
            container = new HBox(5);
            container.setAlignment(Pos.CENTER);
            
            comboBox.setOnAction(e -> {
                TaskStatus selected = comboBox.getValue();
                if (selected != null) {
                    commitEdit(selected);
                    if (selected == TaskStatus.WAIT_UNTIL) {
                        // Set default time if none exists and capture task reference
                        TodoTask currentTask = null;
                        if (getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                            currentTask = getTableView().getItems().get(getIndex());
                            if (currentTask.getWaitUntil() == null) {
                                // Set default to today at 9:00 AM
                                currentTask.setWaitUntil(java.time.LocalDate.now().atTime(9, 0));
                            }
                        }
                        // Capture task reference for deferred execution
                        final TodoTask taskToEdit = currentTask;
                        if (taskToEdit != null) {
                            // Defer showing the dialog to avoid layout conflicts
                            javafx.application.Platform.runLater(() -> showDateTimePicker(taskToEdit));
                        }
                    } else {
                        // Clear wait until time for other statuses
                        if (getTableView() != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                            TodoTask task = getTableView().getItems().get(getIndex());
                            task.setWaitUntil(null);
                        }
                    }
                }
            });
        }
        
        private void showDateTimePicker(TodoTask task) {
            Dialog<LocalDateTime> dialog = new Dialog<>();
            dialog.setTitle("Select Date and Time");
            dialog.setHeaderText("Choose when to wait until:");
            
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20));
            
            // Get existing date/time if available
            LocalDateTime existingTime = task.getWaitUntil();
            
            DatePicker dialogDatePicker = new DatePicker();
            dialogDatePicker.setValue(existingTime != null ? existingTime.toLocalDate() : java.time.LocalDate.now());
            
            // Time controls
            HBox timeBox = new HBox(5);
            timeBox.setAlignment(Pos.CENTER_LEFT);
            
            // Set initial hour/minute/AM-PM from existing time
            int initialHour = 9;
            int initialMinute = 0;
            String initialAmPm = "AM";
            
            if (existingTime != null) {
                int hour24 = existingTime.getHour();
                initialMinute = existingTime.getMinute();
                
                if (hour24 == 0) {
                    initialHour = 12;
                    initialAmPm = "AM";
                } else if (hour24 < 12) {
                    initialHour = hour24;
                    initialAmPm = "AM";
                } else if (hour24 == 12) {
                    initialHour = 12;
                    initialAmPm = "PM";
                } else {
                    initialHour = hour24 - 12;
                    initialAmPm = "PM";
                }
            }
            
            Spinner<Integer> hourSpinner = new Spinner<>(1, 12, initialHour);
            hourSpinner.setPrefWidth(70);
            hourSpinner.setEditable(true);
            
            Label colonLabel = new Label(":");
            
            Spinner<Integer> minuteSpinner = new Spinner<>(0, 59, initialMinute);
            minuteSpinner.setPrefWidth(70);
            minuteSpinner.setEditable(true);
            // Format minutes to always show 2 digits
            minuteSpinner.getValueFactory().setConverter(new StringConverter<Integer>() {
                @Override
                public String toString(Integer value) {
                    return String.format("%02d", value);
                }
                
                @Override
                public Integer fromString(String string) {
                    try {
                        return Integer.valueOf(string);
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                }
            });
            
            ComboBox<String> amPmCombo = new ComboBox<>();
            amPmCombo.getItems().addAll("AM", "PM");
            amPmCombo.setValue(initialAmPm);
            amPmCombo.setPrefWidth(80);
            amPmCombo.setMaxWidth(80);
            
            timeBox.getChildren().addAll(hourSpinner, colonLabel, minuteSpinner, amPmCombo);
            
            grid.add(new Label("Date:"), 0, 0);
            grid.add(dialogDatePicker, 1, 0);
            grid.add(new Label("Time:"), 0, 1);
            grid.add(timeBox, 1, 1);
            
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    try {
                        int hour = hourSpinner.getValue();
                        int minute = minuteSpinner.getValue();
                        String amPm = amPmCombo.getValue();
                        
                        // Convert 12-hour to 24-hour format
                        if (amPm.equals("PM") && hour != 12) {
                            hour += 12;
                        } else if (amPm.equals("AM") && hour == 12) {
                            hour = 0;
                        }
                        
                        return dialogDatePicker.getValue().atTime(hour, minute);
                    } catch (Exception e) {
                        return null;
                    }
                }
                return null;
            });
            
            dialog.showAndWait().ifPresent(dateTime -> {
                task.setWaitUntil(dateTime);
                // Force refresh the entire table row to update the display
                getTableView().refresh();
            });
        }
        
        @Override
        protected void updateItem(TaskStatus status, boolean empty) {
            super.updateItem(status, empty);
            
            if (empty || status == null) {
                setGraphic(null);
                setStyle("");
            } else {
                // Temporarily disable action handler during programmatic update
                EventHandler<ActionEvent> originalHandler = comboBox.getOnAction();
                comboBox.setOnAction(null);
                comboBox.setValue(status);
                comboBox.setOnAction(originalHandler);
                
                // Apply color to the combo box based on status
                Color color = status.getColor();
                String colorStyle = String.format("-fx-background-color: %s; -fx-text-fill: white; -fx-font-weight: bold;",
                    toHexString(color));
                comboBox.setStyle(colorStyle);
                
                setGraphic(comboBox);
                setStyle(""); // Clear cell style
                setAlignment(Pos.CENTER);
            }
        }
        
        private String toHexString(Color color) {
            return String.format("#%02X%02X%02X",
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
        }
        
        @Override
        public void startEdit() {
            super.startEdit();
            if (getItem() != null) {
                comboBox.setValue(getItem());
                setGraphic(comboBox);
            }
        }
        
        @Override
        public void commitEdit(TaskStatus newValue) {
            super.commitEdit(newValue);
            TodoTask task = getTableView().getItems().get(getIndex());
            task.setStatus(newValue);
            updateItem(newValue, false);
        }
    }
    
    // Custom expandable cell for Details column
    private class ExpandableDetailsCell extends TableCell<TodoTask, String> {
        private TextField textField;
        private TextArea currentTextArea;
        
        public ExpandableDetailsCell() {
            setAlignment(Pos.CENTER);
            setStyle("-fx-border-width: 0; -fx-background-color: transparent;");
            setWrapText(true);
            
            // Add double-click handler to expand/collapse with event filtering
            addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getClickCount() == 2 && !isEmpty()) {
                    System.out.println("DEBUG: Double-click FILTER PRESSED detected on Details cell, index=" + getIndex());
                    event.consume();
                    toggleExpanded();
                }
                // Don't consume single clicks - let them through for cell selection
            });
            
            addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, event -> {
                if (event.getClickCount() == 2 && !isEmpty()) {
                    System.out.println("DEBUG: Double-click FILTER CLICKED detected on Details cell, index=" + getIndex());
                    event.consume();
                    // Additional handling in case pressed didn't work
                    javafx.application.Platform.runLater(() -> {
                        if (isEditing()) {
                            System.out.println("DEBUG: Cancelling edit mode in clicked handler");
                            cancelEdit();
                        }
                        // Only toggle if not already expanded (avoid double toggling)
                        if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                            TodoTask task = getTableView().getItems().get(getIndex());
                            if (task != null && !task.isDetailsExpanded()) {
                                toggleExpanded();
                            }
                        }
                    });
                }
                // Don't consume single clicks - let them through for cell selection
            });
        }
        
        private void toggleExpanded() {
            if (getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                TodoTask task = getTableView().getItems().get(getIndex());
                if (task != null) {
                    boolean newState = !task.isDetailsExpanded();
                    System.out.println("DEBUG: Toggling expansion from " + task.isDetailsExpanded() + " to " + newState);
                    task.setDetailsExpanded(newState);
                    updateDisplay();
                }
            }
        }
        
        @Override
        public void startEdit() {
            // Disable normal single-click editing entirely
            // Only allow editing through the expanded TextArea (double-click)
            System.out.println("DEBUG: startEdit() called but disabled for Details column");
            return;
        }
        
        @Override
        public void cancelEdit() {
            super.cancelEdit();
            updateDisplay();
        }
        
        @Override
        public void commitEdit(String newValue) {
            super.commitEdit(newValue);
            updateDisplay();
        }
        
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                setText(null);
                setGraphic(null);
                setStyle("-fx-border-width: 0; -fx-background-color: transparent;");
                setPrefHeight(-1);
            } else {
                setStyle("-fx-border-width: 0; -fx-background-color: transparent;");
                
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                        setGraphic(textField);
                        setText(null);
                    }
                } else {
                    updateDisplay();
                }
            }
        }
        
        private void updateDisplay() {
            if (getIndex() < 0 || getIndex() >= getTableView().getItems().size()) {
                return;
            }
            
            TodoTask task = getTableView().getItems().get(getIndex());
            boolean isExpanded = task != null && task.isDetailsExpanded();
            
            if (isExpanded) {
                // Show full text in a text area when expanded
                currentTextArea = new TextArea(getString());
                currentTextArea.setEditable(true);
                currentTextArea.setWrapText(true);
                currentTextArea.setPrefRowCount(4);
                currentTextArea.setPrefHeight(80);
                currentTextArea.setMaxHeight(80);
                currentTextArea.setStyle("-fx-border-width: 1; -fx-border-color: #cccccc; -fx-background-color: white; -fx-text-alignment: center;");
                
                // Save content when focus is lost (but don't auto-collapse - let global handler do that)
                currentTextArea.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        // Save the content directly to the task
                        if (task != null) {
                            String newText = currentTextArea.getText();
                            System.out.println("DEBUG: Saving text to task: '" + newText + "'");
                            task.setDescription(newText);
                            saveTodoData(); // Save to file
                        }
                    }
                });
                
                setGraphic(currentTextArea);
                setText(null);
                setPrefHeight(85);
                setStyle("-fx-border-width: 0; -fx-background-color: transparent;");
                
                // Focus the text area so user can immediately start typing
                currentTextArea.requestFocus();
            } else {
                // Show truncated text when collapsed
                currentTextArea = null; // Clear reference
                setText(getString());
                setGraphic(null);
                setPrefHeight(-1);
                setStyle("-fx-border-width: 0; -fx-background-color: transparent;");
            }
        }
        
        private void createTextField() {
            textField = new TextField(getString());
            textField.setStyle("-fx-border-width: 0; -fx-background-color: transparent; -fx-text-alignment: center;");
            textField.setAlignment(Pos.CENTER);
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            
            // Ensure centering works for new text input
            textField.textProperty().addListener((obs, oldText, newText) -> {
                textField.setAlignment(Pos.CENTER);
            });
            
            textField.setOnAction(event -> commitEdit(textField.getText()));
            textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    commitEdit(textField.getText());
                }
            });
        }
        
        private String getString() {
            return getItem() == null ? "" : getItem();
        }
        
        // Public method to save current TextArea content
        public void saveCurrentContent() {
            if (currentTextArea != null && getIndex() >= 0 && getIndex() < getTableView().getItems().size()) {
                TodoTask task = getTableView().getItems().get(getIndex());
                if (task != null) {
                    String newText = currentTextArea.getText();
                    System.out.println("DEBUG: Force saving content from TextArea: '" + newText + "'");
                    task.setDescription(newText);
                    saveTodoData();
                }
            }
        }
    }
    
    // Custom text cell that doesn't show borders and centers text
    private static class EditableTextCell extends TableCell<TodoTask, String> {
        private TextField textField;
        
        public EditableTextCell() {
            setAlignment(Pos.CENTER);
            setStyle("-fx-border-width: 0; -fx-background-color: transparent;");
        }
        
        @Override
        public void startEdit() {
            super.startEdit();
            if (textField == null) {
                createTextField();
            }
            setGraphic(textField);
            textField.selectAll();
            textField.requestFocus();
        }
        
        @Override
        public void cancelEdit() {
            super.cancelEdit();
            setText(getItem());
            setGraphic(null);
        }
        
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty) {
                setText(null);
                setGraphic(null);
                setStyle("-fx-border-width: 0; -fx-background-color: transparent;");
            } else {
                setStyle("-fx-border-width: 0; -fx-background-color: transparent;");
                
                if (isEditing()) {
                    if (textField != null) {
                        textField.setText(getString());
                    }
                    setGraphic(textField);
                } else {
                    setText(getString());
                    setGraphic(null);
                }
            }
        }
        
        private void createTextField() {
            textField = new TextField(getString());
            textField.setStyle("-fx-border-width: 0; -fx-background-color: transparent; -fx-text-alignment: center;");
            textField.setAlignment(Pos.CENTER);
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            
            // Ensure centering works for new text input
            textField.textProperty().addListener((obs, oldText, newText) -> {
                textField.setAlignment(Pos.CENTER);
            });
            
            textField.setOnAction(event -> commitEdit(textField.getText()));
            textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                if (!isNowFocused) {
                    commitEdit(textField.getText());
                }
            });
        }
        
        private String getString() {
            return getItem() == null ? "" : getItem();
        }
    }
    
    private VBox createCompletedTasksSection() {
        VBox section = new VBox(10);
        section.setStyle("-fx-background-color: white; -fx-border-color: #4CAF50; -fx-border-width: 2; -fx-border-radius: 5; -fx-background-radius: 5;");
        section.setPadding(new Insets(15));
        
        Label titleLabel = new Label("Completed Tasks");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #4CAF50;");
        
        // Create table for completed tasks
        completedTasksTable = new TableView<>(completedTasks);
        
        TableColumn<CompletedTask, String> nameCol = new TableColumn<>("Name");
        nameCol.setPrefWidth(200);
        nameCol.setMinWidth(100);
        nameCol.setResizable(true);
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        nameCol.setCellFactory(column -> {
            TableCell<CompletedTask, String> cell = new TableCell<CompletedTask, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        
        TableColumn<CompletedTask, String> idCol = new TableColumn<>("ID");
        idCol.setPrefWidth(100);
        idCol.setMinWidth(50);
        idCol.setResizable(true);
        idCol.setCellValueFactory(cellData -> cellData.getValue().idProperty());
        idCol.setCellFactory(column -> {
            TableCell<CompletedTask, String> cell = new TableCell<CompletedTask, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        
        TableColumn<CompletedTask, String> descCol = new TableColumn<>("Description");
        descCol.setPrefWidth(250);
        descCol.setMinWidth(150);
        descCol.setResizable(true);
        descCol.setCellValueFactory(cellData -> cellData.getValue().descriptionProperty());
        descCol.setCellFactory(column -> {
            TableCell<CompletedTask, String> cell = new TableCell<CompletedTask, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText(item);
                    }
                }
            };
            cell.setAlignment(Pos.CENTER);
            return cell;
        });
        
        TableColumn<CompletedTask, LocalDateTime> dateCol = new TableColumn<>("Completed Date");
        dateCol.setPrefWidth(200);
        dateCol.setMinWidth(120);
        dateCol.setResizable(true);
        dateCol.setCellValueFactory(cellData -> cellData.getValue().completedDateProperty());
        dateCol.setCellFactory(column -> new TableCell<CompletedTask, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(DATE_FORMAT));
                }
            }
        });
        
        completedTasksTable.getColumns().addAll(nameCol, idCol, descCol, dateCol);
        
        // Use CONSTRAINED_RESIZE_POLICY to make the table stretch to fill available width
        completedTasksTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Apply saved column widths
        applyColumnWidths(completedTasksTable, loadedCompletedColumnWidths);
        
        // Add listeners to save column widths when they change
        for (TableColumn<CompletedTask, ?> column : completedTasksTable.getColumns()) {
            column.widthProperty().addListener((obs, oldWidth, newWidth) -> saveTodoData());
        }
        
        // Add context menu for completed tasks
        setupCompletedTasksContextMenu();
        
        // Set table properties
        completedTasksTable.setPrefHeight(150);
        completedTasksTable.setMaxHeight(200);
        
        // Table will show its own scrollbar when needed
        section.getChildren().addAll(titleLabel, completedTasksTable);
        section.setMinHeight(100);
        section.setPrefHeight(Region.USE_COMPUTED_SIZE);
        section.setMaxHeight(250);
        
        return section;
    }
    
    private void addNewTask() {
        TodoTask newTask = new TodoTask("", "", "");
        
        // Add listeners to save data when task is modified using the helper method
        addTaskListeners(newTask);
        
        activeTasks.add(newTask);
        saveTodoData();
    }
    
    private void removeSelectedTask() {
        TodoTask selected = activeTasksTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            activeTasks.remove(selected);
            saveTodoData();
        }
    }
    
    private void moveToCompleted(TodoTask task) {
        activeTasks.remove(task);
        completedTasks.add(new CompletedTask(task));
        saveTodoData();
    }
    
    private void setupCompletedTasksContextMenu() {
        completedTasksTable.setRowFactory(tv -> {
            TableRow<CompletedTask> row = new TableRow<>();
            
            ContextMenu contextMenu = new ContextMenu();
            
            MenuItem restoreItem = new MenuItem("Restore");
            restoreItem.setOnAction(e -> {
                CompletedTask completedTask = row.getItem();
                if (completedTask != null) {
                    restoreTask(completedTask);
                }
            });
            
            MenuItem deleteItem = new MenuItem("Permanently Delete");
            deleteItem.setOnAction(e -> {
                CompletedTask completedTask = row.getItem();
                if (completedTask != null) {
                    permanentlyDeleteTask(completedTask);
                }
            });
            
            contextMenu.getItems().addAll(restoreItem, deleteItem);
            
            // Only show context menu for non-empty rows
            row.contextMenuProperty().bind(
                javafx.beans.binding.Bindings.when(row.emptyProperty())
                    .then((ContextMenu) null)
                    .otherwise(contextMenu)
            );
            
            return row;
        });
    }
    
    private void restoreTask(CompletedTask completedTask) {
        // Create new TodoTask from completed task
        TodoTask restoredTask = new TodoTask(
            completedTask.getName(),
            completedTask.getId(),
            completedTask.getDescription()
        );
        
        // Add listeners to save data when task is modified using the helper method
        addTaskListeners(restoredTask);
        
        // Remove from completed tasks and add to active tasks
        completedTasks.remove(completedTask);
        activeTasks.add(restoredTask);
        saveTodoData();
    }
    
    private void permanentlyDeleteTask(CompletedTask completedTask) {
        // Show confirmation dialog
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Deletion");
        confirmAlert.setHeaderText("Permanently Delete Task");
        confirmAlert.setContentText("Are you sure you want to permanently delete this task?\n\nTask: " + completedTask.getName());
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                completedTasks.remove(completedTask);
                saveTodoData();
            }
        });
    }
    
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    /**
     * Apply saved column widths to a table
     */
    private <T> void applyColumnWidths(TableView<T> table, Map<String, Double> columnWidths) {
        if (columnWidths == null || columnWidths.isEmpty()) {
            return;
        }
        
        for (TableColumn<T, ?> column : table.getColumns()) {
            String columnName = column.getText();
            if (columnName != null && columnWidths.containsKey(columnName)) {
                Double width = columnWidths.get(columnName);
                if (width != null && width > 0) {
                    column.setPrefWidth(width);
                }
            }
        }
    }
    
    /**
     * Get the observable list of active tasks for external access
     */
    public javafx.collections.ObservableList<TodoTask> getActiveTasks() {
        return activeTasks;
    }
}