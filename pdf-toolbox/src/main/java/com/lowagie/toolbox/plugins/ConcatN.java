/*
 * $Id: ConcatN.java 3271 2008-04-18 20:39:42Z xlv $
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
import com.lowagie.toolbox.arguments.FileArrayArgument;
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
public class ConcatN extends AbstractTool {

    private static final Logger logger = Logger.getLogger(ConcatN.class.getName());
    public static final String SRCFILES = "srcfiles";
    public static final String DESTFILE = "destfile";

    static {
        addVersion("$Id: ConcatN.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * Constructs a Concat object.
     */
    public ConcatN() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        arguments.add(new FileArrayArgument(this, SRCFILES,
                "The list of PDF files"));
        arguments.add(new FileArgument(this, DESTFILE,
                "The file to which the concatenated PDF has to be written", true,
                new PdfFilter()));
    }

    /**
     * Concatenates two PDF files.
     *
     * @param args String[]
     */
    public static void main(String[] args) throws Exception {
        ConcatN tool = new ConcatN();
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
        internalFrame = new JInternalFrame("Concatenate n PDF files", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== Concat OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() throws Exception {
        File[] sourceFiles = validateInputFiles();
        File destinationFile = getDestinationFile();

        PdfReader reader = null;
        Document document = null;
        FileOutputStream fos = null;
        PdfCopy writer = null;
        List<Map<String, Object>> bookmarks = new ArrayList<>();
        int pageOffset = 0;

        try {
            for (File sourceFile : sourceFiles) {
                reader = new PdfReader(sourceFile.getAbsolutePath());
                reader.consolidateNamedDestinations();

                int numberOfPages = reader.getNumberOfPages();
                processBookmarks(bookmarks, pageOffset, reader);
                pageOffset += numberOfPages;

                logPageInfo(sourceFile, numberOfPages);

                writer = initializeDocument(reader, destinationFile);

                addPages(reader, numberOfPages, writer);
                logProcessedPages(numberOfPages);

                reader.close();  // Close reader after processing each file
            }

            if (!bookmarks.isEmpty()) {
                writer.setOutlines(bookmarks);
            }

            document.close();  // Close document after processing all files
        } finally {
            closeResources(reader, document, fos, writer);
        }
    }

    private File[] validateInputFiles() throws InstantiationException {
        File[] files = (File[]) getValue(SRCFILES);
        if (files == null || files.length == 0) {
            throw new InstantiationException("You need to choose a list of source files");
        }
        return files;
    }

    private File getDestinationFile() throws InstantiationException {
        File pdfFile = (File) getValue(DESTFILE);
        if (pdfFile == null) {
            throw new InstantiationException("You need to choose a destination file");
        }
        return pdfFile;
    }

    private void processBookmarks(List<Map<String, Object>> bookmarks, int pageOffset, PdfReader reader) {
        List<Map<String, Object>> sourceBookmarks = SimpleBookmark.getBookmarkList(reader);
        if (sourceBookmarks != null) {
            if (pageOffset != 0) {
                SimpleBookmark.shiftPageNumbersInRange(sourceBookmarks, pageOffset, null);
            }
            bookmarks.addAll(sourceBookmarks);
        }
    }

    private PdfCopy initializeDocument(PdfReader reader, File destinationFile) throws IOException {
        Document document = new Document(reader.getPageSizeWithRotation(1));
        FileOutputStream fos = new FileOutputStream(destinationFile);
        document.open();
        return new PdfCopy(document, fos);
    }

    private void addPages(PdfReader reader, int numberOfPages, PdfCopy writer) throws IOException {
        PdfImportedPage page;
        for (int p = 0; p < numberOfPages; p++) {
            page = writer.getImportedPage(reader, p + 1);
            writer.addPage(page);
        }
    }

    private void logPageInfo(File sourceFile, int numberOfPages) {
        String message = "There are " + numberOfPages + " pages in " + sourceFile;
        logger.info(message);
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
