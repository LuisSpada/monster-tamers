package com.monstertamers.encounter.internal.application.battle.rewards;

import com.monstertamers.encounter.internal.domain.BattleOutcome;
import com.monstertamers.encounter.internal.domain.BattleRewards;
import com.monstertamers.encounter.internal.domain.Encounter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BattleRewardService {

    private final EggRewardService eggRewardService;

    public BattleRewards grant(UUID accountId, Encounter encounter, BattleOutcome outcome) {

        BattleRewards rewards = new BattleRewards();
        rewards.setEgg(eggRewardService.grant(accountId, encounter, outcome));

        return rewards;
    }
}
