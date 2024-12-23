/*
 * $Id: TableBorders.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.examples.objects.tables;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.awt.Color;
import java.util.logging.Logger;

/**
 * Demonstrates different borderstyles.
 */
public class TableBorders {

    private static final Logger logger = Logger.getLogger(TableBorders.class.getName());

    /**
     * Demonstrates different borderstyles.
     *
     * @param args the number of rows for each table fragment.
     */
    public static void main(String[] args) {

        logger.info("Table Borders");
        // step1

        try (Document document = new Document(PageSize.A4, 50, 50, 50, 50)){
            // step2
            document.open();
            // step4

            // page 1
            Font tableFont = FontFactory.getFont("Helvetica", 8, Font.BOLD, Color.BLACK);
            float padding = 0f;
            Rectangle border = new Rectangle(0f, 0f);
            border.setBorderWidthLeft(6f);
            border.setBorderWidthBottom(5f);
            border.setBorderWidthRight(4f);
            border.setBorderWidthTop(2f);
            border.setBorderColorLeft(Color.RED);
            border.setBorderColorBottom(Color.ORANGE);
            border.setBorderColorRight(Color.YELLOW);
            border.setBorderColorTop(Color.GREEN);
            makeTestPage(tableFont, document, padding, true, true);
            Font font = FontFactory.getFont("Helvetica", 10);
            Paragraph p;
            p = new Paragraph("\nVarious border widths and colors\nuseAscender=true, useDescender=true", font);
            document.add(p);

            document.newPage();

            // page 2
            padding = 2f;
            border = new Rectangle(0f, 0f);
            border.setBorderWidthLeft(1f);
            border.setBorderWidthBottom(2f);
            border.setBorderWidthRight(1f);
            border.setBorderWidthTop(2f);
            border.setBorderColor(Color.BLACK);
            makeTestPage(tableFont, document, padding, true, true);
            p = new Paragraph(
                    "More typical use - padding of 2\nuseBorderPadding=true, useAscender=true, useDescender=true",
                    font);
            document.add(p);

            document.newPage();

            // page 3
            padding = 0f;
            border = new Rectangle(0f, 0f);
            border.setBorderWidthLeft(1f);
            border.setBorderWidthBottom(2f);
            border.setBorderWidthRight(1f);
            border.setBorderWidthTop(2f);
            border.setBorderColor(Color.BLACK);
            makeTestPage(tableFont, document, padding, false, true);
            p = new Paragraph("\nuseBorderPadding=true, useAscender=false, useDescender=true", font);
            document.add(p);

            document.newPage();

            // page 4
            padding = 0f;
            border = new Rectangle(0f, 0f);
            border.setBorderWidthLeft(1f);
            border.setBorderWidthBottom(2f);
            border.setBorderWidthRight(1f);
            border.setBorderWidthTop(2f);
            border.setBorderColor(Color.BLACK);
            makeTestPage(tableFont, document, padding, false, false);
            p = new Paragraph("\nuseBorderPadding=true, useAscender=false, useDescender=false", font);
            document.add(p);

            document.newPage();

            // page 5
            padding = 0f;
            border = new Rectangle(0f, 0f);
            border.setBorderWidthLeft(1f);
            border.setBorderWidthBottom(2f);
            border.setBorderWidthRight(1f);
            border.setBorderWidthTop(2f);
            border.setBorderColor(Color.BLACK);
            makeTestPage(tableFont, document, padding, true, false);
            p = new Paragraph("\nuseBorderPadding=true, useAscender=true, useDescender=false", font);
            document.add(p);
        } catch (DocumentException de) {
            logger.severe("Exception occured");
        }
    }

    private static void makeTestPage(Font tableFont, Document document,
            float padding, boolean ascender, boolean descender) throws DocumentException {
        document.newPage();
        PdfPTable table;
        table = new PdfPTable(4);
        table.setWidthPercentage(100f);

        float leading = tableFont.getSize() * 1.2f;

        table.addCell(
                makeCell("1-Top", Element.ALIGN_TOP, tableFont, leading, padding, ascender,
                        descender));
        table.addCell(
                makeCell("2-Middle", Element.ALIGN_MIDDLE, tableFont, leading, padding,
                        ascender, descender));
        table.addCell(
                makeCell("3-Bottom", Element.ALIGN_BOTTOM, tableFont, leading, padding,
                        ascender, descender));
        table.addCell(makeCell("4-Has a y", Element.ALIGN_TOP, tableFont, leading, padding,
                ascender, descender));

        table.addCell(makeCell("5-Abcdy", Element.ALIGN_TOP, tableFont, leading, padding,
                ascender, descender));
        table.addCell(
                makeCell("6-Abcdy", Element.ALIGN_MIDDLE, tableFont, leading, padding,
                        ascender, descender));
        table.addCell(
                makeCell("7-Abcdy", Element.ALIGN_BOTTOM, tableFont, leading, padding,
                        ascender, descender));
        table.addCell(
                makeCell("8-This\nis\na little\ntaller", Element.ALIGN_TOP, tableFont, leading,
                        padding, ascender, descender));
        document.add(table);
    }

    private static PdfPCell makeCell(String text, int vAlignment, Font font, float leading,
            float padding, boolean ascender, boolean descender) {
        Paragraph p = new Paragraph(text, font);
        p.setLeading(leading);

        PdfPCell cell = new PdfPCell(p);
        cell.setLeading(leading, 0);
        cell.setVerticalAlignment(vAlignment);
        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
        cell.setUseAscender(ascender);
        cell.setUseDescender(descender);
        cell.setUseBorderPadding(true);
        cell.setPadding(padding);
        return cell;
    }
}
