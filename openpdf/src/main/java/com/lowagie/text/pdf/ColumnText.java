/*
 * $Id: ColumnText.java 4091 2009-11-10 15:15:28Z psoares33 $
 *
 * Copyright 2001, 2002 by Paulo Soares.
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

import com.lowagie.text.Chunk;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Image;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.SimpleTable;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.InvalidRunDirectionException;
import com.lowagie.text.pdf.draw.DrawInterface;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Formats text in a columnwise form. The text is bound on the left and on the right by a sequence of lines. This allows
 * the column to have any shape, not only rectangular.
 * <p>
 * Several parameters can be set like the first paragraph line indent and extra space between paragraphs.
 * <p>
 * A call to the method <CODE>go</CODE> will return one of the following situations: the column ended or the text
 * ended.
 * <p>
 * I the column ended, a new column definition can be loaded with the method
 * <CODE>setColumns</CODE> and the method <CODE>go</CODE> can be called again.
 * <p>
 * If the text ended, more text can be loaded with <CODE>addText</CODE> and the method <CODE>go</CODE> can be called
 * again.<BR> The only limitation is that one or more complete paragraphs must be loaded each time.
 * <p>
 * Full bidirectional reordering is supported. If the run direction is
 * <CODE>PdfWriter.RUN_DIRECTION_RTL</CODE> the meaning of the horizontal
 * alignments and margins is mirrored.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */

public class ColumnText {

    static Logger logger = Logger.getLogger(ColumnText.class.getName());

    /**
     * Eliminate the arabic vowels
     */
    public static final int AR_NOVOWEL = ArabicLigaturizer.AR_NOVOWEL;
    /**
     * Compose the tashkeel in the ligatures.
     */
    public static final int AR_COMPOSEDTASHKEEL = ArabicLigaturizer.AR_COMPOSEDTASHKEEL;
    /**
     * Do some extra double ligatures.
     */
    public static final int AR_LIG = ArabicLigaturizer.AR_LIG;
    /**
     * Digit shaping option: Replace European digits (U+0030...U+0039) by Arabic-Indic digits.
     */
    public static final int DIGITS_EN2AN = ArabicLigaturizer.DIGITS_EN2AN;

    /**
     * Digit shaping option: Replace Arabic-Indic digits by European digits (U+0030...U+0039).
     */
    public static final int DIGITS_AN2EN = ArabicLigaturizer.DIGITS_AN2EN;

    /**
     * Digit shaping option: Replace European digits (U+0030...U+0039) by Arabic-Indic digits if the most recent
     * strongly directional character is an Arabic letter (its Bidi direction value is RIGHT_TO_LEFT_ARABIC). The
     * initial state at the start of the text is assumed to be not an Arabic, letter, so European digits at the start of
     * the text will not change. Compare to DIGITS_ALEN2AN_INIT_AL.
     */
    public static final int DIGITS_EN2AN_INIT_LR = ArabicLigaturizer.DIGITS_EN2AN_INIT_LR;

    /**
     * Digit shaping option: Replace European digits (U+0030...U+0039) by Arabic-Indic digits if the most recent
     * strongly directional character is an Arabic letter (its Bidi direction value is RIGHT_TO_LEFT_ARABIC). The
     * initial state at the start of the text is assumed to be an Arabic, letter, so European digits at the start of the
     * text will change. Compare to DIGITS_ALEN2AN_INT_LR.
     */
    public static final int DIGITS_EN2AN_INIT_AL = ArabicLigaturizer.DIGITS_EN2AN_INIT_AL;

    /**
     * Digit type option: Use Arabic-Indic digits (U+0660...U+0669).
     */
    public static final int DIGIT_TYPE_AN = ArabicLigaturizer.DIGIT_TYPE_AN;

    /**
     * Digit type option: Use Eastern (Extended) Arabic-Indic digits (U+06f0...U+06f9).
     */
    public static final int DIGIT_TYPE_AN_EXTENDED = ArabicLigaturizer.DIGIT_TYPE_AN_EXTENDED;
    /**
     * the space char ratio
     */
    public static final float GLOBAL_SPACE_CHAR_RATIO = 0;
    /**
     * Initial value of the status.
     */
    public static final int START_COLUMN = 0;
    /**
     * Signals that there is no more text available.
     */
    public static final int NO_MORE_TEXT = 1;
    /**
     * Signals that there is no more column.
     */
    public static final int NO_MORE_COLUMN = 2;
    /**
     * The column is valid.
     */
    protected static final int LINE_STATUS_OK = 0;
    /**
     * The line is out the column limits.
     */
    protected static final int LINE_STATUS_OFFLIMITS = 1;
    /**
     * The line cannot fit this column position.
     */
    protected static final int LINE_STATUS_NOLINE = 2;
    protected int runDirection = PdfWriter.RUN_DIRECTION_DEFAULT;

    Object[] currentValues = new Object[2];
    PdfFont currentFont = null;
    /**
     * Upper bound of the column.
     */
    protected float maxY;

    /**
     * Lower bound of the column.
     */
    protected float minY;

    protected float leftX;

    protected float rightX;

    /**
     * The column alignment. Default is left alignment.
     */
    protected int alignment = Element.ALIGN_LEFT;

    /**
     * The left column bound.
     */
    protected List<float[]> leftWall;

    /**
     * The right column bound.
     */
    protected List<float[]> rightWall;

    /**
     * The chunks that form the text.
     */

    protected BidiLine bidiLine;

    /**
     * The current y line location. Text will be written at this line minus the leading.
     */
    protected float yLine;

    /**
     * The leading for the current line.
     */
    protected float currentLeading = 16;

    /**
     * The fixed text leading.
     */
    protected float fixedLeading = 16;

    /**
     * The text leading that is multiplied by the biggest font size in the line.
     */
    protected float multipliedLeading = 0;

    /**
     * The <CODE>PdfContent</CODE> where the text will be written to.
     */
    protected PdfContentByte canvas;

    protected PdfContentByte[] canvases;

    /**
     * The line status when trying to fit a line to a column.
     */
    protected int lineStatus;

    /**
     * The first paragraph line indent.
     */
    protected float indent = 0;

    /**
     * The following paragraph lines indent.
     */
    protected float followingIndent = 0;

    /**
     * The right paragraph lines indent.
     */
    protected float rightIndent = 0;

    /**
     * The extra space between paragraphs.
     */
    protected float extraParagraphSpace = 0;

    /**
     * The width of the line when the column is defined as a simple rectangle.
     */
    protected float rectangularWidth = -1;

