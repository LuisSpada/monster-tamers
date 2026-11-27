package com.monstertamers.encounter.internal.error;

import com.monstertamers.web.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum EncounterErrorCode implements ErrorCode {

    ENCOUNTER_NOT_FOUND(
            "encounter.not_found",
            HttpStatus.NOT_FOUND,
            "Encounter not found"
    ),

    ENCOUNTER_LOCKED(
            "encounter.locked",
            HttpStatus.CONFLICT,
            "Encounter is locked"
    ),

    BATTLE_NOT_FOUND(
            "battle.not_found",
            HttpStatus.NOT_FOUND,
            "Battle not found"
    ),

    BATTLE_TEAM_INVALID(
            "battle.team_invalid",
            HttpStatus.UNPROCESSABLE_CONTENT,
            "Battle team must contain one to three unique monsters"
    ),

    BATTLE_MONSTERS_UNAVAILABLE(
            "battle.monsters_unavailable",
            HttpStatus.BAD_GATEWAY,
            "Monster Service could not provide the battle team"
    ),

    SIMULATOR_UNAVAILABLE(
            "battle.simulator_unavailable",
            HttpStatus.SERVICE_UNAVAILABLE,
            "Simulator Service is unavailable"
    ),

    SIMULATOR_RESPONSE_INVALID(
            "battle.simulator_response_invalid",
            HttpStatus.BAD_GATEWAY,
            "Simulator Service returned an invalid battle result"
    );

    private final String code;
    private final HttpStatus status;
    private final String messageTemplate;
}