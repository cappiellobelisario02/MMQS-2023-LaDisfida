/*
 * $Id: FontFactoryImp.java 4063 2009-09-13 19:02:46Z psoares33 $
 *
 * Copyright 2002 by Bruno Lowagie.
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

package com.lowagie.text;

import com.lowagie.text.html.Markup;
import com.lowagie.text.pdf.BaseFont;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;


/**
 * If you are using True Type fonts, you can declare the paths of the different ttf- and ttc-files to this class first
 * and then create fonts in your code using one of the getFont method without having to enter a path as parameter.
 *
 * @author Bruno Lowagie
 */

public class FontFactoryImp implements FontProvider {

    private static final String[] TTFamilyOrder = {
            "3", "1", "1033",
            "3", "0", "1033",
            "1", "0", "0",
            "0", "3", "0"
    };

    private static final Logger logger = Logger.getLogger(FontFactoryImp.class.getName());

    /**
     * This is a map of postscriptfontnames of True Type fonts and the path of their ttf- or ttc-file.
     */
    private final Map<String, String> trueTypeFonts = new HashMap<>();
    /**
     * This is a map of fontfamilies.
     */
    private final Map<String, List<String>> fontFamilies = new HashMap<>();

    /**
     * This is a lock for protecting fontFamilies from race condition.
     */
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    /**
     * This is the default encoding to use.
     */
    public static String DEFAULT_ENCODING = BaseFont.WINANSI;

    /**
     * This is the default value of the <VAR>embedded</VAR> variable.
     */
    public static boolean DEFAULT_EMBEDDING = BaseFont.NOT_EMBEDDED;

