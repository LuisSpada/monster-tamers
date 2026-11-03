import type { EncounterTheme } from "./encounterArt";
import { cn } from "../../../lib/utils";

const SPOTS = Array.from({ length: 22 }, (_, i) => ({
  left: (i * 41 + 9) % 100,
  top: (i * 29 + 13) % 88,
  delay: (i * 0.9) % 6,
  duration: 4.5 + (i % 5) * 1.3,
  size: 2 + (i % 3),
}));

const dot = "absolute rounded-full";

/** Theme-specific ambient particles on top of an encounter banner. Purely decorative. */
export function EncounterParticles({ theme, count = 16, className }: { theme: EncounterTheme; count?: number; className?: string }) {
  const spots = SPOTS.slice(0, count);
  return (
    <div aria-hidden className={cn("pointer-events-none absolute inset-0 overflow-hidden", className)}>
      {theme === "forest" && (
        <>
          <div className="absolute -left-1/4 bottom-0 h-1/2 w-3/2 animate-mist bg-gradient-to-t from-white/25 to-transparent blur-md" />
          <div className="absolute -right-1/4 bottom-4 h-1/3 w-3/2 animate-mist bg-gradient-to-t from-white/15 to-transparent blur-md [animation-delay:-6s]" />
        </>
      )}
      {spots.map((p, i) => {
        const style = { left: `${p.left}%`, animationDelay: `-${p.delay}s`, animationDuration: `${p.duration}s`, width: p.size, height: p.size };
        switch (theme) {
          case "summit":
            return <span key={i} className={cn(dot, "top-0 animate-snow bg-white/90")} style={style} />;
          case "quarry":
            return <span key={i} className={cn(dot, "animate-dust bg-amber-200/70")} style={{ ...style, top: `${40 + (p.top % 50)}%` }} />;
          case "ruins":
            return <span key={i} className={cn(dot, "animate-firefly bg-cyan-300 shadow-[0_0_8px_2px_rgba(103,232,249,0.7)]")} style={{ ...style, top: `${p.top}%` }} />;
          case "meadow":
            return <span key={i} className={cn(dot, "animate-firefly bg-yellow-100 shadow-[0_0_6px_1px_rgba(254,240,138,0.8)]")} style={{ ...style, top: `${40 + (p.top % 50)}%` }} />;
          default:
            return <span key={i} className={cn(dot, "animate-firefly bg-yellow-200 shadow-[0_0_8px_2px_rgba(253,224,71,0.6)]")} style={{ ...style, top: `${p.top}%` }} />;
        }
      })}
    </div>
  );
}
