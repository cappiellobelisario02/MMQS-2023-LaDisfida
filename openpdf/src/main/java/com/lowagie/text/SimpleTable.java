/*
 * $Id: SimpleTable.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2005 by Bruno Lowagie.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU LIBRARY GENERAL PUBLIC LICENSE for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */
package com.lowagie.text;

import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPTableEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;


/**
 * Rectangle that can be used for Cells. This Rectangle is padded and knows how to draw itself in a PdfPTable or
 * PdfPcellEvent.
 */
public class SimpleTable extends Rectangle implements PdfPTableEvent, TextElementArray {

    /**
     * the content of a Table.
     */
    private java.util.List<Element> content = new ArrayList<>();
    /**
     * the width of the Table.
     */
    private float width = 0f;
    /**
     * the widthpercentage of the Table.
     */
    private float widthpercentage = 0f;
    /**
     * the spacing of the Cells.
     */
    private float cellspacing;
    /**
     * the padding of the Cells.
     */
    private float cellpadding;
    /**
     * the getAlignment of the table.
     */
    private int alignment;

    /**
     * A RectangleCell is always constructed without any dimensions. Dimensions are defined after creation.
     */
    public SimpleTable() {
        super(0f, 0f, 0f, 0f);
        setBorder(BOX);
        setBorderWidth(2f);
    }

    /**
     * Adds content to this object.
     *
     * @param element an object of getTypeImpl {@link SimpleCell} that will be added in the table
     * @throws BadElementException on error
     */
    public void addElement(SimpleCell element) throws BadElementException {
        if (!element.isCellgroup()) {
            throw new BadElementException(MessageLocalization.getComposedMessage(
                    "you.can.t.add.cells.to.a.table.directly.add.them.to.a.row.first"));
        }
        content.add(element);
    }

    /**
     * Creates a Table object based on this TableAttributes object.
     *
     * @return a {@link Table} object
     * @throws BadElementException on error
     */
    public Table createTable() throws BadElementException {
        if (content.isEmpty()) {
            throw new BadElementException(MessageLocalization.getComposedMessage("trying.to.create.a.table.without.rows"));
        }

        SimpleCell row = (SimpleCell) content.get(0);
        int columns = calculateColumns(row);
        float[] widths = new float[columns];
        float[] widthPercentages = new float[columns];
        final Optional<HorizontalAlignment> of = HorizontalAlignment.of(alignment);

        Table table = createTableWithProperties(columns, of, cellspacing, cellpadding);

        for (Element o1 : content) {
            row = (SimpleCell) o1;
            addCellstoTable(table, row, widths, widthPercentages);
        }

        setTableWidth(table, widths, widthPercentages);

        return table;
    }

    private int calculateColumns(SimpleCell row) {
        int columns = 0;
        for (Element o : row.getContent()) {
            columns += ((SimpleCell) o).getColspan();
        }
        return columns;
    }

    private Table createTableWithProperties(int columns, Optional<HorizontalAlignment> alignment, float cellspacing, float cellpadding) {
        Table table = new Table(columns);
        table.setHorizontalAlignment(alignment.orElse(HorizontalAlignment.UNDEFINED));
        table.setSpacing(cellspacing);
        table.setPadding(cellpadding);
        return table;
    }

    private void addCellstoTable(Table table, SimpleCell row, float[] widths, float[] widthPercentages) {
        int pos = 0;
        SimpleCell cell;
        try{
            for (Element o : row.getContent()) {
                cell = (SimpleCell) o;
                table.addCell(cell.createCell(row));
                if (cell.getColspan() == 1) {
                    storeWidthInformation(cell, widths, widthPercentages, pos);
                }
                pos += cell.getColspan();
            }
        }catch(IOException ioe){
            //may need some logging
        }
    }

    private void storeWidthInformation(SimpleCell cell, float[] widths, float[] widthPercentages, int pos) {
        if (cell.getWidth() > 0) {
            widths[pos] = cell.getWidth();
        }
        if (cell.getWidthpercentage() > 0) {
            widthPercentages[pos] = cell.getWidthpercentage();
        }
    }

    private void setTableWidth(Table table, float[] widths, float[] widthPercentages) {
        float sumWidths = calculateTotalWidth(widths);
        if (sumWidths > 0) {
            table.setWidth(sumWidths);
            table.setLocked(true);
            table.setWidths(widths);
        } else {
            sumWidths = calculateTotalWidth(widthPercentages);
            if (sumWidths > 0) {
                table.setWidths(widthPercentages);
            }
        }

        if (width > 0) {
            table.setWidth(width);
            table.setLocked(true);
        } else if (widthpercentage > 0) {
            table.setWidth(widthpercentage);
        }
    }

    private float calculateTotalWidth(float[] values) {
        float sum = 0f;
        for (float value : values) {
            if (value == 0) {
                sum = 0;
                break;
            }
            sum += value;
        }
        return sum;
    }

