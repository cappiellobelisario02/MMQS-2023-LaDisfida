/*
 *
 * Copyright 2003 Sivan Toledo
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
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
 */

/*
 * Comments by Sivan Toledo:
 * I created this class in order to add to iText the ability to utilize
 * OpenType fonts with CFF glyphs (these usually have an .otf extension).
 * The CFF font within the CFF table of the OT font might be either a CID
 * or a Type1 font. (CFF fonts may also contain multiple fonts; I do not
 * know if this is allowed in an OT table). The PDF spec, however, only
 * allow a CID font with an Identity-H or Identity-V encoding. Otherwise,
 * you are limited to an 8-bit encoding.
 * Adobe fonts come in both flavors. That is, the OTFs sometimes have
 * a CID CFF inside (for Japanese fonts), and sometimes a Type1 CFF
 * (virtually all the others, Latin/Greek/Cyrillic). So to easily use
 * all the glyphs in the latter, without creating multiple 8-bit encoding,
 * I wrote this class, whose main purpose is to convert a Type1 font inside
 * a CFF container (which might include other fonts) into a CID CFF font
 * that can be directly embeded in the PDF.
 *
 * Limitations of the current version:
 * 1. It does not extract a single CID font from a CFF that contains that
 *    particular CID along with other fonts. The Adobe Japanese OTF's that
 *    I have only have one font in the CFF table, so these can be
 *    embeded in the PDF as is.
 * 2. It does not yet subset fonts.
 * 3. It may or may not work on CFF fonts that are not within OTF's.
 *    I didn't try that. In any case, that would probably only be
 *    useful for subsetting CID fonts, not for CFF Type1 fonts (I don't
 *    think there are any available.
 * I plan to extend the class to support these three features at some
 * future time.
 */

package com.lowagie.text.pdf;

import com.lowagie.text.ExceptionConverter;


public class CFFFont {

    public static final String CHARSET = "charset";
    public static final String PRIVATE = "Private";
    public static final String CHAR_STRINGS = "CharStrings";
    public static final String UNIQUE_ID = "UniqueID";
    public static final String FDARRAY = "FDArray";
    public static final String FDSELECT = "FDSelect";
    static final String[] operatorNames = {
            "version", "Notice", "FullName", "FamilyName",
            "Weight", "FontBBox", "BlueValues", "OtherBlues",
            "FamilyBlues", "FamilyOtherBlues", "StdHW", "StdVW",
            "UNKNOWN_12", UNIQUE_ID, "XUID", CHARSET,
            "Encoding", CHAR_STRINGS, PRIVATE, "Subrs",
            "defaultWidthX", "nominalWidthX", "UNKNOWN_22", "UNKNOWN_23",
            "UNKNOWN_24", "UNKNOWN_25", "UNKNOWN_26", "UNKNOWN_27",
            "UNKNOWN_28", "UNKNOWN_29", "UNKNOWN_30", "UNKNOWN_31",
            "Copyright", "isFixedPitch", "ItalicAngle", "UnderlinePosition",
            "UnderlineThickness", "PaintType", "CharstringType", "FontMatrix",
            "StrokeWidth", "BlueScale", "BlueShift", "BlueFuzz",
            "StemSnapH", "StemSnapV", "ForceBold", "UNKNOWN_12_15",
            "UNKNOWN_12_16", "LanguageGroup", "ExpansionFactor", "initialRandomSeed",
            "SyntheticBase", "PostScript", "BaseFontName", "BaseFontBlend",
            "UNKNOWN_12_24", "UNKNOWN_12_25", "UNKNOWN_12_26", "UNKNOWN_12_27",
            "UNKNOWN_12_28", "UNKNOWN_12_29", "ROS", "CIDFontVersion",
            "CIDFontRevision", "CIDFontType", "CIDCount", "UIDBase",
            FDARRAY, FDSELECT, "FontName"
    };

