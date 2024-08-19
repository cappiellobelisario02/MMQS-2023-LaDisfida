/*
 * $Id: CFFFontSubset.java 3573 2008-07-21 15:08:04Z blowagie $
 *
 * Copyright 2004 Oren Manor and Ygal Blum
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
 * the Initial Developer are Copyright (C) 1999-2005 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2005 by Paulo Soares. All Rights Reserved.
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

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * This Class subsets a CFF Type Font. The subset is preformed for CID fonts and NON CID fonts. The Charstring is
 * subsetted for both types. For CID fonts only the FDArray which are used are embedded. The Lsubroutines of the
 * FDArrays used are subsetted as well. The Subroutine subset supports both Type1 and Type2 formatting although only
 * tested on Type2 Format. For Non CID the Lsubroutines are subsetted. On both types the Gsubroutines is subsetted. A
 * font which was not of CID type is transformed into CID as a part of the subset process. The CID synthetic creation
 * was written by Sivan Toledo (sivan@math.tau.ac.il)
 *
 * @author Oren Manor (manorore@post.tau.ac.il) and Ygal Blum (blumygal@post.tau.ac.il)
 */
public class CFFFontSubset extends CFFFont {

    /**
     * The Strings in this array represent Type1/Type2 operator names
     */
    static final String[] subrsFunctions = {
            "RESERVED_0", "hstem", "RESERVED_2", "vstem", "vmoveto", "rlineto", "hlineto", "vlineto",
            "rrcurveto", "RESERVED_9", "callsubr", "return", "escape", "RESERVED_13",
            "endchar", "RESERVED_15", "RESERVED_16", "RESERVED_17", "hstemhm", "hintmask",
            "cntrmask", "rmoveto", "hmoveto", "vstemhm", "rcurveline", "rlinecurve", "vvcurveto",
            "hhcurveto", "shortint", "callgsubr", "vhcurveto", "hvcurveto"
    };
    /**
     * The Strings in this array represent Type1/Type2 escape operator names
     */
    static final String[] subrsEscapeFuncs = {
            "RESERVED_0", "RESERVED_1", "RESERVED_2", "and", "or", "not", "RESERVED_6",
            "RESERVED_7", "RESERVED_8", "abs", "add", "sub", "div", "RESERVED_13", "neg",
            "eq", "RESERVED_16", "RESERVED_17", "drop", "RESERVED_19", "put", "get", "ifelse",
            "random", "mul", "RESERVED_25", "sqrt", "dup", "exch", "index", "roll", "RESERVED_31",
            "RESERVED_32", "RESERVED_33", "hflex", "flex", "hflex1", "flex1", "RESERVED_REST"
    };

    /**
     * Operator codes for unused  CharStrings and unused local and global Subrs
     */
    static final byte ENDCHAR_OP = 14;
    static final byte RETURN_OP = 11;

    /**
     * A HashMap containing the glyphs used in the text after being converted to glyph number by the CMap
     */
    HashMap<Integer, int[]> glyphsUsed;
    /**
     * The glyphsUsed keys as an ArrayList
     */
    ArrayList<Integer> glyphsInList;
    /**
     * A HashMap for keeping the FDArrays being used by the font
     */
    HashMap<Integer, Object> fdArrayUsed = new HashMap<>();
    /**
     * A HashMaps array for keeping the subroutines used in each FontDict
     */
    HashMap<Integer, int[]>[] hSubrsUsed;
    /**
     * The SubroutinesUsed HashMaps as ArrayLists
     */
    ArrayList<Integer>[] lSubrsUsed;
    /**
     * A HashMap for keeping the Global subroutines used in the font
     */
    HashMap<Integer, int[]> hGSubrsUsed = new HashMap<>();
    /**
     * The Global SubroutinesUsed HashMaps as ArrayLists
     */
    ArrayList<Integer> lGSubrsUsed = new ArrayList<>();
    /**
     * A HashMap for keeping the subroutines used in a non-cid font
     */
    HashMap<Integer, int[]> hSubrsUsedNonCID = new HashMap<>();
    /**
     * The SubroutinesUsed HashMap as ArrayList
     */
    ArrayList<Integer> lSubrsUsedNonCID = new ArrayList<>();
    /**
     * An array of the new Indexes for the local Subr. One index for each FontDict
     */
    byte[][] newLSubrsIndex;
    /**
     * The new subroutines index for a non-cid font
     */
    byte[] newSubrsIndexNonCID;
    /**
     * The new global subroutines index of the font
     */
    byte[] newGSubrsIndex;
    /**
     * The new CharString of the font
     */
    byte[] newCharStringsIndex;

    /**
     * The bias for the global subroutines
     */
    int gbias = 0;

    /**
     * The linked list for generating the new font stream
     */
    LinkedList<Item> outputList;

    /**
     * Number of arguments to the stem operators in a subroutine calculated recursively
     */
    int numOfHints = 0;


    /**
     * C'tor for CFFFontSubset
     *
     * @param rf         - The font file
     * @param glyphsUsed - a HashMap that contains the glyph used in the subset
     */
    public CFFFontSubset(RandomAccessFileOrArray rf, HashMap<Integer, int[]> glyphsUsed) {
        // Use CFFFont c'tor in order to parse the font file.
        super(rf);
        this.glyphsUsed = glyphsUsed;
        //Put the glyphs into a list
        glyphsInList = new ArrayList<>(glyphsUsed.keySet());

        for (int i = 0; i < fonts.length; ++i) {
            // Read the number of glyphs in the font
            seek(fonts[i].charstringsOffset);
            fonts[i].nglyphs = getCard16();

            // Jump to the count field of the String Index
            seek(stringIndexOffset);
            fonts[i].nstrings = getCard16() + standardStrings.length;

            // For each font save the offset array of the charstring
            fonts[i].charstringsOffsets = getIndex(fonts[i].charstringsOffset);

            // Process the FDSelect if exist
            if (fonts[i].fdselectOffset >= 0) {
                // Process the FDSelect
                readFDSelect(i);
                // Build the fdArrayUsed hashmap
                buildFDArrayUsed(i);
            }
            if (fonts[i].isCID) {
                // Build the FD Array used Hash Map
                readFDArray(i);
            }
            // compute the charset length
            fonts[i].CharsetLength = countCharset(fonts[i].charsetOffset, fonts[i].nglyphs);
        }
    }

    /**
     * Calculates the length of the charset according to its format
     *
     * @param offset      The Charset Offset
     * @param numofGlyphs Number of glyphs in the font
     * @return the length of the Charset
     */
    int countCharset(int offset, int numofGlyphs) {
        int format;
        int length = 0;
        seek(offset);
        // Read the format
        format = getCard8();
        // Calc according to format
        switch (format) {
            case 0:
                length = 1 + 2 * numofGlyphs;
                break;
            case 1:
                length = 1 + 3 * countRange(numofGlyphs, 1);
                break;
            case 2:
                length = 1 + 4 * countRange(numofGlyphs, 2);
                break;
            default:
                break;
        }
        return length;
    }

    /**
     * Function calculates the number of ranges in the Charset
     *
     * @param numofGlyphs The number of glyphs in the font
     * @param type        The format of the Charset
     * @return The number of ranges in the Charset data structure
     */
    int countRange(int numofGlyphs, int type) {
        int num = 0;
        char sid;
        int i = 1, nLeft;
        while (i < numofGlyphs) {
            num++;
            sid = getCard16();
            if (type == 1) {
                nLeft = getCard8();
            } else {
                nLeft = getCard16();
            }
            i += nLeft + 1;
        }
        return num;
    }


