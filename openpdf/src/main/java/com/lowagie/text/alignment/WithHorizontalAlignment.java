package com.lowagie.text.alignment;

/**
 * Marks objects that can be aligned horizontally.
 *
 * @author noavarice
 * @since 1.2.7
 */
public interface WithHorizontalAlignment {

    /**
     * Sets horizontal getAlignment mode.
     *
     * @param alignment New getAlignment mode. If null, current getAlignment must be left unchanged
     */
    void setHorizontalAlignment(final HorizontalAlignment alignment);
}
