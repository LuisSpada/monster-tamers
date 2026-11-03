import { describe, expect, it } from "vitest";
import { screen } from "@testing-library/react";
import { EncounterDetailPage } from "../pages/EncounterDetailPage";
import { EncounterListPage } from "../pages/EncounterListPage";
import { renderPage } from "./gameTestUtils";

describe("Encounter pages", () => {
  it("lists the encounters with their status and the final boss", async () => {
    renderPage(<EncounterListPage />);
    expect(await screen.findByText("Meadow Scouts")).toBeTruthy();
    expect(screen.getByText("3 of 5 encounters cleared")).toBeTruthy();
    expect(screen.getByText("Final boss")).toBeTruthy();
    expect(screen.getByText("Locked")).toBeTruthy();
    expect(screen.getByRole("link", { name: /Misty Forest/ }).getAttribute("href")).toBe("/encounters/e4");
  });

  it("shows the egg chance and possible classes for a normal encounter", async () => {
    renderPage(<EncounterDetailPage />, { route: "/encounters/e4", path: "/encounters/:encounterId" });
    expect(await screen.findAllByText("Misty Forest")).toBeTruthy();
    expect(screen.getByText("35%")).toBeTruthy();
    expect(screen.getByText("Possible egg classes")).toBeTruthy();
    expect(screen.getByRole("link", { name: /Prepare for battle/ }).getAttribute("href")).toBe("/encounters/e4/prepare");
  });

  it("explains that the final boss gives no egg and disables the locked action", async () => {
    renderPage(<EncounterDetailPage />, { route: "/encounters/e5", path: "/encounters/:encounterId" });
    expect(await screen.findByText(/never drops an egg/i)).toBeTruthy();
    expect((screen.getByRole("button", { name: /Prepare for battle/ }) as HTMLButtonElement).disabled).toBe(true);
    expect(screen.getByText(/This encounter is locked/)).toBeTruthy();
  });

  it("shows an error for an unknown encounter", async () => {
    renderPage(<EncounterDetailPage />, { route: "/encounters/zzz", path: "/encounters/:encounterId" });
    expect(await screen.findByRole("alert")).toBeTruthy();
  });
});
