import type { Services } from "./contracts";
import { endpoints } from "./endpoints";
import { createHttpClient } from "./httpClient";
import type {
  BattleResult,
  BattleSummary,
  Encounter,
  Monster,
  PlayerProgress,
  TeamState,
} from "../domain/types";

/**
 * Real implementation backed by the gateway. Not used until VITE_API_MODE=http.
 * TODO(gateway): add DTO -> domain mapping here if the backend payloads differ from the domain types.
 */
export function createHttpServices(baseUrl: string): Services {
  const http = createHttpClient(baseUrl);
  return {
    players: {
      getProgress: () => http.get<PlayerProgress>(endpoints.playerProgress),
    },
    monsters: {
      listMonsters: () => http.get<Monster[]>(endpoints.monsters),
      getMonster: (id) => http.get<Monster>(endpoints.monster(id)),
      spendStatPoint: (id, stat) => http.post<Monster>(endpoints.spendStatPoint(id), { stat }),
    },
    teams: {
      getTeam: () => http.get<TeamState>(endpoints.team),
      saveTeam: (monsterIds) => http.put<TeamState>(endpoints.team, { monsterIds }),
    },
    encounters: {
      listEncounters: () => http.get<Encounter[]>(endpoints.encounters),
      getEncounter: (id) => http.get<Encounter>(endpoints.encounter(id)),
    },
    battles: {
      startBattle: (encounterId, monsterIds) =>
        http.post<BattleResult>(endpoints.startBattle(encounterId), { monsterIds }),
    },
    history: {
      listBattles: () => http.get<BattleSummary[]>(endpoints.battles),
      getBattle: (id) => http.get<BattleResult>(endpoints.battle(id)),
    },
  };
}
