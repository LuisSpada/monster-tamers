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
    );

    private final String code;
    private final HttpStatus status;
    private final String messageTemplate;
}