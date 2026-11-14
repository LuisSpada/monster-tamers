package com.monstertamers.monster.internal.web.controller;

import com.monstertamers.monster.internal.application.TeamService;
import com.monstertamers.monster.internal.web.dto.request.SaveTeamRequest;
import com.monstertamers.monster.internal.web.dto.response.TeamDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/monsters/team")
public class TeamController {

    private final TeamService teamService;

    @Operation(summary = "Get the authenticated account's team")
    @ApiResponse(responseCode = "200", description = "Team returned, empty when not set yet")
    @GetMapping
    public TeamDto get(@AuthenticationPrincipal Jwt jwt) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return teamService.find(accountId);
    }

    @Operation(summary = "Replace the authenticated account's team")
    @ApiResponse(responseCode = "200", description = "Team saved")
    @ApiResponse(responseCode = "400", description = "Team is empty or has more than 3 monsters")
    @ApiResponse(responseCode = "422", description = "Team has a duplicate monster or a monster from another account")
    @PutMapping
    public TeamDto save(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SaveTeamRequest request) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return teamService.save(accountId, request);
    }
}
