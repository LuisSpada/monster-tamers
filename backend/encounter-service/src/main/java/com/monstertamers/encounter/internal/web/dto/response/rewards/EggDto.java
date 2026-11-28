package com.monstertamers.encounter.internal.web.dto.response.rewards;

import java.util.UUID;
import lombok.Builder;

@Builder
public record EggDto(UUID id, String name, String monsterClass, int talent, int avatarVariant) {
}
