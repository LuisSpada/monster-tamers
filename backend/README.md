# Backend

The backend uses Java 21, Maven, Spring Boot and Docker Compose. It consists of independently deployable microservices that communicate over HTTPS.

## Prerequisites

Local development requires:

- Java 21
- Docker Desktop
- PowerShell
- OpenSSL
- Java `keytool`
- Git

Check the required certificate tools:

```powershell
openssl version
keytool -version
```

On Windows, OpenSSL may be available through Git for Windows:

```powershell
$env:Path = "C:\Program Files\Git\usr\bin;$env:Path"

Remove-Item Env:OPENSSL_CONF -ErrorAction SilentlyContinue
Remove-Item Env:SSLEAY_CONF -ErrorAction SilentlyContinue

openssl version
```

## Commands

Run the complete backend verification:

```powershell
cd backend
.\mvnw.cmd clean verify
```

Run a service directly without Docker:

```powershell
cd backend
.\mvnw.cmd -pl gateway-service -am spring-boot:run
```

The default Maven and test configuration keeps server TLS disabled unless `SERVER_SSL_ENABLED` is explicitly enabled.

The Docker images and default Compose stack enable TLS and require valid local certificate material.

Each service exposes:

```text
/actuator/health
```

## Service ports

| Service | Port | Docker protocol |
|---|---:|---|
| Gateway Service | 8080 | HTTPS |
| Auth Service | 8081 | HTTPS |
| Player Service | 8082 | HTTPS |
| Monster Service | 8083 | HTTPS |
| Encounter Service | 8084 | HTTPS |
| Simulator Service | 8085 | HTTPS |

Only Gateway port `8080` is published to the host by the default Compose stack.

Internal service ports are available only on the private Docker network.

## Local TLS

The local Compose stack requires a private development certificate authority and one certificate for each application service.

The generation script creates:

- one local development CA
- one PKCS#12 server keystore per service
- one shared PKCS#12 truststore
- one local TLS password file

Generated material is written to:

```text
infrastructure/runtime/tls/
```

This directory is ignored by Git. Private keys, keystores, truststores and password files must never be committed.

### Generate local certificates

From the repository root:

```powershell
$env:Path = "C:\Program Files\Git\usr\bin;$env:Path"

Remove-Item Env:OPENSSL_CONF -ErrorAction SilentlyContinue
Remove-Item Env:SSLEAY_CONF -ErrorAction SilentlyContinue

.\infrastructure\tls\generate-local-certificates.ps1
```

Use a password with:

- at least 12 characters
- printable ASCII characters only
- no spaces
- no non-ASCII letters or symbols

To regenerate existing certificate material:

```powershell
.\infrastructure\tls\generate-local-certificates.ps1 -Force
```

Regeneration creates a new local CA. The new public CA certificate must therefore be installed again.

### Trust the local CA on Windows

Install only the public CA certificate:

```powershell
Import-Certificate `
  -FilePath infrastructure\runtime\tls\ca.crt `
  -CertStoreLocation Cert:\CurrentUser\Root
```

Never install, distribute or commit:

```text
infrastructure/runtime/tls/ca.key
```

Confirm that the CA is trusted:

```powershell
Get-ChildItem Cert:\CurrentUser\Root |
  Where-Object {
    $_.Subject -eq "CN=Monster Tamers Local Development CA"
  } |
  Select-Object Subject, Thumbprint, NotAfter
```

If certificates are regenerated, remove the old development CA before importing the new one:

```powershell
Get-ChildItem Cert:\CurrentUser\Root |
  Where-Object {
    $_.Subject -eq "CN=Monster Tamers Local Development CA"
  } |
  Remove-Item
```

### Verify generated material

Confirm that generated secrets are ignored:

```powershell
git check-ignore -v `
  infrastructure/runtime/tls/ca.key `
  infrastructure/runtime/tls/truststore.p12 `
  infrastructure/runtime/tls/tls-password.txt `
  infrastructure/runtime/tls/gateway-service/server.p12
```

Inspect a certificate:

```powershell
openssl x509 `
  -in infrastructure\runtime\tls\gateway-service\server.crt `
  -noout `
  -subject `
  -issuer `
  -dates `
  -ext subjectAltName
```

The Gateway certificate should include:

```text
DNS:gateway-service
DNS:localhost
IP Address:127.0.0.1
```

Each internal service certificate contains its Docker Compose DNS name, `localhost` and `127.0.0.1`.

Verify a service keystore:

```powershell
$tlsPassword = Get-Content `
  infrastructure\runtime\tls\tls-password.txt `
  -Raw

keytool -list `
  -keystore infrastructure\runtime\tls\gateway-service\server.p12 `
  -storetype PKCS12 `
  -storepass $tlsPassword

Remove-Variable tlsPassword
```

## Backend environment configuration

Copy the backend environment example:

```powershell
Copy-Item backend/.env.example backend/.env
```

Edit `backend/.env` locally.

The file can contain local database, JWT and SMTP configuration. It must not be committed.

Confirm that it is ignored:

```powershell
git check-ignore -v backend/.env
git status --short
```

Avoid running the following command with real credentials when sharing terminal output:

```powershell
docker compose --env-file backend/.env -f backend/compose.yaml config
```

The fully rendered Compose configuration can display interpolated secrets.

Use this safer command when only the service inventory is needed:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  config --services
```

## Container images

Build individual images from the `backend/` directory:

