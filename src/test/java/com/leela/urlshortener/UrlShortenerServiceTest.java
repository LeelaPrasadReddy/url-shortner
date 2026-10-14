package com.leela.urlshortener;

import com.leela.urlshortener.model.UrlMapping;
import com.leela.urlshortener.repository.UrlMappingRepository;
import com.leela.urlshortener.service.UrlShortenerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UrlShortenerServiceTest {

    @Mock
    private UrlMappingRepository repository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private UrlShortenerService service;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testShortenUrl_NewUrl_ShouldCreateMapping() {
        String originalUrl = "https://www.google.com";
        when(repository.findByOriginalUrl(originalUrl)).thenReturn(Optional.empty());
        when(repository.existsByShortCode(anyString())).thenReturn(false);

        UrlMapping mockMapping = new UrlMapping();
        mockMapping.setOriginalUrl(originalUrl);
        mockMapping.setShortCode("abc123");
        mockMapping.setClickCount(0L);
        when(repository.save(any(UrlMapping.class))).thenReturn(mockMapping);

        UrlMapping result = service.shortenUrl(originalUrl);

        assertNotNull(result);
        assertEquals(originalUrl, result.getOriginalUrl());
        verify(repository, times(1)).save(any(UrlMapping.class));
    }

    @Test
    void testShortenUrl_ExistingUrl_ShouldReturnExisting() {
        String originalUrl = "https://www.google.com";
        UrlMapping existing = new UrlMapping();
        existing.setOriginalUrl(originalUrl);
        existing.setShortCode("abc123");

        when(repository.findByOriginalUrl(originalUrl)).thenReturn(Optional.of(existing));

        UrlMapping result = service.shortenUrl(originalUrl);

        assertEquals("abc123", result.getShortCode());
        verify(repository, never()).save(any());
    }

    @Test
    void testResolveUrl_FromCache_ShouldReturnUrl() {
        String shortCode = "abc123";
        String originalUrl = "https://www.google.com";

        when(valueOperations.get("url:" + shortCode)).thenReturn(originalUrl);
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.of(new UrlMapping()));

        Optional<String> result = service.resolveUrl(shortCode);

        assertTrue(result.isPresent());
        assertEquals(originalUrl, result.get());
    }

    @Test
    void testResolveUrl_NotFound_ShouldReturnEmpty() {
        String shortCode = "notexist";
        when(valueOperations.get("url:" + shortCode)).thenReturn(null);
        when(repository.findByShortCode(shortCode)).thenReturn(Optional.empty());

        Optional<String> result = service.resolveUrl(shortCode);

        assertFalse(result.isPresent());
    }

    @Test
    void testBuildShortUrl_ShouldReturnCorrectFormat() {
        String shortCode = "abc123";
        String shortUrl = service.buildShortUrl(shortCode);
        assertTrue(shortUrl.contains(shortCode));
    }
}
