import type { ReactElement } from "react";
import { cleanup, render } from "@testing-library/react";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { LocationProbe } from "./LocationProbe";
import { afterEach } from "vitest";
import { createMockServices, type MockOptions } from "../api/mock/mockServices";
import type { Services } from "../api/contracts";
import { ServicesProvider } from "../api/ServicesProvider";
import { gameRouteElements } from "../app/gameRoutes";

afterEach(() => cleanup());

export function createTestServices(options: MockOptions = {}, overrides: Partial<Services> = {}): Services {
  return { ...createMockServices({ latencyMs: 0, ...options }), ...overrides };
}

/** Renders one page element at a route pattern. */
export function renderPage(ui: ReactElement, opts: { route?: string; path?: string; services?: Services } = {}) {
  const { route = "/", path = "/", services = createTestServices() } = opts;
  return render(
    <ServicesProvider services={services}>
      <MemoryRouter initialEntries={[route]}>
        <Routes>
          <Route path={path} element={ui} />
          <Route path="*" element={<LocationProbe />} />
        </Routes>
      </MemoryRouter>
    </ServicesProvider>,
  );
}

/** Renders the complete set of game routes, for navigation flows. */
export function renderGameApp(route: string, services: Services = createTestServices()) {
  return render(
    <ServicesProvider services={services}>
      <MemoryRouter initialEntries={[route]}>
        <Routes>{gameRouteElements}</Routes>
      </MemoryRouter>
    </ServicesProvider>,
  );
}
