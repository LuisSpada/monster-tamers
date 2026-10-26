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

## Container images

Build from the `backend/` directory. Each Dockerfile builds only its service
and required Maven reactor dependencies, and does not publish a host port.

```powershell
docker build -f gateway-service/Dockerfile -t monster-tamers-gateway .
docker build -f auth-service/Dockerfile -t monster-tamers-auth .
docker build -f player-service/Dockerfile -t monster-tamers-player .
docker build -f monster-service/Dockerfile -t monster-tamers-monster .
docker build -f encounter-service/Dockerfile -t monster-tamers-encounter .
docker build -f simulator-service/Dockerfile -t monster-tamers-simulator .
```

The runtime port is configured with `SERVER_PORT`; defaults are 8080 for the
gateway, 8081 for auth, 8082 for player, 8083 for monster, 8084 for encounter,
and 8085 for simulator. Compose is responsible for host port publishing.

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
