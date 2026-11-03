import type { MonsterClass } from "../../../domain/types";

/** Wide pixel-art scenes that already ship in /public/assets (the small thumbnails are too low-res for banners). */
export const BANNER = {
  hero: "/assets/hero-banner.png",
  goal: "/assets/goal.png",
  scene: "/assets/scene.png",
} as const;

/** Rotating art for encounter cards; the final boss always uses BANNER.hero (castle on the right). */
export const ENCOUNTER_ART = [BANNER.goal, BANNER.scene, BANNER.hero] as const;

/** Entrance delay in ms for the n-th item of a list (capped so long lists do not feel slow). */
export const stagger = (index: number, step = 70, max = 700) => Math.min(index * step, max);

/** Negative animation delays so several floating avatars do not move in sync. */
export const FLOAT_STAGGER = ["", "[animation-delay:-1.3s]", "[animation-delay:-2.6s]"] as const;

export const CLASS_AURA: Record<MonsterClass, string> = {
  Fighter: "shadow-[0_0_34px_-4px_rgba(249,115,22,0.6)]",
  Guardian: "shadow-[0_0_34px_-4px_rgba(14,165,233,0.6)]",
  Shaman: "shadow-[0_0_34px_-4px_rgba(16,185,129,0.6)]",
  Trickster: "shadow-[0_0_34px_-4px_rgba(139,92,246,0.6)]",
};

export const CLASS_TINT: Record<MonsterClass, string> = {
  Fighter: "from-orange-500/25",
  Guardian: "from-sky-500/25",
  Shaman: "from-emerald-500/25",
  Trickster: "from-violet-500/25",
};

/** Subtle lift on hover; disabled for people who prefer reduced motion. */
export const lift = "transition-transform duration-200 hover:-translate-y-1 motion-reduce:transform-none";
