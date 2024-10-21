/*
 * $Id: PhotoAlbum.java 3451 2008-05-26 02:56:13Z xlv $
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
 * and Carsten Hammer. The introduction of the DirFilter by Johannes
 * Schindelin had an impact on this class. Darryl Miles suggested a bugfix
 * that was implemented by Bruno Lowagie.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import com.lowagie.text.pdf.PdfPageLabels;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.filters.DirFilter;
import com.lowagie.rups.io.filters.PdfFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * Converts the image files in a directory to a PDF file that acts as a photo album.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class PhotoAlbum extends AbstractTool {

    public static final Logger logger = Logger.getLogger(PhotoAlbum.class.getName());
    public static final String SRCDIR = "srcdir";
    public static final String DESTFILE = "destfile";

    static {
        addVersion("$Id: PhotoAlbum.java 3451 2008-05-26 02:56:13Z xlv $");
    }

    /**
     * Constructs a PhotoAlbum object.
     */
    public PhotoAlbum() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        arguments.add(new FileArgument(this, SRCDIR,
                "The directory containing the image files", false,
                new DirFilter()));
        arguments.add(new FileArgument(this, DESTFILE,
                "The file to which the converted TIFF has to be written", true,
                new PdfFilter()));
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("PhotoAlbum", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== PhotoAlbum OPENED ===");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        try {
            // Validate source directory
            if (getValue(SRCDIR) == null) {
                throw new InstantiationException("You need to choose a source directory");
            }

            File directory = (File) getValue(SRCDIR);
            if (directory.isFile()) {
                directory = directory.getParentFile();
            }

            // Validate destination file
            if (getValue(DESTFILE) == null) {
                throw new InstantiationException("You need to choose a destination file");
            }

            File pdfFile = (File) getValue(DESTFILE);
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            writer.setViewerPreferences(PdfWriter.PAGE_MODE_USE_THUMBS);
            PdfPageLabels pageLabels = new PdfPageLabels();

            // Collect images
            TreeSet<File> images = new TreeSet<>();
            File[] files = directory.listFiles();
            if (files == null) {
                throw new NullPointerException("listFiles() returns null");
            }
            for (File file : files) {
                if (file.isFile()) {
                    images.add(file);
                }
            }

            // Process each image
            for (File image : images) {
                processImage(image, document, writer, pageLabels);
            }

            // Finalize the document
            if (document.isOpen()) {
                writer.setPageLabels(pageLabels);
                document.close();
            } else {
                logger.severe("No images were found in directory " + directory.getAbsolutePath());
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(internalFrame, e.getMessage(), e.getClass().getName(), JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "An unexpected error occurred during execution.", e);
        }
    }

    private void processImage(File image, Document document, PdfWriter writer, PdfPageLabels pageLabels) {
        try {
            logger.info("Testing image: " + image.getName());

            // Get the image and set its properties
            Image img = Image.getInstance(image.getAbsolutePath());
            String caption = "";
            int dpiX = img.getDpiX();
            if (dpiX == 0) {
                dpiX = 72;
            }
            int dpiY = img.getDpiY();
            if (dpiY == 0) {
                dpiY = 72;
            }
            float imgWidthPica = (72 * img.getPlainWidth()) / dpiX;
            float imgHeightPica = (72 * img.getPlainHeight()) / dpiY;
            img.scaleAbsolute(imgWidthPica, imgHeightPica);
            document.setPageSize(new Rectangle(imgWidthPica, imgHeightPica));

            // Open a new page in the document if it is open; otherwise, open the document
            if (document.isOpen()) {
                document.newPage();
            } else {
                document.open();
            }

            img.setAbsolutePosition(0, 0);
            document.add(img);

            // Add a caption to the image
            BaseFont bf = BaseFont.createFont("Helvetica", BaseFont.WINANSI, false);
            PdfGState gs1 = new PdfGState();
            gs1.setBlendMode(PdfGState.BM_OVERLAY);
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            cb.setGState(gs1);
            cb.beginText();
            cb.setFontAndSize(bf, 40);
            cb.setTextMatrix(50, 50);
            cb.showText(caption);
            cb.endText();
            cb.restoreState();

            // Add page labels
            String label = image.getName();
            if (label.lastIndexOf('.') > 0) {
                label = label.substring(0, label.lastIndexOf('.'));
            }
            pageLabels.addPageLabel(writer.getPageNumber(), PdfPageLabels.EMPTY, label);

            logger.info("Added image: " + image.getName());
        } catch (Exception e) {
            // Log a generic error without revealing specific details
            logger.severe("An error occurred while processing the image: " + image.getName());
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
}
