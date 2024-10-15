package com.lowagie.text.pdf;

public class ResetButtonConfig {
    private String name;
    private String caption;
    private String value;
    private BaseFont font;
    private float fontSize;
    private float llx;
    private float lly;
    private float urx;
    private float ury;
    private PdfFormField button;

    // Constructor
    public ResetButtonConfig(String name, String caption, String value, BaseFont font, float fontSize, float llx,
            float lly, float urx, float ury, PdfFormField button) {
        this.name = name;
        this.caption = caption;
        this.value = value;
        this.font = font;
        this.fontSize = fontSize;
        this.llx = llx;
        this.lly = lly;
        this.urx = urx;
        this.ury = ury;
        this.button = button;
    }

    // Getters
    public String getName() { return name; }
    public String getCaption() { return caption; }
    public String getValue() { return value; }
    public BaseFont getFont() { return font; }
    public float getFontSize() { return fontSize; }
    public float getLlx() { return llx; }
    public float getLly() { return lly; }
    public float getUrx() { return urx; }
    public float getUry() { return ury; }

    public PdfFormField getButton() {
        return button;
    }
}
