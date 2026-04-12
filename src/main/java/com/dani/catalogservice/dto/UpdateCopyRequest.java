package com.dani.catalogservice.dto;

import com.dani.catalogservice.model.CopyCondition;
import jakarta.validation.constraints.NotNull;

public record UpdateCopyRequest(

        @NotNull
        CopyCondition condition
) {}