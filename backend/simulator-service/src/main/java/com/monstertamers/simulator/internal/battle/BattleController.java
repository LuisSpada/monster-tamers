package com.monstertamers.simulator.internal.battle;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/battles")
public class BattleController {

    private final BattleService battleService;

    @PostMapping
    @Operation(
            summary = "Simulate a deterministic internal battle"
    )
    public BattleResponse simulate(
            @Valid
            @RequestBody
            BattleRequest request
    ) {
        return battleService.simulate(request);
    }
}