```powershell
docker build -f gateway-service/Dockerfile -t monster-tamers-gateway .
docker build -f auth-service/Dockerfile -t monster-tamers-auth .
docker build -f player-service/Dockerfile -t monster-tamers-player .
docker build -f monster-service/Dockerfile -t monster-tamers-monster .
docker build -f encounter-service/Dockerfile -t monster-tamers-encounter .
docker build -f simulator-service/Dockerfile -t monster-tamers-simulator .
```

The runtime images:

- run as the non-root `app` user
- enable HTTPS
- read the TLS password from a Docker secret
- mount certificate material at runtime
- validate HTTPS health checks using the local CA

Certificate material is not copied into the images.

## Local Compose stack

Generate the TLS material before starting the stack.

From the repository root:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  up --build -d
```

Inspect service status:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  ps
```

Expected application services:

```text
gateway-service
auth-service
player-service
monster-service
encounter-service
simulator-service
```

All application services should become `healthy`.

The PostgreSQL containers should also become `healthy`.

### Check Gateway health

```powershell
Invoke-RestMethod https://localhost:8080/actuator/health
```

Expected status:

```text
UP
```

Plain HTTP should not work on the Gateway application port:

```powershell
try {
    Invoke-WebRequest http://localhost:8080/actuator/health
} catch {
    $_.Exception.Message
}
```

The HTTP request should fail because port `8080` expects TLS.

### Check internal HTTPS health

Gateway to Auth Service:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  exec gateway-service `
  wget `
  --ca-certificate=/run/tls/ca.crt `
  --spider `
  --no-verbose `
  https://auth-service:8081/actuator/health
```

Auth Service to Player Service:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  exec auth-service `
  wget `
  --ca-certificate=/run/tls/ca.crt `
  --spider `
  --no-verbose `
  https://player-service:8082/actuator/health
```

Player Service to Monster Service:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  exec player-service `
  wget `
  --ca-certificate=/run/tls/ca.crt `
  --spider `
  --no-verbose `
  https://monster-service:8083/actuator/health
```

Each successful command reports an HTTP `200` response.

### Check Gateway routes

```powershell
Invoke-RestMethod https://localhost:8080/docs/auth/v3/api-docs
Invoke-RestMethod https://localhost:8080/docs/players/v3/api-docs
Invoke-RestMethod https://localhost:8080/docs/monsters/v3/api-docs
Invoke-RestMethod https://localhost:8080/docs/encounters/v3/api-docs
```

Successful responses confirm that Gateway trusts the backend service certificates and can route to the internal services over HTTPS.

### View logs

View all logs:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  logs -f
```

View logs for one service:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  logs -f auth-service
```

View recent logs without following:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  logs simulator-service --tail 200
```

### Stop the stack

Stop the stack while preserving database volumes:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  down
```

Remove containers and database volumes:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  down -v
```

## Gateway routes

The public Gateway is available at:

```text
https://localhost:8080
```

| Gateway route | Internal HTTPS target |
|---|---|
| `/api/auth/**` | `https://auth-service:8081` |
| `/api/players/**` | `https://player-service:8082` |
| `/api/monsters/**` | `https://monster-service:8083` |
| `/api/encounters/**` | `https://encounter-service:8084` |

Simulator Service has no public Gateway route.

Internal endpoints under `/internal/**` are not routed publicly through Gateway.

## Service-to-service TLS

The current internal HTTPS connections include:

```text
Gateway → Auth Service
Gateway → Player Service
Gateway → Monster Service
Gateway → Encounter Service
Auth Service → Player Service
Player Service → Monster Service
```

Future Encounter-to-Simulator communication must use:

```text
https://simulator-service:8085
```

Internal clients:

- validate the local or deployment CA
- keep hostname verification enabled
- do not use trust-all certificate managers
- do not disable certificate validation

TLS secures transport and server identity. Authorization for internal endpoints remains a separate concern.

## Frontend connection

The local frontend development server can run on:

```text
http://localhost:5173
```

The frontend calls the backend through the HTTPS Gateway:

```dotenv
VITE_API_MODE=http
VITE_GATEWAY_BASE_URL=https://localhost:8080
VITE_API_BASE_URL=https://localhost:8080/api
```

These local overrides belong in:

```text
frontend/.env.local
```

The browser must trust the local development CA before HTTPS requests to Gateway will succeed.

The HTTP Vite origin is intentionally allowed by Gateway CORS configuration. The frontend-to-Gateway connection still uses HTTPS.

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
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  logs -f auth-service
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

Ensure `.env` remains ignored:

```powershell
git check-ignore -v backend/.env
git status --short
```

For production, inject SMTP credentials through the deployment platform's secret storage instead of keeping credentials in source-controlled files.

If a credential appears in terminal output, chat, a pull request or source control, revoke it and generate a new credential immediately.

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

The current local TLS work secures HTTP communication between application services. PostgreSQL TLS requires separate database-specific certificate and client configuration.

## Modules

| Module | Responsibility |
|---|---|
| `gateway-service` | Public HTTPS entry point and service routing |
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

Run complete backend verification:

```powershell
cd backend
.\mvnw.cmd clean verify
```

Validate the Compose service inventory without printing interpolated configuration:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  config --services
```

Build the Auth Service image:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  build auth-service
```

Start and validate the complete HTTPS stack:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  up --build -d

docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  ps

Invoke-RestMethod https://localhost:8080/actuator/health
```

Check that generated TLS secrets are not tracked:

```powershell
git ls-files |
  Select-String "runtime/tls|\.p12$|\.key$|tls-password"
```

The command should produce no output.