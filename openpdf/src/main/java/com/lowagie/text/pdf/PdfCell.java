/*
 * $Id: PdfCell.java 3671 2009-02-01 14:46:09Z blowagie $
 *
 * Copyright 1999, 2000, 2001, 2002 Bruno Lowagie
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
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text.pdf;

import com.lowagie.text.Anchor;
import com.lowagie.text.Cell;
import com.lowagie.text.Chapter;
import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Section;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A <CODE>PdfCell</CODE> is the PDF translation of a <CODE>Cell</CODE>.
 * <p>
 * A <CODE>PdfCell</CODE> is an <CODE>ArrayList</CODE> of <CODE>PdfLine</CODE>s.
 * <p>
 * When using variable borders ({@link Rectangle#isUseVariableBorders isUseVariableBorders()} == true),
 * the borders are drawn completely inside the cell Rectangle so that adjacent cell borders will not overlap. Otherwise,
 * the borders are drawn on top of the edges of the cell Rectangle and will overlap the borders of adjacent cells.
 *
 * @see Rectangle
 * @see Cell
 * @see PdfLine
 * @see PdfTable
 */

public class PdfCell extends Rectangle {

    public static final String UNEXPECTED_VALUE = "Unexpected value: ";

    // membervariables

    /**
     * These are the PdfLines in the Cell.
     */
    private java.util.List<PdfLine> lines;

    /**
     * These are the PdfLines in the Cell.
     */
    private PdfLine line;

    /**
     * These are the Images in the Cell.
     */
    private java.util.List<Image> images;

    /**
     * This is the leading of the lines.
     */
    private float leading;

    /**
     * This is the number of the row the cell is in.
     */
    private int rownumber;

    /**
     * This is the rowspan of the cell.
     */
    private int rowspan;

    /**
     * This is the cellspacing of the cell.
     */
    private float cellspacing;

    /**
     * This is the cellpadding of the cell.
     */
    private float cellpadding;

    /**
     * Indicates if this cell belongs to the header of a <CODE>PdfTable</CODE>
     */
    private boolean header = false;

    /**
     * This is the total height of the content of the cell.  Note that the actual cell height may be larger due to
     * another cell on the row *
     */
    private float contentHeight = 0.0f;

    /**
     * Indicates that the largest ascender height should be used to determine the height of the first line. Setting this
     * to true can help with vertical alignment problems.
     */
    private boolean useAscender;

    /**
     * Indicates that the largest descender height should be added to the height of the last line (so characters like y
     * don't dip into the border).
     */
    private boolean useDescender;

    /**
     * Adjusts the cell contents to compensate for border widths.
     */
    private boolean useBorderPadding;

    private int verticalAlignment;

    private PdfLine firstLine;
    private PdfLine lastLine;

    // constructors
    /**
     * This is the number of the group the cell is in.
     */
    private int groupNumber;

    /**
     * Constructs a <CODE>PdfCell</CODE>-object.
     *
     * @param cell        the original <CODE>Cell</CODE>
     * @param rownumber   the number of the <CODE>Row</CODE> the <CODE>Cell</CODE> was in.
     * @param left        the left border of the <CODE>PdfCell</CODE>
     * @param right       the right border of the <CODE>PdfCell</CODE>
     * @param top         the top border of the <CODE>PdfCell</CODE>
     * @param cellspacing the cellspacing of the <CODE>Table</CODE>
     * @param cellpadding the cellpadding    of the <CODE>Table</CODE>
     */

