package com.monstertamers.encounter.internal.integration.simulator;

import java.util.List;
import java.util.UUID;

public record SimulatorBattleRequest(
        EncounterInput encounter,
        List<MonsterInput> team
) {

    public record EncounterInput(
            UUID id,
            String name,
            boolean finalBoss,
            List<OpponentInput> opponents
    ) {
    }

    public record OpponentInput(
            UUID id,
            String name,
            String monsterClass,
            int level
    ) {
    }

    public record MonsterInput(
            UUID id,
            String name,
            String monsterClass,
            int level,
            StatsInput stats
    ) {
    }

    public record StatsInput(
            int strength,
            int hp,
            int accuracy,
            int agility
    ) {
    }
}