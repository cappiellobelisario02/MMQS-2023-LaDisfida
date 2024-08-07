/*
 * $Id: ReversePages.java 3271 2008-04-18 20:39:42Z xlv $
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
import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;

/**
 * This tool lets you take pages from an existing PDF and copy them in reverse order into a new PDF.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class ReversePages
        extends AbstractTool {

    public static final Logger logger = Logger.getLogger(ReversePages.class.getName());
    public static final String SRCFILE = "srcfile";
    public static final String DESTFILE = "destfile";

    static {
        addVersion(
                "$Id: ReversePages.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    FileArgument destinationfile = null;

    /**
     * Constructs a ReversePages object.
     */
    public ReversePages() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        FileArgument inputfile = null;
        inputfile = new FileArgument(this, SRCFILE,
                "The file you want to reorder", false,
                new PdfFilter());
        arguments.add(inputfile);
        destinationfile = new FileArgument(this, DESTFILE,
                "The file to which the reordered version of the original PDF has to be written", true,
                new PdfFilter());
        arguments.add(destinationfile);
        inputfile.addPropertyChangeListener(destinationfile);
    }

    /**
     * Take pages from an existing PDF and copy them in reverse order into a new PDF.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        ReversePages tool = new ReversePages();
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
        internalFrame = new JInternalFrame("ReversePages", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== ReversePages OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        PdfReader reader = null;
        Document document = null;
        PdfCopy copy = null;
        FileOutputStream fouts = null;
        String stringToLog = null;

        try {
            if (getValue(SRCFILE) == null) {
                throw new InstantiationException("You need to choose a sourcefile");
            }
            File src = (File) getValue(SRCFILE);
            if (getValue(DESTFILE) == null) {
                throw new InstantiationException("You need to choose a destination file");
            }
            File dest = (File) getValue(DESTFILE);

            // We create a reader for a certain document
            try {
                reader = new PdfReader(src.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }

            stringToLog = "The original file had " + reader.getNumberOfPages() + " pages.";
            logger.info(stringToLog);

            int pages = reader.getNumberOfPages();
            ArrayList<Integer> li = new ArrayList<>();

            for (int i = pages; i > 0; i--) {
                li.add(i);
            }

            reader.selectPages(li);

            stringToLog = "The new file has " + pages + " pages.";
            logger.severe(stringToLog);

            try {
                document = new Document(reader.getPageSizeWithRotation(1));
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                fouts = new FileOutputStream(dest.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                copy = new PdfCopy(document, fouts);
            } catch (Exception e) {
                e.printStackTrace();
            }

            document.open();
            PdfImportedPage page;

            for (int i = 1; i <= pages; i++) {  // Start loop from 1 to pages inclusive
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

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (document != null) {
                try {
                    document.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (copy != null) {
                try {
                    copy.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (fouts != null) {
                try {
                    fouts.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    /**
     * @param arg StringArgument
     * @see com.lowagie.toolbox.AbstractTool#valueHasChanged(com.lowagie.toolbox.arguments.AbstractArgument)
     */
    public void valueHasChanged(AbstractArgument arg) {
        if (internalFrame == null) {
            // if the internal frame is null, the tool was called from the command line
            return;
        }

        if (destinationfile.getValue() == null && arg.getName().equalsIgnoreCase(SRCFILE)) {
            String filename = arg.getValue().toString();
            String filenameout = filename.substring(0, filename.indexOf(".",
                    filename.length() - 4)) + "_out.pdf";
            destinationfile.setValue(filenameout);
        }
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