    /**
     * Creates new FontFactory
     */
    public FontFactoryImp() {
        trueTypeFonts.put(FontFactory.COURIER.toLowerCase(Locale.ROOT), FontFactory.COURIER);
        trueTypeFonts.put(FontFactory.COURIER_BOLD.toLowerCase(Locale.ROOT), FontFactory.COURIER_BOLD);
        trueTypeFonts.put(FontFactory.COURIER_OBLIQUE.toLowerCase(Locale.ROOT), FontFactory.COURIER_OBLIQUE);
        trueTypeFonts.put(FontFactory.COURIER_BOLDOBLIQUE.toLowerCase(Locale.ROOT), FontFactory.COURIER_BOLDOBLIQUE);
        trueTypeFonts.put(FontFactory.HELVETICA.toLowerCase(Locale.ROOT), FontFactory.HELVETICA);
        trueTypeFonts.put(FontFactory.HELVETICA_BOLD.toLowerCase(Locale.ROOT), FontFactory.HELVETICA_BOLD);
        trueTypeFonts.put(FontFactory.HELVETICA_OBLIQUE.toLowerCase(Locale.ROOT), FontFactory.HELVETICA_OBLIQUE);
        trueTypeFonts
                .put(FontFactory.HELVETICA_BOLDOBLIQUE.toLowerCase(Locale.ROOT), FontFactory.HELVETICA_BOLDOBLIQUE);
        trueTypeFonts.put(FontFactory.SYMBOL.toLowerCase(Locale.ROOT), FontFactory.SYMBOL);
        trueTypeFonts.put(FontFactory.TIMES_ROMAN.toLowerCase(Locale.ROOT), FontFactory.TIMES_ROMAN);
        trueTypeFonts.put(FontFactory.TIMES_BOLD.toLowerCase(Locale.ROOT), FontFactory.TIMES_BOLD);
        trueTypeFonts.put(FontFactory.TIMES_ITALIC.toLowerCase(Locale.ROOT), FontFactory.TIMES_ITALIC);
        trueTypeFonts.put(FontFactory.TIMES_BOLDITALIC.toLowerCase(Locale.ROOT), FontFactory.TIMES_BOLDITALIC);
        trueTypeFonts.put(FontFactory.ZAPFDINGBATS.toLowerCase(Locale.ROOT), FontFactory.ZAPFDINGBATS);

        java.util.List<String> tmp = new ArrayList<>();
        tmp.add(FontFactory.COURIER);
        tmp.add(FontFactory.COURIER_BOLD);
        tmp.add(FontFactory.COURIER_OBLIQUE);
        tmp.add(FontFactory.COURIER_BOLDOBLIQUE);
        fontFamilies.put(FontFactory.COURIER.toLowerCase(Locale.ROOT), tmp);
        tmp = new ArrayList<>();
        tmp.add(FontFactory.HELVETICA);
        tmp.add(FontFactory.HELVETICA_BOLD);
        tmp.add(FontFactory.HELVETICA_OBLIQUE);
        tmp.add(FontFactory.HELVETICA_BOLDOBLIQUE);
        fontFamilies.put(FontFactory.HELVETICA.toLowerCase(Locale.ROOT), tmp);
        tmp = new ArrayList<>();
        tmp.add(FontFactory.SYMBOL);
        fontFamilies.put(FontFactory.SYMBOL.toLowerCase(Locale.ROOT), tmp);
        tmp = new ArrayList<>();
        tmp.add(FontFactory.TIMES_ROMAN);
        tmp.add(FontFactory.TIMES_BOLD);
        tmp.add(FontFactory.TIMES_ITALIC);
        tmp.add(FontFactory.TIMES_BOLDITALIC);
        fontFamilies.put(FontFactory.TIMES.toLowerCase(Locale.ROOT), tmp);
        fontFamilies.put(FontFactory.TIMES_ROMAN.toLowerCase(Locale.ROOT), tmp);
        tmp = new ArrayList<>();
        tmp.add(FontFactory.ZAPFDINGBATS);
        fontFamilies.put(FontFactory.ZAPFDINGBATS.toLowerCase(Locale.ROOT), tmp);
    }

    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontName the name of the font
     * @param encoding the encoding of the font
     * @param embedded true if the font is to be embedded in the PDF
     * @param size     the size of this font
     * @param style    the style of this font
     * @param color    the <CODE>Color</CODE> of this font.
     * @return the Font constructed based on the parameters
     */
    public Font getFont(String fontName, String encoding, boolean embedded, float size, int style, Color color) {
        return getFont(fontName, encoding, embedded, size, style, color, true);
    }


    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontname the name of the font
     * @param encoding the encoding of the font
     * @param embedded true if the font is to be embedded in the PDF
     * @param size     the size of this font
     * @param style    the style of this font
     * @param color    the <CODE>Color</CODE> of this font.
     * @param cached   true if the font comes from the cache or is added to the cache if new, false if the font is
     *                 always created new
     * @return the Font constructed based on the parameters
     */
    public Font getFont(String fontname, String encoding, boolean embedded, float size, int style,
            Color color, boolean cached) {
        if (fontname == null) {
            return new Font(Font.UNDEFINED, size, style, color);
        }

        String lowerCaseFontname = fontname.toLowerCase(Locale.ROOT);
        lock.readLock().lock(); // Acquire the read lock
        try {
            List<String> fontFamilie = fontFamilies.get(lowerCaseFontname);
            if (fontFamilie != null) {
                int s = style == Font.UNDEFINED ? Font.NORMAL : style;
                for (String font : fontFamilie) {
                    int fontStyle = Font.getFontStyleFromName(font);
                    if ((s & Font.BOLDITALIC) == fontStyle) {
                        fontname = font;
                        lowerCaseFontname = fontname.toLowerCase(Locale.ROOT);
                        style = s ^ fontStyle; // Remove already present styles
                        break;
                    }
                }
            }
        } finally {
            lock.readLock().unlock(); // Ensure the lock is always released
        }

        BaseFont basefont = null;
        try {
            basefont = createBaseFont(fontname, encoding, embedded, cached, true);
            if (basefont == null) {
                // Check for TrueType font registration
                fontname = trueTypeFonts.get(lowerCaseFontname);
                if (fontname == null) {
                    return new Font(Font.UNDEFINED, size, style, color);
                }
                basefont = BaseFont.createFont(fontname, encoding, embedded, cached, null, null);
            }
        } catch (DocumentException de) {
            throw new ExceptionConverter(de); // Handle document exceptions
        } catch (IOException | NullPointerException ioe) {
            // Handle I/O or null exceptions safely
            return new Font(Font.UNDEFINED, size, style, color);
        }

        return new Font(basefont, size, style, color); // Return the constructed Font
    }