    protected boolean rectangularMode = false;
    protected float descender;
    protected boolean composite = false;
    protected ColumnText compositeColumn;
    protected LinkedList<Element> compositeElements;
    protected int listIdx = 0;
    protected Phrase waitPhrase;
    /**
     * Holds value of property spaceCharRatio.
     */
    private float spaceCharRatio = GLOBAL_SPACE_CHAR_RATIO;
    private boolean lastWasNewline = true;
    /**
     * Holds value of property linesWritten.
     */
    private int linesWritten;
    private float firstLineY;
    private boolean firstLineYDone = false;
    /**
     * Holds value of property arabicOptions.
     */
    private int arabicOptions = 0;
    private boolean splittedRow;
    /**
     * if true, first line height is adjusted so that the max ascender touches the top
     */
    private boolean useAscender = false;

    /**
     * Holds value of property filledWidth.
     */
    private float filledWidth;

    private boolean adjustFirstLine = true;

    /**
     * Creates a <CODE>ColumnText</CODE>.
     *
     * @param canvas the place where the text will be written to. Can be a template.
     */
    public ColumnText(PdfContentByte canvas) {
        this.canvas = canvas;
    }

    /**
     * Creates an independent duplicated of the instance <CODE>org</CODE>.
     *
     * @param org the original <CODE>ColumnText</CODE>
     * @return the duplicated
     */
    public static ColumnText duplicate(ColumnText org) {
        ColumnText ct = new ColumnText(null);
        ct.setACopy(org);
        return ct;
    }

    /**
     * Gets the width that the line will occupy after writing. Only the width of the first line is returned.
     *
     * @param phrase        the <CODE>Phrase</CODE> containing the line
     * @param runDirection  the run direction
     * @param arabicOptions the options for the arabic shaping
     * @return the width of the line
     */
    public static float getWidth(Phrase phrase, int runDirection, int arabicOptions) {
        ColumnText ct = new ColumnText(null);
        ct.addText(phrase);
        ct.addWaitingPhrase();
        PdfLine line = ct.bidiLine.processLine(0, 20000, Element.ALIGN_LEFT, runDirection, arabicOptions);
        if (line == null) {
            return 0;
        } else {
            return 20000 - line.widthLeft();
        }
    }

    /**
     * Gets the width that the line will occupy after writing. Only the width of the first line is returned.
     *
     * @param phrase the <CODE>Phrase</CODE> containing the line
     * @return the width of the line
     */
    public static float getWidth(Phrase phrase) {
        return getWidth(phrase, PdfWriter.RUN_DIRECTION_NO_BIDI, 0);
    }

    /**
     * Shows a line of text. Only the first line is written.
     *
     * @param canvas        where the text is to be written to
     * @param phrase        the <CODE>Phrase</CODE> with the text
     * @param settings      the text alignment settings
     */
    public static void showTextAligned(PdfContentByte canvas, Phrase phrase, TextAlignmentSettings settings) {
        int alignment = settings.getAlignment();
        if (alignment != Element.ALIGN_LEFT && alignment != Element.ALIGN_CENTER && alignment != Element.ALIGN_RIGHT) {
            alignment = Element.ALIGN_LEFT;
        }
        canvas.saveState();
        ColumnText ct = new ColumnText(canvas);
        float lly = -1;
        float ury = 2;
        float llx;
        float urx;

        switch (alignment) {
            case Element.ALIGN_LEFT:
                llx = 0;
                urx = 20000;
                break;
            case Element.ALIGN_RIGHT:
                llx = -20000;
                urx = 0;
                break;
            default:
                llx = -20000;
                urx = 20000;
                break;
        }

        if (settings.getRotation() == 0) {
            llx += settings.getX();
            lly += settings.getY();
            urx += settings.getX();
            ury += settings.getY();
        } else {
            double alpha = settings.getRotation() * Math.PI / 180.0;
            float cos = (float) Math.cos(alpha);
            float sin = (float) Math.sin(alpha);
            canvas.concatCTM(cos, sin, -sin, cos, settings.getX(), settings.getY());
        }

        ct.setSimpleColumn(phrase, llx, lly, urx, ury, 2, alignment);

        int runDirection = settings.getRunDirection();
        if (runDirection == PdfWriter.RUN_DIRECTION_RTL) {
            if (alignment == Element.ALIGN_LEFT) {
                alignment = Element.ALIGN_RIGHT;
            } else if (alignment == Element.ALIGN_RIGHT) {
                alignment = Element.ALIGN_LEFT;
            }
        }

        ct.setAlignment(alignment);
        ct.setArabicOptions(settings.getArabicOptions());
        ct.setRunDirection(runDirection);

        try {
            ct.go();
        } catch (DocumentException e) {
            throw new ExceptionConverter(e);
        }

        canvas.restoreState();
    }


    /**
     * Shows a line of text. Only the first line is written.
     *
     * @param canvas    where the text is to be written to
     * @param alignment the alignment
     * @param phrase    the <CODE>Phrase</CODE> with the text
     * @param x         the x reference position
     * @param y         the y reference position
     * @param rotation  the rotation to be applied in degrees counterclockwise
     */
    public static void showTextAligned(PdfContentByte canvas, int alignment, Phrase phrase, float x, float y,
            float rotation) {
        TextAlignmentSettings settings = new TextAlignmentSettings(alignment, x, y, rotation,
                PdfWriter.RUN_DIRECTION_NO_BIDI, 0);
        showTextAligned(canvas, phrase, settings);
    }

    /**
     * Checks the status variable and looks if there's still some text.
     *
     * @param status statues variable
     * @return <code>true</code> if there's still some text else <code>false</code>
     */
    public static boolean hasMoreText(int status) {
        return (status & ColumnText.NO_MORE_TEXT) == 0;
    }

    /**
     * Makes this instance an independent copy of <CODE>org</CODE>.
     *
     * @param org the original <CODE>ColumnText</CODE>
     * @return itself
     */
    public ColumnText setACopy(ColumnText org) {
        setSimpleVars(org);
        if (org.bidiLine != null) {
            bidiLine = new BidiLine(org.bidiLine);
        }
        return this;
    }

