/*
 * Copyright 2003-2005 by Paulo Soares.
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

import com.ibm.icu.text.Bidi;
import com.lowagie.text.Chunk;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.exceptions.AnnotationException;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Supports text, combo and list fields generating the correct appearances. All the option in the Acrobat GUI are
 * supported in an easy-to-use API.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class TextField extends BaseField {

    float offX;
    private float offsetX;

    /**
     * Holds value of property defaultText.
     */
    private String defaultText;

    /**
     * Holds value of property choices.
     */
    private String[] choices;

    /**
     * Holds value of property choiceExports.
     */
    private String[] choiceExports;

    /**
     * Holds value of property choiceSelection.
     */
    private ArrayList<Integer> choiceSelections = new ArrayList<>();

    private int topFirst;

    private float extraMarginLeft;
    private float extraMarginTop;
    /**
     * Holds value of property substitutionFonts.
     */
    private List<BaseFont> substitutionFonts;
    /**
     * Holds value of property extensionFont.
     */
    private BaseFont extensionFont;

    private static final Logger logger = Logger.getLogger(TextField.class.getName());

    /**
     * Creates a new <CODE>TextField</CODE>.
     *
     * @param writer    the document <CODE>PdfWriter</CODE>
     * @param box       the field location and dimensions
     * @param fieldName the field getName. If <CODE>null</CODE> only the widget keys will be included in the field allowing
     *                  it to be used as a kid field.
     */
    public TextField(PdfWriter writer, Rectangle box, String fieldName) {
        super(writer, box, fieldName);
    }

    private static boolean checkRTL(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        char[] cc = text.toCharArray();
        for (int c : cc) {
            if (c >= 0x590 && c < 0x0780) {
                return true;
            }
        }
        return false;
    }

    private static int textRunDirectionDefault(String ptext) {
        return checkRTL(ptext) ? PdfWriter.RUN_DIRECTION_LTR : PdfWriter.RUN_DIRECTION_NO_BIDI;
    }

    /**
     * Chose the run direction of a text field by its content
     *
     * @param ptext text field content
     * @return int run direction (PdfWriter.RUN_DIRECTION_LTR or PdfWriter.RUN_DIRECTION_RTL or
     * PdfWriter.RUN_DIRECTION_NO_BIDI)
     */
    private static int textRunDirectionByContent(String ptext) {
        if (ptext == null || ptext.isEmpty()) {
            return PdfWriter.RUN_DIRECTION_NO_BIDI;
        }

        Bidi bidi = new Bidi();
        bidi.setPara(ptext, Bidi.LTR, null);

        byte direction = bidi.getDirection();

        if (direction == Bidi.LTR) {
            // This is what OpenPDF uses for LTR
            // https://github.com/LibrePDF/OpenPDF/blob/1.3.26/openpdf/src/main/java/com/lowagie/text/pdf/TextField.java#L211
            return PdfWriter.RUN_DIRECTION_NO_BIDI;
        } else if (direction == Bidi.RTL) {
            // We can't have RUN_DIRECTION_NO_BIDI for RTL
            // In RTL texts ending in punctuation this will place the punctuation in the
            // start of the file.
            return PdfWriter.RUN_DIRECTION_RTL;
        }

        // Bidi.MIXED => Choose LTR/RTL based on which is more prominent
        int ltrCount = 0;
        byte[] levels = bidi.getLevels();

        for (byte level : levels) {
            if (level % 2 == 0) {
                ltrCount++;
            }
        }

        if ((double) ltrCount / levels.length >= 0.5) {
            return PdfWriter.RUN_DIRECTION_LTR;
        } else {
            return PdfWriter.RUN_DIRECTION_RTL;
        }
    }

    private static int textRunDirection(String ptext) {
        try {
            Class.forName("com.ibm.icu.text.Bidi");
            return textRunDirectionByContent(ptext);
        } catch (ClassNotFoundException e) {
            return textRunDirectionDefault(ptext);
        }
    }

    private static void changeFontSize(Phrase p, float size) {
        for (Object o : p) {
            ((Chunk) o).getFont().setSize(size);
        }
    }

    /**
     * Removes CRLF from a <code>String</code>.
     *
     * @param text the String to remove the CRLF from
     * @return String
     * @since 2.1.5
     */
    public static String removeCRLF(String text) {
        if (text.indexOf('\n') >= 0 || text.indexOf('\r') >= 0) {
            char[] p = text.toCharArray();
            StringBuilder sb = new StringBuilder(p.length);
            int k = 0; // Initialize the counter outside the loop

            while (k < p.length) {
                char c = p[k];
                if (c == '\n') {
                    sb.append(' ');
                    k++; // Increment k after handling '\n'
                } else if (c == '\r') {
                    sb.append(' ');
                    k++; // Increment k for '\r'
                    if (k < p.length && p[k] == '\n') {
                        k++; // Skip the next character if it's '\n'
                    }
                } else {
                    sb.append(c);
                    k++; // Increment k for other characters
                }
            }
            return sb.toString();
        }
        return text;
    }


    /**
     * Obfuscates a password <code>String</code>. Every character is replaced by an asterisk (*).
     *
     * @param text the text to obfuscate
     * @return String
     * @since 2.1.5
     */
    public static String obfuscatePassword(String text) {
        char[] pchar = new char[text.length()];
        for (int i = 0; i < text.length(); i++) {
            pchar[i] = '*';
        }
        return new String(pchar);
    }

    private Phrase composePhrase(String text, BaseFont ufont, Color color, float fontSize) {
        Phrase phrase;
        if (extensionFont == null && (substitutionFonts == null || substitutionFonts.isEmpty())) {
            phrase = new Phrase(new Chunk(text, new Font(ufont, fontSize, 0, color)));
        } else {
            FontSelector fs = new FontSelector();
            fs.addFont(new Font(ufont, fontSize, 0, color));
            if (extensionFont != null) {
                fs.addFont(new Font(extensionFont, fontSize, 0, color));
            }
            if (substitutionFonts != null) {
                for (BaseFont substitutionFont : substitutionFonts) {
                    fs.addFont(new Font(substitutionFont, fontSize, 0, color));
                }
            }
            phrase = fs.process(text);
        }
        return phrase;
    }

    /**
     * Flip text getAlignment for RTL texts Not sure why but this is needed
     */
    private int getTextAlignment(int runDirection) {
        if (runDirection == PdfWriter.RUN_DIRECTION_RTL) {
            if (alignment == Element.ALIGN_LEFT) {
                return Element.ALIGN_RIGHT;
            } else if (alignment == Element.ALIGN_RIGHT) {
                return Element.ALIGN_LEFT;
            } else {
                return alignment;
            }
        } else {
            return alignment;
        }
    }

    /**
     * Get the <code>PdfAppearance</code> of a text or combo field
     *
     * @return A <code>PdfAppearance</code>
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public PdfAppearance getAppearance() throws IOException, DocumentException {
        PdfAppearance app = initializeAppearance();
        if (isTextEmpty()) {
            app.endVariableText();
            return app;
        }

        configureAppearanceForBorder(app);
        prepareText(app);

        app.restoreState();
        app.endVariableText();
        return app;
    }

    private PdfAppearance initializeAppearance() throws DocumentException {
        BoxSettings boxSettings = new BoxSettings(super.box, super.rotation);
        AppearanceSettings appearanceSettings = new AppearanceSettings(backgroundColor,
                super.borderStyle, super.borderWidth, super.borderColor, super.options, super.maxCharacterLength);
        return getBorderAppearance(super.writer, boxSettings, appearanceSettings);
    }

    private boolean isTextEmpty() {
        return text == null || text.isEmpty();
    }

    private void configureAppearanceForBorder(PdfAppearance app) {
        boolean borderExtra = isBorderExtra();
        //float h = calculateHeight(borderExtra
        float bw2 = calculateBorderWidth(borderExtra);
        offsetX = Math.max(bw2, 1);
        offX = Math.min(bw2, offsetX);

        app.saveState();
        app.rectangle(offX, offX, box.getWidth() - 2 * offX, box.getHeight() - 2 * offX);
        app.clip();
        app.newPath();
    }

    private boolean isBorderExtra() {
        return borderStyle == PdfBorderDictionary.STYLE_BEVELED || borderStyle == PdfBorderDictionary.STYLE_INSET;
    }

    private float calculateBorderWidth(boolean borderExtra) {
        float bw2 = borderWidth;
        if (borderExtra) {
            bw2 *= 2;
        }
        return bw2;
    }

    private void prepareText(PdfAppearance app) throws DocumentException, IOException {
        String ptext = prepareTextContent();
        BaseFont ufont = getRealFont();
        Color fcolor = (textColor == null) ? GrayColor.GRAYBLACK : textColor;
        int rtl = textRunDirection(ptext);
        float usize = fontSize;
        Phrase phrase = composePhrase(ptext, ufont, fcolor, usize);

        if ((options & MULTILINE) != 0) {
            handleMultilineText(app, ufont, rtl, phrase);
        } else {
            handleSingleLineText(app, ufont, rtl, phrase, usize, ptext);
        }
    }

    private String prepareTextContent() {
        if ((options & PASSWORD) != 0) {
            return obfuscatePassword(text);
        } else if ((options & MULTILINE) == 0) {
            return removeCRLF(text);
        } else {
            return text;
        }
    }

    private void handleMultilineText(PdfAppearance app, BaseFont ufont, int rtl, Phrase phrase) throws DocumentException {
        float factor = ufont.getFontDescriptor(BaseFont.BBOXURY, 1) - ufont.getFontDescriptor(BaseFont.BBOXLLY, 1);
        ColumnText ct = new ColumnText(null);
        float usize = calculateOptimalFontSizeForMultiline(factor, ct, phrase, rtl);
        ct.setCanvas(app);
        float leading = usize * factor;
        float offsetY = offsetX + box.getHeight() - ufont.getFontDescriptor(BaseFont.BBOXURY, usize);
        ct.setSimpleColumn(extraMarginLeft + 2 * offsetX, -20000, box.getWidth() - 2 * offsetX, offsetY + leading);
        ct.setLeading(leading);
        ct.setAlignment(getTextAlignment(rtl));
        ct.setRunDirection(rtl);
        ct.setText(phrase);

        try {
            ct.go();
        } catch (IOException e) {
            // Log the exception and provide context for debugging
            logger.info("IOException occurred while handling multiline text: " + e.getMessage());

            // Optional: Re-throw or handle the exception based on your application's needs
            throw new DocumentException();
        }
    }


    private float calculateOptimalFontSizeForMultiline(float factor, ColumnText ct, Phrase phrase, int rtl) throws DocumentException {
        float usize = fontSize;
        if (usize == 0) {
            usize = box.getHeight() / factor;
            if (usize > 4) {
                usize = Math.min(12, usize);
                float step = Math.max((usize - 4) / 10, 0.2f);
                ct.setSimpleColumn(0, -box.getHeight(), box.getWidth(), 0);
                ct.setAlignment(getTextAlignment(rtl));
                ct.setRunDirection(rtl);
                for (; usize > 4; usize -= step) {
                    ct.setYLine(0);
                    changeFontSize(phrase, usize);
                    ct.setText(phrase);
                    ct.setLeading(factor * usize);
                    int status;
                    try {
                        status = ct.go(true);

                        if ((status & ColumnText.NO_MORE_COLUMN) == 0) {
                            break;
                        }
                    } catch (IOException e) {
                        //may need some logging or some other operation
                    }
                }
            }
            if (usize < 4) {
                usize = 4;
            }
        }
        return usize;
    }

    private void handleSingleLineText(PdfAppearance app, BaseFont ufont, int rtl, Phrase phrase, float usize,
            String ptext) throws DocumentException {
        if (usize == 0) {
            usize = calculateOptimalFontSizeForSingleLine(ufont, phrase, rtl);
        }
        changeFontSize(phrase, usize);
        float offsetY = calculateOffsetY(ufont, usize);
        if ((options & COMB) != 0 && maxCharacterLength > 0) {
            handleCombText(app, phrase, usize, offsetY, ptext);
        } else {
            handleAlignedText(app, phrase, offsetY, rtl);
        }
    }

    private float calculateOptimalFontSizeForSingleLine(BaseFont ufont, Phrase phrase, int rtl) {
        float maxCalculatedSize = box.getHeight() / (ufont.getFontDescriptor(BaseFont.BBOXURX, 1) - ufont.getFontDescriptor(BaseFont.BBOXLLY, 1));
        changeFontSize(phrase, 1);
        float wd = ColumnText.getWidth(phrase, rtl, 0);
        float usize = (wd == 0) ? maxCalculatedSize : Math.min(maxCalculatedSize, (box.getWidth() - extraMarginLeft - 4 * offsetX) / wd);
        return Math.max(usize, 4);
    }

    private float calculateOffsetY(BaseFont ufont, float usize) {
        float offsetY = offX + ((box.getHeight() - 2 * offX) - ufont.getFontDescriptor(BaseFont.ASCENT, usize)) / 2;
        if (offsetY < offX) {
            offsetY = offX;
        }
        if (offsetY - offX < -ufont.getFontDescriptor(BaseFont.DESCENT, usize)) {
            float ny = -ufont.getFontDescriptor(BaseFont.DESCENT, usize) + offX;
            float dy = box.getHeight() - offX - ufont.getFontDescriptor(BaseFont.ASCENT, usize);
            offsetY = Math.min(ny, Math.max(offsetY, dy));
        }
        return offsetY;
    }

    private void handleCombText(PdfAppearance app, Phrase phrase, float usize, float offsetY, String ptext) {
        int textLen = Math.min(maxCharacterLength, ptext.length());
        int position = calculateCombTextPosition(textLen);
        float step = (box.getWidth() - extraMarginLeft) / maxCharacterLength;
        float start = step / 2 + position * step;
        setTextColor(app);
        app.beginText();
        for (Object o : phrase) {
            Chunk ck = (Chunk) o;
            BaseFont bf = ck.getFont().getBaseFont();
            app.setFontAndSize(bf, usize);
            StringBuilder sb = ck.append("");
            for (int j = 0; j < sb.length(); ++j) {
                drawCharacter(app, bf, usize, start, offsetY, sb.substring(j, j + 1));
                start += step;
            }
        }
        app.endText();
    }

    private int calculateCombTextPosition(int textLen) {
        if (alignment == Element.ALIGN_RIGHT) {
            return maxCharacterLength - textLen;
        } else if (alignment == Element.ALIGN_CENTER) {
            return (maxCharacterLength - textLen) / 2;
        }
        return 0;
    }

    private void setTextColor(PdfAppearance app) {
        if (textColor == null) {
            app.setGrayFill(0);
        } else {
            app.setColorFill(textColor);
        }
    }

    private void drawCharacter(PdfAppearance app, BaseFont bf, float usize, float start, float offsetY, String c) {
        float wd = bf.getWidthPoint(c, usize);
        app.setTextMatrix(extraMarginLeft + start - wd / 2, offsetY - extraMarginTop);
        app.showText(c);
    }

    private void handleAlignedText(PdfAppearance app, Phrase phrase, float offsetY, int rtl) {
        float x = calculateAlignedTextX();
        TextAlignmentSettings settings = new TextAlignmentSettings(alignment, x,
                offsetY - extraMarginTop, 0, rtl, 0);
        ColumnText.showTextAligned(app, phrase, settings);
    }

    private float calculateAlignedTextX() {
        return switch (alignment) {
            case Element.ALIGN_RIGHT -> extraMarginLeft + box.getWidth() - (2 * offsetX);
            case Element.ALIGN_CENTER -> extraMarginLeft + (box.getWidth() / 2);
            default -> extraMarginLeft + (2 * offsetX);
        };
    }


    /**
     * Get the <code>PdfAppearance</code> of a list field
     *
     * @return A <code>PdfAppearance</code>
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    PdfAppearance getListAppearance() throws IOException, DocumentException {
        BoxSettings boxSettings = new BoxSettings(super.box, super.rotation);
        AppearanceSettings appearanceSettings = new AppearanceSettings(backgroundColor,
                super.borderStyle, super.borderWidth, super.borderColor, super.options, super.maxCharacterLength);
        PdfAppearance app = getBorderAppearance(super.writer, boxSettings, appearanceSettings);
        if (choices == null || choices.length == 0) {
            return app;
        }
        app.beginVariableText();

        int topChoice = getTopChoice();

        BaseFont ufont = getRealFont();
        float usize = fontSize;
        if (usize == 0) {
            usize = 12;
        }

        boolean borderExtra =
                borderStyle == PdfBorderDictionary.STYLE_BEVELED || borderStyle == PdfBorderDictionary.STYLE_INSET;
        float h = box.getHeight() - borderWidth * 2;
        offsetX = borderWidth;
        if (borderExtra) {
            h -= borderWidth * 2;
            offsetX *= 2;
        }

        float leading =
                ufont.getFontDescriptor(BaseFont.BBOXURY, usize) - ufont.getFontDescriptor(BaseFont.BBOXLLY, usize);
        int maxFit = (int) (h / leading) + 1;
        int first;
        int last;
        first = topChoice;
        last = first + maxFit;
        if (last > choices.length) {
            last = choices.length;
        }
        topFirst = first;
        app.saveState();
        app.rectangle(offsetX, offsetX, box.getWidth() - 2 * offsetX, box.getHeight() - 2 * offsetX);
        app.clip();
        app.newPath();
        Color fcolor = (textColor == null) ? GrayColor.GRAYBLACK : textColor;

        // background boxes for selected value[s]
        app.setColorFill(new Color(10, 36, 106));
        for (Integer choiceSelection : choiceSelections) {
            int curChoice = choiceSelection;
            // only draw selections within our display range... not strictly necessary with
            // that clipping rect from above, but it certainly doesn't hurt either
            if (curChoice >= first && curChoice <= last) {
                app.rectangle(offsetX, offsetX + h - (curChoice - first + 1) * leading, box.getWidth() - 2 * offsetX,
                        leading);
                app.fill();
            }
        }
        float xp = offsetX * 2;
        float yp = offsetX + h - ufont.getFontDescriptor(BaseFont.BBOXURY, usize);
        for (int idx = first; idx < last; ++idx, yp -= leading) {
            String ptext = choices[idx];
            int rtl = textRunDirection(ptext);
            ptext = removeCRLF(ptext);
            // highlight selected values against their (presumably) darker background
            Color textCol = (choiceSelections.contains(idx)) ? GrayColor.GRAYWHITE : fcolor;
            Phrase phrase = composePhrase(ptext, ufont, textCol, usize);
            TextAlignmentSettings settings = new TextAlignmentSettings(Element.ALIGN_LEFT, xp, yp,
                    0, rtl, 0);
            ColumnText.showTextAligned(app, phrase, settings);
        }
        app.restoreState();
        app.endVariableText();
        return app;
    }

    /**
     * Gets a new text field.
     *
     * @return a new text field
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public PdfFormField getTextField() throws IOException, DocumentException {
        // Update options based on conditions
        updateOptions();

        // Create the form field
        PdfFormField field = createTextField();

        // Set getAlignment
        setAlignment(field);

        // Set rotation if needed
        if (rotation != 0) {
            field.setMKRotation(rotation);
        }

        // Set field getName, value, and default value
        setFieldAttributes(field);

        // Set border style
        field.setBorderStyle(new PdfBorderDictionary(borderWidth, borderStyle, new PdfDashPattern(3)));

        // Set appearance
        setAppearance(field);

        // Set visibility
        setVisibility(field);

        return field;
    }

    private void updateOptions() {
        if (maxCharacterLength <= 0) {
            options &= ~COMB;
        }
        if ((options & COMB) != 0) {
            options &= ~MULTILINE;
        }
    }

    private PdfFormField createTextField() throws DocumentException {
        return PdfFormField.createTextField(writer, false, false, maxCharacterLength);
    }

    private void setAlignment(PdfFormField field) {
        switch (alignment) {
            case Element.ALIGN_CENTER:
                field.setQuadding(PdfFormField.Q_CENTER);
                break;
            case Element.ALIGN_RIGHT:
                field.setQuadding(PdfFormField.Q_RIGHT);
                break;
            default:
                field.setQuadding(PdfFormField.Q_LEFT);
                break;
        }
    }

    private void setFieldAttributes(PdfFormField field) {
        if (fieldName != null) {
            field.setFieldName(fieldName);
            if (!"".equals(text)) {
                field.setValueAsString(text);
            }
            if (defaultText != null) {
                field.setDefaultValueAsString(defaultText);
            }
            setFieldFlags(field);
        }
    }

    private void setFieldFlags(PdfFormField field) {
        if ((options & READ_ONLY) != 0) {
            field.setFieldFlags(PdfFormField.FF_READ_ONLY);
        }
        if ((options & REQUIRED) != 0) {
            field.setFieldFlags(PdfFormField.FF_REQUIRED);
        }
        if ((options & MULTILINE) != 0) {
            field.setFieldFlags(PdfFormField.FF_MULTILINE);
        }
        if ((options & DO_NOT_SCROLL) != 0) {
            field.setFieldFlags(PdfFormField.FF_DONOTSCROLL);
        }
        if ((options & PASSWORD) != 0) {
            field.setFieldFlags(PdfFormField.FF_PASSWORD);
        }
        if ((options & FILE_SELECTION) != 0) {
            field.setFieldFlags(PdfFormField.FF_FILESELECT);
        }
        if ((options & DO_NOT_SPELL_CHECK) != 0) {
            field.setFieldFlags(PdfFormField.FF_DONOTSPELLCHECK);
        }
        if ((options & COMB) != 0) {
            field.setFieldFlags(PdfFormField.FF_COMB);
        }
    }

    private void setAppearance(PdfFormField field) throws IOException {
        PdfAppearance tp = getAppearance();
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp);
        PdfAppearance da = (PdfAppearance) tp.getDuplicate();
        da.setFontAndSize(getRealFont(), fontSize);
        if (textColor == null) {
            da.setGrayFill(0);
        } else {
            da.setColorFill(textColor);
        }
        field.setDefaultAppearanceString(da);
        if (borderColor != null) {
            field.setMKBorderColor(borderColor);
        }
        if (backgroundColor != null) {
            field.setMKBackgroundColor(backgroundColor);
        }
    }

    private void setVisibility(PdfFormField field) {
        switch (visibility) {
            case HIDDEN:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_HIDDEN);
                break;
            case HIDDEN_BUT_PRINTABLE:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_NOVIEW);
                break;
            default:
                field.setFlags(PdfAnnotation.FLAGS_PRINT);
                break;
        }
    }


    /**
     * Gets a new combo field.
     *
     * @return a new combo field
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public PdfFormField getComboField() throws IOException, DocumentException {
        return getChoiceField(false);
    }

    /**
     * Gets a new list field.
     *
     * @return a new list field
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public PdfFormField getListField() throws IOException, DocumentException {
        return getChoiceField(true);
    }

    private int getTopChoice() {
        if (choiceSelections == null || choiceSelections.isEmpty()) {
            return 0;
        }

        Integer firstValue = choiceSelections.get(0);

        if (firstValue == null) {
            return 0;
        }

        int topChoice = 0;
        if (choices != null) {
            topChoice = firstValue;
            topChoice = Math.min(topChoice, choices.length);
            topChoice = Math.max(0, topChoice);
        } // else topChoice still 0
        return topChoice;
    }

    protected PdfFormField getChoiceField(boolean isList) throws IOException, DocumentException {
        options &= (~MULTILINE) & (~COMB);

        String[] uchoices = getChoices();
        int topChoice = getTopChoice();
        initializeText(uchoices, topChoice);

        PdfFormField field = createField(isList, uchoices, topChoice);
        field.setWidget(box, PdfAnnotation.HIGHLIGHT_INVERT);

        applyRotation(field);
        applyFieldName(field, uchoices, topChoice);
        applyFieldOptions(field);
        setFieldAppearance(field, isList);

        return field;
    }

    private void initializeText(String[] uchoices, int topChoice) {
        if (text == null) {
            text = ""; // fixed by Kazuya Ujihara (ujihara.jp)
        }
        if (topChoice >= 0) {
            text = uchoices[topChoice];
        }
    }

    private PdfFormField createField(boolean isList, String[] uchoices, int topChoice) throws DocumentException {
        if (choiceExports == null) {
            return createSimpleField(isList, uchoices, topChoice);
        } else {
            String[][] mix = createExportMix(uchoices);
            return createExportField(isList, mix, topChoice);
        }
    }

    private PdfFormField createSimpleField(boolean isList, String[] uchoices, int topChoice) throws DocumentException {
        if (isList) {
            return PdfFormField.createList(writer, uchoices, topChoice);
        } else {
            return PdfFormField.createCombo(writer, (options & EDIT) != 0, uchoices, topChoice);
        }
    }

    private String[][] createExportMix(String[] uchoices) {
        String[][] mix = new String[uchoices.length][2];
        for (int k = 0; k < mix.length; ++k) {
            mix[k][0] = mix[k][1] = uchoices[k];
        }
        for (int k = 0; k < Math.min(uchoices.length, choiceExports.length); ++k) {
            if (choiceExports[k] != null) {
                mix[k][0] = choiceExports[k];
            }
        }
        return mix;
    }

    private PdfFormField createExportField(boolean isList, String[][] mix, int topChoice) throws DocumentException {
        if (isList) {
            return PdfFormField.createList(writer, mix, topChoice);
        } else {
            return PdfFormField.createCombo(writer, (options & EDIT) != 0, mix, topChoice);
        }
    }

    private void applyRotation(PdfFormField field) {
        if (rotation != 0) {
            field.setMKRotation(rotation);
        }
    }

    private void applyFieldName(PdfFormField field, String[] uchoices, int topChoice) throws DocumentException {
        if (fieldName != null) {
            field.setFieldName(fieldName);
            if (uchoices.length > 0) {
                applyFieldValue(field, uchoices, topChoice);
            }
        }
    }

    private void applyFieldValue(PdfFormField field, String[] uchoices, int topChoice) throws DocumentException {
        if (choiceExports != null) {
            applyChoiceExportsValue(field, uchoices, topChoice);
        } else {
            applySimpleValue(field);
        }
    }

    private void applyChoiceExportsValue(PdfFormField field, String[] uchoices, int topChoice) throws DocumentException {
        String[][] mix = createExportMix(uchoices);
        if (choiceSelections.size() < 2) {
            field.setValueAsString(mix[topChoice][0]);
            field.setDefaultValueAsString(mix[topChoice][0]);
        } else {
            writeMultipleValues(field, mix);
        }
    }

    private void applySimpleValue(PdfFormField field) throws DocumentException {
        if (choiceSelections.size() < 2) {
            field.setValueAsString(text);
            field.setDefaultValueAsString(text);
        } else {
            writeMultipleValues(field, null);
        }
    }

    private void applyFieldOptions(PdfFormField field) {
        if ((options & READ_ONLY) != 0) {
            field.setFieldFlags(PdfFormField.FF_READ_ONLY);
        }
        if ((options & REQUIRED) != 0) {
            field.setFieldFlags(PdfFormField.FF_REQUIRED);
        }
        if ((options & DO_NOT_SPELL_CHECK) != 0) {
            field.setFieldFlags(PdfFormField.FF_DONOTSPELLCHECK);
        }
        if ((options & MULTISELECT) != 0) {
            field.setFieldFlags(PdfFormField.FF_MULTISELECT);
        }
    }

    private void setFieldAppearance(PdfFormField field, boolean isList) throws IOException, DocumentException {
        PdfAppearance tp = isList ? getListAppearance() : getAppearance();
        if (isList && topFirst > 0) {
            field.put(PdfName.TI, new PdfNumber(topFirst));
        }
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp);
        setDefaultAppearanceString(field, tp);
        setFieldColors(field);
        setFieldVisibility(field);
    }

    private void setDefaultAppearanceString(PdfFormField field, PdfAppearance tp) throws IOException, DocumentException {
        PdfAppearance da = (PdfAppearance) tp.getDuplicate();
        da.setFontAndSize(getRealFont(), fontSize);
        if (textColor == null) {
            da.setGrayFill(0);
        } else {
            da.setColorFill(textColor);
        }
        field.setDefaultAppearanceString(da);
    }

    private void setFieldColors(PdfFormField field) throws AnnotationException {
        if (borderColor != null) {
            field.setMKBorderColor(borderColor);
        }
        if (backgroundColor != null) {
            field.setMKBackgroundColor(backgroundColor);
        }
    }

    private void setFieldVisibility(PdfFormField field) {
        switch (visibility) {
            case HIDDEN:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_HIDDEN);
                break;
            case HIDDEN_BUT_PRINTABLE:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_NOVIEW);
                break;
            default:
                field.setFlags(PdfAnnotation.FLAGS_PRINT);
                break;
        }
    }

    private void writeMultipleValues(PdfFormField field, String[][] mix) {
        PdfArray indexes = new PdfArray();
        PdfArray values = new PdfArray();
        for (Integer choiceSelection : choiceSelections) {
            int idx = choiceSelection;
            indexes.add(new PdfNumber(idx));

            if (mix != null) {
                values.add(new PdfString(mix[idx][0]));
            } else if (choices != null) {
                values.add(new PdfString(choices[idx]));
            }
        }

        field.put(PdfName.V, values);
        field.put(PdfName.I, indexes);

    }

    /**
     * Gets the default text.
     *
     * @return the default text
     */
    public String getDefaultText() {
        return this.defaultText;
    }

    /**
     * Sets the default text. It is only meaningful for text fields.
     *
     * @param defaultText the default text
     */
    public void setDefaultText(String defaultText) {
        this.defaultText = defaultText;
    }

    /**
     * Gets the choices to be presented to the user in list/combo fields.
     *
     * @return the choices to be presented to the user
     */
    public String[] getChoices() {
        return this.choices;
    }

    /**
     * Sets the choices to be presented to the user in list/combo fields.
     *
     * @param choices the choices to be presented to the user
     */
    public void setChoices(String[] choices) {
        this.choices = choices;
    }

    /**
     * Gets the export values in list/combo fields.
     *
     * @return the export values in list/combo fields
     */
    public String[] getChoiceExports() {
        return this.choiceExports;
    }

    /**
     * Sets the export values in list/combo fields. If this array is <CODE>null</CODE> then the choice values will also
     * be used as the export values.
     *
     * @param choiceExports the export values in list/combo fields
     */
    public void setChoiceExports(String[] choiceExports) {
        this.choiceExports = choiceExports;
    }

    /**
     * Gets the zero based index of the selected item.
     *
     * @return the zero based index of the selected item
     */
    public int getChoiceSelection() {
        return getTopChoice();
    }

    /**
     * Sets the zero based index of the selected item.
     *
     * @param choiceSelection the zero based index of the selected item
     */
    public void setChoiceSelection(int choiceSelection) {
        choiceSelections = new ArrayList<>();
        choiceSelections.add(choiceSelection);
    }

    public ArrayList<Integer> gteChoiceSelections() {
        return choiceSelections;
    }

    /**
     * adds another (or a first I suppose) selection to a MULTISELECT list. This doesn't do anything unless
     * {@code this.options & MUTLISELECT != 0}
     *
     * @param selection new selection
     */
    public void addChoiceSelection(int selection) {
        if ((this.options & BaseField.MULTISELECT) != 0) {
            choiceSelections.add(selection);
        }
    }

    /**
     * replaces the existing selections with the param. If this field isn't a MULTISELECT list, all but the first
     * element will be removed.
     *
     * @param selections new selections.  If null, it clear()s the underlying ArrayList.
     */
    public void setChoiceSelections(List<Integer> selections) {
        if (selections != null) {
            choiceSelections = new ArrayList<>(selections);
            if (choiceSelections.size() > 1 && (options & BaseField.MULTISELECT) == 0) {
                // can't have multiple selections in a single-select field
                while (choiceSelections.size() > 1) {
                    choiceSelections.remove(1);
                }
            }

        } else {
            choiceSelections.clear();
        }
    }

    int getTopFirst() {
        return topFirst;
    }

    /**
     * Sets extra margins in text fields to better mimic the Acrobat layout.
     *
     * @param extraMarginLeft the extra margin left
     * @param extraMarginTop  the extra margin top
     */
    public void setExtraMargin(float extraMarginLeft, float extraMarginTop) {
        this.extraMarginLeft = extraMarginLeft;
        this.extraMarginTop = extraMarginTop;
    }

    /**
     * Gets the list of substitution fonts. The list is composed of <CODE>BaseFont</CODE> and can be <CODE>null</CODE>.
     * The fonts in this list will be used if the original font doesn't contain the needed glyphs.
     *
     * @return the list
     */
    public List<BaseFont> getSubstitutionFontList() {
        return this.substitutionFonts;
    }

    /**
     * Sets a list of substitution fonts. The list is composed of <CODE>BaseFont</CODE> and can also be
     * <CODE>null</CODE>. The fonts in this list will be used if the original font doesn't contain the needed glyphs.
     *
     * @param substitutionFonts the list
     */
    public void setSubstitutionFontList(List<BaseFont> substitutionFonts) {
        this.substitutionFonts = substitutionFonts;
    }

    /**
     * Gets the extensionFont. This font will be searched before the substitution fonts. It may be <code>null</code>.
     *
     * @return the extensionFont
     */
    public BaseFont getExtensionFont() {
        return this.extensionFont;
    }

    /**
     * Sets the extensionFont. This font will be searched before the substitution fonts. It may be <code>null</code>.
     *
     * @param extensionFont New value of property extensionFont.
     */
    public void setExtensionFont(BaseFont extensionFont) {
        this.extensionFont = extensionFont;
    }
}
