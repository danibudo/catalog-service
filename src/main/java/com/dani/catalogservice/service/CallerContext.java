package com.dani.catalogservice.service;

import com.dani.catalogservice.model.UserRole;

import java.util.UUID;

/**
 * Represents the authenticated caller extracted from gateway-injected headers
 * (X-User-Id and X-User-Role). Both fields may be null for unauthenticated requests
 * on public endpoints.
 */
public record CallerContext(UUID userId, UserRole role) {

    public boolean canWrite() {
        return UserRole.LIBRARIAN.equals(role) || UserRole.SUPER_ADMIN.equals(role);
    }
}