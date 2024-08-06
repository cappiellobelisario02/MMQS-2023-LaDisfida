package com.lowagie.text.exceptions;

import java.lang.RuntimeException;

public class InvalidColorTypeException extends RuntimeException {
    public InvalidColorTypeException(String message) {
        super(message);
    }
}
