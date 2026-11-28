package com.monstertamers.encounter.internal.domain;

import java.util.UUID;
import lombok.Builder;

@Builder
public record Egg(UUID monsterId, String name, String monsterClass, int talent, int avatarVariant) {
}
