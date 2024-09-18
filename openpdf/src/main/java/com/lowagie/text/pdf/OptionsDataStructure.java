package com.lowagie.text.pdf;

abstract class OptionsDataStructure {
    abstract String getElement(int index);
    abstract String getElement(int row, int col);

    // Metodo astratto che sarà implementato nelle sottoclassi
    abstract Object getOptions();
}
