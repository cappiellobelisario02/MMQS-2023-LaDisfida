package com.lowagie.text.exceptions;

import java.lang.RuntimeException;

public class IllegalNumberException extends RuntimeException {
    public IllegalNumberException(String message) {
        super(message);
    }
}
