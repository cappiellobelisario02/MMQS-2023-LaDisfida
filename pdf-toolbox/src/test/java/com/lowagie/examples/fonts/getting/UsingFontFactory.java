/*
 * $Id: UsingFontFactory.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.fonts.getting;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Special rendering of Chunks.
 *
 * @author blowagie
 */

public class UsingFontFactory {

    private static final Logger logger = Logger.getLogger(UsingFontFactory.class.getName());

    /**
     * Special rendering of Chunks.
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Fonts in the FontFactory");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            PdfWriter.getInstance(document,
                    new FileOutputStream("FontFactory.pdf"));

            // step 3: we open the document
            document.open();
            // step 4:
            String name;
            Paragraph p = new Paragraph("Font Families", FontFactory.getFont(FontFactory.HELVETICA, 16f));
            document.add(p);
            FontFactory.registerDirectories();
            TreeSet<String> families = new TreeSet<>(FontFactory.getRegisteredFamilies());
            int c = 0;
            for (Iterator<String> i = families.iterator(); i.hasNext() && c < 15; ) {
                name = i.next();
                p = new Paragraph(name);
                document.add(p);
                c++;
            }
            document.newPage();
            String quick = "quick brown fox jumps over the lazy dog";
            for (Iterator<String> i = families.iterator(); i.hasNext() && c > 0; ) {
                name = i.next();
                p = new Paragraph(name);
                document.add(p);
                try {
                    p = new Paragraph(quick, FontFactory.getFont(name, BaseFont.WINANSI, BaseFont.EMBEDDED));
                    document.add(p);
                } catch (DocumentException e) {
                    document.add(new Paragraph(e.getMessage()));
                }
                c--;
            }
        } catch (DocumentException | IOException de) {
            logger.log(Level.SEVERE, "An error occurred while processing the document.", de);
        }

        // step 5: we close the document
        document.close();
    }
}
