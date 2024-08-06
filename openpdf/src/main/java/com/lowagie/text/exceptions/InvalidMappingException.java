package com.lowagie.text.exceptions;

import java.lang.RuntimeException;

public class InvalidMappingException extends RuntimeException {
    public InvalidMappingException(String message) {
        super(message);
    }
}
