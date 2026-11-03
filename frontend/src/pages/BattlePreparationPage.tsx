import { useCallback, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { AlertTriangle, ArrowLeft, Swords } from "lucide-react";
import { paths } from "../app/paths";
import { useServices } from "../api/useServices";
import { getErrorMessage } from "../api/errors";
import { PageHeader } from "../components/common/PageHeader";
import { ErrorState, LoadingState } from "../components/game/AsyncState";
import { ClassBadge } from "../components/game/ClassBadge";
import { EncounterStatusBadge, FinalBossBadge } from "../components/game/EncounterBits";
import { MonsterAvatar } from "../components/game/MonsterAvatar";
import { PageContainer } from "../components/game/PageContainer";
import { Panel } from "../components/game/Panel";
import { btnPrimary, btnSecondary, linkCls } from "../components/game/styles";
import { validateTeam } from "../domain/teamRules";
import { CLASS_AURA, FLOAT_STAGGER } from "../components/game/fx/fx";
import type { Monster, Opponent } from "../domain/types";
import { useAsync } from "../hooks/useAsync";
import { cn } from "../lib/utils";

/** Decorative "versus" arena. The same information is available as text in the panels below. */
function Arena({ lineup, opponents, fighting }: { lineup: Monster[]; opponents: Opponent[]; fighting: boolean }) {
  return (
    <Panel className="relative overflow-hidden bg-gradient-to-b from-card/90 to-accent/10">
      <div aria-hidden className="grid grid-cols-[1fr_auto_1fr] items-center gap-3 py-2">
        <div className="flex flex-wrap items-end justify-center gap-4">
          {lineup.length === 0 && <span className="text-sm text-muted">No team</span>}
          {lineup.map((m, i) => (
            <div key={m.id} className={cn("flex flex-col items-center gap-1", fighting && "animate-clash-left")}>
              <MonsterAvatar monsterClass={m.monsterClass} variant={m.avatarVariant} size="sm" label={m.name} className={cn("animate-float", FLOAT_STAGGER[i % 3], CLASS_AURA[m.monsterClass])} />
              <span className="max-w-20 truncate text-xs text-muted">{m.name}</span>
            </div>
          ))}
        </div>
        <div className={cn("grid h-14 w-14 place-items-center rounded-full border-2 border-gold/60 bg-gold/15 font-pixel text-xl font-bold text-gold", fighting ? "animate-swing" : "animate-glow")}>
          VS
        </div>
        <div className="flex flex-wrap items-end justify-center gap-4">
          {opponents.map((o, i) => (
            <div key={o.id} className={cn("flex flex-col items-center gap-1", fighting && "animate-clash-right")}>
              <MonsterAvatar monsterClass={o.monsterClass} variant={o.avatarVariant} size="sm" label={o.name} className={cn("animate-bob", FLOAT_STAGGER[i % 3])} />
              <span className="max-w-20 truncate text-xs text-muted">{o.name} · Lv. {o.level}</span>
            </div>
          ))}
        </div>
      </div>
    </Panel>
  );
}

export function BattlePreparationPage() {
  const { encounterId = "" } = useParams();
  const navigate = useNavigate();
  const { encounters, monsters, teams, battles } = useServices();

  const load = useCallback(async () => {
    const [encounter, list, team] = await Promise.all([encounters.getEncounter(encounterId), monsters.listMonsters(), teams.getTeam()]);
    return { encounter, list, teamIds: team.monsterIds };
  }, [encounters, monsters, teams, encounterId]);
  const { state, reload } = useAsync(load);

  const [starting, setStarting] = useState(false);
  const [startError, setStartError] = useState<string | null>(null);

  const lineup = useMemo(() => {
    if (state.status !== "success") return [];
    const byId = new Map(state.data.list.map((m) => [m.id, m]));
    return state.data.teamIds.flatMap((id) => byId.get(id) ?? []);
  }, [state]);

  async function start() {
    if (state.status !== "success") return;
    setStarting(true);
    setStartError(null);
    try {
      const result = await battles.startBattle(state.data.encounter.id, state.data.teamIds);
      navigate(paths.battleResult(result.id));
    } catch (error) {
      setStartError(getErrorMessage(error));
      setStarting(false);
    }
  }

  return (
    <>
      <PageHeader title="Battle preparation" description="Check your team and start the automated battle." />
      <PageContainer>
        <Link to={state.status === "success" ? paths.encounter(state.data.encounter.id) : paths.encounters} className={`${linkCls} inline-flex items-center gap-1 text-sm`}>
          <ArrowLeft className="h-4 w-4" aria-hidden /> Back to encounter
        </Link>
        {state.status === "loading" && <LoadingState label="Preparing the battlefield…" />}
        {state.status === "error" && <ErrorState message={state.message} onRetry={reload} />}
        {state.status === "success" &&
          (() => {
            const { encounter, teamIds } = state.data;
            const validation = validateTeam(teamIds, new Set(state.data.list.map((m) => m.id)));
            const locked = encounter.status === "locked";
            const unspent = lineup.filter((m) => m.pendingPoints > 0);
            const canStart = validation.valid && !locked && !starting;
            return (
              <>
                <Panel>
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-sm text-muted">Encounter {encounter.order}</span>
                    {encounter.isFinalBoss && <FinalBossBadge />}
                    <EncounterStatusBadge status={encounter.status} />
                  </div>
                  <h2 className="mt-2 font-pixel text-2xl font-bold">{encounter.name}</h2>
                  <p className="mt-1 text-sm text-muted">
                    {encounter.opponents.length} {encounter.opponents.length === 1 ? "opponent" : "opponents"} ·{" "}
                    {encounter.isFinalBoss ? "no egg reward" : `${encounter.eggChance}% egg chance`}
                  </p>
                </Panel>

                <Arena lineup={lineup} opponents={encounter.opponents} fighting={starting} />

                <Panel title="Your lineup" action={<Link to={paths.team} className={linkCls}>Edit team</Link>}>
                  {lineup.length === 0 ? (
                    <p className="text-sm text-muted">Your team is empty. Add at least one monster in the team builder.</p>
                  ) : (
                    <ol className="space-y-2">
                      {lineup.map((m, i) => (
                        <li key={m.id} className="flex items-center gap-3 rounded-lg border border-line bg-inset/60 p-3">
                          <span className="w-5 text-center font-pixel text-sm text-muted">#{i + 1}</span>
                          <MonsterAvatar monsterClass={m.monsterClass} variant={m.avatarVariant} size="sm" label={`${m.name}, ${m.monsterClass}`} />
                          <div className="min-w-0 flex-1">
                            <div className="truncate font-medium">{m.name}</div>
                            <div className="text-xs text-muted">Lv. {m.level} · Talent {m.talent}</div>
                          </div>
                          <ClassBadge monsterClass={m.monsterClass} showLabel />
                        </li>
                      ))}
                    </ol>
                  )}
                  {!validation.valid && (
                    <ul role="alert" className="mt-3 space-y-1 text-sm font-medium text-danger">
                      {validation.errors.map((e) => <li key={e}>{e}</li>)}
                    </ul>
                  )}
                  {unspent.length > 0 && (
                    <p className="mt-3 flex items-start gap-2 text-sm text-gold">
                      <AlertTriangle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden />
                      {unspent.map((m) => m.name).join(", ")} still {unspent.length === 1 ? "has" : "have"} unspent stat points. Spend them before the battle to be stronger.
                    </p>
                  )}
                </Panel>

                {locked && <p role="alert" className="text-sm font-medium text-danger">This encounter is locked and cannot be started yet.</p>}
                {startError && <p role="alert" className="text-sm font-medium text-danger">{startError}</p>}
                <div className="flex flex-wrap gap-3">
                  <button type="button" className={cn(btnPrimary, canStart && "animate-glow")} disabled={!canStart} onClick={start}>
                    <Swords className={cn("h-4 w-4", starting && "animate-swing")} aria-hidden /> {starting ? "Simulating battle…" : "Start battle"}
                  </button>
                  <Link to={paths.encounters} className={btnSecondary}>Cancel</Link>
                </div>
              </>
            );
          })()}
      </PageContainer>
    </>
  );
}
