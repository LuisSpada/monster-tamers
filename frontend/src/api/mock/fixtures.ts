import {
  MAX_LEVEL,
  type BattleEvent,
  type BattleResult,
  type Encounter,
  type Monster,
  type MonsterSpecies,
  type MonsterClass,
  type NamedEffect,
  type Opponent,
  type StatUpgrade,
  type Stats,
} from "../../domain/types";

/**
 * Raw mock fixtures. Pure data, no service logic.
 * Skill/trait names and descriptions are placeholders until the backend provides the real catalogue.
 */
export interface MockFixtures {
  playerName: string;
  monsters: Monster[];
  teamIds: string[];
  encounters: Encounter[];
  history: BattleResult[];
}

export const CLASS_BASE_STATS: Record<MonsterClass, Stats> = {
  Fighter: { strength: 15, hp: 100, accuracy: 60, agility: 40 },
  Guardian: { strength: 9, hp: 140, accuracy: 55, agility: 30 },
  Shaman: { strength: 6, hp: 90, accuracy: 65, agility: 42 },
  Trickster: { strength: 8, hp: 85, accuracy: 70, agility: 60 },
};

export const MOCK_SKILLS: Record<MonsterClass, NamedEffect[]> = {
  Fighter: [
    { name: "Vampiric Strike", description: "A strong attack that restores a little health." },
    { name: "Cleaving Blow", description: "Hits the target with extra force." },
  ],
  Guardian: [
    { name: "Shield Wall", description: "Reduces damage taken by the team for a short time." },
    { name: "Taunt", description: "Draws the next enemy attacks towards itself." },
  ],
  Shaman: [
    { name: "Healing Aura", description: "Restores health to an ally." },
    { name: "Spirit Boost", description: "Strengthens the next attack of an ally." },
  ],
  Trickster: [
    { name: "Smoke Bomb", description: "Lowers the accuracy of an enemy." },
    { name: "Trip Up", description: "Makes an enemy lose its next action." },
  ],
};

export const MOCK_TRAITS: NamedEffect[] = [
  { name: "Hot-headed", description: "Hits harder while at full health." },
  { name: "Unshakable", description: "Takes slightly less damage." },
  { name: "Quick Learner", description: "Notices openings faster than others." },
  { name: "Lucky", description: "Sometimes dodges an attack completely." },
];

export const MOCK_NAME_ADJECTIVES = ["Fierce", "Calm", "Sturdy", "Sly", "Brave", "Gentle", "Wise", "Wily", "Bold", "Swift"];
export const MOCK_NAME_NOUNS: Record<MonsterClass, string[]> = {
  Fighter: ["Fox", "Lynx", "Boar"],
  Guardian: ["Cub", "Rhino", "Tortoise"],
  Shaman: ["Drake", "Owl", "Newt"],
  Trickster: ["Raccoon", "Weasel", "Gecko"],
};

interface MonsterSeed {
  id: string;
  name: string;
  monsterClass: MonsterClass;
  variant: number;
  level: number;
  xp: number;
  talent: number;
  pendingPoints: number;
  stats: Stats;
  skill: NamedEffect;
  trait: NamedEffect;
  upgrades?: StatUpgrade[];
  obtainedAt: string;
}

function monster(s: MonsterSeed): Monster {
  return {
    id: s.id,
    name: s.name,
    monsterClass: s.monsterClass,
    skill: s.skill,
    trait: s.trait,
    talent: s.talent,
    level: s.level,
    xp: s.xp,
    xpToNext: s.level >= MAX_LEVEL ? null : s.level * 100,
    pendingPoints: s.pendingPoints,
    stats: s.stats,
    upgrades: s.upgrades ?? [],
    avatarVariant: s.variant,
    obtainedAt: s.obtainedAt,
  };
}

