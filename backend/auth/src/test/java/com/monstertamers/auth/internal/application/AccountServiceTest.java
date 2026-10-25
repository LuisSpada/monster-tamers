package com.monstertamers.auth.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.monstertamers.auth.internal.domain.Account;
import com.monstertamers.auth.internal.persistence.AccountRepository;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final String EMAIL = "tamer@example.com";

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Captor
    private ArgumentCaptor<Account> savedAccount;

    @Test
    void create_succeeds() {
        // given
        Account saved = new Account();
        saved.setId(UUID.randomUUID());
        saved.setEmail(EMAIL);
        when(accountRepository.save(any())).thenReturn(saved);

        // when
        accountService.create(EMAIL, "password-hash");

        // then
        verify(accountRepository).save(savedAccount.capture());
        assertThat(savedAccount.getValue().getEmail()).isEqualTo(EMAIL);
        assertThat(savedAccount.getValue().getPassword()).isEqualTo("password-hash");
    }
}
