/*
 * $Id: Split.java 3271 2008-04-18 20:39:42Z xlv $
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
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.IntegerArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import com.lowagie.toolbox.swing.PdfInformationPanel;
import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;

/**
 * This tool lets you split a PDF in two separate PDF files.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Split extends AbstractTool {

    public static final Logger logger = Logger.getLogger(Split.class.getName());
    public static final String SRCFILE = "srcfile";
    public static final String DESTFILE_1 = "destfile1";
    public static final String DESTFILE_2 = "destfile2";

    static {
        addVersion("$Id: Split.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs an Split object.
     */
    public Split() {
        FileArgument f = new FileArgument(this, SRCFILE, "The file you want to split", false, new PdfFilter());
        f.setLabel(new PdfInformationPanel());
        arguments.add(f);
        arguments.add(new FileArgument(this, DESTFILE_1,
                "The file to which the first part of the original PDF has to be written", true, new PdfFilter()));
        arguments.add(new FileArgument(this, DESTFILE_2,
                "The file to which the second part of the original PDF has to be written", true, new PdfFilter()));
        arguments.add(new IntegerArgument(this, "pagenumber", "The pagenumber where you want to split"));
    }

    /**
     * Split a PDF in two separate PDF files.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        Split tool = new Split();
        if (args.length < 4) {
            logger.severe(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Split", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== Split OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        try {
            validateInputFiles();
            int pagenumber = Integer.parseInt((String) getValue("pagenumber"));
            File src = (File) getValue(SRCFILE);
            File file1 = (File) getValue(DESTFILE_1);
            File file2 = (File) getValue(DESTFILE_2);

            splitPdf(src, file1, file2, pagenumber);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validateInputFiles() throws InstantiationException {
        if (getValue(SRCFILE) == null) {
            throw new InstantiationException("You need to choose a sourcefile");
        }
        if (getValue(DESTFILE_1) == null) {
            throw new InstantiationException("You need to choose a destination file for the first part of the PDF");
        }
        if (getValue(DESTFILE_2) == null) {
            throw new InstantiationException("You need to choose a destination file for the second part of the PDF");
        }
    }

    private void splitPdf(File src, File file1, File file2, int pagenumber) throws IOException, DocumentException {
        PdfReader reader = null;
        Document document1 = null;
        Document document2 = null;

        try {
            reader = new PdfReader(src.getAbsolutePath());
            int totalPages = reader.getNumberOfPages();
            logPageCount(totalPages);

            if (pagenumber < 2 || pagenumber > totalPages) {
                throw new DocumentException(
                        "You can't split this document at page " + pagenumber + "; there is no such page.");
            }

            document1 = new Document(reader.getPageSizeWithRotation(1));
            document2 = new Document(reader.getPageSizeWithRotation(pagenumber));

            PdfWriter writer1 = PdfWriter.getInstance(document1, new FileOutputStream(file1));
            PdfWriter writer2 = PdfWriter.getInstance(document2, new FileOutputStream(file2));

            document1.open();
            document2.open();

            PdfContentByte cb1 = writer1.getDirectContent();
            PdfContentByte cb2 = writer2.getDirectContent();

            copyPages(reader, pagenumber, totalPages, document1, cb1, writer1, 1);
            copyPages(reader, pagenumber, totalPages, document2, cb2, writer2, pagenumber);

            document1.close();
            document2.close();
        } finally {
            closeResources(reader, document1, document2);
        }
    }

    private void logPageCount(int totalPages) {
        String stringToLog = "There are " + totalPages + " pages in the original file.";
        logger.info(stringToLog);
    }

    private void copyPages(PdfReader reader, int pagenumber, int totalPages, Document document, PdfContentByte cb, PdfWriter writer, int startPage) throws DocumentException {
        for (int i = startPage; i <= totalPages && i < pagenumber + startPage - 1; i++) {
            document.setPageSize(reader.getPageSizeWithRotation(i));
            document.newPage();
            PdfImportedPage page = writer.getImportedPage(reader, i);
            int rotation = reader.getPageRotation(i);
            if (rotation == 90 || rotation == 270) {
                cb.addTemplate(page, 0, -1f, 1f, 0, 0, reader.getPageSizeWithRotation(i).getHeight());
            } else {
                cb.addTemplate(page, 1f, 0, 0, 1f, 0, 0);
            }
        }
    }

    private void closeResources(PdfReader reader, Document document1, Document document2) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (document1 != null) {
                document1.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (document2 != null) {
                document2.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * @param arg StringArgument
     * @see com.lowagie.toolbox.AbstractTool#valueHasChanged(com.lowagie.toolbox.arguments.AbstractArgument)
     */
    public void valueHasChanged(AbstractArgument arg) {
        // if the internal frame is null, the tool was called from the command line
        // represent the changes of the argument in the internal frame
    }

    /**
     * @return File
     * @throws InstantiationException on error
     * @see com.lowagie.toolbox.AbstractTool#getDestPathPDF()
     */
    protected File getDestPathPDF() throws InstantiationException {
        return (File) getValue(DESTFILE_1);
    }
}
