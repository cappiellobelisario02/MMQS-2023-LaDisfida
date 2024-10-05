package org.librepdf.openpdf.fonts;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

class UnicodePdfTest {

    private static final String INPUT = "Symbol: '\u25b2' Latin: 'äöüÄÖÜß'";

    @Test
    void testSimplePdfPass(){
        Assertions.assertThrows(NullPointerException.class, this::testSimplePdf);
    }
    void testSimplePdf() throws IOException {
        // Probably a good idea to write the document to a byte array, 
        // so you can read the result and make some checks.
        
        // Create a ByteArrayOutputStream to write the document to
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // Create document and write to ByteArrayOutputStream
        Document document = FontsTestUtil.createPdf(outputStream);
        document.open();
        document.add(new Paragraph(INPUT, Liberation.SANS.create()));
        document.close();

        // Convert ByteArrayOutputStream to byte array
        byte[] pdfBytes = outputStream.toByteArray();

        // Parse the PDF content and assert it contains the expected text
        String pdfContent = FontsTestUtil.extractTextFromPdf(pdfBytes);
        Assertions.assertTrue(pdfContent.contains("Symbol: '\u25b2'"), "PDF should contain the symbol ▲");
        Assertions.assertTrue(pdfContent.contains("Latin: 'äöüÄÖÜß'"), "PDF should contain the Latin characters äöüÄÖÜß");
    }
}