    public PdfCell(Cell cell, int rownumber, float left, float right, float top, float cellspacing, float cellpadding) {
        // constructs a Rectangle (the bottomvalue will be changed afterward)
        super(left, top, right, top);
        // copying the other Rectangle attributes from class Cell
        cloneNonPositionParameters(cell);
        this.cellpadding = cellpadding;
        this.cellspacing = cellspacing;
        this.verticalAlignment = cell.getVerticalAlignment();
        this.useAscender = cell.isUseAscender();
        this.useDescender = cell.isUseDescender();
        this.useBorderPadding = cell.isUseBorderPadding();

        // initialization of some parameters
        PdfChunk chunk;
        Element element;
        PdfChunk overflow;
        lines = new ArrayList<>();
        images = new ArrayList<>();
        leading = cell.getLeading();
        int alignment = cell.getHorizontalAlignment();
        left += cellspacing + cellpadding;
        right -= cellspacing + cellpadding;

        left += getBorderWidthInside(LEFT);
        right -= getBorderWidthInside(RIGHT);

        contentHeight = 0;

        rowspan = cell.getRowspan();

        java.util.List<PdfAction> allActions;
        int aCounter;
        // we loop over all the elements of the cell
        for (Iterator<Element> i = cell.getElements(); i.hasNext(); ) {
            element = i.next();
            switch (element.type()) {
                case Element.JPEG,
                     Element.JPEG2000,
                     Element.JBIG2,
                     Element.IMGRAW,
                     Element.IMGTEMPLATE:
                    addImage((Image) element, left, right, alignment); //
                    break;
                // if the element is a list
                case Element.LIST:
                    if (line != null && line.size() > 0) {
                        line.resetAlignment();
                        addLine(line);
                    }
                    // we loop over all the listitems
                    addList((List) element, left, right, alignment);
                    line = new PdfLine(left, right, alignment, leading);
                    break;
                // if the element is something else
                default:
                    allActions = new ArrayList<>();
                    processActions(element, null, allActions);
                    aCounter = 0;

                    float currentLineLeading = leading;
                    float currentLeft = left;
                    float currentRight = right;
                    if (element instanceof Phrase phrase) {
                        currentLineLeading = phrase.getLeading();
                    }
                    if (element instanceof Paragraph paragraph) {
                        currentLeft += paragraph.getIndentationLeft();
                        currentRight -= paragraph.getIndentationRight();
                    }
                    if (line == null) {
                        line = new PdfLine(currentLeft, currentRight, alignment, currentLineLeading);
                    }
                    // we loop over the chunks
                    java.util.List<? extends Object> chunks = element.getChunks();
                    if (chunks.isEmpty()) {
                        addLine(line); // add empty line - all cells need some lines even if they are empty
                        line = new PdfLine(currentLeft, currentRight, alignment, currentLineLeading);
                        int type = element.type();
                        if (type == Element.PARAGRAPH || type == Element.SECTION || type == Element.CHAPTER) {
                            line.resetAlignment();
                            flushCurrentLine();
                        } else {
                            throw new IllegalStateException(UNEXPECTED_VALUE + element.type());
                        }
                    } else {
                        for (Object chunk1 : chunks) {
                            Chunk c = (Chunk) chunk1;
                            chunk = new PdfChunk(c, (allActions.get(aCounter++)));
                            while ((overflow = line.add(chunk)) != null) {
                                addLine(line);
                                line = new PdfLine(currentLeft, currentRight, alignment, currentLineLeading);
                                chunk = overflow;
                            }
                        }
                        int type = element.type();
                        if (type == Element.PARAGRAPH || type == Element.SECTION || type == Element.CHAPTER) {
                            line.resetAlignment();
                            flushCurrentLine();
                        } else {
                            throw new IllegalStateException(UNEXPECTED_VALUE + element.type());
                        }
                    }
                    // if the element is a paragraph, section or chapter, we reset the alignment and add the line
            }
        }
        flushCurrentLine();
        if (lines.size() > cell.getMaxLines()) {
            while (lines.size() > cell.getMaxLines()) {
                removeLine(lines.size() - 1);
            }
            if (cell.getMaxLines() > 0) {
                String more = cell.getShowTruncation();
                if (more != null && !more.isEmpty()) {
                    // Denote that the content has been truncated
                    lastLine = lines.get(lines.size() - 1);
                    if (lastLine.size() >= 0) {
                        PdfChunk lastChunk = lastLine.getChunk(lastLine.size() - 1);
                        float moreWidth = new PdfChunk(more, lastChunk).width();
                        while (!lastChunk.toString().isEmpty() && lastChunk.width() + moreWidth > right - left) {
                            // Remove characters to leave room for the 'more' indicator
                            lastChunk.setValue(lastChunk.toString().substring(0, lastChunk.length() - 1));
                        }
                        lastChunk.setValue(lastChunk.toString() + more);
                    } else {
                        lastLine.add(new PdfChunk(new Chunk(more), null));
                    }
                }
            }
        }
        // we set some additional parameters
        if (useDescender && lastLine != null) {
            contentHeight -= lastLine.getDescender();
        }

        // adjust first line height so that it touches the top
        if (!lines.isEmpty()) {
            firstLine = lines.get(0);
            float firstLineRealHeight = firstLineRealHeight();
            contentHeight -= firstLine.height();
            firstLine.height = firstLineRealHeight;
            contentHeight += firstLineRealHeight;
        }

        float newBottom = top - contentHeight - (2f * cellpadding()) - (2f * cellspacing());
        newBottom -= getBorderWidthInside(TOP) + getBorderWidthInside(BOTTOM);
        setBottom(newBottom);

        this.rownumber = rownumber;
    }

