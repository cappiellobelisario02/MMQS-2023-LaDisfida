package com.lowagie.text.pdf;

import java.awt.Color;

public class ChunkAttributes {
    private float xMarker;
    private float baseXMarker;
    private float yMarker;
    private float tabPosition;
    private int lastChunkStroke;
    private int chunkStrokeIdx;
    private Color color;

    public ChunkAttributes(float xMarker, float baseXMarker, float yMarker, float tabPosition,
            int lastChunkStroke, Color color, int chunkStrokeIdx) {
        this.xMarker = xMarker;
        this.baseXMarker = baseXMarker;
        this.yMarker = yMarker;
        this.tabPosition = tabPosition;
        this.lastChunkStroke = lastChunkStroke;
        this.color = color;
        this.chunkStrokeIdx = chunkStrokeIdx;
    }

    public int getChunkStrokeIdx() {
        return chunkStrokeIdx;
    }

    public void setChunkStrokeIdx(int chunkStrokeIdx) {
        this.chunkStrokeIdx = chunkStrokeIdx;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public int getLastChunkStroke() {
        return lastChunkStroke;
    }

    public void setLastChunkStroke(int lastChunkStroke) {
        this.lastChunkStroke = lastChunkStroke;
    }

    public float getxMarker() {
        return xMarker;
    }

    public void setxMarker(float xMarker) {
        this.xMarker = xMarker;
    }

    public float getBaseXMarker() {
        return baseXMarker;
    }

    public void setBaseXMarker(float baseXMarker) {
        this.baseXMarker = baseXMarker;
    }

    public float getyMarker() {
        return yMarker;
    }

    public void setyMarker(float yMarker) {
        this.yMarker = yMarker;
    }

    public float getTabPosition() {
        return tabPosition;
    }

    public void setTabPosition(float tabPosition) {
        this.tabPosition = tabPosition;
    }
}
