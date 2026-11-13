import {
  useState,
  type FormEvent,
} from "react";
import {
  Link,
  useLocation,
  useNavigate,
} from "react-router-dom";
import {
  confirmRegistration,
} from "@/api/auth";
import { getErrorMessage } from "@/api/errors";
import {
  clearPendingRegistration,
  getPendingRegistration,
  type PendingRegistration,
} from "@/api/registrationSession";
import { SceneBackground } from "@/components/Scenery";
import { Fireflies } from "@/components/game/fx/Fireflies";
import { cn } from "@/lib/utils";

function readRegistration(
  routerState: unknown,
): PendingRegistration | null {
  if (
    routerState &&
    typeof routerState === "object"
  ) {
    const candidate =
      routerState as Partial<PendingRegistration>;

    if (
      typeof candidate.registrationId ===
        "string" &&
      candidate.registrationId.length > 0 &&
      typeof candidate.email === "string" &&
      candidate.email.length > 0
    ) {
      return {
        registrationId:
          candidate.registrationId,
        email: candidate.email,
      };
    }
  }

  return getPendingRegistration();
}

export default function RegistrationConfirmationPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const [registration] = useState(() =>
    readRegistration(location.state),
  );

  const [submitting, setSubmitting] =
    useState(false);

  const [errorMessage, setErrorMessage] =
    useState<string | null>(null);

  const field =
    "h-11 w-full rounded-lg border border-line bg-inset/60 px-3 text-center text-sm tracking-[0.35em] placeholder:text-muted disabled:cursor-not-allowed disabled:opacity-60";

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ): Promise<void> {
    event.preventDefault();

    if (!registration) {
      return;
    }

    setSubmitting(true);
    setErrorMessage(null);

    const formData = new FormData(
      event.currentTarget,
    );

    const code = String(
      formData.get("code") ?? "",
    );

    try {
      await confirmRegistration(
        registration.registrationId,
        code,
      );

      clearPendingRegistration();

      navigate("/login", {
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

      <div className="relative z-10 w-full max-w-sm animate-rise space-y-4 rounded-2xl border border-line bg-card/90 p-8 shadow-2xl shadow-black/20 backdrop-blur-md">
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
            Confirm registration
          </p>

          <h1 className="mt-1 font-pixel text-3xl font-bold">
            Enter your code
          </h1>
        </div>

        {!registration ? (
          <>
            <div
              role="alert"
              className="rounded-lg border border-danger/40 bg-danger/10 px-3 py-2 text-sm text-danger"
            >
              No pending registration was found.
              Start the registration process again.
            </div>

            <Link
              to="/register"
              className="block h-11 rounded-md bg-accent px-4 py-3 text-center text-sm font-semibold text-accent-fg"
            >
              Back to registration
            </Link>
          </>
        ) : (
          <form
            onSubmit={handleSubmit}
            className="space-y-4"
          >
            <p className="text-sm text-muted">
              Enter the six-digit code sent to{" "}
              <strong className="text-fg">
                {registration.email}
              </strong>
              .
            </p>

            <p className="text-xs text-muted">
              During local development, the code
              is written to the Auth Service log.
            </p>

            <label
              htmlFor="code"
              className="block text-sm"
            >
              Confirmation code
              <input
                id="code"
                name="code"
                type="text"
                required
                inputMode="numeric"
                pattern="[0-9]{6}"
                maxLength={6}
                autoComplete="one-time-code"
                disabled={submitting}
                className={cn(field, "mt-1")}
                placeholder="000000"
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
                ? "Confirming..."
                : "Confirm registration"}
            </button>

            <Link
              to="/register"
              className="block text-center text-sm font-medium text-accent"
            >
              Start again
            </Link>
          </form>
        )}
      </div>
    </div>
  );
}