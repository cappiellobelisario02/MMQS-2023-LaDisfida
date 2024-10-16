/*
 * $Id: Annotation.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 1999, 2000, 2001, 2002 by Bruno Lowagie.
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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * An <CODE>Annotation</CODE> is a little note that can be added to a page on a document.
 *
 * @see Element
 * @see Anchor
 */

public class Annotation implements Element {

    // membervariables

    /**
     * This is a possible annotation getTypeImpl.
     */
    public static final int TEXT = 0;

    /**
     * This is a possible annotation getTypeImpl.
     */
    public static final int URL_NET = 1;

    /**
     * This is a possible annotation getTypeImpl.
     */
    public static final int URL_AS_STRING = 2;

    /**
     * This is a possible annotation getTypeImpl.
     */
    public static final int FILE_DEST = 3;

    /**
     * This is a possible annotation getTypeImpl.
     */
    public static final int FILE_PAGE = 4;

    /**
     * This is a possible annotation getTypeImpl.
     */
    public static final int NAMED_DEST = 5;

    /**
     * This is a possible annotation getTypeImpl.
     */
    public static final int LAUNCH = 6;

    /**
     * This is a possible annotation getTypeImpl.
     */
    public static final int SCREEN = 7;

    /**
     * This is a possible attribute.
     */
    public static final String TITLE = "title";

    /**
     * This is a possible attribute.
     */
    public static final String CONTENT = "content";

    /**
     * This is a possible attribute.
     */
    public static final String URL = "url";

    /**
     * This is a possible attribute.
     */
    public static final String FILE = "file";

    /**
     * This is a possible attribute.
     */
    public static final String DESTINATION = "destination";

    /**
     * This is a possible attribute.
     */
    public static final String PAGE = "page";

    /**
     * This is a possible attribute.
     */
    public static final String NAMED = "named";

    /**
     * This is a possible attribute.
     */
    public static final String APPLICATION = "application";

    /**
     * This is a possible attribute.
     */
    public static final String PARAMETERS = "parameters";

    /**
     * This is a possible attribute.
     */
    public static final String OPERATION = "operation";

    /**
     * This is a possible attribute.
     */
    public static final String DEFAULTDIR = "defaultdir";

    /**
     * This is a possible attribute.
     */
    public static final String LLX_CONST = "llx";

    /**
     * This is a possible attribute.
     */
    public static final String LLY_CONST = "lly";

    /**
     * This is a possible attribute.
     */
    public static final String URX_CONST = "urx";

    /**
     * This is a possible attribute.
     */
    public static final String URY_CONST = "ury";

    /**
     * This is a possible attribute.
     */
    public static final String MIMETYPE = "mime";

    /**
     * This is the getTypeImpl of annotation.
     */
    protected int annotationType;

    /**
     * This is the title of the <CODE>Annotation</CODE>.
     */
    protected Map<String, Object> annotationAttributes = new HashMap<>();

    /**
     * This is the lower left x-value
     */
    protected float llxParam = Float.NaN;

    protected float llx = Float.NaN;
    /**
     * This is the lower left y-value
     */
    protected float llyParam = Float.NaN;

    protected float lowerLeftY = Float.NaN;

    /**
     * This is the upper right x-value
     */
    protected float urxParam = Float.NaN;

    /**
     * This is the upper right y-value
     */
    protected float uryParam = Float.NaN;

    // constructors

    /**
     * Constructs an <CODE>Annotation</CODE> with a certain title and some text.
     *
     * @param llxParam lower left x coordinate
     * @param llyParam lower left y coordinate
     * @param urxParam upper right x coordinate
     * @param uryParam upper right y coordinate
     */
    private Annotation(float llxParam, float llyParam, float urxParam, float uryParam) {
        this.llxParam = llxParam;
        this.llyParam = llyParam;
        this.urxParam = urxParam;
        this.uryParam = uryParam;
    }

    /**
     * Copy constructor.
     *
     * @param an an object of getTypeImpl {@link com.lowagie.text.Annotation} that will be copied
     */
    public Annotation(com.lowagie.text.Annotation an) {
        annotationType = an.annotationType;
        annotationAttributes = an.annotationAttributes;
        llxParam = an.llxParam;
        llyParam = an.llyParam;
        urxParam = an.urxParam;
        uryParam = an.uryParam;
    }

    /**
     * Constructs an <CODE>Annotation</CODE> with a certain title and some text.
     *
     * @param title the title of the annotation
     * @param text  the content of the annotation
     */
    public Annotation(String title, String text) {
        annotationType = TEXT;
        annotationAttributes.put(TITLE, title);
        annotationAttributes.put(CONTENT, text);
    }

