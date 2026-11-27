import type { MonsterSpecies } from "../../domain/types";
import { CLASS_INFO } from "../../domain/classInfo";
import { ClassBadge } from "./ClassBadge";
import { MonsterAvatar } from "./MonsterAvatar";
import { chip, surfaceCard } from "./styles";

export function MonsterSpeciesCard({ species }: { species: MonsterSpecies }) {
  const { baseStats } = species;

  return (
    <article className={`${surfaceCard} flex h-full min-h-[250px] flex-col gap-4 p-4 sm:p-5`}>
      <div className="flex items-start gap-3">
        <MonsterAvatar
          monsterClass={species.monsterClass}
          variant={species.avatarVariant}
          size="md"
          label={`${species.name}, ${species.monsterClass}`}
        />
        <div className="min-w-0 flex-1">
          <div className="flex items-center justify-between gap-2">
            <span className="truncate font-pixel text-lg font-semibold">{species.name}</span>
            <ClassBadge monsterClass={species.monsterClass} />
          </div>
          <div className="mt-0.5 text-sm text-muted">
            {species.monsterClass} · {CLASS_INFO[species.monsterClass].role}
          </div>
          <div className="mt-2 flex flex-wrap gap-1.5">
            <span className={chip} title={species.skill.description}>Skill: {species.skill.name}</span>
            <span className={chip} title={species.trait.description}>Trait: {species.trait.name}</span>
          </div>
        </div>
      </div>

      <dl className="mt-auto grid grid-cols-4 gap-2 rounded-lg border border-line bg-inset/60 px-3 py-2 text-center text-xs">
        <div>
          <dt className="text-muted">STR</dt>
          <dd className="font-pixel font-semibold">{baseStats.strength}</dd>
        </div>
        <div>
          <dt className="text-muted">HP</dt>
          <dd className="font-pixel font-semibold">{baseStats.hp}</dd>
        </div>
        <div>
          <dt className="text-muted">ACC</dt>
          <dd className="font-pixel font-semibold">{baseStats.accuracy}</dd>
        </div>
        <div>
          <dt className="text-muted">AGI</dt>
          <dd className="font-pixel font-semibold">{baseStats.agility}</dd>
        </div>
      </dl>
    </article>
  );
}