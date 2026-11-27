package com.monstertamers.monster.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.monstertamers.monster.internal.domain.Monster;
import com.monstertamers.monster.internal.domain.MonsterClass;
import com.monstertamers.monster.internal.domain.MonsterSpecies;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class MonsterGeneratorTest {

    private final MonsterGenerator generator = new MonsterGenerator();

    @Test
    void generateSetsInitialProgressTalentBoundsAndScaledStats() {
        MonsterSpecies species = foxSpecies();

        for (int i = 0; i < 100; i++) {
            Monster generated = generator.generate(species);

            assertThat(generated.getSpecies()).isSameAs(species);
            assertThat(generated.getLevel()).isEqualTo(1);
            assertThat(generated.getXp()).isZero();
            assertThat(generated.getPendingPoints()).isZero();
            assertThat(generated.getAvatarVariant()).isEqualTo(species.getAvatarVariant());
            assertThat(generated.getTalent())
                    .isBetween(MonsterGenerator.MIN_TALENT, MonsterGenerator.MAX_TALENT);

            double factor = generated.getTalent() / 100.0;
            assertThat(generated.getStrength()).isEqualTo((int) Math.max(1, Math.round(species.getBaseStrength() * factor)));
            assertThat(generated.getHp()).isEqualTo((int) Math.max(1, Math.round(species.getBaseHp() * factor)));
            assertThat(generated.getAccuracy()).isEqualTo((int) Math.max(1, Math.round(species.getBaseAccuracy() * factor)));
            assertThat(generated.getAgility()).isEqualTo((int) Math.max(1, Math.round(species.getBaseAgility() * factor)));
        }
    }

    @Test
    void generateRandomThrowsWhenSpeciesPoolIsEmpty() {
        assertThatThrownBy(() -> generator.generateRandom(List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    private MonsterSpecies foxSpecies() {
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
}