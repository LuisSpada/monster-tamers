import { CheckCircle2, Lock, Skull, Swords } from "lucide-react";
import type { EncounterStatus } from "../../domain/types";
import { cn } from "../../lib/utils";

const STATUS: Record<EncounterStatus, { label: string; className: string; Icon: typeof Lock }> = {
  locked: { label: "Locked", className: "border-line bg-inset text-muted", Icon: Lock },
  available: { label: "Available", className: "border-accent/50 bg-accent/15 text-accent", Icon: Swords },
  cleared: { label: "Cleared", className: "border-grass/50 bg-grass/15 text-grass", Icon: CheckCircle2 },
};

export function EncounterStatusBadge({ status }: { status: EncounterStatus }) {
  const { label, className, Icon } = STATUS[status];
  return (
    <span className={cn("inline-flex items-center gap-1 rounded-md border px-2 py-0.5 text-xs font-semibold", className)}>
      <Icon className="h-3 w-3" aria-hidden /> {label}
    </span>
  );
}

export function FinalBossBadge() {
  return (
    <span className="inline-flex items-center gap-1 rounded-md bg-violet-600 px-2 py-0.5 text-xs font-semibold uppercase text-white">
      <Skull className="h-3 w-3" aria-hidden /> Final boss
    </span>
  );
}
