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
  getAccessToken,
} from "@/api/authSession";
import {
  getPendingRegistration,
  setPendingRegistration,
  type PendingRegistration,
} from "@/api/registrationSession";
import RegistrationConfirmationPage from "./RegistrationConfirmationPage";

const pendingRegistration: PendingRegistration = {
  registrationId:
    "d0775e50-aad9-4fd3-a016-c6efa18df154",
  email: "tamer@example.com",
};

function LocationProbe() {
  const location = useLocation();

  return (
    <div data-testid="location">
      {location.pathname}
    </div>
  );
}

function renderConfirmationPage(
  options: {
    routerState?: PendingRegistration;
    useStoredRegistration?: boolean;
  } = {},
) {
  if (options.useStoredRegistration) {
    setPendingRegistration(
      pendingRegistration,
    );
  }

  const initialEntry = options.routerState
    ? {
        pathname: "/register/confirm",
        state: options.routerState,
      }
    : "/register/confirm";

  return render(
    <MemoryRouter initialEntries={[initialEntry]}>
      <Routes>
        <Route
          path="/register/confirm"
          element={
            <RegistrationConfirmationPage />
          }
        />

        <Route
          path="/login"
          element={<LocationProbe />}
        />

        <Route
          path="/register"
          element={
            <div>Registration page</div>
          }
        />
      </Routes>
    </MemoryRouter>,
  );
}

function fillConfirmationCode(
  code = "123456",
) {
  fireEvent.change(
    screen.getByLabelText(
      "Confirmation code",
    ),
    {
      target: {
        value: code,
      },
    },
  );
}

function submitConfirmation() {
  fireEvent.click(
    screen.getByRole("button", {
      name: "Confirm registration",
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

describe("RegistrationConfirmationPage", () => {
  it("uses registration data from router state", () => {
    renderConfirmationPage({
      routerState: pendingRegistration,
    });

    expect(
      screen.getByText(
        "tamer@example.com",
      ),
    ).toBeInTheDocument();

    expect(
      screen.getByLabelText(
        "Confirmation code",
      ),
    ).toHaveAttribute(
      "pattern",
      "[0-9]{6}",
    );
  });

  it("restores pending registration from sessionStorage", () => {
    renderConfirmationPage({
      useStoredRegistration: true,
    });

    expect(
      screen.getByText(
        "tamer@example.com",
      ),
    ).toBeInTheDocument();

    expect(
      screen.getByRole("button", {
        name: "Confirm registration",
      }),
    ).toBeInTheDocument();
  });

  it("does not call the backend when confirmation state is missing", () => {
    const fetchMock = vi.fn();

    vi.stubGlobal("fetch", fetchMock);

    renderConfirmationPage();

    expect(
      screen.getByRole("alert"),
    ).toHaveTextContent(
      "No pending registration was found.",
    );

    expect(
      screen.getByRole("link", {
        name: "Back to registration",
      }),
    ).toHaveAttribute(
      "href",
      "/register",
    );

    expect(fetchMock).not.toHaveBeenCalled();
  });

  it("confirms registration and redirects to login", async () => {
    setPendingRegistration(
      pendingRegistration,
    );

    const fetchMock = vi.fn().mockResolvedValue(
      new Response(null, {
        status: 201,
      }),
    );

    vi.stubGlobal("fetch", fetchMock);

    renderConfirmationPage({
      routerState: pendingRegistration,
    });

    fillConfirmationCode();
    submitConfirmation();

    await waitFor(() => {
      expect(
        screen.getByTestId("location"),
      ).toHaveTextContent("/login");
    });

    expect(fetchMock).toHaveBeenCalledWith(
      "http://localhost:8080/api/auth/registrations/d0775e50-aad9-4fd3-a016-c6efa18df154/confirmation",
      expect.objectContaining({
        method: "POST",
        credentials: "include",
        body: JSON.stringify({
          code: "123456",
        }),
      }),
    );

    expect(
      getPendingRegistration(),
    ).toBeNull();

    expect(getAccessToken()).toBeNull();
  });

  it.each([
    {
      code: "auth.confirmation_code_invalid",
      detail:
        "Confirmation code is invalid",
    },
    {
      code: "auth.confirmation_code_expired",
      detail:
        "Confirmation code has expired",
    },
    {
      code:
        "auth.confirmation_attempts_exceeded",
      detail:
        "Too many confirmation attempts",
    },
  ])(
    "shows $code Problem Details",
    async ({ code, detail }) => {
      vi.stubGlobal(
        "fetch",
        vi.fn().mockResolvedValue(
          new Response(
            JSON.stringify({
              status:
                code ===
                "auth.confirmation_attempts_exceeded"
                  ? 429
                  : 400,
              detail,
              code,
            }),
            {
              status:
                code ===
                "auth.confirmation_attempts_exceeded"
                  ? 429
                  : 400,
              headers: {
                "Content-Type":
                  "application/problem+json",
              },
            },
          ),
        ),
      );

      renderConfirmationPage({
        routerState: pendingRegistration,
      });

      fillConfirmationCode();
      submitConfirmation();

      expect(
        await screen.findByRole("alert"),
      ).toHaveTextContent(detail);

      expect(
        screen.getByLabelText(
          "Confirmation code",
        ),
      ).toBeInTheDocument();
    },
  );

  it("shows a network error and allows another attempt", async () => {
    const fetchMock = vi
      .fn()
      .mockRejectedValueOnce(
        new TypeError("Failed to fetch"),
      )
      .mockResolvedValueOnce(
        new Response(null, {
          status: 201,
        }),
      );

    vi.stubGlobal("fetch", fetchMock);

    renderConfirmationPage({
      routerState: pendingRegistration,
    });

    fillConfirmationCode();
    submitConfirmation();

    expect(
      await screen.findByRole("alert"),
    ).toHaveTextContent(
      "Could not reach the server. Check your connection and try again.",
    );

    submitConfirmation();

    await waitFor(() => {
      expect(
        screen.getByTestId("location"),
      ).toHaveTextContent("/login");
    });

    expect(fetchMock).toHaveBeenCalledTimes(2);
  });

  it("provides a link to restart registration", () => {
    renderConfirmationPage({
      routerState: pendingRegistration,
    });

    expect(
      screen.getByRole("link", {
        name: "Start again",
      }),
    ).toHaveAttribute(
      "href",
      "/register",
    );
  });
});