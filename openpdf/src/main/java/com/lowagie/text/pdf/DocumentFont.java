/*
 * Copyright 2004 by Paulo Soares.
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
import com.lowagie.text.ExceptionConverter;
import java.io.IOException;
import java.util.HashMap;


/**
 * @author psoares
 */
public class DocumentFont extends BaseFont {

    private static final String UNI_JIS_UCS2_H = "UniJIS-UCS2-H";
    private static final String UNI_GB_UCS2_H = "UniGB-UCS2-H";
    private static final String UNI_CNS_UCS2_H = "UniCNS-UCS2-H";
    private static final String UNI_KS_UCS2_H = "UniKS-UCS2-H";

    private static final int[] stdEnc = {
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            32, 33, 34, 35, 36, 37, 38, 8217, 40, 41, 42, 43, 44, 45, 46, 47,
            48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63,
            64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
            80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95,
            8216, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111,
            112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123, 124, 125, 126, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 161, 162, 163, 8260, 165, 402, 167, 164, 39, 8220, 171, 8249, 8250, 64257, 64258,
            0, 8211, 8224, 8225, 183, 0, 182, 8226, 8218, 8222, 8221, 187, 8230, 8240, 0, 191,
            0, 96, 180, 710, 732, 175, 728, 729, 168, 0, 730, 184, 0, 733, 731, 711,
            8212, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
            0, 198, 0, 170, 0, 0, 0, 0, 321, 216, 338, 186, 0, 0, 0, 0,
            0, 230, 0, 0, 0, 305, 0, 0, 322, 248, 339, 223, 0, 0, 0, 0};
    private static String[] cjkNames = {"HeiseiMin-W3", "HeiseiKakuGo-W5", "STSong-Light", "MHei-Medium",
            "MSung-Light", "HYGoThic-Medium", "HYSMyeongJo-Medium", "MSungStd-Light", "STSongStd-Light",
            "HYSMyeongJoStd-Medium", "KozMinPro-Regular"};
    private static String[] cjkEncs = {UNI_JIS_UCS2_H, UNI_JIS_UCS2_H, UNI_GB_UCS2_H, UNI_CNS_UCS2_H,
            UNI_CNS_UCS2_H, UNI_KS_UCS2_H, UNI_KS_UCS2_H, UNI_CNS_UCS2_H, UNI_GB_UCS2_H,
            UNI_KS_UCS2_H, UNI_JIS_UCS2_H};
    private static String[] cjkNames2 = {"MSungStd-Light", "STSongStd-Light", "HYSMyeongJoStd-Medium",
            "KozMinPro-Regular"};
    private static String[] cjkEncs2 = {UNI_CNS_UCS2_H, UNI_GB_UCS2_H, UNI_KS_UCS2_H, UNI_JIS_UCS2_H,
            "UniCNS-UTF16-H", "UniGB-UTF16-H", "UniKS-UTF16-H", "UniJIS-UTF16-H"};
    // code, [glyph, width]
    private HashMap<Integer, int[]> metrics = new HashMap<>();
    private String fontName;
    private PRIndirectReference refFont;
    private PdfDictionary font;
    private IntHashtable uni2byte = new IntHashtable();
    private IntHashtable diffmap;
    private float ascender = 800;
    private static final int CAPHEIGHTCONST = 700;
    private float descender = -200;
    private static final int ITALICANGLECONST = 0;
    private float llx = -50;
    private float lly = -200;
    private float urx = 100;
    private float ury = 900;
    private boolean isType0 = false;
    private BaseFont cjkMirror;
    float capHeightVar;
    float italicAngleVar;


    /**
     * Creates a new instance of DocumentFont
     */
    DocumentFont(PRIndirectReference refFont) {
        this.refFont = refFont;
        initializeFont();
        initializeFontName();
        determineFontTypeAndEncoding();
    }

    private void initializeFont() {
        encoding = "";
        fontSpecific = false;
        fontType = FONT_TYPE_DOCUMENT;
        font = (PdfDictionary) PdfReader.getPdfObject(refFont);
    }

    private void initializeFontName() {
        PdfName asName = font.getAsName(PdfName.BASEFONT);
        if (asName != null) {
            fontName = PdfName.decodeName(asName.toString());
        } else {
            fontName = "badFontName";
        }
    }

