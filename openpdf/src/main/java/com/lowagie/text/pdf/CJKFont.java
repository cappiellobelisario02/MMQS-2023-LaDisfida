/*
 * $Id: CJKFont.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2000, 2001, 2002 by Paulo Soares.
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
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import static com.ibm.icu.util.ULocale.getBaseName;

/**
 * Creates a CJK font compatible with the fonts in the Adobe Asian font Pack.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */

class CJKFont extends BaseFont {

    /**
     * The encoding used in the PDF document for CJK fonts
     */
    static final String CJK_ENCODING = "UnicodeBigUnmarked";
    private static final int FIRST = 0;
    private static final int BRACKET = 1;
    private static final int SERIAL = 2;
    private static final int V1Y = 880;

    static Properties cjkFonts = new Properties();
    static Properties cjkEncodings = new Properties();
    static ConcurrentHashMap<String, HashMap<Object, Object>> allFonts = new ConcurrentHashMap<>(
            500, 0.85f, 64);
    private static boolean propertiesLoaded = false;
    private static Object initLock = new Object();
    HashMap<String, char[]> allCMaps = new HashMap<>();
    /**
     * The font getName
     */
    private String fontName;
    /**
     * The style modifier
     */
    private String style = "";
    /**
     * The CMap getName associated with this font
     */
    private String cMap;

    private boolean cidDirect = false;

    private char[] translationMap;
    private IntHashtable vMetrics;
    private IntHashtable hMetrics;
    private HashMap<Object, Object> fontDesc;
    private boolean vertical = false;

    private static final Logger logger = Logger.getLogger(CJKFont.class.getName());

    /**
     * Creates a CJK font.
     *
     * @param fontName the getName of the font
     * @param enc      the encoding of the font
     * @param emb      always <CODE>false</CODE>. CJK font and not embedded
     * @throws DocumentException on error
     */
    CJKFont(String fontName, String enc, boolean emb) throws DocumentException {
        loadProperties();
        fontType = FONT_TYPE_CJK;
        String nameBase = getBaseName(fontName);

        validateCJKFont(nameBase, fontName, enc);

        if (nameBase.length() < fontName.length()) {
            style = fontName.substring(nameBase.length());
            fontName = nameBase;
        }

        this.fontName = fontName;
        encoding = CJK_ENCODING;
        vertical = enc.endsWith("V");
        cMap = enc;

        if (enc.startsWith("Identity-")) {
            handleIdentityEncoding(fontName);
        } else {
            handleNonIdentityEncoding(enc);
        }

        loadFontMetrics(fontName);
    }

    private void validateCJKFont(String nameBase, String fontName, String enc) throws DocumentException {
        if (!isCJKFont(nameBase, enc)) {
            throw new DocumentException(MessageLocalization.getComposedMessage(
                    "font.1.with.2.encoding.is.not.a.cjk.font", fontName, enc));
        }
    }

    private void handleIdentityEncoding(String fontName) throws DocumentException {
        cidDirect = true;
        String s = cjkFonts.getProperty(fontName);
        s = s.substring(0, s.indexOf('_'));
        char[] c = allCMaps.get(s);
        if (c == null) {
            c = readCMapOrThrow(s);
            c[CID_NEWLINE] = '\n';
            allCMaps.put(s, c);
        }
        translationMap = c;
    }

    private void handleNonIdentityEncoding(String enc) throws DocumentException {
        char[] c = allCMaps.get(enc);
        if (c == null) {
            String s = cjkEncodings.getProperty(enc);
            if (s == null) {
                throw new DocumentException(
                        MessageLocalization.getComposedMessage(
                                "the.resource.cjkencodings.properties.does.not.contain.the.encoding.1", enc));
            }
            c = loadCMapWithFallback(enc, s);
        }
        translationMap = c;
    }

    private char[] loadCMapWithFallback(String enc, String s) throws DocumentException {
        StringTokenizer tk = new StringTokenizer(s);
        String nt = tk.nextToken();
        char[] c = allCMaps.get(nt);
        if (c == null) {
            c = readCMap(nt);
            allCMaps.put(nt, c);
        }
        if (tk.hasMoreTokens()) {
            String nt2 = tk.nextToken();
            char[] m2 = readCMap(nt2);
            mergeCMaps(c, m2);
            allCMaps.put(enc, m2);
            c = m2;
        }
        return c;
    }

