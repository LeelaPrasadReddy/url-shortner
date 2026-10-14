package com.leela.urlshortener.repository;

import com.leela.urlshortener.model.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlMappingRepository
        extends JpaRepository<UrlMapping, Long> {

    Optional<UrlMapping> findByShortCode(String shortCode);

    Optional<UrlMapping> findByOriginalUrl(String originalUrl);

    boolean existsByShortCode(String shortCode);

    List<UrlMapping> findTop5ByOrderByClickCountDesc();
}