package com.lowagie.text.exceptions;

import java.lang.RuntimeException;

public class ActionException extends RuntimeException {
    public ActionException(String message) {
        super(message);
    }
}
