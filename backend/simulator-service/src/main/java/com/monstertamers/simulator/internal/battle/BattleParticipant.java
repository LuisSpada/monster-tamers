package com.monstertamers.simulator.internal.battle;

import java.util.UUID;

public record BattleParticipant(
        UUID monsterId,
        String name,
        String monsterClass,
        int level
) {
}