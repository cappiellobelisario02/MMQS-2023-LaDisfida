package com.lowagie.text.exceptions;

public class PageSizeException extends Exception {
    public PageSizeException(String message) {
        super(message);
    }

    public PageSizeException(String message, Throwable cause) {
        super(message, cause);
    }
}
