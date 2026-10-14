# Backend
Java 21, Maven and Spring Boot modular-monolith starter.

```bash
./mvnw test
./mvnw spring-boot:run

.\mvnw.cmd test
.\mvnw.cmd spring-boot:run
```

Runtime uses PostgreSQL. The test profile uses H2 only for initial context tests. Game entities, authentication and migrations are intentionally not implemented yet.
