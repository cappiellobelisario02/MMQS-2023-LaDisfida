/*
 * $Id: SelectedPages.java 3271 2008-04-18 20:39:42Z xlv $
 * Copyright (c) 2005-2007 Bruno Lowagie, Carsten Hammer
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
 * This class was originally published under the MPL by Bruno Lowagie
 * and Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.StringArgument;
import com.lowagie.rups.io.filters.PdfFilter;
import org.apache.fop.pdf.PDFFilterException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;

/**
 * This tool lets you select pages from an existing PDF and copy them into a new PDF.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class SelectedPages extends AbstractTool {

    public static final Logger logger = Logger.getLogger(com.lowagie.toolbox.plugins.SelectedPages.class.getName());
    public static final String SRCFILE = "srcfile";
    public static final String DESTFILE = "destfile";
    public static final String EXCEPTION_OCCURED = "Exception occured";
    public static final String ERROR_CREATING_DOCUMENT = "Error creating document";

    static {
        addVersion("$Id: SelectedPages.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs a SelectedPages object.
     */
    public SelectedPages() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        arguments.add(new FileArgument(this, SRCFILE, "The file you want to split", false, new PdfFilter()));
        arguments.add(new FileArgument(this, DESTFILE,
                "The file to which the first part of the original PDF has to be written", true, new PdfFilter()));
        arguments.add(new StringArgument(this, "selection", "A selection of pages (see Help for more info)"));
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("SelectedPages", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== SelectedPages OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        PdfReader reader = null;
        Document document = null;
        FileOutputStream fouts = null;
        PdfCopy copy = null;
        String stringToLog;

        try {
            if (getValue(SRCFILE) == null) {
                throw new InstantiationException("You need to choose a source file");
            }
            File src = (File) getValue(SRCFILE);
            if (getValue(DESTFILE) == null) {
                throw new InstantiationException("You need to choose a destination file for the first part of the PDF");
            }
            File dest = (File) getValue(DESTFILE);
            String selection = (String) getValue("selection");

            reader = createPdfReader(src);
            logger.info("The original file had " + reader.getNumberOfPages() + " pages.");
            reader.selectPages(selection);

            int pages = reader.getNumberOfPages();
            stringToLog = "The new file has " + pages + " pages.";
            logger.severe(stringToLog);

            document = createDocument(reader);
            fouts = createFileOutputStream(dest);
            copy = createPdfCopy(document, fouts);

            document.open();
            PdfImportedPage page;

            for (int i = 1; i <= pages; i++) {
                stringToLog = "Processed page " + i;
                logger.info(stringToLog);
                page = copy.getImportedPage(reader, i);
                copy.addPage(page);
            }

            PRAcroForm form = reader.getAcroForm();
            if (form != null) {
                copy.copyAcroForm(reader);
            }

            document.close();
        } catch (IOException | InstantiationException e) {
            logger.severe(EXCEPTION_OCCURED);
        } finally {
            if (reader != null && document != null && fouts != null && copy != null) {
                try {
                    reader.close();
                    document.close();
                    fouts.close();
                    copy.close();
                } catch (IOException e) {
                    logger.severe(EXCEPTION_OCCURED);
                }
            }
        }
    }

    private PdfReader createPdfReader(File src) {
        PdfReader reader = null;
        try {
            reader = new PdfReader(src.getAbsolutePath());
        } catch (IOException | PDFFilterException | SecurityException e) {
            logger.severe(EXCEPTION_OCCURED);
        }
        return reader;
    }
    private Document createDocument(PdfReader reader) {
        Document document = null;
        try {
            document = new Document(reader.getPageSizeWithRotation(1));
        } catch (DocumentException e) {
            logger.severe(ERROR_CREATING_DOCUMENT);
        }
        return document;
    }

    private FileOutputStream createFileOutputStream(File dest) {
        FileOutputStream fouts = null;
        try {
            fouts = new FileOutputStream(dest.getAbsolutePath());
        } catch (FileNotFoundException e) {
            logger.severe(ERROR_CREATING_DOCUMENT);
        }
        return fouts;
    }

    private PdfCopy createPdfCopy(Document document, FileOutputStream fouts) {
        PdfCopy copy = null;
        try {
            copy = new PdfCopy(document, fouts);
        } catch (DocumentException e) {
            logger.severe(ERROR_CREATING_DOCUMENT);
        }
        return copy;
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
