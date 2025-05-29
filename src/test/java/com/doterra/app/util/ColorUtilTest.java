package com.doterra.app.util;

import com.doterra.app.TestConfiguration;
import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class ColorUtilTest extends TestConfiguration {
    
    @Test
    @DisplayName("colorToHex should convert red color correctly")
    void testColorToHexRed() {
        // Given
        Color red = Color.RED;
        
        // When
        String hex = ColorUtil.colorToHex(red);
        
        // Then
        assertEquals("#FF0000", hex);
    }
    
    @Test
    @DisplayName("colorToHex should convert green color correctly")
    void testColorToHexGreen() {
        // Given
        Color green = Color.GREEN;
        
        // When
        String hex = ColorUtil.colorToHex(green);
        
        // Then
        assertEquals("#008000", hex);
    }
    
    @Test
    @DisplayName("colorToHex should convert blue color correctly")
    void testColorToHexBlue() {
        // Given
        Color blue = Color.BLUE;
        
        // When
        String hex = ColorUtil.colorToHex(blue);
        
        // Then
        assertEquals("#0000FF", hex);
    }
    
    @Test
    @DisplayName("colorToHex should convert black color correctly")
    void testColorToHexBlack() {
        // Given
        Color black = Color.BLACK;
        
        // When
        String hex = ColorUtil.colorToHex(black);
        
        // Then
        assertEquals("#000000", hex);
    }
    
    @Test
    @DisplayName("colorToHex should convert white color correctly")
    void testColorToHexWhite() {
        // Given
        Color white = Color.WHITE;
        
        // When
        String hex = ColorUtil.colorToHex(white);
        
        // Then
        assertEquals("#FFFFFF", hex);
    }
    
    @Test
    @DisplayName("colorToHex should convert custom color correctly")
    void testColorToHexCustom() {
        // Given
        Color custom = Color.rgb(255, 87, 51); // Orange-ish color
        
        // When
        String hex = ColorUtil.colorToHex(custom);
        
        // Then
        assertEquals("#FF5733", hex);
    }
    
    @Test
    @DisplayName("hexToColor should convert hex string to red color")
    void testHexToColorRed() {
        // Given
        String hexRed = "#FF0000";
        
        // When
        Color color = ColorUtil.hexToColor(hexRed);
        
        // Then
        assertEquals(1.0, color.getRed(), 0.001);
        assertEquals(0.0, color.getGreen(), 0.001);
        assertEquals(0.0, color.getBlue(), 0.001);
    }
    
    @Test
    @DisplayName("hexToColor should convert hex string to custom color")
    void testHexToColorCustom() {
        // Given
        String hexCustom = "#FF5733";
        
        // When
        Color color = ColorUtil.hexToColor(hexCustom);
        
        // Then
        assertEquals(1.0, color.getRed(), 0.001);
        assertEquals(0.341, color.getGreen(), 0.001);
        assertEquals(0.2, color.getBlue(), 0.001);
    }
    
    @Test
    @DisplayName("colorToHex and hexToColor should be inverse operations")
    void testRoundTrip() {
        // Given
        Color original = Color.rgb(123, 45, 67);
        
        // When
        String hex = ColorUtil.colorToHex(original);
        Color restored = ColorUtil.hexToColor(hex);
        
        // Then
        assertEquals(original.getRed(), restored.getRed(), 0.01);
        assertEquals(original.getGreen(), restored.getGreen(), 0.01);
        assertEquals(original.getBlue(), restored.getBlue(), 0.01);
    }
    
    @Test
    @DisplayName("hexToColor should handle lowercase hex strings")
    void testHexToColorLowercase() {
        // Given
        String hexLower = "#ff5733";
        String hexUpper = "#FF5733";
        
        // When
        Color colorLower = ColorUtil.hexToColor(hexLower);
        Color colorUpper = ColorUtil.hexToColor(hexUpper);
        
        // Then
        assertEquals(colorUpper.getRed(), colorLower.getRed(), 0.001);
        assertEquals(colorUpper.getGreen(), colorLower.getGreen(), 0.001);
        assertEquals(colorUpper.getBlue(), colorLower.getBlue(), 0.001);
    }
    
    @Test
    @DisplayName("hexToColor should handle 3-digit hex codes")
    void testHexToColorShortFormat() {
        // Given
        String hex3 = "#F00"; // Short for #FF0000
        
        // When
        Color color = ColorUtil.hexToColor(hex3);
        
        // Then
        assertEquals(1.0, color.getRed(), 0.001);
        assertEquals(0.0, color.getGreen(), 0.001);
        assertEquals(0.0, color.getBlue(), 0.001);
    }
}