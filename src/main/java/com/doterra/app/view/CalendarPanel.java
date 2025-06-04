package com.doterra.app.view;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Callback;
import javafx.util.Duration;
import java.io.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import com.doterra.app.util.AsyncFileOperations;

public class CalendarPanel extends BorderPane {
    
    private final TodoPanel todoPanel;
    private GridPane calendarGrid;
    private Label monthYearLabel;
    private YearMonth currentMonth;
    private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("d");
    private final DateTimeFormatter tooltipFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private Timeline dailyRefreshTimer;
    
    // Store notes for each date
    private Map<LocalDate, String> dateNotes = new HashMap<>();
    private static final String NOTES_FILE = "data/calendar_notes.dat";
    
    public CalendarPanel(TodoPanel todoPanel) {
        this.todoPanel = todoPanel;
        this.currentMonth = YearMonth.now();
        
        initializeCalendar();
        refreshCalendar();
        
        // Listen for changes in todo tasks to refresh calendar
        if (todoPanel != null) {
            // Listen for list changes (add/remove tasks)
            todoPanel.getActiveTasks().addListener((javafx.collections.ListChangeListener<TodoPanel.TodoTask>) change -> {
                while (change.next()) {
                    // Add listeners to new tasks
                    if (change.wasAdded()) {
                        for (TodoPanel.TodoTask task : change.getAddedSubList()) {
                            addTaskPropertyListeners(task);
                        }
                    }
                }
                refreshCalendar();
            });
            
            // Add listeners to existing tasks
            for (TodoPanel.TodoTask task : todoPanel.getActiveTasks()) {
                addTaskPropertyListeners(task);
            }
        }
        
        // Load saved notes asynchronously
        loadNotesAsync();
        
        // Set up daily timer to refresh calendar so ready tasks move with the current date
        setupDailyRefreshTimer();
    }
    
    /**
     * Add property listeners to a task to refresh calendar when relevant properties change
     */
    private void addTaskPropertyListeners(TodoPanel.TodoTask task) {
        // Listen for changes to properties that affect calendar display
        task.waitUntilProperty().addListener((obs, oldValue, newValue) -> refreshCalendar());
        task.statusProperty().addListener((obs, oldValue, newValue) -> refreshCalendar());
        task.dateCreatedProperty().addListener((obs, oldValue, newValue) -> refreshCalendar());
    }
    
    /**
     * Set up a daily timer to refresh the calendar so ready tasks move to the current date
     */
    private void setupDailyRefreshTimer() {
        // Calculate seconds until midnight
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime midnight = now.toLocalDate().plusDays(1).atStartOfDay();
        long secondsUntilMidnight = ChronoUnit.SECONDS.between(now, midnight);
        
        // Create timer that fires at midnight, then every 24 hours
        dailyRefreshTimer = new Timeline(
            new KeyFrame(Duration.seconds(secondsUntilMidnight), e -> refreshCalendar()),
            new KeyFrame(Duration.hours(24), e -> refreshCalendar())
        );
        dailyRefreshTimer.setCycleCount(Timeline.INDEFINITE);
        dailyRefreshTimer.play();
    }
    
    private void initializeCalendar() {
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f5f5f5;");
        
        // Header with navigation
        HBox header = createHeader();
        setTop(header);
        
        // Calendar content
        VBox calendarContent = createCalendarContent();
        setCenter(calendarContent);
        
        // Legend
        VBox legend = createLegend();
        setBottom(legend);
    }
    
