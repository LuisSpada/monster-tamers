package com.monstertamers.monster.internal.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MonsterClass {
    FIGHTER("Fighter"),
    GUARDIAN("Guardian"),
    SHAMAN("Shaman"),
    TRICKSTER("Trickster");

    @JsonValue
    private final String id;
}
