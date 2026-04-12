package com.dani.catalogservice.exception;

import java.util.UUID;

public class TitleNotFoundException extends RuntimeException {

    public TitleNotFoundException(UUID id) {
        super("Title not found: " + id);
    }
}