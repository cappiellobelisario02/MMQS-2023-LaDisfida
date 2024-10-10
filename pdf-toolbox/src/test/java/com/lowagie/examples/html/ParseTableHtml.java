/*
 * $Id: HelloHtml.java 3373 2008-05-12 16:21:24Z xlv $
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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.html.HtmlParser;
import com.lowagie.text.pdf.PdfWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Generates a simple table HTML page.
 *
 * @author chappyer
 */

public class ParseTableHtml {

    /**
     * Generates an HTML page with a table
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {
        System.out.println("Parse ParseTable");

        // Step 1: Creation of a document-object
        try (Document document = new Document();
                // Using try-with-resources for InputStream to ensure it is closed properly
                InputStream htmlStream = ParseHelloHtml.class.getClassLoader()
                        .getResourceAsStream("com/lowagie/examples/html/parseTable.html")) {

            // Check if the HTML resource was found
            if (htmlStream == null) {
                throw new IOException("Resource not found: com/lowagie/examples/html/parseTable.html");
            }

            PdfWriter.getInstance(document, Files.newOutputStream(Paths.get("parseTable.pdf")));

            // Step 2: Open the document
            document.open();

            // Step 3: Parsing the HTML document to convert it into PDF
            HtmlParser.parse(document, htmlStream);

        } catch (DocumentException | IOException de) {
            System.err.println("Error: " + de.getMessage());
        }
    }

}