    /**
     * Read the FDSelect of the font and compute the array and its length
     *
     * @param font The index of the font being processed
     */
    protected void readFDSelect(int font) {
        // Restore the number of glyphs
        int numOfGlyphs = fonts[font].nglyphs;
        int[] fdSelect = new int[numOfGlyphs];
        // Go to the beginning of the FDSelect
        seek(fonts[font].fdselectOffset);
        // Read the FDSelect's format
        fonts[font].FDSelectFormat = getCard8();

        switch (fonts[font].FDSelectFormat) {
            // Format==0 means each glyph has an entry that indicated
            // its FD.
            case 0:
                for (int i = 0; i < numOfGlyphs; i++) {
                    fdSelect[i] = getCard8();
                }
                // The FDSelect's Length is one for each glyph + the format
                // for later use
                fonts[font].FDSelectLength = fonts[font].nglyphs + 1;
                break;
            case 3:
                // Format==3 means the ranges version
                // The number of ranges
                int nRanges = getCard16();
                int l = 0;
                // Read the first in the first range
                int first = getCard16();
                for (int i = 0; i < nRanges; i++) {
                    // Read the FD index
                    int fd = getCard8();
                    // Read the first of the next range
                    int last = getCard16();
                    // Calc the steps and write to the array
                    int steps = last - first;
                    for (int k = 0; k < steps; k++) {
                        fdSelect[l] = fd;
                        l++;
                    }
                    // The last from this iteration is the first of the next
                    first = last;
                }
                // Store the length for later use
                fonts[font].FDSelectLength = 1 + 2 + nRanges * 3 + 2;
                break;
            default:
                break;
        }
        // Save the FDSelect of the font
        fonts[font].FDSelect = fdSelect;
    }

    /**
     * Function reads the FDSelect and builds the fdArrayUsed HashMap According to the glyphs used
     *
     * @param font the Number of font being processed
     */
    protected void buildFDArrayUsed(int font) {
        int[] fdSelect = fonts[font].FDSelect;
        // For each glyph used
        for (Object o : glyphsInList) {
            // Pop the glyphs index
            int glyph = (Integer) o;
            // Pop the glyph's FD
            int fd = fdSelect[glyph];
            // Put the FD index into the fdArrayUsed HashMap
            fdArrayUsed.put(fd, null);
        }
    }

    /**
     * Read the FDArray count, offsize and Offset array
     *
     * @param font font offset
     */
    protected void readFDArray(int font) {
        seek(fonts[font].fdarrayOffset);
        fonts[font].FDArrayCount = getCard16();
        fonts[font].FDArrayOffsize = getCard8();
        // Since we will change values inside the FDArray objects
        // We increase its offsize to prevent errors
        if (fonts[font].FDArrayOffsize < 4) {
            fonts[font].FDArrayOffsize++;
        }
        fonts[font].FDArrayOffsets = getIndex(fonts[font].fdarrayOffset);
    }


    /**
     * The process function extracts one font out of the CFF file and returns a subset version of the original.
     *
     * @param fontName - The name of the font to be taken out of the CFF
     * @return The new font stream
     * @throws IOException on error
     */
    public byte[] process(String fontName) throws IOException {
        try {
            // Verify that the file is open
            buf.reOpen();
            // Find the Font that we will be dealing with
            int j;
            for (j = 0; j < fonts.length; j++) {
                if (fontName.equals(fonts[j].name)) {
                    break;
                }
            }
            if (j == fonts.length) {
                return new int[0];
            }

            // Calc the bias for the global subrs
            if (gsubrIndexOffset >= 0) {
                gbias = calcBias(gsubrIndexOffset, j);
            }

            // Prepare the new CharStrings Index
            buildNewCharString(j);
            // Prepare the new Global and Local Subrs Indices
            buildNewLGSubrs(j);
            // Build the new file
            byte[] ret = buildNewFile(j);
            return ret;
        } finally {
            try {
                buf.close();
            } catch (Exception e) {
                // empty on purpose
            }
        }
    }

    /**
     * Function calcs bias according to the CharString type and the count of the subrs
     *
     * @param offset The offset to the relevant subrs index
     * @param font   the font
     * @return The calculated Bias
     */
    protected int calcBias(int offset, int font) {
        seek(offset);
        int nSubrs = getCard16();
        // If type==1 -> bias=0
        if (fonts[font].CharstringType == 1) {
            return 0;
        } else if (nSubrs < 1240) {
            // else calc according to the count
            return 107;
        } else if (nSubrs < 33900) {
            return 1131;
        } else {
            return 32768;
        }
    }

    /**
     * Function uses buildNewIndex to create the new index of the subset charstrings
     *
     * @param fontIndex the font
     * @throws IOException on error
     */
    protected void buildNewCharString(int fontIndex) throws IOException {
        newCharStringsIndex = buildNewIndex(fonts[fontIndex].charstringsOffsets, glyphsUsed, ENDCHAR_OP);
    }

    /**
     * Function builds the new local and global subsrs indices. IF CID then All of the FD Array lsubrs will be
     * subsetted.
     *
     * @param font the font
     * @throws IOException on error
     */
    @SuppressWarnings("unchecked")
    protected void buildNewLGSubrs(int font) throws IOException {
        // If the font is CID then the lsubrs are divided into FontDicts.
        // for each FD array the lsubrs will be subsetted.
        if (fonts[font].isCID) {
            // Init the hashmap-array and the arraylist-array to hold the subrs used
            // in each private dict.
            HashMap<Integer, int[]> mapClazz = new HashMap<>();
            hSubrsUsed = (HashMap<Integer, int[]>[]) Array.newInstance(mapClazz.getClass(),
                    fonts[font].fdprivateOffsets.length);
            ArrayList<Integer> listClass = new ArrayList<>();
            lSubrsUsed = (ArrayList<Integer>[]) Array.newInstance(listClass.getClass(),
                    fonts[font].fdprivateOffsets.length);
            // A [][] which will store the byte array for each new FD Array lsubs index
            newLSubrsIndex = new byte[fonts[font].fdprivateOffsets.length][];
            // An array to hold the offset for each Lsubr index
            fonts[font].PrivateSubrsOffset = new int[fonts[font].fdprivateOffsets.length];
            // A [][] which will store the offset array for each lsubr index
            fonts[font].PrivateSubrsOffsetsArray = new int[fonts[font].fdprivateOffsets.length][];

            // Put the fdArrayUsed into a list
            List<Integer> fdInList = new ArrayList<>(fdArrayUsed.keySet());
            // For each FD array which is used subset the lsubr
            for (int FD : fdInList) {
                // The FDArray index, Hash Map, Array List to work on
                hSubrsUsed[FD] = new HashMap<>();
                lSubrsUsed[FD] = new ArrayList<>();
                //Reads the private dicts looking for the subr operator and
                // store both the offset for the index and its offset array
                buildFDSubrsOffsets(font, FD);
                // Verify that FDPrivate has a LSubrs index
                if (fonts[font].PrivateSubrsOffset[FD] >= 0) {
                    //Scans the Charstring data storing the used Local and Global subroutines
                    // by the glyphs. Scans the Subrs recursively.
                    buildSubrUsed(font, FD, fonts[font].PrivateSubrsOffset[FD],
                            fonts[font].PrivateSubrsOffsetsArray[FD], hSubrsUsed[FD], lSubrsUsed[FD]);
                    // Builds the New Local Subrs index
                    newLSubrsIndex[FD] = buildNewIndex(fonts[font].PrivateSubrsOffsetsArray[FD], hSubrsUsed[FD],
                            RETURN_OP);
                }
            }
        } else if (fonts[font].privateSubrs >= 0) {
            // If the font is not CID && the Private Subr exists then subset:
            
            fonts[font].SubrsOffsets = getIndex(fonts[font].privateSubrs);
            //Scans the Charstring data storing the used Local and Global subroutines
            // by the glyphs. Scans the Subrs recursively.
            buildSubrUsed(font, -1, fonts[font].privateSubrs, fonts[font].SubrsOffsets, hSubrsUsedNonCID,
                    lSubrsUsedNonCID);
        }
        // For all fonts subset the Global Subroutines
        // Scan the Global Subr Hashmap recursively on the Gsubrs
        buildGSubrsUsed(font);
        if (fonts[font].privateSubrs >= 0) {
            // Builds the New Local Subrs index
            newSubrsIndexNonCID = buildNewIndex(fonts[font].SubrsOffsets, hSubrsUsedNonCID, RETURN_OP);
        }
        //Builds the New Global Subrs index
        newGSubrsIndex = buildNewIndex(gsubrOffsets, hGSubrsUsed, RETURN_OP);
    }

