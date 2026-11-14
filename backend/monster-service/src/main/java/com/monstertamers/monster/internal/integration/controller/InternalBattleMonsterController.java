package com.monstertamers.monster.internal.integration.controller;

import com.monstertamers.monster.internal.application.MonsterService;
import com.monstertamers.monster.internal.integration.dto.BattleMonsterRequest;
import com.monstertamers.monster.internal.web.dto.response.MonsterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(
        "/internal/accounts/{accountId}/battle-monsters"
)
public class InternalBattleMonsterController {

    private final MonsterService monsterService;

    @PostMapping
    @Operation(
            summary = "Load account-owned monsters for an internal battle"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Ordered battle monsters returned"
    )
    @ApiResponse(
            responseCode = "400",
            description = "The requested team is malformed"
    )
    @ApiResponse(
            responseCode = "404",
            description = "One or more monsters were not found for the account"
    )
    public List<MonsterDto> loadBattleMonsters(
            @PathVariable
            UUID accountId,

            @Valid
            @RequestBody
            BattleMonsterRequest request
    ) {
        return monsterService.findOwnedForBattle(
                accountId,
                request.monsterIds()
        );
    }
}