package com.enicarthage.exception;

public class DatabaseException extends AppException {
    public DatabaseException(String message, Throwable cause) {
        super(500, message, cause);
    }

    public DatabaseException(String message) {
        super(500, message);
    }
}
