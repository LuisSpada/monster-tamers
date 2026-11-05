import { useCallback } from "react";
import { Link } from "react-router-dom";
import { ArrowRight, Check, Dumbbell, Lock, RotateCcw, Sparkles, Swords } from "lucide-react";
import { paths } from "../app/paths";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { EmptyState, ErrorState, LoadingState } from "../components/game/AsyncState";
import { ClassBadge } from "../components/game/ClassBadge";
import { EncounterStatusBadge, FinalBossBadge } from "../components/game/EncounterBits";
import { MonsterAvatar } from "../components/game/MonsterAvatar";
import { PageContainer } from "../components/game/PageContainer";
import { Panel } from "../components/game/Panel";
import { ProgressBar } from "../components/game/ProgressBar";
import { EncounterParticles } from "../components/game/fx/EncounterParticles";
import { PageBanner } from "../components/game/fx/PageBanner";
import { Reveal } from "../components/game/fx/Reveal";
import { THEME_TONE, encounterArt, encounterTheme } from "../components/game/fx/encounterArt";
import { stagger } from "../components/game/fx/fx";
import { btnPrimary, btnSecondary, chip } from "../components/game/styles";
import { formatDateTime } from "../domain/format";
import type { BattleSummary, Encounter, Monster } from "../domain/types";
import { useAsync } from "../hooks/useAsync";
import { cn } from "../lib/utils";

interface BattleRecord {
  attempts: number;
  wins: number;
  lastPlayedAt: string | null;
}

/** Derived from the battle history (newest first), so no extra backend data is needed. */
function recordFor(battles: BattleSummary[], encounterId: string): BattleRecord {
  const rows = battles.filter((b) => b.encounterId === encounterId);
  return { attempts: rows.length, wins: rows.filter((b) => b.outcome === "victory").length, lastPlayedAt: rows[0]?.playedAt ?? null };
}

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

function TrainingPanel({ team }: { team: Monster[] }) {
  return (
    <Panel title="Team training" action={<Dumbbell className="h-5 w-5 text-accent" aria-hidden />} className="animate-rise [animation-delay:150ms]">
      {team.length === 0 ? (
        <p className="text-sm text-muted">
          Your team is empty. <Link to={paths.team} className="font-medium text-accent hover:underline">Build a team</Link> to start training.
        </p>
      ) : (
        <ul className="space-y-3">
          {team.map((m) => (
            <li key={m.id} className="flex items-center gap-3">
              <MonsterAvatar monsterClass={m.monsterClass} variant={m.avatarVariant} size="sm" label={`${m.name}, ${m.monsterClass}`} />
              <div className="min-w-0 flex-1">
                <div className="flex items-center justify-between gap-2 text-sm">
                  <span className="truncate font-medium">{m.name} <span className="text-xs text-muted">Lv. {m.level}</span></span>
                  {m.pendingPoints > 0 && (
                    <Link to={paths.monster(m.id)} className="inline-flex items-center gap-1 rounded bg-gold/20 px-1.5 py-0.5 text-[11px] font-semibold text-gold">
                      <Sparkles className="h-3 w-3" aria-hidden /> {m.pendingPoints} pt
                    </Link>
                  )}
                </div>
                <ProgressBar value={m.xpToNext === null ? 1 : m.xp} max={m.xpToNext ?? 1} label={`${m.name} experience`} barClassName="bg-grass" className="my-1" />
                <div className="text-xs text-muted">{m.xpToNext === null ? "Max level" : `${m.xpToNext - m.xp} XP to level ${m.level + 1}`}</div>
              </div>
            </li>
          ))}
        </ul>
      )}
      <p className="mt-4 rounded-lg border border-line bg-inset/60 p-3 text-xs text-muted">
        Replaying a cleared encounter earns experience for every monster in your team, so you can level up before the next challenge.
      </p>
    </Panel>
  );
}

function RecordChips({ record }: { record: BattleRecord }) {
  if (record.attempts === 0) return <span className={chip}>Not played yet</span>;
  return (
    <>
      <span className={cn(chip, record.wins > 0 && "text-grass")}>Won {record.wins}× of {record.attempts}</span>
      {record.lastPlayedAt && <span className={chip}>Last played {formatDateTime(record.lastPlayedAt)}</span>}
    </>
  );
}

function RewardChips({ e }: { e: Encounter }) {
  return e.isFinalBoss ? (
    <span className={chip}>No egg reward</span>
  ) : (
    <>
      <span className={chip}><span aria-hidden>🥚</span> Egg chance {e.eggChance}%</span>
      {e.rewardClasses.map((c) => <ClassBadge key={c} monsterClass={c} />)}
    </>
  );
}

