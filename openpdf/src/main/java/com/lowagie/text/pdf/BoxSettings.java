package com.lowagie.text.pdf;

import com.lowagie.text.Rectangle;

public class BoxSettings {
    private Rectangle box;
    private int rotation;

    public BoxSettings(Rectangle box, int rotation) {
        this.box = box;
        this.rotation = rotation;
    }

    public Rectangle getBox() { return box; }
    public int getRotation() { return rotation; }
}
