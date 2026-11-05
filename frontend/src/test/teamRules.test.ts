import { describe, expect, it } from "vitest";
import { validateTeam } from "../domain/teamRules";

describe("validateTeam", () => {
  it.each([[["a"]], [["a", "b"]], [["a", "b", "c"]]])("accepts a team of 1-3 monsters: %j", (ids) => {
    expect(validateTeam(ids).valid).toBe(true);
  });

  it("rejects an empty team", () => {
    const result = validateTeam([]);
    expect(result.valid).toBe(false);
    expect(result.errors[0]).toMatch(/at least 1/i);
  });

  it("rejects more than three monsters", () => {
    expect(validateTeam(["a", "b", "c", "d"]).errors.join(" ")).toMatch(/at most 3/i);
  });

  it("rejects duplicates", () => {
    expect(validateTeam(["a", "a"]).errors.join(" ")).toMatch(/only appear once/i);
  });

  it("rejects monsters that are not owned", () => {
    expect(validateTeam(["a", "x"], new Set(["a"])).valid).toBe(false);
  });
});
