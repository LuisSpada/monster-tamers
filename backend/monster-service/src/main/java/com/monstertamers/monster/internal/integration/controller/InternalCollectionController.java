package com.monstertamers.monster.internal.integration.controller;

import com.monstertamers.monster.internal.application.MonsterService;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/accounts/{accountId}/collection")
public class InternalCollectionController {

    private final MonsterService monsterService;

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Initialize player's monster collection")
    public void init(@PathVariable UUID accountId) {
        monsterService.initCollection(accountId);
    }
}