    /**
     * The function finds for the FD array processed the local subr offset and its offset array.
     *
     * @param font the font
     * @param fd   The FDARRAY processed
     */
    protected void buildFDSubrsOffsets(int font, int fd) {
        // Initiate to -1 to indicate lsubr operator present
        fonts[font].PrivateSubrsOffset[fd] = -1;
        // Goto beginning of objects
        seek(fonts[font].fdprivateOffsets[fd]);
        // While in the same object:
        while (getPosition() < fonts[font].fdprivateOffsets[fd] + fonts[font].fdprivateLengths[fd]) {
            getDictItem();
            // If the dictItem is the "Subrs" then find and store offset,
            if (Objects.equals(key, "Subrs")) {
                fonts[font].PrivateSubrsOffset[fd] = (Integer) args[0] + fonts[font].fdprivateOffsets[fd];
            }
        }
        //Read the lsubr index if the lsubr was found
        if (fonts[font].PrivateSubrsOffset[fd] >= 0) {
            fonts[font].PrivateSubrsOffsetsArray[fd] = getIndex(fonts[font].PrivateSubrsOffset[fd]);
        }
    }

    /**
     * Function uses readASubr on the glyph used to build the LSubr and Gsubr HashMap. The HashMap (of the lsubr only)
     * is then scanned recursively for Lsubr and Gsubrs calls.
     *
     * @param font         the font
     * @param fd           FD array processed. 0 indicates function was called by non CID font
     * @param subrOffset   the offset to the subr index to calc the bias
     * @param subrsOffsets the offset array of the subr index
     * @param hSubr        HashMap of the subrs used
     * @param lSubr        ArrayList of the subrs used
     */
    protected void buildSubrUsed(int font, int fd, int subrOffset, int[] subrsOffsets, Map<Integer, int[]> hSubr,
            List<Integer> lSubr) {

        // Calc the Bias for the subr index
        int lBias = calcBias(subrOffset, font);

        // For each glyph used find its GID, start & end pos
        for (Object o : glyphsInList) {
            int glyph = (Integer) o;
            int start = fonts[font].charstringsOffsets[glyph];
            int end = fonts[font].charstringsOffsets[glyph + 1];

            // IF CID:
            if (fd >= 0) {
                emptyStack();
                numOfHints = 0;
                // Using FDSELECT find the FD Array the glyph belongs to.
                int glyphFD = fonts[font].FDSelect[glyph];
                // If the Glyph is part of the FD being processed
                if (glyphFD == fd) {
                    // Find the Subrs called by the glyph and insert to hash:
                    readASubr(start, end, gbias, lBias, hSubr, lSubr, subrsOffsets);
                }
            } else {
                // If the font is not CID
                //Find the Subrs called by the glyph and insert to hash:
                readASubr(start, end, gbias, lBias, hSubr, lSubr, subrsOffsets);
            }
        }
        // For all Lsubrs used, check recursively for Lsubr & Gsubr used
        for (int i = 0; i < lSubr.size(); i++) {
            // Pop the subr value from the hash
            int subr = lSubr.get(i);
            // Ensure the Lsubr call is valid
            if (subr < subrsOffsets.length - 1 && subr >= 0) {
                // Read and process the subr
                int start = subrsOffsets[subr];
                int end = subrsOffsets[subr + 1];
                readASubr(start, end, gbias, lBias, hSubr, lSubr, subrsOffsets);
            }
        }
    }

    /**
     * Function scans the Glsubr used ArrayList to find recursive calls to Gsubrs and adds to Hashmap and ArrayList
     *
     * @param font the font
     */
    protected void buildGSubrsUsed(int font) {
        int lBias = 0;
        int sizeOfNonCIDSubrsUsed = 0;
        if (hasPrivateSubrs(font)) {
            lBias = calculateLBias(font);
            sizeOfNonCIDSubrsUsed = lSubrsUsedNonCID.size();
        }

        for (int Subr : lGSubrsUsed) {
            if (isValidGlobalSubr(Subr)) {
                int start = gsubrOffsets[Subr];
                int end = gsubrOffsets[Subr + 1];

                if (fonts[font].isCID) {
                    processCIDSubr(start, end, gbias, lGSubrsUsed);
                } else {
                    processNonCIDSubr(start, end, gbias, lBias, lSubrsUsedNonCID, fonts[font].SubrsOffsets);
                }
            }
        }
    }

    private boolean hasPrivateSubrs(int font) {
        return fonts[font].privateSubrs >= 0;
    }

    private int calculateLBias(int font) {
        return calcBias(fonts[font].privateSubrs, font);
    }

    private boolean isValidGlobalSubr(int subr) {
        return subr < gsubrOffsets.length - 1 && subr >= 0;
    }

    private void processCIDSubr(int start, int end, int gBias,
            List<Integer> lGSubrsUsed) {
        readASubr(start, end, gBias, 0, hGSubrsUsed, lGSubrsUsed, null);
    }

    private void processNonCIDSubr(int start, int end, int gBias, int lBias, List<Integer> lSubrsUsedNonCID,
            int[] subrsOffsets) {
        readASubr(start, end, gBias, lBias, hSubrsUsedNonCID, lSubrsUsedNonCID, subrsOffsets);

        int initialSize = lSubrsUsedNonCID.size();
        for (int j = initialSize; j < lSubrsUsedNonCID.size(); j++) {
            int lSubr = lSubrsUsedNonCID.get(j);
            if (isValidLocalSubr(lSubr, subrsOffsets)) {
                int lStart = subrsOffsets[lSubr];
                int lEnd = subrsOffsets[lSubr + 1];
                readASubr(lStart, lEnd, gbias, lBias, hSubrsUsedNonCID, lSubrsUsedNonCID, subrsOffsets);
            }
        }
    }

    private boolean isValidLocalSubr(int lSubr, int[] subrsOffsets) {
        return lSubr < subrsOffsets.length - 1 && lSubr >= 0;
    }

