import { Link } from "react-router-dom";
import { Heart, Info, Skull, Swords, Trophy, Wand2 } from "lucide-react";
import { paths } from "../../app/paths";
import { formatDateTime } from "../../domain/format";
import type { BattleEvent, BattleEventKind, BattleResult } from "../../domain/types";
import { cn } from "../../lib/utils";
import { ClassBadge } from "./ClassBadge";
import { MonsterAvatar } from "./MonsterAvatar";
import { Panel } from "./Panel";
import { btnPrimary, btnSecondary, chip } from "./styles";

const EVENT_ICONS: Record<BattleEventKind, typeof Swords> = {
  damage: Swords,
  heal: Heart,
  control: Wand2,
  defeat: Skull,
  info: Info,
};

function groupByRound(events: BattleEvent[]): [number, BattleEvent[]][] {
  const map = new Map<number, BattleEvent[]>();
  for (const e of events) map.set(e.round, [...(map.get(e.round) ?? []), e]);
  return [...map.entries()].sort((a, b) => a[0] - b[0]);
}

/**
 * Shared presentation of a finished battle.
 * variant "result": celebratory view shown right after the battle (with next-step links).
 * variant "history": neutral view for the battle history (log expanded).
 */
export function BattleReport({ result, variant }: { result: BattleResult; variant: "result" | "history" }) {
  const won = result.outcome === "victory";
  const { rewards } = result;

  return (
    <div className="space-y-5">
      <section
        aria-labelledby="battle-outcome"
        className={cn(
          "relative overflow-hidden rounded-xl border p-6 text-center shadow-lg shadow-black/10",
          won
            ? "border-grass/50 bg-[radial-gradient(circle_at_50%_0%,color-mix(in_srgb,var(--grass)_22%,transparent),transparent_65%)]"
            : "border-danger/40 bg-[radial-gradient(circle_at_50%_0%,color-mix(in_srgb,var(--danger)_20%,transparent),transparent_65%)]",
        )}
      >
        {won ? <Trophy className="mx-auto mb-2 h-10 w-10 text-gold" aria-hidden /> : <Skull className="mx-auto mb-2 h-10 w-10 text-danger" aria-hidden />}
        <p className="mb-1 text-xs font-semibold uppercase tracking-[0.2em] text-muted">Battle report</p>
        <h2 id="battle-outcome" className="font-pixel text-3xl font-bold sm:text-4xl">
          {won ? "Victory!" : "Defeat"}
        </h2>
        <p className="mt-1 text-sm text-muted">
          {result.encounterName} · {result.rounds} rounds · {formatDateTime(result.playedAt)}
        </p>
        {rewards.finalBossDefeated && (
          <p className="mt-3 font-medium text-gold">You defeated the final boss. Your journey is complete!</p>
        )}
      </section>

      <div className="grid items-stretch gap-5 lg:grid-cols-2">
        <Panel title="Your team">
          <ul className="space-y-2">
            {result.participants.map((p, i) => (
              <li key={p.monsterId} className="flex items-center gap-3 rounded-lg border border-line bg-inset/60 p-2">
                <span className="w-5 text-center font-pixel text-sm text-muted">#{i + 1}</span>
                <MonsterAvatar monsterClass={p.monsterClass} size="sm" label={`${p.name}, ${p.monsterClass}`} />
                <div className="min-w-0 flex-1">
                  <div className="truncate font-medium">{p.name}</div>
                  <div className="text-xs text-muted">Lv. {p.level}</div>
                </div>
                <ClassBadge monsterClass={p.monsterClass} showLabel />
              </li>
            ))}
          </ul>
        </Panel>

        <Panel title="Rewards">
          <h3 className="mb-1 text-sm font-semibold">Experience</h3>
          <ul className="mb-3 space-y-1 text-sm">
            {rewards.xp.map((x) => (
              <li key={x.monsterId} className="flex justify-between">
                <span>{x.name}</span>
                <span className="font-medium text-grass">+{x.xpGained} XP</span>
              </li>
            ))}
          </ul>

          {rewards.levelUps.length > 0 && (
            <>
              <h3 className="mb-1 text-sm font-semibold">Level-ups</h3>
              <ul className="mb-3 space-y-1 text-sm">
                {rewards.levelUps.map((l) => (
                  <li key={l.monsterId} className="text-gold">
                    {l.name} reached level {l.toLevel} (+{l.pointsGained} stat {l.pointsGained === 1 ? "point" : "points"})
                  </li>
                ))}
              </ul>
            </>
          )}

          <h3 className="mb-1 text-sm font-semibold">Egg</h3>
          {rewards.egg ? (
            <Link
              to={paths.monster(rewards.egg.id)}
              className="flex items-center gap-3 rounded-lg border border-gold/50 bg-gold/10 p-2 hover:bg-gold/20"
            >
              <MonsterAvatar monsterClass={rewards.egg.monsterClass} variant={rewards.egg.avatarVariant} size="sm" label={`${rewards.egg.name}, ${rewards.egg.monsterClass}`} />
              <div>
                <div className="font-medium">An egg hatched: {rewards.egg.name}</div>
                <div className="text-xs text-muted">
                  {rewards.egg.monsterClass} · Talent {rewards.egg.talent}
                </div>
              </div>
            </Link>
          ) : (
            <p className="text-sm text-muted">{result.isFinalBoss ? "The final boss never drops an egg." : "No egg was awarded."}</p>
          )}
        </Panel>
      </div>

      <Panel title="Battle log" action={<span className={chip}>{result.events.length} events</span>}>
        <details open={variant === "history"}>
          <summary className="cursor-pointer text-sm font-medium text-accent focus-visible:outline focus-visible:outline-2 focus-visible:outline-accent">
            {variant === "history" ? "Show or hide the full log" : "Show the full log"}
          </summary>
          <div className="mt-3 space-y-4">
            {groupByRound(result.events).map(([round, events]) => (
              <div key={round}>
                <h3 className="mb-1 font-pixel text-base">Round {round}</h3>
                <ul className="space-y-1">
                  {events.map((e) => {
                    const Icon = EVENT_ICONS[e.kind];
                    return (
                      <li key={e.id} className="flex items-start gap-2 text-sm">
                        <Icon className={cn("mt-0.5 h-4 w-4 shrink-0", e.side === "player" ? "text-accent" : "text-danger")} aria-hidden />
                        <span>
                          <span className="sr-only">{e.side === "player" ? "Your team: " : "Opponent: "}</span>
                          {e.message}
                        </span>
                      </li>
                    );
                  })}
                </ul>
              </div>
            ))}
          </div>
        </details>
      </Panel>

      <div className="flex flex-wrap gap-3">
        {variant === "result" ? (
          <>
            <Link to={paths.collection} className={btnPrimary}>
              Spend stat points
            </Link>
            <Link to={paths.encounters} className={btnSecondary}>
              Back to encounters
            </Link>
            <Link to={paths.history} className={btnSecondary}>
              Battle history
            </Link>
          </>
        ) : (
          <>
            <Link to={paths.history} className={btnSecondary}>
              Back to history
            </Link>
            <Link to={paths.encounter(result.encounterId)} className={btnPrimary}>
              View encounter
            </Link>
          </>
        )}
      </div>
    </div>
  );
}
