package com.urlshortener.util;

import com.urlshortener.config.AppProperties;
import com.urlshortener.repository.UrlRepository;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class ShortCodeGenerator {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UrlRepository urlRepository;
    private final AppProperties appProperties;

    public ShortCodeGenerator(UrlRepository urlRepository, AppProperties appProperties) {
        this.urlRepository = urlRepository;
        this.appProperties = appProperties;
    }

    /**
     * Generates a unique short code, retrying on collision.
     *
     * @throws IllegalStateException if max retries exceeded
     */
    public String generate() {
        int length = appProperties.getShortCode().getLength();
        int maxRetries = appProperties.getShortCode().getMaxRetries();

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            String code = randomCode(length);
            if (!urlRepository.existsByShortCode(code)) {
                return code;
            }
        }

        throw new IllegalStateException("Failed to generate unique short code after " + maxRetries + " attempts");
    }

    private String randomCode(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
