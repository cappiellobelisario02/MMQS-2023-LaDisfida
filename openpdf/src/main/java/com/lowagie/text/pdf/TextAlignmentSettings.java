package com.lowagie.text.pdf;

public class TextAlignmentSettings {
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;

    private int alignment;
    private float x;
    private float y;
    private float rotation;
    private int runDirection;
    private int arabicOptions;

    public TextAlignmentSettings(int alignment, float x, float y, float rotation, int runDirection, int arabicOptions) {
        this.alignment = alignment;
        this.x = x;
        this.y = y;
        this.rotation = rotation;
        this.runDirection = runDirection;
        this.arabicOptions = arabicOptions;
    }

    // Getters and setters
    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getRotation() {
        return rotation;
    }

    public void setRotation(float rotation) {
        this.rotation = rotation;
    }

    public int getRunDirection() {
        return runDirection;
    }

    public void setRunDirection(int runDirection) {
        this.runDirection = runDirection;
    }

    public int getArabicOptions() {
        return arabicOptions;
    }

    public void setArabicOptions(int arabicOptions) {
        this.arabicOptions = arabicOptions;
    }
}
