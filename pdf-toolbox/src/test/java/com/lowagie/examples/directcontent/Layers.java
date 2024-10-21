/*
 * $Id: Layers.java 3838 2009-04-07 18:34:15Z mstorer $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */
package com.lowagie.examples.directcontent;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Explains the concept concerning PdfContentByte layers.
 */
public class Layers {

    /**
     * Draws different things into different layers.
     *
     * @param args no arguments needed
     */


    public static void main(String[] args) {

        Logger logger = Logger.getLogger(Layers.class.getName());
        logger.info("Layers generation process started");

        // step 1: creation of a document-object
        Document document = new Document();

        try {
            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("layers.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            // high level
            Paragraph p = new Paragraph();
            for (int i = 0; i < 100; i++) {
                p.add(new Chunk("Blah blah blah blah blah. "));
            }
            document.add(p);

            // Add image
            Image img = Image.getInstance("hitchcock.png");
            img.setAbsolutePosition(100, 500);
            document.add(img);

            // low level
            PdfContentByte cb = writer.getDirectContent();
            PdfContentByte cbu = writer.getDirectContentUnder();
            cb.setRGBColorFill(0xFF, 0xFF, 0xFF);
            cb.circle(250.0f, 500.0f, 50.0f);
            cb.fill();
            cb.sanityCheck();

            cbu.setRGBColorFill(0xFF, 0x00, 0x00);
            cbu.circle(250.0f, 500.0f, 100.0f);
            cbu.fill();
            cbu.sanityCheck();

        } catch (DocumentException | IOException de) {
            logger.log(Level.SEVERE, "An error occurred while creating the PDF layers.", de);
        } finally {
            // step 5: we close the document
            if (document.isOpen()) {
                document.close();
                logger.info("Document closed successfully.");
            }
        }
    }


}
