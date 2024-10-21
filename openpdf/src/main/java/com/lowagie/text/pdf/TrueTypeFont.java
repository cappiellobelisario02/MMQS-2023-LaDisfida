/*
 * $Id: TrueTypeFont.java 4066 2009-09-19 12:44:47Z psoares33 $
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
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.error_messages.MessageLocalization;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;

import static com.ibm.icu.util.ULocale.getBaseName;
import static com.lowagie.text.pdf.PdfWriter.logger;


/**
 * Reads a Truetype font
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
class TrueTypeFont extends BaseFont {

    /**
     * The code pages possible for a True Type font.
     */
    static final String[] codePages = {
            "1252 Latin 1",
            "1250 Latin 2: Eastern Europe",
            "1251 Cyrillic",
            "1253 Greek",
            "1254 Turkish",
            "1255 Hebrew",
            "1256 Arabic",
            "1257 Windows Baltic",
            "1258 Vietnamese",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "874 Thai",
            "932 JIS/Japan",
            "936 Chinese: Simplified chars--PRC and Singapore",
            "949 Korean Wansung",
            "950 Chinese: Traditional chars--Taiwan and Hong Kong",
            "1361 Korean Johab",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "Macintosh Character Set (US Roman)",
            "OEM Character Set",
            "Symbol Character Set",
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            "869 IBM Greek",
            "866 MS-DOS Russian",
            "865 MS-DOS Nordic",
            "864 Arabic",
            "863 MS-DOS Canadian French",
            "862 Hebrew",
            "861 MS-DOS Icelandic",
            "860 MS-DOS Portuguese",
            "857 IBM Turkish",
            "855 IBM Cyrillic; primarily Russian",
            "852 Latin 2",
            "775 MS-DOS Baltic",
            "737 Greek; former 437 G",
            "708 Arabic; ASMO 708",
            "850 WE/Latin 1",
            "437 US"};

    protected boolean justNames = false;
    /**
     * Contains the location of the several tables. The key is the getName of the table and the value is an
     * <CODE>int[2]</CODE> where position 0 is the offset from the start of the file and position 1 is the length of
     * the table.
     */
    protected HashMap<String, int[]> tables;
    /**
     * The file in use.
     */
    protected RandomAccessFileOrArray rf;
    /**
     * The file getName.
     */
    protected String fileName;

    protected boolean cff = false;

    protected int cffOffset;

    protected int cffLength;

    /**
     * The offset from the start of the file to the table directory. It is 0 for TTF and may vary for TTC depending on
     * the chosen font.
     */
    protected int directoryOffset;
    /**
     * The index for the TTC font. It is an empty <CODE>String</CODE> for a TTF file.
     */
    protected String ttcIndex;
    /**
     * The style modifier
     */
    protected String style = "";
    /**
     * The content of table 'head'.
     */
    protected com.lowagie.text.pdf.TrueTypeFont.FontHeader head = new com.lowagie.text.pdf.TrueTypeFont.FontHeader();
    /**
     * The content of table 'hhea'.
     */
    protected com.lowagie.text.pdf.TrueTypeFont.HorizontalHeader hhea = new com.lowagie.text.pdf.TrueTypeFont.HorizontalHeader();
    /**
     * The content of table 'OS/2'.
     */
    protected com.lowagie.text.pdf.TrueTypeFont.WindowsMetrics os2 = new com.lowagie.text.pdf.TrueTypeFont.WindowsMetrics();
    /**
     * The width of the glyphs. This is essentially the content of table 'hmtx' normalized to 1000 units.
     */
    protected int[] glyphWidths;

    protected int[][] bboxes;
    /**
     * The map containing the code information for the table 'cmap', encoding 1.0. The key is the code and the value is
     * an <CODE>int[2]</CODE> where position 0 is the glyph number and position 1 is the glyph width normalized to 1000
     * units.
     */
    protected HashMap<Integer, int[]> cmap10;
    /**
     * The map containing the code information for the table 'cmap', encoding 3.1 in Unicode.
     * <p>
     * The key is the code and the value is an <CODE>int</CODE>[2] where position 0 is the glyph number and position 1
     * is the glyph width normalized to 1000 units.
     */
    protected HashMap<Integer, int[]> cmap31;

    protected HashMap<Integer, int[]> cmapExt;

    /**
     * The map containing the kerning information. It represents the content of table 'kern'. The key is an
     * <CODE>Integer</CODE> where the top 16 bits are the glyph number for the first character and the lower 16 bits
     * are the glyph number for the second character. The value is the amount of kerning in normalized 1000 units as an
     * <CODE>Integer</CODE>. This value is usually negative.
     */
    protected IntHashtable kerning = new IntHashtable();
    /**
     * The font getName. This getName is usually extracted from the table 'getName' with the 'Name ID' 6.
     */
    protected String fontName;

    /**
     * The full getName of the font
     */
    protected String[][] fullName;

    /**
     * All the names of the Names-Table
     */
    protected String[][] allNameEntries;

    /**
     * The family getName of the font
     */
    protected String[][] familyName;
    /**
     * <CODE>true</CODE> if all the glyphs have the same width.
     */
    protected boolean isFixedPitch = false;

    protected int underlinePosition;

    protected int underlineThickness;

    /**
     * This constructor is present to allow extending the class.
     */
    protected TrueTypeFont() {
    }

    /**
     * Creates a new TrueType font.
     *
     * @param ttFile the location of the font on file. The file must end in '.ttf' or '.ttc' but can have modifiers
     *               after the getName
     * @param enc    the encoding to be applied to this font
     * @param emb    true if the font is to be embedded in the PDF
     * @param ttfAfm the font as a <CODE>byte</CODE> array
     * @throws DocumentException the font is invalid
     * @throws IOException       the font file could not be read
     * @since 2.1.5
     */
    TrueTypeFont(String ttFile, String enc, boolean emb, byte[] ttfAfm, boolean justNames, boolean forceRead)
            throws DocumentException, IOException {
        this.justNames = justNames;
        String nameBase = getBaseName(ttFile);
        String ttcName = getTTCName(nameBase);

        if (nameBase.length() < ttFile.length()) {
            style = ttFile.substring(nameBase.length());
        }

        encoding = enc;
        embeddedBool = emb;
        fileName = ttcName;
        fontType = FONT_TYPE_TT;
        ttcIndex = "";

        if (ttcName.length() < nameBase.length()) {
            ttcIndex = nameBase.substring(ttcName.length() + 1);
        }

        validateFontFile(ttfAfm, forceRead);
        checkEncoding(enc);
        createEncoding();
    }

    private void validateFontFile(byte[] ttfAfm, boolean forceRead) throws DocumentException, IOException {
        // Check file getTypeImpl and throw a more generic exception if invalid
        if (fileName.toLowerCase().endsWith(".ttf") || fileName.toLowerCase().endsWith(".otf") || fileName.toLowerCase().endsWith(".ttc")) {
            process(ttfAfm, forceRead);

            // Check for licensing restrictions with a generic message
            if (!justNames && embeddedBool && os2.fsType == 2) {
                throw new DocumentException(
                        MessageLocalization.getComposedMessage("font.cannot.be.embedded.due.to.licensing.restrictions"));
            }
        } else {
            // Throw a more generic exception without revealing sensitive details
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("the.file.provided.is.not.a.valid.font.file"));
        }
    }

    private void checkEncoding(String enc) throws DocumentException {
        if (!encoding.startsWith("#")) {
            PdfEncodings.convertToBytes(" ", enc); // check if the encoding exists
        }
    }



    /**
     * Gets the getName from a composed TTC file getName. If I have for input "myfont.ttc,2" the return will be "myfont.ttc".
     *
     * @param name the full getName
     * @return the simple file getName
     */
    protected static String getTTCName(String name) {
        int idx = name.toLowerCase().indexOf(".ttc,");
        if (idx < 0) {
            return name;
        } else {
            return name.substring(0, idx + 4);
        }
    }

    protected static int[] compactRanges(ArrayList<?> ranges) {
        List<int[]> simp = simplifyRanges(ranges);
        mergeOverlappingRanges(simp);
        return convertToArray(simp);
    }

    private static List<int[]> simplifyRanges(ArrayList<?> ranges) {
        List<int[]> simp = new ArrayList<>();
        for (Object range : ranges) {
            int[] r = (int[]) range;
            for (int j = 0; j < r.length - 1; j += 2) {
                simp.add(new int[]{Math.max(0, Math.min(r[j], r[j + 1])),
                        Math.min(0xffff, Math.max(r[j], r[j + 1]))});
            }
        }
        return simp;
    }

    private static void mergeOverlappingRanges(List<int[]> simp) {
        for (int k1 = 0; k1 < simp.size() - 1; ++k1) {
            int[] r1 = simp.get(k1);
            ListIterator<int[]> iter = simp.listIterator(simp.size()); // start from the end of the list
            while (iter.hasPrevious()) {
                int[] r2 = iter.previous();
                int k2 = simp.indexOf(r2); // get the index of the element
                if (k2 > k1 && rangesOverlap(r1, r2)) {
                    r1[0] = Math.min(r1[0], r2[0]);
                    r1[1] = Math.max(r1[1], r2[1]);
                    iter.remove(); // remove the element safely
                }
            }
        }
    }



    private static boolean rangesOverlap(int[] r1, int[] r2) {
        return (r1[0] >= r2[0] && r1[0] <= r2[1]) || (r1[1] >= r2[0] && r1[0] <= r2[1]);
    }

    private static int[] convertToArray(List<int[]> simp) {
        int[] s = new int[simp.size() * 2];
        for (int k = 0; k < simp.size(); ++k) {
            int[] r = simp.get(k);
            s[k * 2] = r[0];
            s[k * 2 + 1] = r[1];
        }
        return s;
    }


    /**
     * Reads the tables 'head', 'hhea', 'OS/2' and 'post' filling several variables.
     *
     */
    private static final String TABLE_NOT_EXIST_MESSAGE = "table.1.does.not.exist.in.2";

    void fillTables() throws DocumentException, IOException {
        int[] tableLocation = tables.get("head");
        if (tableLocation == null) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage(TABLE_NOT_EXIST_MESSAGE, "head", fileName + style));
        }
        rf.seek(tableLocation[0] + 16);
        head.flags = rf.readUnsignedShort();
        head.unitsPerEm = rf.readUnsignedShort();
        rf.skipBytes(16);
        head.xMin = rf.readShort();
        head.yMin = rf.readShort();
        head.xMax = rf.readShort();
        head.yMax = rf.readShort();
        head.macStyle = rf.readUnsignedShort();

        tableLocation = tables.get("hhea");
        if (tableLocation == null) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage(TABLE_NOT_EXIST_MESSAGE, "hhea", fileName + style));
        }
        rf.seek(tableLocation[0] + 4);
        hhea.ascender = rf.readShort();
        hhea.descender = rf.readShort();
        hhea.lineGap = rf.readShort();
        hhea.advanceWidthMax = rf.readUnsignedShort();
        hhea.minLeftSideBearing = rf.readShort();
        hhea.minRightSideBearing = rf.readShort();
        hhea.xMaxExtent = rf.readShort();
        hhea.caretSlopeRise = rf.readShort();
        hhea.caretSlopeRun = rf.readShort();
        rf.skipBytes(12);
        hhea.numberOfHMetrics = rf.readUnsignedShort();

        tableLocation = tables.get("OS/2");
        if (tableLocation == null) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage(TABLE_NOT_EXIST_MESSAGE, "OS/2", fileName + style));
        }
        rf.seek(tableLocation[0]);
        int version = rf.readUnsignedShort();
        os2.xAvgCharWidth = rf.readShort();
        os2.usWeightClass = rf.readUnsignedShort();
        os2.usWidthClass = rf.readUnsignedShort();
        os2.fsType = rf.readShort();
        os2.ySubscriptXSize = rf.readShort();
        os2.ySubscriptYSize = rf.readShort();
        os2.ySubscriptXOffset = rf.readShort();
        os2.ySubscriptYOffset = rf.readShort();
        os2.ySuperscriptXSize = rf.readShort();
        os2.ySuperscriptYSize = rf.readShort();
        os2.ySuperscriptXOffset = rf.readShort();
        os2.ySuperscriptYOffset = rf.readShort();
        os2.yStrikeoutSize = rf.readShort();
        os2.yStrikeoutPosition = rf.readShort();
        os2.sFamilyClass = rf.readShort();
        rf.readFully(os2.panose);
        rf.skipBytes(16);
        rf.readFully(os2.achVendID);
        os2.fsSelection = rf.readUnsignedShort();
        os2.usFirstCharIndex = rf.readUnsignedShort();
        os2.usLastCharIndex = rf.readUnsignedShort();
        os2.sTypoAscender = rf.readShort();
        os2.sTypoDescender = rf.readShort();
        if (os2.sTypoDescender > 0) {
            os2.sTypoDescender = (short) (-os2.sTypoDescender);
        }
        os2.sTypoLineGap = rf.readShort();
        os2.usWinAscent = rf.readUnsignedShort();
        os2.usWinDescent = rf.readUnsignedShort();
        os2.ulCodePageRange1 = 0;
        os2.ulCodePageRange2 = 0;
        if (version > 0) {
            os2.ulCodePageRange1 = rf.readInt();
            os2.ulCodePageRange2 = rf.readInt();
        }
        if (version > 1) {
            rf.skipBytes(2);
            os2.sCapHeight = rf.readShort();
        } else {
            os2.sCapHeight = (int) (0.7 * head.unitsPerEm);
        }

        tableLocation = tables.get("post");
        if (tableLocation == null) {
            return;
        }
        rf.seek(tableLocation[0] + 4);
        underlinePosition = rf.readShort();
        underlineThickness = rf.readShort();
        isFixedPitch = rf.readInt() != 0;
    }

    /**
     * Gets the Postscript font getName.
     *
     * @return the Postscript font getName
     * @throws DocumentException the font is invalid
     * @throws IOException       the font file could not be read
     */
    String getBaseFont() throws DocumentException, IOException {
        int[] tableLocation;
        tableLocation = tables.get("name");
        if (tableLocation == null) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage(TABLE_NOT_EXIST_MESSAGE, "name", "a file with the specified style"));
        }
        rf.seek(tableLocation[0] + 2);
        int numRecords = rf.readUnsignedShort();
        int startOfStorage = rf.readUnsignedShort();
        for (int k = 0; k < numRecords; ++k) {
            int platformID = rf.readUnsignedShort();
            int nameID = rf.readUnsignedShort();
            int length = rf.readUnsignedShort();
            int offset = rf.readUnsignedShort();
            if (nameID == 6) {
                rf.seek(tableLocation[0] + startOfStorage + offset);
                if (platformID == 0 || platformID == 3) {
                    return readUnicodeString(length);
                } else {
                    return readStandardString(length);
                }
            }
        }
        // Modifica qui per utilizzare File.separator
        File file = new File(fileName);
        return file.getName().replace(' ', '-').replace(File.separator, "-");
    }

    /**
     * Extracts the names of the font in all the languages available.
     *
     * @param id the getName id to retrieve
     * @throws DocumentException on error
     * @throws IOException       on error
     */
    String[][] getNames(int id) throws DocumentException, IOException {
        int[] tableLocation;
        tableLocation = tables.get("name");
        if (tableLocation == null) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage(TABLE_NOT_EXIST_MESSAGE, "name", fileName + style));
        }
        rf.seek(tableLocation[0] + 2);
        int numRecords = rf.readUnsignedShort();
        int startOfStorage = rf.readUnsignedShort();
        List<String[]> names = new ArrayList<>();
        for (int k = 0; k < numRecords; ++k) {
            int platformID = rf.readUnsignedShort();
            int platformEncodingID = rf.readUnsignedShort();
            int languageID = rf.readUnsignedShort();
            int nameID = rf.readUnsignedShort();
            int length = rf.readUnsignedShort();
            int offset = rf.readUnsignedShort();
            if (nameID == id) {
                int pos = rf.getFilePointer();
                rf.seek(tableLocation[0] + startOfStorage + offset);
                String name;
                if (platformID == 0 || platformID == 3 || (platformID == 2 && platformEncodingID == 1)) {
                    name = readUnicodeString(length);
                } else {
                    name = readStandardString(length);
                }
                names.add(new String[]{String.valueOf(platformID),
                        String.valueOf(platformEncodingID), String.valueOf(languageID), name});
                rf.seek(pos);
            }
        }
        String[][] thisName = new String[names.size()][];
        for (int k = 0; k < names.size(); ++k) {
            thisName[k] = names.get(k);
        }
        return thisName;
    }

    /**
     * Extracts all the names of the names-Table
     *
     * @throws DocumentException on error
     * @throws IOException       on error
     */
    String[][] getAllNames() throws DocumentException, IOException {
        int[] tableLocation = tables.get("name");
        Objects.requireNonNull(tableLocation,
                MessageLocalization.getComposedMessage(TABLE_NOT_EXIST_MESSAGE, "name", fileName + style));
        rf.seek(tableLocation[0] + 2);
        int numRecords = rf.readUnsignedShort();
        int startOfStorage = rf.readUnsignedShort();
        List<String[]> names = new ArrayList<>();
        for (int k = 0; k < numRecords; ++k) {
            int platformID = rf.readUnsignedShort();
            int platformEncodingID = rf.readUnsignedShort();
            int languageID = rf.readUnsignedShort();
            int nameID = rf.readUnsignedShort();
            int length = rf.readUnsignedShort();
            int offset = rf.readUnsignedShort();
            int pos = rf.getFilePointer();
            rf.seek(tableLocation[0] + startOfStorage + offset);
            String name;
            if (platformID == 0 || platformID == 3 || (platformID == 2 && platformEncodingID == 1)) {
                name = readUnicodeString(length);
            } else {
                name = readStandardString(length);
            }
            names.add(new String[]{String.valueOf(nameID), String.valueOf(platformID),
                    String.valueOf(platformEncodingID), String.valueOf(languageID), name});
            rf.seek(pos);
        }
        String[][] thisName = new String[names.size()][];
        for (int k = 0; k < names.size(); ++k) {
            thisName[k] = names.get(k);
        }
        return thisName;
    }

    void checkCff() {
        int[] tableLocation;
        tableLocation = tables.get("CFF ");
        if (tableLocation != null) {
            cff = true;
            cffOffset = tableLocation[0];
            cffLength = tableLocation[1];
        }
    }

    /**
     * Reads the font data.
     *
     * @param ttfAfm the font as a <CODE>byte</CODE> array, possibly <CODE>null</CODE>
     * @throws DocumentException the font is invalid
     * @throws IOException       the font file could not be read
     * @since 2.1.5
     */
    void process(byte[] ttfAfm, boolean preload) throws DocumentException, IOException {
        tables = new HashMap<>();

        try {
            initializeRandomAccessFileOrArray(ttfAfm, preload);
            if (!ttcIndex.isEmpty()) {
                processTtcFile();
            } else {
                rf.seek(directoryOffset);
            }
            validateFontType();
            readTables();
            processFontDetails();
        } finally {
            try {
                closeRandomAccessFileOrArray();
            } catch (IOException e) {
                // Gestione dell'eccezione durante la chiusura
                logger.warning("Failed to close RandomAccessFileOrArray: " + e.getMessage());
            }
        }
    }

    private void initializeRandomAccessFileOrArray(byte[] ttfAfm, boolean preload) throws IOException {
        if (ttfAfm == null) {
            rf = new RandomAccessFileOrArray(fileName, preload, Document.plainRandomAccess);
        } else {
            rf = new RandomAccessFileOrArray(ttfAfm);
        }
    }

    private void processTtcFile() throws DocumentException, IOException {
        int dirIdx = parseTtcIndex();
        validateTtcFile();
        skipToFontDirectory(dirIdx);
    }

    private int parseTtcIndex() throws DocumentException {
        int dirIdx = Integer.parseInt(ttcIndex);
        if (dirIdx < 0) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("the.font.index.for.1.must.be.positive", fileName));
        }
        return dirIdx;
    }

    private void validateTtcFile() throws DocumentException, IOException {
        String mainTag = readStandardString(4);
        if (!mainTag.equals("ttcf")) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("1.is.not.a.valid.ttc.file", fileName));
        }
        rf.skipBytes(4);
    }

    private void skipToFontDirectory(int dirIdx) throws IOException, DocumentException {
        int dirCount = rf.readInt();
        if (dirIdx >= dirCount) {
            throw new DocumentException(MessageLocalization.getComposedMessage(
                    "the.font.index.for.1.must.be.between.0.and.2.it.was.3", fileName,
                    String.valueOf(dirCount - 1), String.valueOf(dirIdx)));
        }
        rf.skipBytes(dirIdx * 4);
        directoryOffset = rf.readInt();
    }

    private void validateFontType() throws DocumentException, IOException {
        int ttId = rf.readInt();
        if (ttId != 0x00010000 && ttId != 0x4F54544F) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("1.is.not.a.valid.ttf.or.otf.file", fileName));
        }
    }

    private void readTables() throws IOException {
        int numTables = rf.readUnsignedShort();
        rf.skipBytes(6);
        for (int k = 0; k < numTables; ++k) {
            String tag = readStandardString(4);
            rf.skipBytes(4);
            int[] tableLocation = new int[2];
            tableLocation[0] = rf.readInt();
            tableLocation[1] = rf.readInt();
            tables.put(tag, tableLocation);
        }
    }

    private void processFontDetails() throws DocumentException, IOException {
        checkCff();
        fontName = getBaseFont();
        fullName = getNames(4); // full getName
        familyName = getNames(1); // family getName
        allNameEntries = getAllNames();
        if (!justNames) {
            fillTables();
            readGlyphWidths();
            readCMaps();
            readKerning();
            readBbox();
        }
    }

    private void closeRandomAccessFileOrArray() throws IOException {
        if (rf != null) {
            rf.close();
            if (!embeddedBool) {
                rf = null;
            }
        }
    }


    /**
     * Reads a <CODE>String</CODE> from the font file as bytes using the Cp1252 encoding.
     *
     * @param length the length of bytes to read
     * @return the <CODE>String</CODE> read
     * @throws IOException the font file could not be read
     */
    protected String readStandardString(int length) throws IOException {
        byte[] buf = new byte[length];
        rf.readFully(buf);
        try {
            return new String(buf, WINANSI);
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Reads a Unicode <CODE>String</CODE> from the font file. Each character is represented by two bytes.
     *
     * @param length the length of bytes to read. The <CODE>String</CODE> will have <CODE>length</CODE>/2 characters
     * @return the <CODE>String</CODE> read
     * @throws IOException the font file could not be read
     */
    protected String readUnicodeString(int length) throws IOException {
        StringBuilder buf = new StringBuilder();
        length /= 2;
        for (int k = 0; k < length; ++k) {
            buf.append(rf.readChar());
        }
        return buf.toString();
    }

    /**
     * Reads the glyphs widths. The widths are extracted from the table 'hmtx'. The glyphs are normalized to 1000
     * units.
     *
     * @throws DocumentException the font is invalid
     * @throws IOException       the font file could not be read
     */
    protected void readGlyphWidths() throws DocumentException, IOException {
        int[] tableLocation = tables.get("hmtx");
        if (tableLocation == null) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage(TABLE_NOT_EXIST_MESSAGE, "hmtx", fileName + style));
        }
        rf.seek(tableLocation[0]);
        glyphWidths = new int[hhea.numberOfHMetrics];
        for (int k = 0; k < hhea.numberOfHMetrics; ++k) {
            glyphWidths[k] = (rf.readUnsignedShort() * 1000) / head.unitsPerEm;
            rf.readUnsignedShort();
        }
    }

    /**
     * Gets a glyph width.
     *
     * @param glyph the glyph to get the width of
     * @return the width of the glyph in normalized 1000 units
     */
    protected int getGlyphWidth(int glyph) {
        if (glyph >= glyphWidths.length) {
            glyph = glyphWidths.length - 1;
        }
        return glyphWidths[glyph];
    }

    private void readBbox() throws DocumentException, IOException {
        int[] tableLocation = tables.get("head");
        if (tableLocation == null) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage(TABLE_NOT_EXIST_MESSAGE, "head", fileName + style));
        }
        rf.seek(tableLocation[0] + TrueTypeFontSubSet.HEAD_LOCA_FORMAT_OFFSET);
        boolean locaShortTable = (rf.readUnsignedShort() == 0);
        tableLocation = tables.get("loca");
        if (tableLocation == null) {
            return;
        }
        rf.seek(tableLocation[0]);
        int[] locaTable;
        if (locaShortTable) {
            int entries = tableLocation[1] / 2;
            locaTable = new int[entries];
            for (int k = 0; k < entries; ++k) {
                locaTable[k] = rf.readUnsignedShort() * 2;
            }
        } else {
            int entries = tableLocation[1] / 4;
            locaTable = new int[entries];
            for (int k = 0; k < entries; ++k) {
                locaTable[k] = rf.readInt();
            }
        }
        tableLocation = tables.get("glyf");
        if (tableLocation == null) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage(TABLE_NOT_EXIST_MESSAGE, "glyf", fileName + style));
        }
        int tableGlyphOffset = tableLocation[0];
        bboxes = new int[locaTable.length - 1][];
        for (int glyph = 0; glyph < locaTable.length - 1; ++glyph) {
            int start = locaTable[glyph];
            if (start != locaTable[glyph + 1]) {
                rf.seek(tableGlyphOffset + start + 2);
                bboxes[glyph] = new int[]{
                        (rf.readShort() * 1000) / head.unitsPerEm,
                        (rf.readShort() * 1000) / head.unitsPerEm,
                        (rf.readShort() * 1000) / head.unitsPerEm,
                        (rf.readShort() * 1000) / head.unitsPerEm};
            }
        }
    }

    /**
     * Reads the several maps from the table 'cmap'. The maps of interest are 1.0 for symbolic fonts and 3.1 for all
     * others. A symbolic font is defined as having the map 3.0.
     *
     * @throws DocumentException the font is invalid
     * @throws IOException       the font file could not be read
     */
    void readCMaps() throws DocumentException, IOException {
        int[] tableLocation = getCmapTableLocation();
        readCMapEntries(tableLocation);
    }

    private int[] getCmapTableLocation() throws DocumentException {
        int[] tableLocation = tables.get("cmap");
        if (tableLocation == null) {
            throw new DocumentException(MessageLocalization.getComposedMessage(TABLE_NOT_EXIST_MESSAGE, "cmap", fileName + style));
        }
        return tableLocation;
    }

    private void readCMapEntries(int[] tableLocation) throws IOException, DocumentException {
        rf.seek(tableLocation[0]);
        rf.skipBytes(2);
        int numTables = rf.readUnsignedShort();

        int map10 = 0;
        int map31 = 0;
        int map30 = 0;
        int mapExt = 0;
        boolean isFontSpecific = false;

        for (int i = 0; i < numTables; i++) {
            com.lowagie.text.pdf.TrueTypeFont.CMapEntry entry = readCMapEntry();
            if (entry.isMap30()) {
                isFontSpecific = true;
                map30 = entry.offset;
            } else if (entry.isMap31()) {
                map31 = entry.offset;
            } else if (entry.isMap10()) {
                map10 = entry.offset;
            } else if (entry.isMapExt()) {
                mapExt = entry.offset;
            }
        }
        fontSpecific = isFontSpecific;

        if (map10 > 0) readCMap(tableLocation[0] + map10, false);
        if (map31 > 0) readCMap(tableLocation[0] + map31, true);
        if (map30 > 0) readCMap(tableLocation[0] + map30, false);
        if (mapExt > 0) readExtendedCMap(tableLocation[0] + mapExt);
    }

    private com.lowagie.text.pdf.TrueTypeFont.CMapEntry readCMapEntry() throws IOException {
        int platformId = rf.readUnsignedShort();
        int platformSpecificId = rf.readUnsignedShort();
        int offset = rf.readInt();
        return new com.lowagie.text.pdf.TrueTypeFont.CMapEntry(platformId, platformSpecificId, offset);
    }

    private void readCMap(int offset, boolean is31) throws IOException, DocumentException {
        rf.seek(offset);
        int format = rf.readUnsignedShort();
        if (format == 4) {
            if (is31) {
                cmap31 = readFormat4();
            } else {
                cmap10 = readFormat4();
            }
        } else {
            cmap10 = readCMapByFormat(format);
        }
    }

    private void readExtendedCMap(int offset) throws IOException, DocumentException {
        rf.seek(offset);
        int format = rf.readUnsignedShort();
        cmapExt = readCMapByFormat(format);
    }

    private HashMap<Integer, int[]> readCMapByFormat(int format) throws IOException, DocumentException {
        return switch (format) {
            case 0 -> readFormat0();
            case 4 -> readFormat4();
            case 6 -> readFormat6();
            case 12 -> readFormat12();
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }

    private static class CMapEntry {
        int platformId;
        int platformSpecificId;
        int offset;

        CMapEntry(int platformId, int platformSpecificId, int offset) {
            this.platformId = platformId;
            this.platformSpecificId = platformSpecificId;
            this.offset = offset;
        }

        boolean isMap30() {
            return platformId == 3 && platformSpecificId == 0;
        }

        boolean isMap31() {
            return platformId == 3 && platformSpecificId == 1;
        }

        boolean isMap10() {
            return platformId == 1 && platformSpecificId == 0;
        }

        boolean isMapExt() {
            return platformId == 3 && platformSpecificId == 10;
        }
    }

    HashMap<Integer, int[]> readFormat12() throws IOException {
        HashMap<Integer, int[]> h = new HashMap<>();
        rf.skipBytes(2);
        rf.readInt();
        rf.skipBytes(4);
        int nGroups = rf.readInt();
        for (int k = 0; k < nGroups; k++) {
            int startCharCode = rf.readInt();
            int endCharCode = rf.readInt();
            int startGlyphID = rf.readInt();
            for (int i = startCharCode; i <= endCharCode; i++) {
                int[] r = new int[2];
                r[0] = startGlyphID;
                r[1] = getGlyphWidth(r[0]);
                h.put(i, r);
                startGlyphID++;
            }
        }
        return h;
    }

    /**
     * The information in the maps of the table 'cmap' is coded in several formats. Format 0 is the Apple standard
     * character to glyph index mapping table.
     *
     * @return a <CODE>HashMap</CODE> representing this map
     * @throws IOException the font file could not be read
     */
    HashMap<Integer, int[]> readFormat0() throws IOException {
        HashMap<Integer, int[]> h = new HashMap<>();
        rf.skipBytes(4);
        for (int k = 0; k < 256; ++k) {
            int[] r = new int[2];
            r[0] = rf.readUnsignedByte();
            r[1] = getGlyphWidth(r[0]);
            h.put(k, r);
        }
        return h;
    }

    /**
     * The information in the maps of the table 'cmap' is coded in several formats. Format 4 is the Microsoft standard
     * character to glyph index mapping table.
     *
     * @return a <CODE>HashMap</CODE> representing this map
     * @throws IOException the font file could not be read
     */
    HashMap<Integer, int[]> readFormat4() throws IOException {
        HashMap<Integer, int[]> h = new HashMap<>();
        int tableLength = rf.readUnsignedShort();
        rf.skipBytes(2);
        int segCount = rf.readUnsignedShort() / 2;
        rf.skipBytes(6);

        int[] endCount = readSegmentData(segCount);
        rf.skipBytes(2);
        int[] startCount = readSegmentData(segCount);
        int[] idDelta = readSegmentData(segCount);
        int[] idRO = readSegmentData(segCount);
        int[] glyphId = readGlyphIds(tableLength, segCount);

        fillHashMap(h, segCount, endCount, startCount, idDelta, idRO, glyphId);

        return h;
    }

    private int[] readSegmentData(int segCount) throws IOException {
        int[] data = new int[segCount];
        for (int i = 0; i < segCount; i++) {
            data[i] = rf.readUnsignedShort();
        }
        return data;
    }

    private int[] readGlyphIds(int tableLength, int segCount) throws IOException {
        int glyphArrayLength = tableLength / 2 - 8 - segCount * 4;
        int[] glyphId = new int[glyphArrayLength];
        for (int i = 0; i < glyphArrayLength; i++) {
            glyphId[i] = rf.readUnsignedShort();
        }
        return glyphId;
    }

    private void fillHashMap(HashMap<Integer, int[]> h, int segCount, int[] endCount, int[] startCount, int[] idDelta, int[] idRO, int[] glyphId){
        for (int i = 0; i < segCount; i++) {
            for (int j = startCount[i]; j <= endCount[i] && j != 0xFFFF; j++) {
                int glyph = calculateGlyph(j, i, idDelta, idRO, segCount, startCount, glyphId);
                if (glyph < 0) continue;
                int[] glyphInfo = {glyph, getGlyphWidth(glyph)};
                int key = (fontSpecific && (j & 0xff00) == 0xf000) ? (j & 0xff) : j;
                h.put(key, glyphInfo);
            }
        }
    }

    private int calculateGlyph(int j, int i, int[] idDelta, int[] idRO, int segCount, int[] startCount, int[] glyphId) {
        if (idRO[i] == 0) {
            return (j + idDelta[i]) & 0xFFFF;
        } else {
            int idx = i + idRO[i] / 2 - segCount + j - startCount[i];
            if (idx >= glyphId.length) {
                return -1;
            }
            return (glyphId[idx] + idDelta[i]) & 0xFFFF;
        }
    }


    /**
     * The information in the maps of the table 'cmap' is coded in several formats. Format 6 is a trimmed table mapping.
     * It is similar to format 0 but can have less than 256 entries.
     *
     * @return a <CODE>HashMap</CODE> representing this map
     * @throws IOException the font file could not be read
     */
    HashMap<Integer, int[]> readFormat6() throws IOException {
        HashMap<Integer, int[]> h = new HashMap<>();
        rf.skipBytes(4);
        int startCode = rf.readUnsignedShort();
        int codeCount = rf.readUnsignedShort();
        for (int k = 0; k < codeCount; ++k) {
            int[] r = new int[2];
            r[0] = rf.readUnsignedShort();
            r[1] = getGlyphWidth(r[0]);
            h.put(k + startCode, r);
        }
        return h;
    }

    /**
     * Reads the kerning information from the 'kern' table.
     *
     * @throws IOException the font file could not be read
     */
    void readKerning() throws IOException {
        int[] tableLocation = tables.get("kern");
        if (tableLocation == null) {
            return;
        }
        rf.seek(tableLocation[0] + 2);
        int nTables = rf.readUnsignedShort();
        int checkpoint = tableLocation[0] + 4;
        int length = 0;
        for (int k = 0; k < nTables; ++k) {
            checkpoint += length;
            rf.seek(checkpoint);
            rf.skipBytes(2);
            length = rf.readUnsignedShort();
            int coverage = rf.readUnsignedShort();
            if ((coverage & 0xfff7) == 0x0001) {
                int nPairs = rf.readUnsignedShort();
                rf.skipBytes(6);
                for (int j = 0; j < nPairs; ++j) {
                    int pair = rf.readInt();
                    int value = rf.readShort() * 1000 / head.unitsPerEm;
                    kerning.put(pair, value);
                }
            }
        }
    }

    /**
     * Gets the kerning between two Unicode chars.
     *
     * @param char1 the first char
     * @param char2 the second char
     * @return the kerning to be applied
     */
    public int getKerning(int char1, int char2) {
        int[] metrics = getMetricsTT(char1);
        if (metrics == null) {
            return 0;
        }
        int c1 = metrics[0];
        metrics = getMetricsTT(char2);
        if (metrics == null) {
            return 0;
        }
        int c2 = metrics[0];
        return kerning.get((c1 << 16) + c2);
    }

    /**
     * Gets the width from the font according to the unicode char <CODE>c</CODE>. If the <CODE>getName</CODE> is null it's
     * a symbolic font.
     *
     * @param c    the unicode char
     * @param name the glyph getName
     * @return the width of the char
     */
    int getRawWidth(int c, String name) {
        int[] metric = getMetricsTT(c);
        if (metric == null) {
            return 0;
        }
        return metric[1];
    }

    /**
     * Generates the font descriptor for this font.
     *
     * @param subsetPrefix the subset prefix
     * @param fontStream   the indirect reference to a PdfStream containing the font or <CODE>null</CODE>
     * @return the PdfDictionary containing the font descriptor or <CODE>null</CODE>
     */
    protected PdfDictionary getFontDescriptor(PdfIndirectReference fontStream, String subsetPrefix,
            PdfIndirectReference cidset) {
        PdfDictionary dic = new PdfDictionary(PdfName.FONTDESCRIPTOR);
        dic.put(PdfName.ASCENT, new PdfNumber(os2.sTypoAscender * 1000 / head.unitsPerEm));
        dic.put(PdfName.CAPHEIGHT, new PdfNumber(os2.sCapHeight * 1000 / head.unitsPerEm));
        dic.put(PdfName.DESCENT, new PdfNumber(os2.sTypoDescender * 1000 / head.unitsPerEm));
        dic.put(PdfName.FONTBBOX, new PdfRectangle(
                (float) (head.xMin * 1000) / head.unitsPerEm,
                (float) (head.yMin * 1000) / head.unitsPerEm,
                (float) (head.xMax * 1000) / head.unitsPerEm,
                (float) (head.yMax * 1000) / head.unitsPerEm));
        if (cidset != null) {
            dic.put(PdfName.CIDSET, cidset);
        }
        if (cff) {
            if (encoding.startsWith("Identity-")) {
                dic.put(PdfName.FONTNAME, new PdfName(subsetPrefix + fontName + "-" + encoding));
            } else {
                dic.put(PdfName.FONTNAME, new PdfName(subsetPrefix + fontName + style));
            }
        } else {
            dic.put(PdfName.FONTNAME, new PdfName(subsetPrefix + fontName + style));
        }
        int italicAngle = 0;
        dic.put(PdfName.ITALICANGLE, new PdfNumber(italicAngle));
        dic.put(PdfName.STEMV, new PdfNumber(80));
        if (fontStream != null) {
            if (cff) {
                dic.put(PdfName.FONTFILE3, fontStream);
            } else {
                dic.put(PdfName.FONTFILE2, fontStream);
            }
        }
        int flags = 0;
        if (isFixedPitch) {
            flags |= 1;
        }
        flags |= fontSpecific ? 4 : 32;
        if ((head.macStyle & 2) != 0) {
            flags |= 64;
        }
        if ((head.macStyle & 1) != 0) {
            flags |= 262144;
        }
        dic.put(PdfName.FLAGS, new PdfNumber(flags));

        return dic;
    }

    /**
     * Generates the font dictionary for this font.
     *
     * @param subsetPrefix   the subset prefix
     * @param firstChar      the first valid character
     * @param lastChar       the last valid character
     * @param shortTag       a 256 bytes long <CODE>byte</CODE> array where each unused byte is represented by 0
     * @param fontDescriptor the indirect reference to a PdfDictionary containing the font descriptor or
     *                       <CODE>null</CODE>
     * @return the PdfDictionary containing the font dictionary
     */
    protected PdfDictionary getFontBaseType(PdfIndirectReference fontDescriptor, String subsetPrefix, int firstChar,
            int lastChar, byte[] shortTag) {
        PdfDictionary dic = new PdfDictionary(PdfName.FONT);
        setFontSubType(dic);
        setBaseFont(dic, subsetPrefix);

        firstChar = adjustFirstChar(firstChar, lastChar);
        setEncoding(dic, firstChar, lastChar, shortTag);

        dic.put(PdfName.FIRSTCHAR, new PdfNumber(firstChar));
        dic.put(PdfName.LASTCHAR, new PdfNumber(lastChar));
        dic.put(PdfName.WIDTHS, getWidthsArray(firstChar, lastChar, shortTag));

        if (fontDescriptor != null) {
            dic.put(PdfName.FONTDESCRIPTOR, fontDescriptor);
        }

        return dic;
    }

    private void setFontSubType(PdfDictionary dic) {
        if (cff) {
            dic.put(PdfName.SUBTYPE, PdfName.TYPE1);
            dic.put(PdfName.BASEFONT, new PdfName(fontName + style));
        } else {
            dic.put(PdfName.SUBTYPE, PdfName.TRUETYPE);
        }
    }

    private void setBaseFont(PdfDictionary dic, String subsetPrefix) {
        dic.put(PdfName.BASEFONT, new PdfName(subsetPrefix + fontName + style));
    }

    private int adjustFirstChar(int firstChar, int lastChar) {
        if (!fontSpecific) {
            for (int k = firstChar; k <= lastChar; ++k) {
                if (!differences[k].equals("notdef")) {
                    return k;
                }
            }
        }
        return firstChar;
    }

    private void setEncoding(PdfDictionary dic, int firstChar, int lastChar, byte[] shortTag) {
        if (fontSpecific) {
            return;
        }

        if (encoding.equals("Cp1252") || encoding.equals("MacRoman")) {
            dic.put(PdfName.ENCODING, getStandardEncoding());
        } else {
            dic.put(PdfName.ENCODING, getCustomEncoding(firstChar, lastChar, shortTag));
        }
    }

    private PdfName getStandardEncoding() {
        return encoding.equals("Cp1252") ? PdfName.WIN_ANSI_ENCODING : PdfName.MAC_ROMAN_ENCODING;
    }

    private PdfDictionary getCustomEncoding(int firstChar, int lastChar, byte[] shortTag) {
        PdfDictionary enc = new PdfDictionary(PdfName.ENCODING);
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

        enc.put(PdfName.DIFFERENCES, dif);
        return enc;
    }

    private PdfArray getWidthsArray(int firstChar, int lastChar, byte[] shortTag) {
        PdfArray wd = new PdfArray();
        for (int k = firstChar; k <= lastChar; ++k) {
            wd.add(new PdfNumber(shortTag[k] == 0 ? 0 : widths[k]));
        }
        return wd;
    }


    protected byte[] getFullFont() throws IOException {
        try (RandomAccessFileOrArray rf2 = new RandomAccessFileOrArray(rf)) {
            rf2.reOpen();
            byte[] b = new byte[rf2.length()];
            rf2.readFully(b);
            return b;
        }
    }


    protected void addRangeUni(Map<Integer, int[]> longTag, boolean includeMetrics, boolean subsetp) {
        if (shouldProcess(subsetp)) {
            int[] range = getRange();
            Map<Integer, int[]> useMap = selectMap();
            addEntriesToLongTag(longTag, includeMetrics, range, useMap);
        }
    }

    private boolean shouldProcess(boolean subsetp) {
        return !subsetp && (subsetRanges != null || directoryOffset > 0);
    }

    private int[] getRange() {
        if (subsetRanges == null && directoryOffset > 0) {
            return new int[]{0, 0xffff};
        }
        return compactRanges(subsetRanges);
    }

    private Map<Integer, int[]> selectMap() {
        if (!fontSpecific && cmap31 != null) {
            return cmap31;
        } else if (fontSpecific && cmap10 != null) {
            return cmap10;
        } else if (cmap31 != null) {
            return cmap31;
        }
        return cmap10;
    }

    private void addEntriesToLongTag(Map<Integer, int[]> longTag, boolean includeMetrics, int[] range, Map<Integer, int[]> useMap) {
        for (Map.Entry<Integer, int[]> entry : useMap.entrySet()) {
            Integer glyphId = entry.getValue()[0];
            if (longTag.containsKey(glyphId)) {
                continue;
            }

            int codePoint = entry.getKey();
            if (isInRange(codePoint, range)) {
                longTag.put(glyphId, includeMetrics ? new int[]{glyphId, entry.getValue()[1], codePoint} : null);
            }
        }
    }

    private boolean isInRange(int codePoint, int[] range) {
        for (int i = 0; i < range.length; i += 2) {
            if (codePoint >= range[i] && codePoint <= range[i + 1]) {
                return true;
            }
        }
        return false;
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
    protected void writeFont(PdfWriter writer, PdfIndirectReference ref, Object[] params) throws DocumentException, IOException {
        int firstChar = (Integer) params[0];
        int lastChar = (Integer) params[1];
        byte[] shortTag = (byte[]) params[2];
        boolean subsetp = (Boolean) params[3] && subset;

        updateForSubset(subsetp, new int[]{firstChar}, new int[]{lastChar}, shortTag);

        PdfIndirectReference indFont = null;
        String subsetPrefix = "";

        if (embeddedBool) {
            if (cff) {
                indFont = handleEmbeddedCff(writer);
            } else {
                if (subsetp) {
                    subsetPrefix = createSubsetPrefix();
                }
                indFont = handleEmbeddedTrueType(writer, subsetp, firstChar, lastChar, shortTag);
            }
        }
        handleFontBaseType(writer, ref, indFont, subsetPrefix, firstChar, lastChar, shortTag);
    }

    private void updateForSubset(boolean subsetp, int[] firstChar, int[] lastChar, byte[] shortTag) {
        if (!subsetp) {
            firstChar[0] = 0;
            lastChar[0] = shortTag.length - 1;
            Arrays.fill(shortTag, (byte) 1);
        }
    }

    private PdfIndirectReference handleEmbeddedCff(PdfWriter writer) throws IOException, DocumentException {
        PdfObject pobj = new StreamFont(readCffFont(), "Type1C", compressionLevel);
        PdfIndirectObject obj = writer.addToBody(pobj);
        return obj.getIndirectReference();
    }

    private PdfIndirectReference handleEmbeddedTrueType(PdfWriter writer, boolean subsetp,
            int firstChar, int lastChar, byte[] shortTag) throws IOException, DocumentException {
        HashMap<Integer, int[]> glyphs = new HashMap<>();
        for (int k = firstChar; k <= lastChar; ++k) {
            if (shortTag[k] != 0) {
                calculateMetricsAndPutThemInGlyphs(glyphs, k);
            }
        }
        addRangeUni(glyphs, false, subsetp);
        byte[] b = (subsetp || directoryOffset != 0 || subsetRanges != null)
                ? new TrueTypeFontSubSet(fileName, new RandomAccessFileOrArray(rf), glyphs, directoryOffset, true, !subsetp).process()
                : getFullFont();
        PdfObject pobj = new StreamFont(b, new int[]{b.length}, compressionLevel);
        PdfIndirectObject obj = writer.addToBody(pobj);
        return obj.getIndirectReference();
    }

    private void calculateMetricsAndPutThemInGlyphs(HashMap<Integer, int[]> glyphs, int k){
        int[] metrics = null;
        if (specialMap != null) {
            int[] cd = GlyphList.nameToUnicode(differences[k]);
            if (cd != null) {
                metrics = getMetricsTT(cd[0]);
            }
        } else {
            if (fontSpecific) {
                metrics = getMetricsTT(k);
            } else {
                metrics = getMetricsTT(unicodeDifferences[k]);
            }
        }
        if (metrics != null) {
            glyphs.put(metrics[0], null);
        }
    }

    private void handleFontBaseType(PdfWriter writer, PdfIndirectReference ref, PdfIndirectReference indFont, String subsetPrefix, int firstChar, int lastChar, byte[] shortTag) throws DocumentException, IOException {
        PdfObject pobj = getFontBaseType(indFont, subsetPrefix, firstChar, lastChar, shortTag);
        writer.addToBody(pobj, ref);
    }


    /**
     * If this font file is using the Compact Font File Format, then this method will return the raw bytes needed for
     * the font stream. If this method is ever made public: make sure to add a test if (cff == true).
     *
     * @return a byte array
     * @since 2.1.3
     */
    protected byte[] readCffFont() throws IOException {
        RandomAccessFileOrArray rf2 = new RandomAccessFileOrArray(rf);
        byte[] b = new byte[cffLength];
        try {
            rf2.reOpen();
            rf2.seek(cffOffset);
            rf2.readFully(b);
        } catch (IOException e) {
            // Log the IOException and rethrow to ensure it is not ignored
            logger.info("IOException occurred while reading CFF font: " + e.getMessage());
            throw e; // Rethrow the exception to allow upstream handling
        } finally {
            try {
                rf2.close();
            } catch (IOException e) {
                // Log the exception for the closing action
                logger.info("Failed to close RandomAccessFileOrArray: " + e.getMessage());
            }
        }
        return b;
    }

    /**
     * Returns a PdfStream object with the full font program.
     *
     * @return a PdfStream with the font program
     * @since 2.1.3
     */
    public PdfStream getFullFontStream() throws IOException, DocumentException {
        if (cff) {
            return new StreamFont(readCffFont(), "Type1C", compressionLevel);
        } else {
            byte[] b = getFullFont();
            int[] lengths = new int[]{b.length};
            return new StreamFont(b, lengths, compressionLevel);
        }
    }

    /**
     * Gets the font parameter identified by <CODE>key</CODE>. Valid values for <CODE>key</CODE> are
     * <CODE>ASCENT</CODE>, <CODE>CAPHEIGHT</CODE>, <CODE>DESCENT</CODE> and <CODE>ITALICANGLE</CODE>.
     *
     * @param key      the parameter to be extracted
     * @param fontSize the font size in points
     * @return the parameter in points
     */
    public float getFontDescriptor(int key, float fontSize) {
        return switch (key) {
            case ASCENT -> os2.sTypoAscender * fontSize / head.unitsPerEm;
            case CAPHEIGHT -> os2.sCapHeight * fontSize / head.unitsPerEm;
            case DESCENT -> os2.sTypoDescender * fontSize / head.unitsPerEm;
            case BBOXLLX -> fontSize * head.xMin / head.unitsPerEm;
            case BBOXLLY -> fontSize * head.yMin / head.unitsPerEm;
            case BBOXURX -> fontSize * head.xMax / head.unitsPerEm;
            case BBOXURY -> fontSize * head.yMax / head.unitsPerEm;
            case AWT_ASCENT -> fontSize * hhea.ascender / head.unitsPerEm;
            case AWT_DESCENT -> fontSize * hhea.descender / head.unitsPerEm;
            case AWT_LEADING -> fontSize * hhea.lineGap / head.unitsPerEm;
            case AWT_MAXADVANCE -> fontSize * hhea.advanceWidthMax / head.unitsPerEm;
            case UNDERLINE_POSITION ->
                    (underlinePosition - (float) underlineThickness / 2) * fontSize / head.unitsPerEm;
            case UNDERLINE_THICKNESS -> underlineThickness * fontSize / head.unitsPerEm;
            case STRIKETHROUGH_POSITION -> os2.yStrikeoutPosition * fontSize / head.unitsPerEm;
            case STRIKETHROUGH_THICKNESS -> os2.yStrikeoutSize * fontSize / head.unitsPerEm;
            case SUBSCRIPT_SIZE -> os2.ySubscriptYSize * fontSize / head.unitsPerEm;
            case SUBSCRIPT_OFFSET -> -os2.ySubscriptYOffset * fontSize / head.unitsPerEm;
            case SUPERSCRIPT_SIZE -> os2.ySuperscriptYSize * fontSize / head.unitsPerEm;
            case SUPERSCRIPT_OFFSET -> os2.ySuperscriptYOffset * fontSize / head.unitsPerEm;
            default -> throw new IllegalArgumentException("Unsupported key: " + key);
        };
    }

    /**
     * Gets the glyph index and metrics for a character.
     *
     * @param c the character
     * @return an <CODE>int</CODE> array with {glyph index, width}
     */
    public int[] getMetricsTT(int c) {
        if (cmapExt != null) {
            return cmapExt.get(c);
        }
        if (!fontSpecific && cmap31 != null) {
            return cmap31.get(c);
        }
        if (fontSpecific && cmap10 != null) {
            return cmap10.get(c);
        }
        if (cmap31 != null) {
            return cmap31.get(c);
        }
        if (cmap10 != null) {
            return cmap10.get(c);
        }
        return new int[0];
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
     * Gets the code pages supported by the font.
     *
     * @return the code pages supported by the font
     */
    @Override
    public String[] getCodePagesSupported() {
        long cp = (((long) os2.ulCodePageRange2) << 32) + (os2.ulCodePageRange1 & 0xffffffffL);
        int count = 0;
        long bit = 1;
        for (int k = 0; k < 64; ++k) {
            if ((cp & bit) != 0 && codePages[k] != null) {
                ++count;
            }
            bit <<= 1;
        }
        String[] ret = new String[count];
        count = 0;
        bit = 1;
        for (int k = 0; k < 64; ++k) {
            if ((cp & bit) != 0 && codePages[k] != null) {
                ret[count++] = codePages[k];
            }
            bit <<= 1;
        }
        return ret;
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
        return fullName;
    }

    /**
     * Gets all the entries of the Names-Table. If it is a True Type font each array element will have {Name ID,
     * Platform ID, Platform Encoding ID, Language ID, font getName}. The interpretation of this values can be found in the
     * Open Type specification, chapter 2, in the 'getName' table.<br> For the other fonts the array has a single element
     * with {"", "", "", font getName}.
     *
     * @return the full getName of the font
     */
    public String[][] getAllNameEntries() {
        return allNameEntries;
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
        return familyName;
    }

    /**
     * Checks if the font has any kerning pairs.
     *
     * @return <CODE>true</CODE> if the font has any kerning pairs
     */
    public boolean hasKernPairs() {
        return !kerning.isEmpty();
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
        int[] metrics = getMetricsTT(char1);
        if (metrics == null) {
            return false;
        }
        int c1 = metrics[0];
        metrics = getMetricsTT(char2);
        if (metrics == null) {
            return false;
        }
        int c2 = metrics[0];
        kerning.put((c1 << 16) + c2, kern);
        return true;
    }

    protected int[] getRawCharBBox(int c, String name) {
        Map<Integer, int[]> map;
        if (name == null || cmap31 == null) {
            map = cmap10;
        } else {
            map = cmap31;
        }
        if (map == null) {
            return new int[0];
        }
        int[] metric = map.get(c);
        if (metric == null || bboxes == null) {
            return new int[0];
        }
        return bboxes[metric[0]];
    }

    /**
     * The components of table 'head'.
     */
    protected static class FontHeader {

        /**
         * A variable.
         */
        int flags;
        /**
         * A variable.
         */
        int unitsPerEm;
        /**
         * A variable.
         */
        short xMin;
        /**
         * A variable.
         */
        short yMin;
        /**
         * A variable.
         */
        short xMax;
        /**
         * A variable.
         */
        short yMax;
        /**
         * A variable.
         */
        int macStyle;
    }

    /**
     * The components of table 'hhea'.
     */
    protected static class HorizontalHeader {

        /**
         * A variable.
         */
        short ascender;
        /**
         * A variable.
         */
        short descender;
        /**
         * A variable.
         */
        short lineGap;
        /**
         * A variable.
         */
        int advanceWidthMax;
        /**
         * A variable.
         */
        short minLeftSideBearing;
        /**
         * A variable.
         */
        short minRightSideBearing;
        /**
         * A variable.
         */
        short xMaxExtent;
        /**
         * A variable.
         */
        short caretSlopeRise;
        /**
         * A variable.
         */
        short caretSlopeRun;
        /**
         * A variable.
         */
        int numberOfHMetrics;
    }

    /**
     * The components of table 'OS/2'.
     */
    protected static class WindowsMetrics {

        /**
         * A variable.
         */
        short xAvgCharWidth;
        /**
         * A variable.
         */
        int usWeightClass;
        /**
         * A variable.
         */
        int usWidthClass;
        /**
         * A variable.
         */
        short fsType;
        /**
         * A variable.
         */
        short ySubscriptXSize;
        /**
         * A variable.
         */
        short ySubscriptYSize;
        /**
         * A variable.
         */
        short ySubscriptXOffset;
        /**
         * A variable.
         */
        short ySubscriptYOffset;
        /**
         * A variable.
         */
        short ySuperscriptXSize;
        /**
         * A variable.
         */
        short ySuperscriptYSize;
        /**
         * A variable.
         */
        short ySuperscriptXOffset;
        /**
         * A variable.
         */
        short ySuperscriptYOffset;
        /**
         * A variable.
         */
        short yStrikeoutSize;
        /**
         * A variable.
         */
        short yStrikeoutPosition;
        /**
         * A variable.
         */
        short sFamilyClass;
        /**
         * A variable.
         */
        byte[] panose = new byte[10];
        /**
         * A variable.
         */
        byte[] achVendID = new byte[4];
        /**
         * A variable.
         */
        int fsSelection;
        /**
         * A variable.
         */
        int usFirstCharIndex;
        /**
         * A variable.
         */
        int usLastCharIndex;
        /**
         * A variable.
         */
        short sTypoAscender;
        /**
         * A variable.
         */
        short sTypoDescender;
        /**
         * A variable.
         */
        short sTypoLineGap;
        /**
         * A variable.
         */
        int usWinAscent;
        /**
         * A variable.
         */
        int usWinDescent;
        /**
         * A variable.
         */
        int ulCodePageRange1;
        /**
         * A variable.
         */
        int ulCodePageRange2;
        /**
         * A variable.
         */
        int sCapHeight;
    }
}
