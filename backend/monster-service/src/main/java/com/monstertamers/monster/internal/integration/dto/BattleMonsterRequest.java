package com.monstertamers.monster.internal.integration.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record BattleMonsterRequest(
        @NotEmpty
        @Size(min = 1, max = 3)
        List<@NotNull UUID> monsterIds
) {
}