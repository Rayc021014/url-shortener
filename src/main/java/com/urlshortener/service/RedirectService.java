package com.urlshortener.service;

import com.urlshortener.domain.Url;
import com.urlshortener.event.ClickEvent;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.repository.UrlRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class RedirectService {

    private final UrlRepository urlRepository;
    private final UrlCacheService urlCacheService;
    private final ApplicationEventPublisher eventPublisher;

    public RedirectService(UrlRepository urlRepository,
                           UrlCacheService urlCacheService,
                           ApplicationEventPublisher eventPublisher) {
        this.urlRepository = urlRepository;
        this.urlCacheService = urlCacheService;
        this.eventPublisher = eventPublisher;
    }

    public String resolveAndTrack(String shortCode, String ipAddress,
                                  String userAgent, String referer) {

        // ── Fast path: Redis cache ───────────────────────────────────────────
        // NOTE: The cache TTL is already set to expire at the same time as the
        // URL itself (see UrlCacheService.cacheUrl), so a cache hit here is
        // always a valid, non-expired URL. No extra expiry check needed.
        String cached = urlCacheService.getCachedUrl(shortCode);
        if (cached != null) {
            publishClickAsync(shortCode, ipAddress, userAgent, referer);
            return cached;
        }

        // ── Slow path: PostgreSQL ────────────────────────────────────────────
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new ResourceNotFoundException("Short URL not found: " + shortCode));

        if (!url.isRedirectAllowed()) {
            // Evict stale cache entry in case it exists (e.g. manually deactivated)
            urlCacheService.evictUrl(shortCode);
            throw new ResourceNotFoundException("This link is inactive or expired");
        }

        // Cache with the URL's actual expiresAt so the TTL matches expiry precisely
        urlCacheService.cacheUrl(url.getShortCode(), url.getOriginalUrl(), url.getExpiresAt());

        eventPublisher.publishEvent(new ClickEvent(url.getId(), ipAddress, userAgent, referer));

        return url.getOriginalUrl();
    }

    private void publishClickAsync(String shortCode, String ipAddress,
                                   String userAgent, String referer) {
        urlRepository.findByShortCode(shortCode).ifPresent(url ->
                eventPublisher.publishEvent(
                        new ClickEvent(url.getId(), ipAddress, userAgent, referer)));
    }
}
