import { useCallback, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeft, Sparkles } from "lucide-react";
import { paths } from "../app/paths";
import { useServices } from "../api/useServices";
import { getErrorMessage } from "../api/errors";
import { PageHeader } from "../components/common/PageHeader";
import { EmptyState, ErrorState, LoadingState } from "../components/game/AsyncState";
import { ClassBadge } from "../components/game/ClassBadge";
import { MonsterAvatar } from "../components/game/MonsterAvatar";
import { PageContainer } from "../components/game/PageContainer";
import { Panel } from "../components/game/Panel";
import { ProgressBar } from "../components/game/ProgressBar";
import { StatBars } from "../components/game/StatBars";
import { btnPrimary, btnSecondary, chip, linkCls } from "../components/game/styles";
import { CLASS_INFO, STAT_LABELS } from "../domain/classInfo";
import { formatDateTime } from "../domain/format";
import { MAX_LEVEL, MAX_TALENT, MIN_TALENT, STAT_KEYS, type Monster, type StatKey } from "../domain/types";
import { Fireflies } from "../components/game/fx/Fireflies";
import { CLASS_AURA, CLASS_TINT } from "../components/game/fx/fx";
import { useAsync } from "../hooks/useAsync";
import { cn } from "../lib/utils";

export function MonsterDetailPage() {
  const { monsterId = "" } = useParams();
  return <MonsterDetailView key={monsterId} monsterId={monsterId} />;
}

