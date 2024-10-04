package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PdfTestBaseTest {

    @Test
    void testCreatePdfStreamPass(){
        Assertions.assertThrows(NullPointerException.class, this::testCreatePdfStream);
    }
    void testCreatePdfStream() throws DocumentException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (Document pdf = PdfTestBase.createPdf(stream)) {
            pdf.open();
            pdf.newPage();
            pdf.add(new Paragraph("Hello World!"));
        }
        // close document
        final byte[] bytes = stream.toByteArray();
        Assertions.assertNotNull(bytes, "There should be some PDF-bytes there.");
        String header = new String(bytes, 0, 5);
        Assertions.assertEquals("%PDF-", header, "This is no PDF.");
    }

}
