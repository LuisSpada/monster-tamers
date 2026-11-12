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
}
