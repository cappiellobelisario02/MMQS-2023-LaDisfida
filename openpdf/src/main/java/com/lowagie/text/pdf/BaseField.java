/*
 * Copyright 2005 by Paulo Soares.
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

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Rectangle;
import com.lowagie.text.error_messages.MessageLocalization;
import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Common field variables.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */

public abstract class BaseField {

    /**
     * A thin border with 1 point width.
     */
    public static final float BORDER_WIDTH_THIN = 1;
    /**
     * A medium border with 2 point width.
     */
    public static final float BORDER_WIDTH_MEDIUM = 2;
    /**
     * A thick border with 3 point width.
     */
    public static final float BORDER_WIDTH_THICK = 3;
    /**
     * The field is visible.
     */
    public static final int VISIBLE = 0;
    /**
     * The field is hidden.
     */
    public static final int HIDDEN1 = 1;

    /**
     * The field is hidden but is printable.
     */
    public static final int HIDDEN_BUT_PRINTABLE = 3;
    /**
     * The field is visible but does not print.
     */
    public static final int VISIBLE_BUT_DOES_NOT_PRINT = 4;

    /**
     * The annotation flag: Invisible.
     */
    public static final int INVISIBLE = PdfAnnotation.FLAGS_INVISIBLE;
    /** The annotation flag Hidden. */
    public static final int HIDDEN = PdfAnnotation.FLAGS_HIDDEN;
    /**
     * The annotation flag Hidden.
     */
    public static final int PRINT = PdfAnnotation.FLAGS_PRINT;
    /**
     * The annotation flag Hidden.
     */
    public static final int NOVIEW = PdfAnnotation.FLAGS_NOVIEW;
    /**
     * The annotation flag Hidden.
     */
    public static final int LOCKED = PdfAnnotation.FLAGS_LOCKED;


    /**
     * The user may not change the value of the field.
     */
    public static final int READ_ONLY = PdfFormField.FF_READ_ONLY;
    /**
     * The field must have a value at the time it is exported by a submit-form action.
     */
    public static final int REQUIRED = PdfFormField.FF_REQUIRED;
    /**
     * The field may contain multiple lines of text. This flag is only meaningful with text fields.
     */
    public static final int MULTILINE = PdfFormField.FF_MULTILINE;
    /**
     * The field will not scroll (horizontally for single-line fields, vertically for multiple-line fields) to
     * accommodate more text than will fit within its annotation rectangle. Once the field is full, no further text will
     * be accepted.
     */
    public static final int DO_NOT_SCROLL = PdfFormField.FF_DONOTSCROLL;
    /**
     * The field is intended for entering a secure password that should not be echoed visibly to the screen.
     */
    public static final int PASSWORD = PdfFormField.FF_PASSWORD;
    /**
     * The text entered in the field represents the pathname of a file whose contents are to be submitted as the value
     * of the field.
     */
    public static final int FILE_SELECTION = PdfFormField.FF_FILESELECT;
    /**
     * The text entered in the field will not be spell-checked. This flag is meaningful only in text fields and in combo
     * fields with the <CODE>EDIT</CODE> flag set.
     */
    public static final int DO_NOT_SPELL_CHECK = PdfFormField.FF_DONOTSPELLCHECK;
    /**
     * If set the combo box includes an editable text box as well as a drop list; if clear, it includes only a drop
     * list. This flag is only meaningful with combo fields.
     */
    public static final int EDIT = PdfFormField.FF_EDIT;

    /**
     * whether or not a list may have multiple selections.  Only applies to /CH LIST fields, not combo boxes.
     */
    public static final int MULTISELECT = PdfFormField.FF_MULTISELECT;

    /**
     * combo box flag.
     */
    public static final int COMB = PdfFormField.FF_COMB;
    private static final Map<PdfName, Integer> fieldKeys = new HashMap<>();

    static {
        fieldKeys.putAll(PdfCopyFieldsImp.fieldKeys);
        fieldKeys.put(PdfName.T, 1);
    }