    private BaseFont createBaseFont(String fontname, String encoding, boolean embedded, boolean cached, boolean isType1) throws DocumentException, IOException {
        try {
            return BaseFont.createFont(fontname, encoding, embedded, cached, null, null, isType1);
        } catch (DocumentException de) {
            // Handle or log exception if needed
            return null;
        }
    }

    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param attributes the attributes of a <CODE>Font</CODE> object.
     * @return the Font constructed based on the attributes
     */

    public Font getFont(Properties attributes) {
        String fontname = extractFontName(attributes);
        String encoding = extractEncoding(attributes);
        boolean embedded = extractEmbedded(attributes);
        float size = extractSize(attributes);
        int style = extractStyle(attributes);
        Color color = extractColor(attributes);

        if (fontname == null) {
            return getFont(null, encoding, embedded, size, style, color);
        }
        return getFont(fontname, encoding, embedded, size, style, color);
    }

    private String extractFontName(Properties attributes) {
        String value = attributes.getProperty(Markup.HTML_ATTR_STYLE);
        if (value != null && !value.isEmpty()) {
            Properties styleAttributes = Markup.parseAttributes(value);
            return styleAttributes.getProperty(Markup.CSS_KEY_FONTFAMILY);
        }
        return attributes.getProperty(ElementTags.FONT);
    }

    private String extractEncoding(Properties attributes) {
        String value = attributes.getProperty(ElementTags.ENCODING);
        return value != null ? value : DEFAULT_ENCODING;
    }

    private boolean extractEmbedded(Properties attributes) {
        return "true".equals(attributes.getProperty(ElementTags.EMBEDDED));
    }

    private float extractSize(Properties attributes) {
        String value;
        if ((value = attributes.getProperty(Markup.CSS_KEY_FONTSIZE)) != null || (value = attributes.getProperty(ElementTags.SIZE)) != null) {
            return Markup.parseLength(value);
        }
        return Font.UNDEFINED;
    }

    private int extractStyle(Properties attributes) {
        int style = Font.NORMAL;
        String value;
        if ((value = attributes.getProperty(Markup.CSS_KEY_FONTWEIGHT)) != null) {
            style |= Font.getStyleValue(value);
        }
        if ((value = attributes.getProperty(Markup.CSS_KEY_FONTSTYLE)) != null) {
            style |= Font.getStyleValue(value);
        }
        if ((value = attributes.getProperty(Markup.HTML_ATTR_STYLE)) != null) {
            style |= Font.getStyleValue(value);
        }
        if ((value = attributes.getProperty(ElementTags.STYLE)) != null) {
            style |= Font.getStyleValue(value);
        }
        return style;
    }

    private Color extractColor(Properties attributes) {
        String r = attributes.getProperty(ElementTags.RED);
        String g = attributes.getProperty(ElementTags.GREEN);
        String b = attributes.getProperty(ElementTags.BLUE);
        if (r != null || g != null || b != null) {
            int red = r != null ? Integer.parseInt(r) : 0;
            int green = g != null ? Integer.parseInt(g) : 0;
            int blue = b != null ? Integer.parseInt(b) : 0;
            return new Color(red, green, blue);
        } else {
            String value = attributes.getProperty(ElementTags.COLOR);
            return value != null ? Markup.decodeColor(value) : null;
        }
    }


    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontname the name of the font
     * @param encoding the encoding of the font
     * @param embedded true if the font is to be embedded in the PDF
     * @param size     the size of this font
     * @param style    the style of this font
     * @return the Font constructed based on the parameters
     */

    public Font getFont(String fontname, String encoding, boolean embedded, float size, int style) {
        return getFont(fontname, encoding, embedded, size, style, null);
    }

    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontname the name of the font
     * @param encoding the encoding of the font
     * @param embedded true if the font is to be embedded in the PDF
     * @param size     the size of this font
     * @return the Font constructed based on the parameters
     */

    public Font getFont(String fontname, String encoding, boolean embedded, float size) {
        return getFont(fontname, encoding, embedded, size, Font.UNDEFINED, null);
    }

    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontname the name of the font
     * @param encoding the encoding of the font
     * @param embedded true if the font is to be embedded in the PDF
     * @return the Font constructed based on the parameters
     */

