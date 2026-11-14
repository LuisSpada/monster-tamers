package com.monstertamers.encounter.internal.web.controller;

import com.monstertamers.encounter.internal.application.EncounterService;
import com.monstertamers.encounter.internal.web.dto.response.EncounterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/encounters")
public class EncounterController {

    private final EncounterService encounterService;

    @Operation(
            summary = "List encounters for the authenticated account"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Encounter catalogue returned"
    )
    @ApiResponse(
            responseCode = "401",
            description = "Authentication is required"
    )
    @GetMapping
    public List<EncounterDto> list(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID accountId =
                UUID.fromString(jwt.getSubject());

        return encounterService.findAll(accountId);
    }

    @Operation(
            summary = "Get encounter details for the authenticated account"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Encounter returned"
    )
    @ApiResponse(
            responseCode = "401",
            description = "Authentication is required"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Encounter not found"
    )
    @GetMapping("/{encounterId}")
    public EncounterDto get(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID encounterId
    ) {
        UUID accountId =
                UUID.fromString(jwt.getSubject());

        return encounterService.findById(
                accountId,
                encounterId
        );
    }
}