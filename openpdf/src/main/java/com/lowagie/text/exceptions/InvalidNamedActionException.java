package com.lowagie.text.exceptions;

import java.lang.RuntimeException;

public class InvalidNamedActionException extends RuntimeException {
    public InvalidNamedActionException(String message) {
        super(message);
    }
}
