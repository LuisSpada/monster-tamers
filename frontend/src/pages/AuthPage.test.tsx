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
import AuthPage from "./AuthPage";

function LocationProbe() {
  const location = useLocation();

  return (
    <div data-testid="location">
      {location.pathname}
    </div>
  );
}

function renderLogin() {
  return render(
    <MemoryRouter initialEntries={["/login"]}>
      <Routes>
        <Route
          path="/login"
          element={<AuthPage mode="login" />}
        />
        <Route
          path="/"
          element={<LocationProbe />}
        />
      </Routes>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  sessionStorage.clear();
  vi.restoreAllMocks();
});

afterEach(() => {
  cleanup();
  vi.unstubAllGlobals();
});

describe("AuthPage", () => {
  it("renders registration credentials and confirmation field", () => {
    render(
      <MemoryRouter>
        <AuthPage mode="register" />
      </MemoryRouter>,
    );

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
          value: "secret-password",
        },
      },
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Log in",
      }),
    );

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

  it("shows Problem Details from invalid credentials", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            title: "Unauthorized",
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

    renderLogin();

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
          value: "wrong-password",
        },
      },
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Log in",
      }),
    );

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
          value: "secret-password",
        },
      },
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Log in",
      }),
    );

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Could not reach the server. Check your connection and try again.",
    );
  });

  it("keeps registration disconnected from Auth Service", () => {
    const fetchMock = vi.fn();

    vi.stubGlobal("fetch", fetchMock);

    render(
      <MemoryRouter>
        <AuthPage mode="register" />
      </MemoryRouter>,
    );

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