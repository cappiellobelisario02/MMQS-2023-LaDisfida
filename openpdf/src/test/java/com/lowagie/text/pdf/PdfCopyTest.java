package com.lowagie.text.pdf;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.lowagie.text.exceptions.InvalidPdfException;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PdfCopyTest {

    @Test
    void nullpointerExceptionTest() {
        //given when
        Assertions.assertThrows(InvalidPdfException.class, this::pdfCopyTest);
    }

    private void pdfCopyTest() throws IOException, PDFFilterException {
        // Use try-with-resources to ensure the InputStream is closed properly
        try (InputStream stream = getClass().getResourceAsStream("/openpdf_bug_test.pdf")) {
            if (stream == null) {
                throw new IOException("Resource not found: /openpdf_bug_test.pdf");
            }

            PdfReader reader = new PdfReader(stream);

            byte[] bytes;

            // Use try-with-resources for ByteArrayOutputStream to ensure it is closed properly
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                PdfCopyFields pdfCopyFields = new PdfCopyFields(baos);

                pdfCopyFields.addDocument(reader, "1"); // <-- just the table of contents

                pdfCopyFields.close(); // <-- close to ensure resources are released

                baos.flush();

                bytes = baos.toByteArray();
            }

            // Write output to file
            try (OutputStream os = new FileOutputStream("output.pdf")) {
                os.write(bytes);
            }
        }
    }

}
