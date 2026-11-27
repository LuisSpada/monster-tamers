import { ApiError } from "../errors";
import type { Services } from "../contracts";
import { validateTeam } from "../../domain/teamRules";
import {
  MAX_LEVEL,
  MAX_TALENT,
  MIN_TALENT,
  type BattleEvent,
  type BattleResult,
  type Encounter,
  type LevelUp,
  type Monster,
  type MonsterClass,
  type StatKey,
} from "../../domain/types";
import {
  CLASS_BASE_STATS,
  MOCK_NAME_ADJECTIVES,
  MOCK_NAME_NOUNS,
  MOCK_SKILLS,
  MOCK_TRAITS,
  MOCK_SPECIES,
  createDefaultFixtures,
  type MockFixtures,
} from "./fixtures";

export interface MockOptions {
  /** Simulated network latency in ms (0 disables it, useful in tests). */
  latencyMs?: number;
  fixtures?: MockFixtures;
  /** Seed for the battle/egg randomness, so results are reproducible. */
  seed?: number;
}

/** How much one stat point adds. Mock-only: the real values are decided by the backend. */
const MOCK_STAT_INCREMENT: Record<StatKey, number> = { strength: 1, hp: 5, accuracy: 1, agility: 1 };

const clone = <T,>(value: T): T => JSON.parse(JSON.stringify(value)) as T;
const sleep = (ms: number) => (ms <= 0 ? Promise.resolve() : new Promise<void>((resolve) => setTimeout(resolve, ms)));

