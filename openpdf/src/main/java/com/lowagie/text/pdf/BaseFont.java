/*
 * $Id: BaseFont.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2000-2006 by Paulo Soares.
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
import com.lowagie.text.error_messages.MessageLocalization;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import static com.ibm.icu.util.ULocale.getBaseName;

/**
 * Base class for the several font types supported
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */

public abstract class BaseFont {

    private static final String TTC_KEY = ".ttc,";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String COURIER = "Courier";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String COURIER_BOLD = "Courier-Bold";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String COURIER_OBLIQUE = "Courier-Oblique";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String COURIER_BOLDOBLIQUE = "Courier-BoldOblique";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String HELVETICA = "Helvetica";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String HELVETICA_BOLD = "Helvetica-Bold";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String HELVETICA_OBLIQUE = "Helvetica-Oblique";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String HELVETICA_BOLDOBLIQUE = "Helvetica-BoldOblique";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String SYMBOL = "Symbol";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String TIMES_ROMAN = "Times-Roman";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String TIMES_BOLD = "Times-Bold";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String TIMES_ITALIC = "Times-Italic";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String TIMES_BOLDITALIC = "Times-BoldItalic";

    /**
     * This is a possible value of a base 14 getTypeImpl 1 font
     */
    public static final String ZAPFDINGBATS = "ZapfDingbats";

    /**
     * The maximum height above the baseline reached by glyphs in this font, excluding the height of glyphs for accented
     * characters.
     */
    public static final int ASCENT = 1;
    /**
     * The y coordinate of the top of flat capital letters, measured from the baseline.
     */
    public static final int CAPHEIGHT = 2;
    /**
     * The maximum depth below the baseline reached by glyphs in this font. The value is a negative number.
     */
    public static final int DESCENT = 3;
    /**
     * The angle, expressed in degrees counterclockwise from the vertical, of the dominant vertical strokes of the font.
     * The value is negative for fonts that slope to the right, as almost all italic fonts do.
     */
    public static final int ITALICANGLE = 4;
    /**
     * The lower left x glyph coordinate.
     */
    public static final int BBOXLLX = 5;
    /**
     * The lower left y glyph coordinate.
     */
    public static final int BBOXLLY = 6;
    /**
     * The upper right x glyph coordinate.
     */
    public static final int BBOXURX = 7;
    /**
     * The upper right y glyph coordinate.
     */
    public static final int BBOXURY = 8;

    /**
     * java.awt.Font property
     */
    public static final int AWT_ASCENT = 9;
    /**
     * java.awt.Font property
     */
    public static final int AWT_DESCENT = 10;
    /**
     * java.awt.Font property
     */
    public static final int AWT_LEADING = 11;
    /**
     * java.awt.Font property
     */
    public static final int AWT_MAXADVANCE = 12;
    /**
     * The underline position. Usually a negative value.
     */
    public static final int UNDERLINE_POSITION = 13;
    /**
     * The underline thickness.
     */
    public static final int UNDERLINE_THICKNESS = 14;
    /**
     * The strikethrough position.
     */
    public static final int STRIKETHROUGH_POSITION = 15;
    /**
     * The strikethrough thickness.
     */
    public static final int STRIKETHROUGH_THICKNESS = 16;
    /**
     * The recommended vertical size for subscripts for this font.
     */
    public static final int SUBSCRIPT_SIZE = 17;
    /**
     * The recommended vertical offset from the baseline for subscripts for this font. Usually a negative value.
     */
    public static final int SUBSCRIPT_OFFSET = 18;
    /**
     * The recommended vertical size for superscripts for this font.
     */
    public static final int SUPERSCRIPT_SIZE = 19;
    /**
     * The recommended vertical offset from the baseline for superscripts for this font.
     */
    public static final int SUPERSCRIPT_OFFSET = 20;
    /**
     * The font is Type 1.
     */
    public static final int FONT_TYPE_T1 = 0;
    /**
     * The font is True Type with a standard encoding.
     */
    public static final int FONT_TYPE_TT = 1;
    /**
     * The font is CJK.
     */
    public static final int FONT_TYPE_CJK = 2;
    /**
     * The font is True Type with a Unicode encoding.
     */
    public static final int FONT_TYPE_TTUNI = 3;
    /**
     * A font already inside the document.
     */
    public static final int FONT_TYPE_DOCUMENT = 4;
    /**
     * A Type3 font.
     */
    public static final int FONT_TYPE_T3 = 5;
    /**
     * The Unicode encoding with horizontal writing.
     */
    public static final String IDENTITY_H = "Identity-H";
    /**
     * The Unicode encoding with vertical writing.
     */
    public static final String IDENTITY_V = "Identity-V";

    /**
     * A possible encoding.
     */
    public static final String CP1250 = "Cp1250";

    /**
     * A possible encoding.
     */
    public static final String CP1252 = "Cp1252";

    /**
     * A possible encoding.
     */
    public static final String CP1257 = "Cp1257";

    /**
     * A possible encoding.
     */
    public static final String WINANSI = "Cp1252";

    /**
     * A possible encoding.
     */
    public static final String MACROMAN = "MacRoman";

    protected static final int[] CHAR_RANGE_LATIN = {0, 0x17f, 0x2000, 0x206f,
            0x20a0, 0x20cf, 0xfb00, 0xfb06};
    protected static final int[] CHAR_RANGE_ARABIC = {0, 0x7f, 0x0600, 0x067f,
            0x20a0, 0x20cf, 0xfb50, 0xfbff, 0xfe70, 0xfeff};
    protected static final int[] CHAR_RANGE_HEBREW = {0, 0x7f, 0x0590, 0x05ff,
            0x20a0, 0x20cf, 0xfb1d, 0xfb4f};
    protected static final int[] CHAR_RANGE_CYRILLIC = {0, 0x7f, 0x0400, 0x052f,
            0x2000, 0x206f, 0x20a0, 0x20cf};

    /**
     * if the font has to be embedded
     */
    public static final boolean EMBEDDED = true;

    /**
     * if the font doesn't have to be embedded
     */
    public static final boolean NOT_EMBEDDED = false;
    /**
     * if the font has to be cached
     */
    public static final boolean CACHED = true;
    /**
     * if the font doesn't have to be cached
     */
    public static final boolean NOT_CACHED = false;

    /**
     * The path to the font resources.
     */
    public static final String RESOURCE_PATH = "com/lowagie/text/pdf/fonts/";
    /**
     * The fake CID code that represents a newline.
     */
    public static final char CID_NEWLINE = '\u7fff';
    /**
     * a not defined character in a custom PDF encoding
     */
    public static final String NOTDEF = ".notdef";
    /**
     * list of the 14 built in fonts.
     */
    protected static final HashMap<String, PdfName> BuiltinFonts14 = new HashMap<>();
    /**
     * cache for the fonts already used.
     */
    protected static ConcurrentHashMap<String, BaseFont> fontCache = new ConcurrentHashMap<>(
            500, 0.85f, 64);

    static {
        BuiltinFonts14.put(COURIER, PdfName.COURIER);
        BuiltinFonts14.put(COURIER_BOLD, PdfName.COURIER_BOLD);
        BuiltinFonts14.put(COURIER_BOLDOBLIQUE, PdfName.COURIER_BOLDOBLIQUE);
        BuiltinFonts14.put(COURIER_OBLIQUE, PdfName.COURIER_OBLIQUE);
        BuiltinFonts14.put(HELVETICA, PdfName.HELVETICA);
        BuiltinFonts14.put(HELVETICA_BOLD, PdfName.HELVETICA_BOLD);
        BuiltinFonts14
                .put(HELVETICA_BOLDOBLIQUE, PdfName.HELVETICA_BOLDOBLIQUE);
        BuiltinFonts14.put(HELVETICA_OBLIQUE, PdfName.HELVETICA_OBLIQUE);
        BuiltinFonts14.put(SYMBOL, PdfName.SYMBOL);
        BuiltinFonts14.put(TIMES_ROMAN, PdfName.TIMES_ROMAN);
        BuiltinFonts14.put(TIMES_BOLD, PdfName.TIMES_BOLD);
        BuiltinFonts14.put(TIMES_BOLDITALIC, PdfName.TIMES_BOLDITALIC);
        BuiltinFonts14.put(TIMES_ITALIC, PdfName.TIMES_ITALIC);
        BuiltinFonts14.put(ZAPFDINGBATS, PdfName.ZAPFDINGBATS);
    }

