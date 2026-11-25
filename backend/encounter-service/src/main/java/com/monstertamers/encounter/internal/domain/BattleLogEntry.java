package com.monstertamers.encounter.internal.domain;

import lombok.Builder;

@Builder
public record BattleLogEntry(
        String id,
        int round,
        String side,
        String kind,
        String message
) {
}