    /**
     * The function reads a subrs (glyph info) between begin and end. Adds calls to a Lsubr to the hSubr and lSubrs.
     * Adds calls to a Gsubr to the hGSubr and lGSubrs.
     *
     * @param begin         the start point of the subr
     * @param end           the end point of the subr
     * @param gBias         the bias of the Global Subrs
     * @param lBias         the bias of the Local Subrs
     * @param hSubr         the HashMap for the lSubrs
     * @param lSubr         the ArrayList for the lSubrs
     * @param lSubrsOffsets The Offsets array of the subroutines
     */
    protected void readASubr(int begin, int end, int gBias, int lBias, Map<Integer, int[]> hSubr, List<Integer> lSubr,
            int[] lSubrsOffsets) {
        emptyStack();
        numOfHints = 0;
        seek(begin);

        while (getPosition() < end) {
            readCommand();
            int pos = getPosition();
            int topElement = getTopElement();
            int numOfArgs = arg_count;

            handelStack();

            if (isCallSubr()) {
                handleCallSubr(topElement, lBias, hSubr, lSubr, lSubrsOffsets, pos, gBias);
            } else if (isCallGSubr()) {
                handleCallGSubr(topElement, gBias, lSubrsOffsets, pos, lBias);
            } else if (isStem()) {
                handleStem(numOfArgs);
            } else if (isMask()) {
                handleMask();
            }
        }
    }

    private int getTopElement() {
        if (arg_count > 0) {
            return (Integer) args[arg_count - 1];
        }
        return 0;
    }

    private boolean isCallSubr() {
        return Objects.equals(key, "callsubr");
    }

    private boolean isCallGSubr() {
        return Objects.equals(key, "callgsubr");
    }

    private boolean isStem() {
        return Objects.equals(key, "hstem") || Objects.equals(key, "vstem") || Objects.equals(key, "hstemhm") || Objects.equals(key, "vstemhm");
    }

    private boolean isMask() {
        return Objects.equals(key, "hintmask") || Objects.equals(key, "cntrmask");
    }

    private void handleCallSubr(int topElement, int lBias, Map<Integer, int[]> hSubr, List<Integer> lSubr,
            int[] lSubrsOffsets, int pos, int gBias) {
        if (arg_count > 0) {
            int subr = topElement + lBias;
            if (!hSubr.containsKey(subr)) {
                hSubr.put(subr, null);
                lSubr.add(subr);
            }
            if (lSubrsOffsets != null) {
                calcHints(lSubrsOffsets[subr], lSubrsOffsets[subr + 1], lBias, gBias, lSubrsOffsets);
            }
            seek(pos);
        }
    }

    private void handleCallGSubr(int topElement, int gBias, int[] lSubrsOffsets, int pos, int lBias) {
        if (arg_count > 0) {
            int subr = topElement + gBias;
            if (!hGSubrsUsed.containsKey(subr)) {
                hGSubrsUsed.put(subr, null);
                lGSubrsUsed.add(subr);
            }
            calcHints(gsubrOffsets[subr], gsubrOffsets[subr + 1], lBias, gBias, lSubrsOffsets);
            seek(pos);
        }
    }

    private void handleStem(int numOfArgs) {
        numOfHints += numOfArgs / 2;
    }

    private void handleMask() {
        int sizeOfMask = (numOfHints + 7) / 8;  // equivalent to Math.ceil(NumOfHints / 8.0)
        for (int i = 0; i < sizeOfMask; i++) {
            getCard8();
        }
    }


    /**
     * Function Checks how the current operator effects the run time stack after being run An operator may increase or
     * decrease the stack size
     */
    protected void handelStack() {
        // Find out what the operator does to the stack
        int stackHandel = stackOpp();
        if (stackHandel < 2) {
            // The operators that enlarge the stack by one
            if (stackHandel == 1) {
                pushStack();
            } else {
                // The operators that pop the stack
                // Abs value for the for loop
                stackHandel *= -1;
                for (int i = 0; i < stackHandel; i++) {
                    popStack();
                }
            }

        } else {
            // All other flush the stack
            emptyStack();
        }
    }

    /**
     * Function checks the key and return the change to the stack after the operator
     *
     * @return The change in the stack. {@literal 2->} flush the stack
     */
    protected int stackOpp() {
        if (Objects.equals(key, "ifelse")) {
            return -3;
        }
        if (Objects.equals(key, "roll") || Objects.equals(key, "put")) {
            return -2;
        }
        if (Objects.equals(key, "callsubr") || Objects.equals(key, "callgsubr") || Objects.equals(key, "add")
                || Objects.equals(key, "sub") ||
                Objects.equals(key, "div") || Objects.equals(key, "mul") || Objects.equals(key, "drop")
                || Objects.equals(key, "and") ||
                Objects.equals(key, "or") || Objects.equals(key, "eq")) {
            return -1;
        }
        if (Objects.equals(key, "abs") || Objects.equals(key, "neg") || Objects.equals(key, "sqrt") || Objects.equals(
                key, "exch") ||
                Objects.equals(key, "index") || Objects.equals(key, "get") || Objects.equals(key, "not")
                || Objects.equals(key, "return")) {
            return 0;
        }
        if (Objects.equals(key, "random") || Objects.equals(key, "dup")) {
            return 1;
        }
        return 2;
    }

    /**
     * Empty the Type2 Stack
     */
    protected void emptyStack() {
        // Null the arguments
        for (int i = 0; i < arg_count; i++) {
            args[i] = null;
        }
        arg_count = 0;
    }

    /**
     * Pop one element from the stack
     */
    protected void popStack() {
        if (arg_count > 0) {
            args[arg_count - 1] = null;
            arg_count--;
        }
    }

    /**
     * Add an item to the stack
     */
    protected void pushStack() {
        arg_count++;
    }

    /**
     * The function reads the next command after the file pointer is set
     */
    protected void readCommand() {
        key = null;
        while (true) {
            char b0 = getCard8();
            if (isOperator(b0)) {
                setKey(b0);
                break;
            } else {
                readArgument(b0);
            }
        }
    }

    private boolean isOperator(char b0) {
        return b0 <= 31;
    }

    private void setKey(char b0) {
        key = b0 == 12 ? getEscapeKey() : subrsFunctions[b0];
    }

    private String getEscapeKey() {
        int b1 = getCard8();
        return b1 > subrsEscapeFuncs.length - 1 ? subrsEscapeFuncs[subrsEscapeFuncs.length - 1] : subrsEscapeFuncs[b1];
    }

    private void readArgument(char b0) {
        switch (b0) {
            case 28:
                readTwoBytesArgument();
                break;
            case 255:
                readFourBytesArgument();
                break;
            default:
                readSingleByteArgument(b0);
        }
    }

    private void readTwoBytesArgument() {
        int first = getCard8();
        int second = getCard8();
        args[arg_count++] = first << 8 | second;
    }

    private void readFourBytesArgument() {
        int first = getCard8();
        int second = getCard8();
        int third = getCard8();
        int fourth = getCard8();
        args[arg_count++] = first << 24 | second << 16 | third << 8 | fourth;
    }

    private void readSingleByteArgument(char b0) {
        int value = b0 >= 32 && b0 <= 246 ? b0 - 139 : calculateSignedShort(b0);
        args[arg_count++] = value;
    }

    private int calculateSignedShort(char b0) {
        int w = getCard8();
        return b0 >= 247 && b0 <= 250 ? (b0 - 247) * 256 + w + 108 : -(b0 - 251) * 256 - w - 108;
    }

