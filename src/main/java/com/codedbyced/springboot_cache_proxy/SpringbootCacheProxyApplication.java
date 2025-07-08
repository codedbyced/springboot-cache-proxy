package com.codedbyced.springboot_cache_proxy;

import com.codedbyced.springboot_cache_proxy.cli.ProxyAppLauncher;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringbootCacheProxyApplication {

    public static void main(String[] args) {
        ProxyAppLauncher.run(args);
    }
}
