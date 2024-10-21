package com.lowagie.text.pdf;

import static com.lowagie.text.pdf.ColumnText.logger;
import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.io.IOException;
import java.io.InputStream;
import com.lowagie.text.exceptions.InvalidPdfException;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PRAcroFormTest {

    @Test
    void infiniteLoopTestPass(){
        Assertions.assertThrows(InvalidPdfException.class, this::infiniteLoopTest);
    }
    @Test
    void infiniteLoopTest() {
        try (InputStream is = PRAcroFormTest.class.getResourceAsStream("/pades_opposite_infinite_loop.pdf");
                PdfReader reader = new PdfReader(is)) {
            assertTimeoutPreemptively(ofMillis(500), () -> {
                reader.getAcroForm();
            });
        } catch (IOException e) {
            // Handle IOException related to file operations
            logger.warning("IOException occurred while reading the PDF: " + e.getMessage());
            // You can rethrow a custom exception or handle it as necessary
        } catch (PdfException e) {
            // Handle PdfException specific to PDF reading
            logger.warning("PdfException occurred: " + e.getMessage());
            // You can rethrow a custom exception or handle it as necessary
        } catch (PDFFilterException e) {
            throw new RuntimeException(e);
        }
    }


}
