package com.lowagie.text.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.fop.pdf.PDFFilterException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TabTest {

    @Test
    void TabTest1() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        String stringWithTab = "data\ttable";
        try (Document document
                = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
                PdfReader rd = new PdfReader(stream.toByteArray())) {
            Document.compress = false;
            PdfWriter.getInstance(document, stream);
            document.open();
            Chunk a = new Chunk(stringWithTab);
            document.add(a);

            PdfTextExtractor pdfTextExtractor = new PdfTextExtractor(rd);
            Assertions.assertEquals(stringWithTab, pdfTextExtractor.getTextFromPage(1));
            Document.compress = true;
        }catch(DocumentException | PDFFilterException e){
            throw new ExceptionConverter(e);
        }

    }
}
