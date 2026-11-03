/**
 * Domain types used by the frontend service layer.
 * Source: Monster Tamers Requirements V2 / Acceptance Criteria.
 * The backend is authoritative for every rule below; the frontend only displays values it receives.
 */
export const MONSTER_CLASSES = ["Fighter", "Guardian", "Shaman", "Trickster"] as const;
export type MonsterClass = (typeof MONSTER_CLASSES)[number];

export const STAT_KEYS = ["strength", "hp", "accuracy", "agility"] as const;
export type StatKey = (typeof STAT_KEYS)[number];
export type Stats = Record<StatKey, number>;

export const MAX_LEVEL = 10;
export const MIN_TALENT = 80;
export const MAX_TALENT = 120;
export const MIN_TEAM_SIZE = 1;
export const MAX_TEAM_SIZE = 3;

export interface NamedEffect {
  name: string;
  description: string;
}

export interface StatUpgrade {
  id: string;
  stat: StatKey;
  /** Value of the stat after the upgrade. */
  newValue: number;
  levelAtUpgrade: number;
  at: string;
}

export interface Monster {
  id: string;
  name: string;
  monsterClass: MonsterClass;
  skill: NamedEffect;
  trait: NamedEffect;
  /** 80-120, fixed for the lifetime of the monster. */
  talent: number;
  level: number;
  xp: number;
  /** XP needed for the next level, or null at max level. */
  xpToNext: number | null;
  pendingPoints: number;
  stats: Stats;
  upgrades: StatUpgrade[];
  /** Selects one of the avatar variants of the class. */
  avatarVariant: number;
  obtainedAt: string;
}

export interface Opponent {
  id: string;
  name: string;
  monsterClass: MonsterClass;
  level: number;
  avatarVariant: number;
}

export type EncounterStatus = "locked" | "available" | "cleared";

export interface Encounter {
  id: string;
  order: number;
  name: string;
  description: string;
  /** 0-100 */
  eggChance: number;
  rewardClasses: MonsterClass[];
  isFinalBoss: boolean;
  status: EncounterStatus;
  opponents: Opponent[];
}

export interface TeamState {
  monsterIds: string[];
}

export type BattleOutcome = "victory" | "defeat";
export type BattleEventKind = "damage" | "heal" | "control" | "defeat" | "info";

export interface BattleEvent {
  id: string;
  round: number;
  side: "player" | "enemy";
  kind: BattleEventKind;
  message: string;
}

export interface BattleParticipant {
  monsterId: string;
  name: string;
  monsterClass: MonsterClass;
  level: number;
}

export interface XpAward {
  monsterId: string;
  name: string;
  xpGained: number;
}

export interface LevelUp {
  monsterId: string;
  name: string;
  fromLevel: number;
  toLevel: number;
  pointsGained: number;
}

export interface BattleRewards {
  xp: XpAward[];
  levelUps: LevelUp[];
  /** Newly hatched monster, if an egg was awarded. */
  egg: Monster | null;
  finalBossDefeated: boolean;
}

export interface BattleResult {
  id: string;
  encounterId: string;
  encounterName: string;
  isFinalBoss: boolean;
  playedAt: string;
  outcome: BattleOutcome;
  rounds: number;
  participants: BattleParticipant[];
  rewards: BattleRewards;
  events: BattleEvent[];
}

export interface BattleSummary {
  id: string;
  encounterId: string;
  encounterName: string;
  playedAt: string;
  outcome: BattleOutcome;
  rounds: number;
  participantNames: string[];
  eggHatched: boolean;
}

export interface PlayerProgress {
  playerName: string;
  collectionSize: number;
  teamSize: number;
  encountersCleared: number;
  encountersTotal: number;
  pendingPoints: number;
  finalBossDefeated: boolean;
  wins: number;
  losses: number;
}