    protected void setSimpleVars(ColumnText org) {
        maxY = org.maxY;
        minY = org.minY;
        alignment = org.alignment;
        leftWall = null;
        if (org.leftWall != null) {
            leftWall = new ArrayList<>(org.leftWall);
        }
        rightWall = null;
        if (org.rightWall != null) {
            rightWall = new ArrayList<>(org.rightWall);
        }
        yLine = org.yLine;
        currentLeading = org.currentLeading;
        fixedLeading = org.fixedLeading;
        multipliedLeading = org.multipliedLeading;
        canvas = org.canvas;
        canvases = org.canvases;
        lineStatus = org.lineStatus;
        indent = org.indent;
        followingIndent = org.followingIndent;
        rightIndent = org.rightIndent;
        extraParagraphSpace = org.extraParagraphSpace;
        rectangularWidth = org.rectangularWidth;
        rectangularMode = org.rectangularMode;
        spaceCharRatio = org.spaceCharRatio;
        lastWasNewline = org.lastWasNewline;
        linesWritten = org.linesWritten;
        arabicOptions = org.arabicOptions;
        runDirection = org.runDirection;
        descender = org.descender;
        composite = org.composite;
        splittedRow = org.splittedRow;
        if (org.composite) {
            compositeElements = new LinkedList<>(org.compositeElements);
            if (splittedRow) {
                PdfPTable table = (PdfPTable) compositeElements.getFirst();
                compositeElements.set(0, new PdfPTable(table));
            }
            if (org.compositeColumn != null) {
                compositeColumn = duplicate(org.compositeColumn);
            }
        }
        listIdx = org.listIdx;
        firstLineY = org.firstLineY;
        leftX = org.leftX;
        rightX = org.rightX;
        firstLineYDone = org.firstLineYDone;
        waitPhrase = org.waitPhrase;
        useAscender = org.useAscender;
        filledWidth = org.filledWidth;
        adjustFirstLine = org.adjustFirstLine;
    }

    private void addWaitingPhrase() {
        if (bidiLine == null && waitPhrase != null) {
            bidiLine = new BidiLine();
            for (Element o : waitPhrase.getChunks()) {
                bidiLine.addChunk(new PdfChunk((Chunk) o, null));
            }
            waitPhrase = null;
        }
    }

    /**
     * Adds a <CODE>Phrase</CODE> to the current text array. Will not have any effect if addElement() was called
     * before.
     *
     * @param phrase the text
     */
    public void addText(Phrase phrase) {
        if (phrase == null || composite) {
            return;
        }
        addWaitingPhrase();
        if (bidiLine == null) {
            waitPhrase = phrase;
            return;
        }
        for (Element o : phrase.getChunks()) {
            bidiLine.addChunk(new PdfChunk((Chunk) o, null));
        }
    }

    /**
     * Replaces the current text array with this <CODE>Phrase</CODE>. Anything added previously with addElement() is
     * lost.
     *
     * @param phrase the text
     */
    public void setText(Phrase phrase) {
        bidiLine = null;
        composite = false;
        compositeColumn = null;
        compositeElements = null;
        listIdx = 0;
        splittedRow = false;
        waitPhrase = phrase;
    }

    /**
     * Adds a <CODE>Chunk</CODE> to the current text array. Will not have any effect if addElement() was called before.
     *
     * @param chunk the text
     */
    public void addText(Chunk chunk) {
        if (chunk == null || composite) {
            return;
        }
        addText(new Phrase(chunk));
    }

