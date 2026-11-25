package com.monstertamers.encounter.internal.domain;

import java.util.UUID;
import lombok.Builder;

@Builder
public record BattleParticipant(
        UUID monsterId,
        String name,
        String monsterClass,
        int level
) {
}
