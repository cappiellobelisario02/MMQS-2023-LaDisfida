/*
 * $Id: HelloWorldMeta.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.html;

import com.lowagie.examples.objects.FancyLists;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.HtmlWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates a simple PDF file with metadata.
 *
 * @author blowagie
 */

public class HelloWorldMeta {

    private static final Logger logger = Logger.getLogger(HelloWorldMeta.class.getName());

    /**
     * Generates a PDF file with metadata
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Metadata");

        // step 1: creation of a document-object
        Document document = new Document();
        try {
            // step 2:
            // we create a writer that listens to the document
            // and directs a PDF-stream to a file
            HtmlWriter.getInstance(document,
                    new FileOutputStream("HelloWorldMeta.html"));

            // step 3: we add some metadata open the document
            // standard meta information
            document.addTitle("Hello World example");
            document.addAuthor("Bruno Lowagie");
            document.addSubject("This example explains step 3 in Chapter 1");
            document.addKeywords("Metadata, iText, step 3, tutorial");
            // custom (HTML) meta information
            document.addHeader("Expires", "0");
            // meta information that will be in a comment section in HTML
            document.addCreator("My program using iText");
            document.open();
            // step 4: we add a paragraph to the document
            document.add(new Paragraph("Hello World"));
        } catch (DocumentException | IOException de) {
            logger.log(Level.SEVERE, "An error occurred while processing the document.", de);
        }

        // step 5: we close the document
        document.close();
    }
}
