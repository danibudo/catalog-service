package com.dani.catalogservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTitleRequest(

        @NotBlank
        @Pattern(regexp = "\\d{13}", message = "ISBN must be exactly 13 digits")
        String isbn,

        @NotBlank
        @Size(max = 255)
        String title,

        @NotBlank
        @Size(max = 255)
        String author,

        @NotBlank
        @Size(max = 100)
        String genre,

        @NotNull
        @Positive
        Integer publicationYear,

        String description
) {}