    private HBox createHeader() {
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));
        
        Button prevButton = new Button("◀");
        prevButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5;");
        prevButton.setOnAction(e -> {
            currentMonth = currentMonth.minusMonths(1);
            refreshCalendar();
        });
        
        monthYearLabel = new Label();
        monthYearLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #333;");
        monthYearLabel.setMinWidth(200);
        monthYearLabel.setAlignment(Pos.CENTER);
        
        Button nextButton = new Button("▶");
        nextButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 5;");
        nextButton.setOnAction(e -> {
            currentMonth = currentMonth.plusMonths(1);
            refreshCalendar();
        });
        
        Button todayButton = new Button("Today");
        todayButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 8 15; -fx-background-radius: 5;");
        todayButton.setOnAction(e -> {
            currentMonth = YearMonth.now();
            refreshCalendar();
        });
        
        header.getChildren().addAll(prevButton, monthYearLabel, nextButton, todayButton);
        return header;
    }
    
    private VBox createCalendarContent() {
        VBox content = new VBox(10);
        content.setStyle("-fx-background-color: white; -fx-border-color: #ddd; -fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        content.setPadding(new Insets(20));
        
        // Day headers
        HBox dayHeaders = createDayHeaders();
        
        // Calendar grid
        calendarGrid = new GridPane();
        calendarGrid.setHgap(2);
        calendarGrid.setVgap(2);
        calendarGrid.setAlignment(Pos.CENTER);
        
        content.getChildren().addAll(dayHeaders, calendarGrid);
        return content;
    }
    
    private HBox createDayHeaders() {
        HBox dayHeaders = new HBox();
        dayHeaders.setAlignment(Pos.CENTER);
        dayHeaders.setPadding(new Insets(0, 0, 10, 0));
        
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        
        for (String dayName : dayNames) {
            Label dayLabel = new Label(dayName.substring(0, 3));
            dayLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #666; -fx-alignment: center;");
            dayLabel.setPrefWidth(80);
            dayLabel.setPrefHeight(30);
            dayLabel.setAlignment(Pos.CENTER);
            dayHeaders.getChildren().add(dayLabel);
        }
        
        return dayHeaders;
    }
    
    private VBox createLegend() {
        VBox legend = new VBox(10);
        legend.setPadding(new Insets(20, 0, 0, 0));
        legend.setAlignment(Pos.CENTER);
        
        Label legendTitle = new Label("Legend");
        legendTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #333;");
        
        HBox legendItems = new HBox(30);
        legendItems.setAlignment(Pos.CENTER);
        
        // Due today/overdue indicator
        HBox dueTodayItem = createLegendItem("#f44336", "Tasks Due/Overdue");
        
        // Ready tasks indicator
        HBox readyItem = createLegendItem("#f44336", "Ready Tasks");
        
        // Week old indicator  
        HBox weekOldItem = createLegendItem("#FF9800", "Week Old / Future Week Milestones");
        
        // Future due dates
        HBox futureDueItem = createLegendItem("#2196F3", "Future Due Dates");
        
        legendItems.getChildren().addAll(dueTodayItem, readyItem, weekOldItem, futureDueItem);
        legend.getChildren().addAll(legendTitle, legendItems);
        
        return legend;
    }
    
    private HBox createLegendItem(String color, String description) {
        HBox item = new HBox(5);
        item.setAlignment(Pos.CENTER);
        
        Label colorBox = new Label();
        colorBox.setStyle("-fx-background-color: " + color + "; -fx-min-width: 15; -fx-min-height: 15; -fx-background-radius: 3;");
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #666;");
        
        item.getChildren().addAll(colorBox, descLabel);
        return item;
    }
    
    private void refreshCalendar() {
        // Update month/year label
        monthYearLabel.setText(currentMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        
        // Clear existing calendar
        calendarGrid.getChildren().clear();
        
        // Get task data
        Map<LocalDate, TaskDayInfo> tasksByDate = getTasksByDate();
        
        // Get first day of month and calculate starting position
        LocalDate firstOfMonth = currentMonth.atDay(1);
        int startDayOfWeek = firstOfMonth.getDayOfWeek().getValue() % 7; // Convert to 0=Sunday format
        
        // Fill calendar grid
        LocalDate currentDate = firstOfMonth.minusDays(startDayOfWeek);
        
        for (int week = 0; week < 6; week++) {
            for (int day = 0; day < 7; day++) {
                StackPane dayCell = createDayCell(currentDate, tasksByDate.get(currentDate));
                calendarGrid.add(dayCell, day, week);
                currentDate = currentDate.plusDays(1);
            }
            
            // Stop if we've gone past the current month and filled at least 4 weeks
            if (week >= 3 && currentDate.getMonth() != currentMonth.getMonth()) {
                break;
            }
        }
    }
    
    private StackPane createDayCell(LocalDate date, TaskDayInfo taskInfo) {
        StackPane cell = new StackPane();
        cell.setPrefSize(80, 70);
        cell.setMinSize(80, 70);
        cell.setMaxSize(80, 70);
        
        boolean isCurrentMonth = date.getMonth() == currentMonth.getMonth();
        boolean isToday = date.equals(LocalDate.now());
        
        // Base styling
        String baseStyle = "-fx-border-color: #e0e0e0; -fx-border-width: 1; -fx-background-radius: 5; -fx-border-radius: 5;";
        
        if (isToday) {
            baseStyle += " -fx-border-color: #2196F3; -fx-border-width: 2;";
        }
        
        if (!isCurrentMonth) {
            baseStyle += " -fx-background-color: #f9f9f9; -fx-opacity: 0.5;";
        } else {
            baseStyle += " -fx-background-color: white;";
        }
        
        cell.setStyle(baseStyle);
        
        VBox content = new VBox(2);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPadding(new Insets(5));
        
        // Day number
        Label dayLabel = new Label(date.format(dayFormatter));
        dayLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: " + (isToday ? "bold" : "normal") + 
                         "; -fx-text-fill: " + (isCurrentMonth ? "#333" : "#999") + ";");
        content.getChildren().add(dayLabel);
        
        // Task indicators
        if (taskInfo != null && taskInfo.getTotalTasks() > 0) {
            HBox indicators = new HBox(2);
            indicators.setAlignment(Pos.CENTER);
            
            // Show task counts with appropriate colors
            if (taskInfo.getDueOverdueTasks() > 0) {
                Label dueLabel = new Label(String.valueOf(taskInfo.getDueOverdueTasks()));
                dueLabel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 9px; " +
                               "-fx-font-weight: bold; -fx-padding: 0 3 0 4; -fx-background-radius: 7; -fx-min-width: 14;");
                indicators.getChildren().add(dueLabel);
            }
            
            if (taskInfo.getReadyTasks() > 0) {
                Label readyLabel = new Label(String.valueOf(taskInfo.getReadyTasks()));
                readyLabel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-font-size: 9px; " +
                                  "-fx-font-weight: bold; -fx-padding: 0 3 0 4; -fx-background-radius: 7; -fx-min-width: 14;");
                indicators.getChildren().add(readyLabel);
            }
            
            if (taskInfo.getWeekOldTasks() > 0) {
                Label weekOldLabel = new Label(String.valueOf(taskInfo.getWeekOldTasks()));
                weekOldLabel.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 9px; " +
                                    "-fx-font-weight: bold; -fx-padding: 0 3 0 4; -fx-background-radius: 7; -fx-min-width: 14;");
                indicators.getChildren().add(weekOldLabel);
            }
            
            if (taskInfo.getFutureDueTasks() > 0) {
                Label futureLabel = new Label(String.valueOf(taskInfo.getFutureDueTasks()));
                futureLabel.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-size: 9px; " +
                                   "-fx-font-weight: bold; -fx-padding: 0 3 0 4; -fx-background-radius: 7; -fx-min-width: 14;");
                indicators.getChildren().add(futureLabel);
            }
            
            if (taskInfo.getFutureWeekMilestoneTasks() > 0) {
                Label milestoneLabel = new Label(String.valueOf(taskInfo.getFutureWeekMilestoneTasks()));
                milestoneLabel.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-size: 9px; " +
                                   "-fx-font-weight: bold; -fx-padding: 0 3 0 4; -fx-background-radius: 7; -fx-min-width: 14;");
                indicators.getChildren().add(milestoneLabel);
            }
            
            content.getChildren().add(indicators);
            
            // Add tooltip with task details
            String tooltipText = buildTooltipText(date, taskInfo);
            if (!tooltipText.isEmpty()) {
                Tooltip tooltip = new Tooltip(tooltipText);
                tooltip.setStyle("-fx-font-size: 12px;");
                Tooltip.install(cell, tooltip);
            }
        }
        
        // Add note icon if there's a note for this date
        if (dateNotes.containsKey(date) && !dateNotes.get(date).trim().isEmpty()) {
            Label noteIcon = new Label("📝");
            noteIcon.setStyle("-fx-font-size: 10px; -fx-text-fill: #666;");
            content.getChildren().add(noteIcon);
        }
        
        cell.getChildren().add(content);
        
        // Add right-click context menu functionality
        cell.setOnContextMenuRequested(e -> {
            ContextMenu contextMenu = new ContextMenu();
            boolean hasNote = dateNotes.containsKey(date) && !dateNotes.get(date).trim().isEmpty();
            
            MenuItem noteItem = new MenuItem(hasNote ? "Edit Note" : "Create Note");
            noteItem.setOnAction(ev -> showNoteDialog(date));
            contextMenu.getItems().add(noteItem);
            
            if (hasNote) {
                MenuItem deleteNoteItem = new MenuItem("Delete Note");
                deleteNoteItem.setOnAction(ev -> {
                    dateNotes.remove(date);
                    saveNotes();
                    refreshCalendar();
                });
                contextMenu.getItems().add(deleteNoteItem);
            }
            
            contextMenu.show(cell, e.getScreenX(), e.getScreenY());
        });
        
        // Add click handler to show task details and/or notes
        boolean hasTaskInfo = taskInfo != null && taskInfo.getTotalTasks() > 0;
        boolean hasNote = dateNotes.containsKey(date) && !dateNotes.get(date).trim().isEmpty();
        
        if (hasTaskInfo || hasNote) {
            cell.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY) { // Only on left click
                    showDateDetailsDialog(date, taskInfo, hasNote);
                }
            });
            cell.setStyle(cell.getStyle() + " -fx-cursor: hand;");
        }
        
        return cell;
    }
    
    private Map<LocalDate, TaskDayInfo> getTasksByDate() {
        Map<LocalDate, TaskDayInfo> tasksByDate = new HashMap<>();
        
        if (todoPanel == null) {
            return tasksByDate;
        }
        
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusWeeks(1);
        
        for (TodoPanel.TodoTask task : todoPanel.getActiveTasks()) {
            // Check for due dates (wait until dates)
            if (task.getWaitUntil() != null) {
                LocalDate dueDate = task.getWaitUntil().toLocalDate();
                boolean isDueOverdue = dueDate.isBefore(today) || dueDate.equals(today);
                boolean isFutureDue = !isDueOverdue;
                
                TaskDayInfo dayInfo = tasksByDate.computeIfAbsent(dueDate, k -> new TaskDayInfo());
                dayInfo.addTask(task, isDueOverdue, false, isFutureDue, false, false);
            }
            
            // Check for week-old tasks based on creation date
            if (task.getDateCreated() != null) {
                LocalDate createdDate = task.getDateCreated().toLocalDate();
                boolean isWeekOld = createdDate.isBefore(weekAgo) || createdDate.equals(weekAgo);
                
                if (isWeekOld) {
                    TaskDayInfo dayInfo = tasksByDate.computeIfAbsent(createdDate, k -> new TaskDayInfo());
                    dayInfo.addTask(task, false, true, false, false, false);
                }
            }
            
            // Special handling for "Waiting for response..." tasks
            if (task.getStatus() == TodoPanel.TaskStatus.WAITING_RESPONSE && task.getDateCreated() != null) {
                // Calculate when this task will be 1 week old
                LocalDate oneWeekDate = task.getDateCreated().toLocalDate().plusWeeks(1);
                
                // If it's not already a week old, show it on the calendar for when it will be
                if (oneWeekDate.isAfter(today)) {
                    TaskDayInfo dayInfo = tasksByDate.computeIfAbsent(oneWeekDate, k -> new TaskDayInfo());
                    dayInfo.addTask(task, false, false, false, true, false);
                }
            }
            
            // Show Ready tasks on today's date
            if (task.getStatus() == TodoPanel.TaskStatus.READY) {
                TaskDayInfo dayInfo = tasksByDate.computeIfAbsent(today, k -> new TaskDayInfo());
                dayInfo.addTask(task, false, false, false, false, true);
            }
        }
        
        return tasksByDate;
    }
    
    private String buildTooltipText(LocalDate date, TaskDayInfo taskInfo) {
        StringBuilder tooltip = new StringBuilder();
        tooltip.append(date.format(tooltipFormatter)).append("\n\n");
        
        if (taskInfo.getDueOverdueTasks() > 0) {
            tooltip.append("Due/Overdue: ").append(taskInfo.getDueOverdueTasks()).append(" tasks\n");
        }
        
        if (taskInfo.getReadyTasks() > 0) {
            tooltip.append("Ready: ").append(taskInfo.getReadyTasks()).append(" tasks\n");
        }
        
        if (taskInfo.getWeekOldTasks() > 0) {
            tooltip.append("Week+ Old: ").append(taskInfo.getWeekOldTasks()).append(" tasks\n");
        }
        
        if (taskInfo.getFutureDueTasks() > 0) {
            tooltip.append("Future Due: ").append(taskInfo.getFutureDueTasks()).append(" tasks\n");
        }
        
        // Show first few task names
        List<TodoPanel.TodoTask> allTasks = taskInfo.getAllTasks();
        if (!allTasks.isEmpty()) {
            tooltip.append("\nTasks:\n");
            int maxToShow = Math.min(5, allTasks.size());
            for (int i = 0; i < maxToShow; i++) {
                TodoPanel.TodoTask task = allTasks.get(i);
                String taskName = task.getName();
                if (taskName == null || taskName.trim().isEmpty()) {
                    taskName = "Untitled Task";
                }
                tooltip.append("• ").append(taskName);
                if (task.getId() != null && !task.getId().trim().isEmpty()) {
                    tooltip.append(" (").append(task.getId()).append(")");
                }
                // Add status info for waiting tasks
                if (task.getStatus() == TodoPanel.TaskStatus.WAITING_RESPONSE) {
                    LocalDate weekOldDate = task.getDateCreated().toLocalDate().plusWeeks(1);
                    if (weekOldDate.equals(date)) {
                        tooltip.append(" - will be 1 week old");
                    }
                }
                tooltip.append("\n");
            }
            
            if (allTasks.size() > maxToShow) {
                tooltip.append("... and ").append(allTasks.size() - maxToShow).append(" more");
            }
        }
        
        return tooltip.toString().trim();
    }
    
    private void showTaskDetailsForDate(LocalDate date, TaskDayInfo taskInfo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Tasks for " + date.format(tooltipFormatter));
        alert.setHeaderText("Task Details");
        alert.setContentText(buildTooltipText(date, taskInfo));
        alert.setResizable(true);
        alert.showAndWait();
    }
    
    /**
     * Show a dialog with both task details and notes for the given date
     */
    private void showDateDetailsDialog(LocalDate date, TaskDayInfo taskInfo, boolean hasNote) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Details for " + date.format(tooltipFormatter));
        alert.setHeaderText("Date Information");
        
        StringBuilder content = new StringBuilder();
        
        // Add task information if present
        if (taskInfo != null && taskInfo.getTotalTasks() > 0) {
            content.append("TASKS:\n");
            content.append(buildTooltipText(date, taskInfo));
        }
        
        // Add note information if present
        if (hasNote) {
            String note = dateNotes.get(date);
            if (content.length() > 0) {
                content.append("\n\n");
            }
            content.append("NOTE:\n");
            content.append(note);
        }
        
        alert.setContentText(content.toString());
        alert.setResizable(true);
        alert.showAndWait();
    }
    
    // Helper class to store task information for each day
    private static class TaskDayInfo {
        private final List<TodoPanel.TodoTask> allTasks = new ArrayList<>();
        private int dueOverdueTasks = 0;
        private int weekOldTasks = 0;
        private int futureDueTasks = 0;
        private int futureWeekMilestoneTasks = 0; // Separate counter for "waiting for response" week milestones
        private int readyTasks = 0; // Counter for ready tasks
        
        public void addTask(TodoPanel.TodoTask task, boolean isDueOverdue, boolean isWeekOld, boolean isFutureDue, boolean isFutureWeekMilestone, boolean isReady) {
            allTasks.add(task);
            
            if (isDueOverdue) {
                dueOverdueTasks++;
            }
            if (isWeekOld) {
                weekOldTasks++;
            }
            if (isFutureDue) {
                futureDueTasks++;
            }
            if (isFutureWeekMilestone) {
                futureWeekMilestoneTasks++;
            }
            if (isReady) {
                readyTasks++;
            }
        }
        
        public int getTotalTasks() {
            return allTasks.size();
        }
        
        public int getDueOverdueTasks() {
            return dueOverdueTasks;
        }
        
        public int getWeekOldTasks() {
            return weekOldTasks;
        }
        
        public int getFutureDueTasks() {
            return futureDueTasks;
        }
        
        public int getFutureWeekMilestoneTasks() {
            return futureWeekMilestoneTasks;
        }
        
        public int getReadyTasks() {
            return readyTasks;
        }
        
        public List<TodoPanel.TodoTask> getAllTasks() {
            return allTasks;
        }
    }
    
    // Method to access active tasks for external classes (needed for listener)
    public javafx.collections.ObservableList<TodoPanel.TodoTask> getActiveTasks() {
        return todoPanel != null ? todoPanel.getActiveTasks() : javafx.collections.FXCollections.observableArrayList();
    }
    
    /**
     * Show dialog to create or edit a note for the given date
     */
    private void showNoteDialog(LocalDate date) {
        String existingNote = dateNotes.getOrDefault(date, "");
        
        TextInputDialog dialog = new TextInputDialog(existingNote);
        dialog.setTitle("Date Note");
        dialog.setHeaderText("Note for " + date.format(tooltipFormatter));
        dialog.setContentText("Enter your note:");
        
        // Make the text input area larger
        TextArea textArea = new TextArea(existingNote);
        textArea.setPrefRowCount(5);
        textArea.setPrefColumnCount(40);
        textArea.setWrapText(true);
        dialog.getDialogPane().setExpandableContent(textArea);
        dialog.getDialogPane().setExpanded(true);
        
        dialog.showAndWait().ifPresent(note -> {
            if (note.trim().isEmpty()) {
                // Remove note if empty
                dateNotes.remove(date);
            } else {
                // Save note
                dateNotes.put(date, note.trim());
            }
            saveNotes();
            refreshCalendar(); // Refresh to show/hide note icon
        });
    }
    
    /**
     * Load notes from file asynchronously
     */
    private void loadNotesAsync() {
        AsyncFileOperations.loadAsync(
            () -> {
                // This runs on background thread
                File file = new File(NOTES_FILE);
                if (file.exists()) {
                    try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                        @SuppressWarnings("unchecked")
                        Map<LocalDate, String> loadedNotes = (Map<LocalDate, String>) ois.readObject();
                        return loadedNotes;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return new HashMap<LocalDate, String>();
                    }
                }
                return new HashMap<LocalDate, String>();
            },
            (loadedNotes) -> {
                // This runs on JavaFX thread
                dateNotes = loadedNotes;
                refreshCalendar(); // Refresh calendar to show loaded notes
            },
            (error) -> {
                // This runs on JavaFX thread
                System.err.println("Error loading calendar notes: " + error.getMessage());
                error.printStackTrace();
                dateNotes = new HashMap<>();
            }
        );
    }
    
    /**
     * Save notes to file with debouncing
     */
    private void saveNotes() {
        AsyncFileOperations.debouncedSave("calendar-notes", 500, () -> {
            try {
                // Ensure parent directory exists
                File file = new File(NOTES_FILE);
                File parentDir = file.getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(NOTES_FILE))) {
                    oos.writeObject(dateNotes);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}