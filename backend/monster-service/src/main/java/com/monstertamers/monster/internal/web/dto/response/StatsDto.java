package com.monstertamers.monster.internal.web.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StatsDto {
    int strength;
    int hp;
    int accuracy;
    int agility;

    /** Placeholder until stats are modelled. */
    public static StatsDto stub() {
        return StatsDto.builder()
                .strength(10)
                .hp(100)
                .accuracy(60)
                .agility(40)
                .build();
    }
}
