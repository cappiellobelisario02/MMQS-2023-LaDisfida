/*
 * $Id: PdfCopyFieldsImp.java 4065 2009-09-16 23:09:11Z psoares33 $
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

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.exceptions.IllegalReferencePointerException;
import com.lowagie.text.pdf.AcroFields.Item;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * @author psoares
 */
class PdfCopyFieldsImp extends PdfWriter {

    protected static final Map<PdfName, Integer> widgetKeys = new HashMap<>();
    protected static final Map<PdfName, Integer> fieldKeys = new HashMap<>();
    private static final PdfName iTextTag = new PdfName("_iTextTag_");
    private static final Integer ZERO = 0;

    static {
        Integer one = 1;
        widgetKeys.put(PdfName.SUBTYPE, one);
        widgetKeys.put(PdfName.CONTENTS, one);
        widgetKeys.put(PdfName.RECT, one);
        widgetKeys.put(PdfName.NM, one);
        widgetKeys.put(PdfName.M, one);
        widgetKeys.put(PdfName.F, one);
        widgetKeys.put(PdfName.BS, one);
        widgetKeys.put(PdfName.BORDER, one);
        widgetKeys.put(PdfName.AP, one);
        widgetKeys.put(PdfName.AS, one);
        widgetKeys.put(PdfName.C, one);
        widgetKeys.put(PdfName.A, one);
        widgetKeys.put(PdfName.STRUCTPARENT, one);
        widgetKeys.put(PdfName.OC, one);
        widgetKeys.put(PdfName.H, one);
        widgetKeys.put(PdfName.MK, one);
        widgetKeys.put(PdfName.DA, one);
        widgetKeys.put(PdfName.Q, one);
        fieldKeys.put(PdfName.AA, one);
        fieldKeys.put(PdfName.FT, one);
        fieldKeys.put(PdfName.TU, one);
        fieldKeys.put(PdfName.TM, one);
        fieldKeys.put(PdfName.FF, one);
        fieldKeys.put(PdfName.V, one);
        fieldKeys.put(PdfName.DV, one);
        fieldKeys.put(PdfName.DS, one);
        fieldKeys.put(PdfName.RV, one);
        fieldKeys.put(PdfName.OPT, one);
        fieldKeys.put(PdfName.MAXLEN, one);
        fieldKeys.put(PdfName.TI, one);
        fieldKeys.put(PdfName.I, one);
        fieldKeys.put(PdfName.LOCK, one);
        fieldKeys.put(PdfName.SV, one);
    }

    private final List<PdfReader> readers = new ArrayList<>();
    private final Map<PdfReader, IntHashtable> pages2intrefs = new HashMap<>();
    private final Map<PdfReader, IntHashtable> visited = new HashMap<>();
    private final List<Object> calculationOrder = new ArrayList<>();
    Map<PdfReader, IntHashtable> readers2intrefs = new HashMap<>();
    List<AcroFields> fields = new ArrayList<>();
    RandomAccessFileOrArray file;
    Map<String, Object> fieldTree = new HashMap<>();
    List<PdfIndirectReference> pageRefs = new ArrayList<>();
    List<PdfDictionary> pageDics = new ArrayList<>();
    PdfDictionary resources = new PdfDictionary();
    PdfDictionary form;
    boolean closing = false;
    Document nd;
    private Map<PdfArray, List<Integer>> tabOrder;
    private List<Object> calculationOrderRefs;
    private boolean hasSignature;

    PdfCopyFieldsImp(OutputStream os) throws DocumentException {
        this(os, '\0');
    }

    PdfCopyFieldsImp(OutputStream os, char pdfVersion) throws DocumentException {
        super(new PdfDocument(), os);
        pdf.addWriter(this);
        if (pdfVersion != 0) {
            super.setPdfVersion(pdfVersion);
        }
        nd = new Document();
        nd.addDocListener(pdf);
    }

