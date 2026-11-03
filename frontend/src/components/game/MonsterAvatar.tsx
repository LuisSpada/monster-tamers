import { CLASS_INFO } from "../../domain/classInfo";
import type { MonsterClass } from "../../domain/types";
import { cn } from "../../lib/utils";

const SIZES = { sm: "h-12 w-12 text-2xl", md: "h-20 w-20 text-4xl", lg: "h-32 w-32 text-6xl" } as const;

interface MonsterAvatarProps {
  monsterClass: MonsterClass;
  variant?: number;
  size?: keyof typeof SIZES;
  label?: string;
  className?: string;
}

/** Emoji + gradient avatar. Swap the inner element for a sprite once pixel-art assets exist. */
export function MonsterAvatar({ monsterClass, variant = 0, size = "md", label, className }: MonsterAvatarProps) {
  const info = CLASS_INFO[monsterClass];
  const emoji = info.emojis[Math.abs(variant) % info.emojis.length];
  return (
    <div
      role="img"
      aria-label={label ?? `${monsterClass} monster`}
      className={cn(
        "grid shrink-0 place-items-center rounded-xl border border-line bg-gradient-to-br shadow-inner",
        info.gradient,
        SIZES[size],
        className,
      )}
    >
      <span aria-hidden>{emoji}</span>
    </div>
  );
}
