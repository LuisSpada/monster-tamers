# 1. Group OMB

Members: 
 - Olzhas Jalmukhambetov: o.jalmukhambetov@studenti.unipi.it
 - Martin Thomas Waxenberger: m.waxenberger@studenti.unipi.it
 - Benjamin Luis Spada: b.spada@studenti.unipi.it

# Monster Tamers

Monster Tamers is a web application for collecting monsters, building teams,
exploring encounters and participating in battles.

The project is organized as a monorepo with:

- TypeScript, React, Vite and Tailwind CSS frontend
- Java 21 and Spring Boot microservices
- PostgreSQL databases owned by individual services
- HTTPS communication through an API Gateway
- Docker Compose for the backend environment
- Maven, Vitest and ESLint verification
- GitHub Actions CI workflows

## Repository layout

```text
monster-tamers/
├── frontend/                 React frontend
├── backend/                  Spring Boot microservices
├── infrastructure/           TLS scripts and infrastructure documentation
├── docs/requirements/        Project requirements
├── docs/architecture/        Architecture documentation
├── .github/workflows/        CI workflows
└── README.md
```

## Architecture

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
    | HTTPS
    v
Simulator Service
```

Only the Gateway is exposed publicly by the backend Docker Compose stack.
Internal services and databases remain on the private Docker network.

## Requirements

Install the following tools:

- Docker Desktop
- Git
- Java 21
- Node.js 24
- OpenSSL
- Java `keytool`
- PowerShell (on mac/linux especially)

Check the required tools:

```powershell
docker --version
git --version
java --version
node --version
npm --version
openssl version
keytool -version
pwsh --version
```

## Quick start

### 1. Clone the repository

```powershell
git clone <repository-url>
cd monster-tamers
```

### 2. Make OpenSSL available

On Windows, OpenSSL may be available through Git for Windows:

```powershell
$env:Path = "C:\Program Files\Git\usr\bin;$env:Path"

Remove-Item Env:OPENSSL_CONF -ErrorAction SilentlyContinue
Remove-Item Env:SSLEAY_CONF -ErrorAction SilentlyContinue

openssl version
```

### 3. Generate local TLS certificates

on Windows:
```powershell
.\infrastructure\tls\generate-local-certificates.ps1
```
On mac/linux:
```powershell
pwsh ./infrastructure/tls/generate-local-certificates.ps1
```

Choose a TLS password with:

- at least 12 characters
- printable ASCII characters only
- no spaces
- no `æ`, `ø`, `å` or other non-ASCII characters

The generated certificates, private keys, keystores and password file are
stored under:

```text
infrastructure/runtime/tls/
```

This directory is ignored by Git and must never be committed.

#### If certificates already exist and must be replaced:

on Windows:
```powershell
.\infrastructure\tls\generate-local-certificates.ps1 -Force
```
on mac/Linux:
```powershell
pwsh ./infrastructure/tls/generate-local-certificates.ps1 -Force
```

### 4. Trust the local development CA

#### Install only the public CA certificate:

on Windows:
```powershell
Import-Certificate `
  -FilePath infrastructure\runtime\tls\ca.crt `
  -CertStoreLocation Cert:\CurrentUser\Root
```
on mac:
```powershell
security add-trusted-cert -d -r trustRoot -k ~/Library/Keychains/login.keychain-db infrastructure/runtime/tls/ca.crt
```
on linux:
```powershell
sudo cp infrastructure/runtime/tls/ca.crt /usr/local/share/ca-certificates/monster-tamers-ca.crt
sudo update-ca-certificates
```

#### Confirm that the certificate was installed:

on Windows:
```powershell
Get-ChildItem Cert:\CurrentUser\Root |
  Where-Object {
    $_.Subject -eq "CN=Monster Tamers Local Development CA"
  } |
  Select-Object Subject, Thumbprint, NotAfter
