# Monster Tamers frontend

The frontend is a React/Vite application using Tailwind CSS v4 and the
pixel-art assets in `public/assets`. It supports both a hybrid HTTP mode
(calling the Gateway for authentication, owned monsters, monster species
catalogue, teams, encounters, battles and battle history) and a local mock mode
for offline development and tests.

## Development

```powershell
npm ci
npm run dev
```

During local development, the Vite server listens on `0.0.0.0:5173`.
Available checks are:

```powershell
npm run lint
npm run test
npm run build
```

The production Docker image builds static frontend assets with Vite, then
Nginx serves those generated assets inside the production container. Docker
Compose exposes the frontend at `http://localhost:3000`. Routes use
`BrowserRouter`, and Nginx falls back to `index.html` so nested routes such as
`/collection` and `/monsters` work when refreshed.

The sidebar is desktop-persistent and starts closed on mobile. It can be
closed with the backdrop, Escape, or after selecting a navigation item.
Light/dark mode is stored in `localStorage`.

## Data boundaries

Domain types live in `src/domain/types.ts`, API adapters live in `src/api/`,
and temporary fixtures live in `src/api/mock/`. The separation is deliberate
so mock data can be replaced endpoint by endpoint without changing page
contracts.

## Assets

See [ATTRIBUTIONS.md](./ATTRIBUTIONS.md) for the pixel-art asset attribution.
