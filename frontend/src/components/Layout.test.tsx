import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { afterEach, describe, expect, it } from "vitest";
import Layout from "./Layout";

afterEach(cleanup);

describe("Layout", () => {
  it("starts with a closed mobile sidebar and exposes an accessible toggle", () => {
    render(
      <MemoryRouter>
        <Layout />
      </MemoryRouter>,
    );

    expect(screen.getByRole("button", { name: "Open sidebar" })).toHaveAttribute(
      "aria-expanded",
      "false",
    );
    expect(screen.getByRole("navigation")).toBeInTheDocument();
  });

  it("opens and closes the sidebar from the header toggle", () => {
    render(
      <MemoryRouter>
        <Layout />
      </MemoryRouter>,
    );

    const toggle = screen.getByRole("button", { name: "Open sidebar" });
    fireEvent.click(toggle);

    expect(screen.getByRole("button", { name: "Close sidebar", expanded: true })).toHaveAttribute(
      "aria-expanded",
      "true",
    );

    fireEvent.click(screen.getByRole("button", { name: "Close sidebar", expanded: true }));

    expect(screen.getByRole("button", { name: "Open sidebar" })).toHaveAttribute(
      "aria-expanded",
      "false",
    );
  });
});
