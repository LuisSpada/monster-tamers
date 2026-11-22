# Threat Model – Monster Tamers

## Assumptions checked against the current system

- Frontend runs in a browser and talks only to the API Gateway.
- Gateway Service is the single public backend entry point.
- All backend service-to-service communication uses HTTPS/TLS with the trust stores configured in `compose.yaml`.
- Auth Service owns registration, confirmation and login.
- Each business service owns its own database.
- Auth Service calls Player Service.
- Player Service calls Monster Service.
- Encounter Service calls Monster Service and Simulator Service.

## Data Flow Diagram (DFD)

```mermaid
flowchart LR
    User[Player Browser]

    subgraph TB1["Trust Boundary 1: Internet / User Device"]
        User
    end

    subgraph TB2["Trust Boundary 2: Application Services"]
        Frontend[Frontend]
        Gateway[Gateway Service]
        Auth[Auth Service]
        Player[Player Service]
        Monster[Monster Service]
        Encounter[Encounter Service]
        Simulator[Simulator Service]
    end

    subgraph TB3["Trust Boundary 3: Data Stores"]
        AuthDB[(Auth PostgreSQL)]
        PlayerDB[(Player PostgreSQL)]
        MonsterDB[(Monster PostgreSQL)]
        EncounterDB[(Encounter PostgreSQL)]
    end

    User -->|"1 HTTPS"| Frontend
    Frontend -->|"2 HTTPS + JWT"| Gateway

    Gateway -->|"3 HTTPS"| Auth
    Gateway -->|"4 HTTPS + JWT"| Player
    Gateway -->|"5 HTTPS + JWT"| Monster
    Gateway -->|"6 HTTPS + JWT"| Encounter

    Auth -->|"7 HTTPS"| Player
    Player -->|"8 HTTPS"| Monster
    Encounter -->|"9 HTTPS"| Monster
    Encounter -->|"10 HTTPS"| Simulator

    Auth -->|"11 JDBC + DB credentials"| AuthDB
    Player -->|"12 JDBC + DB credentials"| PlayerDB
    Monster -->|"13 JDBC + DB credentials"| MonsterDB
    Encounter -->|"14 JDBC + DB credentials"| EncounterDB
```

## Data Flow Review

| # | Source | Destination | Transport | Purpose |
|---|---|---|---|---|
| 1 | User | Frontend | HTTPS | Load UI |
| 2 | Frontend | Gateway | HTTPS + JWT | API requests |
| 3 | Gateway | Auth Service | HTTPS/TLS | Login, registration, confirmation |
| 4 | Gateway | Player Service | HTTPS/TLS + JWT | Player profile |
| 5 | Gateway | Monster Service | HTTPS/TLS + JWT | Collection and teams |
| 6 | Gateway | Encounter Service | HTTPS/TLS + JWT | Encounters and battles |
| 7 | Auth Service | Player Service | HTTPS/TLS | Create player after registration |
| 8 | Player Service | Monster Service | HTTPS/TLS | Initialize collection |
| 9 | Encounter Service | Monster Service | HTTPS/TLS | Load battle monsters |
| 10 | Encounter Service | Simulator Service | HTTPS/TLS | Execute battle simulation |
| 11 | Auth Service | Auth PostgreSQL | JDBC | Accounts and credentials |
| 12 | Player Service | Player PostgreSQL | JDBC | Player profile data |
| 13 | Monster Service | Monster PostgreSQL | JDBC | Monsters and teams |
| 14 | Encounter Service | Encounter PostgreSQL | JDBC | Encounter progress and battle history |

## Detailed Data Flow Diagram: Individual API Calls

This version expands the aggregated flows above. Each currently active HTTP
operation is represented by its own arrow. Operations that remain mock-backed
in the frontend are not included.

## Detailed Data Flow Diagram: Individual API Calls

