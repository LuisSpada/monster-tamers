package com.monstertamers.monster.internal.web.dto.response;
import com.monstertamers.monster.internal.domain.MonsterClass;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;
@Value
@Builder
public class MonsterSpeciesDto {
    UUID id;
    String name;
    MonsterClass monsterClass;
    int avatarVariant;
    StatsDto baseStats;
    SkillDto skill;
    TraitDto trait;
}