package com.monstertamers.simulator.internal.battle;

public record BattleEvent(
        String id,
        int round,
        String side,
        String kind,
        String message
) {
}