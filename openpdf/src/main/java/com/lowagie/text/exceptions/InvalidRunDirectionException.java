package com.lowagie.text.exceptions;

public class InvalidRunDirectionException extends RuntimeException {
    // Constructor that accepts a message
    public InvalidRunDirectionException(String message) {
        super(message);
    }

    // Constructor that accepts a message and a cause
    public InvalidRunDirectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
