package com.lowagie.text.exceptions;

public class IllegalBarcode128CharacterException extends RuntimeException {
    public IllegalBarcode128CharacterException(String message) {
        super(message);
    }

    public IllegalBarcode128CharacterException(String message, Throwable cause) {
        super(message, cause);
    }
}
