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

import com.lowagie.text.ExceptionConverter;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.logging.Logger;


/**
 * Default class to map awt fonts to BaseFont.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */

public class DefaultFontMapper implements FontMapper {

    /**
     * Maps aliases to names.
     */
    private HashMap<String, String> aliases = new HashMap<>();
    /**
     * Maps names to BaseFont parameters.
     */
    private HashMap<String, BaseFontParameters> mapper = new HashMap<>();

    private static final Logger logger = Logger.getLogger(DefaultFontMapper.class.getName());

    /**
     * Returns a BaseFont which can be used to represent the given AWT Font
     *
     * @param font the font to be converted
     * @return a BaseFont which has similar properties to the provided Font
     */
    public BaseFont awtToPdf(Font font) {
        try {
            BaseFontParameters p = getBaseFontParameters(font.getFontName());
            if (p != null) {
                return BaseFont.createFont(p.fontName, p.encoding, p.embedded, p.cached, p.ttfAfm, p.pfb);
            }

            String logicalName = font.getName().toLowerCase();
            String fontKey = getFontKey(logicalName, font.isItalic(), font.isBold());

            return BaseFont.createFont(fontKey, BaseFont.CP1252, false);
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    private String getFontKey(String logicalName, boolean isItalic, boolean isBold) {
        if (isMonospacedFont(logicalName)) {
            return getCourierFontKey(isItalic, isBold);
        } else if (isSerifFont(logicalName)) {
            return getTimesFontKey(isItalic, isBold);
        } else {
            return getHelveticaFontKey(isItalic, isBold);
        }
    }

    private boolean isMonospacedFont(String logicalName) {
        return logicalName.equals("dialoginput") || logicalName.contains("mono") || logicalName.startsWith("courier");
    }

    private boolean isSerifFont(String logicalName) {
        return logicalName.equals("serif") || logicalName.equals("timesroman");
    }

    private String getCourierFontKey(boolean isItalic, boolean isBold) {
        if (isItalic) {
            return isBold ? BaseFont.COURIER_BOLDOBLIQUE : BaseFont.COURIER_OBLIQUE;
        } else {
            return isBold ? BaseFont.COURIER_BOLD : BaseFont.COURIER;
        }
    }

    private String getTimesFontKey(boolean isItalic, boolean isBold) {
        if (isItalic) {
            return isBold ? BaseFont.TIMES_BOLDITALIC : BaseFont.TIMES_ITALIC;
        } else {
            return isBold ? BaseFont.TIMES_BOLD : BaseFont.TIMES_ROMAN;
        }
    }

    private String getHelveticaFontKey(boolean isItalic, boolean isBold) {
        if (isItalic) {
            return isBold ? BaseFont.HELVETICA_BOLDOBLIQUE : BaseFont.HELVETICA_OBLIQUE;
        } else {
            return isBold ? BaseFont.HELVETICA_BOLD : BaseFont.HELVETICA;
        }
    }

    /**
     * Returns an AWT Font which can be used to represent the given BaseFont
     *
     * @param font the font to be converted
     * @param size the desired point size of the resulting font
     * @return a Font which has similar properties to the provided BaseFont
     */

    public Font pdfToAwt(BaseFont font, int size) {
        String[][] names = font.getFullFontName();
        if (names.length == 1) {
            return new Font(names[0][3], Font.PLAIN, size);
        }
        String name10 = null;
        String name3x = null;
        for (String[] name : names) {
            if (name[0].equals("1") && name[1].equals("0")) {
                name10 = name[3];
            } else if (name[2].equals("1033")) {
                name3x = name[3];
                break;
            }
        }
        String finalName = name3x;
        if (finalName == null) {
            finalName = name10;
        }
        if (finalName == null) {
            finalName = names[0][3];
        }
        return new Font(finalName, Font.PLAIN, size);
    }

    /**
     * Maps a getName to a BaseFont parameter.
     *
     * @param awtName    the getName
     * @param parameters the BaseFont parameter
     */
    public void putName(String awtName, BaseFontParameters parameters) {
        mapper.put(awtName, parameters);
    }

    /**
     * Maps an alias to a getName.
     *
     * @param alias   the alias
     * @param awtName the getName
     */
    public void putAlias(String alias, String awtName) {
        aliases.put(alias, awtName);
    }

    /**
     * Looks for a BaseFont parameter associated with a getName.
     *
     * @param name the getName
     * @return the BaseFont parameter or <CODE>null</CODE> if not found.
     */
    public BaseFontParameters getBaseFontParameters(String name) {
        String alias = aliases.get(name);
        if (alias == null) {
            return mapper.get(name);
        }
        BaseFontParameters p = mapper.get(alias);
        if (p == null) {
            return mapper.get(name);
        } else {
            return p;
        }
    }

    /**
     * Inserts the names in this map.
     *
     * @param allNames the returned value of calling {@link BaseFont#getAllFontNames(String, String, byte[])}
     * @param path     the full path to the font
     */
    public void insertNames(Object[] allNames, String path) {
        String[][] names = (String[][]) allNames[2];
        String main = null;
        for (String[] name : names) {
            if (name[2].equals("1033")) {
                main = name[3];
                break;
            }
        }
        if (main == null) {
            main = names[0][3];
        }
        BaseFontParameters p = new BaseFontParameters(path);
        mapper.put(main, p);
        for (String[] name : names) {
            aliases.put(name[3], main);
        }
        aliases.put((String) allNames[0], main);
    }

    /**
     * Inserts all the fonts recognized by iText in the
     * <CODE>directory</CODE> into the map. The encoding
     * will be <CODE>BaseFont.CP1252</CODE> but can be changed later.
     *
     * @param dir the directory to scan
     * @return the number of files processed
     */

    public int insertDirectory(String dir) {
        // Normalizza il percorso utilizzando Path
        Path normalizedPath = Paths.get(dir).normalize();
        File file = normalizedPath.toFile();

        if (!file.exists() || !file.isDirectory()) {
            return 0;
        }

        File[] files = file.listFiles();
        if (files == null) {
            return 0;
        }

        int count = 0;
        for (File file1 : files) {
            file = file1;
            String name = file.getPath().toLowerCase();
            try {
                if (name.endsWith(".ttf") || name.endsWith(".otf") || name.endsWith(".afm")) {
                    Object[] allNames = BaseFont.getAllFontNames(file.getPath(), BaseFont.CP1252, null);
                    insertNames(allNames, file.getPath());
                    ++count;
                } else if (name.endsWith(".ttc")) {
                    String[] ttcs = BaseFont.enumerateTTCNames(file.getPath());
                    for (int j = 0; j < ttcs.length; ++j) {
                        String nt = file.getPath() + "," + j;
                        Object[] allNames = BaseFont.getAllFontNames(nt, BaseFont.CP1252, null);
                        insertNames(allNames, nt);
                    }
                    ++count;
                }
            } catch (IOException e) {
                String stringToLog = "Exception raised in DefaultFontMapper in method 'InsertDirectory'";
                logger.severe(stringToLog);
            }
        }
        return count;
    }

    public HashMap<String, BaseFontParameters> getMapper() {
        return mapper;
    }

    public HashMap<String, String> getAliases() {
        return aliases;
    }

    /**
     * A representation of BaseFont parameters.
     */
    public static class BaseFontParameters {

        /**
         * The font getName.
         */
        public String fontName;
        /**
         * The encoding for that font.
         */
        public String encoding;
        /**
         * The embedding for that font.
         */
        public boolean embedded;
        /**
         * Whether the font is cached of not.
         */
        public boolean cached;
        /**
         * The font bytes for ttf and afm.
         */
        public byte[] ttfAfm;
        /**
         * The font bytes for pfb.
         */
        public byte[] pfb;

        /**
         * Constructs default BaseFont parameters.
         *
         * @param fontName the font getName or location
         */
        public BaseFontParameters(String fontName) {
            this.fontName = fontName;
            encoding = BaseFont.CP1252;
            embedded = BaseFont.EMBEDDED;
            cached = BaseFont.CACHED;
        }
    }
}