    private static String getCOName(PRIndirectReference ref) {
        StringBuilder name = new StringBuilder();
        while (ref != null) {
            PdfObject obj = PdfReader.getPdfObject(ref);
            if (obj == null || obj.getTypeImpl() != PdfObject.DICTIONARY) {
                break;
            }
            PdfDictionary dic = (PdfDictionary) obj;
            PdfString t = dic.getAsString(PdfName.T);
            if (t != null) {
                name.insert(0, t.toUnicodeString() + ".");
            }
            ref = (PRIndirectReference) dic.get(PdfName.PARENT);
        }
        if (name.toString().endsWith(".")) {
            name = new StringBuilder(name.substring(0, name.length() - 1));
        }
        return name.toString();
    }

    void addDocument(PdfReader reader, List<Integer> pagesToKeep) throws DocumentException, IOException {
        if (!readers2intrefs.containsKey(reader) && reader.isTampered()) {
            throw new DocumentException(MessageLocalization.getComposedMessage("the.document.was.reused"));
        }
        reader = new PdfReader(reader);
        reader.selectPages(pagesToKeep);
        if (reader.getNumberOfPages() == 0) {
            return;
        }
        reader.setTampered(false);
        addDocument(reader);
    }

    void addDocument(PdfReader reader) throws DocumentException, IOException {
        if (!reader.isOpenedWithFullPermissions()) {
            throw new BadPasswordException();
        }
        openDoc();
        if (readers2intrefs.containsKey(reader)) {
            reader = new PdfReader(reader);
        } else {
            if (reader.isTampered()) {
                throw new DocumentException(MessageLocalization.getComposedMessage("the.document.was.reused"));
            }
            reader.consolidateNamedDestinations();
            reader.setTampered(true);
        }
        reader.shuffleSubsetNames();
        readers2intrefs.put(reader, new IntHashtable());
        readers.add(reader);
        int len = reader.getNumberOfPages();
        IntHashtable refs = new IntHashtable();
        for (int p = 1; p <= len; ++p) {
            refs.put(reader.getPageOrigRef(p).getNumber(), 1);
            reader.releasePage(p);
        }
        pages2intrefs.put(reader, refs);
        visited.put(reader, new IntHashtable());
        fields.add(reader.getAcroFields());
        updateCalculationOrder(reader);
    }

    /**
     * @since 2.1.5; before 2.1.5 the method was private
     */
    protected void updateCalculationOrder(PdfReader reader) {
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary acro = catalog.getAsDict(PdfName.ACROFORM);
        if (acro == null) {
            return;
        }
        PdfArray co = acro.getAsArray(PdfName.CO);
        if (co == null || co.isEmpty()) {
            return;
        }
        AcroFields af = reader.getAcroFields();
        for (int k = 0; k < co.size(); ++k) {
            PdfObject obj = co.getPdfObject(k);
            if (obj == null || !obj.isIndirect()) {
                continue;
            }
            String name = getCOName((PRIndirectReference) obj);
            if (af.getFieldItem(name) == null) {
                continue;
            }
            name = "." + name;
            if (calculationOrder.contains(name)) {
                continue;
            }
            calculationOrder.add(name);
        }
    }

    private void propagate(PdfObject obj, boolean restricted) {
        if (obj == null || obj instanceof PdfIndirectReference) {
            return;
        }

        switch (obj.getTypeImpl()) {
            case PdfObject.DICTIONARY, PdfObject.STREAM:
                propagateDictionary((PdfDictionary) obj, restricted);
                break;
            case PdfObject.ARRAY:
                propagateArray((PdfArray) obj, restricted);
                break;
            case PdfObject.INDIRECT:
                handleIndirectReference();
                break;
            default:
                handleUnexpectedType(obj);
                break;
        }
    }

    private void propagateDictionary(PdfDictionary dic, boolean restricted) {
        for (PdfName key : dic.getKeys()) {
            if (restricted && isRestrictedKey(key)) {
                continue;
            }
            propagateObject(dic.get(key), restricted);
        }
    }

    private boolean isRestrictedKey(PdfName key) {
        return key.equals(PdfName.PARENT) || key.equals(PdfName.KIDS);
    }

