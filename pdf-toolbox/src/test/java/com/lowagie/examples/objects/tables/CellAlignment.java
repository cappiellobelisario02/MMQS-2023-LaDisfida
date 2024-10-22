/*
 * $Id: CellAlignment.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.util.logging.Logger;

/**
 * Change the getAlignment of the contents of a PdfPCell.
 */
public class CellAlignment {

    private static final Logger logger = Logger.getLogger(CellAlignment.class.getName());

    /**
     * Changing the getAlignment
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("indentation - getAlignment");
        // step1
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
        try {
            // step2
            PdfWriter.getInstance(document,
                    new FileOutputStream("Alignment.pdf"));
            // step3
            document.open();
            // step4
            PdfPTable table = new PdfPTable(2);
            PdfPCell cell;
            Paragraph p = new Paragraph(
                    "Quick brown fox jumps over the lazy dog. Quick brown fox jumps over the lazy dog.");
            table.addCell("default getAlignment");
            cell = new PdfPCell(p);
            table.addCell(cell);
            table.addCell("centered getAlignment");
            cell = new PdfPCell(p);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
            table.addCell("right getAlignment");
            cell = new PdfPCell(p);
            cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell);
            table.addCell("justified getAlignment");
            cell = new PdfPCell(p);
            cell.setHorizontalAlignment(Element.ALIGN_JUSTIFIED);
            table.addCell(cell);
            table.addCell("blah\nblah\nblah\nblah\nblah\nblah\nblah\nblah\nblah\n");
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BASELINE);
            table.addCell("baseline");
            table.addCell("blah\nblah\nblah\nblah\nblah\nblah\nblah\nblah\nblah\n");
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_BOTTOM);
            table.addCell("bottom");
            table.addCell("blah\nblah\nblah\nblah\nblah\nblah\nblah\nblah\nblah\n");
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell("middle");
            table.addCell("blah\nblah\nblah\nblah\nblah\nblah\nblah\nblah\nblah\n");
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_TOP);
            table.addCell("top");
            document.add(table);
        } catch (Exception de) {
            logger.severe("Exception occured");
        }
        // step5
        document.close();
    }
}
