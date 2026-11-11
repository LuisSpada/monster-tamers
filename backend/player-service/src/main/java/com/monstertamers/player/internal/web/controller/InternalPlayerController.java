package com.monstertamers.player.internal.web.controller;

import com.monstertamers.player.internal.application.PlayerService;
import com.monstertamers.player.internal.web.dto.request.CreatePlayerRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/players")
public class InternalPlayerController {

    private final PlayerService playerService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CreatePlayerRequest request) {
        playerService.createIfMissing(request.accountId());
        return ResponseEntity.ok().build();
    }
}
