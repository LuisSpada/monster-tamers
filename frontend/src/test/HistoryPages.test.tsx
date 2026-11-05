import { describe, expect, it } from "vitest";
import { fireEvent, screen } from "@testing-library/react";
import { createDefaultFixtures } from "../api/mock/fixtures";
import { HistoryDetailPage } from "../pages/HistoryDetailPage";
import { HistoryPage } from "../pages/HistoryPage";
import { createTestServices, renderGameApp, renderPage } from "./gameTestUtils";

describe("History pages", () => {
  it("lists battles newest first", async () => {
    renderPage(<HistoryPage />);
    const links = await screen.findAllByRole("link", { name: /Victory|Defeat/ });
    expect(links[0].textContent).toContain("Misty Forest");
    expect(links.length).toBe(5);
  });

  it("filters by outcome", async () => {
    renderPage(<HistoryPage />);
    await screen.findAllByRole("link", { name: /Victory|Defeat/ });
    fireEvent.click(screen.getByRole("button", { name: "Defeats" }));
    expect(screen.getAllByRole("link", { name: /Defeat/ }).length).toBe(2);
    expect(screen.queryByRole("link", { name: /Victory/ })).toBeNull();
  });

  it("shows an empty state without battles", async () => {
    const fixtures = createDefaultFixtures();
    fixtures.history = [];
    renderPage(<HistoryPage />, { services: createTestServices({ fixtures }) });
    expect(await screen.findByText("No battles yet")).toBeTruthy();
    expect(screen.getByRole("link", { name: "Go to encounters" })).toBeTruthy();
  });

  it("opens the detail page from the list", async () => {
    renderGameApp("/battle-history");
    fireEvent.click((await screen.findAllByRole("link", { name: /Victory|Defeat/ }))[0]);
    expect(await screen.findByText("Battle log")).toBeTruthy();
    expect(screen.getByText("Defeat")).toBeTruthy();
  });

  it("shows the rewards and the log of a battle with an egg", async () => {
    renderPage(<HistoryDetailPage />, { route: "/battle-history/b4", path: "/battle-history/:battleId" });
    expect(await screen.findByText(/An egg hatched: Sturdy Cub/)).toBeTruthy();
    expect(screen.getByText("Round 1")).toBeTruthy();
  });

  it("shows an error for an unknown battle", async () => {
    renderPage(<HistoryDetailPage />, { route: "/battle-history/zzz", path: "/battle-history/:battleId" });
    expect(await screen.findByText("This battle could not be found.")).toBeTruthy();
  });
});
