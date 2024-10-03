package com.lowagie.text.exceptions;

// Define a custom exception for font processing issues
public class FontProcessingException extends RuntimeException {
    public FontProcessingException(String message) {
        super(message);
    }

    public FontProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
