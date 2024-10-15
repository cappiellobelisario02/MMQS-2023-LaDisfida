/**
 * Copyright (c) 2005-2006, www.fontbox.org All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <p>
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer. 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 * the following disclaimer in the documentation and/or other materials provided with the distribution. 3. Neither the
 * name of fontbox; nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * <p>
 * http://www.fontbox.org
 */
package com.lowagie.text.pdf.fonts.cmaps;

import com.lowagie.text.error_messages.MessageLocalization;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * This will parser a CMap stream.
 *
 * @author <a href="mailto:ben@benlitchfield.com">Ben Litchfield</a>
 * @version $Revision: 4065 $
 * @since 2.1.4
 */
public class CMapParser {

    private static final String BEGIN_CODESPACE_RANGE = "begincodespacerange";
    private static final String BEGIN_BASE_FONT_CHAR = "beginbfchar";
    private static final String BEGIN_BASE_FONT_RANGE = "beginbfrange";

    private static final String MARK_END_OF_DICTIONARY = ">>";
    private static final String MARK_END_OF_ARRAY = "]";

    private byte[] tokenParserByteBuffer;
    static Logger logger = Logger.getLogger(CMapParser.class.getName());

    /**
     * Creates a new instance of CMapParser.
     */
    public CMapParser() {
        throw new UnsupportedOperationException("This constructor is not supported or implemented.");
    }

    // Helper method to validate the file path
    private static File validatePath(String path) {
        File file = new File(path);

        // Perform security checks: prevent directory traversal attacks
        try {
            String canonicalPath = file.getCanonicalPath();
            String currentDir = new File(".").getCanonicalPath();
            if (!canonicalPath.startsWith(currentDir)) {
                throw new SecurityException("Path manipulation attempt detected: " + path);
            }
        } catch (IOException e) {
            throw new SecurityException("Invalid file path: " + path, e);
        }

        return file;
    }


    /**
     * This will parse the stream and create a cmap object.
     *
     * @param input The CMAP stream to parse.
     * @return The parsed stream as a java object.
     * @throws IOException If there is an error parsing the stream.
     */
    public CMap parse(InputStream input) throws IOException {
        PushbackInputStream cmapStream = new PushbackInputStream(input);
        CMap result = new CMap();
        Object previousToken = null;
        Object token;

        while ((token = parseNextToken(cmapStream)) != null) {
            if (token instanceof com.lowagie.text.pdf.fonts.cmaps.CMapParser.Operator operator) {
                handleOperator(operator, previousToken, cmapStream, result);
            }
            previousToken = token;
        }
        return result;
    }

    private void handleOperator(com.lowagie.text.pdf.fonts.cmaps.CMapParser.Operator op, Object previousToken, PushbackInputStream cmapStream, CMap result) throws IOException {
        switch (op.op) {
            case BEGIN_CODESPACE_RANGE:
                processCodespaceRange(previousToken, cmapStream, result);
                break;
            case BEGIN_BASE_FONT_CHAR:
                processBaseFontChar(previousToken, cmapStream, result);
                break;
            case BEGIN_BASE_FONT_RANGE:
                processBaseFontRange(previousToken, cmapStream, result);
                break;
            default:
                break;
        }
    }

    private void processCodespaceRange(Object previousToken, PushbackInputStream cmapStream, CMap result) throws IOException {
        Number cosCount = (Number) previousToken;
        for (int j = 0; j < cosCount.intValue(); j++) {
            byte[] startRange = (byte[]) parseNextToken(cmapStream);
            byte[] endRange = (byte[]) parseNextToken(cmapStream);
            CodespaceRange range = new CodespaceRange();
            range.setStart(startRange);
            range.setEnd(endRange);
            result.addCodespaceRange(range);
        }
    }

    private void processBaseFontChar(Object previousToken, PushbackInputStream cmapStream, CMap result) throws IOException {
        Number cosCount = (Number) previousToken;
        for (int j = 0; j < cosCount.intValue(); j++) {
            byte[] inputCode = (byte[]) parseNextToken(cmapStream);
            Object nextToken = parseNextToken(cmapStream);
            addMappingForBaseFontChar(result, inputCode, nextToken);
        }
    }

