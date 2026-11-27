package com.monstertamers.monster.internal.web.controller;

import com.monstertamers.monster.internal.application.MonsterSpeciesService;
import com.monstertamers.monster.internal.web.dto.response.MonsterSpeciesDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/monsters/species")
public class MonsterSpeciesController {

    private final MonsterSpeciesService monsterSpeciesService;

    @Operation(summary = "List all monster species in the game")
    @ApiResponse(responseCode = "200", description = "Monster species catalogue returned")
    @GetMapping
    public List<MonsterSpeciesDto> list() {
        return monsterSpeciesService.findAll();
    }

    @Operation(summary = "Get one monster species by ID")
    @ApiResponse(responseCode = "200", description = "Monster species returned")
    @ApiResponse(responseCode = "404", description = "Monster species not found")
    @GetMapping("/{speciesId}")
    public MonsterSpeciesDto get(@PathVariable UUID speciesId) {
        return monsterSpeciesService.findById(speciesId);
    }
}