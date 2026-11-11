package com.monstertamers.auth.internal.client;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class RestPlayerClient implements PlayerClient {

    private final RestClient playerRestClient;

    @Override
    public void createPlayer(UUID accountId) {
        playerRestClient.post()
                .uri("/internal/players")
                .body(new CreatePlayerRequest(accountId))
                .retrieve()
                .toBodilessEntity();
    }

    private record CreatePlayerRequest(UUID accountId) {
    }
}
