package com.monstertamers.encounter.internal.integration.monster.response;

import java.util.UUID;

public record AddedMonsterDto(UUID id, String name, String monsterClass, int talent, int avatarVariant) {
}
