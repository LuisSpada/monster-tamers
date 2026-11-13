package com.monstertamers.monster.internal.application;

import com.monstertamers.monster.internal.domain.Monster;
import com.monstertamers.monster.internal.domain.MonsterClass;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Component;

/** Generates a new monster of a random class. The caller sets the owner and saves it. */
@Component
public class MonsterGenerator {

    // TODO GH-95: replace the class name with a generated monster name
    public Monster generate() {

        MonsterClass monsterClass = randomClass();
        Monster monster = new Monster();
        monster.setName(monsterClass.getId());
        monster.setMonsterClass(monsterClass);
        return monster;
    }

    private MonsterClass randomClass() {
        MonsterClass[] classes = MonsterClass.values();
        return classes[ThreadLocalRandom.current().nextInt(classes.length)];
    }
}
