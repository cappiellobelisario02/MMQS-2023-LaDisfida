package com.lowagie.text.pdf;

public class HtmlPostButtonConfig {
    private String name;
    private String caption;
    private String value;
    private String url;
    private BaseFont font;
    private float fontSize;
    private float llx;
    private float lly;
    private float urx;
    private float ury;
    private PdfFormField button;

    // Constructor
    public HtmlPostButtonConfig(String name, String caption, String value, String url, BaseFont font, float fontSize,
            float llx, float lly, float urx, float ury, PdfFormField button) {
        this.name = name;
        this.caption = caption;
        this.value = value;
        this.url = url;
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
    public String getUrl() { return url; }
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
