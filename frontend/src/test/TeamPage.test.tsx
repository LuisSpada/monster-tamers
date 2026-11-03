import { describe, expect, it } from "vitest";
import { fireEvent, screen } from "@testing-library/react";
import { TeamPage } from "../pages/TeamPage";
import { renderPage } from "./gameTestUtils";

const render = () => renderPage(<TeamPage />, { route: "/team", path: "/team" });

describe("TeamPage", () => {
  it("shows the current team in order with a valid state", async () => {
    render();
    expect(await screen.findByText("Valid team")).toBeTruthy();
    const slots = screen.getByRole("list", { name: "Team slots" });
    expect(slots.textContent).toContain("Fierce Fox");
    expect(slots.textContent).toContain("Empty slot");
  });

  it("disables saving until something changes, then saves", async () => {
    render();
    const save = (await screen.findByRole("button", { name: "Save team" })) as HTMLButtonElement;
    expect(save.disabled).toBe(true);
    fireEvent.click(screen.getByRole("button", { name: "Add Sturdy Cub to team" }));
    expect(save.disabled).toBe(false);
    fireEvent.click(save);
    expect(await screen.findByText("Team saved.")).toBeTruthy();
    expect((screen.getByRole("button", { name: "Save team" }) as HTMLButtonElement).disabled).toBe(true);
  });

  it("does not allow more than three monsters", async () => {
    render();
    fireEvent.click(await screen.findByRole("button", { name: "Add Sturdy Cub to team" }));
    expect(screen.getByText(/team is full/i)).toBeTruthy();
    expect((screen.getByRole("button", { name: "Add Sly Raccoon to team" }) as HTMLButtonElement).disabled).toBe(true);
  });

  it("blocks saving an empty team and explains why", async () => {
    render();
    fireEvent.click(await screen.findByRole("button", { name: "Remove Fierce Fox from team" }));
    fireEvent.click(screen.getByRole("button", { name: "Remove Calm Drake from team" }));
    expect(screen.getByRole("alert").textContent).toMatch(/at least 1 monster/i);
    expect((screen.getByRole("button", { name: "Save team" }) as HTMLButtonElement).disabled).toBe(true);
  });

  it("reorders the team and can reset the change", async () => {
    render();
    fireEvent.click(await screen.findByRole("button", { name: "Move Fierce Fox down" }));
    const slots = screen.getByRole("list", { name: "Team slots" });
    expect(slots.textContent?.indexOf("Calm Drake")).toBeLessThan(slots.textContent?.indexOf("Fierce Fox") ?? 0);
    fireEvent.click(screen.getByRole("button", { name: "Reset changes" }));
    expect(screen.getByRole("list", { name: "Team slots" }).textContent?.indexOf("Fierce Fox")).toBeLessThan(
      screen.getByRole("list", { name: "Team slots" }).textContent?.indexOf("Calm Drake") ?? 0,
    );
  });
});
