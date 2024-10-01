package com.lowagie.text.exceptions;

public class InvalidColorValueException extends RuntimeException {
    public InvalidColorValueException(String message) {
        super(message);
    }

    public InvalidColorValueException(String message, Throwable cause) {
        super(message, cause);
    }
}
