package com.urlshortener.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

public class CreateUrlRequest {

    @NotBlank(message = "URL is required")
    @URL(message = "Must be a valid URL")
    private String originalUrl;

    // Optional custom alias: alphanumeric + hyphens, 4–20 chars
    @Pattern(regexp = "^[a-zA-Z0-9\\-]{4,20}$",
             message = "Alias must be 4–20 alphanumeric characters or hyphens")
    private String customAlias;

    // Optional expiry — null means never expires
    private Instant expiresAt;

    public String getOriginalUrl() { return originalUrl; }
    public void setOriginalUrl(String originalUrl) { this.originalUrl = originalUrl; }

    public String getCustomAlias() { return customAlias; }
    public void setCustomAlias(String customAlias) { this.customAlias = customAlias; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
