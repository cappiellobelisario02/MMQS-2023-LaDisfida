package com.lowagie.text.exceptions;

public class UnsupportedLZWFlavorException extends RuntimeException {
    // Constructor that accepts a message
    public UnsupportedLZWFlavorException(String message) {
        super(message);
    }

    // Constructor that accepts a message and a cause
    public UnsupportedLZWFlavorException(String message, Throwable cause) {
        super(message, cause);
    }
}
