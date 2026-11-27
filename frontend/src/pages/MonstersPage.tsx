import { useCallback, useMemo, useState } from "react";
import { Search } from "lucide-react";
import { useServices } from "../api/useServices";
import { PageHeader } from "../components/common/PageHeader";
import { EmptyState, ErrorState, LoadingState } from "../components/game/AsyncState";
import { MonsterSpeciesCard } from "../components/game/MonsterSpeciesCard";
import { PageContainer } from "../components/game/PageContainer";
import { PageBanner } from "../components/game/fx/PageBanner";
import { Reveal } from "../components/game/fx/Reveal";
import { BANNER, lift, stagger } from "../components/game/fx/fx";
import { Panel } from "../components/game/Panel";
import { btnSecondary, chip, fieldCls } from "../components/game/styles";
import { CLASS_INFO } from "../domain/classInfo";
import { MONSTER_CLASSES, type MonsterClass, type MonsterSpecies } from "../domain/types";
import { useAsync } from "../hooks/useAsync";
import { cn } from "../lib/utils";

type SortKey = "name" | "strength" | "hp" | "agility";
const SORTS: { value: SortKey; label: string }[] = [
  { value: "name", label: "Name (A–Z)" },
  { value: "strength", label: "Strength (high to low)" },
  { value: "hp", label: "HP (high to low)" },
  { value: "agility", label: "Agility (high to low)" },
];

function filterAndSort(
  species: MonsterSpecies[],
  query: string,
  monsterClass: MonsterClass | "All",
  sort: SortKey,
) {
  const q = query.trim().toLowerCase();
  return species
    .filter((s) => (monsterClass === "All" || s.monsterClass === monsterClass) && (!q || s.name.toLowerCase().includes(q)))
    .sort((a, b) => {
      switch (sort) {
        case "name": return a.name.localeCompare(b.name);
        case "strength": return b.baseStats.strength - a.baseStats.strength;
        case "hp": return b.baseStats.hp - a.baseStats.hp;
        case "agility": return b.baseStats.agility - a.baseStats.agility;
      }
    });
}

export function MonstersPage() {
  const { monsters } = useServices();
  const load = useCallback(() => monsters.listSpecies(), [monsters]);
  const { state, reload } = useAsync(load);

  const [query, setQuery] = useState("");
  const [monsterClass, setMonsterClass] = useState<MonsterClass | "All">("All");
  const [sort, setSort] = useState<SortKey>("name");

  const visible = useMemo(
    () => (state.status === "success" ? filterAndSort(state.data, query, monsterClass, sort) : []),
    [state, query, monsterClass, sort],
  );

  const clearFilters = () => {
    setQuery("");
    setMonsterClass("All");
  };

  return (
    <>
      <PageHeader
        title="Monsters"
        description="All monster species in the game. Learn what you may encounter or obtain."
      />
      <PageContainer>
        {state.status === "loading" && <LoadingState label="Loading monster catalogue…" />}
        {state.status === "error" && <ErrorState message={state.message} onRetry={reload} />}
        {state.status === "success" && (
          <>
            <PageBanner
              image={BANNER.scene}
              position="center 62%"
              tone="grass"
              eyebrow="Monster catalogue"
              tagline="Know every species in the wild."
              chips={
                <span className={cn(chip, "bg-bg/70 backdrop-blur-sm")}>
                  {state.data.length} species
                </span>
              }
            />
            <Panel>
              <div role="search" aria-label="Filter monster species" className="grid gap-3 md:grid-cols-[1fr_auto] md:items-end">
                <label className="block text-sm">
                  <span className="mb-1 block text-muted">Search by name</span>
                  <span className="relative block">
                    <Search className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" aria-hidden />
                    <input
                      type="search"
                      value={query}
                      onChange={(e) => setQuery(e.target.value)}
                      placeholder="e.g. Fox"
                      className={cn(fieldCls, "w-full pl-9")}
                    />
                  </span>
                </label>
                <label className="block text-sm">
                  <span className="mb-1 block text-muted">Sort by</span>
                  <select value={sort} onChange={(e) => setSort(e.target.value as SortKey)} className={cn(fieldCls, "w-full")}>
                    {SORTS.map((s) => <option key={s.value} value={s.value}>{s.label}</option>)}
                  </select>
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
              Showing {visible.length} of {state.data.length} species
            </p>

            {state.data.length === 0 ? (
              <EmptyState title="No monster species available" description="The monster catalogue is currently empty." />
            ) : visible.length === 0 ? (
              <EmptyState
                title="No species match your filters"
                action={<button type="button" onClick={clearFilters} className={btnSecondary}>Clear filters</button>}
              />
            ) : (
              <ul className="grid items-stretch gap-5 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-5">
                {visible.map((s, index) => (
                  <Reveal as="li" key={s.id} delay={stagger(index)} className={lift}>
                    <MonsterSpeciesCard species={s} />
                  </Reveal>
                ))}
              </ul>
            )}
          </>
        )}
      </PageContainer>
    </>
  );
}