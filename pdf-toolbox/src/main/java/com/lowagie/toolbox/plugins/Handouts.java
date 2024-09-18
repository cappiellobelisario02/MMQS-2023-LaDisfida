/*
 * $Id: Handouts.java 3271 2008-04-18 20:39:42Z xlv $
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
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BarcodeDatamatrix;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.OptionArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import org.apache.fop.pdf.PDFFilterException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * Generates a PDF file that is usable as Handout.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Handouts extends AbstractTool {

    static {
        addVersion("$Id: Handouts.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    static Logger logger = Logger.getLogger(Handouts.class.getName());

    /**
     * Constructs a Handouts object.
     */
    private static final String SRCFILE_ARGUMENT_NAME = "srcfile";
    private static final String DESTFILE = "destfile";
    public Handouts() {
        arguments.add(new FileArgument(this, SRCFILE_ARGUMENT_NAME, "The file you want to convert", false, new PdfFilter()));
        arguments.add(new FileArgument(this, DESTFILE, "The file to which the Handout has to be written", true,
                new PdfFilter()));
        OptionArgument oa = new OptionArgument(this, "pages", "The number of pages you want on one handout page");
        oa.addOption("2 pages on 1", "2");
        oa.addOption("3 pages on 1", "3");
        oa.addOption("4 pages on 1", "4");
        oa.addOption("5 pages on 1", "5");
        oa.addOption("6 pages on 1", "6");
        oa.addOption("7 pages on 1", "7");
        oa.addOption("8 pages on 1", "8");
        arguments.add(oa);
    }

    /**
     * Converts a PDF file to a PDF file usable as Handout.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        Handouts tool = new Handouts();
        if (args.length < 2) {
            logger.info(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     * @see AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Handouts", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== Handouts OPENED ===");
    }

    /**
     * @see AbstractTool#execute()
     */
    public void execute() {
        try {
            validateInputs();
            File src = (File) getValue(SRCFILE_ARGUMENT_NAME);
            File dest = (File) getValue(DESTFILE);
            int pages = tryParsingPagesNumber();

            float[] y1 = initializeYValues(pages);
            float[] y2 = calculateY2Values(pages, y1);

            processDocument(src, dest, pages, y1, y2);
        } catch (Exception e) {
            showError(e);
        }
    }

    private void validateInputs() throws InstantiationException {
        if (getValue(SRCFILE_ARGUMENT_NAME) == null) {
            throw new InstantiationException("You need to choose a sourcefile");
        }
        if (getValue(DESTFILE) == null) {
            throw new InstantiationException("You need to choose a destination file");
        }
    }

    private float[] initializeYValues(int pages) {
        float[] y1 = new float[pages];
        float height = (778f - (20f * (pages - 1))) / pages;
        y1[0] = 812f;
        for (int i = 1; i < pages; i++) {
            y1[i] = y1[i - 1] - height - 20f;
        }
        return y1;
    }

    private float[] calculateY2Values(int pages, float[] y1) {
        float[] y2 = new float[pages];
        float height = (778f - (20f * (pages - 1))) / pages;
        y2[0] = 812f - height;
        for (int i = 1; i < pages; i++) {
            y2[i] = y1[i] - height;
        }
        return y2;
    }

    private void processDocument(File src, File dest, int pages, float[] y1, float[] y2)
            throws IOException, PDFFilterException {
        PdfReader reader = new PdfReader(src.getAbsolutePath());
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(dest));
        document.open();
        PdfContentByte cb = writer.getDirectContent();
        int n = reader.getNumberOfPages();
        String stringToLog = "There are " + n + " pages in the original file.";
        logger.info(stringToLog);

        int i = 0;
        int p = 0;
        while (i < n) {
            processPage(reader, writer, cb, i + 1, y1, y2, p);
            i++;
            p = (p + 1) % pages;
            if (p == 0) {
                document.newPage();
            }
        }

        document.close();
        reader.close();
    }

    private void processPage(PdfReader reader, PdfWriter writer, PdfContentByte cb, int pageIndex, float[] y1, float[] y2, int p) {
        Rectangle rect = reader.getPageSizeWithRotation(pageIndex);
        float factor = calculateScalingFactor(rect, y1[p], y2[p]);
        float dx = calculateOffsetX(rect, factor);
        float dy = calculateOffsetY(rect, factor, y1[p], y2[p]);
        String stringToLog;

        PdfImportedPage page = writer.getImportedPage(reader, pageIndex);
        int rotation = reader.getPageRotation(pageIndex);
        float x1 = 0;
        float x2 = 0;
        addPageTemplate(cb, page, rotation, factor, dx, dy, x1, y2[p], rect);
        drawRectangle(cb, x1, y2[p], x2);
        stringToLog = "Processed page " + pageIndex;
        logger.info(stringToLog);
    }

    private float calculateScalingFactor(Rectangle rect, float y1, float y2) {
        float x2 = 0;
        float x1 = 0;
        float factorx = (x2 - x1) / rect.getWidth();
        float factory = (y1 - y2) / rect.getHeight();
        return Math.min(factorx, factory);
    }

    private float calculateOffsetX(Rectangle rect, float factor) {
        float factorx = 0;
        float x2 = 0;
        float x1 = 0;
        return (factorx == factor ? 0f : ((x2 - x1) - rect.getWidth() * factor) / 2f);
    }

    private float calculateOffsetY(Rectangle rect, float factor, float y1, float y2) {
        float factory = 0;
        return (factory == factor ? 0f : ((y1 - y2) - rect.getHeight() * factor) / 2f);
    }

    private void addPageTemplate(PdfContentByte cb, PdfImportedPage page, int rotation, float factor, float dx, float dy, float x1, float y2, Rectangle rect) {
        if (rotation == 90 || rotation == 270) {
            cb.addTemplate(page, 0, -factor, factor, 0, x1 + dx, y2 + dy + rect.getHeight() * factor);
        } else {
            cb.addTemplate(page, factor, 0, 0, factor, x1 + dx, y2 + dy);
        }
    }

    private void drawRectangle(PdfContentByte cb, float x1, float x3, float y1) {
        cb.setRGBColorStroke(0xC0, 0xC0, 0xC0);
        float x4 = 0;
        cb.rectangle(x3 - 5f, - 5f, x4 - x3 + 10f, y1  + 10f);
        for (float l = y1 - 19; l > 0; l -= 16) {
            cb.moveTo(x3, l);
            cb.lineTo(x4, l);
        }
        float dx = 0;
        float dy = 0;

        BarcodeDatamatrix rect = new BarcodeDatamatrix();
        int factor = 1;
        cb.rectangle(x1 + dx,  + dy, rect.getWidth() * (float) factor, rect.getHeight() * (float) factor);
        cb.stroke();
    }

    private void showError(Exception e) {
        JOptionPane.showMessageDialog(internalFrame,
                e.getMessage(),
                e.getClass().getName(),
                JOptionPane.ERROR_MESSAGE);
        logger.info(e.getMessage());
    }

    /**
     * @param arg StringArgument
     * @see AbstractTool#valueHasChanged(AbstractArgument)
     */
    public void valueHasChanged(AbstractArgument arg) {
        // represent the changes of the argument in the internal frame
    }

    /**
     * @return File
     * @throws InstantiationException on error
     * @see AbstractTool#getDestPathPDF()
     */
    protected File getDestPathPDF() throws InstantiationException {
        return (File) getValue(DESTFILE);
    }

    private int tryParsingPagesNumber() {
        int pagesToParse;
        try {
            pagesToParse = Integer.parseInt((String) getValue("pages"));
        } catch (Exception e) {
            pagesToParse = 4;
        }
        return pagesToParse;
    }
}
