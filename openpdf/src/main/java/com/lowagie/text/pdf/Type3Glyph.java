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
import com.lowagie.text.error_messages.MessageLocalization;

/**
 * The content where Type3 glyphs are written to.
 */
public final class Type3Glyph extends PdfContentByte {

    private PageResources pageResources;
    private boolean colorized;

    private Type3Glyph() {
        super(null);
    }

    Type3Glyph(Builder builder) {
        super(builder.writer);
        this.pageResources = builder.pageResources;
        this.colorized = builder.colorized;
        if (colorized) {
            content.append(builder.wx).append(" 0 d0\n");
        } else {
            content.append(builder.wx).append(" 0 ").append(builder.llx).append(' ').append(builder.lly).append(' ').append(builder.urx).append(' ')
                    .append(builder.ury).append(" d1\n");
        }
    }

    @Override
    PageResources getPageResources() {
        return pageResources;
    }

    @Override
    public void addImage(Image image, float a, float b, float c, float d, float e, float f, boolean inlineImage)
            throws DocumentException {
        if (!colorized && (!image.isMask() || !(image.getBpc() == 1 || image.getBpc() > 0xff))) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("not.colorized.typed3.fonts.only.accept.mask.images"));
        }
        super.addImage(image, a, b, c, d, e, f, inlineImage);
    }

    @Override
    public PdfContentByte getDuplicate() {
        Type3Glyph dup = new Type3Glyph();
        dup.writer = writer;
        dup.pdf = pdf;
        dup.pageResources = pageResources;
        dup.colorized = colorized;
        return dup;
    }

    public static class Builder {
        private final PdfWriter writer;
        private final PageResources pageResources;
        private final float wx;
        private final float llx;
        private final float lly;
        private final float urx;
        private final float ury;
        private final boolean colorized;

        public Builder(PdfWriter writer, PageResources pageResources, float wx, float llx, float lly, float urx, float ury, boolean colorized) {
            this.writer = writer;
            this.pageResources = pageResources;
            this.wx = wx;
            this.llx = llx;
            this.lly = lly;
            this.urx = urx;
            this.ury = ury;
            this.colorized = colorized;
        }

        public Type3Glyph build() {
            return new Type3Glyph(this);
        }
    }
}
