import { fireEvent, screen, waitFor } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ApiError } from "../api/errors";
import { MonstersPage } from "../pages/MonstersPage";
import { createTestServices, renderPage } from "./gameTestUtils";

describe("MonstersPage", () => {
  it("shows a loading state and then all species", async () => {
    renderPage(<MonstersPage />);

    expect(screen.getByRole("status")).toBeTruthy();
    expect(await screen.findByText("Fierce Fox")).toBeTruthy();
    expect(screen.getByText("Frost Titan")).toBeTruthy();
    expect(screen.getByText(/Showing 19 of 19 species/)).toBeTruthy();
  });

  it("filters by class", async () => {
    renderPage(<MonstersPage />);

    await screen.findByText("Fierce Fox");

    fireEvent.click(screen.getByRole("button", { name: "Shaman" }));

    expect(screen.getByText(/Showing 4 of 19/)).toBeTruthy();
    expect(screen.queryByText("Fierce Fox")).toBeNull();
  });

  it("shows an empty state when nothing matches and lets the user clear the filters", async () => {
    renderPage(<MonstersPage />);

    await screen.findByText("Fierce Fox");

    fireEvent.change(screen.getByLabelText(/search by name/i), {
      target: { value: "zzz" },
    });

    expect(screen.getByText("No species match your filters")).toBeTruthy();

    fireEvent.click(screen.getByRole("button", { name: "Clear filters" }));

    expect(await screen.findByText("Fierce Fox")).toBeTruthy();
  });

  it("shows an error and recovers on retry", async () => {
    let fail = true;
    const base = createTestServices();

    const services = createTestServices(
      {},
      {
        monsters: {
          ...base.monsters,
          listSpecies: () =>
            fail
              ? Promise.reject(new ApiError(500, "X", "Server exploded"))
              : base.monsters.listSpecies(),
        },
      },
    );

    renderPage(<MonstersPage />, { services });

    expect(await screen.findByRole("alert")).toBeTruthy();
    expect(screen.getByText("Server exploded")).toBeTruthy();

    fail = false;

    fireEvent.click(screen.getByRole("button", { name: "Try again" }));

    await waitFor(() => {
      expect(screen.getByText("Fierce Fox")).toBeTruthy();
    });
  });
});