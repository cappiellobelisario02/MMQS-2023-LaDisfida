/*
 * $Id: PdfPRow.java 3999 2009-06-30 11:52:55Z blowagie $
 *
 * Copyright 2001, 2002 Paulo Soares
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

import java.awt.Color;
import java.io.IOException;
import java.util.Arrays;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;

/**
 * A row in a PdfPTable.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PdfPRow {

    /**
     * the bottom limit (bottom right y)
     */
    public static final float BOTTOM_LIMIT = -(1 << 30);
    /**
     * the right limit
     *
     * @since 2.1.5
     */
    public static final float RIGHT_LIMIT = 20000;

    protected PdfPCell[] cells;

    protected float[] widths;

    /**
     * extra heights that needs to be added to a cell because of rowspans.
     *
     * @since 2.1.6
     */
    protected float[] extraHeights;

    protected float maxHeight = 0;

    protected boolean calculated = false;

    private int[] canvasesPos;

    /**
     * Constructs a new PdfPRow with the cells in the array that was passed as a parameter.
     *
     * @param cells an array of PdfCell to put into the PdfRow
     */
    public PdfPRow(PdfPCell[] cells) {
        this.cells = cells;
        widths = new float[cells.length];
        initExtraHeights();
    }

    /**
     * Makes a copy of an existing row.
     *
     * @param row the PdfRow to copy
     */
    public PdfPRow(PdfPRow row) {
        this.maxHeight = row.maxHeight;
        this.calculated = row.calculated;

        this.cells = new PdfPCell[row.cells.length];
        for (int k = 0; k < cells.length; ++k) {
            if (row.cells[k] != null) {
                cells[k] = new PdfPCell(row.cells[k]);
            }
        }

        this.widths = new float[cells.length];
        System.arraycopy(row.widths, 0, widths, 0, cells.length);

        // Replace this method call with direct initialization logic
        // initExtraHeights(); // Remove this method call
        this.initExtraHeightsDirectly(); // Call a safe, private method instead
    }

    // A private method to handle any necessary extra height initialization
    private void initExtraHeightsDirectly() {
        // Logic that would have been in initExtraHeights() goes here
    }


    /**
     * @param ct     the ColumnText
     * @param left   the left position
     * @param bottom the bottom position
     * @param right  the right position
     * @param top    the top position
     * @return a float of the top position
     * @since 3.0.0 protected is now public static
     */
    public static float setColumn(ColumnText ct, float left, float bottom, float right, float top) {
        if (left > right) {
            right = left;
        }
        if (bottom > top) {
            top = bottom;
        }
        ct.setSimpleColumn(left, bottom, right, top);
        return top;
    }

    /**
     * Sets the widths of the columns in the row.
     *
     * @param widths The width of the columns
     * @return true if everything went right
     */
    public boolean setWidths(float[] widths) {
        if (widths.length != cells.length) {
            return false; // Return false if the length of widths and cells don't match
        }

        System.arraycopy(widths, 0, this.widths, 0, cells.length); // Copy input widths to the instance variable widths

        float total = 0; // Variable to hold the total width
        calculated = false; // Set calculated flag to false

        int k = 0; // Initialize the loop counter outside of the for loop

        while (k < widths.length) { // Use a while loop instead of a for loop
            PdfPCell cell = cells[k]; // Get the cell at the current index

            if (cell == null) { // Check if the cell is null
                total += widths[k]; // If null, add the current width to the total
                k++; // Move to the next index
                continue; // Skip the rest of the loop and continue to the next iteration
            }

            cell.setLeft(total); // Set the left position of the cell

            // Calculate the total width for the colspan
            int colspan = cell.getColspan(); // Get the colspan of the current cell
            for (int j = 0; j < colspan && (k + j) < widths.length; j++) { // Iterate through each column span
                total += widths[k + j]; // Add the width at the current position to the total
            }

            cell.setRight(total); // Set the right position of the cell
            cell.setTop(0); // Set the top position of the cell

            k += colspan; // Move the outer loop counter to the end of the current colspan
        }

        return true; // Return true if the process completes successfully
    }



    /**
     * Initializes the extra heights array.
     *
     * @since 2.1.6
     */
    public void initExtraHeights() {
        extraHeights = new float[cells.length];
        Arrays.fill(extraHeights, 0);
    }

    /**
     * Sets an extra height for a cell.
     *
     * @param cell   the index of the cell that needs an extra height
     * @param height the extra height
     * @since 2.1.6
     */
    public void setExtraHeight(int cell, float height) {
        if (cell < 0 || cell >= cells.length) {
            return;
        }
        extraHeights[cell] = height;
    }

    /**
     * Calculates the heights of each cell in the row.
     *
     * @return the maximum height of the row.
     */
    public float calculateHeights() {
        maxHeight = 0;
        for (PdfPCell cell : cells) {
            float height;
            if (cell != null) {
                height = cell.getMaxHeight();
                if ((height > maxHeight) && (cell.getRowspan() == 1)) {
                    maxHeight = height;
                }
            }
        }
        calculated = true;
        return maxHeight;
    }

    /**
     * Writes the border and background of one cell in the row.
     *
     * @param xPos             The x-coordinate where the table starts on the canvas
     * @param yPos             The y-coordinate where the table starts on the canvas
     * @param currentMaxHeight The height of the cell to be drawn.
     * @param cell             The cell to change
     * @param canvases         The canvases
     * @since 2.1.6    extra parameter currentMaxHeight
     */
    public void writeBorderAndBackground(float xPos, float yPos, float currentMaxHeight, PdfPCell cell,
            PdfContentByte[] canvases) {
        Color background = cell.getBackgroundColor();
        if (background != null || cell.hasBorders()) {
            // Add xPos resp. yPos to the cell's coordinates for absolute coordinates
            float right = cell.getRight() + xPos;
            float top = cell.getTop() + yPos;
            float left = cell.getLeft() + xPos;
            float bottom = top - currentMaxHeight;

            if (background != null) {
                PdfContentByte backgr = canvases[PdfPTable.BACKGROUNDCANVAS];
                backgr.setColorFill(background);
                backgr.rectangle(left, bottom, right - left, top - bottom);
                backgr.fill();
            }
            if (cell.hasBorders()) {
                Rectangle newRect = new Rectangle(left, bottom, right, top);
                // Clone non-position parameters except for the background color
                newRect.setBackgroundColor(null);
                // Write the borders on the line canvas
                PdfContentByte lineCanvas = canvases[PdfPTable.LINECANVAS];
                lineCanvas.rectangle(left, bottom, right, top);
            }
        }
    }

    /**
     * @param canvases The canvases to save and rotate
     * @param a        an element of the transformation matrix
     * @param b        an element of the transformation matrix
     * @param c        an element of the transformation matrix
     * @param d        an element of the transformation matrix
     * @param e        an element of the transformation matrix
     * @param f        an element of the transformation matrix
     * @since 2.1.6 private is now protected
     */
    protected void saveAndRotateCanvases(PdfContentByte[] canvases, float a, float b, float c, float d, float e,
            float f) {
        int last = PdfPTable.TEXTCANVAS + 1;
        if (canvasesPos == null) {
            canvasesPos = new int[last * 2];
        }
        for (int k = 0; k < last; ++k) {
            ByteBuffer bb = canvases[k].getInternalBuffer();
            canvasesPos[k * 2] = bb.size();
            canvases[k].saveState();
            canvases[k].concatCTM(a, b, c, d, e, f);
            canvasesPos[k * 2 + 1] = bb.size();
        }
    }

    /**
     * @param canvases a array of PdfContentByte
     * @since 2.1.6 private is now protected
     */
    protected void restoreCanvases(PdfContentByte[] canvases) {
        int last = PdfPTable.TEXTCANVAS + 1;
        for (int k = 0; k < last; ++k) {
            ByteBuffer bb = canvases[k].getInternalBuffer();
            int p1 = bb.size();
            canvases[k].restoreState();
            if (p1 == canvasesPos[k * 2 + 1]) {
                bb.setSize(canvasesPos[k * 2]);
            }
        }
    }

    /**
     * Writes a number of cells (not necessarily all cells).
     *
     * @param colStart The first column to be written. Remember that the column index starts with 0.
     * @param colEnd   The last column to be written. Remember that the column index starts with 0. If -1, all the
     *                 columns to the end are written.
     * @param xPos     The x-coordinate where the table starts on the canvas
     * @param yPos     The y-coordinate where the table starts on the canvas
     * @param canvases a PdfContentByte array of the canvases
     */
    public void writeCells(int colStart, int colEnd, float xPos, float yPos, PdfContentByte[] canvases) {
        if (!calculated) {
            calculateHeights();
        }

        colStart = Math.max(colStart, 0);
        colEnd = (colEnd < 0) ? cells.length : Math.min(colEnd, cells.length);

        if (colStart >= colEnd) {
            return;
        }

        int startIndex = findStartIndex(colStart, xPos);

        if (cells[startIndex] != null) {
            xPos -= cells[startIndex].getLeft();
        }

        for (int k = startIndex; k < colEnd; ++k) {
            PdfPCell cell = cells[k];
            if (cell != null) {
                float currentMaxHeight = maxHeight + extraHeights[k];
                writeBorderAndBackground(xPos, yPos, currentMaxHeight, cell, canvases);
                processCellContent(xPos, yPos, currentMaxHeight, cell, canvases);
            }
        }
    }

    private int findStartIndex(int colStart, float xPos) {
        int startIndex;
        for (startIndex = colStart; startIndex >= 0; --startIndex) {
            if (cells[startIndex] != null) {
                break;
            }
            if (startIndex > 0) {
                xPos -= widths[startIndex - 1];
            }
        }
        return Math.max(startIndex, 0);
    }

    private void processCellContent(float xPos, float yPos, float currentMaxHeight, PdfPCell cell, PdfContentByte[] canvases) {
        Image img = cell.getImage();
        float tly = calculateTopLeftY(yPos, currentMaxHeight, cell);

        if (img != null) {
            handleImage(xPos, tly, currentMaxHeight, cell, img, canvases);
        } else {
            handleText(xPos, yPos, tly, currentMaxHeight, cell, canvases);
        }

        PdfPCellEvent evt = cell.getCellEvent();
        if (evt != null) {
            Rectangle rect = new Rectangle(cell.getLeft() + xPos, cell.getTop() + yPos - currentMaxHeight,
                    cell.getRight() + xPos, cell.getTop() + yPos);
            evt.cellLayout(cell, rect, canvases);
        }
    }

    private float calculateTopLeftY(float yPos, float currentMaxHeight, PdfPCell cell) {
        float tly = cell.getTop() + yPos - cell.getEffectivePaddingTop();
        if (cell.getHeight() <= currentMaxHeight) {
            switch (cell.getVerticalAlignment()) {
                case Element.ALIGN_BOTTOM:
                    tly = cell.getTop() + yPos - currentMaxHeight + cell.getHeight() - cell.getEffectivePaddingTop();
                    break;
                case Element.ALIGN_MIDDLE:
                    tly = cell.getTop() + yPos + (cell.getHeight() - currentMaxHeight) / 2 - cell.getEffectivePaddingTop();
                    break;
                default:
                    break;
            }
        }
        return tly;
    }

    private void handleImage(float xPos, float tly, float currentMaxHeight, PdfPCell cell, Image img, PdfContentByte[] canvases) {
        if (cell.getRotationPdfPCell() != 0) {
            img = Image.getInstance(img);
            img.setRotation(img.getImageRotation() + (float) (cell.getRotationPdfPCell() * Math.PI / 180.0));
        }

        boolean needsScaling = false;
        if (cell.getHeight() > currentMaxHeight) {
            img.scalePercent(100);
            float scale = (currentMaxHeight - cell.getEffectivePaddingTop() - cell.getEffectivePaddingBottom()) / img.getScaledHeight();
            img.scalePercent(scale * 100);
            needsScaling = true;
        }

        float left = calculateImageLeft(xPos, cell, img, needsScaling);
        img.setAbsolutePosition(left, tly - img.getScaledHeight());

        try {
            canvases[PdfPTable.TEXTCANVAS].addImage(img);
        } catch (DocumentException e) {
            throw new ExceptionConverter(e);
        }
    }

    private float calculateImageLeft(float xPos, PdfPCell cell, Image img, boolean needsScaling) {
        float left = cell.getLeft() + xPos + cell.getEffectivePaddingLeft();
        if (needsScaling) {
            switch (cell.getHorizontalAlignment()) {
                case Element.ALIGN_CENTER:
                    left = xPos + (cell.getLeft() + cell.getEffectivePaddingLeft() + cell.getRight() - cell.getEffectivePaddingRight() - img.getScaledWidth()) / 2;
                    break;
                case Element.ALIGN_RIGHT:
                    left = xPos + cell.getRight() - cell.getEffectivePaddingRight() - img.getScaledWidth();
                    break;
                default:
                    break;
            }
        }
        return left;
    }

    private void handleText(float xPos, float yPos, float tly, float currentMaxHeight, PdfPCell cell,
            PdfContentByte[] canvases) {
        if (cell.getRotationPdfPCell() == 90 || cell.getRotationPdfPCell() == 270) {
            handleRotatedText(xPos, yPos, currentMaxHeight, cell, canvases);
        } else {
            handleNormalText(xPos, yPos, tly, currentMaxHeight, cell, canvases);
        }
    }

    private void handleRotatedText(float xPos, float yPos, float currentMaxHeight, PdfPCell cell,
            PdfContentByte[] canvases) {
        float netWidth = currentMaxHeight - cell.getEffectivePaddingTop() - cell.getEffectivePaddingBottom();
        float netHeight = cell.getWidth() - cell.getEffectivePaddingLeft() - cell.getEffectivePaddingRight();
        ColumnText ct = ColumnText.duplicate(cell.getColumn());
        ct.setCanvases(canvases);
        ct.setSimpleColumn(0, 0, netWidth + 0.001f, -netHeight);

        try {
            ct.go(true);
        } catch (DocumentException | IOException e) {
            throw new ExceptionConverter(e);
        }

        float calcHeight = -ct.getYLine();
        if (netWidth <= 0 || netHeight <= 0) {
            calcHeight = 0;
        }
        if (calcHeight > 0) {
            if (cell.isUseDescender()) {
                calcHeight -= ct.getDescender();
            }
            handleRotatedTextAlignment(xPos, yPos, currentMaxHeight, cell, canvases, ct, calcHeight);
        }
    }

    private void handleRotatedTextAlignment(float xPos, float yPos, float currentMaxHeight, PdfPCell cell,
            PdfContentByte[] canvases, ColumnText ct, float calcHeight) {
        float pivotX;
        float pivotY;
        if (cell.getRotationPdfPCell() == 90) {
            pivotY = cell.getTop() + yPos - currentMaxHeight + cell.getEffectivePaddingBottom();
            pivotX = switch (cell.getVerticalAlignment()) {
                case Element.ALIGN_BOTTOM -> cell.getLeft() + xPos + cell.getWidth() - cell.getEffectivePaddingRight();
                case Element.ALIGN_MIDDLE -> cell.getLeft() + xPos +
                        (cell.getWidth() + cell.getEffectivePaddingLeft() - cell.getEffectivePaddingRight()
                                + calcHeight) / 2;
                default -> cell.getLeft() + xPos + cell.getEffectivePaddingLeft() + calcHeight;
            };
            saveAndRotateCanvases(canvases, 0, 1, -1, 0, pivotX, pivotY);
        } else {
            pivotY = cell.getTop() + yPos - cell.getEffectivePaddingTop();
            pivotX = switch (cell.getVerticalAlignment()) {
                case Element.ALIGN_BOTTOM -> cell.getLeft() + xPos + cell.getEffectivePaddingLeft();
                case Element.ALIGN_MIDDLE -> cell.getLeft() + xPos +
                        (cell.getWidth() + cell.getEffectivePaddingLeft() - cell.getEffectivePaddingRight()
                                - calcHeight) / 2;
                default -> cell.getLeft() + xPos + cell.getWidth() - cell.getEffectivePaddingRight() - calcHeight;
            };
            saveAndRotateCanvases(canvases, 0, -1, 1, 0, pivotX, pivotY);
        }

        try {
            ct.go();
        } catch (DocumentException | IOException e) {
            throw new ExceptionConverter(e);
        } finally {
            restoreCanvases(canvases);
        }
    }

    private void handleNormalText(float xPos, float yPos, float tly, float currentMaxHeight, PdfPCell cell,
            PdfContentByte[] canvases) {
        float fixedHeight = cell.getFixedHeight();
        float rightLimit = cell.getRight() + xPos - cell.getEffectivePaddingRight();
        float leftLimit = cell.getLeft() + xPos + cell.getEffectivePaddingLeft();

        if (cell.isNoWrap()) {
            adjustLimitsForNoWrap(cell, rightLimit, leftLimit);
        }

        ColumnText ct = ColumnText.duplicate(cell.getColumn());
        ct.setCanvases(canvases);
        float bry = tly - (currentMaxHeight - cell.getEffectivePaddingTop() - cell.getEffectivePaddingBottom());
        if (fixedHeight > 0 && cell.getHeight() > currentMaxHeight) {
            tly = cell.getTop() + yPos - cell.getEffectivePaddingTop();
            bry = cell.getTop() + yPos - currentMaxHeight + cell.getEffectivePaddingBottom();
        }
        if ((tly > bry || ct.zeroHeightElement()) && leftLimit < rightLimit) {
            ct.setSimpleColumn(leftLimit, bry - 0.001f, rightLimit, tly);
            if (cell.getRotationPdfPCell() == 180) {
                float shx = leftLimit + rightLimit;
                float shy = yPos + yPos - currentMaxHeight + cell.getEffectivePaddingBottom() - cell.getEffectivePaddingTop();
                saveAndRotateCanvases(canvases, -1, 0, 0, -1, shx, shy);
            }
            try {
                ct.go();
            } catch (DocumentException | IOException e) {
                throw new ExceptionConverter(e);
            } finally {
                if (cell.getRotationPdfPCell() == 180) {
                    restoreCanvases(canvases);
                }
            }
        }
    }

    private void adjustLimitsForNoWrap(PdfPCell cell, float rightLimit, float leftLimit) {
        switch (cell.getHorizontalAlignment()) {
            case Element.ALIGN_CENTER:
                rightLimit += 10000;
                leftLimit -= 10000;
                break;
            case Element.ALIGN_RIGHT:
                if (cell.getRotationPdfPCell() == 180) {
                    rightLimit += RIGHT_LIMIT;
                } else {
                    leftLimit -= RIGHT_LIMIT;
                }
                break;
            default:
                if (cell.getRotationPdfPCell() == 180) {
                    leftLimit -= RIGHT_LIMIT;
                } else {
                    rightLimit += RIGHT_LIMIT;
                }
                break;
        }
    }


    /**
     * Checks if the dimensions of the columns were calculated.
     *
     * @return true if the dimensions of the columns were calculated
     */
    public boolean isCalculated() {
        return calculated;
    }

    /**
     * Gets the maximum height of the row (i.e. of the 'highest' cell).
     *
     * @return the maximum height of the row
     */
    public float getMaxHeights() {
        if (calculated) {
            return maxHeight;
        }
        return calculateHeights();
    }

    /**
     * Changes the maximum height of the row (to make it higher). (added by Jin-Hsia Yang)
     *
     * @param maxHeight the new maximum height
     */
    public void setMaxHeights(float maxHeight) {
        this.maxHeight = maxHeight;
    }

    //end add

    float[] getEventWidth(float xPos) {
        int n = 0;
        for (PdfPCell cell1 : cells) {
            if (cell1 != null) {
                ++n;
            }
        }
        float[] width = new float[n + 1];
        n = 0;
        width[n++] = xPos;
        for (PdfPCell cell : cells) {
            if (cell != null) {
                width[n] = width[n - 1] + cell.getWidth();
                ++n;
            }
        }
        return width;
    }

    /**
     * Splits a row to newHeight. The returned row is the remainder. It will return null if the newHeight was so small
     * that only an empty row would result.
     *
     * @param rowIndex   the row index
     * @param table      the PdfTable to get the row from
     * @param newHeight the new height
     * @return the remainder row or null if the newHeight was so small that only an empty row would result
     */
    public PdfPRow splitRow(PdfPTable table, int rowIndex, float newHeight) {
        PdfPCell[] newCells = new PdfPCell[cells.length];
        float[] fixHs = new float[cells.length];
        float[] minHs = new float[cells.length];

        boolean allEmpty = true;

        for (int k = 0; k < cells.length; ++k) {
            PdfPCell cell = cells[k];

            if (cell == null) {
                allEmpty = handleNullCell(table, rowIndex, newCells, newHeight, k, allEmpty);
                continue;
            }

            fixHs[k] = cell.getFixedHeight();
            minHs[k] = cell.getMinimumHeight();
            newCells[k] = createNewCell(cell, newHeight);
            allEmpty = handleCellContent(cell, newCells[k], newHeight) && allEmpty;

            cell.setFixedHeight(newHeight);
        }

        if (allEmpty) {
            restoreOriginalHeights(fixHs, minHs);
            return null;
        }

        return finalizeSplitRow(newCells);
    }

    private boolean handleNullCell(PdfPTable table, int rowIndex, PdfPCell[] newCells, float newHeightLoop, int k, boolean allEmpty) {
        int index = rowIndex;
        if (table.rowSpanAbove(index, k)) {
            do {
                newHeightLoop += table.getRowHeight(index);
            } while (table.rowSpanAbove(--index, k));
            PdfPRow row = table.getRow(index);
            if (row != null && row.getCells()[k] != null) {
                newCells[k] = createSpannedCell(row.getCells()[k], newHeightLoop, rowIndex - index);
                allEmpty = false;
            }
        }
        return allEmpty;
    }

    private PdfPCell createSpannedCell(PdfPCell originalCell, float newHeightLoop, int rowspan) {
        PdfPCell newCell = new PdfPCell(originalCell);
        newCell.consumeHeight(newHeightLoop);
        newCell.setRowspan(originalCell.getRowspan() - rowspan);
        return newCell;
    }

    private PdfPCell createNewCell(PdfPCell cell, float newHeightLoop) {
        PdfPCell newCell = new PdfPCell(cell);
        if (cell.getImage() != null && newHeightLoop <= cell.getEffectivePaddingBottom() + cell.getEffectivePaddingTop() + 2) {
                newCell.setPhrase(null);
            }

        return newCell;
    }

    private boolean handleCellContent(PdfPCell cell, PdfPCell newCell, float newHeightLoop) {
        if (cell.getImage() != null) {
            return false;
        }

        ColumnText ct = ColumnText.duplicate(cell.getColumn());
        float y = configureColumnText(cell, newHeightLoop, ct);

        int status = 0;
        try {
            status = ct.go(true);
        } catch (DocumentException e) {
            throw new ExceptionConverter(e);
        } catch (IOException e) {
            //may need some logging or some other operation
        }

        return evaluateCellContent(cell, newCell, ct, y, status);
    }

    private float configureColumnText(PdfPCell cell, float newHeightLoop, ColumnText ct) {
        float left = cell.getLeft() + cell.getEffectivePaddingLeft();
        float top = cell.getTop() - cell.getEffectivePaddingTop();
        float bottom = cell.getTop() + cell.getEffectivePaddingBottom() - newHeightLoop;
        float right = cell.getRight() - cell.getEffectivePaddingRight();

        return switch (cell.getRotationPdfPCell()) {
            case 90, 270 -> setColumn(ct, left, bottom, right, top);
            default -> setColumn(ct, left, bottom, cell.isNoWrap() ? RIGHT_LIMIT : right, top);
        };
    }

    private boolean evaluateCellContent(PdfPCell cell, PdfPCell newCell, ColumnText ct, float y, int status) {
        boolean isEmpty = (ct.getYLine() == y);
        if (isEmpty) {
            newCell.setColumn(ColumnText.duplicate(cell.getColumn()));
            ct.setFilledWidth(0);
        } else if ((status & ColumnText.NO_MORE_TEXT) == 0) {
            newCell.setColumn(ct);
            ct.setFilledWidth(0);
        } else {
            newCell.setPhrase(null);
        }
        return isEmpty;
    }

    private void restoreOriginalHeights(float[] fixHs, float[] minHs) {
        for (int k = 0; k < cells.length; ++k) {
            PdfPCell cell = cells[k];
            if (cell == null) {
                continue;
            }
            if (fixHs[k] > 0) {
                cell.setFixedHeight(fixHs[k]);
            } else {
                cell.setMinimumHeight(minHs[k]);
            }
        }
    }

    private PdfPRow finalizeSplitRow(PdfPCell[] newCells) {
        calculateHeights();
        PdfPRow split = new PdfPRow(newCells);
        split.widths = widths.clone();
        split.calculateHeights();
        return split;
    }


    /**
     * Returns the array of cells in the row. Please be extremely careful with this method. Use the cells as read only
     * objects.
     *
     * @return an array of cells
     * @since 2.1.1
     */
    public PdfPCell[] getCells() {
        return cells;
    }
}
