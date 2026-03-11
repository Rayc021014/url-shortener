package com.urlshortener.service;

import com.urlshortener.config.AppProperties;
import com.urlshortener.exception.RateLimitExceededException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class UrlCacheService {

    private static final String URL_PREFIX        = "url:";
    private static final String RATE_LIMIT_PREFIX = "rate:";

    private final RedisTemplate<String, String> redisTemplate;
    private final AppProperties appProperties;

    public UrlCacheService(RedisTemplate<String, String> redisTemplate,
                           AppProperties appProperties) {
        this.redisTemplate = redisTemplate;
        this.appProperties = appProperties;
    }

    // ── URL cache ────────────────────────────────────────────────────────────

    /**
     * Cache a short code → original URL mapping.
     *
     * TTL is the LESSER of:
     *  - The configured default (e.g. 1 hour)
     *  - The seconds until the URL actually expires
     *
     * This ensures an expired URL is never served from cache.
     */
    public void cacheUrl(String shortCode, String originalUrl, Instant expiresAt) {
        String key = URL_PREFIX + shortCode;
        long configTtl = appProperties.getRedis().getUrlTtlSeconds();

        long ttl;
        if (expiresAt != null) {
            long secondsUntilExpiry = expiresAt.getEpochSecond() - Instant.now().getEpochSecond();
            if (secondsUntilExpiry <= 0) {
                // Already expired — don't cache at all
                return;
            }
            ttl = Math.min(configTtl, secondsUntilExpiry);
        } else {
            ttl = configTtl;
        }

        redisTemplate.opsForValue().set(key, originalUrl, ttl, TimeUnit.SECONDS);
    }

    /** Returns cached original URL, or null on cache miss. */
    public String getCachedUrl(String shortCode) {
        return redisTemplate.opsForValue().get(URL_PREFIX + shortCode);
    }

    /** Removes a short code from cache (call on update/delete/deactivate). */
    public void evictUrl(String shortCode) {
        redisTemplate.delete(URL_PREFIX + shortCode);
    }

    // ── Rate limiting ────────────────────────────────────────────────────────

    public void checkRateLimit(Long userId) {
        String key = RATE_LIMIT_PREFIX + userId;
        long windowSeconds = appProperties.getRedis().getRateLimitWindowSeconds();
        int maxRequests    = appProperties.getRedis().getRateLimitMaxRequests();

        Long count = redisTemplate.opsForValue().increment(key);

        if (count != null && count == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }

        if (count != null && count > maxRequests) {
            throw new RateLimitExceededException(
                    "Rate limit exceeded. Max " + maxRequests +
                    " requests per " + windowSeconds + " seconds.");
        }
    }
}
