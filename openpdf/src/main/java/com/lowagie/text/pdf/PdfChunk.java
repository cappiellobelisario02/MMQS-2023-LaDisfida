/*
 * $Id: PdfChunk.java 4092 2009-11-11 17:58:16Z psoares33 $
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

import com.lowagie.text.Chunk;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.SplitCharacter;
import com.lowagie.text.Utilities;
import com.lowagie.text.exceptions.BaseFontException;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A <CODE>PdfChunk</CODE> is the PDF translation of a <CODE>Chunk</CODE>.
 * <p>
 * A <CODE>PdfChunk</CODE> is a <CODE>PdfString</CODE> in a certain
 * <CODE>PdfFont</CODE> and <CODE>Color</CODE>.
 *
 * @see PdfString
 * @see com.lowagie.text.Chunk
 * @see com.lowagie.text.Font
 */

public class PdfChunk {

    private static final char SINGLE_SPACE = ' ';
    private static final PdfChunk[] thisChunk = new PdfChunk[1];
    private static final float ITALIC_ANGLE = 0.21256f;
    /**
     * The allowed attributes in variable <CODE>attributes</CODE>.
     */
    private static final Map<String, Object> keysAttributes = new HashMap<>();

    /**
     * The allowed attributes in variable <CODE>noStroke</CODE>.
     */
    private static final Map<String, Object> keysNoStroke = new HashMap<>();
    public static final String CODE0001 = "\u0001";

    static {
        keysAttributes.put(Chunk.ACTION, null);
        keysAttributes.put(Chunk.UNDERLINE, null);
        keysAttributes.put(Chunk.REMOTEGOTO, null);
        keysAttributes.put(Chunk.LOCALGOTO, null);
        keysAttributes.put(Chunk.LOCALDESTINATION, null);
        keysAttributes.put(Chunk.GENERICTAG, null);
        keysAttributes.put(Chunk.NEWPAGE, null);
        keysAttributes.put(Chunk.IMAGE, null);
        keysAttributes.put(Chunk.BACKGROUND, null);
        keysAttributes.put(Chunk.PDFANNOTATION, null);
        keysAttributes.put(Chunk.SKEW, null);
        keysAttributes.put(Chunk.HSCALE, null);
        keysAttributes.put(Chunk.SEPARATOR, null);
        keysAttributes.put(Chunk.TAB, null);
        keysAttributes.put(Chunk.CHAR_SPACING, null);
        keysNoStroke.put(Chunk.SUBSUPSCRIPT, null);
        keysNoStroke.put(Chunk.SPLITCHARACTER, null);
        keysNoStroke.put(Chunk.HYPHENATION, null);
        keysNoStroke.put(Chunk.TEXTRENDERMODE, null);
    }

    // membervariables

    /**
     * The value of this object.
     */
    protected String value = PdfObject.NOTHING;

    /**
     * The encoding.
     */
    protected String encoding = BaseFont.WINANSI;


    /**
     * The font for this <CODE>PdfChunk</CODE>.
     */
    protected PdfFont font;

    protected BaseFont baseFont;

    protected SplitCharacter splitCharacter;
    /**
     * Metric attributes.
     * <p>
     * This attributes require the measurement of characters widths when rendering such as underline.
     */
    protected Map<String, Object> attributes = new HashMap<>();

    /**
     * Non metric attributes.
     * <p>
     * This attributes do not require the measurement of characters widths when rendering such as Color.
     */
    protected Map<String, Object> noStroke = new HashMap<>();

    /**
     * <CODE>true</CODE> if the chunk split was cause by a newline.
     */
    protected boolean newlineSplit;

    /**
     * The image in this <CODE>PdfChunk</CODE>, if it has one
     */
    protected Image image;

    /**
     * The offset in the x direction for the image
     */
    protected float offsetX;

    /**
     * The offset in the y direction for the image
     */
    protected float offsetY;

    /**
     * Indicates if the height and offset of the Image has to be taken into account
     */
    protected boolean changeLeading = false;

    // constructors

    /**
     * Constructs a <CODE>PdfChunk</CODE>-object.
     *
     * @param string the content of the <CODE>PdfChunk</CODE>-object
     * @param other  Chunk with the same style you want for the new Chunk
     */

