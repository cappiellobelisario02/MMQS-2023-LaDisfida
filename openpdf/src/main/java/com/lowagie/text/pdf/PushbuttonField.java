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

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.TransformationMatrix;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.AnnotationException;
import java.io.IOException;

/**
 * Creates a pushbutton field. It supports all the text and icon alignments. The icon may be an image or a template.
 * Example usage:
 * <PRE>
 * Document document = new Document(PageSize.A4, 50, 50, 50, 50); PdfWriter writer = PdfWriter.getInstance(document, new
 * FileOutputStream("output.pdf")); document.open(); PdfContentByte cb = writer.getDirectContent(); Image img =
 * Image.getInstance("image.png"); PushbuttonField bt = new PushbuttonField(writer, new Rectangle(100, 100, 200, 200),
 * "Button1"); bt.setText("My Caption"); bt.setFontSize(0); bt.setImage(img);
 * bt.setLayout(PushbuttonField.LAYOUT_ICON_TOP_LABEL_BOTTOM); bt.setBackgroundColor(Color.cyan);
 * bt.setBorderStyle(PdfBorderDictionary.STYLE_SOLID); bt.setBorderColor(Color.red); bt.setBorderWidth(3); PdfFormField
 * ff = bt.getField(); PdfAction ac = PdfAction.createSubmitForm("http://www.submit-site.com", null, 0);
 * ff.setAction(ac); writer.addAnnotation(ff); document.close();
 * </PRE>
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PushbuttonField extends BaseField {

    float textX;
    float textY;
    Rectangle r2;
    PdfArray matrix;

    /**
     * A layout option
     */
    public static final int LAYOUT_LABEL_ONLY = 1;
    /**
     * A layout option
     */
    public static final int LAYOUT_ICON_ONLY = 2;
    /**
     * A layout option
     */
    public static final int LAYOUT_ICON_TOP_LABEL_BOTTOM = 3;
    /**
     * A layout option
     */
    public static final int LAYOUT_LABEL_TOP_ICON_BOTTOM = 4;
    /**
     * A layout option
     */
    public static final int LAYOUT_ICON_LEFT_LABEL_RIGHT = 5;
    /**
     * A layout option
     */
    public static final int LAYOUT_LABEL_LEFT_ICON_RIGHT = 6;
    /**
     * A layout option
     */
    public static final int LAYOUT_LABEL_OVER_ICON = 7;
    /**
     * An icon scaling option
     */
    public static final int SCALE_ICON_ALWAYS = 1;
    /**
     * An icon scaling option
     */
    public static final int SCALE_ICON_NEVER = 2;
    /**
     * An icon scaling option
     */
    public static final int SCALE_ICON_IS_TOO_BIG = 3;
    /**
     * An icon scaling option
     */
    public static final int SCALE_ICON_IS_TOO_SMALL = 4;

    /**
     * Holds value of property layout.
     */
    private int layout = LAYOUT_LABEL_ONLY;

    /**
     * Holds value of property image.
     */
    private Image image;

    /**
     * Holds value of property template.
     */
    private PdfTemplate template;

    /**
     * Holds value of property scaleIcon.
     */
    private int scaleIcon = SCALE_ICON_ALWAYS;

    /**
     * Holds value of property proportionalIcon.
     */
    private boolean proportionalIcon = true;

    /**
     * Holds value of property iconVerticalAdjustment.
     */
    private float iconVerticalAdjustment = 0.5f;

    /**
     * Holds value of property iconHorizontalAdjustment.
     */
    private float iconHorizontalAdjustment = 0.5f;

    /**
     * Holds value of property iconFitToBounds.
     */
    private boolean iconFitToBounds;

    private PdfTemplate tp;
    /**
     * Holds value of property iconReference.
     */
    private PRIndirectReference iconReference;

    /**
     * Creates a new instance of PushbuttonField
     *
     * @param writer    the document <CODE>PdfWriter</CODE>
     * @param box       the field location and dimensions
     * @param fieldName the field getName. If <CODE>null</CODE> only the widget keys will be included in the field allowing
     *                  it to be used as a kid field.
     */
    public PushbuttonField(PdfWriter writer, Rectangle box, String fieldName) {
        super(writer, box, fieldName);
    }

    /**
     * Getter for property layout.
     *
     * @return Value of property layout.
     */
    public int getLayout() {
        return this.layout;
    }

    /**
     * Sets the icon and label layout. Possible values are <CODE>LAYOUT_LABEL_ONLY</CODE>,
     * <CODE>LAYOUT_ICON_ONLY</CODE>, <CODE>LAYOUT_ICON_TOP_LABEL_BOTTOM</CODE>,
     * <CODE>LAYOUT_LABEL_TOP_ICON_BOTTOM</CODE>, <CODE>LAYOUT_ICON_LEFT_LABEL_RIGHT</CODE>,
     * <CODE>LAYOUT_LABEL_LEFT_ICON_RIGHT</CODE> and <CODE>LAYOUT_LABEL_OVER_ICON</CODE>.
     * The default is <CODE>LAYOUT_LABEL_ONLY</CODE>.
     *
     * @param layout New value of property layout.
     */
    public void setLayout(int layout) {
        if (layout < LAYOUT_LABEL_ONLY || layout > LAYOUT_LABEL_OVER_ICON) {
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("layout.out.of.bounds"));
        }
        this.layout = layout;
    }

    /**
     * Getter for property image.
     *
     * @return Value of property image.
     */
    public Image getImage() {
        return this.image;
    }

    /**
     * Sets the icon as an image.
     *
     * @param image the image
     */
    public void setImage(Image image) {
        this.image = image;
        template = null;
    }

    /**
     * Getter for property template.
     *
     * @return Value of property template.
     */
    public PdfTemplate getTemplate() {
        return this.template;
    }

    /**
     * Sets the icon as a template.
     *
     * @param template the template
     */
    public void setTemplate(PdfTemplate template) {
        this.template = template;
        image = null;
    }

    /**
     * Getter for property scaleIcon.
     *
     * @return Value of property scaleIcon.
     */
    public int getScaleIcon() {
        return this.scaleIcon;
    }

    /**
     * Sets the way the icon will be scaled. Possible values are
     * <CODE>SCALE_ICON_ALWAYS</CODE>, <CODE>SCALE_ICON_NEVER</CODE>,
     * <CODE>SCALE_ICON_IS_TOO_BIG</CODE> and <CODE>SCALE_ICON_IS_TOO_SMALL</CODE>.
     * The default is <CODE>SCALE_ICON_ALWAYS</CODE>.
     *
     * @param scaleIcon the way the icon will be scaled
     */
    public void setScaleIcon(int scaleIcon) {
        if (scaleIcon < SCALE_ICON_ALWAYS || scaleIcon > SCALE_ICON_IS_TOO_SMALL) {
            scaleIcon = SCALE_ICON_ALWAYS;
        }
        this.scaleIcon = scaleIcon;
    }

    /**
     * Getter for property proportionalIcon.
     *
     * @return Value of property proportionalIcon.
     */
    public boolean isProportionalIcon() {
        return this.proportionalIcon;
    }

    /**
     * Sets the way the icon is scaled. If <CODE>true</CODE> the icon is scaled proportionally, if <CODE>false</CODE>
     * the scaling is done anamorphicaly.
     *
     * @param proportionalIcon the way the icon is scaled
     */
    public void setProportionalIcon(boolean proportionalIcon) {
        this.proportionalIcon = proportionalIcon;
    }

    /**
     * Getter for property iconVerticalAdjustment.
     *
     * @return Value of property iconVerticalAdjustment.
     */
    public float getIconVerticalAdjustment() {
        return this.iconVerticalAdjustment;
    }

    /**
     * A number between 0 and 1 indicating the fraction of leftover space to allocate at the bottom of the icon. A value
     * of 0 positions the icon at the bottom of the annotation rectangle. A value of 0.5 centers it within the
     * rectangle. The default is 0.5.
     *
     * @param iconVerticalAdjustment a number between 0 and 1 indicating the fraction of leftover space to allocate at
     *                               the bottom of the icon
     */
    public void setIconVerticalAdjustment(float iconVerticalAdjustment) {
        if (iconVerticalAdjustment < 0) {
            iconVerticalAdjustment = 0;
        } else if (iconVerticalAdjustment > 1) {
            iconVerticalAdjustment = 1;
        }
        this.iconVerticalAdjustment = iconVerticalAdjustment;
    }

    /**
     * Getter for property iconHorizontalAdjustment.
     *
     * @return Value of property iconHorizontalAdjustment.
     */
    public float getIconHorizontalAdjustment() {
        return this.iconHorizontalAdjustment;
    }

    /**
     * A number between 0 and 1 indicating the fraction of leftover space to allocate at the left of the icon. A value
     * of 0 positions the icon at the left of the annotation rectangle. A value of 0.5 centers it within the rectangle.
     * The default is 0.5.
     *
     * @param iconHorizontalAdjustment a number between 0 and 1 indicating the fraction of leftover space to allocate at
     *                                 the left of the icon
     */
    public void setIconHorizontalAdjustment(float iconHorizontalAdjustment) {
        if (iconHorizontalAdjustment < 0) {
            iconHorizontalAdjustment = 0;
        } else if (iconHorizontalAdjustment > 1) {
            iconHorizontalAdjustment = 1;
        }
        this.iconHorizontalAdjustment = iconHorizontalAdjustment;
    }

    private float calculateFontSize(float w, float h) throws IOException, DocumentException {
        BaseFont ufont = getRealFont();
        float fsize = fontSize;
        if (fsize == 0) {
            float bw = ufont.getWidthPoint(text, 1);
            if (bw == 0) {
                fsize = 12;
            } else {
                fsize = w / bw;
            }
            float nfsize = h / (1 - ufont.getFontDescriptor(BaseFont.DESCENT, 1));
            fsize = Math.min(fsize, nfsize);
            if (fsize < 4) {
                fsize = 4;
            }
        }
        return fsize;
    }

    /**
     * Gets the button appearance.
     *
     * @return the button appearance
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public PdfAppearance getAppearance() throws IOException, DocumentException {
        BoxSettings boxSettings = new BoxSettings(super.box, super.rotation);
        AppearanceSettings appearanceSettings = new AppearanceSettings(super.backgroundColor,
                super.borderStyle, super.borderWidth, super.borderColor, super.options, super.maxCharacterLength);
        PdfAppearance app = getBorderAppearance(super.writer, boxSettings, appearanceSettings);
        Rectangle box = new Rectangle(app.getBoundingBox());

        if (shouldReturnAppAsIs()) {
            return app;
        }

        BaseFont ufont = getRealFont();
        boolean borderExtra = isBorderExtra();
        float bw2 = borderWidth;
        if (borderExtra) {
            bw2 *= 2;
        }
        float offsetX = (borderExtra ? 2 * borderWidth : borderWidth);
        offsetX = Math.max(offsetX, 1);
        float offX = Math.min(bw2, offsetX);

        tp = null;
        textX = Float.NaN;
        textY = 0;
        float fsize = fontSize;
        float wt = box.getWidth() - 2 * offX - 2;
        float ht = box.getHeight() - 2 * offX;
        float adj = (iconFitToBounds ? 0 : offX + 1);

        int nlayout = determineLayout();
        Rectangle iconBox = determineIconBox(nlayout, box, wt, ht, offX, adj);

        if (textY < box.getBottom() + offX) {
            textY = box.getBottom() + offX;
        }
        if (iconBox != null && (iconBox.getWidth() <= 0 || iconBox.getHeight() <= 0)) {
            iconBox = null;
        }

        handleIcon(app, iconBox);

        if (!Float.isNaN(textX)) {
            drawText(app, ufont, fsize, offX);
        }

        return app;
    }

    private boolean shouldReturnAppAsIs() {
        return (text == null || text.length() == 0) &&
                (layout == LAYOUT_LABEL_ONLY || (image == null && template == null && iconReference == null))
                || (layout == LAYOUT_ICON_ONLY && image == null && template == null && iconReference == null);
    }

    private boolean isBorderExtra() {
        return borderStyle == PdfBorderDictionary.STYLE_BEVELED || borderStyle == PdfBorderDictionary.STYLE_INSET;
    }

    private int determineLayout() {
        if (image == null && template == null && iconReference == null) {
            return LAYOUT_LABEL_ONLY;
        }
        return layout;
    }

    private Rectangle determineIconBox(int nlayout, Rectangle box, float wt, float ht, float offX, float adj)
            throws IOException {
        Rectangle iconBox = null;
        switch (nlayout) {
            case LAYOUT_LABEL_ONLY, LAYOUT_LABEL_OVER_ICON:
                if (text != null && !text.isEmpty() && wt > 0 && ht > 0) {
                    float fsize = calculateFontSize(wt, ht);
                    textX = (box.getWidth() - getRealFont().getWidthPoint(text, fsize)) / 2;
                    textY = (box.getHeight() - getRealFont().getFontDescriptor(BaseFont.ASCENT, fsize)) / 2;
                }
                // fallthrough
            case LAYOUT_ICON_ONLY:
                if (nlayout == LAYOUT_LABEL_OVER_ICON || nlayout == LAYOUT_ICON_ONLY) {
                    iconBox = new Rectangle(box.getLeft() + adj, box.getBottom() + adj, box.getRight() - adj,
                            box.getTop() - adj);
                }
                break;
            case LAYOUT_ICON_TOP_LABEL_BOTTOM, LAYOUT_LABEL_TOP_ICON_BOTTOM:
                iconBox = handleVerticalLayouts(nlayout, box, wt, offX, adj, ht);
                break;
            case LAYOUT_LABEL_LEFT_ICON_RIGHT, LAYOUT_ICON_LEFT_LABEL_RIGHT:
                iconBox = handleHorizontalLayouts(nlayout, box, wt, offX, adj);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + nlayout);
        }
        return iconBox;
    }

    private Rectangle handleVerticalLayouts(int nlayout, Rectangle box, float wt, float offX, float adj, float ht)
            throws IOException {
        if (text == null || text.isEmpty() || wt <= 0 || ht <= 0) {
            nlayout = LAYOUT_ICON_ONLY;
        }
        float nht = box.getHeight() * 0.35f - offX;
        float fsize = nht > 0 ? calculateFontSize(wt, nht) : 4;
        textY = nlayout == LAYOUT_ICON_TOP_LABEL_BOTTOM
                ? offX - getRealFont().getFontDescriptor(BaseFont.DESCENT, fsize)
                : box.getHeight() - offX - fsize;
        if (textY < offX) {
            textY = offX;
        }
        return new Rectangle(box.getLeft() + adj, nlayout == LAYOUT_ICON_TOP_LABEL_BOTTOM
                ? textY + fsize
                : box.getBottom() + adj,
                box.getRight() - adj,
                nlayout == LAYOUT_ICON_TOP_LABEL_BOTTOM
                        ? box.getTop() - adj
                        : textY + getRealFont().getFontDescriptor(BaseFont.DESCENT, fsize));
    }

    private Rectangle handleHorizontalLayouts(int nlayout, Rectangle box, float wt, float offX, float adj)
            throws IOException {
        float nw = box.getWidth() * 0.35f - offX;
        float fsize = nw > 0 ? calculateFontSize(wt, nw) : 4;
        if (getRealFont().getWidthPoint(text, fsize) >= wt) {
            nlayout = LAYOUT_LABEL_ONLY;
            fsize = fontSize;
        }
        textX = nlayout == LAYOUT_LABEL_LEFT_ICON_RIGHT
                ? offX + 1
                : box.getWidth() - getRealFont().getWidthPoint(text, fsize) - offX - 1;
        return new Rectangle(textX + getRealFont().getWidthPoint(text, fsize), box.getBottom() + adj,
                box.getRight() - adj, box.getTop() - adj);
    }

    private void handleIcon(PdfAppearance app, Rectangle iconBox) throws IOException, DocumentException {
        if (iconBox != null) {
            boolean haveIcon = false;
            float boundingBoxWidth = 0;
            float boundingBoxHeight = 0;
            matrix = null;
            if (image != null) {
                setupTemplateFromImage();
                haveIcon = true;
                boundingBoxWidth = tp.getBoundingBox().getWidth();
                boundingBoxHeight = tp.getBoundingBox().getHeight();
            } else if (template != null) {
                setupTemplateFromTemplate();
                haveIcon = true;
                boundingBoxWidth = tp.getBoundingBox().getWidth();
                boundingBoxHeight = tp.getBoundingBox().getHeight();
            } else if (iconReference != null) {
                setupTemplateFromReference();
                haveIcon = true;
                boundingBoxWidth = r2.getWidth();
                boundingBoxHeight = r2.getHeight();
            }
            if (haveIcon) {
                scaleAndDrawIcon(app, iconBox, boundingBoxWidth, boundingBoxHeight);
            }
        }
    }

    private void setupTemplateFromImage() throws DocumentException {
        tp = new PdfTemplate(writer);
        tp.setBoundingBox(new Rectangle(image.getWidth(), image.getHeight()));
        writer.addDirectTemplateSimple(tp, PdfName.FRM);
        tp.addImage(image, image.getWidth(), 0, 0, image.getHeight(), 0, 0);
    }

    private void setupTemplateFromTemplate() throws DocumentException {
        tp = new PdfTemplate(writer);
        tp.setBoundingBox(new Rectangle(template.getWidth(), template.getHeight()));
        writer.addDirectTemplateSimple(tp, PdfName.FRM);
        tp.addTemplate(template, template.getBoundingBox().getLeft(), template.getBoundingBox().getBottom());
    }

    private void setupTemplateFromReference() {
        PdfDictionary dic = (PdfDictionary) PdfReader.getPdfObject(iconReference);
        if (dic != null) {
            r2 = PdfReader.getNormalizedRectangle(dic.getAsArray(PdfName.BBOX));
            matrix = dic.getAsArray(PdfName.MATRIX);
        }
    }

    private void scaleAndDrawIcon(PdfAppearance app, Rectangle iconBox, float boundingBoxWidth, float boundingBoxHeight) {
        float icx = iconBox.getWidth() / boundingBoxWidth;
        float icy = iconBox.getHeight() / boundingBoxHeight;
        icx = adjustIconScale(icx, icy);
        icy = icx;
        float xpos = iconBox.getLeft() + (iconBox.getWidth() - (boundingBoxWidth * icx)) * iconHorizontalAdjustment;
        float ypos = iconBox.getBottom() + (iconBox.getHeight() - (boundingBoxHeight * icy)) * iconVerticalAdjustment;
        app.saveState();
        app.rectangle(iconBox.getLeft(), iconBox.getBottom(), iconBox.getWidth(), iconBox.getHeight());
        app.clip();
        app.newPath();
        if (tp != null) {
            app.addTemplate(tp, icx, 0, 0, icy, xpos, ypos);
        } else {
            float cox = 0;
            float coy = 0;
            if (matrix != null && matrix.size() == 6) {
                PdfNumber nm = matrix.getAsNumber(4);
                if (nm != null) {
                    cox = nm.floatValue();
                }
                nm = matrix.getAsNumber(5);
                if (nm != null) {
                    coy = nm.floatValue();
                }
            }
            TransformationMatrix transformationMatrix = new TransformationMatrix(icx, 0, 0, icy,
                    xpos - cox * icx, ypos - coy * icy);
            app.addTemplateReference(iconReference, PdfName.FRM, transformationMatrix);
        }
        app.restoreState();
    }

    private float adjustIconScale(float icx, float icy) {
        switch (scaleIcon) {
            case SCALE_ICON_IS_TOO_BIG:
                return Math.min(icx, icy);
            case SCALE_ICON_IS_TOO_SMALL:
                return Math.max(icx, icy);
            case SCALE_ICON_NEVER:
                return 1;
            default:
                return Math.min(icx, icy);
        }
    }

    private void drawText(PdfAppearance app, BaseFont ufont, float fsize, float offX) {
        app.saveState();
        app.rectangle(offX, offX, box.getWidth() - 2 * offX, box.getHeight() - 2 * offX);
        app.clip();
        app.newPath();
        if (textColor == null) {
            app.resetGrayFill();
        } else {
            app.setColorFill(textColor);
        }
        app.beginText();
        app.setFontAndSize(ufont, fsize);
        app.setTextMatrix(textX, textY);
        app.showText(text);
        app.endText();
        app.restoreState();
    }


    /**
     * Gets the pushbutton field.
     *
     * @return the pushbutton field
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public PdfFormField getField() throws IOException, DocumentException {
        PdfFormField field = createBasicPushButtonField();
        setFieldProperties(field);
        setAppearance(field);
        setFieldColor(field);
        setFieldVisibility(field);
        setIconAndScale(field);
        return field;
    }

    private PdfFormField createBasicPushButtonField() throws DocumentException {
        PdfFormField field = PdfFormField.createPushButton(writer);
        field.setWidget(box, PdfAnnotation.HIGHLIGHT_INVERT);
        return field;
    }

    private void setFieldProperties(PdfFormField field) {
        if (fieldName != null) {
            field.setFieldName(fieldName);
            if ((options & READ_ONLY) != 0) {
                field.setFieldFlags(PdfFormField.FF_READ_ONLY);
            }
            if ((options & REQUIRED) != 0) {
                field.setFieldFlags(PdfFormField.FF_REQUIRED);
            }
        }
        if (text != null) {
            field.setMKNormalCaption(text);
        }
        if (rotation != 0) {
            field.setMKRotation(rotation);
        }
        field.setBorderStyle(new PdfBorderDictionary(borderWidth, borderStyle, new PdfDashPattern(3)));
    }

    private void setAppearance(PdfFormField field) throws IOException, DocumentException {
        PdfAppearance tpa = getAppearance();
        field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, tpa);
        PdfAppearance da = (PdfAppearance) tpa.getDuplicate();
        da.setFontAndSize(getRealFont(), fontSize);
        if (textColor == null) {
            da.setGrayFill(0);
        } else {
            da.setColorFill(textColor);
        }
        field.setDefaultAppearanceString(da);
    }

    private void setFieldColor(PdfFormField field) throws AnnotationException {
        if (borderColor != null) {
            field.setMKBorderColor(borderColor);
        }
        if (backgroundColor != null) {
            field.setMKBackgroundColor(backgroundColor);
        }
    }

    private void setFieldVisibility(PdfFormField field) {
        switch (visibility) {
            case HIDDEN:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_HIDDEN);
                break;
            case HIDDEN_BUT_PRINTABLE:
                field.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_NOVIEW);
                break;
            default:
                field.setFlags(PdfAnnotation.FLAGS_PRINT);
                break;
        }
    }

    private void setIconAndScale(PdfFormField field) {
        if (tp != null) {
            field.setMKNormalIcon(tp);
        }
        field.setMKTextPosition(layout - 1);
        PdfName scale = determineScaleIcon();
        field.setMKIconFit(scale, proportionalIcon ? PdfName.P : PdfName.A, iconHorizontalAdjustment,
                iconVerticalAdjustment, iconFitToBounds);
    }

    private PdfName determineScaleIcon() {
        return switch (scaleIcon) {
            case SCALE_ICON_IS_TOO_BIG -> PdfName.B;
            case SCALE_ICON_IS_TOO_SMALL -> PdfName.S;
            case SCALE_ICON_NEVER -> PdfName.N;
            default -> PdfName.A;
        };
    }


    /**
     * Getter for property iconFitToBounds.
     *
     * @return Value of property iconFitToBounds.
     */
    public boolean isIconFitToBounds() {
        return this.iconFitToBounds;
    }

    /**
     * If <CODE>true</CODE> the icon will be scaled to fit fully within the bounds of the annotation, if
     * <CODE>false</CODE> the border width will be taken into account. The default is <CODE>false</CODE>.
     *
     * @param iconFitToBounds if <CODE>true</CODE> the icon will be scaled to fit fully within the bounds of the
     *                        annotation, if <CODE>false</CODE> the border width will be taken into account
     */
    public void setIconFitToBounds(boolean iconFitToBounds) {
        this.iconFitToBounds = iconFitToBounds;
    }

    /**
     * Gets the reference to an existing icon.
     *
     * @return the reference to an existing icon.
     */
    public PRIndirectReference getIconReference() {
        return this.iconReference;
    }

    /**
     * Sets the reference to an existing icon.
     *
     * @param iconReference the reference to an existing icon
     */
    public void setIconReference(PRIndirectReference iconReference) {
        this.iconReference = iconReference;
    }

}
