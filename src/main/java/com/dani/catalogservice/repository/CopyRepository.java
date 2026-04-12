package com.dani.catalogservice.repository;

import com.dani.catalogservice.model.Copy;
import com.dani.catalogservice.model.CopyStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CopyRepository extends JpaRepository<Copy, UUID> {

    List<Copy> findByTitleId(UUID titleId);

    Optional<Copy> findFirstByTitleIdAndStatus(UUID titleId, CopyStatus status);

    long countByTitleIdAndStatus(UUID titleId, CopyStatus status);

    boolean existsByTitleIdAndStatus(UUID titleId, CopyStatus status);
}