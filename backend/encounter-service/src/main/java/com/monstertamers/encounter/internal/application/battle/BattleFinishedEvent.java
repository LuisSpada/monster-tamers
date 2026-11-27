package com.monstertamers.encounter.internal.application.battle;

import com.monstertamers.encounter.internal.domain.BattleOutcome;
import java.util.UUID;

public record BattleFinishedEvent(UUID accountId, UUID encounterId, BattleOutcome outcome) {

    public boolean isVictory() {

        return outcome == BattleOutcome.victory;
    }
}