    static final String[] standardStrings = {
            // Automatically generated from Appendix A of the CFF specification; do
            // not edit. Size should be 391.
            ".notdef", "space", "exclam", "quotedbl", "numbersign", "dollar",
            "percent", "ampersand", "quoteright", "parenleft", "parenright",
            "asterisk", "plus", "comma", "hyphen", "period", "slash", "zero", "one",
            "two", "three", "four", "five", "six", "seven", "eight", "nine", "colon",
            "semicolon", "less", "equal", "greater", "question", "at", "A", "B", "C",
            "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
            "S", "T", "U", "V", "W", "X", "Y", "Z", "bracketleft", "backslash",
            "bracketright", "asciicircum", "underscore", "quoteleft", "a", "b", "c",
            "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r",
            "s", "t", "u", "v", "w", "x", "y", "z", "braceleft", "bar", "braceright",
            "asciitilde", "exclamdown", "cent", "sterling", "fraction", "yen",
            "florin", "section", "currency", "quotesingle", "quotedblleft",
            "guillemotleft", "guilsinglleft", "guilsinglright", "fi", "fl", "endash",
            "dagger", "daggerdbl", "periodcentered", "paragraph", "bullet",
            "quotesinglbase", "quotedblbase", "quotedblright", "guillemotright",
            "ellipsis", "perthousand", "questiondown", "grave", "acute", "circumflex",
            "tilde", "macron", "breve", "dotaccent", "dieresis", "ring", "cedilla",
            "hungarumlaut", "ogonek", "caron", "emdash", "AE", "ordfeminine", "Lslash",
            "Oslash", "OE", "ordmasculine", "ae", "dotlessi", "lslash", "oslash", "oe",
            "germandbls", "onesuperior", "logicalnot", "mu", "trademark", "Eth",
            "onehalf", "plusminus", "Thorn", "onequarter", "divide", "brokenbar",
            "degree", "thorn", "threequarters", "twosuperior", "registered", "minus",
            "eth", "multiply", "threesuperior", "copyright", "Aacute", "Acircumflex",
            "Adieresis", "Agrave", "Aring", "Atilde", "Ccedilla", "Eacute",
            "Ecircumflex", "Edieresis", "Egrave", "Iacute", "Icircumflex", "Idieresis",
            "Igrave", "Ntilde", "Oacute", "Ocircumflex", "Odieresis", "Ograve",
            "Otilde", "Scaron", "Uacute", "Ucircumflex", "Udieresis", "Ugrave",
            "Yacute", "Ydieresis", "Zcaron", "aacute", "acircumflex", "adieresis",
            "agrave", "aring", "atilde", "ccedilla", "eacute", "ecircumflex",
            "edieresis", "egrave", "iacute", "icircumflex", "idieresis", "igrave",
            "ntilde", "oacute", "ocircumflex", "odieresis", "ograve", "otilde",
            "scaron", "uacute", "ucircumflex", "udieresis", "ugrave", "yacute",
            "ydieresis", "zcaron", "exclamsmall", "Hungarumlautsmall",
            "dollaroldstyle", "dollarsuperior", "ampersandsmall", "Acutesmall",
            "parenleftsuperior", "parenrightsuperior", "twodotenleader",
            "onedotenleader", "zerooldstyle", "oneoldstyle", "twooldstyle",
            "threeoldstyle", "fouroldstyle", "fiveoldstyle", "sixoldstyle",
            "sevenoldstyle", "eightoldstyle", "nineoldstyle", "commasuperior",
            "threequartersemdash", "periodsuperior", "questionsmall", "asuperior",
            "bsuperior", "centsuperior", "dsuperior", "esuperior", "isuperior",
            "lsuperior", "msuperior", "nsuperior", "osuperior", "rsuperior",
            "ssuperior", "tsuperior", "ff", "ffi", "ffl", "parenleftinferior",
            "parenrightinferior", "Circumflexsmall", "hyphensuperior", "Gravesmall",
            "Asmall", "Bsmall", "Csmall", "Dsmall", "Esmall", "Fsmall", "Gsmall",
            "Hsmall", "Ismall", "Jsmall", "Ksmall", "Lsmall", "Msmall", "Nsmall",
            "Osmall", "Psmall", "Qsmall", "Rsmall", "Ssmall", "Tsmall", "Usmall",
            "Vsmall", "Wsmall", "Xsmall", "Ysmall", "Zsmall", "colonmonetary",
            "onefitted", "rupiah", "Tildesmall", "exclamdownsmall", "centoldstyle",
            "Lslashsmall", "Scaronsmall", "Zcaronsmall", "Dieresissmall", "Brevesmall",
            "Caronsmall", "Dotaccentsmall", "Macronsmall", "figuredash",
            "hypheninferior", "Ogoneksmall", "Ringsmall", "Cedillasmall",
            "questiondownsmall", "oneeighth", "threeeighths", "fiveeighths",
            "seveneighths", "onethird", "twothirds", "zerosuperior", "foursuperior",
            "fivesuperior", "sixsuperior", "sevensuperior", "eightsuperior",
            "ninesuperior", "zeroinferior", "oneinferior", "twoinferior",
            "threeinferior", "fourinferior", "fiveinferior", "sixinferior",
            "seveninferior", "eightinferior", "nineinferior", "centinferior",
            "dollarinferior", "periodinferior", "commainferior", "Agravesmall",
            "Aacutesmall", "Acircumflexsmall", "Atildesmall", "Adieresissmall",
            "Aringsmall", "AEsmall", "Ccedillasmall", "Egravesmall", "Eacutesmall",
            "Ecircumflexsmall", "Edieresissmall", "Igravesmall", "Iacutesmall",
            "Icircumflexsmall", "Idieresissmall", "Ethsmall", "Ntildesmall",
            "Ogravesmall", "Oacutesmall", "Ocircumflexsmall", "Otildesmall",
            "Odieresissmall", "OEsmall", "Oslashsmall", "Ugravesmall", "Uacutesmall",
            "Ucircumflexsmall", "Udieresissmall", "Yacutesmall", "Thornsmall",
            "Ydieresissmall", "001.000", "001.001", "001.002", "001.003", "Black",
            "Bold", "Book", "Light", "Medium", "Regular", "Roman", "Semibold"
    };
    protected String key;
    protected Object[] args = new Object[48];
    protected int argCount = 0;
    /**
     * A random Access File or an array
     */
    protected RandomAccessFileOrArray buf;
    protected int nameIndexOffset;
    protected int topdictIndexOffset;
    protected int stringIndexOffset;
    protected int gsubrIndexOffset;
    protected int[] nameOffsets;
    protected int[] topdictOffsets;
    protected int[] stringOffsets;
    protected int[] gsubrOffsets;
    // Changed from private to protected by Ygal&Oren
    protected Font[] fonts;
    protected char offSize;
    int nextIndexOffset;

