package com.monstertamers.monster.internal.web.dto.response;

import com.monstertamers.monster.internal.domain.Stat;
import java.time.Instant;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class StatUpgradeDto {
    UUID id;
    Stat stat;
    int newValue;
    int levelAtUpgrade;
    Instant at;
}
