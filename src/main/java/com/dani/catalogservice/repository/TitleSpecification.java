package com.dani.catalogservice.repository;

import com.dani.catalogservice.model.Copy;
import com.dani.catalogservice.model.CopyStatus;
import com.dani.catalogservice.model.Title;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import org.springframework.data.jpa.domain.Specification;

public class TitleSpecification {

    private TitleSpecification() {}

    public static Specification<Title> authorContains(String author) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("author")), "%" + author.toLowerCase() + "%");
    }

    public static Specification<Title> genreContains(String genre) {
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("genre")), "%" + genre.toLowerCase() + "%");
    }

    public static Specification<Title> hasAvailableCopies() {
        return (root, query, cb) -> {
            Subquery<Long> sub = query.subquery(Long.class);
            Root<Copy> copy = sub.from(Copy.class);
            sub.select(cb.count(copy))
               .where(
                   cb.equal(copy.get("titleId"), root.get("id")),
                   cb.equal(copy.get("status"), CopyStatus.AVAILABLE)
               );
            return cb.greaterThan(sub, 0L);
        };
    }
}