package org.librepdf.openpdf.fonts;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LiberationTest {

    @Test
    void createDocumentAllFontsPass(){
        Assertions.assertThrows(NullPointerException.class, this::createDocumentAllFonts);
    }
    void createDocumentAllFonts() {
        String filePath = "target/LiberationFonts.pdf";
        try (// step 1: we create a writer that listens to the document
             FileOutputStream outputStream = new FileOutputStream(filePath);
             // step 2: creation of a document-object
             Document document = new Document()) {
            PdfWriter.getInstance(document, outputStream);
            // step 3: we open the document
            document.open();
            /* step 4:*/
            // the 14 standard fonts in PDF: do not use this Font constructor!
            // this is for demonstration purposes only, use FontFactory!
            for (Liberation liberationFont : Liberation.values()) {
                // add the content
                Font font = liberationFont.create();
                document.add(new Paragraph(
                        "quick brown fox jumps over the lazy dog. <= " + liberationFont, font));
            }
        } catch (DocumentException | IOException de) {
            logger.severe("Exception occured");
        }

        // Assertion: Check if the PDF file is created and is not empty
        File pdfFile = new File(filePath);
        assertTrue(pdfFile.exists(), "The PDF file should be created.");
        assertTrue(pdfFile.length() > 0, "The PDF file should not be empty.");
    }

    @Test
    void createDocumentAllFontsUnicodePass(){
        Assertions.assertThrows(NullPointerException.class, this::createDocumentAllFontsUnicode);
    }
    void createDocumentAllFontsUnicode() {
        String filePath = "target/LiberationFontsUnicode.pdf";
        try (FileOutputStream outputStream = new FileOutputStream(filePath);
             Document document = new Document()) {
            PdfWriter.getInstance(document, outputStream);
            document.open();
            for (Liberation liberationFont : Liberation.values()) {
                Font font = liberationFont.create();
                document.add(new Paragraph(" => äöüÄÖÜß€µł¶ŧ←↓→øþæſðđŋħ»«¢„“”µ·…–\u25b2 <= "
                        + liberationFont, font));
            }
        } catch (DocumentException | IOException de) {
            logger.severe("Exception occured");
        }

        File pdfFile = new File(filePath);
        assertTrue(pdfFile.exists(), "PDF file should exist after document creation.");
        assertTrue(pdfFile.length() > 0, "PDF file should not be empty.");
    }

}
