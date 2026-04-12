package com.dani.catalogservice.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "copies")
public class Copy {

    @Id
    private UUID id;

    @Column(name = "title_id", nullable = false, updatable = false)
    private UUID titleId;

    // Persisted via CopyCondition.PersistenceConverter (autoApply = true)
    @Column(nullable = false, length = 20)
    private CopyCondition condition;

    // Persisted via CopyStatus.PersistenceConverter (autoApply = true)
    @Column(nullable = false, length = 20)
    private CopyStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    // Maintained by DB trigger — never written by the application
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (status == null) {
            status = CopyStatus.AVAILABLE;
        }
        createdAt = OffsetDateTime.now(ZoneOffset.UTC);
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getTitleId() {
        return titleId;
    }

    public void setTitleId(UUID titleId) {
        this.titleId = titleId;
    }

    public CopyCondition getCondition() {
        return condition;
    }

    public void setCondition(CopyCondition condition) {
        this.condition = condition;
    }

    public CopyStatus getStatus() {
        return status;
    }

    public void setStatus(CopyStatus status) {
        this.status = status;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}