    public CFFFont(RandomAccessFileOrArray inputbuffer) {
        buf = inputbuffer;
        seek(0);

        int hdrSize = getCard8();
        offSize = getCard8();

        nameIndexOffset = hdrSize;
        nameOffsets = getIndex(nameIndexOffset);
        topdictIndexOffset = nameOffsets[nameOffsets.length - 1];
        topdictOffsets = getIndex(topdictIndexOffset);
        stringIndexOffset = topdictOffsets[topdictOffsets.length - 1];
        stringOffsets = getIndex(stringIndexOffset);
        gsubrIndexOffset = stringOffsets[stringOffsets.length - 1];
        gsubrOffsets = getIndex(gsubrIndexOffset);

        fonts = new Font[nameOffsets.length - 1];

        extractFontNames();
        parseTopDicts();
    }

    private void extractFontNames() {
        for (int j = 0; j < nameOffsets.length - 1; j++) {
            fonts[j] = new Font();
            seek(nameOffsets[j]);
            fonts[j].name = "";
            StringBuilder sb;
            for (int k = nameOffsets[j]; k < nameOffsets[j + 1]; k++) {
                sb = new StringBuilder(fonts[j].name);
                sb.append(getCard8());
                fonts[j].name = sb.toString();
            }
        }
    }

    private void parseTopDicts() {
        for (int j = 0; j < topdictOffsets.length - 1; j++) {
            seek(topdictOffsets[j]);
            parseTopDictItems(j);
            parsePrivateDict(j);
            parseFdArrayIndex(j);
        }
    }

