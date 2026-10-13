# Monster Tamers

Monster Tamers is a monorepo for a web application with a TypeScript, React,
Vite and Tailwind CSS frontend; a Java 21, Maven and Spring Boot backend; a
PostgreSQL database; and Docker Compose infrastructure.

> **Repository status:** This repository is not yet fully initialized.
> It currently contains the initial monorepo structure and shared project
> configuration only. The frontend and backend applications, database schema,
> infrastructure services and project documentation are not implemented yet.

## Repository layout

- `frontend/` - frontend application (planned)
- `backend/` - backend application (planned)
- `infrastructure/` - Docker Compose and related infrastructure (planned)
- `docs/requirements/` - requirements documentation
- `docs/architecture/` - architecture documentation
- `docs/development-verification/` - development and verification guidance
- `.github/workflows/` - CI/CD workflows

## Development

The application toolchains have not been initialized yet. Do not expect the
frontend or backend to build until their application setup is added.

When local environment configuration is needed, copy `.env.example` to `.env`
and replace placeholder values. Do not commit `.env` or other secret-bearing
files.

See [CONTRIBUTING.md](CONTRIBUTING.md) for contribution guidelines.
