import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { paths } from "../app/paths";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { EmptyState, ErrorState, LoadingState } from "../components/game/AsyncState";
import { PageContainer } from "../components/game/PageContainer";
import { btnSecondary, chip, listCard } from "../components/game/styles";
import { formatDateTime } from "../domain/format";
import type { BattleOutcome } from "../domain/types";
import { useAsync } from "../hooks/useAsync";
import { cn } from "../lib/utils";

type Filter = "all" | BattleOutcome;
const FILTERS: { value: Filter; label: string }[] = [
  { value: "all", label: "All" },
  { value: "victory", label: "Victories" },
  { value: "defeat", label: "Defeats" },
];

export function HistoryPage() {
  const { history } = useServices();
  const load = useCallback(() => history.listBattles(), [history]);
  const { state, reload } = useAsync(load);
  const [filter, setFilter] = useState<Filter>("all");

  return (
    <>
      <PageHeader title="Battle History" description="All your past battles, newest first." />
      <PageContainer>
        {state.status === "loading" && <LoadingState label="Loading battle history…" />}
        {state.status === "error" && <ErrorState message={state.message} onRetry={reload} />}
        {state.status === "success" &&
          (state.data.length === 0 ? (
            <EmptyState
              title="No battles yet"
              description="Win or lose, every battle you play will show up here."
              action={<Link to={paths.encounters} className={btnSecondary}>Go to encounters</Link>}
            />
          ) : (
            (() => {
              const rows = state.data.filter((b) => filter === "all" || b.outcome === filter);
              return (
                <>
                  <div role="group" aria-label="Filter by outcome" className="flex flex-wrap gap-2">
                    {FILTERS.map((f) => (
                      <button key={f.value} type="button" aria-pressed={filter === f.value} onClick={() => setFilter(f.value)}
                        className={cn(btnSecondary, "px-3 py-1.5", filter === f.value && "border-accent bg-accent/15 text-accent")}>
                        {f.label}
                      </button>
                    ))}
                  </div>
                  {rows.length === 0 ? (
                    <EmptyState title="No battles match this filter" action={<button type="button" className={btnSecondary} onClick={() => setFilter("all")}>Show all battles</button>} />
                  ) : (
                    <ul className="space-y-3">
                      {rows.map((b) => (
                        <li key={b.id}>
                          <Link
                            to={paths.historyDetail(b.id)}
                            className={cn(listCard, "flex min-h-[78px] flex-wrap gap-3 p-4 sm:p-5")}
                          >
                            <span className={cn("rounded-md border px-2 py-0.5 text-xs font-semibold", b.outcome === "victory" ? "border-grass/50 bg-grass/15 text-grass" : "border-danger/40 bg-danger/10 text-danger")}>
                              {b.outcome === "victory" ? "Victory" : "Defeat"}
                            </span>
                            <div className="min-w-0 flex-1">
                              <div className="font-pixel text-lg font-semibold">{b.encounterName}</div>
                              <div className="text-sm text-muted">{b.participantNames.join(", ")}</div>
                            </div>
                            <div className="flex flex-wrap items-center gap-2 text-xs text-muted">
                              <span className={chip}>{b.rounds} rounds</span>
                              {b.eggHatched && <span className={cn(chip, "text-gold")}>Egg hatched</span>}
                              <time dateTime={b.playedAt}>{formatDateTime(b.playedAt)}</time>
                            </div>
                            <ArrowRight className="h-4 w-4 text-accent" aria-hidden />
                          </Link>
                        </li>
                      ))}
                    </ul>
                  )}
                </>
              );
            })()
          ))}
      </PageContainer>
    </>
  );
}
