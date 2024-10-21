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

import java.util.LinkedList;
import java.util.List;

/**
 * This class expands a string into a list of numbers. The main use is to select a range of pages.
 * <p>
 * The general syntax is:<br> [!][o][odd][e][even]start-end
 * <p>
 * You can have multiple ranges separated by commas ','. The '!' modifier removes the range from what is already
 * selected. The range changes are incremental, that is, numbers are added or deleted as the range appears. The start or
 * the end, but not both, can be omitted.
 */
public class SequenceList {

    protected static final int COMMA = 1;
    protected static final int MINUS = 2;
    protected static final int NOT = 3;
    protected static final int TEXT_CONST = 4;
    protected static final int NUMBER_CONST = 5;
    protected static final int END = 6;
    protected static final char EOT = '\uffff';

    private static final int FIRST = 0;
    private static final int DIGIT = 1;
    private static final int OTHER_CONST = 2;
    private static final int DIGIT2 = 3;
    private static final String NOT_OTHER = "-,!0123456789";

    protected char[] text;
    protected int ptr;
    protected int number;
    protected String other;

    protected int low;
    protected int high;
    protected boolean odd;
    protected boolean even;
    protected boolean inverse;

    protected SequenceList(String range) {
        ptr = 0;
        text = range.toCharArray();
    }

    /**
     * Generates a list of numbers from a string.
     *
     * @param ranges    the comma separated ranges
     * @param maxNumber the maximum number in the range
     * @return a list with the numbers as <CODE>Integer</CODE>
     */
    public static List<Integer> expand(String ranges, int maxNumber) {
        SequenceList parse = new SequenceList(ranges);
        List<Integer> list = new LinkedList<>();

        while (parse.getAttributes()) {
            adjustRange(parse, maxNumber);

            if (parse.inverse) {
                removeRangeFromList(list, parse);
            } else {
                addRangeToList(list, parse);
            }
        }

        return list;
    }

    private static void adjustRange(SequenceList parse, int maxNumber) {
        if (parse.low < 1) {
            parse.low = 1;
        }
        if (parse.high < 1 || parse.high > maxNumber) {
            parse.high = maxNumber;
        }
        if (parse.low > maxNumber) {
            parse.low = maxNumber;
        }
    }

    private static void removeRangeFromList(List<Integer> list, SequenceList parse) {
        if (parse.low > parse.high) {
            swapLowAndHigh(parse);
        }

        list.removeIf(n -> shouldRemove(parse, n));
    }

    private static boolean shouldRemove(SequenceList parse, int n) {
        return (parse.even && (n & 1) == 1) ||
                (parse.odd && (n & 1) == 0) ||
                (n >= parse.low && n <= parse.high);
    }

    private static void addRangeToList(List<Integer> list, SequenceList parse) {
        int inc = determineIncrement(parse);

        if (parse.low > parse.high) {
            addDescendingRange(list, parse, inc);
        } else {
            addAscendingRange(list, parse, inc);
        }
    }

    private static int determineIncrement(SequenceList parse) {
        int inc = 1;
        if (parse.odd || parse.even) {
            inc = 2;
            if (parse.odd) {
                parse.low |= 1;
            } else {
                parse.low += (parse.low & 1) == 1 ? 1 : 0;
            }
        }
        return inc;
    }

    private static void addDescendingRange(List<Integer> list, SequenceList parse, int inc) {
        // Ensure 'inc' is valid for descending order
        if (inc >= 0) {
            inc = -1; // Set to -1 for descending
        } else {
            inc--; // Decrement inc to ensure it is negative
        }

        // Adjust parse.low based on its even or odd state
        if (parse.even) {
            parse.low &= ~1; // Set low to the nearest even number
        } else {
            parse.low -= (parse.low & 1) == 1 ? 0 : 1; // Make low odd if it's not already
        }

        // Add numbers to the list in descending order
        for (int k = parse.low; k <= parse.high; k += inc) {
            list.add(k);
        }
    }


    private static void addAscendingRange(List<Integer> list, SequenceList parse, int inc) {
        for (int k = parse.low; k <= parse.high; k += inc) {
            list.add(k);
        }
    }

    private static void swapLowAndHigh(SequenceList parse) {
        int temp = parse.low;
        parse.low = parse.high;
        parse.high = temp;
    }


