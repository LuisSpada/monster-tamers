package com.monstertamers.monster.internal.error;

import com.monstertamers.web.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.http.HttpStatus;

@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public enum MonsterErrorCode implements ErrorCode {

    MONSTER_NOT_FOUND(
        "monster.not_found",
        HttpStatus.NOT_FOUND,
        "Monster not found"
    );

    private final String code;
    private final HttpStatus status;
    private final String messageTemplate;
}