    private void mergeCMaps(char[] c, char[] m2) {
        for (int k = 0; k < 0x10000; ++k) {
            if (m2[k] == 0) {
                m2[k] = c[k];
            }
        }
    }

    private char[] readCMapOrThrow(String s) throws DocumentException {
        return readCMap(s);
    }

    private void loadFontMetrics(String fontName) throws DocumentException {
        fontDesc = allFonts.get(fontName);
        if (fontDesc == null) {
            fontDesc = readFontProperties(fontName);
            allFonts.putIfAbsent(fontName, fontDesc);
            fontDesc = allFonts.get(fontName);
        }
        hMetrics = (IntHashtable) fontDesc.get("W");
        vMetrics = (IntHashtable) fontDesc.get("W2");
    }


    private static void loadProperties() {
        if (propertiesLoaded) {
            return;
        }
        synchronized (initLock) {
            if (propertiesLoaded) {
                return;
            }
            try {
                // Use try-with-resources to ensure InputStream is closed properly
                try (InputStream is = getResourceStream(RESOURCE_PATH + "cjkfonts.properties")) {
                    cjkFonts.load(is);
                }
                try (InputStream is = getResourceStream(RESOURCE_PATH + "cjkencodings.properties")) {
                    cjkEncodings.load(is);
                }
            } catch (IOException e) {
                // Log the exception for better debugging
                logger.severe("Error loading properties: " + e.getMessage());
                // Reset properties to new empty instances
                cjkFonts = new Properties();
                cjkEncodings = new Properties();
            }
            propertiesLoaded = true;
        }
    }


    /**
     * Checks if its a valid CJK font.
     *
     * @param fontName the font getName
     * @param enc      the encoding
     * @return <CODE>true</CODE> if it is CJK font
     */
    public static boolean isCJKFont(String fontName, String enc) {
        loadProperties();
        String encodings = cjkFonts.getProperty(fontName);
        return encodings != null
                && (enc.equals("Identity-H") || enc.equals("Identity-V") || encodings.contains("_" + enc + "_"));
    }

    static char[] readCMap(String name) {
        name = name + ".cmap";  // Append .cmap to the filename

        // Using try-with-resources for automatic resource management
        try (InputStream is = getResourceStream(RESOURCE_PATH + name)) {
            char[] c = new char[0x10000];  // Initialize a character array to hold the cmap values

            // Read characters from the input stream
            for (int k = 0; k < 0x10000; ++k) {
                c[k] = (char) ((is.read() << 8) + is.read());
            }

            return c;  // Return the character array after reading
        } catch (IOException e) {
            // Log the exception if needed (optional)
            logger.severe("Error reading CMap");
        }

        return new char[0];  // Return an empty array on failure
    }

    static IntHashtable createMetric(String s) {
        IntHashtable h = new IntHashtable();
        StringTokenizer tk = new StringTokenizer(s);
        while (tk.hasMoreTokens()) {
            int n1 = Integer.parseInt(tk.nextToken());
            h.put(n1, Integer.parseInt(tk.nextToken()));
        }
        return h;
    }

    static String convertToHCIDMetrics(int[] keys, IntHashtable h) {
        if (keys.length == 0) {
            return null;
        }

        int[] startValues = findStartValues(keys, h);
        int start = startValues[0];
        int lastCid = startValues[1];
        int lastValue = startValues[2];

        if (lastValue == 0) {
            return null;
        }

        StringBuilder buf = new StringBuilder();
        buf.append('[');
        buf.append(lastCid);

        processKeys(keys, h, start, lastCid, lastValue, buf);

        finalizeBuffer(buf, lastValue);

        return buf.toString();
    }

    private static int[] findStartValues(int[] keys, IntHashtable h) {
        int lastCid = 0;
        int lastValue = 0;
        int start;
        for (start = 0; start < keys.length; ++start) {
            lastCid = keys[start];
            lastValue = h.get(lastCid);
            if (lastValue != 0) {
                ++start;
                break;
            }
        }
        return new int[] {start, lastCid, lastValue};
    }

