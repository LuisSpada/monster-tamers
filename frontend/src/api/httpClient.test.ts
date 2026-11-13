import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";
import {
  clearAccessToken,
  getAccessToken,
  setAccessToken,
} from "./authSession";
import { ApiError } from "./errors";
import { createHttpClient } from "./httpClient";

beforeEach(() => {
  sessionStorage.clear();
  vi.restoreAllMocks();
});

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("createHttpClient", () => {
  it("adds the bearer token to authenticated requests", async () => {
    setAccessToken("test-access-token");

    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify([]), {
        status: 200,
        headers: {
          "Content-Type": "application/json",
        },
      }),
    );

    vi.stubGlobal("fetch", fetchMock);

    const client = createHttpClient(
      "http://localhost:8080",
    );

    await client.get("/api/monsters");

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/monsters",
      expect.objectContaining({
        headers: expect.objectContaining({
          Authorization:
            "Bearer test-access-token",
        }),
      }),
    );
  });

  it("omits Authorization when no token exists", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(JSON.stringify([]), {
        status: 200,
        headers: {
          "Content-Type": "application/json",
        },
      }),
    );

    vi.stubGlobal("fetch", fetchMock);

    const client = createHttpClient(
      "http://localhost:8080",
    );

    await client.get("/api/monsters");

    const request = fetchMock.mock.calls[0][1] as RequestInit;
    const headers = request.headers as Record<
      string,
      string
    >;

    expect(headers.Authorization).toBeUndefined();
  });

  it("can explicitly disable authentication", async () => {
    setAccessToken("old-access-token");

    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          accessToken: "new-access-token",
        }),
        {
          status: 200,
          headers: {
            "Content-Type": "application/json",
          },
        },
      ),
    );

    vi.stubGlobal("fetch", fetchMock);

    const client = createHttpClient(
      "http://localhost:8080",
      false,
    );

    await client.post("/api/auth/login", {
      email: "tamer@example.com",
      password: "secret",
    });

    const request = fetchMock.mock.calls[0][1] as RequestInit;
    const headers = request.headers as Record<
      string,
      string
    >;

    expect(headers.Authorization).toBeUndefined();
  });

  it("parses Problem Details responses", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            status: 401,
            detail: "Email or password is invalid",
            code: "auth.invalid_credentials",
          }),
          {
            status: 401,
            headers: {
              "Content-Type":
                "application/problem+json",
            },
          },
        ),
      ),
    );

    const client = createHttpClient(
      "http://localhost:8080",
      false,
    );

    let error: unknown;

    try {
      await client.post("/api/auth/login", {
        email: "tamer@example.com",
        password: "wrong",
      });
    } catch (caughtError) {
      error = caughtError;
    }

    expect(error).toBeInstanceOf(ApiError);
    expect(error).toMatchObject({
      status: 401,
      code: "auth.invalid_credentials",
      message: "Email or password is invalid",
    });
  });

  it("clears an existing token after a 401 response", async () => {
    setAccessToken("expired-access-token");

    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            status: 401,
            detail: "Unauthorized",
            code: "common.unauthorized",
          }),
          {
            status: 401,
            headers: {
              "Content-Type":
                "application/problem+json",
            },
          },
        ),
      ),
    );

    const client = createHttpClient(
      "http://localhost:8080",
    );

    await expect(
      client.get("/api/players/me"),
    ).rejects.toBeInstanceOf(ApiError);

    expect(getAccessToken()).toBeNull();
  });

  it("can clear session state explicitly", () => {
    setAccessToken("test-access-token");
    clearAccessToken();

    expect(getAccessToken()).toBeNull();
  });
});