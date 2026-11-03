import { useCallback } from "react";
import { Link } from "react-router-dom";
import { Library, Skull, Sparkles, Trophy, Users } from "lucide-react";
import { paths } from "../app/paths";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { ErrorState, LoadingState } from "../components/game/AsyncState";
import { ClassBadge } from "../components/game/ClassBadge";
import { PageContainer } from "../components/game/PageContainer";
import { Fireflies } from "../components/game/fx/Fireflies";
import { RingProgress } from "../components/game/fx/RingProgress";
import { Panel } from "../components/game/Panel";
import { ProgressBar } from "../components/game/ProgressBar";
import { btnSecondary, linkCls } from "../components/game/styles";
import { formatDateTime } from "../domain/format";
import { CLASS_INFO } from "../domain/classInfo";
import { MONSTER_CLASSES } from "../domain/types";
import { useAsync } from "../hooks/useAsync";
import { cn } from "../lib/utils";

export function ProfilePage() {
  const { players, monsters, history } = useServices();
  const load = useCallback(async () => {
    const [progress, list, battles] = await Promise.all([players.getProgress(), monsters.listMonsters(), history.listBattles()]);
    return { progress, list, recent: battles.slice(0, 3) };
  }, [players, monsters, history]);
  const { state, reload } = useAsync(load);

  return (
    <>
      <PageHeader title="Profile" description="Your overall progress as a Monster Tamer." />
      <PageContainer>
        {state.status === "loading" && <LoadingState label="Loading your profile…" />}
        {state.status === "error" && <ErrorState message={state.message} onRetry={reload} />}
        {state.status === "success" &&
          (() => {
            const { progress, list, recent } = state.data;
            const stats = [
              { label: "Collection", value: progress.collectionSize, Icon: Library, tone: "bg-accent/15 text-accent" },
              { label: "Team size", value: progress.teamSize, Icon: Users, tone: "bg-sky-500/15 text-sky-400" },
              { label: "Wins", value: progress.wins, Icon: Trophy, tone: "bg-gold/15 text-gold" },
              { label: "Losses", value: progress.losses, Icon: Skull, tone: "bg-danger/15 text-danger" },
            ];
            return (
              <>
                <Panel className="relative overflow-hidden bg-[radial-gradient(circle_at_100%_0%,color-mix(in_srgb,var(--accent)_18%,transparent),transparent_45%)]">
                  <Fireflies count={8} />
                  <div className="relative flex flex-wrap items-center gap-4 sm:gap-6">
                    <img src="/assets/avatar-card.png" alt="" className="h-20 w-20 animate-float rounded-xl ring-2 ring-gold/60 shadow-[0_0_30px_-6px_rgba(246,196,83,0.7)] [image-rendering:pixelated]" />
                    <div className="flex-1">
                      <h2 className="font-pixel text-2xl font-bold">{progress.playerName}</h2>
                      <p className="text-sm text-muted">{progress.finalBossDefeated ? "Final boss defeated – journey complete!" : "The final boss has not been defeated yet."}</p>
                    </div>
                    {progress.pendingPoints > 0 && (
                      <Link to={paths.collection} className="inline-flex items-center gap-1.5 rounded-md bg-gold/20 px-3 py-1.5 text-sm font-semibold text-gold">
                        <Sparkles className="h-4 w-4" aria-hidden /> {progress.pendingPoints} stat {progress.pendingPoints === 1 ? "point" : "points"} to spend
                      </Link>
                    )}
                  </div>
                </Panel>

                <dl className="grid grid-cols-2 gap-4 md:grid-cols-4">
                  {stats.map((s, i) => (
                    <div
                      key={s.label}
                      className="animate-rise rounded-xl border border-line bg-card/80 p-4 shadow-lg shadow-black/5 backdrop-blur-sm transition-transform hover:-translate-y-1"
                      style={{ animationDelay: `${i * 70}ms` }}
                    >
                      <span aria-hidden className={cn("mb-2 grid h-9 w-9 place-items-center rounded-lg", s.tone)}>
                        <s.Icon className="h-5 w-5" />
                      </span>
                      <dt className="text-xs text-muted">{s.label}</dt>
                      <dd className="mt-1 font-pixel text-2xl font-semibold">{s.value}</dd>
                    </div>
                  ))}
                </dl>

                <div className="grid items-stretch gap-5 lg:grid-cols-2">
                  <Panel title="Encounter progress">
                    <div className="flex items-center gap-5">
                      <RingProgress value={progress.encountersCleared} max={progress.encountersTotal} label="Encounters cleared" size={112}>
                        <span className="font-pixel text-2xl font-bold">{progress.encountersCleared}/{progress.encountersTotal}</span>
                      </RingProgress>
                      <div>
                        <p className="text-sm text-muted">{progress.encountersCleared} of {progress.encountersTotal} cleared</p>
                        <Link to={paths.encounters} className={`${linkCls} mt-2 inline-block text-sm`}>View encounters</Link>
                      </div>
                    </div>
                  </Panel>
                  <Panel title="Collection by class">
                    <ul className="space-y-3">
                      {MONSTER_CLASSES.map((c) => {
                        const count = list.filter((m) => m.monsterClass === c).length;
                        return (
                          <li key={c} className="text-sm">
                            <div className="mb-1 flex items-center justify-between">
                              <ClassBadge monsterClass={c} showLabel />
                              <span className="font-pixel font-semibold">{count}</span>
                            </div>
                            <ProgressBar value={count} max={Math.max(1, list.length)} label={`${c} monsters`} barClassName={CLASS_INFO[c].badgeClass} />
                          </li>
                        );
                      })}
                    </ul>
                  </Panel>
                </div>

                <Panel title="Recent battles" action={<Link to={paths.history} className={linkCls}>All battles</Link>}>
                  {recent.length === 0 ? (
                    <p className="text-sm text-muted">No battles yet.</p>
                  ) : (
                    <ul className="space-y-2">
                      {recent.map((b) => (
                        <li key={b.id}>
                          <Link to={paths.historyDetail(b.id)} className="flex items-center justify-between gap-3 rounded-lg border border-line bg-inset/60 px-3 py-2 text-sm hover:border-accent">
                            <span className="font-medium">{b.encounterName}</span>
                            <span className="flex items-center gap-3 text-xs text-muted">
                              <span className={cn("font-semibold", b.outcome === "victory" ? "text-grass" : "text-danger")}>{b.outcome === "victory" ? "Victory" : "Defeat"}</span>
                              {formatDateTime(b.playedAt)}
                            </span>
                          </Link>
                        </li>
                      ))}
                    </ul>
                  )}
                </Panel>
                <Link to={paths.settings} className={btnSecondary}>Settings</Link>
              </>
            );
          })()}
      </PageContainer>
    </>
  );
}
