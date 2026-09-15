# Publishing (for maintainers of this library)

This is for whoever maintains `exception-lib` itself — not the microservices that
consume it (they just add the dependency, see [getting-started.md](./getting-started.md)).
Real endpoints referenced below are cataloged in [infrastructure.md](./infrastructure.md).

## Local development

```bash
./gradlew publishToMavenLocal
```

Fastest loop while iterating: puts the jar in your local `~/.m2`, so any service on
the same machine using `mavenLocal()` picks it up immediately. No credentials needed.

## Publishing to Nexus (shared by the team/CI)

`build.gradle` has a `Nexus` maven repository configured under
`publishing.repositories`, targeting `http://coolify.tail1270a7.ts.net:5782`. It
auto-selects Nexus's releases vs snapshots hosted repo based on the `version` in
`build.gradle` — anything ending in `-SNAPSHOT` goes to `maven-snapshots`,
everything else to `maven-releases`.

**On this specific instance, only `maven-releases` is confirmed to exist** (a
`GET /service/rest/v1/repositories` call with the `deployer` credential returned
exactly one repo). Don't publish a `-SNAPSHOT` version expecting it to work here
until you've confirmed `maven-snapshots` exists — see
[Bumping the version](#bumping-the-version) below. The current version,
`0.0.1`, is already published as a release.

`publish` pushes to every configured repository (`mavenLocal` + `Nexus`). To push
to Nexus only: `./gradlew publishMavenJavaPublicationToNexusRepository`.

This Nexus is reachable only over plain `http://` (Tailscale, no TLS
termination) — that's why the `Nexus` repo block in `build.gradle` has
`allowInsecureProtocol = true`; without it Gradle refuses to publish there at all.

### Credentials always come from Vault — just run `./gradlew publish`

There's no script and nothing to type by hand. The `deployer` credentials live
in Vault (KV v2, at `secret/nexus`, fields `base_url`/`username`/`password`),
read via AppRole auth. `publishMavenJavaPublicationToNexusRepository` (and
therefore `publish`) `dependsOn` a `fetchNexusCredentialsFromVault` task
(defined at the bottom of `build.gradle`) that forks
[`vault-tool`](../vault-tool) — a small, unpublished Spring Boot app using
Spring Cloud Vault (AppRole auth, KV v2) — as an isolated JVM via `JavaExec`,
captures the credentials it prints, and injects them into the `Nexus`
repository's `url`/`credentials` right before the actual publish step runs.
Nothing touches disk; the credentials only ever live in that one forked
process and in Gradle's in-memory task state for the rest of the build.

`vault-tool` is its own subproject (not published) rather than a dependency of
`exception-lib` itself — see [design-decisions.md](./design-decisions.md) for
why, and why the fetch runs as a forked `java` process instead of inside the
Gradle daemon.

You just need these already set in your environment before running
`./gradlew publish` (see [infrastructure.md](./infrastructure.md) — on the
primary dev machine these are already persistent Windows env vars):

| Variable | Example |
|---|---|
| `VAULT_URI` | `http://coolify.tail1270a7.ts.net:8200` |
| `VAULT_ROLE_ID` | *(AppRole role id — not secret on its own, but treat as a credential)* |
| `VAULT_SECRET_ID` | *(AppRole secret id — a real secret; rotate immediately if it's ever pasted somewhere it shouldn't be, e.g. a chat)* |

Missing one of these fails the build immediately with a one-line error
(`fetchNexusCredentialsFromVault`'s `doFirst`), before any network call is
made. If your releases/snapshots repos live at different hosts or
non-standard paths, `NEXUS_RELEASES_URL`/`NEXUS_SNAPSHOTS_URL` still work as
overrides — they take precedence over the `base_url` fetched from Vault.

## One-time Nexus setup (if you're standing up a new instance)

Nexus 3 Community Edition can run self-hosted, e.g. via Docker:
```bash
docker run -d -p 8081:8081 --name nexus sonatype/nexus3
```
Out of the box it ships a `maven-releases` and `maven-snapshots` hosted repo
(what `build.gradle`'s version-suffix logic assumes by default) plus a
`maven-public` group repo for consumers to resolve from. **The current
Tailscale/Coolify instance was not necessarily set up this way** — it only
exposes `maven-releases` to the `deployer` credential, so don't assume
`maven-snapshots`/`maven-public` exist there without checking first (`GET
/service/rest/v1/repositories`). First login on a fresh instance is at
`http://<host>:8081` (default admin password printed in
`/nexus-data/admin.password` inside the container on first start) — create a
user/token scoped to publish access instead of using the admin account for CI.

## Bumping the version

`version = '0.0.1'` in `build.gradle`, already published as an immutable
release. Nexus rejects re-publishing the same release version, so the next
change to the library needs `version = '0.0.2'` (etc.) before running the
publish script again. `-SNAPSHOT` versions are repeatable/overwritable in
general, but only use one here if you've first confirmed `maven-snapshots`
actually exists and `deployer` can reach it (see the note above — it currently
returns 403 on this instance).

## Consumers pointing at Nexus

A microservice's `build.gradle` resolves this library directly from
`maven-releases` (there's no confirmed `maven-public` group repo on this
instance to hide that detail behind):
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
```
See [getting-started.md](./getting-started.md) for the full consumer-side walkthrough.
