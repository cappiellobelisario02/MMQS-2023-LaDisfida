/*
 * Copyright 2002 by Paulo Soares.
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

import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Rectangle;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.InvalidCodeTypeException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Image;
import java.awt.image.MemoryImageSource;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;


public class BarcodeEAN extends Barcode {

    static Logger logger = Logger.getLogger(BarcodeEAN.class.getName());

    private static final String MSG_EMPTY_ARR = "Empty array!!";

    /**
     * The bar positions that are guard bars.
     */
    private static final int[] GUARD_EMPTY = {};
    /**
     * The bar positions that are guard bars.
     */
    private static final int[] GUARD_UPCA = {0, 2, 4, 6, 28, 30, 52, 54, 56, 58};
    /**
     * The bar positions that are guard bars.
     */
    private static final int[] GUARD_EAN13 = {0, 2, 28, 30, 56, 58};
    /**
     * The bar positions that are guard bars.
     */
    private static final int[] GUARD_EAN8 = {0, 2, 20, 22, 40, 42};
    /**
     * The bar positions that are guard bars.
     */
    private static final int[] GUARD_UPCE = {0, 2, 28, 30, 32};
    /**
     * The x coordinates to place the text.
     */
    private static final float[] TEXTPOS_EAN13 = {6.5f, 13.5f, 20.5f, 27.5f, 34.5f, 41.5f, 53.5f, 60.5f, 67.5f, 74.5f,
            81.5f, 88.5f};
    /**
     * The x coordinates to place the text.
     */
    private static final float[] TEXTPOS_EAN8 = {6.5f, 13.5f, 20.5f, 27.5f, 39.5f, 46.5f, 53.5f, 60.5f};
    /**
     * The basic bar widths.
     */
    private static final byte[][] BARS =
            {
                    {3, 2, 1, 1}, // 0
                    {2, 2, 2, 1}, // 1
                    {2, 1, 2, 2}, // 2
                    {1, 4, 1, 1}, // 3
                    {1, 1, 3, 2}, // 4
                    {1, 2, 3, 1}, // 5
                    {1, 1, 1, 4}, // 6
                    {1, 3, 1, 2}, // 7
                    {1, 2, 1, 3}, // 8
                    {3, 1, 1, 2}  // 9
            };

    /**
     * The total number of bars for EAN13.
     */
    private static final int TOTALBARS_EAN13 = 11 + 12 * 4;
    /**
     * The total number of bars for EAN8.
     */
    private static final int TOTALBARS_EAN8 = 11 + 8 * 4;
    /**
     * The total number of bars for UPCE.
     */
    private static final int TOTALBARS_UPCE = 9 + 6 * 4;
    /**
     * The total number of bars for supplemental 2.
     */
    private static final int TOTALBARS_SUPP2 = 13;
    /**
     * The total number of bars for supplemental 5.
     */
    private static final int TOTALBARS_SUPP5 = 31;
    /**
     * Marker for odd parity.
     */
    private static final int ODD = 0;
    /**
     * Marker for even parity.
     */
    private static final int EVEN = 1;

    /**
     * Sequence of parities to be used with EAN13.
     */
    private static final byte[][] PARITY13 =
            {
                    {ODD, ODD, ODD, ODD, ODD, ODD},  // 0
                    {ODD, ODD, EVEN, ODD, EVEN, EVEN}, // 1
                    {ODD, ODD, EVEN, EVEN, ODD, EVEN}, // 2
                    {ODD, ODD, EVEN, EVEN, EVEN, ODD},  // 3
                    {ODD, EVEN, ODD, ODD, EVEN, EVEN}, // 4
                    {ODD, EVEN, EVEN, ODD, ODD, EVEN}, // 5
                    {ODD, EVEN, EVEN, EVEN, ODD, ODD},  // 6
                    {ODD, EVEN, ODD, EVEN, ODD, EVEN}, // 7
                    {ODD, EVEN, ODD, EVEN, EVEN, ODD},  // 8
                    {ODD, EVEN, EVEN, ODD, EVEN, ODD}   // 9
            };

    /**
     * Sequence of parities to be used with supplemental 2.
     */
    private static final byte[][] PARITY2 =
            {
                    {ODD, ODD},   // 0
                    {ODD, EVEN},  // 1
                    {EVEN, ODD},   // 2
                    {EVEN, EVEN}   // 3
            };

    /**
     * Sequence of parities to be used with supplemental 2.
     */
    private static final byte[][] PARITY5 =
            {
                    {EVEN, EVEN, ODD, ODD, ODD},  // 0
                    {EVEN, ODD, EVEN, ODD, ODD},  // 1
                    {EVEN, ODD, ODD, EVEN, ODD},  // 2
                    {EVEN, ODD, ODD, ODD, EVEN}, // 3
                    {ODD, EVEN, EVEN, ODD, ODD},  // 4
                    {ODD, ODD, EVEN, EVEN, ODD},  // 5
                    {ODD, ODD, ODD, EVEN, EVEN}, // 6
                    {ODD, EVEN, ODD, EVEN, ODD},  // 7
                    {ODD, EVEN, ODD, ODD, EVEN}, // 8
                    {ODD, ODD, EVEN, ODD, EVEN}  // 9
            };

    /**
     * Sequence of parities to be used with UPCE.
     */
    private static final byte[][] PARITYE =
            {
                    {EVEN, EVEN, EVEN, ODD, ODD, ODD},  // 0
                    {EVEN, EVEN, ODD, EVEN, ODD, ODD},  // 1
                    {EVEN, EVEN, ODD, ODD, EVEN, ODD},  // 2
                    {EVEN, EVEN, ODD, ODD, ODD, EVEN}, // 3
                    {EVEN, ODD, EVEN, EVEN, ODD, ODD},  // 4
                    {EVEN, ODD, ODD, EVEN, EVEN, ODD},  // 5
                    {EVEN, ODD, ODD, ODD, EVEN, EVEN}, // 6
                    {EVEN, ODD, EVEN, ODD, EVEN, ODD},  // 7
                    {EVEN, ODD, EVEN, ODD, ODD, EVEN}, // 8
                    {EVEN, ODD, ODD, EVEN, ODD, EVEN}  // 9
            };

    /**
     * Creates new BarcodeEAN
     */
    public BarcodeEAN() {
        try {
            x = 0.8f;
            font = BaseFont.createFont("Helvetica", "winansi", false);
            size = 8;
            baseline = size;
            barHeight = size * 3;
            guardBars = true;
            codeType = EAN13;
            code = "";
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Calculates the EAN parity character.
     *
     * @param code the code
     * @return the parity character
     */
    public static int calculateEANParity(String code) {
        int mul = 3;
        int total = 0;
        for (int k = code.length() - 1; k >= 0; --k) {
            int n = code.charAt(k) - '0';
            total += mul * n;
            mul ^= 2;
        }
        return (10 - (total % 10)) % 10;
    }

    /**
     * Converts an UPCA code into an UPCE code. If the code can not be converted a <CODE>null</CODE> is returned.
     *
     * @param text the code to convert. It must have 12 numeric characters
     * @return the 8 converted digits or <CODE>null</CODE> if the code could not be converted
     */
    public static String convertUPCAtoUPCE(String text) {
        if (text.length() != 12 || !(text.startsWith("0") || text.startsWith("1"))) {
            return null;
        }
        if (text.startsWith("000", 3) || text.startsWith("100", 3)
                || text.startsWith("200", 3)) {
            if (text.startsWith("00", 6)) {
                return text.charAt(0) + text.substring(1, 3) + text.substring(8, 11) + text.charAt(3)
                        + text.substring(11);
            }
        } else if (text.startsWith("00", 4) && text.startsWith("000", 6)) {
            return text.charAt(0) + text.substring(1, 4) + text.substring(9, 11) + "3" + text.substring(11);
        } else if (text.charAt(5) == '0' && text.startsWith("0000", 6)) {
            return text.charAt(0) + text.substring(1, 5) + text.charAt(10) + "4" + text.substring(11);
        } else if (text.charAt(10) >= '5' && text.startsWith("0000", 6)) {
            return text.charAt(0) + text.substring(1, 6) + text.charAt(10) + text.substring(11);
        }
        return null;
    }

    /**
     * Creates the bars for the barcode EAN13 and UPCA.
     *
     * @param sCode the text with 13 digits
     * @return the barcode
     */
    public static byte[] getBarsEAN13(String sCode) {
        byte[] sequence = new byte[0];
        int[] code = new int[sCode.length()];
        for (int k = 0; k < code.length; ++k) {
            code[k] = sCode.charAt(k) - '0';
        }
        byte[] bars = new byte[TOTALBARS_EAN13];
        int pb = 0;
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 1;
        if(code.length > 0){
            sequence = PARITY13[code[0]];
        } else {
            logger.info(MSG_EMPTY_ARR);
        }
        for (int k = 0; k < sequence.length; ++k) {
            int c = code[k + 1];
            byte[] stripes = BARS[c];
            if (sequence[k] == ODD) {
                bars[pb++] = stripes[0];
                bars[pb++] = stripes[1];
                bars[pb++] = stripes[2];
                bars[pb++] = stripes[3];
            } else {
                bars[pb++] = stripes[3];
                bars[pb++] = stripes[2];
                bars[pb++] = stripes[1];
                bars[pb++] = stripes[0];
            }
        }
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 1;
        for (int k = 7; k < 13; ++k) {
            int c = code[k];
            byte[] stripes = BARS[c];
            bars[pb++] = stripes[0];
            bars[pb++] = stripes[1];
            bars[pb++] = stripes[2];
            bars[pb++] = stripes[3];
        }
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb] = 1;
        return bars;
    }

    /**
     * Creates the bars for the barcode EAN8.
     *
     * @param sCode the text with 8 digits
     * @return the barcode
     */
    public static byte[] getBarsEAN8(String sCode) {
        int[] code = new int[sCode.length()];
        for (int k = 0; k < code.length; ++k) {
            code[k] = sCode.charAt(k) - '0';
        }
        byte[] bars = new byte[TOTALBARS_EAN8];
        int pb = 0;
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 1;
        for (int k = 0; k < 4; ++k) {
            int c = code[k];
            byte[] stripes = BARS[c];
            bars[pb++] = stripes[0];
            bars[pb++] = stripes[1];
            bars[pb++] = stripes[2];
            bars[pb++] = stripes[3];
        }
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 1;
        for (int k = 4; k < 8; ++k) {
            int c = code[k];
            byte[] stripes = BARS[c];
            bars[pb++] = stripes[0];
            bars[pb++] = stripes[1];
            bars[pb++] = stripes[2];
            bars[pb++] = stripes[3];
        }
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb] = 1;
        return bars;
    }

    /**
     * Creates the bars for the barcode UPCE.
     *
     * @param sCode the text with 8 digits
     * @return the barcode
     */
    public static byte[] getBarsUPCE(String sCode) {
        boolean flip = false;
        byte[] sequence = new byte[]{};
        int[] code = new int[sCode.length()];
        for (int k = 0; k < code.length; ++k) {
            code[k] = sCode.charAt(k) - '0';
        }
        byte[] bars = new byte[TOTALBARS_UPCE];

        if (code.length > 0) {
            flip = (code[0] != 0);
            sequence = PARITYE[code[code.length - 1]];
        } else {
            logger.info(MSG_EMPTY_ARR);
        }

        int pb = 0;
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 1;

        // Proceed only if code is valid
        if (code.length > 1) {
            for (int k = 1; k < code.length - 1; ++k) {
                int c = code[k];
                byte[] stripes = BARS[c];
                if (sequence[k - 1] == (flip ? EVEN : ODD)) {
                    bars[pb++] = stripes[0];
                    bars[pb++] = stripes[1];
                    bars[pb++] = stripes[2];
                    bars[pb++] = stripes[3];
                } else {
                    bars[pb++] = stripes[3];
                    bars[pb++] = stripes[2];
                    bars[pb++] = stripes[1];
                    bars[pb++] = stripes[0];
                }
            }
        } else {
            logger.info(MSG_EMPTY_ARR);
        }

        // Set remaining bars
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb] = 1;

        return bars;
    }


    /**
     * Creates the bars for the barcode supplemental 2.
     *
     * @param sCode the text with 2 digits
     * @return the barcode
     */
    public static byte[] getBarsSupplemental2(String sCode) {
        int[] code = new int[2];
        for (int k = 0; k < code.length; ++k) {
            code[k] = sCode.charAt(k) - '0';
        }
        byte[] bars = new byte[TOTALBARS_SUPP2];
        int pb = 0;
        int parity = (code[0] * 10 + code[1]) % 4;
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 2;
        byte[] sequence = PARITY2[parity];
        for (int k = 0; k < sequence.length; ++k) {
            if (k == 1) {
                bars[pb++] = 1;
                bars[pb++] = 1;
            }
            int c = code[k];
            byte[] stripes = BARS[c];
            if (sequence[k] == ODD) {
                bars[pb++] = stripes[0];
                bars[pb++] = stripes[1];
                bars[pb++] = stripes[2];
                bars[pb++] = stripes[3];
            } else {
                bars[pb++] = stripes[3];
                bars[pb++] = stripes[2];
                bars[pb++] = stripes[1];
                bars[pb++] = stripes[0];
            }
        }
        return bars;
    }

    /**
     * Creates the bars for the barcode supplemental 5.
     *
     * @param sCode the text with 5 digits
     * @return the barcode
     */
    public static byte[] getBarsSupplemental5(String sCode) {
        int[] code = new int[5];
        for (int k = 0; k < code.length; ++k) {
            code[k] = sCode.charAt(k) - '0';
        }
        byte[] bars = new byte[TOTALBARS_SUPP5];
        int pb = 0;
        int parity = (((code[0] + code[2] + code[4]) * 3) + ((code[1] + code[3]) * 9)) % 10;
        bars[pb++] = 1;
        bars[pb++] = 1;
        bars[pb++] = 2;
        byte[] sequence = PARITY5[parity];
        for (int k = 0; k < sequence.length; ++k) {
            if (k != 0) {
                bars[pb++] = 1;
                bars[pb++] = 1;
            }
            int c = code[k];
            byte[] stripes = BARS[c];
            if (sequence[k] == ODD) {
                bars[pb++] = stripes[0];
                bars[pb++] = stripes[1];
                bars[pb++] = stripes[2];
                bars[pb++] = stripes[3];
            } else {
                bars[pb++] = stripes[3];
                bars[pb++] = stripes[2];
                bars[pb++] = stripes[1];
                bars[pb++] = stripes[0];
            }
        }
        return bars;
    }

    /**
     * Gets the maximum area that the barcode and the text, if any, will occupy. The lower left corner is always (0,
     * 0).
     *
     * @return the size the barcode occupies.
     */
    public Rectangle getBarcodeSize() {
        float width;
        float height = barHeight;
        if (font != null) {
            if (baseline <= 0) {
                height += -baseline + size;
            } else {
                height += baseline - font.getFontDescriptor(BaseFont.DESCENT, size);
            }
        }
        switch (codeType) {
            case EAN13:
                width = x * (11 + 12 * 7);
                if (font != null) {
                    width += font.getWidthPoint(code.charAt(0), size);
                }
                break;
            case EAN8:
                width = x * (11 + 8 * 7);
                break;
            case UPCA:
                width = x * (11 + 12 * 7);
                if (font != null) {
                    width += font.getWidthPoint(code.charAt(0), size) + font.getWidthPoint(code.charAt(11), size);
                }
                break;
            case UPCE:
                width = x * (9 + 6 * 7);
                if (font != null) {
                    width += font.getWidthPoint(code.charAt(0), size) + font.getWidthPoint(code.charAt(7), size);
                }
                break;
            case SUPP2:
                width = x * (6 + 2 * 7);
                break;
            case SUPP5:
                width = x * (4 + 5 * 7 + 4 * 2);
                break;
            default:
                throw new InvalidCodeTypeException(MessageLocalization.getComposedMessage("invalid.code.getTypeImpl"));
        }
        return new Rectangle(width, height);
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
        Rectangle rect = getBarcodeSize();
        float barStartX = calculateBarStartX();
        float barStartY = calculateBarStartY();
        float textStartY = calculateTextStartY();

        byte[] bars = getBarsForCodeType(codeType);
        int[] guard = getGuardForCodeType(codeType);

        float guardBarOffset = calculateGuardBarOffset();

        drawBars(cb, barColor, bars, guard, barStartX, barStartY, guardBarOffset);
        drawText(cb, textColor, barStartX, textStartY);

        return rect;
    }

    private float calculateBarStartX() {
        if (font == null) return 0;
        return (codeType == EAN13 || codeType == UPCA || codeType == UPCE) ? font.getWidthPoint(code.charAt(0), size) : 0;
    }

    private float calculateBarStartY() {
        if (font == null || baseline <= 0) return 0;
        return -font.getFontDescriptor(BaseFont.DESCENT, size) + baseline;
    }

    private float calculateTextStartY() {
        if (font == null) return 0;
        return baseline <= 0 ? barHeight - baseline : -font.getFontDescriptor(BaseFont.DESCENT, size);
    }

    private byte[] getBarsForCodeType(int codeType) {
        return switch (codeType) {
            case EAN13 -> getBarsEAN13(code);
            case EAN8 -> getBarsEAN8(code);
            case UPCA -> getBarsEAN13("0" + code);
            case UPCE -> getBarsUPCE(code);
            case SUPP2 -> getBarsSupplemental2(code);
            case SUPP5 -> getBarsSupplemental5(code);
            default -> new byte[0];
        };
    }

    private int[] getGuardForCodeType(int codeType) {
        return switch (codeType) {
            case EAN13 -> GUARD_EAN13;
            case EAN8 -> GUARD_EAN8;
            case UPCA -> GUARD_UPCA;
            case UPCE -> GUARD_UPCE;
            default -> GUARD_EMPTY;
        };
    }

    private float calculateGuardBarOffset() {
        return (font != null && baseline > 0 && guardBars) ? baseline / 2 : 0;
    }

    private void drawBars(PdfContentByte cb, Color barColor, byte[] bars, int[] guard, float barStartX, float barStartY, float guardBarOffset) {
        if (barColor != null) {
            cb.setColorFill(barColor);
        }
        if (bars != null) {
            boolean print = true;
            for (int k = 0; k < bars.length; ++k) {
                float w = bars[k] * x;
                if (print) {
                    float rectStartY = Arrays.binarySearch(guard, k) >= 0 ? barStartY - guardBarOffset : barStartY;
                    float rectHeight = barHeight + (rectStartY == barStartY - guardBarOffset ? guardBarOffset : 0);
                    cb.rectangle(barStartX, rectStartY, w - inkSpreading, rectHeight);
                }
                print = !print;
                barStartX += w;
            }
        }
        cb.fill();
    }

    private void drawText(PdfContentByte cb, Color textColor, float keepBarX, float textStartY) {
        if (font == null) return;

        if (textColor != null) {
            cb.setColorFill(textColor);
        }
        cb.beginText();
        cb.setFontAndSize(font, size);

        switch (codeType) {
            case EAN13:
                drawEAN13Text(cb, keepBarX, textStartY);
                break;
            case EAN8:
                drawEAN8Text(cb, textStartY);
                break;
            case UPCA:
                drawUPCAText(cb, keepBarX, textStartY);
                break;
            case UPCE:
                drawUPCEText(cb, keepBarX, textStartY);
                break;
            case SUPP2, SUPP5:
                drawSupplementalText(cb, textStartY);
                break;
            default:
                break;
        }

        cb.endText();
    }

    private void drawEAN13Text(PdfContentByte cb, float keepBarX, float textStartY) {
        cb.setTextMatrix(0, textStartY);
        cb.showText(code.substring(0, 1));
        for (int k = 1; k < 13; ++k) {
            drawSingleCharacter(cb, code.substring(k, k + 1), keepBarX + TEXTPOS_EAN13[k - 1] * x, textStartY);
        }
    }

    private void drawEAN8Text(PdfContentByte cb, float textStartY) {
        for (int k = 0; k < 8; ++k) {
            drawSingleCharacter(cb, code.substring(k, k + 1), TEXTPOS_EAN8[k] * x, textStartY);
        }
    }

    private void drawUPCAText(PdfContentByte cb, float keepBarX, float textStartY) {
        cb.setTextMatrix(0, textStartY);
        cb.showText(code.substring(0, 1));
        for (int k = 1; k < 11; ++k) {
            drawSingleCharacter(cb, code.substring(k, k + 1), keepBarX + TEXTPOS_EAN13[k] * x, textStartY);
        }
        cb.setTextMatrix(keepBarX + x * (11 + 12 * 7), textStartY);
        cb.showText(code.substring(11, 12));
    }

    private void drawUPCEText(PdfContentByte cb, float keepBarX, float textStartY) {
        cb.setTextMatrix(0, textStartY);
        cb.showText(code.substring(0, 1));
        for (int k = 1; k < 7; ++k) {
            drawSingleCharacter(cb, code.substring(k, k + 1), keepBarX + TEXTPOS_EAN13[k - 1] * x, textStartY);
        }
        cb.setTextMatrix(keepBarX + x * (9 + 6 * 7), textStartY);
        cb.showText(code.substring(7, 8));
    }

    private void drawSupplementalText(PdfContentByte cb, float textStartY) {
        for (int k = 0; k < code.length(); ++k) {
            drawSingleCharacter(cb, code.substring(k, k + 1), (7.5f + (9 * k)) * x, textStartY);
        }
    }

    private void drawSingleCharacter(PdfContentByte cb, String c, float positionX, float positionY) {
        float len = font.getWidthPoint(c, size);
        cb.setTextMatrix(positionX - len / 2, positionY);
        cb.showText(c);
    }


    /**
     * Creates a <CODE>java.awt.Image</CODE>. This image only contains the bars without any text.
     *
     * @param foreground the color of the bars
     * @param background the color of the background
     * @return the image
     */
    public Image createAwtImage(Color foreground, Color background) {
        int f = foreground.getRGB();
        int g = background.getRGB();
        Canvas canvas = new Canvas();

        int width;
        byte[] bars;
        width = switch (codeType) {
            case EAN13 -> {
                bars = getBarsEAN13(code);
                yield 11 + 12 * 7;
            }
            case EAN8 -> {
                bars = getBarsEAN8(code);
                yield 11 + 8 * 7;
            }
            case UPCA -> {
                bars = getBarsEAN13("0" + code);
                yield 11 + 12 * 7;
            }
            case UPCE -> {
                bars = getBarsUPCE(code);
                yield 9 + 6 * 7;
            }
            case SUPP2 -> {
                bars = getBarsSupplemental2(code);
                yield 6 + 2 * 7;
            }
            case SUPP5 -> {
                bars = getBarsSupplemental5(code);
                yield 4 + 5 * 7 + 4 * 2;
            }
            default -> throw new InvalidCodeTypeException(
                    MessageLocalization.getComposedMessage("invalid.code.getTypeImpl"));
        };

        boolean print = true;
        int ptr = 0;
        int height = (int) barHeight;
        int[] pix = new int[width * height];
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
        for (int k = width; k < pix.length; k += width) {
            System.arraycopy(pix, 0, pix, k, width);
        }

        return canvas.createImage(new MemoryImageSource(width, height, pix, 0, width));
    }
}
