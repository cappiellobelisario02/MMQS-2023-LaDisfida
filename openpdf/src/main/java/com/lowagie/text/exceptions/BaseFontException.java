package com.lowagie.text.exceptions;

public class BaseFontException extends RuntimeException{
    public BaseFontException(String message) {
        super(message);
    }

    public BaseFontException(Throwable exception) {
        super(exception);
    }
}
