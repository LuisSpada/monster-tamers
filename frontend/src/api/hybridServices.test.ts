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

const backendMonster = {
  id: "44ed4dd1-6abd-4991-97cc-86bf95429a10",
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
    description: "Keeps calm under pressure.",
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

const firstMonsterId =
  "44ed4dd1-6abd-4991-97cc-86bf95429a10";

const secondMonsterId =
  "729827c5-0848-4898-bd4a-959a6d49fb92";

beforeEach(() => {
  sessionStorage.clear();
  vi.restoreAllMocks();
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("createHybridServices", () => {
  it("loads the monster collection from the backend", async () => {
    setAccessToken("monster-list-token");

    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify([backendMonster]),
        {
          status: 200,
          headers: {
            "Content-Type": "application/json",
          },
        },
      ),
    );

    vi.stubGlobal("fetch", fetchMock);

    const services = createHybridServices(
      "http://localhost:8080",
      {
        latencyMs: 0,
      },
    );

    const monsters =
      await services.monsters.listMonsters();

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/monsters",
      expect.objectContaining({
        method: "GET",
        credentials: "include",
        headers: expect.objectContaining({
          Accept: "application/json",
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
    setAccessToken("team-read-token");

    const fetchMock = vi.fn().mockResolvedValue(
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
            "Content-Type": "application/json",
          },
        },
      ),
    );

    vi.stubGlobal("fetch", fetchMock);

    const services = createHybridServices(
      "http://localhost:8080",
      {
        latencyMs: 0,
      },
    );

    const team = await services.teams.getTeam();

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/monsters/team",
      expect.objectContaining({
        method: "GET",
        credentials: "include",
        headers: expect.objectContaining({
          Authorization:
            "Bearer team-read-token",
        }),
      }),
    );

    expect(team.monsterIds).toEqual([
      secondMonsterId,
      firstMonsterId,
    ]);
  });

  it("saves the ordered team through the backend", async () => {
    setAccessToken("team-save-token");

    const requestedIds = [
      secondMonsterId,
      firstMonsterId,
    ];

    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          monsterIds: requestedIds,
        }),
        {
          status: 200,
          headers: {
            "Content-Type": "application/json",
          },
        },
      ),
    );

    vi.stubGlobal("fetch", fetchMock);

    const services = createHybridServices(
      "http://localhost:8080",
      {
        latencyMs: 0,
      },
    );

    const saved =
      await services.teams.saveTeam(
        requestedIds,
      );

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/monsters/team",
      expect.objectContaining({
        method: "PUT",
        credentials: "include",
        headers: expect.objectContaining({
          Accept: "application/json",
          "Content-Type": "application/json",
          Authorization:
            "Bearer team-save-token",
        }),
        body: JSON.stringify({
          monsterIds: requestedIds,
        }),
      }),
    );

    expect(saved.monsterIds).toEqual(
      requestedIds,
    );
  });

  it("keeps monster details mock-backed", async () => {
    const fetchMock = vi.fn();

    vi.stubGlobal("fetch", fetchMock);

    const services = createHybridServices(
      "http://localhost:8080",
      {
        latencyMs: 0,
      },
    );

    const monster =
      await services.monsters.getMonster("m1");

    expect(monster.name).toBe("Fierce Fox");
    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("keeps stat-point spending mock-backed", async () => {
    const fetchMock = vi.fn();

    vi.stubGlobal("fetch", fetchMock);

    const services = createHybridServices(
      "http://localhost:8080",
      {
        latencyMs: 0,
      },
    );

    const before =
      await services.monsters.getMonster("m1");

    const after =
      await services.monsters.spendStatPoint(
        "m1",
        "strength",
      );

    expect(after.pendingPoints).toBe(
      before.pendingPoints - 1,
    );

    expect(after.stats.strength).toBeGreaterThan(
      before.stats.strength,
    );

    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("keeps the remaining domains mock-backed", async () => {
    const fetchMock = vi.fn();

    vi.stubGlobal("fetch", fetchMock);

    const services = createHybridServices(
      "http://localhost:8080",
      {
        latencyMs: 0,
      },
    );

    await services.players.getProgress();
    await services.encounters.listEncounters();
    await services.history.listBattles();

    expect(fetchMock).not.toHaveBeenCalled();
  });
});