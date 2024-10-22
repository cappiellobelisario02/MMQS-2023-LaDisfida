/*
 * $Id: Table.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 1999, 2000, 2001, 2002 by Bruno Lowagie.
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
 *
 * Some methods in this class were contributed by Geert Poels, Kris Jespers and
 * Steve Ogryzek. Check the CVS repository.
 */

package com.lowagie.text;

import com.lowagie.text.alignment.HorizontalAlignment;
import com.lowagie.text.alignment.VerticalAlignment;
import com.lowagie.text.alignment.WithHorizontalAlignment;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.AddCellException;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Optional;

import static com.lowagie.text.Image.logger;


/**
 * A <CODE>Table</CODE> is a <CODE>Rectangle</CODE> that contains <CODE>Cell</CODE>s, ordered in some kind of matrix.
 * <p>
 * Tables that span multiple pages are cut into different parts automatically. If you want a table header to be repeated
 * on every page, you may not forget to mark the end of the header section by using the method
 * <CODE>endHeaders()</CODE>.
 * <p>
 * The matrix of a table is not necessarily an m x n-matrix. It can contain holes or cells that are bigger than the
 * unit. Believe me or not, but it took some serious thinking to make this as user friendly as possible. I hope you will
 * find the result quite simple (I love simple solutions, especially for complex problems). I didn't want it to be
 * something as complex as the Java <CODE>GridBagLayout</CODE>.
 * <p>
 * Example:
 * <BLOCKQUOTE><PRE>
 * // Remark: You MUST know the number of columns when constructing a Table. //         The number of rows is not
 * important.
 * <STRONG>Table table = new Table(3);</STRONG>
 * <STRONG>table.setBorderWidth(1);</STRONG>
 * <STRONG>table.setBorderColor(new Color(0, 0, 255));</STRONG>
 * <STRONG>table.setPadding(5);</STRONG>
 * <STRONG>table.setSpacing(5);</STRONG>
 * Cell cell = new Cell("header"); cell.setHeader(true); cell.setColspan(3);
 * <STRONG>table.addCell(cell);</STRONG>
 * <STRONG>table.endHeaders();</STRONG>
 * cell = new Cell("example cell with colspan 1 and getRowSpan 2"); cell.setRowspan(2); cell.setBorderColor(new Color(255,
 * 0, 0));
 * <STRONG>table.addCell(cell);</STRONG>
 * <STRONG>table.addCell("1.1");</STRONG>
 * <STRONG>table.addCell("2.1");</STRONG>
 * <STRONG>table.addCell("1.2");</STRONG>
 * <STRONG>table.addCell("2.2");</STRONG>
 * <STRONG>table.addCell("cell test1");</STRONG>
 * cell = new Cell("big cell"); cell.setRowspan(2); cell.setColspan(2);
 * <STRONG>table.addCell(cell);</STRONG>
 * <STRONG>table.addCell("cell test2");</STRONG>
 * </PRE></BLOCKQUOTE>
 * The result of this code is a table:
 * <TABLE BORDER="1">
 * <CAPTION>the result of the code above</CAPTION>
 * <TR VALIGN="Left">
 * <TH COLSPAN="3" VALIGN="Left">
 * header
 * </TH>
 * </TR>
 * <TR VALIGN="Left">
 * <TD ROWSPAN="2" VALIGN="Left">
 * example cell with colspan 1 and getRowSpan 2
 * </TD>
 * <TD VALIGN="Left">
 * 1.1
 * </TD>
 * <TD VALIGN="Left">
 * 2.1
 * </TD>
 * </TR>
 * <TR VALIGN="Left">
 * <TD VALIGN="Left">
 * 1.2
 * </TD>
 * <TD VALIGN="Left">
 * 2.2
 * </TD>
 * </TR>
 * <TR VALIGN="Left">
 * <TD VALIGN="Left">
 * cell test1
 * </TD>
 * <TD COLSPAN="2" ROWSPAN="2" VALIGN="Left">
 * big cell
 * </TD>
 * </TR>
 * <TR VALIGN="Left">
 * <TD VALIGN="Left">
 * cell test2
 * </TD>
 * </TR>
 * </TABLE>
 *
 * @see Rectangle
 * @see Element
 * @see Row
 * @see Cell
 */

public class Table extends TableRectangle implements LargeElement, WithHorizontalAlignment {

    public static final String INSERTTABLE_TABLE_HAS_NULL_VALUE = "inserttable.table.has.null.value";

    // membervariables

    /**
     * Boolean to automatically fill empty cells before a table is rendered (takes CPU so may be set to false in case of
     * certainty)
     */
    protected boolean autoFillEmptyCells = false;
    /**
     * if you want to generate tables the old way, set this value to false.
     */
    protected boolean convert2pdfptable = false;
    /**
     * Indicates if this is the first time the section was added.
     *
     * @since iText 2.0.8
     */
    protected boolean notAddedYet = true;
    /**
     * Indicates if the PdfPTable is complete once added to the document.
     *
     * @since iText 2.0.8
     */
    protected boolean complete = true;
    /**
     * If true this table may not be split over two pages.
     */
    boolean tableFitsPage = false;
    /**
     * If true cells may not be split over two pages.
     */
    boolean cellsFitPage = false;
    /**
     * This is the offset of the table.
     */
    float offset = Float.NaN;
    /**
     * This is the number of columns in the <CODE>Table</CODE>.
     */
    private int columns;
    /**
     * This is the list of <CODE>Row</CODE>s.
     */
    private ArrayList<Row> rows = new ArrayList<>();
    /**
     * The current Position in the table.
     */
    private Point curPosition;
    /**
     * This Empty Cell contains the DEFAULT layout of each Cell added with the method addCell(String content).
     */
    private Cell defaultCell = new Cell(true);
    /**
     * This is the number of the last row of the table headers.
     */
    private int lastHeaderRow = -1;
    /**
     * This is the horizontal getAlignment.
     */
    private int alignment = Element.ALIGN_CENTER;
    /**
     * This is getCellPadding.
     */
    private float cellpadding;
    /**
     * This is getCellSpacing.
     */
    private float cellspacing;
    /**
     * This is the width of the table (in percent of the available space).
     */
    private float width = 80;
    /**
     * Is the width a percentage (false) or an absolute width (true)?
     */
    private boolean locked = false;
    /**
     * This is an array containing the widths (in percentages) of every column.
     */
    private float[] widths;
    /**
     * Boolean to track if a table was inserted (to avoid unnecessary computations afterwards)
     */
    private boolean mTableInserted = false;

