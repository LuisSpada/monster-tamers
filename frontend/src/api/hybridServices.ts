import type { Services } from "./contracts";
import { createHttpServices } from "./httpServices";
import {
  createMockServices,
  type MockOptions,
} from "./mock/mockServices";

/**
 * Uses the real backend for functionality that has been integrated.
 *
 * HTTP-backed operations:
 * - monsters.listMonsters()
 * - teams.getTeam()
 * - teams.saveTeam()
 * - encounters.listEncounters()
 * - encounters.getEncounter()
 * - battles.startBattle()
 * - history.listBattles()
 * - history.getBattle()
 *
 * Mock-backed operations:
 * - players.getProgress()
 * - monsters.getMonster()
 * - monsters.spendStatPoint()
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
    players: mockServices.players,

    monsters: {
      getMonster:
        mockServices.monsters.getMonster,

      listMonsters:
        httpServices.monsters.listMonsters,

      spendStatPoint:
        mockServices.monsters
          .spendStatPoint,
    },

    teams: {
      getTeam:
        httpServices.teams.getTeam,

      saveTeam:
        httpServices.teams.saveTeam,
    },

    encounters: {
      listEncounters:
        httpServices.encounters
          .listEncounters,

      getEncounter:
        httpServices.encounters
          .getEncounter,
    },

    battles: {
      startBattle:
        httpServices.battles.startBattle,
    },

    history: {
      listBattles:
        httpServices.history.listBattles,

      getBattle:
        httpServices.history.getBattle,
    },
  };
}