    protected ArrayList<int[]> subsetRanges;
    /**
     * table of characters widths for this encoding
     */
    protected int[] widths = new int[256];
    /**
     * encoding names
     */
    protected String[] differences = new String[256];
    /**
     * same as differences but with the unicode codes
     */
    protected char[] unicodeDifferences = new char[256];
    protected int[][] charBBoxes = new int[256][];
    /**
     * encoding used with this font
     */
    protected String encoding;
    /**
     * true if the font is to be embedded in the PDF
     */
    protected boolean embeddedBool;
    /**
     * The compression level for the font stream.
     *
     * @since 2.1.3
     */
    protected int compressionLevel = PdfStream.DEFAULT_COMPRESSION;
    /**
     * true if the font must use its built in encoding. In that case the
     * <CODE>encoding</CODE> is only used to map a char to the position inside
     * the font, not to the expected char getName.
     */
    protected boolean fontSpecific = true;
    /**
     * Forces the output of the width array. Only matters for the 14 built-in fonts.
     */
    protected boolean forceWidthsOutput = false;

    /**
     * Converts <CODE>char</CODE> directly to <CODE>byte</CODE> by casting.
     */
    protected boolean directTextToByte = false;

    /**
     * Indicates if all the glyphs and widths for that particular encoding should be included in the document.
     */
    protected boolean subset = true;

    protected boolean fastWinansi = false;

    /**
     * Custom encodings use this map to key the Unicode character to the single byte code.
     */
    protected IntHashtable specialMap;

    /**
     * Used to build a randomized prefix for a subset getName
     */
    protected SecureRandom secureRandom;

    /**
     * Indicates if a CIDSet stream should be included in the document.
     */
    protected boolean includeCidSet = true;
    /**
     * The font getTypeImpl.
     */
    int fontType;

    /**
     * Creates new BaseFont
     */
    protected BaseFont() {
    }

    /**
     * Creates a new font. This will always be the default Helvetica font (not embedded). This method is introduced
     * because Helvetica is used in many examples.
     *
     * @return a BaseFont object (Helvetica, Winansi, not embedded)
     * @throws IOException       This shouldn't occur ever
     * @throws DocumentException This shouldn't occur ever
     * @since 2.1.1
     */
    public static BaseFont createFont() throws DocumentException, IOException {
        return createFont(BaseFont.HELVETICA, BaseFont.WINANSI,
                BaseFont.NOT_EMBEDDED);
    }

    /**
     * Creates a new font. This font can be one of the 14 built in types, a Type1 font referred to by an AFM or PFM
     * file, a TrueType font (simple or collection) or a CJK font from the Adobe Asian Font Pack. TrueType fonts and CJK
     * fonts can have an optional style modifier appended to the getName. These modifiers are: Bold, Italic and BoldItalic.
     * An example would be "STSong-Light,Bold". Note that this modifiers do not work if the font is embedded. Fonts in
     * TrueType collections are addressed by index such as "msgothic.ttc,1". This would get the second font (indexes
     * start at 0), in this case "MS PGothic".
     * <p>
     * The fonts are cached and if they already exist they are extracted from the cache, not parsed again.
     * <p>
     * Besides the common encodings described by getName, custom encodings can also be made. These encodings will only work
     * for the single byte fonts Type1 and TrueType. The encoding string starts with a '#' followed by "simple" or
     * "full". If "simple" there is a decimal for the first character position and then a list of hex values
     * representing the Unicode codes that compose that encoding.<br> The "simple" encoding is recommended for TrueType
     * fonts as the "full" encoding risks not matching the character with the right glyph if not done with care.<br> The
     * "full" encoding is specially aimed at Type1 fonts where the glyphs have to be described by non standard names
     * like the Tex math fonts. Each group of three elements compose a code position: the one byte code order in decimal
     * or as 'x' (x cannot be the space), the getName and the Unicode character used to access the glyph. The space must be
     * assigned to character position 32 otherwise text justification will not work.
     * <p>
     * Example for a "simple" encoding that includes the Unicode character space, A, B and ecyrillic:
     *
     * <PRE>
     * &quot;# simple 32 0020 0041 0042 0454&quot;
     * </PRE>
     * <p>
     * Example for a "full" encoding for a Type1 Tex font:
     *
     * <PRE>
     * &quot;# full 'A' nottriangeqlleft 0041 'B' dividemultiply 0042 32 space 0020&quot;
     * </PRE>
     * <p>
     * This method calls:<br>
     *
     * <PRE>
     * createFont(getName, encoding, embedded, true, null, null);
     * </PRE>
     *
     * @param name     the getName of the font or its location on file
     * @param encoding the encoding to be applied to this font
     * @param embedded true if the font is to be embedded in the PDF
     * @return returns a new font. This font may come from the cache
     * @throws DocumentException the font is invalid
     * @throws IOException       the font file could not be read
     */
    public static BaseFont createFont(String name, String encoding,
            boolean embedded) throws DocumentException, IOException {
        return createFont(name, encoding, embedded, true, null, null, false);
    }

    /**
     * Creates a new font. This font can be one of the 14 built in types, a Type1 font referred to by an AFM or PFM
     * file, a TrueType font (simple or collection) or a CJK font from the Adobe Asian Font Pack. TrueType fonts and CJK
     * fonts can have an optional style modifier appended to the getName. These modifiers are: Bold, Italic and BoldItalic.
     * An example would be "STSong-Light,Bold". Note that this modifiers do not work if the font is embedded. Fonts in
     * TrueType collections are addressed by index such as "msgothic.ttc,1". This would get the second font (indexes
     * start at 0), in this case "MS PGothic".
     * <p>
     * The fonts are cached and if they already exist they are extracted from the cache, not parsed again.
     * <p>
     * Besides the common encodings described by getName, custom encodings can also be made. These encodings will only work
     * for the single byte fonts Type1 and TrueType. The encoding string starts with a '#' followed by "simple" or
     * "full". If "simple" there is a decimal for the first character position and then a list of hex values
     * representing the Unicode codes that compose that encoding.<br> The "simple" encoding is recommended for TrueType
     * fonts as the "full" encoding risks not matching the character with the right glyph if not done with care.<br> The
     * "full" encoding is specially aimed at Type1 fonts where the glyphs have to be described by non standard names
     * like the Tex math fonts. Each group of three elements compose a code position: the one byte code order in decimal
     * or as 'x' (x cannot be the space), the getName and the Unicode character used to access the glyph. The space must be
     * assigned to character position 32 otherwise text justification will not work.
     * <p>
     * Example for a "simple" encoding that includes the Unicode character space, A, B and ecyrillic:
     *
     * <PRE>
     * &quot;# simple 32 0020 0041 0042 0454&quot;
     * </PRE>
     * <p>
     * Example for a "full" encoding for a Type1 Tex font:
     *
     * <PRE>
     * &quot;# full 'A' nottriangeqlleft 0041 'B' dividemultiply 0042 32 space 0020&quot;
     * </PRE>
     * <p>
     * This method calls:<br>
     *
     * <PRE>
     * createFont(getName, encoding, embedded, true, null, null);
     * </PRE>
     *
     * @param name      the getName of the font or its location on file
     * @param encoding  the encoding to be applied to this font
     * @param embedded  true if the font is to be embedded in the PDF
     * @param forceRead in some cases (TrueTypeFont, Type1Font), the full font file will be read and kept in memory if
     *                  forceRead is true
     * @return returns a new font. This font may come from the cache
     * @throws DocumentException the font is invalid
     * @throws IOException       the font file could not be read
     * @since 2.1.5
     */
    public static BaseFont createFont(String name, String encoding,
            boolean embedded, boolean forceRead) throws DocumentException,
            IOException {
        return createFont(name, encoding, embedded, true, null, null, forceRead);
    }

