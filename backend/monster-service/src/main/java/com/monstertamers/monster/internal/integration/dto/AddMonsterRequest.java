package com.monstertamers.monster.internal.integration.dto;

import com.monstertamers.monster.internal.domain.MonsterClass;
import jakarta.validation.constraints.NotNull;

public record AddMonsterRequest(@NotNull MonsterClass monsterClass) {
}
