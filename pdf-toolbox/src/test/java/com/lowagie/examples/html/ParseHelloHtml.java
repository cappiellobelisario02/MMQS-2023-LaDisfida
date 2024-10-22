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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.lowagie.tools.SplitPdf.logger;

/**
 * Generates a simple 'Hello World' HTML page.
 *
 * @author blowagie
 */

public class ParseHelloHtml {

    /**
     * Generates an HTML page with the text 'Hello World'
     *
     */
    public static void parseHtmlToPdf() {
        InputStream htmlStream = null;
        try (Document document = new Document()) {
            PdfWriter.getInstance(document, new FileOutputStream("parseHelloWorld.pdf"));

            // Step 2: we open the document
            document.open();

            // Retrieve the class loader and check for null
            ClassLoader classLoader = ParseHelloHtml.class.getClassLoader();
            if (classLoader == null) {
                throw new IllegalStateException("ClassLoader is null.");
            }

            // Step 3: parsing the HTML document to convert it into PDF
            htmlStream = classLoader.getResourceAsStream("com/lowagie/examples/html/parseHelloWorld.html");
            if (htmlStream == null) {
                throw new IOException("Resource not found: com/lowagie/examples/html/parseHelloWorld.html");
            }
            HtmlParser.parse(document, htmlStream);
        } catch (DocumentException | IOException de) {
            logger.info("Error parsing HTML to PDF: " + de);
        } finally {
            if (htmlStream != null){
                try{
                    htmlStream.close();
                }catch (IOException e){
                    logger.info("Error closing stream: " + e);
                }
            }
        }
    }


}
