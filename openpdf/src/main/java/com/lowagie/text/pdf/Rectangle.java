package com.lowagie.text.pdf;

public class Rectangle {
    private final float llx;
    private final float lly;
    private final float urx;
    private final float ury;

    public Rectangle(float llx, float lly, float urx, float ury) {
        this.llx = llx;
        this.lly = lly;
        this.urx = urx;
        this.ury = ury;
    }

    // Getters
    public float getLlx() { return llx; }
    public float getLly() { return lly; }
    public float getUrx() { return urx; }
    public float getUry() { return ury; }
}

