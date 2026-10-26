FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /workspace
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY shared-contracts/pom.xml shared-contracts/pom.xml
COPY player-service/pom.xml player-service/pom.xml
COPY gateway-service/pom.xml gateway-service/pom.xml
COPY auth-service/pom.xml auth-service/pom.xml
COPY monster-service/pom.xml monster-service/pom.xml
COPY encounter-service/pom.xml encounter-service/pom.xml
COPY simulator-service/pom.xml simulator-service/pom.xml
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
COPY shared-contracts/src shared-contracts/src
COPY player-service/src player-service/src
RUN ./mvnw -pl player-service -am package -DskipTests

RUN find player-service/target -maxdepth 1 -type f -name '*.jar' ! -name '*.original' -exec cp {} /workspace/service.jar \;

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN apk add --no-cache wget \
    && addgroup -S app \
    && adduser -S -G app app
COPY --from=build /workspace/service.jar /app/service.jar
USER app
ENV SERVER_PORT=8082
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider "http://127.0.0.1:${SERVER_PORT}/actuator/health" || exit 1
ENTRYPOINT ["sh", "-c", "exec java -Dserver.port=$SERVER_PORT -jar /app/service.jar"]
