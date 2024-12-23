package com.lowagie.text.pdf.hyphenation;

import com.lowagie.text.pdf.BaseFont;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This class is the main entry point to the hyphenation package. You can use only the static methods or create an
 * instance.
 */
public class Hyphenator {

    static Logger logger = Logger.getLogger(com.lowagie.text.pdf.hyphenation.Hyphenator.class.getName());

    private static final String DEFAULT_HYPH_LOCATION = "com/lowagie/text/pdf/hyphenation/hyph/";

    private static Map<String, HyphenationTree> hyphenTrees = new HashMap<>();
    /**
     * Holds value of property hyphenDir.
     */
    private static String hyphenDir = "";
    private HyphenationTree hyphenTree;
    private int remainCharCount;
    private int pushCharCount;

    /**
     * @param lang     The language
     * @param country  the Country
     * @param leftMin  The minimum letters on the left
     * @param rightMin The minimum letters on the right
     */
    public Hyphenator(String lang, String country, int leftMin,
            int rightMin) {
        hyphenTree = getHyphenationTree(lang, country);
        remainCharCount = leftMin;
        pushCharCount = rightMin;
    }

    /**
     * @param lang    The language
     * @param country The country
     * @return the hyphenation tree
     */
    public static HyphenationTree getHyphenationTree(String lang,
            String country) {
        String key = lang;
        // check whether the country code has been used
        if (country != null && !country.equals("none")) {
            key += "_" + country;
        }
        // first try to find it in the cache
        if (hyphenTrees.containsKey(key)) {
            return hyphenTrees.get(key);
        }
        if (hyphenTrees.containsKey(lang)) {
            return hyphenTrees.get(lang);
        }

        HyphenationTree hTree = getResourceHyphenationTree(key);
        if (hTree == null) {
            hTree = getFileHyphenationTree(key);
        }
        // put it into the pattern cache
        if (hTree != null) {
            hyphenTrees.put(key, hTree);
        }
        return hTree;
    }

    /**
     * @param key A String of the key of the hyphenation tree
     * @return a hyphenation tree
     */
    public static HyphenationTree getResourceHyphenationTree(String key) {
        try {
            InputStream stream = BaseFont.getResourceStream(DEFAULT_HYPH_LOCATION + key + ".xml");
            if (stream == null && key.length() > 2) {
                stream = BaseFont.getResourceStream(DEFAULT_HYPH_LOCATION + key.substring(0, 2) + ".xml");
            }
            if (stream == null) {
                return null;
            }
            HyphenationTree hTree = new HyphenationTree();
            hTree.loadSimplePatterns(stream);
            return hTree;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * @param key The language to get the tree from
     * @return a hyphenation tree
     */
    public static HyphenationTree getFileHyphenationTree(String key) {
        try {
            if (hyphenDir == null) {
                return null;
            }
            InputStream stream = getFileInputStream(new File(hyphenDir, key + ".xml"));
            if (stream == null && key.length() > 2) {
                stream = getFileInputStream(new File(hyphenDir, key.substring(0, 2) + ".xml"));
            }
            if (stream == null) {
                return null;
            }
            HyphenationTree hTree = new HyphenationTree();
            hTree.loadSimplePatterns(stream);
            return hTree;
        } catch (IndexOutOfBoundsException e) {
            return null;
        }
    }

    private static InputStream getFileInputStream(File hyphenFile) {
        InputStream stream = null;
        try {
            stream = new FileInputStream(hyphenFile);
        } catch (FileNotFoundException | SecurityException e) {
            logger.warning("Failed to open file: " + hyphenFile.getName());
        }
        return stream;
    }

    /**
     * @param lang     The language
     * @param country  The country
     * @param word     char array containing the word
     * @param leftMin  Minimum number of characters allowed before the hyphenation point
     * @param rightMin Minimum number of characters allowed after the hyphenation point
     * @return a hyphenation object
     */
    public static Hyphenation hyphenate(String lang, String country,
            String word, int leftMin,
            int rightMin) {
        HyphenationTree hTree = getHyphenationTree(lang, country);
        if (hTree == null) {
            return null;
        }
        return hTree.hyphenate(word.toCharArray(), leftMin, rightMin, 0, 0);
    }

    /**
     * @param lang     The language
     * @param country  The country
     * @param word     char array that contains the word to hyphenate
     * @param offset   Offset to the first character in word
     * @param len      The length of the word
     * @param leftMin  Minimum number of characters allowed before the hyphenation point
     * @param rightMin Minimum number of characters allowed after the hyphenation point
     * @return a hyphenation object
     */
    public static Hyphenation hyphenate(String lang, String country,
            char[] word, int offset, int len,
            int leftMin, int rightMin) {
        HyphenationTree hTree = getHyphenationTree(lang, country);
        if (hTree == null) {
            return null;
        }
        return hTree.hyphenate(word, offset, len, leftMin, rightMin);
    }

    /**
     * Getter for property hyphenDir.
     *
     * @return Value of property hyphenDir.
     */
    public static String getHyphenDir() {
        return hyphenDir;
    }

    /**
     * Setter for property hyphenDir.
     *
     * @param hyphendir New value of property hyphenDir.
     */
    public static void setHyphenDir(String hyphendir) {
        hyphenDir = hyphendir;
    }

    /**
     * @param min Minimum number of characters allowed before the hyphenation point
     */
    public void setMinRemainCharCount(int min) {
        remainCharCount = min;
    }

    /**
     * @param min Minimum number of characters allowed after the hyphenation point
     */
    public void setMinPushCharCount(int min) {
        pushCharCount = min;
    }

    /**
     * @param lang    The language
     * @param country The country
     */
    public void setLanguage(String lang, String country) {
        hyphenTree = getHyphenationTree(lang, country);
    }

    /**
     * @param word   Char array that contains the word
     * @param offset Offset to the first character in word
     * @param len    Length of the word
     * @return a hyphenation object
     */
    public Hyphenation hyphenate(char[] word, int offset, int len) {
        if (hyphenTree == null) {
            return null;
        }
        return hyphenTree.hyphenate(word, offset, len, remainCharCount,
                pushCharCount);
    }

    /**
     * @param word The word to hyphenate
     * @return a hyphenation object
     */
    public Hyphenation hyphenate(String word) {
        if (hyphenTree == null) {
            return null;
        }
        return hyphenTree.hyphenate(word.toCharArray(), remainCharCount, pushCharCount, 0, 0);
    }

}
