package com.lowagie.text.pdf;

public class ComboBoxParams {
    private String name;
    private OptionsDataStructure options;
    private String defaultValue;
    private boolean editable;
    private BaseFont font;
    private float fontSize;
    private float llx;
    private float lly;
    private float urx;
    private float ury;

    // Costruttore
    public ComboBoxParams(String name, OptionsDataStructure options, String defaultValue, boolean editable, BaseFont font, float fontSize, float llx, float lly, float urx, float ury)
    {
        this.name = name;
        this.options = options;
        this.defaultValue = defaultValue;
        this.editable = editable;
        this.font = font;
        this.fontSize = fontSize;
        this.llx = llx;
        this.lly = lly;
        this.urx = urx;
        this.ury = ury;
    }

    public void setDefaultValue(String dValue){
        defaultValue = dValue;
    }

    // Getter per tutti i campi
    public String getName() {
        return name;
    }

    public String getElement(int index) {
        return options.getElement(index);
    }

    public String getElement(int row, int col) {
        return options.getElement(row, col);
    }

    // Metodo per ottenere le opzioni
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

    public float getLlx() {
        return llx;
    }

    public float getLly() {
        return lly;
    }

    public float getUrx() {
        return urx;
    }

    public float getUry() {
        return ury;
    }
}