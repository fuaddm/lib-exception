# exception-lib docs

Shared exception-handling library for the Pinterest-clone microservices. One dependency
gives every service the same exception hierarchy, the same JSON error shape, and
(optionally) the same error reporting to Bugsink/Sentry — without copy-pasting a
`@RestControllerAdvice` into each service.

Read in this order if you're integrating it for the first time:

1. [Getting started](./getting-started.md) — publish the jar, add the dependency, confirm it's wired up.
2. [Throwing exceptions](./throwing-exceptions.md) — the exception hierarchy, and how to add your service's own error codes.
3. [Error response format](./error-response-format.md) — the exact JSON shape every service now returns.
4. [Error reporting (Bugsink/Sentry)](./error-reporting.md) — forward server errors to your self-hosted Bugsink instance.
5. [Design decisions & FAQ](./design-decisions.md) — why things are built the way they are, for when you're deciding whether to extend or override something.

Maintaining this library itself (not just consuming it)? See [publishing.md](./publishing.md) for pushing releases/snapshots to Nexus.

Need the actual URLs (Nexus, Vault, Bugsink) instead of placeholders? See [infrastructure.md](./infrastructure.md) — the single source of truth for this project's real endpoints, everything else links to it.

## At a glance

```
com.example.exceptionlib
├── error/            ErrorCode (interface), CommonErrorCode (generic fallback)
├── exception/        BaseException + NotFoundException, ConflictException, BadRequestException,
│                      UnauthorizedException, ForbiddenException, TooManyRequestsException, ValidationException
├── dto/               ErrorResponse, FieldErrorDetail  (the JSON body)
├── handler/           GlobalExceptionHandler            (@RestControllerAdvice, auto-registered)
├── reporting/         ErrorReporter, SentryProtocolErrorReporter, NoopErrorReporter
└── autoconfigure/     ExceptionLibAutoConfiguration, ExceptionLibProperties
```

Group/artifact: `com.example:exception-lib`. Requires Java 17+ and a Spring Boot 4
web (`spring-boot-starter-webmvc`) service — this library is `compileOnly` against
Spring, so it rides on whatever Boot version your service already uses.