    private static void processKeys(int[] keys, IntHashtable h, int start, int lastCid, int lastValue, StringBuilder buf) {
        int state = FIRST;

        for (int k = start; k < keys.length; ++k) {
            int cid = keys[k];
            int value = h.get(cid);
            if (value == 0) {
                continue;
            }

            state = updateBufferBasedOnState(buf, state, cid, value, lastCid, lastValue);

            lastValue = value;
            lastCid = cid;
        }

        finalizeState(buf, state, lastCid, lastValue);
    }

    private static int updateBufferBasedOnState(StringBuilder buf, int state, int cid, int value, int lastCid, int lastValue) {
        return switch (state) {
            case FIRST -> handleFirstState(buf, cid, value, lastCid, lastValue);
            case BRACKET -> handleBracketState(buf, cid, value, lastCid, lastValue);
            case SERIAL -> handleSerialState(buf, cid, value, lastCid, lastValue);
            default -> state;
        };
    }

    private static int handleFirstState(StringBuilder buf, int cid, int value, int lastCid, int lastValue) {
        if (cid == lastCid + 1 && value == lastValue) {
            return SERIAL;
        } else if (cid == lastCid + 1) {
            buf.append('[').append(lastValue);
            return BRACKET;
        } else {
            buf.append('[').append(lastValue).append(']').append(cid);
            return FIRST;
        }
    }

    private static int handleBracketState(StringBuilder buf, int cid, int value, int lastCid, int lastValue) {
        if (cid == lastCid + 1 && value == lastValue) {
            buf.append(']').append(lastCid);
            return SERIAL;
        } else if (cid == lastCid + 1) {
            buf.append(' ').append(lastValue);
            return BRACKET;
        } else {
            buf.append(' ').append(lastValue).append(']').append(cid);
            return FIRST;
        }
    }

    private static int handleSerialState(StringBuilder buf, int cid, int value, int lastCid, int lastValue) {
        if (cid != lastCid + 1 || value != lastValue) {
            buf.append(' ').append(lastCid).append(' ').append(lastValue).append(' ').append(cid);
            return FIRST;
        }
        return SERIAL;
    }

    private static void finalizeState(StringBuilder buf, int state, int lastCid, int lastValue) {
        switch (state) {
            case FIRST:
                buf.append('[').append(lastValue).append("]]");
                break;
            case BRACKET:
                buf.append(' ').append(lastValue).append("]]");
                break;
            case SERIAL:
                buf.append(' ').append(lastCid).append(' ').append(lastValue).append(']');
                break;
            default:
                break;
        }
    }

    private static void finalizeBuffer(StringBuilder buf, int lastValue) {
        // Final adjustments to the buffer if necessary (e.g., closing brackets)
        buf.append(' ').append(lastValue).append(']');
    }


    static String convertToVCIDMetrics(int[] keys, IntHashtable v, IntHashtable h) {
        if (keys.length == 0) {
            return null;
        }

        ConversionContext context = initializeContext(keys, v, h);
        if (context == null) {
            return null;
        }

        StringBuilder buf = new StringBuilder();
        buf.append('[');
        buf.append(context.lastCid);

        processKeys(keys, v, h, context, buf);

        appendFinalEntry(buf, context);
        buf.append(" ]");

        return buf.toString();
    }

    private static ConversionContext initializeContext(int[] keys, IntHashtable v, IntHashtable h) {
        int lastCid = 0;
        int lastValue = 0;
        int lastHValue = 0;
        int start;

        for (start = 0; start < keys.length; ++start) {
            lastCid = keys[start];
            lastValue = v.get(lastCid);
            if (lastValue != 0) {
                ++start;
                break;
            } else {
                lastHValue = h.get(lastCid);
            }
        }

        if (lastValue == 0) {
            return null;
        }

        if (lastHValue == 0) {
            lastHValue = 1000;
        }

        return new ConversionContext(start, lastCid, lastValue, lastHValue);
    }

