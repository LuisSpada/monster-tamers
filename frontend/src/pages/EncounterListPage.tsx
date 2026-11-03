import { useCallback } from "react";
import { Link } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { paths } from "../app/paths";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { EmptyState, ErrorState, LoadingState } from "../components/game/AsyncState";
import { ClassBadge } from "../components/game/ClassBadge";
import { EncounterStatusBadge, FinalBossBadge } from "../components/game/EncounterBits";
import { PageContainer } from "../components/game/PageContainer";
import { Panel } from "../components/game/Panel";
import { ProgressBar } from "../components/game/ProgressBar";
import { chip, listCard } from "../components/game/styles";
import { useAsync } from "../hooks/useAsync";
import { cn } from "../lib/utils";

function environmentClass(order: number, finalBoss: boolean) {
  if (finalBoss) return "from-violet-500/20 via-card/90 to-card/90 border-violet-500/60";
  const palettes = [
    "from-emerald-500/20 via-card/90 to-card/90",
    "from-sky-500/20 via-card/90 to-card/90",
    "from-amber-500/20 via-card/90 to-card/90",
  ];
  return palettes[(order - 1) % palettes.length];
}

export function EncounterListPage() {
  const { encounters } = useServices();
  const load = useCallback(() => encounters.listEncounters(), [encounters]);
  const { state, reload } = useAsync(load);

  return (
    <>
      <PageHeader title="Encounters" description="Choose an encounter, check the possible rewards and prepare your team." />
      <PageContainer>
        {state.status === "loading" && <LoadingState label="Loading encounters…" />}
        {state.status === "error" && <ErrorState message={state.message} onRetry={reload} />}
        {state.status === "success" &&
          (state.data.length === 0 ? (
            <EmptyState title="No encounters available" description="Check back later." />
          ) : (
            <>
              <Panel title="Progress">
                {(() => {
                  const cleared = state.data.filter((e) => e.status === "cleared").length;
                  return (
                    <>
                      <p className="mb-2 text-sm text-muted">{cleared} of {state.data.length} encounters cleared</p>
                      <ProgressBar value={cleared} max={state.data.length} label="Encounters cleared" />
                    </>
                  );
                })()}
              </Panel>
              <ul className="grid items-stretch gap-4 lg:grid-cols-2 2xl:grid-cols-3">
                {state.data.map((e) => (
                  <li key={e.id}>
                    <Link
                      to={paths.encounter(e.id)}
                      className={cn(
                        listCard,
                        "flex h-full min-h-[245px] flex-col gap-3 bg-gradient-to-br p-5",
                        environmentClass(e.order, e.isFinalBoss),
                        e.status === "locked" && "opacity-80",
                      )}
                    >
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <span className="text-sm text-muted">Encounter {e.order}</span>
                        <span className="flex gap-2">
                          {e.isFinalBoss && <FinalBossBadge />}
                          <EncounterStatusBadge status={e.status} />
                        </span>
                      </div>
                      <h2 className="font-pixel text-xl font-semibold">{e.name}</h2>
                      <p className="line-clamp-2 text-sm text-muted">{e.description}</p>
                      <div className="mt-auto flex flex-wrap items-center gap-2">
                        {e.isFinalBoss ? (
                          <span className={chip}>No egg reward</span>
                        ) : (
                          <>
                            <span className={chip}>Egg chance {e.eggChance}%</span>
                            {e.rewardClasses.map((c) => <ClassBadge key={c} monsterClass={c} />)}
                          </>
                        )}
                        <span className="ml-auto inline-flex items-center gap-1 text-sm font-medium text-accent">
                          Details <ArrowRight className="h-4 w-4" aria-hidden />
                        </span>
                      </div>
                      {e.status === "locked" && <p className="text-xs text-muted">Clear the earlier encounters to unlock this one.</p>}
                    </Link>
                  </li>
                ))}
              </ul>
            </>
          ))}
      </PageContainer>
    </>
  );
}
