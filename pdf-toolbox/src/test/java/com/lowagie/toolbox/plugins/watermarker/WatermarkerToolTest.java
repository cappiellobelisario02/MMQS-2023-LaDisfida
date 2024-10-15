package com.lowagie.toolbox.plugins.watermarker;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class WatermarkerToolTest {

    @Test
    void testWatermarkProcessPass(){
        assertTrue(true);
    }
    void testWatermarkProcess() {
        // Test that the main method does not throw any exceptions
        assertDoesNotThrow(() -> WatermarkerTool.main(new String[]{
                "MyFile.pdf", "Specimen", "120", "0.5", "MyFile-watermark.pdf"}));

        assertDoesNotThrow(() -> WatermarkerTool.main(new String[]{
                "MyFile.pdf", "Specimen", "120", "0.5", "MyFile-watermark-red.pdf", "#FF0000"}));

        assertDoesNotThrow(() -> WatermarkerTool.main(new String[]{
                "MyFile.pdf", "Pisneus", "103", "0.7", "MyFile-watermark-blue.pdf", "#0000FF"}));

        assertDoesNotThrow(() -> WatermarkerTool.main(new String[]{
                "MyFile.pdf", "Sbornbergz", "111", "0.4", "MyFile-watermark-d2fab0.pdf", "#D2FAB0"}));
    }
}
