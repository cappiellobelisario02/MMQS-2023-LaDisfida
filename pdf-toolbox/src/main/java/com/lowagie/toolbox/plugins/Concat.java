/*
 * $Id: Concat.java 3271 2008-04-18 20:39:42Z xlv $
 * Copyright (c) 2005-2007 Bruno Lowagie, Carsten Hammer, Paulo Soares
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
 * This class was originally published under the MPL by Bruno Lowagie,
 * Paulo Soares and Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.SimpleBookmark;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;

/**
 * Concatenates two PDF files
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Concat extends AbstractTool {

    private static final Logger logger = Logger.getLogger(Concat.class.getName());
    public static final String SRCFILE_1 = "srcfile1";
    public static final String SRCFILE_2 = "srcfile2";
    public static final String DESTFILE = "destfile";

    static {
        addVersion("$Id: Concat.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs a Concat object.
     */
    public Concat() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        arguments.add(new FileArgument(this, SRCFILE_1, "The first PDF file", false, new PdfFilter()));
        arguments.add(new FileArgument(this, SRCFILE_2, "The second PDF file", false, new PdfFilter()));
        arguments.add(
                new FileArgument(this, DESTFILE, "The file to which the concatenated PDF has to be written", true,
                        new PdfFilter()));
    }

    /**
     * Concatenates two PDF files.
     *
     * @param args String[]
     */
    public static void main(String[] args) throws Exception {
        Concat tool = new Concat();
        if (args.length < 2) {
            logger.severe(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Concatenate 2 PDF files", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== Concat OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() throws Exception {
        validateInputFiles();

        PdfReader reader = null;
        Document document = null;
        FileOutputStream fos = null;
        PdfCopy writer = null;
        int pageOffset = 0;

        try {
            File destinationFile = getDestinationFile();
            List<Map<String, Object>> bookmarks = new ArrayList<>();

            for (File file : getInputFiles()) {
                reader = new PdfReader(String.valueOf(file));
                reader.consolidateNamedDestinations();

                int numberOfPages = reader.getNumberOfPages();
                bookmarks.addAll(SimpleBookmark.getBookmarkList(reader));
                shiftPageNumbers(bookmarks, pageOffset);
                logPageInfo(file, numberOfPages);

                writer = initializeDocument(reader, destinationFile);
                addPages(reader, numberOfPages, writer);
                logProcessedPages(numberOfPages);
                pageOffset += numberOfPages;
            }

            if (!bookmarks.isEmpty()) {
                writer.setOutlines(bookmarks);
            }

            document.close();
        } finally {
            closeResources(reader, document, fos, writer);
        }
    }

    private File[] getInputFiles() throws InstantiationException {
        String[] filePaths = new String[2];
        filePaths[0] = getValue(SRCFILE_1).toString();
        filePaths[1] = getValue(SRCFILE_2).toString();

        if (filePaths[0] == null || filePaths[1] == null) {
            throw new InstantiationException("You need to choose both source files");
        }

        File[] files = new File[2];
        for (int i = 0; i < 2; i++) {
            files[i] = new File(filePaths[i]);
        }

        return files;
    }

    private File getDestinationFile() throws InstantiationException {
        String filePath = getValue(DESTFILE).toString();
        if (filePath == null) {
            throw new InstantiationException("You need to choose a destination file");
        }

        return new File(filePath);
    }

    private void validateInputFiles() throws InstantiationException {
        File[] files = getInputFiles();
        // Combined logic for both checks
        if (files[0] == null || files[1] == null) {
            throw new InstantiationException("You need to choose both source files");
        }
    }

    private PdfCopy initializeDocument(PdfReader reader, File destinationFile) throws IOException {
        Document document = new Document(reader.getPageSizeWithRotation(1));
        FileOutputStream fos = new FileOutputStream(destinationFile);
        document.open();
        return new PdfCopy(document, fos);
    }

    private void shiftPageNumbers(List<Map<String, Object>> bookmarks, int pageOffset) {
        if (bookmarks != null && pageOffset != 0) {
            SimpleBookmark.shiftPageNumbersInRange(bookmarks, pageOffset, null);
        }
    }

    private void logPageInfo(File file, int numberOfPages) {
        String message = "There are " + numberOfPages + " pages in " + file.toString();
        logger.info(message);
    }

    private void addPages(PdfReader reader, int numberOfPages, PdfCopy writer) throws IOException {
        PdfImportedPage page;
        for (int p = 1; p <= numberOfPages; p++) {
            page = writer.getImportedPage(reader, p);
            writer.addPage(page);
        }
    }

    private void logProcessedPages(int numberOfPages) {
        String message = "Processed " + numberOfPages + " pages";
        logger.info(message);
    }

    private void closeResources(PdfReader reader, Document document, FileOutputStream fos, PdfCopy writer) throws Exception {
        if (reader != null) {
            reader.close();
        }
        if (document != null) {
            document.close();
        }
        if (fos != null) {
            fos.close();
        }
        if (writer != null) {
            writer.close();
        }
    }



    /**
     * @param arg StringArgument
     * @see com.lowagie.toolbox.AbstractTool#valueHasChanged(com.lowagie.toolbox.arguments.AbstractArgument)
     */
    public void valueHasChanged(AbstractArgument arg) {
        if (internalFrame == null) {
            // if the internal frame is null, the tool was called from the command line
        }
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
