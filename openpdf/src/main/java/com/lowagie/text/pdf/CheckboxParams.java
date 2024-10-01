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

    // Private constructor so it can only be called from the builder
    private CheckboxParams(Builder builder) {
        this.field = builder.field;
        this.name = builder.name;
        this.value = builder.value;
        this.status = builder.status;
        this.llx = builder.llx;
        this.lly = builder.lly;
        this.urx = builder.urx;
        this.ury = builder.ury;
    }

    // Getter methods
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

    // Builder class
    public static class Builder {
        private PdfFormField field;
        private String name;
        private String value;
        private boolean status;
        private float llx;
        private float lly;
        private float urx;
        private float ury;

        // Setters for builder
        public Builder setField(PdfFormField field) {
            this.field = field;
            return this;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setValue(String value) {
            this.value = value;
            return this;
        }

        public Builder setStatus(boolean status) {
            this.status = status;
            return this;
        }

        public Builder setLlx(float llx) {
            this.llx = llx;
            return this;
        }

        public Builder setLly(float lly) {
            this.lly = lly;
            return this;
        }

        public Builder setUrx(float urx) {
            this.urx = urx;
            return this;
        }

        public Builder setUry(float ury) {
            this.ury = ury;
            return this;
        }

        // Build method to create the object
        public CheckboxParams build() {
            return new CheckboxParams(this);
        }
    }
}
