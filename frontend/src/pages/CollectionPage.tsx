import { useCallback, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { Search } from "lucide-react";
import { paths } from "../app/paths";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { EmptyState, ErrorState, LoadingState } from "../components/game/AsyncState";
import { MonsterCard } from "../components/game/MonsterCard";
import { PageContainer } from "../components/game/PageContainer";
import { PageBanner } from "../components/game/fx/PageBanner";
import { Reveal } from "../components/game/fx/Reveal";
import { BANNER, lift, stagger } from "../components/game/fx/fx";
import { Panel } from "../components/game/Panel";
import { btnSecondary, chip, fieldCls } from "../components/game/styles";
import { CLASS_INFO } from "../domain/classInfo";
import { MONSTER_CLASSES, type Monster, type MonsterClass } from "../domain/types";
import { useAsync } from "../hooks/useAsync";
import { cn } from "../lib/utils";

type SortKey = "level" | "talent" | "name" | "newest";
const SORTS: { value: SortKey; label: string }[] = [
  { value: "level", label: "Level (high to low)" },
  { value: "talent", label: "Talent (high to low)" },
  { value: "name", label: "Name (A–Z)" },
  { value: "newest", label: "Newest first" },
];

function filterAndSort(monsters: Monster[], query: string, monsterClass: MonsterClass | "All", sort: SortKey, onlyPoints: boolean) {
  const q = query.trim().toLowerCase();
  return monsters
    .filter((m) => (monsterClass === "All" || m.monsterClass === monsterClass) && (!onlyPoints || m.pendingPoints > 0) && (!q || m.name.toLowerCase().includes(q)))
    .sort((a, b) => {
      switch (sort) {
        case "level": return b.level - a.level || b.xp - a.xp;
        case "talent": return b.talent - a.talent;
        case "name": return a.name.localeCompare(b.name);
        case "newest": return b.obtainedAt.localeCompare(a.obtainedAt);
      }
    });
}

export function CollectionPage() {
  const { monsters, teams } = useServices();
  const load = useCallback(async () => {
    const [list, team] = await Promise.all([monsters.listMonsters(), teams.getTeam()]);
    return { list, teamIds: team.monsterIds };
  }, [monsters, teams]);
  const { state, reload } = useAsync(load);

  const [query, setQuery] = useState("");
  const [monsterClass, setMonsterClass] = useState<MonsterClass | "All">("All");
  const [sort, setSort] = useState<SortKey>("level");
  const [onlyPoints, setOnlyPoints] = useState(false);

  const visible = useMemo(
    () => (state.status === "success" ? filterAndSort(state.data.list, query, monsterClass, sort, onlyPoints) : []),
    [state, query, monsterClass, sort, onlyPoints],
  );
  const clearFilters = () => {
    setQuery("");
    setMonsterClass("All");
    setOnlyPoints(false);
  };

  return (
    <>
      <PageHeader title="Collection" description="All the monsters you own. Open a monster to see its stats and spend stat points." />
      <PageContainer>
        {state.status === "loading" && <LoadingState label="Loading your collection…" />}
        {state.status === "error" && <ErrorState message={state.message} onRetry={reload} />}
        {state.status === "success" && (
          <>
            <PageBanner
              image={BANNER.scene}
              position="center 62%"
              tone="grass"
              eyebrow="Your monsters"
              tagline="Grow a team worth fighting for."
              chips={
                <>
                  <span className={cn(chip, "bg-bg/70 backdrop-blur-sm")}>{state.data.list.length} monsters</span>
                  <span className={cn(chip, "bg-bg/70 text-gold backdrop-blur-sm")}>
                    {state.data.list.reduce((n, m) => n + m.pendingPoints, 0)} stat points to spend
                  </span>
                </>
              }
            />
            <Panel>
              <div role="search" aria-label="Filter monsters" className="grid gap-3 md:grid-cols-[1fr_auto_auto] md:items-end">
                <label className="block text-sm">
                  <span className="mb-1 block text-muted">Search by name</span>
                  <span className="relative block">
                    <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" aria-hidden />
                    <input type="search" value={query} onChange={(e) => setQuery(e.target.value)} placeholder="e.g. Fox" className={cn(fieldCls, "w-full pl-9")} />
                  </span>
                </label>
                <label className="block text-sm">
                  <span className="mb-1 block text-muted">Sort by</span>
                  <select value={sort} onChange={(e) => setSort(e.target.value as SortKey)} className={cn(fieldCls, "w-full")}>
                    {SORTS.map((s) => <option key={s.value} value={s.value}>{s.label}</option>)}
                  </select>
                </label>
                <label className="flex h-10 items-center gap-2 text-sm">
                  <input type="checkbox" checked={onlyPoints} onChange={(e) => setOnlyPoints(e.target.checked)} className="h-4 w-4 accent-[var(--accent)]" />
                  Has unspent points
                </label>
              </div>
              <div role="group" aria-label="Filter by class" className="mt-3 flex flex-wrap gap-2">
                {(["All", ...MONSTER_CLASSES] as const).map((c) => (
                  <button
                    key={c}
                    type="button"
                    aria-pressed={monsterClass === c}
                    onClick={() => setMonsterClass(c)}
                    className={cn(btnSecondary, "px-3 py-1.5", monsterClass === c && "border-accent bg-accent/15 text-accent")}
                  >
                    {c !== "All" && (
                      <span aria-hidden className={cn("grid h-4 w-4 place-items-center rounded font-pixel text-[10px] text-white", CLASS_INFO[c].badgeClass)}>
                        {CLASS_INFO[c].letter}
                      </span>
                    )}
                    {c}
                  </button>
                ))}
              </div>
            </Panel>

            <p aria-live="polite" className="text-sm text-muted">
              Showing {visible.length} of {state.data.list.length} monsters
            </p>

            {state.data.list.length === 0 ? (
              <EmptyState
                title="Your collection is empty"
                description="Win encounters to hatch eggs and grow your collection."
                action={<Link to={paths.encounters} className={btnSecondary}>Go to encounters</Link>}
              />
            ) : visible.length === 0 ? (
              <EmptyState
                title="No monsters match your filters"
                action={<button type="button" onClick={clearFilters} className={btnSecondary}>Clear filters</button>}
              />
            ) : (
              <ul className="grid items-stretch gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
                {visible.map((m, index) => {
                  const slot = state.data.teamIds.indexOf(m.id);
                  return (
                    <Reveal as="li" key={m.id} delay={stagger(index)} className={lift}>
                      <MonsterCard monster={m} teamSlot={slot >= 0 ? slot + 1 : undefined} />
                    </Reveal>
                  );
                })}
              </ul>
            )}
          </>
        )}
      </PageContainer>
    </>
  );
}
