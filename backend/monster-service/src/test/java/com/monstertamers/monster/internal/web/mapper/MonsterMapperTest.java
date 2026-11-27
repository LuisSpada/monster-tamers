package com.monstertamers.monster.internal.web.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.monstertamers.monster.internal.domain.Monster;
import com.monstertamers.monster.internal.domain.MonsterClass;
import com.monstertamers.monster.internal.domain.MonsterSpecies;
import com.monstertamers.monster.internal.web.dto.response.MonsterDto;
import com.monstertamers.monster.internal.web.dto.response.MonsterSpeciesDto;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MonsterMapperTest {

    private final MonsterMapper mapper = new MonsterMapper();

    @Test
    void toDtoMapsOwnedMonsterAndFallsBackToSpeciesName() {
        MonsterSpecies species = sampleSpecies();
        Monster monster = sampleMonster(species, 3, null);

        MonsterDto dto = mapper.toDto(monster);

        assertThat(dto.getId()).isEqualTo(monster.getId());
        assertThat(dto.getName()).isEqualTo("Fierce Fox");
        assertThat(dto.getMonsterClass()).isEqualTo(MonsterClass.FIGHTER);
        assertThat(dto.getTalent()).isEqualTo(112);
        assertThat(dto.getProgress().getLevel()).isEqualTo(3);
        assertThat(dto.getProgress().getXpToNext()).isEqualTo(300);
        assertThat(dto.getStats().getStrength()).isEqualTo(17);
        assertThat(dto.getSkill().getName()).isEqualTo("Vampiric Strike");
        assertThat(dto.getTrait().getName()).isEqualTo("Hot-headed");
    }

    @Test
    void toDtoUsesNicknameAndNullXpToNextAtMaxLevel() {
        MonsterSpecies species = sampleSpecies();
        Monster monster = sampleMonster(species, 10, "Blaze");

        MonsterDto dto = mapper.toDto(monster);

        assertThat(dto.getName()).isEqualTo("Blaze");
        assertThat(dto.getProgress().getXpToNext()).isNull();
    }

    @Test
    void toSpeciesDtoMapsCatalogueFields() {
        MonsterSpecies species = sampleSpecies();

        MonsterSpeciesDto dto = mapper.toSpeciesDto(species);

        assertThat(dto.getId()).isEqualTo(species.getId());
        assertThat(dto.getName()).isEqualTo("Fierce Fox");
        assertThat(dto.getMonsterClass()).isEqualTo(MonsterClass.FIGHTER);
        assertThat(dto.getBaseStats().getHp()).isEqualTo(100);
        assertThat(dto.getSkill().getName()).isEqualTo("Vampiric Strike");
        assertThat(dto.getTrait().getName()).isEqualTo("Hot-headed");
    }

    private MonsterSpecies sampleSpecies() {
        MonsterSpecies species = new MonsterSpecies();
        species.setId(UUID.fromString("20000000-0000-0000-0000-000000000001"));
        species.setName("Fierce Fox");
        species.setMonsterClass(MonsterClass.FIGHTER);
        species.setBaseStrength(15);
        species.setBaseHp(100);
        species.setBaseAccuracy(62);
        species.setBaseAgility(42);
        species.setSkillName("Vampiric Strike");
        species.setSkillDescription("A strong attack that restores a little health.");
        species.setTraitName("Hot-headed");
        species.setTraitDescription("Hits harder while at full health.");
        species.setAvatarVariant(0);
        return species;
    }

    private Monster sampleMonster(MonsterSpecies species, int level, String nickname) {
        Monster monster = new Monster();
        monster.setId(UUID.randomUUID());
        monster.setAccountId(UUID.randomUUID());
        monster.setSpecies(species);
        monster.setNickname(nickname);
        monster.setLevel(level);
        monster.setXp(40);
        monster.setTalent(112);
        monster.setPendingPoints(1);
        monster.setStrength(17);
        monster.setHp(112);
        monster.setAccuracy(69);
        monster.setAgility(47);
        monster.setAvatarVariant(0);
        monster.setObtainedAt(Instant.parse("2026-11-27T12:00:00Z"));
        return monster;
    }
}