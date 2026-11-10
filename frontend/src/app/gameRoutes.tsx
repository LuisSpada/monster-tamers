import { Route } from "react-router-dom";
import { paths } from "./paths";
import { BattlePreparationPage } from "../pages/BattlePreparationPage";
import { BattleResultPage } from "../pages/BattleResultPage";
import { CollectionPage } from "../pages/CollectionPage";
import { EncounterDetailPage } from "../pages/EncounterDetailPage";
import { EncounterListPage } from "../pages/EncounterListPage";
import { HistoryDetailPage } from "../pages/HistoryDetailPage";
import { HistoryPage } from "../pages/HistoryPage";
import { MonsterDetailPage } from "../pages/MonsterDetailPage";
import { NotFoundPage } from "../pages/NotFoundPage";
import { ProfilePage } from "../pages/ProfilePage";
import { SettingsPage } from "../pages/SettingsPage";
import { TeamPage } from "../pages/TeamPage";

/**
 * All game routes as one fragment. Place it inside the layout route (see the integration guide):
 *   <Route element={<Layout />}> ... {gameRouteElements} </Route>
 */
export const gameRouteElements = (
  <>
    <Route path={paths.patterns.collection} element={<CollectionPage />} />
    <Route path={paths.patterns.monster} element={<MonsterDetailPage />} />
    <Route path={paths.patterns.team} element={<TeamPage />} />
    <Route path={paths.patterns.encounters} element={<EncounterListPage />} />
    <Route path={paths.patterns.encounter} element={<EncounterDetailPage />} />
    <Route path={paths.patterns.prepare} element={<BattlePreparationPage />} />
    <Route path={paths.patterns.battleResult} element={<BattleResultPage />} />
    <Route path={paths.patterns.history} element={<HistoryPage />} />
    <Route path={paths.patterns.historyDetail} element={<HistoryDetailPage />} />
    <Route path={paths.patterns.profile} element={<ProfilePage />} />
    <Route path={paths.patterns.settings} element={<SettingsPage />} />
    <Route path="*" element={<NotFoundPage />} />
  </>
);
