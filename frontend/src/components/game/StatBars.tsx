import { STAT_LABELS, STAT_SCALE } from "../../domain/classInfo";
import { STAT_KEYS, type Stats } from "../../domain/types";
import { ProgressBar } from "./ProgressBar";

export function StatBars({ stats }: { stats: Stats }) {
  return (
    <dl className="space-y-3">
      {STAT_KEYS.map((key) => (
        <div key={key}>
          <div className="mb-1 flex items-baseline justify-between text-sm">
            <dt className="text-muted">{STAT_LABELS[key]}</dt>
            <dd className="font-pixel text-base font-semibold">{stats[key]}</dd>
          </div>
          <ProgressBar value={stats[key]} max={STAT_SCALE[key]} label={`${STAT_LABELS[key]} ${stats[key]}`} barClassName="bg-grass" />
        </div>
      ))}
    </dl>
  );
}
