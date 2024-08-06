package com.lowagie.text.exceptions;

import java.lang.RuntimeException;

public class NotEqualWritersException extends RuntimeException{
    public NotEqualWritersException(String message) {
        super(message);
    }
}
