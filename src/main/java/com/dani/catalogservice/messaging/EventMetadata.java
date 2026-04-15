package com.dani.catalogservice.messaging;

public record EventMetadata(String timestamp, String correlationId) {}
