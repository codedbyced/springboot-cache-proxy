package com.codedbyced.springboot_cache_proxy.controller;

import com.codedbyced.springboot_cache_proxy.record.CachedResponse;
import com.codedbyced.springboot_cache_proxy.service.CacheService;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@TestPropertySource(properties = {"origin.url=http://localhost"})
@WebMvcTest(ProxyController.class)
public class ProxyControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @TestConfiguration
    static class MockBeans {
        @Bean
        public RestTemplate restTemplate() {
            return mock(RestTemplate.class);
        }

        @Bean
        public CacheService cacheService() {
            return mock(CacheService.class);
        }
    }

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CacheService cacheService;

    @Test
    void should_return_cached_response_when_exists() throws Exception {
        HttpHeaders cachedHeaders = new HttpHeaders();
        cachedHeaders.set("Content-Type", "text/plain");

        byte[] cachedBody = "cached".getBytes();
        CachedResponse cachedResponse = new CachedResponse(cachedBody, cachedHeaders);

        when(cacheService.get("/test")).thenReturn(cachedResponse);

        mockMvc.perform(get("/test"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Cache", "HIT"))
                .andExpect(content().bytes(cachedBody));
    }

    @Test
    void should_forward_and_cache_when_not_cached() throws Exception {
        when(cacheService.get("/test")).thenReturn(null);

        HttpHeaders originHeaders = new HttpHeaders();
        originHeaders.setContentType(MediaType.TEXT_PLAIN);

        byte[] originBody = "origin".getBytes();
        ResponseEntity<byte[]> mockResponse = new ResponseEntity<>(originBody, originHeaders, HttpStatus.OK);

        when(restTemplate.exchange(eq("http://localhost/test"), eq(HttpMethod.GET), isNull(), eq(byte[].class)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/test").header("Host", "localhost"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Cache", "MISS"))
                .andExpect(content().bytes(originBody));

        verify(cacheService).put(eq("/test"), eq(originBody), any());
    }

    @Test
    void should_return_error_when_origin_fails() throws Exception {
        when(cacheService.get("/fail")).thenReturn(null);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);

        HttpServerErrorException exception = HttpServerErrorException.create(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Origin failure",
                headers,
                "Origin failure response body".getBytes(),
                null
        );

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(byte[].class)))
                .thenThrow(exception);

        mockMvc.perform(get("/fail"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("X-Cache", "ERROR"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Origin failure response body")));
    }

    @Test
    void should_return_gateway_timeout_when_origin_unreachable() throws Exception {
        when(cacheService.get("/timeout")).thenReturn(null);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(byte[].class)))
                .thenThrow(new org.springframework.web.client.ResourceAccessException("Connection refused"));

        mockMvc.perform(get("/timeout"))
                .andExpect(status().isGatewayTimeout())
                .andExpect(header().string("X-Cache", "ERROR"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Origin server not reachable")));
    }

    @Test
    void should_return_internal_server_error_for_unexpected_exception() throws Exception {
        when(cacheService.get("/unexpected")).thenReturn(null);

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(byte[].class)))
                .thenThrow(new RuntimeException("Something went wrong"));

        mockMvc.perform(get("/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(header().string("X-Cache", "ERROR"))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Unexpected error: Something went wrong")));
    }

    @Test
    void should_return_empty_cache_stats_string() throws Exception {
        when(cacheService.stats()).thenReturn(CacheStats.empty());

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(content().string(CacheStats.empty().toString()));
    }

    @Test
    void should_return_mocked_cache_stats_string() throws Exception {
        CacheStats mockStats = mock(CacheStats.class);
        when(mockStats.toString()).thenReturn("hitCount=5, missCount=2, loadSuccessCount=1");
        when(cacheService.stats()).thenReturn(mockStats);

        mockMvc.perform(get("/stats"))
                .andExpect(status().isOk())
                .andExpect(content().string("hitCount=5, missCount=2, loadSuccessCount=1"));
    }

    @Test
    void should_clear_cache() throws Exception {
        mockMvc.perform(delete("/clear-cache"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cache cleared"));

        verify(cacheService).clear();
    }
}