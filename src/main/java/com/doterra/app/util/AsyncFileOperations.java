package com.doterra.app.util;

import javafx.application.Platform;
import java.util.concurrent.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Utility class for handling asynchronous file operations to prevent UI freezing
 */
public class AsyncFileOperations {
    
    // Single thread executor for file operations to ensure sequential writes
    private static final ExecutorService fileExecutor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("File-IO-Thread");
        return t;
    });
    
    // Map to track debounced save operations
    private static final ConcurrentHashMap<String, ScheduledFuture<?>> debouncedSaves = new ConcurrentHashMap<>();
    
    // Scheduled executor for debouncing
    private static final ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r);
        t.setDaemon(true);
        t.setName("Debounce-Thread");
        return t;
    });
    
    /**
     * Execute a file operation asynchronously
     * @param operation The operation to execute
     * @param onSuccess Optional callback for successful completion (runs on JavaFX thread)
     * @param onError Optional callback for errors (runs on JavaFX thread)
     */
    public static void executeAsync(Runnable operation, Runnable onSuccess, java.util.function.Consumer<Exception> onError) {
        fileExecutor.submit(() -> {
            try {
                operation.run();
                if (onSuccess != null) {
                    Platform.runLater(onSuccess);
                }
            } catch (Exception e) {
                if (onError != null) {
                    Platform.runLater(() -> onError.accept(e));
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Execute a file operation asynchronously without callbacks
     * @param operation The operation to execute
     */
    public static void executeAsync(Runnable operation) {
        executeAsync(operation, null, null);
    }
    
    /**
     * Execute a file load operation asynchronously and return result via callback
     * @param loader The loader function
     * @param onComplete Callback with the loaded data (runs on JavaFX thread)
     * @param onError Optional error handler
     */
    public static <T> void loadAsync(Callable<T> loader, java.util.function.Consumer<T> onComplete, java.util.function.Consumer<Exception> onError) {
        fileExecutor.submit(() -> {
            try {
                T result = loader.call();
                Platform.runLater(() -> onComplete.accept(result));
            } catch (Exception e) {
                if (onError != null) {
                    Platform.runLater(() -> onError.accept(e));
                } else {
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * Execute a save operation with debouncing to prevent rapid repeated saves
     * @param key Unique key for this save operation
     * @param delayMs Delay in milliseconds before executing
     * @param operation The save operation to execute
     */
    public static void debouncedSave(String key, long delayMs, Runnable operation) {
        // Cancel any existing scheduled save for this key
        ScheduledFuture<?> existing = debouncedSaves.get(key);
        if (existing != null && !existing.isDone()) {
            existing.cancel(false);
        }
        
        // Schedule the new save
        ScheduledFuture<?> future = scheduledExecutor.schedule(() -> {
            debouncedSaves.remove(key);
            executeAsync(operation);
        }, delayMs, TimeUnit.MILLISECONDS);
        
        debouncedSaves.put(key, future);
    }
    
    /**
     * Shutdown the executor service (call on application exit)
     */
    public static void shutdown() {
        fileExecutor.shutdown();
        scheduledExecutor.shutdown();
        try {
            if (!fileExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                fileExecutor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            fileExecutor.shutdownNow();
            scheduledExecutor.shutdownNow();
        }
    }
}