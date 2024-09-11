package com.lowagie.text.pdf;

public class LineLayoutParams {
    private boolean isJustified;
    private float baseCharacterSpacing;
    private float baseWordSpacing;
    private float glueWidth;
    private float hangingCorrection;
    private float hScale;
    private int numberOfSpaces;

    public LineLayoutParams(boolean isJustified, float baseCharacterSpacing, float baseWordSpacing,
            float glueWidth, float hangingCorrection, float hScale, int numberOfSpaces){
        this.isJustified = isJustified;
        this.baseCharacterSpacing = baseCharacterSpacing;
        this.baseWordSpacing = baseWordSpacing;
        this.glueWidth = glueWidth;
        this.hangingCorrection = hangingCorrection;
        this.hScale = hScale;
        this.numberOfSpaces = numberOfSpaces;
    }

    public void setJustified(boolean isJustified){
        this.isJustified = isJustified;
    }

    public boolean getJustified(){
        return isJustified;
    }

    public void setBaseCharacterSpacing(float baseCharacterSpacing){
        this.baseCharacterSpacing = baseCharacterSpacing;
    }

    public float getBaseCharacterSpacing(){
        return baseCharacterSpacing;
    }

    public void setBaseWordSpacing(float baseWordSpacing){
        this.baseWordSpacing = baseWordSpacing;
    }

    public float getBaseWordSpacing(){
        return baseWordSpacing;
    }

    public void setGlueWidth(float glueWidth){
        this.glueWidth = glueWidth;
    }

    public float getGlueWidth(){
        return glueWidth;
    }

    public void setHangingCorrection(float hangingCorrection){
        this.hangingCorrection = hangingCorrection;
    }

    public float getHangingCorrection(){
        return hangingCorrection;
    }

    public void setHScale(float hScale){
        this.hScale = hScale;
    }

    public float getHScale(){
        return hScale;
    }

    public void setNumberOfSpaces(int numberOfSpaces){
        this.numberOfSpaces = numberOfSpaces;
    }

    public int getNumberOfSpaces(){
        return numberOfSpaces;
    }
}
