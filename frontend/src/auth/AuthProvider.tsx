import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import {
  AUTH_SESSION_CHANGED_EVENT,
  clearAccessToken,
  getAccessToken,
  setAccessToken,
} from "@/api/authSession";
import {
  AuthContext,
  type AuthContextValue,
} from "./authContext";

export function AuthProvider({
  children,
}: {
  children: ReactNode;
}) {
  const [isAuthenticated, setIsAuthenticated] =
    useState(() => Boolean(getAccessToken()));

  useEffect(() => {
    function refreshAuthenticationState() {
      setIsAuthenticated(Boolean(getAccessToken()));
    }

    window.addEventListener(
      AUTH_SESSION_CHANGED_EVENT,
      refreshAuthenticationState,
    );

    return () => {
      window.removeEventListener(
        AUTH_SESSION_CHANGED_EVENT,
        refreshAuthenticationState,
      );
    };
  }, []);

  const signIn = useCallback(
    (accessToken: string) => {
      setAccessToken(accessToken);
    },
    [],
  );

  const logout = useCallback(() => {
    clearAccessToken();
  }, []);

  const value = useMemo<AuthContextValue>(
    () => ({
      isAuthenticated,
      signIn,
      logout,
    }),
    [isAuthenticated, signIn, logout],
  );

  return (
    <AuthContext.Provider value={value}>
      {children}
    </AuthContext.Provider>
  );
}