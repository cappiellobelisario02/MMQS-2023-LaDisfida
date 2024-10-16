/*
 * $Id: SimpleCell.java 4065 2009-09-16 23:09:11Z psoares33 $
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
import com.lowagie.text.alignment.VerticalAlignment;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import java.util.ArrayList;
import java.util.Optional;


/**
 * Rectangle that can be used for Cells. This Rectangle is padded and knows how to draw itself in a PdfPTable or
 * PdfPcellEvent.
 */
public class SimpleCell extends Rectangle implements PdfPCellEvent, TextElementArray {

    // constants
    /**
     * the CellAttributes object represents a row.
     */
    public static final boolean ROW = true;
    /**
     * the CellAttributes object represents a cell.
     */
    public static final boolean CELL = false;

    // member variables
    /**
     * Indicates that the largest ascender height should be used to determine the height of the first line.  Note that
     * this only has an effect when rendered to PDF.  Setting this to true can help with vertical getAlignment problems.
     */
    protected boolean useAscender = false;
    /**
     * Indicates that the largest descender height should be added to the height of the last line (so characters like y
     * don't dip into the border).   Note that this only has an effect when rendered to PDF.
     */
    protected boolean useDescender = false;
    /**
     * Adjusts the cell contents to compensate for border widths.  Note that this only has an effect when rendered to
     * PDF.
     */
    protected boolean useBorderPadding;
    /**
     * the content of the Cell.
     */
    private java.util.List<Element> content = new ArrayList<>();
    /**
     * the width of the Cell.
     */
    private float width = 0f;
    /**
     * the widthpercentage of the Cell.
     */
    private float widthpercentage = 0f;
    /**
     * an extra spacing variable
     */
    private float spacingLeft = Float.NaN;
    /**
     * an extra spacing variable
     */
    private float spacingRight = Float.NaN;
    /**
     * an extra spacing variable
     */
    private float spacingTop = Float.NaN;
    /**
     * an extra spacing variable
     */
    private float spacingBottom = Float.NaN;
    /**
     * an extra padding variable
     */
    private float paddingLeft = Float.NaN;
    /**
     * an extra padding variable
     */
    private float paddingRight = Float.NaN;
    /**
     * an extra padding variable
     */
    private float paddingTop = Float.NaN;
    /**
     * an extra padding variable
     */
    private float paddingBottom = Float.NaN;
    /**
     * the colspan of a Cell
     */
    private int colspan = 1;
    /**
     * horizontal getAlignment inside the Cell.
     */
    private int horizontalAlignment = Element.ALIGN_UNDEFINED;
    /**
     * vertical getAlignment inside the Cell.
     */
    private int verticalAlignment = Element.ALIGN_UNDEFINED;
    /**
     * indicates if these are the attributes of a single Cell (false) or a group of Cells (true).
     */
    private boolean cellgroup = false;

    /**
     * A CellAttributes object is always constructed without any dimensions. Dimensions are defined after creation.
     *
     * @param row only true if the CellAttributes object represents a row.
     */
    public SimpleCell(boolean row) {
        super(0f, 0f, 0f, 0f);
        cellgroup = row;
        setBorder(BOX);
    }