    // constructors

    /**
     * Constructs a <CODE>Table</CODE> with a certain number of columns.
     *
     * @param columns The number of columns in the table
     * @throws BadElementException if the creator was called with less than 1 column
     */
    public Table(int columns) throws BadElementException {
        this(columns, 1);
    }

    /**
     * Constructs a <CODE>Table</CODE> with a certain number of columns and a certain number of <CODE>Row</CODE>s.
     *
     * @param columns The number of columns in the table
     * @param rows    The number of rows
     * @throws BadElementException if the creator was called with less than 1 column
     */
    public Table(int columns, int rows) throws BadElementException {
        // a Rectangle is create with BY DEFAULT a border with a width of 1
        super(0, 0, 0, 0);
        setBorder(BOX);
        setBorderWidth(1);
        defaultCell.setBorder(BOX);

        // a table should have at least 1 column
        if (columns <= 0) {
            throw new BadElementException(
                    MessageLocalization.getComposedMessage("a.table.should.have.at.least.1.column"));
        }
        this.columns = columns;

        // a certain number of rows are created
        for (int i = 0; i < rows; i++) {
            this.rows.add(new Row(columns));
        }
        curPosition = new Point(0, 0);

        // the DEFAULT widths are calculated
        widths = new float[columns];
        float widthM = 100f / columns;
        for (int i = 0; i < columns; i++) {
            widths[i] = widthM;
        }
    }

    /**
     * Copy constructor (shallow copy).
     *
     * @param t ab object of {@link Table} new table will be created from a shallow copy of this
     */
    public Table(Table t) {
        super(0, 0, 0, 0);
        this.columns = t.columns;
        this.rows = t.rows;
        this.curPosition = t.curPosition;
        this.defaultCell = t.defaultCell;
        this.lastHeaderRow = t.lastHeaderRow;
        this.alignment = t.alignment;
        this.cellpadding = t.cellpadding;
        this.cellspacing = t.cellspacing;
        this.width = t.width;
        this.widths = t.widths;
        this.autoFillEmptyCells = t.autoFillEmptyCells;
        this.tableFitsPage = t.tableFitsPage;
        this.cellsFitPage = t.cellsFitPage;
        this.offset = t.offset;
        this.convert2pdfptable = t.convert2pdfptable;
    }

    // implementation of the Element-methods

    /**
     * Processes the element by adding it (or the different parts) to an
     * <CODE>ElementListener</CODE>.
     *
     * @param listener an <CODE>ElementListener</CODE>
     * @return <CODE>true</CODE> if the element was processed successfully
     */
    @Override
    public boolean process(ElementListener listener) {
        try {
            return listener.add(this);
        } catch (DocumentException de) {
            return false;
        }
    }

    /**
     * Gets the getTypeImpl of the text element.
     *
     * @return a getTypeImpl
     */
    @Override
    public int getTypeImpl() {
        return Element.TABLE;
    }

    /**
     * Gets all the chunks in this element.
     *
     * @return an <CODE>ArrayList</CODE>
     */
    @Override
    public ArrayList<Element> getChunks() {
        return new ArrayList<>();
    }

    @Override
    public float llx() {
        return 0;
    }

    @Override
    public float lly() {
        return 0;
    }

    @Override
    public float urx() {
        return 0;
    }

    @Override
    public float ury() {
        return 0;
    }

    /**
     * @see com.lowagie.text.Element#isNestable()
     * @since iText 2.0.8
     */
    @Override
    public boolean isNestable() {
        return true;
    }

    // getters and setters

    /**
     * Gets the number of columns.
     *
     * @return a value
     */
    public int getColumns() {
        return columns;
    }

    /**
     * Gets the number of rows in this <CODE>Table</CODE>.
     *
     * @return the number of rows in this <CODE>Table</CODE>
     */
    public int size() {
        return rows.size();
    }

    /**
     * Gets the dimension of this table
     *
     * @return dimension
     */
    public Dimension getDimension() {
        return new Dimension(columns, size());
    }

    /**
     * Gets the default layout of the Table.
     *
     * @return a cell with all the defaults
     * @since 2.0.7
     */
    public Cell getDefaultCell() {
        return defaultCell;
    }

    /**
     * Sets the default layout of the Table to the provided Cell
     *
     * @param value a cell with all the defaults
     * @since 2.0.7
     */
    public void setDefaultCell(Cell value) {
        defaultCell = value;
    }

    /**
     * Gets the last number of the rows that contain headers.
     *
     * @return a getRowNumber
     */
    public int getLastHeaderRow() {
        return this.lastHeaderRow;
    }

    /**
     * Sets the horizontal getAlignment.
     *
     * @param value the new value
     */
    public void setLastHeaderRow(int value) {
        lastHeaderRow = value;
    }

    /**
     * Marks the last row of the table headers.
     *
     * @return the number of the last row of the table headers
     */
    public int endHeaders() {
        lastHeaderRow = curPosition.x - 1;
        return lastHeaderRow;
    }

    /**
     * Gets the horizontal getAlignment.
     *
     * @return a value
     */
    public int getAlignment() {
        return alignment;
    }

    @Override
    public void setHorizontalAlignment(final HorizontalAlignment alignment) {
        if (alignment == null) {
            return;
        }

        this.alignment = alignment.getId();
    }

    /**
     * Gets the getCellPadding.
     *
     * @return a value
     */
    public float getPadding() {
        return cellpadding;
    }

    /**
     * Sets the getCellPadding.
     *
     * @param value the new value
     */
    public void setPadding(float value) {
        cellpadding = value;
    }

    /**
     * Gets the getCellSpacing.
     *
     * @return a value
     */
    public float getSpacing() {
        return cellspacing;
    }

