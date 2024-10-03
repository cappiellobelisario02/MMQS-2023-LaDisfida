package com.lowagie.text.pdf;

import java.awt.Color;

public class ColorPair {
    private Color startColor;
    private Color endColor;
    private SpotColor startColorSpot;
    private SpotColor endColorSpot;

    public ColorPair(Color startColor, Color endColor) {
        this.startColor = startColor;
        this.endColor = endColor;
    }

    public ColorPair(SpotColor startColorSpot, SpotColor endColorSpot) {
        this.startColorSpot = startColorSpot;
        this.endColorSpot = endColorSpot;
    }

    public Color getStartColor() { return startColor; }
    public Color getEndColor() { return endColor; }
    public SpotColor getStartColorSpot() { return startColorSpot; }
    public SpotColor getEndColorSpot() { return endColorSpot; }
}

