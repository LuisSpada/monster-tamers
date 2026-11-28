import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";
import { setAccessToken } from "./authSession";
import { createHybridServices } from "./hybridServices";

const baseUrl = "http://localhost:8080";

const firstMonsterId =
  "44ed4dd1-6abd-4991-97cc-86bf95429a10";

const secondMonsterId =
  "729827c5-0848-4898-bd4a-959a6d49fb92";

const firstEncounterId =
  "00000000-0000-0000-0000-000000000001";

const secondEncounterId =
  "00000000-0000-0000-0000-000000000002";

const backendMonster = {
  id: firstMonsterId,
  name: "Backend Fox",
  monsterClass: "Fighter" as const,
  avatarVariant: 0,
  obtainedAt: "2026-11-13T10:00:00Z",
  talent: 100,
  skill: {
    name: "Quick Strike",
    description:
      "A fast attack on the nearest enemy.",
  },
  trait: {
    name: "Steady",
    description:
      "Keeps calm under pressure.",
  },
  level: 1,
  xp: 0,
  xpToNext: 100,
  pendingPoints: 0,
  stats: {
    strength: 10,
    hp: 100,
    accuracy: 60,
    agility: 40,
  },
  upgrades: [],
};

const backendEncounter = {
  id: firstEncounterId,
  order: 1,
  name: "Meadow Scouts",
  description:
    "A gentle first encounter.",
  eggChance: 20,
  rewardClasses: [
    "Fighter",
    "Trickster",
  ] as const,
  isFinalBoss: false,
  status: "available" as const,
  opponents: [
    {
      id: "10000000-0000-0000-0000-000000000001",
      name: "Meadow Pup",
      monsterClass: "Fighter" as const,
      level: 1,
      avatarVariant: 0,
    },
  ],
};

const backendBattleResult = {
  id: "30000000-0000-0000-0000-000000000001",
  encounterId: firstEncounterId,
  encounterName: "Meadow Scouts",
  finalBoss: false,
  playedAt: "2026-11-14T12:00:00Z",
  outcome: "victory" as const,
  rounds: 3,
  participants: [
    {
      monsterId: secondMonsterId,
      name: "Stone Bear",
      monsterClass: "Guardian" as const,
      level: 2,
    },
    {
      monsterId: firstMonsterId,
      name: "Backend Fox",
      monsterClass: "Fighter" as const,
      level: 1,
    },
  ],
  rewards: {
    egg: null,
  },
  events: [
    {
      id: "event-1",
      round: 1,
      side: "player" as const,
      kind: "damage" as const,
      message:
        "Stone Bear dealt 10 damage.",
    },
    {
      id: "event-2",
      round: 3,
      side: "player" as const,
      kind: "info" as const,
      message:
        "Your team won the battle.",
    },
  ],
};