    private static void processKeys(int[] keys, IntHashtable v, IntHashtable h, ConversionContext context, StringBuilder buf) {
        int state = FIRST;

        for (int k = context.start; k < keys.length; ++k) {
            int cid = keys[k];
            int value = v.get(cid);

            if (value == 0) {
                continue;
            }

            int hValue = h.get(context.lastCid);
            if (hValue == 0) {
                hValue = 1000;
            }

            state = processKey(buf, context, cid, value, hValue, state);

            context.lastValue = value;
            context.lastCid = cid;
            context.lastHValue = hValue;
        }
    }

    private static int processKey(StringBuilder buf, ConversionContext context, int cid, int value, int hValue, int state) {
        switch (state) {
            case FIRST: {
                if (cid == context.lastCid + 1 && value == context.lastValue && hValue == context.lastHValue) {
                    return SERIAL;
                } else {
                    appendEntry(buf, context.lastCid, context.lastValue, context.lastHValue, cid);
                    return FIRST;
                }
            }
            case SERIAL: {
                if (cid != context.lastCid + 1 || value != context.lastValue || hValue != context.lastHValue) {
                    appendEntry(buf, context.lastCid, context.lastValue, context.lastHValue, cid);
                    return FIRST;
                }
                break;
            }
            default:
                break;
        }
        return state;
    }

    private static void appendEntry(StringBuilder buf, int lastCid, int lastValue, int lastHValue, int cid) {
        buf.append(' ').append(lastCid).append(' ').append(-lastValue).append(' ').append(lastHValue / 2).append(' ').append(V1Y).append(' ').append(cid);
    }

    private static void appendFinalEntry(StringBuilder buf, ConversionContext context) {
        buf.append(' ').append(context.lastCid).append(' ').append(-context.lastValue).append(' ').append(context.lastHValue / 2).append(' ').append(V1Y);
    }

    private static class ConversionContext {
        int start;
        int lastCid;
        int lastValue;
        int lastHValue;

        ConversionContext(int start, int lastCid, int lastValue, int lastHValue) {
            this.start = start;
            this.lastCid = lastCid;
            this.lastValue = lastValue;
            this.lastHValue = lastHValue;
        }
    }


    static HashMap<Object, Object> readFontProperties(String name) {
        HashMap<Object, Object> map = new HashMap<>();

        name += ".properties";

        // Use try-with-resources to ensure the InputStream is closed properly
        try (InputStream is = getResourceStream(RESOURCE_PATH + name)) {
            Properties p = new Properties();
            p.load(is);

            IntHashtable metricTable = createMetric(p.getProperty("W"));
            p.remove("W");
            IntHashtable metricTable2 = createMetric(p.getProperty("W2"));
            p.remove("W2");

            for (Enumeration<Object> e = p.keys(); e.hasMoreElements(); ) {
                Object obj = e.nextElement();
                map.put(obj, p.getProperty((String) obj));
            }
            map.put("W", metricTable);
            map.put("W2", metricTable2);
        } catch (IOException e) {
            // Log the exception if necessary, or handle it accordingly
            logger.severe("Error reading font properties");
        }

        return map;
    }


    /**
     * Gets the width of a <CODE>char</CODE> in normalized 1000 units.
     *
     * @param char1 the unicode <CODE>char</CODE> to get the width of
     * @return the width in normalized 1000 units
     */
    @Override
    public int getWidth(int char1) {
        int c = char1;
        if (!cidDirect) {
            c = translationMap[c];
        }
        int v;
        if (vertical) {
            v = vMetrics.get(c);
        } else {
            v = hMetrics.get(c);
        }
        if (v > 0) {
            return v;
        } else {
            return 1000;
        }
    }

    @Override
    public int getWidth(String text) {
        int total = 0;
        for (int k = 0; k < text.length(); ++k) {
            int c = text.charAt(k);
            if (!cidDirect) {
                c = translationMap[c];
            }
            int v;
            if (vertical) {
                v = vMetrics.get(c);
            } else {
                v = hMetrics.get(c);
            }
            if (v > 0) {
                total += v;
            } else {
                total += 1000;
            }
        }
        return total;
    }

    @Override
    int getRawWidth(int c, String name) {
        return 0;
    }

    @Override
    public int getKerning(int char1, int char2) {
        return 0;
    }

