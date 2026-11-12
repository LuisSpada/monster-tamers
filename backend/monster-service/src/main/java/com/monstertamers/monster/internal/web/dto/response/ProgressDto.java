package com.monstertamers.monster.internal.web.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ProgressDto {
    int level;
    int xp;
    Integer xpToNext;
    int pendingPoints;

    /** Placeholder until progression is modelled. */
    public static ProgressDto stub() {
        return ProgressDto.builder()
                .level(1)
                .xp(0)
                .xpToNext(100)
                .pendingPoints(0)
                .build();
    }
}
