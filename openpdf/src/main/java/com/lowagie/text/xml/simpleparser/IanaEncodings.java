/*
 * $Id: IanaEncodings.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2003-2007 Paulo Soares and Bruno Lowagie.
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
 *
 * The values used in this class are based on class org.apache.xercis.util.EncodingMap
 * http://svn.apache.org/viewvc/xerces/java/trunk/src/org/apache/xerces/util/EncodingMap.java?view=markup
 * This class was originally published under the following license:
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lowagie.text.xml.simpleparser;

import java.util.HashMap;
import java.util.Map;

/**
 * Translates a IANA encoding getName to a Java encoding.
 */

public class IanaEncodings {

    private IanaEncodings(){
        //empty on purpose
    }

    /**
     * The object that maps IANA to Java encodings.
     */
    private static final Map<String, String> map = new HashMap<>();
    private static final String CODE_PAGE_037 = "CP037";
    private static final String CODE_PAGE_277 = "CP277";
    private static final String CODE_PAGE_278 = "CP278";
    private static final String CODE_PAGE_280 = "CP280";
    private static final String CODE_PAGE_284 = "CP284";
    private static final String CODE_PAGE_285 = "CP285";
    private static final String CODE_PAGE_297 = "CP297";
    private static final String CODE_PAGE_420 = "CP420";
    private static final String CODE_PAGE_424 = "CP424";
    private static final String CODE_PAGE_500 = "CP500";
    private static final String CODE_PAGE_868 = "CP868";
    private static final String CODE_PAGE_869 = "CP869";
    private static final String CODE_PAGE_870 = "CP870";
    private static final String CODE_PAGE_871 = "CP871";
    private static final String CODE_PAGE_918 = "CP918";
    private static final String CHARSET_GB2312 = "GB2312";
    public static final String JIS0208 = "JIS0208";
    public static final String JIS0212 = "JIS0212";
    private static final String ISO_8859_1_ENCODING = "ISO8859_1";
    private static final String ISO_8859_2_ENCODING = "ISO8859_2";
    private static final String ISO_8859_3_ENCODING = "ISO8859_3";
    private static final String ISO_8859_4_ENCODING = "ISO8859_4";
    private static final String ISO_8859_5_ENCODING = "ISO8859_5";
    private static final String ISO_8859_6_ENCODING = "ISO8859_6";
    private static final String ISO_8859_7_ENCODING = "ISO8859_7";
    private static final String ISO_8859_8_ENCODING = "ISO8859_8";
    private static final String ISO_8859_9_ENCODING = "ISO8859_9";
    private static final String ASCII = "ASCII";

