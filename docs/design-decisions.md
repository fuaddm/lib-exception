# Design decisions & FAQ

## Why `compileOnly` for Spring/Jakarta dependencies?

Every service using this library is already a Spring Boot web app with its own
`spring-boot-starter-webmvc`/`-validation` version. Declaring those as
`compileOnly` here means this library compiles against their APIs but never forces
a specific Spring/Jakarta version onto a consumer at runtime — the consumer's own
version wins, avoiding version conflicts as services upgrade independently. The
one real runtime dependency is `io.sentry:sentry` (for [error reporting](./error-reporting.md)), which has no Spring coupling at all.

## Why a custom `ErrorResponse` instead of Spring's `ProblemDetail` (RFC 7807)?

Considered and deliberately not used, for now:
- The whole point of `code` (`PIN_NOT_FOUND`, `CONFLICT`, ...) is a stable,
  typed, switch-on-able field. `ProblemDetail`'s fixed fields are `type`, `title`,
  `status`, `detail`, `instance` — `code` (and `traceId`, and the validation
  `errors` list) would have to go through `setProperty(...)`, an untyped
  extension map with no schema guarantee.
- `ProblemDetail`'s `type` is meant to be a dereferenceable URI identifying the
  problem type — more machinery than an internal microservice mesh needs.
- Spring Boot 4 does ship native `ProblemDetail` support
  (`org.springframework.boot.webmvc.autoconfigure.ProblemDetailsExceptionHandler`,
  off by default), so there's no technical blocker to switching later if the team
  decides interoperability with generic HTTP tooling matters more than the typed
  fields. This was a deliberate choice, not a limitation — revisit if requirements change.

## Why auto-configuration instead of asking each service to `@Import` the handler?

A `@RestControllerAdvice` living in a dependency jar is not picked up by a
consumer's `@ComponentScan` (different base package). Instead, this library
registers itself via
`META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
— the standard Spring Boot "starter" mechanism — so adding the dependency alone is
enough. `@ConditionalOnMissingBean` lets a service override any of the beans
(the handler or the error reporter) simply by defining its own.

## Why does `GlobalExceptionHandler` run at `Ordered.LOWEST_PRECEDENCE`?

So any `@RestControllerAdvice` a service defines for itself automatically takes
priority for the exception types it chooses to handle, without needing to
explicitly out-rank or disable the shared one.

## Outbound error handling (not included yet)

This library currently only covers **inbound** handling — turning exceptions
thrown inside a service into the standard JSON body. It does not yet include a
Feign/RestTemplate/WebClient error decoder that would parse *another* service's
`ErrorResponse` JSON and rethrow a matching typed exception locally. This was an
explicit scope decision (see the getting-started conversation) to ship the
simpler, immediately useful half first. Worth adding once services start calling
each other directly and you want failures from Service A to surface as typed
exceptions inside Service B rather than generic HTTP client exceptions.

## Why is Spring Cloud Vault in its own `vault-tool` subproject, not `exception-lib` itself?

`build.gradle`'s `fetchNexusCredentialsFromVault` task fetches the Nexus
`deployer` credentials from Vault via [`vault-tool`](../vault-tool), a small
Spring Boot app that uses Spring Cloud Vault (AppRole auth) instead of
hand-rolled REST calls. That needs a real Spring `ApplicationContext` to do
its auth/property-source work — which `exception-lib` itself doesn't have
and must not gain: this library declares every Spring/Jakarta dependency
`compileOnly` specifically so it never forces a runtime Spring dependency
onto a consumer (see the first entry in this file). Bundling Spring Cloud
Vault into `exception-lib` would break that, and would also ship a
`main()`/publish-tooling class inside an artifact that's meant to be just
exception-handling code. So it's a separate, unpublished subproject instead
— see [publishing.md](./publishing.md#credentials-always-come-from-vault--just-run-gradlew-publish).

## Why does `fetchNexusCredentialsFromVault` fork `vault-tool` as a separate JVM instead of running it inside the Gradle daemon?

Two reasons. First, isolation: Spring Cloud Vault's dependency graph (Spring
Boot, Spring Web, Jackson, an HTTP client) would sit on the Gradle
buildscript classpath alongside Gradle's own bundled versions of some of the
same libraries — a classic source of hard-to-debug classpath conflicts.
Forking it via `JavaExec` (see `build.gradle`) gives it a clean classpath of
its own. Second, and more subtly: `vault-tool` used to fetch the credentials
*and* shell out to `./gradlew publish` itself as a child process. That's a
nested Gradle invocation against the same project directory — the outer
`./gradlew publish` holds the project/daemon lock while the child tries to
acquire it too, which is a known source of hangs/lock contention. The
current design avoids that entirely: `vault-tool` only fetches and prints
the credentials (`VAULT_TOOL_PROPERTY KEY=VALUE` lines on stdout); the
*same* Gradle build that forked it then injects them into the `Nexus`
repository's credentials and continues on to the actual `publish` task — one
Gradle invocation, one process fork, no nested `gradlew` call.

## Why generic error codes in the library instead of Pinterest-domain ones (Pin, Board, User)?

Domain exceptions (`PinNotFoundException`, `BoardLimitReachedException`, ...)
belong to the service that owns that domain, not to a shared library every service
depends on — otherwise every service would need to redeploy against a new library
version just to add one new error code for its own domain. The library provides
the *contract* (`ErrorCode` interface + the HTTP-status-mapped exception classes)
and a small set of truly generic codes (`CommonErrorCode`); each service defines
its own enum implementing `ErrorCode`. See
[throwing-exceptions.md](./throwing-exceptions.md#recommended-usage--your-own-errorcode-enum).