    protected char nextChar() {
        while (true) {
            if (ptr >= text.length) {
                return EOT;
            }
            char c = text[ptr++];
            if (c > ' ') {
                return c;
            }
        }
    }

    protected void putBack() {
        --ptr;
        if (ptr < 0) {
            ptr = 0;
        }
    }

    protected int getType() {
        StringBuilder buf = new StringBuilder();
        int state = FIRST;

        while (true) {
            char c = nextChar();

            if (c == EOT) {
                return handleEndOfText(state, buf);
            }

            switch (state) {
                case FIRST:
                    state = handleFirstState(c, buf);
                    if (state == NOT || state == MINUS || state == COMMA) {
                        return state;
                    }
                    break;
                case DIGIT:
                    if (handleDigitState(c, buf)) return NUMBER_CONST;
                    break;

                case OTHER_CONST:
                    if (handleOtherConstState(c, buf)) return TEXT_CONST;
                    break;

                default:
                    throw new IllegalArgumentException("Illegal character: " + c);
            }
        }
    }

    private int handleEndOfText(int state, StringBuilder buf) {
        if (state == DIGIT) {
            other = buf.toString();
            number = Integer.parseInt(other);
            return NUMBER_CONST;
        } else if (state == OTHER_CONST) {
            other = buf.toString().toLowerCase();
            return TEXT_CONST;
        }
        return END;
    }

    private int handleFirstState(char c, StringBuilder buf) {
        switch (c) {
            case '!':
                return NOT;
            case '-':
                return MINUS;
            case ',':
                return COMMA;
            default:
                buf.append(c);
                if (c >= '0' && c <= '9') {
                    return DIGIT;
                } else {
                    return OTHER_CONST;
                }
        }
    }

    private boolean handleDigitState(char c, StringBuilder buf) {
        if (c >= '0' && c <= '9') {
            buf.append(c);
        } else {
            putBack();
            other = buf.toString();
            number = Integer.parseInt(other);
            return true;
        }
        return false;
    }

    private boolean handleOtherConstState(char c, StringBuilder buf) {
        if (NOT_OTHER.indexOf(c) < 0) {
            buf.append(c);
        } else {
            putBack();
            other = buf.toString().toLowerCase();
            return true;
        }
        return false;
    }


    private void otherProc() {
        if (other.equals("odd") || other.equals("o")) {
            odd = true;
            even = false;
        } else if (other.equals("even") || other.equals("e")) {
            odd = false;
            even = true;
        }
    }

    protected boolean getAttributes() {
        initializeAttributes();
        int state = OTHER_CONST;

        while (true) {
            int type = getType();

            if (isEndOrComma(type, state)) {
                return type == END;
            }

            state = processState(type, state);
        }
    }

    private void initializeAttributes() {
        low = -1;
        high = -1;
        odd = even = inverse = false;
    }

    private boolean isEndOrComma(int type, int state) {
        if (type == END || type == COMMA) {
            if (state == DIGIT) {
                high = low;
            }
            return true;
        }
        return false;
    }

    private int processState(int type, int state) {
        return switch (state) {
            case OTHER_CONST -> handleOtherConst(type);
            case DIGIT -> handleDigit(type);
            case DIGIT2 -> handleDigit2(type);
            default -> throw new IllegalArgumentException("Illegal state: " + state);
        };
    }

    private int handleOtherConst(int type) {
        switch (type) {
            case NOT:
                inverse = true;
                return OTHER_CONST;
            case MINUS:
                return DIGIT2;
            default:
                if (type == NUMBER_CONST) {
                    low = number;
                    return DIGIT;
                } else {
                    otherProc();
                    return OTHER_CONST;
                }
        }
    }

    private int handleDigit(int type) {
        switch (type) {
            case NOT:
                inverse = true;
                high = low;
                return OTHER_CONST;
            case MINUS:
                return DIGIT2;
            default:
                high = low;
                otherProc();
                return OTHER_CONST;
        }
    }

    private int handleDigit2(int type) {
        return switch (type) {
            case NOT -> {
                inverse = true;
                yield OTHER_CONST;
            }
            case MINUS -> DIGIT2;
            case NUMBER_CONST -> {
                high = number;
                yield OTHER_CONST;
            }
            default -> {
                otherProc();
                yield OTHER_CONST;
            }
        };
    }

}