    private void addMappingForBaseFontChar(CMap result, byte[] inputCode, Object nextToken) throws IOException {
        if (nextToken instanceof byte[] bytesArr) {
            String value = createStringFromBytes(bytesArr);
            result.addMapping(inputCode, value);
        } else if (nextToken instanceof com.lowagie.text.pdf.fonts.cmaps.CMapParser.LiteralName literalName) {
            result.addMapping(inputCode, literalName.name);
        } else {
            throw new IOException(MessageLocalization.getComposedMessage(
                    "error.parsing.cmap.beginbfchar.expected.cosstring.or.cosname.and.not.1",
                    nextToken));
        }
    }

    private void processBaseFontRange(Object previousToken, PushbackInputStream cmapStream, CMap result) throws IOException {
        Number cosCount = (Number) previousToken;
        for (int j = 0; j < cosCount.intValue(); j++) {
            byte[] startCode = (byte[]) parseNextToken(cmapStream);
            byte[] endCode = (byte[]) parseNextToken(cmapStream);
            Object nextToken = parseNextToken(cmapStream);
            processRange(result, startCode, endCode, nextToken);
        }
    }

    private void processRange(CMap result, byte[] startCode, byte[] endCode, Object nextToken) throws IOException {
        List<Object> array = null;
        byte[] tokenBytes;

        // Check if nextToken is a List<byte[]>
        if (nextToken instanceof List<?> listToken && !listToken.isEmpty() && listToken.get(0) instanceof byte[]) {
            // Convert List<byte[]> to List<Object>
            array = new ArrayList<>(listToken);
            tokenBytes = (byte[]) array.get(0); // Get the first byte[] from the list
        } else {
            assert nextToken instanceof byte[];
            tokenBytes = (byte[]) nextToken; // Otherwise, treat nextToken as a byte[]
        }

        // Call the method with the processed data
        processCodeRange(result, startCode, endCode, array, tokenBytes);
    }



    private void processCodeRange(CMap result, byte[] startCode, byte[] endCode, List<Object> array, byte[] tokenBytes) throws IOException {
        String value;
        int arrayIndex = 0;
        boolean done = false;

        while (!done) {
            if (compare(startCode, endCode) >= 0) {
                done = true;
            }
            value = createStringFromBytes(tokenBytes);
            result.addMapping(startCode, value);
            increment(startCode);

            if (array == null) {
                increment(tokenBytes);
            } else {
                arrayIndex++;
                if (arrayIndex < array.size()) {
                    tokenBytes = (byte[]) array.get(arrayIndex);
                }
            }
        }
    }

    private Object parseNextToken(PushbackInputStream is) throws IOException {
        int nextByte = readNextNonWhitespaceByte(is);
        return switch (nextByte) {
            case '%' -> parseComment(is);
            case '(' -> parseString(is);
            case '>' -> parseEndOfDictionary(is);
            case ']' -> MARK_END_OF_ARRAY;
            case '[' -> parseArray(is);
            case '<' -> parseDictionaryOrHex(is);
            case '/' -> parseLiteralName(is);
            case -1 -> null;
            default -> parseNumberOrOperator(is, nextByte);
        };
    }

    private int readNextNonWhitespaceByte(PushbackInputStream is) throws IOException {
        int nextByte = is.read();
        while (nextByte == 0x09 || nextByte == 0x20 || nextByte == 0x0D || nextByte == 0x0A) {
            nextByte = is.read();
        }
        return nextByte;
    }

