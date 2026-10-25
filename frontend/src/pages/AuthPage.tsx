import { Link } from "react-router-dom";
import { SceneBackground } from "@/components/Scenery";
import { cn } from "@/lib/utils";

/** Statisk skjema for registrering/innlogging (US-01, US-02). Ingen logikk ennå. */
export default function AuthPage({ mode }: { mode: "login" | "register" }) {
  const register = mode === "register";
  const field = "h-11 w-full rounded-lg border border-line bg-inset/60 px-3 text-sm placeholder:text-muted";
  return (
    <div className="relative isolate grid min-h-full place-items-center overflow-hidden bg-bg p-6">
      <SceneBackground overlay="bg-bg/30 dark:bg-transparent" />
      <img src="/assets/bottom-strip.png" alt="" aria-hidden
        className="pixelated pointer-events-none absolute inset-x-0 bottom-0 h-[110px] w-full object-cover" />
      <form onSubmit={(e) => e.preventDefault()}
        className="relative z-10 w-full max-w-sm space-y-4 rounded-xl border border-line bg-card/95 p-8 shadow-lg">
        <div className="flex items-center gap-3">
          <img src="/assets/logo.png" alt="" className="pixelated h-12 w-12 rounded-md" />
          <div className="font-pixel text-xl font-bold leading-none">MONSTER <span className="text-accent">TAMERS</span></div>
        </div>
        <h1 className="font-pixel text-2xl font-semibold">{register ? "Create account" : "Log in"}</h1>
        <label className="block text-sm">Email
          <input type="email" autoComplete="email" className={cn(field, "mt-1")} placeholder="you@example.com" />
        </label>
        {register && (
          <label className="block text-sm">Player name
            <input type="text" className={cn(field, "mt-1")} placeholder="Tamer" />
          </label>
        )}
        <label className="block text-sm">Password
          <input type="password" autoComplete={register ? "new-password" : "current-password"} className={cn(field, "mt-1")} />
        </label>
        <button type="submit" className="h-11 w-full rounded-md bg-accent text-sm font-semibold text-accent-fg">
          {register ? "Create account" : "Log in"}
        </button>
        <p className="text-center text-xs text-muted">Work in progress – not connected to the backend yet.</p>
        <p className="text-center text-sm">
          {register ? "Already have an account? " : "New here? "}
          <Link to={register ? "/login" : "/register"} className="font-medium text-accent">
            {register ? "Log in" : "Create account"}
          </Link>
        </p>
      </form>
    </div>
  );
}