    protected float borderWidth = BORDER_WIDTH_THIN;
    protected int borderStyle = PdfBorderDictionary.STYLE_SOLID;
    protected Color borderColor;
    protected static Color backgroundColor;
    protected Color textColor;
    protected BaseFont font;
    protected float fontSize = 0;
    protected int alignment = Element.ALIGN_LEFT;
    protected PdfWriter writer;
    protected String text;
    protected Rectangle box;
    /**
     * Holds value of property rotation.
     */
    protected int rotation = 0;
    /**
     * Holds value of property visibility.
     */
    protected int visibility;
    /**
     * Holds value of property fieldName.
     */
    protected String fieldName;
    /**
     * Holds the value of the alternate field getName. (PDF attribute 'TU')
     */
    protected String alternateFieldName;
    /**
     * Holds the value of the mapping field getName. (PDF attribute 'TM')
     */
    protected String mappingName;
    /**
     * Holds value of property options.
     */
    protected int options;
    /**
     * Holds value of property maxCharacterLength.
     */
    protected int maxCharacterLength;

    /**
     * Creates a new <CODE>TextField</CODE>.
     *
     * @param writer    the document <CODE>PdfWriter</CODE>
     * @param box       the field location and dimensions
     * @param fieldName the field getName. If <CODE>null</CODE> only the widget keys will be included in the field allowing
     *                  it to be used as a kid field.
     */
    protected BaseField(PdfWriter writer, Rectangle box, String fieldName) {
        this.writer = writer;
        setBox(box);
        this.fieldName = fieldName;
    }

    protected static PdfAppearance getBorderAppearance(PdfWriter writer, BoxSettings boxSettings, AppearanceSettings appearanceSettings) {
        Rectangle box = boxSettings.getBox();
        int rotation = boxSettings.getRotation();
        Color backgroundColor = appearanceSettings.getBackgroundColor();
        int borderStyle = appearanceSettings.getBorderStyle();
        float borderWidth = appearanceSettings.getBorderWidth();
        Color borderColor = appearanceSettings.getBorderColor();
        int options = appearanceSettings.getOptions();
        int maxCharacterLength = appearanceSettings.getMaxCharacterLength();

        PdfAppearance app = PdfAppearance.createAppearance(writer, box.getWidth(), box.getHeight());
        applyRotation(app, rotation, box);
        app.saveState();
        drawBackground(app, backgroundColor, box);
        drawBorder(app, borderStyle, borderWidth, borderColor, box, options, maxCharacterLength);
        app.restoreState();
        return app;
    }

    private static void applyRotation(PdfAppearance app, int rotation, Rectangle box) {
        switch (rotation) {
            case 90 -> app.setMatrix(0, 1, -1, 0, box.getHeight(), 0);
            case 180 -> app.setMatrix(-1, 0, 0, -1, box.getWidth(), box.getHeight());
            case 270 -> app.setMatrix(0, -1, 1, 0, 0, box.getWidth());
            default -> throw new IllegalStateException("Unexpected value: " + rotation);
        }
    }

    private static void drawBackground(PdfAppearance app, Color backgroundColor, Rectangle box) {
        if (backgroundColor != null) {
            app.setColorFill(backgroundColor);
            app.rectangle(0, 0, box.getWidth(), box.getHeight());
            app.fill();
        }
    }

    private static void drawBorder(PdfAppearance app, int borderStyle, float borderWidth, Color borderColor, Rectangle box, int options, int maxCharacterLength) {
        if (borderWidth == 0 || borderColor == null) {
            return;
        }

        app.setColorStroke(borderColor);
        app.setLineWidth(borderWidth);

        switch (borderStyle) {
            case PdfBorderDictionary.STYLE_UNDERLINE -> drawUnderlineBorder(app, borderWidth, box);
            case PdfBorderDictionary.STYLE_BEVELED -> drawBeveledBorder(app, borderWidth, box, backgroundColor);
            case PdfBorderDictionary.STYLE_INSET -> drawInsetBorder(app, borderWidth, box);
            default -> drawDefaultBorder(app, borderStyle, borderWidth, box, options, maxCharacterLength);
        }
    }

