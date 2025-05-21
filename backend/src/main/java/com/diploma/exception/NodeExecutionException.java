package com.diploma.exception;

public class NodeExecutionException extends RuntimeException {
    public NodeExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public NodeExecutionException(String message) {
        super(message);
    }
}