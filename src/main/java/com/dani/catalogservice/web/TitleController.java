package com.dani.catalogservice.web;

import com.dani.catalogservice.dto.CopyResponse;
import com.dani.catalogservice.dto.CreateCopyRequest;
import com.dani.catalogservice.dto.CreateTitleRequest;
import com.dani.catalogservice.dto.TitleResponse;
import com.dani.catalogservice.dto.UpdateTitleRequest;
import com.dani.catalogservice.service.CallerContext;
import com.dani.catalogservice.service.CatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/titles")
public class TitleController {

    private final CatalogService catalogService;

    public TitleController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    public List<TitleResponse> getTitles(
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Boolean available,
            @Nullable CallerContext caller) {
        return catalogService.getTitles(author, genre, available).stream()
                .map(t -> TitleResponse.from(t, catalogService.countAvailableCopies(t.getId())))
                .toList();
    }

    @GetMapping("/{id}")
    public TitleResponse getTitle(@PathVariable UUID id, @Nullable CallerContext caller) {
        var title = catalogService.getTitle(id);
        return TitleResponse.from(title, catalogService.countAvailableCopies(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TitleResponse createTitle(@Valid @RequestBody CreateTitleRequest request,
                                     CallerContext caller) {
        var title = catalogService.createTitle(
                request.isbn(), request.title(), request.author(),
                request.genre(), request.publicationYear(), request.description(),
                caller);
        return TitleResponse.from(title, catalogService.countAvailableCopies(title.getId()));
    }

    @PatchMapping("/{id}")
    public TitleResponse updateTitle(@PathVariable UUID id,
                                     @Valid @RequestBody UpdateTitleRequest request,
                                     CallerContext caller) {
        var title = catalogService.updateTitle(
                id, request.title(), request.author(),
                request.genre(), request.publicationYear(), request.description(),
                caller);
        return TitleResponse.from(title, catalogService.countAvailableCopies(id));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTitle(@PathVariable UUID id, CallerContext caller) {
        catalogService.deleteTitle(id, caller);
    }

    @GetMapping("/{id}/copies")
    public List<CopyResponse> getCopies(@PathVariable UUID id, CallerContext caller) {
        return catalogService.getCopiesForTitle(id, caller).stream()
                .map(CopyResponse::from)
                .toList();
    }

    @PostMapping("/{id}/copies")
    @ResponseStatus(HttpStatus.CREATED)
    public CopyResponse createCopy(@PathVariable UUID id,
                                   @Valid @RequestBody CreateCopyRequest request,
                                   CallerContext caller) {
        return CopyResponse.from(catalogService.createCopy(id, request.condition(), caller));
    }
}