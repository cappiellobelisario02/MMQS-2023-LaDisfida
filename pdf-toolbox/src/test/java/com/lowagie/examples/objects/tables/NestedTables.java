/*
 * $Id: NestedTables.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Demonstrates the use of nested tables.
 */
public class NestedTables {

    private static final Logger logger = Logger.getLogger(NestedTables.class.getName());

    /**
     * Using nested tables.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {

        System.out.println("Nested Tables");
        // step1
        Document document = new Document(PageSize.A4.rotate(), 10, 10, 10, 10);
        try {
            // step2
            PdfWriter.getInstance(document,
                    new FileOutputStream("NestedTables.pdf"));
            // step3
            document.open();
            // step4
            PdfPTable table = getPdfPTable();
            document.add(table);
            // step 5: we close the document
            document.close();
        } catch (IOException | DocumentException de) {
            logger.severe("Exception occured");
        }
        // step5
        document.close();
    }

    private static PdfPTable getPdfPTable() {
        PdfPTable table = new PdfPTable(4);
        PdfPTable nested1 = new PdfPTable(2);
        nested1.addCell("1.1");
        nested1.addCell("1.2");
        PdfPTable nested2 = new PdfPTable(1);
        nested2.addCell("2.1");
        nested2.addCell("2.2");
        for (int k = 0; k < 24; ++k) {
            if (k == 1) {
                table.addCell(nested1);
            } else if (k == 20) {
                table.addCell(nested2);
            } else {
                table.addCell("cell " + k);
            }
        }
        return table;
    }
}