    public Font getFont(String fontname, String encoding, boolean embedded) {
        return getFont(fontname, encoding, embedded, Font.UNDEFINED, Font.UNDEFINED, null);
    }

    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontname the name of the font
     * @param encoding the encoding of the font
     * @param size     the size of this font
     * @param style    the style of this font
     * @param color    the <CODE>Color</CODE> of this font.
     * @return the Font constructed based on the parameters
     */

    public Font getFont(String fontname, String encoding, float size, int style, Color color) {
        return getFont(fontname, encoding, DEFAULT_EMBEDDING, size, style, color);
    }

    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontname the name of the font
     * @param encoding the encoding of the font
     * @param size     the size of this font
     * @param style    the style of this font
     * @return the Font constructed based on the parameters
     */

    public Font getFont(String fontname, String encoding, float size, int style) {
        return getFont(fontname, encoding, DEFAULT_EMBEDDING, size, style, null);
    }

    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontname the name of the font
     * @param encoding the encoding of the font
     * @param size     the size of this font
     * @return the Font constructed based on the parameters
     */

    public Font getFont(String fontname, String encoding, float size) {
        return getFont(fontname, encoding, DEFAULT_EMBEDDING, size, Font.UNDEFINED, null);
    }


    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontname the name of the font
     * @param size     the size of this font
     * @param color    the <CODE>Color</CODE> of this font.
     * @return the Font constructed based on the parameters
     * @since 2.1.0
     */

    public Font getFont(String fontname, float size, Color color) {
        return getFont(fontname, DEFAULT_ENCODING, DEFAULT_EMBEDDING, size, Font.UNDEFINED, color);
    }

    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontname the name of the font
     * @param encoding the encoding of the font
     * @return the Font constructed based on the parameters
     */

    public Font getFont(String fontname, String encoding) {
        return getFont(fontname, encoding, DEFAULT_EMBEDDING, Font.UNDEFINED, Font.UNDEFINED, null);
    }

    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontname the name of the font
     * @param size     the size of this font
     * @param style    the style of this font
     * @param color    the <CODE>Color</CODE> of this font.
     * @return the Font constructed based on the parameters
     */

    public Font getFont(String fontname, float size, int style, Color color) {
        return getFont(fontname, DEFAULT_ENCODING, DEFAULT_EMBEDDING, size, style, color);
    }

    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontname the name of the font
     * @param size     the size of this font
     * @param style    the style of this font
     * @return the Font constructed based on the parameters
     */

    public Font getFont(String fontname, float size, int style) {
        return getFont(fontname, DEFAULT_ENCODING, DEFAULT_EMBEDDING, size, style, null);
    }

    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontname the name of the font
     * @param size     the size of this font
     * @return the Font constructed based on the parameters
     */

    public Font getFont(String fontname, float size) {
        return getFont(fontname, DEFAULT_ENCODING, DEFAULT_EMBEDDING, size, Font.UNDEFINED, null);
    }

    /**
     * Constructs a <CODE>Font</CODE>-object.
     *
     * @param fontname the name of the font
     * @return the Font constructed based on the parameters
     */

    public Font getFont(String fontname) {
        return getFont(fontname, DEFAULT_ENCODING, DEFAULT_EMBEDDING, Font.UNDEFINED, Font.UNDEFINED, null);
    }

    /**
     * Register a font by giving explicitly the font family and name.
     *
     * @param familyName the font family
     * @param fullName   the font name
     * @param path       the font path
     */
    public void registerFamily(String familyName, String fullName, String path) {
        // Put the TrueType font path if it's not null
        if (path != null) {
            trueTypeFonts.put(fullName, path);
        }

        // Acquire write lock
        lock.writeLock().lock();
        try {
            // Get the list of font family names
            List<String> tmp = fontFamilies.get(familyName);

            // If the family doesn't exist, create a new entry
            if (tmp == null) {
                tmp = new ArrayList<>();
                tmp.add(fullName);
                fontFamilies.put(familyName, tmp);
            } else {
                // Insert the fullName into the correct position based on its length
                int fullNameLength = fullName.length();
                boolean inserted = false;
                for (int j = 0; j < tmp.size(); ++j) {
                    if (tmp.get(j).length() >= fullNameLength) {
                        tmp.add(j, fullName);
                        inserted = true;
                        break;
                    }
                }
                // If not inserted, add to the end of the list
                if (!inserted) {
                    tmp.add(fullName);
                }
            }
        } catch (Exception e) {
            // Handle any exceptions if necessary
            // Log or manage the exception accordingly (not shown here)
        } finally {
            // Ensure the lock is always released
            lock.writeLock().unlock();
        }
    }