function EncounterArt({ e, className, locked }: { e: Encounter; className?: string; locked?: boolean }) {
  return (
    <div className={cn("relative overflow-hidden", className)}>
      <img
        src={encounterArt(e)}
        alt=""
        aria-hidden
        className={cn("pixelated absolute inset-0 h-full w-full object-cover transition-transform duration-500 group-hover:scale-110", locked && "grayscale brightness-50")}
      />
      {!locked && <EncounterParticles theme={encounterTheme(e)} count={10} />}
      <div aria-hidden className="absolute inset-0 bg-gradient-to-t from-card via-card/10 to-transparent" />
      {locked && <Lock aria-hidden className="absolute left-1/2 top-1/2 h-8 w-8 -translate-x-1/2 -translate-y-1/2 text-white/80" />}
    </div>
  );
}

function EncounterCard({ e, record, mode }: { e: Encounter; record: BattleRecord; mode: "start" | "replay" | "locked" }) {
  return (
    <article
      className={cn(
        "group flex h-full flex-col overflow-hidden rounded-2xl border bg-card/90 shadow-lg shadow-black/10 backdrop-blur-sm transition-all duration-200 hover:-translate-y-1 hover:shadow-xl motion-reduce:transform-none",
        e.isFinalBoss ? "border-violet-500/60" : mode === "start" ? "border-accent/60" : "border-line",
      )}
    >
      <div className="relative">
        <EncounterArt e={e} locked={mode === "locked"} className="h-32" />
        <span className="absolute left-3 top-3 rounded-md bg-bg/75 px-2 py-0.5 text-xs font-semibold backdrop-blur-sm">Encounter {e.order}</span>
        <span className="absolute right-3 top-3 flex gap-2">
          {e.isFinalBoss && <FinalBossBadge />}
          <EncounterStatusBadge status={e.status} />
        </span>
      </div>
      <div className="flex flex-1 flex-col gap-3 p-5">
        <h3 className="font-pixel text-xl font-semibold">
          <Link to={paths.encounter(e.id)} className="hover:underline">{e.name}</Link>
        </h3>
        <p className="line-clamp-2 text-sm text-muted">{e.description}</p>
        <div className="flex flex-wrap items-center gap-2"><RewardChips e={e} /></div>
        {mode !== "locked" && <div className="flex flex-wrap items-center gap-2"><RecordChips record={record} /></div>}
        <div className="mt-auto flex flex-wrap items-center gap-2 pt-1">
          {mode === "locked" ? (
            <p className="text-xs text-muted">Clear the earlier encounters to unlock this one.</p>
          ) : (
            <Link to={paths.prepare(e.id)} aria-label={`${mode === "replay" ? "Replay" : "Start battle"}: ${e.name}`} className={cn(mode === "replay" ? btnSecondary : btnPrimary, "px-3 py-1.5")}>
              {mode === "replay" ? <RotateCcw className="h-4 w-4" aria-hidden /> : <Swords className="h-4 w-4" aria-hidden />}
              {mode === "replay" ? "Replay" : "Start battle"}
            </Link>
          )}
          <Link to={paths.encounter(e.id)} className="ml-auto inline-flex items-center gap-1 text-sm font-medium text-accent transition-transform group-hover:translate-x-1">
            Details <ArrowRight className="h-4 w-4" aria-hidden />
          </Link>
        </div>
      </div>
    </article>
  );
}

function FeaturedEncounter({ e, record }: { e: Encounter; record: BattleRecord }) {
  return (
    <article className="group grid overflow-hidden rounded-2xl border border-accent/60 bg-card/90 shadow-lg shadow-black/10 backdrop-blur-sm md:grid-cols-[1.1fr_1fr]">
      <div className="relative min-h-[190px]">
        <EncounterArt e={e} className="absolute inset-0" />
        <span className="absolute left-4 top-4 rounded-md bg-accent px-2 py-0.5 text-xs font-bold uppercase tracking-wide text-accent-fg">Next up</span>
      </div>
      <div className="flex flex-col gap-3 p-5 sm:p-6">
        <div className="flex flex-wrap items-center gap-2 text-sm text-muted">
          Encounter {e.order} {e.isFinalBoss && <FinalBossBadge />} <EncounterStatusBadge status={e.status} />
        </div>
        <h3 className="font-pixel text-2xl font-bold">
          <Link to={paths.encounter(e.id)} className="hover:underline">{e.name}</Link>
        </h3>
        <p className="text-sm text-muted">{e.description}</p>
        <div className="flex flex-wrap items-center gap-2"><RewardChips e={e} /></div>
        <div className="flex flex-wrap items-center gap-2"><RecordChips record={record} /></div>
        <div className="mt-auto flex flex-wrap gap-3 pt-2">
          <Link to={paths.prepare(e.id)} aria-label={`Start battle: ${e.name}`} className={cn(btnPrimary, "animate-glow")}>
            <Swords className="h-4 w-4" aria-hidden /> Start battle
          </Link>
          <Link to={paths.encounter(e.id)} className={btnSecondary}>Details</Link>
        </div>
      </div>
    </article>
  );
}

function SectionHeading({ id, title, hint }: { id: string; title: string; hint: string }) {
  return (
    <div className="mb-3">
      <h2 id={id} className="font-pixel text-xl font-semibold">{title}</h2>
      <p className="text-sm text-muted">{hint}</p>
    </div>
  );
}

