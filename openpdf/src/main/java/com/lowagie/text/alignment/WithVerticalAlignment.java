package com.lowagie.text.alignment;

/**
 * Marks objects that can be aligned vertically.
 *
 * @author noavarice
 * @since 1.2.7
 */
public interface WithVerticalAlignment {

    /**
     * Sets vertical getAlignment mode.
     *
     * @param alignment New getAlignment mode. If null, current getAlignment must be left unchanged
     */
    void setVerticalAlignment(final VerticalAlignment alignment);
}
