package com.lowagie.text.exceptions;

public class ReadOnlyAcroFieldsException extends RuntimeException {

    // Constructor that accepts a message
    public ReadOnlyAcroFieldsException(String message) {
        super(message);
    }

    // Constructor that accepts a message and a cause
    public ReadOnlyAcroFieldsException(String message, Throwable cause) {
        super(message, cause);
    }
}