    private static void drawUnderlineBorder(PdfAppearance app, float borderWidth, Rectangle box) {
        app.moveTo(0, borderWidth / 2);
        app.lineTo(box.getWidth(), borderWidth / 2);
        app.stroke();
    }

    private static void drawBeveledBorder(PdfAppearance app, float borderWidth, Rectangle box, Color backgroundColor) {
        app.rectangle(borderWidth / 2, borderWidth / 2, box.getWidth() - borderWidth, box.getHeight() - borderWidth);
        app.stroke();
        Color actual = backgroundColor != null ? backgroundColor : Color.white;
        app.setGrayFill(1);
        drawTopFrame(app, borderWidth, box);
        app.setColorFill(actual.darker());
        drawBottomFrame(app, borderWidth, box);
    }

    private static void drawInsetBorder(PdfAppearance app, float borderWidth, Rectangle box) {
        app.rectangle(borderWidth / 2, borderWidth / 2, box.getWidth() - borderWidth, box.getHeight() - borderWidth);
        app.stroke();
        app.setGrayFill(0.5f);
        drawTopFrame(app, borderWidth, box);
        app.setGrayFill(0.75f);
        drawBottomFrame(app, borderWidth, box);
    }

    private static void drawDefaultBorder(PdfAppearance app, int borderStyle, float borderWidth, Rectangle box, int options, int maxCharacterLength) {
        if (borderStyle == PdfBorderDictionary.STYLE_DASHED) {
            app.setLineDash(3, 0);
        }
        app.rectangle(borderWidth / 2, borderWidth / 2, box.getWidth() - borderWidth, box.getHeight() - borderWidth);
        app.stroke();
        if ((options & COMB) != 0 && maxCharacterLength > 1) {
            drawCombBorder(app, borderWidth, box, maxCharacterLength);
        }
    }

    private static void drawCombBorder(PdfAppearance app, float borderWidth, Rectangle box, int maxCharacterLength) {
        float step = box.getWidth() / maxCharacterLength;
        float yb = borderWidth / 2;
        float yt = box.getHeight() - borderWidth / 2;
        for (int k = 1; k < maxCharacterLength; ++k) {
            float x = step * k;
            app.moveTo(x, yb);
            app.lineTo(x, yt);
        }
        app.stroke();
    }

    private static void drawTopFrame(PdfAppearance app, float borderWidth, Rectangle box) {
        app.moveTo(borderWidth, borderWidth);
        app.lineTo(borderWidth, box.getHeight() - borderWidth);
        app.lineTo(box.getWidth() - borderWidth, box.getHeight() - borderWidth);
        app.lineTo(box.getWidth() - 2 * borderWidth, box.getHeight() - 2 * borderWidth);
        app.lineTo(2 * borderWidth, box.getHeight() - 2 * borderWidth);
        app.lineTo(2 * borderWidth, 2 * borderWidth);
        app.lineTo(borderWidth, borderWidth);
        app.fill();
    }

    private static void drawBottomFrame(PdfAppearance app, float borderWidth, Rectangle box) {
        app.moveTo(borderWidth, borderWidth);
        app.lineTo(box.getWidth() - borderWidth, borderWidth);
        app.lineTo(box.getWidth() - borderWidth, box.getHeight() - borderWidth);
        app.lineTo(box.getWidth() - 2 * borderWidth, box.getHeight() - 2 * borderWidth);
        app.lineTo(box.getWidth() - 2 * borderWidth, 2 * borderWidth);
        app.lineTo(2 * borderWidth, 2 * borderWidth);
        app.lineTo(borderWidth, borderWidth);
        app.fill();
    }

    /**
     * Moves the field keys from <CODE>from</CODE> to <CODE>to</CODE>. The moved keys are removed from
     * <CODE>from</CODE>.
     *
     * @param from the source
     * @param to   the destination. It may be <CODE>null</CODE>
     */
    public static void moveFields(PdfDictionary from, PdfDictionary to) {
        for (Iterator<PdfName> i = from.getKeys().iterator(); i.hasNext(); ) {
            PdfName key = i.next();
            if (fieldKeys.containsKey(key)) {
                if (to != null) {
                    to.put(key, from.get(key));
                }
                i.remove();
            }
        }
    }

