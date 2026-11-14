package com.monstertamers.encounter.internal.integration.monster;

import java.util.UUID;

public record BattleMonsterDto(
        UUID id,
        String name,
        String monsterClass,
        int level,
        BattleMonsterStatsDto stats
) {
}