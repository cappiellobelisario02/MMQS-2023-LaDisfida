package com.lowagie.text.exceptions;

public class NewClassException extends IncompatibleClassChangeError{

    public NewClassException() {
    }

    public NewClassException(String s) {
        super(s);
    }
}