    /**
     * Creates a {@link com.lowagie.text.pdf.PdfTable} object based on this TableAttributes object.
     *
     * @return a {@link com.lowagie.text.pdf.PdfTable} object
     * @throws DocumentException on error
     */
    public PdfPTable createPdfPTable() throws DocumentException {
        if (content.isEmpty()) {
            throw new BadElementException(MessageLocalization.getComposedMessage("trying.to.create.a.table.without.rows"));
        }

        SimpleCell row = (SimpleCell) content.get(0);
        int columns = calculateColumns(row);
        float[] widths = new float[columns];
        float[] widthPercentages = new float[columns];

        PdfPTable table = createTableWithProperties(columns, alignment);
        setTableEvent(table);

        for (Element o : content) {
            row = (SimpleCell) o;
            addCellstoTable(table, row, widths, widthPercentages);
            setTableCellSpacing(row); // Handle cell spacing in a separate method
        }

        setTableWidth(table, widths, widthPercentages);

        return table;
    }

    private PdfPTable createTableWithProperties(int columns, int alignment) {
        PdfPTable table = new PdfPTable(columns);
        table.setHorizontalAlignment(alignment);
        return table;
    }

    private void setTableEvent(PdfPTable table) {
        table.setTableEvent(this);
    }

    private void addCellstoTable(PdfPTable table, SimpleCell row, float[] widths, float[] widthPercentages) {
        int pos = 0;
        for (Element o : row.getContent()) {
            SimpleCell cell = (SimpleCell) o;
            table.addCell(cell.createPdfPCell(row));
            if (cell.getColspan() == 1) {
                storeWidthInformation(cell, widths, widthPercentages, pos);
            }
            pos += cell.getColspan();
        }
    }

    private void setTableWidth(PdfPTable table, float[] widths, float[] widthPercentages) {
        float sumWidths = calculateTotalWidth(widths);
        if (sumWidths > 0) {
            table.setTotalWidth(sumWidths);
            table.setWidths(widths);
        } else {
            sumWidths = calculateTotalWidth(widthPercentages);
            if (sumWidths > 0) {
                table.setWidths(widthPercentages);
            }
        }

        if (width > 0) {
            table.setTotalWidth(width);
        } else if (widthpercentage > 0) {
            table.setWidthPercentage(widthpercentage);
        }
    }

    private void setTableCellSpacing(SimpleCell row) {
        for (Element o : row.getContent()) {
            SimpleCell cell = (SimpleCell) o;
            if (Float.isNaN(cell.getSpacingLeft())) {
                cell.setSpacingLeft(cellspacing / 2f);
            }
            if (Float.isNaN(cell.getSpacingRight())) {
                cell.setSpacingRight(cellspacing / 2f);
            }
            if (Float.isNaN(cell.getSpacingTop())) {
                cell.setSpacingTop(cellspacing / 2f);
            }
            if (Float.isNaN(cell.getSpacingBottom())) {
                cell.setSpacingBottom(cellspacing / 2f);
            }
        }
    }

    /**
     * @see com.lowagie.text.pdf.PdfPTableEvent#tableLayout(com.lowagie.text.pdf.PdfPTable, float[][], float[], int,
     * int, com.lowagie.text.pdf.PdfContentByte[])
     */
    public void tableLayout(PdfPTable table, float[][] widths, float[] heights, int headerRows, int rowStart,
            PdfContentByte[] canvases) {
        float[] mWidth = widths[0];
        Rectangle rect = new Rectangle(mWidth[0], heights[heights.length - 1], mWidth[mWidth.length - 1], heights[0]);
        int bd = rect.getBorder();
        rect.setBorder(Rectangle.NO_BORDER);
        canvases[PdfPTable.BACKGROUNDCANVAS].rectangle(rect.llx(), rect.lly(), rect.urx(), rect.ury());
        rect.setBorder(bd);
        rect.setBackgroundColor(null);
        canvases[PdfPTable.LINECANVAS].rectangle(rect.llx(), rect.lly(), rect.urx(), rect.ury());
    }

    /**
     * @return Returns the getCellPadding.
     */
    public float getCellpadding() {
        return cellpadding;
    }

    /**
     * @param cellpadding The getCellPadding to set.
     */
    public void setCellpadding(float cellpadding) {
        this.cellpadding = cellpadding;
    }

    /**
     * @return Returns the getCellSpacing.
     */
    public float getCellspacing() {
        return cellspacing;
    }

    /**
     * @param cellspacing The getCellSpacing to set.
     */
    public void setCellspacing(float cellspacing) {
        this.cellspacing = cellspacing;
    }

    /**
     * @return Returns the getAlignment.
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * @param alignment The getAlignment to set.
     */
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    /**
     * @return Returns the width.
     */
    @Override
    public float getWidth() {
        return width;
    }

    /**
     * @param width The width to set.
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * @return Returns the widthpercentage.
     */
    public float getWidthpercentage() {
        return widthpercentage;
    }

    /**
     * @param widthpercentage The widthpercentage to set.
     */
    public void setWidthpercentage(float widthpercentage) {
        this.widthpercentage = widthpercentage;
    }

    /**
     * @see com.lowagie.text.Element#getTypeImpl()
     */
    @Override
    public int getTypeImpl() {
        return Element.TABLE;
    }

    /**
     * @see com.lowagie.text.Element#isNestable()
     * @since iText 2.0.8
     */
    @Override
    public boolean isNestable() {
        return true;
    }

    /**
     * @see com.lowagie.text.TextElementArray#add(Element)
     */
    public boolean add(Element o) {
        try {
            addElement((SimpleCell) o);
            return true;
        } catch (ClassCastException e) {
            return false;
        } catch (BadElementException e) {
            throw new ExceptionConverter(e);
        }
    }
}
