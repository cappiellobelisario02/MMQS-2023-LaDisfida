/*
 * Copyright 2004 Paulo Soares
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
 * Contributions by:
 * Lubos Strapko
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text.html.simpleparser;

import static com.lowagie.text.html.Markup.parseLength;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.ElementTags;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.FontProvider;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.HtmlTags;
import com.lowagie.text.html.Markup;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.HyphenationAuto;
import com.lowagie.text.pdf.HyphenationEvent;
import com.lowagie.text.utils.NumberUtilities;
import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * @author psoares
 */
public class FactoryProperties {

    private static final String ALIGN_KEY = "align";
    private static final String LEADING_KEY = "leading";

    public static Map<String, String> followTags = new HashMap<>();

    static {
        followTags.put("i", "i");
        followTags.put("b", "b");
        followTags.put("u", "u");
        followTags.put("sub", "sub");
        followTags.put("sup", "sup");
        followTags.put("em", "i");
        followTags.put("strong", "b");
        followTags.put("s", "s");
        followTags.put("strike", "s");
    }

    /**
     * @since iText 5.0    This used to be a FontFactoryImp
     */
    private FontProvider fontImp = FontFactory.getFontImp();

    private static void setParagraphLeading(Paragraph paragraph, String leading) {
        if (leading == null) {
            paragraph.setLeading(0, 1.5f);
            return;
        }
        try {
            StringTokenizer tokenizer = new StringTokenizer(leading, " ,");
            String v = tokenizer.nextToken();
            float v1 = Float.parseFloat(v);
            if (!tokenizer.hasMoreTokens()) {
                paragraph.setLeading(v1, 0);
                return;
            }
            v = tokenizer.nextToken();
            float v2 = Float.parseFloat(v);
            paragraph.setLeading(v1, v2);
        } catch (Exception e) {
            paragraph.setLeading(0, 1.5f);
        }
    }

    public static void createParagraph(Paragraph paragraph, ChainedProperties props) {
        props.findProperty(ALIGN_KEY)
                .map(String::trim)
                .ifPresent(align -> {
                    if (align.equalsIgnoreCase("center")) {
                        paragraph.setAlignment(Element.ALIGN_CENTER);
                    } else if (align.equalsIgnoreCase("right")) {
                        paragraph.setAlignment(Element.ALIGN_RIGHT);
                    } else if (align.equalsIgnoreCase("justify")) {
                        paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
                    }
                });

        paragraph.setHyphenation(getHyphenation(props));
        setParagraphLeading(paragraph, props.getProperty(LEADING_KEY));

        props.findProperty("before")
                .flatMap(NumberUtilities::parseFloat)
                .ifPresent(paragraph::setSpacingBefore);

        props.findProperty("after")
                .flatMap(NumberUtilities::parseFloat)
                .ifPresent(paragraph::setSpacingAfter);

        props.findProperty("extraparaspace")
                .flatMap(NumberUtilities::parseFloat)
                .ifPresent(paragraph::setExtraParagraphSpace);
    }

    public static Paragraph createParagraph(ChainedProperties props) {
        Paragraph paragraph = new Paragraph();
        createParagraph(paragraph, props);
        return paragraph;
    }

    public static ListItem createListItem(ChainedProperties props) {
        ListItem item = new ListItem();
        createParagraph(item, props);
        return item;
    }

    /**
     * Gets a HyphenationEvent based on the hyphenation entry in ChainedProperties.
     *
     * @param props ChainedProperties
     * @return a HyphenationEvent
     * @since 2.1.2
     */
    public static HyphenationEvent getHyphenation(ChainedProperties props) {
        return getHyphenation(props.getProperty("hyphenation"));
    }

    /**
     * Gets a HyphenationEvent based on the hyphenation entry in a HashMap.
     *
     * @param props a HashMap with properties
     * @return a HyphenationEvent
     * @since 2.1.2
     */
    public static HyphenationEvent getHyphenation(HashMap<String, String> props) {
        return getHyphenation((String) props.get("hyphenation"));
    }

