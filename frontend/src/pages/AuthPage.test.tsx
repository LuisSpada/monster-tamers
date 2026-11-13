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

interface LoginNavigationState {
  returnTo?: string;
}

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

function renderLogin(
  options: {
    returnTo?: string;
  } = {},
) {
  const initialEntry = options.returnTo
    ? {
        pathname: "/login",
        state: {
          returnTo: options.returnTo,
        } satisfies LoginNavigationState,
      }
    : "/login";

  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <AuthProvider>
        <Routes>
          <Route
            path="/login"
            element={<AuthPage mode="login" />}
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

function renderRegistration() {
  return render(
    <MemoryRouter initialEntries={["/register"]}>
      <AuthProvider>
        <Routes>
          <Route
            path="/register"
            element={<AuthPage mode="register" />}
          />
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );
}

function fillLoginForm(
  email = "tamer@example.com",
  password = "secret-password",
) {
  fireEvent.change(
    screen.getByLabelText("Email"),
    {
      target: {
        value: email,
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
}

function submitLogin() {
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
  it("renders registration credentials and confirmation field", () => {
    renderRegistration();

    expect(
      screen.getByLabelText("Email"),
    ).toHaveAttribute("name", "email");

    expect(
      screen.getByLabelText("Password"),
    ).toHaveAttribute("name", "password");

    expect(
      screen.getByLabelText("Confirm password"),
    ).toHaveAttribute(
      "name",
      "confirmPassword",
    );

    expect(
      screen.getByRole("textbox", {
        name: "Confirmation code",
      }),
    ).toHaveAttribute("pattern", "[0-9]{6}");
  });

  it("logs in, stores the token and redirects home", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
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
    );

    vi.stubGlobal("fetch", fetchMock);

    renderLogin();
    fillLoginForm();
    submitLogin();

    await waitFor(() => {
      expect(getAccessToken()).toBe(
        "signed-access-token",
      );
    });

    expect(
      screen.getByTestId("location"),
    ).toHaveTextContent("/");

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/auth/login",
      expect.objectContaining({
        method: "POST",
        credentials: "include",
        body: JSON.stringify({
          email: "tamer@example.com",
          password: "secret-password",
        }),
      }),
    );
  });

  it("returns to the originally requested route after login", async () => {
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

    renderLogin({
      returnTo:
        "/collection?class=Fighter#owned",
    });

    fillLoginForm();
    submitLogin();

    await waitFor(() => {
      expect(
        screen.getByTestId("location"),
      ).toHaveTextContent(
        "/collection?class=Fighter#owned",
      );
    });

    expect(getAccessToken()).toBe(
      "signed-access-token",
    );
  });

  it("shows Problem Details from invalid credentials", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            title: "Unauthorized",
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
    fillLoginForm(
      "tamer@example.com",
      "wrong-password",
    );
    submitLogin();

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Email or password is invalid",
    );

    expect(getAccessToken()).toBeNull();
  });

  it("shows a network error when Auth Service is unavailable", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockRejectedValue(
        new TypeError("Failed to fetch"),
      ),
    );

    renderLogin();
    fillLoginForm();
    submitLogin();

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Could not reach the server. Check your connection and try again.",
    );

    expect(getAccessToken()).toBeNull();
  });

  it("keeps registration disconnected from Auth Service", () => {
    const fetchMock = vi.fn();

    vi.stubGlobal("fetch", fetchMock);

    renderRegistration();

    fireEvent.submit(
      screen
        .getByRole("button", {
          name: "Create account",
        })
        .closest("form")!,
    );

    expect(fetchMock).not.toHaveBeenCalled();
  });
});