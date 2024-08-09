package com.lowagie.text.pdf;

import com.lowagie.text.Rectangle;
import java.awt.Color;

public class RadioCheckFieldAppearanceSettings {
    private int checkType;
    private int rotation;
    private Rectangle box;
    private float fontSize;
    private String text;
    private Color textColor;
    private Color backgroundColor;
    private BaseFont ufont;

    public RadioCheckFieldAppearanceSettings(int checkType, int rotation, Rectangle box, float fontSize, String text,
            Color textColor, Color backgroundColor, BaseFont ufont) {
        this.checkType = checkType;
        this.rotation = rotation;
        this.box = box;
        this.fontSize = fontSize;
        this.text = text;
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
        this.ufont = ufont;
    }

    public int getRCFCheckType() { return checkType; }
    public int getRCFRotation() { return rotation; }
    public Rectangle getRCFBox() { return box; }
    public float getRCFFontSize() { return fontSize; }
    public String getRCFText() { return text; }
    public Color getRCFTextColor() { return textColor; }
    public Color getRCFBackgroundColor() { return backgroundColor; }
    public BaseFont getRCF_Ufont() { return ufont; }
}

