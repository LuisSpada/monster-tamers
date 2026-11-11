package com.monstertamers.player.internal.web.dto.response;

import java.util.UUID;

public record PlayerProfileResponse(
        UUID id,
        String nickname
) {
}