    /**
     * Gets a HyphenationEvent based on a String. For instance "en_UK,3,2" returns new HyphenationAuto("en", "UK", 3,
     * 2);
     *
     * @param s a String, for instance "en_UK,2,2"
     * @return a HyphenationEvent
     * @since 2.1.2
     */
    public static HyphenationEvent getHyphenation(String s) {
        if (s == null || s.length() == 0) {
            return null;
        }
        String lang = s;
        String country = null;
        int leftMin = 2;
        int rightMin = 2;

        int pos = s.indexOf('_');
        if (pos == -1) {
            return new HyphenationAuto(lang, country, leftMin, rightMin);
        }
        lang = s.substring(0, pos);
        country = s.substring(pos + 1);
        pos = country.indexOf(',');
        if (pos == -1) {
            return new HyphenationAuto(lang, country, leftMin, rightMin);
        }
        s = country.substring(pos + 1);
        country = country.substring(0, pos);
        pos = s.indexOf(',');
        if (pos == -1) {
            leftMin = Integer.parseInt(s);
        } else {
            leftMin = Integer.parseInt(s.substring(0, pos));
            rightMin = Integer.parseInt(s.substring(pos + 1));
        }
        return new HyphenationAuto(lang, country, leftMin, rightMin);
    }

    /**
     * @deprecated This method is deprecated since version 1.2 and is planned for removal in version 2.0.
     * @deprecated since 1.2
     * @deprecated forRemoval = true
     */
    @Deprecated(since = "1.2", forRemoval = true)
    public static void insertStyle(Map<String, String> h) {
        String style = h.get("style");
        if (style == null) {
            return;
        }

        Properties prop = Markup.parseAttributes(style);
        for (Object o : prop.keySet()) {
            String key = (String) o;
            String value = prop.getProperty(key);

            switch (key) {
                case Markup.CSS_KEY_FONTFAMILY:
                    h.put("face", value);
                    break;
                case Markup.CSS_KEY_FONTSIZE:
                    h.put("size", parseLength(value) + "pt");
                    break;
                case Markup.CSS_KEY_FONTSTYLE:
                    handleFontStyle(h, value);
                    break;
                case Markup.CSS_KEY_FONTWEIGHT:
                    handleFontWeight(h, value);
                    break;
                case Markup.CSS_KEY_TEXTDECORATION:
                    handleTextDecoration(h, value);
                    break;
                case Markup.CSS_KEY_COLOR:
                    handleColor(h, value);
                    break;
                case Markup.CSS_KEY_LINEHEIGHT:
                    handleLineHeight(h, value);
                    break;
                case Markup.CSS_KEY_TEXTALIGN:
                    h.put(ALIGN_KEY, value.trim().toLowerCase());
                    break;
                default:
                    break;
            }
        }
    }

    private static void handleFontStyle(Map<String, String> h, String value) {
        if (value.trim().toLowerCase().equals("italic") || value.trim().toLowerCase().equals("oblique")) {
            h.put("i", null);
        }
    }

    private static void handleFontWeight(Map<String, String> h, String value) {
        String ss = value.trim().toLowerCase();
        if (ss.equals("bold") || ss.equals("700") || ss.equals("800") || ss.equals("900")) {
            h.put("b", null);
        }
    }

    private static void handleTextDecoration(Map<String, String> h, String value) {
        if (value.trim().toLowerCase().equals(Markup.CSS_VALUE_UNDERLINE)) {
            h.put("u", null);
        }
    }

    private static void handleColor(Map<String, String> h, String value) {
        Color c = Markup.decodeColor(value);
        if (c != null) {
            int hh = c.getRGB();
            String hs = Integer.toHexString(hh);
            hs = "000000" + hs;
            hs = "#" + hs.substring(hs.length() - 6);
            h.put("color", hs);
        }
    }

    private static void handleLineHeight(Map<String, String> h, String value) {
        String ss = value.trim();
        float v = parseLength(value);
        if (ss.endsWith("%")) {
            h.put(LEADING_KEY, "0," + (v / 100));
        } else if ("normal".equalsIgnoreCase(ss)) {
            h.put(LEADING_KEY, "0,1.5");
        } else {
            h.put(LEADING_KEY, v + ",0");
        }
    }

