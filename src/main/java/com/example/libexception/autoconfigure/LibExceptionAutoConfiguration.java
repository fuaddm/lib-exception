package com.example.libexception.autoconfigure;

import com.example.libexception.handler.GlobalExceptionHandler;
import com.example.libexception.reporting.ErrorReporter;
import com.example.libexception.reporting.NoopErrorReporter;
import com.example.libexception.reporting.SentryProtocolErrorReporter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.autoconfigure.WebMvcAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.DispatcherServlet;

/**
 * Auto-registers {@link GlobalExceptionHandler} (and its {@link ErrorReporter}) into
 * any Spring MVC microservice that has this library on its classpath - no manual
 * {@code @ComponentScan} or {@code @Import} needed on the consumer's side.
 * <p>
 * Picked up automatically by Spring Boot via
 * {@code META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports}.
 */
@AutoConfiguration(after = WebMvcAutoConfiguration.class)
@ConditionalOnClass(DispatcherServlet.class)
@EnableConfigurationProperties(LibExceptionProperties.class)
public class LibExceptionAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ErrorReporter errorReporter(LibExceptionProperties properties) {
        LibExceptionProperties.ErrorReporting config = properties.getErrorReporting();
        if (config.isEnabled() && StringUtils.hasText(config.getDsn())) {
            return new SentryProtocolErrorReporter(config);
        }
        return new NoopErrorReporter();
    }

    @Bean
    @ConditionalOnMissingBean
    public GlobalExceptionHandler globalExceptionHandler(ErrorReporter errorReporter, LibExceptionProperties properties) {
        return new GlobalExceptionHandler(errorReporter, properties.getErrorReporting().getMinimumStatus());
    }
}
