package com.lowagie.text.exceptions;

public class ReadOnlyException extends RuntimeException {
    public ReadOnlyException(String message) {
        super(message);
    }
}
