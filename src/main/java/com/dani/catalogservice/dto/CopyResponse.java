package com.dani.catalogservice.dto;

import com.dani.catalogservice.model.Copy;
import com.dani.catalogservice.model.CopyCondition;
import com.dani.catalogservice.model.CopyStatus;

import java.time.OffsetDateTime;
import java.util.UUID;

public record CopyResponse(
        UUID id,
        UUID titleId,
        CopyCondition condition,
        CopyStatus status,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static CopyResponse from(Copy copy) {
        return new CopyResponse(
                copy.getId(),
                copy.getTitleId(),
                copy.getCondition(),
                copy.getStatus(),
                copy.getCreatedAt(),
                copy.getUpdatedAt()
        );
    }
}