    /**
     * Constructs an <CODE>Annotation</CODE> with a certain title and some text.
     *
     * @param title the title of the annotation
     * @param text  the content of the annotation
     * @param llxParam   the lower left x-value
     * @param llyParam   the lower left y-value
     * @param urxParam   the upper right x-value
     * @param uryParam   the upper right y-value
     */
    public Annotation(String title, String text, float llxParam, float llyParam,
            float urxParam, float uryParam) {
        this(llxParam, llyParam, urxParam, uryParam);
        annotationType = TEXT;
        annotationAttributes.put(TITLE, title);
        annotationAttributes.put(CONTENT, text);
    }

    /**
     * Constructs an <CODE>Annotation</CODE>.
     *
     * @param llxParam the lower left x-value
     * @param llyParam the lower left y-value
     * @param urxParam the upper right x-value
     * @param uryParam the upper right y-value
     * @param url the external reference
     */
    public Annotation(float llxParam, float llyParam, float urxParam, float uryParam, URL url) {
        this(llxParam, llyParam, urxParam, uryParam);
        annotationType = URL_NET;
        annotationAttributes.put(URL, url);
    }

    /**
     * Constructs an <CODE>Annotation</CODE>.
     *
     * @param llxParam the lower left x-value
     * @param llyParam the lower left y-value
     * @param urxParam the upper right x-value
     * @param uryParam the upper right y-value
     * @param url the external reference
     */
    public Annotation(float llxParam, float llyParam, float urxParam, float uryParam, String url) {
        this(llxParam, llyParam, urxParam, uryParam);
        annotationType = URL_AS_STRING;
        annotationAttributes.put(FILE, url);
    }

    /**
     * Constructs an <CODE>Annotation</CODE>.
     *
     * @param llxParam  the lower left x-value
     * @param llyParam  the lower left y-value
     * @param urxParam  the upper right x-value
     * @param uryParam  the upper right y-value
     * @param file an external PDF file
     * @param dest the destination in this file
     */
    public Annotation(float llxParam, float llyParam, float urxParam, float uryParam, String file,
            String dest) {
        this(llxParam, llyParam, urxParam, uryParam);
        annotationType = FILE_DEST;
        annotationAttributes.put(FILE, file);
        annotationAttributes.put(DESTINATION, dest);
    }

    /**
     * Creates a Screen annotation to embed media clips
     *
     * @param llxParam           {@link com.lowagie.text.Annotation#llxParam}
     * @param llyParam           {@link com.lowagie.text.Annotation#llyParam}
     * @param urxParam           {@link com.lowagie.text.Annotation#urxParam}
     * @param uryParam           {@link com.lowagie.text.Annotation#uryParam}
     * @param moviePath     path to the media clip file
     * @param mimeType      mime getTypeImpl of the media
     * @param showOnDisplay if true play on display of the page
     */
    public Annotation(float llxParam, float llyParam, float urxParam, float uryParam,
            String moviePath, String mimeType, boolean showOnDisplay) {
        this(llxParam, llyParam, urxParam, uryParam);
        annotationType = SCREEN;
        annotationAttributes.put(FILE, moviePath);
        annotationAttributes.put(MIMETYPE, mimeType);
        annotationAttributes.put(PARAMETERS, new boolean[]{
                false /* embedded */, showOnDisplay});
    }

    /**
     * Constructs an <CODE>Annotation</CODE>.
     *
     * @param llxParam  the lower left x-value
     * @param llyParam  the lower left y-value
     * @param urxParam  the upper right x-value
     * @param uryParam  the upper right y-value
     * @param file an external PDF file
     * @param page a page number in this file
     */
    public Annotation(float llxParam, float llyParam, float urxParam, float uryParam, String file,
            int page) {
        this(llxParam, llyParam, urxParam, uryParam);
        annotationType = FILE_PAGE;
        annotationAttributes.put(FILE, file);
        annotationAttributes.put(PAGE, page);
    }

    /**
     * Constructs an <CODE>Annotation</CODE>.
     *
     * @param llxParam   the lower left x-value
     * @param llyParam   the lower left y-value
     * @param urxParam   the upper right x-value
     * @param uryParam   the upper right y-value
     * @param named a named destination in this file
     */
    public Annotation(float llxParam, float llyParam, float urxParam, float uryParam, int named) {
        this(llxParam, llyParam, urxParam, uryParam);
        annotationType = NAMED_DEST;
        annotationAttributes.put(NAMED, named);
    }

    // Private constructor to enforce the use of the builder
    private Annotation(com.lowagie.text.Annotation.AnnotationBuilder builder) {
        this.llxParam = builder.llx;
        this.llyParam = builder.lly;
        this.urxParam = builder.urx;
        this.uryParam = builder.ury;
        this.annotationType = LAUNCH;
        this.annotationAttributes.put(APPLICATION, builder.application);
        this.annotationAttributes.put(PARAMETERS, builder.parameters);
        this.annotationAttributes.put(OPERATION, builder.operation);
        this.annotationAttributes.put(DEFAULTDIR, builder.defaultdir);
    }

    // Inner class AnnotationBuilder
    public static class AnnotationBuilder {
        private float llx;
        private float lly;
        private float urx;
        private float ury;
        private String application;
        private String parameters;
        private String operation;
        private String defaultdir;

