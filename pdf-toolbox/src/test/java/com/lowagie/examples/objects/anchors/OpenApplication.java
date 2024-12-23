/*
 * $Id: OpenApplication.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.objects.anchors;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Creates a documents with different named actions.
 *
 * @author blowagie
 */

public class OpenApplication {

    private static final Logger logger = Logger.getLogger(OpenApplication.class.getName());

    /**
     * Creates a document with Named Actions.
     *
     * @param args the system root (for instance "C:\windows\")
     */
    public static void main(String[] args) {

        System.out.println("Open Application");

        // step 1: creation of a document-object
        try (Document document = new Document(PageSize.A4, 50, 50, 50, 50)){
            // step 2: we create a writer that listens to the document
            PdfWriter.getInstance(document,
                    new FileOutputStream("OpenApplication.pdf"));
            // step 3: we open the document
            document.open();
            // step 4: we add some content
            String application = args[0] + "notepad.exe";
            Paragraph p = new Paragraph(new Chunk("Click to open "
                    + application).setAction(new PdfAction(application, null,
                    null, null)));
            document.add(p);
        } catch (IOException | DocumentException | SecurityException de) {
            logger.severe("Exception occured");
        }

    }
}