```mermaid
flowchart LR
    User[Player Browser]

    subgraph TB1["Trust Boundary 1: Internet / User Device"]
        User
        Frontend[Frontend]
    end

    subgraph TB2["Trust Boundary 2: Application Services"]
        Gateway[Gateway Service]
        Auth[Auth Service]
        Player[Player Service]
        Monster[Monster Service]
        Encounter[Encounter Service]
        Simulator[Simulator Service]
    end

    subgraph TB3["Trust Boundary 3: Data Stores"]
        AuthDB[(Auth PostgreSQL)]
        PlayerDB[(Player PostgreSQL)]
        MonsterDB[(Monster PostgreSQL)]
        EncounterDB[(Encounter PostgreSQL)]
    end

    User -->|"HTTPS: load and use UI"| Frontend

    Frontend -->|"POST /api/auth/login"| Gateway
    Gateway -->|"POST /api/auth/login"| Auth

    Frontend -->|"POST /api/auth/registrations"| Gateway
    Gateway -->|"POST /api/auth/registrations"| Auth

    Frontend -->|"POST /api/auth/registrations/{registrationId}/confirmation"| Gateway
    Gateway -->|"POST /api/auth/registrations/{registrationId}/confirmation"| Auth

    Frontend -->|"GET /api/monsters + JWT"| Gateway
    Gateway -->|"GET /api/monsters + JWT"| Monster

    Frontend -->|"GET /api/monsters/team + JWT"| Gateway
    Gateway -->|"GET /api/monsters/team + JWT"| Monster

    Frontend -->|"PUT /api/monsters/team + JWT"| Gateway
    Gateway -->|"PUT /api/monsters/team + JWT"| Monster

    Frontend -->|"GET /api/encounters + JWT"| Gateway
    Gateway -->|"GET /api/encounters + JWT"| Encounter

    Frontend -->|"GET /api/encounters/{encounterId} + JWT"| Gateway
    Gateway -->|"GET /api/encounters/{encounterId} + JWT"| Encounter

    Frontend -->|"POST /api/encounters/{encounterId}/battles + JWT"| Gateway
    Gateway -->|"POST /api/encounters/{encounterId}/battles + JWT"| Encounter

    Frontend -.->|"GET /api/players/me/progress + JWT"| Gateway
    Gateway -.->|"GET /api/players/me/progress + JWT"| Player

    Frontend -.->|"GET /api/monsters/{monsterId} + JWT"| Gateway
    Gateway -.->|"GET /api/monsters/{monsterId} + JWT"| Monster

    Frontend -.->|"POST /api/monsters/{monsterId}/stat-points + JWT"| Gateway
    Gateway -.->|"POST /api/monsters/{monsterId}/stat-points + JWT"| Monster

    Frontend -.->|"GET /api/encounters/battles + JWT"| Gateway
    Gateway -.->|"GET /api/encounters/battles + JWT"| Encounter

    Frontend -.->|"GET /api/encounters/battles/{battleId} + JWT"| Gateway
    Gateway -.->|"GET /api/encounters/battles/{battleId} + JWT"| Encounter

    Auth -->|"POST /internal/players over HTTPS"| Player
    Player -->|"PUT /internal/accounts/{accountId}/collection over HTTPS"| Monster
    Encounter -->|"POST /internal/accounts/{accountId}/battle-monsters over HTTPS"| Monster
    Encounter -->|"POST /internal/battles over HTTPS"| Simulator

    Auth -->|"JDBC + DB credentials"| AuthDB
    Player -->|"JDBC + DB credentials"| PlayerDB
    Monster -->|"JDBC + DB credentials"| MonsterDB
    Encounter -->|"JDBC + DB credentials"| EncounterDB

    linkStyle 19,20,21,22,23,24,25,26,27,28 stroke:#d97706,stroke-width:2px
```

**Legend:**

- Solid arrow: implemented and active in frontend HTTP mode.
- Orange dashed arrow: HTTP adapter/backend operation exists, but the frontend currently uses mock or in-memory behavior.
- Authentication operations are public.
- Public game API operations require a JWT.
- Internal service calls bypass the Gateway and use the private TLS network.

The authentication endpoints are public. The Monster and Encounter API calls
carry a JWT. Internal calls do not pass through the Gateway and rely on the
private service network and configured TLS trust.

## What the AI Draft Is Likely to Miss MUST BE REMOVED

1. The three trust boundaries.
2. The four separate PostgreSQL databases.
3. Auth Service → Player Service communication.
4. Player Service → Monster Service communication.
5. Encounter Service → Simulator Service communication.
6. TLS on internal service-to-service traffic.
7. The frontend communicating through the Gateway rather than directly with backend services.

## Assets

| ID | Asset |
|----|--------|
| A1 | User accounts |
| A2 | User credentials |
| A3 | JWT secret |
| A4 | JWT access tokens |
| A5 | Player profiles |
| A6 | Monster collections |
| A7 | Team configurations |
| A8 | Encounter progress |
| A9 | Battle history |
| A10 | PostgreSQL databases |
| A11 | TLS certificates and keystores |
| A12 | Trust stores |
| A13 | Registration confirmation codes |
| A14 | Environment-variable secrets |

## Attack Surfaces

| ID | Attack Surface |
|----|----------------|
| S1 | Frontend exposed to browsers |
| S2 | Gateway HTTPS endpoints |
| S3 | Registration endpoints |
| S4 | Login endpoints |
| S5 | JWT validation |
| S6 | Player Service APIs |
| S7 | Monster Service APIs |
| S8 | Encounter Service APIs |
| S9 | Internal service-to-service APIs |
| S10 | Simulator internal endpoint |
| S11 | PostgreSQL instances |
| S12 | TLS certificate handling |
| S13 | Docker secrets and mounted credentials |
