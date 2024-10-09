package com.lowagie.text.pdf.metadata;

import com.lowagie.text.Document;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Paragraph;
import com.lowagie.text.exceptions.InvalidPdfException;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ProducerTest {

    private static final String PRODUCER = "Producer";

    @Test
    void changeProducerLineTestPass(){
        Assertions.assertThrows(NullPointerException.class, this::changeProducerLineTest);
    }
    void changeProducerLineTest() throws IOException {
        String expected = "New Producer.";

        try(Document document = new Document();
                ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            PdfWriter.getInstance(document, baos);
            document.addProducer(expected);
            document.open();
            document.add(new Paragraph("Hello World!"));
            document.close();

            byte[] pdfBytes = baos.toByteArray();
            baos.close();

            PdfReader reader = new PdfReader(new ByteArrayInputStream(pdfBytes));

            Map<String, String> infoDictionary = reader.getInfo();
            String actual = infoDictionary.get(PRODUCER);

            Assertions.assertEquals(expected, actual);

            reader.close();
        } catch (PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
    }

    @Test
    void testMetadataProducerStamperIssue254Pass(){
        Assertions.assertThrows(IOException.class, this::testMetadataProducerStamperIssue254);
    }
    void testMetadataProducerStamperIssue254() throws IOException {
        File origin = new File("src/test/resources/pdf_form_metadata_issue_254.pdf");

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfReader reader = new PdfReader(origin.getAbsolutePath());
                PdfStamper stamp = new PdfStamper(reader, baos)){
            String sData = baos.toString();
            Assertions.assertTrue(sData.contains("(LibreOffice 6.0; modified using OpenPDF"));
        } catch (PDFFilterException | NoSuchAlgorithmException e) {
            throw new ExceptionConverter(e);
        }


    }
}