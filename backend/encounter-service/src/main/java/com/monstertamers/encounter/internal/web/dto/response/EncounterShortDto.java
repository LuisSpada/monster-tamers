package com.monstertamers.encounter.internal.web.dto.response;

import java.util.UUID;

public record EncounterShortDto(
        UUID encounterId,
        String encounterName,
        boolean finalBoss
) {
}