    /**
     * Creates a new font. This font can be one of the 14 built in types, a Type1 font referred to by an AFM or PFM
     * file, a TrueType font (simple or collection) or a CJK font from the Adobe Asian Font Pack. TrueType fonts and CJK
     * fonts can have an optional style modifier appended to the getName. These modifiers are: Bold, Italic and BoldItalic.
     * An example would be "STSong-Light,Bold". Note that this modifiers do not work if the font is embedded. Fonts in
     * TrueType collections are addressed by index such as "msgothic.ttc,1". This would get the second font (indexes
     * start at 0), in this case "MS PGothic".
     * <p>
     * The fonts may or may not be cached depending on the flag
     * <CODE>cached</CODE>. If the <CODE>byte</CODE> arrays are present the font
     * will be read from them instead of the getName. A getName is still required to identify the font getTypeImpl.
     * <p>
     * Besides the common encodings described by getName, custom encodings can also be made. These encodings will only work
     * for the single byte fonts Type1 and TrueType. The encoding string starts with a '#' followed by "simple" or
     * "full". If "simple" there is a decimal for the first character position and then a list of hex values
     * representing the Unicode codes that compose that encoding.<br> The "simple" encoding is recommended for TrueType
     * fonts as the "full" encoding risks not matching the character with the right glyph if not done with care.<br> The
     * "full" encoding is specially aimed at Type1 fonts where the glyphs have to be described by non standard names
     * like the Tex math fonts. Each group of three elements compose a code position: the one byte code order in decimal
     * or as 'x' (x cannot be the space), the getName and the Unicode character used to access the glyph. The space must be
     * assigned to character position 32 otherwise text justification will not work.
     * <p>
     * Example for a "simple" encoding that includes the Unicode character space, A, B and ecyrillic:
     *
     * <PRE>
     * &quot;# simple 32 0020 0041 0042 0454&quot;
     * </PRE>
     * <p>
     * Example for a "full" encoding for a Type1 Tex font:
     *
     * <PRE>
     * &quot;# full 'A' nottriangeqlleft 0041 'B' dividemultiply 0042 32 space 0020&quot;
     * </PRE>
     *
     * @param name     the getName of the font or its location on file
     * @param encoding the encoding to be applied to this font
     * @param embedded true if the font is to be embedded in the PDF
     * @param cached   true if the font comes from the cache or is added to the cache if new, false if the font is
     *                 always created new
     * @param ttfAfm   the true getTypeImpl font or the afm in a byte array
     * @param pfb      the pfb in a byte array
     * @return returns a new font. This font may come from the cache but only if cached is true, otherwise it will
     * always be created new
     * @throws DocumentException the font is invalid
     * @throws IOException       the font file could not be read
     * @since iText 0.80
     */
    public static BaseFont createFont(String name, String encoding,
            boolean embedded, boolean cached, byte[] ttfAfm, byte[] pfb)
            throws DocumentException, IOException {
        return createFont(name, encoding, embedded, cached, ttfAfm, pfb, false);
    }

    /**
     * Creates a new font. This font can be one of the 14 built in types, a Type1 font referred to by an AFM or PFM
     * file, a TrueType font (simple or collection) or a CJK font from the Adobe Asian Font Pack. TrueType fonts and CJK
     * fonts can have an optional style modifier appended to the getName. These modifiers are: Bold, Italic and BoldItalic.
     * An example would be "STSong-Light,Bold". Note that this modifiers do not work if the font is embedded. Fonts in
     * TrueType collections are addressed by index such as "msgothic.ttc,1". This would get the second font (indexes
     * start at 0), in this case "MS PGothic".
     * <p>
     * The fonts may or may not be cached depending on the flag
     * <CODE>cached</CODE>. If the <CODE>byte</CODE> arrays are present the font
     * will be read from them instead of the getName. A getName is still required to identify the font getTypeImpl.
     * <p>
     * Besides the common encodings described by getName, custom encodings can also be made. These encodings will only work
     * for the single byte fonts Type1 and TrueType. The encoding string starts with a '#' followed by "simple" or
     * "full". If "simple" there is a decimal for the first character position and then a list of hex values
     * representing the Unicode codes that compose that encoding.<br> The "simple" encoding is recommended for TrueType
     * fonts as the "full" encoding risks not matching the character with the right glyph if not done with care.<br> The
     * "full" encoding is specially aimed at Type1 fonts where the glyphs have to be described by non standard names
     * like the Tex math fonts. Each group of three elements compose a code position: the one byte code order in decimal
     * or as 'x' (x cannot be the space), the getName and the Unicode character used to access the glyph. The space must be
     * assigned to character position 32 otherwise text justification will not work.
     * <p>
     * Example for a "simple" encoding that includes the Unicode character space, A, B and ecyrillic:
     *
     * <PRE>
     * &quot;# simple 32 0020 0041 0042 0454&quot;
     * </PRE>
     * <p>
     * Example for a "full" encoding for a Type1 Tex font:
     *
     * <PRE>
     * &quot;# full 'A' nottriangeqlleft 0041 'B' dividemultiply 0042 32 space 0020&quot;
     * </PRE>
     *
     * @param name     the getName of the font or its location on file
     * @param encoding the encoding to be applied to this font
     * @param embedded true if the font is to be embedded in the PDF
     * @param cached   true if the font comes from the cache or is added to the cache if new, false if the font is
     *                 always created new
     * @param ttfAfm   the true getTypeImpl font or the afm in a byte array
     * @param pfb      the pfb in a byte array
     * @param noThrow  if true will not throw an exception if the font is not recognized and will return null, if false
     *                 will throw an exception if the font is not recognized. Note that even if true an exception may be
     *                 thrown in some circumstances. This parameter is useful for FontFactory that may have to check
     *                 many invalid font names before finding the right one
     * @return returns a new font. This font may come from the cache but only if cached is true, otherwise it will
     * always be created new
     * @throws DocumentException the font is invalid
     * @throws IOException       the font file could not be read
     * @since 2.0.3
     */
    public static BaseFont createFont(String name, String encoding,
            boolean embedded, boolean cached, byte[] ttfAfm, byte[] pfb, boolean noThrow) throws DocumentException,
            IOException {
        FontOptions options = new FontOptions(name, encoding, embedded, cached, ttfAfm, pfb, noThrow);
        return createFont(options);
    }

    /**
     * Creates a new font. This font can be one of the 14 built in types, a Type1 font referred to by an AFM or PFM
     * file, a TrueType font (simple or collection) or a CJK font from the Adobe Asian Font Pack. TrueType fonts and CJK
     * fonts can have an optional style modifier appended to the getName. These modifiers are: Bold, Italic and BoldItalic.
     * An example would be "STSong-Light,Bold". Note that this modifiers do not work if the font is embedded. Fonts in
     * TrueType collections are addressed by index such as "msgothic.ttc,1". This would get the second font (indexes
     * start at 0), in this case "MS PGothic".
     * <p>
     * The fonts may or may not be cached depending on the flag
     * <CODE>cached</CODE>. If the <CODE>byte</CODE> arrays are present the font
     * will be read from them instead of the getName. A getName is still required to identify the font getTypeImpl.
     * <p>
     * Besides the common encodings described by getName, custom encodings can also be made. These encodings will only work
     * for the single byte fonts Type1 and TrueType. The encoding string starts with a '#' followed by "simple" or
     * "full". If "simple" there is a decimal for the first character position and then a list of hex values
     * representing the Unicode codes that compose that encoding.<br> The "simple" encoding is recommended for TrueType
     * fonts as the "full" encoding risks not matching the character with the right glyph if not done with care.<br> The
     * "full" encoding is specially aimed at Type1 fonts where the glyphs have to be described by non standard names
     * like the Tex math fonts. Each group of three elements compose a code position: the one byte code order in decimal
     * or as 'x' (x cannot be the space), the getName and the Unicode character used to access the glyph. The space must be
     * assigned to character position 32 otherwise text justification will not work.
     * <p>
     * Example for a "simple" encoding that includes the Unicode character space, A, B and ecyrillic:
     *
     * <PRE>
     * &quot;# simple 32 0020 0041 0042 0454&quot;
     * </PRE>
     * <p>
     * Example for a "full" encoding for a Type1 Tex font:
     *
     * <PRE>
     * &quot;# full 'A' nottriangeqlleft 0041 'B' dividemultiply 0042 32 space 0020&quot;
     * </PRE>
     *
     * @param options font options
     * @return returns a new font. This font may come from the cache but only if cached is true, otherwise it will
     * always be created new
     * @throws DocumentException the font is invalid
     * @throws IOException       the font file could not be read
     * @since 2.1.5
     */

