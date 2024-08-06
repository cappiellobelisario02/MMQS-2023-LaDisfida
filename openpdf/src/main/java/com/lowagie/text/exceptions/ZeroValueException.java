package com.lowagie.text.exceptions;

import java.lang.RuntimeException;

public class ZeroValueException extends RuntimeException {
    public ZeroValueException(String message) {
        super(message);
    }
}