```
on mac:
```powershell
security find-certificate -c "Monster Tamers Local Development CA" -p ~/Library/Keychains/login.keychain-db | openssl x509 -noout -subject -fingerprint -enddate
```

on Linux:
```powershell
openssl x509 -in /etc/ssl/certs/monster-tamers-ca.pem -noout -subject -fingerprint -enddate
```

#### Never install, share or commit the CA private key:

```text
infrastructure/runtime/tls/ca.key
```

#### If the certificates are regenerated, remove the old CA before importing the new one:

on Windows:
```powershell
Get-ChildItem Cert:\CurrentUser\Root |
  Where-Object {
    $_.Subject -eq "CN=Monster Tamers Local Development CA"
  } |
  Remove-Item

Import-Certificate `
  -FilePath infrastructure\runtime\tls\ca.crt `
  -CertStoreLocation Cert:\CurrentUser\Root
```

on mac:
```powershell
security delete-certificate -c "Monster Tamers Local Development CA" ~/Library/Keychains/login.keychain-db
security add-trusted-cert -d -r trustRoot -k ~/Library/Keychains/login.keychain-db infrastructure/runtime/tls/ca.crt
```

on Linux:
 - Step 1: regenerate certs
```powershell
sudo rm -f /usr/local/share/ca-certificates/monster-tamers-ca.crt
sudo update-ca-certificates --fresh
sudo cp infrastructure/runtime/tls/ca.crt /usr/local/share/ca-certificates/monster-tamers-ca.crt
sudo update-ca-certificates
```
 - Step 2: browser trust store for Chrome and Chromium
```powershell
sudo apt-get install -y libnss3-tools
certutil -d sql:$HOME/.pki/nssdb -A -t "C,," -n "Monster Tamers Local Development CA" -i infrastructure/runtime/tls/ca.crt
certutil -d sql:$HOME/.pki/nssdb -L | grep "Monster Tamers"
```

 - Step 2.1: if certs are regenerated, delete old browser entry before re-importing:
```powershell
certutil -d sql:$HOME/.pki/nssdb -D -n "Monster Tamers Local Development CA"
certutil -d sql:$HOME/.pki/nssdb -A -t "C,," -n "Monster Tamers Local Development CA" -i infrastructure/runtime/tls/ca.crt
```

### 5. Configure the backend

Create the local backend environment file:

```powershell
Copy-Item backend\.env.example backend\.env
```

on mac/Linux:
```powershell
cp backend/.env.example backend/.env
```

For local registration, make sure `backend/.env` contains:

```dotenv
MAIL_MODE=logging
```

The application does not require an email provider during local development.
Registration confirmation codes are written to the Auth Service logs.

Do not commit `backend/.env`.

### 6. Start the backend

Start all microservices and databases with Docker Compose:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  up --build -d
```

Check the container status:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  ps
```

The following application services should become healthy:

```text
gateway-service
auth-service
player-service
monster-service
encounter-service
simulator-service
```

The four PostgreSQL containers should also become healthy.

#### Test the HTTPS Gateway:

on Windows:
```powershell
Invoke-RestMethod https://localhost:8080/actuator/health
```

on mac/Linux:
```powershell
curl -fsSL https://localhost:8080/actuator/health
```

Expected result:

```text
UP
```

### 7. Configure the frontend

Create the local frontend environment file:

on Windows:
```powershell
@"
VITE_API_MODE=http
VITE_GATEWAY_BASE_URL=https://localhost:8080
VITE_API_BASE_URL=https://localhost:8080/api
VITE_MOCK_LATENCY_MS=0
"@ | Set-Content frontend\.env.local
```

on mac/Linux:
```powershell
cat << 'EOF' > frontend/.env.local
VITE_API_MODE=http
VITE_GATEWAY_BASE_URL=https://localhost:8080
VITE_API_BASE_URL=https://localhost:8080/api
VITE_MOCK_LATENCY_MS=0
EOF
```

Do not commit `frontend/.env.local`.

### 8. Start the frontend

```powershell
cd frontend
npm ci
npm run dev
```

Open the application:

```text
http://localhost:5173
```

Keep the frontend terminal running while using the application.

## Register an account

1. Open `http://localhost:5173`.
2. Select **Create account**.
3. Enter an email address and password.
4. Open another PowerShell terminal.
5. Follow the Auth Service logs:

