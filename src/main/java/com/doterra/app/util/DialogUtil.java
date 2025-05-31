package com.doterra.app.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Utility class for configuring JavaFX dialogs to be independent and always on top.
 * This ensures that dialogs don't require the parent window to stay open and remain
 * visible above all other windows.
 */
public class DialogUtil {
    
    /**
     * Configures a Dialog to be independent and always on top.
     * 
     * @param dialog The dialog to configure
     */
    public static void configureDialog(Dialog<?> dialog) {
        // Make the dialog independent of parent window
        dialog.initModality(Modality.NONE);
        
        // Get the stage and configure it to stay on top
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        if (stage != null) {
            stage.setAlwaysOnTop(true);
        } else {
            // If stage is not available yet, set it when the dialog is shown
            dialog.setOnShown(e -> {
                Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
                if (dialogStage != null) {
                    dialogStage.setAlwaysOnTop(true);
                }
            });
        }
    }
    
    /**
     * Configures a TextInputDialog to be independent and always on top.
     * 
     * @param dialog The TextInputDialog to configure
     */
    public static void configureDialog(TextInputDialog dialog) {
        // Make the dialog independent of parent window
        dialog.initModality(Modality.NONE);
        
        // Get the stage and configure it to stay on top
        Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
        if (stage != null) {
            stage.setAlwaysOnTop(true);
        } else {
            // If stage is not available yet, set it when the dialog is shown
            dialog.setOnShown(e -> {
                Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
                if (dialogStage != null) {
                    dialogStage.setAlwaysOnTop(true);
                }
            });
        }
    }
    
    /**
     * Configures an Alert to be independent and always on top.
     * 
     * @param alert The Alert to configure
     */
    public static void configureDialog(Alert alert) {
        // Make the dialog independent of parent window
        alert.initModality(Modality.NONE);
        
        // Get the stage and configure it to stay on top
        Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
        if (stage != null) {
            stage.setAlwaysOnTop(true);
        } else {
            // If stage is not available yet, set it when the dialog is shown
            alert.setOnShown(e -> {
                Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
                if (dialogStage != null) {
                    dialogStage.setAlwaysOnTop(true);
                }
            });
        }
    }
}