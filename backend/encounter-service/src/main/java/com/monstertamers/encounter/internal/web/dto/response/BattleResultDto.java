package com.monstertamers.encounter.internal.web.dto.response;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BattleResultDto(
        UUID id,
        UUID encounterId,
        String encounterName,
        boolean finalBoss,
        Instant playedAt,
        String outcome,
        int rounds,
        List<ParticipantDto> participants,
        List<EventDto> events
) {

    public record ParticipantDto(
            UUID monsterId,
            String name,
            String monsterClass,
            int level
    ) {
    }

    public record EventDto(
            String id,
            int round,
            String side,
            String kind,
            String message
    ) {
    }
}