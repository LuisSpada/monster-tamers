import { useCallback, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, Skull, Trophy } from "lucide-react";
import { paths } from "../app/paths";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { EmptyState, ErrorState, LoadingState } from "../components/game/AsyncState";
import { PageContainer } from "../components/game/PageContainer";
import { PageBanner } from "../components/game/fx/PageBanner";
import { Reveal } from "../components/game/fx/Reveal";
import { BANNER, stagger } from "../components/game/fx/fx";
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
              const wins = state.data.filter((b) => b.outcome === "victory").length;
              const latest = state.data[0].outcome;
              let streak = 0;
              for (const b of state.data) {
                if (b.outcome !== latest) break;
                streak += 1;
              }
              return (
                <>
                  <PageBanner
                    image={BANNER.goal}
                    tone={latest === "victory" ? "grass" : "danger"}
                    eyebrow="Your record"
                    tagline={`${wins} won, ${state.data.length - wins} lost`}
                    chips={
                      <span className={cn(chip, "bg-bg/70 backdrop-blur-sm", latest === "victory" ? "text-grass" : "text-danger")}>
                        Current streak: {streak} {latest === "victory" ? (streak === 1 ? "win" : "wins") : streak === 1 ? "loss" : "losses"}
                      </span>
                    }
                  />
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
                    <ul className="relative space-y-3 before:absolute before:bottom-3 before:left-[5px] before:top-3 before:hidden before:w-0.5 before:bg-line sm:pl-8 sm:before:block">
                      {rows.map((b, index) => (
                        <Reveal as="li" key={b.id} delay={stagger(index)} className="relative">
                          <span
                            aria-hidden
                            className={cn("absolute -left-8 top-1/2 hidden h-3 w-3 -translate-y-1/2 rounded-full ring-4 ring-bg sm:block", b.outcome === "victory" ? "bg-grass" : "bg-danger")}
                          />
                          <Link
                            to={paths.historyDetail(b.id)}
                            className={cn(listCard, "flex min-h-[78px] flex-wrap gap-3 border-l-4 p-4 sm:p-5", b.outcome === "victory" ? "border-l-grass" : "border-l-danger")}
                          >
                            <span className={cn("inline-flex h-fit items-center whitespace-nowrap rounded-md border px-2 py-0.5 text-xs font-semibold", b.outcome === "victory" ? "border-grass/50 bg-grass/15 text-grass" : "border-danger/40 bg-danger/10 text-danger")}>
                              {b.outcome === "victory" ? <Trophy className="mr-1 inline h-3 w-3" aria-hidden /> : <Skull className="mr-1 inline h-3 w-3" aria-hidden />}
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
                        </Reveal>
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