const monsters: Monster[] = [
  monster({
    id: "m1", name: "Fierce Fox", monsterClass: "Fighter", variant: 0, level: 4, xp: 210, talent: 112, pendingPoints: 1,
    stats: { strength: 16, hp: 104, accuracy: 61, agility: 41 },
    skill: MOCK_SKILLS.Fighter[0], trait: MOCK_TRAITS[0], obtainedAt: "2026-10-20T09:00:00Z",
    upgrades: [
      { id: "u1", stat: "strength", newValue: 16, levelAtUpgrade: 2, at: "2026-10-24T18:10:00Z" },
      { id: "u2", stat: "hp", newValue: 104, levelAtUpgrade: 3, at: "2026-10-27T19:40:00Z" },
      { id: "u3", stat: "accuracy", newValue: 61, levelAtUpgrade: 3, at: "2026-10-28T08:15:00Z" },
    ],
  }),
  monster({
    id: "m2", name: "Calm Drake", monsterClass: "Shaman", variant: 0, level: 3, xp: 140, talent: 96, pendingPoints: 1,
    stats: { strength: 6, hp: 92, accuracy: 66, agility: 42 },
    skill: MOCK_SKILLS.Shaman[0], trait: MOCK_TRAITS[1], obtainedAt: "2026-10-21T12:30:00Z",
  }),
  monster({
    id: "m3", name: "Sturdy Cub", monsterClass: "Guardian", variant: 0, level: 2, xp: 60, talent: 104, pendingPoints: 0,
    stats: { strength: 9, hp: 140, accuracy: 55, agility: 30 },
    skill: MOCK_SKILLS.Guardian[0], trait: MOCK_TRAITS[2], obtainedAt: "2026-10-29T16:20:00Z",
  }),
  monster({
    id: "m4", name: "Sly Raccoon", monsterClass: "Trickster", variant: 0, level: 2, xp: 95, talent: 88, pendingPoints: 0,
    stats: { strength: 8, hp: 85, accuracy: 70, agility: 60 },
    skill: MOCK_SKILLS.Trickster[0], trait: MOCK_TRAITS[3], obtainedAt: "2026-10-22T10:00:00Z",
  }),
  monster({
    id: "m5", name: "Brave Lynx", monsterClass: "Fighter", variant: 1, level: 1, xp: 30, talent: 119, pendingPoints: 0,
    stats: { strength: 12, hp: 90, accuracy: 55, agility: 35 },
    skill: MOCK_SKILLS.Fighter[1], trait: MOCK_TRAITS[0], obtainedAt: "2026-10-30T11:00:00Z",
  }),
  monster({
    id: "m6", name: "Gentle Newt", monsterClass: "Shaman", variant: 2, level: 10, xp: 0, talent: 101, pendingPoints: 0,
    stats: { strength: 11, hp: 150, accuracy: 80, agility: 50 },
    skill: MOCK_SKILLS.Shaman[1], trait: MOCK_TRAITS[1], obtainedAt: "2026-10-18T08:00:00Z",
  }),
  monster({
    id: "m7", name: "Wise Owl", monsterClass: "Guardian", variant: 1, level: 1, xp: 10, talent: 93, pendingPoints: 0,
    stats: { strength: 9, hp: 140, accuracy: 55, agility: 30 },
    skill: MOCK_SKILLS.Guardian[1], trait: MOCK_TRAITS[2], obtainedAt: "2026-10-31T14:00:00Z",
  }),
  monster({
    id: "m8", name: "Wily Weasel", monsterClass: "Trickster", variant: 1, level: 3, xp: 180, talent: 107, pendingPoints: 0,
    stats: { strength: 9, hp: 88, accuracy: 73, agility: 63 },
    skill: MOCK_SKILLS.Trickster[1], trait: MOCK_TRAITS[3], obtainedAt: "2026-10-23T13:45:00Z",
  }),
];

const opp = (id: string, name: string, monsterClass: MonsterClass, level: number, avatarVariant = 0): Opponent => ({
  id, name, monsterClass, level, avatarVariant,
});

