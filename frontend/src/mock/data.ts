import type { Encounter, OwnedMonster, PlayerSummary } from "@/types/game";

/** Midlertidig mock-data. Byttes ut med API-kall (GET /me/progress, /me/team, /encounters). */
export const summary: PlayerSummary = {
  playerName: "Tamer",
  collectionSize: 8,
  teamSize: 2,
  encountersCleared: 3,
  encountersTotal: 5,
  pendingPoints: 2,
  finalBossDefeated: false,
};

export const team: OwnedMonster[] = [
  {
    id: "m1", name: "Fierce Fox", className: "Fighter", skill: "Vampiric Strike", trait: "Hot-headed",
    talent: 112, level: 4, xp: 210, xpToNext: 400, pendingPoints: 1,
    stats: { strength: 16, hp: 104, accuracy: 61, agility: 41 }, image: "flamkit",
  },
  {
    id: "m2", name: "Calm Drake", className: "Shaman", skill: "Healing Aura", trait: "Unshakable",
    talent: 96, level: 3, xp: 140, xpToNext: 300, pendingPoints: 1,
    stats: { strength: 6, hp: 92, accuracy: 66, agility: 42 }, image: "aquor",
  },
];

export const nextEncounter: Encounter = {
  id: "e4", name: "Misty Forest", order: 4, eggChance: 35,
  rewardClasses: ["Fighter", "Guardian", "Shaman"], unlocked: true, cleared: false,
};

export const opponentSprites = ["enc-1", "enc-2", "enc-3"];

/** Midlertidige varsler. Byttes ut med data fra backend. */
export const notifications = [
  { id: "n1", icon: "act-level", title: "Fierce Fox reached level 4", sub: "1 stat point to spend", time: "2h ago", unread: true },
  { id: "n2", icon: "act-battle", title: "Won against Forest Scout", sub: "+48 XP (Talent 112)", time: "3h ago", unread: true },
  { id: "n3", icon: "act-catch", title: "Egg hatched: Sturdy Cub", sub: "A new Guardian joined your collection!", time: "5h ago", unread: false },
];
