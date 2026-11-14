package com.monstertamers.simulator.internal.battle;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record BattleRequest(
        @NotNull
        @Valid
        EncounterInput encounter,

        @NotEmpty
        @Size(min = 1, max = 3)
        List<@Valid MonsterInput> team
) {

    public record EncounterInput(
            @NotNull
            UUID id,

            @NotBlank
            String name,

            boolean finalBoss,

            @NotEmpty
            List<@Valid OpponentInput> opponents
    ) {
    }

    public record MonsterInput(
            @NotNull
            UUID id,

            @NotBlank
            String name,

            @NotBlank
            String monsterClass,

            @Min(1)
            int level,

            @NotNull
            @Valid
            StatsInput stats
    ) {
    }

    public record OpponentInput(
            @NotNull
            UUID id,

            @NotBlank
            String name,

            @NotBlank
            String monsterClass,

            @Min(1)
            int level
    ) {
    }

    public record StatsInput(
            @Min(1)
            int strength,

            @Min(1)
            int hp,

            @Min(1)
            @Max(100)
            int accuracy,

            @Min(1)
            int agility
    ) {
    }
}