package com.monstertamers.monster.internal.application;
import com.monstertamers.monster.internal.domain.Monster;
import com.monstertamers.monster.internal.domain.MonsterSpecies;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;
/** Generates a new monster instance from a species with a normally distributed talent. */
@Component
public class MonsterGenerator {
    public static final int MIN_TALENT = 80;
    public static final int MAX_TALENT = 120;
    private static final double TALENT_MEAN = 100.0;
    private static final double TALENT_STD_DEV = 8.0;
    public Monster generateRandom(List<MonsterSpecies> speciesPool) {
        if (speciesPool == null || speciesPool.isEmpty()) {
            throw new IllegalStateException("Cannot generate a monster: species catalogue is empty");
        }
        MonsterSpecies chosen = speciesPool.get(ThreadLocalRandom.current().nextInt(speciesPool.size()));
        return generate(chosen);
    }
    public Monster generate(MonsterSpecies species) {
        int talent = generateTalent();
        Monster monster = new Monster();
        monster.setSpecies(species);
        monster.setLevel(1);
        monster.setXp(0);
        monster.setTalent(talent);
        monster.setPendingPoints(0);
        monster.setStrength(scaleStat(species.getBaseStrength(), talent));
        monster.setHp(scaleStat(species.getBaseHp(), talent));
        monster.setAccuracy(scaleStat(species.getBaseAccuracy(), talent));
        monster.setAgility(scaleStat(species.getBaseAgility(), talent));
        monster.setAvatarVariant(species.getAvatarVariant());
        return monster;
    }
    private int generateTalent() {
        double sample = ThreadLocalRandom.current().nextGaussian(TALENT_MEAN, TALENT_STD_DEV);
        return (int) Math.clamp(Math.round(sample), MIN_TALENT, MAX_TALENT);
    }
    private int scaleStat(int baseStat, int talent) {
        return Math.max(1, (int) Math.round(baseStat * (talent / 100.0)));
    }
}