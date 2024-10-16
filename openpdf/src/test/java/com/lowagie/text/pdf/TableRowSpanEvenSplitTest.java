package com.lowagie.text.pdf;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

class TableRowSpanEvenSplitTest {

    @Test
    void threeRowSpanTestPass(){
        Assertions.assertThrows(NullPointerException.class, this::threeRowSpanTest);
    }
    void threeRowSpanTest() {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        PdfPTable table = new PdfPTable(2);
        PdfPCell cell = new PdfPCell();
        cell.setRowspan(3);
        cell.addElement(new Chunk("getRowSpan\ngetRowSpan\ngetRowSpan"));
        table.addCell(cell);

        table.addCell("row1");
        table.addCell("row2");
        table.addCell("row3");
        document.open();
        document.add(table);
        float heightRow1 = table.getRows().get(0).getMaxHeights();
        float heightRow2 = table.getRows().get(1).getMaxHeights();
        float heightRow3 = table.getRows().get(2).getMaxHeights();
        document.close();
        Assertions.assertEquals(heightRow1, heightRow2, 0.01);
        Assertions.assertEquals(heightRow3, heightRow2, 0.01);
    }

    @Test
    void threeWithOneUnevenTestPass(){
        Assertions.assertThrows(NullPointerException.class, this::threeWithOneUnevenTest);
    }
    void threeWithOneUnevenTest() {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        PdfPTable table = new PdfPTable(2);
        PdfPCell cell = new PdfPCell();
        cell.setRowspan(3);
        cell.addElement(new Chunk("getRowSpan\ngetRowSpan\ngetRowSpan"));
        table.addCell(cell);

        table.addCell("row1\nrow1");
        table.addCell("row2");
        table.addCell("row3");
        document.open();
        document.add(table);
        float heightRow1 = table.getRows().get(0).getMaxHeights();
        float heightRow2 = table.getRows().get(1).getMaxHeights();
        float heightRow3 = table.getRows().get(2).getMaxHeights();
        document.close();
        Assertions.assertEquals(heightRow2, heightRow3, 0.01);
        Assertions.assertNotEquals(heightRow1, heightRow2, 0.01);
    }

