/*
 * $Id: PdfPrinterGraphics2D.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2004 Paulo Soares and Alexandru Carstoiu
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
 * the Initial Developer are Copyright (C) 1999-2005 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2005 by Paulo Soares. All Rights Reserved.
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

import java.awt.print.PrinterGraphics;
import java.awt.print.PrinterJob;

/**
 * This is an extension class for the sole purpose of implementing the
 * {@link PrinterGraphics PrinterGraphics} interface.
 */
public class PdfPrinterGraphics2D extends PdfGraphics2D implements PrinterGraphics {

    private PrinterJob printerJob;

    PdfPrinterGraphics2D(Builder builder, PrinterJob printerJob) {
        super(builder.cb, builder.width, builder.height, builder.fontMapper, builder.onlyShapes, builder.convertImagesToJPEG, builder.quality);
        this.printerJob = printerJob;
    }

    protected PdfPrinterGraphics2D(PdfPrinterGraphics2D parent) {
        super(parent);
        printerJob = parent.printerJob;
    }

    public PrinterJob getPrinterJob() {
        return printerJob;
    }
    
    @Override
    protected PdfGraphics2D createChild() {
        return new PdfPrinterGraphics2D(this);
    }

    public static class Builder {
        private final PdfContentByte cb;
        private final float width;
        private final float height;
        private final FontMapper fontMapper;
        private final boolean onlyShapes;
        private final boolean convertImagesToJPEG;
        private final float quality;

        public Builder(PdfContentByte cb, float width, float height, FontMapper fontMapper, boolean onlyShapes, boolean convertImagesToJPEG, float quality)
        {
            this.cb = cb;
            this.width = width;
            this.height = height;
            this.fontMapper = fontMapper;
            this.onlyShapes = onlyShapes;
            this.convertImagesToJPEG = convertImagesToJPEG;
            this.quality = quality;
        }

        public PdfPrinterGraphics2D build(PrinterJob printerJob) {
            return new PdfPrinterGraphics2D(this, printerJob);
        }
    }
}
