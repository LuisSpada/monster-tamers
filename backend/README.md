# Backend

The backend uses Java 21, Maven and Spring Boot service modules.

## Commands

Run the complete backend verification:

```powershell
.\mvnw.cmd clean verify
```

Run Gateway Service locally:

```powershell
.\mvnw.cmd -pl gateway-service -am spring-boot:run
```

Run Auth Service locally:

```powershell
.\mvnw.cmd -pl auth-service -am spring-boot:run "-Dspring-boot.run.profiles=local"
```

Each service is independently executable and exposes `/actuator/health`.

## Service ports

| Service | Port |
|---|---:|
| Gateway Service | 8080 |
| Auth Service | 8081 |
| Player Service | 8082 |
| Monster Service | 8083 |
| Encounter Service | 8084 |
| Simulator Service | 8085 |

Only Gateway port `8080` is published to the host by the default Compose stack.

## Container images

Build from the `backend/` directory:

```powershell
docker build -f gateway-service/Dockerfile -t monster-tamers-gateway .
docker build -f auth-service/Dockerfile -t monster-tamers-auth .
docker build -f player-service/Dockerfile -t monster-tamers-player .
docker build -f monster-service/Dockerfile -t monster-tamers-monster .
docker build -f encounter-service/Dockerfile -t monster-tamers-encounter .
docker build -f simulator-service/Dockerfile -t monster-tamers-simulator .
```

## Local Compose stack

From the repository root:

```powershell
docker compose -f backend/compose.yaml up --build -d
```

Inspect service status:

```powershell
docker compose -f backend/compose.yaml ps
```

Check Gateway health:

```powershell
Invoke-RestMethod http://localhost:8080/actuator/health
```

Check health from inside the containers:

```powershell
docker compose -f backend/compose.yaml exec gateway-service wget --spider --no-verbose http://127.0.0.1:8080/actuator/health
docker compose -f backend/compose.yaml exec auth-service wget --spider --no-verbose http://127.0.0.1:8081/actuator/health
```

View logs:

```powershell
docker compose -f backend/compose.yaml logs -f
docker compose -f backend/compose.yaml logs -f auth-service
```

Stop the stack while preserving database volumes:

```powershell
docker compose -f backend/compose.yaml down
```

Remove containers and database volumes:

```powershell
docker compose -f backend/compose.yaml down -v
```

## Gateway routes

The Gateway is available at `http://localhost:8080`.

| Gateway route | Internal target |
|---|---|
| `/api/auth/**` | `auth-service:8081` |
| `/api/players/**` | `player-service:8082` |
| `/api/monsters/**` | `monster-service:8083` |
| `/api/encounters/**` | `encounter-service:8084` |

Simulator Service has no public Gateway route.

## Confirmation email delivery

Auth Service supports two confirmation-email delivery modes:

- `logging`
- `smtp`

The mode is selected with:

```dotenv
MAIL_MODE=logging
```

### Logging mode

Logging mode is the default for local development:

```dotenv
MAIL_MODE=logging
```

The recipient, confirmation code and registration ID are written to the Auth Service logs.

Inspect the generated code with:

```powershell
docker compose -f backend/compose.yaml logs -f auth-service
```

Logging mode exposes confirmation codes in logs. Do not use logging mode in production.

### SMTP mode

Enable provider-neutral SMTP delivery with:

```dotenv
MAIL_MODE=smtp
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=smtp-user
MAIL_PASSWORD=smtp-password
MAIL_FROM=no-reply@example.com
MAIL_SMTP_AUTH=true
MAIL_STARTTLS=true
MAIL_CONNECTION_TIMEOUT_MS=5000
MAIL_READ_TIMEOUT_MS=5000
MAIL_WRITE_TIMEOUT_MS=5000
```

The SMTP implementation can be configured for providers such as Mailtrap, Brevo, SendGrid SMTP and Amazon SES SMTP.

Auth Service fails during startup when SMTP mode is enabled and required configuration is missing or invalid.

Required SMTP configuration:

| Variable | Requirement |
|---|---|
| `MAIL_HOST` | Required in SMTP mode |
| `MAIL_PORT` | Valid port from 1 to 65535; defaults to 587 |
| `MAIL_FROM` | Required in SMTP mode |
| `MAIL_USERNAME` | Required when SMTP authentication is enabled |
| `MAIL_PASSWORD` | Required when SMTP authentication is enabled |

Optional transport settings:

| Variable | Default |
|---|---:|
| `MAIL_SMTP_AUTH` | `true` |
| `MAIL_STARTTLS` | `true` |
| `MAIL_CONNECTION_TIMEOUT_MS` | `5000` |
| `MAIL_READ_TIMEOUT_MS` | `5000` |
| `MAIL_WRITE_TIMEOUT_MS` | `5000` |

All timeout values use milliseconds and must be greater than zero.

When SMTP authentication is disabled, empty credentials are allowed:

```dotenv
MAIL_MODE=smtp
MAIL_HOST=localhost
MAIL_PORT=1025
MAIL_FROM=no-reply@localhost
MAIL_SMTP_AUTH=false
MAIL_STARTTLS=false
```

This configuration is suitable for a local SMTP test server such as Mailpit.

### SMTP credentials

Never commit real SMTP credentials to Git.

Copy the example file:

```powershell
Copy-Item backend/.env.example backend/.env
```

Then edit `backend/.env` locally.

Ensure `.env` remains ignored by Git:

```powershell
git status --short
```

For production, inject SMTP credentials through the deployment platform's secret storage instead of keeping credentials in source-controlled files.

### Delivery failure behavior

Confirmation-email delivery occurs after the registration transaction commits.

If SMTP delivery fails:

- The pending registration remains stored.
- The registration transaction is not rolled back.
- The delivery failure is logged.
- The confirmation code is not included in the SMTP failure log.
- Auth Service does not fall back to logging mode.
- No automatic retry is performed.

Retries and resend-code endpoints should be implemented separately.

## Databases

PostgreSQL is private to the backend network.

Auth, Player, Monster and Encounter Services own separate databases and named volumes:

| Service | Database | Volume |
|---|---|---|
| Auth Service | `monster_tamers_auth` | `auth-postgres-data` |
| Player Service | `monster_tamers_player` | `player-postgres-data` |
| Monster Service | `monster_tamers_monster` | `monster-postgres-data` |
| Encounter Service | `monster_tamers_encounter` | `encounter-postgres-data` |

Services must not read another service's database tables.

The default passwords are intended for local development only and must be replaced outside local development.

## Modules

| Module | Responsibility |
|---|---|
| `gateway-service` | Public backend entry point and service routing |
| `auth-service` | Accounts, login, registration and confirmation email |
| `player-service` | Player bounded context |
| `monster-service` | Monster bounded context |
| `encounter-service` | Encounter bounded context |
| `simulator-service` | Battle simulation |
| `shared-contracts` | Framework-light shared contracts |
| `shared-web` | Shared Spring MVC infrastructure |

Service modules do not depend on another service's implementation module.

## Migrations

Flyway migrations are service-owned:

```text
{service}/src/main/resources/db/migration/{service}/
```

## Validation

Run focused Auth Service tests:

```powershell
cd backend
.\mvnw.cmd -pl auth-service -am clean test
```

Run the complete backend verification:

```powershell
.\mvnw.cmd clean verify
```

Validate the Compose configuration:

```powershell
docker compose -f compose.yaml config
```

Build the Auth Service image:

```powershell
docker compose -f compose.yaml build auth-service
```