    PdfChunk(String string, PdfChunk other) {
        value = string;
        this.font = other.font;
        this.attributes = other.attributes;
        this.noStroke = other.noStroke;
        this.baseFont = other.baseFont;
        Object[] obj = (Object[]) attributes.get(Chunk.IMAGE);
        if (obj == null) {
            image = null;
        } else {
            image = (Image) obj[0];
            offsetX = (Float) obj[1];
            offsetY = (Float) obj[2];
            changeLeading = (Boolean) obj[3];
        }
        encoding = font.getFont().getEncoding();
        splitCharacter = (SplitCharacter) noStroke.get(Chunk.SPLITCHARACTER);
        if (splitCharacter == null) {
            splitCharacter = DefaultSplitCharacter.DEFAULT;
        }
    }

    /**
     * Constructs a <CODE>PdfChunk</CODE>-object.
     *
     * @param chunk  the original <CODE>Chunk</CODE>-object
     * @param action the <CODE>PdfAction</CODE> if the <CODE>Chunk</CODE> comes from an <CODE>Anchor</CODE>
     */

    PdfChunk(Chunk chunk, PdfAction action) {
        value = chunk.getContent();
        Font f = chunk.getFont();

        float size = initializeFontSize(f);
        baseFont = initializeBaseFont(f, chunk);
        processFontStyles(f, size);

        font = new PdfFont(baseFont, size);

        processAttributes(chunk);
        processUnderlineAndStrikethrough(f);
        processAction(action);
        processImage();

        font.setImage(image);
        processHorizontalScaling();
        encoding = font.getFont().getEncoding();
        processSplitCharacter();
    }

    private float initializeFontSize(Font f) {
        float size = f.getSize();
        return size == Font.UNDEFINED ? 12 : size;
    }

    private BaseFont initializeBaseFont(Font f, Chunk chunk) {
        BaseFont bf = f.getBaseFont();
        if (bf == null) {
            return determineBaseFont(chunk, f);
        }
        return bf;
    }