    private PdfDictionary getFontDescriptor() {
        PdfDictionary dic = new PdfDictionary(PdfName.FONTDESCRIPTOR);
        dic.put(PdfName.ASCENT, new PdfLiteral((String) fontDesc.get("Ascent")));
        dic.put(PdfName.CAPHEIGHT,
                new PdfLiteral((String) fontDesc.get("CapHeight")));
        dic.put(PdfName.DESCENT,
                new PdfLiteral((String) fontDesc.get("Descent")));
        dic.put(PdfName.FLAGS, new PdfLiteral((String) fontDesc.get("Flags")));
        dic.put(PdfName.FONTBBOX,
                new PdfLiteral((String) fontDesc.get("FontBBox")));
        dic.put(PdfName.FONTNAME, new PdfName(fontName + style));
        dic.put(PdfName.ITALICANGLE,
                new PdfLiteral((String) fontDesc.get("ItalicAngle")));
        dic.put(PdfName.STEMV, new PdfLiteral((String) fontDesc.get("StemV")));
        PdfDictionary pdic = new PdfDictionary();
        pdic.put(PdfName.PANOSE, new PdfString((String) fontDesc.get("Panose"),
                null));
        dic.put(PdfName.STYLE, pdic);
        return dic;
    }

    private PdfDictionary getCIDFont(PdfIndirectReference fontDescriptor,
            IntHashtable cjkTag) {
        PdfDictionary dic = new PdfDictionary(PdfName.FONT);
        dic.put(PdfName.SUBTYPE, PdfName.CIDFONTTYPE0);
        dic.put(PdfName.BASEFONT, new PdfName(fontName + style));
        dic.put(PdfName.FONTDESCRIPTOR, fontDescriptor);
        int[] keys = cjkTag.toOrderedKeys();
        String w = convertToHCIDMetrics(keys, hMetrics);
        if (w != null) {
            dic.put(PdfName.W, new PdfLiteral(w));
        }
        if (vertical) {
            w = convertToVCIDMetrics(keys, vMetrics, hMetrics);
            if (w != null) {
                dic.put(PdfName.W2, new PdfLiteral(w));
            }
        } else {
            dic.put(PdfName.DW, new PdfNumber(1000));
        }
        PdfDictionary cdic = new PdfDictionary();
        cdic.put(PdfName.REGISTRY,
                new PdfString((String) fontDesc.get("Registry"), null));
        cdic.put(PdfName.ORDERING,
                new PdfString((String) fontDesc.get("Ordering"), null));
        cdic.put(PdfName.SUPPLEMENT,
                new PdfLiteral((String) fontDesc.get("Supplement")));
        dic.put(PdfName.CIDSYSTEMINFO, cdic);
        return dic;
    }

    private PdfDictionary getFontBaseType(PdfIndirectReference cidFont) {
        PdfDictionary dic = new PdfDictionary(PdfName.FONT);
        dic.put(PdfName.SUBTYPE, PdfName.TYPE0);
        String name = fontName;
        if (!style.isEmpty()) {
            name += "-" + style.substring(1);
        }
        name += "-" + cMap;
        dic.put(PdfName.BASEFONT, new PdfName(name));
        dic.put(PdfName.ENCODING, new PdfName(cMap));
        dic.put(PdfName.DESCENDANTFONTS, new PdfArray(cidFont));
        return dic;
    }

    @Override
    void writeFont(PdfWriter writer, PdfIndirectReference ref, Object[] params)
            throws DocumentException, IOException {
        IntHashtable cjkTag = (IntHashtable) params[0];
        PdfIndirectReference indFont;
        PdfObject pobj;
        PdfIndirectObject obj;
        pobj = getFontDescriptor();

            obj = writer.addToBody(pobj);
            indFont = obj.getIndirectReference();

        pobj = getCIDFont(indFont, cjkTag);

            obj = writer.addToBody(pobj);
            indFont = obj.getIndirectReference();

        pobj = getFontBaseType(indFont);
        writer.addToBody(pobj, ref);
    }

    /**
     * You can't get the FontStream of a CJK font (CJK fonts are never embedded), so this method always returns null.
     *
     * @return null
     * @since 2.1.3
     */
    @Override
    public PdfStream getFullFontStream() {
        return null;
    }

