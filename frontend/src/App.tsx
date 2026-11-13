import { Route, Routes } from "react-router-dom";
import { gameRouteElements } from "@/app/gameRoutes";
import { ProtectedRoute } from "@/auth/ProtectedRoute";
import Layout from "@/components/Layout";
import AuthPage from "@/pages/AuthPage";
import Home from "@/pages/Home";

export default function App() {
  return (
    <Routes>
      <Route
        path="login"
        element={<AuthPage mode="login" />}
      />

      <Route
        path="register"
        element={<AuthPage mode="register" />}
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