    /**
     * Adds content to this object.
     *
     * @param element an object of getTypeImpl {@link Element} that you want to add to the cell
     * @throws BadElementException on error
     */
    public void addElement(Element element) throws BadElementException {
        if (cellgroup) {
            if (element instanceof SimpleCell simpleCell) {
                if (simpleCell.isCellgroup()) {
                    throw new BadElementException(
                            MessageLocalization.getComposedMessage("you.can.t.add.one.row.to.another.row"));
                }
                content.add(element);
                return;
            } else {
                throw new BadElementException(
                        MessageLocalization.getComposedMessage("you.can.only.add.cells.to.rows.no.objects.of.getTypeImpl.1",
                                element.getClass().getName()));
            }
        }
        if (element.getTypeImpl() == Element.PARAGRAPH
                || element.getTypeImpl() == Element.PHRASE
                || element.getTypeImpl() == Element.ANCHOR
                || element.getTypeImpl() == Element.CHUNK
                || element.getTypeImpl() == Element.LIST
                || element.getTypeImpl() == Element.MARKED
                || element.getTypeImpl() == Element.JPEG
                || element.getTypeImpl() == Element.JPEG2000
                || element.getTypeImpl() == Element.JBIG2
                || element.getTypeImpl() == Element.IMGRAW
                || element.getTypeImpl() == Element.IMGTEMPLATE) {
            content.add(element);
        } else {
            throw new BadElementException(
                    MessageLocalization.getComposedMessage("you.can.t.add.an.element.of.getTypeImpl.1.to.a.simplecell",
                            element.getClass().getName()));
        }
    }

    /**
     * Creates a Cell with these attributes.
     *
     * @param rowAttributes an object of getTypeImpl {@link SimpleCell} whose attributes are to be used to create new cell
     * @return a cell based on these attributes.
     * @throws BadElementException on error
     */
    public Cell createCell(SimpleCell rowAttributes) throws BadElementException {
        Cell cell = new Cell();
        cell.cloneNonPositionParameters(rowAttributes);
        cell.softCloneNonPositionParameters(this);
        cell.setColspan(colspan);
        Optional<HorizontalAlignment> hAlignment = HorizontalAlignment.of(horizontalAlignment);
        cell.setHorizontalAlignment(hAlignment.orElse(HorizontalAlignment.UNDEFINED));
        Optional<VerticalAlignment> vAlignment = VerticalAlignment.of(verticalAlignment);
        cell.setVerticalAlignment(vAlignment.orElse(VerticalAlignment.UNDEFINED));
        cell.setUseAscender(useAscender);
        cell.setUseBorderPadding(useBorderPadding);
        cell.setUseDescender(useDescender);
        Element element;
        for (Element o : content) {
            element = o;
            cell.addElement(element);
        }
        return cell;
    }

