package com.lowagie.text.exceptions;

import java.lang.RuntimeException;

public class IllegalContentException extends RuntimeException {
    public IllegalContentException(String message) {
        super(message);
    }
}
