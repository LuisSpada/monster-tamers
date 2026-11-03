import { useEffect, useState } from "react";
import { NavLink, Outlet } from "react-router-dom";
import { Menu } from "lucide-react";
import { NotificationsMenu, UserMenu } from "@/components/HeaderMenus";
import Scenery from "@/components/Scenery";
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
  const [isDesktop, setIsDesktop] = useState(false);

  useEffect(() => {
    if (!window.matchMedia) return;
    const mediaQuery = window.matchMedia("(min-width: 1024px)");
    const onChange = () => setIsDesktop(mediaQuery.matches);
    onChange();
    mediaQuery.addEventListener("change", onChange);
    return () => mediaQuery.removeEventListener("change", onChange);
  }, []);

  useEffect(() => {
    const onKeyDown = (event: KeyboardEvent) => {
      if (event.key === "Escape") setOpen(false);
    };
    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, []);

  const sidebarOpen = isDesktop ? !open : open;

  return (
    <div className="flex min-h-full">
      <aside
        className={cn(
          "fixed inset-y-0 left-0 z-30 flex w-[264px] flex-col overflow-hidden border-r border-line bg-sidebar shadow-2xl shadow-black/20 transition-transform",
          sidebarOpen ? "translate-x-0" : "-translate-x-full"
        )}
      >
        <div aria-hidden className="pointer-events-none absolute inset-0">
          <div className="absolute -left-20 -top-20 h-56 w-56 rounded-full bg-accent/15 blur-3xl" />
          <div className="absolute -bottom-20 -right-20 h-64 w-64 rounded-full bg-indigo-500/10 blur-3xl" />
          <div className="atmosphere-grid absolute inset-0 opacity-30" />
        </div>
        <div className="relative flex h-[82px] items-center gap-3 px-6">
          <img src="/assets/logo.png" alt="" className="pixelated h-[58px] w-[58px] rounded-md" />
          <div className="font-pixel text-[24px] font-bold leading-[0.95] tracking-wide">
            <div>MONSTER</div>
            <div className="text-accent">TAMERS</div>
          </div>
        </div>

        <nav className="relative mt-6 flex flex-col gap-1.5 px-4">
          {main.map((i) => <NavItem key={i.to} {...i} onNavigate={() => setOpen(false)} />)}
          <div className="my-4 border-t border-line" />
          {secondary.map((i) => <NavItem key={i.to} {...i} onNavigate={() => setOpen(false)} />)}
        </nav>

        <div className="relative mt-12 flex flex-col gap-4 p-4">
          <ThemeSwitch />
          {/* Ingen logikk ennå – legges til senere */}
          <button type="button" className="flex w-full items-center gap-3 rounded-lg border-2 border-danger/40 bg-danger/10 px-4 py-3 text-[15px] font-medium text-danger transition-colors hover:bg-danger/20">
            <Px name="nav-logout" className="h-[26px] w-[26px] object-contain" />
            Log out
          </button>
        </div>
      </aside>

      {open && !isDesktop && (
        <button
          type="button"
          aria-label="Close sidebar"
          className="fixed inset-0 z-20 bg-black/40 lg:hidden"
          onClick={() => setOpen(false)}
        />
      )}
      <div className={cn("flex min-w-0 flex-1 flex-col", sidebarOpen && "lg:ml-[264px]")}>
        <header className="sticky top-0 z-20 flex h-[82px] items-center gap-5 border-b border-line bg-sidebar/90 px-6 backdrop-blur">
          <button
            type="button"
            onClick={() => setOpen((o) => !o)}
            aria-label={sidebarOpen ? "Close sidebar" : "Open sidebar"}
            aria-expanded={sidebarOpen}
            className="grid h-11 w-11 place-items-center rounded-lg border border-line bg-inset/60"
          >
            <Menu className="h-5 w-5 text-accent" />
          </button>

          <div className="hidden min-w-0 flex-1 sm:block">
            <div className="font-pixel text-sm font-semibold tracking-wide text-fg">Monster Tamers</div>
            <div className="text-xs text-muted">Build your team. Face the unknown.</div>
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