    private void determineFontTypeAndEncoding() {
        PdfName subType = font.getAsName(PdfName.SUBTYPE);
        if (PdfName.TYPE1.equals(subType) || PdfName.TRUETYPE.equals(subType)) {
            doType1TT();
        } else if (!tryHandleCJKFont()) {
            handleNonCJKFont(subType);
        }
    }

    private boolean tryHandleCJKFont() {
        for (String cjkName : cjkNames) {
            if (fontName.startsWith(cjkName)) {
                fontName = cjkName;
                try {
                    cjkMirror = BaseFont.createFont(fontName, getCJKEncoding(cjkName), false);
                } catch (Exception e) {
                    throw new ExceptionConverter(e);
                }
                return true;
            }
        }
        return false;
    }

    private String getCJKEncoding(String fontName) {
        for (int k = 0; k < cjkNames.length; ++k) {
            if (cjkNames[k].equals(fontName)) {
                return cjkEncs[k];
            }
        }
        return null;
    }

    private void handleNonCJKFont(PdfName subType) {
        PdfName encName = font.getAsName(PdfName.ENCODING);
        if (encName != null) {
            String enc = PdfName.decodeName(encName.toString());
            if (!tryHandleCJKEncoding(enc)) {
                encoding = enc;
                if (PdfName.TYPE0.equals(subType) && "Identity-H".equals(enc)) {
                    processType0(font);
                    isType0 = true;
                }
            }
        }
    }

    private boolean tryHandleCJKEncoding(String enc) {
        for (int k = 0; k < cjkEncs2.length; ++k) {
            if (enc.startsWith(cjkEncs2[k])) {
                try {
                    int adjustedK = (k > 3) ? k - 4 : k;
                    cjkMirror = BaseFont.createFont(cjkNames2[adjustedK], cjkEncs2[adjustedK], false);
                } catch (Exception e) {
                    throw new ExceptionConverter(e);
                }
                return true;
            }
        }
        return false;
    }

