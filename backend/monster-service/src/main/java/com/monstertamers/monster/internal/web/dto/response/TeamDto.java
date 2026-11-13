package com.monstertamers.monster.internal.web.dto.response;

import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class TeamDto {
    List<UUID> monsterIds;
}