function MonsterDetailView({ monsterId }: { monsterId: string }) {
  const { monsters, teams } = useServices();
  const load = useCallback(async () => {
    const [monster, team] = await Promise.all([monsters.getMonster(monsterId), teams.getTeam()]);
    return { monster, slot: team.monsterIds.indexOf(monsterId) };
  }, [monsters, teams, monsterId]);
  const { state, reload } = useAsync(load);

  const [updated, setUpdated] = useState<Monster | null>(null);
  const [busyStat, setBusyStat] = useState<StatKey | null>(null);
  const [notice, setNotice] = useState<{ kind: "success" | "error"; text: string } | null>(null);

  async function spend(monster: Monster, stat: StatKey) {
    setBusyStat(stat);
    setNotice(null);
    try {
      const next = await monsters.spendStatPoint(monster.id, stat);
      setUpdated(next);
      setNotice({ kind: "success", text: `${STAT_LABELS[stat]} increased to ${next.stats[stat]}.` });
    } catch (error) {
      setNotice({ kind: "error", text: getErrorMessage(error) });
    } finally {
      setBusyStat(null);
    }
  }

  const monster = updated ?? (state.status === "success" ? state.data.monster : null);

  return (
    <>
      <PageHeader title={monster ? monster.name : "Monster details"} description="Inspect stats, skill, trait, Talent and progression." />
      <PageContainer>
        <Link to={paths.collection} className={`${linkCls} inline-flex items-center gap-1 text-sm`}>
          <ArrowLeft className="h-4 w-4" aria-hidden /> Back to collection
        </Link>

        {state.status === "loading" && <LoadingState label="Loading monster…" />}
        {state.status === "error" && <ErrorState message={state.message} onRetry={reload} />}
        {state.status === "success" && monster && (
          <>
            <Panel className={cn("relative overflow-hidden bg-gradient-to-br to-card/90", CLASS_TINT[monster.monsterClass])}>
              <Fireflies count={9} />
              <div className="relative flex flex-col gap-5 sm:flex-row sm:items-center">
                <MonsterAvatar
                  monsterClass={monster.monsterClass}
                  variant={monster.avatarVariant}
                  size="lg"
                  label={`${monster.name}, ${monster.monsterClass}`}
                  className={cn("animate-float", CLASS_AURA[monster.monsterClass])}
                />
                <div className="min-w-0 flex-1 space-y-3">
                  <div className="flex flex-wrap items-center gap-3">
                    <h2 className="font-pixel text-2xl font-bold">{monster.name}</h2>
                    <ClassBadge monsterClass={monster.monsterClass} showLabel />
                  </div>
                  <p className="text-sm text-muted">
                    {CLASS_INFO[monster.monsterClass].role} · {CLASS_INFO[monster.monsterClass].description}
                  </p>
                  <div className="flex flex-wrap gap-2">
                    <span className={chip}>Level {monster.level} / {MAX_LEVEL}</span>
                    <span className={chip}>{state.data.slot >= 0 ? `In team, slot #${state.data.slot + 1}` : "Not in team"}</span>
                    <span className={chip}>Obtained {formatDateTime(monster.obtainedAt)}</span>
                  </div>
                  <div>
                    <div className="mb-1 flex justify-between text-sm">
                      <span className="text-muted">Talent (XP multiplier)</span>
                      <span className="font-pixel font-semibold">{monster.talent}</span>
                    </div>
                    <ProgressBar value={monster.talent - MIN_TALENT} max={MAX_TALENT - MIN_TALENT} label={`Talent ${monster.talent} on a scale from ${MIN_TALENT} to ${MAX_TALENT}`} barClassName="bg-gold" />
                    <p className="mt-1 text-xs text-muted">Talent is fixed and never changes.</p>
                  </div>
                </div>
              </div>
            </Panel>

            <div className="grid animate-rise items-stretch gap-5 [animation-delay:120ms] lg:grid-cols-2">
              <Panel title="Progression">
                {monster.xpToNext === null ? (
                  <p className="text-sm">This monster has reached the maximum level ({MAX_LEVEL}). <strong>Max level</strong></p>
                ) : (
                  <>
                    <div className="mb-1 flex justify-between text-sm">
                      <span className="text-muted">Experience to level {monster.level + 1}</span>
                      <span>{monster.xp} / {monster.xpToNext} XP</span>
                    </div>
                    <ProgressBar value={monster.xp} max={monster.xpToNext} label={`Experience ${monster.xp} of ${monster.xpToNext}`} barClassName="bg-grass" />
                  </>
                )}
                <div className="mt-4 rounded-lg border border-line bg-inset/60 p-3 text-sm">
                  <span className="font-semibold">Unspent stat points: </span>
                  <span className="font-pixel text-base text-gold">{monster.pendingPoints}</span>
                </div>
              </Panel>
              <Panel title="Statistics">
                <StatBars stats={monster.stats} />
              </Panel>
            </div>

            <div className="grid animate-rise items-stretch gap-5 [animation-delay:200ms] lg:grid-cols-2">
              <Panel title="Skill">
                <p className="font-medium">{monster.skill.name}</p>
                <p className="mt-1 text-sm text-muted">{monster.skill.description}</p>
              </Panel>
              <Panel title="Trait">
                <p className="font-medium">{monster.trait.name}</p>
                <p className="mt-1 text-sm text-muted">{monster.trait.description}</p>
              </Panel>
            </div>

            <Panel title="Level up" className={cn("[animation-delay:260ms]", monster.pendingPoints > 0 ? "animate-glow border-gold/60" : "animate-rise")}>
              {monster.pendingPoints > 0 ? (
                <>
                  <p className="mb-3 flex items-center gap-2 text-sm">
                    <Sparkles className="h-4 w-4 animate-wobble text-gold" aria-hidden />
                    You have {monster.pendingPoints} unspent stat {monster.pendingPoints === 1 ? "point" : "points"}. Choose a stat to improve:
                  </p>
                  <div className="flex flex-wrap gap-2">
                    {STAT_KEYS.map((stat) => (
                      <button key={stat} type="button" disabled={busyStat !== null} onClick={() => spend(monster, stat)} className={btnPrimary}>
                        {busyStat === stat ? "Saving…" : `Increase ${STAT_LABELS[stat]}`}
                      </button>
                    ))}
                  </div>
                </>
              ) : (
                <p className="text-sm text-muted">No unspent stat points. Earn experience in encounters to level up.</p>
              )}
              {notice && (
                <p key={notice.text} role={notice.kind === "error" ? "alert" : "status"} className={`mt-3 animate-pop text-sm font-medium ${notice.kind === "error" ? "text-danger" : "text-grass"}`}>
                  {notice.text}
                </p>
              )}
            </Panel>

            <Panel title="Upgrade history" className="animate-rise [animation-delay:320ms]">
              {monster.upgrades.length === 0 ? (
                <EmptyState title="No upgrades yet" description="Spend stat points to see your upgrades here." />
              ) : (
                <ol className="space-y-2">
                  {[...monster.upgrades].reverse().map((u) => (
                    <li key={u.id} className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-line bg-inset/60 px-3 py-2 text-sm">
                      <span>{STAT_LABELS[u.stat]} increased to <strong>{u.newValue}</strong> <span className="text-muted">(at level {u.levelAtUpgrade})</span></span>
                      <span className="text-xs text-muted">{formatDateTime(u.at)}</span>
                    </li>
                  ))}
                </ol>
              )}
            </Panel>

            <div className="flex flex-wrap gap-3">
              <Link to={paths.team} className={btnSecondary}>Manage team</Link>
            </div>
          </>
        )}
      </PageContainer>
    </>
  );
}
