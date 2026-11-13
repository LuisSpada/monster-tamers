package com.monstertamers.player.internal.integration.client.monster;

import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PutExchange;

@HttpExchange("/internal/accounts")
public interface MonsterClient {

    @PutExchange("/{accountId}/collection")
    void initCollection(@PathVariable UUID accountId);
}
