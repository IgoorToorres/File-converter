package com.igor.fileconverter.domain.exception;

public class ConversionProcessingException extends RuntimeException {
    public ConversionProcessingException(String message) {
        super(message);
    }
    public ConversionProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}
