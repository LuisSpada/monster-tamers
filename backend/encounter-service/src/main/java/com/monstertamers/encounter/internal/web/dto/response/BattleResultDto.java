package com.monstertamers.encounter.internal.web.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.monstertamers.encounter.internal.domain.BattleOutcome;
import com.monstertamers.encounter.internal.web.dto.response.rewards.BattleRewardsDto;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BattleResultDto(
        UUID id,
        @JsonUnwrapped EncounterShortDto encounter,
        Instant playedAt,
        BattleOutcome outcome,
        int rounds,
        List<ParticipantDto> participants,
        BattleRewardsDto rewards,
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
