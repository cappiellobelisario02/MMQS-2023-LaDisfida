package com.lowagie.text.pdf;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.InputStream;
import com.lowagie.text.exceptions.InvalidPdfException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PRAcroFormTest {

    @Test
    void infiniteLoopTestPass(){
        Assertions.assertThrows(InvalidPdfException.class, this::infiniteLoopTest);
    }
    void infiniteLoopTest() throws Exception {
        try (InputStream is = PRAcroFormTest.class.getResourceAsStream("/pades_opposite_infinite_loop.pdf");
                PdfReader reader = new PdfReader(is)) {
            assertTimeoutPreemptively(ofMillis(500), () -> {
                reader.getAcroForm();
            });
        }

    }

}
