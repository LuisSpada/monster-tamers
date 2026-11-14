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
import {
  getPendingRegistration,
} from "@/api/registrationSession";
import RegistrationPage from "./RegistrationPage";

function LocationProbe() {
  const location = useLocation();

  return (
    <div data-testid="location">
      {location.pathname}
    </div>
  );
}

function renderRegistrationPage() {
  return render(
    <MemoryRouter initialEntries={["/register"]}>
      <Routes>
        <Route
          path="/register"
          element={<RegistrationPage />}
        />

        <Route
          path="/register/confirm"
          element={<LocationProbe />}
        />
      </Routes>
    </MemoryRouter>,
  );
}

function fillRegistrationForm(
  options: {
    email?: string;
    password?: string;
    confirmPassword?: string;
  } = {},
) {
  const {
    email = "tamer@example.com",
    password = "password123",
    confirmPassword = "password123",
  } = options;

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

  fireEvent.change(
    screen.getByLabelText("Confirm password"),
    {
      target: {
        value: confirmPassword,
      },
    },
  );
}

function submitRegistration() {
  fireEvent.click(
    screen.getByRole("button", {
      name: "Create account",
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

describe("RegistrationPage", () => {
  it("registers the account and navigates to confirmation", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          registrationId:
            "d0775e50-aad9-4fd3-a016-c6efa18df154",
        }),
        {
          status: 202,
          headers: {
            "Content-Type": "application/json",
          },
        },
      ),
    );

    vi.stubGlobal("fetch", fetchMock);

    renderRegistrationPage();
    fillRegistrationForm();
    submitRegistration();

    await waitFor(() => {
      expect(
        screen.getByTestId("location"),
      ).toHaveTextContent(
        "/register/confirm",
      );
    });

    expect(fetchMock).toHaveBeenCalledWith(
      "https://localhost:8080/api/auth/registrations",
      expect.objectContaining({
        method: "POST",
        credentials: "include",
        body: JSON.stringify({
          email: "tamer@example.com",
          password: "password123",
          confirmPassword: "password123",
        }),
      }),
    );

    expect(
      getPendingRegistration(),
    ).toEqual({
      registrationId:
        "d0775e50-aad9-4fd3-a016-c6efa18df154",
      email: "tamer@example.com",
    });
  });

  it("does not submit playerName or a confirmation code", async () => {
    const fetchMock = vi.fn().mockResolvedValue(
      new Response(
        JSON.stringify({
          registrationId:
            "d0775e50-aad9-4fd3-a016-c6efa18df154",
        }),
        {
          status: 202,
          headers: {
            "Content-Type": "application/json",
          },
        },
      ),
    );

    vi.stubGlobal("fetch", fetchMock);

    renderRegistrationPage();
    fillRegistrationForm();
    submitRegistration();

    await waitFor(() => {
      expect(fetchMock).toHaveBeenCalledTimes(1);
    });

    const request =
      fetchMock.mock.calls[0][1] as RequestInit;

    const body = JSON.parse(
      String(request.body),
    ) as Record<string, unknown>;

    expect(body).toEqual({
      email: "tamer@example.com",
      password: "password123",
      confirmPassword: "password123",
    });

    expect(body).not.toHaveProperty("playerName");
    expect(body).not.toHaveProperty(
      "confirmationCode",
    );
  });

  it("rejects mismatched passwords without calling the backend", () => {
    const fetchMock = vi.fn();

    vi.stubGlobal("fetch", fetchMock);

    renderRegistrationPage();

    fillRegistrationForm({
      password: "password123",
      confirmPassword: "different123",
    });

    submitRegistration();

    expect(
      screen.getByRole("alert"),
    ).toHaveTextContent(
      "Passwords must match.",
    );

    expect(fetchMock).not.toHaveBeenCalled();
    expect(getPendingRegistration()).toBeNull();
  });

  it("shows a duplicate-email Problem Details message", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            status: 409,
            detail:
              "Email is already registered",
            code: "auth.email_taken",
          }),
          {
            status: 409,
            headers: {
              "Content-Type":
                "application/problem+json",
            },
          },
        ),
      ),
    );

    renderRegistrationPage();
    fillRegistrationForm();
    submitRegistration();

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Email is already registered",
    );

    expect(getPendingRegistration()).toBeNull();
  });

  it("shows backend validation errors", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockResolvedValue(
        new Response(
          JSON.stringify({
            status: 400,
            detail: "Validation failed",
            code: "common.validation_failed",
          }),
          {
            status: 400,
            headers: {
              "Content-Type":
                "application/problem+json",
            },
          },
        ),
      ),
    );

    renderRegistrationPage();
    fillRegistrationForm();
    submitRegistration();

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Validation failed",
    );
  });

  it("shows a network error when Auth Service is unavailable", async () => {
    vi.stubGlobal(
      "fetch",
      vi.fn().mockRejectedValue(
        new TypeError("Failed to fetch"),
      ),
    );

    renderRegistrationPage();
    fillRegistrationForm();
    submitRegistration();

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Could not reach the server. Check your connection and try again.",
    );

    expect(getPendingRegistration()).toBeNull();
  });

  it("links back to login", () => {
    renderRegistrationPage();

    expect(
      screen.getByRole("link", {
        name: "Log in",
      }),
    ).toHaveAttribute("href", "/login");
  });
});