    private float getDescNumber(String name) {
        return Integer.parseInt((String) fontDesc.get(name));
    }

    private float getBBox(int idx) {
        String s = (String) fontDesc.get("FontBBox");
        StringTokenizer tk = new StringTokenizer(s, " []\r\n\t\f");
        String ret = tk.nextToken();
        for (int k = 0; k < idx; ++k) {
            ret = tk.nextToken();
        }
        return Integer.parseInt(ret);
    }

    /**
     * Gets the font parameter identified by <CODE>key</CODE>. Valid values for
     * <CODE>key</CODE> are <CODE>ASCENT</CODE>, <CODE>CAPHEIGHT</CODE>,
     * <CODE>DESCENT</CODE> and <CODE>ITALICANGLE</CODE>.
     *
     * @param key      the parameter to be extracted
     * @param fontSize the font size in points
     * @return the parameter in points
     */
    @Override
    public float getFontDescriptor(int key, float fontSize) {
        return switch (key) {
            case AWT_ASCENT, ASCENT -> getDescNumber("Ascent") * fontSize / 1000;
            case CAPHEIGHT -> getDescNumber("CapHeight") * fontSize / 1000;
            case AWT_DESCENT, DESCENT -> getDescNumber("Descent") * fontSize / 1000;
            case ITALICANGLE -> getDescNumber("ItalicAngle");
            case BBOXLLX -> fontSize * getBBox(0) / 1000;
            case BBOXLLY -> fontSize * getBBox(1) / 1000;
            case BBOXURX -> fontSize * getBBox(2) / 1000;
            case BBOXURY -> fontSize * getBBox(3) / 1000;
            case AWT_MAXADVANCE -> fontSize * (getBBox(2) - getBBox(0)) / 1000;
            default -> 0;
        };
    }

    @Override
    public String getPostscriptFontName() {
        return fontName;
    }

    /**
     * Sets the font getName that will appear in the pdf font dictionary. Use with care as it can easily make a font
     * unreadable if not embedded.
     *
     * @param name the new font getName
     */
    @Override
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
    @Override
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
     */
    @Override
    public String[][] getAllNameEntries() {
        return new String[][]{{"4", "", "", "", fontName}};
    }

    /**
     * Gets the family getName of the font. If it is a True Type font each array element will have {Platform ID, Platform
     * Encoding ID, Language ID, font getName}. The interpretation of this values can be found in the Open Type
     * specification, chapter 2, in the 'getName' table.<br> For the other fonts the array has a single element with {"",
     * "", "", font getName}.
     *
     * @return the family getName of the font
     */
    @Override
    public String[][] getFamilyFontName() {
        return getFullFontName();
    }

    @Override
    public int getUnicodeEquivalent(int c) {
        if (cidDirect) {
            return translationMap[c];
        }
        return c;
    }

    @Override
    public int getCidCode(int c) {
        if (cidDirect) {
            return c;
        }
        return translationMap[c];
    }

    /**
     * Checks if the font has any kerning pairs.
     *
     * @return always <CODE>false</CODE>
     */
    @Override
    public boolean hasKernPairs() {
        return false;
    }

    /**
     * Checks if a character exists in this font.
     *
     * @param c the character to check
     * @return <CODE>true</CODE> if the character has a glyph,
     * <CODE>false</CODE> otherwise
     */
    @Override
    public boolean charExists(int c) {
        if (c >= translationMap.length) {
            return false;
        }
        return translationMap[c] != 0;
    }

    /**
     * Sets the character advance.
     *
     * @param c       the character
     * @param advance the character advance normalized to 1000 units
     * @return <CODE>true</CODE> if the advance was set, <CODE>false</CODE>
     * otherwise. Will always return <CODE>false</CODE>
     */
    @Override
    public boolean setCharAdvance(int c, int advance) {
        return false;
    }

    @Override
    public boolean setKerning(int char1, int char2, int kern) {
        return false;
    }

    @Override
    public int[] getCharBBox(int c) {
        return new int[0];
    }

    @Override
    protected int[] getRawCharBBox(int c, String name) {
        return new int[0];
    }
}
