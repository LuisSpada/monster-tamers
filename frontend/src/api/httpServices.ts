import type { Services } from "./contracts";
import { endpoints } from "./endpoints";
import { createHttpClient } from "./httpClient";
import type {
  BattleEvent,
  BattleOutcome,
  BattleParticipant,
  BattleResult,
  BattleSummary,
  Encounter,
  Monster,
  PlayerProgress,
  TeamState,
} from "../domain/types";

interface BackendBattleResult {
  id: string;
  encounterId: string;
  encounterName: string;
  finalBoss: boolean;
  playedAt: string;
  outcome: BattleOutcome;
  rounds: number;
  participants: BattleParticipant[];
  events: BattleEvent[];
}

function toBattleResult(
  response: BackendBattleResult,
): BattleResult {
  return {
    id: response.id,
    encounterId: response.encounterId,
    encounterName: response.encounterName,
    isFinalBoss: response.finalBoss,
    playedAt: response.playedAt,
    outcome: response.outcome,
    rounds: response.rounds,
    participants: response.participants,
    events: response.events,
    rewards: {
      xp: [],
      levelUps: [],
      egg: null,
      finalBossDefeated: false,
    },
  };
}

/**
 * Real service implementation backed by the API Gateway.
 *
 * DTO-to-domain mapping is performed here whenever the backend
 * representation differs from the frontend domain representation.
 */
export function createHttpServices(
  baseUrl: string,
): Services {
  const http = createHttpClient(baseUrl);

  return {
    players: {
      getProgress: () =>
        http.get<PlayerProgress>(
          endpoints.playerProgress,
        ),
    },

    monsters: {
      listMonsters: () =>
        http.get<Monster[]>(
          endpoints.monsters,
        ),

      getMonster: (monsterId) =>
        http.get<Monster>(
          endpoints.monster(monsterId),
        ),

      spendStatPoint: (
        monsterId,
        stat,
      ) =>
        http.post<Monster>(
          endpoints.spendStatPoint(
            monsterId,
          ),
          { stat },
        ),
    },

    teams: {
      getTeam: () =>
        http.get<TeamState>(
          endpoints.team,
        ),

      saveTeam: (monsterIds) =>
        http.put<TeamState>(
          endpoints.team,
          { monsterIds },
        ),
    },

    encounters: {
      listEncounters: () =>
        http.get<Encounter[]>(
          endpoints.encounters,
        ),

      getEncounter: (encounterId) =>
        http.get<Encounter>(
          endpoints.encounter(
            encounterId,
          ),
        ),
    },

    battles: {
      startBattle: async (
        encounterId,
        monsterIds,
      ) => {
        const response =
          await http.post<BackendBattleResult>(
            endpoints.startBattle(
              encounterId,
            ),
            {
              monsterIds,
            },
          );

        return toBattleResult(response);
      },
    },

    history: {
      listBattles: () =>
        http.get<BattleSummary[]>(
          endpoints.battles,
        ),

      getBattle: async (battleId) => {
        const response =
          await http.get<BackendBattleResult>(
            endpoints.battle(battleId),
          );

        return toBattleResult(response);
      },
    },
  };
}