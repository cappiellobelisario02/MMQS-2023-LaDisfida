package com.lowagie.text.exceptions;

public class ColorParseException extends RuntimeException {
    public ColorParseException(String message) {
        super(message);
    }

    public ColorParseException(String message, Throwable cause) {
        super(message, cause);
    }
}
