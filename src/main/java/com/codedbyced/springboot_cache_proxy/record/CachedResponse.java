package com.codedbyced.springboot_cache_proxy.record;

import org.springframework.http.HttpHeaders;

public record CachedResponse(byte[] body, HttpHeaders headers) {}