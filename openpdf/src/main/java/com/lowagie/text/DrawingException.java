package com.lowagie.text;


public class DrawingException extends Exception {
    public DrawingException(String message) {
        super(message);
    }

    public DrawingException(String message, Throwable cause) {
        super(message, cause);
    }
}