package com.monstertamers.monster.internal.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Stat {
    STRENGTH("strength"),
    HP("hp"),
    ACCURACY("accuracy"),
    AGILITY("agility");

    @JsonValue
    private final String id;
}
