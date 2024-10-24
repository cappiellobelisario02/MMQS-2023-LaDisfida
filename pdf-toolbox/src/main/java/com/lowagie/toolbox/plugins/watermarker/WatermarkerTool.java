/*
 * $Id: Watermarker.java 3271 2008-04-18 20:39:42Z xlv $
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

package com.lowagie.toolbox.plugins.watermarker;

import static java.awt.Color.BLACK;
import static java.awt.Color.decode;
import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;

import com.lowagie.rups.io.filters.PdfFilter;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.FloatArgument;
import com.lowagie.toolbox.arguments.IntegerArgument;
import com.lowagie.toolbox.arguments.StringArgument;
import org.apache.fop.pdf.PDFFilterException;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * This tool lets you add a text watermark to all pages of a document.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class WatermarkerTool extends AbstractTool {

    public static final Logger logger = Logger.getLogger(WatermarkerTool.class.getName());
    public static final String SRCFILE = "srcfile";
    public static final String WATERMARK = "watermark";
    public static final String FONTSIZE = "fontsize";
    public static final String OPACITY = "opacity";
    public static final String DESTFILE = "destfile";
    public static final String COLOR = "color";

    FileArgument destfileFileArgument;

    /**
     * This tool lets you add a text watermark to all pages of a document.
     */
    public WatermarkerTool() {
        super();
        FileArgument inputfile = new FileArgument(this, SRCFILE,
                "The file you want to watermark", false, new PdfFilter());
        arguments.add(inputfile);
        arguments.add(new StringArgument(this, WATERMARK,
                "The text that can be used as watermark"));
        arguments.add(new IntegerArgument(this, FONTSIZE,
                "The fontsize of the watermark text"));
        arguments.add(new FloatArgument(this, OPACITY,
                "The opacity of the watermark text"));
        destfileFileArgument = new FileArgument(this, DESTFILE,
                "The file to which the watermarked PDF has to be written",
                true, new PdfFilter());
        arguments.add(destfileFileArgument);
        arguments.add(new StringArgument(this, COLOR,
                "The color of the watermark text"));
        inputfile.addPropertyChangeListener(destfileFileArgument);
    }

    /**
     * These methods help you run this tool as a standalone application.
     *
     * <p>
     * Call it like this from command line: java com.lowagie.tools.plugins.Watermarker input.pdf Draft 230 0.2
     * output.pdf #FF000000
     *
     * <p>
     * "input.pdf" is the input file getName to be processed
     * <p>
     * "Draft" is the text written as transparent "watermark" on top of each page
     * <p>
     * "230" is the font size
     * <p>
     * "0.2" is the opacity (1.0 completely opaque, 0.0 completely transparent)
     * <p>
     * "output.pdf" is the output file getName
     * <p>
     * (Optional) "#FF0000" is the color (in hex format like #nnnnnn or 0xnnnnnn), #000000 (black) by default
     *
     * <p>
     */
    public static void main(String[] args) {
        // Check if the number of arguments is valid
        if (args.length < 5 || args.length > 6) {
            logger.info("Usage: java com.lowagie.tools.plugins.Watermarker <input.pdf> <watermarkText> <fontSize> "
                    + "<opacity> <output.pdf> [<colorHex>]");
            return;
        }

        try {
            // Parse the input arguments
            String inputPdf = args[0];
            String watermarkText = args[1];
            int fontSize = Integer.parseInt(args[2]);
            float opacity = Float.parseFloat(args[3]);
            String outputPdf = args[4];
            String colorHex = (args.length == 6) ? args[5] : "#000000";  // Default to black if not provided

            // Convert hex color string to Color object
            Color watermarkColor;
            if (colorHex.startsWith("#")) {
                watermarkColor = Color.decode(colorHex);
            } else {
                throw new IllegalArgumentException("Invalid color format. Use hex format like #FF0000.");
            }

            // Read the input PDF and apply watermark
            Path pathToFile = Paths.get(inputPdf);
            byte[] pdfBytes = Files.readAllBytes(pathToFile);
            Watermarker watermarker = new Watermarker(pdfBytes, watermarkText, fontSize, opacity)
                    .withColor(watermarkColor);

            // Generate the watermarked PDF
            byte[] watermarkedPdf = watermarker.write();
            Files.write(Paths.get(outputPdf), watermarkedPdf);

            String msg = "Watermarked PDF created successfully: " + outputPdf;
            logger.info(msg);
        } catch (IOException e) {
            logger.severe("An error occurred");
        }
    }

    /**
     * Creates the internal frame.
     */
    @Override
    protected void createFrame() {
        internalFrame = new JInternalFrame("Watermark", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        logger.info("=== Watermark OPENED ===");
    }

    /**
     * Executes the tool (in most cases this generates a PDF file).
     */
    @Override
    public void execute() {
        try (PdfReader reader = new PdfReader(((File) getValue(SRCFILE)).getAbsolutePath());
                FileOutputStream fouts = new FileOutputStream((File) getValue(DESTFILE));
                PdfStamper stamp = new PdfStamper(reader, fouts)
        ){
            if (getValue(SRCFILE) == null) {
                throw new InstantiationException(
                        "You need to choose a sourcefile");
            }
            if (getValue(DESTFILE) == null) {
                throw new InstantiationException(
                        "You need to choose a destination file");
            }
            if (getValue(WATERMARK) == null) {
                throw new InstantiationException(
                        "You need to add a text for the watermark");
            }
            if (getValue(FONTSIZE) == null) {
                throw new InstantiationException(
                        "You need to add a fontsize for the watermark");
            }
            if (getValue(OPACITY) == null) {
                throw new InstantiationException(
                        "You need to add a opacity for the watermark");
            }

            Color color = BLACK;
            if (getValue(COLOR) != null) {
                color = decode((String) getValue(COLOR));
            }

            String text = (String) getValue(WATERMARK);
            int fontsize = parseInt((String) getValue(FONTSIZE));
            float opacity = parseFloat((String) getValue(OPACITY));

            Writer writer = new Writer(reader, stamp, text, fontsize, opacity, color);
            writer.write();
        } catch (IOException | InstantiationException | PDFFilterException | NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(internalFrame, e.getMessage(), e
                    .getClass().getName(), JOptionPane.ERROR_MESSAGE);
            logger.log(Level.SEVERE, "An error occurred during execution.", e);
        }
    }

    /**
     * Gets the PDF file that should be generated (or null if the output isn't a PDF file).
     *
     * @return the PDF file that should be generated
     * @throws InstantiationException on error
     */
    @Override
    protected File getDestPathPDF() throws InstantiationException {
        return (File) getValue(DESTFILE);
    }

    /**
     * Indicates that the value of an argument has changed.
     *
     * @param arg the argument that has changed
     */
    @Override
    public void valueHasChanged(AbstractArgument arg) {
        checkInternalFrame(arg, internalFrame == null, destfileFileArgument, SRCFILE);
    }

    public static void checkInternalFrame(AbstractArgument arg, boolean b, FileArgument destfileFileArgument,
            String srcfile) {
        if (b) {
            // if the internal frame is null, the tool was called from the
            // command line
            return;
        }
        if (destfileFileArgument.getValue() == null
                && arg.getName().equalsIgnoreCase(srcfile)) {
            String filename = arg.getValue().toString();
            String filenameout = filename.substring(0,
                    filename.indexOf(".", filename.length() - 4))
                    + "_out.pdf";
            destfileFileArgument.setValue(filenameout);
        }
    }

}
