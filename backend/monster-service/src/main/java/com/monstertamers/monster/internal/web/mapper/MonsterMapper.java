package com.monstertamers.monster.internal.web.mapper;
import com.monstertamers.monster.internal.domain.Monster;
import com.monstertamers.monster.internal.domain.MonsterSpecies;
import com.monstertamers.monster.internal.web.dto.response.MonsterDto;
import com.monstertamers.monster.internal.web.dto.response.MonsterSpeciesDto;
import com.monstertamers.monster.internal.web.dto.response.ProgressDto;
import com.monstertamers.monster.internal.web.dto.response.SkillDto;
import com.monstertamers.monster.internal.web.dto.response.StatsDto;
import com.monstertamers.monster.internal.web.dto.response.TraitDto;
import java.util.List;
import org.springframework.stereotype.Component;
@Component
public class MonsterMapper {
    private static final int MAX_LEVEL = 10;
    public MonsterDto toDto(Monster monster) {
        MonsterSpecies species = monster.getSpecies();
        Integer xpToNext = monster.getLevel() >= MAX_LEVEL ? null : monster.getLevel() * 100;
        return MonsterDto.builder()
                .id(monster.getId())
                .name(monster.getDisplayName())
                .monsterClass(species.getMonsterClass())
                .avatarVariant(monster.getAvatarVariant())
                .obtainedAt(monster.getObtainedAt())
                .talent(monster.getTalent())
                .skill(SkillDto.builder()
                        .name(species.getSkillName())
                        .description(species.getSkillDescription())
                        .build())
                .trait(TraitDto.builder()
                        .name(species.getTraitName())
                        .description(species.getTraitDescription())
                        .build())
                .progress(ProgressDto.builder()
                        .level(monster.getLevel())
                        .xp(monster.getXp())
                        .xpToNext(xpToNext)
                        .pendingPoints(monster.getPendingPoints())
                        .build())
                .stats(StatsDto.builder()
                        .strength(monster.getStrength())
                        .hp(monster.getHp())
                        .accuracy(monster.getAccuracy())
                        .agility(monster.getAgility())
                        .build())
                .upgrades(List.of())
                .build();
    }
    public MonsterSpeciesDto toSpeciesDto(MonsterSpecies species) {
        return MonsterSpeciesDto.builder()
                .id(species.getId())
                .name(species.getName())
                .monsterClass(species.getMonsterClass())
                .avatarVariant(species.getAvatarVariant())
                .baseStats(StatsDto.builder()
                        .strength(species.getBaseStrength())
                        .hp(species.getBaseHp())
                        .accuracy(species.getBaseAccuracy())
                        .agility(species.getBaseAgility())
                        .build())
                .skill(SkillDto.builder()
                        .name(species.getSkillName())
                        .description(species.getSkillDescription())
                        .build())
                .trait(TraitDto.builder()
                        .name(species.getTraitName())
                        .description(species.getTraitDescription())
                        .build())
                .build();
    }
}