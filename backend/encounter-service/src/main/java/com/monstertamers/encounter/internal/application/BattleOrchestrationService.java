package com.monstertamers.encounter.internal.application;

import com.monstertamers.encounter.internal.domain.AccountEncounterProgress;
import com.monstertamers.encounter.internal.domain.Encounter;
import com.monstertamers.encounter.internal.domain.EncounterOpponent;
import com.monstertamers.encounter.internal.domain.EncounterStatus;
import com.monstertamers.encounter.internal.error.EncounterErrorCode;
import com.monstertamers.encounter.internal.integration.monster.BattleMonsterDto;
import com.monstertamers.encounter.internal.integration.monster.BattleMonsterRequest;
import com.monstertamers.encounter.internal.integration.monster.MonsterClient;
import com.monstertamers.encounter.internal.integration.simulator.SimulatorBattleRequest;
import com.monstertamers.encounter.internal.integration.simulator.SimulatorBattleResponse;
import com.monstertamers.encounter.internal.integration.simulator.SimulatorClient;
import com.monstertamers.encounter.internal.persistence.AccountEncounterProgressRepository;
import com.monstertamers.encounter.internal.persistence.EncounterOpponentRepository;
import com.monstertamers.encounter.internal.persistence.EncounterRepository;
import com.monstertamers.encounter.internal.web.dto.request.StartBattleRequest;
import com.monstertamers.encounter.internal.web.dto.response.BattleResultDto;
import com.monstertamers.web.error.ApiException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;

@Service
@RequiredArgsConstructor
public class BattleOrchestrationService {

    private static final int MIN_TEAM_SIZE = 1;
    private static final int MAX_TEAM_SIZE = 3;

    private final EncounterRepository encounterRepository;
    private final EncounterOpponentRepository opponentRepository;
    private final AccountEncounterProgressRepository progressRepository;
    private final MonsterClient monsterClient;
    private final SimulatorClient simulatorClient;

    @Transactional(readOnly = true)
    public BattleResultDto startBattle(
            UUID accountId,
            UUID encounterId,
            StartBattleRequest request
    ) {
        validateTeam(request.monsterIds());

        Encounter encounter = encounterRepository
                .findById(encounterId)
                .orElseThrow(() ->
                        new ApiException(
                                EncounterErrorCode
                                        .ENCOUNTER_NOT_FOUND
                        )
                );

        EncounterStatus status = resolveStatus(
                accountId,
                encounter
        );

        if (status == EncounterStatus.locked) {
            throw new ApiException(
                    EncounterErrorCode.ENCOUNTER_LOCKED
            );
        }

        List<EncounterOpponent> opponents =
                opponentRepository
                        .findAllByEncounterIdOrderByIdAsc(
                                encounterId
                        );

        List<BattleMonsterDto> monsters =
                loadBattleMonsters(
                        accountId,
                        request.monsterIds()
                );

        validateMonsterResponse(
                request.monsterIds(),
                monsters
        );

        SimulatorBattleRequest simulatorRequest =
                toSimulatorRequest(
                        encounter,
                        opponents,
                        monsters
                );

        SimulatorBattleResponse simulatorResponse =
                simulate(simulatorRequest);

        validateSimulatorResponse(
                encounterId,
                simulatorResponse
        );

        return toResultDto(simulatorResponse);
    }

    private EncounterStatus resolveStatus(
            UUID accountId,
            Encounter encounter
    ) {
        return progressRepository
                .findByAccountIdAndEncounterId(
                        accountId,
                        encounter.getId()
                )
                .map(
                        AccountEncounterProgress::getStatus
                )
                .orElseGet(() -> {
                    if (encounter.getOrder() == 1) {
                        return EncounterStatus.available;
                    }

                    return EncounterStatus.locked;
                });
    }

    private List<BattleMonsterDto> loadBattleMonsters(
            UUID accountId,
            List<UUID> monsterIds
    ) {
        try {
            return monsterClient.loadBattleMonsters(
                    accountId,
                    new BattleMonsterRequest(
                            List.copyOf(monsterIds)
                    )
            );
        } catch (RestClientException exception) {
            throw new ApiException(
                    EncounterErrorCode
                            .BATTLE_MONSTERS_UNAVAILABLE
            );
        }
    }

    private SimulatorBattleResponse simulate(
            SimulatorBattleRequest request
    ) {
        try {
            return simulatorClient.simulate(request);
        } catch (RestClientException exception) {
            throw new ApiException(
                    EncounterErrorCode
                            .SIMULATOR_UNAVAILABLE
            );
        }
    }

