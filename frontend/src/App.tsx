import { Route, Routes } from "react-router-dom";
import { gameRouteElements } from "@/app/gameRoutes";
import { ProtectedRoute } from "@/auth/ProtectedRoute";
import Layout from "@/components/Layout";
import AuthPage from "@/pages/AuthPage";
import Home from "@/pages/Home";
import RegistrationConfirmationPage from "@/pages/RegistrationConfirmationPage";
import RegistrationPage from "@/pages/RegistrationPage";

export default function App() {
  return (
    <Routes>
      <Route
        path="login"
        element={<AuthPage />}
      />

      <Route
        path="register"
        element={<RegistrationPage />}
      />

      <Route
        path="register/confirm"
        element={
          <RegistrationConfirmationPage />
        }
      />

      <Route element={<ProtectedRoute />}>
        <Route element={<Layout />}>
          <Route index element={<Home />} />
          {gameRouteElements}
        </Route>
      </Route>
    </Routes>
  );
}