```powershell
docker compose `
  --env-file backend/.env `
  -f backend/compose.yaml `
  logs -f auth-service
```

6. Find the logged registration confirmation code.
7. Enter the code on the confirmation page.
8. Log in with the registered account.

Press `Ctrl+C` to stop following the logs. This does not stop the containers.

Email delivery is not required for the current local setup because
`MAIL_MODE=logging` writes the code directly to the Auth Service logs.

## Local addresses

| Component | Address |
|---|---|
| Frontend | `http://localhost:5173` |
| HTTPS Gateway | `https://localhost:8080` |
| API base URL | `https://localhost:8080/api` |
| Gateway health | `https://localhost:8080/actuator/health` |
| Swagger UI | `https://localhost:8080/swagger-ui.html` |

Internal microservices and PostgreSQL databases are not published to the host.

## Stop the application

Stop the frontend by pressing `Ctrl+C` in the frontend terminal.

Stop the backend while preserving database data:

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

The second command permanently removes local database data.

## Run tests

### Backend

```powershell
cd backend
.\mvnw.cmd clean verify
```

for mac/Linux:
```powershell
cd backend
./mvnw clean verify
```

### Frontend

```powershell
cd frontend
npm test
npm run lint
npm run build
```

## Verify HTTPS routing

#### Check the Gateway:

For Windows:
```powershell
Invoke-RestMethod https://localhost:8080/actuator/health
```
for mac/Linux:
```powershell
curl -fsSL https://localhost:8080/actuator/health
```

#### Check the service documentation through the Gateway:

For Windows:
```powershell
Invoke-RestMethod https://localhost:8080/docs/auth/v3/api-docs
Invoke-RestMethod https://localhost:8080/docs/players/v3/api-docs
Invoke-RestMethod https://localhost:8080/docs/monsters/v3/api-docs
Invoke-RestMethod https://localhost:8080/docs/encounters/v3/api-docs
```

for mac/Linux:
```powershell
curl -fsSL https://localhost:8080/docs/auth/v3/api-docs
curl -fsSL https://localhost:8080/docs/players/v3/api-docs
curl -fsSL https://localhost:8080/docs/monsters/v3/api-docs
curl -fsSL https://localhost:8080/docs/encounters/v3/api-docs
```

#### Plain HTTP should be rejected:

For Windows:
```powershell
try {
    Invoke-WebRequest http://localhost:8080/actuator/health
} catch {
    $_.Exception.Message
}
```

for mac/Linux:
```powershell
curl -fsS http://localhost:8080/actuator/health || echo "Plain HTTP rejected as expected"
```

## Security

Never commit:

- `backend/.env`
- `frontend/.env.local`
- `infrastructure/runtime/tls/`
- CA private keys
- service private keys
- PKCS#12 files
- truststores
- TLS passwords
- SMTP credentials

Each developer and reviewer should generate separate local TLS material.

If a credential is displayed in terminal output, chat, source control or a pull
request, revoke it and generate a replacement immediately.

## Frontend Docker status

All backend microservices and databases run through Docker Compose.

The frontend currently runs through the Vite development server:

```powershell
cd frontend
npm run dev
```

The final delivery requires the complete application to run through Docker.
The production frontend build must also be served from a frontend container,
for example through Nginx, and added to `backend/compose.yaml`.

Until that integration is implemented, follow the frontend startup instructions
in this README.

## More documentation

- backend/README.md
- infrastructure/README.md
- [Contribution guidelines](CONTRIBUTING.md)
- docs/requirements/
- docs/architecture/
