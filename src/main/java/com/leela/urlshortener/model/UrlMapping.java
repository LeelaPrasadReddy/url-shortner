package com.leela.urlshortener.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "url_mappings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UrlMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_url", nullable = false, length = 2048)
    private String originalUrl;

    @Column(name = "short_code", nullable = false, unique = true, length = 10)
    private String shortCode;

    @Column(name = "click_count", nullable = false)
    private Long clickCount = 0L;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "last_accessed_at")
    private LocalDateTime lastAccessedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
