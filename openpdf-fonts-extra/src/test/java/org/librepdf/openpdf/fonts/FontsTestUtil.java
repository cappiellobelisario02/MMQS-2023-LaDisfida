package org.librepdf.openpdf.fonts;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FontsTestUtil {

    static Document createPdf(OutputStream outputStream) throws DocumentException {
        // create a new document
        Document document = new Document(PageSize.A4);
        // generate file
        PdfWriter.getInstance(document, outputStream);
        return document;
    }

    static Document createPdf(String filename) throws FileNotFoundException {
        FileOutputStream outputStream = new FileOutputStream(filename);
        return createPdf(outputStream);
    }

    static String extractTextFromPdf(byte[] pdfBytes) throws IOException {
        // Create a PdfReader from the byte array
        PdfReader reader;
        try {
            reader = new PdfReader(new ByteArrayInputStream(pdfBytes));
        } catch (org.apache.fop.pdf.PDFFilterException e) {
            throw new ExceptionConverter(e);
        }
        StringBuilder extractedText = new StringBuilder();

        // Loop through each page and extract text
        PdfTextExtractor textExtractor = new PdfTextExtractor(reader);
        for (int i = 1; i <= reader.getNumberOfPages(); i++) {
            extractedText.append(textExtractor.getTextFromPage(i));
        }

        // Close the reader
        reader.close();

        return extractedText.toString();
    }
}
