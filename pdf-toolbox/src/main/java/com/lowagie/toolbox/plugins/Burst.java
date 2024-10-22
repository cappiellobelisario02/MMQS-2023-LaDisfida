/*
 * $Id: Burst.java 3271 2008-04-18 20:39:42Z xlv $
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
 * This toolbox plug-in is based on a small example published
 * with the following notices:
 *
 * This code is free software. It may only be copied or modified
 * if you include the following copyright notice:
 *
 * This class by Mark Thompson. Copyright (c) 2002 Mark Thompson.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * It was adapted by Bruno Lowagie and published as a toolbox plug-in
 * under the MPL by Bruno Lowagie and Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.rups.io.filters.PdfFilter;
import com.lowagie.toolbox.swing.PdfInformationPanel;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;

/**
 * This tool lets you split a PDF in several separate PDF files (1 per page).
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Burst extends AbstractTool {

    private static final Logger logger = Logger.getLogger(com.lowagie.toolbox.plugins.Burst.class.getName());

    static {
        addVersion("$Id: Burst.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs a Burst object.
     */
    private static final String SRCFILE_ARG = "srcfile";
    public Burst() {
        FileArgument f = new FileArgument(this, SRCFILE_ARG, "The file you want to split", false, new PdfFilter());
        f.setLabel(new PdfInformationPanel());
        arguments.add(f);
    }

    /**
     * Divide PDF file into pages.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        com.lowagie.toolbox.plugins.Burst tool = new com.lowagie.toolbox.plugins.Burst();
        if (args.length < 1) {
            logger.severe(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Burst", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== Burst OPENED ===");
    }

    /**
     * @see AbstractTool#execute()
     */
    public void execute() {
        PdfReader reader = null;
        Document document = null;
        try {
            if (getValue(SRCFILE_ARG) == null) {
                throw new InstantiationException("You need to choose a sourcefile");
            }

            File src = (File) getValue(SRCFILE_ARG);
            File directory = src.getParentFile();
            String name = src.getName();
            name = name.substring(0, name.lastIndexOf('.'));

            // We create a reader for a certain document
            int n = reader.getNumberOfPages();
            int digits = 1 + (n / 10);
            String stringToLog = "There are " + n + " pages in the original file.";
            logger.info(stringToLog);

            for (int i = 0; i < n; i++) {
                int pagenumber = i + 1;
                String filename = String.format("_%0" + digits + "d.pdf", pagenumber);

                // Create a document-object and a writer that listens to the document
                File outputFile = new File(directory, name + filename);
                try (FileOutputStream fos = new FileOutputStream(outputFile);
                        PdfWriter writer = PdfWriter.getInstance(document, fos)) {

                    // Create and open the document
                    Document document2 = new Document(reader.getPageSizeWithRotation(pagenumber));
                    try {
                        document2.open();
                        PdfContentByte cb = writer.getDirectContent();
                        Split.assignImportedPageAndRotation(reader, cb, writer, pagenumber);
                    } finally {
                        // Ensure the document is closed
                        document.close();
                    }
                }
            }
        } catch (InstantiationException | IOException e) {
            logger.severe("An error occurred: " + e);
        }
        finally {
            if (reader != null) {
                try{
                    reader.close();
                } catch (Exception e){
                    logger.severe("Exception occured");
                }
            }
            if (document != null) {
                try {
                    document.close();
                } catch (Exception e) {
                    logger.severe("Exception occured");
                }
            }
        }
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
        throw new InstantiationException("There is more than one destfile.");
    }
}
