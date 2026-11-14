package com.monstertamers.simulator.internal.battle;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record BattleResponse(
        UUID id,
        UUID encounterId,
        String encounterName,
        boolean finalBoss,
        Instant playedAt,
        String outcome,
        int rounds,
        List<BattleParticipant> participants,
        List<BattleEvent> events
) {
}