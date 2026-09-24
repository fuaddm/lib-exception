# Trace ID Logging

To make debugging across distributed microservices easier, `lib-exception` automatically includes a `traceId` field in the JSON `ErrorResponse`:

```json
{
  "status": 500,
  "code": "INTERNAL_SERVER_ERROR",
  "message": "An unexpected error occurred.",
  "traceId": "a9z3-b4k2-98xm"
}
```

## How It Works

The `GlobalExceptionHandler` reads this value directly from the **SLF4J MDC (Mapped Diagnostic Context)** using the key `traceId`:

```java
MDC.get("traceId");
```

For this to work, your consuming microservice needs to populate the MDC at the beginning of each HTTP request. 

## Setting Up a Trace Filter

To automatically generate and inject a trace ID for every incoming request, add a standard Servlet `Filter` to your Spring Boot application:

```java
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

@Component
public class TraceIdFilter implements Filter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String MDC_TRACE_ID_KEY = "traceId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
            
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 1. Try to get an existing trace ID from the incoming request (e.g., from an API Gateway)
        String traceId = httpRequest.getHeader(TRACE_ID_HEADER);
        
        // 2. If it doesn't exist, generate a new one
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }

        // 3. Put it in the MDC so the GlobalExceptionHandler (and your logs) can access it
        MDC.put(MDC_TRACE_ID_KEY, traceId);

        try {
            // 4. Continue processing the request
            chain.doFilter(request, response);
        } finally {
            // 5. Always clean up the MDC to prevent memory leaks in thread pools
            MDC.remove(MDC_TRACE_ID_KEY);
        }
    }
}
```

## Updating Your Logback Configuration

Once the `traceId` is in the MDC, you can also inject it into your standard application logs by updating your `logback-spring.xml` (or `application.yml` logging pattern) to include `%X{traceId}`:

```xml
<pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} [traceId=%X{traceId}] - %msg%n</pattern>
```

With this setup, the trace ID will perfectly correlate the `ErrorResponse` returned to the client, your server-side logs, and your Bugsink/Sentry error reports.
