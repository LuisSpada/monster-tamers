import { describe, expect, it } from "vitest";
import { render, screen } from "@testing-library/react";
import { BattleSplash } from "../components/game/fx/BattleSplash";
import { Confetti } from "../components/game/fx/Confetti";
import { PageBanner } from "../components/game/fx/PageBanner";
import { Reveal } from "../components/game/fx/Reveal";
import { THEME_ART, encounterTheme } from "../components/game/fx/encounterArt";
import { RingProgress } from "../components/game/fx/RingProgress";
import "./gameTestUtils"; // registers the automatic cleanup

describe("motion components", () => {
  it("keeps the battle splash and the confetti out of the accessibility tree", () => {
    const { container } = render(
      <>
        <BattleSplash outcome="victory" />
        <Confetti />
      </>,
    );
    expect(screen.queryByRole("heading")).toBeNull();
    container.querySelectorAll("div[aria-hidden]").forEach((el) => expect(el.getAttribute("aria-hidden")).toBe("true"));
    expect(container.textContent).toContain("VICTORY");
  });

  it("shows DEFEAT for a lost battle", () => {
    render(<BattleSplash outcome="defeat" />);
    expect(screen.getByText("DEFEAT")).toBeTruthy();
  });

  it("exposes the ring progress as a progressbar", () => {
    render(<RingProgress value={3} max={5} label="Encounters cleared"><span>3/5</span></RingProgress>);
    const ring = screen.getByRole("progressbar", { name: "Encounters cleared" });
    expect(ring.getAttribute("aria-valuenow")).toBe("3");
    expect(ring.getAttribute("aria-valuemax")).toBe("5");
  });

  it("renders banner chips and hides the decorative image", () => {
    const { container } = render(<PageBanner image="/assets/goal.png" eyebrow="Your record" chips={<span>3 won</span>} />);
    expect(screen.getByText("Your record")).toBeTruthy();
    expect(screen.getByText("3 won")).toBeTruthy();
    expect(container.querySelector("img")?.getAttribute("aria-hidden")).toBe("true");
  });

  it("reveals its children with the requested delay", () => {
    render(<Reveal delay={120}><p>Hello</p></Reveal>);
    expect((screen.getByText("Hello").parentElement as HTMLElement).style.animationDelay).toBe("120ms");
  });

  it("gives every encounter its own embedded banner", () => {
    expect([1, 2, 3, 4].map((order) => encounterTheme({ order, isFinalBoss: false }))).toEqual(["meadow", "quarry", "ruins", "forest"]);
    expect(encounterTheme({ order: 5, isFinalBoss: true })).toBe("summit");
    expect(new Set(Object.values(THEME_ART)).size).toBe(5);
    Object.values(THEME_ART).forEach((uri) => expect(uri.startsWith("data:image/png;base64,")).toBe(true));
  });
});