    private void validateTeam(
            List<UUID> monsterIds
    ) {
        if (
                monsterIds == null
                        || monsterIds.size()
                        < MIN_TEAM_SIZE
                        || monsterIds.size()
                        > MAX_TEAM_SIZE
                        || monsterIds.stream()
                        .anyMatch(id -> id == null)
        ) {
            throw new ApiException(
                    EncounterErrorCode
                            .BATTLE_TEAM_INVALID
            );
        }

        Set<UUID> uniqueIds =
                new HashSet<>(monsterIds);

        if (uniqueIds.size() != monsterIds.size()) {
            throw new ApiException(
                    EncounterErrorCode
                            .BATTLE_TEAM_INVALID
            );
        }
    }

    private void validateMonsterResponse(
            List<UUID> requestedIds,
            List<BattleMonsterDto> monsters
    ) {
        if (
                monsters == null
                        || monsters.size()
                        != requestedIds.size()
        ) {
            throw new ApiException(
                    EncounterErrorCode
                            .BATTLE_MONSTERS_UNAVAILABLE
            );
        }

        for (
                int index = 0;
                index < requestedIds.size();
                index++
        ) {
            BattleMonsterDto monster =
                    monsters.get(index);

            if (
                    monster == null
                            || monster.id() == null
                            || !requestedIds
                            .get(index)
                            .equals(monster.id())
                            || monster.name() == null
                            || monster.name().isBlank()
                            || monster.monsterClass() == null
                            || monster.monsterClass().isBlank()
                            || monster.level() < 1
                            || monster.stats() == null
            ) {
                throw new ApiException(
                        EncounterErrorCode
                                .BATTLE_MONSTERS_UNAVAILABLE
                );
            }
        }
    }

    private SimulatorBattleRequest toSimulatorRequest(
            Encounter encounter,
            List<EncounterOpponent> opponents,
            List<BattleMonsterDto> monsters
    ) {
        List<SimulatorBattleRequest.OpponentInput>
                opponentInputs =
                opponents
                        .stream()
                        .map(opponent ->
                                new SimulatorBattleRequest
                                        .OpponentInput(
                                        opponent.getId(),
                                        opponent.getName(),
                                        opponent.getMonsterClass(),
                                        opponent.getLevel()
                                )
                        )
                        .toList();

        List<SimulatorBattleRequest.MonsterInput>
                teamInputs =
                monsters
                        .stream()
                        .map(this::toMonsterInput)
                        .toList();

        return new SimulatorBattleRequest(
                new SimulatorBattleRequest
                        .EncounterInput(
                        encounter.getId(),
                        encounter.getName(),
                        encounter.isFinalBoss(),
                        opponentInputs
                ),
                teamInputs
        );
    }

    private SimulatorBattleRequest.MonsterInput
    toMonsterInput(
            BattleMonsterDto monster
    ) {
        return new SimulatorBattleRequest.MonsterInput(
                monster.id(),
                monster.name(),
                monster.monsterClass(),
                monster.level(),
                new SimulatorBattleRequest.StatsInput(
                        monster.stats().strength(),
                        monster.stats().hp(),
                        monster.stats().accuracy(),
                        monster.stats().agility()
                )
        );
    }

    private void validateSimulatorResponse(
            UUID encounterId,
            SimulatorBattleResponse response
    ) {
        if (
                response == null
                        || response.id() == null
                        || response.encounterId() == null
                        || !encounterId.equals(
                                response.encounterId()
                        )
                        || response.encounterName() == null
                        || response.encounterName().isBlank()
                        || response.playedAt() == null
                        || response.outcome() == null
                        || !Set.of(
                                "victory",
                                "defeat"
                        ).contains(response.outcome())
                        || response.rounds() < 1
                        || response.participants() == null
                        || response.events() == null
        ) {
            throw new ApiException(
                    EncounterErrorCode
                            .SIMULATOR_RESPONSE_INVALID
            );
        }
    }

    private BattleResultDto toResultDto(
            SimulatorBattleResponse response
    ) {
        List<BattleResultDto.ParticipantDto>
                participants =
                response.participants()
                        .stream()
                        .map(participant ->
                                new BattleResultDto
                                        .ParticipantDto(
                                        participant.monsterId(),
                                        participant.name(),
                                        participant.monsterClass(),
                                        participant.level()
                                )
                        )
                        .toList();

        List<BattleResultDto.EventDto> events =
                response.events()
                        .stream()
                        .map(event ->
                                new BattleResultDto.EventDto(
                                        event.id(),
                                        event.round(),
                                        event.side(),
                                        event.kind(),
                                        event.message()
                                )
                        )
                        .toList();

        return new BattleResultDto(
                response.id(),
                response.encounterId(),
                response.encounterName(),
                response.finalBoss(),
                response.playedAt(),
                response.outcome(),
                response.rounds(),
                participants,
                events
        );
    }
}