import { useCallback, useEffect, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { Bell, ChevronDown, LogOut, Settings, User } from "lucide-react";
import { cn } from "@/lib/utils";
import { useServices } from "@/api/useServices";
import { useAsync } from "@/hooks/useAsync";

const notifications = [
  { id: "level", icon: "act-level", title: "Monster progress updated", sub: "Review your collection", time: "Recently", unread: true },
  { id: "battle", icon: "act-battle", title: "Battle history is available", sub: "Review your latest encounter", time: "Recently", unread: true },
];

/** Lukker en meny ved klikk utenfor eller Escape. */
function useMenu() {
  const [open, setOpen] = useState(false);
  const ref = useRef<HTMLDivElement>(null);
  useEffect(() => {
    if (!open) return;
    const onDown = (e: MouseEvent) => { if (!ref.current?.contains(e.target as Node)) setOpen(false); };
    const onKey = (e: KeyboardEvent) => { if (e.key === "Escape") setOpen(false); };
    document.addEventListener("mousedown", onDown);
    document.addEventListener("keydown", onKey);
    return () => { document.removeEventListener("mousedown", onDown); document.removeEventListener("keydown", onKey); };
  }, [open]);
  return { open, setOpen, ref };
}

const panel = "absolute right-0 top-[calc(100%+10px)] z-40 rounded-xl border border-line bg-card shadow-xl shadow-black/30";

export function NotificationsMenu() {
  const { open, setOpen, ref } = useMenu();
  const [items, setItems] = useState(notifications);
  const unread = items.some((n) => n.unread);

  return (
    <div ref={ref} className="relative">
      <button type="button" aria-label="Notifications" aria-haspopup="true" aria-expanded={open}
        onClick={() => setOpen((o) => !o)} className="relative grid h-10 w-10 place-items-center rounded-lg hover:bg-inset">
        <Bell className="h-6 w-6 text-fg/90" />
        {unread && <span className="absolute right-1.5 top-1.5 h-2.5 w-2.5 rounded-full bg-danger" />}
      </button>
      {open && (
        <div className={cn(panel, "w-[340px] max-w-[calc(100vw-2rem)]")} role="menu">
          <div className="flex items-center justify-between border-b border-line px-4 py-3">
            <span className="font-pixel text-base font-semibold">Notifications</span>
            <button type="button" disabled={!unread} className="text-xs font-medium text-accent disabled:text-muted"
              onClick={() => setItems((l) => l.map((n) => ({ ...n, unread: false })))}>
              Mark all as read
            </button>
          </div>
          <ul className="max-h-[360px] overflow-y-auto p-2">
            {items.map((n) => (
              <li key={n.id} className="flex items-center gap-3 rounded-lg px-2 py-2.5 hover:bg-inset">
                <img src={`/assets/${n.icon}.png`} alt="" className="pixelated h-9 w-9 shrink-0 object-contain" />
                <div className="min-w-0 flex-1">
                  <div className="text-sm font-semibold leading-snug">{n.title}</div>
                  <div className="text-xs leading-snug text-muted">{n.sub}</div>
                </div>
                <div className="flex flex-col items-end gap-1.5">
                  <span className="text-xs text-muted">{n.time}</span>
                  {n.unread && <span className="h-2 w-2 rounded-full bg-accent" />}
                </div>
              </li>
            ))}
          </ul>
          <Link to="/battle-history" onClick={() => setOpen(false)}
            className="block border-t border-line px-4 py-3 text-center text-sm font-medium text-accent">
            View battle history
          </Link>
        </div>
      )}
    </div>
  );
}

export function UserMenu() {
  const { open, setOpen, ref } = useMenu();
  const { players } = useServices();
  const loadProgress = useCallback(() => players.getProgress(), [players]);
  const { state } = useAsync(loadProgress);
  const item = "flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm hover:bg-inset";
  return (
    <div ref={ref} className="relative">
      <button type="button" aria-haspopup="true" aria-expanded={open} aria-label="User menu"
        onClick={() => setOpen((o) => !o)} className="flex items-center gap-3 rounded-lg py-1 pl-1 pr-2 hover:bg-inset">
        <img src="/assets/avatar-top.png" alt="" className="pixelated h-11 w-11 rounded-full ring-1 ring-line" />
        <div className="hidden text-left leading-tight sm:block">
          <div className="text-sm font-semibold">{state.status === "success" ? state.data.playerName : "Tamer"}</div>
          <div className="text-xs text-muted">Player</div>
        </div>
        <ChevronDown className={cn("h-4 w-4 text-muted transition-transform", open && "rotate-180")} />
      </button>
      {open && (
        <div className={cn(panel, "w-[220px] p-2")} role="menu">
          <Link to="/profile" role="menuitem" className={item} onClick={() => setOpen(false)}>
            <User className="h-4 w-4 text-muted" /> Profile
          </Link>
          <Link to="/settings" role="menuitem" className={item} onClick={() => setOpen(false)}>
            <Settings className="h-4 w-4 text-muted" /> Settings
          </Link>
          <div className="my-1 border-t border-line" />
          {/* Ingen logikk ennå – legges til senere */}
          <button type="button" role="menuitem" className={cn(item, "text-danger")}>
            <LogOut className="h-4 w-4" /> Log out
          </button>
        </div>
      )}
    </div>
  );
}