const encounters: Encounter[] = [
  {
    id: "e1", order: 1, name: "Meadow Scouts", status: "cleared", isFinalBoss: false, eggChance: 20,
    description: "A pack of curious scouts patrols the sunny meadow. A gentle first test for new tamers.",
    rewardClasses: ["Fighter", "Trickster"],
    opponents: [opp("o1", "Meadow Pup", "Fighter", 1), opp("o2", "Field Mouse", "Trickster", 1)],
  },
  {
    id: "e2", order: 2, name: "Old Quarry", status: "cleared", isFinalBoss: false, eggChance: 25,
    description: "Echoing stone walls hide tough defenders who have claimed the quarry as their home.",
    rewardClasses: ["Guardian", "Fighter"],
    opponents: [opp("o3", "Rock Cub", "Guardian", 2), opp("o4", "Quarry Boar", "Fighter", 2, 2)],
  },
  {
    id: "e3", order: 3, name: "Mossy Ruins", status: "cleared", isFinalBoss: false, eggChance: 30,
    description: "Ancient ruins overgrown with moss. Spirits and tricksters lurk between the broken pillars.",
    rewardClasses: ["Shaman", "Trickster"],
    opponents: [opp("o5", "Ruin Wisp", "Shaman", 3), opp("o6", "Moss Gecko", "Trickster", 3, 2), opp("o7", "Stone Newt", "Guardian", 3, 2)],
  },
  {
    id: "e4", order: 4, name: "Misty Forest", status: "available", isFinalBoss: false, eggChance: 35,
    description: "A thick fog hangs between the trees. Travellers say the whole forest whispers at night.",
    rewardClasses: ["Fighter", "Guardian", "Shaman"],
    opponents: [opp("o8", "Fog Wolf", "Fighter", 3, 1), opp("o9", "Forest Scout", "Trickster", 3, 1), opp("o10", "Mist Owl", "Shaman", 4, 1)],
  },
  {
    id: "e5", order: 5, name: "Frostpeak Summit", status: "locked", isFinalBoss: true, eggChance: 0,
    description: "At the top of the frozen mountain waits the final boss. Defeat it to complete your journey.",
    rewardClasses: [],
    opponents: [opp("o11", "Frost Titan", "Guardian", 10, 0)],
  },
];

const sp = (
  id: string,
  name: string,
  monsterClass: MonsterClass,
  avatarVariant: number,
  baseStats: Stats,
  skill: NamedEffect,
  trait: NamedEffect,
): MonsterSpecies => ({ id, name, monsterClass, avatarVariant, baseStats, skill, trait });

