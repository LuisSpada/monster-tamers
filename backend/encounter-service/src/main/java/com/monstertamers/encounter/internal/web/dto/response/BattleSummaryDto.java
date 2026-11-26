package com.monstertamers.encounter.internal.web.dto.response;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.monstertamers.encounter.internal.domain.BattleOutcome;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BattleSummaryDto {

    private final UUID id;

    @JsonUnwrapped
    private final EncounterShortDto encounter;

    private final Instant playedAt;
    private final BattleOutcome outcome;
    private final int rounds;
    private final List<String> participantNames;
    private final boolean eggHatched;
}