    /**
     * Register a ttf- or a ttc-file.
     *
     * @param path the path to a ttf- or ttc-file
     */

    public void register(String path) throws Exception {
        register(path, null);
    }

    /**
     * Register a font file and use an alias for the font contained in it.
     *
     * @param path  the path to a font file
     * @param alias the alias you want to use for the font
     */

    public void register(String path, String alias) throws Exception {
        String lowerCasePath = path.toLowerCase();
        String stringToLog;

        if (isTtfOrOtf(lowerCasePath)) {
            registerTrueTypeFont(path, alias);
        } else if (isTtc(lowerCasePath)) {
            registerTrueTypeCollection(path);
        } else if (isAfmOrPfm(lowerCasePath)) {
            registerAFMOrPFMFont(path);
        } else {
            stringToLog = "Unsupported file type: " + path;
            // Handle unsupported file types
            logger.warning(stringToLog);
        }
    }

    private boolean isTtfOrOtf(String path) {
        return path.endsWith(".ttf") || path.endsWith(".otf") || path.indexOf(".ttc,") >= 1;
    }

    private boolean isTtc(String path) {
        return path.endsWith(".ttc");
    }

    private boolean isAfmOrPfm(String path) {
        return path.endsWith(".afm") || path.endsWith(".pfm");
    }

    private void registerTrueTypeFont(String path, String alias) throws IOException, DocumentException {
        Object[] allNames = BaseFont.getAllFontNames(path, BaseFont.WINANSI, null);
        String[][] names = (String[][]) allNames[2]; // full name

        registerFontNames(path, alias, names, allNames);
        registerFamilyName(allNames, path);
    }

    private void registerFontNames(String path, String alias, String[][] names, Object[] allNames) {
        trueTypeFonts.put(((String) allNames[0]).toLowerCase(), path);
        if (alias != null) {
            trueTypeFonts.put(alias.toLowerCase(), path);
        }

        for (String[] name : names) {
            trueTypeFonts.put(name[3].toLowerCase(), path);
        }
    }

    private void registerFamilyName(Object[] allNames, String path) {
        String[][] names = (String[][]) allNames[1]; // family name
        String familyName = findFamilyName(names);

        if (familyName != null) {
            registerFontFamilies(allNames, familyName, path);
        }
    }

    private String findFamilyName(String[][] names) {
        for (int k = 0; k < TTFamilyOrder.length; k += 3) {
            for (String[] name : names) {
                if (name.length == 4 && TTFamilyOrder.length > k + 2
                        && TTFamilyOrder[k].equals(name[0])
                        && TTFamilyOrder[k + 1].equals(name[1])
                        && TTFamilyOrder[k + 2].equals(name[2])) {
                    return name[3].toLowerCase();
                }
            }
        }
        return null;
    }

    private void registerFontFamilies(Object[] allNames, String familyName, String path) {
        String[][] names = (String[][]) allNames[2]; // full name
        String lastName = "";

        for (String[] name : names) {
            for (int k = 0; k < TTFamilyOrder.length; k += 3) {
                if (name.length == 4 && TTFamilyOrder.length > k + 2
                        && TTFamilyOrder[k].equals(name[0])
                        && TTFamilyOrder[k + 1].equals(name[1])
                        && TTFamilyOrder[k + 2].equals(name[2])) {
                    String fullName = name[3];
                    if (!fullName.equals(lastName)) {
                        lastName = fullName;
                        registerFamily(familyName, fullName, path);
                    }
                    break;
                }
            }
        }
    }

    private void registerTrueTypeCollection(String path) throws Exception {
        String[] names = BaseFont.enumerateTTCNames(path);
        for (int i = 0; i < names.length; i++) {
            register(path + "," + i, null);
        }
    }

