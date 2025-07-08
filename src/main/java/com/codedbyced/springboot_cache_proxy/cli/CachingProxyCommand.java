package com.codedbyced.springboot_cache_proxy.cli;

import lombok.Getter;
import picocli.CommandLine;

@CommandLine.Command(
        name = "caching-proxy",
        mixinStandardHelpOptions = true,
        description = "Starts a caching proxy server.",
        version = "1.0"
)
public class CachingProxyCommand implements Runnable {

    @Getter
    @CommandLine.Option(names = "--port", description = "Proxy server port")
    private Integer port;

    @Getter
    @CommandLine.Option(names = "--origin", description = "Origin server base URL")
    private String origin;

    @Getter
    @CommandLine.Option(names = {"-h", "--help"}, usageHelp = true, description = "Show help and exit")
    private boolean helpRequested;

    @Override
    public void run() {
        if (!helpRequested) {

            if (port == null || origin == null) {
                System.err.println("Error: --port and --origin are required unless --help is specified.");
                CommandLine.usage(this, System.err);

                throw new CommandLine.ParameterException(new CommandLine(this),
                        "Error: --port and --origin are required unless --help is specified.");
            }

            System.out.printf("Starting proxy on port %d forwarding to %s%n", port, origin);
        }
    }
}