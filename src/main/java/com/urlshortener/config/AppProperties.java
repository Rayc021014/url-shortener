package com.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String baseUrl;
    private Jwt jwt = new Jwt();
    private ShortCode shortCode = new ShortCode();
    private Redis redis = new Redis();
    private Cors cors = new Cors();

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }

    public ShortCode getShortCode() { return shortCode; }
    public void setShortCode(ShortCode shortCode) { this.shortCode = shortCode; }

    public Redis getRedis() { return redis; }
    public void setRedis(Redis redis) { this.redis = redis; }

    public Cors getCors() { return cors; }
    public void setCors(Cors cors) { this.cors = cors; }

    // ── Nested config classes ────────────────────────────────────────────────

    public static class Jwt {
        private String secret;
        private long accessTokenExpiryMs;
        private long refreshTokenExpiryMs;

        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }

        public long getAccessTokenExpiryMs() { return accessTokenExpiryMs; }
        public void setAccessTokenExpiryMs(long accessTokenExpiryMs) { this.accessTokenExpiryMs = accessTokenExpiryMs; }

        public long getRefreshTokenExpiryMs() { return refreshTokenExpiryMs; }
        public void setRefreshTokenExpiryMs(long refreshTokenExpiryMs) { this.refreshTokenExpiryMs = refreshTokenExpiryMs; }
    }

    public static class ShortCode {
        private int length = 6;
        private int maxRetries = 5;

        public int getLength() { return length; }
        public void setLength(int length) { this.length = length; }

        public int getMaxRetries() { return maxRetries; }
        public void setMaxRetries(int maxRetries) { this.maxRetries = maxRetries; }
    }

    public static class Redis {
        private long urlTtlSeconds = 3600;
        private long rateLimitWindowSeconds = 60;
        private int rateLimitMaxRequests = 30;

        public long getUrlTtlSeconds() { return urlTtlSeconds; }
        public void setUrlTtlSeconds(long urlTtlSeconds) { this.urlTtlSeconds = urlTtlSeconds; }

        public long getRateLimitWindowSeconds() { return rateLimitWindowSeconds; }
        public void setRateLimitWindowSeconds(long rateLimitWindowSeconds) { this.rateLimitWindowSeconds = rateLimitWindowSeconds; }

        public int getRateLimitMaxRequests() { return rateLimitMaxRequests; }
        public void setRateLimitMaxRequests(int rateLimitMaxRequests) { this.rateLimitMaxRequests = rateLimitMaxRequests; }
    }

    public static class Cors {
        private String allowedOrigins = "http://localhost:5173";

        public String getAllowedOrigins() { return allowedOrigins; }
        public void setAllowedOrigins(String allowedOrigins) { this.allowedOrigins = allowedOrigins; }
    }
}
