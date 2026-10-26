package com.monstertamers.auth.internal.application;

import com.monstertamers.auth.api.AccountCreated;
import com.monstertamers.auth.internal.domain.Account;
import com.monstertamers.auth.internal.persistence.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void create(String email, String passwordHash) {
        
        Account account = new Account();
        account.setEmail(email);
        account.setPassword(passwordHash);
        Account saved = accountRepository.save(account);

        eventPublisher.publishEvent(new AccountCreated(saved.getId(), saved.getEmail()));

        log.info("Account {} created", saved.getId());
    }
}
