package com.monstertamers.encounter.internal.integration.monster;

import java.util.List;
import java.util.UUID;

public record BattleMonsterRequest(
        List<UUID> monsterIds
) {
}