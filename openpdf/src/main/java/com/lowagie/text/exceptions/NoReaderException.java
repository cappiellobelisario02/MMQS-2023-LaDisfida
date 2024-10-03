package com.lowagie.text.exceptions;

public class NoReaderException extends RuntimeException {

    // Constructor with a custom message
    public NoReaderException(String message) {
        super(message);
    }

    // Optional: Constructor with a custom message and a cause
    public NoReaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
