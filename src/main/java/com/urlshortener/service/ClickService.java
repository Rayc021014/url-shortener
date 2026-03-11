package com.urlshortener.service;

import com.urlshortener.domain.Click;
import com.urlshortener.domain.Url;
import com.urlshortener.dto.response.AnalyticsResponse;
import com.urlshortener.event.ClickEvent;
import com.urlshortener.exception.ResourceNotFoundException;
import com.urlshortener.repository.ClickRepository;
import com.urlshortener.repository.UrlRepository;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class ClickService {

    private final ClickRepository clickRepository;
    private final UrlRepository urlRepository;

    public ClickService(ClickRepository clickRepository, UrlRepository urlRepository) {
        this.clickRepository = clickRepository;
        this.urlRepository = urlRepository;
    }

    /**
     * Async listener — fires after redirect, persists the click without blocking the HTTP response.
     * @EnableAsync in the main class enables this.
     */
    @Async
    @EventListener
    @Transactional
    public void handleClickEvent(ClickEvent event) {
        Url url = urlRepository.findById(event.getUrlId()).orElse(null);
        if (url == null) return;   // URL may have been deleted since the event was fired

        Click click = new Click();
        click.setUrl(url);
        click.setIpAddress(event.getIpAddress());
        click.setUserAgent(event.getUserAgent());
        click.setReferer(event.getReferer());
        clickRepository.save(click);
    }

    /** Returns analytics for a URL owned by the given user. */
    @Transactional(readOnly = true)
    public AnalyticsResponse getAnalytics(Long urlId, Long userId) {
        Url url = urlRepository.findByIdAndUserId(urlId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("URL not found: " + urlId));

        long totalClicks = clickRepository.countByUrlId(urlId);

        // Last 30 days, grouped by day
        Instant since = Instant.now().minus(30, ChronoUnit.DAYS);
        List<Object[]> rawRows = clickRepository.countClicksByDaySince(urlId, since);

        List<AnalyticsResponse.DailyCount> dailyCounts = rawRows.stream()
                .map(row -> new AnalyticsResponse.DailyCount(
                        row[0].toString(),        // date string
                        ((Number) row[1]).longValue()
                ))
                .toList();

        AnalyticsResponse response = new AnalyticsResponse();
        response.setUrlId(urlId);
        response.setShortCode(url.getShortCode());
        response.setTotalClicks(totalClicks);
        response.setClicksPerDay(dailyCounts);
        return response;
    }
}
