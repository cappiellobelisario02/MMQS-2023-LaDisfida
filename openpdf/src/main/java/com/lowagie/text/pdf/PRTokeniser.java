/*
 * $Id: PRTokeniser.java 4083 2009-10-30 21:25:10Z trumpetinc $
 *
 * Copyright 2001, 2002 by Paulo Soares.
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

import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.InvalidPdfException;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PRTokeniser implements AutoCloseable {

    static Logger logger = Logger.getLogger(PRTokeniser.class.getName());

    public static final int TK_NUMBER = 1;
    public static final int TK_STRING = 2;
    public static final int TK_NAME = 3;
    public static final int TK_COMMENT = 4;
    public static final int TK_START_ARRAY = 5;
    public static final int TK_END_ARRAY = 6;
    public static final int TK_START_DIC = 7;
    public static final int TK_END_DIC = 8;
    public static final int TK_REF = 9;
    public static final int TK_OTHER = 10;
    public static final int TK_ENDOFFILE = 11;
    protected static final boolean[] delims = {
            true, true, false, false, false, false, false, false, false, false,
            true, true, false, true, true, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, true, false, false, false, false, true, false,
            false, true, true, false, false, false, false, false, true, false,
            false, false, false, false, false, false, false, false, false, false,
            false, true, false, true, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, true, false, true, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false, false, false, false,
            false, false, false, false, false, false, false};

    static final String EMPTY = "";


    protected RandomAccessFileOrArray file;
    protected int type;
    protected String stringValue;
    protected int reference;
    protected int generation;
    protected boolean hexString;

    public PRTokeniser(String filename) throws IOException {
        file = new RandomAccessFileOrArray(filename);
    }

    public PRTokeniser(byte[] pdfIn) {
        file = new RandomAccessFileOrArray(pdfIn);
    }

    public PRTokeniser(RandomAccessFileOrArray file) {
        this.file = file;
    }

    public static final boolean isWhitespace(int ch) {
        return (ch == 0 || ch == 9 || ch == 10 || ch == 12 || ch == 13 || ch == 32);
    }

    public static final boolean isDelimiter(int ch) {
        return (ch == '(' || ch == ')' || ch == '<' || ch == '>' || ch == '[' || ch == ']' || ch == '/' || ch == '%');
    }

    public static final boolean isDelimiterWhitespace(int ch) {
        return delims[ch + 1];
    }

    public static int getHex(int v) {
        if (v >= '0' && v <= '9') {
            return v - '0';
        }
        if (v >= 'A' && v <= 'F') {
            return v - 'A' + 10;
        }
        if (v >= 'a' && v <= 'f') {
            return v - 'a' + 10;
        }
        return -1;
    }

    public static int[] checkObjectStart(byte[] line) {
        PRTokeniser tk = initializeTokeniser(line);
        if (tk == null) {
            return new int[0];
        }

        try {
            int num = 0;
            int gen = 0;
            if (!tk.nextToken() || tk.getTokenType() != TK_NUMBER) {
                return new int[0];
            }
            num = tk.intValue();
            if (!tk.nextToken() || tk.getTokenType() != TK_NUMBER) {
                return new int[0];
            }
            gen = tk.intValue();
            if (!tk.nextToken()) {
                return new int[0];
            }
            if (!tk.getStringValue().equals("obj")) {
                return new int[0];
            }
            return new int[]{num, gen};
        } catch (IOException ioe) {
            String stringToLog = "Exception raised in PRTokeniser";
            logger.severe(stringToLog);
            // empty on purpose
        }
        return new int[0];
    }

    private static PRTokeniser initializeTokeniser(byte[] line) {
        try {
            return new PRTokeniser(line);
        } catch (InstantiationError e) {
            logger.info("ERROR >> PRTokeniser");
            return null;
        }
    }
    
    public void seek(int pos) throws IOException {
        file.seek(pos);
    }

    public int getFilePointer() throws IOException {
        return file.getFilePointer();
    }

    public void close() throws IOException {
        file.close();
    }

    public int length() throws IOException {
        return file.length();
    }

    public int read() throws IOException {
        return file.read();
    }

    public RandomAccessFileOrArray getSafeFile() {
        return new RandomAccessFileOrArray(file);
    }

    public RandomAccessFileOrArray getFile() {
        return file;
    }

    public String readString(int size) throws IOException {
        StringBuilder buf = new StringBuilder();
        int ch;
        while ((size--) > 0) {
            ch = file.read();
            if (ch == -1) {
                break;
            }
            buf.append((char) ch);
        }
        return buf.toString();
    }

    public int getTokenType() {
        return type;
    }

    public String getStringValue() {
        return stringValue;
    }

    public int getReference() {
        return reference;
    }

    public int getGeneration() {
        return generation;
    }

    public void backOnePosition(int ch) {
        if (ch != -1) {
            file.pushBack((byte) ch);
        }
    }

    public void throwError(String error) throws IOException {
        throw new InvalidPdfException(MessageLocalization.getComposedMessage("1.at.file.pointer.2", error,
                String.valueOf(file.getFilePointer())));
    }

    public char checkPdfHeader() throws IOException {
        file.setStartOffset(0);
        String str = readString(1024);
        int idx = str.indexOf("%PDF-");
        if (idx < 0) {
            throw new InvalidPdfException(MessageLocalization.getComposedMessage("pdf.header.not.found"));
        }
        file.setStartOffset(idx);
        return str.charAt(idx + 7);
    }

    public void checkFdfHeader() throws IOException {
        file.setStartOffset(0);
        String str = readString(1024);
        int idx = str.indexOf("%FDF-1.2");
        if (idx < 0) {
            throw new InvalidPdfException(MessageLocalization.getComposedMessage("fdf.header.not.found"));
        }
        file.setStartOffset(idx);
    }

    public int getStartxref() throws IOException {
        int step = 1024; // packet size to read the file from the end
        int delta = 8; // delta to provide packets overlapping in case 'startxref' appears split between two packets
        int pos = file.length() - delta;
        int idx;
        do {
            pos = Math.max(0, pos - step);
            file.seek(pos);
            String str = readString(step + delta);
            idx = str.lastIndexOf("startxref");
        } while (pos > 0 && idx < 0);
        if (idx < 0) {
            throw new InvalidPdfException(MessageLocalization.getComposedMessage("pdf.startxref.not.found"));
        }
        return pos + idx;
    }

    public void nextValidToken() throws IOException {
        int level = 0;
        String n1 = null;
        String n2 = null;
        int ptr = 0;

        while (nextToken() || level == 2) {
            if (type == TK_COMMENT) {
                continue;
            }

            switch (level) {
                case 0:
                    level = processLevelZero();
                    break;
                case 1:
                    level = processLevelOne(ptr, n1);
                    break;
                default:
                    processDefaultLevel(ptr, n1, n2);
                    return;
            }
        }

        handleEndOfFile(level, ptr, n1);
    }

    private int processLevelZero() {
        if (type != TK_NUMBER) {
            return 0;
        }
        return 1;
    }

    private int processLevelOne(int ptr, String n1) throws IOException {
        if (type != TK_NUMBER) {
            file.seek(ptr);
            type = TK_NUMBER;
            stringValue = n1;
            return 0;
        }
        return 2;
    }

    private void processDefaultLevel(int ptr, String n1, String n2) throws IOException {
        if (type != TK_OTHER || !stringValue.equals("R")) {
            file.seek(ptr);
            type = TK_NUMBER;
            stringValue = n1;
            return;
        }
        type = TK_REF;
        reference = Integer.parseInt(n1);
        generation = Integer.parseInt(n2);
    }

    private void handleEndOfFile(int level, int ptr, String n1) throws IOException {
        if (level > 0) {
            type = TK_NUMBER;
            file.seek(ptr);
            stringValue = n1;
            return;
        }
        throwError("Unexpected end of file");
    }

    public boolean nextToken() throws IOException {
        int ch = skipWhitespace();
        if (ch == -1) {
            type = TK_ENDOFFILE;
            return false;
        }

        StringBuilder outBuf = null;
        stringValue = EMPTY;

        switch (ch) {
            case '[':
                type = TK_START_ARRAY;
                break;
            case ']':
                type = TK_END_ARRAY;
                break;
            case '/':
                outBuf = handleNameToken();
                break;
            case '>':
                handleEndDictionary();
                break;
            case '<':
                outBuf = handleStringOrDictionary();
                break;
            case '%':
                handleComment();
                break;
            case '(':
                outBuf = handleLiteralString();
                break;
            default:
                outBuf = handleNumberOrOther(ch);
                break;
        }

        if (outBuf != null) {
            stringValue = outBuf.toString();
        }
        return true;
    }

    private int skipWhitespace() throws IOException {
        int ch;
        do {
            ch = file.read();
        } while (ch != -1 && isWhitespace(ch));
        return ch;
    }

    private StringBuilder handleNameToken() throws IOException {
        StringBuilder outBuf = new StringBuilder();
        type = TK_NAME;
        int ch;
        while (true) {
            ch = file.read();
            if (delims[ch + 1]) {
                break;
            }
            if (ch == '#') {
                ch = (getHex(file.read()) << 4) + getHex(file.read());
            }
            outBuf.append((char) ch);
        }
        backOnePosition(ch);
        return outBuf;
    }

    private void handleEndDictionary() throws IOException {
        int ch = file.read();
        if (ch != '>') {
            throwError(MessageLocalization.getComposedMessage("greaterthan.not.expected"));
        }
        type = TK_END_DIC;
    }

    private StringBuilder handleStringOrDictionary() throws IOException {
        int v1 = readNonWhitespace();
        if (v1 == '<') {
            type = TK_START_DIC;
            return null;
        }

        StringBuilder outBuf = new StringBuilder();
        type = TK_STRING;
        hexString = true;

        while (v1 >= 0) {
            int v1Hex = processHex(v1);
            if (v1Hex < 0) break;

            int v2 = readNonWhitespace();
            if (v2 == '>') {
                outBuf.append((char) (v1Hex << 4));
                break;
            }

            int v2Hex = processHex(v2);
            if (v2Hex < 0) break;

            outBuf.append((char) ((v1Hex << 4) + v2Hex));
            v1 = file.read();
        }

        validateHexRead(v1);
        return outBuf;
    }

    private int readNonWhitespace() throws IOException {
        int ch;
        do {
            ch = file.read();
        } while (isWhitespace(ch));
        return ch;
    }

    private int processHex(int value) {
        return getHex(value);
    }

    private void validateHexRead(int v1) throws IOException {
        if (v1 < 0) {
            throwError(MessageLocalization.getComposedMessage("error.reading.string"));
        }
    }

    private void handleComment() throws IOException {
        type = TK_COMMENT;
        int ch;
        do {
            ch = file.read();
        } while (ch != -1 && ch != '\r' && ch != '\n');
    }

    private StringBuilder handleLiteralString() throws IOException {
        StringBuilder outBuf = new StringBuilder();
        type = TK_STRING;
        hexString = false;
        int nesting = 0;
        int ch;
        while (true) {
            ch = file.read();
            if (ch == -1) {
                break;
            }
            if (ch == '(') {
                nesting++;
            } else if (ch == ')') {
                nesting--;
            } else if (ch == '\\') {
                ch = handleEscapeSequence();
            } else if (ch == '\r') {
                ch = file.read();
                if (ch != '\n') {
                    backOnePosition(ch);
                    ch = '\n';
                }
            }
            if (nesting == -1) {
                break;
            }
            outBuf.append((char) ch);
        }
        if (ch == -1) {
            throwError(MessageLocalization.getComposedMessage("error.reading.string"));
        }
        return outBuf;
    }

    private int handleEscapeSequence() throws IOException {
        int ch = file.read();
        switch (ch) {
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case 't':
                return '\t';
            case 'b':
                return '\b';
            case 'f':
                return '\f';
            case '(', ')', '\\':
                return ch;
            case '\r', '\n':
                // Handle line break escape sequence
                return '\n';
            default:
                return handleOctalEscape(ch);
        }
    }

    private int handleOctalEscape(int ch) throws IOException {
        if (ch < '0' || ch > '7') {
            return ch;
        }
        int octal = ch - '0';
        ch = file.read();
        if (ch >= '0' && ch <= '7') {
            octal = (octal << 3) + ch - '0';
            ch = file.read();
            if (ch >= '0' && ch <= '7') {
                octal = (octal << 3) + ch - '0';
            } else {
                backOnePosition(ch);
            }
        } else {
            backOnePosition(ch);
        }
        return octal & 0xff;
    }

    private StringBuilder handleNumberOrOther(int ch) throws IOException {
        StringBuilder outBuf = new StringBuilder();
        if (ch == '-' || ch == '+' || ch == '.' || (ch >= '0' && ch <= '9')) {
            type = TK_NUMBER;
            do {
                outBuf.append((char) ch);
                ch = file.read();
            } while ((ch >= '0' && ch <= '9') || ch == '.');
        } else {
            type = TK_OTHER;
            do {
                outBuf.append((char) ch);
                ch = file.read();
            } while (!delims[ch + 1]);
        }
        backOnePosition(ch);
        return outBuf;
    }

    public int intValue() {
        return Integer.parseInt(stringValue);
    }

    public boolean readLineSegment(byte[] input) throws IOException {
        int ptr = 0;
        int len = input.length;

        // Skip initial whitespace
        skipInitialWhitespace();

        // Read until end of line or end of input
        while (ptr < len) {
            int c = read();
            if (isEndOfLine(c)) {
                break;
            }
            if (c == -1) {
                break;
            }
            input[ptr++] = (byte) c;
        }

        // Handle end of file if needed
        if (ptr >= len) {
            handleEndOfFile();
        }

        // Handle special cases for output
        return handleSpecialCases(ptr, len, input);
    }

    private void skipInitialWhitespace() throws IOException {
        int c;
        while ((c = read()) != -1 && isWhitespace(c)) {
            // skip whitespace
        }
        if (c != -1) {
            seek(getFilePointer() - 1); // Put back the last read character
        }
    }

    private boolean isEndOfLine(int c) {
        return c == -1 || c == '\n' || c == '\r';
    }

    private void handleEndOfFile() throws IOException {
        int c;
        while (!isEndOfLine(c = read())) {
            if (c == -1) {
                break;
            }
        }
        if (c == '\r' && read() != '\n') {
            seek(getFilePointer() - 1);
        }
    }

    private boolean handleSpecialCases(int ptr, int len, byte[] input) {
        if (ptr >= len) {
            return ptr == 0;
        }
        if (ptr + 2 <= len) {
            input[ptr++] = (byte) ' ';
            input[ptr] = (byte) 'X';
        }
        return false;
    }


    public boolean isHexString() {
        return this.hexString;
    }

} 
