package com.urlshortener.controller;

import com.urlshortener.service.RedirectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@Tag(name = "Redirect", description = "Public redirect endpoint")
public class RedirectController {

    private final RedirectService redirectService;

    public RedirectController(RedirectService redirectService) {
        this.redirectService = redirectService;
    }

    // Exclude known frontend routes: login, register, dashboard, analytics
    // Short codes must be 4–20 chars and NOT match any frontend path segment
    private static final java.util.Set<String> FRONTEND_ROUTES = java.util.Set.of(
            "login", "register", "dashboard", "analytics"
    );

    @GetMapping("/{shortCode:[a-zA-Z0-9\\-]{4,20}}")
    @Operation(summary = "Redirect to original URL")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode,
                                         HttpServletRequest request) {
        // Let Spring's default 404 handler pass these through to the SPA
        if (FRONTEND_ROUTES.contains(shortCode.toLowerCase())) {
            return ResponseEntity.notFound().build();
        }

        String ipAddress = extractClientIp(request);
        String userAgent = request.getHeader(HttpHeaders.USER_AGENT);
        String referer   = request.getHeader(HttpHeaders.REFERER);

        String originalUrl = redirectService.resolveAndTrack(shortCode, ipAddress, userAgent, referer);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Extracts the real client IP, handling common reverse-proxy headers.
     * Order: X-Forwarded-For → X-Real-IP → remote address
     */
    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();  // first entry is client IP
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isBlank()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}