    // overriding of the Rectangle methods

    private void addList(List list, float left, float right, int alignment) {
        PdfChunk chunk;
        PdfChunk overflow;
        java.util.List<PdfAction> allActions = new ArrayList<>();
        processActions(list, null, allActions);
        int aCounter = 0;
        for (Element o1 : list.getItems()) {
            switch (o1.type()) {
                case Element.LISTITEM:
                    ListItem item = (ListItem) o1;
                    line = new PdfLine(left + item.getIndentationLeft(), right, alignment, item.getLeading());
                    line.setListItem(item);
                    for (Element o : item.getChunks()) {
                        chunk = new PdfChunk((Chunk) o, allActions.get(aCounter++));
                        while ((overflow = line.add(chunk)) != null) {
                            addLine(line);
                            line = new PdfLine(left + item.getIndentationLeft(), right, alignment, item.getLeading());
                            chunk = overflow;
                        }
                        line.resetAlignment();
                        addLine(line);
                        line = new PdfLine(left + item.getIndentationLeft(), right, alignment, leading);
                    }
                    break;
                case Element.LIST:
                    List sublist = (List) o1;
                    addList(sublist, left + sublist.getIndentationLeft(), right, alignment);
                    break;
                default:
                    throw new IllegalStateException(UNEXPECTED_VALUE + o1.type());
            }
        }
    }

    /**
     * Returns the lower left x-coordinate.
     *
     * @return the lower left x-coordinate
     */

    @Override
    public float getLeft() {
        return super.getLeft(cellspacing);
    }

    /**
     * Returns the upper right x-coordinate.
     *
     * @return the upper right x-coordinate
     */

    @Override
    public float getRight() {
        return super.getRight(cellspacing);
    }

    /**
     * Returns the upper right y-coordinate.
     *
     * @return the upper right y-coordinate
     */

    @Override
    public float getTop() {
        return super.getTop(cellspacing);
    }

    /**
     * Returns the lower left y-coordinate.
     *
     * @return the lower left y-coordinate
     */

    @Override
    public float getBottom() {
        return super.getBottom(cellspacing);
    }

    // methods

    /**
     * Sets the bottom of the Rectangle and determines the proper {link #verticalOffset} to appropriately align the
     * contents vertically.
     *
     * @param value the lower-left y-coordinate of the rectangle
     */
    @Override
    public void setBottom(float value) {
        super.setBottom(value);
        float firstLineRealHeight = firstLineRealHeight();

        float totalHeight = ury - value; // can't use top (already compensates for cellspacing)
        float nonContentHeight = (cellpadding() * 2f) + (cellspacing() * 2f);
        nonContentHeight += getBorderWidthInside(TOP) + getBorderWidthInside(BOTTOM);

        float interiorHeight = totalHeight - nonContentHeight;
        float extraHeight = switch (verticalAlignment) {
            case Element.ALIGN_BOTTOM -> interiorHeight - contentHeight;
            case Element.ALIGN_MIDDLE -> (interiorHeight - contentHeight) / 2.0f;
            default ->    // ALIGN_TOP
                    0f;
        };

        extraHeight += cellpadding() + cellspacing();
        extraHeight += getBorderWidthInside(TOP);
        if (firstLine != null) {
            firstLine.height = firstLineRealHeight + extraHeight;
            contentHeight += extraHeight;
        }
    }

    private void addLine(PdfLine line) {
        lines.add(line);
        contentHeight += line.height();
        lastLine = line;
        this.line = null;
    }