    private void processType0(PdfDictionary font) {
        try {
            PdfObject toUniObject = PdfReader.getPdfObjectRelease(font.get(PdfName.TOUNICODE));
            PdfArray df = (PdfArray) PdfReader.getPdfObjectRelease(font.get(PdfName.DESCENDANTFONTS));
            PdfDictionary cidft = (PdfDictionary) PdfReader.getPdfObjectRelease(df.getPdfObject(0));
            PdfNumber dwo = (PdfNumber) PdfReader.getPdfObjectRelease(cidft.get(PdfName.DW));
            int dw = 1000;
            if (dwo != null) {
                dw = dwo.intValue();
            }
            IntHashtable widths = readWidths((PdfArray) PdfReader.getPdfObjectRelease(cidft.get(PdfName.W)));
            PdfDictionary fontDesc = (PdfDictionary) PdfReader.getPdfObjectRelease(cidft.get(PdfName.FONTDESCRIPTOR));
            fillFontDesc(fontDesc);
            if (toUniObject != null) {
                fillMetrics(PdfReader.getStreamBytes((PRStream) toUniObject), widths, dw);
            }

        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    private IntHashtable readWidths(PdfArray ws) {
        IntHashtable hh = new IntHashtable(); // Initialize an IntHashtable to store widths

        if (ws == null) {
            return hh; // Return empty IntHashtable if ws is null
        }

        int size = ws.size(); // Get the size of the PdfArray ws
        int index = 0; // Initialize an index variable

        while (index < size) {
            int c1 = ((PdfNumber) PdfReader.getPdfObjectRelease(ws.getPdfObject(index))).intValue(); // Get the first number

            // Move to the next object in the array
            index++;
            PdfObject obj = PdfReader.getPdfObjectRelease(ws.getPdfObject(index)); // Get the object at the next index

            // Check if the object is an array
            if (obj.isArray()) {
                PdfArray a2 = (PdfArray) obj; // Cast the object to PdfArray
                for (int j = 0; j < a2.size(); j++) {
                    int c2 = ((PdfNumber) PdfReader.getPdfObjectRelease(a2.getPdfObject(j))).intValue(); // Get the second number
                    hh.put(c1, c2); // Put the mapping into the IntHashtable
                    c1++; // Increment c1
                }
            } else {
                int c2 = ((PdfNumber) obj).intValue(); // If not an array, get the integer value
                index++; // Move to the next object in the array
                int w = ((PdfNumber) PdfReader.getPdfObjectRelease(ws.getPdfObject(index))).intValue(); // Get the width

                for (int i = c1; i <= c2; i++) {
                    hh.put(i, w); // Put the range mapping into the IntHashtable
                }
            }

            // Move to the next object in the array
            index++;
        }

        return hh; // Return the populated IntHashtable
    }


    private String decodeString(PdfString ps) {
        if (ps.isHexWriting()) {
            return PdfEncodings.convertToString(ps.getBytes(), "UnicodeBigUnmarked");
        } else {
            return ps.toUnicodeString();
        }
    }

    private void fillMetrics(byte[] touni, IntHashtable widths, int dw) {
        try {
            PdfContentParser ps = new PdfContentParser(new PRTokeniser(touni));
            PdfObject ob = null;
            PdfObject last = null;
            while ((ob = ps.readPRObject()) != null) {
                if (ob.getTypeImpl() == PdfContentParser.COMMAND_TYPE) {
                    String command = ob.toString();
                    int n = ((PdfNumber) last).intValue();
                    if ("beginbfchar".equals(command)) {
                        handleBeginbfchar(ps, widths, dw, n);
                    } else if ("beginbfrange".equals(command)) {
                        handleBeginbfrange(ps, widths, dw, n);
                    }
                } else {
                    last = ob;
                }
            }
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    private void handleBeginbfchar(PdfContentParser ps, IntHashtable widths, int dw, int n) throws IOException {
        for (int k = 0; k < n; ++k) {
            String cid = decodeString((PdfString) ps.readPRObject());
            String uni = decodeString((PdfString) ps.readPRObject());
            if (uni.length() == 1) {
                int cidc = cid.charAt(0);
                int unic = uni.charAt(uni.length() - 1);
                int w = getWidthOrDefault(widths, cidc, dw);
                metrics.put(unic, new int[]{cidc, w});
            }
        }
    }

    private void handleBeginbfrange(PdfContentParser ps, IntHashtable widths, int dw, int n) throws IOException {
        for (int k = 0; k < n; ++k) {
            String cid1 = decodeString((PdfString) ps.readPRObject());
            String cid2 = decodeString((PdfString) ps.readPRObject());
            int cid1c = cid1.charAt(0);
            int cid2c = cid2.charAt(0);
            PdfObject ob2 = ps.readPRObject();
            if (ob2.isString()) {
                handleStringRange(widths, dw, cid1c, cid2c, (PdfString) ob2);
            } else {
                handleArrayRange(widths, dw, cid1c, (PdfArray) ob2);
            }
        }
    }

    private void handleStringRange(IntHashtable widths, int dw, int cid1c, int cid2c, PdfString uni) {
        if (uni.length() == 1) {
            int unic = uni.charAt(uni.length() - 1);
            for (; cid1c <= cid2c; cid1c++, unic++) {
                int w = getWidthOrDefault(widths, cid1c, dw);
                metrics.put(unic, new int[]{cid1c, w});
            }
        }
    }

    private void handleArrayRange(IntHashtable widths, int dw, int cid1c, PdfArray a) {
        for (int j = 0; j < a.size(); ++j, ++cid1c) {
            String uni = decodeString(a.getAsString(j));
            if (uni.length() == 1) {
                int unic = uni.charAt(uni.length() - 1);
                int w = getWidthOrDefault(widths, cid1c, dw);
                metrics.put(unic, new int[]{cid1c, w});
            }
        }
    }

    private int getWidthOrDefault(IntHashtable widths, int cidc, int dw) {
        return widths.containsKey(cidc) ? widths.get(cidc) : dw;
    }


    private void doType1TT() {
        PdfObject enc = PdfReader.getPdfObject(font.get(PdfName.ENCODING));
        handleEncoding(enc);
        handleFontWidths();
        fillFontDesc(font.getAsDict(PdfName.FONTDESCRIPTOR));
    }

    private void handleEncoding(PdfObject enc) {
        if (enc == null) {
            handleNoEncoding();
        } else if (enc.isName()) {
            fillEncoding((PdfName) enc);
        } else {
            handleComplexEncoding((PdfDictionary) enc);
        }
    }

    private void handleNoEncoding() {
        PdfName baseFont = font.getAsName(PdfName.BASEFONT);
        if (PdfName.SYMBOL.equals(baseFont) || PdfName.ZAPFDINGBATS.equals(baseFont)) {
            fillEncoding(baseFont);
        } else {
            fillEncoding(null);
        }
    }

    private void handleComplexEncoding(PdfDictionary encDic) {
        PdfObject baseEnc = PdfReader.getPdfObject(encDic.get(PdfName.BASEENCODING));
        if (baseEnc == null) {
            fillEncoding(null);
        } else {
            fillEncoding((PdfName) baseEnc);
        }
        PdfArray diffs = encDic.getAsArray(PdfName.DIFFERENCES);
        if (diffs != null) {
            processDifferences(diffs);
        }
    }

    private void processDifferences(PdfArray diffs) {
        diffmap = new IntHashtable();
        int currentNumber = 0;
        for (int k = 0; k < diffs.size(); ++k) {
            PdfObject obj = diffs.getPdfObject(k);
            if (obj.isNumber()) {
                currentNumber = ((PdfNumber) obj).intValue();
            } else {
                processDifferenceObject(obj, currentNumber);
                ++currentNumber;
            }
        }
    }

    private void processDifferenceObject(PdfObject obj, int currentNumber) {
        int[] c = GlyphList.nameToUnicode(PdfName.decodeName(obj.toString()));
        if (c != null && c.length > 0) {
            uni2byte.put(c[0], currentNumber);
            diffmap.put(c[0], currentNumber);
        }
    }

    private void handleFontWidths() {
        PdfArray newWidths = font.getAsArray(PdfName.WIDTHS);
        PdfNumber first = font.getAsNumber(PdfName.FIRSTCHAR);
        PdfNumber last = font.getAsNumber(PdfName.LASTCHAR);
        if (BuiltinFonts14.containsKey(fontName)) {
            applyBuiltinFontWidths();
        }
        if (first != null && last != null && newWidths != null) {
            int f = first.intValue();
            for (int k = 0; k < newWidths.size(); ++k) {
                widths[f + k] = newWidths.getAsNumber(k).intValue();
            }
        }
    }

    private void applyBuiltinFontWidths() {
        try {
            BaseFont bf = BaseFont.createFont(fontName, WINANSI, false);
            applyWidthsFromBaseFont(bf);
            applyDiffmapWidths(bf);
            setFontMetrics(bf);
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    private void applyWidthsFromBaseFont(BaseFont bf) {
        int[] e = uni2byte.toOrderedKeys();
        for (int i : e) {
            int n = uni2byte.get(i);
            widths[n] = bf.getRawWidth(n, GlyphList.unicodeToName(i));
        }
    }

    private void applyDiffmapWidths(BaseFont bf) {
        if (diffmap != null) {
            int[] e = diffmap.toOrderedKeys();
            for (int i : e) {
                int n = diffmap.get(i);
                widths[n] = bf.getRawWidth(n, GlyphList.unicodeToName(i));
            }
            diffmap = null;
        }
    }

    private void setFontMetrics(BaseFont bf) {
        ascender = bf.getFontDescriptor(ASCENT, 1000);
        capHeightVar = bf.getFontDescriptor(CAPHEIGHTCONST, 1000);
        descender = bf.getFontDescriptor(DESCENT, 1000);
        italicAngleVar = bf.getFontDescriptor(ITALICANGLECONST, 1000);
        llx = bf.getFontDescriptor(BBOXLLX, 1000);
        lly = bf.getFontDescriptor(BBOXLLY, 1000);
        urx = bf.getFontDescriptor(BBOXURX, 1000);
        ury = bf.getFontDescriptor(BBOXURY, 1000);
    }


    private void fillFontDesc(PdfDictionary fontDesc) {
        if (fontDesc == null) {
            return;
        }
        PdfNumber v = fontDesc.getAsNumber(PdfName.ASCENT);
        if (v != null) {
            ascender = v.floatValue();
        }
        v = fontDesc.getAsNumber(PdfName.CAPHEIGHT);
        if (v != null) {
            capHeightVar = v.floatValue();
        }
        v = fontDesc.getAsNumber(PdfName.DESCENT);
        if (v != null) {
            descender = v.floatValue();
        }
        v = fontDesc.getAsNumber(PdfName.ITALICANGLE);
        if (v != null) {
            italicAngleVar = v.floatValue();
        }
        PdfArray bbox = fontDesc.getAsArray(PdfName.FONTBBOX);
        if (bbox != null) {
            llx = bbox.getAsNumber(0).floatValue();
            lly = bbox.getAsNumber(1).floatValue();
            urx = bbox.getAsNumber(2).floatValue();
            ury = bbox.getAsNumber(3).floatValue();
            if (llx > urx) {
                float t = llx;
                llx = urx;
                urx = t;
            }
            if (lly > ury) {
                float t = lly;
                lly = ury;
                ury = t;
            }
        }
    }

    private void fillEncoding(PdfName encoding) {
        if (PdfName.MAC_ROMAN_ENCODING.equals(encoding) || PdfName.WIN_ANSI_ENCODING.equals(encoding)
                || PdfName.SYMBOL.equals(encoding) || PdfName.ZAPFDINGBATS.equals(encoding)) {
            byte[] b = new byte[256];
            for (int k = 0; k < 256; ++k) {
                b[k] = (byte) k;
            }
            String enc = WINANSI;
            if (PdfName.MAC_ROMAN_ENCODING.equals(encoding)) {
                enc = MACROMAN;
            } else if (PdfName.SYMBOL.equals(encoding)) {
                enc = SYMBOL;
            } else if (PdfName.ZAPFDINGBATS.equals(encoding)) {
                enc = ZAPFDINGBATS;
            }
            String cv = PdfEncodings.convertToString(b, enc);
            char[] arr = cv.toCharArray();
            for (int k = 0; k < 256; ++k) {
                uni2byte.put(arr[k], k);
            }
        } else {
            for (int k = 0; k < 256; ++k) {
                uni2byte.put(stdEnc[k], k);
            }
        }
    }

    /**
     * Gets the family getName of the font. If it is a True Type font each array element will have {Platform ID, Platform
     * Encoding ID, Language ID, font getName}. The interpretation of this values can be found in the Open Type
     * specification, chapter 2, in the 'getName' table.<br> For the other fonts the array has a single element with {"",
     * "", "", font getName}.
     *
     * @return the family getName of the font
     */
    public String[][] getFamilyFontName() {
        return getFullFontName();
    }

    /**
     * Gets the font parameter identified by <CODE>key</CODE>. Valid values for <CODE>key</CODE> are
     * <CODE>ASCENT</CODE>,
     * <CODE>CAPHEIGHT</CODE>, <CODE>DESCENT</CODE>,
     * <CODE>ITALICANGLE</CODE>, <CODE>BBOXLLX</CODE>, <CODE>BBOXLLY</CODE>, <CODE>BBOXURX</CODE>
     * and <CODE>BBOXURY</CODE>.
     *
     * @param key      the parameter to be extracted
     * @param fontSize the font size in points
     * @return the parameter in points
     */
    public float getFontDescriptor(int key, float fontSize) {
        if (cjkMirror != null) {
            return cjkMirror.getFontDescriptor(key, fontSize);
        }
        return switch (key) {
            case AWT_ASCENT, ASCENT -> ascender * fontSize / 1000;
            case CAPHEIGHTCONST -> capHeightVar * fontSize / 1000;
            case AWT_DESCENT, DESCENT -> descender * fontSize / 1000;
            case ITALICANGLECONST -> italicAngleVar;
            case BBOXLLX -> llx * fontSize / 1000;
            case BBOXLLY -> lly * fontSize / 1000;
            case BBOXURX -> urx * fontSize / 1000;
            case BBOXURY -> ury * fontSize / 1000;
            case AWT_MAXADVANCE -> (urx - llx) * fontSize / 1000;
            default -> 0;
        };
    }

    /**
     * Gets the full getName of the font. If it is a True Type font each array element will have {Platform ID, Platform
     * Encoding ID, Language ID, font getName}. The interpretation of this values can be found in the Open Type
     * specification, chapter 2, in the 'getName' table.<br> For the other fonts the array has a single element with {"",
     * "", "", font getName}.
     *
     * @return the full getName of the font
     */
    public String[][] getFullFontName() {
        return new String[][]{{"", "", "", fontName}};
    }

    /**
     * Gets all the entries of the names-table. If it is a True Type font each array element will have {Name ID,
     * Platform ID, Platform Encoding ID, Language ID, font getName}. The interpretation of this values can be found in the
     * Open Type specification, chapter 2, in the 'getName' table.<br> For the other fonts the array has a single element
     * with {"4", "", "", "", font getName}.
     *
     * @return the full getName of the font
     * @since 2.0.8
     */
    public String[][] getAllNameEntries() {
        return new String[][]{{"4", "", "", "", fontName}};
    }

    /**
     * Gets the kerning between two Unicode chars.
     *
     * @param char1 the first char
     * @param char2 the second char
     * @return the kerning to be applied
     */
    public int getKerning(int char1, int char2) {
        return 0;
    }

    /**
     * Gets the postscript font getName.
     *
     * @return the postscript font getName
     */
    public String getPostscriptFontName() {
        return fontName;
    }

    /**
     * Sets the font getName that will appear in the pdf font dictionary. It does nothing in this case as the font is
     * already in the document.
     *
     * @param name the new font getName
     */
    public void setPostscriptFontName(String name) {
        throw new UnsupportedOperationException("Setting postscript font getName is not supported.");
    }

    /**
     * Gets the width from the font according to the Unicode char <CODE>c</CODE> or the <CODE>getName</CODE>. If the
     * <CODE>getName</CODE> is null it's a symbolic font.
     *
     * @param c    the unicode char
     * @param name the glyph getName
     * @return the width of the char
     */
    int getRawWidth(int c, String name) {
        return 0;
    }

    /**
     * Checks if the font has any kerning pairs.
     *
     * @return <CODE>true</CODE> if the font has any kerning pairs
     */
    public boolean hasKernPairs() {
        return false;
    }

    /**
     * Outputs to the writer the font dictionaries and streams.
     *
     * @param writer the writer for this document
     * @param ref    the font indirect reference
     * @param params several parameters that depend on the font getTypeImpl
     * @throws DocumentException error in generating the object
     */
    void writeFont(PdfWriter writer, PdfIndirectReference ref, Object[] params) throws DocumentException {
        throw new UnsupportedOperationException("Writing font is not supported.");
    }

    /**
     * Always returns null.
     *
     * @return null
     * @since 2.1.3
     */
    public PdfStream getFullFontStream() {
        return null;
    }

    /**
     * Gets the width of a <CODE>char</CODE> in normalized 1000 units.
     *
     * @param char1 the unicode <CODE>char</CODE> to get the width of
     * @return the width in normalized 1000 units
     */
    @Override
    public int getWidth(int char1) {
        if (cjkMirror != null) {
            return cjkMirror.getWidth(char1);
        } else if (isType0) {
            int[] ws = metrics.get(char1);
            if (ws != null) {
                return ws[1];
            } else {
                return 0;
            }
        } else {
            return super.getWidth(char1);
        }
    }

    @Override
    public int getWidth(String text) {
        if (cjkMirror != null) {
            return cjkMirror.getWidth(text);
        } else if (isType0) {
            char[] chars = text.toCharArray();
            int total = 0;
            for (char aChar : chars) {
                int[] ws = metrics.get(Character.getNumericValue(aChar));
                if (ws != null) {
                    total += ws[1];
                }
            }
            return total;
        } else {
            return super.getWidth(text);
        }
    }

    @Override
    byte[] convertToBytes(String text) {
        if (cjkMirror != null) {
            return convertToCJKBytes(text);
        } else if (isType0) {
            return convertToType0Bytes(text);
        } else {
            return convertToStandardBytes(text);
        }
    }

    private byte[] convertToCJKBytes(String text) {
        return PdfEncodings.convertToBytes(text, CJKFont.CJK_ENCODING);
    }

    private byte[] convertToType0Bytes(String text) {
        char[] chars = text.toCharArray();
        int len = chars.length;
        byte[] b = new byte[len * 2];
        int bptr = 0;
        for (char aChar : chars) {
            int[] ws = metrics.get(Character.getNumericValue(aChar));
            if (ws != null) {
                int g = ws[0];
                b[bptr++] = (byte) (g / 256);
                b[bptr++] = (byte) (g);
            }
        }
        return trimByteArray(b, bptr);
    }

    private byte[] convertToStandardBytes(String text) {
        char[] cc = text.toCharArray();
        byte[] b = new byte[cc.length];
        int ptr = 0;
        for (char c : cc) {
            if (uni2byte.containsKey(c)) {
                b[ptr++] = (byte) uni2byte.get(c);
            }
        }
        return trimByteArray(b, ptr);
    }

    private byte[] trimByteArray(byte[] b, int length) {
        if (length == b.length) {
            return b;
        } else {
            byte[] trimmedArray = new byte[length];
            System.arraycopy(b, 0, trimmedArray, 0, length);
            return trimmedArray;
        }
    }

    @Override
    byte[] convertToBytes(int char1) {
        if (cjkMirror != null) {
            return PdfEncodings.convertToBytes((char) char1, CJKFont.CJK_ENCODING);
        } else if (isType0) {
            int[] ws = metrics.get(char1);
            if (ws != null) {
                int g = ws[0];
                return new byte[]{(byte) (g / 256), (byte) (g)};
            } else {
                return new byte[0];
            }
        } else {
            if (uni2byte.containsKey(char1)) {
                return new byte[]{(byte) uni2byte.get(char1)};
            } else {
                return new byte[0];
            }
        }
    }

    PdfIndirectReference getIndirectReference() {
        return refFont;
    }

    @Override
    public boolean charExists(int c) {
        if (cjkMirror != null) {
            return cjkMirror.charExists(c);
        } else if (isType0) {
            return metrics.containsKey(c);
        } else {
            return super.charExists(c);
        }
    }

    public boolean setKerning(int char1, int char2, int kern) {
        return true;
    }

    @Override
    public int[] getCharBBox(int c) {
        return new int[0];
    }

    @Override
    protected int[] getRawCharBBox(int c, String name) {
        return new int[0];
    }

    /**
     * Exposes the unicode - > CID map that is constructed from the font's encoding
     *
     * @return the unicode to CID map
     * @since 2.1.7
     */
    IntHashtable getUni2Byte() {
        return uni2byte;
    }

    public PdfDictionary getFontDescriptor() {
        return this.font.getAsDict(PdfName.FONTDESCRIPTOR);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DocumentFont [");
        if (metrics != null) {
            builder.append("metrics=");
            builder.append(metrics);
            builder.append(", ");
        }
        if (fontName != null) {
            builder.append("fontName=");
            builder.append(fontName);
            builder.append(", ");
        }
        if (refFont != null) {
            builder.append("refFont=");
            builder.append(refFont);
            builder.append(", ");
        }
        if (font != null) {
            builder.append("font=");
            builder.append(font);
            builder.append(", ");
        }
        if (uni2byte != null) {
            builder.append("uni2byte size=");
            builder.append(uni2byte.size());
            builder.append(", ");
        }
        if (diffmap != null) {
            builder.append("diffmap=");
            builder.append(diffmap);
            builder.append(", ");
        }
        builder.append("Ascender=");
        builder.append(ascender);
        builder.append(", CapHeight=");
        builder.append(capHeightVar);
        builder.append(", Descender=");
        builder.append(descender);
        builder.append(", ItalicAngle=");
        builder.append(italicAngleVar);
        builder.append(", llx=");
        builder.append(llx);
        builder.append(", lly=");
        builder.append(lly);
        builder.append(", urx=");
        builder.append(urx);
        builder.append(", ury=");
        builder.append(ury);
        builder.append(", isType0=");
        builder.append(isType0);
        builder.append(", ");
        if (cjkMirror != null) {
            builder.append("cjkMirror=");
            builder.append(cjkMirror);
        }
        builder.append("]");
        return builder.toString();
    }
}
