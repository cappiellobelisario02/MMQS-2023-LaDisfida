package com.lowagie.text.pdf;

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
