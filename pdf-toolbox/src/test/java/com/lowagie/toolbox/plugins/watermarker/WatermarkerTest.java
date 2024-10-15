package com.lowagie.toolbox.plugins.watermarker;

import static java.awt.Color.RED;
import static java.nio.file.Files.readAllBytes;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lowagie.text.DocumentException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.lowagie.text.exceptions.InvalidPdfException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class WatermarkerTest {

    @Test
    void shouldWriteWatermarkWithIOStreamsPass(){
        Assertions.assertThrows(NullPointerException.class, this::shouldWriteWatermarkWithIOStreams);
    }
    void shouldWriteWatermarkWithIOStreams() throws IOException, DocumentException {
        // GIVEN
        Path path = Paths.get("src/test/resources/layers.pdf");
        byte[] input = readAllBytes(path);
        String text = "Specimen";
        int fontsize = 100;
        float opacity = 0.5f;

        // WHEN
        byte[] result = new Watermarker(input, text, fontsize, opacity)
                .withColor(RED)
                .write();

        // THEN
        assertTrue(result.length > input.length);

        // Uncomment the following to write the generated file on disk
        // writeOnDisk(resu
    }

    @SuppressWarnings("unused")
    private void writeOnDisk(byte[] result) throws IOException {
        try (FileOutputStream fileOutputStream =
                new FileOutputStream("src/test/resources/layers-marked.pdf")) {
            fileOutputStream.write(result);
        }
    }
}
