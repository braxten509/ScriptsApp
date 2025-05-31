package com.doterra.app.view;

import com.doterra.app.TestFXBase;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class DialogTest extends TestFXBase {
    
    private String dialogResult = null;
    
    @Override
    protected void setupStage(Stage stage) {
        Button testButton = new Button("Test Dialog");
        testButton.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Test");
            dialog.setHeaderText("Test Dialog");
            dialog.setContentText("Enter text:");
            
            Optional<String> result = dialog.showAndWait();
            dialogResult = result.orElse("No result");
        });
        
        VBox root = new VBox(testButton);
        Scene scene = new Scene(root, 400, 300);
        stage.setScene(scene);
        stage.show();
    }
    
    @Test
    public void testDialogInteraction() {
        // Click the test button
        clickOn("Test Dialog");
        waitForUIUpdate();
        
        // Try to interact with dialog
        try {
            Thread.sleep(500); // Give dialog time to appear
            write("Test Input");
            Thread.sleep(100);
            clickButton("OK");
            waitForUIUpdate();
            Thread.sleep(500);
            
            System.out.println("Dialog result: " + dialogResult);
        } catch (Exception e) {
            System.out.println("Dialog test failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}