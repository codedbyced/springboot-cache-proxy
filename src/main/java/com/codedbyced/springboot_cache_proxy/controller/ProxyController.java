package com.codedbyced.springboot_cache_proxy.controller;

import com.codedbyced.springboot_cache_proxy.record.CachedResponse;
import com.codedbyced.springboot_cache_proxy.service.CacheService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
public class ProxyController {

    private final RestTemplate restTemplate;
    private final CacheService cacheService;

    @Value("${origin.url:}")
    private String origin;

    @RequestMapping("/**")
    public ResponseEntity<?> proxy(HttpServletRequest request) {
        String fullPath = request.getRequestURI() +
                (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        // 1. Try the cache
        CachedResponse cached = cacheService.get(fullPath);
        if (cached != null) {
            HttpHeaders headers = new HttpHeaders(cached.headers());
            headers.set("X-Cache", "HIT");
            return new ResponseEntity<>(cached.body(), headers, HttpStatus.OK);
        }

        // 2. Forward to origin
        String forwardUrl = origin + fullPath;
        try {
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    forwardUrl,
                    HttpMethod.GET,
                    null,
                    byte[].class
            );

            HttpHeaders originHeaders = new HttpHeaders(response.getHeaders());

            // 3. Cache response
            cacheService.put(fullPath, response.getBody(), originHeaders);

            // 4. Return response with X-Cache: MISS
            HttpHeaders headers = new HttpHeaders(originHeaders);
            headers.set("X-Cache", "MISS");

            return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());

        } catch (org.springframework.web.client.HttpStatusCodeException ex) {
            // Origin returned 4xx or 5xx
            return ResponseEntity
                    .status(ex.getStatusCode())
                    .header("X-Cache", "ERROR")
                    .body(ex.getResponseBodyAsString());

        } catch (org.springframework.web.client.ResourceAccessException ex) {
            // Origin server unreachable (DNS error, timeout, etc.)
            return ResponseEntity
                    .status(HttpStatus.GATEWAY_TIMEOUT)
                    .header("X-Cache", "ERROR")
                    .body("Origin server not reachable: " + ex.getMessage());

        } catch (Exception ex) {
            // Unexpected errors
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("X-Cache", "ERROR")
                    .body("Unexpected error: " + ex.getMessage());
        }
    }

    @GetMapping("/stats")
    public String stats() {
        return cacheService.stats().toString();
    }

    @DeleteMapping("/clear-cache")
    public ResponseEntity<String> clearCache() {
        cacheService.clear();
        return ResponseEntity.ok("Cache cleared");
    }
}