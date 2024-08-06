package com.lowagie.text.pdf;

abstract class OptionsDataStructure {
    abstract String getElement(int index);
    abstract String getElement(int row, int col);

    // Metodo astratto che sarà implementato nelle sottoclassi
    abstract Object getOptions();
}

class VectorData extends OptionsDataStructure {
    private String[] vector;

    public VectorData(String[] vector) {
        this.vector = vector;
    }

    @Override
    String getElement(int index) {
        return vector[index];
    }

    @Override
    String getElement(int row, int col) {
        throw new UnsupportedOperationException("Not a matrix");
    }

    @Override
    String[] getOptions() {
        return vector;
    }
}

class MatrixData extends OptionsDataStructure {
    private String[][] matrix;

    public MatrixData(String[][] matrix) {
        this.matrix = matrix;
    }

    @Override
    String getElement(int index) {
        throw new UnsupportedOperationException("Not a vector");
    }

    @Override
    String getElement(int row, int col) {
        return matrix[row][col];
    }

    @Override
    String[][] getOptions() {
        return matrix;
    }
}

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

    // Costruttore
    public SelectListParams(String name, OptionsDataStructure options, String defaultValue, BaseFont font, float fontSize, float llx, float lly, float urx, float ury) {
        this.name = name;
        this.options = options;
        this.defaultValue = defaultValue;
        this.font = font;
        this.fontSize = fontSize;
        this.llx = llx;
        this.lly = lly;
        this.urx = urx;
        this.ury = ury;
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

    // Metodo per ottenere le opzioni
    public Object getOptions() {
        return options.getOptions();
    }
}