import {
  useState,
  type FormEvent,
} from "react";
import {
  Link,
  useLocation,
  useNavigate,
} from "react-router-dom";
import { login } from "@/api/auth";
import {
  type LoginNavigationState,
} from "@/auth/ProtectedRoute";
import { useAuth } from "@/auth/useAuth";
import { getErrorMessage } from "@/api/errors";
import { SceneBackground } from "@/components/Scenery";
import { Fireflies } from "@/components/game/fx/Fireflies";
import { cn } from "@/lib/utils";

export default function AuthPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { signIn } = useAuth();

  const navigationState =
    location.state as LoginNavigationState | null;

  const returnTo =
    navigationState?.returnTo ?? "/";

  const [submitting, setSubmitting] =
    useState(false);

  const [errorMessage, setErrorMessage] =
    useState<string | null>(null);

  const field =
    "h-11 w-full rounded-lg border border-line bg-inset/60 px-3 text-sm placeholder:text-muted disabled:cursor-not-allowed disabled:opacity-60";

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ): Promise<void> {
    event.preventDefault();
    setSubmitting(true);
    setErrorMessage(null);

    const formData = new FormData(
      event.currentTarget,
    );

    const email = String(
      formData.get("email") ?? "",
    );

    const password = String(
      formData.get("password") ?? "",
    );

    try {
      const response = await login(
        email,
        password,
      );

      signIn(response.accessToken);

      navigate(returnTo, {
        replace: true,
      });
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setSubmitting(false);
    }
  }

  return (
    <div className="relative isolate grid min-h-full place-items-center overflow-hidden bg-bg p-6">
      <SceneBackground />
      <Fireflies count={16} className="-z-10" />

      <form
        onSubmit={handleSubmit}
        className="relative z-10 w-full max-w-sm animate-rise space-y-4 rounded-2xl border border-line bg-card/90 p-8 shadow-2xl shadow-black/20 backdrop-blur-md"
      >
        <div className="flex items-center gap-3">
          <img
            src="/assets/logo.png"
            alt=""
            className="pixelated h-12 w-12 animate-float rounded-md"
          />

          <div className="font-pixel text-xl font-bold leading-none">
            MONSTER{" "}
            <span className="text-accent">
              TAMERS
            </span>
          </div>
        </div>

        <div>
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-accent">
            Welcome, tamer
          </p>

          <h1 className="mt-1 font-pixel text-3xl font-bold">
            Log in
          </h1>
        </div>

        <label
          htmlFor="email"
          className="block text-sm"
        >
          Email
          <input
            id="email"
            name="email"
            type="email"
            required
            autoComplete="email"
            disabled={submitting}
            className={cn(field, "mt-1")}
            placeholder="you@example.com"
          />
        </label>

        <label
          htmlFor="password"
          className="block text-sm"
        >
          Password
          <input
            id="password"
            name="password"
            type="password"
            required
            autoComplete="current-password"
            disabled={submitting}
            className={cn(field, "mt-1")}
          />
        </label>

        {errorMessage && (
          <div
            role="alert"
            className="rounded-lg border border-danger/40 bg-danger/10 px-3 py-2 text-sm text-danger"
          >
            {errorMessage}
          </div>
        )}

        <button
          type="submit"
          disabled={submitting}
          className="h-11 w-full rounded-md bg-accent text-sm font-semibold text-accent-fg disabled:cursor-not-allowed disabled:opacity-60"
        >
          {submitting
            ? "Logging in..."
            : "Log in"}
        </button>

        <p className="text-center text-sm">
          New here?{" "}
          <Link
            to="/register"
            className="font-medium text-accent"
          >
            Create account
          </Link>
        </p>
      </form>
    </div>
  );
}