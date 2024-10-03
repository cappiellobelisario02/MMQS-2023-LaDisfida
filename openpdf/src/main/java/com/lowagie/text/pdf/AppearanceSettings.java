package com.lowagie.text.pdf;

import java.awt.Color;

public class AppearanceSettings {
    private Color backgroundColor;
    private int borderStyle;
    private float borderWidth;
    private Color borderColor;
    private int options;
    private int maxCharacterLength;

    public AppearanceSettings(Color backgroundColor, int borderStyle, float borderWidth, Color borderColor, int options, int maxCharacterLength) {
        this.backgroundColor = backgroundColor;
        this.borderStyle = borderStyle;
        this.borderWidth = borderWidth;
        this.borderColor = borderColor;
        this.options = options;
        this.maxCharacterLength = maxCharacterLength;
    }

    public Color getBackgroundColor() { return backgroundColor; }
    public int getBorderStyle() { return borderStyle; }
    public float getBorderWidth() { return borderWidth; }
    public Color getBorderColor() { return borderColor; }
    public int getOptions() { return options; }
    public int getMaxCharacterLength() { return maxCharacterLength; }
}