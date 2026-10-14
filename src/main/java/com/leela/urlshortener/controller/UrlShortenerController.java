package com.leela.urlshortener.controller;

import com.leela.urlshortener.model.UrlMapping;
import com.leela.urlshortener.service.UrlShortenerService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*")
public class UrlShortenerController {

    private final UrlShortenerService service;

    public UrlShortenerController(UrlShortenerService service) {
        this.service = service;
    }

    // POST /api/shorten — create short URL
    @PostMapping("/api/shorten")
    public ResponseEntity<?> shortenUrl(@RequestBody @Valid ShortenRequest request) {
        try {
            UrlMapping mapping = service.shortenUrl(request.getUrl());
            return ResponseEntity.ok(Map.of(
                "shortCode", mapping.getShortCode(),
                "shortUrl", service.buildShortUrl(mapping.getShortCode()),
                "originalUrl", mapping.getOriginalUrl(),
                "clickCount", mapping.getClickCount(),
                "createdAt", mapping.getCreatedAt().toString()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /{shortCode} — redirect to original URL
    @GetMapping("/{shortCode:[A-Za-z0-9]{6}}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode) {
        Optional<String> originalUrl = service.resolveUrl(shortCode);
        if (originalUrl.isPresent()) {
            return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", originalUrl.get())
                .build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Short URL not found"));
    }

    // GET /api/analytics/{shortCode} — get click analytics
    @GetMapping("/api/analytics/{shortCode}")
    public ResponseEntity<?> getAnalytics(@PathVariable String shortCode) {
        Optional<UrlMapping> mapping = service.getAnalytics(shortCode);
        if (mapping.isPresent()) {
            UrlMapping m = mapping.get();
            return ResponseEntity.ok(Map.of(
                "shortCode", m.getShortCode(),
                "shortUrl", service.buildShortUrl(m.getShortCode()),
                "originalUrl", m.getOriginalUrl(),
                "clickCount", m.getClickCount(),
                "createdAt", m.getCreatedAt().toString(),
                "lastAccessedAt", m.getLastAccessedAt() != null ?
                    m.getLastAccessedAt().toString() : "Never"
            ));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("error", "Short URL not found"));
    }

    // GET /api/urls — get all URLs with analytics
    @GetMapping("/api/urls")
    public ResponseEntity<List<UrlMapping>> getAllUrls() {
        return ResponseEntity.ok(service.getAllUrls());
    }

    // GET /api/urls/top — get top clicked URLs
    @GetMapping("/api/urls/top")
    public ResponseEntity<List<UrlMapping>> getTopUrls() {
        return ResponseEntity.ok(service.getTopUrls());
    }

    // Request DTO
    public static class ShortenRequest {
        @NotBlank(message = "URL cannot be blank")
        @Pattern(regexp = "^(https?://).+", message = "URL must start with http:// or https://")
        private String url;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }
}
