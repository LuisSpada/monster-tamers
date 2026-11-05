import { cn } from "../../lib/utils";

interface ProgressBarProps {
  value: number;
  max: number;
  label: string;
  barClassName?: string;
  className?: string;
}

export function ProgressBar({ value, max, label, barClassName = "bg-accent", className }: ProgressBarProps) {
  const pct = max > 0 ? Math.min(100, Math.max(0, Math.round((value / max) * 100))) : 0;
  return (
    <div
      role="progressbar"
      aria-label={label}
      aria-valuemin={0}
      aria-valuemax={max}
      aria-valuenow={Math.min(value, max)}
      className={cn("h-2 overflow-hidden rounded-full bg-inset", className)}
    >
      <div className={cn("h-full rounded-full transition-[width] duration-500 motion-reduce:transition-none", barClassName)} style={{ width: `${pct}%` }} />
    </div>
  );
}
