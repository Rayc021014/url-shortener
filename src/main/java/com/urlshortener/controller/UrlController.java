package com.urlshortener.controller;

import com.urlshortener.domain.User;
import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.request.UpdateUrlRequest;
import com.urlshortener.dto.response.AnalyticsResponse;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.service.ClickService;
import com.urlshortener.service.UrlService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
@Tag(name = "URLs", description = "Create and manage shortened URLs")
@SecurityRequirement(name = "bearerAuth")
public class UrlController {

    private final UrlService urlService;
    private final ClickService clickService;

    public UrlController(UrlService urlService, ClickService clickService) {
        this.urlService = urlService;
        this.clickService = clickService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a shortened URL")
    public UrlResponse create(@Valid @RequestBody CreateUrlRequest request,
                              @AuthenticationPrincipal User currentUser) {
        return urlService.createUrl(request, currentUser);
    }

    @GetMapping
    @Operation(summary = "List all your URLs (paginated)")
    public Page<UrlResponse> list(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return urlService.listUrls(currentUser.getId(), pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a single URL by ID")
    public UrlResponse get(@PathVariable Long id,
                           @AuthenticationPrincipal User currentUser) {
        return urlService.getUrl(id, currentUser.getId());
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update alias, expiry, or active status")
    public UrlResponse update(@PathVariable Long id,
                              @Valid @RequestBody UpdateUrlRequest request,
                              @AuthenticationPrincipal User currentUser) {
        return urlService.updateUrl(id, request, currentUser.getId());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a URL")
    public void delete(@PathVariable Long id,
                       @AuthenticationPrincipal User currentUser) {
        urlService.deleteUrl(id, currentUser.getId());
    }

    @GetMapping("/{id}/analytics")
    @Operation(summary = "Get click analytics for a URL (last 30 days)")
    public AnalyticsResponse analytics(@PathVariable Long id,
                                       @AuthenticationPrincipal User currentUser) {
        return clickService.getAnalytics(id, currentUser.getId());
    }
}
