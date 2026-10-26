# Backend

Java 21, Maven and Spring Boot service modules.

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd -pl gateway-service -am spring-boot:run
.\mvnw.cmd -pl auth-service -am spring-boot:run
```

Each service is an independently executable Spring Boot application and exposes
`/actuator/health`. Ports and service names are configured in each service's
`src/main/resources/application.yml`.

## Modules

| Module | Responsibility |
|---|---|
| `gateway-service` | Public backend entry point; routing is intentionally not configured yet |
| `auth-service` | Accounts and registration; owns its Flyway migrations |
| `player-service` | Player bounded context |
| `monster-service` | Monster bounded context |
| `encounter-service` | Encounter bounded context |
| `simulator-service` | Simulator bounded context |
| `shared-contracts` | Framework-light contracts shared by services |

Service modules do not depend on another service's implementation module.
Inter-service HTTP communication, gateway routing, separate databases, TLS and
container configuration are intentionally deferred.

## Migrations

Flyway migrations remain service-owned under
`{service}/src/main/resources/db/migration/{service}/`.
