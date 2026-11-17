FROM mcr.microsoft.com/powershell:lts-7.4-alpine-3.20
RUN apk add --no-cache openssl openjdk21-jre-headless git
WORKDIR /repo
CMD test -f infrastructure/runtime/tls/tls-password.txt || pwsh infrastructure/tls/generate-local-certificates.ps1 -Password "$TLS_PASSWORD"
