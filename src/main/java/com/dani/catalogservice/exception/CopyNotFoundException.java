package com.dani.catalogservice.exception;

import java.util.UUID;

public class CopyNotFoundException extends RuntimeException {

    public CopyNotFoundException(UUID id) {
        super("Copy not found: " + id);
    }
}