package com.monstertamers.auth.internal.application;

import com.monstertamers.auth.internal.domain.Account;
import com.monstertamers.auth.internal.persistence.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public void create(String email, String passwordHash) {
        Account account = new Account();
        account.setEmail(email);
        account.setPassword(passwordHash);
        Account saved = accountRepository.save(account);

        log.info("Account {} registered", saved.getId());
    }
}
