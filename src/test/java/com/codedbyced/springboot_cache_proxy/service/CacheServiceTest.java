package com.codedbyced.springboot_cache_proxy.service;

import com.codedbyced.springboot_cache_proxy.record.CachedResponse;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CacheServiceTest {

    @Mock
    private Cache<String, CachedResponse> mockCache;

    @Mock
    private HttpHeaders mockHeaders;

    @Mock
    private CacheStats mockCacheStats;


    private CacheService cacheService;

    @BeforeEach
    public void setUp() {
        cacheService = new CacheService(mockCache);
    }

    @Test
    void should_construct_with_default_constructor() {
        CacheService cacheService = new CacheService();
        assertThat(cacheService).isNotNull();
    }

    @Test
    void should_get_when_get_given_key() {
        cacheService.get("abc");
        verify(mockCache).getIfPresent("abc");
    }

    @Test
    void should_put_when_put_given_key_body_header() {
        cacheService.put("abc", "xyz".getBytes(), mockHeaders);
        verify(mockCache).put(eq("abc"), any(CachedResponse.class));
    }

    @Test
    void should_clear_cache(){
        cacheService.clear();
        verify(mockCache).invalidateAll();
    }

    @Test
    void should_return_stats(){
        when(mockCache.stats()).thenReturn(mockCacheStats);
        CacheStats stats = cacheService.stats();
        verify(mockCache).stats();
        assertThat(stats).isInstanceOf(CacheStats.class);
    }
}
