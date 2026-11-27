package com.monstertamers.encounter.internal.integration.simulator;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.monstertamers.encounter.internal.domain.BattleOutcome;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SimulatorBattleResponse(
        UUID id,
        UUID encounterId,
        String encounterName,
        boolean finalBoss,
        Instant playedAt,
        @JsonFormat(with = JsonFormat.Feature.READ_UNKNOWN_ENUM_VALUES_AS_NULL)
        BattleOutcome outcome,
        int rounds,
        List<Participant> participants,
        List<Event> events
) {

    public record Participant(
            UUID monsterId,
            String name,
            String monsterClass,
            int level
    ) {
    }

    public record Event(
            String id,
            int round,
            String side,
            String kind,
            String message
    ) {
    }
}