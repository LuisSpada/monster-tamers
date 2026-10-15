FROM maven:3.9.11-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY app/pom.xml app/
COPY auth/pom.xml auth/
COPY encounter/pom.xml encounter/
COPY monster/pom.xml monster/
COPY player/pom.xml player/
COPY shared/pom.xml shared/
COPY simulator/pom.xml simulator/
RUN mvn -q -DskipTests dependency:go-offline

COPY . .
RUN mvn -q package -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN apk add --no-cache wget

COPY --from=build /app/app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]