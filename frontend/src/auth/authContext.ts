import { createContext } from "react";

export interface AuthContextValue {
  isAuthenticated: boolean;
  signIn(accessToken: string): void;
  logout(): void;
}

export const AuthContext =
  createContext<AuthContextValue | null>(null);