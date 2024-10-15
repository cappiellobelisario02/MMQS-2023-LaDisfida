package com.lowagie.text.pdf;

public class CheckboxParams {
    private PdfFormField field;
    private String name;
    private String value;
    private boolean status;
    private float llx;
    private float lly;
    private float urx;
    private float ury;

    // Costruttore
    public CheckboxParams(PdfFormField field, String name, String value, boolean status, float llx, float lly, float urx, float ury) {
        this.field = field;
        this.name = name;
        this.value = value;
        this.status = status;
        this.llx = llx;
        this.lly = lly;
        this.urx = urx;
        this.ury = ury;
    }


    // Getter per tutti i campi
    public PdfFormField getField() {
        return field;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public boolean isStatus() {
        return status;
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
