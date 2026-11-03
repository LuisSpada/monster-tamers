import type {
  BattleResult,
  BattleSummary,
  Encounter,
  Monster,
  PlayerProgress,
  StatKey,
  TeamState,
} from "../domain/types";

export interface PlayerService {
  getProgress(): Promise<PlayerProgress>;
}

export interface MonsterService {
  listMonsters(): Promise<Monster[]>;
  getMonster(monsterId: string): Promise<Monster>;
  /** Spends one pending stat point on the given stat. Rejects with ApiError when none are left. */
  spendStatPoint(monsterId: string, stat: StatKey): Promise<Monster>;
}

export interface TeamService {
  getTeam(): Promise<TeamState>;
  /** Replaces the ordered team. Rejects with ApiError (422) when the team rules are violated. */
  saveTeam(monsterIds: string[]): Promise<TeamState>;
}

export interface EncounterService {
  listEncounters(): Promise<Encounter[]>;
  getEncounter(encounterId: string): Promise<Encounter>;
}

export interface BattleService {
  /** Runs the automated battle with the given ordered team and returns the full result. */
  startBattle(encounterId: string, monsterIds: string[]): Promise<BattleResult>;
}

export interface HistoryService {
  /** Newest first. */
  listBattles(): Promise<BattleSummary[]>;
  getBattle(battleId: string): Promise<BattleResult>;
}

export interface Services {
  players: PlayerService;
  monsters: MonsterService;
  teams: TeamService;
  encounters: EncounterService;
  battles: BattleService;
  history: HistoryService;
}
