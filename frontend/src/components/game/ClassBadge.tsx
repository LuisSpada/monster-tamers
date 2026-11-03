import { CLASS_INFO } from "../../domain/classInfo";
import type { MonsterClass } from "../../domain/types";
import { cn } from "../../lib/utils";

export function ClassBadge({ monsterClass, showLabel = false, className }: { monsterClass: MonsterClass; showLabel?: boolean; className?: string }) {
  const info = CLASS_INFO[monsterClass];
  return (
    <span className={cn("inline-flex items-center gap-1.5", className)} title={`${monsterClass} – ${info.role}`}>
      <span
        aria-hidden
        className={cn("grid h-5 w-5 place-items-center rounded font-pixel text-xs font-bold text-white", info.badgeClass)}
      >
        {info.letter}
      </span>
      {showLabel ? <span className="text-xs text-muted">{monsterClass}</span> : <span className="sr-only">{monsterClass}</span>}
    </span>
  );
}
