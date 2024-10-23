/*
 * $Id: Divide.java 3271 2008-04-18 20:39:42Z xlv $
 * Copyright (c) 2005-2007 Carsten Hammer
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * This class was originally published under the MPL by Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.rups.io.filters.PdfFilter;
import org.apache.fop.pdf.PDFFilterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;

/**
 * This tool lets you generate a PDF that shows N pages on 1.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Divide extends AbstractTool {

    static {
        addVersion("$Id: Divide.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    static Logger logger = Logger.getLogger(com.lowagie.toolbox.plugins.Divide.class.getName());

    /**
     * Constructs an Divide object.
     */
    private static final String DESTFILE = "destfile";
    private static final String SRCFILE_ARGUMENT_NAME = "srcfile";
    public Divide() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        arguments.add(new FileArgument(this, SRCFILE_ARGUMENT_NAME,
                "The file you want to divide", false, new PdfFilter()));
        arguments.add(new FileArgument(this, DESTFILE, "The resulting PDF",
                true, new PdfFilter()));
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Divide", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== Divide OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        PdfReader reader;
        Document document;
        try {
            if (getValue(SRCFILE_ARGUMENT_NAME) == null) {
                throw new InstantiationException("You need to choose a sourcefile");
            }
            File src = (File) getValue(SRCFILE_ARGUMENT_NAME);

            if (getValue(DESTFILE) == null) {
                throw new InstantiationException("You need to choose a destination file");
            }
            File dest = (File) getValue(DESTFILE);

            // Create PdfReader with the extracted method
            reader = createPdfReader(src);

            // Retrieve the total number of pages
            int total = reader.getNumberOfPages();
            String stringToLog = "There are " + total + " pages in the original file.";
            logger.info(stringToLog);  // Replaced System.out.println with logger

            // Get page size and create a new size for the document
            Rectangle pageSize = reader.getPageSize(1);
            Rectangle newSize = new Rectangle(pageSize.getWidth() / 2, pageSize.getHeight());

            // Create Document with the extracted method
            document = createDocument(newSize);

            // Create PdfWriter that listens to the document
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest));

            // Open the document
            document.open();

            // Add content to the document
            PdfContentByte cb = writer.getDirectContent();
            PdfImportedPage page;
            float offsetX;
            float offsetY;
            int p;
            for (int i = 0; i < total; i++) {
                p = i + 1;
                pageSize = reader.getPageSize(p);
                newSize = new Rectangle(pageSize.getWidth() / 2, pageSize.getHeight());

                // Add the left half of the page
                document.newPage();
                offsetX = 0;
                offsetY = 0;
                page = writer.getImportedPage(reader, p);
                cb.addTemplate(page, 1, 0, 0, 1, offsetX, offsetY);

                // Add the right half of the page
                document.newPage();
                offsetX = -newSize.getWidth();
                offsetY = 0;
                page = writer.getImportedPage(reader, p);
                cb.addTemplate(page, 1, 0, 0, 1, offsetX, offsetY);
            }

            // Close the document
            document.close();
        } catch (FileNotFoundException | InstantiationException e) {
            logger.info(e.getMessage());  // Logging the exception
        }
    }

    // Method to handle PdfReader instantiation
    private PdfReader createPdfReader(File src) {
        PdfReader reader = null;
        try {
            reader = new PdfReader(src.getAbsolutePath());
        } catch (PDFFilterException | IOException e) {
            logger.info("Failed to create PdfReader: " + e.getMessage());
        }
        return reader;
    }

    // Method to handle Document creation
    private Document createDocument(Rectangle newSize) {
        Document document = null;
        try {
            document = new Document(newSize, 0, 0, 0, 0);
        } catch (DocumentException e) {
            logger.info("Failed to create Document: " + e.getMessage());
        }
        return document;
    }

    /**
     * @param arg StringArgument
     * @see com.lowagie.toolbox.AbstractTool#valueHasChanged(com.lowagie.toolbox.arguments.AbstractArgument)
     */
    public void valueHasChanged(AbstractArgument arg) {
        // represent the changes of the argument in the internal frame
    }

    /**
     * @return File
     * @throws InstantiationException on error
     * @see com.lowagie.toolbox.AbstractTool#getDestPathPDF()
     */
    protected File getDestPathPDF() throws InstantiationException {
        return (File) getValue(DESTFILE);
    }
}
