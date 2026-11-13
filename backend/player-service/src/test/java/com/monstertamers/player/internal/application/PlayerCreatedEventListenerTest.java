package com.monstertamers.player.internal.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import com.monstertamers.player.api.PlayerCreatedEvent;
import com.monstertamers.player.internal.integration.client.monster.MonsterClient;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;

@ExtendWith(MockitoExtension.class)
class PlayerCreatedEventListenerTest {

    @Mock
    private MonsterClient monsterClient;

    @InjectMocks
    private PlayerCreatedEventListener listener;

    @Test
    @DisplayName("onPlayerCreated initializes the monster collection of the account")
    void onPlayerCreated_succeeds() {

        // given
        UUID accountId = UUID.randomUUID();

        // when
        listener.onPlayerCreated(new PlayerCreatedEvent(accountId));

        // then
        verify(monsterClient).initCollection(accountId);
    }

    @Test
    @DisplayName("onPlayerCreated does not fail when monster-service is unavailable")
    void onPlayerCreated_clientFails_succeeds() {

        // given
        UUID accountId = UUID.randomUUID();
        doThrow(new RestClientException("monster-service is down"))
                .when(monsterClient).initCollection(accountId);

        // when / then
        assertThatCode(() -> listener.onPlayerCreated(new PlayerCreatedEvent(accountId)))
                .doesNotThrowAnyException();
    }
}
