package com.codedbyced.springboot_cache_proxy.cli;

import com.codedbyced.springboot_cache_proxy.SpringbootCacheProxyApplication;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mockStatic;

class ProxyAppLauncherTest {

    @AfterEach
    void clearSystemProperties() {
        System.clearProperty("server.port");
        System.clearProperty("origin.url");
    }

    @Test
    void should_return_1_when_args_are_missing() {
        int code = ProxyAppLauncher.run(new String[]{"--port", "8080"});
        assertThat(code).isEqualTo(1);
    }

    @Test
    void should_set_properties_and_run_springboot_when_args_are_valid() {
        String[] args = {"--port", "8080", "--origin", "http://localhost"};

        try (MockedStatic<SpringApplication> springMock = mockStatic(SpringApplication.class)) {
            springMock.when(() -> SpringApplication.run(SpringbootCacheProxyApplication.class, args))
                    .thenReturn(null);

            int code = ProxyAppLauncher.run(args);

            assertThat(code).isEqualTo(0);
            assertThat(System.getProperty("server.port")).isEqualTo("8080");
            assertThat(System.getProperty("origin.url")).isEqualTo("http://localhost");

            springMock.verify(() -> SpringApplication.run(SpringbootCacheProxyApplication.class, args));
        }
    }

    @Test
    void should_return_0_for_help_argument() {
        int code = ProxyAppLauncher.run(new String[]{"--help"});
        assertThat(code).isEqualTo(0);
    }
}