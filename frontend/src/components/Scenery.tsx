import { cn } from "@/lib/utils";

/**
 * Komplett pixel art-scene (natthimmel, fjell, innsjø, palmer og jungel) som dekker hele flaten.
 * Et tema-farget lag oppå sørger for lesbarhet i både dark og light mode.
 */
export function SceneBackground({
  overlay = "bg-bg/80 dark:bg-bg/20",
  position = "center bottom",
  className,
}: { overlay?: string; position?: string; className?: string }) {
  return (
    <div aria-hidden className={cn("pointer-events-none absolute inset-0 -z-20", className)}>
      <div className="absolute inset-0"
        style={{ backgroundImage: "url(/assets/scene.png)", backgroundSize: "cover", backgroundPosition: position, imageRendering: "pixelated" }} />
      <div className={cn("absolute inset-0", overlay)} />
    </div>
  );
}

/** Bakgrunn for hovedområdet: scenen og vann/and nederst. */
export default function Scenery() {
  return (
    <SceneBackground />
  );
}
