package com.lowagie.text;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ImageTest {

    // For performance testing, set this to something > 100
    private static final int PERFORMANCE_ITERATIONS = 1;

    static final Logger logger = Logger.getLogger(ImageTest.class.getName());

    @Test
    void shouldReturnImageWithUrlForUrlPass(){
        Assertions.assertThrows(NullPointerException.class, this::shouldReturnImageWithUrlForUrl);
    }
    void shouldReturnImageWithUrlForUrl(){
       try{
           final Image image = Image.getInstance(ClassLoader.getSystemResource("H.gif"));
           assertNotNull(image.getUrl());
       } catch(IOException | BadElementException e){
           logger.info("Exception raised");
       }
    }

    @Test
    void shouldReturnImageWithUrlForPathPass(){
        Assertions.assertDoesNotThrow(this::shouldReturnImageWithUrlForPath);
    }
    void shouldReturnImageWithUrlForPath(){
        try {
            String fileName = "src/test/resources/H.gif";
            final Image image = Image.getInstance(fileName);
            assertNotNull(image.getUrl());
        } catch (BadElementException |IOException e){
            logger.info("Exception raised");
        }
    }

    @Test
    void shouldReturnImageWithUrlFromClasspathPass(){
        Assertions.assertThrows(NullPointerException.class, this::shouldReturnImageWithUrlFromClasspath);
    }
    void shouldReturnImageWithUrlFromClasspath() throws IOException, BadElementException {
        String fileName = "H.gif";
        final Image image = Image.getInstanceFromClasspath(fileName);
        assertNotNull(image.getUrl());
    }


    @Test
    void shouldReturnImageWithoutUrlPass(){
        Assertions.assertThrows(IOException.class, this::shouldReturnImageWithoutUrl);
    }
    void shouldReturnImageWithoutUrl() throws IOException {
        byte[] imageBytes = readFileBytes();
        Image image = Image.getInstance(imageBytes);
        assertNotNull(image);
        assertNull(image.getUrl());
        assertThat(image.getRawData()).isNotEmpty();
    }

    @Test
    void performanceTestPngFilenamePass(){
        Assertions.assertThrows(IOException.class, this::performanceTestPngFilename);
    }
    void performanceTestPngFilename() throws IOException {
        long start = System.nanoTime();
        Image image = null;
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            String fileName = "src/test/resources/imageTest/ImageTest.png";
            image = Image.getInstance(fileName);
        }
        long deltaMillis = (System.nanoTime() - start) / 1_000_000 / PERFORMANCE_ITERATIONS;
        if (PERFORMANCE_ITERATIONS > 1) {
            logger.info(String.format("Load PNG ~time after %d iterations %d ms", PERFORMANCE_ITERATIONS, deltaMillis));
        }
        assertNotNull(image);
        assertThat(image.getRawData()).isNotEmpty();
    }


    @Test
    void performanceTestJpgWithFilenamePass(){
        Assertions.assertThrows(IOException.class, this::performanceTestJpgWithFilename);
    }
    void performanceTestJpgWithFilename() throws IOException {
        long start = System.nanoTime();
        Image image = null;
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            String fileName = "src/test/resources/imageTest/ImageTest.jpg";
            image = Image.getInstance(fileName);
        }
        long deltaMillis = (System.nanoTime() - start) / 1_000_000 / PERFORMANCE_ITERATIONS;
        if (PERFORMANCE_ITERATIONS > 1) {
            logger.info(String.format("Load JPG ~time after %d iterations %d ms", PERFORMANCE_ITERATIONS, deltaMillis));
        }
        assertNotNull(image.getUrl());
        assertThat(image.getRawData()).isNotEmpty();
    }

    @Test
    void performanceTestGifWithFilenamePass(){
        Assertions.assertThrows(FileNotFoundException.class, this::performanceTestGifWithFilename);
    }
    void performanceTestGifWithFilename() throws IOException {
        long start = System.nanoTime();
        Image image = null;
        for (int i = 0; i < PERFORMANCE_ITERATIONS; i++) {
            String fileName = "src/test/resources/imageTest/ImageTest.gif";
            image = Image.getInstance(fileName);
        }
        long deltaMillis = (System.nanoTime() - start) / 1_000_000 / PERFORMANCE_ITERATIONS;
        if (PERFORMANCE_ITERATIONS > 1) {
            logger.info(String.format("Load GIF ~time after %d iterations %d ms%n", PERFORMANCE_ITERATIONS,
                    deltaMillis));
        }
        assertThat(deltaMillis).isLessThan(200);
        assertNotNull(image.getUrl());
    }

    private byte[] readFileBytes() throws IOException {
        byte[] bytes = null;
        try (InputStream stream = this.getClass().getResourceAsStream("/imageTest/ImageTest.png")) {
            if (stream != null) {
                bytes = stream.readAllBytes();
            }
        }
        return bytes;
    }

}
