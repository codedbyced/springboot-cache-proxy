package com.codedbyced.springboot_cache_proxy.cli;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

public class CachingProxyCommandTest {

    @Test
    void should_parse_port_and_origin() {
        CachingProxyCommand command = new CachingProxyCommand();
        CommandLine cmd = new CommandLine(command);

        int exitCode = cmd.execute("--port", "8080", "--origin", "http://localhost:9000");

        assertThat(exitCode).isEqualTo(0);
        assertThat(command.getPort()).isEqualTo(8080);
        assertThat(command.getOrigin()).isEqualTo("http://localhost:9000");
    }

    @Test
    void should_show_help_when_help_flag_is_passed() {
        CachingProxyCommand command = new CachingProxyCommand();
        CommandLine cmd = new CommandLine(command);

        int exitCode = cmd.execute("--help");

        assertThat(exitCode).isEqualTo(0);
        assertThat(command.isHelpRequested()).isTrue();
    }


    @Test
    void should_show_help_when_dash_h_flag_is_passed() {
        CachingProxyCommand command = new CachingProxyCommand();
        CommandLine cmd = new CommandLine(command);

        int exitCode = cmd.execute("-h");

        assertThat(exitCode).isEqualTo(0);
        assertThat(command.isHelpRequested()).isTrue();
    }

    @Test
    void should_throw_when_missing_origin() {
        CachingProxyCommand command = new CachingProxyCommand();
        CommandLine cmd = new CommandLine(command);

        cmd.parseArgs("--port", "8080");

        Throwable thrown = catchThrowable(command::run);

        assertThat(thrown)
                .isInstanceOf(CommandLine.ParameterException.class)
                .hasMessageContaining("Error: --port and --origin are required");
    }

    @Test
    void should_throw_when_missing_port() {
        CachingProxyCommand command = new CachingProxyCommand();
        CommandLine cmd = new CommandLine(command);

        cmd.parseArgs("--origin", "http://localhost:9000");

        Throwable thrown = catchThrowable(command::run);

        assertThat(thrown)
                .isInstanceOf(CommandLine.ParameterException.class)
                .hasMessageContaining("Error: --port and --origin are required");
    }
}