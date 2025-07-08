package com.codedbyced.springboot_cache_proxy.cli;

import com.codedbyced.springboot_cache_proxy.SpringbootCacheProxyApplication;
import org.springframework.boot.SpringApplication;
import picocli.CommandLine;

public class ProxyAppLauncher {

    public static int run(String[] args) {
        CachingProxyCommand command = new CachingProxyCommand();
        CommandLine cmd = new CommandLine(command);

        try {
            cmd.parseArgs(args);

            if (command.isHelpRequested()) {
                cmd.usage(System.out);
                return 0;
            }

            command.run();

            System.setProperty("server.port", String.valueOf(command.getPort()));
            System.setProperty("origin.url", command.getOrigin());

            SpringApplication.run(SpringbootCacheProxyApplication.class, args);
            return 0;
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return 1;
        }
    }
}
