package com.monstertamers.monster.internal.web.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record SaveTeamRequest(

        @NotNull
        @Size(min = 1, max = 3)
        List<@NotNull UUID> monsterIds
) {
}
