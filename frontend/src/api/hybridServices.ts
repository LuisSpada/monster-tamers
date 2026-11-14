import type { Services } from "./contracts";
import { createHttpServices } from "./httpServices";
import {
  createMockServices,
  type MockOptions,
} from "./mock/mockServices";

/**
 * Uses the real backend only for functionality that has been integrated.
 *
 * Current HTTP-backed operations:
 * - monsters.listMonsters()
 * - teams.getTeam()
 * - teams.saveTeam()
 * - encounters.listEncounters()
 * - encounters.getEncounter()
 *
 * Player progress, monster details, stat spending,
 * battles and history remain mock-backed.
 */
export function createHybridServices(
  baseUrl: string,
  mockOptions: MockOptions = {},
): Services {
  const mockServices =
    createMockServices(mockOptions);

  const httpServices =
    createHttpServices(baseUrl);

  return {
    ...mockServices,

    monsters: {
      ...mockServices.monsters,
      listMonsters:
        httpServices.monsters.listMonsters,
    },

    teams: {
      getTeam: httpServices.teams.getTeam,
      saveTeam: httpServices.teams.saveTeam,
    },

    encounters: {
      listEncounters:
        httpServices.encounters.listEncounters,

      getEncounter:
        httpServices.encounters.getEncounter,
    },
  };
}