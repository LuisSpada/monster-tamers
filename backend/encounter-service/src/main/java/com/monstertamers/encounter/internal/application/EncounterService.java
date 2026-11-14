package com.monstertamers.encounter.internal.application;

import com.monstertamers.encounter.internal.domain.AccountEncounterProgress;
import com.monstertamers.encounter.internal.domain.Encounter;
import com.monstertamers.encounter.internal.domain.EncounterOpponent;
import com.monstertamers.encounter.internal.domain.EncounterStatus;
import com.monstertamers.encounter.internal.error.EncounterErrorCode;
import com.monstertamers.encounter.internal.persistence.AccountEncounterProgressRepository;
import com.monstertamers.encounter.internal.persistence.EncounterOpponentRepository;
import com.monstertamers.encounter.internal.persistence.EncounterRepository;
import com.monstertamers.encounter.internal.web.dto.response.EncounterDto;
import com.monstertamers.encounter.internal.web.dto.response.OpponentDto;
import com.monstertamers.web.error.ApiException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EncounterService {

    private final EncounterRepository encounterRepository;
    private final EncounterOpponentRepository
            opponentRepository;
    private final AccountEncounterProgressRepository
            progressRepository;

    @Transactional(readOnly = true)
    public List<EncounterDto> findAll(
            UUID accountId
    ) {
        List<Encounter> encounters =
                encounterRepository
                        .findAllByOrderByOrderAsc();

        Map<UUID, EncounterStatus> statuses =
                statusesFor(accountId);

        List<UUID> encounterIds = encounters
                .stream()
                .map(Encounter::getId)
                .toList();

        Map<UUID, List<EncounterOpponent>>
                opponentsByEncounter =
                opponentsByEncounter(encounterIds);

        return encounters
                .stream()
                .map(encounter -> toDto(
                        encounter,
                        resolveStatus(
                                encounter,
                                statuses
                        ),
                        opponentsByEncounter.getOrDefault(
                                encounter.getId(),
                                List.of()
                        )
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public EncounterDto findById(
            UUID accountId,
            UUID encounterId
    ) {
        Encounter encounter = encounterRepository
                .findById(encounterId)
                .orElseThrow(() ->
                        new ApiException(
                                EncounterErrorCode
                                        .ENCOUNTER_NOT_FOUND
                        )
                );

        EncounterStatus status = progressRepository
                .findByAccountIdAndEncounterId(
                        accountId,
                        encounterId
                )
                .map(
                        AccountEncounterProgress
                                ::getStatus
                )
                .orElseGet(() ->
                        defaultStatus(encounter)
                );

        List<EncounterOpponent> opponents =
                opponentRepository
                        .findAllByEncounterIdOrderByIdAsc(
                                encounterId
                        );

        return toDto(
                encounter,
                status,
                opponents
        );
    }

    private Map<UUID, EncounterStatus> statusesFor(
            UUID accountId
    ) {
        Map<UUID, EncounterStatus> statuses =
                new HashMap<>();

        for (
                AccountEncounterProgress progress
                : progressRepository
                        .findAllByAccountId(accountId)
        ) {
            statuses.put(
                    progress.getEncounterId(),
                    progress.getStatus()
            );
        }

        return statuses;
    }

    private Map<UUID, List<EncounterOpponent>>
    opponentsByEncounter(
            List<UUID> encounterIds
    ) {
        if (encounterIds.isEmpty()) {
            return Map.of();
        }

        Map<UUID, List<EncounterOpponent>> result =
                new HashMap<>();

        List<EncounterOpponent> opponents =
                opponentRepository
                        .findAllByEncounterIdInOrderByEncounterIdAscIdAsc(
                                encounterIds
                        );

        for (EncounterOpponent opponent : opponents) {
            result.computeIfAbsent(
                    opponent.getEncounterId(),
                    ignored -> new ArrayList<>()
            ).add(opponent);
        }

        return result;
    }

    private EncounterStatus resolveStatus(
            Encounter encounter,
            Map<UUID, EncounterStatus> statuses
    ) {
        return statuses.getOrDefault(
                encounter.getId(),
                defaultStatus(encounter)
        );
    }

    private EncounterStatus defaultStatus(
            Encounter encounter
    ) {
        if (encounter.getOrder() == 1) {
            return EncounterStatus.available;
        }

        return EncounterStatus.locked;
    }

    private EncounterDto toDto(
            Encounter encounter,
            EncounterStatus status,
            List<EncounterOpponent> opponents
    ) {
        return new EncounterDto(
                encounter.getId(),
                encounter.getOrder(),
                encounter.getName(),
                encounter.getDescription(),
                encounter.getEggChance(),
                List.copyOf(
                        encounter.getRewardClasses()
                ),
                encounter.isFinalBoss(),
                status,
                opponents
                        .stream()
                        .map(this::toOpponentDto)
                        .toList()
        );
    }

    private OpponentDto toOpponentDto(
            EncounterOpponent opponent
    ) {
        return new OpponentDto(
                opponent.getId(),
                opponent.getName(),
                opponent.getMonsterClass(),
                opponent.getLevel(),
                opponent.getAvatarVariant()
        );
    }
}