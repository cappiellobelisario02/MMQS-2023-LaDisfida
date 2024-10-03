package com.lowagie.text.exceptions;

public class MultiByteGlyphsNotImplementedException extends RuntimeException {

    public MultiByteGlyphsNotImplementedException() {
    }

    public MultiByteGlyphsNotImplementedException(String message) {
        super(message);
    }

    public MultiByteGlyphsNotImplementedException(String message, Throwable cause) {
        super(message, cause);
    }

    public MultiByteGlyphsNotImplementedException(Throwable cause) {
        super(cause);
    }

    public MultiByteGlyphsNotImplementedException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
