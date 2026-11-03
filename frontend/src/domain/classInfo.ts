import type { MonsterClass, StatKey } from "./types";

export interface ClassInfo {
  letter: string;
  role: string;
  description: string;
  badgeClass: string;
  gradient: string;
  emojis: readonly [string, string, string];
}

export const CLASS_INFO: Record<MonsterClass, ClassInfo> = {
  Fighter: {
    letter: "F",
    role: "Damage",
    description: "Front-line attackers that focus on dealing damage.",
    badgeClass: "bg-orange-500",
    gradient: "from-orange-500/40 to-amber-800/30",
    emojis: ["🦊", "🐯", "🐗"],
  },
  Guardian: {
    letter: "G",
    role: "Protection",
    description: "Sturdy defenders that protect the rest of the team.",
    badgeClass: "bg-sky-500",
    gradient: "from-sky-500/40 to-blue-900/30",
    emojis: ["🐻", "🦏", "🐢"],
  },
  Shaman: {
    letter: "S",
    role: "Support",
    description: "Supporters that heal and strengthen their allies.",
    badgeClass: "bg-emerald-500",
    gradient: "from-emerald-500/40 to-teal-900/30",
    emojis: ["🐉", "🦉", "🐸"],
  },
  Trickster: {
    letter: "T",
    role: "Control",
    description: "Cunning controllers that disrupt the enemy.",
    badgeClass: "bg-violet-500",
    gradient: "from-violet-500/40 to-purple-900/30",
    emojis: ["🦝", "🐍", "🦎"],
  },
};

export const STAT_LABELS: Record<StatKey, string> = {
  strength: "Strength",
  hp: "HP",
  accuracy: "Accuracy",
  agility: "Agility",
};

/** Upper bound used only to scale the stat bars visually. */
export const STAT_SCALE: Record<StatKey, number> = {
  strength: 60,
  hp: 300,
  accuracy: 100,
  agility: 100,
};