    protected BaseFont getRealFont() throws IOException, DocumentException {
        if (font == null) {
            return BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
        } else {
            return font;
        }
    }

    /**
     * Gets the border width in points.
     *
     * @return the border width in points
     */
    public float getBorderWidth() {
        return this.borderWidth;
    }

    /**
     * Sets the border width in points. To eliminate the border set the border color to <CODE>null</CODE>.
     *
     * @param borderWidth the border width in points
     */
    public void setBorderWidth(float borderWidth) {
        this.borderWidth = borderWidth;
    }

    /**
     * Gets the border style.
     *
     * @return the border style
     */
    public int getBorderStyle() {
        return this.borderStyle;
    }

    /**
     * Sets the border style. The styles are found in <CODE>PdfBorderDictionary</CODE> and can be
     * <CODE>STYLE_SOLID</CODE>, <CODE>STYLE_DASHED</CODE>,
     * <CODE>STYLE_BEVELED</CODE>, <CODE>STYLE_INSET</CODE> and
     * <CODE>STYLE_UNDERLINE</CODE>.
     *
     * @param borderStyle the border style
     */
    public void setBorderStyle(int borderStyle) {
        this.borderStyle = borderStyle;
    }

    /**
     * Gets the border color.
     *
     * @return the border color
     */
    public Color getBorderColor() {
        return this.borderColor;
    }

    /**
     * Sets the border color. Set to <CODE>null</CODE> to remove the border.
     *
     * @param borderColor the border color
     */
    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    /**
     * Gets the background color.
     *
     * @return the background color
     */
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    /**
     * Sets the background color. Set to <CODE>null</CODE> for transparent background.
     *
     * @param backgroundColor the background color
     */
    public void setBackgroundColor(Color backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    /**
     * Gets the text color.
     *
     * @return the text color
     */
    public Color getTextColor() {
        return this.textColor;
    }

    /**
     * Sets the text color. If <CODE>null</CODE> the color used will be black.
     *
     * @param textColor the text color
     */
    public void setTextColor(Color textColor) {
        this.textColor = textColor;
    }

    /**
     * Gets the text font.
     *
     * @return the text font
     */
    public BaseFont getFont() {
        return this.font;
    }

    /**
     * Sets the text font. If <CODE>null</CODE> then Helvetica will be used.
     *
     * @param font the text font
     */
    public void setFont(BaseFont font) {
        this.font = font;
    }

    /**
     * Gets the font size.
     *
     * @return the font size
     */
    public float getFontSize() {
        return this.fontSize;
    }

    /**
     * Sets the font size. If 0 then auto-sizing will be used but only for text fields.
     *
     * @param fontSize the font size
     */
    public void setFontSize(float fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * Gets the text horizontal getAlignment.
     *
     * @return the text horizontal getAlignment
     */
    public int getAlignment() {
        return this.alignment;
    }

    /**
     * Sets the text horizontal getAlignment. It can be <CODE>Element.ALIGN_LEFT</CODE>,
     * <CODE>Element.ALIGN_CENTER</CODE> and <CODE>Element.ALIGN_RIGHT</CODE>.
     *
     * @param alignment the text horizontal getAlignment
     */
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    /**
     * Gets the text.
     *
     * @return the text
     */
    public String getText() {
        return this.text;
    }

    /**
     * Sets the text for text fields.
     *
     * @param text the text
     */
    public void setText(String text) {
        this.text = text;
    }

    /**
     * Gets the field dimension and position.
     *
     * @return the field dimension and position
     */
    public Rectangle getBox() {
        return this.box;
    }

    /**
     * Sets the field dimension and position.
     *
     * @param box the field dimension and position
     */
    public void setBox(Rectangle box) {
        if (box == null) {
            this.box = null;
        } else {
            this.box = new Rectangle(box);
            this.box.normalize();
        }
    }

    /**
     * Gets the field rotation.
     *
     * @return the field rotation
     */
    public int getRotation() {
        return this.rotation;
    }

    /**
     * Sets the field rotation. This value should be the same as the page rotation where the field will be shown.
     *
     * @param rotation the field rotation
     */
    public void setRotation(int rotation) {
        if (rotation % 90 != 0) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("rotation.must.be.a.multiple.of.90"));
        }
        rotation %= 360;
        if (rotation < 0) {
            rotation += 360;
        }
        this.rotation = rotation;
    }