    /**
     * The function reads the subroutine and returns the number of the hint in it. If a call to another subroutine is
     * found the function calls recursively.
     *
     * @param begin         the start point of the subr
     * @param end           the end point of the subr
     * @param lBias         the bias of the Local Subrs
     * @param gBias         the bias of the Global Subrs
     * @param lSubrsOffsets The Offsets array of the subroutines
     */
    protected void calcHints(int begin, int end, int lBias, int gBias, int[] lSubrsOffsets) {
        seek(begin);
        while (getPosition() < end) {
            readCommand();
            int pos = getPosition();
            int numOfArgs = arg_count;
            Object topElement = getTopElement();

            handelStack();

            if (isCallSubr()) {
                handleCallSubr(numOfArgs, topElement, lBias, lSubrsOffsets, pos, gBias);
            } else if (isCallGSubr()) {
                handleCallGSubr(numOfArgs, topElement, gBias, lSubrsOffsets, pos, lBias);
            } else if (isStem()) {
                handleStem(numOfArgs);
            } else if (isMask()) {
                handleMask();
            }
        }
    }

    private void handleCallSubr(int numOfArgs, Object topElement, int lBias, int[] lSubrsOffsets, int pos, int gBias) {
        if (numOfArgs > 0) {
            int subr = (Integer) topElement + lBias;
            calcHints(lSubrsOffsets[subr], lSubrsOffsets[subr + 1], lBias, gBias, lSubrsOffsets);
            seek(pos);
        }
    }

    private void handleCallGSubr(int numOfArgs, Object topElement, int gBias, int[] lSubrsOffsets, int pos, int lBias) {
        if (numOfArgs > 0) {
            int subr = (Integer) topElement + gBias;
            calcHints(gsubrOffsets[subr], gsubrOffsets[subr + 1], lBias, gBias, lSubrsOffsets);
            seek(pos);
        }
    }

    /**
     * Function builds the new offset array, object array and assembles the index. used for creating the glyph and subrs
     * subsetted index
     *
     * @param offsets                  the offset array of the original index
     * @param used                     the hashmap of the used objects
     * @param operatorForUnusedEntries the operator inserted into the data stream for unused entries
     * @return the new index subset version
     * @throws IOException on error
     */
    protected byte[] buildNewIndex(int[] offsets, Map<Integer, int[]> used, byte operatorForUnusedEntries)
            throws IOException {
        int unusedCount = 0;
        int offset = 0;
        int[] newOffsets = new int[offsets.length];
        // Build the Offsets Array for the Subset
        for (int i = 0; i < offsets.length; ++i) {
            newOffsets[i] = offset;
            // If the object in the offset is also present in the used
            // HashMap then increment the offset var by its size
            if (used.containsKey(i)) {
                if (offsets.length > i + 1) {
                    offset += offsets[i + 1] - offsets[i];
                }
            } else {
                // Else the same offset is kept in i+1.
                unusedCount++;
            }
        }
        // Offset var determines the size of the object array
        byte[] newObjects = new byte[offset + unusedCount];
        // Build the new Object array
        int unusedOffset = 0;
        for (int i = 0; i < offsets.length - 1; ++i) {
            int start = newOffsets[i];
            int end = newOffsets[i + 1];
            newOffsets[i] = start + unusedOffset;
            // If start != End then the Object is used
            // So, we will copy the object data from the font file
            if (start != end) {
                // All offsets are Global Offsets relative to the beginning of the font file.
                // Jump the file pointer to the start address to read from.
                buf.seek(offsets[i]);
                // Read from the buffer and write into the array at start.
                buf.readFully(newObjects, start + unusedOffset, end - start);
            } else {
                newObjects[start + unusedOffset] = operatorForUnusedEntries;
                unusedOffset++;
            }
        }
        newOffsets[offsets.length - 1] += unusedOffset;
        // Use assembleIndex to build the index from the offset & object arrays
        return assembleIndex(newOffsets, newObjects);
    }

    /**
     * Function creates the new index, inserting the count,offsetsize,offset array and object array.
     *
     * @param newOffsets the subsetted offset array
     * @param newObjects the subsetted object array
     * @return the new index created
     */
    protected byte[] assembleIndex(int[] newOffsets, byte[] newObjects) {
        // Calc the index' count field
        char count = (char) (newOffsets.length - 1);
        // Calc the size of the object array
        int size = newOffsets[newOffsets.length - 1];
        // Calc the Offsize
        byte offsize;
        if (size <= 0xff) {
            offsize = 1;
        } else if (size <= 0xffff) {
            offsize = 2;
        } else if (size <= 0xffffff) {
            offsize = 3;
        } else {
            offsize = 4;
        }
        // The byte array for the new index. The size is calc by
        // Count=2, Offsize=1, OffsetArray = Offsize*(Count+1), The object array
        byte[] newIndex = new byte[2 + 1 + offsize * (count + 1) + newObjects.length];
        // The counter for writing
        int place = 0;
        // Write the count field
        newIndex[place++] = (byte) ((count >>> 8) & 0xff);
        newIndex[place++] = (byte) ((count >>> 0) & 0xff);
        // Write the offsize field
        newIndex[place++] = offsize;
        // Write the offset array according to the offsize
        for (int newOffset : newOffsets) {
            // The value to be written
            int num = newOffset - newOffsets[0] + 1;
            // Write in bytes according to the offsize
            switch (offsize) {
                case 4:
                    newIndex[place++] = (byte) ((num >>> 24) & 0xff); // fallthrough
                case 3:
                    newIndex[place++] = (byte) ((num >>> 16) & 0xff); // fallthrough
                case 2:
                    newIndex[place++] = (byte) ((num >>> 8) & 0xff); // fallthrough
                case 1:
                    newIndex[place++] = (byte) ((num) & 0xff);
                default:
                    break;
            }
        }
        // Write the new object array one by one
        for (byte newObject : newObjects) {
            newIndex[place++] = newObject;
        }
        // Return the new index
        return newIndex;
    }

