package com.urlshortener.dto.request;

import jakarta.validation.constraints.Pattern;

import java.time.Instant;

// All fields optional — only non-null fields are applied (PATCH semantics)
public class UpdateUrlRequest {

    @Pattern(regexp = "^[a-zA-Z0-9\\-]{4,20}$",
             message = "Alias must be 4–20 alphanumeric characters or hyphens")
    private String customAlias;

    private Boolean isActive;

    private Instant expiresAt;

    public String getCustomAlias() { return customAlias; }
    public void setCustomAlias(String customAlias) { this.customAlias = customAlias; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
