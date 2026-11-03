import type { ElementType, ReactNode } from "react";
import { cn } from "../../../lib/utils";

interface RevealProps {
  as?: "div" | "li" | "section";
  /** Delay in ms, usually `stagger(index)`. */
  delay?: number;
  className?: string;
  children: ReactNode;
}

/** Fades and slides its content in when it mounts. */
export function Reveal({ as, delay = 0, className, children }: RevealProps) {
  const Tag: ElementType = as ?? "div";
  return (
    <Tag className={cn("animate-rise", className)} style={{ animationDelay: `${delay}ms` }}>
      {children}
    </Tag>
  );
}