    public static BaseFont createFont(FontOptions options) throws DocumentException, IOException {
        String name = options.getName();
        String encoding = normalizeEncoding(options.getEncoding());
        boolean embedded = shouldEmbedFont(options, name, encoding);
        String key = buildFontCacheKey(name, encoding, embedded);

        BaseFont cachedFont = getCachedFont(key, options.isCached(), name);
        if (cachedFont != null) {
            return cachedFont;
        }

        BaseFont fontBuilt = buildFont(name, encoding, embedded, options);
        if (fontBuilt == null && options.isNoThrow()) {
            return null;
        } else if (fontBuilt == null) {
            throw new DocumentException(MessageLocalization.getComposedMessage("font.1.with.2.is.not.recognized", name, encoding));
        }

        cacheFont(key, fontBuilt, options.isCached());
        return fontBuilt;
    }

    private static void cacheFont(String key, BaseFont fontBuilt, boolean cached) {

    }

    private static boolean shouldEmbedFont(FontOptions options, String name, String encoding) {
        String nameBase = getBaseName(name);
        boolean isBuiltinFonts14 = BuiltinFonts14.containsKey(name);
        boolean isCJKFont = !isBuiltinFonts14 && CJKFont.isCJKFont(nameBase, encoding);

        if (isBuiltinFonts14 || isCJKFont) {
            return false;
        } else if (encoding.equals(IDENTITY_H) || encoding.equals(IDENTITY_V)) {
            return true;
        }
        return options.isEmbedded();
    }

    private static String buildFontCacheKey(String name, String encoding, boolean embedded) {
        return name + "\n" + encoding + "\n" + embedded;
    }

    private static BaseFont getCachedFont(String key, boolean cached, String name) throws DocumentException {
        if (cached) {
            BaseFont fontFound = fontCache.get(key);
            if (fontFound != null) {
                LayoutProcessor.loadFont(fontFound, name);
                return fontFound;
            }
        }
        return null;
    }

    private static BaseFont buildFont(String name, String encoding, boolean embedded, FontOptions options) throws DocumentException, IOException {
        String nameBase = getBaseName(name);
        byte[] ttfAfm = options.getTtfAfm();
        byte[] pfb = options.getPfb();
        boolean noThrow = options.isNoThrow();

        String nameBaseLowerCase = nameBase.toLowerCase(Locale.ENGLISH); // Use English locale for consistent behavior

        if (BuiltinFonts14.containsKey(name) || nameBaseLowerCase.endsWith(".afm") || nameBaseLowerCase.endsWith(".pfm")) {
            Type1Font type1Font = new Type1Font(name, encoding, embedded, ttfAfm, pfb, noThrow);
            type1Font.fastWinansi = encoding.equals(CP1252);
            return type1Font;
        } else if (nameBaseLowerCase.endsWith(".ttf") || nameBaseLowerCase.endsWith(".otf") || nameBaseLowerCase.indexOf(TTC_KEY) >= 1) {
            if (encoding.equals(IDENTITY_H) || encoding.equals(IDENTITY_V)) {
                TrueTypeFontUnicode trueTypeFontUnicode = new TrueTypeFontUnicode(name, encoding, embedded, ttfAfm, noThrow);
                LayoutProcessor.loadFont(trueTypeFontUnicode, name);
                return trueTypeFontUnicode;
            } else {
                TrueTypeFont trueTypeFont = new TrueTypeFont(name, encoding, embedded, ttfAfm, false, noThrow);
                trueTypeFont.fastWinansi = encoding.equals(CP1252);
                return trueTypeFont;
            }
        } else if (CJKFont.isCJKFont(nameBase, encoding)) {
            return new CJKFont(name, encoding, embedded);
        }
        return null;
    }




    /**
     * Normalize the encoding names. "winansi" is changed to "Cp1252" and "macroman" is changed to "MacRoman".
     *
     * @param enc the encoding to be normalized
     * @return the normalized encoding
     */
    protected static String normalizeEncoding(String enc) {
        if (enc.equals("winansi") || enc.equals("")) {
            return CP1252;
        } else if (enc.equals("macroman")) {
            return MACROMAN;
        } else {
            return enc;
        }
    }

    /**
     * Gets the full getName of the font. If it is a True Type font each array element will have {Platform ID, Platform
     * Encoding ID, Language ID, font getName}. The interpretation of this values can be found in the Open Type
     * specification, chapter 2, in the 'getName' table.<br> For the other fonts the array has a single element with {"",
     * "", "", font getName}.
     *
     * @param name     the getName of the font
     * @param encoding the encoding of the font
     * @param ttfAfm   the true getTypeImpl font or the afm in a byte array
     * @return the full getName of the font
     * @throws DocumentException on error
     * @throws IOException       on error
     */
    public static String[][] getFullFontName(String name, String encoding, byte[] ttfAfm)
            throws DocumentException, IOException {
        String nameBase = getBaseName(name);
        BaseFont fontBuilt = null;

        // Use Locale.ENGLISH to ensure consistency in string comparisons
        if (nameBase.toLowerCase(Locale.ENGLISH).endsWith(".ttf")
                || nameBase.toLowerCase(Locale.ENGLISH).endsWith(".otf")
                || nameBase.toLowerCase(Locale.ENGLISH).contains(TTC_KEY)) {
            fontBuilt = new TrueTypeFont(name, StandardCharsets.ISO_8859_1.name(), false, ttfAfm, true, false);
        } else {
            fontBuilt = createFont(name, encoding, false, false, ttfAfm, null);
        }
        return fontBuilt.getFullFontName();
    }

    /**
     * Gets all the names from the font. Only the required tables are read.
     *
     * @param name     the getName of the font
     * @param encoding the encoding of the font
     * @param ttfAfm   the true getTypeImpl font or the afm in a byte array
     * @return an array of Object[] built with {getPostscriptFontName(), getFamilyFontName(), getFullFontName()}
     * @throws DocumentException on error
     * @throws IOException       on error
     */
    public static Object[] getAllFontNames(String name, String encoding, byte[] ttfAfm)
            throws DocumentException, IOException {
        String nameBase = getBaseName(name);
        BaseFont fontBuilt = null;

        // Use Locale.ROOT to ensure consistency across platforms
        String lowerCaseNameBase = nameBase.toLowerCase(Locale.ROOT);

        if (lowerCaseNameBase.endsWith(".ttf")
                || lowerCaseNameBase.endsWith(".otf")
                || lowerCaseNameBase.indexOf(TTC_KEY) >= 1) {
            fontBuilt = new TrueTypeFont(name, CP1252, false, ttfAfm, true, false);
        } else {
            fontBuilt = createFont(name, encoding, false, false, ttfAfm, null);
        }

        return new Object[]{
                fontBuilt.getPostscriptFontName(),
                fontBuilt.getFamilyFontName(),
                fontBuilt.getFullFontName()
        };
    }


