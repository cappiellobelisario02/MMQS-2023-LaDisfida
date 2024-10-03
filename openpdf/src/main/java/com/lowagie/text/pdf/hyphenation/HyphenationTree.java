/*
 * Copyright 1999-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id: HyphenationTree.java 3117 2008-01-31 05:53:22Z xlv $ */

package com.lowagie.text.pdf.hyphenation;

import java.io.InputStream;
import java.io.Serial;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This tree structure stores the hyphenation patterns in an efficient way for fast lookup. It provides the
 * method to hyphenate a word.
 *
 * @author <a href="cav@uniscope.co.jp">Carlos Villegas</a>
 */
public class HyphenationTree extends TernaryTree
        implements PatternConsumer {

    @Serial
    private static final long serialVersionUID = -7763254239309429432L;
    private static final Logger logger = Logger.getLogger(HyphenationTree.class.getName());

    /**
     * value space: stores the interletter values
     */
    protected ByteVector vspace;

    /**
     * This map stores hyphenation exceptions
     */
    protected transient Map<String, List<String>> stoplist;

    /**
     * This map stores the character classes
     */
    protected TernaryTree classmap;

    /**
     * Temporary map to store interletter values on pattern loading.
     */
    private transient TernaryTree ivalues;

    public HyphenationTree() {
        stoplist = new HashMap<>(23);    // usually a small table
        classmap = new TernaryTree();
        vspace = new ByteVector();
        vspace.alloc(1);    // this reserves index 0, which we don't use
    }

    /**
     * Packs the values by storing them in 4 bits, two values into a byte Values range is from 0 to 9. We use zero as
     * terminator, so we'll add 1 to the value.
     *
     * @param values a string of digits from '0' to '9' representing the interletter values.
     * @return the index into the vspace array where the packed values are stored.
     */
    protected int packValues(String values) {
        int i;
        int n = values.length();
        int m = (n & 1) == 1 ? (n >> 1) + 2 : (n >> 1) + 1;
        int offset = vspace.alloc(m);
        byte[] va = vspace.getArray();
        for (i = 0; i < n; i++) {
            int j = i >> 1;
            byte v = (byte) ((values.charAt(i) - '0' + 1) & 0x0f);
            if ((i & 1) == 1) {
                va[j + offset] = (byte) (va[j + offset] | v);
            } else {
                va[j + offset] = (byte) (v << 4);    // big endian
            }
        }
        va[m - 1 + offset] = 0;    // terminator
        return offset;
    }

    protected String unpackValues(int k) {
        StringBuilder buf = new StringBuilder();
        byte v = vspace.get(k++);
        while (v != 0) {
            char c = (char) ((v >>> 4) - 1 + '0');
            buf.append(c);
            c = (char) (v & 0x0f);
            if (c == 0) {
                break;
            }
            c = (char) (c - 1 + '0');
            buf.append(c);
            v = vspace.get(k++);
        }
        return buf.toString();
    }

    public void loadSimplePatterns(InputStream stream) {
        SimplePatternParser pp = new SimplePatternParser();
        ivalues = new TernaryTree();

        pp.parse(stream, this);

        // patterns/values should be now in the tree
        // let's optimize a bit
        trimToSize();
        vspace.trimToSize();
        classmap.trimToSize();

        // get rid of the auxiliary map
        ivalues = null;
    }


    public String findPattern(String pat) {
        int k = super.find(pat);
        if (k >= 0) {
            return unpackValues(k);
        }
        return "";
    }

    /**
     * String compare, returns 0 if equal or t is a substring of s
     *
     * @param s  The first String to compare
     * @param si The index to start at on String s
     * @param t  The second String to compare
     * @param ti The index to start at on String t
     * @return 0 if equal or
     */
    protected int hstrcmp(char[] s, int si, char[] t, int ti) {
        for (; s[si] == t[ti]; si++, ti++) {
            if (s[si] == 0) {
                return 0;
            }
        }
        if (t[ti] == 0) {
            return 0;
        }
        return s[si] - t[ti];
    }

    protected byte[] getValues(int k) {
        StringBuilder buf = new StringBuilder();
        byte v = vspace.get(k++);
        while (v != 0) {
            char c = (char) ((v >>> 4) - 1);
            buf.append(c);
            c = (char) (v & 0x0f);
            if (c == 0) {
                break;
            }
            c = (char) (c - 1);
            buf.append(c);
            v = vspace.get(k++);
        }
        byte[] res = new byte[buf.length()];
        for (int i = 0; i < res.length; i++) {
            res[i] = (byte) buf.charAt(i);
        }
        return res;
    }

    /**
     * <p>Search for all possible partial matches of word starting
     * at index an update interletter values. In other words, it does something like:</p>
     * <pre>
     * {@code
     *  for(i=0; i<patterns.length; i++) {
     *      if ( word.substring(index).startsWidth(patterns[i]) ) {
     *          update_interletter_values(patterns[i]);
     *      }
     *  }
     * }</pre>
     * <p>But it is done in an efficient way since the patterns are
     * stored in a ternary tree. In fact, this is the whole purpose of having the tree: doing this search without having
     * to test every single pattern. The number of patterns for languages such as English range from 4000 to 10000.
     * Thus, doing thousands of string comparisons for each word to hyphenate would be really slow without the tree. The
     * tradeoff is memory, but using a ternary tree instead of a trie, almost halves the the memory used by Lout or TeX.
     * It's also faster than using a hash table</p>
     *
     * @param word  null terminated word to match
     * @param index start index from word
     * @param il    interletter values array to update
     */
    protected void searchPatterns(char[] word, int index, byte[] il) {
        char sp = word[index];
        char p = root;
        searchPatternsRecursive(word, index, il, sp, p);
    }

    private void searchPatternsRecursive(char[] word, int index, byte[] il, char sp, char p) {
        while (p > 0 && p < sc.length) {
            if (sc[p] == 0xFFFF) {
                processPattern(word, index, il, p);
                return;
            }

            int d = sp - sc[p];
            if (d == 0) {
                if (sp == 0) {
                    break;
                }
                sp = word[++index];
                p = eq[p];
                searchForPatternEnding(index, il, p);
            } else {
                p = d < 0 ? lo[p] : hi[p];
            }
        }
    }

    private void processPattern(char[] word, int index, byte[] il, char p) {
        if (hstrcmp(word, index, kv.getArray(), lo[p]) == 0) {
            byte[] values = getValues(eq[p]);
            updateValues(il, index, values);
        }
    }

    private void updateValues(byte[] il, int index, byte[] values) {
        int j = index;
        for (byte value : values) {
            if (j < il.length && value > il[j]) {
                il[j] = value;
            }
            j++;
        }
    }

    private void searchForPatternEnding(int index, byte[] il, char p) {
        char q = p;
        boolean shouldBreak = false; // Flag to determine if we should exit the loop

        while (q > 0 && q < sc.length && !shouldBreak) {
            if (sc[q] == 0xFFFF) {
                shouldBreak = true; // Set flag to true instead of breaking
            } else if (sc[q] == 0) {
                processPatternEnding(index, il, q);
                shouldBreak = true; // Set flag to true instead of breaking
            } else {
                q = lo[q];
            }
        }
    }


    private void processPatternEnding(int index, byte[] il, char q) {
        byte[] values = getValues(eq[q]);
        updateValues(il, index, values);
    }


    /**
     * Hyphenate word and return an array of hyphenation points.
     *
     * @param w               char array that contains the word
     * @param offset          Offset to first character in word
     * @param len             Length of word
     * @param remainCharCount Minimum number of characters allowed before the hyphenation point.
     * @param pushCharCount   Minimum number of characters allowed after the hyphenation point.
     * @return a {@link Hyphenation Hyphenation} object representing the hyphenated word or null if word is not
     * hyphenated.
     */
    public Hyphenation hyphenate(char[] w, int offset, int len,
            int remainCharCount, int pushCharCount) {
        char[] normalizedWord = normalizeWord(w, offset, len);
        if (normalizedWord == null || normalizedWord.length < (remainCharCount + pushCharCount)) {
            return null; // Word is too short or could not be normalized
        }

        int[] result = getHyphenationPoints(normalizedWord, remainCharCount, pushCharCount);
        if (result.length > 0) {
            return new Hyphenation(new String(w, offset, len), result);
        } else {
            return null;
        }
    }

    private char[] normalizeWord(char[] w, int offset, int len) {
        char[] word = new char[len + 3];
        int iIgnoreAtBeginning = 0;
        int iLength = len;
        boolean endOfLetters = false;

        for (int i = 1; i <= len; i++) {
            char[] c = { w[offset + i - 1] };
            int nc = classmap.find(c, 0);
            if (nc < 0) {
                if (i == (1 + iIgnoreAtBeginning)) {
                    iIgnoreAtBeginning++;
                } else {
                    endOfLetters = true;
                }
                iLength--;
            } else {
                if (!endOfLetters) {
                    word[i - iIgnoreAtBeginning] = (char) nc;
                } else {
                    return new char[0];
                }
            }
        }
        return Arrays.copyOfRange(word, 0, iLength + 3);
    }

    private int[] getHyphenationPoints(char[] word, int remainCharCount, int pushCharCount) {
        int len = word.length - 3; // Adjusted length considering markers
        int[] result = new int[len + 1];
        int k;

        if (checkStopList(word, remainCharCount, pushCharCount, result)) {
            k = result[0]; // Number of hyphenation points found in stoplist
        } else {
            k = findHyphenationPointsUsingAlgorithm(word, remainCharCount, pushCharCount, result);
        }

        return Arrays.copyOf(result, k);
    }

    private boolean checkStopList(char[] word, int remainCharCount, int pushCharCount, int[] result) {
        String sw = new String(word, 1, word.length - 3);
        if (stoplist.containsKey(sw)) {
            List<String> hw = stoplist.get(sw);
            int j = 0;
            int k = 0;
            for (String s : hw) {
                j += s.length();
                if (j >= remainCharCount && j < (word.length - pushCharCount)) {
                    result[k++] = j;
                }
            }
            return true;
        }
        return false;
    }

    private int findHyphenationPointsUsingAlgorithm(char[] word, int remainCharCount, int pushCharCount, int[] result) {
        word[0] = '.'; // Start marker
        word[word.length - 2] = '.'; // End marker
        word[word.length - 1] = 0; // Null terminator

        byte[] il = new byte[word.length]; // Initialized to zero
        for (int i = 0; i < word.length - 1; i++) {
            searchPatterns(word, i, il);
        }

        int k = 0;
        for (int i = 0; i < word.length - 3; i++) {
            if ((il[i + 1] & 1) == 1 && i >= remainCharCount && i <= (word.length - 3 - pushCharCount)) {
                result[k++] = i;
            }
        }
        return k;
    }

    /**
     * Add a character class to the tree. It is used by {@link SimplePatternParser SimplePatternParser} as callback to
     * add character classes. Character classes define the valid word characters for hyphenation. If a word contains a
     * character not defined in any of the classes, it is not hyphenated. It also defines a way to normalize the
     * characters in order to compare them with the stored patterns. Usually pattern files use only lower case
     * characters, in this case a class for letter 'a', for example, should be defined as "aA", the first character
     * being the normalization char.
     */
    @Override
    public void addClass(String chargroup) {
        if (!chargroup.isEmpty()) {
            char equivChar = chargroup.charAt(0);
            char[] key = new char[2];
            for (int i = 0; i < chargroup.length(); i++) {
                key[0] = chargroup.charAt(i);
                classmap.insert(key, 0, equivChar);
            }
        }
    }

    /**
     * Add an exception to the tree. It is used by {@link SimplePatternParser SimplePatternParser} class as callback to
     * store the hyphenation exceptions.
     *
     * @param word           normalized word
     * @param hyphenatedword a vector of alternating strings and {@link Hyphen hyphen} objects.
     */
    public void addException(String word, ArrayList hyphenatedword) {
        stoplist.put(word, hyphenatedword);
    }

    /**
     * Add a pattern to the tree. Mainly, to be used by {@link SimplePatternParser SimplePatternParser} class as
     * callback to add a pattern to the tree.
     *
     * @param pattern the hyphenation pattern
     * @param ivalue  interletter weight values indicating the desirability and priority of hyphenating at a given point
     *                within the pattern. It should contain only digit characters. (i.e. '0' to '9').
     */
    @Override
    public void addPattern(String pattern, String ivalue) {
        int k = ivalues.find(ivalue);
        if (k <= 0) {
            k = packValues(ivalue);
            ivalues.insert(ivalue, (char) k);
        }
        insert(pattern, (char) k);
    }

    @Override
    public void printStats() {
        String stringToLog = "Value space size = " + vspace.length();
        logger.info(stringToLog);
        super.printStats();
    }
}
