import { cn } from "../../../lib/utils";

/** Full-screen "VICTORY" / "DEFEAT" flash that fades out by itself. Decorative: the real outcome is in the page. */
export function BattleSplash({ outcome }: { outcome: "victory" | "defeat" }) {
  const won = outcome === "victory";
  return (
    <div aria-hidden className="pointer-events-none fixed inset-0 z-50 grid animate-splash place-items-center bg-black/60">
      <span
        className={cn("font-pixel text-6xl font-bold tracking-[0.2em] sm:text-8xl", won ? "text-gold" : "text-danger")}
        style={{ textShadow: "0 0 30px currentColor, 0 4px 0 rgba(0,0,0,0.6)" }}
      >
        {won ? "VICTORY" : "DEFEAT"}
      </span>
    </div>
  );
}
