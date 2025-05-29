package com.doterra.app.util;

import javafx.scene.paint.Color;

public class ColorUtil {
    
    /**
     * Converts a JavaFX Color to a hex string for CSS.
     * 
     * @param color The color to convert
     * @return A hexadecimal string representation of the color (e.g., "#FF5733")
     */
    public static String colorToHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
    
    /**
     * Converts a hex string to a JavaFX Color.
     * 
     * @param hexColor The hex string to convert (e.g., "#FF5733")
     * @return A JavaFX Color object
     */
    public static Color hexToColor(String hexColor) {
        return Color.web(hexColor);
    }
}