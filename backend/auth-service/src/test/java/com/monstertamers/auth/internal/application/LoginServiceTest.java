package com.monstertamers.auth.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

import com.monstertamers.auth.error.ApiException;
import com.monstertamers.auth.internal.domain.Account;
import com.monstertamers.auth.internal.persistence.AccountRepository;
import com.monstertamers.auth.internal.web.dto.request.LoginRequest;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final String EMAIL = "tamer@example.com";
    private static final LoginRequest REQUEST = new LoginRequest(EMAIL, "secret123");

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private LoginService loginService;

    @Test
    void unknownEmail_rejected() {
        // given
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> loginService.login(REQUEST));

        // then
        assertThat(thrown).isInstanceOfSatisfying(ApiException.class, ex -> {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(ex.getBody().getProperties()).containsEntry("code", "auth.invalid_credentials");
        });
    }

    @Test
    void wrongPassword_rejected() {
        // given
        Account account = new Account();
        account.setEmail(EMAIL);
        account.setPassword("password-hash");
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(account));
        when(passwordEncoder.matches(REQUEST.password(), account.getPassword())).thenReturn(false);

        // when
        Throwable thrown = catchThrowable(() -> loginService.login(REQUEST));

        // then
        assertThat(thrown).isInstanceOfSatisfying(ApiException.class, ex -> {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(ex.getBody().getProperties()).containsEntry("code", "auth.invalid_credentials");
        });
    }
}