    /**
     * Sets the getCellSpacing.
     *
     * @param value the new value
     */
    public void setSpacing(float value) {
        cellspacing = value;
    }

    /**
     * Enables/disables automatic insertion of empty cells before table is rendered. (default = false) As some people
     * may want to create a table, fill only a couple of the cells and don't bother with investigating which empty ones
     * need to be added, this default behavior may be very welcome. Disabling is recommended to increase speed. (empty
     * cells should be added through extra code then)
     *
     * @param aDoAutoFill enable/disable autofill
     */
    public void setAutoFillEmptyCells(boolean aDoAutoFill) {
        autoFillEmptyCells = aDoAutoFill;
    }

    /**
     * Gets the table width (a percentage).
     *
     * @return the table width
     */
    @Override
    public float getWidth() {
        return width;
    }

    /**
     * Sets the width of this table (in percentage of the available space).
     *
     * @param width the width
     */
    public void setWidth(float width) {
        this.width = width;
    }

    /**
     * @return the locked
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * @param locked the locked to set
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * Gets the proportional widths of the columns in this <CODE>Table</CODE>.
     *
     * @return the proportional widths of the columns in this <CODE>Table</CODE>
     */
    public float[] getProportionalWidths() {
        return widths;
    }

    /**
     * Sets the widths of the different columns (percentages).
     * <p>
     * You can give up relative values of borderwidths. The sum of these values will be considered 100%. The values will
     * be recalculated as percentages of this sum.
     * <p>
     * example:
     * <BLOCKQUOTE><PRE>
     * float[] widths = {2, 1, 1};
     * <STRONG>table.setWidths(widths)</STRONG>
     * </PRE></BLOCKQUOTE>
     * The widths will be: a width of 50% for the first column, 25% for the second and third column.
     *
     * @param widths an array with values
     * @throws BadElementException on error
     */
    public void setWidths(float[] widths) throws BadElementException {
        if (widths.length != columns) {
            throw new BadElementException(MessageLocalization.getComposedMessage("wrong.number.of.columns"));
        }

        // The sum of all values is 100%
        float hundredPercent = 0;
        for (int i = 0; i < columns; i++) {
            hundredPercent += widths[i];
        }

        // The different percentages are calculated
        this.widths[columns - 1] = 100;
        for (int i = 0; i < columns - 1; i++) {
            if(hundredPercent != 0){
                width = (100.0f * widths[i]) / hundredPercent;
            }
            this.widths[i] = width;
            this.widths[columns - 1] -= width;
        }
    }

    /**
     * Sets the widths of the different columns (percentages).
     * <p>
     * You can give up relative values of borderwidths. The sum of these values will be considered 100%. The values will
     * be recalculated as percentages of this sum.
     *
     * @param widths an array with values
     * @throws DocumentException on error
     */
    public void setWidths(int[] widths) throws DocumentException {
        int[] tb = Arrays.copyOf(widths, widths.length);
        setWidths(tb);
    }

    /**
     * Checks if this <CODE>Table</CODE> has to fit a page.
     *
     * @return <code>true</code> if the table may not be split
     */
    public boolean isTableFitsPage() {
        return tableFitsPage;
    }

    /**
     * Allows you to control when a page break occurs.
     * <p>
     * When a table doesn't fit a page, it is split in two parts. If you want to avoid this, you should set the
     * <VAR>tableFitsPage</VAR> value to true.
     *
     * @param fitPage enter true if you don't want to split cells
     */
    public void setTableFitsPage(boolean fitPage) {
        this.tableFitsPage = fitPage;
        if (fitPage) {
            setCellsFitPage(true);
        }
    }

    /**
     * Checks if the cells of this <CODE>Table</CODE> have to fit a page.
     *
     * @return <code>true</code> if the cells may not be split
     */
    public boolean isCellsFitPage() {
        return cellsFitPage;
    }

    /**
     * Allows you to control when a page break occurs.
     * <p>
     * When a cell doesn't fit a page, it is split in two parts. If you want to avoid this, you should set the
     * <VAR>cellsFitPage</VAR> value to true.
     *
     * @param fitPage enter true if you don't want to split cells
     */
    public void setCellsFitPage(boolean fitPage) {
        this.cellsFitPage = fitPage;
    }

    /**
     * Gets the offset of this table.
     *
     * @return the space between this table and the previous element.
     */
    public float getOffset() {
        return offset;
    }

    /**
     * Sets the offset of this table.
     * <p>
     * Normally a newline is added before you add a Table object. This newline uses the current getLeading. If you want to
     * control the space between the table and the previous element yourself, you have to set the offset of this table.
     *
     * @param offset the space between this table and the previous object.
     */
    public void setOffset(float offset) {
        this.offset = offset;
    }

    /**
     * Method to check if the Table should be converted to a PdfPTable or not.
     *
     * @return false if the table should be handled the old fashioned way.
     */
    public boolean isConvert2pdfptable() {
        return convert2pdfptable;
    }

    /**
     * If set to true, iText will try to convert the Table to a PdfPTable.
     *
     * @param convert2pdfptable true if you want iText to try to convert the Table to a PdfPTable
     */
    public void setConvert2pdfptable(boolean convert2pdfptable) {
        this.convert2pdfptable = convert2pdfptable;
    }

    // methods to add content to the table

    /**
     * Adds a <CODE>Cell</CODE> to the <CODE>Table</CODE> at a certain row and column.
     *
     * @param aCell  The <CODE>Cell</CODE> to add
     * @param row    The row where the <CODE>Cell</CODE> will be added
     * @param column The column where the <CODE>Cell</CODE> will be added
     * @throws BadElementException on error
     */
    public void addCell(Cell aCell, int row, int column) throws BadElementException, AddCellException, IOException {
        addCell(aCell, new Point(row, column));
    }

