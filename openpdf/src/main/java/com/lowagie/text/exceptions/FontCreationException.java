package com.lowagie.text.exceptions;

public class FontCreationException extends RuntimeException {
    // Constructor that accepts a message
    public FontCreationException(String message) {
        super(message);
    }

    // Constructor that accepts a message and a cause
    public FontCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}