package com.monstertamers.auth.internal.application;

import com.monstertamers.auth.api.AccountCreated;
import com.monstertamers.auth.internal.client.PlayerClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountCreatedListener {

    private final PlayerClient playerClient;

    @EventListener
    public void onAccountCreated(AccountCreated event) {
        playerClient.createPlayer(event.accountId());
    }
}
