/*
 * Copyright 2003-2005 by Paulo Soares.
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

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.AnnotationException;
import com.lowagie.text.exceptions.ColorParseException;
import com.lowagie.text.exceptions.FontProcessingException;
import com.lowagie.text.exceptions.InvalidColorValueException;
import com.lowagie.text.exceptions.NoReaderException;
import com.lowagie.text.exceptions.ReadOnlyAcroFieldsException;
import com.lowagie.text.exceptions.ReadOnlyException;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.fop.pdf.PDFFilterException;
import org.w3c.dom.Node;


/**
 * Query and change fields in existing documents either by method calls or by FDF merging.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class AcroFields {

    public static final String BGCOLOR = "bgcolor";
    public static final String THIS_ACROFIELDS_INSTANCE_IS_READ_ONLY = "this.acrofields.instance.is.read.only";
    static Logger logger = Logger.getLogger(AcroFields.class.getName());

    public static final int DA_FONT = 0;
    public static final int DA_SIZE = 1;
    public static final int DA_COLOR = 2;
    /**
     * A field type invalid or not found.
     */
    public static final int FIELD_TYPE_NONE = 0;
    /**
     * A field type.
     */
    public static final int FIELD_TYPE_PUSHBUTTON = 1;
    /**
     * A field type.
     */
    public static final int FIELD_TYPE_CHECKBOX = 2;
    /**
     * A field type.
     */
    public static final int FIELD_TYPE_RADIOBUTTON = 3;
    /**
     * A field type.
     */
    public static final int FIELD_TYPE_TEXT = 4;
    /**
     * A field type.
     */
    public static final int FIELD_TYPE_LIST = 5;
    /**
     * A field type.
     */
    public static final int FIELD_TYPE_COMBO = 6;
    /**
     * A field type.
     */
    public static final int FIELD_TYPE_SIGNATURE = 7;
    private static final HashMap<String, String[]> stdFieldFontNames = new HashMap<>();
    private static final PdfName[] buttonRemove = {PdfName.MK, PdfName.F, PdfName.FF, PdfName.Q, PdfName.BS,
            PdfName.BORDER};

    public static final String UNI_KS_UCS_2_H = "UniKS-UCS2-H";

    static {
        stdFieldFontNames.put("CoBO", new String[]{"Courier-BoldOblique"});
        stdFieldFontNames.put("CoBo", new String[]{"Courier-Bold"});
        stdFieldFontNames.put("CoOb", new String[]{"Courier-Oblique"});
        stdFieldFontNames.put("Cour", new String[]{"Courier"});
        stdFieldFontNames.put("HeBO", new String[]{"Helvetica-BoldOblique"});
        stdFieldFontNames.put("HeBo", new String[]{"Helvetica-Bold"});
        stdFieldFontNames.put("HeOb", new String[]{"Helvetica-Oblique"});
        stdFieldFontNames.put("Helv", new String[]{"Helvetica"});
        stdFieldFontNames.put("Symb", new String[]{"Symbol"});
        stdFieldFontNames.put("TiBI", new String[]{"Times-BoldItalic"});
        stdFieldFontNames.put("TiBo", new String[]{"Times-Bold"});
        stdFieldFontNames.put("TiIt", new String[]{"Times-Italic"});
        stdFieldFontNames.put("TiRo", new String[]{"Times-Roman"});
        stdFieldFontNames.put("ZaDb", new String[]{"ZapfDingbats"});
        stdFieldFontNames.put("HySm", new String[]{"HYSMyeongJo-Medium", UNI_KS_UCS_2_H});
        stdFieldFontNames.put("HyGo", new String[]{"HYGoThic-Medium", UNI_KS_UCS_2_H});
        stdFieldFontNames.put("KaGo", new String[]{"HeiseiKakuGo-W5", UNI_KS_UCS_2_H});
        stdFieldFontNames.put("KaMi", new String[]{"HeiseiMin-W3", "UniJIS-UCS2-H"});
        stdFieldFontNames.put("MHei", new String[]{"MHei-Medium", "UniCNS-UCS2-H"});
        stdFieldFontNames.put("MSun", new String[]{"MSung-Light", "UniCNS-UCS2-H"});
        stdFieldFontNames.put("STSo", new String[]{"STSong-Light", "UniGB-UCS2-H"});
    }

    private final Map<Integer, BaseFont> extensionFonts = new HashMap<>();
    private final Map<String, BaseFont> localFonts = new HashMap<>();
    PdfReader reader;
    PdfWriter writer;
    private Map<String, Item> fields;
    private int topFirst;
    private Map<String, int[]> sigNames;
    private boolean append;
    private XfaForm xfa;
    private boolean lastWasString;
    /**
     * Holds value of property generateAppearances.
     */
    private boolean generateAppearances = true;
    private float extraMarginLeft;
    private float extraMarginTop;
    private List<BaseFont> substitutionFonts;
    /**
     * Holds value of property totalRevisions.
     */
    private int totalRevisions;
    /**
     * Holds value of property fieldCache.
     *
     * @since 2.1.5  this used to be a HashMap
     */
    private Map<String, BaseField> fieldCache;

    AcroFields(PdfReader reader, PdfWriter writer) {
        this.reader = reader;
        this.writer = writer;
        try {
            xfa = new XfaForm(reader);
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
        if (writer instanceof PdfStamperImp stamperImp) {
            append = stamperImp.isAppend();
        }
        fill();
    }

    public static Object[] splitDAelements(String da) {
        try {
            PRTokeniser tk = createPRTokeniser(PdfEncodings.convertToBytes(da, null));
            List<String> stack = new ArrayList<>();
            Object[] ret = new Object[3];

            while (tk.nextToken()) {
                if (tk.getTokenType() == PRTokeniser.TK_COMMENT) {
                    continue;
                }

                if (tk.getTokenType() == PRTokeniser.TK_OTHER) {
                    String operator = tk.getStringValue();
                    processOperator(operator, stack, ret);
                    stack.clear();
                } else {
                    stack.add(tk.getStringValue());
                }
            }

            return ret;
        } catch (IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }

    private static void processOperator(String operator, List<String> stack, Object[] ret) {
        switch (operator) {
            case "Tf":
                handleTf(stack, ret);
                break;
            case "g":
                handleG(stack, ret);
                break;
            case "rg":
                handleRg(stack, ret);
                break;
            case "k":
                handleK(stack, ret);
                break;
            default:
                break;
        }
    }

    private static void handleTf(List<String> stack, Object[] ret) {
        if (stack.size() >= 2) {
            ret[DA_FONT] = stack.get(stack.size() - 2);
            ret[DA_SIZE] = Float.parseFloat(stack.get(stack.size() - 1));
        }
    }

    private static void handleG(List<String> stack, Object[] ret) {
        if (!stack.isEmpty()) {
            float gray = Float.parseFloat(stack.get(stack.size() - 1));
            if (gray != 0) {
                ret[DA_COLOR] = new GrayColor(gray);
            }
        }
    }

    private static void handleRg(List<String> stack, Object[] ret) {
        if (stack.size() >= 3) {
            float red = Float.parseFloat(stack.get(stack.size() - 3));
            float green = Float.parseFloat(stack.get(stack.size() - 2));
            float blue = Float.parseFloat(stack.get(stack.size() - 1));
            ret[DA_COLOR] = new Color(red, green, blue);
        }
    }

    private static void handleK(List<String> stack, Object[] ret) {
        if (stack.size() >= 4) {
            float cyan = Float.parseFloat(stack.get(stack.size() - 4));
            float magenta = Float.parseFloat(stack.get(stack.size() - 3));
            float yellow = Float.parseFloat(stack.get(stack.size() - 2));
            float black = Float.parseFloat(stack.get(stack.size() - 1));
            ret[DA_COLOR] = new CMYKColor(cyan, magenta, yellow, black);
        }
    }


    private static PRTokeniser createPRTokeniser(byte[] da) {
        PRTokeniser tk = null;
        try {
            tk = new PRTokeniser(PdfEncodings.convertToBytes(Arrays.toString(da), null));
        } catch (Exception e) {
            logger.info("PRTokeniser error: " + e.getMessage());
        } finally {
            closePRTokeniser(tk);
        }
        return tk;
    }

    // Method to handle PRTokeniser closing
    private static void closePRTokeniser(PRTokeniser tk) {
        if (tk != null) {
            try {
                tk.close();
            } catch (IOException e) {
                logger.info("Error in PRTokeniser closing: " + e.getMessage());
            }
        }
    }

    /**
     * Parses and converts colors from PDF to standard AWT Colors.
     *
     * @param pdfColor an array of colors
     * @return AWT-Color
     */
    public static Color parseColor(PdfArray pdfColor) {

        //Check for no color -> thus transparent
        if (pdfColor != null && !pdfColor.isEmpty()) {
            if (pdfColor.size() == 1) {
                PdfNumber grey = pdfColor.getAsNumber(0);
                if (grey != null) {
                    return new GrayColor(grey.floatValue());
                }
            } else if (pdfColor.size() == 3) {
                PdfNumber red = pdfColor.getAsNumber(0);
                PdfNumber green = pdfColor.getAsNumber(1);
                PdfNumber blue = pdfColor.getAsNumber(2);
                return new Color(red.floatValue(), green.floatValue(), blue.floatValue());

            } else if (pdfColor.size() == 4) {
                PdfNumber c = pdfColor.getAsNumber(0);
                PdfNumber m = pdfColor.getAsNumber(1);
                PdfNumber y = pdfColor.getAsNumber(2);
                PdfNumber k = pdfColor.getAsNumber(3);

                if (c != null && m != null && y != null && k != null) {
                    return new CMYKColor(c.floatValue(), m.floatValue(), y.floatValue(), k.floatValue());
                }
            } else {
                throw new ColorParseException(
                        "Error extracting color: " + pdfColor + " since the color array is too long: "
                                + pdfColor.length());
            }
        }
        return null;
    }

    public Color createColor(Float red, Float green, Float blue) {
        if (red == null || green == null || blue == null) {
            throw new InvalidColorValueException("Red, green, and blue values must all be provided.");
        }
        return new Color(red, green, blue);
    }

    void fill() {
        fields = new HashMap<>();
        PdfDictionary top = getAcroFormCatalog();
        if (top == null) return;

        PdfArray arrfds = getFieldsArray(top);
        if (arrfds == null || arrfds.isEmpty()) return;

        processPages();
        processInvisibleSignatures(top, arrfds);
    }

    private PdfDictionary getAcroFormCatalog() {
        return (PdfDictionary) PdfReader.getPdfObjectReleaseNullConverting(
                reader.getCatalog().get(PdfName.ACROFORM));
    }

    private PdfArray getFieldsArray(PdfDictionary top) {
        return (PdfArray) PdfReader.getPdfObjectRelease(top.get(PdfName.FIELDS));
    }

    private void processPages() {
        for (int k = 1; k <= reader.getNumberOfPages(); ++k) {
            PdfDictionary page = reader.getPageNRelease(k);
            PdfArray annots = getAnnotations(page);
            if (annots == null) continue;

            for (int j = 0; j < annots.size(); ++j) {
                processAnnotation(annots.getAsDict(j), annots.getAsIndirectObject(j), k, j);
            }
        }
    }

    private PdfArray getAnnotations(PdfDictionary page) {
        Object o = PdfReader.getPdfObjectRelease(page.get(PdfName.ANNOTS), page);
        return (o instanceof PdfArray array) ? array : null;
    }

    private void processAnnotation(PdfDictionary annot, PdfIndirectReference indirectObject, int pageNum, int tabOrder) {
        if (annot == null || !PdfName.WIDGET.equals(annot.getAsName(PdfName.SUBTYPE))) {
            PdfReader.releaseLastXrefPartial(indirectObject);
            return;
        }

        PdfDictionary dic = new PdfDictionary();
        dic.putAll(annot);

        String name = extractName(annot);
        PdfObject value = extractValue(annot);

        Item item = fields.computeIfAbsent(name, n -> new Item(indirectObject));
        item.addValue((PdfDictionary) Objects.requireNonNullElse(value, annot));
        item.addWidget(annot);
        item.addWidgetRef(indirectObject); // must be a reference
        dic.mergeDifferent(getAcroFormCatalog());
        item.addMerged(dic);
        item.addPage(pageNum);
        item.addTabOrder(tabOrder);
    }

    private String extractName(PdfDictionary annot) {
        StringBuilder name = new StringBuilder();
        PdfDictionary parentAnnot = annot;
        while (parentAnnot != null) {
            PdfString t = parentAnnot.getAsString(PdfName.T);
            if (t != null) {
                name.insert(0, t.toUnicodeString() + ".");
            }
            parentAnnot = getParentAnnot(parentAnnot);
        }
        return !name.isEmpty() ? name.substring(0, name.length() - 1) : "";
    }

    private PdfDictionary getParentAnnot(PdfDictionary annot) {
        PdfIndirectReference asIndirectObject = annot.getAsIndirectObject(PdfName.PARENT);
        return (asIndirectObject != null) ? annot.getAsDict(PdfName.PARENT) : null;
    }

    private PdfObject extractValue(PdfDictionary annot) {
        return (annot.get(PdfName.V) != null) ? PdfReader.getPdfObjectRelease(annot.get(PdfName.V)) : null;
    }

    private void processInvisibleSignatures(PdfDictionary top, PdfArray arrfds) {
        PdfNumber sigFlags = top.getAsNumber(PdfName.SIGFLAGS);
        if (sigFlags == null || (sigFlags.intValue() & 1) != 1) return;

        for (int j = 0; j < arrfds.size(); ++j) {
            processInvisibleSignature(arrfds.getAsDict(j), arrfds.getAsIndirectObject(j));
        }
    }

    private void processInvisibleSignature(PdfDictionary annot, PdfIndirectReference annotRef) {
        if (annot == null || !PdfName.WIDGET.equals(annot.getAsName(PdfName.SUBTYPE))) {
            PdfReader.releaseLastXrefPartial(annotRef);
            return;
        }

        PdfArray kids = (PdfArray) PdfReader.getPdfObjectRelease(annot.get(PdfName.KIDS));
        if (kids != null) return;

        PdfDictionary dic = new PdfDictionary();
        dic.putAll(annot);
        PdfString t = annot.getAsString(PdfName.T);
        if (t == null) return;

        String name = t.toUnicodeString();
        if (fields.containsKey(name)) return;

        Item item = new Item(annotRef);
        fields.put(name, item);
        item.addValue(dic);
        item.addWidget(dic);
        item.addWidgetRef(annotRef); // must be a reference
        item.addMerged(dic);
        item.addPage(-1);
        item.addTabOrder(-1);
    }


    /**
     * Gets the list of appearance names. Use it to get the names allowed with radio and checkbox fields. If the /Opt
     * key exists the values will also be included. The name 'Off' may also be valid even if not returned in the list.
     *
     * @param fieldName the fully qualified field name
     * @return the list of names or <CODE>null</CODE> if the field does not exist
     */
    public String[] getAppearanceStates(String fieldName) {
        Item field = fields.get(fieldName);
        if (field == null) {
            return new String[0];
        }

        Set<String> names = new HashSet<>();
        extractNamesFromField(field, names);
        extractNamesFromWidgets(field, names);

        return names.toArray(new String[0]);
    }

    private void extractNamesFromField(Item field, Set<String> names) {
        PdfDictionary values = field.getValue(0);
        addNamesFromOptional(values, names);
    }

    private void addNamesFromOptional(PdfDictionary values, Set<String> names) {
        PdfString singleOpt = values.getAsString(PdfName.OPT);
        if (singleOpt != null) {
            names.add(singleOpt.toUnicodeString());
        } else {
            PdfArray multipleOpts = values.getAsArray(PdfName.OPT);
            if (multipleOpts != null) {
                for (int i = 0; i < multipleOpts.size(); ++i) {
                    PdfString optStr = multipleOpts.getAsString(i);
                    if (optStr != null) {
                        names.add(optStr.toUnicodeString());
                    }
                }
            }
        }
    }

    private void extractNamesFromWidgets(Item field, Set<String> names) {
        for (int i = 0; i < field.size(); ++i) {
            PdfDictionary widget = field.getWidget(i);
            PdfDictionary appearanceDict = widget.getAsDict(PdfName.AP);
            if (appearanceDict != null) {
                PdfDictionary normalAppearanceDict = appearanceDict.getAsDict(PdfName.N);
                if (normalAppearanceDict != null) {
                    addNamesFromAppearanceDict(normalAppearanceDict, names);
                }
            }
        }
    }

    private void addNamesFromAppearanceDict(PdfDictionary appearanceDict, Set<String> names) {
        for (Object key : appearanceDict.getKeys()) {
            String name = PdfName.decodeName(key.toString());
            names.add(name);
        }
    }


    /**
     * Returns the names of the N-appearance dictionaries
     *
     * @param fieldName name of the form field
     * @param idx       widget index
     * @return String[] of appearance names or null if the field can not be found
     */
    public String[] getAppearanceNames(String fieldName, int idx) {
        Item fd = this.fields.get(fieldName);
        if (fd == null) {
            return new String[0];
        }
        ArrayList<String> names = new ArrayList<>();

        PdfDictionary dic = fd.getWidget(idx);
        dic = dic.getAsDict(PdfName.AP);
        if (dic != null) {
            dic = dic.getAsDict(PdfName.N);
            if (dic != null) {

                for (PdfName pdfName : dic.getKeys()) {
                    String name = PdfName.decodeName(pdfName.toString());
                    if (!names.contains(name)) {
                        names.add(name);
                    }
                }
            }
        }
        return names.toArray(new String[0]);
    }

    private String[] getListOption(String fieldName, int idx) {
        Item fd = getFieldItem(fieldName);
        if (fd == null) {
            return new String[0];
        }
        PdfArray ar = fd.getMerged(0).getAsArray(PdfName.OPT);
        if (ar == null) {
            return new String[0];
        }
        String[] ret = new String[ar.size()];
        for (int k = 0; k < ar.size(); ++k) {
            PdfObject obj = ar.getDirectObject(k);
            try {
                if (obj.isArray()) {
                    obj = ((PdfArray) obj).getDirectObject(idx);
                }
                if (obj.isString()) {
                    ret[k] = ((PdfString) obj).toUnicodeString();
                } else {
                    ret[k] = obj.toString();
                }
            } catch (Exception e) {
                ret[k] = "";
            }
        }
        return ret;
    }

    /**
     * Gets the list of export option values from fields of type list or combo. If the field doesn't exist or the field
     * type is not list or combo it will return
     * <CODE>null</CODE>.
     *
     * @param fieldName the field name
     * @return the list of export option values from fields of type list or combo
     */
    public String[] getListOptionExport(String fieldName) {
        return getListOption(fieldName, 0);
    }

    /**
     * Gets the list of display option values from fields of type list or combo. If the field doesn't exist or the field
     * type is not list or combo it will return
     * <CODE>null</CODE>.
     *
     * @param fieldName the field name
     * @return the list of export option values from fields of type list or combo
     */
    public String[] getListOptionDisplay(String fieldName) {
        return getListOption(fieldName, 1);
    }

    /**
     * Sets the option list for fields of type list or combo. One of <CODE>exportValues</CODE> or
     * <CODE>displayValues</CODE> may be
     * <CODE>null</CODE> but not both. This method will only set the list but will not set the value or appearance. For
     * that, calling
     * <CODE>setField()</CODE> is required.
     * <p>
     * An example:
     * </p>
     * <PRE>
     * PdfReader pdf = new PdfReader("input.pdf"); PdfStamper stp = new PdfStamper(pdf, new
     * FileOutputStream("output.pdf")); AcroFields af = stp.getAcroFields(); af.setListOption("ComboBox", new
     * String[]{"a", "b", "c"}, new String[]{"first", "second", "third"}); af.setField("ComboBox", "b"); stp.close();
     * </PRE>
     *
     * @param fieldName     the field name
     * @param exportValues  the export values
     * @param displayValues the display values
     * @return <CODE>true</CODE> if the operation succeeded, <CODE>false</CODE> otherwise
     */
    public boolean setListOption(String fieldName, String[] exportValues, String[] displayValues) {
        if (exportValues == null && displayValues == null) {
            return false;
        }
        if (exportValues != null && displayValues != null && exportValues.length != displayValues.length) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("the.export.and.the.display.array.must.have.the.same.size"));
        }
        int ftype = getFieldType(fieldName);
        if (ftype != FIELD_TYPE_COMBO && ftype != FIELD_TYPE_LIST) {
            return false;
        }
        Item fd = fields.get(fieldName);
        String[] sing = null;
        if (exportValues == null) {
            sing = displayValues;
        } else if (displayValues == null) {
            sing = exportValues;
        }
        PdfArray opt = new PdfArray();
        if (sing != null) {
            for (String s : sing) {
                opt.add(new PdfString(s, PdfObject.TEXT_UNICODE));
            }
        } else {
            for (int k = 0; k < exportValues.length; ++k) {
                PdfArray a = new PdfArray();
                a.add(new PdfString(exportValues[k], PdfObject.TEXT_UNICODE));
                a.add(new PdfString(displayValues[k], PdfObject.TEXT_UNICODE));
                opt.add(a);
            }
        }
        fd.writeToAll(PdfName.OPT, opt, Item.WRITE_VALUE | Item.WRITE_MERGED);
        return true;
    }

    /**
     * Gets the field type. The type can be one of: <CODE>FIELD_TYPE_PUSHBUTTON</CODE>,
     * <CODE>FIELD_TYPE_CHECKBOX</CODE>, <CODE>FIELD_TYPE_RADIOBUTTON</CODE>,
     * <CODE>FIELD_TYPE_TEXT</CODE>, <CODE>FIELD_TYPE_LIST</CODE>,
     * <CODE>FIELD_TYPE_COMBO</CODE> or <CODE>FIELD_TYPE_SIGNATURE</CODE>.
     *
     * <p>If the field does not exist or is invalid it returns
     * <CODE>FIELD_TYPE_NONE</CODE>.
     *
     * @param fieldName the field name
     * @return the field type
     */
    public int getFieldType(String fieldName) {
        Item fd = getFieldItem(fieldName);
        if (fd == null) {
            return FIELD_TYPE_NONE;
        }
        PdfDictionary merged = fd.getMerged(0);
        PdfName type = merged.getAsName(PdfName.FT);
        if (type == null) {
            return FIELD_TYPE_NONE;
        }
        int ff = 0;
        PdfNumber ffo = merged.getAsNumber(PdfName.FF);
        if (ffo != null) {
            ff = ffo.intValue();
        }
        if (PdfName.BTN.equals(type)) {
            if ((ff & PdfFormField.FF_PUSHBUTTON) != 0) {
                return FIELD_TYPE_PUSHBUTTON;
            }
            if ((ff & PdfFormField.FF_RADIO) != 0) {
                return FIELD_TYPE_RADIOBUTTON;
            } else {
                return FIELD_TYPE_CHECKBOX;
            }
        } else if (PdfName.TX.equals(type)) {
            return FIELD_TYPE_TEXT;
        } else if (PdfName.CH.equals(type)) {
            if ((ff & PdfFormField.FF_COMBO) != 0) {
                return FIELD_TYPE_COMBO;
            } else {
                return FIELD_TYPE_LIST;
            }
        } else if (PdfName.SIG.equals(type)) {
            return FIELD_TYPE_SIGNATURE;
        }
        return FIELD_TYPE_NONE;
    }

    /**
     * Export the fields as a FDF.
     *
     * @param writer the FDF writer
     */
    public void exportAsFdf(FdfWriter writer) {
        for (Map.Entry<String, Item> entry : fields.entrySet()) {
            Item item = entry.getValue();
            String name = entry.getKey();
            PdfObject v = item.getMerged(0).get(PdfName.V);
            if (v == null) {
                continue;
            }
            String value = getField(name);
            if (lastWasString) {
                writer.setFieldAsString(name, value);
            } else {
                writer.setFieldAsName(name, value);
            }
        }
    }

    /**
     * Renames a field. Only the last part of the name can be renamed. For example, if the original field is "ab.cd.ef"
     * only the "ef" part can be renamed.
     *
     * @param oldName the old field name
     * @param newName the new field name
     * @return <CODE>true</CODE> if the renaming was successful, <CODE>false</CODE>
     * otherwise
     */
    public boolean renameField(String oldName, String newName) {
        int idx1 = oldName.lastIndexOf('.') + 1;
        int idx2 = newName.lastIndexOf('.') + 1;
        if (idx1 != idx2) {
            return false;
        }
        if (!oldName.substring(0, idx1).equals(newName.substring(0, idx2))) {
            return false;
        }
        if (fields.containsKey(newName)) {
            return false;
        }
        Item item = fields.get(oldName);
        if (item == null) {
            return false;
        }
        newName = newName.substring(idx2);
        PdfString ss = new PdfString(newName, PdfObject.TEXT_UNICODE);

        item.writeToAll(PdfName.T, ss, Item.WRITE_VALUE | Item.WRITE_MERGED);
        item.markUsed(this, Item.WRITE_VALUE);

        fields.remove(oldName);
        fields.put(newName, item);

        return true;
    }

    public void decodeGenericDictionary(PdfDictionary merged, BaseField tx) throws DocumentException {
        setTextProperties(merged, tx);
        setVisualProperties(merged, tx);
        setFlags(merged, tx);
        setMultilineAndMaxLength(merged, tx);
        setAlignment(merged, tx);
        setBorderStyles(merged, tx);
    }

    private void setTextProperties(PdfDictionary merged, BaseField tx) throws DocumentException {
        PdfString da = merged.getAsString(PdfName.DA);
        if (da != null) {
            Object[] dab = splitDAelements(da.toUnicodeString());
            setFontSizeAndColor(dab, tx);
            setFont(dab, merged, tx);
        }
    }

    private void setFontSizeAndColor(Object[] dab, BaseField tx) {
        if (dab[DA_SIZE] != null) {
            tx.setFontSize((Float) dab[DA_SIZE]);
        }
        if (dab[DA_COLOR] != null) {
            tx.setTextColor((Color) dab[DA_COLOR]);
        }
    }

    private void setFont(Object[] dab, PdfDictionary merged, BaseField tx) throws DocumentException {
        if (dab[DA_FONT] != null) {
            PdfDictionary dr = merged.getAsDict(PdfName.DR);
            if (dr != null) {
                PdfDictionary font = dr.getAsDict(PdfName.FONT);
                if (font != null) {
                    PdfObject po = font.get(new PdfName((String) dab[DA_FONT]));
                    handleFontObject(po, dr, tx, (String) dab[DA_FONT]);
                }
            }
        }
    }

    private void handleFontObject(PdfObject po, PdfDictionary dr, BaseField tx, String fontName) throws DocumentException {
        if (po != null && po.type() == PdfObject.INDIRECT) {
            PRIndirectReference por = (PRIndirectReference) po;
            adjustFontEncoding(dr, por);
            BaseFont bp = new DocumentFont(por);
            tx.setFont(bp);
            handleExtensionFont(por, tx);
        } else {
            setLocalOrStandardFont(fontName, tx);
        }
    }

    private void handleExtensionFont(PRIndirectReference por, BaseField tx) throws DocumentException {
        Integer porkey = por.getNumber();
        BaseFont porf = extensionFonts.get(porkey);
        if (porf == null && !extensionFonts.containsKey(porkey)) {
            loadExtensionFont(porkey, por);
        }
        if (tx instanceof TextField textField) {
            textField.setExtensionFont(porf);
        }
    }

    private void loadExtensionFont(Integer porkey, PRIndirectReference por) throws DocumentException {
        PdfDictionary fo = (PdfDictionary) PdfReader.getPdfObject(por);
        PdfDictionary fd = fo.getAsDict(PdfName.FONTDESCRIPTOR);
        if (fd != null) {
            PRStream prs = (PRStream) PdfReader.getPdfObject(fd.get(PdfName.FONTFILE2));
            if (prs == null) {
                prs = (PRStream) PdfReader.getPdfObject(fd.get(PdfName.FONTFILE3));
            }
            try {
                BaseFont porf = (prs != null) ? BaseFont.createFont("font.ttf", BaseFont.IDENTITY_H, true, false, PdfReader.getStreamBytes(prs), null) : null;
                extensionFonts.put(porkey, porf);
            } catch (IOException | PDFFilterException e) {
                extensionFonts.put(porkey, null);
            }
        }
    }

    private void setLocalOrStandardFont(String fontName, BaseField tx) {
        BaseFont bf = localFonts.get(fontName);
        if (bf == null) {
            String[] fn = stdFieldFontNames.get(fontName);
            if (fn != null) {
                try {
                    bf = BaseFont.createFont(fn[0], fn.length > 1 ? fn[1] : "winansi", false);
                    tx.setFont(bf);
                } catch (Exception ignored) {
                    // Empty on purpose
                }
            }
        } else {
            tx.setFont(bf);
        }
    }

    private void setVisualProperties(PdfDictionary merged, BaseField tx) {
        PdfDictionary mk = merged.getAsDict(PdfName.MK);
        if (mk != null) {
            tx.setBorderColor(getMKColor(mk.getAsArray(PdfName.BC)));
            tx.setBackgroundColor(getMKColor(mk.getAsArray(PdfName.BG)));
            PdfNumber rotation = mk.getAsNumber(PdfName.R);
            if (rotation != null) {
                tx.setRotation(rotation.intValue());
            }
        }
    }

    private void setFlags(PdfDictionary merged, BaseField tx) {
        PdfNumber nfl = merged.getAsNumber(PdfName.F);
        if (nfl != null) {
            int flags = nfl.intValue();
            if((flags & PdfAnnotation.FLAGS_PRINT) != 0){
                if((flags & PdfAnnotation.FLAGS_HIDDEN) != 0){
                    tx.setVisibility(BaseField.HIDDEN);
                }
                else{
                    tx.setVisibility(BaseField.VISIBLE);
                }
            } else{
                tx.setVisibility(BaseField.VISIBLE_BUT_DOES_NOT_PRINT);
            }
        }
    }

    private void setMultilineAndMaxLength(PdfDictionary merged, BaseField tx) {
        PdfNumber nfl = merged.getAsNumber(PdfName.FF);
        if (nfl != null && (nfl.intValue() & PdfFormField.FF_COMB) != 0) {
            PdfNumber maxLen = merged.getAsNumber(PdfName.MAXLEN);
            tx.setMaxCharacterLength(maxLen != null ? maxLen.intValue() : 0);
        }
    }

    private void setAlignment(PdfDictionary merged, BaseField tx) {
        PdfNumber nfl = merged.getAsNumber(PdfName.Q);
        if (nfl != null) {
            if(nfl.intValue() == PdfFormField.Q_CENTER){
                tx.setAlignment(Element.ALIGN_CENTER);
            }
            if(nfl.intValue() == PdfFormField.Q_RIGHT){
                tx.setAlignment(Element.ALIGN_RIGHT);
            } else{
                tx.setAlignment(Element.ALIGN_LEFT);
            }
        }
    }

    private void setBorderStyles(PdfDictionary merged, BaseField tx) {
        PdfDictionary bs = merged.getAsDict(PdfName.BS);
        if (bs != null) {
            setBorderStyle(bs, tx);
        } else {
            PdfArray bd = merged.getAsArray(PdfName.BORDER);
            if (bd != null) {
                setBorderArrayStyle(bd, tx);
            }
        }
    }

    private void setBorderStyle(PdfDictionary bs, BaseField tx) {
        PdfNumber w = bs.getAsNumber(PdfName.W);
        if (w != null) {
            tx.setBorderWidth(w.floatValue());
        }
        PdfName s = bs.getAsName(PdfName.S);
        if (s != null) {
            tx.setBorderStyle(mapBorderStyle(s));
        }
    }

    private int mapBorderStyle(PdfName s) {
        if (PdfName.D.equals(s)) {
            return PdfBorderDictionary.STYLE_DASHED;
        } else if (PdfName.B.equals(s)) {
            return PdfBorderDictionary.STYLE_BEVELED;
        } else if (PdfName.I.equals(s)) {
            return PdfBorderDictionary.STYLE_INSET;
        } else if (PdfName.U.equals(s)) {
            return PdfBorderDictionary.STYLE_UNDERLINE;
        }
        return PdfBorderDictionary.STYLE_SOLID;
    }

    private void setBorderArrayStyle(PdfArray bd, BaseField tx) {
        if (bd.size() >= 3) {
            tx.setBorderWidth(bd.getAsNumber(2).floatValue());
        }
        if (bd.size() >= 4) {
            tx.setBorderStyle(PdfBorderDictionary.STYLE_DASHED);
        }
    }

    PdfAppearance getAppearance(PdfDictionary merged, String[] values, String fieldName)
            throws IOException, DocumentException {
        topFirst = 0;
        String text = (values.length > 0) ? values[0] : null;

        TextField tx = getOrCreateTextField(merged, fieldName);
        PdfName fieldType = merged.getAsName(PdfName.FT);

        return switch (fieldType.toString()) {
            case "TX" -> handleTextField(tx, values);
            case "CH" -> handleChoiceField(tx, merged, values, text);
            default -> throw new DocumentException(MessageLocalization.getComposedMessage(
                    "an.appearance.was.requested.without.a.variable.text.field"));
        };
    }

    private TextField getOrCreateTextField(PdfDictionary merged, String fieldName) throws DocumentException {
        TextField tx;
        if (fieldCache == null || !fieldCache.containsKey(fieldName)) {
            tx = new TextField(writer, null, null);
            tx.setExtraMargin(extraMarginLeft, extraMarginTop);
            tx.setBorderWidth(0);
            tx.setSubstitutionFontList(substitutionFonts);
            decodeGenericDictionary(merged, tx);
            setTextFieldBox(tx, merged);
            if (fieldCache != null) {
                fieldCache.put(fieldName, tx);
            }
        } else {
            tx = (TextField) fieldCache.get(fieldName);
            tx.setWriter(writer);
        }
        return tx;
    }

    private void setTextFieldBox(TextField tx, PdfDictionary merged) {
        PdfArray rect = merged.getAsArray(PdfName.RECT);
        Rectangle box = PdfReader.getNormalizedRectangle(rect);
        if (tx.getRotation() == 90 || tx.getRotation() == 270) {
            box = box.rotate();
        }
        tx.setBox(box);
    }

    private PdfAppearance handleTextField(TextField tx, String[] values) throws IOException, DocumentException {
        if (values.length > 0 && values[0] != null) {
            tx.setText(values[0]);
        }
        return tx.getAppearance();
    }

    private PdfAppearance handleChoiceField(TextField tx, PdfDictionary merged, String[] values, String text) throws IOException, DocumentException {
        PdfArray opt = merged.getAsArray(PdfName.OPT);
        int flags = getFlags(merged);

        if ((flags & PdfFormField.FF_COMBO) != 0 && opt == null) {
            tx.setText(text);
            return tx.getAppearance();
        }

        if (opt != null) {
            handleChoices(tx, opt, values, text, flags);
        }

        PdfAppearance app = tx.getListAppearance();
        topFirst = tx.getTopFirst();
        return app;
    }

    private int getFlags(PdfDictionary merged) {
        PdfNumber nfl = merged.getAsNumber(PdfName.FF);
        return (nfl != null) ? nfl.intValue() : 0;
    }

    private void handleChoices(TextField tx, PdfArray opt, String[] values, String text, int flags) {
        String[] choices = new String[opt.size()];
        String[] choicesExp = new String[opt.size()];

        for (int k = 0; k < opt.size(); ++k) {
            PdfObject obj = opt.getPdfObject(k);
            if (obj.isString()) {
                choices[k] = choicesExp[k] = ((PdfString) obj).toUnicodeString();
            } else {
                PdfArray a = (PdfArray) obj;
                choicesExp[k] = a.getAsString(0).toUnicodeString();
                choices[k] = a.getAsString(1).toUnicodeString();
            }
        }

        if ((flags & PdfFormField.FF_COMBO) != 0) {
            text = updateTextForCombo(text, choicesExp);
            tx.setText(text);
            return;
        }

        List<Integer> indexes = getSelectedIndexes(values, choicesExp);
        tx.setChoices(choices);
        tx.setChoiceExports(choicesExp);
        tx.setChoiceSelections(indexes);
    }

    private String updateTextForCombo(String text, String[] choicesExp) {
        for (String s : choicesExp) {
            if (text != null && text.equals(s)) {
                text = s;
                break;
            }
        }
        return text;
    }

    private List<Integer> getSelectedIndexes(String[] values, String[] choicesExp) {
        List<Integer> indexes = new ArrayList<>();
        for (int k = 0; k < choicesExp.length; ++k) {
            for (String val : values) {
                if (val != null && val.equals(choicesExp[k])) {
                    indexes.add(k);
                    break;
                }
            }
        }
        return indexes;
    }


    /**
     * Set font encoding from DR-structure if font doesn't have this info itself
     */
    private void adjustFontEncoding(PdfDictionary dr, PRIndirectReference por) {
        PdfDictionary drEncoding = dr.getAsDict(PdfName.ENCODING);
        if (drEncoding != null) {
            PdfDictionary fontDict = (PdfDictionary) PdfReader.getPdfObject(por);
            if (fontDict != null && fontDict.get(PdfName.ENCODING) == null) {
                for (PdfName key : drEncoding.getKeys()) {
                    fontDict.put(PdfName.ENCODING, drEncoding.get(key));
                }
            }
        }
    }

    PdfAppearance getAppearance(PdfDictionary merged, String text, String fieldName)
            throws IOException, DocumentException {
        String[] valueArr = new String[1];
        valueArr[0] = text;
        return getAppearance(merged, valueArr, fieldName);
    }

    Color getMKColor(PdfArray ar) {
        if (ar == null) {
            return null;
        }
        switch (ar.size()) {
            case 1:
                return new GrayColor(ar.getAsNumber(0).floatValue());
            case 3:
                return new Color(ExtendedColor.normalize(ar.getAsNumber(0).floatValue()),
                        ExtendedColor.normalize(ar.getAsNumber(1).floatValue()),
                        ExtendedColor.normalize(ar.getAsNumber(2).floatValue()));
            case 4:
                return new CMYKColor(ar.getAsNumber(0).floatValue(), ar.getAsNumber(1).floatValue(),
                        ar.getAsNumber(2).floatValue(),
                        ar.getAsNumber(3).floatValue());
            default:
                return null;
        }
    }

    /**
     * Gets the field value.
     *
     * @param name the fully qualified field name
     * @return the field value
     */
    public String getField(String name) {
        if (xfa.isXfaPresent()) {
            return getXfaField(name);
        }
        return getPdfField(name);
    }

    private String getXfaField(String name) {
        name = xfa.findFieldName(name, this);
        if (name == null) {
            return null;
        }
        name = XfaForm.Xml2Som.getShortName(name);
        return XfaForm.getNodeText(xfa.findDatasetsNode(name));
    }

    private String getPdfField(String name) {
        Item item = fields.get(name);
        if (item == null) {
            return null;
        }
        lastWasString = false;
        PdfDictionary mergedDict = item.getMerged(0);

        PdfObject value = PdfReader.getPdfObject(mergedDict.get(PdfName.V));
        if (value == null) {
            return getDefaultFieldValue(name);
        }
        return processPdfValue(value, mergedDict, item);
    }

    private String getDefaultFieldValue(String name) {
        return (this.getFieldType(name) == AcroFields.FIELD_TYPE_CHECKBOX) ? "Off" : "";
    }

    private String processPdfValue(PdfObject value, PdfDictionary mergedDict, Item item) {
        if (value instanceof PRStream stream) {
            return readStreamValue(stream);
        }
        if (PdfName.BTN.equals(mergedDict.getAsName(PdfName.FT))) {
            return processButtonField(value, item, mergedDict);
        }
        return processOtherPdfValue(value);
    }

    private String readStreamValue(PRStream stream) {
        try {
            byte[] valBytes = PdfReader.getStreamBytes(stream);
            return new String(valBytes);
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        } catch (PDFFilterException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
    }

    private String processButtonField(PdfObject value, Item item, PdfDictionary mergedDict) {
        PdfNumber ff = mergedDict.getAsNumber(PdfName.FF);
        int flags = (ff != null) ? ff.intValue() : 0;
        if ((flags & PdfFormField.FF_PUSHBUTTON) != 0) {
            return "";
        }
        String stringValue = getStringValue(value);
        return getOptionValue(item, stringValue);
    }

    private String getStringValue(PdfObject value) {
        if (value instanceof PdfName) {
            return PdfName.decodeName(value.toString());
        } else if (value instanceof PdfString string) {
            return string.toUnicodeString();
        }
        return "";
    }

    private String getOptionValue(Item item, String value) {
        PdfArray options = item.getValue(0).getAsArray(PdfName.OPT);
        if (options != null) {
            try {
                int idx = Integer.parseInt(value);
                PdfString optionString = options.getAsString(idx);
                lastWasString = true;
                return optionString.toUnicodeString();
            } catch (NumberFormatException e) {
                //da vedere come effettuare il log
            }
        }
        return "";
    }

    private String processOtherPdfValue(PdfObject value) {
        if (value instanceof PdfString string) {
            lastWasString = true;
            return string.toUnicodeString();
        } else if (value instanceof PdfName) {
            return PdfName.decodeName(value.toString());
        } else if (value instanceof PdfArray array) {
            return array.toString();
        }
        return "";
    }


    /**
     * Gets the field values of a Choice field.
     *
     * @param name the fully qualified field name
     * @return the field value
     * @since 2.1.3
     */
    public String[] getListSelection(String name) {
        String[] ret;
        String s = getField(name);
        if (s == null) {
            ret = new String[]{};
        } else {
            ret = new String[]{s};
        }
        Item item = fields.get(name);
        if (item == null) {
            return ret;
        }
        PdfArray values = item.getMerged(0).getAsArray(PdfName.I);
        if (values == null) {
            return ret;
        }
        ret = new String[values.size()];
        String[] options = getListOptionExport(name);
        PdfNumber n;
        int idx = 0;
        for (PdfObject pdfObject : values.getElements()) {
            n = (PdfNumber) pdfObject;
            ret[idx++] = options[n.intValue()];
        }
        return ret;
    }

    /**
     * Sets a field property. Valid property names are:
     *
     * <ul>
     * <li>textfont - sets the text font. The value for this entry is a <CODE>BaseFont</CODE>.<br>
     * <li>textcolor - sets the text color. The value for this entry is a <CODE>java.awt.Color</CODE>.<br>
     * <li>textsize - sets the text size. The value for this entry is a <CODE>Float</CODE>.
     * <li>bgcolor - sets the background color. The value for this entry is a <CODE>java.awt.Color</CODE>.
     * If <code>null</code> removes the background.<br>
     * <li>bordercolor - sets the border color. The value for this entry is a <CODE>java.awt.Color</CODE>.
     * If <code>null</code> removes the border.<br>
     * </ul>
     *
     * @param field the field name
     * @param name  the property name
     * @param value the property value
     * @param inst  an array of <CODE>int</CODE> indexing into <CODE>AcroField.Item.merged</CODE> elements to process.
     *              Set to <CODE>null</CODE> to process all
     * @return <CODE>true</CODE> if the property exists, <CODE>false</CODE> otherwise
     */
    public boolean setFieldProperty(String field, String name, Object value, int[] inst) {
        if (writer == null) {
            throw new ReadOnlyException(MessageLocalization.getComposedMessage(THIS_ACROFIELDS_INSTANCE_IS_READ_ONLY));
        }
        try {
            Item item = fields.get(field);
            if (item == null) {
                return false;
            }
            InstHit hit = new InstHit(inst);

            switch (name.toLowerCase()) {
                case "textfont":
                    handleTextFont(item, value, hit);
                    break;
                case "textcolor":
                    handleTextColor(item, value, hit);
                    break;
                case "textsize":
                    handleTextSize(item, value, hit);
                    break;
                case BGCOLOR, "bordercolor":
                    handleColorProperty(item, value, hit, name);
                    break;
                default:
                    return false;
            }
            return true;
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    private void handleTextFont(Item item, Object value, InstHit hit) throws DocumentException {
        for (int k = 0; k < item.size(); ++k) {
            if (hit.isHit(k)) {
                PdfDictionary merged = item.getMerged(k);
                PdfString da = merged.getAsString(PdfName.DA);
                PdfDictionary dr = merged.getAsDict(PdfName.DR);
                if (da != null && dr != null) {
                    Object[] dao = splitDAelements(da.toUnicodeString());
                    updateFont(item, k, dr, dao, (BaseFont) value);
                }
            }
        }
    }

    private void handleTextColor(Item item, Object value, InstHit hit) {
        for (int k = 0; k < item.size(); ++k) {
            if (hit.isHit(k)) {
                PdfDictionary merged = item.getMerged(k);
                PdfString da = merged.getAsString(PdfName.DA);
                if (da != null) {
                    Object[] dao = splitDAelements(da.toUnicodeString());
                    updateTextAppearance(item, k, dao, (Color) value, null);
                }
            }
        }
    }

    private void handleTextSize(Item item, Object value, InstHit hit) {
        for (int k = 0; k < item.size(); ++k) {
            if (hit.isHit(k)) {
                PdfDictionary merged = item.getMerged(k);
                PdfString da = merged.getAsString(PdfName.DA);
                if (da != null) {
                    Object[] dao = splitDAelements(da.toUnicodeString());
                    updateTextAppearance(item, k, dao, null, (Float) value);
                }
            }
        }
    }

    private void handleColorProperty(Item item, Object value, InstHit hit, String name) throws AnnotationException {
        PdfName dname = name.equalsIgnoreCase(BGCOLOR) ? PdfName.BG : PdfName.BC;
        for (int k = 0; k < item.size(); ++k) {
            if (hit.isHit(k)) {
                PdfDictionary merged = item.getMerged(k);
                PdfDictionary mk = merged.getAsDict(PdfName.MK);
                updateColorDictionary(item, k, mk, value, dname);
            }
        }
    }

    private void updateFont(Item item, int index, PdfDictionary dr, Object[] dao, BaseFont bf) throws DocumentException {
        PdfAppearance cb = new PdfAppearance(this.writer);
        if (dao[DA_FONT] != null) {
            PdfName psn = PdfAppearance.stdFieldFontNames.get(bf.getPostscriptFontName());
            if (psn == null) {
                psn = new PdfName(bf.getPostscriptFontName());
            }
            PdfDictionary fonts = dr.getAsDict(PdfName.FONT);
            if (fonts == null) {
                fonts = new PdfDictionary();
                dr.put(PdfName.FONT, fonts);
            }
            PdfIndirectReference fref = (PdfIndirectReference) fonts.get(psn);
            if (fref == null) {
                handleFontReference(fonts, bf, psn);
            }
            ByteBuffer buf = cb.getInternalBuffer();
            buf.append(psn.getBytes()).append(' ').append((Float) dao[DA_SIZE]).append(" Tf ");
            if (dao[DA_COLOR] != null) {
                cb.setColorFill((Color) dao[DA_COLOR]);
            }
            updateDA(item, index, cb);
        }
    }

    private void handleFontReference(PdfDictionary fonts, BaseFont bf, PdfName psn) throws DocumentException {
        PdfDictionary top = reader.getCatalog().getAsDict(PdfName.ACROFORM);
        markUsed(top);

        // Ottieni o crea il dizionario delle risorse (DR) all'interno di AcroForm
        PdfDictionary drTop = top.getAsDict(PdfName.DR);
        if (drTop == null) {
            drTop = new PdfDictionary();
            top.put(PdfName.DR, drTop);
        }
        markUsed(drTop);

        // Ottieni o crea il dizionario dei font all'interno di DR
        PdfDictionary fontsTop = drTop.getAsDict(PdfName.FONT);
        if (fontsTop == null) {
            fontsTop = new PdfDictionary();
            drTop.put(PdfName.FONT, fontsTop);
        }
        markUsed(fontsTop);

        // Cerca il riferimento indiretto al font nel dizionario globale
        PdfIndirectReference frefTop = (PdfIndirectReference) fontsTop.get(psn);

        if (frefTop != null) {
            // Se esiste un riferimento nel dizionario globale, lo aggiungiamo anche a quello locale
            fonts.put(psn, frefTop);
        } else {
            // Se non esiste, creiamo un nuovo font e aggiungiamo i riferimenti
            FontDetails fd;
            if (bf.getFontType() == BaseFont.FONT_TYPE_DOCUMENT) {
                // Se il font è già presente nel documento
                fd = new FontDetails(null, ((DocumentFont) bf).getIndirectReference(), bf);
            } else {
                // Se il font è nuovo, lo aggiungiamo come semplice font
                bf.setSubset(false);
                fd = writer.addSimple(bf);
                localFonts.put(psn.toString().substring(1), bf);
            }
            // Aggiungiamo il riferimento indiretto ai dizionari
            fontsTop.put(psn, fd.getIndirectReference());
            fonts.put(psn, fd.getIndirectReference());
        }
    }

    private void updateTextAppearance(Item item, int index, Object[] dao, Color color, Float size) {
        PdfAppearance cb = new PdfAppearance(this.writer);
        if (dao[DA_FONT] != null) {
            ByteBuffer buf = cb.getInternalBuffer();
            buf.append(new PdfName((String) dao[DA_FONT]).getBytes());
            if (size != null) {
                buf.append(' ').append(size).append(" Tf ");
            } else {
                buf.append(' ').append((Float) dao[DA_SIZE]).append(" Tf ");
            }
            if (color != null) {
                cb.setColorFill(color);
            } else {
                cb.setColorFill((Color) dao[DA_COLOR]);
            }
            updateDA(item, index, cb);
        }
    }

    private void updateDA(Item item, int index, PdfAppearance cb) {
        PdfString s = new PdfString(cb.toString());
        item.getMerged(index).put(PdfName.DA, s);
        item.getWidget(index).put(PdfName.DA, s);
        markUsed(item.getWidget(index));
    }

    private void updateColorDictionary(Item item, int index, PdfDictionary mk, Object value, PdfName dname)
            throws AnnotationException {
        if (mk == null) {
            if (value == null) {
                return;
            }
            mk = new PdfDictionary();
            item.getMerged(index).put(PdfName.MK, mk);
            item.getWidget(index).put(PdfName.MK, mk);
            markUsed(item.getWidget(index));
        } else {
            markUsed(mk);
        }
        if (value == null) {
            mk.remove(dname);
        } else {
            mk.put(dname, PdfAnnotation.getMKColor((Color) value));
        }
    }


    /**
     * Sets the fields by FDF merging.
     *
     * @param fdf the FDF form
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public void setFields(FdfReader fdf) throws IOException, DocumentException {
        Map<String, PdfDictionary> fd = fdf.getAllFields();
        for (String f : fd.keySet()) {
            String v = fdf.getFieldValue(f);
            if (v != null) {
                setField(f, v);
            }
        }
    }

    /**
     * Allows merging the fields by a field reader. One use would be to set the fields by XFDF merging.
     *
     * @param fieldReader The fields to merge.
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public void setFields(XfdfReader fieldReader) throws IOException, DocumentException {
        setFields((FieldReader) fieldReader);
    }

    /**
     * Allows merging the fields by a field reader. One use would be to set the fields by XFDF merging.
     *
     * @param fieldReader The fields to merge.
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public void setFields(FieldReader fieldReader) throws IOException, DocumentException {
        Map<String, String> fd = fieldReader.getAllFields();
        for (String f : fd.keySet()) {
            String v = fieldReader.getFieldValue(f);
            if (v != null) {
                setField(f, v);
            }
            List<String> list = fieldReader.getListValues(f);
            if (list != null) {
                setListSelection(v, list.toArray(new String[0]));
            }
        }
    }

    /**
     * Regenerates the field appearance. This is useful when you change a field property, but not its value, for
     * instance form.setFieldProperty("f", "bgcolor", Color.BLUE, null); This won't have any effect, unless you use
     * regenerateField("f") after changing the property.
     *
     * @param name the fully qualified field name or the partial name in the case of XFA forms
     * @return <CODE>true</CODE> if the field was found and changed,
     * <CODE>false</CODE> otherwise
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public boolean regenerateField(String name) throws IOException, DocumentException {
        String value = getField(name);
        return setField(name, value, value);
    }

    /**
     * Sets the field value.
     *
     * @param name  the fully qualified field name or the partial name in the case of XFA forms
     * @param value the field value
     * @return <CODE>true</CODE> if the field was found and changed,
     * <CODE>false</CODE> otherwise
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public boolean setField(String name, String value) throws IOException, DocumentException {
        return setField(name, value, null);
    }

    /**
     * Sets the field value and the display string. The display string is used to build the appearance in the cases
     * where the value is modified by Acrobat with JavaScript and the algorithm is known.
     *
     * @param name    the fully qualified field name or the partial name in the case of XFA forms
     * @param value   the field value
     * @param display the string that is used for the appearance. If <CODE>null</CODE> the <CODE>value</CODE> parameter
     *                will be used
     * @return <CODE>true</CODE> if the field was found and changed,
     * <CODE>false</CODE> otherwise
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public boolean setField(String name, String value, String display) throws IOException, DocumentException {
        checkWriter();

        if (xfa.isXfaPresent()) {
            name = handleXfa(name, value);
            if (name == null) {
                return false;
            }
        }

        Item item = fields.get(name);
        if (item == null) {
            return false;
        }

        PdfDictionary merged = item.getMerged(0);
        PdfName type = merged.getAsName(PdfName.FT);

        value = adjustValueLength(value, type, merged);

        if (display == null) {
            display = value;
        }

        if (PdfName.TX.equals(type) || PdfName.CH.equals(type)) {
            return handleTextField(item, value, display, name, type);
        } else if (PdfName.BTN.equals(type)) {
            return handleButtonField(item, value, name);
        }

        return false;
    }

    private void checkWriter() throws DocumentException {
        if (writer == null) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage(THIS_ACROFIELDS_INSTANCE_IS_READ_ONLY));
        }
    }

    private String handleXfa(String name, String value) {
        name = xfa.findFieldName(name, this);
        if (name == null) {
            return null;
        }
        String shortName = XfaForm.Xml2Som.getShortName(name);
        Node xn = xfa.findDatasetsNode(shortName);
        if (xn == null) {
            xn = xfa.getDatasetsSom().insertNode(xfa.getDatasetsNode(), shortName);
        }
        xfa.setNodeText(xn, value);
        return name;
    }

    private String adjustValueLength(String value, PdfName type, PdfDictionary merged) {
        if (PdfName.TX.equals(type)) {
            PdfNumber maxLen = merged.getAsNumber(PdfName.MAXLEN);
            int len = maxLen != null ? maxLen.intValue() : 0;
            if (len > 0) {
                value = value.substring(0, Math.min(len, value.length()));
            }
        }
        return value;
    }

    private boolean handleTextField(Item item, String value, String display, String name, PdfName type)
            throws IOException {
        PdfString v = new PdfString(value, PdfObject.TEXT_UNICODE);
        for (int idx = 0; idx < item.size(); ++idx) {
            updateField(item, idx, v, display, name, type);
        }
        return true;
    }

    private void updateField(Item item, int idx, PdfString value, String display, String name, PdfName type)
            throws IOException {
        PdfDictionary valueDic = item.getValue(idx);
        valueDic.put(PdfName.V, value);
        valueDic.remove(PdfName.I);
        markUsed(valueDic);

        PdfDictionary merged = item.getMerged(idx);
        merged.remove(PdfName.I);
        merged.put(PdfName.V, value);

        PdfDictionary widget = item.getWidget(idx);
        updateWidgetAppearance(widget, merged, display, name, type);
        markUsed(widget);
    }

    private void updateWidgetAppearance(PdfDictionary widget, PdfDictionary merged, String display, String name, PdfName type)
            throws IOException {
        if (generateAppearances) {
            PdfAppearance app = getAppearance(merged, display, name);
            if (PdfName.CH.equals(type)) {
                PdfNumber n = new PdfNumber(topFirst);
                widget.put(PdfName.TI, n);
                merged.put(PdfName.TI, n);
            }
            PdfDictionary appDic = widget.getAsDict(PdfName.AP);
            if (appDic == null) {
                appDic = new PdfDictionary();
                widget.put(PdfName.AP, appDic);
                merged.put(PdfName.AP, appDic);
            }
            appDic.put(PdfName.N, app.getIndirectReference());
            writer.releaseTemplate(app);
        } else {
            widget.remove(PdfName.AP);
            merged.remove(PdfName.AP);
        }
    }

    private boolean handleButtonField(Item item, String value, String name) throws IOException {
        PdfNumber ff = item.getMerged(0).getAsNumber(PdfName.FF);
        int flags = ff != null ? ff.intValue() : 0;

        if ((flags & PdfFormField.FF_PUSHBUTTON) != 0) {
            return handlePushbuttonField(value, name);
        }

        return handleChoiceField(item, value);
    }

    private boolean handlePushbuttonField(String value, String name) throws IOException {
        Image img;
        try {
            img = Image.getInstance(Base64.getDecoder().decode(value));
        } catch (Exception e) {
            return false;
        }
        PushbuttonField pb = getNewPushbuttonFromField(name);
        pb.setImage(img);
        replacePushbuttonField(name, pb.getField());
        return true;
    }

    private boolean handleChoiceField(Item item, String value) {
        List<String> lopt = new ArrayList<>();
        PdfArray opts = item.getValue(0).getAsArray(PdfName.OPT);
        if (opts != null) {
            for (int k = 0; k < opts.size(); ++k) {
                PdfString valStr = opts.getAsString(k);
                lopt.add(valStr != null ? valStr.toUnicodeString() : null);
            }
        }

        PdfName vt = getChoiceFieldValue(value, lopt);
        for (int idx = 0; idx < item.size(); ++idx) {
            updateChoiceField(item, idx, vt);
        }
        return true;
    }

    private PdfName getChoiceFieldValue(String value, List<String> lopt) {
        int vidx = lopt.indexOf(value);
        return vidx >= 0 ? new PdfName(String.valueOf(vidx)) : new PdfName(value);
    }

    private void updateChoiceField(Item item, int idx, PdfName vt) {
        PdfDictionary merged = item.getMerged(idx);
        PdfDictionary widget = item.getWidget(idx);
        PdfDictionary valDict = item.getValue(idx);

        markUsed(valDict);
        valDict.put(PdfName.V, vt);
        merged.put(PdfName.V, vt);

        markUsed(widget);
        if (isInAP(widget, vt)) {
            merged.put(PdfName.AS, vt);
            widget.put(PdfName.AS, vt);
        } else {
            merged.put(PdfName.AS, PdfName.offLowCase);
            widget.put(PdfName.AS, PdfName.offLowCase);
        }
    }


    /**
     * Sets different values in a list selection. No appearance is generated yet; nor does the code check if multiple
     * select is allowed.
     *
     * @param name  the name of the field
     * @param value an array with values that need to be selected
     * @return true only if the field value was changed
     * @throws IOException       on error
     * @throws DocumentException on error
     * @since 2.1.4
     */
    public boolean setListSelection(String name, String[] value) throws IOException, DocumentException {
        Item item = getFieldItem(name);
        if (item == null) {
            return false;
        }
        PdfDictionary merged = item.getMerged(0);
        PdfName type = merged.getAsName(PdfName.FT);
        if (!PdfName.CH.equals(type)) {
            return false;
        }
        String[] options = getListOptionExport(name);
        PdfArray array = new PdfArray();
        for (String s1 : value) {
            for (int j = 0; j < options.length; j++) {
                if (options[j].equals(s1)) {
                    array.add(new PdfNumber(j));
                    break;
                }
            }
        }
        item.writeToAll(PdfName.I, array, Item.WRITE_MERGED | Item.WRITE_VALUE);

        PdfArray vals = new PdfArray();
        for (String s : value) {
            vals.add(new PdfString(s));
        }
        item.writeToAll(PdfName.V, vals, Item.WRITE_MERGED | Item.WRITE_VALUE);

        PdfAppearance app = getAppearance(merged, value, name);

        PdfDictionary apDic = new PdfDictionary();
        apDic.put(PdfName.N, app.getIndirectReference());
        item.writeToAll(PdfName.AP, apDic, Item.WRITE_MERGED | Item.WRITE_WIDGET);

        writer.releaseTemplate(app);

        item.markUsed(this, Item.WRITE_VALUE | Item.WRITE_WIDGET);
        return true;
    }

    boolean isInAP(PdfDictionary dic, PdfName check) {
        PdfDictionary appDic = dic.getAsDict(PdfName.AP);
        if (appDic == null) {
            return false;
        }
        PdfDictionary nDic = appDic.getAsDict(PdfName.N);
        return (nDic != null && nDic.get(check) != null);
    }

    /**
     * Gets all the fields. The fields are keyed by the fully qualified field name and the value is an instance of
     * <CODE>AcroFields.Item</CODE>.
     *
     * @return all the fields
     */
    public Map<String, Item> getAllFields() {
        return fields;
    }

    /**
     * Gets the field structure.
     *
     * @param name the name of the field
     * @return the field structure or <CODE>null</CODE> if the field does not exist
     */
    public Item getFieldItem(String name) {
        if (xfa.isXfaPresent()) {
            name = xfa.findFieldName(name, this);
            if (name == null) {
                return null;
            }
        }
        return fields.get(name);
    }

    /**
     * Gets the long XFA translated name.
     *
     * @param name the name of the field
     * @return the long field name
     */
    public String getTranslatedFieldName(String name) {
        if (xfa.isXfaPresent()) {
            String namex = xfa.findFieldName(name, this);
            if (namex != null) {
                name = namex;
            }
        }
        return name;
    }

    /**
     * Gets the field box positions in the document. The return is an array of <CODE>float</CODE> multiple of 5. For
     * each of these groups the values are: [page, llx, lly, urx, ury]. The coordinates have the page rotation in
     * consideration.
     *
     * @param name the field name
     * @return the positions or <CODE>null</CODE> if field does not exist
     */
    public float[] getFieldPositions(String name) {
        Item item = getFieldItem(name);
        if (item == null) {
            return new float[0];
        }
        float[] ret = new float[item.size() * 5];
        int ptr = 0;
        for (int k = 0; k < item.size(); ++k) {
            try {
                PdfDictionary wd = item.getWidget(k);
                PdfArray rect = wd.getAsArray(PdfName.RECT);
                if (rect == null) {
                    continue;
                }
                Rectangle r = PdfReader.getNormalizedRectangle(rect);
                int page = item.getPage(k);
                int rotation = reader.getPageRotation(page);
                ret[ptr++] = page;
                if (rotation != 0) {
                    Rectangle pageSize = reader.getPageSize(page);
                    switch (rotation) {
                        case 270:
                            r = new Rectangle(
                                    pageSize.getTop() - r.getBottom(),
                                    r.getLeft(),
                                    pageSize.getTop() - r.getTop(),
                                    r.getRight());
                            break;
                        case 180:
                            r = new Rectangle(
                                    pageSize.getRight() - r.getLeft(),
                                    pageSize.getTop() - r.getBottom(),
                                    pageSize.getRight() - r.getRight(),
                                    pageSize.getTop() - r.getTop());
                            break;
                        case 90:
                            r = new Rectangle(
                                    r.getBottom(),
                                    pageSize.getRight() - r.getLeft(),
                                    r.getTop(),
                                    pageSize.getRight() - r.getRight());
                            break;
                        default:
                            break;
                    }
                    r.normalize();
                }
                ret[ptr++] = r.getLeft();
                ret[ptr++] = r.getBottom();
                ret[ptr++] = r.getRight();
                ret[ptr++] = r.getTop();
            } catch (Exception e) {
                // empty on purpose
            }
        }
        if (ptr < ret.length) {
            float[] ret2 = new float[ptr];
            System.arraycopy(ret, 0, ret2, 0, ptr);
            return ret2;
        }
        return ret;
    }

    private int removeRefFromArray(PdfArray array, PdfObject refo) {
        if (refo == null || !refo.isIndirect()) {
            return array.size();
        }
        PdfIndirectReference ref = (PdfIndirectReference) refo;
        int j = 0;
        while (j < array.size()) {
            PdfObject obj = array.getPdfObject(j);
            if (!obj.isIndirect()) {
                continue;
            }
            if (((PdfIndirectReference) obj).getNumber() == ref.getNumber()) {
                array.remove(j--);
            }
            ++j;
        }
        return array.size();
    }

    /**
     * Removes all the fields from <CODE>page</CODE>.
     *
     * @param page the page to remove the fields from
     * @return <CODE>true</CODE> if any field was removed, <CODE>false otherwise</CODE>
     */
    public boolean removeFieldsFromPage(int page) {
        if (page < 1) {
            return false;
        }
        String[] names = new String[fields.size()];
        fields.keySet().toArray(names);
        boolean found = false;
        for (String name : names) {
            boolean fr = removeField(name, page);
            found = (found || fr);
        }
        return found;
    }

    /**
     * Removes a field from the document. If page equals -1 all the fields with this
     * <CODE>name</CODE> are removed from the document otherwise only the fields in
     * that particular page are removed.
     *
     * @param name the field name
     * @param page the page to remove the field from or -1 to remove it from all the pages
     * @return <CODE>true</CODE> if the field exists, <CODE>false otherwise</CODE>
     */
    public boolean removeField(String name, int page) {
        Item item = getFieldItem(name);
        if (item == null) {
            return false;
        }

        PdfDictionary acroForm = getAcroForm();
        if (acroForm == null) {
            return false;
        }

        PdfArray arrayf = acroForm.getAsArray(PdfName.FIELDS);
        if (arrayf == null) {
            return false;
        }

        processItem(item, page, arrayf);

        if (page == -1 || item.size() == 0) {
            fields.remove(name);
        }
        return true;
    }

    private PdfDictionary getAcroForm() {
        return (PdfDictionary) PdfReader.getPdfObject(
                reader.getCatalog().get(PdfName.ACROFORM),
                reader.getCatalog()
        );
    }

    private void processItem(Item item, int page, PdfArray arrayf) {
        int k = 0;
        while (k < item.size()) {
            int pageV = item.getPage(k);
            if (page != -1 && page != pageV) {
                k++;
                continue;
            }
            handleFieldRemoval(item, k, pageV, arrayf);
            if (page != -1) {
                item.remove(k);
                continue;
            }
            k++;
        }
    }

    private void handleFieldRemoval(Item item, int index, int pageV, PdfArray arrayf) {
        PdfIndirectReference ref = item.getWidgetRef(index);
        PdfDictionary wd = item.getWidget(index);
        PdfDictionary pageDic = reader.getPageN(pageV);
        PdfArray annots = pageDic.getAsArray(PdfName.ANNOTS);

        if (annots != null) {
            processAnnots(pageDic, annots, ref);
        }

        removeFromParents(wd, ref);
        removeRefFromArray(arrayf, ref);
        markUsed(arrayf);
    }

    private void processAnnots(PdfDictionary pageDic, PdfArray annots, PdfIndirectReference ref) {
        if (removeRefFromArray(annots, ref) == 0) {
            pageDic.remove(PdfName.ANNOTS);
            markUsed(pageDic);
        } else {
            markUsed(annots);
        }
    }

    private void removeFromParents(PdfDictionary wd, PdfIndirectReference ref) {
        PdfIndirectReference kid = ref;
        while ((ref = wd.getAsIndirectObject(PdfName.PARENT)) != null) {
            wd = wd.getAsDict(PdfName.PARENT);
            PdfArray kids = wd.getAsArray(PdfName.KIDS);
            if (removeRefFromArray(kids, kid) != 0) {
                break;
            }
            kid = ref;
            PdfReader.killIndirect(ref);
        }
    }


    /**
     * Removes a field from the document.
     *
     * @param name the field name
     * @return <CODE>true</CODE> if the field exists, <CODE>false otherwise</CODE>
     */
    public boolean removeField(String name) {
        return removeField(name, -1);
    }

    /**
     * Gets the property generateAppearances.
     *
     * @return the property generateAppearances
     */
    public boolean isGenerateAppearances() {
        return generateAppearances;
    }

    /**
     * Sets the option to generate appearances. Not generating appearances will speed-up form filling but the results
     * can be unexpected in Acrobat. Don't use it unless your environment is well controlled. The default is
     * <CODE>true</CODE>.
     *
     * @param generateAppearances the option to generate appearances
     */
    public void setGenerateAppearances(boolean generateAppearances) {
        this.generateAppearances = generateAppearances;
        PdfDictionary top = reader.getCatalog().getAsDict(PdfName.ACROFORM);
        if (generateAppearances) {
            top.remove(PdfName.NEEDAPPEARANCES);
        } else {
            top.put(PdfName.NEEDAPPEARANCES, PdfBoolean.PDFTRUE);
        }
    }

    /**
     * Gets the field names that have signatures and are signed.
     *
     * @return the field names that have signatures and are signed
     */
    public List<String> getSignedFieldNames() {
        if (sigNames != null) {
            return new ArrayList<>(sigNames.keySet());
        }

        sigNames = new HashMap<>();
        List<Object[]> sorter = new ArrayList<>();

        for (Map.Entry<String, Item> entry : fields.entrySet()) {
            processField(entry, sorter);
        }

        sorter.sort(new SorterComparator());
        updateTotalRevisions(sorter);
        updateSigNames(sorter);

        return new ArrayList<>(sigNames.keySet());
    }

    private void processField(Map.Entry<String, Item> entry, List<Object[]> sorter) {
        Item item = entry.getValue();
        PdfDictionary merged = item.getMerged(0);

        if (!isSignatureField(merged)) {
            return;
        }

        PdfDictionary v = merged.getAsDict(PdfName.V);
        if (v == null || !hasValidSignatureContents(v)) {
            return;
        }

        int length = calculateFieldLength(v);
        sorter.add(new Object[]{entry.getKey(), new int[]{length, 0}});
    }

    private boolean isSignatureField(PdfDictionary merged) {
        return PdfName.SIG.equals(merged.get(PdfName.FT));
    }

    private boolean hasValidSignatureContents(PdfDictionary v) {
        PdfString contents = v.getAsString(PdfName.CONTENTS);
        PdfArray ro = v.getAsArray(PdfName.BYTERANGE);
        return contents != null && ro != null && ro.size() >= 2;
    }

    private int calculateFieldLength(PdfDictionary v) {
        PdfString contents = v.getAsString(PdfName.CONTENTS);
        PdfArray ro = v.getAsArray(PdfName.BYTERANGE);
        int lengthOfSignedBlocks = 0;

        for (int i = ro.size() - 1; i > 0; i -= 2) {
            lengthOfSignedBlocks += ro.getAsNumber(i).intValue();
        }

        int unsignedBlock = contents.getOriginalBytes().length * 2 + 2;
        return lengthOfSignedBlocks + unsignedBlock;
    }

    private void updateTotalRevisions(List<Object[]> sorter) {
        if (!sorter.isEmpty()) {
            int lastIndex = sorter.size() - 1;
            int lengthOfLast = ((int[]) sorter.get(lastIndex)[1])[0];

            if (lengthOfLast == reader.getFileLength()) {
                totalRevisions = sorter.size();
            } else {
                totalRevisions = sorter.size() + 1;
            }
        }
    }

    private void updateSigNames(List<Object[]> sorter) {
        for (int k = 0; k < sorter.size(); ++k) {
            Object[] objs = sorter.get(k);
            String name = (String) objs[0];
            int[] p = (int[]) objs[1];
            p[1] = k + 1;
            sigNames.put(name, p);
        }
    }

    /**
     * Gets the signature dictionary, the one keyed by /V.
     *
     * @param name the field name
     * @return the signature dictionary keyed by /V or <CODE>null</CODE> if the field is not a signature
     */
    public PdfDictionary getSignatureDictionary(String name) {
        getSignedFieldNames();
        name = getTranslatedFieldName(name);
        if (!sigNames.containsKey(name)) {
            return null;
        }
        Item item = fields.get(name);
        PdfDictionary merged = item.getMerged(0);
        return merged.getAsDict(PdfName.V);
    }

    /**
     * Checks is the signature covers the entire document or just part of it.
     *
     * @param name the signature field name
     * @return <CODE>true</CODE> if the signature covers the entire document,
     * <CODE>false</CODE> otherwise
     */
    public boolean signatureCoversWholeDocument(String name) {
        getSignedFieldNames();
        name = getTranslatedFieldName(name);
        if (!sigNames.containsKey(name)) {
            return false;
        }
        return sigNames.get(name)[0] == reader.getFileLength();
    }

    /**
     * Verifies a signature. An example usage is:
     *
     * <pre>
     * KeyStore kall = PdfPKCS7.loadCacertsKeyStore();
     * PdfReader reader = new PdfReader("my_signed_doc.pdf");
     * AcroFields af = reader.getAcroFields();
     * ArrayList names = af.getSignatureNames();
     * for (int k = 0; k &lt; names.size(); ++k) {
     *    String name = (String)names.get(k);
     *    System.out.println("Signature name: " + name);
     *    System.out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
     *    PdfPKCS7 pk = af.verifySignature(name);
     *    Calendar cal = pk.getSignDate();
     *    Certificate pkc[] = pk.getCertificates();
     *    System.out.println("Subject: " + PdfPKCS7.getSubjectFields(pk.getSigningCertificate()));
     *    System.out.println("Document modified: " + !pk.verify());
     *    Object fails[] = PdfPKCS7.verifyCertificates(pkc, kall, null, cal);
     *    if (fails == null)
     *        System.out.println("Certificates verified against the KeyStore");
     *    else
     *        System.out.println("Certificate failed: " + fails[1]);
     * }
     * </pre>
     *
     * @param name the signature field name
     * @return a <CODE>PdfPKCS7</CODE> class to continue the verification
     */
    public PdfPKCS7 verifySignature(String name) {
        return verifySignature(name, null);
    }

    /**
     * Verifies a signature. An example usage is:
     *
     * <pre>
     * KeyStore kall = PdfPKCS7.loadCacertsKeyStore();
     * PdfReader reader = new PdfReader("my_signed_doc.pdf");
     * AcroFields af = reader.getAcroFields();
     * ArrayList names = af.getSignatureNames();
     * for (int k = 0; k &lt; names.size(); ++k) {
     *    String name = (String)names.get(k);
     *    System.out.println("Signature name: " + name);
     *    System.out.println("Signature covers whole document: " + af.signatureCoversWholeDocument(name));
     *    PdfPKCS7 pk = af.verifySignature(name);
     *    Calendar cal = pk.getSignDate();
     *    Certificate pkc[] = pk.getCertificates();
     *    System.out.println("Subject: " + PdfPKCS7.getSubjectFields(pk.getSigningCertificate()));
     *    System.out.println("Document modified: " + !pk.verify());
     *    Object fails[] = PdfPKCS7.verifyCertificates(pkc, kall, null, cal);
     *    if (fails == null)
     *        System.out.println("Certificates verified against the KeyStore");
     *    else
     *        System.out.println("Certificate failed: " + fails[1]);
     * }
     * </pre>
     *
     * @param name     the signature field name
     * @param provider the provider or <code>null</code> for the default provider
     * @return a <CODE>PdfPKCS7</CODE> class to continue the verification
     */
    public PdfPKCS7 verifySignature(String name, String provider) {
        PdfDictionary v = getSignatureDictionary(name);
        if (v == null) {
            return null;
        }
        try {
            PdfName sub = v.getAsName(PdfName.SUBFILTER);
            PdfString contents = v.getAsString(PdfName.CONTENTS);
            PdfPKCS7 pk = null;
            if (sub.equals(PdfName.ADBE_X509_RSA_SHA1)) {
                PdfString cert = v.getAsString(PdfName.CERT);
                pk = new PdfPKCS7(contents.getOriginalBytes(), cert.getBytes(), provider);
            } else {
                pk = new PdfPKCS7(contents.getOriginalBytes(), provider);
            }
            updateByteRange(pk, v);
            PdfString str = v.getAsString(PdfName.M);
            if (str != null) {
                pk.setSignDate(PdfDate.decode(str.toString()));
            }
            PdfObject obj = PdfReader.getPdfObject(v.get(PdfName.NAME));
            if (obj != null) {
                if (obj.isString()) {
                    pk.setSignName(((PdfString) obj).toUnicodeString());
                } else if (obj.isName()) {
                    pk.setSignName(PdfName.decodeName(obj.toString()));
                }
            }
            str = v.getAsString(PdfName.REASON);
            if (str != null) {
                pk.setReason(str.toUnicodeString());
            }
            str = v.getAsString(PdfName.LOCATION);
            if (str != null) {
                pk.setLocation(str.toUnicodeString());
            }
            return pk;
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    private void updateByteRange(PdfPKCS7 pkcs7, PdfDictionary v) {
        PdfArray b = v.getAsArray(PdfName.BYTERANGE);
        RandomAccessFileOrArray rf = reader.getSafeFile();
        try {
            rf.reOpen();
            byte[] buf = new byte[8192];
            int k = 0;
            while (k < b.size()) {
                int start = b.getAsNumber(k).intValue();
                int length = b.getAsNumber(++k).intValue();
                rf.seek(start);
                while (length > 0) {
                    int rd = rf.read(buf, 0, Math.min(length, buf.length));
                    if (rd <= 0) {
                        break;
                    }
                    length -= rd;
                    pkcs7.update(buf, 0, rd);
                }
                ++k;
            }
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        } finally {
            try {
                rf.close();
            } catch(IOException ignored) {
                //da vedere come effettuare il log
            }
        }
    }

    /**
     * Indicate that a PDF object has just been added. If it is not in append mode the object will just be changed. In
     * append mode a new object (with the same id) will be appended (cp. PDF incremental update).
     *
     * @param obj which should be marked as 'used' (=dirty)
     */
    public void markUsed(PdfObject obj) {
        if (!append) {
            return;
        }
        ((PdfStamperImp) writer).markUsed(obj);
    }

    /**
     * Gets the total number of revisions this document has.
     *
     * @return the total number of revisions
     */
    public int getTotalRevisions() {
        getSignedFieldNames();
        return this.totalRevisions;
    }

    /**
     * Gets this <CODE>field</CODE> revision.
     *
     * @param field the signature field name
     * @return the revision or zero if it's not a signature field
     */
    public int getRevision(String field) {
        getSignedFieldNames();
        field = getTranslatedFieldName(field);
        if (!sigNames.containsKey(field)) {
            return 0;
        }
        return sigNames.get(field)[1];
    }

    /**
     * Extracts a revision from the document.
     *
     * @param field the signature field name
     * @return an <CODE>InputStream</CODE> covering the revision. Returns <CODE>null</CODE> if it's not a signature
     * field
     * @throws IOException on error
     */
    public InputStream extractRevision(String field) throws IOException {
        getSignedFieldNames();
        field = getTranslatedFieldName(field);
        if (!sigNames.containsKey(field)) {
            return null;
        }
        int length = sigNames.get(field)[0];
        RandomAccessFileOrArray raf = reader.getSafeFile();
        raf.reOpen();
        raf.seek(0);
        return new RevisionStream(raf, length);
    }

    /**
     * Indicates whether the stamper is in append mode or not
     *
     * @return true when everything is done in the append mode otherwise false
     */
    public boolean isAppend() {
        return this.append;
    }

    /**
     * Gets the appearances cache.
     *
     * @return the appearances cache
     */
    public Map<String, BaseField> getFieldCacheMap() {
        return fieldCache;
    }

    /**
     * Sets a cache for field appearances. Parsing the existing PDF to create a new TextField is time expensive. For
     * those tasks that repeatedly fill the same PDF with different field values the use of the cache has dramatic speed
     * advantages. An example usage:
     *
     * <pre>
     * String pdfFile = ...;// the pdf file used as template
     * ArrayList xfdfFiles = ...;// the xfdf file names
     * ArrayList pdfOutFiles = ...;// the output file names, one for each element in xpdfFiles
     * HashMap cache = new HashMap();// the appearances cache
     * PdfReader originalReader = new PdfReader(pdfFile);
     * for (int k = 0; k &lt; xfdfFiles.size(); ++k) {
     *    PdfReader reader = new PdfReader(originalReader);
     *    XfdfReader xfdf = new XfdfReader((String)xfdfFiles.get(k));
     *    PdfStamper stp = new PdfStamper(reader, new FileOutputStream((String)pdfOutFiles.get(k)));
     *    AcroFields af = stp.getAcroFields();
     *    af.setFieldCache(cache);
     *    af.setFields(xfdf);
     *    stp.close();
     * }
     * </pre>
     *
     * @param fieldCache a Map that will carry the cached appearances
     */
    public void setFieldCacheMap(Map<String, BaseField> fieldCache) {
        this.fieldCache = fieldCache;
    }

    /**
     * Sets extra margins in text fields to better mimic the Acrobat layout.
     *
     * @param extraMarginLeft the extra margin left
     * @param extraMarginTop  the extra margin top
     */
    public void setExtraMargin(float extraMarginLeft, float extraMarginTop) {
        this.extraMarginLeft = extraMarginLeft;
        this.extraMarginTop = extraMarginTop;
    }

    /**
     * Adds a substitution font to the list. The fonts in this list will be used if the original font doesn't contain
     * the needed glyphs.
     *
     * @param font the font
     */
    public void addSubstitutionFont(BaseFont font) {
        if (substitutionFonts == null) {
            substitutionFonts = new ArrayList<>();
        }
        substitutionFonts.add(font);
    }

    /**
     * Gets the list of substitution fonts. The list is composed of <CODE>BaseFont</CODE> and can be <CODE>null</CODE>.
     * The fonts in this list will be used if the original font doesn't contain the needed glyphs.
     *
     * @return the list
     */
    public List<BaseFont> getAllSubstitutionFonts() {
        return substitutionFonts;
    }

    /**
     * Sets a list of substitution fonts. The list is composed of <CODE>BaseFont</CODE> and can also be
     * <CODE>null</CODE>. The fonts in this list will be used if the original font doesn't contain the needed glyphs.
     *
     * @param substitutionFonts the list
     */
    public void setAllSubstitutionFonts(List<BaseFont> substitutionFonts) {
        this.substitutionFonts = substitutionFonts;
    }

    /**
     * Gets the XFA form processor.
     *
     * @return the XFA form processor
     */
    public XfaForm getXfa() {
        return xfa;
    }

    /**
     * Removes the XFA stream from the document
     */
    public void removeXfa() {
        PdfDictionary root = this.reader.getCatalog();
        PdfDictionary acroform = root.getAsDict(PdfName.ACROFORM);

        if (acroform != null) {
            acroform.remove(PdfName.XFA);
            try {
                this.xfa = new XfaForm(this.reader);
            } catch (Exception e) {
                throw new ExceptionConverter(e);
            }
        }
    }

    /**
     * Creates a new pushbutton from an existing field. If there are several pushbuttons with the same name only the
     * first one is used. This pushbutton can be changed and be used to replace an existing one, with the same name or
     * other name, as long is it is in the same document. To replace an existing pushbutton call
     * {@link #replacePushbuttonField(String, PdfFormField)}.
     *
     * @param field the field name that should be a pushbutton
     * @return a new pushbutton or <CODE>null</CODE> if the field is not a pushbutton
     */
    public PushbuttonField getNewPushbuttonFromField(String field) {
        return getNewPushbuttonFromField(field, 0);
    }

    /**
     * Creates a new pushbutton from an existing field. This pushbutton can be changed and be used to replace an
     * existing one, with the same name or other name, as long is it is in the same document. To replace an existing
     * pushbutton call {@link #replacePushbuttonField(String, PdfFormField, int)}.
     *
     * @param field the field name that should be a pushbutton
     * @param order the field order in fields with same name
     * @return a new pushbutton or <CODE>null</CODE> if the field is not a pushbutton
     * @since 2.0.7
     */
    public PushbuttonField getNewPushbuttonFromField(String field, int order) {
        try {
            if (getFieldType(field) != FIELD_TYPE_PUSHBUTTON) {
                return null;
            }

            Item item = getFieldItem(field);
            if (order >= item.size()) {
                return null;
            }

            int posi = order * 5;
            float[] pos = getFieldPositions(field);
            Rectangle box = new Rectangle(pos[posi + 1], pos[posi + 2], pos[posi + 3], pos[posi + 4]);
            PushbuttonField newButton = new PushbuttonField(writer, box, null);

            PdfDictionary dic = item.getMerged(order);
            decodeGenericDictionary(dic, newButton);

            configurePushbuttonField(newButton, dic);
            return newButton;
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    private void configurePushbuttonField(PushbuttonField newButton, PdfDictionary dic) {
        PdfDictionary mk = dic.getAsDict(PdfName.MK);
        if (mk == null) {
            return;
        }

        setText(newButton, mk);
        setLayout(newButton, mk);
        configureIcon(newButton, mk);
    }

    private void setText(PushbuttonField newButton, PdfDictionary mk) {
        PdfString text = mk.getAsString(PdfName.CA);
        if (text != null) {
            newButton.setText(text.toUnicodeString());
        }
    }

    private void setLayout(PushbuttonField newButton, PdfDictionary mk) {
        PdfNumber tp = mk.getAsNumber(PdfName.TP);
        if (tp != null) {
            newButton.setLayout(tp.intValue() + 1);
        }
    }

    private void configureIcon(PushbuttonField newButton, PdfDictionary mk) {
        PdfDictionary ifit = mk.getAsDict(PdfName.IF);
        if (ifit == null) {
            return;
        }

        setIconScale(newButton, ifit);
        setIconProportional(newButton, ifit);
        setIconAdjustments(newButton, ifit);
        setIconFitToBounds(newButton, ifit);

        PdfObject i = mk.get(PdfName.I);
        if (i != null && i.isIndirect()) {
            newButton.setIconReference((PRIndirectReference) i);
        }
    }

    private void setIconScale(PushbuttonField newButton, PdfDictionary ifit) {
        PdfName sw = ifit.getAsName(PdfName.SW);
        if (sw != null) {
            int scale = PushbuttonField.SCALE_ICON_ALWAYS;
            if (sw.equals(PdfName.B)) {
                scale = PushbuttonField.SCALE_ICON_IS_TOO_BIG;
            } else if (sw.equals(PdfName.S)) {
                scale = PushbuttonField.SCALE_ICON_IS_TOO_SMALL;
            } else if (sw.equals(PdfName.N)) {
                scale = PushbuttonField.SCALE_ICON_NEVER;
            }
            newButton.setScaleIcon(scale);
        }
    }

    private void setIconProportional(PushbuttonField newButton, PdfDictionary ifit) {
        PdfName sw = ifit.getAsName(PdfName.S);
        if (sw != null && sw.equals(PdfName.A)) {
            newButton.setProportionalIcon(false);
        }
    }

    private void setIconAdjustments(PushbuttonField newButton, PdfDictionary ifit) {
        PdfArray aj = ifit.getAsArray(PdfName.A);
        if (aj != null && aj.size() == 2) {
            float left = aj.getAsNumber(0).floatValue();
            float bottom = aj.getAsNumber(1).floatValue();
            newButton.setIconHorizontalAdjustment(left);
            newButton.setIconVerticalAdjustment(bottom);
        }
    }

    private void setIconFitToBounds(PushbuttonField newButton, PdfDictionary ifit) {
        PdfBoolean fb = ifit.getAsBoolean(PdfName.FB);
        if (fb != null && fb.booleanValue()) {
            newButton.setIconFitToBounds(true);
        }
    }


    /**
     * Replaces the first field with a new pushbutton. The pushbutton can be created with
     * {@link #getNewPushbuttonFromField(String)} from the same document or it can be a generic PdfFormField of the type
     * pushbutton.
     *
     * @param field  the field name
     * @param button the <CODE>PdfFormField</CODE> representing the pushbutton
     * @return <CODE>true</CODE> if the field was replaced, <CODE>false</CODE> if the field
     * was not a pushbutton
     */
    public boolean replacePushbuttonField(String field, PdfFormField button) {
        return replacePushbuttonField(field, button, 0);
    }

    /**
     * Replaces the designated field with a new pushbutton. The pushbutton can be created with
     * {@link #getNewPushbuttonFromField(String, int)} from the same document or it can be a generic PdfFormField of the
     * type pushbutton.
     *
     * @param field  the field name
     * @param button the <CODE>PdfFormField</CODE> representing the pushbutton
     * @param order  the field order in fields with same name
     * @return <CODE>true</CODE> if the field was replaced, <CODE>false</CODE> if the field
     * was not a pushbutton
     * @since 2.0.7
     */
    public boolean replacePushbuttonField(String field, PdfFormField button, int order) {
        if (getFieldType(field) != FIELD_TYPE_PUSHBUTTON) {
            return false;
        }
        Item item = getFieldItem(field);
        if (order >= item.size()) {
            return false;
        }
        PdfDictionary merged = item.getMerged(order);
        PdfDictionary values = item.getValue(order);
        PdfDictionary widgets = item.getWidget(order);
        for (PdfName pdfName : buttonRemove) {
            merged.remove(pdfName);
            values.remove(pdfName);
            widgets.remove(pdfName);
        }
        for (PdfName key : button.getKeys()) {
            if (key.equals(PdfName.T) || key.equals(PdfName.RECT)) {
                continue;
            }
            if (key.equals(PdfName.FF)) {
                values.put(key, button.get(key));
            } else {
                widgets.put(key, button.get(key));
            }
            merged.put(key, button.get(key));
        }
        return true;
    }

    /**
     * The field representations for retrieval and modification.
     */
    public static class Item {

        /**
         * <CODE>writeToAll</CODE> constant.
         *
         * @since 2.1.5
         */
        public static final int WRITE_MERGED = 1;

        /**
         * <CODE>writeToAll</CODE> and <CODE>markUsed</CODE> constant.
         *
         * @since 2.1.5
         */
        public static final int WRITE_WIDGET = 2;

        /**
         * <CODE>writeToAll</CODE> and <CODE>markUsed</CODE> constant.
         *
         * @since 2.1.5
         */
        public static final int WRITE_VALUE = 4;
        /**
         * An array of <CODE>PdfDictionary</CODE> where the value tag /V is present.
         */
        ArrayList<PdfDictionary> values = new ArrayList<>();
        /**
         * An array of <CODE>PdfDictionary</CODE> with the widgets.
         */
        ArrayList<PdfDictionary> widgets = new ArrayList<>();
        /**
         * An array of <CODE>PdfDictionary</CODE> with the widget references.
         */
        ArrayList<PdfIndirectReference> widgetRefs = new ArrayList<>();
        /**
         * An array of <CODE>PdfDictionary</CODE> with all the field and widget tags merged.
         */
        ArrayList<PdfDictionary> merged = new ArrayList<>();
        /**
         * An array of <CODE>Integer</CODE> with the page numbers where the widgets are displayed.
         */
        ArrayList<Integer> page = new ArrayList<>();
        /**
         * An array of <CODE>Integer</CODE> with the tab order of the field in the page.
         */
        ArrayList<Integer> tabOrder = new ArrayList<>();
        /**
         * The indirect reference of the item itself
         */
        private PdfIndirectReference fieldReference;

        public Item(PdfIndirectReference ref) {
            this.fieldReference = ref;
        }

        /**
         * This function writes the given key/value pair to all the instances of merged, widget, and/or value, depending
         * on the
         * <code>writeFlags</code> setting
         *
         * @param key        you'll never guess what this is for.
         * @param value      if value is null, the key will be removed
         * @param writeFlags ORed together WRITE_* flags
         * @since 2.1.5
         */
        public void writeToAll(PdfName key, PdfObject value, int writeFlags) {
            int i;
            PdfDictionary curDict = null;
            if ((writeFlags & WRITE_MERGED) != 0) {
                for (i = 0; i < merged.size(); ++i) {
                    curDict = getMerged(i);
                    curDict.put(key, value);
                }
            }
            if ((writeFlags & WRITE_WIDGET) != 0) {
                for (i = 0; i < widgets.size(); ++i) {
                    curDict = getWidget(i);
                    curDict.put(key, value);
                }
            }
            if ((writeFlags & WRITE_VALUE) != 0) {
                for (i = 0; i < values.size(); ++i) {
                    curDict = getValue(i);
                    curDict.put(key, value);
                }
            }
        }

        /**
         * Mark all the item dictionaries used matching the given flags
         *
         * @param writeFlags   WRITE_MERGED is ignored
         * @param parentFields parent fields
         * @since 2.1.5
         */
        public void markUsed(AcroFields parentFields, int writeFlags) {
            if ((writeFlags & WRITE_VALUE) != 0) {
                for (int i = 0; i < size(); ++i) {
                    parentFields.markUsed(getValue(i));
                }
            }
            if ((writeFlags & WRITE_WIDGET) != 0) {
                for (int i = 0; i < size(); ++i) {
                    parentFields.markUsed(getWidget(i));
                }
            }
        }

        /**
         * Preferred method of determining the number of instances of a given field.
         *
         * @return number of instances
         * @since 2.1.5
         */
        public int size() {
            return values.size();
        }

        /**
         * Remove the given instance from this item.  It is possible to remove all instances using this function.
         *
         * @since 2.1.5
         */
        public void remove(int killIdx) {
            values.remove(killIdx);
            widgets.remove(killIdx);
            widgetRefs.remove(killIdx);
            merged.remove(killIdx);
            page.remove(killIdx);
            tabOrder.remove(killIdx);
        }

        /**
         * Retrieve the value dictionary of the given instance
         *
         * @param idx instance index
         * @return dictionary storing this instance's value.  It may be shared across instances.
         * @since 2.1.5
         */
        public PdfDictionary getValue(int idx) {
            return values.get(idx);
        }

        /**
         * Add a value dict to this Item
         *
         * @param value new value dictionary
         * @since 2.1.5
         */
        void addValue(PdfDictionary value) {
            values.add(value);
        }

        /**
         * Retrieve the widget dictionary of the given instance
         *
         * @param idx instance index
         * @return The dictionary found in the appropriate page's Annot array.
         * @since 2.1.5
         */
        public PdfDictionary getWidget(int idx) {
            return widgets.get(idx);
        }

        /**
         * Add a widget dict to this Item
         *
         * @since 2.1.5
         */
        void addWidget(PdfDictionary widget) {
            widgets.add(widget);
        }

        /**
         * Retrieve the reference to the given instance
         *
         * @param idx instance index
         * @return reference to the given field instance
         * @since 2.1.5
         */
        public PdfIndirectReference getWidgetRef(int idx) {
            return widgetRefs.get(idx);
        }

        /**
         * Add a widget ref to this Item
         *
         * @since 2.1.5
         */
        void addWidgetRef(PdfIndirectReference widgRef) {
            widgetRefs.add(widgRef);
        }

        /**
         * Retrieve the merged dictionary for the given instance.  The merged dictionary contains all the keys present
         * in parent fields, though they may have been overwritten (or modified?) by children. Example: a merged radio
         * field dict will contain /V
         *
         * @param idx instance index
         * @return the merged dictionary for the given instance
         * @since 2.1.5
         */
        public PdfDictionary getMerged(int idx) {
            return merged.get(idx);
        }

        /**
         * Adds a merged dictionary to this Item.
         *
         * @since 2.1.5
         */
        void addMerged(PdfDictionary mergeDict) {
            merged.add(mergeDict);
        }

        /**
         * Retrieve the page number of the given instance
         *
         * @param idx index
         * @return remember, pages are "1-indexed", not "0-indexed" like field instances.
         * @since 2.1.5
         */
        public Integer getPage(int idx) {
            return page.get(idx);
        }

        /**
         * Adds a page to the current Item.
         *
         * @since 2.1.5
         */
        void addPage(int pg) {
            page.add(pg);
        }

        /**
         * forces a page value into the Item.
         *
         * @since 2.1.5
         */
        void forcePage(int idx, int pg) {
            page.set(idx, pg);
        }

        /**
         * Gets the tabOrder.
         *
         * @param idx index
         * @return tab index of the given field instance
         * @since 2.1.5
         */
        public Integer getTabOrder(int idx) {
            return tabOrder.get(idx);
        }

        /**
         * Adds a tab order value to this Item.
         *
         * @param order order for the tab
         * @since 2.1.5
         */
        void addTabOrder(int order) {
            tabOrder.add(order);
        }

        /**
         * Returns the indirect reference of the field itself
         *
         * @return PdfIndirectReferenceof the field
         */
        public PdfIndirectReference getFieldReference() {
            return this.fieldReference;
        }
    }

    private static class InstHit {

        IntHashtable hits;

        public InstHit(int[] inst) {
            if (inst == null) {
                return;
            }
            hits = new IntHashtable();
            for (int i : inst) {
                hits.put(i, 1);
            }
        }

        public boolean isHit(int n) {
            if (hits == null) {
                return true;
            }
            return hits.containsKey(n);
        }
    }

    private static class RevisionStream extends InputStream {

        private final byte[] b = new byte[1];
        private final RandomAccessFileOrArray raf;
        private final int length;
        private int rangePosition = 0;
        private boolean closed;

        private RevisionStream(RandomAccessFileOrArray raf, int length) {
            this.raf = raf;
            this.length = length;
        }

        public int read() throws IOException {
            int n = read(b);
            if (n != 1) {
                return -1;
            }
            return b[0] & 0xff;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            } else if ((off < 0) || (off > b.length) || (len < 0) ||
                    ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }
            if (rangePosition >= length) {
                close();
                return -1;
            }
            int elen = Math.min(len, length - rangePosition);
            raf.readFully(b, off, elen);
            rangePosition += elen;
            return elen;
        }

        @Override
        public void close() throws IOException {
            if (!closed) {
                raf.close();
                closed = true;
            }
        }
    }

    private static class SorterComparator implements Comparator<Object[]> {

        public int compare(Object[] o1, Object[] o2) {
            int n1 = ((int[]) o1[1])[0];
            int n2 = ((int[]) o2[1])[0];
            return n1 - n2;
        }
    }

}
