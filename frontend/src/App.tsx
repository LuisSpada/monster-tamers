import { Route, Routes } from "react-router-dom";
import Layout from "@/components/Layout";
import AuthPage from "@/pages/AuthPage";
import Home from "@/pages/Home";
import WorkInProgress from "@/pages/WorkInProgress";

export default function App() {
  return (
    <Routes>
      <Route path="login" element={<AuthPage mode="login" />} />
      <Route path="register" element={<AuthPage mode="register" />} />
      <Route element={<Layout />}>
        <Route index element={<Home />} />
        <Route path="encounters" element={<WorkInProgress title="Encounters" items={[
          "3–5 predefined encounters with egg chance and possible reward classes",
          "Encounter preview and start with the active team",
          "Automated result with turn-by-turn summary",
        ]} />} />
        <Route path="collection" element={<WorkInProgress title="Collection" items={[
          "All owned monsters with class, skill, trait, talent, level and XP",
          "Monster detail with stats and upgrade history",
          "Level-up screen: spend one stat point on Strength, HP, Accuracy or Agility",
          "Overview of the four classes: Fighter, Guardian, Shaman, Trickster",
        ]} />} />
        <Route path="team" element={<WorkInProgress title="Team" items={[
          "Active team of 1–3 ordered monsters",
          "Add, replace or remove monsters; team validity indicator",
          "Start encounter from the team window",
        ]} />} />
        <Route path="battle-history" element={<WorkInProgress title="Battle History" items={[
          "Your encounters, newest first",
          "Detail view with participants, XP, rewards and expandable event log",
        ]} />} />
        <Route path="profile" element={<WorkInProgress title="Profile" items={[
          "Player name and avatar",
          "Collection size, encounter progress, pending points and final-boss status",
        ]} />} />
        <Route path="settings" element={<WorkInProgress title="Settings" />} />
        <Route path="*" element={<WorkInProgress title="Not found" />} />
      </Route>
    </Routes>
  );
}
