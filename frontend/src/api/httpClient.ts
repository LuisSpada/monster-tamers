import {
  clearAccessToken,
  getAccessToken,
} from "./authSession";
import {
  ApiError,
  createApiError,
} from "./errors";

export interface HttpClient {
  get<T>(path: string): Promise<T>;
  post<T>(path: string, body?: unknown): Promise<T>;
  put<T>(path: string, body?: unknown): Promise<T>;
}

function authHeaders(): Record<string, string> {
  const accessToken = getAccessToken();

  if (!accessToken) {
    return {};
  }

  return {
    Authorization: `Bearer ${accessToken}`,
  };
}

async function handleFailedResponse(
  response: Response,
): Promise<never> {
  if (response.status === 401) {
    clearAccessToken();
  }

  throw await createApiError(response);
}

async function readSuccessfulResponse<T>(
  response: Response,
): Promise<T> {
  if (
    response.status === 204 ||
    response.status === 205
  ) {
    return undefined as T;
  }

  const text = await response.text();

  if (!text) {
    return undefined as T;
  }

  return JSON.parse(text) as T;
}

export function createHttpClient(
  baseUrl: string,
  includeAuthentication = true,
): HttpClient {
  async function request<T>(
    method: string,
    path: string,
    body?: unknown,
  ): Promise<T> {
    let response: Response;

    try {
      response = await fetch(`${baseUrl}${path}`, {
        method,
        credentials: "include",
        headers: {
          Accept: "application/json",
          ...(body !== undefined
            ? {
                "Content-Type": "application/json",
              }
            : {}),
          ...(includeAuthentication
            ? authHeaders()
            : {}),
        },
        body:
          body !== undefined
            ? JSON.stringify(body)
            : undefined,
      });
    } catch {
      throw new ApiError(
        0,
        "NETWORK_ERROR",
        "Could not reach the server. Check your connection and try again.",
      );
    }

    if (!response.ok) {
      return handleFailedResponse(response);
    }

    return readSuccessfulResponse<T>(response);
  }

  return {
    get: (path) => request("GET", path),
    post: (path, body) =>
      request("POST", path, body),
    put: (path, body) =>
      request("PUT", path, body),
  };
}

export async function apiRequest<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  const baseUrl = String(
    import.meta.env.VITE_API_BASE_URL ??
      "https://localhost:8080/api",
  );

  let response: Response;

  try {
    response = await fetch(`${baseUrl}${path}`, {
      ...init,
      credentials: "include",
      headers: {
        Accept: "application/json",
        ...(init.body !== undefined
          ? {
              "Content-Type": "application/json",
            }
          : {}),
        ...authHeaders(),
        ...init.headers,
      },
    });
  } catch {
    throw new ApiError(
      0,
      "NETWORK_ERROR",
      "Could not reach the server. Check your connection and try again.",
    );
  }

  if (!response.ok) {
    return handleFailedResponse(response);
  }

  return readSuccessfulResponse<T>(response);
}