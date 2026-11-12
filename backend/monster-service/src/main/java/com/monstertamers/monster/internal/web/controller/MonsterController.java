package com.monstertamers.monster.internal.web.controller;

import com.monstertamers.monster.internal.web.dto.response.MonsterDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/monsters")
public class MonsterController {

    @Operation(summary = "List the authenticated account's monsters")
    @ApiResponse(responseCode = "200", description = "Monsters returned")
    @GetMapping
    public List<MonsterDto> list() {
        return List.of();
    }
}
