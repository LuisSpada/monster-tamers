import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { describe, expect, it } from "vitest";
import AuthPage from "./AuthPage";

describe("AuthPage", () => {
  it("renders registration credentials and a six-digit confirmation field", () => {
    render(
      <MemoryRouter>
        <AuthPage mode="register" />
      </MemoryRouter>,
    );

    expect(screen.getByLabelText("Email")).toHaveAttribute("name", "email");
    expect(screen.getByLabelText("Password")).toHaveAttribute("name", "password");
    expect(screen.getByLabelText("Confirm password")).toHaveAttribute(
      "name",
      "confirmPassword",
    );
    expect(screen.getByRole("textbox", { name: "Confirmation code" })).toHaveAttribute(
      "pattern",
      "[0-9]{6}",
    );
  });
});
