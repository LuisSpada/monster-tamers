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
}
