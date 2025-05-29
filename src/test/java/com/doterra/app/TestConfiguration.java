package com.doterra.app;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

public class TestConfiguration {
    
    private static boolean javaFxInitialized = false;
    
    @BeforeAll
    public static void initializeJavaFx() {
        if (!javaFxInitialized) {
            Platform.startup(() -> {});
            javaFxInitialized = true;
        }
    }
}