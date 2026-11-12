package com.monstertamers.monster.internal.web.mapper;

import com.monstertamers.monster.internal.domain.Monster;
import com.monstertamers.monster.internal.web.dto.response.MonsterDto;
import com.monstertamers.monster.internal.web.dto.response.ProgressDto;
import com.monstertamers.monster.internal.web.dto.response.SkillDto;
import com.monstertamers.monster.internal.web.dto.response.StatsDto;
import com.monstertamers.monster.internal.web.dto.response.TraitDto;
import java.util.List;
import org.springframework.stereotype.Component;

/** Fields not modelled yet are filled with placeholders until their features land. */
@Component
public class MonsterMapper {

    private static final int DEFAULT_TALENT = 100;
    private static final int DEFAULT_AVATAR_VARIANT = 0;

    public MonsterDto toDto(Monster monster) {
        return MonsterDto.builder()
                .id(monster.getId())
                .name(monster.getName())
                .monsterClass(monster.getMonsterClass())
                .avatarVariant(DEFAULT_AVATAR_VARIANT)
                .obtainedAt(monster.getObtainedAt())
                .talent(DEFAULT_TALENT)
                .skill(SkillDto.stub())
                .trait(TraitDto.stub())
                .progress(ProgressDto.stub())
                .stats(StatsDto.stub())
                .upgrades(List.of())
                .build();
    }
}
