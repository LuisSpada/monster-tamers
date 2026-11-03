import { cn } from "../../../lib/utils";

const FLIES = Array.from({ length: 16 }, (_, i) => ({
  left: (i * 37 + 11) % 96,
  top: (i * 53 + 7) % 88,
  delay: (i * 0.7) % 5,
  duration: 5 + (i % 4) * 1.5,
}));

/** Soft glowing dots drifting around. Purely decorative. */
export function Fireflies({ count = 12, className }: { count?: number; className?: string }) {
  return (
    <div aria-hidden className={cn("pointer-events-none absolute inset-0 overflow-hidden", className)}>
      {FLIES.slice(0, count).map((f, i) => (
        <span
          key={i}
          className="absolute h-1.5 w-1.5 animate-firefly rounded-full bg-gold shadow-[0_0_10px_3px_rgba(246,196,83,0.65)]"
          style={{ left: `${f.left}%`, top: `${f.top}%`, animationDelay: `-${f.delay}s`, animationDuration: `${f.duration}s` }}
        />
      ))}
    </div>
  );
}