export function EncounterListPage() {
  const { encounters, history, teams, monsters } = useServices();
  const load = useCallback(async () => {
    const [list, battles, team, owned] = await Promise.all([
      encounters.listEncounters(),
      history.listBattles(),
      teams.getTeam(),
      monsters.listMonsters(),
    ]);
    const byId = new Map(owned.map((m) => [m.id, m]));
    return { list, battles, teamMonsters: team.monsterIds.flatMap((id) => byId.get(id) ?? []) };
  }, [encounters, history, teams, monsters]);
  const { state, reload } = useAsync(load);

  return (
    <>
      <PageHeader title="Encounters" description="Choose an encounter, check the possible rewards and prepare your team." />
      <PageContainer>
        {state.status === "loading" && <LoadingState label="Loading encounters…" />}
        {state.status === "error" && <ErrorState message={state.message} onRetry={reload} />}
        {state.status === "success" &&
          (state.data.list.length === 0 ? (
            <EmptyState title="No encounters available" description="Check back later." />
          ) : (
            (() => {
              const { list, battles, teamMonsters } = state.data;
              const cleared = list.filter((e) => e.status === "cleared");
              const available = list.filter((e) => e.status === "available");
              const locked = list.filter((e) => e.status === "locked");
              const [featured, ...otherAvailable] = available;
              const heroEncounter = featured ?? list[list.length - 1];
              const wins = battles.filter((b) => b.outcome === "victory").length;
              return (
                <>
                  <PageBanner
                    image={encounterArt(heroEncounter)}
                    tone={THEME_TONE[encounterTheme(heroEncounter)]}
                    overlay={<EncounterParticles theme={encounterTheme(heroEncounter)} count={14} />}
                    eyebrow="Train and explore"
                    tagline="Replay encounters to grow stronger, then push further."
                    chips={<span className={cn(chip, "bg-bg/70 backdrop-blur-sm")}>{wins} {wins === 1 ? "victory" : "victories"} so far</span>}
                  />

                  <div className="grid items-stretch gap-5 lg:grid-cols-[1.15fr_1fr]">
                    <Panel title="Progress" className="animate-rise [animation-delay:100ms]">
                      <p className="mb-2 text-sm text-muted">{cleared.length} of {list.length} encounters cleared</p>
                      <ProgressBar value={cleared.length} max={list.length} label="Encounters cleared" />
                      <div className="mt-4">
                        <JourneyTrail encounters={list} />
                      </div>
                    </Panel>
                    <TrainingPanel team={teamMonsters} />
                  </div>

                  <section aria-labelledby="continue-heading">
                    <SectionHeading id="continue-heading" title="Continue your journey" hint="Clear new encounters to unlock the next ones." />
                    {featured ? (
                      <div className="space-y-4">
                        <Reveal>
                          <FeaturedEncounter e={featured} record={recordFor(battles, featured.id)} />
                        </Reveal>
                        {otherAvailable.length > 0 && (
                          <ul className="grid items-stretch gap-4 lg:grid-cols-2">
                            {otherAvailable.map((e, i) => (
                              <Reveal as="li" key={e.id} delay={stagger(i)}>
                                <EncounterCard e={e} record={recordFor(battles, e.id)} mode="start" />
                              </Reveal>
                            ))}
                          </ul>
                        )}
                      </div>
                    ) : (
                      <EmptyState title="You have cleared every available encounter" description="Replay them below to keep training your monsters." />
                    )}
                  </section>

                  <section aria-labelledby="training-heading">
                    <SectionHeading id="training-heading" title="Training grounds" hint="Replay cleared encounters to earn experience and more egg chances." />
                    {cleared.length === 0 ? (
                      <EmptyState title="Nothing to replay yet" description="Win your first encounter and it will show up here for training." />
                    ) : (
                      <ul className="grid items-stretch gap-4 lg:grid-cols-2 2xl:grid-cols-3">
                        {cleared.map((e, i) => (
                          <Reveal as="li" key={e.id} delay={stagger(i)}>
                            <EncounterCard e={e} record={recordFor(battles, e.id)} mode="replay" />
                          </Reveal>
                        ))}
                      </ul>
                    )}
                  </section>

                  {locked.length > 0 && (
                    <section aria-labelledby="coming-heading">
                      <SectionHeading id="coming-heading" title="Coming up" hint="These open up as you progress." />
                      <ul className="grid items-stretch gap-4 lg:grid-cols-2 2xl:grid-cols-3">
                        {locked.map((e, i) => (
                          <Reveal as="li" key={e.id} delay={stagger(i)}>
                            <EncounterCard e={e} record={recordFor(battles, e.id)} mode="locked" />
                          </Reveal>
                        ))}
                      </ul>
                    </section>
                  )}
                </>
              );
            })()
          ))}
      </PageContainer>
    </>
  );
}