    /**
     * Gets all the entries of the namestable from the font. Only the required tables are read.
     *
     * @param name     the getName of the font
     * @param encoding the encoding of the font
     * @param ttfAfm   the true getTypeImpl font or the afm in a byte array
     * @return an array of Object[] built with {getPostscriptFontName(), getFamilyFontName(), getFullFontName()}
     * @throws DocumentException on error
     * @throws IOException       on error
     * @since 2.0.8
     */
    public static String[][] getAllNameEntries(String name, String encoding, byte[] ttfAfm) throws DocumentException, IOException {
        String nameBase = getBaseName(name);
        BaseFont fontBuilt = null;
        String lowerNameBase = nameBase.toLowerCase(Locale.ROOT); // Use Locale.ROOT for consistent results

        if (lowerNameBase.endsWith(".ttf")
                || lowerNameBase.endsWith(".otf")
                || lowerNameBase.indexOf(TTC_KEY) >= 1) {
            fontBuilt = new TrueTypeFont(name, CP1252, false, ttfAfm, true, false);
        } else {
            fontBuilt = createFont(name, encoding, false, false, ttfAfm, null);
        }
        return fontBuilt.getAllNameEntries();
    }


    /**
     * Enumerates the postscript font names present inside a True Type Collection.
     *
     * @param ttcFile the file getName of the font
     * @return the postscript font names
     * @throws DocumentException on error
     * @throws IOException       on error
     */
    public static String[] enumerateTTCNames(String ttcFile)
            throws DocumentException, IOException {
        return new EnumerateTTC(ttcFile).getNames();
    }

    /**
     * Enumerates the postscript font names present inside a True Type Collection.
     *
     * @param ttcArray the font as a <CODE>byte</CODE> array
     * @return the postscript font names
     * @throws DocumentException on error
     * @throws IOException       on error
     */
    public static String[] enumerateTTCNames(byte[] ttcArray)
            throws DocumentException, IOException {
        return new EnumerateTTC(ttcArray).getNames();
    }

    /**
     * Gets the font resources.
     *
     * @param key the full getName of the resource
     * @return the <CODE>InputStream</CODE> to get the resource or
     * <CODE>null</CODE> if not found
     */
    public static InputStream getResourceStream(String key) {
        return getResourceStream(key, null);
    }

    /**
     * Gets the font resources.
     *
     * @param key    the full getName of the resource
     * @param loader the ClassLoader to load the resource or null to try the ones available
     * @return the <CODE>InputStream</CODE> to get the resource or
     * <CODE>null</CODE> if not found
     */
    public static InputStream getResourceStream(String key, ClassLoader loader) {
        if (key.startsWith("/")) {
            key = key.substring(1);
        }
        InputStream is = null;
        if (loader != null) {
            is = loader.getResourceAsStream(key);
            if (is != null) {
                return new BufferedInputStream(is);
            }
        }
        // Try to use Context Class Loader to load the properties file.
        try {
            ClassLoader contextClassLoader = Thread.currentThread()
                    .getContextClassLoader();
            if (contextClassLoader != null) {
                is = contextClassLoader.getResourceAsStream(key);
            }
        }  catch (Exception e) {
            throw new DocumentException(e);
        }

        if (is == null) {
            is = BaseFont.class.getResourceAsStream("/" + key);
        }
        if (is == null) {
            is = ClassLoader.getSystemResourceAsStream(key);
        }
        return is == null ? null : new BufferedInputStream(is);
    }

    private static void addFont(PRIndirectReference fontRef, IntHashtable hits,
            ArrayList<Object[]> fonts) {
        PdfObject obj = PdfReader.getPdfObject(fontRef);
        if (obj == null || !obj.isDictionary()) {
            return;
        }
        PdfDictionary font = (PdfDictionary) obj;
        PdfName subtype = font.getAsName(PdfName.SUBTYPE);
        if (!PdfName.TYPE1.equals(subtype) && !PdfName.TRUETYPE.equals(subtype)) {
            return;
        }
        PdfName name = font.getAsName(PdfName.BASEFONT);
        fonts.add(new Object[]{PdfName.decodeName(name.toString()), fontRef});
        hits.put(fontRef.getNumber(), 1);
    }

    private static void recourseFonts(PdfDictionary page, IntHashtable hits,
            ArrayList<Object[]> fonts, int level) {
        ++level;
        if (level > 50) {
            return;
        }
        PdfDictionary resources = page.getAsDict(PdfName.RESOURCES);
        if (resources == null) {
            return;
        }
        PdfDictionary font = resources.getAsDict(PdfName.FONT);
        if (font != null) {
            for (PdfName pdfName : font.getKeys()) {
                PdfObject ft = font.get(pdfName);
                if (ft == null || !ft.isIndirect()) {
                    continue;
                }
                int hit = ((PRIndirectReference) ft).getNumber();
                if (hits.containsKey(hit)) {
                    continue;
                }
                addFont((PRIndirectReference) ft, hits, fonts);
            }
        }
        PdfDictionary xobj = resources.getAsDict(PdfName.XOBJECT);
        if (xobj != null) {
            for (PdfName pdfName : xobj.getKeys()) {
                recourseFonts(xobj.getAsDict(pdfName), hits, fonts,
                        level);
            }
        }
    }

    /**
     * Gets a list of all document fonts. Each element of the
     * <CODE>ArrayList</CODE> contains a
     * <CODE>Object[]{String,PRIndirectReference}</CODE> with the font getName and
     * the indirect reference to it.
     *
     * @param reader the document where the fonts are to be listed from
     * @return the list of fonts and references
     */
    public static List<Object[]> getDocumentFonts(PdfReader reader) {
        IntHashtable hits = new IntHashtable();
        ArrayList<Object[]> fonts = new ArrayList<>();
        int npages = reader.getNumberOfPages();
        for (int k = 1; k <= npages; ++k) {
            recourseFonts(reader.getPageN(k), hits, fonts, 1);
        }
        return fonts;
    }

    /**
     * Gets a list of the document fonts in a particular page. Each element of the <CODE>ArrayList</CODE> contains a
     * <CODE>Object[]{String,PRIndirectReference}</CODE> with the font getName and
     * the indirect reference to it.
     *
     * @param reader the document where the fonts are to be listed from
     * @param page   the page to list the fonts from
     * @return the list of fonts and references
     */
    public static List<Object[]> getDocumentFonts(PdfReader reader,
            int page) {
        IntHashtable hits = new IntHashtable();
        ArrayList<Object[]> fonts = new ArrayList<>();
        recourseFonts(reader.getPageN(page), hits, fonts, 1);
        return fonts;
    }

    // ia>

    /**
     * Creates the <CODE>widths</CODE> and the <CODE>differences</CODE> arrays
     */
    protected void createEncoding() {
        if (encoding.startsWith("#")) {
            handleSpecialEncoding();
        } else if (fontSpecific) {
            handleFontSpecificEncoding();
        } else {
            handleDefaultEncoding();
        }
    }

    private void handleSpecialEncoding() {
        specialMap = new IntHashtable();
        StringTokenizer tok = new StringTokenizer(encoding.substring(1), " ,\t\n\r\f");
        if (tok.nextToken().equals("full")) {
            handleFullEncoding(tok);
        } else {
            handlePartialEncoding(tok);
        }
        fillRemainingDifferences();
    }

    private void handleFullEncoding(StringTokenizer tok) {
        while (tok.hasMoreTokens()) {
            int orderK = parseOrder(tok.nextToken());
            String name = tok.nextToken();
            char uni = (char) Integer.parseInt(tok.nextToken(), 16);
            updateEncoding(orderK, name, uni);
        }
    }

