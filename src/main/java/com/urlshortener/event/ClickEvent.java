package com.urlshortener.event;

/**
 * Published after a successful redirect.
 * Processed asynchronously so the redirect response is never delayed by DB writes.
 */
public class ClickEvent {

    private final Long urlId;
    private final String ipAddress;
    private final String userAgent;
    private final String referer;

    public ClickEvent(Long urlId, String ipAddress, String userAgent, String referer) {
        this.urlId = urlId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.referer = referer;
    }

    public Long getUrlId() { return urlId; }
    public String getIpAddress() { return ipAddress; }
    public String getUserAgent() { return userAgent; }
    public String getReferer() { return referer; }
}