    private void registerAFMOrPFMFont(String path) throws IOException, DocumentException {
        BaseFont bf = BaseFont.createFont(path, BaseFont.CP1252, false);
        String fullName = bf.getFullFontName()[0][3].toLowerCase();
        String familyName = bf.getFamilyFontName()[0][3].toLowerCase();
        String psName = bf.getPostscriptFontName().toLowerCase();
        registerFamily(familyName, fullName, null);
        trueTypeFonts.put(psName, path);
        trueTypeFonts.put(fullName, path);
    }

    /**
     * Register all the fonts in a directory.
     *
     * @param dir the directory
     * @return the number of fonts registered
     */
    public int registerDirectory(String dir) {
        return registerDirectory(dir, false);
    }

    /**
     * Register all the fonts in a directory and possibly its subdirectories.
     *
     * @param dir                the directory
     * @param scanSubdirectories recursively scan subdirectories if <code>true</code>
     * @return the number of fonts registered
     * @since 2.1.2
     */
    public int registerDirectory(String dir, boolean scanSubdirectories) {
        int count = 0;
        try {
            File directory = new File(dir);
            if (!directory.exists() || !directory.isDirectory()) {
                return 0;
            }
            String[] files = directory.list();
            if (files == null) {
                return 0;
            }
            for (String fileName : files) {
                File file = new File(dir, fileName);
                count += processFile(file, scanSubdirectories);
            }
        } catch (Exception e) {
            // Handle or log exception if needed
        }
        return count;
    }

    private int processFile(File file, boolean scanSubdirectories) {
        int count = 0;
        try {
            if (file.isDirectory() && scanSubdirectories) {
                    count += registerDirectory(file.getAbsolutePath(), true);
            } else {
                String name = file.getPath();
                String suffix = name.length() < 4 ? null : name.substring(name.length() - 4).toLowerCase();
                if (".afm".equals(suffix) || ".pfm".equals(suffix)) {
                    /* Only register Type 1 fonts with matching .pfb files */
                    File pfb = new File(name.substring(0, name.length() - 4) + ".pfb");
                    if (pfb.exists()) {
                        register(name, null);
                        ++count;
                    }
                } else if (".ttf".equals(suffix) || ".otf".equals(suffix)) {
                    register(name, file.getName());
                    ++count;
                } else if (".ttc".equals(suffix)) {
                    register(name, null);
                    ++count;
                }
            }
        } catch (Exception e) {
            // Handle or log exception if needed
        }
        return count;
    }


    /**
     * Register fonts in some probable directories. It usually works in Windows, Linux and Solaris.
     *
     * @return the number of fonts registered
     */
    public int registerDirectories() {
        int count = 0;
        count += registerDirectory("c:/windows/fonts");
        count += registerDirectory("c:/winnt/fonts");
        count += registerDirectory("d:/windows/fonts");
        count += registerDirectory("d:/winnt/fonts");
        count += registerDirectory("/usr/share/X11/fonts", true);
        count += registerDirectory("/usr/X/lib/X11/fonts", true);
        count += registerDirectory("/usr/openwin/lib/X11/fonts", true);
        count += registerDirectory("/usr/share/fonts", true);
        count += registerDirectory("/usr/X11R6/lib/X11/fonts", true);
        count += registerDirectory("/Library/Fonts");
        count += registerDirectory("/System/Library/Fonts");
        return count;
    }

    /**
     * Gets a set of registered fontnames.
     *
     * @return a set of registered fonts
     */

    public Set<String> getRegisteredFonts() {
        return Utilities.getKeySet(trueTypeFonts);
    }

    /**
     * Gets a set of registered fontnames.
     *
     * @return a set of registered font families
     */

    public Set<String> getRegisteredFamilies() {
        return Utilities.getKeySet(fontFamilies);
    }

    /**
     * Checks if a certain font is registered.
     *
     * @param fontName the name of the font that has to be checked.
     * @return true if the font is found
     */
    public boolean isRegistered(String fontName) {
        return trueTypeFonts.containsKey(fontName.toLowerCase());
    }

    /**
     * Get a registered font path.
     *
     * @param fontname the name of the font to get.
     * @return the font path if found or null
     */
    public Object getFontPath(String fontname) {
        return trueTypeFonts.get(fontname.toLowerCase());
    }
}
