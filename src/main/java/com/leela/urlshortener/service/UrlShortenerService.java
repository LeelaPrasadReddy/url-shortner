package com.leela.urlshortener.service;

import com.leela.urlshortener.model.UrlMapping;
import com.leela.urlshortener.repository.UrlMappingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class UrlShortenerService {

    private final UrlMappingRepository repository;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.short-code-length}")
    private int shortCodeLength;

    private static final String CHARACTERS =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static final String REDIS_PREFIX = "url:";

    private static final long CACHE_TTL_HOURS = 24;

    private static final SecureRandom RANDOM = new SecureRandom();

    public UrlShortenerService(
            UrlMappingRepository repository,
            RedisTemplate<String, String> redisTemplate) {

        this.repository = repository;
        this.redisTemplate = redisTemplate;
    }

    public UrlMapping shortenUrl(String originalUrl) {

        Optional<UrlMapping> existing =
                repository.findByOriginalUrl(originalUrl);

        if (existing.isPresent()) {
            return existing.get();
        }

        String shortCode = generateUniqueShortCode();

        UrlMapping mapping = new UrlMapping();
        mapping.setOriginalUrl(originalUrl);
        mapping.setShortCode(shortCode);
        mapping.setClickCount(0L);

        UrlMapping saved = repository.save(mapping);

        redisTemplate.opsForValue().set(
                REDIS_PREFIX + shortCode,
                originalUrl,
                CACHE_TTL_HOURS,
                TimeUnit.HOURS
        );

        return saved;
    }

    public Optional<String> resolveUrl(String shortCode) {

        String cachedUrl =
                redisTemplate.opsForValue()
                        .get(REDIS_PREFIX + shortCode);

        if (cachedUrl != null) {
            updateClickCount(shortCode);
            return Optional.of(cachedUrl);
        }

        Optional<UrlMapping> mapping =
                repository.findByShortCode(shortCode);

        if (mapping.isPresent()) {

            redisTemplate.opsForValue().set(
                    REDIS_PREFIX + shortCode,
                    mapping.get().getOriginalUrl(),
                    CACHE_TTL_HOURS,
                    TimeUnit.HOURS
            );

            updateClickCount(shortCode);

            return Optional.of(mapping.get().getOriginalUrl());
        }

        return Optional.empty();
    }

    public Optional<UrlMapping> getAnalytics(String shortCode) {
        return repository.findByShortCode(shortCode);
    }

    public List<UrlMapping> getTopUrls() {
        return repository.findTop5ByOrderByClickCountDesc();
    }

    public List<UrlMapping> getAllUrls() {
        return repository.findAll();
    }

    public String buildShortUrl(String shortCode) {
        return baseUrl + "/" + shortCode;
    }

    private void updateClickCount(String shortCode) {

        repository.findByShortCode(shortCode)
                .ifPresent(mapping -> {

                    mapping.setClickCount(
                            mapping.getClickCount() + 1
                    );

                    mapping.setLastAccessedAt(
                            LocalDateTime.now()
                    );

                    repository.save(mapping);
                });
    }

    private String generateUniqueShortCode() {

        String code;

        do {
            code = generateRandomCode();
        } while (repository.existsByShortCode(code));

        return code;
    }

    private String generateRandomCode() {

        StringBuilder code =
                new StringBuilder(shortCodeLength);

        for (int i = 0; i < shortCodeLength; i++) {
            int index = RANDOM.nextInt(CHARACTERS.length());
            code.append(CHARACTERS.charAt(index));
        }

        return code.toString();
    }
}