    private void propagateArray(PdfArray array, boolean restricted) {
        for (PdfObject ob : array.getElements()) {
            propagateObject(ob, restricted);
        }
    }

    private void propagateObject(PdfObject ob, boolean restricted) {
        if (ob != null && ob.isIndirect()) {
            PRIndirectReference ind = (PRIndirectReference) ob;
            if (!isVisited(ind) && !isPage(ind) && isValidReference(ind)) {
                propagate(PdfReader.getPdfObjectRelease(ind), restricted);
            }
        } else {
            propagate(ob, restricted);
        }
    }

    private boolean isValidReference(PRIndirectReference ind) {
        return ind.getReader().getPdfObject(ind.getNumber()) != null;
    }

    private void handleIndirectReference() {
        throw new IllegalReferencePointerException(
                MessageLocalization.getComposedMessage("reference.pointing.to.reference"));
    }

    private void handleUnexpectedType(PdfObject obj) {
        throw new IllegalArgumentException("Unexpected PdfObject getTypeImpl: " + obj.getTypeImpl());
    }


    private void adjustTabOrder(PdfArray annots, PdfIndirectReference ind, PdfNumber nn) {
        int v = nn.intValue();
        List<Integer> t = tabOrder.get(annots);
        if (t == null) {
            t = new ArrayList<>();
            int size = annots.size() - 1;
            for (int k = 0; k < size; ++k) {
                t.add(ZERO);
            }
            t.add(v);
            tabOrder.put(annots, t);
            annots.add(ind);
        } else {
            int size = t.size() - 1;
            for (int k = size; k >= 0; --k) {
                if (t.get(k) <= v) {
                    t.add(k + 1, v);
                    annots.add(k + 1, ind);
                    size = -2;
                    break;
                }
            }
            if (size != -2) {
                t.add(0, v);
                annots.add(0, ind);
            }
        }
    }

    protected PdfArray branchForm(Map<String, Object> level, PdfIndirectReference parent, String fname)
            throws IOException {
        PdfArray arr = new PdfArray();
        for (Map.Entry<String, Object> entry : level.entrySet()) {
            String name = entry.getKey();
            PdfIndirectReference ind = getPdfIndirectReference();
            PdfDictionary dic = createDictionary(parent, name);
            String fname2 = fname + "." + name;
            updateCalculationOrder(fname2, ind);

            Object obj = entry.getValue();
            if (obj instanceof Map) {
                handleMapCase((Map<String, Object>) obj, dic, ind, fname2, arr);
            } else {
                handleListCase((List<?>) obj, dic, ind, arr);
            }
        }
        return arr;
    }

    private PdfDictionary createDictionary(PdfIndirectReference parent, String name) {
        PdfDictionary dic = new PdfDictionary();
        if (parent != null) {
            dic.put(PdfName.PARENT, parent);
        }
        dic.put(PdfName.T, new PdfString(name, PdfObject.TEXT_UNICODE));
        return dic;
    }

    private void updateCalculationOrder(String fname2, PdfIndirectReference ind) {
        int coidx = calculationOrder.indexOf(fname2);
        if (coidx >= 0) {
            calculationOrderRefs.set(coidx, ind);
        }
    }

    private void handleMapCase(Map<String, Object> map, PdfDictionary dic, PdfIndirectReference ind, String fname2, PdfArray arr) throws IOException {
        dic.put(PdfName.KIDS, branchForm(map, ind, fname2));
        arr.add(ind);
        addToBody(dic, ind);
    }

    private void handleListCase(List<?> list, PdfDictionary dic, PdfIndirectReference ind, PdfArray arr) throws IOException {
        dic.mergeDifferent((PdfDictionary) list.get(0));
        if (list.size() == 3) {
            handleThreeElementList(list, dic, ind);
        } else {
            handleMultipleElementsList(list, dic, ind);
        }
        arr.add(ind);
        addToBody(dic, ind);
        propagate(dic, false);
    }

