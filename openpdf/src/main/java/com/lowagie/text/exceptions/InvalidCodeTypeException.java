package com.lowagie.text.exceptions;

/**
 * Generates barcodes in several formats: EAN13, EAN8, UPCA, UPCE, supplemental 2 and 5. The default parameters are:
 * <pre>
 * x = 0.8f;
 * font = BaseFont.createFont("Helvetica", "winansi", false);
 * size = 8;
 * baseline = size;
 * barHeight = size * 3;
 * guardBars = true;
 * codeType = EAN13;
 * code = "";
 * </pre>
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */

public class InvalidCodeTypeException extends RuntimeException {
    public InvalidCodeTypeException(String message) {
        super(message);
    }
}
