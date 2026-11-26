package com.monstertamers.encounter.internal.web.controller;

import com.monstertamers.encounter.internal.application.BattleHistoryService;
import com.monstertamers.encounter.internal.application.BattleOrchestrationService;
import com.monstertamers.encounter.internal.web.dto.request.StartBattleRequest;
import com.monstertamers.encounter.internal.web.dto.response.BattleResultDto;
import com.monstertamers.encounter.internal.web.dto.response.BattleSummaryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/encounters")
public class BattleController {

    private final BattleOrchestrationService
            battleOrchestrationService;
    private final BattleHistoryService battleHistoryService;

    @PostMapping("/{encounterId}/battles")
    @Operation(
            summary = "Start an automated encounter battle"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Battle result returned"
    )
    @ApiResponse(
            responseCode = "401",
            description = "Authentication is required"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Encounter not found"
    )
    @ApiResponse(
            responseCode = "409",
            description = "Encounter is locked"
    )
    @ApiResponse(
            responseCode = "422",
            description = "The selected team is invalid"
    )
    @ApiResponse(
            responseCode = "502",
            description = "An internal service returned an invalid response"
    )
    @ApiResponse(
            responseCode = "503",
            description = "Simulator Service is unavailable"
    )
    public BattleResultDto startBattle(
            @AuthenticationPrincipal
            Jwt jwt,

            @PathVariable
            UUID encounterId,

            @Valid
            @RequestBody
            StartBattleRequest request
    ) {
        UUID accountId =
                UUID.fromString(jwt.getSubject());

        return battleOrchestrationService
                .startBattle(
                        accountId,
                        encounterId,
                        request
                );
    }

    @GetMapping("/battles")
    @Operation(summary = "List battles of the authenticated account, newest first")
    @ApiResponse(responseCode = "200", description = "Battle history returned")
    @ApiResponse(responseCode = "401", description = "Authentication is required")
    public List<BattleSummaryDto> listBattles(@AuthenticationPrincipal Jwt jwt) {

        UUID accountId = UUID.fromString(jwt.getSubject());
        return battleHistoryService.findAll(accountId);
    }
}
