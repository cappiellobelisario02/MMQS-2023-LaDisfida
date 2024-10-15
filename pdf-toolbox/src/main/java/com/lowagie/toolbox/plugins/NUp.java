/*
 * $Id: NUp.java 3427 2008-05-24 18:32:31Z xlv $
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
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.OptionArgument;
import com.lowagie.rups.io.filters.PdfFilter;
import org.apache.fop.pdf.PDFFilterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;

/**
 * This tool lets you generate a PDF that shows N pages on 1.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class NUp extends AbstractTool {

    private static final Logger logger = Logger.getLogger(NUp.class.getName());
    public static final String SRCFILE = "srcfile";
    public static final String DESTFILE = "destfile";

    static {
        addVersion("$Id: NUp.java 3427 2008-05-24 18:32:31Z xlv $");
    }

    /**
     * Constructs an NUp object.
     */
    public NUp() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        arguments.add(new FileArgument(this, SRCFILE, "The file you want to N-up", false, new PdfFilter()));
        arguments.add(new FileArgument(this, DESTFILE, "The resulting PDF", true, new PdfFilter()));
        OptionArgument oa = new OptionArgument(this, "pow2", "The number of pages you want to copy to 1 page");
        oa.addOption("2", "1");
        oa.addOption("4", "2");
        oa.addOption("8", "3");
        oa.addOption("16", "4");
        oa.addOption("32", "5");
        oa.addOption("64", "6");
        arguments.add(oa);
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("N-up", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== N-up OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        PdfReader reader = null;
        Document document = null;
        String stringToLog;
        try {
            if (getValue(SRCFILE) == null) {
                throw new InstantiationException("You need to choose a sourcefile");
            }
            File src = (File) getValue(SRCFILE);
            if (getValue(DESTFILE) == null) {
                throw new InstantiationException("You need to choose a destination file");
            }
            File dest = (File) getValue(DESTFILE);

            int pow2 = tryPowSquared();

            // we create a reader for a certain document
            /*PdfReader reader */ reader = new PdfReader(src.getAbsolutePath());
            // we retrieve the total number of pages and the page size
            int total = reader.getNumberOfPages();
            stringToLog = "There are " + total + " pages in the original file.";
            logger.info(stringToLog);
            Rectangle pageSize = reader.getPageSize(1);
            Rectangle newSize = (pow2 % 2) == 0 ? new Rectangle(pageSize.getWidth(), pageSize.getHeight())
                    : new Rectangle(pageSize.getHeight(), pageSize.getWidth());
            Rectangle unitSize = new Rectangle(pageSize.getWidth(), pageSize.getHeight());
            Rectangle currentSize;
            for (int i = 0; i < pow2; i++) {
                unitSize = new Rectangle(unitSize.getHeight() / 2, unitSize.getWidth());
            }
            int n = (int) Math.pow(2, pow2);
            int r = (int) Math.pow(2, (double) pow2 / 2);
            int c = n / r;
            // step 1: creation of a document-object
            /*Document document*/ document = new Document(newSize, 0, 0, 0, 0);
            // step 2: we create a writer that listens to the document
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest));
            // step 3: we open the document
            document.open();
            // step 4: adding the content
            PdfContentByte cb = writer.getDirectContent();
            PdfImportedPage page;
            float offsetX;
            float offsetY;
            float factor;
            int p;
            for (int i = 0; i < total; i++) {
                if (i % n == 0) {
                    document.newPage();
                }
                p = i + 1;
                offsetX = unitSize.getWidth() * ((i % n) % c);
                offsetY = newSize.getHeight() - (unitSize.getHeight() * (((float) (i % n) / c) + 1));
                currentSize = reader.getPageSize(p);
                factor = Math.min(unitSize.getWidth() / currentSize.getWidth(),
                        unitSize.getHeight() / currentSize.getHeight());
                offsetX += (unitSize.getWidth() - (currentSize.getWidth() * factor)) / 2f;
                offsetY += (unitSize.getHeight() - (currentSize.getHeight() * factor)) / 2f;
                page = writer.getImportedPage(reader, p);
                cb.addTemplate(page, factor, 0, 0, factor, offsetX, offsetY);
            }
            // step 5: we close the document
            document.close();
        } catch (IOException | InstantiationException | PDFFilterException e) {
            //da vedere come effettuare il log
        } finally {
            if (reader != null && document != null){
                try {
                    reader.close();
                    document.close();
                } catch (Exception e){
                    //da vedere come effettuare il log
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
        return (File) getValue(DESTFILE);
    }

    private int tryPowSquared(){
        int powSquared;
        try {
            powSquared = Integer.parseInt((String) getValue("pow2"));
        } catch (Exception e) {
            powSquared = 1;
        }
        return powSquared;
    }
}
