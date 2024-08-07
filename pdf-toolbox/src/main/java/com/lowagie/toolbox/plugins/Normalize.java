/*
 * $Id: Normalize.java 3736 2009-02-26 08:52:21Z xlv $
 *
 * Copyright 2005 by Carsten Hammer.
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
package com.lowagie.toolbox.plugins;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfIndirectReference;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;

/**
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Normalize
        extends AbstractTool {

    private static final Logger logger = Logger.getLogger(Normalize.class.getName());
    public static final String SRCFILE = "srcfile";
    public static final String DESTFILE = "destfile";

    static {
        addVersion("$Id: Normalize.java 3736 2009-02-26 08:52:21Z xlv $");
    }

    FileArgument destinationfile = null;
    int pagecount;
    float width;
    float height;
    PdfDictionary lastpage = null;
    float tolerancex = 60;
    float tolerancey = 60;
    int pagecountinsertedpages;
    int pagecountrotatedpages;

    /**
     * Constructs a Burst object.
     */
    public Normalize() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        FileArgument inputfile = new FileArgument(this, SRCFILE,
                "The file you want to normalize", false,
                new PdfFilter());
        arguments.add(inputfile);
        destinationfile = new FileArgument(this, DESTFILE, "The resulting PDF", true,
                new PdfFilter());
        arguments.add(destinationfile);
        inputfile.addPropertyChangeListener(destinationfile);
    }

    /**
     * Normalize PDF file.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        Normalize tool = new Normalize();
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
        internalFrame = new JInternalFrame("Normalize", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== Normalize OPENED ===");
    }

    protected void iteratePages(PdfDictionary page, PdfReader pdfreader,
            ArrayList<PdfDictionary> pageInh,
            int countInLeaf, PdfWriter writer) throws
            IOException {
        String stringToLog = null;
        float curwidth;
        float curheight;
        PdfArray kidsPR = page.getAsArray(PdfName.KIDS);

        if (kidsPR == null) {
            PdfArray arr = page.getAsArray(PdfName.MEDIABOX);
            curwidth = Float.parseFloat(arr.getPdfObject(2).toString());
            curheight = Float.parseFloat(arr.getPdfObject(3).toString());

            PdfNumber rotation = page.getAsNumber(PdfName.ROTATE);

            if (rotation == null) {
                logger.info("optional rotation missing");
                rotation = new PdfNumber(0);
            }

            Ausrichtung ausr = new Ausrichtung(rotation.floatValue(),
                    new Rectangle(curwidth, curheight));

            if (ausr.type == Ausrichtung.A_4_LANDSCAPE ||
                    ausr.type == Ausrichtung.A_3_PORTRAIT) {
                ausr.rotate();
                page.put(PdfName.ROTATE, new PdfNumber(ausr.getRotation()));
                stringToLog = "rotate page:" + (pagecount + 1) + " targetformat: " +
                        ausr;
                logger.info(stringToLog);
                this.pagecountrotatedpages++;
            }

            curwidth = ausr.getM5();
            curheight = ausr.getM6();

            if (((pagecount + 1) % 2) == 0 && ((Math.abs(curwidth - width) > tolerancex) ||
                    (Math.abs(curheight - height) > tolerancey))) {
                seitehinzufuegen(page, countInLeaf, writer, arr);
                this.pagecountinsertedpages++;

            }

            /**
             * Bei ungeraden Seiten die Seitenabmessungen speichern
             */
            if (((pagecount + 1) % 2) == 1) {
                width = curwidth;
                height = curheight;
                lastpage = page;
            }

            pageInh.add(pagecount, page);
            pagecount++;
        } else {
            page.put(PdfName.TYPE, PdfName.PAGES);

            for (int k = 0; k < kidsPR.size(); ++k) {
                PdfDictionary kid = kidsPR.getAsDict(k);
                iteratePages(kid, pdfreader, pageInh, k, writer);
            }
        }
    }

    private void seitehinzufuegen(PdfDictionary page, int countInLeaf,
            PdfWriter writer,
            PdfArray array) throws IOException {
        String stringToLog = null;
        logger.info("change!");

        PdfDictionary parent = page.getAsDict(PdfName.PARENT);
        PdfArray kids = parent.getAsArray(PdfName.KIDS);
        PdfIndirectReference ref = writer.getPdfIndirectReference();
        kids.add(countInLeaf, ref);

        PdfDictionary newPage = new PdfDictionary(PdfName.PAGE);
        newPage.merge(lastpage);
        newPage.remove(PdfName.CONTENTS);
        newPage.remove(PdfName.ANNOTS);
        newPage.put(PdfName.RESOURCES, new PdfDictionary());
        writer.addToBody(newPage, ref);

        PdfNumber count = null;

        while (parent != null) {
            count = parent.getAsNumber(PdfName.COUNT);

            parent.put(PdfName.COUNT, new PdfNumber(count.intValue() + 1));
            parent = parent.getAsDict(PdfName.PARENT);
        }
        stringToLog = "page:" + (pagecount + 1) + " nr in leaf:" +
                countInLeaf + " arl x:" +
                array.getPdfObject(0) + " y:" + array.getPdfObject(1) + " width:" + array.getPdfObject(2) +
                " height:" + array.getPdfObject(3);
        logger.info(stringToLog);
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        PdfReader reader = null;
        FileOutputStream fouts = null;
        PdfStamper stp = null;
        String stringToLog = null;
        try {
            if (getValue(SRCFILE) == null) {
                throw new InstantiationException("You need to choose a sourcefile");
            }
            File src = (File) getValue(SRCFILE);
            if (getValue(DESTFILE) == null) {
                throw new InstantiationException(
                        "You need to choose a destination file");
            }
            File dest = (File) getValue(DESTFILE);

            pagecountinsertedpages = 0;
            pagecountrotatedpages = 0;
            pagecount = 0;
            /*PdfReader reader*/ reader = new PdfReader(src.getAbsolutePath());
            fouts = new FileOutputStream(dest);
            /*PdfStamper stp*/ stp = new PdfStamper(reader, fouts);

            PdfWriter writer = stp.getWriter();

            ArrayList<PdfDictionary> pageInh = new ArrayList<>();
            PdfDictionary catalog = reader.getCatalog();
            PdfDictionary rootPages = catalog.getAsDict(PdfName.PAGES);
            iteratePages(rootPages, reader, pageInh, 0, writer);

            if (((pagecount) % 2) == 1) {
                appendemptypageatend(reader, writer);
                this.pagecountinsertedpages++;
            }

            stp.close();
            stringToLog = "In " + dest.getAbsolutePath() + " pages= " +
                    pagecount +
                    " inserted pages=" + this.getPagecountinsertedpages() +
                    " rotated pages=" +
                    this.getPagecountrotatedpages();
            logger.info(stringToLog);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An exception occurred ", e);
        } finally {
            if (reader != null && fouts != null && stp != null) {
                try {
                    reader.close();
                    fouts.close();
                    stp.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void appendemptypageatend(PdfReader reader, PdfWriter writer) throws
            IOException {
        logger.info("last page odd. add page!");

        PdfDictionary page = reader.getPageN(reader.getNumberOfPages());
        PdfDictionary parent = page.getAsDict(PdfName.PARENT);
        PdfArray kids = parent.getAsArray(PdfName.KIDS);
        PdfIndirectReference ref = writer.getPdfIndirectReference();
        kids.add(ref);

        PdfDictionary newPage = new PdfDictionary(PdfName.PAGE);
        newPage.merge(lastpage);
        newPage.remove(PdfName.CONTENTS);
        newPage.remove(PdfName.ANNOTS);
        newPage.put(PdfName.RESOURCES, new PdfDictionary());
        writer.addToBody(newPage, ref);

        PdfNumber count = null;

        while (parent != null) {
            count = parent.getAsNumber(PdfName.COUNT);
            parent.put(PdfName.COUNT, new PdfNumber(count.intValue() + 1));
            parent = parent.getAsDict(PdfName.PARENT);
        }
    }

    public int getPagecountinsertedpages() {
        return pagecountinsertedpages;
    }

    public int getPagecountrotatedpages() {
        return pagecountrotatedpages;
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
        // represent the changes of the argument in the internal frame
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

    public class Ausrichtung {

        static final float TOLERANCE = 60;
        static final int UNKNOWN = 0;
        static final int A_4_PORTRAIT = 1;
        static final int A_4_LANDSCAPE = 2;
        static final int A_3_PORTRAIT = 3;
        static final int A_3_LANDSCAPE = 4;
        float rotation;
        Rectangle rect;
        float m5;
        float m6;
        int type;

        public Ausrichtung() {
            this(0, new Rectangle(1, 1));
        }

        public Ausrichtung(float rotation, Rectangle unrotatedoriginalrect) {
            this.rotation = rotation;
            if ((rotation == 90) || (rotation == 270)) {
                rect = unrotatedoriginalrect.rotate();
            } else {
                rect = unrotatedoriginalrect;
            }

            m5 = rect.getWidth();
            m6 = rect.getHeight();
            klassifiziere();

        }

        private void klassifiziere() {
            if (Math.abs(rect.getWidth() - 595) < TOLERANCE &&
                    Math.abs(rect.getHeight() - 842) < TOLERANCE) {
                this.type = A_4_PORTRAIT;
            } else if (Math.abs(rect.getWidth() - 842) < TOLERANCE &&
                    Math.abs(rect.getHeight() - 595) < TOLERANCE) {
                this.type = A_4_LANDSCAPE;
            } else if (Math.abs(rect.getWidth() - 1190) < TOLERANCE &&
                    Math.abs(rect.getHeight() - 842) < TOLERANCE) {
                this.type = A_3_LANDSCAPE;
            } else if (Math.abs(rect.getWidth() - 842) < TOLERANCE &&
                    Math.abs(rect.getHeight() - 1190) < TOLERANCE) {
                this.type = A_3_PORTRAIT;
            } else {
                type = UNKNOWN;
            }
        }

        public float getM5() {
            return m5;
        }

        public float getM6() {
            return m6;
        }

        public String toString() {
            String back;
            switch (type) {
                case UNKNOWN:
                    back = rect.getWidth() + "*" + rect.getHeight();
                    break;
                case A_3_LANDSCAPE:
                    back = "A3 Landscape";
                    break;
                case A_3_PORTRAIT:
                    back = "A3 Portrait";
                    break;
                case A_4_LANDSCAPE:
                    back = "A4 Landscape";
                    break;
                case A_4_PORTRAIT:
                    back = "A4 Portrait";
                    break;
                default:
                    back = "";
            }
            return back;
        }

        public void rotate() {
            rect = rect.rotate();
            m5 = rect.getWidth();
            m6 = rect.getHeight();
            rotation += 90;
            rotation = rotation % 360;
            klassifiziere();
        }

        public float getRotation() {
            return rotation;
        }
    }

}