    /**
     * The function builds the new output stream according to the subset process
     *
     * @param font the font
     * @return the subsetted font stream
     */
    protected byte[] buildNewFile(int font) {
        // Prepare linked list for new font components
        outputList = new LinkedList<>();

        // copy the header of the font
        copyHeader();

        // create a name index
        buildIndexHeader(1, 1, 1);
        outputList.addLast(new UInt8Item((char) (1 + fonts[font].name.length())));
        outputList.addLast(new StringItem(fonts[font].name));

        // create the topdict Index
        buildIndexHeader(1, 2, 1);
        OffsetItem topdictIndex1Ref = new IndexOffsetItem(2);
        outputList.addLast(topdictIndex1Ref);
        IndexBaseItem topdictBase = new IndexBaseItem();
        outputList.addLast(topdictBase);

        // Initialize the Dict Items for later use
        OffsetItem charsetRef = new DictOffsetItem();
        OffsetItem charstringsRef = new DictOffsetItem();
        OffsetItem fdarrayRef = new DictOffsetItem();
        OffsetItem fdselectRef = new DictOffsetItem();
        OffsetItem privateRef = new DictOffsetItem();

        // If the font is not CID create the following keys
        createKeysIfFontNotCID(font);
        // Go to the TopDict of the font being processed
        seek(topdictOffsets[font]);
        // Run until the end of the TopDict
        while (getPosition() < topdictOffsets[font + 1]) {
            int p1 = getPosition();
            getDictItem();
            int p2 = getPosition();
            // The encoding key is disregarded since CID has no encoding
            final boolean isEncodingKey = Objects.equals(key, "Encoding")
                    // These keys will be added manually by the process.
                    || Objects.equals(key, "Private")
                    || Objects.equals(key, "FDSelect")
                    || Objects.equals(key, "FDArray")
                    || Objects.equals(key, "charset")
                    || Objects.equals(key, "CharStrings");
            if (!isEncodingKey) {
                // copy key "as is" to the output list
                outputList.add(new RangeItem(buf, p1, p2 - p1));
            }
        }
        // Create the FDArray, FDSelect, Charset and CharStrings Keys
        createKeys(fdarrayRef, fdselectRef, charsetRef, charstringsRef);

        // Mark the end of the top dict area
        outputList.addLast(new IndexMarkerItem(topdictIndex1Ref, topdictBase));

        // Copy the string index

        if (fonts[font].isCID) {
            outputList.addLast(getEntireIndexRange(stringIndexOffset));
        } else {
            // If the font is not CID we need to append new strings.
            // We need 3 more strings: Registry, Ordering, and a FontName for one FD.
            // The total length is at most "Adobe"+"Identity"+63 = 76
            createNewStringIndex(font);
        }

        // copy the new subsetted global subroutine index
        outputList.addLast(new RangeItem(new RandomAccessFileOrArray(newGSubrsIndex), 0, newGSubrsIndex.length));

        // deal with fdarray, fdselect, and the font descriptors
        // If the font is CID:
        if (fonts[font].isCID) {
            updateOffsetItems(fdselectRef, charsetRef, fdarrayRef, privateRef, font);
        } else {
            // If the font is not CID
            // create FDSelect
            createFDSelect(fdselectRef, fonts[font].nglyphs);
            // recreate a new charset
            createCharset(charsetRef, fonts[font].nglyphs);
            // create a font dict index (fdarray)
            createFDArray(fdarrayRef, privateRef, font);
        }

        // if a private dict exists insert its subsetted version
        if (fonts[font].privateOffset >= 0) {
            // Mark the beginning of the private dict
            IndexBaseItem privateBase = new IndexBaseItem();
            outputList.addLast(privateBase);
            outputList.addLast(new MarkerItem(privateRef));

            OffsetItem subr = new DictOffsetItem();
            // Build and copy the new private dict
            createNonCIDPrivate(font, subr);
            // Copy the new LSubrs index
            createNonCIDSubrs(font, privateBase, subr);
        }

        // copy the charstring index
        outputList.addLast(new MarkerItem(charstringsRef));

        // Add the subsetted charstring
        outputList.addLast(
                new RangeItem(new RandomAccessFileOrArray(newCharStringsIndex), 0, newCharStringsIndex.length));

        // now create the new CFF font
        int[] currentOffset = new int[1];
        currentOffset[0] = 0;
        // Count and save the offset for each item
        Iterator<Item> listIter = outputList.iterator();
        while (listIter.hasNext()) {
            Item item = (Item) listIter.next();
            item.increment(currentOffset);
        }
        // Compute the Xref for each of the offset items
        listIter = outputList.iterator();
        while (listIter.hasNext()) {
            Item item = (Item) listIter.next();
            item.xref();
        }

        int size = currentOffset[0];
        byte[] b = new byte[size];

        // Emit all the items into the new byte array
        listIter = outputList.iterator();
        while (listIter.hasNext()) {
            Item item = (Item) listIter.next();
            item.emit(b);
        }
        // Return the new stream
        return b;
    }

    private void createKeysIfFontNotCID(int font){
        if (!fonts[font].isCID) {
            // create a ROS key
            outputList.addLast(new DictNumberItem(fonts[font].nstrings));
            outputList.addLast(new DictNumberItem(fonts[font].nstrings + 1));
            outputList.addLast(new DictNumberItem(0));
            outputList.addLast(new UInt8Item((char) 12));
            outputList.addLast(new UInt8Item((char) 30));
            // create a CIDCount key
            outputList.addLast(new DictNumberItem(fonts[font].nglyphs));
            outputList.addLast(new UInt8Item((char) 12));
            outputList.addLast(new UInt8Item((char) 34));
            // Sivan's comments
            // What about UIDBase (12,35)? Don't know what is it.
            // I don't think we need FontName; the font I looked at didn't have it.
        }
    }

    private void updateOffsetItems(OffsetItem fdselectRef, OffsetItem charsetRef, OffsetItem fdarrayRef,
            OffsetItem privateRef, int font){
        // copy the FDArray, FDSelect, charset

        // Copy FDSelect
        // Mark the beginning
        outputList.addLast(new MarkerItem(fdselectRef));
        // If an FDSelect exists copy it
        if (fonts[font].fdselectOffset >= 0) {
            outputList.addLast(new RangeItem(buf, fonts[font].fdselectOffset, fonts[font].FDSelectLength));
        } else {
            // Else create a new one
            createFDSelect(fdselectRef, fonts[font].nglyphs);
        }

        // Copy the Charset
        // Mark the beginning and copy entirely
        outputList.addLast(new MarkerItem(charsetRef));
        outputList.addLast(new RangeItem(buf, fonts[font].charsetOffset, fonts[font].CharsetLength));

        // Copy the FDArray
        // If an FDArray exists
        if (fonts[font].fdarrayOffset >= 0) {
            // Mark the beginning
            outputList.addLast(new MarkerItem(fdarrayRef));
            // Build a new FDArray with its private dicts and their LSubrs
            reconstruct(font);
        } else {
            // Else create a new one
            createFDArray(fdarrayRef, privateRef, font);
        }
    }

    /**
     * Function Copies the header from the original fileto the output list
     */
    protected void copyHeader() {
        seek(0);
        int major = getCard8();
        int minor = getCard8();
        int hdrSize = getCard8();
        int offSize = getCard8();
        nextIndexOffset = hdrSize;
        outputList.addLast(new RangeItem(buf, 0, hdrSize));
    }

    /**
     * Function Build the header of an index
     *
     * @param count   the count field of the index
     * @param offsize the offsize field of the index
     * @param first   the first offset of the index
     */
    protected void buildIndexHeader(int count, int offsize, int first) {
        // Add the count field
        outputList.addLast(new UInt16Item((char) count)); // count
        // Add the offsize field
        outputList.addLast(new UInt8Item((char) offsize)); // offSize
        // Add the first offset according to the offsize
        switch (offsize) {
            case 1:
                outputList.addLast(new UInt8Item((char) first)); // first offset
                break;
            case 2:
                outputList.addLast(new UInt16Item((char) first)); // first offset
                break;
            case 3:
                outputList.addLast(new UInt24Item((char) first)); // first offset
                break;
            case 4:
                outputList.addLast(new UInt32Item((char) first)); // first offset
                break;
            default:
                break;
        }
    }

    /**
     * Function adds the keys into the TopDict
     *
     * @param fdarrayRef     OffsetItem for the FDArray
     * @param fdselectRef    OffsetItem for the FDSelect
     * @param charsetRef     OffsetItem for the CharSet
     * @param charstringsRef OffsetItem for the CharString
     */
    protected void createKeys(OffsetItem fdarrayRef, OffsetItem fdselectRef, OffsetItem charsetRef,
            OffsetItem charstringsRef) {
        // create an FDArray key
        outputList.addLast(fdarrayRef);
        outputList.addLast(new UInt8Item((char) 12));
        outputList.addLast(new UInt8Item((char) 36));
        // create an FDSelect key
        outputList.addLast(fdselectRef);
        outputList.addLast(new UInt8Item((char) 12));
        outputList.addLast(new UInt8Item((char) 37));
        // create an charset key
        outputList.addLast(charsetRef);
        outputList.addLast(new UInt8Item((char) 15));
        // create a CharStrings key
        outputList.addLast(charstringsRef);
        outputList.addLast(new UInt8Item((char) 17));
    }

