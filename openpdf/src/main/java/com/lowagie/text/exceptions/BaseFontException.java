package com.lowagie.text.exceptions;

import java.lang.RuntimeException;

public class BaseFontException extends RuntimeException{
    public BaseFontException(String message) {
        super(message);
    }

    public BaseFontException(Throwable exception) {
        super(exception);
    }
}