    private void parseTopDictItems(int fontIndex) {
        while (getPosition() < topdictOffsets[fontIndex + 1]) {
            getDictItem();
            switch (key) {
                case "FullName":
                    fonts[fontIndex].fullName = getString((char) ((Integer) args[0]).intValue());
                    break;
                case "ROS":
                    fonts[fontIndex].isCID = true;
                    break;
                case PRIVATE:
                    fonts[fontIndex].privateLength = (Integer) args[0];
                    fonts[fontIndex].privateOffset = (Integer) args[1];
                    break;
                case CHARSET:
                    fonts[fontIndex].charsetOffset = (Integer) args[0];
                    break;
                case CHAR_STRINGS:
                    handleCharStrings(fontIndex);
                    break;
                case FDARRAY:
                    fonts[fontIndex].fdarrayOffset = (Integer) args[0];
                    break;
                case FDSELECT:
                    fonts[fontIndex].fdselectOffset = (Integer) args[0];
                    break;
                case "CharstringType":
                    fonts[fontIndex].charstringType = (Integer) args[0];
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + key);
            }
        }
    }

    private void handleCharStrings(int fontIndex) {
        int p = getPosition();
        fonts[fontIndex].charstringsOffsets = getIndex(fonts[fontIndex].charstringsOffset);
        seek(p);
    }

    private void parsePrivateDict(int fontIndex) {
        if (fonts[fontIndex].privateOffset >= 0) {
            seek(fonts[fontIndex].privateOffset);
            while (getPosition() < fonts[fontIndex].privateOffset + fonts[fontIndex].privateLength) {
                getDictItem();
                if ("Subrs".equals(key)) {
                    fonts[fontIndex].privateSubrs = (Integer) args[0] + fonts[fontIndex].privateOffset;
                }
            }
        }
    }

    private void parseFdArrayIndex(int fontIndex) {
        if (fonts[fontIndex].fdarrayOffset >= 0) {
            int[] fdarrayOffsets = getIndex(fonts[fontIndex].fdarrayOffset);
            fonts[fontIndex].fdprivateOffsets = new int[fdarrayOffsets.length - 1];
            fonts[fontIndex].fdprivateLengths = new int[fdarrayOffsets.length - 1];

            for (int k = 0; k < fdarrayOffsets.length - 1; k++) {
                seek(fdarrayOffsets[k]);
                parseFdArrayItems(fontIndex, k, fdarrayOffsets[k + 1]);
            }
        }
    }

    private void parseFdArrayItems(int fontIndex, int k, int endOffset) {
        while (getPosition() < endOffset) {
            getDictItem();
            if (PRIVATE.equals(key)) {
                fonts[fontIndex].fdprivateLengths[k] = (Integer) args[0];
                fonts[fontIndex].fdprivateOffsets[k] = (Integer) args[1];
            }
        }
    }


    public String getString(char sid) {
        if (sid < standardStrings.length) {
            return standardStrings[sid];
        }
        if (sid >= standardStrings.length + (stringOffsets.length - 1)) {
            return null;
        }
        int j = sid - standardStrings.length;
        int p = getPosition();
        seek(stringOffsets[j]);
        StringBuilder s = new StringBuilder();
        for (int k = stringOffsets[j]; k < stringOffsets[j + 1]; k++) {
            s.append(getCard8());
        }
        seek(p);
        return s.toString();
    }

    char getCard8() {
        try {
            byte i = buf.readByte();
            return (char) (i & 0xff);
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    char getCard16() {
        try {
            return buf.readChar();
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    int getOffset(int offSize) {
        int offset = 0;
        for (int i = 0; i < offSize; i++) {
            offset *= 256;
            offset += getCard8();
        }
        return offset;
    }

    void seek(int offset) {
        try {
            buf.seek(offset);
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    short getShort() {
        try {
            return buf.readShort();
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    int getInt() {
        try {
            return buf.readInt();
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    int getPosition() {
        try {
            return buf.getFilePointer();
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    // read the offsets in the next index
    // data structure, convert to global
    // offsets, and return them.
    // Sets the nextIndexOffset.
    int[] getIndex(int nextIndexOffset) {
        int count;
        int indexOffSize;

        seek(nextIndexOffset);
        count = getCard16();
        int[] offsets = new int[count + 1];

        if (count == 0) {
            offsets[0] = -1;
            return offsets;
        }

        indexOffSize = getCard8();

        for (int j = 0; j <= count; j++) {
            //nextIndexOffset = ofset to relative segment
            offsets[j] = nextIndexOffset
                    //2-> count in the index header. 1->offset size in index header
                    + 2 + 1
                    //offset array size * offset size
                    + (count + 1) * indexOffSize
                    //???zero <-> one base
                    - 1
                    // read object offset relative to object array base
                    + getOffset(indexOffSize);
        }
        return offsets;
    }

    protected void getDictItem() {
        resetArgs();
        key = null;

        while (!processKey()) {
            char b0 = getCard8();

            if (b0 == 29) {
                handleIntItem();
            } else if (b0 == 28) {
                handleShortItem();
            } else if (b0 >= 32 && b0 <= 246) {
                handleByteItem(b0);
            } else if (b0 >= 247 && b0 <= 250) {
                handlePositiveRangeItem(b0);
            } else if (b0 >= 251 && b0 <= 254) {
                handleNegativeRangeItem(b0);
            } else if (b0 == 30) {
                handleStringItem();
            }
        }
    }

    private void resetArgs() {
        for (int i = 0; i < argCount; i++) {
            args[i] = null;
        }
        argCount = 0;
    }

    private void handleIntItem() {
        int item = getInt();
        args[argCount++] = item;
    }

    private void handleShortItem() {
        short item = getShort();
        args[argCount++] = (int) item;
    }

    private void handleByteItem(char b0) {
        byte item = (byte) (b0 - 139);
        args[argCount++] = (int) item;
    }

    private void handlePositiveRangeItem(char b0) {
        char b1 = getCard8();
        short item = (short) ((b0 - 247) * 256 + b1 + 108);
        args[argCount++] = (int) item;
    }

    private void handleNegativeRangeItem(char b0) {
        char b1 = getCard8();
        short item = (short) (-(b0 - 251) * 256 - b1 - 108);
        args[argCount++] = (int) item;
    }

    private void handleStringItem() {
        StringBuilder item = new StringBuilder();
        boolean done = false;
        char buffer = 0;
        byte avail = 0;
        int nibble;

        while (!done) {
            nibble = getNextNibble(buffer, avail);
            avail = (byte) ((avail == 1) ? 0 : 1);
            buffer = (avail == 2) ? getCard8() : buffer;

            done = appendNibbleToString(item, nibble);
        }

        args[argCount++] = item.toString();
    }

    private int getNextNibble(char buffer, byte avail) {
        return (avail == 1) ? (buffer / 16) : (buffer % 16);
    }

    private boolean appendNibbleToString(StringBuilder item, int nibble) {
        switch (nibble) {
            case 0xa:
                item.append(".");
                break;
            case 0xb:
                item.append("E");
                break;
            case 0xc:
                item.append("E-");
                break;
            case 0xe:
                item.append("-");
                break;
            case 0xf:
                return true;
            default:
                if (nibble <= 9) {
                    item.append(nibble);
                } else {
                    item.append("<NIBBLE ERROR: ").append(nibble).append('>');
                    return true;
                }
        }
        return false;
    }

    private boolean processKey() {
        char b0 = getCard8();
        if (b0 <= 21) {
            key = (b0 != 12) ? operatorNames[b0] : operatorNames[32 + getCard8()];
            return true;
        }
        return false;
    }


    /**
     * a utility that creates a range item for an entire index
     *
     * @param indexOffset where the index is
     * @return a range item representing the entire index
     */

    protected RangeItem getEntireIndexRange(int indexOffset) {
        seek(indexOffset);
        int count = getCard16();
        if (count == 0) {
            return new RangeItem(buf, indexOffset, 2);
        } else {
            int indexOffSize = getCard8();
            seek(indexOffset + 2 + 1 + count * indexOffSize);
            int size = getOffset(indexOffSize) - 1;
            return new RangeItem(buf, indexOffset,
                    2 + 1 + (count + 1) * indexOffSize + size);
        }
    }

    public boolean exists(String fontName) {
        int j;
        for (j = 0; j < fonts.length; j++) {
            if (fontName.equals(fonts[j].name)) {
                return true;
            }
        }
        return false;
    }

    public String[] getNames() {
        String[] names = new String[fonts.length];
        for (int i = 0; i < fonts.length; i++) {
            names[i] = fonts[i].name;
        }
        return names;
    }

    /**
     * List items for the linked list that builds the new CID font.
     */
    protected abstract static class Item {

        protected int myOffset = -1;

        /**
         * remember the current offset and increment by item's size in bytes.
         *
         * @param currentOffset current offset
         */
        public void increment(int[] currentOffset) {
            myOffset = currentOffset[0];
        }

        /**
         * Emit the byte stream for this item.
         *
         * @param buffer byte array
         */
        public void emit(byte[] buffer) {
        }

        /**
         * Fix up cross references to this item (applies only to markers).
         */
        public void xref() {
        }
    }

    protected abstract static class OffsetItem extends Item {

        int value;

        /**
         * set the value of an offset item that was initially unknown. It will be fixed up latex by a call to xref on
         * some marker.
         *
         * @param offset offset to be set
         */
        public void set(int offset) {
            this.value = offset;
        }
    }

    /**
     * A range item.
     */

    protected static final class RangeItem extends Item {

        private final RandomAccessFileOrArray buf;
        int offset;
        int length;

        public RangeItem(RandomAccessFileOrArray buf, int offset, int length) {
            this.offset = offset;
            this.length = length;
            this.buf = buf;
        }

        @Override
        public void increment(int[] currentOffset) {
            super.increment(currentOffset);
            currentOffset[0] += length;
        }

        @Override
        public void emit(byte[] buffer) {
            try {
                buf.seek(offset);
                for (int i = myOffset; i < myOffset + length; i++) {
                    buffer[i] = buf.readByte();
                }
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
    }

    /**
     * An index-offset item for the list. The size denotes the required size in the CFF. A positive value means that we
     * need a specific size in bytes (for offset arrays) and a negative value means that this is a dict item that uses a
     * variable-size representation.
     */
    protected static final class IndexOffsetItem extends OffsetItem {

        public final int size;

        public IndexOffsetItem(int size, int value) {
            this.size = size;
            this.value = value;
        }

        public IndexOffsetItem(int size) {
            this.size = size;
        }
        
        @Override
        public void increment(int[] currentOffset) {
            super.increment(currentOffset);
            currentOffset[0] += size;
        }

        @Override
        public void emit(byte[] buffer) {
            int i = 0;
            switch (size) {
                case 4:
                    buffer[myOffset + i] = (byte) ((value >>> 24) & 0xff);
                    i++;
                    // fallthrough
                case 3:
                    buffer[myOffset + i] = (byte) ((value >>> 16) & 0xff);
                    i++;
                    // fallthrough
                case 2:
                    buffer[myOffset + i] = (byte) ((value >>> 8) & 0xff);
                    i++;
                    // fallthrough
                case 1:
                    buffer[myOffset + i] = (byte) ((value) & 0xff);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + size);
            }
        }
    }

    protected static final class IndexBaseItem extends Item {

        public IndexBaseItem() {
            //empty on purpose for now
        }
    }

    protected static final class IndexMarkerItem extends Item {

        private final OffsetItem offItem;
        private final IndexBaseItem indexBase;

        public IndexMarkerItem(OffsetItem offItem, IndexBaseItem indexBase) {
            this.offItem = offItem;
            this.indexBase = indexBase;
        }

        @Override
        public void xref() {
            offItem.set(this.myOffset - indexBase.myOffset + 1);
        }
    }

    /**
     * To change the template for this generated type comment go to Window - Preferences - Java - Code Generation -
     * Code and Comments
     */
    protected static final class SubrMarkerItem extends Item {

        private final OffsetItem offItem;
        private final IndexBaseItem indexBase;

        public SubrMarkerItem(OffsetItem offItem, IndexBaseItem indexBase) {
            this.offItem = offItem;
            this.indexBase = indexBase;
        }
        @Override
        public void xref() {
            offItem.set(this.myOffset - indexBase.myOffset);
        }
    }

    /**
     * an unknown offset in a dictionary for the list. We will fix up the offset later; for now, assume it's large.
     */
    protected static final class DictOffsetItem extends OffsetItem {

        public final int size;

        public DictOffsetItem() {
            this.size = 5;
        }
        @Override
        public void increment(int[] currentOffset) {
            super.increment(currentOffset);
            currentOffset[0] += size;
        }

        // this is incomplete!
        @Override
        public void emit(byte[] buffer) {
            if (size == 5) {
                buffer[myOffset] = 29;
                buffer[myOffset + 1] = (byte) ((value >>> 24) & 0xff);
                buffer[myOffset + 2] = (byte) ((value >>> 16) & 0xff);
                buffer[myOffset + 3] = (byte) ((value >>> 8) & 0xff);
                buffer[myOffset + 4] = (byte) ((value) & 0xff);
            }
        }
    }

    /**
     * Card24 item.
     */

    protected static final class UInt24Item extends Item {

        int value;

        public UInt24Item(int value) {
            this.value = value;
        }
        @Override
        public void increment(int[] currentOffset) {
            super.increment(currentOffset);
            currentOffset[0] += 3;
        }

        // this is incomplete!
        @Override
        public void emit(byte[] buffer) {
            buffer[myOffset] = (byte) ((value >>> 16) & 0xff);
            buffer[myOffset + 1] = (byte) ((value >>> 8) & 0xff);
            buffer[myOffset + 2] = (byte) ((value) & 0xff);
        }
    }

    /**
     * Card32 item.
     */

    protected static final class UInt32Item extends Item {

        int value;

        public UInt32Item(int value) {
            this.value = value;
        }
        @Override
        public void increment(int[] currentOffset) {
            super.increment(currentOffset);
            currentOffset[0] += 4;
        }

        // this is incomplete!
        @Override
        public void emit(byte[] buffer) {
            buffer[myOffset] = (byte) ((value >>> 24) & 0xff);
            buffer[myOffset + 1] = (byte) ((value >>> 16) & 0xff);
            buffer[myOffset + 2] = (byte) ((value >>> 8) & 0xff);
            buffer[myOffset + 3] = (byte) ((value) & 0xff);
        }
    }

    /**
     * A SID or Card16 item.
     */

    protected static final class UInt16Item extends Item {

        char value;

        public UInt16Item(char value) {
            this.value = value;
        }
        @Override
        public void increment(int[] currentOffset) {
            super.increment(currentOffset);
            currentOffset[0] += 2;
        }

        // this is incomplete!
        @Override
        public void emit(byte[] buffer) {
            buffer[myOffset] = (byte) ((value >>> 8) & 0xff);
            buffer[myOffset + 1] = (byte) ((value) & 0xff);
        }
    }

    /**
     * A Card8 item.
     */

    protected static final class UInt8Item extends Item {

        char value;

        public UInt8Item(char value) {
            this.value = value;
        }

        @Override
        public void increment(int[] currentOffset) {
            super.increment(currentOffset);
            currentOffset[0] += 1;
        }

        // this is incomplete!
        @Override
        public void emit(byte[] buffer) {
            buffer[myOffset] = (byte) ((value) & 0xff);
        }
    }

    protected static final class StringItem extends Item {

        String s;

        public StringItem(String s) {
            this.s = s;
        }

        @Override
        public void increment(int[] currentOffset) {
            super.increment(currentOffset);
            currentOffset[0] += s.length();
        }

        @Override
        public void emit(byte[] buffer) {
            for (int i = 0; i < s.length(); i++) {
                buffer[myOffset + i] = (byte) (s.charAt(i) & 0xff);
            }
        }
    }

    /**
     * A dictionary number on the list. This implementation is inefficient: it doesn't use the variable-length
     * representation.
     */
    protected static final class DictNumberItem extends Item {

        public final int value;

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        int size = 5;
        
        public DictNumberItem(int value) {
            this.value = value;
        }
        
        @Override
        public void increment(int[] currentOffset) {
            super.increment(currentOffset);
            currentOffset[0] += size;
        }

        // this is incomplete!
        @Override
        public void emit(byte[] buffer) {
            if (size == 5) {
                buffer[myOffset] = 29;
                buffer[myOffset + 1] = (byte) ((value >>> 24) & 0xff);
                buffer[myOffset + 2] = (byte) ((value >>> 16) & 0xff);
                buffer[myOffset + 3] = (byte) ((value >>> 8) & 0xff);
                buffer[myOffset + 4] = (byte) ((value) & 0xff);
            }
        }
    }

    /**
     * An offset-marker item for the list. It is used to mark an offset and to set the offset list item.
     */

    protected static final class MarkerItem extends Item {

        OffsetItem p;

        public MarkerItem(OffsetItem pointerToMarker) {
            p = pointerToMarker;
        }
        @Override
        public void xref() {
            p.set(this.myOffset);
        }
    }

    protected static final class Font {

        String name;
        String fullName;
        boolean isCID = false;
        int privateOffset = -1; // only if not CID
        int privateLength = -1; // only if not CID
        int privateSubrs = -1;
        int charstringsOffset = -1;
        int charsetOffset = -1;
        int fdarrayOffset = -1; // only if CID
        int fdselectOffset = -1; // only if CID
        int[] fdprivateOffsets;
        int[] fdprivateLengths;

        // Added by Oren & Ygal
        int nglyphs;
        int nstrings;
        int charsetLength;
        int[] charstringsOffsets;
        int[] charset;
        int[] fdselect;
        int fdselectlength;
        int fdselectformat;
        int charstringType = 2;
        int fdarraycount;
        int fdarrayoffsize;
        int[] fdarrayoffsets;
        int[] privateSubrsOffset;
        int[][] privateSubrsOffsetsArray;
        int[] subrsOffsets;

    }
}
