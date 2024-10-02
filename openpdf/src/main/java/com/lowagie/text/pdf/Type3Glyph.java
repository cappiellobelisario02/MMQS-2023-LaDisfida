package com.lowagie.text.pdf;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.pdf.Type3Glyph.Builder;

/**
 * The content where Type3 glyphs are written to.
 */
public final class Type3Glyph extends PdfContentByte {

    private PageResources pageResources;
    private boolean colorized;
    private Rectangle rectangle;
    private float wx;

    Type3Glyph(com.lowagie.text.pdf.Type3Glyph.Builder builder) {
        super(builder.writer);
        this.pageResources = builder.pageResources;
        this.colorized = builder.colorized;
        this.rectangle = builder.rectangle;
        this.wx = builder.wx;

        if (colorized) {
            content.append(wx).append(" 0 d0\n");
        } else {
            content.append(wx).append(" 0 ")
                    .append(rectangle.getLlx()).append(' ')
                    .append(rectangle.getLly()).append(' ')
                    .append(rectangle.getUrx()).append(' ')
                    .append(rectangle.getUry()).append(" d1\n");
        }
    }

    @Override
    PageResources getPageResources() {
        return pageResources;
    }

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
        return new com.lowagie.text.pdf.Type3Glyph.Builder(writer, pageResources, wx, rectangle, colorized).build();
    }

    public static class Builder {
        private final PdfWriter writer;
        private final PageResources pageResources;
        private final float wx;
        private final Rectangle rectangle;
        private final boolean colorized;

        public Builder(PdfWriter writer, PageResources pageResources, float wx, Rectangle rectangle, boolean colorized) {
            this.writer = writer;
            this.pageResources = pageResources;
            this.wx = wx;
            this.rectangle = rectangle;
            this.colorized = colorized;
        }

        public com.lowagie.text.pdf.Type3Glyph build() {
            return new com.lowagie.text.pdf.Type3Glyph(this);
        }
    }
}