        public com.lowagie.text.Annotation.AnnotationBuilder setCoordinates(float llx, float lly, float urx, float ury) {
            this.llx = llx;
            this.lly = lly;
            this.urx = urx;
            this.ury = ury;
            return this;
        }

        public com.lowagie.text.Annotation.AnnotationBuilder setApplication(String application) {
            this.application = application;
            return this;
        }

        public com.lowagie.text.Annotation.AnnotationBuilder setParameters(String parameters) {
            this.parameters = parameters;
            return this;
        }

        public com.lowagie.text.Annotation.AnnotationBuilder setOperation(String operation) {
            this.operation = operation;
            return this;
        }

        public com.lowagie.text.Annotation.AnnotationBuilder setDefaultDir(String defaultdir) {
            this.defaultdir = defaultdir;
            return this;
        }

        public com.lowagie.text.Annotation build() {
            return new com.lowagie.text.Annotation(this);
        }
    }


    // implementation of the Element-methods

    /**
     * Gets the getTypeImpl of the text element.
     *
     * @return a getTypeImpl
     */
    public int getTypeImpl() {
        return Element.ANNOTATION;
    }

    /**
     * Processes the element by adding it (or the different parts) to an <CODE> ElementListener</CODE>.
     *
     * @param listener an <CODE>ElementListener</CODE>
     * @return <CODE>true</CODE> if the element was processed successfully
     */
    public boolean process(ElementListener listener) {
        try {
            return listener.add(this);
        } catch (DocumentException de) {
            return false;
        }
    }

    /**
     * Gets all the chunks in this element.
     *
     * @return an <CODE>ArrayList</CODE>
     */

    public ArrayList<Element> getChunks() {
        return new ArrayList<>();
    }

    @Override
    public float llx() {
        return 0;
    }

    @Override
    public float lly() {
        return lowerLeftY; // Return the value of the renamed field
    }

    @Override
    public float urx() {
        return 0;
    }

    @Override
    public float ury() {
        return 0;
    }

    // methods

    /**
     * Sets the dimensions of this annotation.
     *
     * @param llx the lower left x-value
     * @param lly the lower left y-value
     * @param urx the upper right x-value
     * @param ury the upper right y-value
     */
    public void setDimensions(float llx, float lly, float urx, float ury) {
        this.llxParam = llx;
        this.llyParam = lly;
        this.urxParam = urx;
        this.uryParam = ury;
    }

    // methods to retrieve information

    /**
     * Returns the lower left y-value.
     *
     * @return a value
     */
    public float lowLeftY() {
        return llyParam;
    }

    /**
     * Returns the upper right x-value.
     *
     * @return a value
     */
    public float upRightX() {
        return urxParam;
    }

    /**
     * Returns the lower left x-value.
     *
     * @param def the default value
     * @return a value
     */
    public float llx(float def) {
        if (Float.isNaN(llxParam)) {
            return def;
        }
        return llxParam;
    }

    /**
     * Returns the lower left y-value.
     *
     * @param def the default value
     * @return a value
     */
    public float llyMethod(float def) {
        if (Float.isNaN(llyParam)) {
            return def;
        }
        return llyParam;
    }

    /**
     * Returns the upper right x-value.
     *
     * @param def the default value
     * @return a value
     */
    public float urxMethod(float def) {
        if (Float.isNaN(urxParam)) {
            return def;
        }
        return urxParam;
    }

    /**
     * Returns the upper right y-value.
     *
     * @param def the default value
     * @return a value
     */
    public float uryMethod(float def) {
        if (Float.isNaN(uryParam)) {
            return def;
        }
        return uryParam;
    }

    /**
     * Returns the getTypeImpl of this <CODE>Annotation</CODE>.
     *
     * @return a getTypeImpl
     */
    public int annotationType() {
        return annotationType;
    }

    /**
     * Returns the title of this <CODE>Annotation</CODE>.
     *
     * @return a getName
     */
    public String titleMethod() {
        String s = (String) annotationAttributes.get(TITLE);
        if (s == null) {
            s = "";
        }
        return s;
    }

    /**
     * Gets the content of this <CODE>Annotation</CODE>.
     *
     * @return a reference
     */
    public String contentMethod() {
        String s = (String) annotationAttributes.get(CONTENT);
        if (s == null) {
            s = "";
        }
        return s;
    }

    /**
     * Gets the content of this <CODE>Annotation</CODE>.
     *
     * @return a reference
     */
    public Map<String, Object> getAttributes() {
        return annotationAttributes;
    }

    /**
     * @see com.lowagie.text.Element#isContent()
     * @since iText 2.0.8
     */
    public boolean isContent() {
        return true;
    }

    /**
     * @see com.lowagie.text.Element#isNestable()
     * @since iText 2.0.8
     */
    public boolean isNestable() {
        return true;
    }

}
