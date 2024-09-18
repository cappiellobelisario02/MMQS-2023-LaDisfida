package com.lowagie.text.pdf;

// Class to hold the positional coordinates
class Position {
    private float llx;
    private float lly;
    private float urx;
    private float ury;

    public Position(float llx, float lly, float urx, float ury) {
        this.llx = llx;
        this.lly = lly;
        this.urx = urx;
        this.ury = ury;
    }

    public float getLlx() { return llx; }
    public float getLly() { return lly; }
    public float getUrx() { return urx; }
    public float getUry() { return ury; }
}

// Updated ComboBoxParams class
public class ComboBoxParams {
    private String name;
    private OptionsDataStructure options;
    private String defaultValue;
    private boolean editable;
    private BaseFont font;
    private float fontSize;
    private Position position;

    // Constructor with grouped parameters
    public ComboBoxParams(String name, OptionsDataStructure options, String defaultValue, boolean editable, BaseFont font, float fontSize, Position position) {
        this.name = name;
        this.options = options;
        this.defaultValue = defaultValue;
        this.editable = editable;
        this.font = font;
        this.fontSize = fontSize;
        this.position = position;
    }

    // Setters and getters
    public void setDefaultValue(String dValue) {
        defaultValue = dValue;
    }

    public String getName() {
        return name;
    }

    public String getElement(int index) {
        return options.getElement(index);
    }

    public String getElement(int row, int col) {
        return options.getElement(row, col);
    }

    public Object getOptions() {
        return options.getOptions();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public boolean isEditable() {
        return editable;
    }

    public BaseFont getFont() {
        return font;
    }

    public float getFontSize() {
        return fontSize;
    }

    public Position getPosition() {
        return position;
    }
}