    /**
     * Adds a <CODE>Cell</CODE> to the <CODE>Table</CODE> at a certain location.
     *
     * @param aCell     The <CODE>Cell</CODE> to add
     * @param aLocation The location where the <CODE>Cell</CODE> will be added
     * @throws BadElementException on error
     */
    public void addCell(Cell aCell, Point aLocation) throws BadElementException, AddCellException, IOException {
        if (aCell == null) {
            throw new NullPointerException(MessageLocalization.getComposedMessage("addcell.cell.has.null.value"));
        }
        if (aLocation == null) {
            throw new NullPointerException(MessageLocalization.getComposedMessage("addcell.point.has.null.value"));
        }
        if (aCell.isTable()) {
            insertTable((Table) aCell.getElements().next(), aLocation);
        }

        if (aLocation.x < 0) {
            throw new BadElementException(
                    MessageLocalization.getComposedMessage("row.coordinate.of.location.must.be.gt.eq.0"));
        }
        if ((aLocation.y <= 0) && (aLocation.y > columns)) {
            throw new BadElementException(MessageLocalization.getComposedMessage(
                    "column.coordinate.of.location.must.be.gt.eq.0.and.lt.nr.of.columns"));
        }
        if (!isValidLocation(aCell, aLocation)) {
            throw new BadElementException(MessageLocalization.getComposedMessage(
                    "adding.a.cell.at.the.location.1.2.with.a.colspan.of.3.and.a.getRowSpan.of.4.is.illegal.beyond.boundaries.overlapping",
                    String.valueOf(aLocation.x), String.valueOf(aLocation.y), String.valueOf(aCell.getColspan()),
                    String.valueOf(aCell.getRowspan())));
        }

        if (aCell.getBorder() == UNDEFINED) {
            aCell.setBorder(defaultCell.getBorder());
        }
        aCell.fill();
        placeCell(rows, aCell, aLocation);
        setCurrentLocationToNextValidPosition(aLocation);
    }

    /**
     * Adds a <CODE>Cell</CODE> to the <CODE>Table</CODE>.
     *
     * @param cell a <CODE>Cell</CODE>
     */
    public void addCell(Cell cell) throws IOException {
        try {
            addCell(cell, curPosition);
        } catch (BadElementException bee) {
            // Log a warning message to inform that the cell was not added
            logger.warning("Failed to add cell: " + bee.getMessage());
            // Optionally, you could throw a specific exception or handle it as needed
        } catch (AddCellException e) {
            // Rethrow AddCellException as IOException for further handling
            throw new IOException("Error adding cell to table: " + e.getMessage(), e);
        }
    }


    /**
     * Adds a <CODE>Cell</CODE> to the <CODE>Table</CODE>.
     * <p>
     * This is a shortcut for <CODE>addCell(Cell cell)</CODE>. The <CODE>Phrase</CODE> will be converted to a
     * <CODE>Cell</CODE>.
     *
     * @param content a <CODE>Phrase</CODE>
     * @throws BadElementException this should never happen
     */
    public void addCell(Phrase content) throws BadElementException, AddCellException, IOException {
        addCell(content, curPosition);
    }

    /**
     * Adds a <CODE>Cell</CODE> to the <CODE>Table</CODE>.
     * <p>
     * This is a shortcut for <CODE>addCell(Cell cell, Point location)</CODE>. The <CODE>Phrase</CODE> will be converted
     * to a <CODE>Cell</CODE>.
     *
     * @param content  a <CODE>Phrase</CODE>
     * @param location a <CODE>Point</CODE>
     * @throws BadElementException this should never happen
     */
    public void addCell(Phrase content, Point location) throws BadElementException, AddCellException, IOException {
        Cell cell = new Cell(content);
        cell.setBorder(defaultCell.getBorder());
        cell.setBorderWidth(defaultCell.getBorderWidth());
        cell.setBorderColor(defaultCell.getBorderColor());
        cell.setBackgroundColor(defaultCell.getBackgroundColor());
        Optional<HorizontalAlignment> optionalHorizontalAlignment = HorizontalAlignment
                .of(defaultCell.getHorizontalAlignment());
        cell.setHorizontalAlignment(optionalHorizontalAlignment.orElse(HorizontalAlignment.UNDEFINED));
        Optional<VerticalAlignment> optionalVerticalAlignment = VerticalAlignment
                .of(defaultCell.getVerticalAlignment());
        cell.setVerticalAlignment(optionalVerticalAlignment.orElse(VerticalAlignment.UNDEFINED));
        cell.setColspan(defaultCell.getColspan());
        cell.setRowspan(defaultCell.getRowspan());
        addCell(cell, location);
    }

    /**
     * Adds a <CODE>Cell</CODE> to the <CODE>Table</CODE>.
     * <p>
     * This is a shortcut for <CODE>addCell(Cell cell)</CODE>. The <CODE>String</CODE> will be converted to a
     * <CODE>Cell</CODE>.
     *
     * @param content a <CODE>String</CODE>
     * @throws BadElementException this should never happen
     */

    public void addCell(String content) throws BadElementException, AddCellException, IOException {
        addCell(new Phrase(content), curPosition);
    }

    /**
     * Adds a <CODE>Cell</CODE> to the <CODE>Table</CODE>.
     * <p>
     * This is a shortcut for <CODE>addCell(Cell cell, Point location)</CODE>. The <CODE>String</CODE> will be converted
     * to a <CODE>Cell</CODE>.
     *
     * @param content  a <CODE>String</CODE>
     * @param location a <CODE>Point</CODE>
     * @throws BadElementException this should never happen
     */
    public void addCell(String content, Point location) throws BadElementException, AddCellException, IOException {
        addCell(new Phrase(content), location);
    }

    /**
     * To put a table within the existing table at the current position generateTable will of course re-arrange the
     * widths of the columns.
     *
     * @param aTable the table you want to insert
     */
    public void insertTable(Table aTable) throws AddCellException, IOException {
        if (aTable == null) {
            throw new NullPointerException(MessageLocalization.getComposedMessage(INSERTTABLE_TABLE_HAS_NULL_VALUE));
        }
        insertTable(aTable, curPosition);
    }

    /**
     * To put a table within the existing table at the given position generateTable will of course re-arrange the widths
     * of the columns.
     *
     * @param aTable The <CODE>Table</CODE> to add
     * @param row    The row where the <CODE>Cell</CODE> will be added
     * @param column The column where the <CODE>Cell</CODE> will be added
     */
    public void insertTable(Table aTable, int row, int column) throws AddCellException, IOException {
        if (aTable == null) {
            throw new NullPointerException(MessageLocalization.getComposedMessage(INSERTTABLE_TABLE_HAS_NULL_VALUE));
        }
        insertTable(aTable, new Point(row, column));
    }

