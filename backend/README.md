# Backend
Java 21, Maven and Spring Boot modular-monolith starter.

```bash
./mvnw test
./mvnw install -DskipTests && ./mvnw -pl app spring-boot:run

.\mvnw.cmd test
.\mvnw.cmd install -DskipTests; .\mvnw.cmd -pl app spring-boot:run
```

Runtime uses PostgreSQL. The test profile uses H2 only for initial context tests. Game entities, authentication and migrations are intentionally not implemented yet.

## Modules

| Module | Owns | May use |
|---|---|---|
| `auth` | accounts, login, tokens | `player`, `monster`, `shared` |
| `player` | profile, stats, final-boss progress | `encounter`, `shared` |
| `monster` | catalogue, owned monsters, generation, progression, team | `shared` |
| `encounter` | encounters, unlocks, rewards, history | `monster`, `simulator`, `shared` |
| `simulator` | pure deterministic battle engine, no database | — |
| `shared` | technical helpers, no game logic | — |
| `app` | Spring Boot entry point, configuration, tests across modules | all |

Other modules may only use a module's `api` package; everything else is internal.
`ModularityTests` (Spring Modulith) fails the build otherwise.

## Migrations

Flyway. Each module keeps its migrations in `{module}/src/main/resources/db/migration/{module}/`,
named `V{yyyyMMdd_HHmm}__{module}_{description}.sql`. Versions are global across modules.
Never rewrite a migration already shared with the team.
