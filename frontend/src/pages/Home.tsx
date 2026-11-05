import { Link } from "react-router-dom";
import { useCallback } from "react";
import { ArrowRight, Check, Plus } from "lucide-react";
import SceneImage from "@/components/SceneImage";
import { cn } from "@/lib/utils";
import { useServices } from "@/api/useServices";
import { ErrorState, LoadingState } from "@/components/game/AsyncState";
import { MonsterAvatar } from "@/components/game/MonsterAvatar";
import { useAsync } from "@/hooks/useAsync";
import { CLASS_INFO } from "@/domain/classInfo";
import { MAX_TEAM_SIZE, type Encounter, type Monster, type MonsterClass, type PlayerProgress } from "@/domain/types";
import { surfaceCard } from "@/components/game/styles";
import { Fireflies } from "@/components/game/fx/Fireflies";
import { FLOAT_STAGGER } from "@/components/game/fx/fx";
import { THEME_ART, encounterArt } from "@/components/game/fx/encounterArt";

const activity = [
  { icon: "act-level", title: "Fierce Fox reached level 4", sub: "1 stat point to spend", time: "2h ago" },
  { icon: "act-battle", title: "Won against Forest Scout", sub: "+48 XP (Talent 112)", time: "3h ago" },
  { icon: "act-catch", title: "Egg hatched: Sturdy Cub", sub: "A new Guardian joined your collection!", time: "5h ago" },
];

const Px = ({ name, className }: { name: string; className?: string }) => (
  <img src={`/assets/${name}.png`} alt="" className={cn("pixelated object-contain", className)} />
);

function ClassBadge({ name, label, className }: { name: MonsterClass; label?: boolean; className?: string }) {
  const c = CLASS_INFO[name];
  return (
    <span className={cn("inline-flex items-center gap-1.5", className)} title={`${name} – ${c.role}`}>
      <span className={cn("grid h-5 w-5 place-items-center rounded font-pixel text-xs font-bold text-white", c.badgeClass)}>
        {c.letter}
      </span>
      {label && <span className="text-xs text-muted">{name}</span>}
    </span>
  );
}

function Card({ className, children }: { className?: string; children: React.ReactNode }) {
  return (
    <section className={cn(surfaceCard, className)}>
      {children}
    </section>
  );
}

function PrimaryButton({ to, children, className }: { to: string; children: React.ReactNode; className?: string }) {
  return (
    <Link to={to} className={cn(
      "inline-flex items-center justify-center gap-2 rounded-md bg-accent px-5 py-2.5 text-sm font-semibold text-accent-fg transition-[filter] hover:brightness-110", className)}>
      {children}
      <ArrowRight className="h-4 w-4" />
    </Link>
  );
}

function Hero({ playerName }: { playerName: string }) {
  return (
    <SceneImage src="/assets/hero-banner.png" className="h-[176px] bg-[#0a1830] [&>img]:object-[left_center]">
      <Fireflies count={10} />
      <div className="relative z-10 flex h-full flex-col justify-center px-10 sm:px-[76px]">
        <h1 className="font-pixel text-[34px] font-bold leading-tight text-white drop-shadow-[0_2px_0_rgba(0,0,0,0.6)]">
          Welcome back, <span className="text-gold">{playerName}!</span>
        </h1>
        <p className="mt-2 text-[15px] text-white/85 drop-shadow">“Generate. Train. Battle. Defeat the final boss.”</p>
      </div>
    </SceneImage>
  );
}

function GoalCard() {
  return (
    <SceneImage src="/assets/goal.png" className="min-h-[196px] rounded-xl border border-line bg-[#0b2a3a] [&>img]:object-[right_center]">
      <div className="absolute inset-0 bg-gradient-to-r from-[#0b1f26]/90 via-[#0b1f26]/55 to-transparent" />
      <div className="relative z-10 flex h-full flex-col p-6">
        <div className="text-[13px] font-medium uppercase text-accent">Current goal</div>
        <h2 className="mt-1 font-pixel text-[28px] font-bold leading-tight text-white">Continue your journey</h2>
        <p className="mt-2 max-w-[330px] text-sm text-white/85">
          Develop your monsters and defeat the final boss. The Misty Forest awaits...
        </p>
        <PrimaryButton to="/encounters" className="mt-5 w-fit animate-glow px-7 py-3">Go to Encounters</PrimaryButton>
      </div>
    </SceneImage>
  );
}

