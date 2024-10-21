/*
 * $Id: Barcode128.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2002-2006 by Paulo Soares.
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

import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Rectangle;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.IllegalBarcode128CharacterException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.MemoryImageSource;

/**
 * Implements the code 128 and UCC/EAN-128. Other symbologies are allowed in raw mode.<p> The code types allowed
 * are:<br>
 * <ul>
 * <li><b>CODE128</b> - plain barcode 128.
 * <li><b>CODE128_UCC</b> - support for UCC/EAN-128 with a full list of AI.
 * <li><b>CODE128_RAW</b> - raw mode. The code attribute has the actual codes from 0
 *     to 105 followed by '&#92;uffff' and the human readable text.
 * </ul>
 * The default parameters are:
 * <pre>
 * x = 0.8f;
 * font = BaseFont.createFont("Helvetica", "winansi", false);
 * size = 8;
 * baseline = size;
 * barHeight = size * 3;
 * textAlignment = Element.ALIGN_CENTER;
 * codeType = CODE128;
 * </pre>
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class Barcode128 extends Barcode {

    /**
     * The charset code change.
     */
    public static final char CODE_AB_TO_C = 99;
    /**
     * The charset code change.
     */
    public static final char CODE_AC_TO_B = 100;
    /**
     * The charset code change.
     */
    public static final char CODE_BC_TO_A = 101;
    /**
     * The code for UCC/EAN-128.
     */
    public static final char FNC1_INDEX = 102;
    /**
     * The start code.
     */
    public static final char START_A = 103;
    /**
     * The start code.
     */
    public static final char START_B = 104;
    /**
     * The start code.
     */
    public static final char START_C = 105;
    public static final char FNC1 = '\u00ca';
    public static final char DEL = '\u00c3';
    public static final char FNC3 = '\u00c4';
    public static final char FNC2 = '\u00c5';
    public static final char SHIFT = '\u00c6';
    public static final char CODE_C = '\u00c7';
    public static final char CODE_A = '\u00c8';
    public static final char FNC4 = '\u00c8';
    public static final char STARTA = '\u00cb';
    public static final char STARTB = '\u00cc';
    public static final char STARTC = '\u00cd';
    /**
     * The bars to generate the code.
     */
    private static final byte[][] BARS =
            {
                    {2, 1, 2, 2, 2, 2},
                    {2, 2, 2, 1, 2, 2},
                    {2, 2, 2, 2, 2, 1},
                    {1, 2, 1, 2, 2, 3},
                    {1, 2, 1, 3, 2, 2},
                    {1, 3, 1, 2, 2, 2},
                    {1, 2, 2, 2, 1, 3},
                    {1, 2, 2, 3, 1, 2},
                    {1, 3, 2, 2, 1, 2},
                    {2, 2, 1, 2, 1, 3},
                    {2, 2, 1, 3, 1, 2},
                    {2, 3, 1, 2, 1, 2},
                    {1, 1, 2, 2, 3, 2},
                    {1, 2, 2, 1, 3, 2},
                    {1, 2, 2, 2, 3, 1},
                    {1, 1, 3, 2, 2, 2},
                    {1, 2, 3, 1, 2, 2},
                    {1, 2, 3, 2, 2, 1},
                    {2, 2, 3, 2, 1, 1},
                    {2, 2, 1, 1, 3, 2},
                    {2, 2, 1, 2, 3, 1},
                    {2, 1, 3, 2, 1, 2},
                    {2, 2, 3, 1, 1, 2},
                    {3, 1, 2, 1, 3, 1},
                    {3, 1, 1, 2, 2, 2},
                    {3, 2, 1, 1, 2, 2},
                    {3, 2, 1, 2, 2, 1},
                    {3, 1, 2, 2, 1, 2},
                    {3, 2, 2, 1, 1, 2},
                    {3, 2, 2, 2, 1, 1},
                    {2, 1, 2, 1, 2, 3},
                    {2, 1, 2, 3, 2, 1},
                    {2, 3, 2, 1, 2, 1},
                    {1, 1, 1, 3, 2, 3},
                    {1, 3, 1, 1, 2, 3},
                    {1, 3, 1, 3, 2, 1},
                    {1, 1, 2, 3, 1, 3},
                    {1, 3, 2, 1, 1, 3},
                    {1, 3, 2, 3, 1, 1},
                    {2, 1, 1, 3, 1, 3},
                    {2, 3, 1, 1, 1, 3},
                    {2, 3, 1, 3, 1, 1},
                    {1, 1, 2, 1, 3, 3},
                    {1, 1, 2, 3, 3, 1},
                    {1, 3, 2, 1, 3, 1},
                    {1, 1, 3, 1, 2, 3},
                    {1, 1, 3, 3, 2, 1},
                    {1, 3, 3, 1, 2, 1},
                    {3, 1, 3, 1, 2, 1},
                    {2, 1, 1, 3, 3, 1},
                    {2, 3, 1, 1, 3, 1},
                    {2, 1, 3, 1, 1, 3},
                    {2, 1, 3, 3, 1, 1},
                    {2, 1, 3, 1, 3, 1},
                    {3, 1, 1, 1, 2, 3},
                    {3, 1, 1, 3, 2, 1},
                    {3, 3, 1, 1, 2, 1},
                    {3, 1, 2, 1, 1, 3},
                    {3, 1, 2, 3, 1, 1},
                    {3, 3, 2, 1, 1, 1},
                    {3, 1, 4, 1, 1, 1},
                    {2, 2, 1, 4, 1, 1},
                    {4, 3, 1, 1, 1, 1},
                    {1, 1, 1, 2, 2, 4},
                    {1, 1, 1, 4, 2, 2},
                    {1, 2, 1, 1, 2, 4},
                    {1, 2, 1, 4, 2, 1},
                    {1, 4, 1, 1, 2, 2},
                    {1, 4, 1, 2, 2, 1},
                    {1, 1, 2, 2, 1, 4},
                    {1, 1, 2, 4, 1, 2},
                    {1, 2, 2, 1, 1, 4},
                    {1, 2, 2, 4, 1, 1},
                    {1, 4, 2, 1, 1, 2},
                    {1, 4, 2, 2, 1, 1},
                    {2, 4, 1, 2, 1, 1},
                    {2, 2, 1, 1, 1, 4},
                    {4, 1, 3, 1, 1, 1},
                    {2, 4, 1, 1, 1, 2},
                    {1, 3, 4, 1, 1, 1},
                    {1, 1, 1, 2, 4, 2},
                    {1, 2, 1, 1, 4, 2},
                    {1, 2, 1, 2, 4, 1},
                    {1, 1, 4, 2, 1, 2},
                    {1, 2, 4, 1, 1, 2},
                    {1, 2, 4, 2, 1, 1},
                    {4, 1, 1, 2, 1, 2},
                    {4, 2, 1, 1, 1, 2},
                    {4, 2, 1, 2, 1, 1},
                    {2, 1, 2, 1, 4, 1},
                    {2, 1, 4, 1, 2, 1},
                    {4, 1, 2, 1, 2, 1},
                    {1, 1, 1, 1, 4, 3},
                    {1, 1, 1, 3, 4, 1},
                    {1, 3, 1, 1, 4, 1},
                    {1, 1, 4, 1, 1, 3},
                    {1, 1, 4, 3, 1, 1},
                    {4, 1, 1, 1, 1, 3},
                    {4, 1, 1, 3, 1, 1},
                    {1, 1, 3, 1, 4, 1},
                    {1, 1, 4, 1, 3, 1},
                    {3, 1, 1, 1, 4, 1},
                    {4, 1, 1, 1, 3, 1},
                    {2, 1, 1, 4, 1, 2},
                    {2, 1, 1, 2, 1, 4},
                    {2, 1, 1, 2, 3, 2}
            };
    /**
     * The stop bars.
     */
    private static final byte[] BARS_STOP = {2, 3, 3, 1, 1, 1, 2};
    private static final IntHashtable ais = new IntHashtable();

    static {
        ais.put(0, 20);
        ais.put(1, 16);
        ais.put(2, 16);
        ais.put(10, -1);
        ais.put(11, 9);
        ais.put(12, 8);
        ais.put(13, 8);
        ais.put(15, 8);
        ais.put(17, 8);
        ais.put(20, 4);
        ais.put(21, -1);
        ais.put(22, -1);
        ais.put(23, -1);
        ais.put(240, -1);
        ais.put(241, -1);
        ais.put(250, -1);
        ais.put(251, -1);
        ais.put(252, -1);
        ais.put(30, -1);
        for (int k = 3100; k < 3700; ++k) {
            ais.put(k, 10);
        }
        ais.put(37, -1);
        for (int k = 3900; k < 3940; ++k) {
            ais.put(k, -1);
        }
        ais.put(400, -1);
        ais.put(401, -1);
        ais.put(402, 20);
        ais.put(403, -1);
        for (int k = 410; k < 416; ++k) {
            ais.put(k, 16);
        }
        ais.put(420, -1);
        ais.put(421, -1);
        ais.put(422, 6);
        ais.put(423, -1);
        ais.put(424, 6);
        ais.put(425, 6);
        ais.put(426, 6);
        ais.put(7001, 17);
        ais.put(7002, -1);
        for (int k = 7030; k < 7040; ++k) {
            ais.put(k, -1);
        }
        ais.put(8001, 18);
        ais.put(8002, -1);
        ais.put(8003, -1);
        ais.put(8004, -1);
        ais.put(8005, 10);
        ais.put(8006, 22);
        ais.put(8007, -1);
        ais.put(8008, -1);
        ais.put(8018, 22);
        ais.put(8020, -1);
        ais.put(8100, 10);
        ais.put(8101, 14);
        ais.put(8102, 6);
        for (int k = 90; k < 100; ++k) {
            ais.put(k, -1);
        }
    }

    /**
     * Creates new Barcode128
     */
    public Barcode128() {
        try {
            x = 0.8f;
            font = BaseFont.createFont("Helvetica", "winansi", false);
            size = 8;
            baseline = size;
            barHeight = size * 3;
            textAlignment = Element.ALIGN_CENTER;
            codeType = CODE128;
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Removes the FNC1 codes in the text.
     *
     * @param code the text to clean
     * @return the cleaned text
     */
    public static String removeFNC1(String code) {
        int len = code.length();
        StringBuilder buf = new StringBuilder(len);
        for (int k = 0; k < len; ++k) {
            char c = code.charAt(k);
            if (c >= 32 && c <= 126) {
                buf.append(c);
            }
        }
        return buf.toString();
    }

    /**
     * Gets the human readable text of a sequence of AI.
     *
     * @param code the text
     * @return the human readable text
     */
    public static String getHumanReadableUCCEAN(String code) {
        StringBuilder buf = new StringBuilder();
        String fnc1 = String.valueOf(FNC1);

        while (!code.isEmpty()) {
            if (code.startsWith(fnc1)) {
                code = code.substring(1);
                continue;
            }

            int idlen = findIdentifierLength(code);
            if (idlen == 0) {
                break;
            }

            int n = ais.get(Integer.parseInt(code.substring(0, idlen)));
            buf.append('(').append(code.substring(0, idlen)).append(')');
            code = code.substring(idlen);

            if (n > 0) {
                buf.append(removeFNC1(code.substring(0, n)));
                code = code.substring(n);
            } else {
                int idx = code.indexOf(fnc1);
                buf.append(code.substring(0, idx));
                code = code.substring(idx + 1);
            }
        }

        buf.append(removeFNC1(code));
        return buf.toString();
    }

    private static int findIdentifierLength(String code) {
        for (int k = 2; k <= 5; k++) {
            if (code.length() < k) {
                return 0;
            }
            int n = ais.get(Integer.parseInt(code.substring(0, k)));
            if (n != 0) {
                return k;
            }
        }
        return 0;
    }

    /**
     * Returns <CODE>true</CODE> if the next <CODE>numDigits</CODE> starting from index <CODE>textIndex</CODE> are
     * numeric skipping any FNC1.
     *
     * @param text      the text to check
     * @param textIndex where to check from
     * @param numDigits the number of digits to check
     * @return the check result
     */
    static boolean isNextDigits(String text, int textIndex, int numDigits) {
        int len = text.length();
        while (textIndex < len && numDigits > 0) {
            if (text.charAt(textIndex) == FNC1) {
                ++textIndex;
                continue;
            }
            int n = Math.min(2, numDigits);
            if (textIndex + n > len) {
                return false;
            }
            while (n-- > 0) {
                char c = text.charAt(textIndex++);
                if (c < '0' || c > '9') {
                    return false;
                }
                --numDigits;
            }
        }
        return numDigits == 0;
    }

    /**
     * Packs the digits for charset C also considering FNC1. It assumes that all the parameters are valid.
     *
     * @param text      the text to pack
     * @param textIndex where to pack from
     * @param numDigits the number of digits to pack. It is always an even number
     * @return the packed digits, two digits per character
     */
    static String getPackedRawDigits(String text, int textIndex, int numDigits) {
        StringBuilder out = new StringBuilder();
        int start = textIndex;
        while (numDigits > 0) {
            if (text.charAt(textIndex) == FNC1) {
                out.append(FNC1_INDEX);
                ++textIndex;
                continue;
            }
            numDigits -= 2;
            int c1 = text.charAt(textIndex++) - '0';
            int c2 = text.charAt(textIndex++) - '0';
            out.append((char) (c1 * 10 + c2));
        }
        return (char) (textIndex - start) + out.toString();
    }

    /**
     * Converts the human readable text to the characters needed to create a barcode. Some optimization is done to get
     * the shortest code.
     *
     * @param text the text to convert
     * @param ucc  <CODE>true</CODE> if it is an UCC/EAN-128. In this case
     *             the character FNC1 is added
     * @return the code ready to be fed to getBarsCode128Raw()
     */
    public static String getRawText(String text, boolean ucc) {
        StringBuilder out = new StringBuilder();
        int tLen = text.length();
        if (tLen == 0) {
            appendStartCode(out, ucc, START_B);
            return out.toString();
        }

        validateText(text);

        int index = 0;
        char currentCode = determineInitialCode(text, ucc, out);
        index = processInitialCharacter(text, out, currentCode, index);

        while (index < tLen) {
            currentCode = processCharacters(text, out, currentCode, index);
            index = updateIndex(currentCode, text, index);
        }

        return out.toString();
    }

    private static void appendStartCode(StringBuilder out, boolean ucc, char startCode) {
        out.append(startCode);
        if (ucc) {
            out.append(FNC1_INDEX);
        }
    }

    private static void validateText(String text) {
        for (int k = 0; k < text.length(); ++k) {
            int c = text.charAt(k);
            if (c > 127 && c != FNC1) {
                throw new IllegalBarcode128CharacterException(
                        MessageLocalization.getComposedMessage("there.are.illegal.characters.for.barcode.128.in.1", text));
            }
        }
    }

    private static char determineInitialCode(String text, boolean ucc, StringBuilder out) {
        int c = text.charAt(0);
        if (isNextDigits(text, 0, 2)) {
            appendStartCode(out, ucc, START_C);
            return START_C;
        } else if (c < ' ') {
            appendStartCode(out, ucc, START_A);
            return START_A;
        } else {
            appendStartCode(out, ucc, START_B);
            return START_B;
        }
    }

    private static int processInitialCharacter(String text, StringBuilder out, char currentCode, int index) {
        int c = text.charAt(0);
        if (currentCode == START_C) {
            String out2 = getPackedRawDigits(text, index, 2);
            index += out2.charAt(0);
            out.append(out2.substring(1));
        } else if (currentCode == START_A) {
            out.append((char) (c + 64));
            index++;
        } else {
            if (c == FNC1) {
                out.append(FNC1_INDEX);
            } else {
                out.append((char) (c - ' '));
            }
            index++;
        }
        return index;
    }

    private static char processCharacters(String text, StringBuilder out, char currentCode, int index) {
        switch (currentCode) {
            case START_A:
                currentCode = processStartA(text, out, index);
                break;
            case START_B:
                currentCode = processStartB(text, out, index);
                break;
            case START_C:
                currentCode = processStartC(text, out, index);
                break;
            default:
                break;
        }
        return currentCode;
    }

    private static int updateIndex(char currentCode, String text, int index) {
        if (currentCode == START_C && isNextDigits(text, index, 2)) {
            return index + 2;
        }
        return index + 1;
    }

    private static char processStartA(String text, StringBuilder out, int index) {
        if (isNextDigits(text, index, 4)) {
            out.append(CODE_AB_TO_C);
            String out2 = getPackedRawDigits(text, index, 4);
            out.append(out2.substring(1));
            return START_C;
        } else {
            return processStartABCommon(text, out, index, START_B, CODE_AC_TO_B);
        }
    }

    private static char processStartB(String text, StringBuilder out, int index) {
        if (isNextDigits(text, index, 4)) {
            out.append(CODE_AB_TO_C);
            String out2 = getPackedRawDigits(text, index, 4);
            out.append(out2.substring(1));
            return START_C;
        } else {
            return processStartABCommon(text, out, index, START_A, CODE_BC_TO_A);
        }
    }

    private static char processStartC(String text, StringBuilder out, int index) {
        if (isNextDigits(text, index, 2)) {
            String out2 = getPackedRawDigits(text, index, 2);
            out.append(out2.substring(1));
            return START_C;
        } else {
            return processStartCCommon(text, out, index);
        }
    }

    private static char processStartABCommon(String text, StringBuilder out, int index, char switchCode, char switchCodeAppend) {
        int c = text.charAt(index++);
        if (c == FNC1) {
            out.append(FNC1_INDEX);
        } else if (c < ' ') {
            out.append(switchCodeAppend);
            out.append((char) (c + 64));
            return switchCode;
        } else {
            out.append((char) (c - ' '));
        }
        return switchCode == START_B ? START_A : START_B;
    }

    private static char processStartCCommon(String text, StringBuilder out, int index) {
        // Read the character at the specified index and increment the index
        char c = text.charAt(index);
        index++; // Increment the index after accessing the character

        if (c == FNC1) {
            out.append(FNC1_INDEX);
        } else if (c < ' ') {
            out.append(CODE_BC_TO_A);
            out.append((char) (c + 64));
            return START_A;
        } else {
            out.append(CODE_AC_TO_B);
            out.append((char) (c - ' '));
            return START_B;
        }

        return 0; // Return 0 in case of FNC1
    }



    /**
     * Generates the bars. The input has the actual barcodes, not the human readable text.
     *
     * @param text the barcode
     * @return the bars
     */
    public static byte[] getBarsCode128Raw(String text) {
        int idx = text.indexOf('\uffff');
        if (idx >= 0) {
            text = text.substring(0, idx);
        }
        int chk = text.charAt(0);
        for (int k = 1; k < text.length(); ++k) {
            chk += k * text.charAt(k);
        }
        chk = chk % 103;
        text += (char) chk;
        byte[] bars = new byte[(text.length() + 1) * 6 + 7];
        int k;
        for (k = 0; k < text.length(); ++k) {
            System.arraycopy(BARS[text.charAt(k)], 0, bars, k * 6, 6);
        }
        System.arraycopy(BARS_STOP, 0, bars, k * 6, 7);
        return bars;
    }

    /**
     * Gets the maximum area that the barcode and the text, if any, will occupy. The lower left corner is always (0,
     * 0).
     *
     * @return the size the barcode occupies.
     */
    public Rectangle getBarcodeSize() {
        String fullCode = determineFullCode();
        float fontX = calculateFontX(fullCode);
        float fontY = calculateFontY();
        float fullWidth = calculateFullWidth(fullCode, fontX);
        float fullHeight = calculateFullHeight(fontY);
        return new Rectangle(fullWidth, fullHeight);
    }

    private float calculateFontX(String fullCode) {
        if (font == null) {
            return 0;
        }
        return font.getWidthPoint(altText != null ? altText : fullCode, size);
    }

    private float calculateFontY() {
        if (font == null) {
            return 0;
        }
        return baseline > 0 ? baseline - font.getFontDescriptor(BaseFont.DESCENT, size) : -baseline + size;
    }

    private String determineFullCode() {
        if (codeType == CODE128_RAW) {
            return extractRawCode(code);
        } else if (codeType == CODE128_UCC) {
            return getHumanReadableUCCEAN(code);
        } else {
            return removeFNC1(code);
        }
    }

    private String extractRawCode(String code) {
        int idx = code.indexOf('\uffff');
        return idx >= 0 ? code.substring(0, idx) : code;
    }

    private float calculateFullWidth(String fullCode, float fontX) {
        int len = fullCode.length();
        return Math.max((len + 2) * 11 * x + 2 * x, fontX);
    }

    private float calculateFullHeight(float fontY) {
        return barHeight + fontY;
    }

    /**
     * Places the barcode in a <CODE>PdfContentByte</CODE>. The barcode is always placed at coordinates (0, 0). Use the
     * translation matrix to move it elsewhere.
     * <p> The bars and text are written in the following colors:</p>
     * <TABLE BORDER=1>
     * <CAPTION>table of the colors of the bars and text</CAPTION>
     * <TR>
     * <TH><P><CODE>barColor</CODE></TH>
     * <TH><P><CODE>textColor</CODE></TH>
     * <TH><P>Result</TH>
     * </TR>
     * <TR>
     * <TD><P><CODE>null</CODE></TD>
     * <TD><P><CODE>null</CODE></TD>
     * <TD><P>bars and text painted with current fill color</TD>
     * </TR>
     * <TR>
     * <TD><P><CODE>barColor</CODE></TD>
     * <TD><P><CODE>null</CODE></TD>
     * <TD><P>bars and text painted with <CODE>barColor</CODE></TD>
     * </TR>
     * <TR>
     * <TD><P><CODE>null</CODE></TD>
     * <TD><P><CODE>textColor</CODE></TD>
     * <TD><P>bars painted with current color<br>text painted with <CODE>textColor</CODE></TD>
     * </TR>
     * <TR>
     * <TD><P><CODE>barColor</CODE></TD>
     * <TD><P><CODE>textColor</CODE></TD>
     * <TD><P>bars painted with <CODE>barColor</CODE><br>text painted with <CODE>textColor</CODE></TD>
     * </TR>
     * </TABLE>
     *
     * @param cb        the <CODE>PdfContentByte</CODE> where the barcode will be placed
     * @param barColor  the color of the bars. It can be <CODE>null</CODE>
     * @param textColor the color of the text. It can be <CODE>null</CODE>
     * @return the dimensions the barcode occupies
     */
    public Rectangle placeBarcode(PdfContentByte cb, Color barColor, Color textColor) {
        String fullCode = calculateFullCode();
        float fontX = calculateFontX(fullCode);
        String bCode = calculateBarcodeCode();
        float fullWidth = calculateFullWidth(bCode.length());
        float[] positions = calculateBarAndTextPositions(fontX, fullWidth);
        float barStartX = positions[0];
        float textStartX = positions[1];
        float[] startY = calculateStartY();
        float barStartY = startY[0];
        float textStartY = startY[1];

        drawBars(cb, barColor, bCode, barStartX, barStartY);
        drawText(cb, textColor, fullCode, textStartX, textStartY);

        return getBarcodeSize();
    }

    private String calculateFullCode() {
        switch (codeType) {
            case CODE128_RAW:
                return getRawFullCode();
            case CODE128_UCC:
                return getHumanReadableUCCEAN(code);
            default:
                return removeFNC1(code);
        }
    }

    private String getRawFullCode() {
        int idx = code.indexOf('\uffff');
        if (idx < 0) {
            return "";
        } else {
            return code.substring(idx + 1);
        }
    }

    private String calculateBarcodeCode() {
        if (codeType == CODE128_RAW) {
            int idx = code.indexOf('\uffff');
            return (idx >= 0) ? code.substring(0, idx) : code;
        } else {
            return getRawText(code, codeType == CODE128_UCC);
        }
    }

    private float calculateFullWidth(int len) {
        return (len + 2) * 11 * x + 2 * x;
    }

    private float[] calculateBarAndTextPositions(float fontX, float fullWidth) {
        float barStartX = 0;
        float textStartX = 0;
        switch (textAlignment) {
            case Element.ALIGN_RIGHT:
                if (fontX > fullWidth) {
                    barStartX = fontX - fullWidth;
                } else {
                    textStartX = fullWidth - fontX;
                }
                break;
            case Element.ALIGN_CENTER:
            default:
                if (fontX > fullWidth) {
                    barStartX = (fontX - fullWidth) / 2;
                } else {
                    textStartX = (fullWidth - fontX) / 2;
                }
                break;
        }
        return new float[]{barStartX, textStartX};
    }

    private float[] calculateStartY() {
        float barStartY = 0;
        float textStartY = 0;
        if (font != null) {
            if (baseline <= 0) {
                textStartY = barHeight - baseline;
            } else {
                textStartY = -font.getFontDescriptor(BaseFont.DESCENT, size);
                barStartY = textStartY + baseline;
            }
        }
        return new float[]{barStartY, textStartY};
    }

    private void drawBars(PdfContentByte cb, Color barColor, String bCode, float barStartX, float barStartY) {
        if (barColor != null) {
            cb.setColorFill(barColor);
        }
        byte[] bars = getBarsCode128Raw(bCode);
        boolean print = true;
        for (byte bar : bars) {
            float w = bar * x;
            if (print) {
                cb.rectangle(barStartX, barStartY, w - inkSpreading, barHeight);
            }
            print = !print;
            barStartX += w;
        }
        cb.fill();
    }

    private void drawText(PdfContentByte cb, Color textColor, String fullCode, float textStartX, float textStartY) {
        if (font != null) {
            if (textColor != null) {
                cb.setColorFill(textColor);
            }
            cb.beginText();
            cb.setFontAndSize(font, size);
            cb.setTextMatrix(textStartX, textStartY);
            cb.showText(fullCode);
            cb.endText();
        }
    }


    /**
     * Creates a <CODE>java.awt.Image</CODE>. This image only contains the bars without any text.
     *
     * @param foreground the color of the bars
     * @param background the color of the background
     * @return the image
     */
    public java.awt.Image createAwtImage(Color foreground, Color background) {
        int f = foreground.getRGB();
        int g = background.getRGB();
        Canvas canvas = new Canvas();
        String bCode;
        if (codeType == CODE128_RAW) {
            int idx = code.indexOf('\uffff');
            if (idx >= 0) {
                bCode = code.substring(0, idx);
            } else {
                bCode = code;
            }
        } else {
            bCode = getRawText(code, codeType == CODE128_UCC);
        }
        int len = bCode.length();
        int fullWidth = (len + 2) * 11 + 2;
        byte[] bars = getBarsCode128Raw(bCode);

        boolean print = true;
        int ptr = 0;
        int height = (int) barHeight;
        int[] pix = new int[fullWidth * height];
        for (int w : bars) {
            int c = g;
            if (print) {
                c = f;
            }
            print = !print;
            for (int j = 0; j < w; ++j) {
                pix[ptr++] = c;
            }
        }
        for (int k = fullWidth; k < pix.length; k += fullWidth) {
            System.arraycopy(pix, 0, pix, k, fullWidth);
        }

        return canvas.createImage(new MemoryImageSource(fullWidth, height, pix, 0, fullWidth));
    }

    /**
     * Sets the code to generate. If it's an UCC code and starts with '(' it will be split by the AI. This code in UCC
     * mode is valid:
     * <p>
     * <code>(01)00000090311314(10)ABC123(15)060916</code>
     *
     * @param code the code to generate
     */
    @Override
    public void setCode(String code) {
        if (isUccCodeWithParentheses(code)) {
            super.setCode(processUccCode(code));
        } else {
            super.setCode(code);
        }
    }

    private boolean isUccCodeWithParentheses(String code) {
        return getCodeType() == Barcode.CODE128_UCC && code.startsWith("(");
    }

    private String processUccCode(String code) {
        StringBuilder ret = new StringBuilder();
        int idx = 0;

        while (idx >= 0) {
            int end = getNextClosingParenthesisIndex(code, idx);
            String sai = extractAndValidateSai(code, idx, end);

            int ai = Integer.parseInt(sai);
            int len = getAiLength(ai);

            sai = formatAi(sai);
            idx = code.indexOf('(', end);
            int next = (idx < 0 ? code.length() : idx);

            ret.append(sai).append(code.substring(end + 1, next));
            validateAiLength(len, sai, next, end);

            if (len < 0 && idx >= 0) {
                ret.append(FNC1);
            }
        }

        return ret.toString();
    }

    private int getNextClosingParenthesisIndex(String code, int idx) {
        int end = code.indexOf(')', idx);
        if (end < 0) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("badly.formed.ucc.string.1", code)
            );
        }
        return end;
    }

    private String extractAndValidateSai(String code, int idx, int end) {
        String sai = code.substring(idx + 1, end);
        if (sai.length() < 2) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("ai.too.short.1", sai)
            );
        }
        return sai;
    }

    private int getAiLength(int ai) {
        int len = ais.get(ai);
        if (len == 0) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("ai.not.found.1", String.valueOf(ai))
            );
        }
        return len;
    }

    private String formatAi(String sai) {
        return sai.length() == 1 ? "0" + sai : sai;
    }

    private void validateAiLength(int len, String sai, int next, int end) {
        if (len >= 0 && next - end - 1 + sai.length() != len) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("invalid.ai.length.1", sai)
            );
        }
    }

}
