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
} from "vitest";
import {
  getAccessToken,
  setAccessToken,
} from "@/api/authSession";
import { ServicesProvider } from "@/api/ServicesProvider";
import { AuthProvider } from "@/auth/AuthProvider";
import {
  createTestServices,
} from "@/test/gameTestUtils";
import Layout from "./Layout";

function LocationProbe() {
  const location = useLocation();

  return (
    <div data-testid="location">
      {location.pathname}
    </div>
  );
}

function renderLayout() {
  const services = createTestServices();

  return render(
    <MemoryRouter initialEntries={["/"]}>
      <AuthProvider>
        <ServicesProvider services={services}>
          <Routes>
            <Route
              path="/login"
              element={<LocationProbe />}
            />

            <Route element={<Layout />}>
              <Route
                index
                element={<div>Home content</div>}
              />
            </Route>
          </Routes>
        </ServicesProvider>
      </AuthProvider>
    </MemoryRouter>,
  );
}

beforeEach(() => {
  sessionStorage.clear();
  setAccessToken("test-access-token");
});

afterEach(() => {
  cleanup();
  sessionStorage.clear();
});

describe("Layout", () => {
  it("starts with a closed mobile sidebar and exposes an accessible toggle", () => {
    renderLayout();

    expect(
      screen.getByRole("button", {
        name: "Open sidebar",
      }),
    ).toHaveAttribute(
      "aria-expanded",
      "false",
    );

    expect(
      screen.getByRole("navigation"),
    ).toBeInTheDocument();
  });

  it("opens and closes the sidebar from the header toggle", () => {
    renderLayout();

    const openButton = screen.getByRole(
      "button",
      {
        name: "Open sidebar",
      },
    );

    fireEvent.click(openButton);

    const closeButton = screen.getByRole(
      "button",
      {
        name: "Close sidebar",
        expanded: true,
      },
    );

    expect(closeButton).toHaveAttribute(
      "aria-expanded",
      "true",
    );

    fireEvent.click(closeButton);

    expect(
      screen.getByRole("button", {
        name: "Open sidebar",
      }),
    ).toHaveAttribute(
      "aria-expanded",
      "false",
    );
  });

  it("logs out from the sidebar and navigates to login", async () => {
    renderLayout();

    fireEvent.click(
      screen.getByRole("button", {
        name: "Open sidebar",
      }),
    );

    fireEvent.click(
      screen.getByRole("button", {
        name: "Log out",
      }),
    );

    await waitFor(() => {
      expect(getAccessToken()).toBeNull();
    });

    expect(
      screen.getByTestId("location"),
    ).toHaveTextContent("/login");
  });

  it("logs out from the user menu and navigates to login", async () => {
    renderLayout();

    fireEvent.click(
      screen.getByRole("button", {
        name: "User menu",
      }),
    );

    fireEvent.click(
      screen.getByRole("menuitem", {
        name: "Log out",
      }),
    );

    await waitFor(() => {
      expect(getAccessToken()).toBeNull();
    });

    expect(
      screen.getByTestId("location"),
    ).toHaveTextContent("/login");
  });
});