    private PdfLine removeLine(int index) {
        PdfLine oldLine = lines.remove(index);
        contentHeight -= oldLine.height();
        if ((index == 0) && (!lines.isEmpty()))  {
                firstLine = lines.get(0);
                float firstLineRealHeight = firstLineRealHeight();
                contentHeight -= firstLine.height();
                firstLine.height = firstLineRealHeight;
                contentHeight += firstLineRealHeight;
        }
        return oldLine;
    }

    private void flushCurrentLine() {
        if (line != null && line.size() > 0) {
            addLine(line);
        }
    }

    /**
     * Calculates what the height of the first line should be so that the content will be flush with the top.  For text,
     * this is the height of the ascender.  For an image, it is the actual height of the image.
     *
     * @return the real height of the first line
     */
    private float firstLineRealHeight() {
        float firstLineRealHeight = 0f;
        if (firstLine != null) {
            PdfChunk chunk = firstLine.getChunk(0);
            if (chunk != null) {
                Image image = chunk.getImage();
                if (image != null) {
                    firstLineRealHeight = firstLine.getChunk(0).getImage().getScaledHeight();
                } else {
                    firstLineRealHeight = useAscender ? firstLine.getAscender() : leading;
                }
            }
        }
        return firstLineRealHeight;
    }

    /**
     * Gets the amount of the border for the specified side that is inside the Rectangle. For non-variable width borders
     * this is only 1/2 the border width on that side.  This always returns 0 if {@link #useBorderPadding} is false;
     *
     * @param side the side to check.  One of the side constants in {@link Rectangle}
     * @return the borderwidth inside the cell
     */
    private float getBorderWidthInside(int side) {
        float width = 0f;
        if (useBorderPadding) {
            width = switch (side) {
                case Rectangle.LEFT -> getBorderWidthLeft();
                case Rectangle.RIGHT -> getBorderWidthRight();
                case Rectangle.TOP -> getBorderWidthTop();
                default ->    // default and BOTTOM
                        getBorderWidthBottom();
            };
            // non-variable (original style) borders overlap the rectangle (only 1/2 counts)
            if (!isUseVariableBorders()) {
                width = width / 2f;
            }
        }
        return width;
    }

    /**
     * Adds an image to this Cell.
     *
     * @param i           the image to add
     * @param left        the left border
     * @param right       the right border
     * @param alignment   horizontal alignment (constant from Element class)
     * @return the height of the image
     */

    private float addImage(Image i, float left, float right, int alignment) {
        Image image = Image.getInstance(i);
        if (image.getScaledWidth() > right - left) {
            image.scaleToFit(right - left, Float.MAX_VALUE);
        }
        flushCurrentLine();
        if (line == null) {
            line = new PdfLine(left, right, alignment, leading);
        }
        PdfLine imageLine = line;

        // left and right in chunk is relative to the start of the line
        right = right - left;
        left = 0f;

        if ((image.getAlignment() & Image.RIGHT) == Image.RIGHT) {
            left = right - image.getScaledWidth();
        } else if ((image.getAlignment() & Image.MIDDLE) == Image.MIDDLE) {
            left = left + ((right - left - image.getScaledWidth()) / 2f);
        }
        Chunk imageChunk = new Chunk(image, left, 0);
        imageLine.add(new PdfChunk(imageChunk, null));
        addLine(imageLine);
        return imageLine.height();
    }

    /**
     * Checks if this cell belongs to the header of a <CODE>PdfTable</CODE>.
     *
     * @return <CODE>void</CODE>
     */

    boolean isHeader() {
        return header;
    }

    /**
     * Indicates that this cell belongs to the header of a <CODE>PdfTable</CODE>.
     */

    void setHeader() {
        header = true;
    }

    /**
     * Checks if the cell may be removed.
     * <p>
     * Headers may always be removed, even if they are drawn only partially: they will be repeated on each following
     * page anyway!
     *
     * @return <CODE>true</CODE> if all the lines are already drawn; <CODE>false</CODE> otherwise.
     */

    boolean mayBeRemoved() {
        return (header || (lines.isEmpty() && images.isEmpty()));
    }

    /**
     * Returns the number of lines in the cell.
     *
     * @return a value
     */

    public int size() {
        return lines.size();
    }

