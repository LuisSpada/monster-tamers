import { ApiError } from "./errors";

export interface HttpClient {
  get<T>(path: string): Promise<T>;
  post<T>(path: string, body?: unknown): Promise<T>;
  put<T>(path: string, body?: unknown): Promise<T>;
}

/** TODO(auth): inject the Authorization header here once authentication is implemented. */
function authHeaders(): Record<string, string> {
  return {};
}

export function createHttpClient(baseUrl: string): HttpClient {
  async function request<T>(method: string, path: string, body?: unknown): Promise<T> {
    let response: Response;
    try {
      response = await fetch(`${baseUrl}${path}`, {
        method,
        credentials: "include",
        headers: {
          Accept: "application/json",
          ...(body !== undefined ? { "Content-Type": "application/json" } : {}),
          ...authHeaders(),
        },
        body: body !== undefined ? JSON.stringify(body) : undefined,
      });
    } catch {
      throw new ApiError(0, "NETWORK_ERROR", "Could not reach the server. Check your connection and try again.");
    }

    if (!response.ok) {
      let message = `Request failed with status ${response.status}.`;
      let code = "HTTP_ERROR";
      try {
        const payload = (await response.json()) as { message?: string; code?: string };
        if (payload.message) message = payload.message;
        if (payload.code) code = payload.code;
      } catch {
        /* response had no JSON body */
      }
      throw new ApiError(response.status, code, message);
    }

    if (response.status === 204) return undefined as T;
    return (await response.json()) as T;
  }

  return {
    get: (path) => request("GET", path),
    post: (path, body) => request("POST", path, body),
    put: (path, body) => request("PUT", path, body),
  };
}

/** Backward-compatible request helper for callers that predate the service layer. */
export async function apiRequest<T>(path: string, init: RequestInit = {}): Promise<T> {
  const baseUrl = String(import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080/api");
  let response: Response;
  try {
    response = await fetch(`${baseUrl}${path}`, {
      ...init,
      credentials: "include",
      headers: {
        "Content-Type": "application/json",
        ...init.headers,
      },
    });
  } catch {
    throw new ApiError(0, "NETWORK_ERROR", "Could not reach the server. Check your connection and try again.");
  }
  if (!response.ok) throw new ApiError(response.status, "HTTP_ERROR", `Request failed with status ${response.status}.`);
  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
}
