package com.lowagie.text;

public class TransformationMatrix {
    private final float a;
    private final float b;
    private final float c;
    private final float d;
    private final float e;
    private final float f;

    public TransformationMatrix(float a, float b, float c, float d, float e, float f) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
        this.e = e;
        this.f = f;
    }

    public float getA() { return a; }
    public float getB() { return b; }
    public float getC() { return c; }
    public float getD() { return d; }
    public float getE() { return e; }
    public float getF() { return f; }
}
