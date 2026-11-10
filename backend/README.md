# Backend

Java 21, Maven and Spring Boot service modules.

```powershell
.\mvnw.cmd clean verify
.\mvnw.cmd -pl gateway-service -am spring-boot:run
.\mvnw.cmd -pl auth-service -am spring-boot:run "-Dspring-boot.run.profiles=local"
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

## Local Compose stack

The backend stack is defined in `compose.yaml` and is started from the
repository root with:

```powershell
docker compose -f backend/compose.yaml up --build -d
```

Inspect service status and health:

```powershell
docker compose -f backend/compose.yaml ps
Invoke-RestMethod http://localhost:8080/actuator/health
docker compose -f backend/compose.yaml exec gateway-service wget --spider --no-verbose http://localhost:8080/actuator/health
docker compose -f backend/compose.yaml exec auth-service wget --spider --no-verbose http://localhost:8081/actuator/health
```

View logs:

```powershell
docker compose -f backend/compose.yaml logs -f
docker compose -f backend/compose.yaml logs -f auth-service
```

Stop the stack while preserving the database volume:

```powershell
docker compose -f backend/compose.yaml down
```

The gateway is available at `http://localhost:8080`. Public gateway routes are:

| Gateway route | Internal target |
|---|---|
| `/api/auth/**` | `auth-service:8081` |
| `/api/players/**` | `player-service:8082` |
| `/api/monsters/**` | `monster-service:8083` |
| `/api/encounters/**` | `encounter-service:8084` |

All routes preserve their incoming paths. Internal service hostnames and ports are `gateway-service:8080`, `auth-service:8081`,
`player-service:8082`, `monster-service:8083`, `encounter-service:8084`, and
`simulator-service:8085`; only the gateway port is published to the host.

PostgreSQL is private to the backend network and currently owns the
`monster_tamers_auth` database for `auth-service`. The named
`auth-postgres-data` volume preserves development data. Other services do not
receive datasource or Flyway settings because they do not currently require a
database. Future service databases should use separate credentials and
database ownership rather than sharing auth tables.

Simulator Service has no public gateway route. Current limitations: JWT,
authentication enforcement, TLS, retries, circuit breakers, frontend
integration, and separate service databases are not configured.
The default PostgreSQL password is for local development only and must be
overridden through environment variables outside local development.
Auth Service has a default `JWT_SECRET` only in the `local` profile; without
that profile it does not start unless `JWT_SECRET` is set.

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
| `shared-web` | Spring MVC infrastructure shared by services |

Service modules do not depend on another service's implementation module.
Inter-service HTTP communication, gateway routing, separate databases, TLS and
container configuration are intentionally deferred.

## Migrations

Flyway migrations remain service-owned under
`{service}/src/main/resources/db/migration/{service}/`.
