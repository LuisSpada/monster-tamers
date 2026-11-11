package com.monstertamers.player.internal.web.controller;

import com.monstertamers.player.internal.application.PlayerService;
import com.monstertamers.player.internal.domain.Player;
import com.monstertamers.player.internal.web.dto.response.PlayerProfileResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    @Operation(summary = "Get the authenticated player's profile")
    @ApiResponse(responseCode = "200", description = "Profile returned")
    @GetMapping("/me")
    public PlayerProfileResponse me(@AuthenticationPrincipal Jwt jwt) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        Player player = playerService.createIfMissing(accountId);
        return new PlayerProfileResponse(player.getId(), player.getNickname());
    }
}
