import {
  cleanup,
  render,
  screen,
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
} from "vitest";
import {
  clearAccessToken,
  setAccessToken,
} from "@/api/authSession";
import { AuthProvider } from "./AuthProvider";
import {
  ProtectedRoute,
  type LoginNavigationState,
} from "./ProtectedRoute";

function LoginProbe() {
  const location = useLocation();

  const state =
    location.state as LoginNavigationState | null;

  return (
    <>
      <div data-testid="location">
        {location.pathname}
      </div>

      <div data-testid="return-to">
        {state?.returnTo ?? ""}
      </div>
    </>
  );
}

function renderProtectedRoute(
  initialRoute = "/collection",
) {
  return render(
    <MemoryRouter initialEntries={[initialRoute]}>
      <AuthProvider>
        <Routes>
          <Route
            path="/login"
            element={<LoginProbe />}
          />

          <Route element={<ProtectedRoute />}>
            <Route
              path="/collection"
              element={
                <div>Protected collection</div>
              }
            />
          </Route>
        </Routes>
      </AuthProvider>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  sessionStorage.clear();
});

afterEach(() => {
  clearAccessToken();
  cleanup();
});

describe("ProtectedRoute", () => {
  it("redirects unauthenticated users to login", () => {
    renderProtectedRoute();

    expect(
      screen.getByTestId("location"),
    ).toHaveTextContent("/login");
  });

  it("preserves the complete requested route", () => {
    renderProtectedRoute(
      "/collection?class=Fighter#owned",
    );

    expect(
      screen.getByTestId("return-to"),
    ).toHaveTextContent(
      "/collection?class=Fighter#owned",
    );
  });

  it("renders protected content with an existing token", () => {
    setAccessToken("existing-session-token");

    renderProtectedRoute();

    expect(
      screen.getByText("Protected collection"),
    ).toBeInTheDocument();
  });
});