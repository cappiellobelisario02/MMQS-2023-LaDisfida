/*
 * Copyright 2005 by Paulo Soares.
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
 * Creates a number tree.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PdfNumberTree {

    private static final int LEAF_SIZE = 64;

    private PdfNumberTree(){

    }

    /**
     * Creates a number tree.
     *
     * @param items  the item of the number tree. The key is an <CODE>Integer</CODE> and the value is a
     *               <CODE>PdfObject</CODE>.
     * @param writer the writer
     * @return the dictionary with the number tree.
     * @throws IOException on error
     */
    public static PdfDictionary writeTree(Map<Integer, ? extends PdfObject> items, PdfWriter writer) throws IOException {
        if (items.isEmpty()) {
            return null;
        }

        Integer[] numbers = items.keySet().toArray(new Integer[0]);
        Arrays.sort(numbers);

        if (numbers.length <= LEAF_SIZE) {
            return createLeafDictionary(numbers, items);
        }

        return createTreeDictionary(numbers, items, writer);
    }

    private static PdfDictionary createLeafDictionary(Integer[] numbers, Map<Integer, ? extends PdfObject> items) {
        PdfDictionary dic = new PdfDictionary();
        PdfArray ar = new PdfArray();
        for (Integer number : numbers) {
            ar.add(new PdfNumber(number));
            ar.add(items.get(number));
        }
        dic.put(PdfName.NUMS, ar);
        return dic;
    }

    private static PdfDictionary createTreeDictionary(Integer[] numbers, Map<Integer, ? extends PdfObject> items, PdfWriter writer) throws IOException {
        int leafSize = LEAF_SIZE;
        PdfIndirectReference[] kids = createKidsArray(numbers, items, writer, leafSize);

        return buildTree(kids, numbers.length, leafSize, writer);
    }

    private static PdfIndirectReference[] createKidsArray(Integer[] numbers, Map<Integer, ? extends PdfObject> items, PdfWriter writer, int leafSize) throws IOException {
        int numKids = (numbers.length + leafSize - 1) / leafSize;
        PdfIndirectReference[] kids = new PdfIndirectReference[numKids];
        for (int k = 0; k < numKids; ++k) {
            int offset = k * leafSize;
            int end = Math.min(offset + leafSize, numbers.length);
            PdfDictionary dic = createNodeDictionary(numbers, items, offset, end);
            kids[k] = writer.addToBody(dic).getIndirectReference();
        }
        return kids;
    }

    private static PdfDictionary createNodeDictionary(Integer[] numbers, Map<Integer, ? extends PdfObject> items, int offset, int end) {
        PdfDictionary dic = new PdfDictionary();
        PdfArray limits = new PdfArray();
        limits.add(new PdfNumber(numbers[offset]));
        limits.add(new PdfNumber(numbers[end - 1]));
        dic.put(PdfName.LIMITS, limits);

        PdfArray nums = new PdfArray();
        for (; offset < end; ++offset) {
            nums.add(new PdfNumber(numbers[offset]));
            nums.add(items.get(numbers[offset]));
        }
        dic.put(PdfName.NUMS, nums);

        return dic;
    }

    private static PdfDictionary buildTree(PdfIndirectReference[] kids, int totalSize, int leafSize, PdfWriter writer) throws IOException {
        int top = kids.length;
        while (true) {
            if (top <= leafSize) {
                return createTopLevelDictionary(kids);
            }

            leafSize *= LEAF_SIZE;
            int numNodes = (totalSize + leafSize - 1) / leafSize;
            kids = createIntermediateNodes(kids, top, leafSize, writer);
            top = numNodes;
        }
    }

    private static PdfDictionary createTopLevelDictionary(PdfIndirectReference[] kids) {
        PdfArray arr = new PdfArray();
        for (PdfIndirectReference kid : kids) {
            arr.add(kid);
        }
        PdfDictionary dic = new PdfDictionary();
        dic.put(PdfName.KIDS, arr);
        return dic;
    }

    private static PdfIndirectReference[] createIntermediateNodes(PdfIndirectReference[] kids, int top, int leafSize, PdfWriter writer) throws IOException {
        int numNodes = (kids.length + leafSize - 1) / leafSize;
        PdfIndirectReference[] newKids = new PdfIndirectReference[numNodes];
        for (int k = 0; k < numNodes; ++k) {
            int offset = k * leafSize;
            int end = Math.min(offset + leafSize, top);
            PdfDictionary dic = createIntermediateNodeDictionary(kids, offset, end);
            newKids[k] = writer.addToBody(dic).getIndirectReference();
        }
        return newKids;
    }

    private static PdfDictionary createIntermediateNodeDictionary(PdfIndirectReference[] kids, int offset, int end) {
        PdfDictionary dic = new PdfDictionary();
        PdfArray limits = new PdfArray();
        limits.add(new PdfNumber(kids[offset].getNumber()));  // Assuming the first indirect reference number is the lower limit
        limits.add(new PdfNumber(kids[end - 1].getNumber()));  // Assuming the last indirect reference number is the upper limit
        dic.put(PdfName.LIMITS, limits);

        PdfArray kidsArray = new PdfArray();
        for (; offset < end; ++offset) {
            kidsArray.add(kids[offset]);
        }
        dic.put(PdfName.KIDS, kidsArray);

        return dic;
    }


    private static void iterateItems(PdfDictionary dic, Map<Integer, PdfObject> items) {
        PdfArray nn = (PdfArray) PdfReader.getPdfObjectRelease(dic.get(PdfName.NUMS));
        if (nn != null) {
            int k = 0;
            while (k < nn.size()) {
                PdfNumber s = (PdfNumber) PdfReader.getPdfObjectRelease(nn.getPdfObject(k+1));
                items.put(s.intValue(), nn.getPdfObject(k));
                k++;
            }
        } else if ((nn = (PdfArray) PdfReader.getPdfObjectRelease(dic.get(PdfName.KIDS))) != null) {
            for (int k = 0; k < nn.size(); ++k) {
                PdfDictionary kid = (PdfDictionary) PdfReader.getPdfObjectRelease(nn.getPdfObject(k));
                iterateItems(kid, items);
            }
        }
    }

    public static HashMap<Integer, PdfObject> readTree(PdfDictionary dic) {
        HashMap<Integer, PdfObject> items = new HashMap<>();
        if (dic != null) {
            iterateItems(dic, items);
        }
        return items;
    }
}
