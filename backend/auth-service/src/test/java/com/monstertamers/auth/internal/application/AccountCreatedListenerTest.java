package com.monstertamers.auth.internal.application;

import static org.mockito.Mockito.verify;

import com.monstertamers.auth.api.AccountCreated;
import com.monstertamers.auth.internal.client.PlayerClient;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountCreatedListenerTest {

    @Mock
    private PlayerClient playerClient;

    @InjectMocks
    private AccountCreatedListener listener;

    @Test
    void accountCreated_callsPlayerClientWithAccountId() {
        UUID accountId = UUID.randomUUID();

        listener.onAccountCreated(new AccountCreated(accountId, "player@example.com"));

        verify(playerClient).createPlayer(accountId);
    }
}