    /**
     * Function takes the original string item and adds the new strings to accommodate the CID rules
     *
     * @param font the font
     */
    protected void createNewStringIndex(int font) {
        String fdFontName = fonts[font].name + "-OneRange";
        if (fdFontName.length() > 127) {
            fdFontName = fdFontName.substring(0, 127);
        }
        String extraStrings = "Adobe" + "Identity" + fdFontName;

        int origStringsLen = stringOffsets[stringOffsets.length - 1]
                - stringOffsets[0];
        int stringsBaseOffset = stringOffsets[0] - 1;

        byte stringsIndexOffSize;
        if (origStringsLen + extraStrings.length() <= 0xff) {
            stringsIndexOffSize = 1;
        } else if (origStringsLen + extraStrings.length() <= 0xffff) {
            stringsIndexOffSize = 2;
        } else if (origStringsLen + extraStrings.length() <= 0xffffff) {
            stringsIndexOffSize = 3;
        } else {
            stringsIndexOffSize = 4;
        }

        outputList.addLast(new UInt16Item((char) ((stringOffsets.length - 1) + 3))); // count
        outputList.addLast(new UInt8Item((char) stringsIndexOffSize)); // offSize
        for (int stringOffset : stringOffsets) {
            outputList.addLast(new IndexOffsetItem(stringsIndexOffSize,
                    stringOffset - stringsBaseOffset));
        }
        int currentStringsOffset = stringOffsets[stringOffsets.length - 1]
                - stringsBaseOffset;
        
        currentStringsOffset += "Adobe".length();
        outputList.addLast(new IndexOffsetItem(stringsIndexOffSize, currentStringsOffset));
        currentStringsOffset += "Identity".length();
        outputList.addLast(new IndexOffsetItem(stringsIndexOffSize, currentStringsOffset));
        currentStringsOffset += fdFontName.length();
        outputList.addLast(new IndexOffsetItem(stringsIndexOffSize, currentStringsOffset));

        outputList.addLast(new RangeItem(buf, stringOffsets[0], origStringsLen));
        outputList.addLast(new StringItem(extraStrings));
    }

    /**
     * Function creates new FDSelect for non-CID fonts. The FDSelect built uses a single range for all glyphs
     *
     * @param fdselectRef OffsetItem for the FDSelect
     * @param nglyphs     the number of glyphs in the font
     */
    protected void createFDSelect(OffsetItem fdselectRef, int nglyphs) {
        outputList.addLast(new MarkerItem(fdselectRef));
        outputList.addLast(new UInt8Item((char) 3)); // format identifier
        outputList.addLast(new UInt16Item((char) 1)); // nRanges

        outputList.addLast(new UInt16Item((char) 0)); // Range[0].firstGlyph
        outputList.addLast(new UInt8Item((char) 0)); // Range[0].fd

        outputList.addLast(new UInt16Item((char) nglyphs)); // sentinel
    }

    /**
     * Function creates new CharSet for non-CID fonts. The CharSet built uses a single range for all glyphs
     *
     * @param charsetRef OffsetItem for the CharSet
     * @param nglyphs    the number of glyphs in the font
     */
    protected void createCharset(OffsetItem charsetRef, int nglyphs) {
        outputList.addLast(new MarkerItem(charsetRef));
        outputList.addLast(new UInt8Item((char) 2)); // format identifier
        outputList.addLast(new UInt16Item((char) 1)); // first glyph in range (ignore .notdef)
        outputList.addLast(new UInt16Item((char) (nglyphs - 1))); // nLeft
    }

    /**
     * Function creates new FDArray for non-CID fonts. The FDArray built has only the "Private" operator that points to
     * the font's original private dict
     *
     * @param fdarrayRef OffsetItem for the FDArray
     * @param privateRef OffsetItem for the Private Dict
     * @param font       the font
     */
    protected void createFDArray(OffsetItem fdarrayRef, OffsetItem privateRef, int font) {
        outputList.addLast(new MarkerItem(fdarrayRef));
        // Build the header (count=offsize=first=1)
        buildIndexHeader(1, 1, 1);

        // Mark
        OffsetItem privateIndex1Ref = new IndexOffsetItem(1);
        outputList.addLast(privateIndex1Ref);
        IndexBaseItem privateBase = new IndexBaseItem();
        // Insert the private operands and operator
        outputList.addLast(privateBase);
        // Calc the new size of the private after subsetting
        // Origianl size
        int newSize = fonts[font].privateLength;
        // Calc the original size of the Subr offset in the private
        int orgSubrsOffsetSize = calcSubrOffsetSize(fonts[font].privateOffset, fonts[font].privateLength);
        // Increase the ptivate's size
        if (orgSubrsOffsetSize != 0) {
            newSize += 5 - orgSubrsOffsetSize;
        }
        outputList.addLast(new DictNumberItem(newSize));
        outputList.addLast(privateRef);
        outputList.addLast(new UInt8Item((char) 18)); // Private

        outputList.addLast(new IndexMarkerItem(privateIndex1Ref, privateBase));
    }

    /**
     * Function reconstructs the FDArray, PrivateDict and LSubr for CID fonts
     *
     * @param font the font
     */
    void reconstruct(int font) {
        // Init for later use
        OffsetItem[] fdPrivate = new DictOffsetItem[fonts[font].FDArrayOffsets.length - 1];
        IndexBaseItem[] fdPrivateBase = new IndexBaseItem[fonts[font].fdprivateOffsets.length];
        OffsetItem[] fdSubrs = new DictOffsetItem[fonts[font].fdprivateOffsets.length];
        // Reconstruct each type
        reconstructFDArray(font, fdPrivate);
        reconstructPrivateDict(font, fdPrivate, fdPrivateBase, fdSubrs);
        reconstructPrivateSubrs(font, fdPrivateBase, fdSubrs);
    }

    /**
     * Function subsets the FDArray and builds the new one with new offsets
     *
     * @param font      The font
     * @param fdPrivate OffsetItem Array (one for each FDArray)
     */
    void reconstructFDArray(int font, OffsetItem[] fdPrivate) {
        // Build the header of the index
        buildIndexHeader(fonts[font].FDArrayCount, fonts[font].FDArrayOffsize, 1);

        IndexBaseItem fdArrayBase = new IndexBaseItem();
        // Create offset items and declare beginning of the object array
        createOffsetItemsAndBase(font, fdArrayBase);

        // Process each object
        processObjects(font, fdPrivate, fdArrayBase);
    }

    private void createOffsetItemsAndBase(int font, IndexBaseItem fdArrayBase) {
        OffsetItem[] fdOffsets = new IndexOffsetItem[fonts[font].FDArrayOffsets.length - 1];
        for (int i = 0; i < fonts[font].FDArrayOffsets.length - 1; i++) {
            fdOffsets[i] = new IndexOffsetItem(fonts[font].FDArrayOffsize);
            outputList.addLast(fdOffsets[i]);
        }
        outputList.addLast(fdArrayBase);

    }

    private void processObjects(int font, OffsetItem[] fdPrivate, IndexBaseItem fdArrayBase) {
        for (int k = 0; k < fonts[font].FDArrayOffsets.length - 1; k++) {
            if (fdArrayUsed.containsKey(k)) {
                processObject(font, k, fdPrivate);
            }
            markObjectEnd(k, fdPrivate, fdArrayBase);
        }
    }

    private void processObject(int font, int k, OffsetItem[] fdPrivate) {
        seek(fonts[font].FDArrayOffsets[k]);
        while (getPosition() < fonts[font].FDArrayOffsets[k + 1]) {
            int p1 = getPosition();
            getDictItem();
            int p2 = getPosition();


            if (isPrivateDict()) {
                handlePrivateDict(p1, p2, font, k, fdPrivate);
            } else {
                copyDictRange(p1, p2);
            }
        }
    }

