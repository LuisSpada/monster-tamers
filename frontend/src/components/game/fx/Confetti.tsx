import type { CSSProperties } from "react";
import { cn } from "../../../lib/utils";

const COLORS = ["bg-gold", "bg-accent", "bg-grass", "bg-violet-500", "bg-orange-500", "bg-sky-400"];
const PIECES = Array.from({ length: 44 }, (_, i) => ({
  left: (i * 23 + 5) % 100,
  delay: (i % 11) * 0.12,
  duration: 2.2 + (i % 5) * 0.35,
  size: 6 + (i % 4) * 2,
  color: COLORS[i % COLORS.length],
  rotation: (i * 47) % 360,
}));

/** One-off celebration for a victory. Pure CSS, ignores pointer events. */
export function Confetti() {
  return (
    <div aria-hidden className="pointer-events-none fixed inset-x-0 top-0 z-40 h-screen overflow-hidden">
      {PIECES.map((p, i) => (
        <span
          key={i}
          className={cn("absolute -top-4 animate-fall rounded-[2px]", p.color)}
          style={
            {
              left: `${p.left}%`,
              width: p.size,
              height: p.size * 1.6,
              animationDelay: `${p.delay}s`,
              animationDuration: `${p.duration}s`,
              "--r": `${p.rotation}deg`,
            } as CSSProperties
          }
        />
      ))}
    </div>
  );
}