    private String parseComment(PushbackInputStream is) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append((char) is.read());
        readUntilEndOfLine(is, buffer);
        return buffer.toString();
    }

    private String parseString(PushbackInputStream is) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int stringByte = is.read();
        while (stringByte != -1 && stringByte != ')') {
            buffer.append((char) stringByte);
            stringByte = is.read();
        }
        return buffer.toString();
    }

    private Object parseEndOfDictionary(PushbackInputStream is) throws IOException {
        int secondCloseBrace = is.read();
        if (secondCloseBrace == '>') {
            return MARK_END_OF_DICTIONARY;
        } else {
            throw new IOException(MessageLocalization.getComposedMessage("error.expected.the.end.of.a.dictionary"));
        }
    }

    private List<Object> parseArray(PushbackInputStream is) throws IOException {
        List<Object> list = new ArrayList<>();
        Object nextToken = parseNextToken(is);
        while (nextToken != MARK_END_OF_ARRAY) {
            list.add(nextToken);
            nextToken = parseNextToken(is);
        }
        return list;
    }

    private Object parseDictionaryOrHex(PushbackInputStream is) throws IOException {
        int theNextByte = is.read();
        if (theNextByte == '<') {
            return parseDictionary(is);
        } else {
            return parseHex(is, theNextByte);
        }
    }

    private Map<String, Object> parseDictionary(PushbackInputStream is) throws IOException {
        Map<String, Object> result = new HashMap<>();
        Object key = parseNextToken(is);
        while (key instanceof com.lowagie.text.pdf.fonts.cmaps.CMapParser.LiteralName literalName) {
            Object value = parseNextToken(is);
            result.put(literalName.name, value);
            key = parseNextToken(is);
        }
        return result;
    }

    private byte[] parseHex(PushbackInputStream is, int firstByte) throws IOException {
        int multiplier = 16;
        int bufferIndex = -1;
        while (firstByte != -1 && firstByte != '>') {
            int intValue = getHexValue(firstByte);
            if (multiplier == 16) {
                bufferIndex++;
                tokenParserByteBuffer[bufferIndex] = 0;
                multiplier = 1;
            } else {
                multiplier = 16;
            }
            tokenParserByteBuffer[bufferIndex] += (byte) intValue;
            firstByte = is.read();
        }
        byte[] finalResult = new byte[bufferIndex + 1];
        System.arraycopy(tokenParserByteBuffer, 0, finalResult, 0, bufferIndex + 1);
        return finalResult;
    }

    private int getHexValue(int hexChar) throws IOException {
        if (hexChar >= '0' && hexChar <= '9') {
            return hexChar - '0';
        } else if (hexChar >= 'A' && hexChar <= 'F') {
            return 10 + hexChar - 'A';
        } else if (hexChar >= 'a' && hexChar <= 'f') {
            return 10 + hexChar - 'a';
        } else {
            throw new IOException(MessageLocalization.getComposedMessage(
                    "error.expected.hex.character.and.not.char.thenextbyte.1", hexChar));
        }
    }

    private Object parseLiteralName(PushbackInputStream is) throws IOException {
        StringBuilder buffer = new StringBuilder();
        int stringByte = is.read();
        while (isWhitespaceOrEOF(stringByte)) {
            buffer.append((char) stringByte);
            stringByte = is.read();
        }
        return new com.lowagie.text.pdf.fonts.cmaps.CMapParser.LiteralName(buffer.toString());
    }

    private Object parseNumberOrOperator(PushbackInputStream is, int firstByte) throws IOException {
        StringBuilder buffer = new StringBuilder();
        buffer.append((char) firstByte);
        int nextByte = is.read();
        while (isWhitespaceOrEOF(nextByte)) {
            buffer.append((char) nextByte);
            nextByte = is.read();
        }
        is.unread(nextByte);
        String value = buffer.toString();
        if (value.indexOf('.') >= 0) {
            return Double.valueOf(value);
        } else {
            return Integer.valueOf(value);
        }
    }

    private void readUntilEndOfLine(InputStream is, StringBuilder buf) throws IOException {
        int nextByte = is.read();
        while (nextByte != -1 && nextByte != 0x0D && nextByte != 0x0A) {
            buf.append((char) nextByte);
            nextByte = is.read();
        }
    }

    private boolean isWhitespaceOrEOF(int aByte) {
        return aByte != -1 && aByte != 0x20 && aByte != 0x0D && aByte != 0x0A;
    }

    private void increment(byte[] data) {
        increment(data, data.length - 1);
    }

    private void increment(byte[] data, int position) {
        if (position > 0 && (data[position] + 256) % 256 == 255) {
            data[position] = 0;
            increment(data, position - 1);
        } else {
            data[position] = (byte) (data[position] + 1);
        }
    }

    private String createStringFromBytes(byte[] bytes) {
        String retval;
        if (bytes.length == 1) {
            retval = new String(bytes);
        } else {
            retval = new String(bytes, StandardCharsets.UTF_16BE);
        }
        return retval;
    }

    private int compare(byte[] first, byte[] second) {
        int retval = 1;
        boolean done = false;
        for (int i = 0; i < first.length && !done; i++) {
            if (((first[i] + 256) % 256) < ((second[i] + 256) % 256)) {
                done = true;
                retval = -1;
            } else {
                done = true;
            }
        }
        return retval;
    }

    /**
     * Internal class.
     */
    private class LiteralName {

        private String name;

        private LiteralName(String theName) {
            name = theName;
        }
    }

    /**
     * Internal class.
     */
    private static class Operator {

        private String op;

    }
}
