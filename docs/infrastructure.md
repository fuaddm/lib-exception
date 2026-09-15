# Infrastructure reference

The actual, verified endpoints this project's tooling talks to — so the other
docs can say "see infrastructure.md" instead of repeating (and drifting from)
placeholder hostnames. Everything here is a hostname/path, never a credential —
actual secrets (passwords, tokens, DSN keys) live only in Vault, never in this repo.

| System | URL | Notes |
|---|---|---|
| **Nexus** | `http://coolify.tail1270a7.ts.net:5782` | Self-hosted via Coolify, reachable only over Tailscale. Plain `http://`, no TLS — that's why `build.gradle` and any consumer's repo block need `allowInsecureProtocol = true`. |
| Nexus releases repo | `http://coolify.tail1270a7.ts.net:5782/repository/maven-releases/` | Confirmed working (published `com.example:exception-lib:0.0.1` here). |
| Nexus snapshots repo | *(not confirmed to exist)* | `GET /service/rest/v1/repositories` on this instance only returned `maven-releases` for the `deployer` credential — see [publishing.md](./publishing.md#bumping-the-version). Don't assume `maven-snapshots` or a `maven-public` group repo exist here until you've checked. |
| **Vault** | `http://coolify.tail1270a7.ts.net:8200` | Set as the persistent Windows env var `VAULT_URI` on the primary dev machine. AppRole auth (`VAULT_ROLE_ID` + `VAULT_SECRET_ID`, also persistent env vars there). |
| Vault secret path | `secret/nexus` (KV v2) | Fields: `base_url`, `username`, `password` — the Nexus `deployer` credentials. Read via `GET /v1/secret/data/nexus` with a token from AppRole login. |
| **Bugsink** | `http://coolify.tail1270a7.ts.net:8001` | Self-hosted, Sentry-ingestion-protocol compatible. DSN format: `http://<key>@coolify.tail1270a7.ts.net:8001/<project-id>` — project `1` is the one used so far. The `<key>` is a write-only ingest credential; treat it like any other secret (see [error-reporting.md](./error-reporting.md)). |

## How these fit together

```
Vault (secret/nexus)  ──AppRole login──>  scripts/publish-nexus.{ps1,sh}  ──env vars──>  ./gradlew publish  ──>  Nexus (maven-releases)
```
See [publishing.md](./publishing.md) for the full flow and [design-decisions.md](./design-decisions.md) for why Vault isn't wired into `build.gradle` directly.

## Everything here is Tailscale-only

All three hosts (`coolify.tail1270a7.ts.net`) are only reachable from machines on
that Tailscale network. If a build/publish/test fails with a connection timeout
(not a 401/403), check Tailscale connectivity first before assuming a
credentials or config problem.