    /**
     * Creates a PdfPCell with these attributes.
     *
     * @param rowAttributes an object of getTypeImpl {@link SimpleCell} whose attributes are to be used to create new cell
     * @return a PdfPCell based on these attributes.
     */
    public PdfPCell createPdfPCell(SimpleCell rowAttributes) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(NO_BORDER);
        SimpleCell tmp = new SimpleCell(CELL);
        tmp.setSpacingLeft(spacingLeft);
        tmp.setSpacingRight(spacingRight);
        tmp.setSpacingTop(spacingTop);
        tmp.setSpacingBottom(spacingBottom);
        tmp.cloneNonPositionParameters(rowAttributes);
        tmp.softCloneNonPositionParameters(this);
        cell.setCellEvent(tmp);
        cell.setHorizontalAlignment(rowAttributes.horizontalAlignment);
        cell.setVerticalAlignment(rowAttributes.verticalAlignment);
        cell.setUseAscender(rowAttributes.useAscender);
        cell.setUseBorderPadding(rowAttributes.useBorderPadding);
        cell.setUseDescender(rowAttributes.useDescender);
        cell.setColspan(colspan);
        if (horizontalAlignment != Element.ALIGN_UNDEFINED) {
            cell.setHorizontalAlignment(horizontalAlignment);
        }
        if (verticalAlignment != Element.ALIGN_UNDEFINED) {
            cell.setVerticalAlignment(verticalAlignment);
        }
        if (useAscender) {
            cell.setUseAscender(useAscender);
        }
        if (useBorderPadding) {
            cell.setUseBorderPadding(useBorderPadding);
        }
        if (useDescender) {
            cell.setUseDescender(useDescender);
        }
        float p;
        float spLeft = spacingLeft;
        if (Float.isNaN(spLeft)) {
            spLeft = 0f;
        }
        float spRight = spacingRight;
        if (Float.isNaN(spRight)) {
            spRight = 0f;
        }
        float spTop = spacingTop;
        if (Float.isNaN(spTop)) {
            spTop = 0f;
        }
        float spBottom = spacingBottom;
        if (Float.isNaN(spBottom)) {
            spBottom = 0f;
        }
        Result result = new Result(spLeft, spRight, spTop, spBottom);
        p = paddingLeft;
        if (Float.isNaN(p)) {
            p = 0f;
        }
        cell.setPaddingLeft(p + result.spLeft());
        p = paddingRight;
        if (Float.isNaN(p)) {
            p = 0f;
        }
        cell.setPaddingRight(p + result.spRight());
        p = paddingTop;
        if (Float.isNaN(p)) {
            p = 0f;
        }
        cell.setPaddingTop(p + result.spTop());
        p = paddingBottom;
        if (Float.isNaN(p)) {
            p = 0f;
        }
        cell.setPaddingBottom(p + result.spBottom());
        Element element;
        for (Element o : content) {
            element = o;
            cell.addElement(element);
        }
        return cell;
    }

    private record Result(float spLeft, float spRight, float spTop, float spBottom) {

    }

    /**
     * @see com.lowagie.text.pdf.PdfPCellEvent#cellLayout(com.lowagie.text.pdf.PdfPCell, com.lowagie.text.Rectangle,
     * com.lowagie.text.pdf.PdfContentByte[])
     */
    public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
        float spLeft = spacingLeft;
        if (Float.isNaN(spLeft)) {
            spLeft = 0f;
        }
        float spRight = spacingRight;
        if (Float.isNaN(spRight)) {
            spRight = 0f;
        }
        float spTop = spacingTop;
        if (Float.isNaN(spTop)) {
            spTop = 0f;
        }
        float spBottom = spacingBottom;
        if (Float.isNaN(spBottom)) {
            spBottom = 0f;
        }
        Rectangle rect = new Rectangle(position.getLeft(spLeft), position.getBottom(spBottom),
                position.getRight(spRight), position.getTop(spTop));
        rect.cloneNonPositionParameters(this);
        canvases[PdfPTable.BACKGROUNDCANVAS].rectangle(rect.llx(), rect.lly(), rect.urx(), rect.ury());
        rect.setBackgroundColor(null);
        canvases[PdfPTable.LINECANVAS].rectangle(rect.llx(), rect.lly(), rect.urx(), rect.ury());
    }

    /**
     * Sets the padding parameters if they are undefined.
     *
     * @param padding padding that will be set
     */
    public void setPadding(float padding) {
        if (Float.isNaN(paddingRight)) {
            setPaddingRight(padding);
        }
        if (Float.isNaN(paddingLeft)) {
            setPaddingLeft(padding);
        }
        if (Float.isNaN(paddingTop)) {
            setPaddingTop(padding);
        }
        if (Float.isNaN(paddingBottom)) {
            setPaddingBottom(padding);
        }
    }

    /**
     * @return Returns the colspan.
     */
    public int getColspan() {
        return colspan;
    }

    /**
     * @param colspan The colspan to set.
     */
    public void setColspan(int colspan) {
        if (colspan > 0) {
            this.colspan = colspan;
        }
    }

    /**
     * @return Returns the padding_bottom.
     */
    public float getPaddingBottom() {
        return paddingBottom;
    }

    /**
     * @param paddingBottom The padding_bottom to set.
     */
    public void setPaddingBottom(float paddingBottom) {
        this.paddingBottom = paddingBottom;
    }

    /**
     * @return Returns the padding_left.
     */
    public float getPaddingLeft() {
        return paddingLeft;
    }

    /**
     * @param paddingLeft The padding_left to set.
     */
    public void setPaddingLeft(float paddingLeft) {
        this.paddingLeft = paddingLeft;
    }

    /**
     * @return Returns the padding_right.
     */
    public float getPaddingRight() {
        return paddingRight;
    }

    /**
     * @param paddingRight The padding_right to set.
     */
    public void setPaddingRight(float paddingRight) {
        this.paddingRight = paddingRight;
    }

    /**
     * @return Returns the padding_top.
     */
    public float getPaddingTop() {
        return paddingTop;
    }

    /**
     * @param paddingTop The padding_top to set.
     */
    public void setPaddingTop(float paddingTop) {
        this.paddingTop = paddingTop;
    }

    /**
     * @return Returns the spacing.
     */
    public float getSpacingLeft() {
        return spacingLeft;
    }

    /**
     * @param spacing The spacing to set.
     */
    public void setSpacingLeft(float spacing) {
        this.spacingLeft = spacing;
    }

    /**
     * @return Returns the spacing.
     */
    public float getSpacingRight() {
        return spacingRight;
    }

    /**
     * @param spacing The spacing to set.
     */
    public void setSpacingRight(float spacing) {
        this.spacingRight = spacing;
    }

    /**
     * @return Returns the spacing.
     */
    public float getSpacingTop() {
        return spacingTop;
    }

    /**
     * @param spacing The spacing to set.
     */
    public void setSpacingTop(float spacing) {
        this.spacingTop = spacing;
    }

    /**
     * @return Returns the spacing.
     */
    public float getSpacingBottom() {
        return spacingBottom;
    }

    /**
     * @param spacing The spacing to set.
     */
    public void setSpacingBottom(float spacing) {
        this.spacingBottom = spacing;
    }

    /**
     * @param spacing The spacing to set.
     */
    public void setSpacing(float spacing) {
        this.spacingLeft = spacing;
        this.spacingRight = spacing;
        this.spacingTop = spacing;
        this.spacingBottom = spacing;
    }

    /**
     * @return Returns the cellgroup.
     */
    public boolean isCellgroup() {
        return cellgroup;
    }

    /**
     * @param cellgroup The cellgroup to set.
     */
    public void setCellgroup(boolean cellgroup) {
        this.cellgroup = cellgroup;
    }

    /**
     * @return Returns the horizontal getAlignment.
     */
    public int getHorizontalAlignment() {
        return horizontalAlignment;
    }

    /**
     * @param horizontalAlignment The horizontalAlignment to set.
     */
    public void setHorizontalAlignment(int horizontalAlignment) {
        this.horizontalAlignment = horizontalAlignment;
    }

    /**
     * @return Returns the vertical getAlignment.
     */
    public int getVerticalAlignment() {
        return verticalAlignment;
    }

    /**
     * @param verticalAlignment The verticalAligment to set.
     */
    public void setVerticalAlignment(int verticalAlignment) {
        this.verticalAlignment = verticalAlignment;
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
     * @return Returns the useAscender.
     */
    public boolean isUseAscender() {
        return useAscender;
    }

    /**
     * @param useAscender The useAscender to set.
     */
    public void setUseAscender(boolean useAscender) {
        this.useAscender = useAscender;
    }

    /**
     * @return Returns the useBorderPadding.
     */
    public boolean isUseBorderPadding() {
        return useBorderPadding;
    }

    /**
     * @param useBorderPadding The useBorderPadding to set.
     */
    public void setUseBorderPadding(boolean useBorderPadding) {
        this.useBorderPadding = useBorderPadding;
    }

    /**
     * @return Returns the useDescender.
     */
    public boolean isUseDescender() {
        return useDescender;
    }

    /**
     * @param useDescender The useDescender to set.
     */
    public void setUseDescender(boolean useDescender) {
        this.useDescender = useDescender;
    }

    /**
     * @return Returns the content.
     */
    java.util.List<Element> getContent() {
        return content;
    }

    /**
     * @see com.lowagie.text.TextElementArray#add(Element)
     */
    public boolean add(Element o) {
        try {
            addElement(o);
            return true;
        } catch (ClassCastException e) {
            return false;
        } catch (BadElementException e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * @see com.lowagie.text.Element#getTypeImpl()
     */
    @Override
    public int getTypeImpl() {
        return Element.CELL;
    }
}
