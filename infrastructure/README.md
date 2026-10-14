# Infrastructure

Docker Compose runs the complete local Monster Tamers environment:

- PostgreSQL on port 5432
- Spring Boot backend on port 8080
- React frontend through Nginx on port 3000

## Start everything

From the repository root:

docker compose -f infrastructure/compose.yaml up --build

Run in the background:

docker compose -f infrastructure/compose.yaml up --build -d

## Check service status

docker compose -f infrastructure/compose.yaml ps

## View logs

docker compose -f infrastructure/compose.yaml logs -f

Backend logs only:

docker compose -f infrastructure/compose.yaml logs -f backend

## Stop the environment

docker compose -f infrastructure/compose.yaml down

The PostgreSQL data volume is preserved.

## Reset the database

docker compose -f infrastructure/compose.yaml down -v

Warning: This permanently removes the local PostgreSQL data volume.

## Local addresses

- Frontend: http://localhost:3000
- Backend: http://localhost:8080/api
- Backend health: http://localhost:8080/actuator/health
- PostgreSQL: localhost:5432

## Environment overrides

Copy infrastructure/.env.example to infrastructure/.env if local values must be changed.
Never commit the real infrastructure/.env file.
