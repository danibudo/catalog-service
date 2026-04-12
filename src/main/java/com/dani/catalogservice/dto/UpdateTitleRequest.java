package com.dani.catalogservice.dto;

import jakarta.validation.constraints.Size;

public record UpdateTitleRequest(

        @Size(max = 255)
        String title,

        @Size(max = 255)
        String author,

        @Size(max = 100)
        String genre,

        Integer publicationYear,

        String description
) {}