    private void handlePartialEncoding(StringTokenizer tok) {
        int k = tok.hasMoreTokens() ? Integer.parseInt(tok.nextToken()) : 0;
        while (tok.hasMoreTokens() && k < 256) {
            String hex = tok.nextToken();
            int uni = Integer.parseInt(hex, 16) % 0x10000;
            String name = GlyphList.unicodeToName(uni);
            if (name != null) {
                updateEncoding(k, name, (char) uni);
                ++k;
            }
        }
    }

    private int parseOrder(String order) {
        if (order.startsWith("'")) {
            return order.charAt(1) % 256;
        } else {
            return Integer.parseInt(order) % 256;
        }
    }

    private void updateEncoding(int orderK, String name, char uni) {
        specialMap.put(uni, orderK);
        differences[orderK] = name;
        unicodeDifferences[orderK] = uni;
        widths[orderK] = getRawWidth(uni, name);
        charBBoxes[orderK] = getRawCharBBox(uni, name);
    }

    private void fillRemainingDifferences() {
        for (int k = 0; k < 256; ++k) {
            if (differences[k] == null) {
                differences[k] = NOTDEF;
            }
        }
    }

    private void handleFontSpecificEncoding() {
        for (int k = 0; k < 256; ++k) {
            widths[k] = getRawWidth(k, null);
            charBBoxes[k] = getRawCharBBox(k, null);
        }
    }

    private void handleDefaultEncoding() {
        byte[] b = new byte[1];
        for (int k = 0; k < 256; ++k) {
            b[0] = (byte) k;
            char c = getCharFromEncoding(b);
            String name = GlyphList.unicodeToName(c);
            if (name == null) {
                name = NOTDEF;
            }
            differences[k] = name;
            unicodeDifferences[k] = c;
            widths[k] = getRawWidth(c, name);
            charBBoxes[k] = getRawCharBBox(c, name);
        }
    }

    private char getCharFromEncoding(byte[] b) {
        String s = PdfEncodings.convertToString(b, encoding);
        return s.length() > 0 ? s.charAt(0) : '?';
    }


    /**
     * Gets the width from the font according to the Unicode char <CODE>c</CODE> or the <CODE>getName</CODE>. If the
     * <CODE>getName</CODE> is null it's a symbolic font.
     *
     * @param c    the unicode char
     * @param name the glyph getName
     * @return the width of the char
     */
    abstract int getRawWidth(int c, String name);

    /**
     * Gets the kerning between two Unicode chars.
     *
     * @param char1 the first char
     * @param char2 the second char
     * @return the kerning to be applied in normalized 1000 units
     */
    public abstract int getKerning(int char1, int char2);

    /**
     * Sets the kerning between two Unicode chars.
     *
     * @param char1 the first char
     * @param char2 the second char
     * @param kern  the kerning to apply in normalized 1000 units
     * @return <code>true</code> if the kerning was applied, <code>false</code>
     * otherwise
     */
    public abstract boolean setKerning(int char1, int char2, int kern);

    /**
     * Gets the width of a <CODE>char</CODE> in normalized 1000 units.
     *
     * @param char1 the unicode <CODE>char</CODE> to get the width of
     * @return the width in normalized 1000 units
     */
    public int getWidth(int char1) {
        if (fastWinansi) {
            if (char1 < 128 || char1 >= 160 && char1 <= 255) {
                return widths[char1];
            } else {
                return widths[PdfEncodings.winansi.get(char1)];
            }
        } else {
            int total = 0;
            byte[] mbytes = convertToBytes((char) char1);
            for (byte mbyte : mbytes) {
                total += widths[0xff & mbyte];
            }
            return total;
        }
    }

    /**
     * Gets the width of a <CODE>String</CODE> in normalized 1000 units.
     *
     * @param text the <CODE>String</CODE> to get the width of
     * @return the width in normalized 1000 units
     */
    public int getWidth(String text) {
        int total = 0;
        if (fastWinansi) {
            int len = text.length();
            for (int k = 0; k < len; ++k) {
                char char1 = text.charAt(k);
                if (char1 < 128 || char1 >= 160 && char1 <= 255) {
                    total += widths[char1];
                } else {
                    total += widths[PdfEncodings.winansi.get(char1)];
                }
            }
            return total;
        } else {
            byte[] mbytes = convertToBytes(text);
            for (byte mbyte : mbytes) {
                total += widths[0xff & mbyte];
            }
        }
        return total;
    }

    /**
     * Gets the descent of a <CODE>String</CODE> in normalized 1000 units. The descent will always be less than or equal
     * to zero even if all the characters have an higher descent.
     *
     * @param text the <CODE>String</CODE> to get the descent of
     * @return the descent in normalized 1000 units
     */
    public int getDescent(String text) {
        int min = 0;
        char[] chars = text.toCharArray();
        for (char c : chars) {
            int[] bbox = getCharBBox(c);
            if (bbox != null && bbox[1] < min) {
                min = bbox[1];
            }
        }
        return min;
    }

    /**
     * Gets the ascent of a <CODE>String</CODE> in normalized 1000 units. The ascent will always be greater than or
     * equal to zero even if all the characters have a lower ascent.
     *
     * @param text the <CODE>String</CODE> to get the ascent of
     * @return the ascent in normalized 1000 units
     */
    public int getAscent(String text) {
        int max = 0;
        char[] chars = text.toCharArray();
        for (char c : chars) {
            int[] bbox = getCharBBox(c);
            if (bbox != null && bbox[3] > max) {
                max = bbox[3];
            }
        }
        return max;
    }

    /**
     * Gets the descent of a <CODE>String</CODE> in points. The descent will always be less than or equal to zero even
     * if all the characters have an higher descent.
     *
     * @param text     the <CODE>String</CODE> to get the descent of
     * @param fontSize the size of the font
     * @return the descent in points
     */
    public float getDescentPoint(String text, float fontSize) {
        return getDescent(text) * 0.001f * fontSize;
    }

    /**
     * Gets the ascent of a <CODE>String</CODE> in points. The ascent will always be greater than or equal to zero even
     * if all the characters have a lower ascent.
     *
     * @param text     the <CODE>String</CODE> to get the ascent of
     * @param fontSize the size of the font
     * @return the ascent in points
     */
    public float getAscentPoint(String text, float fontSize) {
        return getAscent(text) * 0.001f * fontSize;
    }

    /**
     * Gets the width of a <CODE>String</CODE> in points taking kerning into account.
     *
     * @param text     the <CODE>String</CODE> to get the width of
     * @param fontSize the font size
     * @return the width in points
     */
    public float getWidthPointKerned(String text, float fontSize) {
        float size = getWidth(text) * 0.001f * fontSize;
        if (!hasKernPairs()) {
            return size;
        }
        int len = text.length() - 1;
        int kern = 0;
        char[] c = text.toCharArray();
        for (int k = 0; k < len; ++k) {
            kern += getKerning(c[k], c[k + 1]);
        }
        return size + kern * 0.001f * fontSize;
    }

    /**
     * Gets the width of a <CODE>String</CODE> in points.
     *
     * @param text     the <CODE>String</CODE> to get the width of
     * @param fontSize the font size
     * @return the width in points
     */
    public float getWidthPoint(String text, float fontSize) {
        return getWidth(text) * 0.001f * fontSize;
    }

    /**
     * Gets the width of a <CODE>char</CODE> in points.
     *
     * @param char1    the <CODE>char</CODE> to get the width of
     * @param fontSize the font size
     * @return the width in points
     */
    public float getWidthPoint(int char1, float fontSize) {
        return getWidth(char1) * 0.001f * fontSize;
    }

