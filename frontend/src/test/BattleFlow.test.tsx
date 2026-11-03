import { describe, expect, it } from "vitest";
import { fireEvent, screen } from "@testing-library/react";
import { createTestServices, renderGameApp } from "./gameTestUtils";

describe("battle flow", () => {
  it("prepares, starts a battle and shows the result", async () => {
    renderGameApp("/encounters/e4/prepare");
    expect(await screen.findByText("Your lineup")).toBeTruthy();
    expect(screen.getByText(/still have unspent stat points/)).toBeTruthy();

    const start = screen.getByRole("button", { name: "Start battle" }) as HTMLButtonElement;
    fireEvent.click(start);
    expect(await screen.findByText("Victory!")).toBeTruthy();
    expect(screen.getByText("Rewards")).toBeTruthy();
    expect(screen.getByText("Battle log")).toBeTruthy();
    expect(screen.getByRole("link", { name: "Spend stat points" })).toBeTruthy();
  });

  it("keeps the start button disabled for a locked encounter", async () => {
    renderGameApp("/encounters/e5/prepare");
    await screen.findByText("Your lineup");
    expect((screen.getByRole("button", { name: "Start battle" }) as HTMLButtonElement).disabled).toBe(true);
    expect(screen.getByText(/locked and cannot be started/)).toBeTruthy();
  });

  it("shows an error when the battle fails and allows another try", async () => {
    const base = createTestServices();
    const services = createTestServices({}, { battles: { startBattle: () => Promise.reject(new Error("Simulator unavailable")) } });
    services.encounters = base.encounters;
    renderGameApp("/encounters/e4/prepare", services);
    fireEvent.click(await screen.findByRole("button", { name: "Start battle" }));
    expect(await screen.findByText("Simulator unavailable")).toBeTruthy();
    expect((screen.getByRole("button", { name: "Start battle" }) as HTMLButtonElement).disabled).toBe(false);
  });
});
