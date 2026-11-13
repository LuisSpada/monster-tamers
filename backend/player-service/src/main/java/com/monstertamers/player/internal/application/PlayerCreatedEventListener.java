package com.monstertamers.player.internal.application;

import com.monstertamers.player.api.PlayerCreatedEvent;
import com.monstertamers.player.internal.integration.client.monster.MonsterClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlayerCreatedEventListener {

    private final MonsterClient monsterClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPlayerCreated(PlayerCreatedEvent event) {
        try {
            monsterClient.initCollection(event.accountId());
        } catch (RestClientException e) {
            log.error("Monster collection not initialized for account {}", event.accountId(), e);
        }
    }
}