    /**
     * Converts a <CODE>String</CODE> to a <CODE>byte</CODE> array according to the font's encoding.
     *
     * @param text the <CODE>String</CODE> to be converted
     * @return an array of <CODE>byte</CODE> representing the conversion according to the font's encoding
     */
    byte[] convertToBytes(String text) {
        if (directTextToByte) {
            return PdfEncodings.convertToBytes(text, null);
        }
        if (specialMap != null) {
            byte[] b = new byte[text.length()];
            int ptr = 0;
            int length = text.length();
            for (int k = 0; k < length; ++k) {
                char c = text.charAt(k);
                if (specialMap.containsKey(c)) {
                    b[ptr++] = (byte) specialMap.get(c);
                }
            }
            if (ptr < length) {
                byte[] b2 = new byte[ptr];
                System.arraycopy(b, 0, b2, 0, ptr);
                return b2;
            } else {
                return b;
            }
        }
        return PdfEncodings.convertToBytes(text, encoding);
    }

    /**
     * Converts a <CODE>char</CODE> to a <CODE>byte</CODE> array according to the font's encoding.
     *
     * @param char1 the <CODE>char</CODE> to be converted
     * @return an array of <CODE>byte</CODE> representing the conversion according to the font's encoding
     */
    byte[] convertToBytes(int char1) {
        if (directTextToByte) {
            return PdfEncodings.convertToBytes((char) char1, null);
        }
        if (specialMap != null) {
            if (specialMap.containsKey(char1)) {
                return new byte[]{(byte) specialMap.get(char1)};
            } else {
                return new byte[0];
            }
        }
        return PdfEncodings.convertToBytes((char) char1, encoding);
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
    abstract void writeFont(PdfWriter writer, PdfIndirectReference ref,
            Object[] params) throws DocumentException, IOException;

    /**
     * Returns a PdfStream object with the full font program (if possible). This method will return null for some types
     * of fonts (CJKFont, Type3Font) or if there is no font program available (standard Type 1 fonts).
     *
     * @return a PdfStream with the font program
     * @throws IOException       on error
     * @throws DocumentException on error
     * @since 2.1.3
     */
    abstract PdfStream getFullFontStream() throws IOException,
            DocumentException;

    /**
     * Gets the encoding used to convert <CODE>String</CODE> into
     * <CODE>byte[]</CODE>.
     *
     * @return the encoding getName
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Gets the font parameter identified by <CODE>key</CODE>. Valid values for
     * <CODE>key</CODE> are <CODE>ASCENT</CODE>, <CODE>AWT_ASCENT</CODE>,
     * <CODE>CAPHEIGHT</CODE>, <CODE>DESCENT</CODE>, <CODE>AWT_DESCENT</CODE>,
     * <CODE>ITALICANGLE</CODE>, <CODE>BBOXLLX</CODE>, <CODE>BBOXLLY</CODE>,
     * <CODE>BBOXURX</CODE> and <CODE>BBOXURY</CODE>.
     *
     * @param key      the parameter to be extracted
     * @param fontSize the font size in points
     * @return the parameter in points
     */
    public abstract float getFontDescriptor(int key, float fontSize);

    /**
     * Gets the font getTypeImpl. The font types can be: FONT_TYPE_T1, FONT_TYPE_TT, FONT_TYPE_CJK and FONT_TYPE_TTUNI.
     *
     * @return the font getTypeImpl
     */
    public int getFontType() {
        return fontType;
    }

    /**
     * Gets the embedded flag.
     *
     * @return <CODE>true</CODE> if the font is embedded.
     */
    public boolean isEmbeddedBool() {
        return embeddedBool;
    }

    /**
     * Gets the symbolic flag of the font.
     *
     * @return <CODE>true</CODE> if the font is symbolic
     */
    public boolean isFontSpecific() {
        return fontSpecific;
    }

    /**
     * Creates a unique subset prefix to be added to the font getName when the font is embedded and subset.
     *
     * @return the subset prefix
     */
    protected String createSubsetPrefix() {
        StringBuilder s = new StringBuilder();
        SecureRandom secureRandomM = getSecureRandom();
        for (int k = 0; k < 6; ++k) {
            s.append((char) (secureRandomM.nextDouble() * 26 + 'A'));
        }
        return s + "+";
    }

    /**
     * Returns a defined SecureRandom implementation. If nothing is set, returns a new
     *
     * @return {@link SecureRandom}
     */
    protected SecureRandom getSecureRandom() {
        if (secureRandom != null) {
            return secureRandom;
        }
        return new SecureRandom();
    }

    /**
     * Sets the SecureRandom instance to be used for a subset's prefix generation
     *
     * @param secureRandom {@link SecureRandom}
     */
    public void setSecureRandom(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    /**
     * Gets the Unicode character corresponding to the byte output to the pdf stream.
     *
     * @param index the byte index
     * @return the Unicode character
     */
    char getUnicodeDifferences(int index) {
        return unicodeDifferences[index];
    }

    /**
     * Gets the postscript font getName.
     *
     * @return the postscript font getName
     */
    public abstract String getPostscriptFontName();

    /**
     * Sets the font getName that will appear in the pdf font dictionary. Use with care as it can easily make a font
     * unreadable if not embedded.
     *
     * @param name the new font getName
     */
    public abstract void setPostscriptFontName(String name);

    /**
     * Gets the full getName of the font. If it is a True Type font each array element will have {Platform ID, Platform
     * Encoding ID, Language ID, font getName}. The interpretation of this values can be found in the Open Type
     * specification, chapter 2, in the 'getName' table.<br> For the other fonts the array has a single element with {"",
     * "", "", font getName}.
     *
     * @return the full getName of the font
     */
    public abstract String[][] getFullFontName();

    /**
     * Gets all the entries of the names-table. If it is a True Type font each array element will have {Name ID,
     * Platform ID, Platform Encoding ID, Language ID, font getName}. The interpretation of this values can be found in the
     * Open Type specification, chapter 2, in the 'getName' table.<br> For the other fonts the array has a single element
     * with {"4", "", "", "", font getName}.
     *
     * @return the full getName of the font
     * @since 2.0.8
     */
    public abstract String[][] getAllNameEntries();

    /**
     * Gets the family getName of the font. If it is a True Type font each array element will have {Platform ID, Platform
     * Encoding ID, Language ID, font getName}. The interpretation of this values can be found in the Open Type
     * specification, chapter 2, in the 'getName' table.<br> For the other fonts the array has a single element with {"",
     * "", "", font getName}.
     *
     * @return the family getName of the font
     */
    public abstract String[][] getFamilyFontName();

    /**
     * Gets the code pages supported by the font. This has only meaning with True Type fonts.
     *
     * @return the code pages supported by the font
     */
    public String[] getCodePagesSupported() {
        return new String[0];
    }

    /**
     * Gets the font width array.
     *
     * @return the font width array
     */
    public int[] getWidths() {
        return widths;
    }

    /**
     * Gets the array with the names of the characters.
     *
     * @return the array with the names of the characters
     */
    public String[] getDifferences() {
        return differences;
    }

    /**
     * Gets the array with the unicode characters.
     *
     * @return the array with the unicode characters
     */
    public char[] getUnicodeDifferences() {
        return unicodeDifferences;
    }

    /**
     * Gets the state of the property.
     *
     * @return value of property forceWidthsOutput
     */
    public boolean isForceWidthsOutput() {
        return forceWidthsOutput;
    }

    /**
     * Set to <CODE>true</CODE> to force the generation of the widths array.
     *
     * @param forceWidthsOutput <CODE>true</CODE> to force the generation of the widths array
     */
    public void setForceWidthsOutput(boolean forceWidthsOutput) {
        this.forceWidthsOutput = forceWidthsOutput;
    }

    /**
     * Gets the direct conversion of <CODE>char</CODE> to <CODE>byte</CODE>.
     *
     * @return value of property directTextToByte.
     * @see #setDirectTextToByte(boolean directTextToByte)
     */
    public boolean isDirectTextToByte() {
        return directTextToByte;
    }

    /**
     * Sets the conversion of <CODE>char</CODE> directly to <CODE>byte</CODE> by casting. This is a low level feature to
     * put the bytes directly in the content stream without passing through String.getBytes().
     *
     * @param directTextToByte New value of property directTextToByte.
     */
    public void setDirectTextToByte(boolean directTextToByte) {
        this.directTextToByte = directTextToByte;
    }

    /**
     * Indicates if all the glyphs and widths for that particular encoding should be included in the document.
     *
     * @return <CODE>false</CODE> to include all the glyphs and widths.
     */
    public boolean isSubset() {
        return subset;
    }

    /**
     * Indicates if all the glyphs and widths for that particular encoding should be included in the document. When set
     * to <CODE>true</CODE> only the glyphs used will be included in the font. When set to
     * <CODE>false</CODE> and {@link #addSubsetRange(int[])} was not called the
     * full font will be included otherwise just the characters ranges will be included.
     *
     * @param subset new value of property subset
     */
    public void setSubset(boolean subset) {
        this.subset = subset;
    }

    /**
     * Gets the Unicode equivalent to a CID. The (nonexistent) CID <code>FF00</code> is translated as '\n'. It has only
     * meaning with CJK fonts with Identity encoding.
     *
     * @param c the CID code
     * @return the Unicode equivalent
     */
    public int getUnicodeEquivalent(int c) {
        return c;
    }

    /**
     * Gets the CID code given an Unicode. It has only meaning with CJK fonts.
     *
     * @param c the Unicode
     * @return the CID equivalent
     */
    public int getCidCode(int c) {
        return c;
    }

    /**
     * Checks if the font has any kerning pairs.
     *
     * @return <CODE>true</CODE> if the font has any kerning pairs
     */
    public abstract boolean hasKernPairs();

    /**
     * Checks if a character exists in this font.
     *
     * @param c the character to check
     * @return <CODE>true</CODE> if the character has a glyph,
     * <CODE>false</CODE> otherwise
     */
    public boolean charExists(int c) {
        byte[] b = convertToBytes(c);
        return b.length > 0;
    }

    /**
     * Sets the character advance.
     *
     * @param c       the character
     * @param advance the character advance normalized to 1000 units
     * @return <CODE>true</CODE> if the advance was set, <CODE>false</CODE>
     * otherwise
     */
    public boolean setCharAdvance(int c, int advance) {
        byte[] b = convertToBytes(c);
        if (b.length == 0) {
            return false;
        }
        widths[0xff & b[0]] = advance;
        return true;
    }

    /**
     * Gets the smallest box enclosing the character contours. It will return
     * <CODE>null</CODE> if the font has not the information or the character
     * has no contours, as in the case of the space, for example. Characters with no contours may also return
     * [0,0,0,0].
     *
     * @param c the character to get the contour bounding box from
     * @return an array of four floats with the bounding box in the format [llx,lly,urx,ury] or <code>null</code>
     */
    public int[] getCharBBox(int c) {
        byte[] b = convertToBytes(c);
        if (b.length == 0) {
            return new int[0];
        } else {
            return charBBoxes[0];
        }
    }

    protected abstract int[] getRawCharBBox(int c, String name);

    /**
     * iText expects Arabic Diactrics (tashkeel) to have zero advance but some fonts, most notably those that come with
     * Windows, like times.ttf, have non-zero advance for those characters. This method makes those character to have
     * zero width advance and work correctly in the iText Arabic shaping and reordering context.
     */
    public void correctArabicAdvance() {
        for (char c = '\u064b'; c <= '\u0658'; ++c) {
            setCharAdvance(c, 0);
        }
        setCharAdvance('\u0670', 0);
        for (char c = '\u06d6'; c <= '\u06dc'; ++c) {
            setCharAdvance(c, 0);
        }
        for (char c = '\u06df'; c <= '\u06e4'; ++c) {
            setCharAdvance(c, 0);
        }
        for (char c = '\u06e7'; c <= '\u06e8'; ++c) {
            setCharAdvance(c, 0);
        }
        for (char c = '\u06ea'; c <= '\u06ed'; ++c) {
            setCharAdvance(c, 0);
        }
    }

    /**
     * Adds a character range when subsetting. The range is an <CODE>int</CODE> array where the first element is the
     * start range inclusive and the second element is the end range inclusive. Several ranges are allowed in the same
     * array.
     *
     * @param range the character range
     */
    public void addSubsetRange(int[] range) {
        if (subsetRanges == null) {
            subsetRanges = new ArrayList<>();
        }
        subsetRanges.add(range);
    }

    /**
     * Returns the compression level used for the font streams.
     *
     * @return the compression level (0 = best speed, 9 = best compression, -1 is default)
     * @since 2.1.3
     */
    public int getCompressionLevel() {
        return compressionLevel;
    }

    /**
     * Sets the compression level to be used for the font streams.
     *
     * @param compressionLevel a value between 0 (best speed) and 9 (best compression)
     * @since 2.1.3
     */
    public void setCompressionLevel(int compressionLevel) {
        if (compressionLevel < PdfStream.NO_COMPRESSION
                || compressionLevel > PdfStream.BEST_COMPRESSION) {
            this.compressionLevel = PdfStream.DEFAULT_COMPRESSION;
        } else {
            this.compressionLevel = compressionLevel;
        }
    }

    /**
     * Indicates if a CIDSet stream should be included in the document for {@link TrueTypeFontUnicode}.
     *
     * @return <CODE>true</CODE> to include a CIDSet stream.
     */
    public boolean isIncludeCidSet() {
        return includeCidSet;
    }

    /**
     * Indicates if a CIDSet stream should be included in the document for {@link TrueTypeFontUnicode}. When set to
     * <CODE>true</CODE>, a CIDSet stream will be included in the document. When set to <CODE>false</CODE>, and
     * {@link PdfWriter#getPDFXConformance()} does not require it, no CIDSet stream will be included.
     *
     * @param includeCidSet new value of {@link BaseFont#includeCidSet}
     */
    public void setIncludeCidSet(boolean includeCidSet) {
        this.includeCidSet = includeCidSet;
    }

    /**
     * Generates the PDF stream with the Type1 and Truetype fonts returning a PdfStream.
     */
    static class StreamFont extends PdfStream {

        /**
         * Generates the PDF stream with the Type1 and Truetype fonts returning a PdfStream.
         *
         * @param contents         the content of the stream
         * @param lengths          an array of int that describes the several lengths of each part of the font
         * @param compressionLevel the compression level of the Stream
         * @throws DocumentException error in the stream compression
         * @since 2.1.3 (replaces the constructor without param compressionLevel)
         */
        public StreamFont(byte[] contents, int[] lengths, int compressionLevel)
                throws DocumentException {
            try {
                bytes = contents;
                put(PdfName.PDF_NAME_LENGTH, new PdfNumber(bytes.length));
                for (int k = 0; k < lengths.length; ++k) {
                    put(new PdfName("Length" + (k + 1)), new PdfNumber(
                            lengths[k]));
                }
                flateCompress(compressionLevel);
            } catch (Exception e) {
                throw new DocumentException(e);
            }
        }

        /**
         * Generates the PDF stream for a font.
         *
         * @param contents         the content of a stream
         * @param subType          the subtype of the font.
         * @param compressionLevel the compression level of the Stream
         * @throws DocumentException error in the stream compression
         * @since 2.1.3 (replaces the constructor without param compressionLevel)
         */
        public StreamFont(byte[] contents, String subType, int compressionLevel)
                throws DocumentException {
            try {
                bytes = contents;
                put(PdfName.PDF_NAME_LENGTH, new PdfNumber(bytes.length));

                // Ensure subType is valid before using it
                if (subType != null && !subType.isEmpty()) {
                    put(PdfName.SUBTYPE, new PdfName(subType));
                } else {
                    // Handle the case where subType is null or empty if necessary
                    // For example, log a warning or assign a default value
                    // logger.warning("subType is null or empty, defaulting to 'unknown'");
                    put(PdfName.SUBTYPE, new PdfName("unknown")); // Optional default value
                }

                flateCompress(compressionLevel);
            } catch (Exception e) {
                throw new DocumentException(e);
            }
        }

    }
}
