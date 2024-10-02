/*
 * Copyright 2003 Paulo Soares
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
 */
package com.lowagie.text.xml.simpleparser;

import com.lowagie.text.error_messages.MessageLocalization;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A simple XML and HTML parser.  This parser is, like the SAX parser, an event based parser, but with much less
 * functionality.
 * <p>
 * The parser can:
 * </p>
 * <ul>
 * <li>It recognizes the encoding used
 * <li>It recognizes all the elements' start tags and end tags
 * <li>It lists attributes, where attribute values can be enclosed in single or double quotes
 * <li>It recognizes the <code>&lt;[CDATA[ ... ]]&gt;</code> construct
 * <li>It recognizes the standard entities: &amp;amp;, &amp;lt;, &amp;gt;, &amp;quot;, and &amp;apos;, as well as numeric entities
 * <li>It maps lines ending in <code>\r\n</code> and <code>\r</code> to <code>\n</code> on input, in accordance with the XML Specification, Section 2.11
 * </ul>
 */

public final class SimpleXMLParser {
    /**
     * possible states
     */
    private static final int UNKNOWN = 0;
    private static final int TEXT = 1;
    private static final int TAG_ENCOUNTERED = 2;
    private static final int EXAMIN_TAG = 3;
    private static final int TAG_EXAMINED = 4;
    private static final int IN_CLOSETAG = 5;
    private static final int SINGLE_TAG = 6;
    private static final int CDATA = 7;
    private static final int COMMENT = 8;
    private static final int PI = 9;
    private static final int ENTITY = 10;
    private static final int QUOTE = 11;
    private static final int ATTRIBUTE_KEY = 12;
    private static final int ATTRIBUTE_EQUAL = 13;
    private static final int ATTRIBUTE_VALUE = 14;

    /**
     * the state stack
     */
    Deque<Integer> stack;
    /**
     * The current character.
     */
    int character = 0;
    /**
     * The previous character.
     */
    int previousCharacter = -1;
    /**
     * the line we are currently reading
     */
    int lines = 1;
    /**
     * the column where the current character occurs
     */
    int columns = 0;
    /**
     * was the last character equivalent to a newline?
     */
    boolean eol = false;
    /**
     * A boolean indicating if the next character should be taken into account if it's a space character. When nospace
     * is false, the previous character wasn't whitespace.
     *
     * @since 2.1.5
     */
    boolean nowhite = false;
    /**
     * the current state
     */
    int state;
    /**
     * Are we parsing HTML?
     */
    boolean html;
    /**
     * current text (whatever is encountered between tags)
     */
    StringBuilder textSB = new StringBuilder();
    /**
     * current entity (whatever is encountered between & and ;)
     */
    StringBuilder entitySB = new StringBuilder();
    /**
     * current tagname
     */
    String tag = null;
    /**
     * current attributes
     */
    Map<String, String> attributes = null;
    /**
     * The handler to which we are going to forward document content
     */
    SimpleXMLDocHandler doc;
    /**
     * The handler to which we are going to forward comments.
     */
    SimpleXMLDocHandlerComment comment1;
    /**
     * Keeps track of the number of tags that are open.
     */
    int nested = 0;
    /**
     * the quote character that was used to open the quote.
     */
    int quoteCharacter = '"';
    /**
     * the attribute key.
     */
    String attributekey = null;
    /**
     * the attribute value.
     */
    String attributevalue = null;

    /**
     * Creates a Simple XML parser object. Call go(BufferedReader) immediately after creation.
     */
    private SimpleXMLParser(SimpleXMLDocHandler doc, SimpleXMLDocHandlerComment comment, boolean html) {
        this.doc = doc;
        this.comment1 = comment;
        this.html = html;
        stack = new ArrayDeque<>();
        state = html ? TEXT : UNKNOWN;
    }

    /**
     * Parses the XML document firing the events to the handler.
     *
     * @param doc     the document handler
     * @param comment {@link com.lowagie.text.xml.simpleparser.SimpleXMLParser#comment1}
     * @param r       the document. The encoding is already resolved. The reader is not closed
     * @param html    {@link com.lowagie.text.xml.simpleparser.SimpleXMLParser#html}
     * @throws IOException on error
     */
    public static void parse(SimpleXMLDocHandler doc, SimpleXMLDocHandlerComment comment, Reader r, boolean html)
            throws IOException {
        com.lowagie.text.xml.simpleparser.SimpleXMLParser parser = new com.lowagie.text.xml.simpleparser.SimpleXMLParser(doc, comment, html);
        parser.go(r);
    }