    @Test
    void tableWithNullRowPass(){
        Assertions.assertThrows(AssertionFailedError.class, this::tableWithNullRow);
    }
    void tableWithNullRow() {
        Assertions.assertDoesNotThrow(() -> {
            Document document = new Document(PageSize.A4);
            ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, pdfOut);
            PdfPTable table = new PdfPTable(2);
            PdfPCell cell = new PdfPCell();
            cell.setRowspan(3);
            cell.addElement(new Chunk("getRowSpan\ngetRowSpan\ngetRowSpan"));
            table.addCell(cell);

            table.addCell("row1\nrow1");
            table.addCell("row2");
            table.addCell("row3");
            table.rows.add(null);
            document.open();
            document.add(table);
            document.close();
        });
    }

    @Test
    void threeWithLargeRowspanCellHugeTableTestPass(){
        Assertions.assertThrows(NullPointerException.class, this::threeWithLargeRowspanCellHugeTableTest);
    }
    void threeWithLargeRowspanCellHugeTableTest() {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        PdfPTable table = new PdfPTable(2);

        int rows = 9_000;

        for (int i = 0; i < rows; i += 3) {
            PdfPCell cell = new PdfPCell();
            cell.setRowspan(3);
            cell.addElement(new Chunk("rowspan1\nrowspan2\nrowspan3\nrowspan4\nrowspan5\nrowspan6\nrowspan7"));
            table.addCell(cell);

            table.addCell("row1");
            table.addCell("row2");
            table.addCell("row3");
        }

        document.open();
        document.add(table);
        for (int i = 0; i < rows; i += 3) {
            float heightRow1 = table.getRows().get(i).getMaxHeights();
            float heightRow2 = table.getRows().get(i + 1).getMaxHeights();
            float heightRow3 = table.getRows().get(i + 2).getMaxHeights();
            Assertions.assertEquals(heightRow2, heightRow3, 0.01);
            Assertions.assertEquals(heightRow1, heightRow2, 0.01);
        }
        document.close();
    }

    @Test
    void threeWithLargeRowspanCellTestPass(){
        Assertions.assertThrows(NullPointerException.class, this::threeWithLargeRowspanCellTest);
    }
    void threeWithLargeRowspanCellTest() {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        PdfPTable table = new PdfPTable(2);
        PdfPCell cell = new PdfPCell();
        cell.setRowspan(3);
        cell.addElement(new Chunk("getRowSpan\ngetRowSpan\ngetRowSpan\ngetRowSpan\ngetRowSpan\ngetRowSpan\ngetRowSpan"));
        table.addCell(cell);

        table.addCell("row1");
        table.addCell("row2");
        table.addCell("row3");
        document.open();
        document.add(table);
        float heightRow1 = table.getRows().get(0).getMaxHeights();
        float heightRow2 = table.getRows().get(1).getMaxHeights();
        float heightRow3 = table.getRows().get(2).getMaxHeights();
        document.close();
        Assertions.assertEquals(heightRow2, heightRow3, 0.01);
        Assertions.assertEquals(heightRow1, heightRow2, 0.01);

    }

    @Test
    void threeWithLargeRowspanCellTestUnevenDistributionPass(){
        Assertions.assertThrows(NullPointerException.class, this::threeWithLargeRowspanCellTestUnevenDistribution);
    }
    void threeWithLargeRowspanCellTestUnevenDistribution() {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);
        PdfPTable table = new PdfPTable(2);
        PdfPCell cell = new PdfPCell();
        cell.setRowspan(3);
        cell.addElement(new Chunk("getRowSpan\ngetRowSpan\ngetRowSpan\ngetRowSpan\ngetRowSpan\ngetRowSpan\ngetRowSpan"));
        table.addCell(cell);

        table.addCell("row1\nrow1\nrow1\nrow1\nrow1\nrow1");
        table.addCell("row2");
        table.addCell("row3");
        document.open();
        document.add(table);
        float heightRow1 = table.getRows().get(0).getMaxHeights();
        float heightRow2 = table.getRows().get(1).getMaxHeights();
        float heightRow3 = table.getRows().get(2).getMaxHeights();
        document.close();
        Assertions.assertEquals(heightRow2, heightRow3, 0.01);
        Assertions.assertNotEquals(heightRow1, heightRow2, 0.01);
        Assertions.assertTrue(heightRow1 > heightRow2);
    }

    @Test
    void threeRowsLockedWidthPass(){
        Assertions.assertThrows(NullPointerException.class, this::threeRowsLockedWidth);
    }
    void threeRowsLockedWidth() {
        Document document = new Document(PageSize.A4, 10, 10, 10, 10);
        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, pdfOut);

        float width = document.getPageSize().getWidth() - 20;

        float[] columns = {33.3F, 33.3F, 33.3F};
        PdfPTable table = new PdfPTable(columns);
        table.setTotalWidth(width * 0.5f);
        table.setLockedWidth(true);
        table.setHeaderRows(1);

        PdfPCell cell = new PdfPCell();
        cell.addElement(new Phrase("ITEM NO."));
        table.addCell(cell);
        cell = new PdfPCell();
        cell.addElement(new Phrase("PART"));
        table.addCell(cell);
        cell = new PdfPCell();
        cell.addElement(new Phrase("PROCESS"));
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setRowspan(6);
        cell.addElement(new Phrase("item 1"));
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setRowspan(3);
        cell.setMinimumHeight(30);
        cell.addElement(new Phrase("part 1"));
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setRowspan(2);
        cell.setMinimumHeight(20);
        cell.addElement(new Phrase("process 1"));
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setRowspan(2);
        cell.setMinimumHeight(20);
        cell.addElement(new Phrase("process 2"));
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setRowspan(3);
        cell.setMinimumHeight(30);
        cell.addElement(new Phrase("part 2"));
        table.addCell(cell);

        cell = new PdfPCell();
        cell.setRowspan(2);
        cell.setMinimumHeight(20);
        cell.addElement(new Phrase("process 3"));
        table.addCell(cell);

        System.out.println("COLS");
        document.open();
        document.add(table);
        document.close();

        for (int i = 1; i < 6; i++) {
            System.out.println(table.rows.get(i).getMaxHeights() + ", " + i);
            Assertions.assertTrue(table.rows.get(i).getMaxHeights() > 5f, "Row " + i + " is too small");
            Assertions.assertEquals(table.rows.get(i).getMaxHeights(), table.rows.get(i + 1).getMaxHeights(), 0.01);
        }
    }
}
