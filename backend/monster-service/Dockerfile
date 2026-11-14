FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /workspace

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

COPY shared-contracts/pom.xml shared-contracts/pom.xml
COPY shared-web/pom.xml shared-web/pom.xml
COPY monster-service/pom.xml monster-service/pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY player-service/pom.xml player-service/pom.xml
COPY encounter-service/pom.xml encounter-service/pom.xml
COPY simulator-service/pom.xml simulator-service/pom.xml

RUN sed -i 's/\r$//' mvnw \
    && chmod +x mvnw

COPY shared-contracts/src shared-contracts/src
COPY shared-web/src shared-web/src
COPY monster-service/src monster-service/src

RUN ./mvnw \
    -pl monster-service \
    -am \
    package \
    -DskipTests

RUN find monster-service/target \
    -maxdepth 1 \
    -type f \
    -name '*.jar' \
    ! -name '*.original' \
    -exec cp {} /workspace/service.jar \;


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache wget \
    && addgroup -S app \
    && adduser -S -G app app

COPY --from=build /workspace/service.jar /app/service.jar

USER app

ENV SERVER_PORT=8083
ENV SERVER_SSL_ENABLED=true
ENV SERVER_SSL_KEY_STORE=/run/tls/server.p12
ENV SERVER_SSL_KEY_STORE_TYPE=PKCS12
ENV SERVER_SSL_KEY_ALIAS=monster-service

HEALTHCHECK \
    --interval=30s \
    --timeout=5s \
    --start-period=30s \
    --retries=3 \
    CMD wget \
        --ca-certificate=/run/tls/ca.crt \
        --no-verbose \
        --tries=1 \
        --spider \
        "https://127.0.0.1:${SERVER_PORT}/actuator/health" \
        || exit 1
ENTRYPOINT ["sh", "-c", "if [ -f /run/secrets/tls_password ]; then export SERVER_SSL_KEY_STORE_PASSWORD=\"$(cat /run/secrets/tls_password)\"; fi; exec java -Dserver.port=\"$SERVER_PORT\" -jar /app/service.jar"]