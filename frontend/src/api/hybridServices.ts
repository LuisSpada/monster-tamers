import type { Services } from "./contracts";
import { createHttpServices } from "./httpServices";
import {
  createMockServices,
  type MockOptions,
} from "./mock/mockServices";
import type { BattleResult } from "../domain/types";

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
 *
 * Mock-backed operations:
 * - players.getProgress()
 * - monsters.getMonster()
 * - monsters.spendStatPoint()
 * - history.listBattles()
 * - persisted history fallback
 *
 * A newly started battle is cached in memory so that the battle-result
 * route can display it immediately. The backend does not yet provide
 * persisted battle-history lookup for this result.
 */
export function createHybridServices(
  baseUrl: string,
  mockOptions: MockOptions = {},
): Services {
  const mockServices =
    createMockServices(mockOptions);

  const httpServices =
    createHttpServices(baseUrl);

  const recentBattles =
    new Map<string, BattleResult>();

  return {
    players: mockServices.players,

    monsters: {
      getMonster:
        mockServices.monsters.getMonster,

      listMonsters:
        httpServices.monsters.listMonsters,

      listSpecies:
        httpServices.monsters.listSpecies,

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
      startBattle: async (
        encounterId,
        monsterIds,
      ) => {
        const result =
          await httpServices.battles
            .startBattle(
              encounterId,
              monsterIds,
            );

        recentBattles.set(
          result.id,
          result,
        );

        return result;
      },
    },

    history: {
      listBattles:
        mockServices.history.listBattles,

      getBattle: async (
        battleId,
      ) => {
        const recentResult =
          recentBattles.get(battleId);

        if (recentResult) {
          return recentResult;
        }

        return mockServices.history
          .getBattle(battleId);
      },
    },
  };
}