package com.monstertamers.encounter.internal.web.dto.response;

import java.util.UUID;

public record OpponentDto(
        UUID id,
        String name,
        String monsterClass,
        int level,
        int avatarVariant
) {
}