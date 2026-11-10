import { useCallback, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { ArrowDown, ArrowUp, CheckCircle2, Plus, Trash2, XCircle } from "lucide-react";
import { paths } from "../app/paths";
import { useServices } from "../api/useServices";
import { getErrorMessage } from "../api/errors";
import { PageHeader } from "../components/common/PageHeader";
import { ErrorState, LoadingState } from "../components/game/AsyncState";
import { ClassBadge } from "../components/game/ClassBadge";
import { MonsterAvatar } from "../components/game/MonsterAvatar";
import { PageContainer } from "../components/game/PageContainer";
import { Panel } from "../components/game/Panel";
import { PageBanner } from "../components/game/fx/PageBanner";
import { BANNER, CLASS_AURA, CLASS_TINT, stagger } from "../components/game/fx/fx";
import { btnDanger, btnPrimary, btnSecondary, chip } from "../components/game/styles";
import { validateTeam } from "../domain/teamRules";
import { MAX_TEAM_SIZE, type Monster } from "../domain/types";
import { useAsync } from "../hooks/useAsync";
import { cn } from "../lib/utils";

const NO_MONSTERS: Monster[] = [];

export function TeamPage() {
  const { monsters, teams } = useServices();
  const load = useCallback(async () => {
    const [list, team] = await Promise.all([monsters.listMonsters(), teams.getTeam()]);
    return { list, teamIds: team.monsterIds };
  }, [monsters, teams]);
  const { state, reload } = useAsync(load);

  const [savedIds, setSavedIds] = useState<string[] | null>(null);
  const [draft, setDraft] = useState<string[] | null>(null);
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState<{ kind: "success" | "error"; text: string } | null>(null);

  const list = state.status === "success" ? state.data.list : NO_MONSTERS;
  const serverIds = savedIds ?? (state.status === "success" ? state.data.teamIds : []);
  const current = draft ?? serverIds;
  const byId = useMemo(() => new Map(list.map((m) => [m.id, m])), [list]);
  const validation = useMemo(() => validateTeam(current, new Set(list.map((m) => m.id))), [current, list]);
  const dirty = draft !== null && draft.join("|") !== serverIds.join("|");
  const full = current.length >= MAX_TEAM_SIZE;

  const edit = (next: string[]) => {
    setNotice(null);
    setDraft(next);
  };
  const move = (index: number, delta: -1 | 1) => {
    const next = [...current];
    [next[index], next[index + delta]] = [next[index + delta], next[index]];
    edit(next);
  };

  async function save() {
    setSaving(true);
    setNotice(null);
    try {
      const saved = await teams.saveTeam(current);
      setSavedIds(saved.monsterIds);
      setDraft(null);
      setNotice({ kind: "success", text: "Team saved." });
    } catch (error) {
      setNotice({ kind: "error", text: getErrorMessage(error) });
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <PageHeader title="Team builder" description={`Build an ordered team of up to ${MAX_TEAM_SIZE} monsters. The order decides the lineup in battle.`} />
      <PageContainer>
        {state.status === "loading" && <LoadingState label="Loading your team…" />}
        {state.status === "error" && <ErrorState message={state.message} onRetry={reload} />}
        {state.status === "success" && (
          <>
            <PageBanner
              image={BANNER.hero}
              tone="accent"
              eyebrow="Lineup"
              tagline="Pick your fighters. Slot #1 steps up first."
              chips={
                <>
                  <span className={cn(chip, "bg-bg/70 backdrop-blur-sm")}>{current.length} / {MAX_TEAM_SIZE} monsters</span>
                  <span className={cn(chip, "bg-bg/70 backdrop-blur-sm", validation.valid ? "text-grass" : "text-danger")}>
                    {validation.valid ? "Ready for battle" : "Not ready yet"}
                  </span>
                </>
              }
            />
            <Panel title="Your team" action={<span className="text-sm text-muted">{current.length} / {MAX_TEAM_SIZE}</span>}>
              <ol aria-label="Team slots" className="grid items-stretch gap-4 md:grid-cols-3">
                {Array.from({ length: MAX_TEAM_SIZE }, (_, index) => {
                  const monster: Monster | undefined = byId.get(current[index] ?? "");
                  return (
                    <li
                      key={index}
                      className={cn(
                        "flex min-h-[190px] flex-col rounded-xl border p-4 transition-colors",
                        monster
                          ? cn("border-line bg-gradient-to-br to-inset/60", CLASS_TINT[monster.monsterClass])
                          : "border-dashed border-accent/40 bg-inset/30",
                      )}
                    >
                      <div className="mb-2 font-pixel text-sm text-muted">Slot #{index + 1}</div>
                      {monster ? (
                        <div key={monster.id} className="flex flex-1 animate-pop flex-col justify-between gap-3">
                          <div className="flex items-center gap-3">
                            <MonsterAvatar monsterClass={monster.monsterClass} variant={monster.avatarVariant} size="sm" label={`${monster.name}, ${monster.monsterClass}`} className={cn("animate-bob", CLASS_AURA[monster.monsterClass])} />
                            <div className="min-w-0">
                              <div className="truncate font-medium">{monster.name}</div>
                              <div className="flex items-center gap-2 text-xs text-muted">Lv. {monster.level} <ClassBadge monsterClass={monster.monsterClass} showLabel /></div>
                            </div>
                          </div>
                          <div className="flex gap-2">
                            <button type="button" className={`${btnSecondary} px-2 py-1.5`} disabled={index === 0} onClick={() => move(index, -1)} aria-label={`Move ${monster.name} up`}>
                              <ArrowUp className="h-4 w-4" aria-hidden />
                            </button>
                            <button type="button" className={`${btnSecondary} px-2 py-1.5`} disabled={index >= current.length - 1} onClick={() => move(index, 1)} aria-label={`Move ${monster.name} down`}>
                              <ArrowDown className="h-4 w-4" aria-hidden />
                            </button>
                            <button type="button" className={btnDanger} onClick={() => edit(current.filter((id) => id !== monster.id))} aria-label={`Remove ${monster.name} from team`}>
                              <Trash2 className="h-4 w-4" aria-hidden /> Remove
                            </button>
                          </div>
                        </div>
                      ) : (
                        <div className="grid flex-1 place-items-center text-center text-sm text-muted">
                          <div>
                            <span aria-hidden className="mb-1 block animate-bob text-3xl text-accent/70">＋</span>
                            Empty slot. Add a monster below.
                          </div>
                        </div>
                      )}
                    </li>
                  );
                })}
              </ol>

              <div className="mt-4 space-y-3">
                {validation.valid ? (
                  <p role="status" className="flex items-center gap-2 text-sm font-medium text-grass">
                    <CheckCircle2 className="h-4 w-4" aria-hidden /> Valid team
                  </p>
                ) : (
                  <ul role="alert" className="space-y-1">
                    {validation.errors.map((e) => (
                      <li key={e} className="flex items-center gap-2 text-sm font-medium text-danger">
                        <XCircle className="h-4 w-4" aria-hidden /> {e}
                      </li>
                    ))}
                  </ul>
                )}
                {notice && (
                  <p role={notice.kind === "error" ? "alert" : "status"} className={`text-sm font-medium ${notice.kind === "error" ? "text-danger" : "text-grass"}`}>
                    {notice.text}
                  </p>
                )}
                <div className="flex flex-wrap gap-3">
                  <button type="button" className={btnPrimary} disabled={!validation.valid || !dirty || saving} onClick={save}>
                    {saving ? "Saving…" : "Save team"}
                  </button>
                  <button type="button" className={btnSecondary} disabled={!dirty || saving} onClick={() => edit(serverIds)}>
                    Reset changes
                  </button>
                  <Link to={paths.encounters} className={btnSecondary}>Choose an encounter</Link>
                </div>
              </div>
            </Panel>

            <Panel title="Available monsters">
              {full && <p className="mb-3 text-sm text-muted">Your team is full. Remove a monster to add another one.</p>}
              <ul className="grid items-stretch gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
                {list.map((m, i) => {
                  const inTeam = current.includes(m.id);
                  return (
                    <li key={m.id} className="flex h-full animate-rise items-center gap-3 rounded-lg border border-line bg-inset/60 p-3 transition-colors hover:border-accent" style={{ animationDelay: `${stagger(i, 40, 400)}ms` }}>
                      <MonsterAvatar monsterClass={m.monsterClass} variant={m.avatarVariant} size="sm" label={`${m.name}, ${m.monsterClass}`} />
                      <div className="min-w-0 flex-1">
                        <div className="truncate text-sm font-medium">{m.name}</div>
                        <div className="text-xs text-muted">Lv. {m.level} · {m.monsterClass}</div>
                      </div>
                      <button
                        type="button"
                        className={`${btnSecondary} px-2.5 py-1.5`}
                        disabled={inTeam || full}
                        onClick={() => edit([...current, m.id])}
                        aria-label={inTeam ? `${m.name} is already in the team` : `Add ${m.name} to team`}
                      >
                        <Plus className="h-4 w-4" aria-hidden /> {inTeam ? "In team" : "Add"}
                      </button>
                    </li>
                  );
                })}
              </ul>
            </Panel>
          </>
        )}
      </PageContainer>
    </>
  );
}