function PlayerCard({ progress }: { progress: PlayerProgress }) {
  const pct = Math.round((progress.encountersCleared / progress.encountersTotal) * 100);
  const tiles = [
    { label: "Collection", value: String(progress.collectionSize) },
    { label: "Team", value: `${progress.teamSize} / ${MAX_TEAM_SIZE}` },
    { label: "Stat points", value: String(progress.pendingPoints) },
    { label: "Final boss", value: progress.finalBossDefeated ? "Defeated" : "Locked" },
  ];
  return (
    <Card className="p-5">
      <div className="flex items-center gap-4">
        <img src="/assets/avatar-card.png" alt="" className="pixelated h-[68px] w-[68px] rounded-lg ring-1 ring-line" />
        <div className="min-w-0 flex-1">
          <div className="font-pixel text-xl font-bold">{progress.playerName}</div>
          <div className="mt-1 flex items-baseline justify-between">
            <span className="text-sm text-muted">Encounter progress</span>
            <span className="text-xs text-muted">{progress.encountersCleared} / {progress.encountersTotal} cleared</span>
          </div>
          <div className="mt-1.5 h-2 overflow-hidden rounded-full bg-inset">
            <div className="grow-x h-full rounded-full bg-accent" style={{ width: `${pct}%` }} />
          </div>
        </div>
      </div>
      <div className="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-4">
        {tiles.map((t) => (
          <div key={t.label} className="rounded-lg border border-line bg-inset/60 px-3 py-3">
            <div className="truncate text-xs text-muted">{t.label}</div>
            <div className="mt-1.5 whitespace-nowrap font-pixel text-xl font-semibold">{t.value}</div>
          </div>
        ))}
      </div>
    </Card>
  );
}

function TeamCard({ team }: { team: Monster[] }) {
  const empty = Array.from({ length: Math.max(0, MAX_TEAM_SIZE - team.length) });
  const valid = team.length >= 1 && team.length <= MAX_TEAM_SIZE;
  return (
    <Card className="flex flex-col p-4">
      <div className="mb-3 flex items-center justify-between gap-3">
        <div className="flex items-center gap-3">
          <h3 className="font-pixel text-lg font-semibold">Your Team</h3>
          {valid && (
            <span className="inline-flex items-center gap-1 rounded-md bg-grass/15 px-2 py-0.5 text-xs font-medium text-grass">
              <Check className="h-3 w-3" /> Valid team
            </span>
          )}
        </div>
        <Link to="/team" className="flex items-center gap-1.5 text-sm font-medium text-accent">
          View team <ArrowRight className="h-4 w-4" />
        </Link>
      </div>
      <div className="grid flex-1 grid-cols-2 gap-3 md:grid-cols-3">
        {team.map((m, i) => (
          <div key={m.id} className="relative flex flex-col rounded-lg border border-line bg-inset/60 p-3">
            <span className="absolute left-2 top-2 font-pixel text-sm text-muted">#{i + 1}</span>
            {m.pendingPoints > 0 && (
              <span className="absolute right-2 top-2 rounded bg-gold/20 px-1.5 py-0.5 text-[11px] font-semibold text-gold">
                +{m.pendingPoints} pt
              </span>
            )}
            <div className="flex flex-1 items-end justify-center pt-4">
              <MonsterAvatar monsterClass={m.monsterClass} variant={m.avatarVariant} size="md" label={m.name} className={cn("animate-float", FLOAT_STAGGER[i % 3])} />
            </div>
            <div className="mt-2 flex items-center justify-between gap-2">
              <span className="truncate font-pixel text-base font-semibold">{m.name}</span>
              <ClassBadge name={m.monsterClass} />
            </div>
            <div className="flex items-center justify-between text-xs text-muted">
              <span>Lv. {m.level}</span>
              <span>{m.xpToNext === null ? "Max level" : `${m.xp} / ${m.xpToNext} XP`}</span>
            </div>
            <div className="mt-2 h-2 overflow-hidden rounded-full bg-bg/60">
              <div className="grow-x h-full rounded-full bg-grass" style={{ width: `${m.xpToNext === null ? 100 : Math.round((m.xp / m.xpToNext) * 100)}%` }} />
            </div>
          </div>
        ))}
        {empty.map((_, i) => (
          <Link key={i} to="/team"
            className="flex min-h-[150px] flex-col items-center justify-center gap-1 rounded-lg border border-dashed border-line bg-inset/30 text-sm text-muted">
            <Plus className="h-6 w-6 text-fg" />
            Add Monster
            <span className="text-xs">Slot #{team.length + i + 1}</span>
          </Link>
        ))}
      </div>
    </Card>
  );
}

function NextEncounterCard({ encounter, total }: { encounter: Encounter; total: number }) {
  const e = encounter;
  return (
    <Card className="p-4">
      <h3 className="mb-3 font-pixel text-lg font-semibold">Next Encounter</h3>
      <div className="flex gap-4">
        <SceneImage src={encounterArt(e)} className="h-[150px] w-[130px] shrink-0 rounded-md bg-[#2a6b4a]" />
        <div className="min-w-0 flex-1">
          <div className="font-pixel text-lg font-semibold">{e.name}</div>
          <div className="text-sm text-muted">Encounter {e.order} of {total}</div>
          <span className="mt-2 inline-block rounded-md border border-line bg-inset px-2.5 py-1 text-xs text-muted">
            Egg chance {e.eggChance}%
          </span>
          <div className="mt-2 flex items-center gap-1.5">
            <span className="mr-1 text-xs text-muted">Egg classes</span>
            {e.rewardClasses.map((c) => <ClassBadge key={c} name={c} />)}
          </div>
          <div className="mt-2 flex gap-2">
            {e.opponents.map((opponent, i) => (
              <MonsterAvatar key={opponent.id} monsterClass={opponent.monsterClass} variant={opponent.avatarVariant} size="sm" label={opponent.name} className={cn("animate-bob", FLOAT_STAGGER[i % 3])} />
            ))}
          </div>
        </div>
      </div>
      <PrimaryButton to="/encounters" className="mt-3 w-full animate-glow">Prepare for Battle</PrimaryButton>
    </Card>
  );
}

