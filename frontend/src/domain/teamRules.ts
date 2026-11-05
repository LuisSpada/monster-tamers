import { MAX_TEAM_SIZE, MIN_TEAM_SIZE } from "./types";

export interface TeamValidation {
  valid: boolean;
  errors: string[];
}

/**
 * Client-side mirror of the team rules, used for immediate feedback only.
 * The backend remains authoritative.
 */
export function validateTeam(monsterIds: readonly string[], ownedIds?: ReadonlySet<string>): TeamValidation {
  const errors: string[] = [];
  if (monsterIds.length < MIN_TEAM_SIZE) {
    errors.push(`A team needs at least ${MIN_TEAM_SIZE} monster.`);
  }
  if (monsterIds.length > MAX_TEAM_SIZE) {
    errors.push(`A team can have at most ${MAX_TEAM_SIZE} monsters.`);
  }
  if (new Set(monsterIds).size !== monsterIds.length) {
    errors.push("A monster can only appear once in a team.");
  }
  if (ownedIds && monsterIds.some((id) => !ownedIds.has(id))) {
    errors.push("The team contains a monster that is not in your collection.");
  }
  return { valid: errors.length === 0, errors };
}
