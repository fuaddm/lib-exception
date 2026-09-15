# Getting started

`com.example:exception-lib:0.0.1` is already published and live on Nexus (see
[infrastructure.md](./infrastructure.md) for the actual URLs). You don't need to
publish anything yourself just to *use* the library in a microservice — only if
you're changing the library's own code (see [publishing.md](./publishing.md)).

## 1. Point the microservice's `build.gradle` at Nexus

This Nexus instance requires authentication even to *read* a package (it returns
401 without credentials), and it's plain `http://` (Tailscale-only, no TLS):

```groovy
repositories {
    maven {
        url = uri("http://coolify.tail1270a7.ts.net:5782/repository/maven-releases/")
        allowInsecureProtocol = true
        credentials {
            username = System.getenv("NEXUS_USERNAME")
            password = System.getenv("NEXUS_PASSWORD")
        }
    }
    mavenCentral()
}

dependencies {
    implementation 'com.example:exception-lib:0.0.1'
}
```

Get `NEXUS_USERNAME`/`NEXUS_PASSWORD` the same way the library itself is
published — from Vault (`secret/nexus`), not typed in by hand. If you're running
the build locally with `VAULT_URI`/`VAULT_ROLE_ID`/`VAULT_SECRET_ID` already set
(see [infrastructure.md](./infrastructure.md)), fetch them the same way
[`scripts/publish-nexus.ps1`](../scripts/publish-nexus.ps1) does, just ending the
script in `./gradlew build` (or whatever task you're running) instead of
`./gradlew publish`.

> Iterating on the library locally and don't want to touch Nexus at all yet?
> `./gradlew publishToMavenLocal` from the `exception-lib` repo installs it into
> `~/.m2` instead, and a consuming service can resolve it from `mavenLocal()`
> with no credentials — good for a fast local loop, not for anything shared with
> teammates or CI.

Requirements on the consuming service itself:
- Spring Boot 4.x with `spring-boot-starter-webmvc` (a regular REST controller service)
- Java 17+
- Nothing else — no `@Import`, no `@ComponentScan` changes needed.

## 2. Confirm it's wired up

Start the service and hit any endpoint that doesn't exist, or trigger an exception on
purpose:

```java
@GetMapping("/api/debug/boom")
public void boom() {
    throw new com.example.exceptionlib.exception.NotFoundException("just testing");
}
```

You should get back:

```json
{
  "timestamp": "2026-09-14T12:00:00Z",
  "status": 404,
  "error": "Not Found",
  "code": "RESOURCE_NOT_FOUND",
  "message": "just testing",
  "path": "/api/debug/boom"
}
```

If instead you get Spring Boot's default whitelabel error page or a plain stack
trace, the auto-configuration didn't activate — check:
- The dependency actually resolved (`./gradlew dependencies --configuration runtimeClasspath | grep exception-lib`) — a 401/403 during resolution means the Nexus credentials above aren't reaching the build.
- You're using `spring-boot-starter-webmvc` (the auto-config is conditional on `DispatcherServlet` being on the classpath)
- No `@ConditionalOnMissingBean` collision — if your service already defines its own bean
  named `globalExceptionHandler` of type `GlobalExceptionHandler`, ours backs off silently.

Next: [Throwing exceptions](./throwing-exceptions.md).
