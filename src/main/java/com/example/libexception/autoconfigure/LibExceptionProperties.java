package com.example.libexception.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Configuration for exception-lib, bound from each consuming service's own
 * {@code application.yml} - e.g.:
 *
 * <pre>{@code
 * exception-lib:
 *   error-reporting:
 *     dsn: http://<key>@your-bugsink-host:8001/1
 *     environment: production
 * }</pre>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "lib-exception")
public class LibExceptionProperties {

    @NestedConfigurationProperty
    private final ErrorReporting errorReporting = new ErrorReporting();

    @Getter
    @Setter
    public static class ErrorReporting {

        /** Master switch. Even with a DSN set, reporting can be turned off (e.g. for local dev). */
        private boolean enabled = true;

        /**
         * DSN of a server speaking the Sentry event-ingestion protocol - Sentry itself,
         * self-hosted Bugsink, GlitchTip, etc. Leave unset to disable reporting entirely
         * (the default; a {@code NoopErrorReporter} is used instead).
         */
        private String dsn;

        /** Reported as the "environment" tag, e.g. "production", "staging". */
        private String environment;

        /** Only exceptions resulting in this HTTP status or higher get forwarded. Default: 500 (server errors only). */
        private int minimumStatus = 500;
    }
}
