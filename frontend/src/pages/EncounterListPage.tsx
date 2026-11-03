import { useCallback } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, Check, Lock } from "lucide-react";
import { paths } from "../app/paths";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { EmptyState, ErrorState, LoadingState } from "../components/game/AsyncState";
import { ClassBadge } from "../components/game/ClassBadge";
import { EncounterStatusBadge, FinalBossBadge } from "../components/game/EncounterBits";
import { PageContainer } from "../components/game/PageContainer";
import { Panel } from "../components/game/Panel";
import { ProgressBar } from "../components/game/ProgressBar";
import { PageBanner } from "../components/game/fx/PageBanner";
import { Reveal } from "../components/game/fx/Reveal";
import { BANNER, ENCOUNTER_ART, stagger } from "../components/game/fx/fx";
import { chip } from "../components/game/styles";
import type { Encounter } from "../domain/types";
import { useAsync } from "../hooks/useAsync";
import { cn } from "../lib/utils";

const artFor = (e: Encounter) => (e.isFinalBoss ? BANNER.hero : ENCOUNTER_ART[(e.order - 1) % ENCOUNTER_ART.length]);

/** A little map: one node per encounter, the available one pulses. */
function JourneyTrail({ encounters }: { encounters: Encounter[] }) {
  return (
    <ol aria-label="Journey map" className="flex items-center">
      {encounters.map((e, i) => (
        <li key={e.id} className="flex flex-1 items-center last:flex-none">
          <span
            title={`${e.name} – ${e.status}`}
            className={cn(
              "grid h-9 w-9 shrink-0 place-items-center rounded-full border-2 font-pixel text-sm font-bold",
              e.status === "cleared" && "border-grass bg-grass/20 text-grass",
              e.status === "available" && "animate-glow border-accent bg-accent/20 text-accent",
              e.status === "locked" && "border-line bg-inset text-muted",
              e.isFinalBoss && "border-violet-500 text-violet-400",
            )}
          >
            {e.status === "cleared" ? <Check className="h-4 w-4" aria-hidden /> : e.status === "locked" ? <Lock className="h-3.5 w-3.5" aria-hidden /> : e.order}
          </span>
          {i < encounters.length - 1 && (
            <span aria-hidden className={cn("mx-1 h-0.5 flex-1 rounded", e.status === "cleared" ? "bg-grass/70" : "border-t-2 border-dashed border-line")} />
          )}
        </li>
      ))}
    </ol>
  );
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
            (() => {
              const cleared = state.data.filter((e) => e.status === "cleared").length;
              return (
                <>
                  <PageBanner image={BANNER.hero} tone="accent" eyebrow="The road ahead" tagline="Every victory opens the next path." />
                  <Panel title="Progress" className="animate-rise [animation-delay:100ms]">
                    <p className="mb-2 text-sm text-muted">{cleared} of {state.data.length} encounters cleared</p>
                    <ProgressBar value={cleared} max={state.data.length} label="Encounters cleared" />
                    <div className="mt-4">
                      <JourneyTrail encounters={state.data} />
                    </div>
                  </Panel>
                  <ul className="grid items-stretch gap-5 lg:grid-cols-2 xl:grid-cols-3">
                    {state.data.map((e, index) => (
                      <Reveal as="li" key={e.id} delay={stagger(index)}>
                        <Link
                          to={paths.encounter(e.id)}
                          className={cn(
                            "group flex h-full min-h-[260px] flex-col overflow-hidden rounded-2xl border bg-card/90 shadow-lg shadow-black/10 backdrop-blur-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-xl motion-reduce:transform-none",
                            e.isFinalBoss ? "border-violet-500/60" : e.status === "available" ? "border-accent/60" : "border-line",
                          )}
                        >
                          <div className="relative h-28 overflow-hidden">
                            <img
                              src={artFor(e)}
                              alt=""
                              aria-hidden
                              className={cn("pixelated h-full w-full object-cover transition-transform duration-500 group-hover:scale-110", e.status === "locked" && "grayscale brightness-50")}
                            />
                            <div aria-hidden className="absolute inset-0 bg-gradient-to-t from-card via-card/10 to-transparent" />
                            <span className="absolute left-3 top-3 rounded-md bg-bg/75 px-2 py-0.5 text-xs font-semibold backdrop-blur-sm">Encounter {e.order}</span>
                            <span className="absolute right-3 top-3 flex gap-2">
                              {e.isFinalBoss && <FinalBossBadge />}
                              <EncounterStatusBadge status={e.status} />
                            </span>
                            {e.status === "locked" && <Lock aria-hidden className="absolute left-1/2 top-1/2 h-8 w-8 -translate-x-1/2 -translate-y-1/2 text-white/80" />}
                          </div>
                          <div className="flex flex-1 flex-col gap-3 p-5">
                            <h2 className="font-pixel text-xl font-semibold">{e.name}</h2>
                            <p className="line-clamp-2 text-sm text-muted">{e.description}</p>
                            <div className="mt-auto flex flex-wrap items-center gap-2">
                              {e.isFinalBoss ? (
                                <span className={chip}>No egg reward</span>
                              ) : (
                                <>
                                  <span className={chip}><span aria-hidden>🥚</span> Egg chance {e.eggChance}%</span>
                                  {e.rewardClasses.map((c) => <ClassBadge key={c} monsterClass={c} />)}
                                </>
                              )}
                              <span className="ml-auto inline-flex items-center gap-1 text-sm font-medium text-accent transition-transform group-hover:translate-x-1">
                                Details <ArrowRight className="h-4 w-4" aria-hidden />
                              </span>
                            </div>
                            {e.status === "locked" && <p className="text-xs text-muted">Clear the earlier encounters to unlock this one.</p>}
                          </div>
                        </Link>
                      </Reveal>
                    ))}
                  </ul>
                </>
              );
            })()
          ))}
      </PageContainer>
    </>
  );
}
