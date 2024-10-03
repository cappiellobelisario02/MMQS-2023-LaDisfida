package com.lowagie.text.pdf;

public class MapButtonConfig {
    private String name;
    private String value;
    private String url;
    private PdfContentByte appearance;
    private float llx;
    private float lly;
    private float urx;
    private float ury;

    // Constructor
    public MapButtonConfig(String name, String value, String url, PdfContentByte appearance, float llx, float lly, float urx, float ury) {
        this.name = name;
        this.value = value;
        this.url = url;
        this.appearance = appearance;
        this.llx = llx;
        this.lly = lly;
        this.urx = urx;
        this.ury = ury;
    }

    // Getters
    public String getName() { return name; }
    public String getValue() { return value; }
    public String getUrl() { return url; }
    public PdfContentByte getAppearance() { return appearance; }
    public float getLlx() { return llx; }
    public float getLly() { return lly; }
    public float getUrx() { return urx; }
    public float getUry() { return ury; }
}
