package com.lowagie.text.pdf;

import java.awt.Color;

public class ColorPair {
    private Color startColor;
    private Color endColor;

    public ColorPair(Color startColor, Color endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
    }

    public Color getStartColor() { return startColor; }
    public Color getEndColor() { return endColor; }
}

