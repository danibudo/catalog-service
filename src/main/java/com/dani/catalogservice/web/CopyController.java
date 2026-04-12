package com.dani.catalogservice.web;

import com.dani.catalogservice.dto.CopyResponse;
import com.dani.catalogservice.dto.UpdateCopyRequest;
import com.dani.catalogservice.service.CallerContext;
import com.dani.catalogservice.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/copies")
public class CopyController {

    private final CatalogService catalogService;

    public CopyController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @PatchMapping("/{id}")
    public CopyResponse updateCopy(@PathVariable UUID id,
                                   @Valid @RequestBody UpdateCopyRequest request,
                                   CallerContext caller) {
        return CopyResponse.from(catalogService.updateCopy(id, request.condition(), caller));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCopy(@PathVariable UUID id, CallerContext caller) {
        catalogService.deleteCopy(id, caller);
    }
}