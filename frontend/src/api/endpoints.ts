export const endpoints = {
  playerProgress: "/api/players/me/progress",
  monsters: "/api/monsters",
  monster: (id: string) => `/api/monsters/${encodeURIComponent(id)}`,
  spendStatPoint: (id: string) => `/api/monsters/${encodeURIComponent(id)}/stat-points`,
  team: "/api/monsters/team",
  encounters: "/api/encounters",
  encounter: (id: string) => `/api/encounters/${encodeURIComponent(id)}`,
  startBattle: (encounterId: string) => `/api/encounters/${encodeURIComponent(encounterId)}/battles`,
  battles: "/api/encounters/battles",
  battle: (id: string) => `/api/encounters/battles/${encodeURIComponent(id)}`,
} as const;
