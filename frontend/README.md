# Monster Tamers frontend

The frontend is a React/Vite application using Tailwind CSS v4 and the
pixel-art assets in `public/assets`. It currently uses local mock data only;
authentication, search, logout, and API integration are intentionally static.

## Development

```powershell
npm ci
npm run dev
```

The Vite server listens on `0.0.0.0:5173`, which works locally and in the
frontend Docker image. Available checks are:

```powershell
npm run lint
npm run test
npm run build
```

Routes use `BrowserRouter`. The nginx production configuration falls back to
`index.html`, so refreshing nested routes such as `/collection` works.

The sidebar is desktop-persistent and starts closed on mobile. It can be
closed with the backdrop, Escape, or after selecting a navigation item.
Light/dark mode is stored in `localStorage`.

## Data boundaries

Domain types live in `src/types`, while temporary fixtures live in `src/mock`.
The separation is deliberate so mock data can later be replaced by API
adapters without changing page contracts.

## Assets

See [ATTRIBUTIONS.md](./ATTRIBUTIONS.md) for the pixel-art asset attribution.
