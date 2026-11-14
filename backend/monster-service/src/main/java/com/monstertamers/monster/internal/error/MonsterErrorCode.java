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
    ),
    TEAM_DUPLICATE_MONSTER(
        "team.duplicate_monster",
        HttpStatus.UNPROCESSABLE_CONTENT,
        "A monster can only appear once in a team"
    ),

    TEAM_MONSTER_NOT_OWNED(
        "team.monster_not_owned",
        HttpStatus.UNPROCESSABLE_CONTENT,
        "Team contains a monster that is not in your collection"
    );

    private final String code;
    private final HttpStatus status;
    private final String messageTemplate;
}
