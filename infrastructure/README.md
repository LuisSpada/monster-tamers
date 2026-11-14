# Infrastructure

The local Monster Tamers environment consists of:

- React frontend
- HTTPS API Gateway
- Auth Service
- Player Service
- Monster Service
- Encounter Service
- Simulator Service
- separate PostgreSQL databases for service-owned data

The application services communicate over HTTPS. Only the Gateway is published from the backend Compose stack.

## Local architecture

```text
Frontend
    |
    | HTTPS
    v
Gateway Service
    |
    | HTTPS
    +--> Auth Service
    +--> Player Service
    +--> Monster Service
    +--> Encounter Service

Auth Service
    |
    | HTTPS
    v
Player Service
    |
    | HTTPS
    v
Monster Service

Encounter Service
    |
    | HTTPS in the battle integration
    v
Simulator Service
```

Simulator Service has no public Gateway route.

PostgreSQL containers remain private to the Docker backend network.

## Prerequisites

Local infrastructure requires:

- Docker Desktop
- PowerShell
- OpenSSL
- Java `keytool`
- Git

Check the required certificate tools:

```powershell
openssl version
keytool -version
git --version
```

On Windows, OpenSSL may be available through Git for Windows:

```powershell
$env:Path = "C:\Program Files\Git\usr\bin;$env:Path"

Remove-Item Env:OPENSSL_CONF -ErrorAction SilentlyContinue
Remove-Item Env:SSLEAY_CONF -ErrorAction SilentlyContinue

openssl version
```

## Local TLS certificates

Generate local TLS material before starting the environment:

```powershell
.\infrastructure\tls\generate-local-certificates.ps1
```

The script requests a TLS password. Use:

- at least 12 characters
- printable ASCII characters only
- no spaces
- no non-ASCII letters or symbols

To replace existing local certificates:

```powershell
.\infrastructure\tls\generate-local-certificates.ps1 -Force
```

Generated material is stored under:

```text
infrastructure/runtime/tls/
```

The generated directory contains:

- a local development CA
- one server certificate and keystore per service
- a shared truststore
- the local TLS password file

The runtime directory is ignored by Git. Never commit its contents.

## Trust the local CA

The browser and local command-line clients must trust the development CA before connecting to the HTTPS Gateway.

On Windows, install only the public CA certificate:

```powershell
Import-Certificate `
  -FilePath infrastructure\runtime\tls\ca.crt `
  -CertStoreLocation Cert:\CurrentUser\Root
```

Never install, share or commit:

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

Regenerating the certificates also generates a new CA. Remove the previous development CA before importing the new one:

```powershell
Get-ChildItem Cert:\CurrentUser\Root |
  Where-Object {
    $_.Subject -eq "CN=Monster Tamers Local Development CA"
  } |
  Remove-Item
```

Then import the regenerated public certificate:

```powershell
Import-Certificate `
  -FilePath infrastructure\runtime\tls\ca.crt `
  -CertStoreLocation Cert:\CurrentUser\Root
```

## Environment configuration

### Backend

Create the local backend environment file:

```powershell
Copy-Item backend/.env.example backend/.env
```

Edit `backend/.env` with local database, JWT and optional SMTP settings.

Never commit the real file.

Confirm that Git ignores it:

```powershell
git check-ignore -v backend/.env
```

Avoid sharing the output of:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  config
```

The rendered configuration can contain interpolated credentials.

To inspect only the service inventory, use:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  config --services
```

### Frontend

Create a local frontend environment file:

```powershell
@"
VITE_API_MODE=http
VITE_GATEWAY_BASE_URL=https://localhost:8080
VITE_API_BASE_URL=https://localhost:8080/api
VITE_MOCK_LATENCY_MS=0
"@ | Set-Content frontend/.env.local
```

Confirm that Git ignores it:

```powershell
git check-ignore -v frontend/.env.local
```

The Vite development server may run over HTTP locally while calling the backend through the HTTPS Gateway.

## Start the backend

From the repository root:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  up --build -d
```

The backend stack starts:

- six Spring Boot application services
- four PostgreSQL containers
- one private backend network

Only Gateway port `8080` is published to the host.

## Check backend status

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

All application services should report:

```text
healthy
```

The PostgreSQL containers should also become healthy.

## Start the frontend

Run the frontend development server separately:

```powershell
cd frontend
npm run dev
```

The local frontend is available at:

```text
http://localhost:5173
```

The frontend calls:

```text
https://localhost:8080
```

Restart Vite after changing `.env.local`.

## Local addresses

| Component | Address |
|---|---|
| Frontend development server | `http://localhost:5173` |
| HTTPS Gateway | `https://localhost:8080` |
| API base URL | `https://localhost:8080/api` |
| Gateway health | `https://localhost:8080/actuator/health` |
| Swagger UI | `https://localhost:8080/swagger-ui.html` |

Internal microservices and PostgreSQL ports are not published by the default backend Compose stack.

## Validate HTTPS

Check Gateway health:

```powershell
Invoke-RestMethod https://localhost:8080/actuator/health
```

Expected status:

```text
UP
```

Confirm that plaintext HTTP is rejected:

```powershell
try {
    Invoke-WebRequest http://localhost:8080/actuator/health
} catch {
    $_.Exception.Message
}
```

The HTTP request should fail because port `8080` expects TLS.

## Validate Gateway routing

```powershell
Invoke-RestMethod https://localhost:8080/docs/auth/v3/api-docs
Invoke-RestMethod https://localhost:8080/docs/players/v3/api-docs
Invoke-RestMethod https://localhost:8080/docs/monsters/v3/api-docs
Invoke-RestMethod https://localhost:8080/docs/encounters/v3/api-docs
```

Successful responses confirm that Gateway can validate the internal service certificates and route to the services over HTTPS.

## Validate internal HTTPS

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

A successful check returns HTTP status `200`.

## View logs

View all backend logs:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  logs -f
```

View one service:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  logs -f auth-service
```

View recent Simulator Service logs:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  logs simulator-service --tail 200
```

## Stop the backend

Stop containers while preserving database volumes:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  down
```

## Reset local databases

Remove containers and database volumes:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  down -v
```

Warning: this permanently removes local PostgreSQL data.

Generated TLS material remains under:

```text
infrastructure/runtime/tls/
```

Delete that directory separately only when certificates must be regenerated.

## Security checks

Confirm that generated TLS secrets are ignored:

```powershell
git check-ignore -v `
  infrastructure/runtime/tls/ca.key `
  infrastructure/runtime/tls/truststore.p12 `
  infrastructure/runtime/tls/tls-password.txt `
  infrastructure/runtime/tls/gateway-service/server.p12
```

Confirm that no generated TLS secrets are tracked:

```powershell
git ls-files |
  Select-String "runtime/tls|\.p12$|\.key$|tls-password"
```

The command should produce no output.

Confirm that no generated TLS secrets are staged:

```powershell
git diff --cached --name-only |
  Select-String "runtime/tls|\.p12$|\.key$|tls-password"
```

The command should also produce no output.

## Production deployments

The locally generated CA and certificates are for development only.

Production deployments must obtain certificates and credentials from the deployment platform or an approved secret manager. Examples include:

- Kubernetes Secrets with cert-manager
- Azure Key Vault
- AWS Secrets Manager and AWS Certificate Manager
- HashiCorp Vault

Do not deploy:

- the local CA private key
- local service private keys
- local PKCS#12 files
- the local TLS password file

Production clients must continue validating certificate chains and hostnames. Do not configure trust-all certificate managers or disable hostname verification.