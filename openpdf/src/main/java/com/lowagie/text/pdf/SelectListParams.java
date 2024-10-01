package com.lowagie.text.pdf;

public class SelectListParams {
    private String name;
    private OptionsDataStructure options;
    private String defaultValue;
    private BaseFont font;
    private float fontSize;
    private float llx;
    private float lly;
    private float urx;
    private float ury;

    // Private constructor so it can only be called from the builder
    private SelectListParams(SelectListParamsBuilder builder) {
        this.name = builder.name;
        this.options = builder.options;
        this.defaultValue = builder.defaultValue;
        this.font = builder.font;
        this.fontSize = builder.fontSize;
        this.llx = builder.llx;
        this.lly = builder.lly;
        this.urx = builder.urx;
        this.ury = builder.ury;
    }

    // Getter methods
    public String getName() {
        return name;
    }

    public String getElement(int index) {
        return options.getElement(index);
    }

    public String getElement(int row, int col) {
        return options.getElement(row, col);
    }

    public String getDefaultValue() {
        return defaultValue;
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

    // Method to obtain options
    public Object getOptions() {
        return options.getOptions();
    }

    // Inner class for building SelectListParams instances
    public static class SelectListParamsBuilder {
        private String name;
        private OptionsDataStructure options;
        private String defaultValue;
        private BaseFont font;
        private float fontSize;
        private float llx;
        private float lly;
        private float urx;
        private float ury;

        // Setters for builder
        public SelectListParamsBuilder setName(String name) {
            this.name = name;
            return this;
        }

        public SelectListParamsBuilder setOptions(OptionsDataStructure options) {
            this.options = options;
            return this;
        }

        public SelectListParamsBuilder setDefaultValue(String defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public SelectListParamsBuilder setFont(BaseFont font) {
            this.font = font;
            return this;
        }

        public SelectListParamsBuilder setFontSize(float fontSize) {
            this.fontSize = fontSize;
            return this;
        }

        public SelectListParamsBuilder setLlx(float llx) {
            this.llx = llx;
            return this;
        }

        public SelectListParamsBuilder setLly(float lly) {
            this.lly = lly;
            return this;
        }

        public SelectListParamsBuilder setUrx(float urx) {
            this.urx = urx;
            return this;
        }

        public SelectListParamsBuilder setUry(float ury) {
            this.ury = ury;
            return this;
        }

        // Build method to create the object
        public SelectListParams build() {
            return new SelectListParams(this);
        }
    }
}
