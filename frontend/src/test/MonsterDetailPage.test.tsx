import { describe, expect, it } from "vitest";
import { fireEvent, screen } from "@testing-library/react";
import { MonsterDetailPage } from "../pages/MonsterDetailPage";
import { renderPage } from "./gameTestUtils";

const render = (id: string) => renderPage(<MonsterDetailPage />, { route: `/collection/${id}`, path: "/collection/:monsterId" });

describe("MonsterDetailPage", () => {
  it("shows stats, skill, trait, talent and progression", async () => {
    render("m1");
    expect((await screen.findAllByText("Fierce Fox")).length).toBeGreaterThan(0);
    expect(screen.getByText("Vampiric Strike")).toBeTruthy();
    expect(screen.getByText("Hot-headed")).toBeTruthy();
    expect(screen.getByText("Strength")).toBeTruthy();
    expect(screen.getByText("210 / 400 XP")).toBeTruthy();
    expect(screen.getByText(/In team, slot #1/)).toBeTruthy();
  });

  it("spends a stat point and confirms it", async () => {
    render("m1");
    fireEvent.click(await screen.findByRole("button", { name: "Increase Strength" }));
    expect(await screen.findByText(/Strength increased to 17/)).toBeTruthy();
    expect(screen.queryByRole("button", { name: "Increase Strength" })).toBeNull();
    expect(screen.getByText(/No unspent stat points/)).toBeTruthy();
  });

  it("offers no level-up buttons without points", async () => {
    render("m3");
    await screen.findAllByText("Sturdy Cub");
    expect(screen.queryByRole("button", { name: /Increase/ })).toBeNull();
    expect(screen.getByText("No upgrades yet")).toBeTruthy();
  });

  it("shows max level instead of an XP bar", async () => {
    render("m6");
    await screen.findAllByText("Gentle Newt");
    expect(screen.getAllByText(/Max level/).length).toBeGreaterThan(0);
  });

  it("shows an error for an unknown monster", async () => {
    render("does-not-exist");
    expect(await screen.findByRole("alert")).toBeTruthy();
    expect(screen.getByText("This monster could not be found.")).toBeTruthy();
  });
});
