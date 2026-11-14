package com.monstertamers.encounter.internal.integration.monster;

public record BattleMonsterStatsDto(
        int strength,
        int hp,
        int accuracy,
        int agility
) {
}