package com.urlshortener.service;

import com.urlshortener.config.AppProperties;
import com.urlshortener.domain.Url;
import com.urlshortener.domain.User;
import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.request.UpdateUrlRequest;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.exception.ConflictException;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.ShortCodeGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final UrlCacheService urlCacheService;
    private final AppProperties appProperties;

    public UrlService(UrlRepository urlRepository,
                      ShortCodeGenerator shortCodeGenerator,
                      UrlCacheService urlCacheService,
                      AppProperties appProperties) {
        this.urlRepository = urlRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.urlCacheService = urlCacheService;
        this.appProperties = appProperties;
    }

    @Transactional
    public UrlResponse createUrl(CreateUrlRequest request, User currentUser) {
        urlCacheService.checkRateLimit(currentUser.getId());

        // Determine short code: custom alias or auto-generated
        String shortCode;
        if (request.getCustomAlias() != null && !request.getCustomAlias().isBlank()) {
            shortCode = request.getCustomAlias();
            if (urlRepository.existsByShortCode(shortCode)) {
                throw new ConflictException("Alias '" + shortCode + "' is already taken");
            }
        } else {
            shortCode = shortCodeGenerator.generate();
        }

        Url url = new Url();
        url.setUser(currentUser);
        url.setOriginalUrl(request.getOriginalUrl());
        url.setShortCode(shortCode);
        url.setExpiresAt(request.getExpiresAt());

        Url saved = urlRepository.save(url);
        urlCacheService.cacheUrl(saved.getShortCode(), saved.getOriginalUrl(), saved.getExpiresAt());

        return UrlResponse.from(saved, appProperties.getBaseUrl());
    }

    @Transactional(readOnly = true)
    public Page<UrlResponse> listUrls(Long userId, Pageable pageable) {
        return urlRepository.findByUserId(userId, pageable)
                .map(url -> UrlResponse.from(url, appProperties.getBaseUrl()));
    }

    @Transactional(readOnly = true)
    public UrlResponse getUrl(Long urlId, Long userId) {
        Url url = findOwnedUrl(urlId, userId);
        return UrlResponse.from(url, appProperties.getBaseUrl());
    }

    @Transactional
    public UrlResponse updateUrl(Long urlId, UpdateUrlRequest request, Long userId) {
        Url url = findOwnedUrl(urlId, userId);

        // Apply only fields that were provided (PATCH semantics)
        if (request.getCustomAlias() != null) {
            String newCode = request.getCustomAlias();
            if (!newCode.equals(url.getShortCode())) {
                if (urlRepository.existsByShortCode(newCode)) {
                    throw new ConflictException("Alias '" + newCode + "' is already taken");
                }
                urlCacheService.evictUrl(url.getShortCode());  // evict old code
                url.setShortCode(newCode);
            }
        }

        if (request.getIsActive() != null) {
            url.setActive(request.getIsActive());
            if (!request.getIsActive()) {
                urlCacheService.evictUrl(url.getShortCode());  // evict inactive URLs
            }
        }

        if (request.getExpiresAt() != null) {
            url.setExpiresAt(request.getExpiresAt());
        }

        Url saved = urlRepository.save(url);

        // Re-cache with new data if still active
        if (saved.isActive()) {
            urlCacheService.cacheUrl(saved.getShortCode(), saved.getOriginalUrl(), saved.getExpiresAt());
        }

        return UrlResponse.from(saved, appProperties.getBaseUrl());
    }

    @Transactional
    public void deleteUrl(Long urlId, Long userId) {
        Url url = findOwnedUrl(urlId, userId);
        urlCacheService.evictUrl(url.getShortCode());
        urlRepository.delete(url);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private Url findOwnedUrl(Long urlId, Long userId) {
        return urlRepository.findByIdAndUserId(urlId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found: " + urlId));
    }
}
