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

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Creates a getName tree.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PdfNameTree {

    private static final int LEAF_SIZE = 64;

    private PdfNameTree(){
        
    }

    /**
     * Writes a getName tree to a PdfWriter.
     *
     * @param items  the item of the getName tree. The key is a <CODE>String</CODE> and the value is a
     *               <CODE>PdfObject</CODE>. Note that although the keys are strings only the lower byte is used and no
     *               check is made for chars with the same lower byte and different upper byte. This will generate a
     *               wrong tree getName.
     * @param writer the writer
     * @return the dictionary with the getName tree. This dictionary is the one generally pointed to by the key /Dests, for
     * example
     * @throws IOException on error
     */
    public static PdfDictionary writeTree(HashMap<String, ? extends PdfObject> items, PdfWriter writer)
            throws IOException {
        return writeTree((Map<String, ? extends PdfObject>) items, writer);
    }

    /**
     * Writes a getName tree to a PdfWriter.
     *
     * @param items  the item of the getName tree. The key is a <CODE>String</CODE> and the value is a
     *               <CODE>PdfObject</CODE>. Note that although the keys are strings only the lower byte is used and no
     *               check is made for chars with the same lower byte and different upper byte. This will generate a
     *               wrong tree getName.
     * @param writer the writer
     * @return the dictionary with the getName tree. This dictionary is the one generally pointed to by the key /Dests, for
     * example
     * @throws IOException on error
     */
    public static PdfDictionary writeTree(Map<String, ? extends PdfObject> items, PdfWriter writer) throws IOException {
        if (items.isEmpty()) {
            return null;
        }
        String[] names = items.keySet().toArray(new String[0]);
        Arrays.sort(names);
        if (names.length <= LEAF_SIZE) {
            return createLeafDictionary(items, names);
        }
        return createInternalDictionaryTree(items, names, writer);
    }

    private static PdfDictionary createLeafDictionary(Map<String, ? extends PdfObject> items, String[] names) {
        PdfDictionary dic = new PdfDictionary();
        PdfArray ar = new PdfArray();
        for (String name : names) {
            ar.add(new PdfString(name, null));
            ar.add(items.get(name));
        }
        dic.put(PdfName.NAMES, ar);
        return dic;
    }

    private static PdfDictionary createInternalDictionaryTree(Map<String, ? extends PdfObject> items, String[] names, PdfWriter writer) throws IOException {
        int numKids = (names.length + LEAF_SIZE - 1) / LEAF_SIZE;
        PdfIndirectReference[] kids = new PdfIndirectReference[numKids];

        for (int k = 0; k < numKids; ++k) {
            int offset = k * LEAF_SIZE;
            int end = Math.min(offset + LEAF_SIZE, names.length);
            PdfDictionary dic = createNodeDictionary(names, items, offset, end);
            kids[k] = writer.addToBody(dic).getIndirectReference();
        }

        return createInternalTree(kids, names.length, writer);
    }

    private static PdfDictionary createNodeDictionary(String[] names, Map<String, ? extends PdfObject> items, int offset, int end) {
        PdfDictionary dic = new PdfDictionary();
        PdfArray limits = new PdfArray();
        limits.add(new PdfString(names[offset], null));
        limits.add(new PdfString(names[end - 1], null));
        dic.put(PdfName.LIMITS, limits);

        PdfArray namesArray = new PdfArray();
        for (int i = offset; i < end; ++i) {
            namesArray.add(new PdfString(names[i], null));
            namesArray.add(items.get(names[i]));
        }
        dic.put(PdfName.NAMES, namesArray);

        return dic;
    }

    private static PdfDictionary createInternalTree(PdfIndirectReference[] kids, int totalItems, PdfWriter writer) throws IOException {
        int numKids = kids.length;
        int skip = LEAF_SIZE;

        while (numKids > LEAF_SIZE) {
            skip *= LEAF_SIZE;
            int newNumKids = (totalItems + skip - 1) / skip;
            PdfIndirectReference[] newKids = new PdfIndirectReference[newNumKids];

            for (int k = 0; k < newNumKids; ++k) {
                int offset = k * LEAF_SIZE;
                int end = Math.min(offset + LEAF_SIZE, numKids);
                PdfDictionary dic = createInternalNodeDictionary(kids, offset, end);
                newKids[k] = writer.addToBody(dic).getIndirectReference();
            }

            numKids = newNumKids;
            kids = newKids;
        }

        PdfDictionary rootDic = new PdfDictionary();
        PdfArray kidsArray = new PdfArray();
        for (PdfIndirectReference kid : kids) {
            kidsArray.add(kid);
        }
        rootDic.put(PdfName.KIDS, kidsArray);

        return rootDic;
    }

    private static PdfDictionary createInternalNodeDictionary(PdfIndirectReference[] kids, int offset, int end) {
        PdfDictionary dic = new PdfDictionary();
        PdfArray limits = new PdfArray();
        limits.add(new PdfString(kids[offset].toString(), null));
        limits.add(new PdfString(kids[end - 1].toString(), null));
        dic.put(PdfName.LIMITS, limits);

        PdfArray kidsArray = new PdfArray();
        for (int i = offset; i < end; ++i) {
            kidsArray.add(kids[i]);
        }
        dic.put(PdfName.KIDS, kidsArray);

        return dic;
    }


    private static void iterateItems(PdfDictionary dic, HashMap<String, PdfObject> items) {
        PdfArray nn = (PdfArray) PdfReader.getPdfObjectRelease(dic.get(PdfName.NAMES));
        if (nn != null) {
            int k = 0;
            while (k < nn.size()) {
                PdfString s = (PdfString) PdfReader.getPdfObjectRelease(nn.getPdfObject(k+1));
                items.put(PdfEncodings.convertToString(s.getBytes(), null), nn.getPdfObject(k));
                k++;
            }
        } else if ((nn = (PdfArray) PdfReader.getPdfObjectRelease(dic.get(PdfName.KIDS))) != null) {
            for (int k = 0; k < nn.size(); ++k) {
                PdfDictionary kid = (PdfDictionary) PdfReader.getPdfObjectRelease(nn.getPdfObject(k));
                iterateItems(kid, items);
            }
        }
    }

    public static HashMap<String, PdfObject> readTree(PdfDictionary dic) {
        HashMap<String, PdfObject> items = new HashMap<>();
        if (dic != null) {
            iterateItems(dic, items);
        }
        return items;
    }
}