    /**
     * Returns the total height of all the lines in the cell.
     *
     * @return a value
     */
    private float remainingLinesHeight() {
        if (lines.isEmpty()) {
            return 0;
        }
        float result = 0;
        PdfLine lineM;
        for (PdfLine line1 : lines) {
            lineM = line1;
            result += lineM.height();
        }
        return result;
    }

    // methods to retrieve membervariables

    /**
     * Returns the height needed to draw the remaining text.
     *
     * @return a height
     */

    public float remainingHeight() {
        float result = 0f;
        for (Image image1 : images) {
            result += image1.getScaledHeight();
        }
        return remainingLinesHeight() + cellspacing + 2 * cellpadding + result;
    }

    /**
     * Gets the leading of a cell.
     *
     * @return the leading of the lines is the cell.
     */

    public float leading() {
        return leading;
    }

    /**
     * Gets the number of the row this cell is in..
     *
     * @return a number
     */

    public int rownumber() {
        return rownumber;
    }

    /**
     * Gets the rowspan of a cell.
     *
     * @return the rowspan of the cell
     */

    public int rowspan() {
        return rowspan;
    }

    /**
     * Gets the cellspacing of a cell.
     *
     * @return a value
     */

    public float cellspacing() {
        return cellspacing;
    }

    /**
     * Gets the cellpadding of a cell.
     *
     * @return a value
     */

    public float cellpadding() {
        return cellpadding;
    }

    /**
     * Processes all actions contained in the cell.
     *
     * @param element    an element in the cell
     * @param action     an action that should be coupled to the cell
     * @param allActions a list of PdfAction to execute
     */

    protected void processActions(Element element, PdfAction action, java.util.List<PdfAction> allActions) {
        if (element.type() == Element.ANCHOR) {
            String url = ((Anchor) element).getReference();
            if (url != null) {
                action = new PdfAction(url);
            }
        }

        Iterator<Element> i;
        switch (element.type()) {
            case Element.PHRASE,
                 Element.SECTION,
                 Element.ANCHOR,
                 Element.CHAPTER,
                 Element.LISTITEM,
                 Element.PARAGRAPH:
                // Check if element can be cast to a composite element like Section or Chapter
                if (element instanceof Section sectionElement) {
                    i = sectionElement.iterator(); // Safely iterate over Section elements
                } else if (element instanceof Chapter chapterElement) {
                    i = chapterElement.iterator(); // Safely iterate over Chapter elements
                } else {
                    // For other elements like Paragraph, check for chunks
                    i = element.getChunks().iterator(); // Safely iterate over chunks of the element
                }

                while (i.hasNext()) {
                    processActions(i.next(), action, allActions);
                }
                break;

            case Element.CHUNK:
                allActions.add(action);
                break;

            case Element.LIST:
                for (i = ((com.lowagie.text.List) element).getItems().iterator(); i.hasNext(); ) {
                    processActions(i.next(), action, allActions);
                }
                break;

            default:
                // Handle chunks for the default case
                int n = element.getChunks().size();
                while (n-- > 0) {
                    allActions.add(action);
                }
                break;
        }
    }


    /**
     * Gets the number of the group this cell is in..
     *
     * @return a number
     */

    public int getGroupNumber() {
        return groupNumber;
    }

    /**
     * Sets the group number.
     *
     * @param number
     */

    void setGroupNumber(int number) {
        groupNumber = number;
    }

    /**
     * Gets the value of useAscender
     *
     * @return useAscender
     */
    public boolean isUseAscender() {
        return useAscender;
    }

    /**
     * Sets the value of useAscender.
     *
     * @param use use ascender height if true
     */
    public void setUseAscender(boolean use) {
        useAscender = use;
    }

    /**
     * gets the value of useDescender
     *
     * @return useDescender
     */
    public boolean isUseDescender() {
        return useDescender;
    }

    /**
     * Sets the value of useDescender.
     *
     * @param use use descender height if true
     */
    public void setUseDescender(boolean use) {
        useDescender = use;
    }

    /**
     * Gets the value of useBorderPadding.
     *
     * @return useBorderPadding
     */
    public boolean isUseBorderPadding() {
        return useBorderPadding;
    }

    /**
     * Sets the value of useBorderPadding.
     *
     * @param use adjust layout for borders if true
     */
    public void setUseBorderPadding(boolean use) {
        useBorderPadding = use;
    }

}
