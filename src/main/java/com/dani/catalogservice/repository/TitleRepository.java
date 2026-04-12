package com.dani.catalogservice.repository;

import com.dani.catalogservice.model.Title;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface TitleRepository extends JpaRepository<Title, UUID>, JpaSpecificationExecutor<Title> {

    boolean existsByIsbn(String isbn);
}