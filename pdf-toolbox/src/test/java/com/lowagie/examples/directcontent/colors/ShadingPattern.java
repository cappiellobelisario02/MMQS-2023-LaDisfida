/*
 * $Id: ShadingPattern.java 3838 2009-04-07 18:34:15Z mstorer $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */
package com.lowagie.examples.directcontent.colors;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColorPair;
import com.lowagie.text.pdf.Coordinates;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfShading;
import com.lowagie.text.pdf.PdfShadingPattern;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RadialCoordinates;
import java.awt.Color;
import java.io.FileOutputStream;
import java.util.logging.Logger;

/**
 * Shading example
 */
public class ShadingPattern {

    private static final Logger logger = Logger.getLogger(ShadingPattern.class.getName());

    /**
     * Shading example.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        System.out.println("Shading pattern");
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("shading_pattern.pdf"));
            document.open();

            ColorPair colors = new ColorPair(Color.red, Color.cyan);
            Coordinates coords = new Coordinates(100, 100, 400, 100);
            PdfShading shading = PdfShading.simpleAxial(writer, coords, colors);
            PdfShadingPattern shadingPattern = new PdfShadingPattern(shading);
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf = BaseFont.createFont(BaseFont.TIMES_BOLD, BaseFont.WINANSI, false);
            cb.setShadingFill(shadingPattern);
            cb.beginText();
            cb.setTextMatrix(100, 100);
            cb.setFontAndSize(bf, 40);
            cb.showText("Look at this text!");
            cb.endText();
            RadialCoordinates rCoords = new RadialCoordinates(200, 500, 50, 300, 500, 100);
            ColorPair rColors = new ColorPair(new Color(255, 247, 148), new Color(247, 138, 107));
            PdfShading shadingR = PdfShading.simpleRadial(writer, rCoords, rColors, false, false);
            cb.paintShading(shadingR);
            cb.sanityCheck();
            document.close();
        } catch (Exception de) {
            logger.severe("Exception occured");
        }
    }
}
