import { useEffect, useState } from "react";
import { NavLink, Outlet } from "react-router-dom";
import { Menu, Search } from "lucide-react";
import { NotificationsMenu, UserMenu } from "@/components/HeaderMenus";
import Scenery, { SceneBackground } from "@/components/Scenery";
import { cn } from "@/lib/utils";
import { useTheme } from "@/lib/theme";

const main = [
  { to: "/", label: "Home", icon: "nav-home" },
  { to: "/encounters", label: "Encounters", icon: "nav-encounters" },
  { to: "/collection", label: "Collection", icon: "nav-monsters" },
  { to: "/team", label: "Team", icon: "nav-team" },
  { to: "/battle-history", label: "Battle History", icon: "nav-history" },
];
const secondary = [
  { to: "/profile", label: "Profile", icon: "nav-profile" },
  { to: "/settings", label: "Settings", icon: "nav-settings" },
];

/** Pikselikon fra /public/assets. I light mode mørknes de for kontrast. */
function Px({ name, className }: { name: string; className?: string }) {
  return (
    <img src={`/assets/${name}.png`} alt="" aria-hidden
      className={cn("pixelated shrink-0", className)} />
  );
}

function NavItem({ to, label, icon, onNavigate }: (typeof main)[number] & { onNavigate: () => void }) {
  return (
    <NavLink
      to={to}
      end={to === "/"}
      onClick={onNavigate}
      className={({ isActive }) =>
        cn(
          "flex items-center gap-3 rounded-lg px-4 py-3 text-[15px] font-medium transition-colors",
          isActive
            ? "bg-accent/15 text-accent shadow-[inset_3px_0_0_var(--accent)]"
            : "text-fg/90 hover:bg-inset"
        )
      }
    >
      <Px name={icon} className="h-[26px] w-[26px] object-contain [html:not(.dark)_&:not([src*=home])]:brightness-[0.45]" />
      {label}
    </NavLink>
  );
}

function ThemeSwitch() {
  const { theme, setTheme } = useTheme();
  const dark = theme === "dark";
  return (
    <div className="flex items-center justify-between rounded-lg border-2 border-line bg-inset/70 px-4 py-3">
      <Px name="icon-sun" className={cn("h-5 w-5", dark ? "opacity-70" : "")} />
      <button
        role="switch"
        aria-checked={dark}
        aria-label="Toggle dark mode"
        onClick={() => setTheme(dark ? "light" : "dark")}
        className="relative h-7 w-14 rounded-full border border-line bg-bg"
      >
        <span
          className={cn(
            "absolute top-0.5 h-5 w-7 rounded-full bg-gradient-to-br from-indigo-400 to-violet-600 transition-all",
            dark ? "left-[26px]" : "left-0.5"
          )}
        />
      </button>
      <Px name="icon-moon" className={cn("h-5 w-5", dark ? "" : "opacity-60 brightness-50")} />
    </div>
  );
}

export default function Layout() {
  const [open, setOpen] = useState(false);

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") setOpen(false);
    };
    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, []);

  return (
    <div className="flex min-h-full">
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-30 flex w-[230px] flex-col border-r border-line bg-sidebar transition-transform",
          open ? "translate-x-0" : "-translate-x-full lg:translate-x-0"
        )}
      >
        <SceneBackground overlay="bg-sidebar/80" position="left center" className="-z-0" />
        <img src="/assets/sidebar-bottom.png" alt="" aria-hidden
          className="pixelated pointer-events-none absolute inset-x-0 bottom-0 h-auto w-full" />
        <div className="relative flex h-[68px] items-center gap-3 px-5">
          <img src="/assets/logo.png" alt="" className="pixelated h-[50px] w-[50px] rounded-md" />
          <div className="font-pixel text-[22px] font-bold leading-[0.95] tracking-wide">
            <div>MONSTER</div>
            <div className="text-accent">TAMERS</div>
          </div>
        </div>

        <nav className="relative mt-4 flex flex-col gap-1 px-3">
          {main.map((i) => <NavItem key={i.to} {...i} onNavigate={() => setOpen(false)} />)}
          <div className="my-4 border-t border-line" />
          {secondary.map((i) => <NavItem key={i.to} {...i} onNavigate={() => setOpen(false)} />)}
        </nav>

        <div className="relative mt-10 flex flex-col gap-4 p-3">
          <ThemeSwitch />
          {/* Ingen logikk ennå – legges til senere */}
          <button type="button" className="flex w-full items-center gap-3 rounded-lg border-2 border-danger/40 bg-danger/10 px-4 py-3 text-[15px] font-medium text-danger transition-colors hover:bg-danger/20">
            <Px name="nav-logout" className="h-[26px] w-[26px] object-contain" />
            Log out
          </button>
        </div>
      </aside>

      {open && (
        <button
          type="button"
          aria-label="Close sidebar"
          className="fixed inset-0 z-20 bg-black/40 lg:hidden"
          onClick={() => setOpen(false)}
        />
      )}
      <div className="flex min-w-0 flex-1 flex-col lg:ml-[230px]">
        <header className="sticky top-0 z-20 flex h-[68px] items-center gap-4 border-b border-line bg-sidebar/90 px-5 backdrop-blur">
          <button
            type="button"
            onClick={() => setOpen((o) => !o)}
            aria-label={open ? "Close sidebar" : "Open sidebar"}
            aria-expanded={open}
            className="grid h-10 w-10 place-items-center rounded-lg border border-line bg-inset/60"
          >
            <Menu className="h-5 w-5 text-accent" />
          </button>

          <div className="relative max-w-[500px] flex-1">
            <Search className="absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-muted" />
            <input
              placeholder="Search monsters, encounters, ..."
              className="h-10 w-full rounded-lg border border-line bg-inset/60 pl-10 pr-14 text-sm placeholder:text-muted"
            />
            <kbd className="absolute right-2.5 top-1/2 -translate-y-1/2 rounded border border-line bg-card px-1.5 py-0.5 text-[11px] text-muted">
              ⌘ K
            </kbd>
          </div>

          <div className="ml-auto flex items-center gap-3">
            <NotificationsMenu />
            <UserMenu />
          </div>
        </header>

        <main className="relative isolate flex-1">
          <Scenery />
          <Outlet />
        </main>
      </div>
    </div>
  );
}
