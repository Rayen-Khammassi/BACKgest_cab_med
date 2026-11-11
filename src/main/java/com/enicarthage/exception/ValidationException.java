package com.enicarthage.exception;

public class ValidationException extends AppException {
    public ValidationException(String message) {
        super(400, message);
    }
}
