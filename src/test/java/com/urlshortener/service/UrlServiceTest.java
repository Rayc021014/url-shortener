package com.urlshortener.service;

import com.urlshortener.config.AppProperties;
import com.urlshortener.domain.Url;
import com.urlshortener.domain.User;
import com.urlshortener.dto.request.CreateUrlRequest;
import com.urlshortener.dto.response.UrlResponse;
import com.urlshortener.exception.ConflictException;
import com.urlshortener.repository.UrlRepository;
import com.urlshortener.util.ShortCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock UrlRepository urlRepository;
    @Mock ShortCodeGenerator shortCodeGenerator;
    @Mock UrlCacheService urlCacheService;
    @Mock AppProperties appProperties;

    @InjectMocks UrlService urlService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setEmail("user@example.com");

        AppProperties.Redis redisProps = new AppProperties.Redis();
        when(appProperties.getRedis()).thenReturn(redisProps);
        when(appProperties.getBaseUrl()).thenReturn("http://localhost:8080");
    }

    @Test
    void createUrl_shouldGenerateShortCode_whenNoAliasProvided() {
        // Arrange
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://example.com/very-long-url");

        when(shortCodeGenerator.generate()).thenReturn("abc123");
        when(urlRepository.save(any(Url.class))).thenAnswer(inv -> {
            Url url = inv.getArgument(0);
            url.setShortCode("abc123");
            return url;
        });

        // Act
        UrlResponse response = urlService.createUrl(request, testUser);

        // Assert
        assertThat(response.getShortCode()).isEqualTo("abc123");
        assertThat(response.getShortUrl()).isEqualTo("http://localhost:8080/abc123");
        verify(shortCodeGenerator).generate();
        verify(urlCacheService).cacheUrl(eq("abc123"), eq("https://example.com/very-long-url"), any());

    }

    @Test
    void createUrl_shouldUseCustomAlias_whenProvided() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://example.com");
        request.setCustomAlias("my-link");

        when(urlRepository.existsByShortCode("my-link")).thenReturn(false);
        when(urlRepository.save(any(Url.class))).thenAnswer(inv -> inv.getArgument(0));

        UrlResponse response = urlService.createUrl(request, testUser);

        assertThat(response.getShortCode()).isEqualTo("my-link");
        verify(shortCodeGenerator, never()).generate();  // auto-gen should NOT be called
    }

    @Test
    void createUrl_shouldThrowConflict_whenCustomAliasTaken() {
        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://example.com");
        request.setCustomAlias("taken");

        when(urlRepository.existsByShortCode("taken")).thenReturn(true);

        assertThatThrownBy(() -> urlService.createUrl(request, testUser))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("taken");
    }
}
