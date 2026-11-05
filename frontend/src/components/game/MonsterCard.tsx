import { Link } from "react-router-dom";
import { Sparkles } from "lucide-react";
import { paths } from "../../app/paths";
import { MAX_LEVEL, type Monster } from "../../domain/types";
import { ClassBadge } from "./ClassBadge";
import { MonsterAvatar } from "./MonsterAvatar";
import { ProgressBar } from "./ProgressBar";
import { chip, listCard } from "./styles";

export function MonsterCard({ monster, teamSlot }: { monster: Monster; teamSlot?: number }) {
  return (
    <Link
      to={paths.monster(monster.id)}
      className={`${listCard} group flex h-full min-h-[250px] flex-col gap-4 p-4 sm:p-5`}
    >
      <div className="flex items-start gap-3">
        <MonsterAvatar monsterClass={monster.monsterClass} variant={monster.avatarVariant} size="md" label={`${monster.name}, ${monster.monsterClass}`} />
        <div className="min-w-0 flex-1">
          <div className="flex items-center justify-between gap-2">
            <span className="truncate font-pixel text-lg font-semibold">{monster.name}</span>
            <ClassBadge monsterClass={monster.monsterClass} />
          </div>
          <div className="mt-0.5 text-sm text-muted">
            {monster.monsterClass} · Lv. {monster.level}
            {monster.level >= MAX_LEVEL ? " (max)" : ""}
          </div>
          <div className="mt-2 flex flex-wrap gap-1.5">
            <span className={chip}>Talent {monster.talent}</span>
            {teamSlot !== undefined && <span className={chip}>Team #{teamSlot}</span>}
            {monster.pendingPoints > 0 && (
              <span className="inline-flex items-center gap-1 rounded-md bg-gold/20 px-2 py-0.5 text-xs font-semibold text-gold">
                <Sparkles className="h-3 w-3" aria-hidden />
                {monster.pendingPoints} stat {monster.pendingPoints === 1 ? "point" : "points"}
              </span>
            )}
          </div>
        </div>
      </div>
      <div className="mt-auto">
        <div className="mb-1 flex justify-between text-xs text-muted">
          <span>XP</span>
          <span>{monster.xpToNext === null ? "Max level" : `${monster.xp} / ${monster.xpToNext}`}</span>
        </div>
        <ProgressBar value={monster.xpToNext === null ? 1 : monster.xp} max={monster.xpToNext ?? 1} label={`${monster.name} experience`} barClassName="bg-grass" />
      </div>
    </Link>
  );
}
