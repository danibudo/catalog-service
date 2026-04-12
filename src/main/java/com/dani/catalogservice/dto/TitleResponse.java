package com.dani.catalogservice.dto;

import com.dani.catalogservice.model.Title;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TitleResponse(
        UUID id,
        String isbn,
        String title,
        String author,
        String genre,
        Integer publicationYear,
        String description,
        long availableCopies,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static TitleResponse from(Title title, long availableCopies) {
        return new TitleResponse(
                title.getId(),
                title.getIsbn(),
                title.getTitle(),
                title.getAuthor(),
                title.getGenre(),
                title.getPublicationYear(),
                title.getDescription(),
                availableCopies,
                title.getCreatedAt(),
                title.getUpdatedAt()
        );
    }
}