function RecentActivityCard() {
  return (
    <Card className="p-4">
      <h3 className="mb-3 font-pixel text-base font-semibold">Recent Activity</h3>
      <ul className="space-y-3">
        {activity.map((a) => (
          <li key={a.title} className="flex items-center gap-3">
            <div className="grid h-10 w-10 shrink-0 place-items-center"><Px name={a.icon} className="h-9 w-9" /></div>
            <div className="min-w-0 flex-1">
              <div className="truncate text-sm font-semibold">{a.title}</div>
              <div className="truncate text-xs text-muted">{a.sub}</div>
            </div>
            <span className="self-start text-xs text-muted">{a.time}</span>
          </li>
        ))}
      </ul>
    </Card>
  );
}

function FinalBossCard({ defeated }: { defeated: boolean }) {
  return (
    <Card className="p-4">
      <h3 className="mb-3 font-pixel text-base font-semibold">Final Boss</h3>
      <div className="flex gap-4">
        <SceneImage src={THEME_ART.summit} className="h-[130px] w-[114px] shrink-0 rounded-md bg-[#5b86c4]" />
        <div className="min-w-0">
          <span className="rounded bg-violet-600 px-2 py-0.5 text-[10px] font-semibold uppercase text-white">Objective</span>
          <div className="mt-1.5 font-pixel text-base font-semibold">
            {defeated ? "Defeated" : "Not yet defeated"}
          </div>
          <p className="mt-1 text-sm leading-snug text-muted">
            Clear the encounters, grow your team and take on the final boss. It never drops an egg.
          </p>
          <Link to="/encounters" className="mt-1 inline-flex items-center gap-1.5 text-sm font-medium text-accent">
            View encounters <ArrowRight className="h-3.5 w-3.5" />
          </Link>
        </div>
      </div>
    </Card>
  );
}

function TipCard() {
  return (
    <Card className="relative min-h-[190px] overflow-hidden p-4 xl:bg-gradient-to-r xl:from-card/95 xl:via-card/75 xl:to-transparent xl:backdrop-blur-0">
      <div className="flex items-center gap-2.5">
        <Px name="bulb" className="h-5 w-5" />
        <h3 className="font-pixel text-base font-semibold">Tip of the Day</h3>
      </div>
      <p className="mt-3 max-w-[215px] text-sm leading-relaxed text-muted">
        Mix classes in your team: Fighters deal damage, Guardians protect, Shamans support and Tricksters control.
      </p>
    </Card>
  );
}

export default function Home() {
  const { players, monsters, teams, encounters } = useServices();
  const load = useCallback(async () => {
    const [progress, monsterList, teamState, encounterList] = await Promise.all([
      players.getProgress(),
      monsters.listMonsters(),
      teams.getTeam(),
      encounters.listEncounters(),
    ]);
    const byId = new Map(monsterList.map((monster) => [monster.id, monster]));
    const team = teamState.monsterIds.flatMap((id) => {
      const monster = byId.get(id);
      return monster ? [monster] : [];
    });
    const nextEncounter = encounterList.find((encounter) => encounter.status === "available") ?? encounterList[0];
    return { progress, team, nextEncounter };
  }, [players, monsters, teams, encounters]);
  const { state, reload } = useAsync(load);

  if (state.status === "loading") return <LoadingState label="Loading your dashboard…" />;
  if (state.status === "error") return <ErrorState message={state.message} onRetry={reload} />;
  const { progress, team, nextEncounter } = state.data;

  return (
    <div className="relative isolate overflow-hidden">
      <Hero playerName={progress.playerName} />
      <div className="w-full space-y-6 px-4 pb-[130px] pt-6 lg:px-6">
        <div className="grid animate-rise gap-5 [animation-delay:60ms] xl:grid-cols-[1.66fr_1fr]">
          <GoalCard />
          <PlayerCard progress={progress} />
        </div>
        <div className="grid animate-rise gap-5 [animation-delay:160ms] xl:grid-cols-[1.66fr_1fr]">
          <TeamCard team={team} />
          {nextEncounter ? <NextEncounterCard encounter={nextEncounter} total={progress.encountersTotal} /> : <Card className="p-4">No encounters available.</Card>}
        </div>
        <div className="grid animate-rise gap-5 [animation-delay:260ms] lg:grid-cols-2 xl:grid-cols-[1fr_1.15fr_1.1fr]">
          <RecentActivityCard />
          <FinalBossCard defeated={progress.finalBossDefeated} />
          <TipCard />
        </div>
      </div>
    </div>
  );
}
