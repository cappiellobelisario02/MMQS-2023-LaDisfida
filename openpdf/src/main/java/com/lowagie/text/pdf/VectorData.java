package com.lowagie.text.pdf;

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
