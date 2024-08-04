/*
 * $Id: SpecialSymbol.java 3963 2009-06-11 11:45:49Z psoares33 $
 *
 * Copyright 2000, 2001, 2002 by Bruno Lowagie.
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

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains the symbols that correspond with special symbols.
 * <p>
 * When you construct a <CODE>Phrase</CODE> with Phrase.getInstance using a <CODE>String</CODE>, this
 * <CODE>String</CODE> can contain special Symbols. These are characters with an int value between 913 and 937 (except
 * 930) and between 945 and 969. With this class the value of the corresponding character of the Font Symbol, can be
 * retrieved.
 *
 * @author Bruno Lowagie
 * @author Evelyne De Cordier
 * @see Phrase
 */

public class SpecialSymbol {

    private static final Map<Integer, Character> greekLetterMap = createGreekLetterMap();

    private static Map<Integer, Character> createGreekLetterMap() {
        Map<Integer, Character> map = new HashMap<>();

        map.put(913, 'A'); // ALFA
        map.put(914, 'B'); // BETA
        map.put(915, 'G'); // GAMMA
        map.put(916, 'D'); // DELTA
        map.put(917, 'E'); // EPSILON
        map.put(918, 'Z'); // ZETA
        map.put(919, 'H'); // ETA
        map.put(920, 'Q'); // THETA
        map.put(921, 'I'); // IOTA
        map.put(922, 'K'); // KAPPA
        map.put(923, 'L'); // LAMBDA
        map.put(924, 'M'); // MU
        map.put(925, 'N'); // NU
        map.put(926, 'X'); // XI
        map.put(927, 'O'); // OMICRON
        map.put(928, 'P'); // PI
        map.put(929, 'R'); // RHO
        map.put(931, 'S'); // SIGMA
        map.put(932, 'T'); // TAU
        map.put(933, 'U'); // UPSILON
        map.put(934, 'F'); // PHI
        map.put(935, 'C'); // CHI
        map.put(936, 'Y'); // PSI
        map.put(937, 'W'); // OMEGA
        map.put(945, 'a'); // alfa
        map.put(946, 'b'); // beta
        map.put(947, 'g'); // gamma
        map.put(948, 'd'); // delta
        map.put(949, 'e'); // epsilon
        map.put(950, 'z'); // zeta
        map.put(951, 'h'); // eta
        map.put(952, 'q'); // theta
        map.put(953, 'i'); // iota
        map.put(954, 'k'); // kappa
        map.put(955, 'l'); // lambda
        map.put(956, 'm'); // mu
        map.put(957, 'n'); // nu
        map.put(958, 'x'); // xi
        map.put(959, 'o'); // omicron
        map.put(960, 'p'); // pi
        map.put(961, 'r'); // rho
        map.put(962, 'V'); // sigma
        map.put(963, 's'); // sigma
        map.put(964, 't'); // tau
        map.put(965, 'u'); // upsilon
        map.put(966, 'f'); // phi
        map.put(967, 'c'); // chi
        map.put(968, 'y'); // psi
        map.put(969, 'w'); // omega

        return map;
    }

    // Private constructor to prevent instantiation
    private SpecialSymbol() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
    /**
     * Returns the first occurrence of a special symbol in a <CODE>String</CODE>.
     *
     * @param string a <CODE>String</CODE>
     * @return an index of -1 if no special symbol was found
     */
    public static int index(String string) {
        int length = string.length();
        for (int i = 0; i < length; i++) {
            if (getCorrespondingSymbol(string.charAt(i)) != ' ') {
                return i;
            }
        }
        return -1;
    }

    /**
     * Gets a chunk with a symbol character.
     *
     * @param c    a character that has to be changed into a symbol
     * @param font Font if there is no SYMBOL character corresponding with c
     * @return a SYMBOL version of a character
     */
    public static Chunk get(char c, Font font) {
        char greek = SpecialSymbol.getCorrespondingSymbol(c);
        if (greek == ' ') {
            return new Chunk(String.valueOf(c), font);
        }
        Font symbol = new Font(Font.SYMBOL, font.getSize(), font.getStyle(), font.getColor());
        String s = String.valueOf(greek);
        return new Chunk(s, symbol);
    }

    /**
     * Looks for the corresponding symbol in the font Symbol.
     *
     * @param c the original ASCII-char
     * @return the corresponding symbol in font Symbol
     */
    public static char getCorrespondingSymbol(char c) {
        if (c >= 913 && c <= 937) {
            return getUpperCaseGreekSymbol(c);
        } else if (c >= 945 && c <= 969) {
            return getLowerCaseGreekSymbol(c);
        } else {
            return ' ';
        }
    }

    private static char getUpperCaseGreekSymbol(char c) {
        return switch (c) {
            case 913 -> 'A'; // ALFA
            case 914 -> 'B'; // BETA
            case 915 -> 'G'; // GAMMA
            case 916 -> 'D'; // DELTA
            case 917 -> 'E'; // EPSILON
            case 918 -> 'Z'; // ZETA
            case 919 -> 'H'; // ETA
            case 920 -> 'Q'; // THETA
            case 921 -> 'I'; // IOTA
            case 922 -> 'K'; // KAPPA
            case 923 -> 'L'; // LAMBDA
            case 924 -> 'M'; // MU
            case 925 -> 'N'; // NU
            case 926 -> 'X'; // XI
            case 927 -> 'O'; // OMICRON
            case 928 -> 'P'; // PI
            case 929 -> 'R'; // RHO
            case 931 -> 'S'; // SIGMA
            case 932 -> 'T'; // TAU
            case 933 -> 'U'; // UPSILON
            case 934 -> 'F'; // PHI
            case 935 -> 'C'; // CHI
            case 936 -> 'Y'; // PSI
            case 937 -> 'W'; // OMEGA
            default -> ' ';
        };
    }

    private static char getLowerCaseGreekSymbol(char c) {
        return switch (c) {
            case 945 -> 'a'; // alfa
            case 946 -> 'b'; // beta
            case 947 -> 'g'; // gamma
            case 948 -> 'd'; // delta
            case 949 -> 'e'; // epsilon
            case 950 -> 'z'; // zeta
            case 951 -> 'h'; // eta
            case 952 -> 'q'; // theta
            case 953 -> 'i'; // iota
            case 954 -> 'k'; // kappa
            case 955 -> 'l'; // lambda
            case 956 -> 'm'; // mu
            case 957 -> 'n'; // nu
            case 958 -> 'x'; // xi
            case 959 -> 'o'; // omicron
            case 960 -> 'p'; // pi
            case 961 -> 'r'; // rho
            case 962 -> 'V'; // sigma finale
            case 963 -> 's'; // sigma
            case 964 -> 't'; // tau
            case 965 -> 'u'; // upsilon
            case 966 -> 'f'; // phi
            case 967 -> 'c'; // chi
            case 968 -> 'y'; // psi
            case 969 -> 'w'; // omega
            default -> ' ';
        };
    }
}