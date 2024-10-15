package com.lowagie.text.pdf;

public class Coordinates {
    private float x0;
    private float y0;
    private float x1;
    private float y1;

    public Coordinates(float x0, float y0, float x1, float y1) {
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
    }

    public float getX0() { return x0; }
    public float getY0() { return y0; }
    public float getX1() { return x1; }
    public float getY1() { return y1; }
}