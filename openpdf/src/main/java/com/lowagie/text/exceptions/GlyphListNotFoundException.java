package com.lowagie.text.exceptions;

public class GlyphListNotFoundException extends RuntimeException {
    // Constructor that accepts a message
    public GlyphListNotFoundException(String message) {
        super(message);
    }

    // Constructor that accepts a message and a cause
    public GlyphListNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
