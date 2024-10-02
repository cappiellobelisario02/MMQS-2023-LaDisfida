package com.lowagie.text.exceptions;

public class DocumentPrintingException extends RuntimeException {
    public DocumentPrintingException(String message, Throwable cause) {
        super(message, cause);
    }

    public DocumentPrintingException(Throwable cause) {
        super(cause);
    }
}
