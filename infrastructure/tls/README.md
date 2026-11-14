# Local TLS

This directory contains the script used to generate local TLS certificates for
the Monster Tamers backend.

Generated keys, certificates, keystores, truststores and passwords are written
to:

```text
infrastructure/runtime/tls/
```

That directory is ignored by Git and must never be committed.

## Requirements

The certificate-generation script requires:

- PowerShell 7 or Windows PowerShell
- OpenSSL
- Java `keytool`
- Git

Check the tools:

```powershell
openssl version
keytool -help
git --version
```

## Generate local certificates

From the repository root:

```powershell
.\infrastructure\tls\generate-local-certificates.ps1
```

The script asks for a local password. Use at least 12 characters.

To replace previously generated material:

```powershell
.\infrastructure\tls\generate-local-certificates.ps1 -Force
```

## Generated identities

The script generates one certificate for each application service:

- `gateway-service`
- `auth-service`
- `player-service`
- `monster-service`
- `encounter-service`
- `simulator-service`

Each certificate contains these Subject Alternative Names:

- the service's Docker Compose DNS name
- `localhost`
- `127.0.0.1`

This permits both Docker-network connections and certificate-validating
container health checks.

## Generated files

```text
infrastructure/runtime/tls/
├── ca.crt
├── ca.key
├── truststore.p12
├── tls-password.txt
├── gateway-service/
│   ├── server.crt
│   ├── server.key
│   └── server.p12
├── auth-service/
├── player-service/
├── monster-service/
├── encounter-service/
└── simulator-service/
```

The CA private key, service private keys, PKCS#12 files and password file are
secrets.

## Verify Git exclusion

```powershell
git check-ignore -v infrastructure/runtime/tls/ca.key
git check-ignore -v infrastructure/runtime/tls/truststore.p12
git check-ignore -v infrastructure/runtime/tls/tls-password.txt
```

All three commands must show an ignore rule.

Check that no generated material is staged:

```powershell
git status --short
git diff --cached --name-only |
  Select-String "runtime/tls|\.p12$|\.key$|tls-password"
```

The second command must produce no output.

## Inspect a certificate

```powershell
openssl x509 `
  -in infrastructure\runtime\tls\gateway-service\server.crt `
  -noout `
  -subject `
  -issuer `
  -dates `
  -ext subjectAltName
```

## Security

These certificates are for local development only.

Deployment environments must provide certificates and passwords through their
own secret-management system. Do not deploy the generated local CA private key.