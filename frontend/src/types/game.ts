/**
 * Typer som speiler kravspesifikasjonen (V2) og REST-API-et fra arkitekturdokumentet.
 * Backend er autoritativ (CON-06 / UI-05): frontend viser kun verdier den får fra API-et.
 */
export type ClassName = "Fighter" | "Guardian" | "Shaman" | "Trickster";

export interface Stats {
  strength: number;
  hp: number;
  accuracy: number;
  agility: number;
}

/** GET /me/monsters[/{id}] */
export interface OwnedMonster {
  id: string;
  name: string; // generert: adjektiv + substantiv (GEN-04)
  className: ClassName; // permanent (CLS-04)
  skill: string; // ett skill fra klassens pool (GEN-01)
  trait: string; // ett trait fra felles pool (GEN-02)
  talent: number; // 80-120, fast (GEN-03)
  level: number; // 1-10 (PRO-01)
  xp: number;
  xpToNext: number; // 100 x level (PRO-02), levert av backend
  pendingPoints: number; // ubrukte stat-poeng (PRO-05)
  stats: Stats;
  image: string; // filnavn i /assets, valgt fra klassens bildepool (GEN-06)
}

/** GET /me/progress (PLY-05) */
export interface PlayerSummary {
  playerName: string;
  collectionSize: number;
  teamSize: number; // 1-3 (CON-03)
  encountersCleared: number;
  encountersTotal: number;
  pendingPoints: number;
  finalBossDefeated: boolean;
}

/** GET /encounters (RWD-01: eggsjanse og belønningsklasser vises før start) */
export interface Encounter {
  id: string;
  name: string;
  order: number;
  eggChance: number; // 0-100
  rewardClasses: ClassName[];
  unlocked: boolean;
  cleared: boolean;
}

export const CLASS_INFO: Record<ClassName, { letter: string; role: string; color: string }> = {
  Fighter: { letter: "F", role: "Damage", color: "bg-orange-500" },
  Guardian: { letter: "G", role: "Protection", color: "bg-sky-500" },
  Shaman: { letter: "S", role: "Support", color: "bg-emerald-500" },
  Trickster: { letter: "T", role: "Control", color: "bg-violet-500" },
};

export const MAX_TEAM_SIZE = 3;