    /**
     * Detect charset from BOM, as per <a href="https://unicode.org/faq/utf_bom.html#bom4">Unicode FAQ</a>.
     */
    private static Optional<Charset> detectCharsetFromBOM(byte[] bom) {
        if (isUTF32BE(bom)) {
            return Optional.of(Charset.forName("UTF-32BE"));
        }
        if (isUTF8(bom)) {
            return Optional.of(StandardCharsets.UTF_8);
        }
        if (isUTF16BE(bom)) {
            return Optional.of(StandardCharsets.UTF_16BE);
        }
        if (isUTF32LE(bom)) {
            return Optional.of(Charset.forName("UTF-32LE"));
        }
        if (isUTF16LE(bom)) {
            return Optional.of(StandardCharsets.UTF_16LE);
        }
        return Optional.empty();
    }

    private static boolean isUTF32BE(byte[] bom) {
        return bom.length >= 4 && bom[0] == (byte) 0x00 && bom[1] == (byte) 0x00 && bom[2] == (byte) 0xFE && bom[3] == (byte) 0xFF;
    }

    private static boolean isUTF8(byte[] bom) {
        return bom.length >= 3 && bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF;
    }

    private static boolean isUTF16BE(byte[] bom) {
        return bom.length >= 2 && bom[0] == (byte) 0xFE && bom[1] == (byte) 0xFF;
    }

    private static boolean isUTF32LE(byte[] bom) {
        return bom.length >= 4 && bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE && bom[2] == (byte) 0x00 && bom[3] == (byte) 0x00;
    }

    private static boolean isUTF16LE(byte[] bom) {
        return bom.length >= 2 && bom[0] == (byte) 0xFF && bom[1] == (byte) 0xFE;
    }