    static {
        // add IANA to Java encoding mappings.
        map.put("BIG5", "Big5");
        map.put("CSBIG5", "Big5");
        map.put(CODE_PAGE_037, CODE_PAGE_037);
        map.put("IBM037", CODE_PAGE_037);
        map.put("CSIBM037", CODE_PAGE_037);
        map.put("EBCDIC-CP-US", CODE_PAGE_037);
        map.put("EBCDIC-CP-CA", CODE_PAGE_037);
        map.put("EBCDIC-CP-NL", CODE_PAGE_037);
        map.put("EBCDIC-CP-WT", CODE_PAGE_037);
        map.put("IBM277", CODE_PAGE_277);
        map.put(CODE_PAGE_277, CODE_PAGE_277);
        map.put("CSIBM277", CODE_PAGE_277);
        map.put("EBCDIC-CP-DK", CODE_PAGE_277);
        map.put("EBCDIC-CP-NO", CODE_PAGE_277);
        map.put("IBM278", CODE_PAGE_278);
        map.put(CODE_PAGE_278, CODE_PAGE_278);
        map.put("CSIBM278", CODE_PAGE_278);
        map.put("EBCDIC-CP-FI", CODE_PAGE_278);
        map.put("EBCDIC-CP-SE", CODE_PAGE_278);
        map.put("IBM280", CODE_PAGE_280);
        map.put(CODE_PAGE_280, CODE_PAGE_280);
        map.put("CSIBM280", CODE_PAGE_280);
        map.put("EBCDIC-CP-IT", CODE_PAGE_280);
        map.put("IBM284", CODE_PAGE_284);
        map.put(CODE_PAGE_284, CODE_PAGE_284);
        map.put("CSIBM284", CODE_PAGE_284);
        map.put("EBCDIC-CP-ES", CODE_PAGE_284);
        map.put("EBCDIC-CP-GB", CODE_PAGE_285);
        map.put("IBM285", CODE_PAGE_285);
        map.put(CODE_PAGE_285, CODE_PAGE_285);
        map.put("CSIBM285", CODE_PAGE_285);
        map.put("EBCDIC-CP-FR", CODE_PAGE_297);
        map.put("IBM297", CODE_PAGE_297);
        map.put(CODE_PAGE_297, CODE_PAGE_297);
        map.put("CSIBM297", CODE_PAGE_297);
        map.put("EBCDIC-CP-AR1", CODE_PAGE_420);
        map.put("IBM420", CODE_PAGE_420);
        map.put(CODE_PAGE_420, CODE_PAGE_420);
        map.put("CSIBM420", CODE_PAGE_420);
        map.put("EBCDIC-CP-HE", CODE_PAGE_424);
        map.put("IBM424", CODE_PAGE_424);
        map.put(CODE_PAGE_424, CODE_PAGE_424);
        map.put("CSIBM424", CODE_PAGE_424);
        map.put("EBCDIC-CP-CH", CODE_PAGE_500);
        map.put("IBM500", CODE_PAGE_500);
        map.put(CODE_PAGE_500, CODE_PAGE_500);
        map.put("CSIBM500", CODE_PAGE_500);
        map.put("EBCDIC-CP-CH", CODE_PAGE_500);
        map.put("EBCDIC-CP-BE", CODE_PAGE_500);
        map.put("IBM868", CODE_PAGE_868);
        map.put(CODE_PAGE_868, CODE_PAGE_868);
        map.put("CSIBM868", CODE_PAGE_868);
        map.put("CP-AR", CODE_PAGE_868);
        map.put("IBM869", CODE_PAGE_869);
        map.put(CODE_PAGE_869, CODE_PAGE_869);
        map.put("CSIBM869", CODE_PAGE_869);
        map.put("CP-GR", CODE_PAGE_869);
        map.put("IBM870", CODE_PAGE_870);
        map.put(CODE_PAGE_870, CODE_PAGE_870);
        map.put("CSIBM870", CODE_PAGE_870);
        map.put("EBCDIC-CP-ROECE", CODE_PAGE_870);
        map.put("EBCDIC-CP-YU", CODE_PAGE_870);
        map.put("IBM871", CODE_PAGE_871);
        map.put(CODE_PAGE_871, CODE_PAGE_871);
        map.put("CSIBM871", CODE_PAGE_871);
        map.put("EBCDIC-CP-IS", CODE_PAGE_871);
        map.put("IBM918", CODE_PAGE_918);
        map.put(CODE_PAGE_918, CODE_PAGE_918);
        map.put("CSIBM918", CODE_PAGE_918);
        map.put("EBCDIC-CP-AR2", CODE_PAGE_918);
        map.put("EUC-JP", "EUCJIS");
        map.put("CSEUCPkdFmtJapanese", "EUCJIS");
        map.put("EUC-KR", "KSC5601");
        map.put(CHARSET_GB2312, CHARSET_GB2312);
        map.put("CSGB2312", CHARSET_GB2312);
        map.put("ISO-2022-JP", "JIS");
        map.put("CSISO2022JP", "JIS");
        map.put("ISO-2022-KR", "ISO2022KR");
        map.put("CSISO2022KR", "ISO2022KR");
        map.put("ISO-2022-CN", "ISO2022CN");

        map.put("X0201", "JIS0201");
        map.put("CSISO13JISC6220JP", "JIS0201");
        map.put("X0208", JIS0208);
        map.put("ISO-IR-87", JIS0208);
        map.put("X0208dbiJIS_X0208-1983", JIS0208);
        map.put("CSISO87JISX0208", JIS0208);
        map.put("X0212", JIS0212);
        map.put("ISO-IR-159", JIS0212);
        map.put("CSISO159JISX02121990", JIS0212);
        map.put("SHIFT_JIS", "SJIS");
        map.put("CSSHIFT_JIS", "SJIS");
        map.put("MS_Kanji", "SJIS");

        // Add support for Cp1252 and its friends
        map.put("WINDOWS-1250", "Cp1250");
        map.put("WINDOWS-1251", "Cp1251");
        map.put("WINDOWS-1252", "Cp1252");
        map.put("WINDOWS-1253", "Cp1253");
        map.put("WINDOWS-1254", "Cp1254");
        map.put("WINDOWS-1255", "Cp1255");
        map.put("WINDOWS-1256", "Cp1256");
        map.put("WINDOWS-1257", "Cp1257");
        map.put("WINDOWS-1258", "Cp1258");
        map.put("TIS-620", "TIS620");

        map.put("ISO-8859-1", ISO_8859_1_ENCODING);
        map.put("ISO-IR-100", ISO_8859_1_ENCODING);
        map.put("ISO_8859-1", ISO_8859_1_ENCODING);
        map.put("LATIN1", ISO_8859_1_ENCODING);
        map.put("CSISOLATIN1", ISO_8859_1_ENCODING);
        map.put("L1", ISO_8859_1_ENCODING);
        map.put("IBM819", ISO_8859_1_ENCODING);
        map.put("CP819", ISO_8859_1_ENCODING);

        map.put("ISO-8859-2", ISO_8859_2_ENCODING);
        map.put("ISO-IR-101", ISO_8859_2_ENCODING);
        map.put("ISO_8859-2", ISO_8859_2_ENCODING);
        map.put("LATIN2", ISO_8859_2_ENCODING);
        map.put("CSISOLATIN2", ISO_8859_2_ENCODING);
        map.put("L2", ISO_8859_2_ENCODING);

        map.put("ISO-8859-3", ISO_8859_3_ENCODING);
        map.put("ISO-IR-109", ISO_8859_3_ENCODING);
        map.put("ISO_8859-3", ISO_8859_3_ENCODING);
        map.put("LATIN3", ISO_8859_3_ENCODING);
        map.put("CSISOLATIN3", ISO_8859_3_ENCODING);
        map.put("L3", ISO_8859_3_ENCODING);

        map.put("ISO-8859-4", ISO_8859_4_ENCODING);
        map.put("ISO-IR-110", ISO_8859_4_ENCODING);
        map.put("ISO_8859-4", ISO_8859_4_ENCODING);
        map.put("LATIN4", ISO_8859_4_ENCODING);
        map.put("CSISOLATIN4", ISO_8859_4_ENCODING);
        map.put("L4", ISO_8859_4_ENCODING);

        map.put("ISO-8859-5", ISO_8859_5_ENCODING);
        map.put("ISO-IR-144", ISO_8859_5_ENCODING);
        map.put("ISO_8859-5", ISO_8859_5_ENCODING);
        map.put("CYRILLIC", ISO_8859_5_ENCODING);
        map.put("CSISOLATINCYRILLIC", ISO_8859_5_ENCODING);

        map.put("ISO-8859-6", ISO_8859_6_ENCODING);
        map.put("ISO-IR-127", ISO_8859_6_ENCODING);
        map.put("ISO_8859-6", ISO_8859_6_ENCODING);
        map.put("ECMA-114", ISO_8859_6_ENCODING);
        map.put("ASMO-708", ISO_8859_6_ENCODING);
        map.put("ARABIC", ISO_8859_6_ENCODING);
        map.put("CSISOLATINARABIC", ISO_8859_6_ENCODING);

        map.put("ISO-8859-7", ISO_8859_7_ENCODING);
        map.put("ISO-IR-126", ISO_8859_7_ENCODING);
        map.put("ISO_8859-7", ISO_8859_7_ENCODING);
        map.put("ELOT_928", ISO_8859_7_ENCODING);
        map.put("ECMA-118", ISO_8859_7_ENCODING);
        map.put("GREEK", ISO_8859_7_ENCODING);
        map.put("CSISOLATINGREEK", ISO_8859_7_ENCODING);
        map.put("GREEK8", ISO_8859_7_ENCODING);

        map.put("ISO-8859-8", ISO_8859_8_ENCODING);
        map.put("ISO-8859-8-I", ISO_8859_8_ENCODING); // added since this encoding only differs w.r.t. presentation
        map.put("ISO-IR-138", ISO_8859_8_ENCODING);
        map.put("ISO_8859-8", ISO_8859_8_ENCODING);
        map.put("HEBREW", ISO_8859_8_ENCODING);
        map.put("CSISOLATINHEBREW", ISO_8859_8_ENCODING);

        map.put("ISO-8859-9", ISO_8859_9_ENCODING);
        map.put("ISO-IR-148", ISO_8859_9_ENCODING);
        map.put("ISO_8859-9", ISO_8859_9_ENCODING);
        map.put("LATIN5", ISO_8859_9_ENCODING);
        map.put("CSISOLATIN5", ISO_8859_9_ENCODING);
        map.put("L5", ISO_8859_9_ENCODING);

        map.put("KOI8-R", "KOI8_R");
        map.put("CSKOI8-R", "KOI8_R");
        map.put("US-ASCII", ASCII);
        map.put("ISO-IR-6", ASCII);
        map.put("ANSI_X3.4-1986", ASCII);
        map.put("ISO_646.IRV:1991", ASCII);
        map.put(ASCII, ASCII);
        map.put("CSASCII", ASCII);
        map.put("ISO646-US", ASCII);
        map.put("US", ASCII);
        map.put("IBM367", ASCII);
        map.put("CP367", ASCII);
        map.put("UTF-8", "UTF8");
        map.put("UTF-16", "Unicode");
        map.put("UTF-16BE", "UnicodeBig");
        map.put("UTF-16LE", "UnicodeLittle");
    }

    /**
     * Gets the java encoding from the IANA encoding. If the encoding cannot be found it returns the input.
     *
     * @param iana the IANA encoding
     * @return the java encoding
     */
    public static String getJavaEncoding(String iana) {
        String IANA = iana.toUpperCase();
        String jdec = map.get(IANA);
        if (jdec == null) {
            jdec = iana;
        }
        return jdec;
    }
}
