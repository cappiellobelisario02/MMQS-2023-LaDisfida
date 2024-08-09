package com.lowagie.text.pdf;

import java.awt.Color;

public class BorderSettings {
    private int borderStyle;
    private float borderWidth;
    private Color borderColor;

    public BorderSettings(int borderStyle, float borderWidth, Color borderColor) {
        this.borderStyle = borderStyle;
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
    }

    public int getBorderStyle() { return borderStyle; }
    public float getBorderWidth() { return borderWidth; }
    public Color getBorderColor() { return borderColor; }
}

