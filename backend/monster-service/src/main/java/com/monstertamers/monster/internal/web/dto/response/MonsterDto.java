package com.monstertamers.monster.internal.web.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.monstertamers.monster.internal.domain.MonsterClass;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class MonsterDto {
    UUID id;
    String name;
    MonsterClass monsterClass;
    int avatarVariant;
    Instant obtainedAt;

    int talent;
    SkillDto skill;
    TraitDto trait;

    @JsonUnwrapped
    ProgressDto progress;
    StatsDto stats;
    List<StatUpgradeDto> upgrades;
}
