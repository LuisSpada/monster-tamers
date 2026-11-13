import {
  Navigate,
  Outlet,
  useLocation,
} from "react-router-dom";
import { useAuth } from "./useAuth";

export interface LoginNavigationState {
  returnTo?: string;
}

export function ProtectedRoute() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (isAuthenticated) {
    return <Outlet />;
  }

  const returnTo =
    location.pathname +
    location.search +
    location.hash;

  return (
    <Navigate
      to="/login"
      replace
      state={{
        returnTo,
      } satisfies LoginNavigationState}
    />
  );
}