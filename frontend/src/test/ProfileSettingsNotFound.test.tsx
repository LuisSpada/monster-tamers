import { describe, expect, it } from "vitest";
import { fireEvent, screen } from "@testing-library/react";
import { NotFoundPage } from "../pages/NotFoundPage";
import { ProfilePage } from "../pages/ProfilePage";
import { SettingsPage } from "../pages/SettingsPage";
import { renderPage } from "./gameTestUtils";

describe("Profile, settings and not found", () => {
  it("shows the overall progress", async () => {
    renderPage(<ProfilePage />);
    expect(await screen.findByText("3 of 5 cleared")).toBeTruthy();
    expect(screen.getByText(/final boss has not been defeated/i)).toBeTruthy();
    expect(screen.getByText(/2 stat points to spend/)).toBeTruthy();
    expect(screen.getByText("Recent battles")).toBeTruthy();
  });

  it("switches the theme from the settings", () => {
    renderPage(<SettingsPage />);
    fireEvent.click(screen.getByRole("radio", { name: /Light/ }));
    expect(document.documentElement.classList.contains("dark")).toBe(false);
    fireEvent.click(screen.getByRole("radio", { name: /Dark/ }));
    expect(document.documentElement.classList.contains("dark")).toBe(true);
  });

  it("offers a way back from the not-found page", () => {
    renderPage(<NotFoundPage />);
    expect(screen.getByRole("link", { name: "Return home" }).getAttribute("href")).toBe("/");
  });
});
