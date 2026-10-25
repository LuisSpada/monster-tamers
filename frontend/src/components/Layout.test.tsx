import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it } from "vitest";
import Layout from "./Layout";

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
});
