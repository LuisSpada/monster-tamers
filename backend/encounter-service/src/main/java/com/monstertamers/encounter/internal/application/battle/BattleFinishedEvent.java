package com.monstertamers.encounter.internal.application.battle;

import java.util.UUID;

public record BattleFinishedEvent(UUID accountId, UUID encounterId, String outcome) {

    public boolean isVictory() {

        return "victory".equals(outcome);
    }
}
