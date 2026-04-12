package com.dani.catalogservice.exception;

public class InsufficientPermissionsException extends RuntimeException {

    public InsufficientPermissionsException() {
        super("Insufficient permissions");
    }
}