import type { ReactNode } from "react";
import { cn } from "../../../lib/utils";

const TONES = {
  accent: "from-accent/30",
  gold: "from-gold/30",
  violet: "from-violet-500/35",
  danger: "from-danger/30",
  grass: "from-grass/30",
} as const;

const TWINKLES = [
  { left: "12%", top: "18%", delay: "0s" },
  { left: "34%", top: "34%", delay: "-1.2s" },
  { left: "58%", top: "14%", delay: "-2.1s" },
  { left: "76%", top: "30%", delay: "-0.6s" },
  { left: "90%", top: "16%", delay: "-1.8s" },
];

interface PageBannerProps {
  image: string;
  eyebrow?: string;
  tagline?: string;
  chips?: ReactNode;
  /** Decorative layer on top of the image, e.g. <EncounterParticles />. */
  overlay?: ReactNode;
  tone?: keyof typeof TONES;
  position?: string;
  className?: string;
}

/** Decorative pixel-art banner with a slow zoom and twinkling stars. It never repeats the page title. */
export function PageBanner({ image, eyebrow, tagline, chips, overlay, tone = "accent", position = "center", className }: PageBannerProps) {
  return (
    <div className={cn("relative isolate h-[140px] animate-rise overflow-hidden rounded-2xl border border-line shadow-lg shadow-black/15 sm:h-[160px]", className)}>
      <img
        src={image}
        alt=""
        aria-hidden
        className="pixelated absolute inset-0 -z-20 h-full w-full animate-kenburns object-cover"
        style={{ objectPosition: position }}
      />
      <div aria-hidden className="absolute inset-0 -z-10 bg-gradient-to-r from-bg/90 via-bg/45 to-transparent" />
      <div aria-hidden className={cn("absolute inset-0 -z-10 bg-gradient-to-t to-transparent", TONES[tone])} />
      {overlay}
      {TWINKLES.map((t, i) => (
        <span
          key={i}
          aria-hidden
          className="absolute h-1 w-1 animate-twinkle rounded-full bg-white"
          style={{ left: t.left, top: t.top, animationDelay: t.delay }}
        />
      ))}
      <div className="flex h-full flex-col justify-end gap-2 p-4 sm:p-5">
        {eyebrow && <p className="text-xs font-semibold uppercase tracking-[0.22em] text-accent">{eyebrow}</p>}
        {tagline && <p className="max-w-xl font-pixel text-lg font-semibold leading-snug text-fg sm:text-xl">{tagline}</p>}
        {chips && <div className="flex flex-wrap gap-2">{chips}</div>}
      </div>
    </div>
  );
}
