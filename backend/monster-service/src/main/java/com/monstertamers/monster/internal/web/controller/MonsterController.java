package com.monstertamers.monster.internal.web.controller;

import com.monstertamers.monster.internal.application.MonsterService;
import com.monstertamers.monster.internal.web.dto.response.MonsterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/monsters")
public class MonsterController {

    private final MonsterService monsterService;

    @Operation(summary = "List the authenticated account's monsters")
    @ApiResponse(responseCode = "200", description = "Monsters returned")
    @GetMapping
    public List<MonsterDto> list(@AuthenticationPrincipal Jwt jwt) {
        UUID accountId = UUID.fromString(jwt.getSubject());
        return monsterService.listForAccount(accountId);
    }
}