    private void handleThreeElementList(List<?> list, PdfDictionary dic, PdfIndirectReference ind) {
        dic.mergeDifferent((PdfDictionary) list.get(2));
        int page = (Integer) list.get(1);
        PdfDictionary pageDic = pageDics.get(page - 1);
        PdfArray annots = getOrCreateAnnotsArray(pageDic);
        PdfNumber nn = (PdfNumber) dic.get(iTextTag);
        dic.remove(iTextTag);
        adjustTabOrder(annots, ind, nn);
    }

    private void handleMultipleElementsList(List<?> list, PdfDictionary dic, PdfIndirectReference ind) throws IOException {
        PdfArray kids = new PdfArray();
        for (int k = 1; k < list.size(); k += 2) {
            int page = (Integer) list.get(k);
            PdfDictionary pageDic = pageDics.get(page - 1);
            PdfArray annots = getOrCreateAnnotsArray(pageDic);
            PdfDictionary widget = createWidget(list, k, ind);
            PdfIndirectReference wref = addToBody(widget).getIndirectReference();
            adjustTabOrder(annots, wref, (PdfNumber) widget.get(iTextTag));
            kids.add(wref);
            propagate(widget, false);
        }
        dic.put(PdfName.KIDS, kids);
    }

    private PdfArray getOrCreateAnnotsArray(PdfDictionary pageDic) {
        PdfArray annots = pageDic.getAsArray(PdfName.ANNOTS);
        if (annots == null) {
            annots = new PdfArray();
            pageDic.put(PdfName.ANNOTS, annots);
        }
        return annots;
    }

    private PdfDictionary createWidget(List<?> list, int k, PdfIndirectReference ind) {
        PdfDictionary widget = new PdfDictionary();
        widget.merge((PdfDictionary) list.get(k + 1));
        widget.put(PdfName.PARENT, ind);
        widget.remove(iTextTag);
        return widget;
    }


    protected void createAcroForms() throws IOException {
        if (fieldTree.isEmpty()) {
            return;
        }
        form = new PdfDictionary();
        form.put(PdfName.DR, resources);
        propagate(resources, false);
        form.put(PdfName.DA, new PdfString("/Helv 0 Tf 0 g "));
        tabOrder = new HashMap<>();
        calculationOrderRefs = new ArrayList<>(calculationOrder);
        form.put(PdfName.FIELDS, branchForm(fieldTree, null, ""));
        if (hasSignature) {
            form.put(PdfName.SIGFLAGS, new PdfNumber(3));
        }
        PdfArray co = new PdfArray();
        for (Object obj : calculationOrderRefs) {
            if (obj instanceof PdfIndirectReference pdfIndirectReference) {
                co.add(pdfIndirectReference);
            }
        }
        if (!co.isEmpty()) {
            form.put(PdfName.CO, co);
        }
    }