    /**
     * Adds an element. Elements supported are <CODE>Paragraph</CODE>,
     * <CODE>List</CODE>, <CODE>PdfPTable</CODE>, <CODE>Image</CODE> and
     * <CODE>Graphic</CODE>.
     * <p>
     * It removes all the text placed with <CODE>addText()</CODE>.
     *
     * @param element the <CODE>Element</CODE>
     */
    public void addElement(Element element) {
        if (element == null) {
            return;
        }
        if (element instanceof Image image) {
            Image img = image;
            PdfPTable t = new PdfPTable(1);
            float w = img.getWidthPercentage();
            if (w == 0) {
                t.setTotalWidth(img.getScaledWidth());
                t.setLockedWidth(true);
            } else {
                t.setWidthPercentage(w);
            }
            t.setSpacingAfter(img.getSpacingAfter());
            t.setSpacingBefore(img.getSpacingBefore());
            switch (img.getAlignment()) {
                case Image.LEFT:
                    t.setHorizontalAlignment(Element.ALIGN_LEFT);
                    break;
                case Image.RIGHT:
                    t.setHorizontalAlignment(Element.ALIGN_RIGHT);
                    break;
                default:
                    t.setHorizontalAlignment(Element.ALIGN_CENTER);
                    break;
            }
            PdfPCell c = new PdfPCell(img, true);
            c.setPadding(0);
            c.setBorder(img.getBorder());
            c.setBorderColor(img.getBorderColor());
            c.setBorderWidth(img.getBorderWidth());
            c.setBackgroundColor(img.getBackgroundColor());
            t.addCell(c);
            element = t;
        }
        if (element.type() == Element.CHUNK) {
            element = new Paragraph((Chunk) element);
        } else if (element.type() == Element.PHRASE) {
            element = new Paragraph((Phrase) element);
        }
        if (element instanceof SimpleTable simpleTable) {
            try {
                element = simpleTable.createPdfPTable();
            } catch (DocumentException e) {
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage("element.not.allowed"));
            }
        } else if (element.type() != Element.PARAGRAPH && element.type() != Element.LIST
                && element.type() != Element.PTABLE && element.type() != Element.YMARK) {
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("element.not.allowed"));
        }
        if (!composite) {
            composite = true;
            compositeElements = new LinkedList<>();
            bidiLine = null;
            waitPhrase = null;
        }
        compositeElements.add(element);
    }

    /**
     * Converts a sequence of lines representing one of the column bounds into an internal format.
     * <p>
     * Each array element will contain a <CODE>float[4]</CODE> representing the line x = ax + b.
     *
     * @param cLine the column array
     * @return the converted array
     */
    protected List<float[]> convertColumn(float[] cLine) {
        if (cLine.length < 4) {
            throw new InvalidRunDirectionException(MessageLocalization.getComposedMessage("no.valid.column.line.found"));
        }
        List<float[]> cc = new ArrayList<>();
        for (int k = 0; k < cLine.length - 2; k += 2) {
            if (cLine.length == k + 3) {
                break;
            }
            float x1 = cLine[k];
            float y1 = cLine[k + 1];
            float x2 = cLine[k + 2];
            float y2 = cLine[k + 3];
            if (y1 == y2) {
                continue;
            }
            // x = ay + b
            float a = (x1 - x2) / (y1 - y2);
            float b = x1 - a * y1;
            float[] r = new float[4];
            r[0] = Math.min(y1, y2);
            r[1] = Math.max(y1, y2);
            r[2] = a;
            r[3] = b;
            cc.add(r);
            maxY = Math.max(maxY, r[1]);
            minY = Math.min(minY, r[0]);
        }
        if (cc.isEmpty()) {
            throw new InvalidRunDirectionException(MessageLocalization.getComposedMessage("no.valid.column.line.found"));
        }
        return cc;
    }

    /**
     * Finds the intersection between the <CODE>yLine</CODE> and the column. It will set the <CODE>lineStatus</CODE>
     * appropriately.
     *
     * @param wall the column to intersect
     * @return the x coordinate of the intersection
     */
    protected float findLimitsPoint(List<float[]> wall) {
        lineStatus = LINE_STATUS_OK;
        if (yLine < minY || yLine > maxY) {
            lineStatus = LINE_STATUS_OFFLIMITS;
            return 0;
        }
        for (float[] o : wall) {
            if (yLine < o[0] || yLine > o[1]) {
                continue;
            }
            return o[2] * yLine + o[3];
        }
        lineStatus = LINE_STATUS_NOLINE;
        return 0;
    }

    /**
     * Finds the intersection between the <CODE>yLine</CODE> and the two column bounds. It will set the
     * <CODE>lineStatus</CODE> appropriately.
     *
     * @return a <CODE>float[2]</CODE>with the x coordinates of the intersection
     */
    protected float[] findLimitsOneLine() {
        float x1 = findLimitsPoint(leftWall);
        if (lineStatus == LINE_STATUS_OFFLIMITS || lineStatus == LINE_STATUS_NOLINE) {
            return new float[0];
        }
        float x2 = findLimitsPoint(rightWall);
        if (lineStatus == LINE_STATUS_NOLINE) {
            return new float[0];
        }
        return new float[]{x1, x2};
    }

    /**
     * Finds the intersection between the <CODE>yLine</CODE>, the <CODE>yLine-leading</CODE>and the two column bounds.
     * It will set the <CODE>lineStatus</CODE> appropriately.
     *
     * @return a <CODE>float[4]</CODE>with the x coordinates of the intersection
     */
    protected float[] findLimitsTwoLines() {
        boolean repeat = false;
        for (; ; ) {
            if (repeat && currentLeading == 0) {
                return new float[0];
            }
            repeat = true;
            float[] x1 = findLimitsOneLine();
            if (lineStatus == LINE_STATUS_OFFLIMITS) {
                return new float[0];
            }
            yLine -= currentLeading;
            if (lineStatus == LINE_STATUS_NOLINE) {
                continue;
            }
            float[] x2 = findLimitsOneLine();
            if (lineStatus == LINE_STATUS_OFFLIMITS) {
                return new float[0];
            }
            if (lineStatus == LINE_STATUS_NOLINE) {
                yLine -= currentLeading;
                continue;
            }
            if (x1[0] >= x2[1] || x2[0] >= x1[1]) {
                continue;
            }
            return new float[]{x1[0], x1[1], x2[0], x2[1]};
        }
    }

    /**
     * Sets the columns bounds. Each column bound is described by a
     * <CODE>float[]</CODE> with the line points [x1,y1,x2,y2,...].
     * The array must have at least 4 elements.
     *
     * @param leftLine  the left column bound
     * @param rightLine the right column bound
     */
    public void setColumns(float[] leftLine, float[] rightLine) {
        maxY = -10e20f;
        minY = 10e20f;
        setYLine(Math.max(leftLine[1], leftLine[leftLine.length - 1]));
        rightWall = convertColumn(rightLine);
        leftWall = convertColumn(leftLine);
        rectangularWidth = -1;
        rectangularMode = false;
    }

    /**
     * Simplified method for rectangular columns.
     *
     * @param phrase    a <CODE>Phrase</CODE>
     * @param llx       the lower left x corner
     * @param lly       the lower left y corner
     * @param urx       the upper right x corner
     * @param ury       the upper right y corner
     * @param leading   the leading
     * @param alignment the column alignment
     */
    public void setSimpleColumn(Phrase phrase, float llx, float lly, float urx, float ury, float leading,
            int alignment) {
        addText(phrase);
        setSimpleColumn(llx, lly, urx, ury, leading, alignment);
    }

    /**
     * Simplified method for rectangular columns.
     *
     * @param llx       the lower left x corner
     * @param lly       the lower left y corner
     * @param urx       the upper right x corner
     * @param ury       the upper right y corner
     * @param leading   the leading
     * @param alignment the column alignment
     */
    public void setSimpleColumn(float llx, float lly, float urx, float ury, float leading, int alignment) {
        setLeading(leading);
        this.alignment = alignment;
        setSimpleColumn(llx, lly, urx, ury);
    }

    /**
     * Simplified method for rectangular columns.
     *
     * @param llx lower-left-x
     * @param lly lower-left-y
     * @param urx upper-right-x
     * @param ury upper-right-y
     */
    public void setSimpleColumn(float llx, float lly, float urx, float ury) {
        leftX = Math.min(llx, urx);
        maxY = Math.max(lly, ury);
        minY = Math.min(lly, ury);
        rightX = Math.max(llx, urx);
        yLine = maxY;
        rectangularWidth = rightX - leftX;
        if (rectangularWidth < 0) {
            rectangularWidth = 0;
        }
        rectangularMode = true;
    }

    /**
     * Sets the leading fixed and variable. The resultant leading will be fixedLeading+multipliedLeading*maxFontSize
     * where maxFontSize is the size of the biggest font in the line.
     *
     * @param fixedLeading      the fixed leading
     * @param multipliedLeading the variable leading
     */
    public void setLeading(float fixedLeading, float multipliedLeading) {
        this.fixedLeading = fixedLeading;
        this.multipliedLeading = multipliedLeading;
    }

    /**
     * Gets the fixed leading.
     *
     * @return the leading
     */
    public float getLeading() {
        return fixedLeading;
    }

    /**
     * Sets the leading to fixed.
     *
     * @param leading the leading
     */
    public void setLeading(float leading) {
        fixedLeading = leading;
        multipliedLeading = 0;
    }

    /**
     * Gets the variable leading.
     *
     * @return the leading
     */
    public float getMultipliedLeading() {
        return multipliedLeading;
    }

    /**
     * Gets the yLine.
     *
     * @return the yLine
     */
    public float getYLine() {
        return yLine;
    }

    /**
     * Sets the yLine. The line will be written to yLine-leading.
     *
     * @param yLine the yLine
     */
    public void setYLine(float yLine) {
        this.yLine = yLine;
    }

    /**
     * Gets the alignment.
     *
     * @return the alignment
     */
    public int getAlignment() {
        return alignment;
    }

    /**
     * Sets the alignment.
     *
     * @param alignment the alignment
     */
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    /**
     * Gets the first paragraph line indent.
     *
     * @return the indent
     */
    public float getIndent() {
        return indent;
    }

    /**
     * Sets the first paragraph line indent.
     *
     * @param indent the indent
     */
    public void setIndent(float indent) {
        this.indent = indent;
        lastWasNewline = true;
    }

    /**
     * Gets the following paragraph lines indent.
     *
     * @return the indent
     */
    public float getFollowingIndent() {
        return followingIndent;
    }

    /**
     * Sets the following paragraph lines indent.
     *
     * @param indent the indent
     */
    public void setFollowingIndent(float indent) {
        this.followingIndent = indent;
        lastWasNewline = true;
    }

    /**
     * Gets the right paragraph lines indent.
     *
     * @return the indent
     */
    public float getRightIndent() {
        return rightIndent;
    }

    /**
     * Sets the right paragraph lines indent.
     *
     * @param indent the indent
     */
    public void setRightIndent(float indent) {
        this.rightIndent = indent;
        lastWasNewline = true;
    }

    /**
     * Outputs the lines to the document. It is equivalent to <CODE>go(false)</CODE>.
     *
     * @return returns the result of the operation. It can be <CODE>NO_MORE_TEXT</CODE> and/or
     * <CODE>NO_MORE_COLUMN</CODE>
     * @throws DocumentException on error
     */
    public int go() throws DocumentException {
        return go(false);
    }

    /**
     * Outputs the lines to the document. The output can be simulated.
     *
     * @param simulate <CODE>true</CODE> to simulate the writing to the document
     * @return returns the result of the operation. It can be <CODE>NO_MORE_TEXT</CODE> and/or
     * <CODE>NO_MORE_COLUMN</CODE>
     * @throws DocumentException on error
     */
    public int go(boolean simulate) throws DocumentException {
        if (composite) {
            return goComposite(simulate);
        }
        addWaitingPhrase();
        if (bidiLine == null) {
            return NO_MORE_TEXT;
        }


        PdfDocument pdf = (PdfDocument) initialize(simulate)[0];
        PdfContentByte graphics = (PdfContentByte) initialize(simulate)[1];
        PdfContentByte text = (PdfContentByte) initialize(simulate)[2];
        float ratio = (float) initialize(simulate)[3];
        int localRunDirection = (int) initialize(simulate)[4];
        currentValues[1] = 0.0F;
        PdfLine line;

        int status = 0;
        boolean dirty = false;
        while (true) {
            float firstIndent = calculateFirstIndent();
            if (rectangularMode) {
                status = (int) handleRectangularMode(simulate, firstIndent, text, dirty, localRunDirection)[0];
                dirty = (boolean) handleRectangularMode(simulate, firstIndent, text, dirty, localRunDirection)[1];
                line = (PdfLine) handleRectangularMode(simulate, firstIndent, text, dirty, localRunDirection)[2];
            } else {
                status = (int) handleNonRectangularMode(simulate, firstIndent, text, dirty, localRunDirection)[0];
                dirty = (boolean) handleNonRectangularMode(simulate, firstIndent, text, dirty, localRunDirection)[1];
                line = (PdfLine) handleNonRectangularMode(simulate, firstIndent, text, dirty, localRunDirection)[2];
            }

            if (status != 0) {
                break;
            }

            processLine(simulate, firstIndent, pdf, graphics, text, ratio, line);
        }

        finalizeText(text, dirty);
        return status;
    }

    private Object[] initialize(boolean simulate) {
        PdfDocument pdf = null;
        PdfContentByte graphics = null;
        PdfContentByte text = null;
        int localRunDirection = PdfWriter.RUN_DIRECTION_NO_BIDI;
        float ratio = spaceCharRatio;
        descender = 0;
        linesWritten = 0;
        firstLineY = Float.NaN;

        if (runDirection != PdfWriter.RUN_DIRECTION_DEFAULT) {
            localRunDirection = runDirection;
        }

        if (canvas != null) {
            graphics = canvas;
            pdf = canvas.getPdfDocument();
            text = canvas.getDuplicate();
        } else if (!simulate) {
            throw new NullPointerException(MessageLocalization.getComposedMessage(
                    "columntext.go.with.simulate.eq.eq.false.and.text.eq.eq.null"));
        }

        if (!simulate) {
            ratio = (spaceCharRatio == GLOBAL_SPACE_CHAR_RATIO) ? text.getPdfWriter().getSpaceCharRatio() : Math.max(spaceCharRatio, 0.001f);
        }

        return new Object[]{pdf, graphics, text, ratio, localRunDirection};
    }

    private float calculateFirstIndent() {
        return lastWasNewline ? indent : followingIndent;
    }

    private Object[] handleRectangularMode(boolean simulate, float firstIndent,
            PdfContentByte text, boolean dirty, int localRunDirection) throws DocumentException {
        if (rectangularWidth <= firstIndent + rightIndent) {
            return new Object[]{handleNoMoreColumn(), dirty, null};
        }
        if (bidiLine.isEmpty()) {
            return new Object[]{NO_MORE_TEXT, dirty, null};
        }

        PdfLine line = bidiLine.processLine(leftX, rectangularWidth - firstIndent - rightIndent, alignment, localRunDirection, arabicOptions);
        if (line == null) {
            return new Object[]{NO_MORE_TEXT, dirty, null};
        }

        if (isOutOfBounds(line)) {
            bidiLine.restore();
            return new Object[]{NO_MORE_COLUMN, dirty, line};
        }

        updateYLine();
        dirty = beginTextIfNeeded(simulate, text, dirty);
        updateFilledWidth(rectangularWidth - line.widthLeft());

        return new Object[]{0, dirty, line};
    }

    private Object[] handleNonRectangularMode(boolean simulate, float firstIndent,
            PdfContentByte text, boolean dirty, int localRunDirection) throws DocumentException {
        float[] xx = findLimitsTwoLines();
        if (xx == null) {
            return new Object[]{handleNoMoreColumn(), dirty, null};
        }
        if (bidiLine.isEmpty()) {
            return new Object[]{NO_MORE_TEXT, dirty, null};
        }

        float x1 = Math.max(xx[0], xx[2]);
        float x2 = Math.min(xx[1], xx[3]);

        if (x2 - x1 <= firstIndent + rightIndent) {
            return new Object[]{0, dirty, null};
        }

        dirty = beginTextIfNeeded(simulate, text, dirty);

        PdfLine line = bidiLine.processLine(x1, x2 - x1 - firstIndent - rightIndent, alignment, localRunDirection, arabicOptions);
        if (line == null) {
            return new Object[]{NO_MORE_TEXT, dirty, null};
        }

        return new Object[]{0, dirty, line};
    }

    private void processLine(boolean simulate, float firstIndent, PdfDocument pdf,
            PdfContentByte graphics, PdfContentByte text, float ratio, PdfLine line) throws DocumentException {
        if (!simulate) {
            currentValues[0] = currentFont;
            text.setTextMatrix(leftX + (line.isRTL() ? rightIndent : firstIndent) + line.indentLeft(), yLine);
            pdf.writeLineToContent(line, text, graphics, currentValues, ratio);
            currentFont = (PdfFont) currentValues[0];
        }
        lastWasNewline = line.isNewlineSplit();
        yLine -= line.isNewlineSplit() ? extraParagraphSpace : 0;
        ++linesWritten;
        descender = line.getDescender();
    }

    private boolean isOutOfBounds(PdfLine line) {
        float[] maxSize = line.getMaxSize();
        if (isUseAscender() && Float.isNaN(firstLineY)) {
            currentLeading = line.getAscender();
        } else {
            currentLeading = Math.max(fixedLeading + maxSize[0] * multipliedLeading, maxSize[1]);
        }
        return yLine > maxY || yLine - currentLeading < minY;
    }

    private void updateYLine() {
        yLine -= currentLeading;
        if (Float.isNaN(firstLineY)) {
            firstLineY = yLine;
        }
    }

    private boolean beginTextIfNeeded(boolean simulate, PdfContentByte text, boolean dirty) {
        if (!simulate && !dirty) {
            text.beginText();
            return true;
        }
        return false;
    }

    private int handleNoMoreColumn() {
        int status = NO_MORE_COLUMN;
        if (bidiLine.isEmpty()) {
            status |= NO_MORE_TEXT;
        }
        return status;
    }

    private void finalizeText(PdfContentByte text, boolean dirty) throws DocumentException {
        if (dirty) {
            text.endText();
            if (canvas != null) {
                canvas.add(text);
            }
        }
    }

    /**
     * Sets the extra space between paragraphs.
     *
     * @return the extra space between paragraphs
     */
    public float getExtraParagraphSpace() {
        return extraParagraphSpace;
    }

    /**
     * Sets the extra space between paragraphs.
     *
     * @param extraParagraphSpace the extra space between paragraphs
     */
    public void setExtraParagraphSpace(float extraParagraphSpace) {
        this.extraParagraphSpace = extraParagraphSpace;
    }

    /**
     * Clears the chunk array. A call to <CODE>go()</CODE> will always return NO_MORE_TEXT.
     */
    public void clearChunks() {
        if (bidiLine != null) {
            bidiLine.clearChunks();
        }
    }

    /**
     * Gets the space/character extra spacing ratio for fully justified text.
     *
     * @return the space/character extra spacing ratio
     */
    public float getSpaceCharRatio() {
        return spaceCharRatio;
    }

    /**
     * Sets the ratio between the extra word spacing and the extra character spacing when the text is fully justified.
     * Extra word spacing will grow <CODE>spaceCharRatio</CODE> times more than extra character spacing. If the ratio
     * is
     * <CODE>PdfWriter.NO_SPACE_CHAR_RATIO</CODE> then the extra character spacing will be zero.
     *
     * @param spaceCharRatio the ratio between the extra word spacing and the extra character spacing
     */
    public void setSpaceCharRatio(float spaceCharRatio) {
        this.spaceCharRatio = spaceCharRatio;
    }

    /**
     * Gets the run direction.
     *
     * @return the run direction
     */
    public int getRunDirection() {
        return runDirection;
    }

    /**
     * Sets the run direction.
     *
     * @param runDirection the run direction
     */
    public void setRunDirection(int runDirection) {
        if (runDirection < PdfWriter.RUN_DIRECTION_DEFAULT || runDirection > PdfWriter.RUN_DIRECTION_RTL) {
            throw new InvalidRunDirectionException(MessageLocalization.getComposedMessage("invalid.run.direction.1", runDirection));
        }
        this.runDirection = runDirection;
    }

    /**
     * Gets the number of lines written.
     *
     * @return the number of lines written
     */
    public int getLinesWritten() {
        return this.linesWritten;
    }

    /**
     * Gets the arabic shaping options.
     *
     * @return the arabic shaping options
     */
    public int getArabicOptions() {
        return this.arabicOptions;
    }

    /**
     * Sets the arabic shaping options. The option can be AR_NOVOWEL, AR_COMPOSEDTASHKEEL and AR_LIG.
     *
     * @param arabicOptions the arabic shaping options
     */
    public void setArabicOptions(int arabicOptions) {
        this.arabicOptions = arabicOptions;
    }

    /**
     * Gets the biggest descender value of the last line written.
     *
     * @return the biggest descender value of the last line written
     */
    public float getDescender() {
        return descender;
    }

    protected int goComposite(boolean simulate) throws DocumentException {
        if (!rectangularMode) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("irregular.columns.are.not.supported.in.composite.mode"));
        }
        linesWritten = 0;
        descender = 0;
        boolean firstPass = adjustFirstLine;

        while (true) {
            if (compositeElements.isEmpty()) {
                return NO_MORE_TEXT;
            }

            Element element = compositeElements.getFirst();
            switch (element.type()) {
                case Element.PARAGRAPH:
                    return handleParagraph((Paragraph) element, firstPass, simulate);

                case Element.LIST:
                    return handleList((com.lowagie.text.List) element,
                            simulate,
                            firstPass);

                case Element.PTABLE:
                    return handleTable((PdfPTable) element, simulate, firstPass);

                case Element.YMARK:
                    if (!simulate) {
                        ((DrawInterface) element).draw(canvas, leftX, minY, rightX, maxY, yLine);
                    }
                    compositeElements.removeFirst();
                    break;

                default:
                    compositeElements.removeFirst();
                    break;
            }
            firstPass = false;
        }
    }

    private int handleList(com.lowagie.text.List list, boolean simulate, boolean firstPass) throws DocumentException {
        ListItem item = findListItem(list);
        if (item == null) {
            listIdx = 0;
            compositeElements.removeFirst();
            return NO_MORE_TEXT;
        }

        int status = 0;
        for (int keep = 0; keep < 2; ++keep) {
            float lastY = yLine;
            if (initializeCompositeColumn(item, list, firstPass) && !firstPass) {
                yLine -= item.getSpacingBefore();
            }

            status = processCompositeColumn(item, simulate, keep, lastY);
            if (shouldBreakSimulation(simulate, status, keep)) break;
        }

        finalizeListProcessing(status, item);
        return determineFinalStatus(status);
    }

    private ListItem findListItem(com.lowagie.text.List list) {
        java.util.List<Element> items = list.getItems();
        float listIndentation = list.getIndentationLeft();
        int count = 0;
        Deque<Object[]> stack = new ArrayDeque<>();

        for (int k = 0; k < items.size(); ++k) {
            Object obj = items.get(k);
            if (obj instanceof ListItem listitem) {
                if (count == listIdx) {
                    return listitem;
                }
                ++count;
            } else if (obj instanceof com.lowagie.text.List lowagieList) {
                stack.push(new Object[]{list, k, listIndentation});
                list = lowagieList;
                items = list.getItems();
                listIndentation += list.getIndentationLeft();
                k = -1;
            }
            if (k == items.size() - 1 && !stack.isEmpty()) {
                Object[] objs = stack.pop();
                list = (com.lowagie.text.List) objs[0];
                items = list.getItems();
                k = (Integer) objs[1];
                listIndentation = (Float) objs[2];
            }
        }
        return new ListItem();
    }

    private boolean initializeCompositeColumn(ListItem item, com.lowagie.text.List list, boolean firstPass) {
        if (compositeColumn == null) {
            compositeColumn = new ColumnText(canvas);
            compositeColumn.setUseAscender(firstPass && useAscender);
            compositeColumn.setAlignment(item.getAlignment());
            compositeColumn.setIndent(item.getIndentationLeft() + list.getIndentationLeft() + item.getFirstLineIndent());
            compositeColumn.setExtraParagraphSpace(item.getExtraParagraphSpace());
            compositeColumn.setFollowingIndent(compositeColumn.getIndent());
            compositeColumn.setRightIndent(item.getIndentationRight() + list.getIndentationRight());
            compositeColumn.setLeading(item.getLeading(), item.getMultipliedLeading());
            compositeColumn.setRunDirection(item.getRunDirection() == PdfWriter.RUN_DIRECTION_DEFAULT ? runDirection : item.getRunDirection());
            compositeColumn.setArabicOptions(arabicOptions);
            compositeColumn.setSpaceCharRatio(spaceCharRatio);
            compositeColumn.addText(item.getListSymbol());
            return true;
        }
        return false;
    }

    private int processCompositeColumn(ListItem item, boolean simulate, int keep, float lastY) throws DocumentException {
        compositeColumn.leftX = leftX;
        compositeColumn.rightX = rightX;
        compositeColumn.yLine = yLine;
        compositeColumn.rectangularWidth = rectangularWidth;
        compositeColumn.rectangularMode = rectangularMode;
        compositeColumn.minY = minY;
        compositeColumn.maxY = maxY;

        boolean keepCandidate = (item != null && item.getKeepTogether() && !simulate);
        int status = compositeColumn.go(simulate || (keepCandidate && keep == 0));
        updateFilledWidth(compositeColumn.filledWidth);

        if ((status & NO_MORE_TEXT) == 0 && keepCandidate) {
            compositeColumn = null;
            yLine = lastY;
            return NO_MORE_COLUMN;
        }
        if (keep == 0 && !simulate) {
            compositeColumn = null;
            yLine = lastY;
        }

        return status;
    }

    private boolean shouldBreakSimulation(boolean simulate, int status, int keep) {
        return simulate || (keep == 0 && (status & NO_MORE_TEXT) == 0);
    }

    private void finalizeListProcessing(int status, ListItem item) {
        yLine = compositeColumn.yLine;
        linesWritten += compositeColumn.linesWritten;
        descender = compositeColumn.descender;

        if (!Float.isNaN(compositeColumn.firstLineY) && !compositeColumn.firstLineYDone) {
            drawListSymbol(item);
            compositeColumn.firstLineYDone = true;
        }

        if ((status & NO_MORE_TEXT) != 0) {
            compositeColumn = null;
            ++listIdx;
            if (item != null) {
                yLine -= item.getSpacingAfter();
            }
        }
    }

    private void drawListSymbol(ListItem item) {
        if (item != null) {
            int elementRunDirection = (item.getRunDirection() == PdfWriter.RUN_DIRECTION_NO_BIDI || item.getRunDirection() == PdfWriter.RUN_DIRECTION_DEFAULT)
                    ? PdfWriter.RUN_DIRECTION_NO_BIDI : item.getRunDirection();
            TextAlignmentSettings settings = new TextAlignmentSettings(Element.ALIGN_LEFT,
                    compositeColumn.leftX + item.getIndentationLeft(), compositeColumn.firstLineY,
                    0, elementRunDirection, 0);
            showTextAligned(canvas, new Phrase(item.getListSymbol()), settings);
        }
    }

    private int determineFinalStatus(int status) {
        if ((status & NO_MORE_COLUMN) != 0) {
            return NO_MORE_COLUMN;
        }
        return 0;
    }


    private int handleTable(PdfPTable table, boolean simulate, boolean firstPass) throws DocumentException {
        if (yLine < minY || yLine > maxY) {
            return NO_MORE_COLUMN;
        }

        if (table.size() <= table.getHeaderRows()) {
            compositeElements.removeFirst();
            return NO_MORE_TEXT;
        }

        float yTemp = yLine;
        if (!firstPass && listIdx == 0) {
            yTemp -= table.spacingBefore();
        }

        if (yTemp < minY || yTemp > maxY) {
            return NO_MORE_COLUMN;
        }

        PdfPTable localTable = table;
        if (compositeColumn == null) {
            compositeColumn = new ColumnText(canvas);
        }
        compositeColumn.leftX = leftX;
        compositeColumn.rightX = rightX;
        compositeColumn.yLine = yTemp;
        compositeColumn.rectangularWidth = rectangularWidth;
        compositeColumn.rectangularMode = rectangularMode;
        compositeColumn.minY = minY;
        compositeColumn.maxY = maxY;
        compositeColumn.runDirection = runDirection;
        compositeColumn.adjustFirstLine = adjustFirstLine;
        compositeColumn.setUseAscender(firstPass && useAscender);
        compositeColumn.addElement(localTable);

        int status = compositeColumn.go(simulate);
        updateFilledWidth(compositeColumn.filledWidth);

        yLine = compositeColumn.yLine;
        linesWritten += compositeColumn.linesWritten;
        descender = compositeColumn.descender;

        if ((status & NO_MORE_TEXT) != 0) {
            compositeColumn = null;
            compositeElements.removeFirst();
            yLine -= table.spacingAfter();
        }

        if ((status & NO_MORE_COLUMN) != 0) {
            return NO_MORE_COLUMN;
        }

        return 0;
    }

    private int handleParagraph(Paragraph para, boolean firstPass, boolean simulate) throws DocumentException {
        int status = 0;
        for (int keep = 0; keep < 2; ++keep) {
            float lastY = yLine;
            boolean createHere = false;

            if (compositeColumn == null) {
                compositeColumn = createCompositeColumnForParagraph(para, firstPass);
                if (!firstPass) {
                    yLine -= para.getSpacingBefore();
                }
                createHere = true;
            }

            status = processColumn(para.getKeepTogether() && createHere && !firstPass, simulate, keep, lastY);
            if (statusNeedsBreaking(status, simulate, keep)) break;
        }

        updateLineAndStatusForParagraph(status, para);
        return checkStatusAndRemoveElement(status);
    }

    private ColumnText createCompositeColumnForParagraph(Paragraph para, boolean firstPass) {
        ColumnText column = new ColumnText(canvas);
        column.setUseAscender(firstPass && useAscender);
        column.setAlignment(para.getAlignment());
        column.setIndent(para.getIndentationLeft() + para.getFirstLineIndent());
        column.setExtraParagraphSpace(para.getExtraParagraphSpace());
        column.setFollowingIndent(para.getIndentationLeft());
        column.setRightIndent(para.getIndentationRight());
        column.setLeading(para.getLeading(), para.getMultipliedLeading());
        column.setRunDirection(para.getRunDirection() == PdfWriter.RUN_DIRECTION_DEFAULT ? runDirection : para.getRunDirection());
        column.setArabicOptions(arabicOptions);
        column.setSpaceCharRatio(spaceCharRatio);
        column.addText(para);
        return column;
    }

    private int processColumn(boolean keepCandidate, boolean simulate, int keep, float lastY) throws DocumentException {
        compositeColumn.leftX = leftX;
        compositeColumn.rightX = rightX;
        compositeColumn.yLine = yLine;
        compositeColumn.rectangularWidth = rectangularWidth;
        compositeColumn.rectangularMode = rectangularMode;
        compositeColumn.minY = minY;
        compositeColumn.maxY = maxY;

        int status = compositeColumn.go(simulate || (keepCandidate && keep == 0));
        updateFilledWidth(compositeColumn.filledWidth);

        if ((status & NO_MORE_TEXT) == 0 && keepCandidate) {
            compositeColumn = null;
            yLine = lastY;
            return NO_MORE_COLUMN;
        }

        if (simulate || !keepCandidate) {
            return status;
        }

        if (keep == 0) {
            compositeColumn = null;
            yLine = lastY;
        }

        return status;
    }

    private boolean statusNeedsBreaking(int status, boolean simulate, int keep) {
        return simulate || (status & NO_MORE_TEXT) == 0 || keep == 0;
    }

    private void updateLineAndStatusForParagraph(int status, Paragraph para) {
        if (compositeColumn != null) {
            yLine = compositeColumn.yLine;
            linesWritten += compositeColumn.linesWritten;
            descender = compositeColumn.descender;
        }

        if ((status & NO_MORE_TEXT) != 0) {
            compositeColumn = null;
            compositeElements.removeFirst();
            yLine -= para.getSpacingAfter();
        }
    }

    private int checkStatusAndRemoveElement(int status) {
        if ((status & NO_MORE_COLUMN) != 0) {
            return NO_MORE_COLUMN;
        }

        if (compositeColumn != null) {
            yLine = compositeColumn.yLine;
            linesWritten += compositeColumn.linesWritten;
            descender = compositeColumn.descender;
        }
        return 0;
    }

    public void pushToStack(Object[] item, Deque<Object[]> stack) {
        stack.addFirst(item);  // equivalent to Stack.push(item)
    }

    public Object[] popFromStack(Deque<Object[]> stack) {
        return stack.removeFirst();  // equivalent to Stack.pop()
    }

    public Object[] peekAtStack(Deque<Object[]> stack) {
        return stack.peekFirst();  // equivalent to Stack.peek()
    }

    public boolean isStackEmpty(Deque<Object[]> stack) {
        return stack.isEmpty();  // equivalent to Stack.isEmpty()
    }

    public static void main(String[] args) {
        String stringToLog;
        PdfWriter writer = new PdfWriter();
        PdfContentByte canvas = new PdfContentByte(writer);
        ColumnText example = new ColumnText(canvas);
        ArrayDeque<Object[]> stack = new ArrayDeque<>();
        example.pushToStack(new Object[] {"first item"}, stack);
        stringToLog = example.peekAtStack(stack)[0].toString();
        logger.info(stringToLog);  // Output: first item
        stringToLog = example.popFromStack(stack)[0].toString();
        logger.info(stringToLog);  // Output: first item
        stringToLog = String.valueOf(example.isStackEmpty(stack));
        logger.info(stringToLog);  // Output: true
    }

    /**
     * Gets the canvas. If a set of four canvases exists, the TEXTCANVAS is returned.
     *
     * @return a PdfContentByte.
     */
    public PdfContentByte getCanvas() {
        return canvas;
    }

    /**
     * Sets the canvas. If before a set of four canvases was set, it is being unset.
     *
     * @param canvas an object of {@link PdfContentByte}
     */
    public void setCanvas(PdfContentByte canvas) {
        this.canvas = canvas;
        this.canvases = null;
        if (compositeColumn != null) {
            compositeColumn.setCanvas(canvas);
        }
    }

    /**
     * Gets the canvases.
     *
     * @return an array of PdfContentByte
     */
    public PdfContentByte[] getCanvases() {
        return canvases;
    }

    /**
     * Sets the canvases.
     *
     * @param canvases an array of {@link PdfContentByte}
     */
    public void setCanvases(PdfContentByte[] canvases) {
        this.canvases = canvases;
        this.canvas = canvases[PdfPTable.TEXTCANVAS];
        if (compositeColumn != null) {
            compositeColumn.setCanvases(canvases);
        }
    }

    /**
     * Checks if the element has a height of 0.
     *
     * @return true or false
     * @since 2.1.2
     */
    public boolean zeroHeightElement() {
        return composite && !compositeElements.isEmpty() && compositeElements.getFirst().type() == Element.YMARK;
    }

    /**
     * Checks if UseAscender is enabled/disabled.
     *
     * @return true is the adjustment of the first line height is based on max ascender.
     */
    public boolean isUseAscender() {
        return useAscender;
    }

    /**
     * Enables/Disables adjustment of first line height based on max ascender.
     *
     * @param useAscender enable adjustment if true
     */
    public void setUseAscender(boolean useAscender) {
        this.useAscender = useAscender;
    }

    /**
     * Gets the real width used by the largest line.
     *
     * @return the real width used by the largest line
     */
    public float getFilledWidth() {
        return filledWidth;
    }

    /**
     * Sets the real width used by the largest line. Only used to set it to zero to start another measurement.
     *
     * @param filledWidth the real width used by the largest line
     */
    public void setFilledWidth(float filledWidth) {
        this.filledWidth = filledWidth;
    }

    /**
     * Replaces the <CODE>filledWidth</CODE> if greater than the existing one.
     *
     * @param w the new <CODE>filledWidth</CODE> if greater than the existing one
     */
    public void updateFilledWidth(float w) {
        if (w > filledWidth) {
            filledWidth = w;
        }
    }


    /**
     * Gets the first line adjustment property.
     *
     * @return the first line adjustment property.
     */
    public boolean isAdjustFirstLine() {
        return adjustFirstLine;
    }

    /**
     * Sets the first line adjustment. Some objects have properties, like spacing before, that behave differently if the
     * object is the first to be written after go() or not. The first line adjustment is <CODE>true</CODE> by default
     * but can be changed if several objects are to be placed one after the other in the same column calling go()
     * several times.
     *
     * @param adjustFirstLine <CODE>true</CODE> to adjust the first line, <CODE>false</CODE> otherwise
     */
    public void setAdjustFirstLine(boolean adjustFirstLine) {
        this.adjustFirstLine = adjustFirstLine;
    }
}