    /**
     * New method contributed by Lubos Strapko
     *
     * @param h      a Map
     * @param cprops the ChainedProperties
     * @since 2.1.3
     */
    public static void insertStyle(Map<String, String> h, ChainedProperties cprops) {
        String style = h.get("style");
        if (style == null) {
            return;
        }

        Properties prop = Markup.parseAttributes(style);
        for (Object o : prop.keySet()) {
            String key = (String) o;
            String value = prop.getProperty(key);

            switch (key) {
                case Markup.CSS_KEY_FONTFAMILY:
                    h.put(ElementTags.FACE, value);
                    break;
                case Markup.CSS_KEY_FONTSIZE:
                    h.put(ElementTags.SIZE, formatFontSize(value, cprops));
                    break;
                case Markup.CSS_KEY_FONTSTYLE:
                    handleFontStyle(h, value);
                    break;
                case Markup.CSS_KEY_FONTWEIGHT:
                    handleFontWeight(h, value);
                    break;
                case Markup.CSS_KEY_TEXTDECORATION:
                    handleTextDecoration(h, value);
                    break;
                case Markup.CSS_KEY_BGCOLOR:
                case Markup.CSS_KEY_BG:
                    handleBackgroundColor(h, value);
                    break;
                case Markup.CSS_KEY_COLOR:
                    h.put(Markup.CSS_KEY_COLOR, value);
                    break;
                case Markup.CSS_KEY_LINEHEIGHT:
                    handleLineHeight(h, value);
                    break;
                case Markup.CSS_KEY_TEXTALIGN:
                    h.put(ALIGN_KEY, value.trim().toLowerCase());
                    break;
                case Markup.CSS_KEY_PADDINGLEFT:
                    h.put("indent", Float.toString(parseLength(value)));
                    break;
                default:
                    break;
            }
        }
    }

    private static String formatFontSize(String value, ChainedProperties cprops) {
        float actualFontSize = parseLength(cprops.getProperty(ElementTags.SIZE), Markup.DEFAULT_FONT_SIZE);
        if (actualFontSize <= 0f) {
            actualFontSize = Markup.DEFAULT_FONT_SIZE;
        }
        return parseLength(value, actualFontSize) + "pt";
    }

    private static void handleBackgroundColor(Map<String, String> h, String value) {
        Color c = Markup.decodeColor(value);
        if (c != null) {
            int hh = c.getRGB();
            String hs = Integer.toHexString(hh);
            hs = "000000" + hs;
            hs = "#" + hs.substring(hs.length() - 6);
            h.put(Markup.CSS_KEY_BGCOLOR, hs);
        }
    }

    public Chunk createChunk(String text, ChainedProperties props) {
        Font font = getFont(props);
        float size = font.getSize();
        size /= 2;
        Chunk chunk = new Chunk(text, font);
        if (props.hasProperty("sub")) {
            chunk.setTextRise(-size);
        } else if (props.hasProperty("sup")) {
            chunk.setTextRise(size);
        }
        Color bgColor = Markup.decodeColor(props.getProperty(Markup.CSS_KEY_BGCOLOR));
        if (bgColor != null) {
            chunk.setBackground(bgColor);
        }
        chunk.setHyphenation(getHyphenation(props));
        return chunk;
    }

    public Font getFont(ChainedProperties props) {
        String face = extractFace(props.getProperty(ElementTags.FACE));
        int style = extractStyle(props);
        float size = extractSize(props);
        Color color = extractColor(props);
        String encoding = props.getOrDefault("encoding", BaseFont.WINANSI);

        return fontImp.getFont(face, encoding, true, size, style, color);
    }

    private String extractFace(String face) {
        if (face == null) {
            return null;
        }

        StringTokenizer tokenizer = new StringTokenizer(face, ",");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            token = removeQuotes(token);
            if (fontImp.isRegistered(token)) {
                return token;
            }
        }
        return null;
    }

    private String removeQuotes(String face) {
        return face.startsWith("\"") ? face.substring(1) : face;
    }

    private int extractStyle(ChainedProperties props) {
        int style = 0;
        if (props.hasProperty(HtmlTags.I)) {
            style |= Font.ITALIC;
        }
        if (props.hasProperty(HtmlTags.B)) {
            style |= Font.BOLD;
        }
        if (props.hasProperty(HtmlTags.U)) {
            style |= Font.UNDERLINE;
        }
        if (props.hasProperty(HtmlTags.S)) {
            style |= Font.STRIKETHRU;
        }
        return style;
    }

    private float extractSize(ChainedProperties props) {
        return props.findProperty(ElementTags.SIZE)
                .flatMap(NumberUtilities::parseFloat)
                .orElse(12f);
    }

    private Color extractColor(ChainedProperties props) {
        return Markup.decodeColor(props.getProperty(Markup.CSS_KEY_COLOR));
    }

    public FontProvider getFontImp() {
        return fontImp;
    }

    public void setFontImp(FontProvider fontImp) {
        this.fontImp = fontImp;
    }
}