    @Override
    public void close() {
        if (closing) {
            super.close();
            return;
        }
        closing = true;
        try {
            closeIt();
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Creates the new PDF by merging the fields and forms.
     */
    protected void closeIt() throws IOException {
        for (PdfReader reader : readers) {
            reader.removeFields();
        }
        for (PdfReader reader : readers) {
            for (int page = 1; page <= reader.getNumberOfPages(); ++page) {
                pageRefs.add(getNewReference(reader.getPageOrigRef(page)));
                pageDics.add(reader.getPageN(page));
            }
        }
        mergeFields();
        createAcroForms();
        for (PdfReader reader : readers) {
            for (int page = 1; page <= reader.getNumberOfPages(); ++page) {
                PdfDictionary dic = reader.getPageN(page);
                PdfIndirectReference pageRef = getNewReference(reader.getPageOrigRef(page));
                PdfIndirectReference parent = root.addPageRef(pageRef);
                dic.put(PdfName.PARENT, parent);
                propagate(dic, false);
            }
        }
        for (Map.Entry<PdfReader, IntHashtable> entry : readers2intrefs.entrySet()) {
            PdfReader reader = entry.getKey();
            try {
                file = reader.getSafeFile();
                file.reOpen();
                IntHashtable t = entry.getValue();
                int[] keys = t.toOrderedKeys();
                for (int key : keys) {
                    PRIndirectReference ref = new PRIndirectReference(reader, key);
                    addToBody(PdfReader.getPdfObjectRelease(ref), t.get(key));
                }
            } catch (IOException ioe) {
                logger.severe("IOException while processing file: " + ioe.getMessage());
                throw ioe; // Re-throw if needed to alert the calling method
            } catch (Exception e) {
                logger.warning("Unexpected exception: " + e.getMessage());
            } finally {
                try {
                    if (file != null) {
                        file.close();
                    }
                    reader.close();
                } catch (IOException ioe) {
                    logger.warning("IOException during file or reader close: " + ioe.getMessage());
                }
            }
        }
        pdf.close();
    }


    void addPageOffsetToField(Map<String, Item> fd, int pageOffset) {
        if (pageOffset == 0) {
            return;
        }
        for (Item item : fd.values()) {
            for (int k = 0; k < item.size(); ++k) {
                int p = item.getPage(k);
                item.forcePage(k, p + pageOffset);
            }
        }
    }

    void createWidgets(List<Object> list, Item item) {
        for (int k = 0; k < item.size(); ++k) {
            list.add(item.getPage(k)); // add an Integer
            PdfDictionary merged = item.getMerged(k);
            PdfObject dr = merged.get(PdfName.DR);
            if (dr != null) {
                PdfFormField.mergeResources(resources, (PdfDictionary) PdfReader.getPdfObject(dr));
            }
            PdfDictionary widget = new PdfDictionary();
            for (PdfName key : merged.getKeys()) {
                if (widgetKeys.containsKey(key)) {
                    widget.put(key, merged.get(key));
                }
            }
            widget.put(iTextTag, new PdfNumber(item.getTabOrder(k) + 1));
            list.add(widget); // add a PdfDictionary
        }
    }

    void mergeField(String name, Item item) {
        Map<String, Object> map = fieldTree;
        StringTokenizer tk = new StringTokenizer(name, ".");

        if (!tk.hasMoreTokens()) {
            return;
        }

        while (true) {
            String token = tk.nextToken();
            Object obj = map.get(token);

            if (tk.hasMoreTokens()) {
                map = getOrCreateSubMap(map, token, obj);
                if (map == null) {
                    return;
                }
            } else {
                handleFinalToken(map, token, obj, item);
                return;
            }
        }
    }

    private Map<String, Object> getOrCreateSubMap(Map<String, Object> map, String token, Object obj) {
        if (obj == null) {
            Map<String, Object> newMap = new HashMap<>();
            map.put(token, newMap);
            return newMap;
        } else if (obj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> castMap = (Map<String, Object>) obj;
            return castMap;
        } else {
            return new HashMap<>();
        }
    }

    private void handleFinalToken(Map<String, Object> map, String token, Object obj, Item item) {
        if (obj instanceof Map) {
            return;
        }

        PdfDictionary merged = item.getMerged(0);
        if (obj == null) {
            addNewField(map, token, merged, item);
        } else {
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) obj;
            PdfDictionary field = (PdfDictionary) list.get(0);
            PdfName type1 = (PdfName) field.get(PdfName.FT);
            PdfName type2 = (PdfName) merged.get(PdfName.FT);

            if (type1 == null || !type1.equals(type2)) {
                return;
            }

            if (!isValidFieldCombination(type1, field, merged)) {
                return;
            }

            createWidgets(list, item);
        }
    }

    private void addNewField(Map<String, Object> map, String token, PdfDictionary merged, Item item) {
        PdfDictionary field = new PdfDictionary();
        if (PdfName.SIG.equals(merged.get(PdfName.FT))) {
            hasSignature = true;
        }
        for (PdfName key : merged.getKeys()) {
            if (fieldKeys.containsKey(key)) {
                field.put(key, merged.get(key));
            }
        }
        List<Object> list = new ArrayList<>();
        list.add(field);
        createWidgets(list, item);
        map.put(token, list);
    }

    private boolean isValidFieldCombination(PdfName type1, PdfDictionary field, PdfDictionary merged) {
        int flag1 = getFieldFlag(field, PdfName.FF);
        int flag2 = getFieldFlag(merged, PdfName.FF);

        if (type1.equals(PdfName.BTN)) {
            if (((flag1 ^ flag2) & PdfFormField.FF_PUSHBUTTON) != 0) {
                return false;
            }
            return (flag1 & PdfFormField.FF_PUSHBUTTON) != 0 || ((flag1 ^ flag2) & PdfFormField.FF_RADIO) == 0;
        } else
            return !type1.equals(PdfName.CH) || ((flag1 ^ flag2) & PdfFormField.FF_COMBO) == 0;
    }

    private int getFieldFlag(PdfDictionary dictionary, PdfName flagName) {
        PdfObject flagObj = dictionary.get(flagName);
        if (flagObj != null && flagObj.isNumber()) {
            return ((PdfNumber) flagObj).intValue();
        }
        return 0;
    }


    void mergeWithMaster(Map<String, Item> fd) {
        for (Map.Entry<String, Item> entry : fd.entrySet()) {
            String name = entry.getKey();
            mergeField(name, entry.getValue());
        }
    }

    void mergeFields() {
        int pageOffset = 0;
        for (int k = 0; k < fields.size(); ++k) {
            Map<String, Item> fd = fields.get(k).getAllFields();
            addPageOffsetToField(fd, pageOffset);
            mergeWithMaster(fd);
            pageOffset += readers.get(k).getNumberOfPages();
        }
    }

    @Override
    public PdfIndirectReference getPageReference(int page) {
        return pageRefs.get(page - 1);
    }

    @Override
    protected PdfDictionary getCatalog(PdfIndirectReference rootObj) {
        try {
            PdfDictionary cat = pdf.getCatalog(rootObj);
            if (form != null) {
                PdfIndirectReference ref = addToBody(form).getIndirectReference();
                cat.put(PdfName.ACROFORM, ref);
            }
            return cat;
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    protected PdfIndirectReference getNewReference(PRIndirectReference ref) {
        return new PdfIndirectReference(0, getNewObjectNumber(ref.getReader(), ref.getNumber()));
    }

    @Override
    protected int getNewObjectNumber(PdfReader reader, int number) {
        IntHashtable refs = readers2intrefs.get(reader);
        int n = refs.get(number);
        if (n == 0) {
            n = getIndirectReferenceNumber();
            refs.put(number, n);
        }
        return n;
    }

    /**
     * Sets a reference to "visited" in the copy process.
     *
     * @param ref the reference that needs to be set to "visited"
     * @return true if the reference was set to visited
     */
    protected boolean setVisited(PRIndirectReference ref) {
        IntHashtable refs = visited.get(ref.getReader());
        if (refs != null) {
            return (refs.put(ref.getNumber(), 1) != 0);
        } else {
            return false;
        }
    }

    /**
     * Checks if a reference has already been "visited" in the copy process.
     *
     * @param ref the reference that needs to be checked
     * @return true if the reference was already visited
     */
    protected boolean isVisited(PRIndirectReference ref) {
        IntHashtable refs = visited.get(ref.getReader());
        if (refs != null) {
            return refs.containsKey(ref.getNumber());
        } else {
            return false;
        }
    }

    protected boolean isVisited(PdfReader reader, int number) {
        IntHashtable refs = readers2intrefs.get(reader);
        return refs.containsKey(number);
    }

    /**
     * Checks if a reference refers to a page object.
     *
     * @param ref the reference that needs to be checked
     * @return true is the reference refers to a page object.
     */
    protected boolean isPage(PRIndirectReference ref) {
        IntHashtable refs = pages2intrefs.get(ref.getReader());
        if (refs != null) {
            return refs.containsKey(ref.getNumber());
        } else {
            return false;
        }
    }

    @Override
    RandomAccessFileOrArray getReaderFile() {
        return file;
    }

    public void openDoc() {
        if (!nd.isOpen()) {
            nd.open();
        }
    }
}