export const MOCK_SPECIES: MonsterSpecies[] = [
  sp("20000000-0000-0000-0000-000000000001", "Fierce Fox", "Fighter", 0, { strength: 15, hp: 100, accuracy: 62, agility: 42 }, MOCK_SKILLS.Fighter[0], MOCK_TRAITS[0]),
  sp("20000000-0000-0000-0000-000000000002", "Calm Drake", "Shaman", 0, { strength: 6, hp: 92, accuracy: 65, agility: 42 }, MOCK_SKILLS.Shaman[0], MOCK_TRAITS[1]),
  sp("20000000-0000-0000-0000-000000000003", "Sturdy Cub", "Guardian", 0, { strength: 9, hp: 140, accuracy: 55, agility: 30 }, MOCK_SKILLS.Guardian[0], MOCK_TRAITS[2]),
  sp("20000000-0000-0000-0000-000000000004", "Sly Raccoon", "Trickster", 0, { strength: 8, hp: 85, accuracy: 70, agility: 60 }, MOCK_SKILLS.Trickster[0], MOCK_TRAITS[3]),
  sp("20000000-0000-0000-0000-000000000005", "Brave Lynx", "Fighter", 1, { strength: 16, hp: 95, accuracy: 60, agility: 44 }, MOCK_SKILLS.Fighter[1], MOCK_TRAITS[0]),
  sp("20000000-0000-0000-0000-000000000006", "Gentle Newt", "Shaman", 2, { strength: 6, hp: 95, accuracy: 66, agility: 40 }, MOCK_SKILLS.Shaman[1], MOCK_TRAITS[1]),
  sp("20000000-0000-0000-0000-000000000007", "Wise Owl", "Guardian", 1, { strength: 8, hp: 135, accuracy: 60, agility: 32 }, MOCK_SKILLS.Guardian[1], MOCK_TRAITS[2]),
  sp("20000000-0000-0000-0000-000000000008", "Wily Weasel", "Trickster", 1, { strength: 9, hp: 82, accuracy: 72, agility: 62 }, MOCK_SKILLS.Trickster[1], MOCK_TRAITS[3]),
  sp("20000000-0000-0000-0000-000000000009", "Meadow Pup", "Fighter", 0, { strength: 13, hp: 92, accuracy: 58, agility: 40 }, MOCK_SKILLS.Fighter[1], MOCK_TRAITS[0]),
  sp("20000000-0000-0000-0000-000000000010", "Field Mouse", "Trickster", 0, { strength: 7, hp: 80, accuracy: 68, agility: 64 }, MOCK_SKILLS.Trickster[1], MOCK_TRAITS[3]),
  sp("20000000-0000-0000-0000-000000000011", "Rock Cub", "Guardian", 0, { strength: 9, hp: 145, accuracy: 54, agility: 28 }, MOCK_SKILLS.Guardian[0], MOCK_TRAITS[1]),
  sp("20000000-0000-0000-0000-000000000012", "Quarry Boar", "Fighter", 2, { strength: 17, hp: 108, accuracy: 56, agility: 36 }, MOCK_SKILLS.Fighter[1], MOCK_TRAITS[1]),
  sp("20000000-0000-0000-0000-000000000013", "Ruin Wisp", "Shaman", 0, { strength: 7, hp: 86, accuracy: 68, agility: 45 }, MOCK_SKILLS.Shaman[1], MOCK_TRAITS[2]),
  sp("20000000-0000-0000-0000-000000000014", "Moss Gecko", "Trickster", 2, { strength: 8, hp: 86, accuracy: 71, agility: 61 }, MOCK_SKILLS.Trickster[1], MOCK_TRAITS[3]),
  sp("20000000-0000-0000-0000-000000000015", "Stone Newt", "Guardian", 2, { strength: 8, hp: 142, accuracy: 56, agility: 30 }, MOCK_SKILLS.Guardian[1], MOCK_TRAITS[1]),
  sp("20000000-0000-0000-0000-000000000016", "Fog Wolf", "Fighter", 1, { strength: 16, hp: 102, accuracy: 62, agility: 43 }, MOCK_SKILLS.Fighter[0], MOCK_TRAITS[0]),
  sp("20000000-0000-0000-0000-000000000017", "Forest Scout", "Trickster", 1, { strength: 9, hp: 84, accuracy: 73, agility: 63 }, MOCK_SKILLS.Trickster[0], MOCK_TRAITS[2]),
  sp("20000000-0000-0000-0000-000000000018", "Mist Owl", "Shaman", 1, { strength: 6, hp: 94, accuracy: 67, agility: 43 }, MOCK_SKILLS.Shaman[0], MOCK_TRAITS[3]),
  sp("20000000-0000-0000-0000-000000000019", "Frost Titan", "Guardian", 0, { strength: 12, hp: 175, accuracy: 58, agility: 26 }, MOCK_SKILLS.Guardian[0], MOCK_TRAITS[1]),
];

let eventSeq = 0;
const ev = (round: number, side: BattleEvent["side"], kind: BattleEvent["kind"], message: string): BattleEvent => ({
  id: `fx-ev-${++eventSeq}`, round, side, kind, message,
});

