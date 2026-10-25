package com.monstertamers.auth.internal.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.monstertamers.auth.internal.application.mail.MailService;
import com.monstertamers.auth.internal.domain.RegistrationAttempt;
import com.monstertamers.auth.internal.persistence.AccountRepository;
import com.monstertamers.auth.internal.persistence.RegistrationAttemptRepository;
import com.monstertamers.auth.internal.web.dto.request.RegistrationRequest;
import com.monstertamers.shared.error.ApiException;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RegistrationServiceTest {

    private static final String EMAIL = "tamer@example.com";
    private static final RegistrationRequest REQUEST =
            new RegistrationRequest("  Tamer@Example.COM ", "secret123", "secret123");

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RegistrationAttemptRepository registrationAttemptRepository;

    @Mock
    private CodeGenerator codeGenerator;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private MailService mailService;

    @InjectMocks
    private RegistrationService registrationService;

    @Captor
    private ArgumentCaptor<RegistrationAttempt> savedAttempt;

    @BeforeEach
    void setUp() {
        when(codeGenerator.confirmationCode()).thenReturn("123456");
        when(passwordEncoder.encode("secret123")).thenReturn("password-hash");
        when(passwordEncoder.encode("123456")).thenReturn("code-hash");
    }

    @Test
    void rejectsTakenEmail() {
        when(accountRepository.existsByEmail(EMAIL)).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(REQUEST))
                .isInstanceOfSatisfying(ApiException.class, ex -> {
                    assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getBody().getProperties()).containsEntry("code", "auth.email_taken");
                });
        verifyNoInteractions(registrationAttemptRepository, mailService);
    }

    @Test
    void registersFreeEmail() {
        RegistrationAttempt saved = new RegistrationAttempt();
        saved.setId(UUID.randomUUID());
        when(registrationAttemptRepository.save(any())).thenReturn(saved);

        assertThat(registrationService.register(REQUEST).registrationId()).isEqualTo(saved.getId());

        InOrder order = inOrder(registrationAttemptRepository);
        order.verify(registrationAttemptRepository).deleteAllByEmail(EMAIL);
        order.verify(registrationAttemptRepository).save(savedAttempt.capture());
        RegistrationAttempt attempt = savedAttempt.getValue();
        assertThat(attempt.getId()).isNull();
        assertThat(attempt.getEmail()).isEqualTo(EMAIL);
        assertThat(attempt.getPassword()).isEqualTo("password-hash");
        assertThat(attempt.getCode()).isEqualTo("code-hash");
        verify(mailService).sendConfirmationCode(EMAIL, saved.getId(), "123456");
    }
}
