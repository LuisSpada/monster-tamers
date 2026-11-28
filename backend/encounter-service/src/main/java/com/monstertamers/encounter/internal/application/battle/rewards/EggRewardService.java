package com.monstertamers.encounter.internal.application.battle.rewards;

import com.monstertamers.encounter.internal.domain.BattleOutcome;
import com.monstertamers.encounter.internal.domain.Egg;
import com.monstertamers.encounter.internal.domain.Encounter;
import com.monstertamers.encounter.internal.integration.monster.MonsterClient;
import com.monstertamers.encounter.internal.integration.monster.request.AddMonsterRequest;
import com.monstertamers.encounter.internal.integration.monster.response.AddedMonsterDto;
import com.monstertamers.encounter.internal.mapper.rewards.EggMapper;
import jakarta.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

@Slf4j
@Service
@RequiredArgsConstructor
public class EggRewardService {

    private final MonsterClient monsterClient;
    private final EggMapper eggMapper;

    @Nullable
    public Egg grant(UUID accountId, Encounter encounter, BattleOutcome outcome) {

        if (!isEggDropped(encounter, outcome)) {
            return null;
        }

        String monsterClass = pickMonsterClass(encounter.getRewardClasses());

        AddedMonsterDto monster = addMonster(accountId, monsterClass);

        return eggMapper.toEgg(monster);
    }

    private boolean isEggDropped(Encounter encounter, BattleOutcome outcome) {

        if (outcome != BattleOutcome.victory) {
            return false;
        }

        if (ThreadLocalRandom.current().nextInt(100) >= encounter.getEggChance()) {
            return false;
        }

        if (encounter.getRewardClasses().isEmpty()) {
            log.warn("Encounter {} has egg chance {} but no reward classes, egg not granted",
                    encounter.getId(),
                    encounter.getEggChance()
            );
            return false;
        }

        return true;
    }

    private String pickMonsterClass(List<String> rewardClasses) {

        return rewardClasses.get(ThreadLocalRandom.current().nextInt(rewardClasses.size()));
    }

    @Nullable
    private AddedMonsterDto addMonster(UUID accountId, String monsterClass) {

        try {
            AddedMonsterDto monster = monsterClient.addMonster(accountId, new AddMonsterRequest(monsterClass));
            log.info("Account {} got an egg of class {}, monster {} hatched", accountId, monsterClass, monster.id());
            return monster;
        } catch (RestClientException exception) {
            log.error("Monster Service call failed, egg of class {} not granted to account {}",
                    monsterClass,
                    accountId,
                    exception
            );
            return null;
        }
    }
}
