package com.lowagie.examples.objects.columns;


import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;


/**
 * Scenario description: Generate a pdf file with multi tables by the ColumnText class, and the first table is at the
 * bottom of the first page. So the second table will generate at the new page.
 *
 * @author mainly written by alesky78, modified by chappyer
 */
public class ColumnTextMultiTable {

    public static float a4MarginLeft = 40;
    public static float a4MarginRight = 40;
    public static float a4MarginTop = 100;
    public static float a4MarginBottom = a4MarginTop;
    public static float a4HeightBody = PageSize.A4.getHeight() - a4MarginTop - a4MarginBottom;
    public static float a4WidthBody = PageSize.A4.getWidth() - a4MarginLeft - a4MarginRight;
    protected static Document document;
    protected PdfWriter pdfWriter;

    public static void main(String[] args) throws IOException {
        ColumnTextMultiTable columnTextTable = new ColumnTextMultiTable();
        File outputPDF = new File("columnTextMultiTables.pdf");

        ColumnTextMultiTable.document = new Document(PageSize.A4);
        ColumnTextMultiTable.document.setMargins(a4MarginLeft, a4MarginRight, a4MarginTop, a4MarginBottom);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        columnTextTable.pdfWriter = PdfWriter.getInstance(document, baos);
        columnTextTable.pdfWriter.setStrictImageSequence(true);

        document.open();
        document.add(new Chunk("The example page"));

        PdfPTable table = columnTextTable.getPdfPTable();
        float space = columnTextTable.getHeightOfBlock(table);

        ColumnText ct = new ColumnText(columnTextTable.pdfWriter.getDirectContent());
        ct.setSimpleColumn(a4MarginLeft, a4MarginBottom, a4WidthBody + a4MarginLeft,
                a4HeightBody + a4MarginBottom);
        float actualY = ColumnTextMultiTable.addFullBockToPage(a4MarginBottom + space, ct, table);

        ct = new ColumnText(columnTextTable.pdfWriter.getDirectContent());
        ct.setSimpleColumn(a4MarginLeft, a4MarginBottom, a4WidthBody + a4MarginLeft,
                a4HeightBody + a4MarginBottom);
        addFullBockToPage(actualY, ct, table);

        document.close();

        try (FileOutputStream fos = new FileOutputStream(outputPDF)) {
            fos.write(baos.toByteArray());
        }
    }


    static float addFullBockToPage(float actualY, ColumnText ct, Element... elements) {
        int result;
        try {
            for (Element element : elements) {
                ct.addElement(element);
            }
            ct.setYLine(actualY);
            result = ct.go(true);
            if (!ColumnText.hasMoreText(result)) {
                ct.setYLine(actualY);
                ct.setText(null);
                for (Element element : elements) {
                    ct.addElement(element);
                }
                ct.go(false);
            } else {
                ct.setText(null);
                for (Element element : elements) {
                    ct.addElement(element);
                }

                result = 0;
                while (ColumnText.hasMoreText(result)) {
                    document.newPage();
                    ct.setYLine(a4HeightBody + a4MarginBottom);
                    result = ct.go(false);
                }
            }
            actualY = ct.getYLine();
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }

        return actualY;

    }

    /**
     * Get the PdfPTable which will be written into the PDF file.
     *
     * @return PdfPTable
     */
    PdfPTable getPdfPTable() {
        Paragraph f1, f2;
        PdfPTable table;
        table = new PdfPTable(1);
        table.setTotalWidth(a4WidthBody);
        table.setLockedWidth(true);
        table.setSplitRows(false);
        PdfPCell cell;
        f1 = new Paragraph(new Chunk("cell1 example content"));
        f1.add(Chunk.NEWLINE);
        f1.setAlignment(Element.ALIGN_CENTER);
        f2 = new Paragraph(new Chunk("cell2 example content"));
        f2.add(Chunk.NEWLINE);
        f2.setAlignment(Element.ALIGN_CENTER);
        cell = new PdfPCell();
        cell.addElement(f1);
        table.addCell(cell);
        cell = new PdfPCell();
        cell.addElement(f2);
        table.addCell(cell);
        return table;
    }

    /**
     * Get the height of the input block.
     *
     * @param elements The input elements which will be used to calculate the height.
     * @return height, which is the height of the input block.
     */
    float getHeightOfBlock(Element... elements) {
        ColumnText ct = new ColumnText(pdfWriter.getDirectContent());
        float startY = a4HeightBody + a4MarginBottom;

        float height;

        ct.setSimpleColumn(a4MarginLeft, a4MarginBottom, a4WidthBody + a4MarginLeft,
                a4HeightBody + a4MarginBottom);
        ct.setYLine(startY);

        for (Element element : elements) {
            ct.addElement(element);
        }

        int result;
        try {
            result = ct.go(true);
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
        if (ColumnText.hasMoreText(result)) {
            return -1;
        } else {
            height = startY - ct.getYLine();
            return height;
        }
    }


}
