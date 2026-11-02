package com.monstertamers.auth.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

import com.monstertamers.auth.error.ApiException;
import com.monstertamers.auth.internal.domain.Account;
import com.monstertamers.auth.internal.persistence.AccountRepository;
import com.monstertamers.auth.internal.web.dto.request.LoginRequest;
import com.monstertamers.auth.internal.web.dto.response.LoginResponse;
import java.util.Optional;
import java.util.UUID;
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
    private static final String PASSWORD_HASH = "password-hash";
    private static final String ACCESS_TOKEN = "access-token";
    private static final LoginRequest REQUEST = new LoginRequest(EMAIL, "secret123");

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AccessTokenIssuer accessTokenIssuer;

    @InjectMocks
    private LoginService loginService;

    private final Account account = account();

    @Test
    void unknownEmail_rejected() {
        // given
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // when
        Throwable thrown = catchThrowable(() -> loginService.login(REQUEST));

        // then
        assertInvalidCredentials(thrown);
    }

    @Test
    void wrongPassword_rejected() {
        // given
        givenAccountExists();
        givenPasswordMatches(false);

        // when
        Throwable thrown = catchThrowable(() -> loginService.login(REQUEST));

        // then
        assertInvalidCredentials(thrown);
    }

    @Test
    void validCredentials_issueAccessToken() {
        // given
        givenAccountExists();
        givenPasswordMatches(true);
        when(accessTokenIssuer.issue(account.getId())).thenReturn(ACCESS_TOKEN);

        // when
        LoginResponse response = loginService.login(REQUEST);

        // then
        assertThat(response.accessToken()).isEqualTo(ACCESS_TOKEN);
    }

    private static Account account() {
        Account account = new Account();
        account.setId(UUID.randomUUID());
        account.setEmail(EMAIL);
        account.setPassword(PASSWORD_HASH);
        return account;
    }

    private void givenAccountExists() {
        when(accountRepository.findByEmail(EMAIL)).thenReturn(Optional.of(account));
    }

    private void givenPasswordMatches(boolean matches) {
        when(passwordEncoder.matches(REQUEST.password(), PASSWORD_HASH)).thenReturn(matches);
    }

    private static void assertInvalidCredentials(Throwable thrown) {
        assertThat(thrown).isInstanceOfSatisfying(ApiException.class, ex -> {
            assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(ex.getBody().getProperties()).containsEntry("code", "auth.invalid_credentials");
        });
    }
}
