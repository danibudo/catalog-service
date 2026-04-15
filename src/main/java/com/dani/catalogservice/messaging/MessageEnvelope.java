package com.dani.catalogservice.messaging;
import java.time.Instant;
import java.util.UUID;

public record MessageEnvelope<T>(String event, T data, EventMetadata metadata) {
    public static <T> MessageEnvelope<T> of(String event, T data) {
        return new MessageEnvelope<>(
                event,
                data,
                new EventMetadata(Instant.now().toString(), UUID.randomUUID().toString())
        );
    }
}