    /**
     * Convenience method to set the field rotation the same as the page rotation.
     *
     * @param page the page
     */
    public void setRotationFromPage(Rectangle page) {
        setRotation(page.getRotationPdfPCell());
    }

    /**
     * Gets the field visibility flag.
     *
     * @return the field visibility flag
     */
    public int getVisibility() {
        return this.visibility;
    }

    /**
     * Sets the field visibility flag. This flags can be one of
     * <CODE>VISIBLE</CODE>, <CODE>HIDDEN</CODE>, <CODE>VISIBLE_BUT_DOES_NOT_PRINT</CODE>
     * and <CODE>HIDDEN_BUT_PRINTABLE</CODE>.
     *
     * @param visibility field visibility flag
     */
    public void setVisibility(int visibility) {
        this.visibility = visibility;
    }

    /**
     * Gets the field getName.
     *
     * @return the field getName
     */
    public String getFieldName() {
        return this.fieldName;
    }

    /**
     * Sets the field getName.
     *
     * @param fieldName the field getName. If <CODE>null</CODE> only the widget keys will be included in the field allowing
     *                  it to be used as a kid field.
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Gets the alternate field getName. (PDF attribute TU)
     *
     * @return the alternate field getName
     */
    public String getAlternateFieldName() {
        return this.alternateFieldName;
    }

    /**
     * Sets the alternateFieldName field getName.
     *
     * @param alternateFieldName the alternate field getName.
     */
    public void setAlternateFieldName(String alternateFieldName) {
        this.alternateFieldName = alternateFieldName;
    }

    /**
     * Gets the mapping getName. (PDF attribute TM)
     *
     * @return the mapping field getName
     */
    public String getMappingName() {
        return this.mappingName;
    }

    /**
     * Sets the mapping getName. (PDF TM)
     *
     * @param mappingName the mapping getName.
     */
    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    /**
     * Gets the option flags.
     *
     * @return the option flags
     */
    public int getOptions() {
        return this.options;
    }

    /**
     * Sets the option flags. The option flags can be a combination by oring of
     * <CODE>READ_ONLY</CODE>, <CODE>REQUIRED</CODE>,
     * <CODE>MULTILINE</CODE>, <CODE>DO_NOT_SCROLL</CODE>,
     * <CODE>PASSWORD</CODE>, <CODE>FILE_SELECTION</CODE>,
     * <CODE>DO_NOT_SPELL_CHECK</CODE> and <CODE>EDIT</CODE>.
     *
     * @param options the option flags
     */
    public void setOptions(int options) {
        this.options = options;
    }

    /**
     * Gets the maximum length of the field's text, in characters.
     *
     * @return the maximum length of the field's text, in characters.
     */
    public int getMaxCharacterLength() {
        return this.maxCharacterLength;
    }

    /**
     * Sets the maximum length of the field's text, in characters. It is only meaningful for text fields.
     *
     * @param maxCharacterLength the maximum length of the field's text, in characters
     */
    public void setMaxCharacterLength(int maxCharacterLength) {
        this.maxCharacterLength = maxCharacterLength;
    }

    /**
     * Getter for property writer.
     *
     * @return Value of property writer.
     */
    public PdfWriter getWriter() {
        return writer;
    }

    /**
     * Setter for property writer.
     *
     * @param writer New value of property writer.
     */
    public void setWriter(PdfWriter writer) {
        this.writer = writer;
    }
}
