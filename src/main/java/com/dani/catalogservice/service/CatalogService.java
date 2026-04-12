package com.dani.catalogservice.service;

import com.dani.catalogservice.exception.CopyNotFoundException;
import com.dani.catalogservice.exception.InsufficientPermissionsException;
import com.dani.catalogservice.exception.InvalidOperationException;
import com.dani.catalogservice.exception.IsbnAlreadyExistsException;
import com.dani.catalogservice.exception.TitleNotFoundException;
import com.dani.catalogservice.model.Copy;
import com.dani.catalogservice.model.CopyCondition;
import com.dani.catalogservice.model.CopyStatus;
import com.dani.catalogservice.model.Title;
import com.dani.catalogservice.repository.CopyRepository;
import com.dani.catalogservice.repository.TitleRepository;
import com.dani.catalogservice.repository.TitleSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class CatalogService {

    private static final Logger log = LoggerFactory.getLogger(CatalogService.class);

    private final TitleRepository titleRepository;
    private final CopyRepository copyRepository;

    public CatalogService(TitleRepository titleRepository, CopyRepository copyRepository) {
        this.titleRepository = titleRepository;
        this.copyRepository = copyRepository;
    }

    // -------------------------------------------------------------------------
    // Title — reads
    // -------------------------------------------------------------------------

    public List<Title> getTitles(String author, String genre, Boolean available) {
        Specification<Title> spec = Specification.where(null);
        if (author != null) spec = spec.and(TitleSpecification.authorContains(author));
        if (genre != null)  spec = spec.and(TitleSpecification.genreContains(genre));
        if (Boolean.TRUE.equals(available)) spec = spec.and(TitleSpecification.hasAvailableCopies());
        return titleRepository.findAll(spec);
    }

    public Title getTitle(UUID id) {
        return titleRepository.findById(id)
                .orElseThrow(() -> new TitleNotFoundException(id));
    }

    public long countAvailableCopies(UUID titleId) {
        return copyRepository.countByTitleIdAndStatus(titleId, CopyStatus.AVAILABLE);
    }

    // -------------------------------------------------------------------------
    // Title — writes
    // -------------------------------------------------------------------------

    @Transactional
    public Title createTitle(String isbn, String title, String author, String genre,
                             int publicationYear, String description, CallerContext caller) {
        requireWriteAccess(caller);

        if (titleRepository.existsByIsbn(isbn)) {
            throw new IsbnAlreadyExistsException(isbn);
        }

        Title entity = new Title();
        entity.setIsbn(isbn);
        entity.setTitle(title);
        entity.setAuthor(author);
        entity.setGenre(genre);
        entity.setPublicationYear(publicationYear);
        entity.setDescription(description);

        titleRepository.saveAndFlush(entity);
        log.info("Title created: id={}, isbn={}", entity.getId(), isbn);
        return titleRepository.findById(entity.getId()).orElseThrow();
    }

    @Transactional
    public Title updateTitle(UUID id, String title, String author, String genre,
                             Integer publicationYear, String description, CallerContext caller) {
        requireWriteAccess(caller);

        Title entity = titleRepository.findById(id)
                .orElseThrow(() -> new TitleNotFoundException(id));

        if (title != null)           entity.setTitle(title);
        if (author != null)          entity.setAuthor(author);
        if (genre != null)           entity.setGenre(genre);
        if (publicationYear != null) entity.setPublicationYear(publicationYear);
        if (description != null)     entity.setDescription(description);

        titleRepository.saveAndFlush(entity);
        log.info("Title updated: id={}", id);
        return titleRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void deleteTitle(UUID id, CallerContext caller) {
        requireWriteAccess(caller);

        Title entity = titleRepository.findById(id)
                .orElseThrow(() -> new TitleNotFoundException(id));

        if (copyRepository.existsByTitleIdAndStatus(id, CopyStatus.ON_LOAN)) {
            throw new InvalidOperationException(
                    "Cannot delete title with copies currently on loan: " + id);
        }

        // Remove copies first to satisfy the FK constraint
        List<Copy> copies = copyRepository.findByTitleId(id);
        copyRepository.deleteAll(copies);

        titleRepository.delete(entity);
        log.info("Title deleted: id={}", id);
    }

    // -------------------------------------------------------------------------
    // Copy — reads
    // -------------------------------------------------------------------------

    public List<Copy> getCopiesForTitle(UUID titleId, CallerContext caller) {
        requireWriteAccess(caller);

        if (!titleRepository.existsById(titleId)) {
            throw new TitleNotFoundException(titleId);
        }
        return copyRepository.findByTitleId(titleId);
    }

    // -------------------------------------------------------------------------
    // Copy — writes
    // -------------------------------------------------------------------------

    @Transactional
    public Copy createCopy(UUID titleId, CopyCondition condition, CallerContext caller) {
        requireWriteAccess(caller);

        if (!titleRepository.existsById(titleId)) {
            throw new TitleNotFoundException(titleId);
        }

        Copy copy = new Copy();
        copy.setTitleId(titleId);
        copy.setCondition(condition);

        copyRepository.saveAndFlush(copy);
        log.info("Copy created: id={}, titleId={}", copy.getId(), titleId);
        return copyRepository.findById(copy.getId()).orElseThrow();
    }

    @Transactional
    public Copy updateCopy(UUID id, CopyCondition condition, CallerContext caller) {
        requireWriteAccess(caller);

        Copy copy = copyRepository.findById(id)
                .orElseThrow(() -> new CopyNotFoundException(id));

        copy.setCondition(condition);

        copyRepository.saveAndFlush(copy);
        log.info("Copy updated: id={}", id);
        return copyRepository.findById(id).orElseThrow();
    }

    @Transactional
    public void deleteCopy(UUID id, CallerContext caller) {
        requireWriteAccess(caller);

        Copy copy = copyRepository.findById(id)
                .orElseThrow(() -> new CopyNotFoundException(id));

        if (CopyStatus.ON_LOAN.equals(copy.getStatus())) {
            throw new InvalidOperationException(
                    "Cannot decommission a copy that is currently on loan: " + id);
        }

        copyRepository.delete(copy);
        log.info("Copy decommissioned: id={}", id);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private void requireWriteAccess(CallerContext caller) {
        if (caller == null || !caller.canWrite()) {
            throw new InsufficientPermissionsException();
        }
    }
}