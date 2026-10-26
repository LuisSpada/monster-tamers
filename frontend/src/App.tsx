import { Route, Routes } from "react-router-dom";
import Layout from "@/components/Layout";
import AuthPage from "@/pages/AuthPage";
import Home from "@/pages/Home";
import { CollectionPage } from "@/pages/CollectionPage";
import { EncounterListPage } from "@/pages/EncounterListPage";
import { HistoryPage } from "@/pages/HistoryPage";
import { TeamPage } from "@/pages/TeamPage";
import { MonsterDetailPage } from "@/pages/MonsterDetailPage";
import WorkInProgress from "@/pages/WorkInProgress";
import { NotFoundPage } from "@/pages/NotFoundPage";

export default function App() {
  return (
    <Routes>
      <Route path="login" element={<AuthPage mode="login" />} />
      <Route path="register" element={<AuthPage mode="register" />} />
      <Route element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="encounters" element={<EncounterListPage />} />
        <Route path="collection" element={<CollectionPage />} />
        <Route path="collection/:monsterId" element={<MonsterDetailPage />} />
        <Route path="team" element={<TeamPage />} />
        <Route path="battle-history" element={<HistoryPage />} />
        <Route path="profile" element={<WorkInProgress title="Profile" items={[
          "Player name and avatar",
          "Collection size, encounter progress, pending points and final-boss status",
        ]} />} />
        <Route path="settings" element={<WorkInProgress title="Settings" />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  );
}
