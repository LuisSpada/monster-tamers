package com.monstertamers.player.internal.web.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreatePlayerRequest(

        @NotNull
        UUID accountId
){
}
