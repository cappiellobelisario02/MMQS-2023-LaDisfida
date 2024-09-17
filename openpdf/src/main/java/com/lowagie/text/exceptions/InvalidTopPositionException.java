package com.lowagie.text.exceptions;

public class InvalidTopPositionException extends RuntimeException {
    // Constructor that accepts a message
    public InvalidTopPositionException(String message) {
        super(message);
    }

    // Constructor that accepts a message and a cause
    public InvalidTopPositionException(String message, Throwable cause) {
        super(message, cause);
    }
}