function history(): BattleResult[] {
  const [fox, drake, cub, raccoon] = monsters;
  const part = (m: Monster, level = m.level) => ({ monsterId: m.id, name: m.name, monsterClass: m.monsterClass, level });
  return [
    {
      id: "b1", encounterId: "e1", encounterName: "Meadow Scouts", isFinalBoss: false, playedAt: "2026-10-26T17:05:00Z",
      outcome: "victory", rounds: 3, participants: [part(fox, 2)],
      rewards: { xp: [{ monsterId: "m1", name: "Fierce Fox", xpGained: 55 }], levelUps: [], egg: null, finalBossDefeated: false },
      events: [
        ev(1, "player", "damage", "Fierce Fox uses Vampiric Strike on Meadow Pup for 14 damage."),
        ev(1, "enemy", "damage", "Meadow Pup bites Fierce Fox for 6 damage."),
        ev(2, "player", "damage", "Fierce Fox hits Field Mouse for 12 damage."),
        ev(3, "enemy", "defeat", "Meadow Pup and Field Mouse are defeated."),
      ],
    },
    {
      id: "b2", encounterId: "e2", encounterName: "Old Quarry", isFinalBoss: false, playedAt: "2026-10-27T19:40:00Z",
      outcome: "victory", rounds: 4, participants: [part(fox, 3), part(drake, 2)],
      rewards: {
        xp: [{ monsterId: "m1", name: "Fierce Fox", xpGained: 67 }, { monsterId: "m2", name: "Calm Drake", xpGained: 58 }],
        levelUps: [{ monsterId: "m1", name: "Fierce Fox", fromLevel: 2, toLevel: 3, pointsGained: 1 }],
        egg: null, finalBossDefeated: false,
      },
      events: [
        ev(1, "player", "heal", "Calm Drake uses Healing Aura on Fierce Fox for 9 health."),
        ev(1, "enemy", "damage", "Rock Cub rams Fierce Fox for 8 damage."),
        ev(2, "player", "damage", "Fierce Fox cleaves Quarry Boar for 18 damage."),
        ev(4, "enemy", "defeat", "Rock Cub and Quarry Boar are defeated."),
      ],
    },
    {
      id: "b3", encounterId: "e3", encounterName: "Mossy Ruins", isFinalBoss: false, playedAt: "2026-10-28T20:10:00Z",
      outcome: "defeat", rounds: 5, participants: [part(drake, 3)],
      rewards: { xp: [{ monsterId: "m2", name: "Calm Drake", xpGained: 14 }], levelUps: [], egg: null, finalBossDefeated: false },
      events: [
        ev(1, "enemy", "control", "Moss Gecko trips Calm Drake. Calm Drake loses its action."),
        ev(2, "enemy", "damage", "Ruin Wisp hits Calm Drake for 11 damage."),
        ev(5, "player", "defeat", "Calm Drake is defeated."),
      ],
    },
    {
      id: "b4", encounterId: "e3", encounterName: "Mossy Ruins", isFinalBoss: false, playedAt: "2026-10-29T16:20:00Z",
      outcome: "victory", rounds: 4, participants: [part(fox, 3), part(drake, 3), part(raccoon, 2)],
      rewards: {
        xp: [
          { monsterId: "m1", name: "Fierce Fox", xpGained: 70 },
          { monsterId: "m2", name: "Calm Drake", xpGained: 62 },
          { monsterId: "m4", name: "Sly Raccoon", xpGained: 57 },
        ],
        levelUps: [], egg: cub, finalBossDefeated: false,
      },
      events: [
        ev(1, "player", "control", "Sly Raccoon throws a Smoke Bomb. Ruin Wisp loses accuracy."),
        ev(2, "player", "damage", "Fierce Fox hits Moss Gecko for 17 damage."),
        ev(3, "player", "heal", "Calm Drake uses Healing Aura on Sly Raccoon for 10 health."),
        ev(4, "enemy", "defeat", "All opponents are defeated. An egg hatches: Sturdy Cub!"),
      ],
    },
    {
      id: "b5", encounterId: "e4", encounterName: "Misty Forest", isFinalBoss: false, playedAt: "2026-11-01T18:30:00Z",
      outcome: "defeat", rounds: 5, participants: [part(fox, 4), part(drake, 3)],
      rewards: {
        xp: [{ monsterId: "m1", name: "Fierce Fox", xpGained: 15 }, { monsterId: "m2", name: "Calm Drake", xpGained: 13 }],
        levelUps: [], egg: null, finalBossDefeated: false,
      },
      events: [
        ev(1, "enemy", "damage", "Fog Wolf bites Fierce Fox for 15 damage."),
        ev(2, "enemy", "control", "Forest Scout trips Calm Drake."),
        ev(3, "player", "damage", "Fierce Fox hits Mist Owl for 16 damage."),
        ev(5, "player", "defeat", "Fierce Fox and Calm Drake are defeated."),
      ],
    },
  ];
}

export function createDefaultFixtures(): MockFixtures {
  return {
    playerName: "Tamer",
    monsters: JSON.parse(JSON.stringify(monsters)) as Monster[],
    teamIds: ["m1", "m2"],
    encounters: JSON.parse(JSON.stringify(encounters)) as Encounter[],
    history: JSON.parse(JSON.stringify(history())) as BattleResult[],
  };
}