    /**
     * Parses the XML document firing the events to the handler.
     *
     * @param doc the document handler
     * @param in  the document. The encoding is deduced from the stream. The stream is not closed
     * @throws IOException on error
     */
    public static void parse(SimpleXMLDocHandler doc, InputStream in) throws IOException {
        byte[] b4 = new byte[4];
        int count = in.read(b4);
        if (count != 4) {
            throw new IOException(MessageLocalization.getComposedMessage("insufficient.length"));
        }
        Charset encoding = detectCharsetFromBOM(b4).orElse(null);
        if (encoding == null) {
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = in.read()) != -1) {
                if (c == '>') {
                    break;
                }
                sb.append((char) c);
            }
            String decl = getDeclaredEncoding(sb.toString());
            if (decl == null) {
                encoding = StandardCharsets.UTF_8;
            } else {
                try {
                    encoding = Charset.forName(decl);
                } catch (UnsupportedCharsetException e) {
                    encoding = Charset.forName(IanaEncodings.getJavaEncoding(decl));
                }
            }
        }
        parse(doc, new InputStreamReader(in, encoding));
    }

    private static String getDeclaredEncoding(String decl) {
        if (decl == null) {
            return null;
        }
        int idx = decl.indexOf("encoding");
        if (idx < 0) {
            return null;
        }
        int idx1 = decl.indexOf('"', idx);
        int idx2 = decl.indexOf('\'', idx);
        if (idx1 == idx2) {
            return null;
        }
        if ((idx1 < 0 && idx2 > 0) || (idx2 > 0 && idx2 < idx1)) {
            int idx3 = decl.indexOf('\'', idx2 + 1);
            if (idx3 < 0) {
                return null;
            }
            return decl.substring(idx2 + 1, idx3);
        }
        if ((idx2 < 0 && idx1 > 0) || (idx1 > 0 && idx1 < idx2)) {
            int idx3 = decl.indexOf('"', idx1 + 1);
            if (idx3 < 0) {
                return null;
            }
            return decl.substring(idx1 + 1, idx3);
        }
        return null;
    }

    public static void parse(SimpleXMLDocHandler doc, Reader r) throws IOException {
        parse(doc, null, r, false);
    }

    /**
     * Does the actual parsing. Perform this immediately after creating the parser object.
     */
    private void go(Reader r) throws IOException {
        BufferedReader reader = getBufferedReader(r);
        doc.startDocument();
        processDocument(reader);
    }

    private BufferedReader getBufferedReader(Reader r) {
        return (r instanceof BufferedReader bufferedReader) ? bufferedReader : new BufferedReader(r);
    }

    private void processDocument(BufferedReader reader) throws IOException {
        while (true) {
            int mCharacter = getNextCharacter(reader);
            if (isEndOfFile(mCharacter)) {
                handleEndOfFile();
                return;
            }
            updateLineAndColumn(mCharacter);
            processCharacter(mCharacter);
        }
    }

    private int getNextCharacter(BufferedReader reader) throws IOException {
        if (previousCharacter == -1) {
            return reader.read();
        } else {
            int mCharacter = previousCharacter;
            previousCharacter = -1;
            return mCharacter;
        }
    }

    private boolean isEndOfFile(int character) {
        return character == -1;
    }

    private void handleEndOfFile() throws IOException {
        if (html) {
            if (state == TEXT) flush();
            doc.endDocument();
        } else {
            throwException("missing.end.tag");
        }
    }

    private void updateLineAndColumn(int character) {
        if (character == '\n' && eol) {
            eol = false;
        } else if (eol) {
            eol = false;
        } else if (character == '\n' || character == '\r') {
            handleNewLine(character);
        } else {
            columns++;
        }
    }

    private void handleNewLine(int character) {
        eol = (character == '\r');
        lines++;
        columns = 0;
    }

    private void processCharacter(int character) throws IOException {
        switch (state) {
            case UNKNOWN -> handleUnknown(character);
            case TEXT -> handleText(character);
            case TAG_ENCOUNTERED -> handleTagEncountered(character);
            case EXAMIN_TAG -> handleExamineTag(character);
            case TAG_EXAMINED -> handleTagExamined(character);
            case IN_CLOSETAG -> handleCloseTag(character);
            case SINGLE_TAG -> handleSingleTag(character);
            case CDATA -> handleCdata(character);
            case COMMENT -> handleComment(character);
            case PI -> handlePI(character);
            case ENTITY -> handleEntity(character);
            case QUOTE -> handleQuote(character);
            case ATTRIBUTE_KEY -> handleAttributeKey(character);
            case ATTRIBUTE_EQUAL -> handleAttributeEqual(character);
            case ATTRIBUTE_VALUE -> handleAttributeValue(character);
            default -> throw new IllegalStateException("Unexpected state: " + state);
        }
    }

    private void handleUnknown(int character) {
        if (character == '<') {
            saveState(TEXT);
            state = TAG_ENCOUNTERED;
        }
    }

    private void handleText(int character) {
        if (character == '<') {
            flush();
            saveState(state);
            state = TAG_ENCOUNTERED;
        } else if (character == '&') {
            saveState(state);
            entitySB.setLength(0);
            state = ENTITY;
            nowhite = true;
        } else if (Character.isWhitespace((char) character)) {
            if (nowhite) textSB.append((char) character);
            nowhite = false;
        } else {
            textSB.append((char) character);
            nowhite = true;
        }
    }

    private void handleTagEncountered(int character) {
        initTag();
        if (character == '/') {
            state = IN_CLOSETAG;
        } else if (character == '?') {
            restoreState();
            state = PI;
        } else {
            textSB.append((char) character);
            state = EXAMIN_TAG;
        }
    }

    private void handleExamineTag(int character) {
        if (character == '>') {
            processTagEnd();
        } else if (character == '/') {
            state = SINGLE_TAG;
        } else if (character == '-' && textSB.toString().equals("!-")) {
            flush();
            state = COMMENT;
        } else if (character == '[' && textSB.toString().equals("![CDATA")) {
            flush();
            state = CDATA;
        } else if (character == 'E' && textSB.toString().equals("!DOCTYP")) {
            flush();
            state = PI;
        } else if (Character.isWhitespace((char) character)) {
            doTag();
            state = TAG_EXAMINED;
        } else {
            textSB.append((char) character);
        }
    }

    private void handleTagExamined(int character) {
        if (character == '>') {
            processTag(true);
            initTag();
            state = restoreState();
        } else if (character == '/') {
            state = SINGLE_TAG;
        }
    }

    private void handleCloseTag(int character) {
        if (character == '>') {
            processTag(false);
            if (!html && nested == 0) return;
            state = restoreState();
        } else if (!Character.isWhitespace((char) character)) {
            textSB.append((char) character);
        }
    }

    private void handleSingleTag(int character) throws IOException {
        if (character != '>') {
            throwException("expected.gt.for.tag.lt.1.gt" + tag);
        }
        processTag(true);
        processTag(false);
        initTag();
        if (!html && nested == 0) {
            doc.endDocument();
        } else {
            state = restoreState();
        }
    }

    private void handleCdata(int character) {
        if (character == '>' && textSB.toString().endsWith("]]")) {
            textSB.setLength(textSB.length() - 2);
            flush();
            state = restoreState();
        } else {
            textSB.append((char) character);
        }
    }

    private void handleComment(int character) {
        if (character == '>' && textSB.toString().endsWith("--")) {
            textSB.setLength(textSB.length() - 2);
            flush();
            state = restoreState();
        } else {
            textSB.append((char) character);
        }
    }

    private void handlePI(int character) {
        if (character == '>') state = restoreState();
    }

    private void handleEntity(int character) {
        if (character == ';') {
            processEntity();
        } else if (!isValidEntityCharacter(character)) {
            handleInvalidEntity(character);
        } else {
            entitySB.append((char) character);
        }
    }

    private void processEntity() {
        state = restoreState();
        String entity = entitySB.toString();
        entitySB.setLength(0);
        char decodedEntity = EntitiesToUnicode.decodeEntity(entity);
        if (decodedEntity == '\0') {
            textSB.append('&').append(entity).append(';');
        } else {
            textSB.append(decodedEntity);
        }
    }

    private boolean isValidEntityCharacter(int character) {
        return (character == '#' || isAlphanumeric(character)) && entitySB.length() < 7;
    }

    private void handleInvalidEntity(int character) {
        state = restoreState();
        previousCharacter = character;
        textSB.append('&').append(entitySB.toString());
        entitySB.setLength(0);
    }

    private boolean isAlphanumeric(int character) {
        return (character >= '0' && character <= '9') ||
                (character >= 'a' && character <= 'z') ||
                (character >= 'A' && character <= 'Z');
    }

    private void handleQuote(int character) {
        if (quoteCharacter == ' ' && character == '>') {
            flush();
            processTag(true);
            initTag();
            state = restoreState();
        } else if (quoteCharacter == ' ' && Character.isWhitespace((char) character)) {
            flush();
            state = TAG_EXAMINED;
        } else if (quoteCharacter == ' ') {
            textSB.append((char) character);
        } else {
            textSB.append(translateSpecialWhitespace(character));
        }
    }

    private char translateSpecialWhitespace(int character) {
        return " \r\n\t".indexOf(character) >= 0 ? ' ' : (char) character;
    }

    private void handleAttributeKey(int character) {
        if (Character.isWhitespace((char) character)) {
            flush();
            state = ATTRIBUTE_EQUAL;
        } else if (character == '=') {
            flush();
            state = ATTRIBUTE_VALUE;
        } else {
            textSB.append((char) character);
        }
    }

    private void handleAttributeEqual(int character) {
        if (character == '=') {
            state = ATTRIBUTE_VALUE;
        }
    }

    private void handleAttributeValue(int character) {
        if (character == '"' || character == '\'') {
            quoteCharacter = character;
            state = QUOTE;
        } else {
            textSB.append((char) character);
            quoteCharacter = ' ';
            state = QUOTE;
        }
    }

    private void processTagEnd() {
        doTag();
        processTag(true);
        initTag();
        state = restoreState();
    }


    /**
     * Gets a state from the stack
     *
     * @return the previous state
     */
    private int restoreState() {
        if (!stack.isEmpty()) {
            return stack.pop();
        } else {
            return UNKNOWN;
        }
    }

    /**
     * Adds a state to the stack.
     *
     * @param s a state to add to the stack
     */
    private void saveState(int s) {
        stack.push(s);
    }

    /**
     * Flushes the text that is currently in the buffer. The text can be ignored, added to the document as content or as
     * comment,... depending on the current state.
     */
    private void flush() {
        switch (state) {
            case TEXT,
                 CDATA:
                if (!textSB.isEmpty()) {
                    doc.text(textSB.toString());
                }
                break;
            case COMMENT:
                if (comment1 != null) {
                    comment1.comment(textSB.toString());
                }
                break;
            case ATTRIBUTE_KEY:
                attributekey = textSB.toString();
                if (html) {
                    attributekey = attributekey.toLowerCase();
                }
                break;
            case QUOTE,
                 ATTRIBUTE_VALUE:
                attributevalue = textSB.toString();
                attributes.put(attributekey, attributevalue);
                break;
            default:
                // do nothing
        }
        textSB.setLength(0);
    }

    /**
     * Initialized the tag name and attributes.
     */
    private void initTag() {
        tag = null;
        attributes = new HashMap<>();
    }

    /**
     * Sets the name of the tag.
     */
    private void doTag() {
        if (tag == null) {
            tag = textSB.toString();
        }
        if (html) {
            tag = tag.toLowerCase();
        }
        textSB.setLength(0);
    }

    /**
     * processes the tag.
     *
     * @param start if true we are dealing with a tag that has just been opened; if false we are closing a tag.
     */
    private void processTag(boolean start) {
        if (start) {
            nested++;
            doc.startElement(tag, attributes);
        } else {
            nested--;
            doc.endElement(tag);
        }
    }

    /**
     * Throws an exception
     */
    private void throwException(String s) throws IOException {
        throw new IOException(MessageLocalization.getComposedMessage("1.near.line.2.column.3", s, String.valueOf(lines),
                String.valueOf(columns)));
    }

}
