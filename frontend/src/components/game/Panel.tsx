import { useId, type ReactNode } from "react";
import { cn } from "../../lib/utils";

interface PanelProps {
  title?: string;
  action?: ReactNode;
  children: ReactNode;
  className?: string;
}

export function Panel({ title, action, children, className }: PanelProps) {
  const headingId = useId();
  return (
    <section
      aria-labelledby={title ? headingId : undefined}
      className={cn("rounded-xl border border-line bg-card/90 p-4 shadow-lg shadow-black/10 backdrop-blur-sm sm:p-5", className)}
    >
      {title && (
        <div className="mb-3 flex items-center justify-between gap-3">
          <div className="min-w-0">
            <h2 id={headingId} className="font-pixel text-lg font-semibold text-fg">
              {title}
            </h2>
            <div className="mt-1 h-px w-10 bg-gradient-to-r from-accent to-transparent" />
          </div>
          {action}
        </div>
      )}
      {children}
    </section>
  );
}
