package com.codedbyced.springboot_cache_proxy.service;

import com.codedbyced.springboot_cache_proxy.record.CachedResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    private final Cache<String, CachedResponse> cache;

    public CacheService(Cache<String, CachedResponse> cache) {
        this.cache = cache;
    }

    public CacheService() {
        this(Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumWeight(10 * 1024 * 1024)
                .weigher((String key, CachedResponse response) -> response.body().length)
                .recordStats()
                .build());
    }

    public CachedResponse get(String key) {
        return cache.getIfPresent(key);
    }

    public void put(String key, byte[] body, HttpHeaders headers) {
        cache.put(key, new CachedResponse(body, headers));
    }

    public void clear() {
        cache.invalidateAll();
    }

    public CacheStats stats() {
        return cache.stats();
    }
}