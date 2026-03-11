package com.urlshortener.dto.response;

import com.urlshortener.domain.Url;

import java.time.Instant;

public class UrlResponse {

    private Long id;
    private String originalUrl;
    private String shortCode;
    private String shortUrl;
    private boolean isActive;
    private Instant expiresAt;
    private Instant createdAt;

    public static UrlResponse from(Url url, String baseUrl) {
        UrlResponse r = new UrlResponse();
        r.id = url.getId();
        r.originalUrl = url.getOriginalUrl();
        r.shortCode = url.getShortCode();
        r.shortUrl = baseUrl + "/" + url.getShortCode();
        r.isActive = url.isActive();
        r.expiresAt = url.getExpiresAt();
        r.createdAt = url.getCreatedAt();
        return r;
    }

    public Long getId() { return id; }
    public String getOriginalUrl() { return originalUrl; }
    public String getShortCode() { return shortCode; }
    public String getShortUrl() { return shortUrl; }
    public boolean isActive() { return isActive; }
    public Instant getExpiresAt() { return expiresAt; }
    public Instant getCreatedAt() { return createdAt; }
}