    private BaseFont determineBaseFont(Chunk chunk, Font f) {
        if (chunk.getContent().chars().allMatch(c -> (c >= 0x0 && c <= 0xFF))) {
            return f.getCalculatedBaseFont(false);
        } else {
            try {
                return BaseFont.createFont("font-fallback/LiberationSans-Regular.ttf",
                        BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } catch (IOException e) {
                throw new BaseFontException(e.getMessage());
            }
        }
    }

    private void processFontStyles(Font f, float size) {
        int style = f.getStyle();
        if (style == Font.UNDEFINED) {
            style = Font.NORMAL;
        }
        if ((style & Font.BOLD) != 0) {
            attributes.put(Chunk.TEXTRENDERMODE,
                    new Object[]{PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE, size / 30f, null});
        }
        if ((style & Font.ITALIC) != 0) {
            attributes.put(Chunk.SKEW, new float[]{0, ITALIC_ANGLE});
        }
    }

    private void processAttributes(Chunk chunk) {
        Map<String, Object> attr = chunk.getChunkAttributes();
        if (attr != null) {
            for (Map.Entry<String, Object> entry : attr.entrySet()) {
                String name = entry.getKey();
                if (keysAttributes.containsKey(name)) {
                    attributes.put(name, entry.getValue());
                } else if (keysNoStroke.containsKey(name)) {
                    noStroke.put(name, entry.getValue());
                }
            }
            if ("".equals(attr.get(Chunk.GENERICTAG))) {
                attributes.put(Chunk.GENERICTAG, chunk.getContent());
            }
        }
    }

    private void processUnderlineAndStrikethrough(Font f) {
        if (f.isUnderlined()) {
            Object[] obj = {null, new float[]{0, 1f / 15, 0, -1f / 3, 0}};
            Object[][] unders = Utilities.addToArray((Object[][]) attributes.get(Chunk.UNDERLINE), obj);
            attributes.put(Chunk.UNDERLINE, unders);
        }
        if (f.isStrikethru()) {
            Object[] obj = {null, new float[]{0, 1f / 15, 0, 1f / 3, 0}};
            Object[][] unders = Utilities.addToArray((Object[][]) attributes.get(Chunk.UNDERLINE), obj);
            attributes.put(Chunk.UNDERLINE, unders);
        }
    }

    private void processAction(PdfAction action) {
        if (action != null) {
            attributes.put(Chunk.ACTION, action);
        }
    }

    private void processImage() {
        Object[] obj = (Object[]) attributes.get(Chunk.IMAGE);
        if (obj == null) {
            image = null;
        } else {
            attributes.remove(Chunk.HSCALE); // images are scaled in other ways
            image = (Image) obj[0];
            offsetX = (Float) obj[1];
            offsetY = (Float) obj[2];
            changeLeading = (Boolean) obj[3];
        }
    }

    private void processHorizontalScaling() {
        Float hs = (Float) attributes.get(Chunk.HSCALE);
        if (hs != null) {
            font.setHorizontalScaling(hs);
        }
    }

    private void processSplitCharacter() {
        splitCharacter = (SplitCharacter) noStroke.get(Chunk.SPLITCHARACTER);
        if (splitCharacter == null) {
            splitCharacter = DefaultSplitCharacter.DEFAULT;
        }
    }


    // methods

    public static boolean noPrint(int c) {
        return ((c >= 0x200b && c <= 0x200f) || (c >= 0x202a && c <= 0x202e));
    }

    /**
     * Gets the Unicode equivalent to a CID. The (nonexistent) CID <code>FF00</code> is translated as '\n'. It has only
     * meaning with CJK fonts with Identity encoding.
     *
     * @param c the CID code
     * @return the Unicode equivalent
     */
    public int getUnicodeEquivalent(int c) {
        return baseFont.getUnicodeEquivalent(c);
    }

    protected int getWord(String text, int start) {
        int len = text.length();
        while (start < len) {
            if (!Character.isLetter(text.charAt(start))) {
                break;
            }
            ++start;
        }
        return start;
    }

    /**
     * Splits this <CODE>PdfChunk</CODE> if it's too long for the given width.
     * <p>
     * Returns <VAR>null</VAR> if the <CODE>PdfChunk</CODE> wasn't truncated.
     *
     * @param width a given width
     * @return the <CODE>PdfChunk</CODE> that doesn't fit into the width.
     */

    PdfChunk split(float width) {
        newlineSplit = false;

        if (shouldReturnImageChunk(width)) {
            return createImageChunk();
        }

        int currentPosition = 0;
        float currentWidth = 0;
        int splitPosition = -1;
        int lastSpace = -1;
        float lastSpaceWidth = 0;
        char[] valueArray = value.toCharArray();
        int length = valueArray.length;

        while (currentPosition < length) {
            char character = valueArray[currentPosition];

            if (shouldSplitForNewline(currentPosition, length, character)) {
                return handleNewlineSplit(currentPosition, character);
            }

            boolean surrogate = Utilities.isSurrogatePair(valueArray, currentPosition);
            currentWidth += calculateCurrentWidth(character, surrogate, currentPosition, valueArray);

            if (character == SINGLE_SPACE) {
                lastSpace = currentPosition + 1;
                lastSpaceWidth = currentWidth;
            }

            if (currentWidth > width) {
                break;
            }

            if (shouldUpdateSplitPosition(currentPosition, length, valueArray)) {
                splitPosition = currentPosition + 1;
            }

            currentPosition += surrogate ? 2 : 1;
        }

        return handleTruncatedString(currentPosition, length, splitPosition, lastSpace, lastSpaceWidth, width);
    }

    private boolean shouldReturnImageChunk(float width) {
        return image != null && image.getScaledWidth() > width;
    }

    private PdfChunk createImageChunk() {
        PdfChunk pc = new PdfChunk(Chunk.OBJECT_REPLACEMENT_CHARACTER, this);
        value = "";
        attributes.clear();
        image = null;
        font = PdfFont.getDefaultFont();
        return pc;
    }

    private boolean shouldSplitForNewline(int currentPosition, int length, char character) {
        return character == '\n' || (character == '\r' && (currentPosition + 1 >= length || value.charAt(currentPosition + 1) != '\n'));
    }

    private PdfChunk handleNewlineSplit(int currentPosition, char character) {
        newlineSplit = true;
        int inc = (character == '\r' && currentPosition + 1 < value.length() && value.charAt(currentPosition + 1) == '\n') ? 2 : 1;
        String returnValue = value.substring(currentPosition + inc);
        value = value.substring(0, currentPosition);
        if (value.isEmpty()) {
            value = " ";
        }
        return new PdfChunk(returnValue, this);
    }

    private float calculateCurrentWidth(char character, boolean surrogate, int currentPosition, char[] valueArray) {
        if (surrogate) {
            return getCharWidth(Utilities.convertToUtf32(valueArray[currentPosition], valueArray[currentPosition + 1]));
        } else {
            return getCharWidth(character);
        }
    }

    private boolean shouldUpdateSplitPosition(int currentPosition, int length, char[] valueArray) {
        return splitCharacter.isSplitCharacter(0, currentPosition, length, valueArray, thisChunk);
    }

    private PdfChunk handleTruncatedString(int currentPosition, int length, int splitPosition, int lastSpace, float lastSpaceWidth, float width) {
        if (currentPosition == length) {
            return null;
        }
        if (splitPosition < 0) {
            String returnValue = value;
            value = "";
            return new PdfChunk(returnValue, this);
        }
        if (lastSpace > splitPosition && splitCharacter.isSplitCharacter(0, 0, 1, new char[]{' '}, null)) {
            splitPosition = lastSpace;
        }
        HyphenationEvent hyphenationEvent = (HyphenationEvent) noStroke.get(Chunk.HYPHENATION);
        if (hyphenationEvent != null && lastSpace >= 0 && lastSpace < currentPosition) {
            PdfChunk hyphenatedChunk = handleHyphenation(lastSpace, lastSpaceWidth, width, hyphenationEvent);
            if (hyphenatedChunk != null) {
                return hyphenatedChunk;
            }
        }
        String returnValue = value.substring(splitPosition);
        value = trim(value.substring(0, splitPosition));
        return new PdfChunk(returnValue, this);
    }

    private PdfChunk handleHyphenation(int lastSpace, float lastSpaceWidth, float width, HyphenationEvent hyphenationEvent) {
        int wordIdx = getWord(value, lastSpace);
        if (wordIdx > lastSpace) {
            String pre = hyphenationEvent.getHyphenatedWordPre(value.substring(lastSpace, wordIdx), font.getFont(), font.size(), width - lastSpaceWidth);
            String post = hyphenationEvent.getHyphenatedWordPost();
            if (!pre.isEmpty()) {
                String returnValue = post + value.substring(wordIdx);
                value = trim(value.substring(0, lastSpace) + pre);
                return new PdfChunk(returnValue, this);
            }
        }
        return null;
    }


    // methods to retrieve the membervariables

    /**
     * Truncates this <CODE>PdfChunk</CODE> if it's too long for the given width.
     * <p>
     * Returns <VAR>null</VAR> if the <CODE>PdfChunk</CODE> wasn't truncated.
     *
     * @param width a given width
     * @return the <CODE>PdfChunk</CODE> that doesn't fit into the width.
     */

    PdfChunk truncate(float width) {
        if (shouldTruncateImage(width)) {
            return handleImageTruncation();
        }

        if (width < font.width()) {
            return truncateToFirstCharacter();
        }

        int truncatePosition = findTruncatePosition(width);
        return finalizeTruncation(truncatePosition);
    }

    private boolean shouldTruncateImage(float width) {
        return image != null && image.getScaledWidth() > width;
    }

    private PdfChunk handleImageTruncation() {
        PdfChunk pc = new PdfChunk("", this);
        value = "";
        attributes.remove(Chunk.IMAGE);
        image = null;
        font = PdfFont.getDefaultFont();
        return pc;
    }

    private PdfChunk truncateToFirstCharacter() {
        String returnValue = value.substring(1);
        value = value.substring(0, 1);
        return new PdfChunk(returnValue, this);
    }

    private int findTruncatePosition(float width) {
        int currentPosition = 0;
        float currentWidth = 0;
        int length = value.length();
        boolean surrogate = false;

        while (currentPosition < length) {
            surrogate = Utilities.isSurrogatePair(value, currentPosition);
            currentWidth += surrogate ? getCharWidth(Utilities.convertToUtf32(value, currentPosition)) : getCharWidth(value.charAt(currentPosition));
            if (currentWidth > width) {
                break;
            }
            currentPosition += surrogate ? 2 : 1;
        }

        return currentPosition == length ? -1 : currentPosition;
    }

    private PdfChunk finalizeTruncation(int currentPosition) {
        if (currentPosition == -1) {
            return null;
        }

        if (currentPosition == 0) {
            currentPosition = Utilities.isSurrogatePair(value, 0) ? 2 : 1;
        }

        String returnValue = value.substring(currentPosition);
        value = value.substring(0, currentPosition);
        return new PdfChunk(returnValue, this);
    }


    /**
     * Returns the font of this <CODE>Chunk</CODE>.
     *
     * @return a <CODE>PdfFont</CODE>
     */

    PdfFont getFont() { // Renamed method for clarity
        return font;
    }

    /**
     * Returns the color of this <CODE>Chunk</CODE>.
     *
     * @return a <CODE>Color</CODE>
     */

    Color color() {
        return (Color) noStroke.get(Chunk.COLOR);
    }

    /**
     * Returns the width of this <CODE>PdfChunk</CODE>.
     *
     * @return a width
     */

    float width() {
        if (isAttribute(Chunk.TAB)) {
            return 0.0f;
        }
        if (isAttribute(Chunk.CHAR_SPACING)) {
            Float cs = (Float) getAttribute(Chunk.CHAR_SPACING);
            return font.width(value) + value.length() * cs;
        }
        return font.width(value);
    }

    /**
     * Checks if the <CODE>PdfChunk</CODE> split was caused by a newline.
     *
     * @return <CODE>true</CODE> if the <CODE>PdfChunk</CODE> split was caused by a newline.
     */

    public boolean isNewlineSplit() {
        return newlineSplit;
    }

    /**
     * Gets the width of the <CODE>PdfChunk</CODE> taking into account the extra character and word spacing.
     *
     * @param charSpacing the extra character spacing
     * @param wordSpacing the extra word spacing
     * @return the calculated width
     */

    public float getWidthCorrected(float charSpacing, float wordSpacing) {
        if (image != null) {
            return image.getScaledWidth() + charSpacing;
        }
        int numberOfSpaces = 0;
        int idx = -1;
        while ((idx = value.indexOf(' ', idx + 1)) >= 0) {
            ++numberOfSpaces;
        }
        return width() + (value.length() * charSpacing + numberOfSpaces * wordSpacing);
    }

    /**
     * Gets the text displacement relative to the baseline.
     *
     * @return a displacement in points
     */
    public float getTextRise() {
        Float f = (Float) getAttribute(Chunk.SUBSUPSCRIPT);
        if (f != null) {
            return f;
        }
        return 0.0f;
    }

    /**
     * Trims the last space.
     *
     * @return the width of the space trimmed, otherwise 0
     */

    public float trimLastSpace() {
        BaseFont ft = font.getFont();
        if (ft.getFontType() == BaseFont.FONT_TYPE_CJK && ft.getUnicodeEquivalent(' ') != ' ') {
            if (value.length() > 1 && value.endsWith(CODE0001)) {
                value = value.substring(0, value.length() - 1);
                return font.width('\u0001');
            }
        } else {
            if (value.length() > 1 && value.endsWith(" ")) {
                value = value.substring(0, value.length() - 1);
                return font.width(' ');
            }
        }
        return 0;
    }

    public float trimFirstSpace() {
        BaseFont ft = font.getFont();
        if (ft.getFontType() == BaseFont.FONT_TYPE_CJK && ft.getUnicodeEquivalent(' ') != ' ') {
            if (value.length() > 1 && value.startsWith(CODE0001)) {
                value = value.substring(1);
                return font.width('\u0001');
            }
        } else {
            if (value.length() > 1 && value.startsWith(" ")) {
                value = value.substring(1);
                return font.width(' ');
            }
        }
        return 0;
    }

    /**
     * Gets an attribute. The search is made in <CODE>attributes</CODE> and <CODE>noStroke</CODE>.
     *
     * @param name the attribute key
     * @return the attribute value or null if not found
     */

    Object getAttribute(String name) {
        if (attributes.containsKey(name)) {
            return attributes.get(name);
        }
        return noStroke.get(name);
    }

    /**
     * Checks if the attribute exists.
     *
     * @param name the attribute key
     * @return <CODE>true</CODE> if the attribute exists
     */

    boolean isAttribute(String name) {
        if (attributes.containsKey(name)) {
            return true;
        }
        return noStroke.containsKey(name);
    }

    /**
     * Checks if this <CODE>PdfChunk</CODE> needs some special metrics handling.
     *
     * @return <CODE>true</CODE> if this <CODE>PdfChunk</CODE> needs some special metrics handling.
     */

    boolean isStroked() {
        return (!attributes.isEmpty());
    }

    /**
     * Checks if this <CODE>PdfChunk</CODE> is a Separator Chunk.
     *
     * @return true if this chunk is a separator.
     * @since 2.1.2
     */
    boolean isSeparator() {
        return isAttribute(Chunk.SEPARATOR);
    }

    /**
     * Checks if this <CODE>PdfChunk</CODE> is a horizontal Separator Chunk.
     *
     * @return true if this chunk is a horizontal separator.
     * @since 2.1.2
     */
    boolean isHorizontalSeparator() {
        if (isAttribute(Chunk.SEPARATOR)) {
            Object[] o = (Object[]) getAttribute(Chunk.SEPARATOR);
            return !(Boolean) o[1];
        }
        return false;
    }

    /**
     * Checks if this <CODE>PdfChunk</CODE> is a vertical Separator Chunk.
     *
     * @return true if this chunk is a vertical separator.
     * @since OpenPDF
     */
    boolean isVerticalSeparator() {
        if (isAttribute(Chunk.SEPARATOR)) {
            Object[] o = (Object[]) getAttribute(Chunk.SEPARATOR);
            return (Boolean) o[1];
        }
        return false;
    }

    /**
     * Checks if this <CODE>PdfChunk</CODE> is a tab Chunk.
     *
     * @return true if this chunk is a separator.
     * @since 2.1.2
     */
    boolean isTab() {
        return isAttribute(Chunk.TAB);
    }

    /**
     * Correction for the tab position based on the left starting position.
     *
     * @param newValue the new value for the left X.
     * @since 2.1.2
     */
    void adjustLeft(float newValue) {
        Object[] o = (Object[]) attributes.get(Chunk.TAB);
        if (o != null) {
            attributes.put(Chunk.TAB, new Object[]{o[0], o[1], o[2], newValue});
        }
    }

    /**
     * Checks if there is an image in the <CODE>PdfChunk</CODE>.
     *
     * @return <CODE>true</CODE> if an image is present
     */

    boolean isImage() {
        return image != null;
    }

    /**
     * Gets the image in the <CODE>PdfChunk</CODE>.
     *
     * @return the image or <CODE>null</CODE>
     */

    Image getImage() {
        return image;
    }

    /**
     * Gets the image offset in the x direction
     *
     * @return the image offset in the x direction
     */

    float getImageOffsetX() {
        return offsetX;
    }

    /**
     * Sets the image offset in the x direction
     *
     * @param offsetX the image offset in the x direction
     */

    void setImageOffsetX(float offsetX) {
        this.offsetX = offsetX;
    }

    /**
     * Gets the image offset in the y direction
     *
     * @return Gets the image offset in the y direction
     */

    float getImageOffsetY() {
        return offsetY;
    }

    /**
     * Sets the image offset in the y direction
     *
     * @param offsetY the image offset in the y direction
     */

    void setImageOffsetY(float offsetY) {
        this.offsetY = offsetY;
    }

    /**
     * sets the value.
     *
     * @param value content of the Chunk
     */

    void setValue(String value) {
        this.value = value;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return value;
    }

    /**
     * Tells you if this string is in Chinese, Japanese, Korean or Identity-H.
     *
     * @return true if the Chunk has a special encoding
     */

    boolean isSpecialEncoding() {
        return encoding.equals(CJKFont.CJK_ENCODING) || encoding.equals(BaseFont.IDENTITY_H);
    }

    /**
     * Gets the encoding of this string.
     *
     * @return a <CODE>String</CODE>
     */

    String getEncoding() {
        return encoding;
    }

    int length() {
        return value.length();
    }

    int lengthUtf32() {
        if (!BaseFont.IDENTITY_H.equals(encoding)) {
            return value.length();
        }
        int total = 0;
        int len = value.length();
        int k = 0;
        while (k < len) {
            if (Utilities.isSurrogateHigh(value.charAt(k))) {
                k++;
            }
            k++;
            total++;
        }
        return total;
    }

    boolean isExtSplitCharacter(int start, int current, int end, char[] cc, PdfChunk[] ck) {
        return splitCharacter.isSplitCharacter(start, current, end, cc, ck);
    }

    /**
     * Removes all the <VAR>' '</VAR> and <VAR>'-'</VAR>-characters on the right of a <CODE>String</CODE>.
     * <p>
     *
     * @param string the <CODE>String</CODE> that has to be trimmed.
     * @return the trimmed <CODE>String</CODE>
     */
    String trim(String string) {
        BaseFont ft = font.getFont();
        if (ft.getFontType() == BaseFont.FONT_TYPE_CJK && ft.getUnicodeEquivalent(' ') != ' ') {
            while (string.endsWith(CODE0001)) {
                string = string.substring(0, string.length() - 1);
            }
        } else {
            while (string.endsWith(" ") || string.endsWith("\t")) {
                string = string.substring(0, string.length() - 1);
            }
        }
        return string;
    }

    public boolean changeLeading() {
        return changeLeading;
    }

    float getCharWidth(int c) {
        if (noPrint(c)) {
            return 0;
        }
        if (isAttribute(Chunk.CHAR_SPACING)) {
            Float cs = (Float) getAttribute(Chunk.CHAR_SPACING);
            return font.width(c) + cs;
        }
        return font.width(c);
    }

}
