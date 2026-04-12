package com.dani.catalogservice.exception;

public class IsbnAlreadyExistsException extends RuntimeException {

    public IsbnAlreadyExistsException(String isbn) {
        super("ISBN already in use: " + isbn);
    }
}