/*
 * $Id: PdfAcroForm.java 3912 2009-04-26 08:38:15Z blowagie $
 *
 * Copyright 2002 Bruno Lowagie
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

import com.lowagie.text.Rectangle;
import com.lowagie.text.exceptions.AnnotationException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Each PDF document can contain maximum 1 AcroForm.
 */

public class PdfAcroForm extends PdfDictionary {

    private PdfWriter writer;

    public void drawButton(ResetButtonConfig config) {
        PdfFormField button = config.getButton();
        String caption = config.getCaption();
        BaseFont font = config.getFont();
        float fontSize = config.getFontSize();
        float llx = config.getLlx();
        float lly = config.getLly();
        float urx = config.getUrx();
        float ury = config.getUry();
    
        PdfAppearance pa = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        pa.drawButton(0f, 0f, urx - llx, ury - lly, caption, font, fontSize);
        button.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, pa);
    }

    public void drawSingleLineOfText(TextDrawingConfig config) {
    PdfAppearance tp = PdfAppearance.createAppearance(writer, config.getUrx() - config.getLlx(), config.getUry() - config.getLly());
    PdfAppearance tp2 = (PdfAppearance) tp.getDuplicate();
    tp2.setFontAndSize(config.getFont(), config.getFontSize());
    tp2.resetRGBColorFill();
    config.getField().setDefaultAppearanceString(tp2);
    tp.drawTextField(0f, 0f, config.getUrx() - config.getLlx(), config.getUry() - config.getLly());
    tp.beginVariableText();
    tp.saveState();
    tp.rectangle(3f, 3f, config.getUrx() - config.getLlx() - 6f, config.getUry() - config.getLly() - 6f);
    tp.clip();
    tp.newPath();
    tp.beginText();
    tp.setFontAndSize(config.getFont(), config.getFontSize());
    tp.resetRGBColorFill();
    tp.setTextMatrix(4, (config.getUry() - config.getLly()) / 2 - (config.getFontSize() * 0.3f));
    tp.showText(config.getText());
    tp.endText();
    tp.restoreState();
    tp.endVariableText();
    config.getField().setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp);
    }

    // Create the configuration object

    // Create a new text field
    PdfFormField field = PdfFormField.createTextField(writer, PdfFormField.SINGLELINE, PdfFormField.PLAINTEXT, 0);

    /**
     * This is a map containing FieldTemplates.
     */
    private Map<PdfTemplate, Object> fieldTemplates = new HashMap<>();

    /**
     * This is an array containing DocumentFields.
     */
    private PdfArray documentFields = new PdfArray();

    /**
     * This is an array containing the calculationorder of the fields.
     */
    private PdfArray calculationOrder = new PdfArray();

    /**
     * Contains the signature flags.
     */
    private int sigFlags = 0;

    /**
     * Creates new PdfAcroForm
     *
     * @param writer {@link PdfWriter}
     */
    public PdfAcroForm(PdfWriter writer) {
        this.writer = writer;
    }

    public void setNeedAppearances(boolean value) {
        put(PdfName.NEEDAPPEARANCES, new PdfBoolean(value));
    }


    /**
     * Adds fieldTemplates.
     *
     * @param ft field templates
     */

    public void addFieldTemplates(Map<PdfTemplate, Object> ft) {
        fieldTemplates.putAll(ft);
    }

    /**
     * Adds documentFields.
     *
     * @param ref an object of {@link PdfIndirectReference}
     */

    public void addDocumentField(PdfIndirectReference ref) {
        documentFields.add(ref);
    }

    /**
     * Checks if the Acroform is valid
     *
     * @return true if the Acroform is valid
     */

    public boolean isValid() {
        if (documentFields.isEmpty()) {
            return false;
        }
        put(PdfName.FIELDS, documentFields);
        if (sigFlags != 0) {
            put(PdfName.SIGFLAGS, new PdfNumber(sigFlags));
        }
        if (!calculationOrder.isEmpty()) {
            put(PdfName.CO, calculationOrder);
        }
        if (fieldTemplates.isEmpty()) {
            return true;
        }
        PdfDictionary dic = new PdfDictionary();
        for (PdfTemplate o : fieldTemplates.keySet()) {
            PdfFormField.mergeResources(dic, (PdfDictionary) o.getResources());
        }
        put(PdfName.DR, dic);
        put(PdfName.DA, new PdfString("/Helv 0 Tf 0 g "));
        PdfDictionary fonts = (PdfDictionary) dic.get(PdfName.FONT);
        if (fonts != null) {
            writer.eliminateFontSubset(fonts);
        }
        return true;
    }

    /**
     * Adds an object to the calculationOrder.
     *
     * @param formField an object of {@link PdfFormField}
     */

    public void addCalculationOrder(PdfFormField formField) {
        calculationOrder.add(formField.getIndirectReference());
    }

    /**
     * Sets the signature flags.
     *
     * @param f signature flag
     */

    public void setSigFlags(int f) {
        sigFlags |= f;
    }

    /**
     * Adds a formfield to the AcroForm.
     *
     * @param formField an object of {@link PdfFormField}
     */

    public void addFormField(PdfFormField formField) {
        try{
            writer.addAnnotation(formField);
        }catch(IOException e){
            //may need some logging
        }
    }

    /**
     * @param config HTML POST button configuration
     * @return a PdfFormField
     */
    public PdfFormField addHtmlPostButton(HtmlPostButtonConfig config) {
        PdfAction action = PdfAction.createSubmitForm(config.getUrl(), null, PdfAction.SUBMIT_HTML_FORMAT);
        PdfFormField button = new PdfFormField(writer, config.getLlx(), config.getLly(), config.getUrx(), config.getUry(), action);
        setButtonParams(button, PdfFormField.FF_PUSHBUTTON, config.getName(), config.getValue());


        
        drawButton(config);
        
        addFormField(button);
        return button;
    }


    /**
     * @param config Reset button configuration
     * @return a PdfFormField
     */
    public PdfFormField addResetButton(ResetButtonConfig config) {
        PdfAction action = PdfAction.createResetForm(null, 0);
        PdfFormField button = new PdfFormField(writer, config.getLlx(), config.getLly(), config.getUrx(), config.getUry(), action);
        setButtonParams(button, PdfFormField.FF_PUSHBUTTON, config.getName(), config.getValue());
        
        drawButton(config);
        
        addFormField(button);
        return button;
    }

    /**
     * @param config Map button configuration
     * @return a PdfFormField
     */
    public PdfFormField addMap(MapButtonConfig config) {
        PdfAction action = PdfAction.createSubmitForm(config.getUrl(), null, PdfAction.SUBMIT_HTML_FORMAT | PdfAction.SUBMIT_COORDINATES);
        PdfFormField button = new PdfFormField(writer, config.getLlx(), config.getLly(), config.getUrx(), config.getUry(), action);
        setButtonParams(button, PdfFormField.FF_PUSHBUTTON, config.getName(), null);

        PdfAppearance pa = PdfAppearance.createAppearance(writer, config.getUrx() - config.getLlx(), config.getUry() - config.getLly());
        pa.add(config.getAppearance());
        button.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, pa);

        addFormField(button);
        return button;
    }


    /**
     * @param button          an object of {@link PdfFormField}
     * @param characteristics characteristics
     * @param name            name of the field
     * @param value           value of the field
     */
    public void setButtonParams(PdfFormField button, int characteristics, String name, String value) {
        button.setButton(characteristics);
        button.setFlags(PdfAnnotation.FLAGS_PRINT);
        button.setPage();
        button.setFieldName(name);
        if (value != null) {
            button.setValueAsString(value);
        }
    }

    /**
     * @param config HTML POST button configuration
     */
    public void drawButton(HtmlPostButtonConfig config) {
        PdfFormField button = config.getButton();
        String caption = config.getCaption();
        BaseFont font = config.getFont();
        float fontSize = config.getFontSize();
        float llx = config.getLlx();
        float lly = config.getLly();
        float urx = config.getUrx();
        float ury = config.getUry();

        PdfAppearance pa = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        pa.drawButton(0f, 0f, urx - llx, ury - lly, caption, font, fontSize);
        button.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, pa);
    }


    /**
     * @param name  name of the field
     * @param value value of the field
     * @return a PdfFormField
     */
    public PdfFormField addHiddenField(String name, String value) {
        PdfFormField hidden = PdfFormField.createEmpty(writer);
        hidden.setFieldName(name);
        hidden.setValueAsName(value);
        addFormField(hidden);
        return hidden;
    }


    /**
     * @param textDrawingConfig Text drawing configuration
     */
    public void drawMultiLineOfText(TextDrawingConfig textDrawingConfig) {
        PdfAppearance tp = PdfAppearance.createAppearance(writer, textDrawingConfig.getUrx() - textDrawingConfig.getLlx(), textDrawingConfig.getUry() - textDrawingConfig.getLly());
        PdfAppearance tp2 = (PdfAppearance) tp.getDuplicate();
        tp2.setFontAndSize(textDrawingConfig.getFont(), textDrawingConfig.getFontSize());
        tp2.resetRGBColorFill();
        field.setDefaultAppearanceString(tp2);
        tp.drawTextField(0f, 0f, textDrawingConfig.getUrx() - textDrawingConfig.getLlx(), textDrawingConfig.getUry() - textDrawingConfig.getLly());
        tp.beginVariableText();
        tp.saveState();
        tp.rectangle(3f, 3f, textDrawingConfig.getUrx() - textDrawingConfig.getLlx() - 6f, textDrawingConfig.getUry() - textDrawingConfig.getLly() - 6f);
        tp.clip();
        tp.newPath();
        tp.beginText();
        tp.setFontAndSize(textDrawingConfig.getFont(), textDrawingConfig.getFontSize());
        tp.resetRGBColorFill();
        tp.setTextMatrix(4, 5);
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(textDrawingConfig.getText(), "\n");
        float yPos = textDrawingConfig.getUry() - textDrawingConfig.getLly();
        while (tokenizer.hasMoreTokens()) {
            yPos -= textDrawingConfig.getFontSize() * 1.2f;
            tp.showTextAligned(PdfContentByte.ALIGN_LEFT, tokenizer.nextToken(), 3, yPos, 0);
        }
        tp.endText();
        tp.restoreState();
        tp.endVariableText();
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp);
    }

    /**
     * @param name          name of the field
     * @param defaultValue  default value
     * @param noToggleToOff noToggleToOff
     * @return a PdfFormField
     */
    public PdfFormField getRadioGroup(String name, String defaultValue, boolean noToggleToOff) {
        PdfFormField radio = PdfFormField.createRadioButton(writer, noToggleToOff);
        radio.setFieldName(name);
        radio.setValueAsName(defaultValue);
        return radio;
    }

    /**
     * @param radiogroup radio group field
     * @param value      value of the field
     * @param llx        lower-left-x
     * @param lly        lower-left-y
     * @param urx        upper-right-x
     * @param ury        upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addRadioButton(PdfFormField radiogroup, String value, float llx, float lly, float urx,
            float ury) {
        PdfFormField radio = PdfFormField.createEmpty(writer);
        radio.setWidget(new Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_TOGGLE);
        String name = radiogroup.get(PdfName.V).toString().substring(1);
        if (name.equals(value)) {
            radio.setAppearanceState(value);
        } else {
            radio.setAppearanceState("Off");
        }
        drawRadioAppearences(radio, value, llx, lly, urx, ury);
        radiogroup.addKid(radio);
        return radio;
    }

    /**
     * @param field field, an object of {@link PdfFormField}
     * @param value value
     * @param llx   lower-left-x
     * @param lly   lower-left-y
     * @param urx   upper-right-x
     * @param ury   upper-right-y
     */
    public void drawRadioAppearences(PdfFormField field, String value, float llx, float lly, float urx, float ury) {
        PdfAppearance tpOn = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        tpOn.drawRadioField(0f, 0f, urx - llx, ury - lly, true);
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, value, tpOn);
        PdfAppearance tpOff = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        tpOff.drawRadioField(0f, 0f, urx - llx, ury - lly, false);
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, "Off", tpOff);
    }

    /**
     * @param params select list parameters
     * @return a PdfFormField
     */
    public PdfFormField addSelectList(SelectListParams params) throws UnsupportedOperationException{
        
        try{
            params.getElement(0);

            PdfFormField choice = PdfFormField.createList(writer, (String[]) params.getOptions(), 0);
            setChoiceParams(choice, params.getName(), params.getDefaultValue(), params.getLlx(), params.getLly(), params.getUrx(), params.getUry());
            StringBuilder text = new StringBuilder();
            for (String option : (String[]) params.getOptions()) {
                text.append(option).append('\n');
            }
            TextDrawingConfig textDrawingConfig = new TextDrawingConfig(choice, text.toString(), params.getFont(), params.getFontSize(), params.getLlx(), params.getLly(), params.getUrx(), params.getUry(), params.getName());
            drawMultiLineOfText(textDrawingConfig);
            addFormField(choice);
            return choice;

        } catch(UnsupportedOperationException e) {
            PdfFormField choice = PdfFormField.createList(writer, (String[][]) params.getOptions(), 0);
            setChoiceParams(choice, params.getName(), params.getDefaultValue(), params.getLlx(), params.getLly(), params.getUrx(), params.getUry());
            StringBuilder text = new StringBuilder();
            for (String[] option : (String[][]) params.getOptions()) {
                text.append(option[1]).append('\n');
            }
            TextDrawingConfig textDrawingConfig = new TextDrawingConfig(choice, text.toString(), params.getFont(), params.getFontSize(), params.getLlx(), params.getLly(), params.getUrx(), params.getUry(), params.getName());
            drawMultiLineOfText(textDrawingConfig);
            addFormField(choice);
            return choice;
        }

    }

    /**
     * @param params combo box parameters
     * @return a PdfFormField
     */
    public PdfFormField addComboBox(ComboBoxParams params) throws UnsupportedOperationException {
        try {
            params.getElement(0);

            PdfFormField choice = PdfFormField.createCombo(writer, params.isEditable(), (String[]) params.getOptions(), 0);
            setChoiceParams(choice, params.getName(), params.getDefaultValue(), params.getLlx(), params.getLly(), 
                params.getUrx(), params.getUry());
            if (params.getDefaultValue() == null) {
                params.setDefaultValue(params.getElement(0));
            }
            TextDrawingConfig config = new TextDrawingConfig(choice, params.getDefaultValue(), params.getFont(), 
                params.getFontSize(), params.getLlx(), params.getLly(), params.getUrx(), params.getUry(), 
                params.getName());
            drawSingleLineOfText(config);
            addFormField(choice);
            return choice;
            
        } catch(UnsupportedOperationException e) {
            PdfFormField choice = PdfFormField.createCombo(writer, params.isEditable(), (String[][]) params.getOptions(), 0);
            setChoiceParams(choice, params.getName(), params.getDefaultValue(), params.getLlx(), params.getLly(), 
                params.getUrx(), params.getUry());
            String value = null;
            for (String[] option : (String[][]) params.getOptions()) {
                if (option[0].equals(params.getDefaultValue())) {
                    value = option[1];
                    break;
                }
            }
            if (value == null) {
                value = params.getElement(0, 1);
            }
            TextDrawingConfig config = new TextDrawingConfig(choice, value, params.getFont(), 
                params.getFontSize(), params.getLlx(), params.getLly(), params.getUrx(), params.getUry(), 
                params.getName());
            drawSingleLineOfText(config);
            addFormField(choice);
            return choice;
        }
        
    }

    /**
     * @param field        field, an object of {@link PdfFormField}
     * @param name         field name
     * @param defaultValue default calue
     * @param llx          lower-left-x
     * @param lly          lower-left-y
     * @param urx          upper-right-x
     * @param ury          upper-right-y
     */
    public void setChoiceParams(PdfFormField field, String name, String defaultValue, float llx, float lly, float urx,
            float ury) {
        field.setWidget(new Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_INVERT);
        if (defaultValue != null) {
            field.setValueAsString(defaultValue);
            field.setDefaultValueAsString(defaultValue);
        }
        field.setFieldName(name);
        field.setFlags(PdfAnnotation.FLAGS_PRINT);
        field.setPage();
        field.setBorderStyle(new PdfBorderDictionary(2, PdfBorderDictionary.STYLE_SOLID));
    }

    /**
     * @param name field name
     * @param llx  lower-left-x
     * @param lly  lower-left-y
     * @param urx  upper-right-x
     * @param ury  upper-right-y
     * @return a PdfFormField
     */
    public PdfFormField addSignature(String name,
            float llx, float lly, float urx, float ury) throws AnnotationException {
        PdfFormField signature = PdfFormField.createSignature(writer);
        setSignatureParams(signature, name, llx, lly, urx, ury);
        drawSignatureAppearences(signature, llx, lly, urx, ury);
        addFormField(signature);
        return signature;
    }

    /**
     * @param name  field name
     * @param field field, an object of {@link PdfFormField}
     * @param llx   lower-left-x
     * @param lly   lower-left-y
     * @param urx   upper-right-x
     * @param ury   upper-right-y
     */
    public void setSignatureParams(PdfFormField field, String name,
            float llx, float lly, float urx, float ury) throws AnnotationException {
        field.setWidget(new Rectangle(llx, lly, urx, ury), PdfAnnotation.HIGHLIGHT_INVERT);
        field.setFieldName(name);
        field.setFlags(PdfAnnotation.FLAGS_PRINT);
        field.setPage();
        field.setMKBorderColor(java.awt.Color.black);
        field.setMKBackgroundColor(java.awt.Color.white);
    }

    /**
     * @param field field, an object of {@link PdfFormField}
     * @param llx   lower-left-x
     * @param lly   lower-left-y
     * @param urx   upper-right-x
     * @param ury   upper-right-y
     */
    public void drawSignatureAppearences(PdfFormField field,
            float llx, float lly, float urx, float ury) {
        PdfAppearance tp = PdfAppearance.createAppearance(writer, urx - llx, ury - lly);
        tp.setGrayFill(1.0f);
        tp.rectangle(0, 0, urx - llx, ury - lly);
        tp.fill();
        tp.setGrayStroke(0);
        tp.setLineWidth(1);
        tp.rectangle(0.5f, 0.5f, urx - llx - 0.5f, ury - lly - 0.5f);
        tp.closePathStroke();
        tp.saveState();
        tp.rectangle(1, 1, urx - llx - 2, ury - lly - 2);
        tp.clip();
        tp.newPath();
        tp.restoreState();
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tp);
    }
}
