package com.lowagie.text.pdf;

public class TextDrawingConfig {
    private PdfFormField field;
    private String text;
    private BaseFont font;
    private float fontSize;
    private float llx;
    private float lly;
    private float urx;
    private float ury;
    private String name; // Field for the getName of the text field

    // Constructor
    public TextDrawingConfig(PdfFormField field, String text, BaseFont font, float fontSize, float llx, float lly, float urx, float ury, String name) {
        this.field = field;
        this.text = text;
        this.font = font;
        this.fontSize = fontSize;
        this.llx = llx;
        this.lly = lly;
        this.urx = urx;
        this.ury = ury;
        this.name = name;
    }

    // Getters
    public PdfFormField getField() { return field; }
    public String getText() { return text; }
    public BaseFont getFont() { return font; }
    public float getFontSize() { return fontSize; }
    public float getLlx() { return llx; }
    public float getLly() { return lly; }
    public float getUrx() { return urx; }
    public float getUry() { return ury; }
    public String getName() { return name; }

}
