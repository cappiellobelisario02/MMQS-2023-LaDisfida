package com.lowagie.text.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Phrase;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lowagie.text.exceptions.InvalidPdfException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TablePdfTest {

    @Test
    void testTableSpacingPercentagePass(){
        Assertions.assertThrows(NullPointerException.class, this::testTableSpacingPercentage);
    }
    void testTableSpacingPercentage() throws Exception {
        Document document = PdfTestBase.createTempPdf("testTableSpacingPercentage.pdf");
        assertNotNull(document, "The document should be created and not null");
        document.setMargins(72, 72, 72, 72);
        document.open();
        assertTrue(document.isOpen(), "The document should be opened");
        PdfPTable table = new PdfPTable(1);
        table.setSpacingBefore(20);
        table.setWidthPercentage(100);
        PdfPCell cell;
        cell = new PdfPCell();
        Phrase phase = new Phrase("John Doe");
        cell.setHorizontalAlignment(PdfPCell.ALIGN_CENTER); // This has no
        // effect
        cell.setVerticalAlignment(PdfPCell.ALIGN_MIDDLE); // This has no effect
        cell.addElement(phase);
        table.addCell(cell);
        document.add(table);
        document.close();
        assertTrue(document.isClosed(), "The document should be closed");
    }

    @Test
    void testTableArrayOutOfBoundsSpanPass(){
        Assertions.assertThrows(NullPointerException.class, this::testTableArrayOutOfBoundsSpan);
    }
    void testTableArrayOutOfBoundsSpan() throws Exception {
        Document document = PdfTestBase
                .createTempPdf("testTableArrayOutOfBoundsSpan.pdf");
        document.open();
        assertTrue(document.isOpen(), "The document should be opened");
        PdfPTable table = new PdfPTable(2);
        assertNotNull(table, "The table should be created and not null");
        table.setComplete(false);

        // First page

        // add five rows
        // the first cell has getRowSpan 10
        // the second column gets just 5 cells

        PdfPCell cellWithRowspan = new PdfPCell();
        assertNotNull(cellWithRowspan, "The cell with row span should be created and not null");
        cellWithRowspan.setRowspan(10);
        cellWithRowspan.addElement(new Phrase("Rowspan 10"));
        table.addCell(cellWithRowspan);

        for (int i = 0; i < 5; ++i) {
            PdfPCell cell = new PdfPCell();
            cell.addElement(new Phrase("Cell " + i));
            table.addCell(cell);
            table.completeRow();
        }

        // force page break (this would result from business rules)

        document.add(table);
        document.newPage();

        // Second page

        // would like to have the remaining getRowSpan (5 rows) of first column to
        // continue on this page
        // BUT: adding the table to the document lost the information about
        // cells with getRowSpan

        // as a consequence adding the remaining 5 rows of cells for column 2
        // does not work as expected
        // => crashes in PdfPTable.rowSpanAbove with NullPointerException
        for (int i = 6; i < 9; ++i) {
            PdfPCell cell = new PdfPCell();
            cell.addElement(new Phrase("Cell " + i));
            table.addCell(cell);
        }

        // finish second page

        table.setComplete(true);
        document.add(table);
        document.close();
        assertTrue(document.isClosed(), "The document should be closed");
    }

    @Test
    void testCreateTablePass(){
        Assertions.assertThrows(NullPointerException.class, this::testCreateTable);
    }
    void testCreateTable() throws Exception {
        // create document
        Document document = PdfTestBase.createTempPdf("testCreateTable.pdf");
        try {
            // new page with a table
            document.open();
            assertTrue(document.isOpen(), "The document should be opened");
            document.newPage();

            PdfPTable table = createPdfTable(2);
            assertNotNull(table, "The table should be created and not null");

            for (int i = 0; i < 10; i++) {
                PdfPCell cell = new PdfPCell();
                cell.setRowspan(2);
                table.addCell(cell);

            }
            table.calculateHeights(true);
            document.add(table);
            document.newPage();

        } finally {
            // close document
            if (document != null) {
                document.close();
                assertTrue(document.isClosed(), "The document should be closed");
            }
        }

    }

    private PdfPTable createPdfTable(int numberOfColumns)
            throws DocumentException {

        PdfPTable table = new PdfPTable(numberOfColumns);

        table.getDefaultCell().setBorder(1);
        table.setSpacingBefore(0f);
        table.setSpacingAfter(0);
        table.setKeepTogether(true);
        table.getDefaultCell().setUseAscender(true);
        table.getDefaultCell().setUseDescender(true);
        table.getDefaultCell().setUseBorderPadding(false);

        return table;
    }

}