    /**
     * To put a table within the existing table at the given position generateTable will of course re-arrange the widths
     * of the columns.
     *
     * @param aTable    the table you want to insert
     * @param aLocation a <CODE>Point</CODE>
     */
    public void insertTable(Table aTable, Point aLocation) throws AddCellException, IOException {

        if (aTable == null) {
            throw new NullPointerException(MessageLocalization.getComposedMessage(INSERTTABLE_TABLE_HAS_NULL_VALUE));
        }
        if (aLocation == null) {
            throw new NullPointerException(MessageLocalization.getComposedMessage("inserttable.point.has.null.value"));
        }
        mTableInserted = true;
        aTable.complete();

        if (aLocation.y > columns) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("inserttable.wrong.columnposition.1.of.location.max.eq.2",
                            String.valueOf(aLocation.y), String.valueOf(columns)));
        }

        int rowCount = aLocation.x + 1 - rows.size();
        int i = 0;
        if (rowCount > 0) {   //create new rows ?
            for (; i < rowCount; i++) {
                rows.add(new Row(columns));
            }
        }

        rows.get(aLocation.x).setElement(aTable, aLocation.y);

        setCurrentLocationToNextValidPosition(aLocation);
    }

    /**
     * Gives you the possibility to add columns.
     *
     * @param aColumns the number of columns to add
     */
    public void addColumns(int aColumns) {
        ArrayList<Row> newRows = new ArrayList<>(rows.size());

        int newColumns = columns + aColumns;
        Row row;
        for (int i = 0; i < rows.size(); i++) {
            row = new Row(newColumns);
            for (int j = 0; j < columns; j++) {
                row.setElement(rows.get(i).getCell(j), j);
            }
            for (int j = columns; j < newColumns && i < curPosition.x; j++) {
                row.setElement(null, j);
            }
            newRows.add(row);
        }
        // applied 1 column-fix; last column needs to have a width of 0
        float[] newWidths = new float[newColumns];
        System.arraycopy(widths, 0, newWidths, 0, columns);
        for (int j = columns; j < newColumns; j++) {
            newWidths[j] = 0;
        }
        columns = newColumns;
        widths = newWidths;
        rows = newRows;
    }

    /**
     * Deletes a column in this table.
     *
     * @param column the number of the column that has to be deleted
     * @throws BadElementException on error
     */
    public void deleteColumn(int column) throws BadElementException {
        float[] newWidths = new float[--columns];
        System.arraycopy(widths, 0, newWidths, 0, column);
        System.arraycopy(widths, column + 1, newWidths, column, columns - column);
        setWidths(newWidths);
        System.arraycopy(widths, 0, newWidths, 0, columns);
        widths = newWidths;
        Row row;
        int size = rows.size();
        for (int i = 0; i < size; i++) {
            row = rows.get(i);
            row.deleteColumn(column);
            rows.set(i, row);
        }
        if (column == columns) {
            curPosition.setLocation(curPosition.x + 1, 0);
        }
    }

    /**
     * Deletes a row.
     *
     * @param row the number of the row to delete
     * @return boolean <CODE>true</CODE> if the row was deleted; <CODE>false</CODE> if not
     */
    public boolean deleteRow(int row) {
        if (row < 0 || row >= rows.size()) {
            return false;
        }
        rows.remove(row);
        curPosition.setLocation(curPosition.x - 1, curPosition.y);
        return true;
    }

    /**
     * Deletes all rows in this table. (contributed by dperezcar@fcc.es)
     */
    public void deleteAllRows() {
        rows.clear();
        rows.add(new Row(columns));
        curPosition.setLocation(0, 0);
        lastHeaderRow = -1;
    }

    /**
     * Deletes the last row in this table.
     *
     * @return boolean <CODE>true</CODE> if the row was deleted; <CODE>false</CODE> if not
     */
    public boolean deleteLastRow() {
        return deleteRow(rows.size() - 1);
    }

    /**
     * Will fill empty cells with valid blank <CODE>Cell</CODE>s
     */
    public void complete() throws AddCellException, IOException {
        if (mTableInserted) {
            mergeInsertedTables();  // integrate tables in the table
            mTableInserted = false;
        }
        if (autoFillEmptyCells) {
            fillEmptyMatrixCells();
        }
    }

    // private helper classes

    /**
     * returns the element at the position row, column (Cast to Cell or Table)
     *
     * @param row    row number
     * @param column column number
     * @return dimension
     * @since 2.1.0 (was made private in 2.0.3)
     */
    public TableRectangle getElement(int row, int column) {
        return rows.get(row).getCell(column);
    }

    /**
     * Integrates all added tables and recalculates column widths.
     */
    private void mergeInsertedTables() throws AddCellException {
        // Prepara dati di base
        int[] lDummyWidths = new int[columns];
        float[][] lDummyColumnWidths = new float[columns][];
        int[] lDummyHeights = new int[rows.size()];

        // 1. Calcola le nuove colonne
        int lTotalColumns = calculateNewColumns(lDummyWidths, lDummyColumnWidths);

        // 2. Calcola le nuove righe
        int lTotalRows = calculateNewRows(lDummyHeights);

        // 3. Gestione dei cambiamenti (aggiunta di nuove righe o colonne)
        if ((lTotalColumns != columns) || (lTotalRows != rows.size()) || containsTable()) {
            float[] lNewWidths = calculateNewWidths(lTotalColumns, lDummyWidths, lDummyColumnWidths);
            ArrayList<Row> newRows = generateNewTable(lTotalRows, lTotalColumns);

            // 4. Copia i valori delle celle
            copyCellValues(newRows, lDummyWidths, lDummyHeights, lTotalColumns);

            // 5. Imposta la nuova matrice
            setNewMatrix(lTotalColumns, lNewWidths, newRows);
        }
    }

    private int calculateNewColumns(int[] lDummyWidths, float[][] lDummyColumnWidths) {
        int lTotalColumns = 0;
        for (int j = 0; j < columns; j++) {
            lDummyColumnWidths[j] = calculateColumnWidths(j);
            if (lDummyColumnWidths[j] != null) {
                lDummyWidths[j] = lDummyColumnWidths[j].length; // Il numero di colonne dopo lo split
            } else {
                lDummyWidths[j] = 1; // Nessuno split, rimane la colonna originale
            }
            lTotalColumns += lDummyWidths[j];
        }
        return lTotalColumns;
    }

    private float[] calculateColumnWidths(int columnIndex) {
        float[] tmpWidths = null;
        for (Row row : rows) {
            if (row.getCell(columnIndex) instanceof Table lDummyTable) {
                tmpWidths = mergeTableWidths(tmpWidths, lDummyTable.widths);
            }
        }
        return tmpWidths;
    }

    private float[] mergeTableWidths(float[] tmpWidths, float[] newWidths) {
        if (tmpWidths == null) {
            return newWidths;
        }

        int cols = newWidths.length;
        float[] mergedWidths = new float[tmpWidths.length + cols];
        float tpW = 0;
        int btW = 0;
        int totW = 0;
        int tpI = 0;
        int btI = 0;
        int totI = 0;

        tpW += tmpWidths[0];
        btW += (int) newWidths[0];

        while (tpI < tmpWidths.length && btI < cols) {
            if (btW > tpW) {
                mergedWidths[totI] = tpW - totW;
                tpI++;
                if (tpI < tmpWidths.length) {
                    tpW += tmpWidths[tpI];
                }
            } else {
                mergedWidths[totI] = (float) btW - totW;
                btI++;
                if (Math.abs(btW - tpW) < 0.0001) {
                    tpI++;
                    if (tpI < tmpWidths.length) {
                        tpW += tmpWidths[tpI];
                    }
                }
                if (btI < cols) {
                    btW += (int) newWidths[btI];
                }
            }
            totW += (int) mergedWidths[totI];
            totI++;
        }

        // Se avanzano ancora colonne da copiare
        if (tpI < tmpWidths.length) {
            System.arraycopy(tmpWidths, tpI, mergedWidths, totI, tmpWidths.length - tpI);
            totI += tmpWidths.length - tpI;
        }
        if (btI < cols) {
            System.arraycopy(newWidths, btI, mergedWidths, totI, cols - btI);
            totI += cols - btI;
        }

        float[] resultWidths = new float[totI];
        System.arraycopy(mergedWidths, 0, resultWidths, 0, totI);
        return resultWidths;
    }

    private int calculateNewRows(int[] lDummyHeights) {
        int lTotalRows = 0;
        for (int i = 0; i < rows.size(); i++) {
            lDummyHeights[i] = calculateRowHeight(i);
            lTotalRows += lDummyHeights[i];
        }
        return lTotalRows;
    }

    private int calculateRowHeight(int rowIndex) {
        int lNewMaxRows = 1;
        for (int j = 0; j < columns; j++) {
            if (rows.get(rowIndex).getCell(j) instanceof Table lDummyTable) {
                lNewMaxRows = Math.max(lNewMaxRows, lDummyTable.getDimension().height);
            }
        }
        return lNewMaxRows;
    }

    private void copyCellValues(ArrayList<Row> newRows, int[] lDummyWidths, int[] lDummyHeights, int lTotalColumns)
            throws AddCellException {
        int lDummyRow = 0;
        for (int i = 0; i < rows.size(); i++) {
            copyRowValues(i, lDummyRow, newRows, lDummyWidths, lTotalColumns);
            lDummyRow += lDummyHeights[i];
        }
    }

    private void copyRowValues(int rowIndex, int newRow, ArrayList<Row> newRows, int[] lDummyWidths, int lTotalColumns)
            throws AddCellException {
        int lDummyColumn = 0;
        for (int j = 0; j < lTotalColumns; j++) {
            if (rows.get(rowIndex).getCell(j) instanceof Table lDummyTable) {
                copyTableValues(lDummyTable, newRows, newRow, lDummyColumn, lDummyWidths[j]);
            } else {
                copyCell(rowIndex, j, newRows, newRow, lDummyColumn);
            }
            lDummyColumn += lDummyWidths[j];
        }
    }

    private void copyTableValues(Table lDummyTable, ArrayList<Row> newRows, int newRow, int newColumn, int colSpan) {
        int[] colMap = createColMap(lDummyTable.widths, newColumn, colSpan);

        for (int k = 0; k < lDummyTable.getDimension().height; k++) {
            for (int l = 0; l < lDummyTable.getDimension().width; l++) {
                TableRectangle lDummyElement = lDummyTable.getElement(k, l);
                if (lDummyElement != null) {
                    int col = colMap[l];
                    if (lDummyElement instanceof Cell cell) {
                        int ot = colMap[l + cell.getColspan()];
                        cell.setColspan(ot - col);
                    }
                    newRows.get(k + newRow).addElement(lDummyElement, col);
                }
            }
        }
    }

    private int[] createColMap(float[] widths, int newColumn, int colSpan) {
        int[] colMap = new int[widths.length + 1];
        int ct = 0;
        int cb = 0;

        for (; cb < widths.length; cb++) {
            colMap[cb] = newColumn + ct;
            float wt = 0;
            float wb = widths[cb];

            while (ct < colSpan) {
                wt += widths[ct++];
                if (Math.abs(wb - wt) < 0.0001) {
                    break;
                }
            }
        }
        colMap[cb] = newColumn + ct;
        return colMap;
    }

    private void copyCell(int rowIndex, int colIndex, ArrayList<Row> newRows, int newRow, int newColumn)
            throws AddCellException {
        TableRectangle aElement = getElement(rowIndex, colIndex);
        if (aElement instanceof Cell cell) {
            cell.setRowspan(cell.getRowspan() + (newRow - rowIndex));
            cell.setColspan(cell.getColspan() + (newColumn - colIndex));
            placeCell(newRows, cell, new Point(newRow, newColumn));
        }
    }

    private void setNewMatrix(int lTotalColumns, float[] lNewWidths, ArrayList<Row> newRows) {
        this.columns = lTotalColumns;
        this.widths = lNewWidths;
        this.rows = newRows;
    }

    private float[] calculateNewWidths(int lTotalColumns, int[] lDummyWidths, float[][] lDummyColumnWidths) {
        float[] lNewWidths = new float[lTotalColumns];
        int lDummy = 0;
        for (int tel = 0; tel < widths.length; tel++) {
            if (lDummyWidths[tel] != 1) {
                for (int tel2 = 0; tel2 < lDummyWidths[tel]; tel2++) {
                    lNewWidths[lDummy] = widths[tel] * lDummyColumnWidths[tel][tel2] / 100f;
                    lDummy++;
                }
            } else {
                lNewWidths[lDummy] = widths[tel];
                lDummy++;
            }
        }
        return lNewWidths;
    }

    private boolean containsTable() {
        for (Row row : rows) {
            for (int j = 0; j < columns; j++) {
                if (row.getCell(j) instanceof Table) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * adds new<CODE>Cell</CODE>'s to empty/null spaces.
     */
    private void fillEmptyMatrixCells() throws IOException {
        try {
            for (int i = 0; i < rows.size(); i++) {
                for (int j = 0; j < columns; j++) {
                    if (!rows.get(i).isReserved(j)) {
                        addCell(defaultCell, new Point(i, j));
                    }
                }
            }
        } catch (BadElementException bee) {
            throw new ExceptionConverter(bee);
        } catch (AddCellException e) {
            throw new IOException(e);
        }
    }

    /**
     * check if <CODE>Cell</CODE> 'fits' the table.
     * <p>
     * <UL><LI>getRowSpan/colspan not beyond borders
     * <LI>spanned cell don't overlap existing cells</UL>
     *
     * @param aCell     the cell that has to be checked
     * @param aLocation the location where the cell has to be placed
     * @return true if the location was valid
     */
    private boolean isValidLocation(Cell aCell, Point aLocation) {
        // Check if the location exceeds the row boundary
        if (aLocation.x >= rows.size()) {
            return isWithinColumnBounds(aCell, aLocation);
        }

        // Check if the colspan exceeds the column limit
        if ((aLocation.y + aCell.getColspan()) > columns) {
            return false;
        }

        // Calculate the valid getRowSpan and colspan limits
        int difx = Math.min(aCell.getRowspan(), rows.size() - aLocation.x);
        int dify = Math.min(aCell.getColspan(), columns - aLocation.y);

        // Check if any targeted cells are reserved
        return !isAnyCellReserved(aLocation, difx, dify);
    }

    private boolean isWithinColumnBounds(Cell aCell, Point aLocation) {
        return (aLocation.y + aCell.getColspan()) <= columns;
    }

    private boolean isAnyCellReserved(Point aLocation, int difx, int dify) {
        for (int i = aLocation.x; i < aLocation.x + difx; i++) {
            for (int j = aLocation.y; j < aLocation.y + dify; j++) {
                if (rows.get(i).isReserved(j)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Sets the unset cell properties to be the table defaults.
     *
     * @param aCell The cell to set to table defaults as necessary.
     */
    private void assumeTableDefaults(Cell aCell) {

        if (aCell.getBorder() == Rectangle.UNDEFINED) {
            aCell.setBorder(defaultCell.getBorder());
        }
        if (aCell.getBorderWidth() == Rectangle.UNDEFINED) {
            aCell.setBorderWidth(defaultCell.getBorderWidth());
        }
        if (aCell.getBorderColor() == null) {
            aCell.setBorderColor(defaultCell.getBorderColor());
        }
        if (aCell.getBackgroundColor() == null) {
            aCell.setBackgroundColor(defaultCell.getBackgroundColor());
        }
        if (aCell.getHorizontalAlignment() == Element.ALIGN_UNDEFINED) {
            final Optional<HorizontalAlignment> of = HorizontalAlignment
                    .of(defaultCell.getHorizontalAlignment());
            aCell.setHorizontalAlignment(of.orElse(HorizontalAlignment.UNDEFINED));
        }
        if (aCell.getVerticalAlignment() == Element.ALIGN_UNDEFINED) {
            final Optional<VerticalAlignment> of = VerticalAlignment
                    .of(defaultCell.getVerticalAlignment());
            aCell.setVerticalAlignment(of.orElse(VerticalAlignment.UNDEFINED));
        }
    }

    /**
     * Inserts a Cell in a cell-array and reserves cells defined by row-/colspan.
     *
     * @param someRows  some rows
     * @param aCell     the cell that has to be inserted
     * @param aPosition the position where the cell has to be placed
     */
    private void placeCell(ArrayList<Row> someRows, Cell aCell, Point aPosition) throws AddCellException {
        int i;
        Row row;
        int rowCount = aPosition.x + aCell.getRowspan() - someRows.size();
        assumeTableDefaults(aCell);
        if ((aPosition.x + aCell.getRowspan()) > someRows.size()) {
            for (i = 0; i < rowCount; i++) {
                row = new Row(columns);
                someRows.add(row);
            }
        }

        // reserve cell in rows below
        for (i = aPosition.x + 1; i < (aPosition.x + aCell.getRowspan()); i++) {
            if (!someRows.get(i).reserve(aPosition.y, aCell.getColspan())) {

                // should be impossible to come here :-)
                throw new AddCellException(MessageLocalization.getComposedMessage("addcell.error.in.reserve"));
            }
        }
        row = someRows.get(aPosition.x);
        row.addElement(aCell, aPosition.y);

    }

    /**
     * Sets current col/row to valid(empty) pos after addCell/Table
     *
     * @param aLocation a location in the Table
     */
    private void setCurrentLocationToNextValidPosition(Point aLocation) {
        // set latest location to next valid position
        int i;
        int j;
        i = aLocation.x;
        j = aLocation.y;
        do {
            if ((j + 1) == columns) {    // goto next row
                i++;
                j = 0;
            } else {
                j++;
            }
        } while (
                (i < rows.size()) && (j < columns) && (rows.get(i).isReserved(j))
        );
        curPosition = new Point(i, j);
    }

    // public helper methods

    /**
     * Gets an array with the positions of the borders between every column.
     * <p>
     * This method translates the widths expressed in percentages into the x-coordinate of the borders of the columns on
     * a real document.
     *
     * @param left       this is the position of the first border at the left (getCellPadding not included)
     * @param totalWidth this is the space between the first border at the left and the last border at the right
     *                   (getCellPadding not included)
     * @return an array with border positions
     */
    public float[] getWidths(float left, float totalWidth) {
        // for x columns, there are x+1 borders
        float[] w = new float[columns + 1];
        float wPercentage;
        if (locked) {
            wPercentage = 100 * width / totalWidth;
        } else {
            wPercentage = width;
        }
        // the border at the left is calculated
        switch (alignment) {
            case Element.ALIGN_LEFT:
                w[0] = left;
                break;
            case Element.ALIGN_RIGHT:
                w[0] = left + (totalWidth * (100 - wPercentage)) / 100;
                break;
            case Element.ALIGN_CENTER:
            default:
                w[0] = left + (totalWidth * (100 - wPercentage)) / 200;
        }
        // the total available width is changed
        totalWidth = (totalWidth * wPercentage) / 100;
        // the inner borders are calculated
        for (int i = 1; i < columns; i++) {
            w[i] = w[i - 1] + (widths[i - 1] * totalWidth / 100);
        }
        // the border at the right is calculated
        w[columns] = w[0] + totalWidth;
        return w;
    }

    /**
     * Gets an <CODE>Iterator</CODE> of all the <CODE>Row</CODE>s.
     *
     * @return an <CODE>Iterator</CODE>
     */
    public Iterator<Row> iterator() {
        return rows.iterator();
    }

    /**
     * Create a PdfPTable based on this Table object.
     *
     * @return a PdfPTable object
     * @throws BadElementException on error
     */
    public PdfPTable createPdfPTable() throws BadElementException, AddCellException, IOException {
        validatePdfTableConversion();

        setAutoFillEmptyCells(true);
        complete();
        PdfPTable pdfptable = initializePdfPTable();

        configureTableProperties(pdfptable);
        addRowsToPdfPTable(pdfptable);

        return pdfptable;
    }

    private void validatePdfTableConversion() throws BadElementException {
        if (!convert2pdfptable) {
            throw new BadElementException(MessageLocalization.getComposedMessage("no.error.just.an.old.style.table"));
        }
    }

    private PdfPTable initializePdfPTable() {
        PdfPTable pdfptable = new PdfPTable(widths);
        pdfptable.setComplete(complete);
        if (isNotAddedYet()) {
            pdfptable.setSkipFirstHeader(true);
        }
        return pdfptable;
    }

    private void configureTableProperties(PdfPTable pdfptable) {
        SimpleTable tEvt = new SimpleTable();
        tEvt.setCellspacing(cellspacing);
        pdfptable.setTableEvent(tEvt);
        pdfptable.setHeaderRows(lastHeaderRow + 1);
        pdfptable.setSplitLate(cellsFitPage);
        pdfptable.setKeepTogether(tableFitsPage);

        if (!Float.isNaN(offset)) {
            pdfptable.setSpacingBefore(offset);
        }

        pdfptable.setHorizontalAlignment(alignment);

        if (locked) {
            pdfptable.setTotalWidth(width);
            pdfptable.setLockedWidth(true);
        } else {
            pdfptable.setWidthPercentage(width);
        }
    }

    private void addRowsToPdfPTable(PdfPTable pdfptable) throws AddCellException, IOException {
        for (Row row : this.rows) {
            addCellsToPdfPTable(pdfptable, row);
        }
    }

    private void addCellsToPdfPTable(PdfPTable pdfptable, Row row) throws AddCellException, IOException {
        for (int i = 0; i < row.getColumns(); i++) {
            Element cell = row.getCell(i);
            if (cell != null) {
                PdfPCell pcell = createPdfPCell(cell);
                pdfptable.addCell(pcell);
            }
        }
    }

    private PdfPCell createPdfPCell(Element cell) throws AddCellException, IOException {
        if (cell instanceof Table table) {
            return new PdfPCell(table.createPdfPTable());
        } else if (cell instanceof Cell instanceCell) {
            PdfPCell pcell = instanceCell.createPdfPCell();
            pcell.setPadding(cellpadding + cellspacing / 2f);
            SimpleCell cEvt = new SimpleCell(SimpleCell.CELL);
            cEvt.setSpacing(cellspacing * 2f);
            pcell.setCellEvent(cEvt);
            return pcell;
        } else {
            return new PdfPCell();
        }
    }

    private ArrayList<Row> generateNewTable(int lTotalRows, int lTotalColumns) {
        ArrayList<Row> newRows = new ArrayList<>(lTotalRows);

        // Inizializza le nuove righe con il numero di colonne corretto
        for (int i = 0; i < lTotalRows; i++) {
            newRows.add(new Row(lTotalColumns));
        }

        return newRows;
    }

    /**
     * Indicates if this is the first time the section is added.
     *
     * @return true if the section wasn't added yet
     * @since iText2.0.8
     */
    public boolean isNotAddedYet() {
        return notAddedYet;
    }

    /**
     * Sets the indication if the section was already added to the document.
     *
     * @param notAddedYet {@link Table#notAddedYet}
     * @since iText2.0.8
     */
    public void setNotAddedYet(boolean notAddedYet) {
        this.notAddedYet = notAddedYet;
    }

    /**
     * @see com.lowagie.text.LargeElement#flushContent()
     * @since iText 2.0.8
     */
    public void flushContent() {
        this.setNotAddedYet(false);
        ArrayList<Row> headerRows = new ArrayList<>();
        for (int i = 0; i < getLastHeaderRow() + 1; i++) {
            headerRows.add(rows.get(i));
        }
        rows = headerRows;
    }

    /**
     * @see com.lowagie.text.LargeElement#isComplete()
     * @since iText 2.0.8
     */
    public boolean isComplete() {
        return complete;
    }

    /**
     * @see com.lowagie.text.LargeElement#setComplete(boolean)
     * @since iText 2.0.8
     */
    public void setComplete(boolean complete) {
        this.complete = complete;
    }

}
