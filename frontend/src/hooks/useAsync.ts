import { useCallback, useEffect, useState } from "react";
import { getErrorMessage } from "../api/errors";

export type AsyncState<T> =
  | { status: "loading" }
  | { status: "success"; data: T }
  | { status: "error"; message: string };

/**
 * Runs `loader` on mount (and whenever the loader identity changes) and exposes loading/success/error.
 * Wrap the loader in useCallback so it only changes when its dependencies do.
 */
export function useAsync<T>(loader: () => Promise<T>): { state: AsyncState<T>; reload: () => void } {
  const [state, setState] = useState<AsyncState<T>>({ status: "loading" });
  const [nonce, setNonce] = useState(0);

  useEffect(() => {
    let cancelled = false;
    loader().then(
      (data) => {
        if (!cancelled) setState({ status: "success", data });
      },
      (error: unknown) => {
        if (!cancelled) setState({ status: "error", message: getErrorMessage(error) });
      },
    );
    return () => {
      cancelled = true;
    };
  }, [loader, nonce]);

  const reload = useCallback(() => {
    setState({ status: "loading" });
    setNonce((n) => n + 1);
  }, []);

  return { state, reload };
}
