import type { ReactNode } from "react";
import { AlertTriangle, Inbox, Loader2 } from "lucide-react";
import { btnSecondary } from "./styles";

export function LoadingState({ label = "Loading…" }: { label?: string }) {
  return (
    <div role="status" aria-live="polite" className="rounded-xl border border-line bg-card/90 p-8 text-center">
      <Loader2 className="mx-auto mb-3 h-8 w-8 animate-spin text-accent motion-reduce:animate-none" aria-hidden />
      <p className="text-sm text-muted">{label}</p>
    </div>
  );
}

export function ErrorState({ message, onRetry }: { message: string; onRetry?: () => void }) {
  return (
    <div role="alert" className="rounded-xl border border-danger/40 bg-danger/10 p-6 text-center">
      <AlertTriangle className="mx-auto mb-3 h-8 w-8 text-danger" aria-hidden />
      <p className="font-medium">Something went wrong</p>
      <p className="mt-1 text-sm text-muted">{message}</p>
      {onRetry && (
        <button type="button" onClick={onRetry} className={`${btnSecondary} mt-4`}>
          Try again
        </button>
      )}
    </div>
  );
}

export function EmptyState({ title, description, action }: { title: string; description?: string; action?: ReactNode }) {
  return (
    <div className="rounded-xl border border-dashed border-line bg-card/70 p-8 text-center">
      <Inbox className="mx-auto mb-3 h-8 w-8 text-muted" aria-hidden />
      <p className="font-medium">{title}</p>
      {description && <p className="mt-1 text-sm text-muted">{description}</p>}
      {action && <div className="mt-4">{action}</div>}
    </div>
  );
}
