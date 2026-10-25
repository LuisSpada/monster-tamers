package com.monstertamers.auth.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.monstertamers.auth.internal.persistence.AccountRepository;
import com.monstertamers.auth.internal.web.dto.request.RegistrationRequest;
import com.monstertamers.shared.error.ApiException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    private static final RegistrationRequest REQUEST =
            new RegistrationRequest("  Tamer@Example.COM ", "secret123", "secret123");

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void checksEmailTrimmedAndLowerCased() {
        assertThat(registrationService.register(REQUEST).registrationId()).isNotNull();

        verify(accountRepository).existsByEmail("tamer@example.com");
    }

    @Test
    void rejectsTakenEmail() {
        when(accountRepository.existsByEmail("tamer@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(REQUEST))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getBody().getProperties()).containsEntry("code", "auth.email_taken");
                });
    }
}