    private void handlePrivateDict(int p1, int p2, int font, int k, OffsetItem[] fdPrivate) {
        // Save the original length of the private dict
        int newSize = (Integer) args[0];
        // Save the size of the offset to the subrs in that private
        int orgSubrsOffsetSize = calcSubrOffsetSize(fonts[font].fdprivateOffsets[k], fonts[font].fdprivateLengths[k]);
        // Increase the private's length accordingly
        if (orgSubrsOffsetSize != 0) {
            newSize += 5 - orgSubrsOffsetSize;
        }
        // Insert the new size, OffsetItem and operator key number
        outputList.addLast(new DictNumberItem(newSize));
        fdPrivate[k] = new DictOffsetItem();
        outputList.addLast(fdPrivate[k]);
        outputList.addLast(new UInt8Item((char) 18)); // Private

        // Copy the private dict content, potentially modifying it based on your requirements
        outputList.addLast(new RangeItem(buf, p1, p2 - p1));
    }

    private void copyDictRange(int p1, int p2) {
        outputList.addLast(new RangeItem(buf, p1, p2 - p1));
    }

    private void markObjectEnd(int k, OffsetItem[] fdOffsets, IndexBaseItem fdArrayBase) {
        outputList.addLast(new IndexMarkerItem(fdOffsets[k], fdArrayBase));
    }

    private boolean isPrivateDict() {
        return Objects.equals(key, "Private");
    }

    /**
     * Function Adds the new private dicts (only for the FDs used) to the list
     *
     * @param font          the font
     * @param fdPrivate     OffsetItem array one element for each private
     * @param fdPrivateBase IndexBaseItem array one element for each private
     * @param fdSubrs       OffsetItem array one element for each private
     */
    void reconstructPrivateDict(int font, OffsetItem[] fdPrivate, IndexBaseItem[] fdPrivateBase,
            OffsetItem[] fdSubrs) {

        // For each fdarray private dict check if that FD is used.
        // if is used build a new one by changing the subrs offset
        // Else do nothing
        for (int i = 0; i < fonts[font].fdprivateOffsets.length; i++) {
            if (fdArrayUsed.containsKey(i)) {
                // Mark beginning
                outputList.addLast(new MarkerItem(fdPrivate[i]));
                fdPrivateBase[i] = new IndexBaseItem();
                outputList.addLast(fdPrivateBase[i]);
                // Goto beginning of objects
                seek(fonts[font].fdprivateOffsets[i]);
                while (getPosition() < fonts[font].fdprivateOffsets[i] + fonts[font].fdprivateLengths[i]) {
                    int p1 = getPosition();
                    getDictItem();
                    int p2 = getPosition();
                    // If the dictItem is the "Subrs" then,
                    // use marker for offset and write operator number
                    if (Objects.equals(key, "Subrs")) {
                        fdSubrs[i] = new DictOffsetItem();
                        outputList.addLast(fdSubrs[i]);
                        outputList.addLast(new UInt8Item((char) 19)); // Subrs
                    } else {
                        // Else copy the entire range
                        outputList.addLast(new RangeItem(buf, p1, p2 - p1));
                    }
                }
            }
        }
    }

    /**
     * Function Adds the new LSubrs dicts (only for the FDs used) to the list
     *
     * @param font          The index of the font
     * @param fdPrivateBase The IndexBaseItem array for the linked list
     * @param fdSubrs       OffsetItem array for the linked list
     */

    void reconstructPrivateSubrs(int font, IndexBaseItem[] fdPrivateBase,
            OffsetItem[] fdSubrs) {
        // For each private dict
        for (int i = 0; i < fonts[font].fdprivateLengths.length; i++) {
            // If that private dict's Subrs are used insert the new LSubrs
            // computed earlier
            if (fdSubrs[i] != null && fonts[font].PrivateSubrsOffset[i] >= 0) {
                outputList.addLast(new SubrMarkerItem(fdSubrs[i], fdPrivateBase[i]));
                outputList.addLast(
                        new RangeItem(new RandomAccessFileOrArray(newLSubrsIndex[i]), 0, newLSubrsIndex[i].length));
            }
        }
    }

    /**
     * Calculates how many byte it took to write the offset for the subrs in a specific private dict.
     *
     * @param offset The Offset for the private dict
     * @param size   The size of the private dict
     * @return The size of the offset of the subrs in the private dict
     */
    int calcSubrOffsetSize(int offset, int size) {
        // Set the size to 0
        int offsetSize = 0;
        // Go to the beginning of the private dict
        seek(offset);
        // Go until the end of the private dict
        while (getPosition() < offset + size) {
            int p1 = getPosition();
            getDictItem();
            int p2 = getPosition();
            // When reached to the subrs offset
            if (Objects.equals(key, "Subrs")) {
                // The Offsize (minus the subrs key)
                offsetSize = p2 - p1 - 1;
            }
            // All other keys are ignored
        }
        // return the size
        return offsetSize;
    }

    /**
     * Function computes the size of an index
     *
     * @param indexOffset The offset for the computed index
     * @return The size of the index
     */
    protected int countEntireIndexRange(int indexOffset) {
        // Go to the beginning of the index
        seek(indexOffset);
        // Read the count field
        int count = getCard16();
        // If count==0 -> size=2
        if (count == 0) {
            return 2;
        } else {
            // Read the offsize field
            int indexOffSize = getCard8();
            // Go to the last element of the offset array
            seek(indexOffset + 2 + 1 + count * indexOffSize);
            // The size of the object array is the value of the last element-1
            int size = getOffset(indexOffSize) - 1;
            // Return the size of the entire index
            return 2 + 1 + (count + 1) * indexOffSize + size;
        }
    }

    /**
     * The function creates a private dict for a font that was not CID All the keys are copied as is except for the
     * subrs key
     *
     * @param font the font
     * @param subr The OffsetItem for the subrs of the private
     */
    void createNonCIDPrivate(int font, OffsetItem subr) {
        // Go to the beginning of the private dict and read until the end
        seek(fonts[font].privateOffset);
        while (getPosition() < fonts[font].privateOffset + fonts[font].privateLength) {
            int p1 = getPosition();
            getDictItem();
            int p2 = getPosition();
            // If the dictItem is the "Subrs" then,
            // use marker for offset and write operator number
            if (Objects.equals(key, "Subrs")) {
                outputList.addLast(subr);
                outputList.addLast(new UInt8Item((char) 19)); // Subrs
            } else {
                // Else copy the entire range
                outputList.addLast(new RangeItem(buf, p1, p2 - p1));
            }
        }
    }

    /**
     * the function marks the beginning of the subrs index and adds the subsetted subrs index to the output list.
     *
     * @param font        the font
     * @param privateBase IndexBaseItem for the private that's referencing to the subrs
     * @param subrs       OffsetItem for the subrs
     */
    void createNonCIDSubrs(int font, IndexBaseItem privateBase, OffsetItem subrs) {
        // Mark the beginning of the Subrs index
        outputList.addLast(new SubrMarkerItem(subrs, privateBase));
        // Put the subsetted new subrs index
        if (newSubrsIndexNonCID != null) {
            outputList.addLast(
                    new RangeItem(new RandomAccessFileOrArray(newSubrsIndexNonCID), 0, newSubrsIndexNonCID.length));
        }
    }
}
