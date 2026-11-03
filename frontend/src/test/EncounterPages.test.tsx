import { describe, expect, it } from "vitest";
import { screen } from "@testing-library/react";
import { createDefaultFixtures } from "../api/mock/fixtures";
import { EncounterDetailPage } from "../pages/EncounterDetailPage";
import { EncounterListPage } from "../pages/EncounterListPage";
import { createTestServices, renderPage } from "./gameTestUtils";

const detail = (id: string) => renderPage(<EncounterDetailPage />, { route: `/encounters/${id}`, path: "/encounters/:encounterId" });

describe("Encounter list", () => {
  it("groups encounters into journey, training grounds and coming up", async () => {
    renderPage(<EncounterListPage />);
    expect(await screen.findByText("3 of 5 encounters cleared")).toBeTruthy();
    expect(screen.getByRole("heading", { name: "Continue your journey" })).toBeTruthy();
    expect(screen.getByRole("heading", { name: "Training grounds" })).toBeTruthy();
    expect(screen.getByRole("heading", { name: "Coming up" })).toBeTruthy();
    expect(screen.getByText("Final boss")).toBeTruthy();
    expect(screen.getByText("Locked")).toBeTruthy();
  });

  it("offers a replay link for every cleared encounter", async () => {
    renderPage(<EncounterListPage />);
    const replay = await screen.findByRole("link", { name: "Replay: Meadow Scouts" });
    expect(replay.getAttribute("href")).toBe("/encounters/e1/prepare");
    expect(screen.getAllByRole("link", { name: /^Replay:/ }).length).toBe(3);
  });

  it("features the next encounter with a start link", async () => {
    renderPage(<EncounterListPage />);
    const start = await screen.findByRole("link", { name: "Start battle: Misty Forest" });
    expect(start.getAttribute("href")).toBe("/encounters/e4/prepare");
    expect(screen.getByText("Next up")).toBeTruthy();
  });

  it("shows how often each encounter was won", async () => {
    renderPage(<EncounterListPage />);
    expect(await screen.findByText("Won 1× of 2")).toBeTruthy(); // Mossy Ruins: one defeat, one victory
    expect(screen.getByText("Won 0× of 1")).toBeTruthy(); // Misty Forest: one defeat
  });

  it("shows the training progress of the team", async () => {
    renderPage(<EncounterListPage />);
    expect(await screen.findByText("Team training")).toBeTruthy();
    expect(screen.getByText("190 XP to level 5")).toBeTruthy();
    expect(screen.getByText(/Replaying a cleared encounter earns experience/)).toBeTruthy();
  });

  it("tells the player there is nothing to replay before the first victory", async () => {
    const fixtures = createDefaultFixtures();
    fixtures.history = [];
    fixtures.encounters.forEach((e) => {
      e.status = e.order === 1 ? "available" : "locked";
    });
    renderPage(<EncounterListPage />, { services: createTestServices({ fixtures }) });
    expect(await screen.findByText("Nothing to replay yet")).toBeTruthy();
    expect(screen.getByRole("link", { name: "Start battle: Meadow Scouts" })).toBeTruthy();
  });
});

describe("Encounter details", () => {
  it("shows the egg chance, possible classes and the record of an open encounter", async () => {
    detail("e4");
    expect(await screen.findAllByText("Misty Forest")).toBeTruthy();
    expect(screen.getByText("35%")).toBeTruthy();
    expect(screen.getByText("Possible egg classes")).toBeTruthy();
    expect(screen.getByText(/last played/)).toBeTruthy();
    expect(screen.getByRole("link", { name: /Prepare for battle/ }).getAttribute("href")).toBe("/encounters/e4/prepare");
  });

  it("turns the action into a replay for a cleared encounter", async () => {
    detail("e1");
    const replay = await screen.findByRole("link", { name: /Replay encounter/ });
    expect(replay.getAttribute("href")).toBe("/encounters/e1/prepare");
    expect(screen.getByText(/Replay it to earn experience/)).toBeTruthy();
  });

  it("explains that the final boss gives no egg and disables the locked action", async () => {
    detail("e5");
    expect(await screen.findByText(/never drops an egg/i)).toBeTruthy();
    expect((screen.getByRole("button", { name: /Prepare for battle/ }) as HTMLButtonElement).disabled).toBe(true);
    expect(screen.getByText(/This encounter is locked/)).toBeTruthy();
  });

  it("shows an error for an unknown encounter", async () => {
    detail("zzz");
    expect(await screen.findByRole("alert")).toBeTruthy();
  });
});