beforeEach(() => {
  sessionStorage.clear();
  vi.restoreAllMocks();
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("createHybridServices", () => {
  it("loads the monster collection from the backend", async () => {
    setAccessToken(
      "monster-list-token",
    );

    const fetchMock = vi
      .fn()
      .mockResolvedValue(
        new Response(
          JSON.stringify([
            backendMonster,
          ]),
          {
            status: 200,
            headers: {
              "Content-Type":
                "application/json",
            },
          },
        ),
      );

    vi.stubGlobal(
      "fetch",
      fetchMock,
    );

    const services =
      createHybridServices(
        baseUrl,
        {
          latencyMs: 0,
        },
      );

    const monsters =
      await services.monsters
        .listMonsters();

    expect(fetchMock)
      .toHaveBeenCalledWith(
        `${baseUrl}/api/monsters`,
        expect.objectContaining({
          method: "GET",
          credentials: "include",
          headers:
            expect.objectContaining({
              Accept:
                "application/json",
              Authorization:
                "Bearer monster-list-token",
            }),
        }),
      );

    expect(monsters).toEqual([
      backendMonster,
    ]);
  });

  it("loads the ordered team from the backend", async () => {
    setAccessToken(
      "team-read-token",
    );

    const fetchMock = vi
      .fn()
      .mockResolvedValue(
        new Response(
          JSON.stringify({
            monsterIds: [
              secondMonsterId,
              firstMonsterId,
            ],
          }),
          {
            status: 200,
            headers: {
              "Content-Type":
                "application/json",
            },
          },
        ),
      );

    vi.stubGlobal(
      "fetch",
      fetchMock,
    );

    const services =
      createHybridServices(
        baseUrl,
        {
          latencyMs: 0,
        },
      );

    const team =
      await services.teams.getTeam();

    expect(fetchMock)
      .toHaveBeenCalledWith(
        `${baseUrl}/api/monsters/team`,
        expect.objectContaining({
          method: "GET",
          credentials: "include",
          headers:
            expect.objectContaining({
              Accept:
                "application/json",
              Authorization:
                "Bearer team-read-token",
            }),
        }),
      );

    expect(
      team.monsterIds,
    ).toEqual([
      secondMonsterId,
      firstMonsterId,
    ]);
  });

  it("saves the ordered team through the backend", async () => {
    setAccessToken(
      "team-save-token",
    );

    const requestedIds = [
      secondMonsterId,
      firstMonsterId,
    ];

    const fetchMock = vi
      .fn()
      .mockResolvedValue(
        new Response(
          JSON.stringify({
            monsterIds:
              requestedIds,
          }),
          {
            status: 200,
            headers: {
              "Content-Type":
                "application/json",
            },
          },
        ),
      );

    vi.stubGlobal(
      "fetch",
      fetchMock,
    );

    const services =
      createHybridServices(
        baseUrl,
        {
          latencyMs: 0,
        },
      );

    const saved =
      await services.teams.saveTeam(
        requestedIds,
      );

    expect(fetchMock)
      .toHaveBeenCalledWith(
        `${baseUrl}/api/monsters/team`,
        expect.objectContaining({
          method: "PUT",
          credentials: "include",
          headers:
            expect.objectContaining({
              Accept:
                "application/json",
              "Content-Type":
                "application/json",
              Authorization:
                "Bearer team-save-token",
            }),
          body: JSON.stringify({
            monsterIds:
              requestedIds,
          }),
        }),
      );

    expect(
      saved.monsterIds,
    ).toEqual(requestedIds);
  });

  it("loads encounters from the backend", async () => {
    setAccessToken(
      "encounter-list-token",
    );

    const fetchMock = vi
      .fn()
      .mockResolvedValue(
        new Response(
          JSON.stringify([
            backendEncounter,
          ]),
          {
            status: 200,
            headers: {
              "Content-Type":
                "application/json",
            },
          },
        ),
      );

    vi.stubGlobal(
      "fetch",
      fetchMock,
    );

    const services =
      createHybridServices(
        baseUrl,
        {
          latencyMs: 0,
        },
      );

    const encounters =
      await services.encounters
        .listEncounters();

    expect(fetchMock)
      .toHaveBeenCalledWith(
        `${baseUrl}/api/encounters`,
        expect.objectContaining({
          method: "GET",
          credentials: "include",
          headers:
            expect.objectContaining({
              Accept:
                "application/json",
              Authorization:
                "Bearer encounter-list-token",
            }),
        }),
      );

    expect(encounters).toEqual([
      backendEncounter,
    ]);

    expect(
      encounters[0],
    ).toMatchObject({
      order: 1,
      status: "available",
      rewardClasses: [
        "Fighter",
        "Trickster",
      ],
    });
  });

  it("loads encounter details from the backend", async () => {
    setAccessToken(
      "encounter-detail-token",
    );

    const detailEncounter = {
      id: secondEncounterId,
      order: 2,
      name: "Old Quarry",
      description:
        "Tough defenders occupy the quarry.",
      eggChance: 25,
      rewardClasses: [
        "Guardian",
        "Fighter",
      ] as const,
      isFinalBoss: false,
      status: "locked" as const,
      opponents: [
        {
          id: "10000000-0000-0000-0000-000000000003",
          name: "Rock Cub",
          monsterClass:
            "Guardian" as const,
          level: 2,
          avatarVariant: 0,
        },
      ],
    };

    const fetchMock = vi
      .fn()
      .mockResolvedValue(
        new Response(
          JSON.stringify(
            detailEncounter,
          ),
          {
            status: 200,
            headers: {
              "Content-Type":
                "application/json",
            },
          },
        ),
      );

    vi.stubGlobal(
      "fetch",
      fetchMock,
    );

    const services =
      createHybridServices(
        baseUrl,
        {
          latencyMs: 0,
        },
      );

    const encounter =
      await services.encounters
        .getEncounter(
          secondEncounterId,
        );

    expect(fetchMock)
      .toHaveBeenCalledWith(
        `${baseUrl}/api/encounters/${secondEncounterId}`,
        expect.objectContaining({
          method: "GET",
          credentials: "include",
          headers:
            expect.objectContaining({
              Accept:
                "application/json",
              Authorization:
                "Bearer encounter-detail-token",
            }),
        }),
      );

    expect(encounter).toEqual(
      detailEncounter,
    );

    expect(
      encounter.opponents,
    ).toEqual([
      expect.objectContaining({
        name: "Rock Cub",
        monsterClass:
          "Guardian",
        level: 2,
      }),
    ]);
  });

  it("keeps monster details mock-backed", async () => {
    const fetchMock = vi.fn();

    vi.stubGlobal(
      "fetch",
      fetchMock,
    );

    const services =
      createHybridServices(
        baseUrl,
        {
          latencyMs: 0,
        },
      );

    const monster =
      await services.monsters
        .getMonster("m1");

    expect(monster.name).toBe(
      "Fierce Fox",
    );

    expect(fetchMock)
      .not.toHaveBeenCalled();
  });

  it("keeps stat-point spending mock-backed", async () => {
    const fetchMock = vi.fn();

    vi.stubGlobal(
      "fetch",
      fetchMock,
    );

    const services =
      createHybridServices(
        baseUrl,
        {
          latencyMs: 0,
        },
      );

    const before =
      await services.monsters
        .getMonster("m1");

    const after =
      await services.monsters
        .spendStatPoint(
          "m1",
          "strength",
        );

    expect(
      after.pendingPoints,
    ).toBe(
      before.pendingPoints - 1,
    );

    expect(
      after.stats.strength,
    ).toBeGreaterThan(
      before.stats.strength,
    );

    expect(fetchMock)
      .not.toHaveBeenCalled();
  });

  it("starts battles through the backend and preserves team order", async () => {
    setAccessToken(
      "battle-token",
    );

    const requestedIds = [
      secondMonsterId,
      firstMonsterId,
    ];

    const fetchMock = vi
      .fn()
      .mockResolvedValue(
        new Response(
          JSON.stringify(
            backendBattleResult,
          ),
          {
            status: 200,
            headers: {
              "Content-Type":
                "application/json",
            },
          },
        ),
      );

    vi.stubGlobal(
      "fetch",
      fetchMock,
    );

    const services =
      createHybridServices(
        baseUrl,
        {
          latencyMs: 0,
        },
      );

    const result =
      await services.battles
        .startBattle(
          firstEncounterId,
          requestedIds,
        );

    expect(fetchMock)
      .toHaveBeenCalledWith(
        `${baseUrl}/api/encounters/${firstEncounterId}/battles`,
        expect.objectContaining({
          method: "POST",
          credentials: "include",
          headers:
            expect.objectContaining({
              Accept:
                "application/json",
              "Content-Type":
                "application/json",
              Authorization:
                "Bearer battle-token",
            }),
          body: JSON.stringify({
            monsterIds:
              requestedIds,
          }),
        }),
      );

    expect(result).toMatchObject({
      id: backendBattleResult.id,
      encounterId:
        firstEncounterId,
      encounterName:
        "Meadow Scouts",
      isFinalBoss: false,
      outcome: "victory",
      rounds: 3,
    });

    expect(
      result.participants.map(
        (participant) =>
          participant.monsterId,
      ),
    ).toEqual(requestedIds);

    expect(
      result.rewards,
    ).toEqual({
      xp: [],
      levelUps: [],
      egg: null,
      finalBossDefeated: false,
    });
  });

  it("keeps players, monster details and stat spending mock-backed", async () => {
    const fetchMock = vi.fn();

    vi.stubGlobal(
      "fetch",
      fetchMock,
    );

    const services =
      createHybridServices(
        baseUrl,
        {
          latencyMs: 0,
        },
      );

    await services.players
      .getProgress();

    const monster =
      await services.monsters
        .getMonster("m1");

    const upgraded =
      await services.monsters
        .spendStatPoint(
          "m1",
          "strength",
        );

    expect(monster.name).toBe(
      "Fierce Fox",
    );

    expect(
      upgraded.stats.strength,
    ).toBeGreaterThan(
      monster.stats.strength,
    );

    expect(fetchMock)
      .not.toHaveBeenCalled();
  });
});