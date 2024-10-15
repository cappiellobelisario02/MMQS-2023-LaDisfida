package com.lowagie.text.pdf;

public class RadialCoordinates {
    private float x0;
    private float y0;
    private float r0;
    private float x1;
    private float y1;
    private float r1;

    public RadialCoordinates(float x0, float y0, float r0, float x1, float y1, float r1) {
        this.x0 = x0;
        this.y0 = y0;
        this.r0 = r0;
        this.x1 = x1;
        this.y1 = y1;
        this.r1 = r1;
    }

    public float getX0() { return x0; }
    public float getY0() { return y0; }
    public float getR0() { return r0; }
    public float getX1() { return x1; }
    public float getY1() { return y1; }
    public float getR1() { return r1; }
}

