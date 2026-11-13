import { fireEvent, screen, waitFor } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import { ApiError } from "../api/errors";
import { CollectionPage } from "../pages/CollectionPage";
import {
  createTestServices,
  renderPage,
} from "./gameTestUtils";

describe("CollectionPage", () => {
  it("shows a loading state and then the monsters", async () => {
    renderPage(<CollectionPage />);

    expect(screen.getByRole("status")).toBeTruthy();
    expect(
      await screen.findByText("Fierce Fox"),
    ).toBeTruthy();
    expect(
      screen.getByText(/Showing 8 of 8 monsters/),
    ).toBeTruthy();
  });

  it("marks monsters that are in the team", async () => {
    renderPage(<CollectionPage />);

    await screen.findByText("Fierce Fox");

    expect(screen.getByText("Team #1")).toBeTruthy();
    expect(screen.getByText("Team #2")).toBeTruthy();
  });

  it("filters by class", async () => {
    renderPage(<CollectionPage />);

    await screen.findByText("Fierce Fox");

    fireEvent.click(
      screen.getByRole("button", {
        name: "Shaman",
      }),
    );

    expect(
      screen.getByText(/Showing 2 of 8/),
    ).toBeTruthy();
    expect(
      screen.queryByText("Fierce Fox"),
    ).toBeNull();
  });

  it("shows an empty state when nothing matches and lets the user clear the filters", async () => {
    renderPage(<CollectionPage />);

    await screen.findByText("Fierce Fox");

    fireEvent.change(
      screen.getByLabelText(/search by name/i),
      {
        target: {
          value: "zzz",
        },
      },
    );

    expect(
      screen.getByText(
        "No monsters match your filters",
      ),
    ).toBeTruthy();

    fireEvent.click(
      screen.getByRole("button", {
        name: "Clear filters",
      }),
    );

    expect(
      await screen.findByText("Fierce Fox"),
    ).toBeTruthy();
  });

  it("links every card to the monster details", async () => {
    renderPage(<CollectionPage />);

    const link = await screen.findByRole("link", {
      name: /Fierce Fox/,
    });

    expect(link.getAttribute("href")).toBe(
      "/collection/m1",
    );
  });

  it("shows an error and recovers on retry", async () => {
    let fail = true;
    const base = createTestServices();

    const services = createTestServices(
      {},
      {
        monsters: {
          ...base.monsters,
          listMonsters: () =>
            fail
              ? Promise.reject(
                  new ApiError(
                    500,
                    "X",
                    "Server exploded",
                  ),
                )
              : base.monsters.listMonsters(),
        },
      },
    );

    renderPage(<CollectionPage />, {
      services,
    });

    expect(
      await screen.findByRole("alert"),
    ).toBeTruthy();
    expect(
      screen.getByText("Server exploded"),
    ).toBeTruthy();

    fail = false;

    fireEvent.click(
      screen.getByRole("button", {
        name: "Try again",
      }),
    );

    await waitFor(() => {
      expect(
        screen.getByText("Fierce Fox"),
      ).toBeTruthy();
    });
  });

  it("shows the collection empty state when the backend returns no monsters", async () => {
    const base = createTestServices();

    const services = createTestServices(
      {},
      {
        monsters: {
          ...base.monsters,
          listMonsters: () => Promise.resolve([]),
        },
      },
    );

    renderPage(<CollectionPage />, {
      services,
    });

    expect(
      await screen.findByText(
        "Your collection is empty",
      ),
    ).toBeTruthy();

    expect(
      screen.getByText(
        "Win encounters to hatch eggs and grow your collection.",
      ),
    ).toBeTruthy();

    expect(
      screen.getByText("Showing 0 of 0 monsters"),
    ).toBeTruthy();
  });
});