import {
  cleanup,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react";
import {
  MemoryRouter,
  Route,
  Routes,
  useLocation,
} from "react-router-dom";
import {
  afterEach,
  beforeEach,
  describe,
  expect,
  it,
  vi,
} from "vitest";
import { getAccessToken } from "@/api/authSession";
import { AuthProvider } from "@/auth/AuthProvider";
import AuthPage from "./AuthPage";

function LocationProbe() {
  const location = useLocation();

  return (
    <div data-testid="location">
      {location.pathname}
      {location.search}
      {location.hash}
    </div>
  );
}

function renderLogin(returnTo?: string) {
  const initialEntry = returnTo
    ? {
        pathname: "/login",
        state: {
          returnTo,
        },
      }
    : "/login";

  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <AuthProvider>
        <Routes>
          <Route
            path="/login"
            element={<AuthPage />}
          />

          <Route
            path="*"
            element={<LocationProbe />}
          />
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );
}

function fillAndSubmitLogin(
  password = "secret-password",
) {
  fireEvent.change(
    screen.getByLabelText("Email"),
    {
      target: {
        value: "tamer@example.com",
      },
    },
  );

  fireEvent.change(
    screen.getByLabelText("Password"),
    {
      target: {
        value: password,
      },
    },
  );

  fireEvent.click(
    screen.getByRole("button", {
      name: "Log in",
    }),
  );
}

beforeEach(() => {
  sessionStorage.clear();
  vi.restoreAllMocks();
});

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
  sessionStorage.clear();
});

describe("AuthPage", () => {
  it("logs in, stores the token and redirects home", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            accessToken: "signed-access-token",
          }),
          {
            status: 200,
            headers: {
              "Content-Type": "application/json",
            },
          },
        ),
      ),
    );

    renderLogin();
    fillAndSubmitLogin();

    await waitFor(() => {
      expect(getAccessToken()).toBe(
        "signed-access-token",
      );
    });

    expect(
      screen.getByTestId("location"),
    ).toHaveTextContent("/");
  });

  it("returns to the requested route after login", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            accessToken: "signed-access-token",
          }),
          {
            status: 200,
            headers: {
              "Content-Type": "application/json",
            },
          },
        ),
      ),
    );

    renderLogin(
      "/collection?class=Fighter#owned",
    );

    fillAndSubmitLogin();

    await waitFor(() => {
      expect(
        screen.getByTestId("location"),
      ).toHaveTextContent(
        "/collection?class=Fighter#owned",
      );
    });
  });

  it("shows invalid-credentials Problem Details", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            status: 401,
            detail:
              "Email or password is invalid",
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

    renderLogin();
    fillAndSubmitLogin("wrong-password");

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Email or password is invalid",
    );

    expect(getAccessToken()).toBeNull();
  });

  it("shows a network error", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockRejectedValue(
        new TypeError("Failed to fetch"),
      ),
    );

    renderLogin();
    fillAndSubmitLogin();

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Could not reach the server. Check your connection and try again.",
    );
  });

  it("links to the registration page", () => {
    renderLogin();

    expect(
      screen.getByRole("link", {
        name: "Create account",
      }),
    ).toHaveAttribute(
      "href",
      "/register",
    );
  });
});