package com.monstertamers.encounter.internal.web.dto.response;

import com.monstertamers.encounter.internal.domain.EncounterStatus;
import java.util.List;
import java.util.UUID;

public record EncounterDto(
        UUID id,
        int order,
        String name,
        String description,
        int eggChance,
        List<String> rewardClasses,
        boolean isFinalBoss,
        EncounterStatus status,
        List<OpponentDto> opponents
) {
}
