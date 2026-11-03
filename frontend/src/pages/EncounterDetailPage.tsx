import { useCallback } from "react";
import { Link, useParams } from "react-router-dom";
import { ArrowLeft, Swords } from "lucide-react";
import { paths } from "../app/paths";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { ErrorState, LoadingState } from "../components/game/AsyncState";
import { ClassBadge } from "../components/game/ClassBadge";
import { EncounterStatusBadge, FinalBossBadge } from "../components/game/EncounterBits";
import { MonsterAvatar } from "../components/game/MonsterAvatar";
import { PageContainer } from "../components/game/PageContainer";
import { Panel } from "../components/game/Panel";
import { ProgressBar } from "../components/game/ProgressBar";
import { btnPrimary, linkCls } from "../components/game/styles";
import { CLASS_INFO } from "../domain/classInfo";
import { useAsync } from "../hooks/useAsync";

export function EncounterDetailPage() {
  const { encounterId = "" } = useParams();
  const { encounters } = useServices();
  const load = useCallback(() => encounters.getEncounter(encounterId), [encounters, encounterId]);
  const { state, reload } = useAsync(load);
  const encounter = state.status === "success" ? state.data : null;

  return (
    <>
      <PageHeader title={encounter ? encounter.name : "Encounter details"} description="Opponents, egg chance and possible rewards." />
      <PageContainer>
        <Link to={paths.encounters} className={`${linkCls} inline-flex items-center gap-1 text-sm`}>
          <ArrowLeft className="h-4 w-4" aria-hidden /> Back to encounters
        </Link>
        {state.status === "loading" && <LoadingState label="Loading encounter…" />}
        {state.status === "error" && <ErrorState message={state.message} onRetry={reload} />}
        {encounter && (
          <>
            <Panel>
              <div className="flex flex-wrap items-center gap-2">
                <span className="text-sm text-muted">Encounter {encounter.order}</span>
                {encounter.isFinalBoss && <FinalBossBadge />}
                <EncounterStatusBadge status={encounter.status} />
              </div>
              <h2 className="mt-2 font-pixel text-2xl font-bold">{encounter.name}</h2>
              <p className="mt-2 text-sm text-muted">{encounter.description}</p>
            </Panel>

            <div className="grid items-stretch gap-5 lg:grid-cols-2">
              <Panel title="Opponents">
                <ul className="space-y-2">
                  {encounter.opponents.map((o) => (
                    <li key={o.id} className="flex items-center gap-3 rounded-lg border border-line bg-inset/60 p-2">
                      <MonsterAvatar monsterClass={o.monsterClass} variant={o.avatarVariant} size="sm" label={`${o.name}, ${o.monsterClass}`} />
                      <div className="min-w-0 flex-1">
                        <div className="truncate font-medium">{o.name}</div>
                        <div className="text-xs text-muted">Lv. {o.level}</div>
                      </div>
                      <ClassBadge monsterClass={o.monsterClass} showLabel />
                    </li>
                  ))}
                </ul>
              </Panel>

              <Panel title="Rewards preview">
                {encounter.isFinalBoss ? (
                  <p className="text-sm">The final boss never drops an egg. Defeating it completes your journey.</p>
                ) : (
                  <>
                    <div className="mb-1 flex justify-between text-sm">
                      <span className="text-muted">Egg chance</span>
                      <span className="font-pixel font-semibold">{encounter.eggChance}%</span>
                    </div>
                    <ProgressBar value={encounter.eggChance} max={100} label={`Egg chance ${encounter.eggChance} percent`} barClassName="bg-gold" />
                    <h3 className="mb-2 mt-4 text-sm font-semibold">Possible egg classes</h3>
                    <ul className="space-y-2">
                      {encounter.rewardClasses.map((c) => (
                        <li key={c} className="flex items-center gap-2 text-sm">
                          <ClassBadge monsterClass={c} showLabel />
                          <span className="text-muted">{CLASS_INFO[c].role}</span>
                        </li>
                      ))}
                    </ul>
                  </>
                )}
                <p className="mt-4 text-xs text-muted">Every monster in the battle also earns experience, adjusted by its Talent.</p>
              </Panel>
            </div>

            <div className="flex flex-wrap items-center gap-3">
              {encounter.status === "locked" ? (
                <>
                  <button type="button" disabled className={btnPrimary}>
                    <Swords className="h-4 w-4" aria-hidden /> Prepare for battle
                  </button>
                  <p className="text-sm text-muted">This encounter is locked. Clear the earlier encounters first.</p>
                </>
              ) : (
                <Link to={paths.prepare(encounter.id)} className={btnPrimary}>
                  <Swords className="h-4 w-4" aria-hidden /> Prepare for battle
                </Link>
              )}
            </div>
          </>
        )}
      </PageContainer>
    </>
  );
}
