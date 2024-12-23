/*
 * $Id: BarcodeInter25.java 4065 2009-09-16 23:09:11Z psoares33 $
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
import java.awt.Canvas;
import java.awt.Color;
import java.awt.image.MemoryImageSource;
import java.io.IOException;

/**
 * Implements the code interleaved 2 of 5. The text can include non-numeric characters that are printed but do not
 * generate bars. The default parameters are:
 * <pre>
 * x = 0.8f;
 * n = 2;
 * font = BaseFont.createFont("Helvetica", "winansi", false);
 * size = 8;
 * baseline = size;
 * barHeight = size * 3;
 * textAlignment = Element.ALIGN_CENTER;
 * generateChecksum = false;
 * checksumText = false;
 * </pre>
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class BarcodeInter25 extends Barcode {

    /**
     * The bars to generate the code.
     */
    private static final byte[][] BARS =
            {
                    {0, 0, 1, 1, 0},
                    {1, 0, 0, 0, 1},
                    {0, 1, 0, 0, 1},
                    {1, 1, 0, 0, 0},
                    {0, 0, 1, 0, 1},
                    {1, 0, 1, 0, 0},
                    {0, 1, 1, 0, 0},
                    {0, 0, 0, 1, 1},
                    {1, 0, 0, 1, 0},
                    {0, 1, 0, 1, 0}
            };

    /**
     * Creates new BarcodeInter25
     */
    public BarcodeInter25() {
        try {
            x = 0.8f;
            n = 2;
            font = BaseFont.createFont("Helvetica", "winansi", false);
            size = 8;
            baseline = size;
            barHeight = size * 3;
            textAlignment = Element.ALIGN_CENTER;
            generateChecksum = false;
            checksumText = false;
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Deletes all the non-numeric characters from <CODE>text</CODE>.
     *
     * @param text the text
     * @return a <CODE>String</CODE> with only numeric characters
     */
    public static String keepNumbers(String text) {
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < text.length(); ++k) {
            char c = text.charAt(k);
            if (c >= '0' && c <= '9') {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Calculates the checksum.
     *
     * @param text the numeric text
     * @return the checksum
     */
    public static char getChecksum(String text) {
        int mul = 3;
        int total = 0;
        for (int k = text.length() - 1; k >= 0; --k) {
            int n = text.charAt(k) - '0';
            total += mul * n;
            mul ^= 2;
        }
        return (char) (((10 - (total % 10)) % 10) + '0');
    }

    /**
     * Creates the bars for the barcode.
     *
     * @param text the text. It can contain non-numeric characters
     * @return the barcode
     */
    public static byte[] getBarsInter25(String text) {
        // Keep only numbers from the input text
        text = keepNumbers(text);

        // Validate that the length of the text is even
        if ((text.length() & 1) != 0) {
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("the.text.length.must.be.even"));
        }

        // Calculate the length of the bars array
        int totalBarsLength = text.length() * 5 + 7;
        byte[] bars = new byte[totalBarsLength];

        // Initialize the starting position for writing to bars
        int position = 0;

        // Start with the beginning sequence of bars
        bars[position++] = 0; // Start
        bars[position++] = 0; // Start
        bars[position++] = 0; // Start
        bars[position++] = 0; // Start

        // Process each pair of digits in the input text
        int len = text.length() / 2;
        for (int k = 0; k < len; ++k) {
            // Get the two characters, convert to numbers
            int c1 = text.charAt(k * 2) - '0';
            int c2 = text.charAt(k * 2 + 1) - '0';

            // Retrieve the corresponding byte arrays for the characters
            byte[] b1 = BARS[c1];
            byte[] b2 = BARS[c2];

            // Append the bar representations for both characters
            for (int j = 0; j < 5; ++j) {
                bars[position++] = b1[j];
                bars[position++] = b2[j];
            }
        }

        // End with the termination sequence of bars
        bars[position++] = 1; // End
        bars[position++] = 0; // End
        bars[position] = 0; // End

        return bars; // Return the completed bar array
    }


    /**
     * Gets the maximum area that the barcode and the text, if any, will occupy. The lower left corner is always (0,
     * 0).
     *
     * @return the size the barcode occupies.
     */
    public Rectangle getBarcodeSize() {
        float fontX = 0;
        float fontY = 0;
        if (font != null) {
            if (baseline > 0) {
                fontY = baseline - font.getFontDescriptor(BaseFont.DESCENT, size);
            } else {
                fontY = -baseline + size;
            }
            String fullCode = code;
            if (generateChecksum && checksumText) {
                fullCode += getChecksum(fullCode);
            }
            fontX = font.getWidthPoint(altText != null ? altText : fullCode, size);
        }
        String fullCode = keepNumbers(code);
        int len = fullCode.length();
        if (generateChecksum) {
            ++len;
        }
        float fullWidth = len * (3 * x + 2 * x * n) + (6 + n) * x;
        fullWidth = Math.max(fullWidth, fontX);
        float fullHeight = barHeight + fontY;
        return new Rectangle(fullWidth, fullHeight);
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
        String fullCode = getFullCode();
        String bCode = getBarcodeCode();

        float fontX = (font != null) ? font.getWidthPoint(fullCode, size) : 0;
        float fullWidth = getBarcodeWidth(bCode);

        float barStartX = getBarStartX(fontX, fullWidth);
        float textStartX = getTextStartX(fontX, fullWidth);

        float barStartY = 0;
        float textStartY = 0;

        if (font != null) {
            float[] startY = calculateTextAndBarStartY();
            textStartY = startY[0];
            barStartY = startY[1];
        }

        drawBars(cb, barColor, bCode, barStartX, barStartY);

        if (font != null) {
            drawText(cb, textColor, fullCode, textStartX, textStartY);
        }

        return getBarcodeSize();
    }

    private String getFullCode() {
        String fullCode = code;
        if (font != null) {
            if (generateChecksum && checksumText) {
                fullCode += getChecksum(fullCode);
            }
            fullCode = (altText != null) ? altText : fullCode;
        }
        return fullCode;
    }

    private String getBarcodeCode() {
        String bCode = keepNumbers(code);
        if (generateChecksum) {
            bCode += getChecksum(bCode);
        }
        return bCode;
    }

    private float getBarcodeWidth(String bCode) {
        int len = bCode.length();
        return len * (3 * x + 2 * x * n) + (6 + n) * x;
    }

    private float getBarStartX(float fontX, float fullWidth) {
        float barStartX;
        if (textAlignment == Element.ALIGN_RIGHT) {
            barStartX = (fontX > fullWidth) ? fontX - fullWidth : 0;
        } else {
            barStartX = (fontX > fullWidth) ? (fontX - fullWidth) / 2 : 0;
        }
        return barStartX;
    }

    private float getTextStartX(float fontX, float fullWidth) {
        float textStartX;
        if (textAlignment == Element.ALIGN_RIGHT) {
            textStartX = (fontX <= fullWidth) ? fullWidth - fontX : 0;
        } else {
            textStartX = (fontX <= fullWidth) ? (fullWidth - fontX) / 2 : 0;
        }
        return textStartX;
    }

    private float[] calculateTextAndBarStartY() {
        float textStartY;
        float barStartY;
        if (baseline <= 0) {
            textStartY = barHeight - baseline;
            barStartY = 0;
        } else {
            textStartY = -font.getFontDescriptor(BaseFont.DESCENT, size);
            barStartY = textStartY + baseline;
        }
        return new float[] {textStartY, barStartY};
    }

    private void drawBars(PdfContentByte cb, Color barColor, String bCode, float barStartX, float barStartY) {
        byte[] bars = getBarsInter25(bCode);
        boolean print = true;
        if (barColor != null) {
            cb.setColorFill(barColor);
        }
        for (byte bar : bars) {
            float w = (bar == 0 ? x : x * n);
            if (print) {
                cb.rectangle(barStartX, barStartY, w - inkSpreading, barHeight);
            }
            print = !print;
            barStartX += w;
        }
        cb.fill();
    }

    private void drawText(PdfContentByte cb, Color textColor, String fullCode, float textStartX, float textStartY) {
        if (textColor != null) {
            cb.setColorFill(textColor);
        }
        cb.beginText();
        cb.setFontAndSize(font, size);
        cb.setTextMatrix(textStartX, textStartY);
        cb.showText(fullCode);
        cb.endText();
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

        String bCode = keepNumbers(code);
        if (generateChecksum) {
            bCode += getChecksum(bCode);
        }
        int len = bCode.length();
        int nn = (int) n;
        int fullWidth = len * (3 + 2 * nn) + (6 + nn);
        byte[] bars = getBarsInter25(bCode);
        boolean print = true;
        int ptr = 0;
        int height = (int) barHeight;
        int[] pix = new int[fullWidth * height];
        for (byte bar : bars) {
            int w = (bar == 0 ? 1 : nn);
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
}
