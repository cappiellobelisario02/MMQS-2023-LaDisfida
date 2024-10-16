/*
 * $Id: Type1Font.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2001-2006 Paulo Soares
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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.pdf.fonts.FontsResourceAnchor;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * Reads a Type1 font
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
class Type1Font extends BaseFont {

    /**
     * Types of records in a PFB file. ASCII is 1 and BINARY is 2. They have to appear in the PFB file in this
     * sequence.
     */
    private static final int[] PFB_TYPES = {1, 2, 1};
    public static final String NOTDEF1 = ".notdef";
    public static final String UNEXPECTED_IDENTIFIER = "Unexpected identifier: ";
    public static final String CP_1252 = "Cp1252";
    private static FontsResourceAnchor resourceAnchor;
    private float italicAngleVar;
    private float capHeightVar;
    private static final Logger logger = Logger.getLogger(Type1Font.class.getName());

    /**
     * The PFB file if the input was made with a <CODE>byte</CODE> array.
     */
    protected byte[] pfb;
    /**
     * The Postscript font getName.
     */
    private String fontName;
    /**
     * The full getName of the font.
     */
    private String fullName;
    /**
     * The family getName of the font.
     */
    private String familyName;
    /**
     * The weight of the font: normal, bold, etc.
     */
    private String weight = "";
    /**
     * The italic angle of the font, usually 0.0 or negative.
     */
    private static final int ITALICANGLE_CONST = 0;
    /**
     * <CODE>true</CODE> if all the characters have the same
     * width.
     */
    private boolean isFixedPitch = false;
    /**
     * The llx of the FontBox.
     */
    private int llx = -50;
    /**
     * The lly of the FontBox.
     */
    private int lly = -200;
    /**
     * The lurx of the FontBox.
     */
    private int urx = 1000;
    /**
     * The ury of the FontBox.
     */
    private int ury = 900;
    /**
     * The underline position.
     */
    private int underlinePosition = -100;
    /**
     * The underline thickness.
     */
    private int underlineThickness = 50;
    /**
     * The font's encoding getName. This encoding is 'StandardEncoding' or 'AdobeStandardEncoding' for a font that can be
     * totally encoded according to the characters names. For all other names the font is treated as symbolic.
     */
    private String encodingScheme = "FontSpecific";
    /**
     * A variable.
     */
    private static final int CAPHEIGHT_CONST = 700;
    /**
     * A variable.
     */
    private int ascender = 800;
    /**
     * A variable.
     */
    private int descender = -200;

    /**
     * A variable.
     */
    private int stdVW = 80;

    private static final String DELIMITER = "\u00ff";

    /**
     * Represents the section CharMetrics in the AFM file. Each value of this array contains a <CODE>Object[4]</CODE>
     * with an Integer, Integer, String and int[]. This is the code, width, getName and char bbox. The key is the getName of
     * the char and also an Integer with the char number.
     */
    private Map<Object, Object[]> charMetrics = new HashMap<>();
    /**
     * Represents the section KernPairs in the AFM file. The key is the getName of the first character and the value is a
     * <CODE>Object[]</CODE> with 2 elements for each kern pair. Position 0 is the getName of the second character and
     * position 1 is the kerning distance. This is repeated for all the pairs.
     */
    private Map<String, Object[]> kernPairs = new HashMap<>();
    /**
     * The file in use.
     */
    private String fileName;
    /**
     * <CODE>true</CODE> if this font is one of the 14 built in fonts.
     */
    private boolean builtinFont = false;

    /**
     * Creates a new Type1 font.
     *
     * @param ttfAfm  the AFM file if the input is made with a <CODE>byte</CODE> array
     * @param pfb     the PFB file if the input is made with a <CODE>byte</CODE> array
     * @param afmFile the getName of one of the 14 built-in fonts or the location of an AFM file. The file must end in
     *                '.afm'
     * @param enc     the encoding to be applied to this font
     * @param emb     true if the font is to be embedded in the PDF
     * @throws DocumentException the AFM file is invalid
     * @throws IOException       the AFM file could not be read
     * @since 2.1.5
     */
    Type1Font(String afmFile, String enc, boolean emb, byte[] ttfAfm, byte[] pfb, boolean forceRead)
            throws DocumentException, IOException {
        validateInput(emb, ttfAfm, pfb);

        encoding = enc;
        embeddedBool = emb;
        fileName = afmFile;
        fontType = FONT_TYPE_T1;

        if (BuiltinFonts14.containsKey(afmFile)) {
            handleBuiltinFont(afmFile);
        } else {
            processFontFile(afmFile, ttfAfm, forceRead);
        }

        finalizeEncoding();
    }

    private void validateInput(boolean emb, byte[] ttfAfm, byte[] pfb) throws DocumentException {
        if (emb && ttfAfm != null && pfb == null) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("two.byte.arrays.are.needed.if.the.type1.font.is.embedded"));
        }
        if (emb && ttfAfm != null) {
            this.pfb = pfb;
        }
    }

    private void handleBuiltinFont(String afmFile) throws DocumentException, IOException {
        embeddedBool = false;
        builtinFont = true;
        byte[] buf = readResourceFont(afmFile);
        try (RandomAccessFileOrArray rf = new RandomAccessFileOrArray(buf)) {
            process(rf);
        }
    }

    private byte[] readResourceFont(String afmFile) throws DocumentException, IOException {
        try (InputStream is = getResourceStream(RESOURCE_PATH + afmFile + ".afm", resourceAnchor.getClass().getClassLoader());
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            if (is == null) {
                throw new DocumentException("Resource not found: " + afmFile);
            }
            byte[] buf = new byte[1024];
            int size;
            while ((size = is.read(buf)) >= 0) {
                out.write(buf, 0, size);
            }
            return out.toByteArray();
        } catch (IOException e) {
            // Log a generic error message without exposing sensitive information
            logger.warning("Failed to read resource font: " + afmFile + ". Check resource availability.");
            throw e; // Re-throw the exception if necessary
        }
    }

    private void processFontFile(String afmFile, byte[] ttfAfm, boolean forceRead) throws DocumentException, IOException {
        if (afmFile.toLowerCase().endsWith(".afm")) {
            processAfmFile(afmFile, ttfAfm, forceRead);
        } else if (afmFile.toLowerCase().endsWith(".pfm")) {
            processPfmFile(afmFile, ttfAfm);
        } else {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("1.is.not.an.afm.or.pfm.font.file", afmFile));
        }
    }

    private void processAfmFile(String afmFile, byte[] ttfAfm, boolean forceRead) throws DocumentException, IOException {
        try (RandomAccessFileOrArray rf = ttfAfm == null ? new RandomAccessFileOrArray(afmFile, forceRead, Document.plainRandomAccess)
                : new RandomAccessFileOrArray(ttfAfm)) {
            process(rf);
        }
    }

    private void processPfmFile(String afmFile, byte[] ttfAfm) throws DocumentException, IOException {
        try (RandomAccessFileOrArray rf = ttfAfm == null ? new RandomAccessFileOrArray(afmFile, false, Document.plainRandomAccess)
                : new RandomAccessFileOrArray(ttfAfm);
                ByteArrayOutputStream ba = new ByteArrayOutputStream()) {
            Pfm2afm.convert(rf, ba);
            try (RandomAccessFileOrArray newRf = new RandomAccessFileOrArray(ba.toByteArray())) {
                process(newRf);
            }
        }
    }

    private void finalizeEncoding() throws DocumentException {
        encodingScheme = encodingScheme.trim();
        if (encodingScheme.equals("AdobeStandardEncoding") || encodingScheme.equals("StandardEncoding")) {
            fontSpecific = false;
        }
        if (!encoding.startsWith("#")) {
            PdfEncodings.convertToBytes(" ", encoding); // Check if the encoding exists
        }
        createEncoding();
    }


    /**
     * Gets the width from the font according to the <CODE>getName</CODE> or, if the <CODE>getName</CODE> is null, meaning it
     * is a symbolic font, the char <CODE>c</CODE>.
     *
     * @param c    the char if the font is symbolic
     * @param name the glyph getName
     * @return the width of the char
     */
    int getRawWidth(int c, String name) {
        Object[] metrics;
        if (name == null) { // font specific
            metrics = charMetrics.get(c);
        } else {
            if (name.equals(NOTDEF1)) {
                return 0;
            }
            metrics = charMetrics.get(name);
        }
        if (metrics != null) {
            return (Integer) (metrics[1]);
        }
        return 0;
    }

    /**
     * Gets the kerning between two Unicode characters. The characters are converted to names and this names are used to
     * find the kerning pairs in the <CODE>HashMap</CODE> <CODE>KernPairs</CODE>.
     *
     * @param char1 the first char
     * @param char2 the second char
     * @return the kerning to be applied
     */
    public int getKerning(int char1, int char2) {
        String first = GlyphList.unicodeToName(char1);
        if (first == null) {
            return 0;
        }
        String second = GlyphList.unicodeToName(char2);
        if (second == null) {
            return 0;
        }
        Object[] obj = kernPairs.get(first);
        if (obj == null) {
            return 0;
        }
        for (int k = 0; k < obj.length; k += 2) {
            if (second.equals(obj[k]) && obj.length > k + 1) {
                return (Integer) obj[k + 1];
            }
        }
        return 0;
    }


    /**
     * Reads the font metrics
     *
     * @param rf the AFM file
     * @throws DocumentException the AFM file is invalid
     * @throws IOException       the AFM file could not be read
     */
    public void process(RandomAccessFileOrArray rf) throws DocumentException, IOException {
        readFontHeader(rf);
        readCharMetrics(rf);
        readKernPairs(rf);
        rf.close();
    }

    private void readFontHeader(RandomAccessFileOrArray rf) throws DocumentException, IOException {
        String line;
        boolean isMetrics = false;
        String stringToLog;

        while ((line = rf.readLine()) != null) {
            StringTokenizer tok = new StringTokenizer(line, " ,\n\r\t\f");
            if (!tok.hasMoreTokens()) continue;

            String ident = tok.nextToken();
            switch (ident) {
                case "FontName":
                    fontName = tok.nextToken(DELIMITER).substring(1);
                    break;
                case "FullName":
                    fullName = tok.nextToken(DELIMITER).substring(1);
                    break;
                case "FamilyName":
                    familyName = tok.nextToken(DELIMITER).substring(1);
                    break;
                case "Weight":
                    weight = tok.nextToken(DELIMITER).substring(1);
                    break;
                case "ItalicAngle":
                    italicAngleVar = Float.parseFloat(tok.nextToken());
                    break;
                case "IsFixedPitch":
                    isFixedPitch = tok.nextToken().equals("true");
                    break;
                case "FontBBox":
                    parseFontBBox(tok);
                    break;
                case "UnderlinePosition":
                    underlinePosition = (int) Float.parseFloat(tok.nextToken());
                    break;
                case "UnderlineThickness":
                    underlineThickness = (int) Float.parseFloat(tok.nextToken());
                    break;
                case "EncodingScheme":
                    encodingScheme = tok.nextToken(DELIMITER).substring(1);
                    break;
                case "CapHeight":
                    capHeightVar = (int) Float.parseFloat(tok.nextToken());
                    break;
                case "Ascender":
                    ascender = (int) Float.parseFloat(tok.nextToken());
                    break;
                case "Descender":
                    descender = (int) Float.parseFloat(tok.nextToken());
                    break;
                case "StdHW":
                    break;
                case "StdVW":
                    stdVW = (int) Float.parseFloat(tok.nextToken());
                    break;
                case "StartCharMetrics":
                    isMetrics = true;
                    break;
                default:
                    stringToLog = UNEXPECTED_IDENTIFIER + ident;
                    // Log a message or handle the unexpected identifier
                    logger.warning(stringToLog);
                    // Optionally, you can choose to ignore or throw an exception depending on your needs
                    break;
            }

        }

        if (!isMetrics) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("missing.startcharmetrics.in.1", fileName));
        }
    }

    private void parseFontBBox(StringTokenizer tok) {
        llx = (int) Float.parseFloat(tok.nextToken());
        lly = (int) Float.parseFloat(tok.nextToken());
        urx = (int) Float.parseFloat(tok.nextToken());
        ury = (int) Float.parseFloat(tok.nextToken());
    }

    private void readCharMetrics(RandomAccessFileOrArray rf) throws DocumentException, IOException {
        String line;
        boolean isMetrics = true;

        while ((line = rf.readLine()) != null) {
            StringTokenizer tok = new StringTokenizer(line);
            if (!tok.hasMoreTokens()) continue;

            String ident = tok.nextToken();
            if (ident.equals("EndCharMetrics")) {
                isMetrics = false;
                break;
            }

            if (ident.equals("StartCharMetrics")) {
                continue;
            }

            Object[] metrics = parseCharMetrics(line);
            Integer c = (Integer) metrics[0];
            if (c >= 0) {
                charMetrics.put(c, metrics);
            }
            charMetrics.put(metrics[2], metrics);
        }

        if (isMetrics) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("missing.endcharmetrics.in.1", fileName));
        }

        if (!charMetrics.containsKey("nonbreakingspace")) {
            Object[] space = charMetrics.get("space");
            if (space != null) {
                charMetrics.put("nonbreakingspace", space);
            }
        }
    }

    private Object[] parseCharMetrics(String line) {
        StringTokenizer tok = new StringTokenizer(line, ";");
        String stringToLog;
        int c = -1;
        int wx = 250;
        String n = "";
        int[] b = null;

        while (tok.hasMoreTokens()) {
            StringTokenizer tokc = new StringTokenizer(tok.nextToken());
            if (!tokc.hasMoreTokens()) continue;

            String ident = tokc.nextToken();
            switch (ident) {
                case "C":
                    c = Integer.parseInt(tokc.nextToken());
                    break;
                case "WX":
                    wx = (int) Float.parseFloat(tokc.nextToken());
                    break;
                case "N":
                    n = tokc.nextToken();
                    break;
                case "B":
                    b = new int[]{
                            Integer.parseInt(tokc.nextToken()),
                            Integer.parseInt(tokc.nextToken()),
                            Integer.parseInt(tokc.nextToken()),
                            Integer.parseInt(tokc.nextToken())
                    };
                    break;
                default:
                    stringToLog = UNEXPECTED_IDENTIFIER + ident;
                    // Log a message or handle the unexpected identifier
                    logger.warning(stringToLog);
                    // Optionally, you can choose to ignore or throw an exception depending on your needs
                    break;
            }
        }

        return new Object[]{c, wx, n, b};
    }

    private void readKernPairs(RandomAccessFileOrArray rf) throws DocumentException, IOException {
        String line;
        boolean isMetrics = false;

        while ((line = rf.readLine()) != null) {
            StringTokenizer tok = new StringTokenizer(line);
            if (!tok.hasMoreTokens()) continue;

            String ident = tok.nextToken();
            switch (ident) {
                case "EndFontMetrics" -> {
                    return;
                }
                case "StartKernPairs" -> isMetrics = true;
                case "KPX" -> processKernPair(tok);
                default -> throw new IllegalStateException("Unexpected value: " + ident);
            }

        }

        if (isMetrics) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("missing.endkernpairs.in.1", fileName));
        }
    }

    private void processKernPair(StringTokenizer tok) {
        String first = tok.nextToken();
        String second = tok.nextToken();
        int width = Integer.parseInt(tok.nextToken());

        Object[] relates = kernPairs.get(first);
        if (relates == null) {
            kernPairs.put(first, new Object[]{second, width});
        } else {
            int n = relates.length;
            Object[] relates2 = new Object[n + 2];
            System.arraycopy(relates, 0, relates2, 0, n);
            relates2[n] = second;
            relates2[n + 1] = width;
            kernPairs.put(first, relates2);
        }
    }


    /**
     * If the embedded flag is <CODE>false</CODE> or if the font is one of the 14 built in types, it returns
     * <CODE>null</CODE>, otherwise the font is read and output in a PdfStream object.
     *
     * @return the PdfStream containing the font or <CODE>null</CODE>
     * @throws DocumentException if there is an error reading the font
     * @since 2.1.3
     */
    public PdfStream getFullFontStream() throws DocumentException {
        if (builtinFont || !embeddedBool) {
            return null;
        }

        try (RandomAccessFileOrArray rf = createRandomAccessFileOrArray()) {
            byte[] fontData = readFontData(rf);
            int[] lengths = extractSegmentLengths(rf);
            return new StreamFont(fontData, lengths, compressionLevel);
        } catch (Exception e) {
            throw new DocumentException(e);
        }
    }

    private RandomAccessFileOrArray createRandomAccessFileOrArray() throws IOException {
        String filePfb = fileName.substring(0, fileName.length() - 3) + "pfb";
        if (pfb == null) {
            return new RandomAccessFileOrArray(filePfb, true, Document.plainRandomAccess);
        } else {
            return new RandomAccessFileOrArray(pfb);
        }
    }

    private byte[] readFontData(RandomAccessFileOrArray rf) throws IOException, DocumentException {
        int fileLength = rf.length();
        byte[] st = new byte[fileLength - 18];
        int bytePtr = 0;

        for (int k = 0; k < 3; ++k) {
            validateSegment(rf, k);
            int size = readSegmentSize(rf);
            readSegmentData(rf, st, bytePtr, size);
            bytePtr += size;
        }

        return st;
    }

    private void validateSegment(RandomAccessFileOrArray rf, int segmentIndex) throws IOException, DocumentException {
        if (rf.read() != 0x80 || rf.read() != PFB_TYPES[segmentIndex]) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("incorrect.segment.getTypeImpl.in.1", fileName));
        }
    }

    private int readSegmentSize(RandomAccessFileOrArray rf) throws IOException {
        int size = rf.read();
        size += rf.read() << 8;
        size += rf.read() << 16;
        size += rf.read() << 24;
        return size;
    }

    private void readSegmentData(RandomAccessFileOrArray rf, byte[] buffer, int offset, int size) throws IOException, DocumentException {
        while (size > 0) {
            int readBytes = rf.read(buffer, offset, size);
            if (readBytes < 0) {
                throw new DocumentException(
                        MessageLocalization.getComposedMessage("premature.end.in.1", fileName));
            }
            size -= readBytes;
        }
    }

    private int[] extractSegmentLengths(RandomAccessFileOrArray rf) throws IOException, DocumentException {
        int[] lengths = new int[3];
        for (int k = 0; k < 3; ++k) {
            validateSegment(rf, k);
            lengths[k] = readSegmentSize(rf);
        }
        return lengths;
    }


    /**
     * Generates the font descriptor for this font or <CODE>null</CODE> if it is one of the 14 built in fonts.
     *
     * @param fontStream the indirect reference to a PdfStream containing the font or <CODE>null</CODE>
     * @return the PdfDictionary containing the font descriptor or <CODE>null</CODE>
     */
    private PdfDictionary getFontDescriptor(PdfIndirectReference fontStream) {
        if (builtinFont) {
            return null;
        }
        PdfDictionary dic = new PdfDictionary(PdfName.FONTDESCRIPTOR);
        dic.put(PdfName.ASCENT, new PdfNumber(ascender));
        dic.put(PdfName.CAPHEIGHT, new PdfNumber(capHeightVar));
        dic.put(PdfName.DESCENT, new PdfNumber(descender));
        dic.put(PdfName.FONTBBOX, new PdfRectangle(llx, lly, urx, ury));
        dic.put(PdfName.FONTNAME, new PdfName(fontName));
        dic.put(PdfName.ITALICANGLE, new PdfNumber(italicAngleVar));
        dic.put(PdfName.STEMV, new PdfNumber(stdVW));
        if (fontStream != null) {
            dic.put(PdfName.FONTFILE, fontStream);
        }
        int flags = 0;
        if (isFixedPitch) {
            flags |= 1;
        }
        flags |= fontSpecific ? 4 : 32;
        if (italicAngleVar < 0) {
            flags |= 64;
        }
        if (fontName.contains("Caps") || fontName.endsWith("SC")) {
            flags |= 131072;
        }
        if (weight.equals("Bold")) {
            flags |= 262144;
        }
        dic.put(PdfName.FLAGS, new PdfNumber(flags));

        return dic;
    }

    /**
     * Generates the font dictionary for this font.
     *
     * @param firstChar      the first valid character
     * @param lastChar       the last valid character
     * @param shortTag       a 256 bytes long <CODE>byte</CODE> array where each unused byte is represented by 0
     * @param fontDescriptor the indirect reference to a PdfDictionary containing the font descriptor or
     *                       <CODE>null</CODE>
     * @return the PdfDictionary containing the font dictionary
     */
    private PdfDictionary getFontBaseType(PdfIndirectReference fontDescriptor, int firstChar, int lastChar, byte[] shortTag) {
        PdfDictionary dic = new PdfDictionary(PdfName.FONT);
        dic.put(PdfName.SUBTYPE, PdfName.TYPE1);
        dic.put(PdfName.BASEFONT, new PdfName(fontName));

        boolean stdEncoding = isStandardEncoding();
        if (!fontSpecific || specialMap != null) {
            firstChar = adjustFirstChar(firstChar, lastChar);
            dic.put(PdfName.ENCODING, getEncoding(stdEncoding, firstChar, lastChar, shortTag));
        }

        if (shouldIncludeWidthAndCharInfo()) {
            addWidthAndCharInfo(dic, firstChar, lastChar, shortTag);
        }

        if (!builtinFont && fontDescriptor != null) {
            dic.put(PdfName.FONTDESCRIPTOR, fontDescriptor);
        }

        return dic;
    }

    private boolean isStandardEncoding() {
        return encoding.equals(CP_1252) || encoding.equals("MacRoman");
    }

    private int adjustFirstChar(int firstChar, int lastChar) {
        for (int k = firstChar; k <= lastChar; ++k) {
            if (!differences[k].equals(NOTDEF1)) {
                return k;
            }
        }
        return firstChar; // Fallback in case no valid character is found
    }

    private PdfDictionary getEncoding(boolean stdEncoding, int firstChar, int lastChar, byte[] shortTag) {
        if (stdEncoding && encoding.equals(CP_1252)) {
            return new PdfDictionary(PdfName.WIN_ANSI_ENCODING);
        }
        else if(!encoding.equals(CP_1252)){
            return new PdfDictionary(PdfName.MAC_ROMAN_ENCODING);
        }
        else {
            PdfDictionary enc = new PdfDictionary(PdfName.ENCODING);
            enc.put(PdfName.DIFFERENCES, getDifferencesArray(firstChar, lastChar, shortTag));
            return enc;
        }
    }

    private PdfArray getDifferencesArray(int firstChar, int lastChar, byte[] shortTag) {
        PdfArray dif = new PdfArray();
        boolean gap = true;
        for (int k = firstChar; k <= lastChar; ++k) {
            if (shortTag[k] != 0) {
                if (gap) {
                    dif.add(new PdfNumber(k));
                    gap = false;
                }
                dif.add(new PdfName(differences[k]));
            } else {
                gap = true;
            }
        }
        return dif;
    }

    private boolean shouldIncludeWidthAndCharInfo() {
        return specialMap != null || forceWidthsOutput || !(builtinFont && (fontSpecific || isStandardEncoding()));
    }

    private void addWidthAndCharInfo(PdfDictionary dic, int firstChar, int lastChar, byte[] shortTag) {
        dic.put(PdfName.FIRSTCHAR, new PdfNumber(firstChar));
        dic.put(PdfName.LASTCHAR, new PdfNumber(lastChar));
        dic.put(PdfName.WIDTHS, getWidthsArray(firstChar, lastChar, shortTag));
    }

    private PdfArray getWidthsArray(int firstChar, int lastChar, byte[] shortTag) {
        PdfArray wd = new PdfArray();
        for (int k = firstChar; k <= lastChar; ++k) {
            wd.add(new PdfNumber(shortTag[k] == 0 ? 0 : widths[k]));
        }
        return wd;
    }


    /**
     * Outputs to the writer the font dictionaries and streams.
     *
     * @param writer the writer for this document
     * @param ref    the font indirect reference
     * @param params several parameters that depend on the font getTypeImpl
     * @throws IOException       on error
     * @throws DocumentException error in generating the object
     */
    void writeFont(PdfWriter writer, PdfIndirectReference ref, Object[] params) throws DocumentException, IOException {
        int firstChar = (Integer) params[0];
        int lastChar = (Integer) params[1];
        byte[] shortTag = (byte[]) params[2];
        boolean subsetp = (Boolean) params[3] && subset;
        if (!subsetp) {
            firstChar = 0;
            lastChar = shortTag.length - 1;
            for (int k = 0; k < shortTag.length; ++k) {
                shortTag[k] = 1;
            }
        }
        PdfIndirectReference indFont = null;
        PdfObject pobj = null;
        PdfIndirectObject obj = null;
        pobj = getFullFontStream();
        if (pobj != null) {
            obj = writer.addToBody(pobj);
            indFont = obj.getIndirectReference();
        }
        pobj = getFontDescriptor(indFont);
        if (pobj != null) {
            obj = writer.addToBody(pobj);
            indFont = obj.getIndirectReference();
        }
        pobj = getFontBaseType(indFont, firstChar, lastChar, shortTag);
        writer.addToBody(pobj, ref);
    }

    /**
     * Gets the font parameter identified by <CODE>key</CODE>. Valid values for <CODE>key</CODE> are
     * <CODE>ASCENT</CODE>, <CODE>CAPHEIGHT</CODE>, <CODE>DESCENT</CODE>,
     * <CODE>ITALICANGLE</CODE>, <CODE>BBOXLLX</CODE>, <CODE>BBOXLLY</CODE>, <CODE>BBOXURX</CODE>
     * and <CODE>BBOXURY</CODE>.
     *
     * @param key      the parameter to be extracted
     * @param fontSize the font size in points
     * @return the parameter in points
     */
    public float getFontDescriptor(int key, float fontSize) {
        switch (key) {
            case AWT_ASCENT, ASCENT:
                return ascender * fontSize / 1000;
            case CAPHEIGHT_CONST:
                return capHeightVar * fontSize / 1000;
            case AWT_DESCENT, DESCENT:
                return descender * fontSize / 1000;
            case ITALICANGLE_CONST:
                return italicAngleVar;
            case BBOXLLX:
                return llx * fontSize / 1000;
            case BBOXLLY:
                return lly * fontSize / 1000;
            case BBOXURX:
                return urx * fontSize / 1000;
            case BBOXURY:
                return ury * fontSize / 1000;
            case AWT_LEADING:
                return 0;
            case AWT_MAXADVANCE:
                return (urx - llx) * fontSize / 1000;
            case UNDERLINE_POSITION:
                return underlinePosition * fontSize / 1000;
            case UNDERLINE_THICKNESS:
                return underlineThickness * fontSize / 1000;
            default:
                String stringToLog = UNEXPECTED_IDENTIFIER + key;
                // Log a message or handle the unexpected identifier
                logger.warning(stringToLog);
                // Optionally, you can choose to ignore or throw an exception depending on your needs
                break;
        }
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
     * Sets the font getName that will appear in the pdf font dictionary. Use with care as it can easily make a font
     * unreadable if not embedded.
     *
     * @param name the new font getName
     */
    public void setPostscriptFontName(String name) {
        fontName = name;
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
        return new String[][]{{"", "", "", fullName}};
    }

    /**
     * Gets all the entries of the names-table. If it is a True Type font each array element will have {Name ID,
     * Platform ID, Platform Encoding ID, Language ID, font getName}. The interpretation of this values can be found in the
     * Open Type specification, chapter 2, in the 'getName' table.<br> For the other fonts the array has a single element
     * with {"4", "", "", "", font getName}.
     *
     * @return the full getName of the font
     */
    public String[][] getAllNameEntries() {
        return new String[][]{{"4", "", "", "", fullName}};
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
        return new String[][]{{"", "", "", familyName}};
    }

    /**
     * Checks if the font has any kerning pairs.
     *
     * @return <CODE>true</CODE> if the font has any kerning pairs
     */
    public boolean hasKernPairs() {
        return !kernPairs.isEmpty();
    }

    /**
     * Sets the kerning between two Unicode chars.
     *
     * @param char1 the first char
     * @param char2 the second char
     * @param kern  the kerning to apply in normalized 1000 units
     * @return <code>true</code> if the kerning was applied, <code>false</code> otherwise
     */
    public boolean setKerning(int char1, int char2, int kern) {
        String first = GlyphList.unicodeToName(char1);
        if (first == null) {
            return false;
        }
        String second = GlyphList.unicodeToName(char2);
        if (second == null) {
            return false;
        }
        Object[] obj = kernPairs.get(first);
        if (obj == null) {
            obj = new Object[]{second, kern};
            kernPairs.put(first, obj);
            return true;
        }
        for (int k = 0; k < obj.length; k += 2) {
            if (second.equals(obj[k]) && obj.length > k + 1) {
                obj[k + 1] = kern;
                return true;
            }
        }
        int size = obj.length;
        Object[] obj2 = new Object[size + 2];
        System.arraycopy(obj, 0, obj2, 0, size);
        obj2[size] = second;
        obj2[size + 1] = kern;
        kernPairs.put(first, obj2);
        return true;
    }

    protected int[] getRawCharBBox(int c, String name) {
        Object[] metrics;
        if (name == null) { // font specific
            metrics = charMetrics.get(c);
        } else {
            if (name.equals(NOTDEF1)) {
                return new int[]{};
            }
            metrics = charMetrics.get(name);
        }
        if (metrics != null) {
            return ((int[]) (metrics[3]));
        }
        return new int[]{};
    }

}
