import { describe, expect, it } from "vitest";
import { ApiError } from "../api/errors";
import { createMockServices } from "../api/mock/mockServices";

const make = () => createMockServices({ latencyMs: 0 });

describe("mock services", () => {
  it("lists the collection and the team", async () => {
    const s = make();
    expect((await s.monsters.listMonsters()).length).toBe(8);
    expect((await s.teams.getTeam()).monsterIds).toEqual(["m1", "m2"]);
  });

  it("rejects an unknown monster with a 404 ApiError", async () => {
    await expect(make().monsters.getMonster("nope")).rejects.toMatchObject({ status: 404 });
  });

  it("spends a stat point and records the upgrade", async () => {
    const s = make();
    const before = await s.monsters.getMonster("m1");
    const after = await s.monsters.spendStatPoint("m1", "strength");
    expect(after.pendingPoints).toBe(before.pendingPoints - 1);
    expect(after.stats.strength).toBeGreaterThan(before.stats.strength);
    expect(after.upgrades.length).toBe(before.upgrades.length + 1);
  });

  it("refuses to spend points that do not exist", async () => {
    await expect(make().monsters.spendStatPoint("m3", "hp")).rejects.toBeInstanceOf(ApiError);
  });

  it("validates the team on save", async () => {
    const s = make();
    await expect(s.teams.saveTeam([])).rejects.toMatchObject({ status: 422 });
    await expect(s.teams.saveTeam(["m1", "m2", "m3", "m4"])).rejects.toMatchObject({ status: 422 });
    expect((await s.teams.saveTeam(["m2", "m1"])).monsterIds).toEqual(["m2", "m1"]);
  });

  it("keeps the final boss locked until the others are cleared", async () => {
    const s = make();
    const list = await s.encounters.listEncounters();
    expect(list.find((e) => e.isFinalBoss)?.status).toBe("locked");
    await expect(s.battles.startBattle("e5", ["m1"])).rejects.toMatchObject({ code: "ENCOUNTER_LOCKED" });
  });

  it("simulates a victory, stores it in the history and unlocks the final boss", async () => {
    const s = make();
    const result = await s.battles.startBattle("e4", ["m1", "m2"]);
    expect(result.outcome).toBe("victory");
    expect(result.rewards.xp.length).toBe(2);
    const history = await s.history.listBattles();
    expect(history[0].id).toBe(result.id);
    expect((await s.encounters.getEncounter("e5")).status).toBe("available");
    expect((await s.history.getBattle(result.id)).events.length).toBeGreaterThan(0);
  });

  it("simulates a defeat for a weak team without an egg", async () => {
    const s = make();
    const result = await s.battles.startBattle("e4", ["m5"]);
    expect(result.outcome).toBe("defeat");
    expect(result.rewards.egg).toBeNull();
    expect((await s.encounters.getEncounter("e4")).status).toBe("available");
  });

  it("lists the history newest first", async () => {
    const dates = (await make().history.listBattles()).map((b) => b.playedAt);
    expect(dates).toEqual([...dates].sort().reverse());
  });
});