function mulberry32(seed: number): () => number {
  let a = seed >>> 0;
  return () => {
    a = (a + 0x6d2b79f5) >>> 0;
    let t = a;
    t = Math.imul(t ^ (t >>> 15), t | 1);
    t ^= t + Math.imul(t ^ (t >>> 7), t | 61);
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

export function createMockServices(options: MockOptions = {}): Services {
  const latency = options.latencyMs ?? 350;
  const fixtures = options.fixtures ?? createDefaultFixtures();
  const rng = mulberry32(options.seed ?? 20261102);

  const state = {
    playerName: fixtures.playerName,
    monsters: clone(fixtures.monsters),
    teamIds: [...fixtures.teamIds],
    encounters: clone(fixtures.encounters),
    history: clone(fixtures.history),
    seq: 0,
  };

  async function respond<T>(produce: () => T): Promise<T> {
    await sleep(latency > 0 ? latency + Math.random() * latency * 0.4 : 0);
    return clone(produce());
  }

  const pick = <T,>(items: readonly T[]): T => items[Math.floor(rng() * items.length)];
  const nextId = (prefix: string) => `${prefix}-${++state.seq}`;

  function findMonster(id: string): Monster {
    const found = state.monsters.find((m) => m.id === id);
    if (!found) throw new ApiError(404, "MONSTER_NOT_FOUND", "This monster could not be found.");
    return found;
  }

  function findEncounter(id: string): Encounter {
    const found = state.encounters.find((e) => e.id === id);
    if (!found) throw new ApiError(404, "ENCOUNTER_NOT_FOUND", "This encounter could not be found.");
    return found;
  }

  /** Unlock rules: encounters unlock in order, the final boss once every other encounter is cleared. */
  function recomputeStatuses() {
    const sorted = [...state.encounters].sort((a, b) => a.order - b.order);
    const regular = sorted.filter((e) => !e.isFinalBoss);
    for (const e of sorted) {
      if (e.status === "cleared") continue;
      if (e.isFinalBoss) {
        e.status = regular.every((r) => r.status === "cleared") ? "available" : "locked";
      } else {
        const earlier = regular.filter((r) => r.order < e.order);
        e.status = earlier.every((r) => r.status === "cleared") ? "available" : "locked";
      }
    }
  }

  function applyXp(monster: Monster, gained: number): LevelUp | null {
    if (monster.level >= MAX_LEVEL || gained <= 0) return null;
    const from = monster.level;
    monster.xp += gained;
    while (monster.level < MAX_LEVEL && monster.xp >= monster.level * 100) {
      monster.xp -= monster.level * 100;
      monster.level += 1;
      monster.pendingPoints += 1;
    }
    if (monster.level >= MAX_LEVEL) {
      monster.xp = 0;
      monster.xpToNext = null;
    } else {
      monster.xpToNext = monster.level * 100;
    }
    return monster.level > from
      ? { monsterId: monster.id, name: monster.name, fromLevel: from, toLevel: monster.level, pointsGained: monster.level - from }
      : null;
  }

  function hatchMonster(monsterClass: MonsterClass): Monster {
    const nouns = MOCK_NAME_NOUNS[monsterClass];
    return {
      id: nextId("hatched"),
      name: `${pick(MOCK_NAME_ADJECTIVES)} ${pick(nouns)}`,
      monsterClass,
      skill: pick(MOCK_SKILLS[monsterClass]),
      trait: pick(MOCK_TRAITS),
      talent: MIN_TALENT + Math.floor(rng() * (MAX_TALENT - MIN_TALENT + 1)),
      level: 1,
      xp: 0,
      xpToNext: 100,
      pendingPoints: 0,
      stats: { ...CLASS_BASE_STATS[monsterClass] },
      upgrades: [],
      avatarVariant: Math.floor(rng() * 3),
      obtainedAt: new Date().toISOString(),
    };
  }

  const monsterPower = (m: Monster) => m.stats.strength * 2 + m.stats.hp / 10 + m.stats.accuracy / 20 + m.stats.agility / 20;

  function simulate(encounter: Encounter, team: Monster[]): BattleResult {
    const teamPower = team.reduce((sum, m) => sum + monsterPower(m), 0);
    const enemyPower = encounter.opponents.reduce((sum, o) => sum + (8 + o.level * 5) * (encounter.isFinalBoss ? 3 : 1), 0);
    const outcome = teamPower * (0.8 + rng() * 0.4) >= enemyPower * 0.8 ? "victory" : "defeat";
    const rounds = 3 + Math.floor(rng() * 4);

    const events: BattleEvent[] = [];
    const push = (round: number, side: BattleEvent["side"], kind: BattleEvent["kind"], message: string) =>
      events.push({ id: nextId("ev"), round, side, kind, message });

    for (let round = 1; round <= rounds; round++) {
      const actor = team[(round - 1) % team.length];
      const target = encounter.opponents[(round - 1) % encounter.opponents.length];
      push(round, "player", "damage", `${actor.name} uses ${actor.skill.name} on ${target.name} for ${Math.round(actor.stats.strength * 1.5 + rng() * 6)} damage.`);
      const attacker = encounter.opponents[round % encounter.opponents.length];
      const victim = team[round % team.length];
      push(round, "enemy", "damage", `${attacker.name} hits ${victim.name} for ${Math.round(4 + attacker.level * 2 + rng() * 5)} damage.`);
    }
    if (outcome === "victory") {
      push(rounds, "enemy", "defeat", encounter.isFinalBoss ? "The final boss is defeated!" : "All opponents are defeated.");
    } else {
      push(rounds, "player", "defeat", "Your team is defeated.");
    }

    const participants = team.map((m) => ({ monsterId: m.id, name: m.name, monsterClass: m.monsterClass, level: m.level }));
    const base = 30 + encounter.order * 10;
    const factor = outcome === "victory" ? 1 : 0.25;
    const levelUps: LevelUp[] = [];
    const xp = team.map((m) => {
      const live = findMonster(m.id);
      const gained = live.level >= MAX_LEVEL ? 0 : Math.round((base * factor * live.talent) / 100);
      const levelUp = applyXp(live, gained);
      if (levelUp) levelUps.push(levelUp);
      return { monsterId: live.id, name: live.name, xpGained: gained };
    });

    let egg: Monster | null = null;
    if (outcome === "victory" && !encounter.isFinalBoss && encounter.rewardClasses.length > 0 && rng() * 100 < encounter.eggChance) {
      egg = hatchMonster(pick(encounter.rewardClasses));
      state.monsters.push(clone(egg));
    }

    if (outcome === "victory") {
      encounter.status = "cleared";
      recomputeStatuses();
    }

    return {
      id: nextId("battle"),
      encounterId: encounter.id,
      encounterName: encounter.name,
      isFinalBoss: encounter.isFinalBoss,
      playedAt: new Date().toISOString(),
      outcome,
      rounds,
      participants,
      rewards: { xp, levelUps, egg, finalBossDefeated: outcome === "victory" && encounter.isFinalBoss },
      events,
    };
  }

  const services: Services = {
    players: {
      getProgress: () =>
        respond(() => {
          const boss = state.encounters.find((e) => e.isFinalBoss);
          return {
            playerName: state.playerName,
            collectionSize: state.monsters.length,
            teamSize: state.teamIds.length,
            encountersCleared: state.encounters.filter((e) => e.status === "cleared").length,
            encountersTotal: state.encounters.length,
            pendingPoints: state.monsters.reduce((sum, m) => sum + m.pendingPoints, 0),
            finalBossDefeated: boss?.status === "cleared",
            wins: state.history.filter((b) => b.outcome === "victory").length,
            losses: state.history.filter((b) => b.outcome === "defeat").length,
          };
        }),
    },
    monsters: {
      listMonsters: () => respond(() => state.monsters),
      listSpecies: () => respond(() => [...MOCK_SPECIES].sort((a, b) => a.name.localeCompare(b.name))),
      getMonster: (id) => respond(() => findMonster(id)),
      spendStatPoint: (id, stat) =>
        respond(() => {
          const monster = findMonster(id);
          if (monster.pendingPoints <= 0) {
            throw new ApiError(409, "NO_STAT_POINTS", "This monster has no unspent stat points.");
          }
          monster.pendingPoints -= 1;
          monster.stats[stat] += MOCK_STAT_INCREMENT[stat];
          monster.upgrades.push({
            id: nextId("upgrade"),
            stat,
            newValue: monster.stats[stat],
            levelAtUpgrade: monster.level,
            at: new Date().toISOString(),
          });
          return monster;
        }),
    },
    teams: {
      getTeam: () => respond(() => ({ monsterIds: state.teamIds.filter((id) => state.monsters.some((m) => m.id === id)) })),
      saveTeam: (monsterIds) =>
        respond(() => {
          const check = validateTeam(monsterIds, new Set(state.monsters.map((m) => m.id)));
          if (!check.valid) throw new ApiError(422, "INVALID_TEAM", check.errors.join(" "));
          state.teamIds = [...monsterIds];
          return { monsterIds: [...state.teamIds] };
        }),
    },
    encounters: {
      listEncounters: () => respond(() => [...state.encounters].sort((a, b) => a.order - b.order)),
      getEncounter: (id) => respond(() => findEncounter(id)),
    },
    battles: {
      startBattle: (encounterId, monsterIds) =>
        respond(() => {
          const encounter = findEncounter(encounterId);
          if (encounter.status === "locked") {
            throw new ApiError(409, "ENCOUNTER_LOCKED", "This encounter is still locked.");
          }
          const check = validateTeam(monsterIds, new Set(state.monsters.map((m) => m.id)));
          if (!check.valid) throw new ApiError(422, "INVALID_TEAM", check.errors.join(" "));
          const result = simulate(encounter, monsterIds.map(findMonster));
          state.history.push(clone(result));
          return result;
        }),
    },
    history: {
      listBattles: () =>
        respond(() =>
          [...state.history]
            .sort((a, b) => b.playedAt.localeCompare(a.playedAt))
            .map((b) => ({
              id: b.id,
              encounterId: b.encounterId,
              encounterName: b.encounterName,
              playedAt: b.playedAt,
              outcome: b.outcome,
              rounds: b.rounds,
              participantNames: b.participants.map((p) => p.name),
              eggHatched: b.rewards.egg !== null,
            })),
        ),
      getBattle: (id) =>
        respond(() => {
          const found = state.history.find((b) => b.id === id);
          if (!found) throw new ApiError(404, "BATTLE_NOT_FOUND", "This battle could not be found.");
          return found;
        }),
    },
  };

  return services;
}
